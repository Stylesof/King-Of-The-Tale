package styles.world;

import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.modules.entity.component.NewSpawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.player.component.InvulnerabilityComponent;
import styles.team.KOTTTeam;
import styles.ui.KOTTEndUI;
import styles.ui.KOTTPointsUI;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    private PlayerRef host;
    private CommandContext commandContextHost;
    private World matchWorld;

    private KOTTScoreboard scoreBoard = new KOTTScoreboard();

    private boolean isEnding = false; // It's in process to end

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

        UserMapMarkersStore userMapMarkerStore = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());
        UserMapMarker zoneMarker = new UserMapMarker();
            zoneMarker.setId(UUID.randomUUID().toString());
            zoneMarker.setPosition(startPos.x, startPos.z);
            zoneMarker.setName("Attack Zone");
            zoneMarker.setIcon("UserF.png");
            zoneMarker.setColorTint(new Color((byte) 255, (byte) 0, (byte) 0));
        Zone = new KOTTZone(zoneRadius, startPos, world, zoneMarker);
        userMapMarkerStore.addUserMapMarker(Zone.getZoneMarker());

        float angleBetweenBases = 360.0f / teamCount;
        float startAngle = 0.0f;
        int distanceZoneBase = zoneRadius + KOTTTeamZone.baseRadius + KOTTTeam.distanceBaseFromZone;

        // Calculates the first base position
        Vector3i baseLocation = new Vector3i(distanceZoneBase, 0, 0);
        baseLocation = MathHelper.vectorSum(baseLocation.toVector3d(), Zone.getPosition().toVector3d()).ceil().toVector3i();

        // Using a pre-defined name template, get a list with random names
        List<String> nameList = StringGenerator.genRandomNameList(teamCount);
        List<Color> colorList = ColorHandler.genRandomColorList(teamCount);

        CompletableFuture<Boolean> fun = CompletableFuture.completedFuture(true);

        int i = 0;
        while (i < teamCount) {
            baseLocation = WorldBuilder.alignVectorToWorldSurface(baseLocation, world); // get the highest position of floor before sky
            if (baseLocation == null) {
                printChat(commandContext, "Failed to define Base Position, try in another place!");
                MessageHandler.printLog("ERROR: Invalid Base position!");
                stop(world.getName());
                return CompletableFuture.completedFuture(null);
            }
            baseLocation.y += 1;

            UserMapMarker zoneMarker2 = new UserMapMarker();
                zoneMarker2.setId(UUID.randomUUID().toString());
                zoneMarker2.setPosition(baseLocation.x, baseLocation.z);
                zoneMarker2.setName("Team " + nameList.get(i) + " Base");
                zoneMarker2.setIcon("UserD.png");
                zoneMarker2.setColorTint(new Color(colorList.get(i).red, colorList.get(i).green, colorList.get(i).blue));

            // Create %teamCount% teams
            MessageHandler.printLog("Starting Team creation...");
            if (KOTTTeam.createTeam(Teams, UUID.randomUUID(), nameList.get(i), baseLocation, world, zoneMarker2)) {

                userMapMarkerStore.addUserMapMarker(getLastTeamAdded().getBaseZone().getZoneMarker());

                if (i < teamCount - 1) {
                    startAngle += angleBetweenBases;
                    Vector3d other = MathHelper.convertAngleToUnitVector(startAngle);
                    other = MathHelper.scalarVector(other, distanceZoneBase);
                    baseLocation = MathHelper.vectorSum(other, Zone.getPosition().toVector3d()).ceil().toVector3i();
                }

                // Get the ColorType from a color Team
                ColorHandler.ColorType teamColorType = ColorHandler.getColorType(colorList.get(i));

                // Clear area and construct base
                Vector3i basePos = getLastTeamAdded().getBaseZone().getPosition();
                basePos.y--; // the base spawn inside ground

                KOTTTeam team = getLastTeamAdded();
                fun = fun.thenCompose(status -> {
                    if (!status) return CompletableFuture.completedFuture(false);

                    return WorldBuilder.clearAreaSquare(basePos, 10, world).thenCompose(unused -> {
                        basePos.y++;
                        return WorldBuilder.constructTeamBase(basePos, teamColorType, world).thenCompose(status2 -> {
                            if(!status2) {
                                printNotification(
                                        playerRef,
                                        "Failed to create Team Base!",
                                        "",
                                        ItemTypes.MITHRIL_SWORD,
                                        NotificationTypes.ERROR
                                );
                                MessageHandler.printLog("ERROR: Failed to create the team base!", Level.SEVERE);
                                stop(world.getName());
                                return CompletableFuture.completedFuture(false);
                            }

                            printNotification(
                                    playerRef,
                                    "Created Team Base!",
                                    "Created an Team Base)",
                                    ItemTypes.MITHRIL_SWORD,
                                    NotificationTypes.SUCCESS
                            );
                            MessageHandler.printLog("Created base of Team \"" + team.getDisplayName() + "\". (X: " + basePos.x + ", Y: " + basePos.y + ", Z: " + basePos.z);

                            return CompletableFuture.completedFuture(true);
                        });
                    });
                });

                i++;
            } else {
                printNotification(
                        playerRef,
                        "Failed to create team!",
                        "Failed to create the Team " + nameList.get(i),
                        ItemTypes.MITHRIL_SWORD,
                        NotificationTypes.ERROR
                );
                MessageHandler.printLog("ERROR: Failed to create the team: " + nameList.get(i));
                stop(world.getName());
                return CompletableFuture.completedFuture(null);
            }
        }

        return fun.thenCompose(status -> {
            if (status) {
                setKOTTMatchStatus(true);

                MessageHandler.printLog("Finished KOTT Match creation! World name: " + world.getName());
                printNotification(
                        playerRef,
                        "Sending players to the base...",
                        "",
                        ItemTypes.MITHRIL_SWORD,
                        NotificationTypes.WARNING
                );

                for (PlayerRef _playerRef : world.getPlayerRefs()) {
                    if (_playerRef.getReference() == null) continue;

                    join(_playerRef);
                    KOTTTeam team = getPlayerTeam(_playerRef);
                    if (team == null || team.getBaseZone() == null) {
                        MessageHandler.printLog("Invalid Team!");
                        continue;
                    }

                    Transform transform = _playerRef.getTransform();
                    transform.setPosition(team.getBaseZone().getPosition().toVector3d());
                    Teleport tp = Teleport.createForPlayer(transform);
                    world.getEntityStore().getStore().addComponent(_playerRef.getReference(), Teleport.getComponentType(), tp);
                    world.getEntityStore().getStore().getComponent(_playerRef.getReference(), NewSpawnComponent.getComponentType());

                    Vector3i pos = team.getBaseZone().getPosition();

                    Player player = world.getEntityStore().getStore().getComponent(_playerRef.getReference(), Player.getComponentType());
                    if (player == null){
                        MessageHandler.printLog("Invalid Player!");
                        continue;
                    }


                    world.execute(() -> {
                        PlayerRespawnPointData[] respawnPointData = { new PlayerRespawnPointData(pos, pos.toVector3d(), "Team " + team.getDisplayName() + " Respawn") };
                        player.getPlayerConfigData().getPerWorldData(world.getName()).setRespawnPoints(respawnPointData);
                        KOTTPointsUI newHud = new KOTTPointsUI(_playerRef, this);
                        player.getHudManager().setCustomHud(_playerRef, newHud);
                        newHud.initializeThreadUpdate();
                        if (world.getEntityStore().getStore().getComponent(_playerRef.getReference(), InvulnerabilityComponent.getComponentType()) == null) {
                            world.getEntityStore().getStore().addComponent(_playerRef.getReference(), InvulnerabilityComponent.getComponentType(), new InvulnerabilityComponent());
                        }
                    });
                }

                for (KOTTTeam team : this.getTeams()) {
                    // Add Weapon Shop into Team Bases
                    Vector3d spawnPos = team.getBaseZone().getPosition().toVector3d();
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
                            PropComponent propComponent = world.getEntityStore().getStore().getComponent(npc.first(), PropComponent.getComponentType());
                            if (propComponent == null) {
                                world.getEntityStore().getStore().addComponent(npc.first(), PropComponent.getComponentType(), PropComponent.get());
                            }
                        }
                    });
                }

                MessageHandler.printLog("Match created and started on World: " + world.getName());
                printNotification(
                            playerRef,
                            "Match created!",
                            "Match created on World: " + world.getName() + "!",
                            ItemTypes.MITHRIL_SWORD,
                            NotificationTypes.SUCCESS
                );
                setCanMarkPoint(true);
            } else {
                MessageHandler.printLog("ERROR: Failed to create and start the match on World: " + world.getName());
                printNotification(
                        playerRef,
                        "Failed to create Match!",
                        "Couldn't create and start the match!",
                        ItemTypes.MITHRIL_SWORD,
                        NotificationTypes.SUCCESS
                );

                stop(world.getName(), true);
            }

            return CompletableFuture.completedFuture(true);
        }).thenRun(() -> {});
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

    public void join(@Nonnull PlayerRef playerRef) {
        if (!getKOTTMatchStatus() && !getIsEnding()){
            printNotification(
                    playerRef,
                    "Failed to Join!",
                    "The match wasn't initialized",
                    ItemTypes.MITHRIL_SWORD,
                    NotificationTypes.WARNING
            );
            return;
        }

        removeFromMatch(playerRef);

        KOTTTeam chosenTeam = (KOTTTeam) Teams.values().toArray()[0];
        int playerCounter = Universe.get().getPlayerCount();
        for (KOTTTeam team : Teams.values()) {
            if (!team.getPlayerList().contains(playerRef)) {
                if (team.getPlayerCount() < playerCounter) {
                    playerCounter = team.getPlayerCount();
                    chosenTeam = team;
                }
            } else {
                addToMatch(playerRef, team);
                printNotification(
                        playerRef,
                        "Failed to Join!",
                        "The player is already in a Team",
                        ItemTypes.MITHRIL_SWORD,
                        NotificationTypes.ERROR
                );
                return;
            }
        }

        addToMatch(playerRef, chosenTeam);

        Teleport tp = Teleport.createForPlayer(chosenTeam.getBaseZone().getWorld(), new Transform(chosenTeam.getBaseZone().getPosition().toVector3d()));
        if (playerRef.getWorldUuid() != null) {
            Objects.requireNonNull(Universe.get().getWorld(playerRef.getWorldUuid()))
                    .getEntityStore()
                    .getStore()
                    .addComponent(
                            Objects.requireNonNull(playerRef.getReference()),
                            Teleport.getComponentType(),
                            tp
                    );
        }

        printNotification(
                playerRef,
                "You joined the Team " + chosenTeam.getDisplayName() + "!",
                "",
                ItemTypes.MITHRIL_SWORD,
                NotificationTypes.SUCCESS
        );
        printLog("Player " + playerRef.getUsername() + " has joined into the Team " + chosenTeam.getDisplayName());

        if (!this.scoreBoard.getPlayersKillCount().containsKey(playerRef)) {
            this.scoreBoard.getPlayersKillCount().put(playerRef, 0);
        }
        if (!this.scoreBoard.getPlayersDeathCount().containsKey(playerRef)) {
            this.scoreBoard.getPlayersDeathCount().put(playerRef, 0);
        }
    }

    // MATCH STOP
    public static CompletableFuture<Void> stop(@Nonnull String worldName) { return stop(worldName, false, null, false); }
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
                MessageHandler.printLog(match.Zone.getWorld().getName() + ": Removing UserMapMarker from Team: " + team.getDisplayName() + "...");
                store.removeUserMapMarker(team.getBaseZone().getZoneMarker().getId());
            }

            team.destroyTeamBase();
        }

        printLog("Removing players CustomHUDs...");
        for (PlayerRef playerRef : match.getZone().getPlayersInZone()) {
            if (playerRef != null && playerRef.getReference() != null) {
                world.execute(() -> {
                    Player player = world.getEntityStore().getStore().getComponent(playerRef.getReference(), Player.getComponentType());

                    if (player != null) {
                        player.getHudManager().resetHud(playerRef);

                        if (world.getEntityStore().getStore().getComponent(playerRef.getReference(), InvulnerabilityComponent.getComponentType()) != null) {
                            world.getEntityStore().getStore().removeComponent(playerRef.getReference(), InvulnerabilityComponent.getComponentType());
                        }
                    }
                });
            }
        }
        printLog("Removed players CustomHUDs!");

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

        MessageHandler.printLog(match.Zone.getWorld().getName() + ": Removing UserMapMarker from Zone...");
        store.removeUserMapMarker(match.Zone.getZoneMarker().getId());
        MessageHandler.printLog(match.Zone.getWorld().getName() + ": Removed all UserMapMakers on " + match.getZone().getWorld().getName());

        Vector3i finalWordPos = match.matchStartPos;
        int finalTeamCount = match.getTeams().size();
        int finalZoneRadius = match.Zone.getZoneRadius();
        boolean finalZoneLoop = match.isLoop && !forceStop;
        boolean finalZoneSafe = match.isSafe;
        PlayerRef finalHost = match.host;
        CommandContext finalCommandCtxHost = match.commandContextHost;
        World finalLobbyWorld = match.Lobby;
        Vector3i finalLobbyPos = match.LobbyPos;

        match.Teams.clear();
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
        this.playersInMatch.put(playerRef.getUuid(), playerRef);
        this.Zone.getPlayersInZone().add(playerRef);
        team.addPlayerRef(playerRef);
    }

    private static void removeFromMatch(PlayerRef playerRef) {
        for (KOTTMatch match : KOTTMatch.getMatchesList().values()) {
            if (match.getPlayersInMatch().containsValue(playerRef)) {
                for (KOTTTeam team : match.Teams.values()) {
                    if (team.containsPlayer(playerRef)) {
                        match.playersInMatch.remove(playerRef.getUuid());
                        match.Zone.removeFromZone(playerRef);
                        team.removeFromTeam(playerRef);
                        return;
                    }
                }
            }
        }
    }

    public boolean getKOTTMatchStatus() { return KOTTMatchStatus; }

    public boolean getCanMarkPoint () { return  this.canMarkPoint; }

    public boolean getIsEnding() { return this.isEnding; }

    public List<KOTTTeam> getTeams(){ return this.Teams.values().stream().toList(); }

    private KOTTTeam getLastTeamAdded() { return this.Teams.lastEntry().getValue(); }

    @Nullable
    public KOTTTeam getPlayerTeam(PlayerRef playerRef) {
        if (getKOTTMatchStatus()) {
            for (KOTTTeam team : this.Teams.values()) {
                if (team.containsPlayer(playerRef)) {
                    return team;
                }
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
