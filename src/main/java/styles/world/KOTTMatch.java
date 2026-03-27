package styles.world;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import styles.team.KOTTTeam;
import styles.util.ColorHandler;
import styles.util.StringGenerator;
import styles.util.MathHelper;
import styles.util.log.LogTypes;
import styles.world.util.WorldBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;
import static styles.util.log.PrintLog.printLog;

public class KOTTMatch {

    // [World Name] [KOTTMatch]
    private static final Map<String, KOTTMatch> matchesList = new HashMap<>();
    private boolean KOTTMatchStatus = false;
    private boolean isLoop = false;
    private boolean isSafe = false;
    private final Map<UUID, PlayerRef> playersInMatch = new HashMap<>();
    private final LinkedHashMap<UUID, KOTTTeam> Teams = new LinkedHashMap<>();
    private Vector3i matchStartPos = new Vector3i();
    private KOTTZone Zone;
    private World Lobby;
    private Vector3i LobbyPos;

    public static final long timeToPoint = 45000;

    public static boolean addMatch(String worldName) {
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

    public static CompletableFuture<String> tryCreateMatch(@Nonnull Vector3i startPos, int teamCount, int zoneRadius, boolean safe, boolean loop, @Nullable PlayerRef playerRef, @Nullable CommandContext commandContext, @Nonnull World world, @Nonnull World lobbyWorld, @Nonnull Vector3i lobbyPos) {
        // Verify area size
        if (zoneRadius < 100 || zoneRadius > 500) {
            printLog(playerRef, LogTypes.KOTTInvalidAreaSize);
            return CompletableFuture.completedFuture(null);
        }

        // Verify number of teams
        if (teamCount < 1 || teamCount > 5) {
            printLog(playerRef, LogTypes.KOTTInvalidTeamCount);
            return CompletableFuture.completedFuture(null);
        }

        String tempWorldName = world.getName();

        CompletableFuture<World> fun;
        if (safe) {
            UUID uuid = UUID.randomUUID();
            tempWorldName = String.format("%.13s", "temp_" + uuid);
            fun = Universe.get().addWorld(tempWorldName);
        } else {
            fun = CompletableFuture.completedFuture(world);
        }

        boolean matchAdded = addMatch(tempWorldName);
        if (!matchAdded) {
            printLog(playerRef, LogTypes.KOTTMatchAlreadyRunning, "World name: \"" + tempWorldName + "\"!");
            return CompletableFuture.completedFuture(null);
        }

        String finalTempWorldName = tempWorldName;
        return fun.thenCompose(world1 -> world1.getChunkAsync(startPos.x, startPos.z)
                .thenCompose(worldChunk -> {
                    KOTTMatch.getMatchesList().get(world1.getName()).start(
                            startPos,
                            teamCount,
                            zoneRadius,
                            safe,
                            loop,
                            playerRef,
                            commandContext,
                            world1,
                            lobbyWorld,
                            lobbyPos
                    );
                    return CompletableFuture.completedFuture(world1.getName());
                })
        ).thenApply(unused -> finalTempWorldName);
    }

    private CompletableFuture<Void> start(@Nonnull Vector3i startPos, int teamCount, int zoneRadius, boolean safe, boolean loop, @Nullable PlayerRef playerRef, @Nullable CommandContext commandContext, @Nonnull World world, @Nonnull World lobbyWorld, @Nonnull Vector3i lobbyPos) {
        this.Lobby = lobbyWorld;
        this.LobbyPos = lobbyPos;
        this.isLoop = loop;
        this.isSafe = safe;

        this.matchStartPos = startPos;

        printL("Starting KOTT Match creation...");
        printL("Team Count: " + teamCount);

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
        int distanceZoneBase = zoneRadius * 2 + KOTTTeam.distanceBaseFromZone;

        // Calculates the first base position
        Vector3i baseLocation = new Vector3i(distanceZoneBase, 0, 0);
        baseLocation = MathHelper.vectorSum(baseLocation.toVector3d(), Zone.getPosition().toVector3d()).ceil().toVector3i();

        // Using a pre-defined name template, get an list with random names
        List<String> nameList = StringGenerator.genRandomNameList(teamCount);
        List<Color> colorList = ColorHandler.genRandomColorList(teamCount);

        CompletableFuture<Boolean> fun = CompletableFuture.completedFuture(true);

        int i = 0;
        while (i < teamCount) {
            baseLocation = WorldBuilder.alignVectorToWorldSurface(baseLocation, world); // get the highest position of floor before sky
            if (baseLocation == null) {
                print(commandContext, "Failed to define Base Position, try in another place!");
                printL("ERROR: Invalid Base position!");
                stop(world.getName());
                return CompletableFuture.completedFuture(null);
            }
            baseLocation.y += 1;

            UserMapMarker zoneMarker2 = new UserMapMarker();
                zoneMarker2.setId(UUID.randomUUID().toString());
                zoneMarker2.setPosition(baseLocation.x, baseLocation.z);
                zoneMarker2.setName("Team " + nameList.get(i) + " Base");
                zoneMarker2.setIcon("UserD.png");
                zoneMarker2.setColorTint(colorList.get(i));

            // Create %teamCount% teams
            printL("Starting Team creation...");
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
                if (teamColorType == null) {
                    print(playerRef, "Failed to find team colorType!");
                    printL("Failed to find team colorType!");
                    stop(world.getName());
                    return CompletableFuture.completedFuture(null);
                }

                // Clear area and construct base
                Vector3i basePos = getLastTeamAdded().getBaseZone().getPosition();
                basePos.y--;
                KOTTTeam team = getLastTeamAdded();
                fun = fun.thenCompose(status -> {
                    if (!status) return CompletableFuture.completedFuture(false);

                    return WorldBuilder.clearAreaSquare(basePos, 10, world).thenCompose(unused -> {
                        basePos.y++;
                        return WorldBuilder.constructTeamBase(basePos, teamColorType, world).thenCompose(status2 -> {
                            if(!status2) {
                                print(playerRef, "Failed to create the team base!");
                                printL("Error: Failed to create the team base!", Level.SEVERE);
                                stop(world.getName());
                                return CompletableFuture.completedFuture(false);
                            }

                            print(playerRef, "Created base of Team \"" + team.getDisplayName() + "\". (X: " + basePos.x + ", Y: " + basePos.y + ", Z: " + basePos.z + ")");
                            printL("Created base of Team \"" + team.getDisplayName() + "\". (X: " + basePos.x + ", Y: " + basePos.y + ", Z: " + basePos.z + ")");
                            return CompletableFuture.completedFuture(true);
                        });
                    });
                });

                i++;
            } else {
                print(commandContext, "[KOTT] Failed to create the team: " + nameList.get(i));
                printL("Error: Failed to create the team: " + nameList.get(i));
                stop(world.getName());
                return CompletableFuture.completedFuture(null);
            }
        }

        return fun.thenCompose(status -> {
            printL("Finished KOTT Match creation! World name: " + world.getName());
            if (status) {
                print(playerRef, "Sending players to the base...");

                int j = 0;
                for (PlayerRef _playerRef : world.getPlayerRefs()) {
                    if (_playerRef.getReference() == null) continue;

                    KOTTTeam team = (KOTTTeam) Teams.values().toArray()[j++];
                    team.addPlayerRef(playerRef);
                    this.playersInMatch.put(_playerRef.getUuid(), _playerRef);

                    Transform transform = _playerRef.getTransform();
                    transform.setPosition(team.getBaseZone().getPosition().toVector3d());
                    Teleport tp = Teleport.createForPlayer(transform);
                    world.getEntityStore().getStore().addComponent(_playerRef.getReference(), Teleport.getComponentType(), tp);

                    print(_playerRef, "[KOTT] You are from the Team " + team.getDisplayName());
                }

                print(playerRef, "[KOTT] Match created and started on World: " + world.getName());
                printL("[KOTT Debug] Match created and started on World: " + world.getName());
                setKOTTMatchStatus(true);
            } else {
                print(playerRef, "Failed to create and start the match on World: " + world.getName());
                printL("Error: Failed to create and start the match on World: " + world.getName());
                stop(world.getName(), true);
            }

            return CompletableFuture.completedFuture(true);
        }).thenRun(() -> {});
    }

    public void join(@Nonnull PlayerRef playerRef) {
        if (!getKOTTMatchStatus()){
            print(playerRef, "The match was not initialized");
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
                print(playerRef, "The player is already on a Team!");
                return;
            }
        }

        addToMatch(playerRef, chosenTeam);

        Teleport tp = Teleport.createForPlayer(chosenTeam.getBaseZone().getWorld(), new Transform(chosenTeam.getBaseZone().getPosition().toVector3d()));
        Universe.get().getWorld(playerRef.getWorldUuid())
                .getEntityStore()
                .getStore()
                .addComponent(
                        playerRef.getReference(),
                        Teleport.getComponentType(),
                        tp
        );

        print(playerRef, "You joined the match into the Team " + chosenTeam.getDisplayName());
        printL("Player " + playerRef.getUsername() + " has joined into the Team " + chosenTeam.getDisplayName());
    }

    // MATCH STOP
    public static CompletableFuture<Void> stop(@Nonnull String worldName) { return stop(worldName, false, null); }

    public static CompletableFuture<Void> stop(@Nonnull String worldName, boolean forceStop) { return stop(worldName, forceStop, null); }

    public static CompletableFuture<Void> stop(@Nonnull String worldName, boolean forceStop, @Nullable CommandContext commandContext) {
        printL("Stopping match...");
        World world = Universe.get().getWorld(worldName);
        if (world == null) {
            if (commandContext == null || !commandContext.isPlayer()) {
                printL("You need to specify an world via --world parameter!", Level.WARNING);
                return CompletableFuture.completedFuture(null);
            }
            printL("Trying to stop a match on a invalid world!", Level.WARNING);
            print(commandContext, "Invalid World!");
            return CompletableFuture.completedFuture(null);
        }

        if (!KOTTMatch.getMatchesList().containsKey(worldName)) {
            printL("There isn't a match happening in this world!", Level.WARNING);
            print(commandContext, "There isn't a match happening in this world!");
            return CompletableFuture.completedFuture(null);
        }
        KOTTMatch match = KOTTMatch.getMatchesList().get(worldName);
        if (match == null || !match.getKOTTMatchStatus()) {
            print(commandContext, "[KOTH] The match wasn't started yet!");
            return CompletableFuture.completedFuture(null);
        }

        UserMapMarkersStore store = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());

        for (KOTTTeam team : match.getTeams().values()) {
            if (team.getBaseZone().getZoneMarker() != null) {
                printL(match.Zone.getWorld().getName() + ": Removing UserMapMarker from Team: " + team.getDisplayName() + "...");
                store.removeUserMapMarker(team.getBaseZone().getZoneMarker().getId());
            }

            team.destroyTeamBase();
        }

        printL(match.Zone.getWorld().getName() + ": Removing UserMapMarker from Zone...");
        store.removeUserMapMarker(match.Zone.getZoneMarker().getId());
        printL(match.Zone.getWorld().getName() + ": Removed all UserMapMakers on " + match.getZone().getWorld().getName());

        Vector3i finalWordPos = match.matchStartPos;
        int finalTeamCount = match.getTeams().size();
        int finalZoneSize = match.Zone.getZoneRadius();
        boolean finalZoneLoop = match.isLoop;
        boolean finalZoneSafe = match.isSafe;
        World finalLobbyWorld = match.Lobby;
        Vector3i finalLobbyPos = match.LobbyPos;

        List<PlayerRef> playerRefList = match.Zone.playersInZone;

        match.Teams.clear();
        match.Zone = null;
        match.setKOTTMatchStatus(false);

        KOTTMatch.getMatchesList().remove(worldName);
        print(commandContext, "Stopped the active KOTT match!");

        CompletableFuture<World> fun2 = CompletableFuture.completedFuture(null);
        if (match.isLoop && !forceStop) {
            UUID uuid = UUID.randomUUID();
            String tempWorldName = "temp_" + uuid;
            fun2 = Universe.get().addWorld(tempWorldName);
        }

        return fun2.thenCompose(world1 -> {
            if (world1 != null) {
                return world1.getChunkAsync(finalWordPos.x, finalWordPos.z).thenAccept(WorldChunk::markNeedsSaving).thenCompose(unused ->
                    tryCreateMatch(
                            finalWordPos,
                            finalTeamCount,
                            finalZoneSize,
                            finalZoneSafe,
                            finalZoneLoop,
                            null,
                            commandContext,
                            world1,
                            finalLobbyWorld,
                            finalLobbyPos
                    ).thenCompose(loadedWorldName -> {
                        for (PlayerRef playerRef : playerRefList) {
                            Teleport tp = Teleport.createForPlayer(world1, new Transform(finalWordPos));
                            Universe.get().getWorld(worldName).getEntityStore().getStore().addComponent(playerRef.getReference(), Teleport.getComponentType(), tp);
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                );
            }else {
                if (finalZoneSafe) {
                    return CompletableFuture.completedFuture(null).thenCompose(unused2 -> {
                       for (PlayerRef playerRef : playerRefList) {
                           Teleport tp = Teleport.createForPlayer(finalLobbyWorld, new Transform(finalLobbyPos));
                           Universe.get().getWorld(worldName).getEntityStore().getStore().addComponent(playerRef.getReference(), Teleport.getComponentType(), tp);
                       }

                       return CompletableFuture.completedFuture(null).thenCompose(unused3 -> {
                           Universe.get().removeWorld(worldName);
                           return CompletableFuture.completedFuture(null);
                       });
                    });
                }
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    private void addToMatch(@Nonnull PlayerRef playerRef, KOTTTeam team) {
        this.playersInMatch.put(playerRef.getUuid(), playerRef);
        this.Zone.playersInZone.add(playerRef);
        team.addPlayerRef(playerRef);
    }

    private static void removeFromMatch(PlayerRef playerRef) {
        for (KOTTMatch match : KOTTMatch.getMatchesList().values()) {
            if (match.getPlayersInMatch().containsValue(playerRef)) {
                for (KOTTTeam team : match.Teams.values()) {
                    if (team.containsPlayer(playerRef)) {
                        team.removeFromTeam(playerRef);
                        match.Zone.removeFromZone(playerRef);
                        match.playersInMatch.remove(playerRef.getUuid());
                        return;
                    }
                }
            }
        }
    }

    public boolean getKOTTMatchStatus() {
        return KOTTMatchStatus;
    }

    public void setKOTTMatchStatus(boolean state) {
        KOTTMatchStatus = state;
    }

    public Map<UUID, KOTTTeam> getTeams(){ return this.Teams; }

    public KOTTTeam getLastTeamAdded() { return this.Teams.lastEntry().getValue(); }

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

    public static Map<String, KOTTMatch> getMatchesList() { return matchesList; }

    // public Vector3i getMatchStartPos() { return this.matchStartPos; }
}
