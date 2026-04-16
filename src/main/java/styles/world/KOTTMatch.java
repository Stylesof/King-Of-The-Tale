package styles.world;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.KOTT;
import styles.player.component.InvulnerabilityComponent;
import styles.team.KOTTTeam;
import styles.ui.KOTTEndUI;
import styles.ui.KOTTLoadingUI;
import styles.ui.KOTTPointsUI;
import styles.util.TeleportProvider;
import styles.util.ColorHandler;
import styles.util.MessageHandler;
import styles.util.StringGenerator;
import styles.util.MathHelper;
import styles.util.item.ItemTypes;
import styles.util.log.LogTypes;
import styles.world.match.KOTTScoreboard;
import styles.world.util.WorldBuilder;
import styles.world.zone.KOTTTeamZone;
import styles.world.zone.KOTTZone;
import styles.world.zone.MapMarkersHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import static styles.util.MessageHandler.*;

public class KOTTMatch {

    // [World Name] [KOTTMatch]
    private static final Map<String, KOTTMatch> matchesList = new HashMap<>();
    public static final long timeToPoint = 35000;

    public AtomicLong matchStartTimer = new AtomicLong(System.currentTimeMillis());
    public AtomicLong npcSpawnTimer = new AtomicLong(System.currentTimeMillis());

    public final int npcCounter = 10;

    private boolean KOTTMatchStatus = false;
    private boolean canMarkPoint = false;
    private boolean isLoop = false;
    private boolean isSafe = false;
    private final LinkedHashMap<UUID, KOTTTeam> Teams = new LinkedHashMap<>();
    private final Map<UUID, PlayerRef> playersInMatch = new HashMap<>();
    private World Lobby;
    private Vector3i matchStartPos = new Vector3i();
    private KOTTZone Zone;
    private Vector3i LobbyPos; // A 'home' to use when the match stops

    private CompletableFuture<Boolean> startMatchThread = CompletableFuture.runAsync(() -> {}).thenCompose(status -> CompletableFuture.completedFuture(true));
    private AtomicBoolean startMatchThreadSuccess = new AtomicBoolean(true);

    private PlayerRef host;
    private CommandContext commandContextHost;
    private World matchWorld;

    private KOTTScoreboard scoreBoard = new KOTTScoreboard();

    private boolean isEnding = false; // It's in process to end

    /*
        Create MATCH workflow:
            Validate Zone position
            Create Zone
            // get -> Valid zone position!

            Validate Team Base position
            // get -> Team Base in a secure place

            Create Teams
            // get -> Team successfully created!

            Clear Team Bases Area
            Build Team Bases
            // get -> Team bases successfully created!

            Add players to the teams
            Teleport player to the team base position (this is necessary to guarantee NPC spawn in team base)
            Set Player Respawn in base
            // get -> Player added to Team and send to the base

            Spawn Weapon Shop NPC in team bases
            // get -> NPC spawned

            Create UserMapMarkers for teams
            // get -> Team marker created

            Create ZoneMarker
            // get -> Zone and zone marker created!

            Match Successfully Created!
     */

    public static CompletableFuture<String> tryCreateMatch(@Nonnull Vector3i startPos, int teamCount, int zoneRadius, boolean safe, boolean loop, @Nullable PlayerRef playerRef, @Nullable CommandContext commandContext, @Nonnull World world, @Nonnull World lobbyWorld, @Nonnull Vector3i lobbyPos) {
        // Verify area size
        if (zoneRadius < 100 || zoneRadius > 500) {
            printChat(playerRef, LogTypes.KOTTInvalidAreaSize, java.awt.Color.red);
            return CompletableFuture.completedFuture(null);
        }

        // Verify number of teams
        if (teamCount < 1 || teamCount > 5) {
            printChat(playerRef, LogTypes.KOTTInvalidTeamCount, java.awt.Color.RED);
            return CompletableFuture.completedFuture(null);
        }

        String tempWorldName = world.getName();
        CompletableFuture<World> fun = CompletableFuture.completedFuture(world);
        // TODO: Safe, implementation

        boolean matchAdded = addMatch(tempWorldName);
        if (!matchAdded) {
            printChat(playerRef, LogTypes.KOTTMatchAlreadyRunning, java.awt.Color.RED);
            return CompletableFuture.completedFuture(null);
        }

        if (playerRef != null) {
            KOTTLoadingUI.loadHud(playerRef);
        }

        return fun.thenCompose(_world -> _world.getChunkAsync(startPos.x, startPos.z)
                .thenCompose(worldChunk -> {
                    KOTTMatch.getMatchesList().get(_world.getName()).start(
                            startPos,
                            teamCount,
                            zoneRadius,
                            safe,
                            loop,
                            playerRef,
                            commandContext,
                            _world,
                            lobbyWorld,
                            lobbyPos
                    );
                    return CompletableFuture.completedFuture(_world.getName());
                })
        ).thenApply(unused -> tempWorldName);
    }

    private CompletableFuture<Void> start(@Nonnull Vector3i startPos, int teamCount, int zoneRadius, boolean safe, boolean loop, @Nullable PlayerRef playerRef, @Nullable CommandContext commandContext, @Nonnull World world, @Nonnull World lobbyWorld, @Nonnull Vector3i lobbyPos) {
        this.matchWorld = world;
        this.matchStartPos = startPos;
        this.Lobby = lobbyWorld;
        this.LobbyPos = lobbyPos;
        this.isLoop = loop;
        this.isSafe = safe;
        if (playerRef != null) this.host = playerRef;
        if (commandContext != null) this.commandContextHost = commandContext;

        MessageHandler.printLog("Starting KOTT Match creation...");
        MessageHandler.printLog("Team Count: " + teamCount);

        Vector3i zonePosition = WorldBuilder.alignVectorToWorldSurface(startPos, world);
        if (zonePosition == null) {
            printChat(playerRef, Message.raw("Failed to create the Match! Invalid position!"));
            printLog("Cannot create the zone in the received position!" + startPos);
            stop(world.getName(), true);
            return CompletableFuture.completedFuture(null);
        }
        this.Zone = new KOTTZone(zoneRadius, zonePosition, world);

        printLog("Zone is in a safe place!");
        printLog("Zone position: " + zonePosition);

        // Using a pre-defined name template, get a list with random names
        List<String> nameList = StringGenerator.genRandomNameList(teamCount);
        List<Color> colorList = ColorHandler.genRandomColorList(teamCount);

        float angleBetweenBases = 360.0f / teamCount;
        float baseAngle = 0.0f;
        int distanceZoneBase = zoneRadius + KOTTTeamZone.baseRadius + KOTTTeam.distanceBaseFromZone;

        for (int i = 0; i < teamCount; i++) {

            Vector3i basePosition = new Vector3i(distanceZoneBase, 0, 0);
            basePosition = MathHelper.vectorSum(basePosition.toVector3d(), zonePosition.toVector3d()).ceil().toVector3i();

            if (i != 0) { // Calculates the others base position by angle
                baseAngle += angleBetweenBases;
                Vector3d other = MathHelper.convertAngleToUnitVector(baseAngle);
                other = MathHelper.scalarVector(other, distanceZoneBase);
                basePosition = MathHelper.vectorSum(other, zonePosition.toVector3d()).ceil().toVector3i();
            }

            basePosition = WorldBuilder.alignVectorToWorldSurface(basePosition, world); // get the highest point of the floor before sky
            if (basePosition == null) {
                printChat(commandContext, "Failed to validate Team Base position!");
                printLog("ERROR: Invalid Base position!");
                forceStop();
                return CompletableFuture.completedFuture(null);
            }

            basePosition.y += 1;

            printLog("Starting Team creation...");
            Color teamColor = colorList.get(i);
            ColorHandler.ColorType teamColorType = ColorHandler.getColorType(teamColor);
            if (!createTeam(UUID.randomUUID(), nameList.get(i), teamColor, basePosition, world)) {
                printNotification(
                        playerRef,
                        "Failed to create a team!",
                        "Failed to create the Team " + nameList.get(i) + ". More info in the logs!",
                        ItemTypes.MITHRIL_SWORD,
                        NotificationTypes.ERROR
                );
                printLog("ERROR: Failed to create the team: " + nameList.get(i));
                forceStop();
                return CompletableFuture.completedFuture(null);
            }

            KOTTTeam lastTeamAdded = getLastTeamAdded();
            printLog("Successfully created the Team " + lastTeamAdded.getDisplayName());

            Vector3i teamBaseSpawnPos = lastTeamAdded.getBaseZone().getPosition();
            startMatchThread = startMatchThread.thenCompose(status -> {
               if (!status) return CompletableFuture.completedFuture(false);
               return
               WorldBuilder.clearAreaSquare(new Vector3i(teamBaseSpawnPos.x, teamBaseSpawnPos.y - 1, teamBaseSpawnPos.z), 10, world).thenCompose(unused ->
                    WorldBuilder.constructTeamBase(new Vector3i(teamBaseSpawnPos), teamColorType, world).thenCompose(created ->{
                        if (created) {
                            printNotification(
                                    playerRef,
                                    "Created Team Base!",
                                    "Created an Team Base",
                                    ItemTypes.MITHRIL_SWORD,
                                    NotificationTypes.SUCCESS
                            );
                            MessageHandler.printLog("Created base of Team \"" + lastTeamAdded.getDisplayName() + "\". (X: " + teamBaseSpawnPos.x + ", Y: " + teamBaseSpawnPos.y + ", Z: " + teamBaseSpawnPos.z + ").");
                            return CompletableFuture.completedFuture(true);
                        }

                        printNotification(
                                playerRef,
                                "Failed to create Team Base!",
                                "",
                                ItemTypes.MITHRIL_SWORD,
                                NotificationTypes.ERROR
                        );
                        MessageHandler.printLog("ERROR: Failed to create the team base!", Level.SEVERE);
                        startMatchThreadSuccess.set(false);
                        forceStop();
                        return CompletableFuture.completedFuture(false);
                    })
               );
            });
        }

        startMatchThread = startMatchThread.thenCompose(status -> {
            if (!startMatchThreadSuccess.get()) {
                forceStop();
                return CompletableFuture.completedFuture(null);
            }

            setKOTTMatchStatus(true);

            printLog("Adding players to the Teams and sending to Base");
            for (PlayerRef _playerRef : this.getMatchWorld().getPlayerRefs()) {
                if (_playerRef == null || _playerRef.getReference() == null) {
                    printLog("Invalid player reference");
                    continue;
                }

                Player playerComp = this.getMatchWorld().getEntityStore().getStore().getComponent(_playerRef.getReference(), Player.getComponentType());

                boolean joinStatus = join(_playerRef);
                if (playerComp == null || !joinStatus) {
                    printLog("Failed to add the player: " + _playerRef.getUsername() + ", to a team");
                    continue;
                }
            }

            printLog("All players from the world has sent to base");

            return CompletableFuture.completedFuture(true);
        });

        startMatchThread = startMatchThread.thenCompose(status -> {
            if (!startMatchThreadSuccess.get()) {
                forceStop();
                return CompletableFuture.completedFuture(null);
            }

            for (KOTTTeam _team : this.getTeams()) {

                // Add Weapon Shop into Team Bases
                Vector3d spawnPos = _team.getBaseZone().getPosition().toVector3d();
                spawnPos.y += 1; // to npc don't clip to the ground
                spawnPos.x += 4.5d;
                spawnPos.z -= 3.5d;

                world.execute(() -> {
                    var npc = NPCPlugin.get().spawnNPC(
                            world.getEntityStore().getStore(),
                            "WeaponShop",
                            null,
                            spawnPos,
                            new Vector3f(0.0f, (float) Math.toRadians(135), 0.0f)
                    );

                    if (npc != null) {
                        PropComponent propComponent = this.getMatchWorld().getEntityStore().getStore().getComponent(npc.first(), PropComponent.getComponentType());
                        if (propComponent == null) {
                            this.getMatchWorld().getEntityStore().getStore().addComponent(npc.first(), PropComponent.getComponentType(), PropComponent.get());
                        }

                        printLog("Weapon Shop NPC spawn in the base of team: " + _team.getDisplayName());
                    } else {
                        printLog("Failed to Spawn the Weapon Shop NPC for the team: " + _team.getDisplayName());
                        startMatchThreadSuccess.set(false);
                    }
                });

                if (!startMatchThreadSuccess.get()) {
                    forceStop();
                    return CompletableFuture.completedFuture(null);
                }

                _team.init();
            }

            this.Zone.createUserMapMarker(
                    "Attack Zone",
                    new Color((byte) 255, (byte) 0, (byte) 0),
                    MapMarkersHandler.MarkerType.ATTACK
            );

            printLog("Match created and started on World: " + world.getName());
            printNotification(
                    playerRef,
                    "Match created!",
                    "Match created on World: " + world.getName() + "!",
                    ItemTypes.MITHRIL_SWORD,
                    NotificationTypes.SUCCESS
            );
            setCanMarkPoint(true);

            return CompletableFuture.completedFuture(true);
        });

        return startMatchThread.thenRun(() -> {});
    }

    private boolean createTeam(UUID id, String displayName, Color teamColor, Vector3i basePosition, @Nonnull World world) {
        if(this.Teams.containsKey(id)){
            printLog("Failed to create the Team! Duplicated Team UUID!");
            return false;
        }else{
            this.Teams.put(id, new KOTTTeam(id, displayName, teamColor, basePosition, world));
            return true;
        }
    }

    private static boolean addMatch(String worldName) {
        if (!KOTTMatch.getMatchesList().containsKey(worldName)) {
            KOTTMatch.getMatchesList().put(worldName, new KOTTMatch());
            return true;
        }else {
            if (!KOTTMatch.getMatchesList().get(worldName).getKOTTMatchStatus()) {
                KOTTMatch.getMatchesList().remove(worldName);
                KOTTMatch.getMatchesList().put(worldName, new KOTTMatch());
                return true;
            }
        }
        return false;
    }

    public boolean join(@Nonnull PlayerRef playerRef) {
        if (!getKOTTMatchStatus() && !getIsEnding()){
            printNotification(
                    playerRef,
                    "Failed to Join!",
                    "The match wasn't initialized",
                    ItemTypes.MITHRIL_SWORD,
                    NotificationTypes.WARNING
            );
            return false;
        }


        KOTTTeam chosenTeam = (KOTTTeam) Teams.values().toArray()[0];
        int playerCounter = Universe.get().getPlayerCount();
        for (KOTTTeam team : Teams.values()) {
            if (!team.getPlayerList().contains(playerRef)) {
                if (team.getPlayerCount() <= playerCounter) {
                    playerCounter = team.getPlayerCount();
                    chosenTeam = team;
                }
            } else {
                printNotification(
                        playerRef,
                        "Failed to Join!",
                        "Player is already in this match!",
                        ItemTypes.MITHRIL_SWORD,
                        NotificationTypes.ERROR
                );
                return false;
            }
        }

        addToMatch(playerRef, chosenTeam);

        TeleportProvider.TeleportPlayer(
                playerRef,
                chosenTeam.getBaseZone().getPosition().toVector3d(),
                this.getMatchWorld()
        );

        KOTTLoadingUI.unloadHud(playerRef);
        KOTTPointsUI.loadHud(playerRef, this);

        if (!this.getScoreBoard().getPlayersKillCount().containsKey(playerRef)) {
            this.getScoreBoard().getPlayersKillCount().put(playerRef, 0);
        }
        if (!this.getScoreBoard().getPlayersDeathCount().containsKey(playerRef)) {
            this.getScoreBoard().getPlayersDeathCount().put(playerRef, 0);
        }

        printNotification(
                playerRef,
                "You joined the Team " + chosenTeam.getDisplayName() + "!",
                "",
                ItemTypes.MITHRIL_SWORD,
                NotificationTypes.SUCCESS
        );
        printLog("Player " + playerRef.getUsername() + " has joined into the Team " + chosenTeam.getDisplayName());

        return true;
    }

    public void leave(@Nonnull PlayerRef playerRef) { leave(playerRef, null); }
    public void leave(@Nonnull PlayerRef playerRef, @Nullable Holder<EntityStore> holder) {
        // if has an holder, it's probably the player haven't been started yet, like in AddPlayerWorldEvent, or something like it
        // for it, it's necessary another way to handle it
        removeFromMatch(playerRef);

        if (holder == null) {
            assert playerRef.getWorldUuid() != null;
            World world = Universe.get().getWorld(playerRef.getWorldUuid());

            assert world != null;
            KOTTPointsUI.unloadHud(playerRef, world);

            world.execute(() -> {
                if (world.getEntityStore().getStore().getComponent(Objects.requireNonNull(playerRef.getReference()), InvulnerabilityComponent.getComponentType()) != null) {
                    world.getEntityStore().getStore().removeComponent(playerRef.getReference(), InvulnerabilityComponent.getComponentType());
                }
            });

            TeleportProvider.TeleportPlayer(playerRef, this.LobbyPos.toVector3d(), this.Lobby);

        } else {
            KOTTPointsUI.unloadHud(playerRef, holder);
            if (holder.getComponent(InvulnerabilityComponent.getComponentType()) != null) {
                holder.removeComponent(InvulnerabilityComponent.getComponentType());
            }
        }

        printLog("Player " + playerRef.getUsername() + ". Leaved the match on world: " + getMatchWorld().getName() + "!");
    }

    // MATCH STOP
    private CompletableFuture<Void> forceStop() {
        if (!this.startMatchThread.isCancelled()) {
            this.startMatchThread.cancel(true);
        }

        return stop(this.getMatchWorld().getName(), true, null, true);
    }

    public static CompletableFuture<Void> stop(@Nonnull String worldName, boolean forceStop) { return stop(worldName, forceStop, null, false); }
    public static CompletableFuture<Void> stop(@Nonnull String worldName, boolean forceStop, @Nullable CommandContext commandContext) { return stop(worldName, forceStop, commandContext, false); }
    public static CompletableFuture<Void> stop(@Nonnull String worldName, boolean forceStop, @Nullable CommandContext commandContext, boolean serverStop) {
        MessageHandler.printLog("Stopping match...");
        World world = Universe.get().getWorld(worldName);
        if (world == null) {
            if (commandContext == null || !commandContext.isPlayer()) {
                MessageHandler.printLog("You need to specify an world via --world parameter!", Level.WARNING);
                return CompletableFuture.completedFuture(null);
            }
            MessageHandler.printLog("Trying to stop a match on a invalid world!", Level.WARNING);
            printChat(commandContext, "Invalid World!");
            return CompletableFuture.completedFuture(null);
        }

        if (!KOTTMatch.getMatchesList().containsKey(worldName)) {
            MessageHandler.printLog("There isn't a match happening in this world!", Level.WARNING);
            printChat(commandContext, "There isn't a match happening in this world!");
            return CompletableFuture.completedFuture(null);
        }
        KOTTMatch match = KOTTMatch.getMatchesList().get(worldName);
        if (match == null || (!match.getKOTTMatchStatus() && serverStop)) {
            printChat(commandContext, "The match wasn't started yet!");
            return CompletableFuture.completedFuture(null);
        }

        UserMapMarkersStore store = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());

        for (KOTTTeam team : match.getTeams()) {
            if (team.getBaseZone().getZoneMarker() != null) {
                printLog(match.Zone.getWorld().getName() + ": Removing UserMapMarker from Team: " + team.getDisplayName() + "...");
                store.removeUserMapMarker(team.getBaseZone().getZoneMarker().getId());
            }

            team.destroyTeamBase();
        }

        printLog("Removing bots from match...");
        world.execute(() -> world.getEntityStore().getStore().forEachChunk(NPCEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                NPCEntity npc = archetypeChunk.getComponent(index, Objects.requireNonNull(NPCEntity.getComponentType()));

                if (npc != null) {
                    if (npc.getNPCTypeId().equals("FighterNPC") || npc.getNPCTypeId().equals("WeaponShop")) {
                        world.execute(() -> {
                            world.getEntityStore().getStore().removeEntity(Objects.requireNonNull(npc.getReference()), RemoveReason.REMOVE);
                        });
                    }
               }
            }
        }));
        printLog("Removed bots from match!");

        printLog(match.getMatchWorld().getName() + ": Removing UserMapMarker from Zone...");
        store.removeUserMapMarker(match.Zone.getZoneMarker().getId());
        printLog(match.getMatchWorld().getName() + ": Removed all UserMapMakers on " + match.getZone().getWorld().getName());

        printLog("Returning player to lobby...");
        for (PlayerRef playerRef : match.getPlayersInMatch().values()) {
            if (playerRef != null && playerRef.getReference() != null) {
                world.execute(() -> {
                    Player player = world.getEntityStore().getStore().getComponent(playerRef.getReference(), Player.getComponentType());

                    if (player != null) {
                        if (world.getEntityStore().getStore().getComponent(playerRef.getReference(), InvulnerabilityComponent.getComponentType()) != null) {
                            world.getEntityStore().getStore().removeComponent(playerRef.getReference(), InvulnerabilityComponent.getComponentType());
                        }
                    }
                });

                match.leave(playerRef);
            }
        }
        printLog("All players from match has been returned to the lobby!");

        Vector3i finalWordPos = match.matchStartPos;
        int finalTeamCount = match.getTeams().size();
        int finalZoneRadius = match.Zone.getZoneRadius();
        boolean finalZoneLoop = match.isLoop && !forceStop;
        boolean finalZoneSafe = match.isSafe;
        PlayerRef finalHost = match.host;
        CommandContext finalCommandCtxHost = match.commandContextHost;
        World finalLobbyWorld = match.Lobby;
        Vector3i finalLobbyPos = match.LobbyPos;

        match.startMatchThread.cancel(true);

        // match.getTeams().clear();
        match.Zone = null;
        match.setKOTTMatchStatus(false);
        match.setCanMarkPoint(false);

        KOTTMatch.getMatchesList().remove(worldName);
        printChat(commandContext, "Stopped the active KOTT match!");

        // NEW LOOP SYSTEM
        // IF NO SAFE IT WILL GET RANDOM POS IN THE WORLD TO RE-START THE MATCH
        return CompletableFuture.runAsync(() -> {
           if (finalZoneLoop) {
               Random random = new Random();
               finalWordPos.x += random.nextInt(10000);
               finalWordPos.y += random.nextInt(10000);
               finalWordPos.z += random.nextInt(10000);

               KOTTMatch.tryCreateMatch(
                   finalWordPos,
                   finalTeamCount,
                   finalZoneRadius,
                   finalZoneSafe,
                   true,
                   finalHost,
                   finalCommandCtxHost,
                   world,
                   finalLobbyWorld,
                   finalLobbyPos
               );
           }
        });
    }

    public CompletableFuture<Void> end(KOTTTeam winnerTeam) {
        setCanMarkPoint(false);
        setKOTTMatchStatus(false);
        isEnding = true;

        CompletableFuture<Boolean> fun = CompletableFuture.completedFuture(true);
        for (int i = 0; i <= 20; i++) {
            int finalI = i;
            fun = fun.thenCompose((var) -> {
                if (!var) return CompletableFuture.completedFuture(false);

                for (PlayerRef playerRef : getPlayersInMatch().values()) {
                    if (this.matchWorld != null && playerRef.getReference() != null && getMatchesList().containsKey(this.matchWorld.getName())) {
                        this.matchWorld.execute(() -> {
                            Player player = this.matchWorld.getEntityStore().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                            if (player != null) {
                                player.getHudManager().resetHud(playerRef);
                                player.getHudManager().setCustomHud(playerRef, new KOTTEndUI(playerRef, winnerTeam, 20 - finalI, this.isLoop));
                            }
                        });
                    } else {
                        return CompletableFuture.completedFuture(false);
                    }
                }
                return CompletableFuture.completedFuture(true);
            }).thenCompose((var) -> CompletableFuture.supplyAsync(() -> var, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS)));
        }

        return fun.thenCompose((var) -> {
            if (var)
                stop(this.matchWorld.getName(), false);
            return CompletableFuture.completedFuture(null);
        });
    }

    private void addToMatch(@Nonnull PlayerRef playerRef, KOTTTeam team) {
        this.getPlayersInMatch().put(playerRef.getUuid(), playerRef);
        this.getZone().addToZone(playerRef);
        team.addPlayer(playerRef);
    }

    private void removeFromMatch(@Nonnull PlayerRef playerRef) {
        KOTTTeam team = this.getPlayerTeam(playerRef);
        if (team != null) {
            this.getPlayersInMatch().remove(playerRef.getUuid());
            this.getZone().removeFromZone(playerRef);
            team.removeFromTeam(playerRef);
        }
    }

    public boolean getKOTTMatchStatus() { return KOTTMatchStatus; }

    public boolean getCanMarkPoint () { return  this.canMarkPoint; }

    public boolean getIsEnding() { return this.isEnding; }

    public List<KOTTTeam> getTeams(){ return this.Teams.values().stream().toList(); }

    private KOTTTeam getLastTeamAdded() { return this.Teams.lastEntry().getValue(); }

    @Nullable
    public KOTTTeam getPlayerTeam(PlayerRef playerRef) {
        for (KOTTTeam team : this.Teams.values()) {
            if (team.containsPlayer(playerRef)) {
                return team;
            }
        }

        return null;
    }

    public Map<UUID, PlayerRef> getPlayersInMatch() { return this.playersInMatch; }

    public KOTTZone getZone() { return this.Zone; }

    @Nonnull
    public World getMatchWorld() { return this.matchWorld; }

    public static Map<String, KOTTMatch> getMatchesList() { return matchesList; }

    @Nullable
    public static KOTTMatch getMatch(String worldName) { return getMatchesList().get(worldName); }

    public KOTTScoreboard getScoreBoard() { return this.scoreBoard; }

    public void setCanMarkPoint (boolean state) { this.canMarkPoint = state; }

    public void setKOTTMatchStatus(boolean state) {
        KOTTMatchStatus = state;
    }
}
