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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;
import static styles.util.log.PrintLog.printLog;

public class KOTTMatch {

    // [World Name] [KOTTMatch]
    private static final Map<String, KOTTMatch> matchesList = new HashMap<>();

    private boolean KOTHMatchStatus = false;
    private boolean isLoop = false;
    private boolean isSafe = false;
    private Vector3i matchStartPos = new Vector3i();
    private final LinkedHashMap<UUID, KOTTTeam> Teams = new LinkedHashMap<>();
    private KOTTZone Zone;
    private final Map<UUID, PlayerRef> playersInZone = new HashMap<>();

    public static boolean addMatch(String worldName) {
        if (!KOTTMatch.getMatchesList().containsKey(worldName)) {
            KOTTMatch.getMatchesList().put(worldName, new KOTTMatch());
            return true;
        }else {
            if (!KOTTMatch.getMatchesList().get(worldName).getKOTHMatchStatus()) {
                KOTTMatch.getMatchesList().remove(worldName);
                KOTTMatch.getMatchesList().put(worldName, new KOTTMatch());
                return true;
            }
        }
        return false;
    }

    public static CompletableFuture<String> tryCreateMatch(@Nonnull Vector3i startPos, int teamCount, int zoneRadius, boolean safe, boolean loop, @Nullable PlayerRef playerRef, @Nullable CommandContext commandContext, @Nonnull World world) {
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
                            world1
                    );
                    return CompletableFuture.completedFuture(world1.getName());
                })
        ).thenApply(unused -> finalTempWorldName);
    }

    private CompletableFuture<Void> start(@Nonnull Vector3i startPos, int teamCount, int zoneRadius, boolean safe, boolean loop, @Nullable PlayerRef playerRef, @Nullable CommandContext commandContext, @Nonnull World world) {
        isLoop = loop;
        isSafe = safe;

        matchStartPos = startPos;

        printL("[KOTT Debug] Starting KOTT Match creation...");
        printL("[KOTT Debug] Team Count: " + teamCount);

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
                print(commandContext, "[KOTH] Failed to define Base Position, try in another place!");
                printL("[KOTH Debug] ERROR: Invalid Base position!");
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
            printL("[KOTT Debug] Starting Team creation...");
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
                    print(playerRef, "[KOTT] Failed to find team colorType!");
                    printL("[KOTT Debug] Failed to find team colorType!");
                    stop(world.getName());
                    return CompletableFuture.completedFuture(null);
                }

                // Clear area and construct base
                Vector3i basePos = getLastTeamAdded().getBaseZone().getPosition();
                basePos.y--;
                KOTTTeam team = getLastTeamAdded();
                fun = fun.thenCompose((status) -> {
                    if (!status) {
                        return CompletableFuture.completedFuture(false);
                    }

                    return WorldBuilder.clearAreaSquare(basePos, 10, world).thenCompose(unused -> {
                        if (!WorldBuilder.constructTeamBase(basePos, teamColorType, world)) {
                            print(playerRef, "[KOTT] Failed to create the team base!");
                            printL("[KOTT Debug] Error: Failed to create the team base!", Level.SEVERE);
                            stop(world.getName());
                            return CompletableFuture.completedFuture(false);
                        }
                        print(playerRef, "[KOTT] Created base of Team \"" + team.getDisplayName() + "\". (X: " + basePos.x + ", Y: " + basePos.y + ", Z: " + basePos.z + ")");
                        printL("[KOTT Debug] Created base of Team \"" + team.getDisplayName() + "\". (X: " + basePos.x + ", Y: " + basePos.y + ", Z: " + basePos.z + ")");
                        return CompletableFuture.completedFuture(true);
                    });
                });
                basePos.y++;

                i++;
            } else {
                print(commandContext, "[KOTT] Failed to create the team: " + nameList.get(i));
                printL("[KOTT Debug] Error: Failed to create the team: " + nameList.get(i));
                stop(world.getName());
                return CompletableFuture.completedFuture(null);
            }
        }
        return fun.thenCompose(status -> {
            printL("[KOTT Debug] Finished KOTT Match creation! World name: " + world.getName());
            if (status) {
                print(playerRef, "[KOTT] Sending players to the base...");

                int j = 0;
                for (PlayerRef _playerRef : world.getPlayerRefs()) {
                    if (_playerRef.getReference() == null) continue;

                    KOTTTeam team = (KOTTTeam) Teams.values().toArray()[j++];
                    team.addPlayerRef(playerRef);
                    this.playersInZone.put(_playerRef.getUuid(), _playerRef);

                    Transform transform = _playerRef.getTransform();
                    transform.setPosition(team.getBaseZone().getPosition().toVector3d());
                    Teleport tp = Teleport.createForPlayer(transform);
                    world.getEntityStore().getStore().addComponent(_playerRef.getReference(), Teleport.getComponentType(), tp);

                    print(_playerRef, "[KOTT] You are from the Team " + team.getDisplayName());
                }

                print(playerRef, "[KOTT] Match created and started on World: " + world.getName());
                printL("[KOTT Debug] Match created and started on World: " + world.getName());
                setKOTHMatchStatus(true);
            } else {
                print(playerRef, "[KOTT] Failed to create and start the match on World: " + world.getName());
                printL("[KOTT Debug] Error: Failed to create and start the match on World: " + world.getName());
                stop(world.getName(), true);
            }

            return CompletableFuture.completedFuture(true);
        }).thenRun(() -> {});
    }

    // MATCH PLAYER JOIN
    public void join(@Nonnull PlayerRef playerRef) {
        if (!getKOTHMatchStatus()){
            print(playerRef, "[KOTT] The match was not initialized");
            return;
        }

        KOTTTeam chosenTeam = (KOTTTeam) Teams.values().toArray()[0];
        int playerCounter = Universe.get().getPlayerCount();
        for (KOTTTeam team : Teams.values()) {
            if (!team.getPlayerList().contains(playerRef)) {
                if (team.getPlayerCount() < playerCounter) {
                    playerCounter = team.getPlayerCount();
                    chosenTeam = team;
                }
            } else {
                print(playerRef, "[KOTT] The player is already on a Team!");
                return;
            }
        }

        this.playersInZone.put(playerRef.getUuid(), playerRef);
        chosenTeam.addPlayerRef(playerRef);

        Teleport tp = Teleport.createForPlayer(chosenTeam.getBaseZone().getWorld(), new Transform(chosenTeam.getBaseZone().getPosition().toVector3d()));
        Universe.get().getWorld(playerRef.getWorldUuid())
                .getEntityStore()
                .getStore()
                .addComponent(
                        Objects.requireNonNull(playerRef.getReference()),
                        Teleport.getComponentType(),
                        tp
        );

        print(playerRef, "[KOTT] Player " + playerRef.getUsername() + " has joined into the Team " + chosenTeam.getDisplayName());
    }

    //public boolean leave(@Nonnull PlayerRef playerRef) {}  TODO: Implement this

    // MATCH STOP
    public static CompletableFuture<Void> stop(@Nonnull String worldName) { return stop(worldName, false, null); }

    public static CompletableFuture<Void> stop(@Nonnull String worldName, boolean forceStop) { return stop(worldName, forceStop, null); }

    public static CompletableFuture<Void> stop(@Nonnull String worldName, boolean forceStop, @Nullable CommandContext commandContext) {
        World world = Universe.get().getWorld(worldName);
        if (world == null) {
            if (commandContext == null || !commandContext.isPlayer()) {
                printL("[KOTT Debug] You need to specify an world via --world parameter!", Level.WARNING);
                return CompletableFuture.completedFuture(null);
            }
            printL("[KOTT Debug] Trying to stop a match on a invalid world!", Level.WARNING);
            print(commandContext, "[KOTT] Invalid World!");
            return CompletableFuture.completedFuture(null);
        }

        if (!KOTTMatch.getMatchesList().containsKey(worldName)) {
            printL("[KOTT Debug] There isn't a match happening in this world!", Level.WARNING);
            print(commandContext, "[KOTT] There isn't a match happening in this world!");
            return CompletableFuture.completedFuture(null);
        }
        KOTTMatch match = KOTTMatch.getMatchesList().get(worldName);
        if (match == null || !match.getKOTHMatchStatus()) {
            print(commandContext, "[KOTH] The match wasn't started yet!");
            return CompletableFuture.completedFuture(null);
        }

        UserMapMarkersStore store = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());

        for (KOTTTeam team : match.getTeams().values()) {
            if (team.getBaseZone().getZoneMarker() != null) {
                // printL("Team " + team.getDisplayName() + " Base zone and Zone marker is valid!");
                store.removeUserMapMarker(team.getBaseZone().getZoneMarker().getId());
                // printL("Removed UserMapMarker");
            }

            Vector3i basePos = team.getBaseZone().getPosition();
            WorldBuilder.clearAreaSquare(basePos, 7, world);
            basePos.y -= 2;
            WorldBuilder.clearAreaSquare(basePos, 8, world);
            basePos.y += 4;
            WorldBuilder.clearAreaSquare(basePos, 7, world);
        }

        store.removeUserMapMarker(match.Zone.getZoneMarker().getId());
        printL("[KOTT Debug] Removed all UserMapMakers on " + match.getZone().getWorld().getName());

        if (match.isSafe) {
            Universe.get().removeWorld(worldName);
        }

        Vector3i finalWordPos = match.matchStartPos;
        int finalTeamCount = match.getTeams().size();
        int finalZoneSize = match.Zone.getZoneRadius();
        boolean finalZoneLoop = match.isLoop;
        boolean finalZoneSafe = match.isSafe;

        match.Teams.clear();
        match.Zone = null;
        match.setKOTHMatchStatus(false);

        KOTTMatch.getMatchesList().remove(worldName);
        print(commandContext, "[KOTH] Stopped the active KOTH match!");

        CompletableFuture<World> fun = CompletableFuture.completedFuture(null);
        if (match.isLoop && !forceStop) {
            UUID uuid = UUID.randomUUID();
            String tempWorldName = "temp_" + uuid;
            fun = Universe.get().addWorld(tempWorldName);
        }

        return fun.thenAccept(world1 -> {
            if (world1 != null) {
                world1.getChunkAsync(finalWordPos.x, finalWordPos.z).thenAccept(WorldChunk::markNeedsSaving).thenRun(() -> {
                    tryCreateMatch(
                            finalWordPos,
                            finalTeamCount,
                            finalZoneSize,
                            finalZoneSafe,
                            finalZoneLoop,
                            null,
                            commandContext,
                            world1
                    );
                });
            }
        });
    }

    public boolean getKOTHMatchStatus() {
        return KOTHMatchStatus;
    }

    public void setKOTHMatchStatus(boolean state) {
        KOTHMatchStatus = state;
    }

    public Map<UUID, KOTTTeam> getTeams(){ return this.Teams; }

    public KOTTTeam getLastTeamAdded() { return this.Teams.lastEntry().getValue(); }

    @Nullable
    public KOTTTeam getPlayerTeam(PlayerRef playerRef) {
        if (getKOTHMatchStatus()) {
            for (KOTTTeam team : this.Teams.values()) {
                if (team.containsPlayer(playerRef)) {
                    return team;
                }
            }
        }

        return null;
    }

    public Map<UUID, PlayerRef> getPlayersInZone() { return this.playersInZone; }

    public KOTTZone getZone() { return this.Zone; }

    public static Map<String, KOTTMatch> getMatchesList() { return matchesList; }

    // public Vector3i getMatchStartPos() { return this.matchStartPos; }
}
