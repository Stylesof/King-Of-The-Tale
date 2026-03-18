package styles.world;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import styles.team.KOTTTeam;
import styles.util.ColorHandler;
import styles.util.StringGenerator;
import styles.util.MathHelper;
import styles.world.util.WorldBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class KOTTMatch {

    // [World Name] [KOTTMatch]
    private static final Map<String, KOTTMatch> matchesList = new HashMap<>();

    private boolean KOTHMatchStatus = false;
    private boolean isLoop = false;
    private boolean isSafe = false;
    private Vector3i matchStartPos = new Vector3i();
    private final LinkedHashMap<UUID, KOTTTeam> Teams = new LinkedHashMap<>();
    private KOTTZone Zone;
    //private UserMapMarkersStore userMapMarkerStore;
    //private static final List<KOTHTeamZone> TeamZones = new ArrayList<>();

    public static boolean createMatch(String worldName) {
        if (!KOTTMatch.getMatchesList().containsKey(worldName)) {
            KOTTMatch.getMatchesList().put(worldName, new KOTTMatch());
            return true;
        }else {
            if (!KOTTMatch.getMatchesList().get(worldName).getKOTHMatchStatus()) {
                KOTTMatch.getMatchesList().put(worldName, new KOTTMatch());
                return true;
            }
        }

        return false;
    }

    // MATCH START
    public void start(@Nonnull Vector3i startPos, int teamCount, int areaRadius, @Nullable PlayerRef playerRef, @Nonnull World world, @Nullable CommandContext commandContext) { start(startPos, teamCount, areaRadius, false, true, playerRef, world, commandContext); }

    public void start(@Nonnull Vector3i startPos, int teamCount, int areaRadius, boolean loop, boolean safe, @Nullable PlayerRef playerRef, @Nonnull World world, @Nullable CommandContext commandContext) {
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
        Zone = new KOTTZone(startPos, world, zoneMarker);
        userMapMarkerStore.addUserMapMarker(Zone.getZoneMarker());

        float angleBetweenBases = 360.0f / teamCount;
        float startAngle = 0.0f;
        int distanceZoneBase = areaRadius + KOTTTeam.distanceBaseFromZone + KOTTZone.zoneRadius;

        // Calculates the first base position
        Vector3i baseLocation = new Vector3i(distanceZoneBase, 0, 0);
        baseLocation = MathHelper.vectorSum(baseLocation.toVector3d(), Zone.getPosition().toVector3d()).ceil().toVector3i();

        // Using a pre-defined name template, get an list with random names
        List<String> nameList = StringGenerator.genRandomNameList(teamCount);
        List<Color> colorList = ColorHandler.genRandomColorList(teamCount);

         //printL("[KOTT Debug] Starting Team creation system...");
        int i = 0;
        while (i < teamCount) {

            baseLocation = WorldBuilder.alignVectorToWorldSurface(baseLocation, world); // get the highest position of floor before sky

            if (baseLocation == null) {
                print(commandContext, "[KOTH] Failed to define Base Position, try in another place!");
                printL("[KOTH Debug] ERROR: Invalid Base position!");
                stop(world.getName());
                return;
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

                print(playerRef, "[KOTH] Base of team \"" + getLastTeamAdded().getDisplayName() + "\" created on: (" + baseLocation.x + ", " + baseLocation.y + ", " + baseLocation.z + ")");

                if (i < teamCount - 1) {
                    startAngle += angleBetweenBases;
                    Vector3d other = MathHelper.convertAngleToUnitVector(startAngle);
                    other = MathHelper.scalarVector(other, distanceZoneBase);
                    baseLocation = MathHelper.vectorSum(other, Zone.getPosition().toVector3d()).ceil().toVector3i();
                }

                // Clear a specified area position with a specific size, in a square shape
                Vector3i basePos = getLastTeamAdded().getBaseZone().getPosition();
                basePos.y--;
                WorldBuilder.clearAreaSquare(basePos, 10, world);
                basePos.y++;
                // Create default base
                ColorHandler.ColorType teamColorType = ColorHandler.getColorType(colorList.get(i));
                if (teamColorType == null) {
                    print(playerRef, "[KOTT] Failed to find team colorType!");
                    printL("[KOTT Debug] Failed to find team colorType!");
                    stop(world.getName());
                    return;
                }

                if (!WorldBuilder.constructTeamBase(basePos, teamColorType, world)) {
                    print(playerRef, "[KOTT] Failed to create an team base!");
                    printL("[KOTT Debug] Failed to create an team base!", Level.SEVERE);
                    stop(world.getName());
                    return;
                }

                i++;
            } else {
                print(commandContext, "[KOTT] Failed to create the team: " + nameList.get(i));
                printL("[KOTT Debug] Failed to create the team: " + nameList.get(i));
                stop(world.getName());
                return;
            }
        }

        // For every player in the match world, add to a team (order mode) and teleport them to their base
        // if no ones is on the world (or is a new world), do nothing
        i = 0;
        for (PlayerRef _playerRef : world.getPlayerRefs()) {
            if (_playerRef.getReference() == null) continue;

            KOTTTeam team = (KOTTTeam) Teams.values().toArray()[i++];
            team.addPlayerRef(playerRef);

            Transform transform = playerRef.getTransform();
            transform.setPosition(team.getBaseZone().getPosition().toVector3d());
            Teleport tp = Teleport.createForPlayer(transform);
            world.getEntityStore().getStore().addComponent(_playerRef.getReference(), Teleport.getComponentType(), tp);
        }

        printL("[KOTT Debug] Finished KOTT Match creation!");
        setKOTHMatchStatus(true);
    }

    // MATCH PLAYER JOIN
    public boolean join(PlayerRef playerRef) {
        if (!getKOTHMatchStatus()){
            return false;
        }

        KOTTTeam chosenTeam = (KOTTTeam) Teams.values().toArray()[0];
        int playerCounter = Universe.get().getPlayerCount();
        for (KOTTTeam team : Teams.values()) {
            if(team.getPlayerCount() < playerCounter){
                playerCounter = team.getPlayerCount();
                chosenTeam = team;
            }
        }

        chosenTeam.addPlayerRef(playerRef);
        // zone on 0, 0, 0
        // base on 1, 2, 0
        Teleport tp = new Teleport(
                Zone.getWorld(), chosenTeam.getBaseZone().getPosition().toVector3d(),
                new Vector3f(
                        1.0f / (chosenTeam.getBaseZone().getPosition().x + Zone.getPosition().x),
                        1.0f / (chosenTeam.getBaseZone().getPosition().y + Zone.getPosition().y),
                        1.0f / (chosenTeam.getBaseZone().getPosition().z + Zone.getPosition().z)
                )
        );

        Universe.get().getWorld(playerRef.getWorldUuid())
                .getEntityStore()
                .getStore()
                .addComponent(
                    playerRef.getReference(),
                    Teleport.getComponentType(),
                    tp
        );

        return true;
    }

    // MATCH STOP
    public static void stop(@Nonnull String worldName) { stop(worldName, false, null); }

    public static void stop(@Nonnull String worldName, boolean forceStop) { stop(worldName, forceStop, null); }

    public static void stop(@Nonnull String worldName, boolean forceStop, @Nullable CommandContext commandContext) {
        World world = Universe.get().getWorld(worldName);

        if (world == null) {
            if (commandContext == null || !commandContext.isPlayer()) {
                printL("[KOTT Debug] You need to specify an world via --world parameter!", Level.WARNING);
                return;
            }
            printL("[KOTT Debug] Trying to stop a match on a invalid world!", Level.WARNING);
            print(commandContext, "[KOTT] Invalid World!");
            return;
        }

        if (!KOTTMatch.getMatchesList().containsKey(worldName)) {
            printL("[KOTT Debug] There isn't a match happening in this world!", Level.WARNING);
            print(commandContext, "[KOTT] There isn't a match happening in this world!");
            return;
        }
        KOTTMatch match = KOTTMatch.getMatchesList().get(worldName);
        if (match == null || !match.getKOTHMatchStatus()) {
            print(commandContext, "[KOTH] There isn't any match happening in the moment!");
            return;
        }

        UserMapMarkersStore store = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());

        for (KOTTTeam team : match.getTeams().values()) {
            if (team.getBaseZone().getZoneMarker() != null) {
                store.removeUserMapMarker(team.getBaseZone().getZoneMarker().getId());
            }

            Vector3i basePos = team.getBaseZone().getPosition();
            WorldBuilder.clearAreaSquare(basePos, 7, world);
            basePos.y -= 2;
            WorldBuilder.clearAreaSquare(basePos, 8, world);
            basePos.y += 4;
            WorldBuilder.clearAreaSquare(basePos, 7, world);
        }

        store.removeUserMapMarker(match.Zone.getZoneMarker().getId());

        match.Teams.clear();
        match.Zone = null;
        match.setKOTHMatchStatus(false);
        //KOTTMatch.getMatchesList().remove(worldName);

        if (match.isLoop && !forceStop) {
            UUID uuid = UUID.randomUUID();
            String tempWorldName = "temp_" + uuid;
            //printL("[KOTT Debug] New world name: " + tempWorldName);
            CompletableFuture<World> fun = Universe.get().addWorld(tempWorldName);
            fun.join();
            World tempWorld = Universe.get().getWorld(tempWorldName);
            if (tempWorld != null) {
                printL("[KOTT Debug] New world created!");
                if (createMatch(tempWorldName)) {
                    KOTTMatch newMatch = getMatchesList().get(tempWorldName);
                    newMatch.start(
                            match.getMatchStartPos(),
                            match.getTeams().size(),
                            KOTTZone.zoneRadius,
                            match.isLoop,
                            match.isSafe,
                            null,
                            world,
                            commandContext
                    );
                    printL("[KOTT Debug] Match successfully created!");
                }else {
                    print(commandContext, "[KOTT] Failed to restart the match!");
                }
            }else {
                printL("[KOTT Debug] Failed to create new world!", Level.SEVERE);
            }
        }

        if (match.isSafe) {
            KOTTMatch.getMatchesList().remove(worldName);

            if (!Universe.get().removeWorld(worldName)) {
                print(commandContext, "[KOTT] Failed to remove temporary world!");
            }

            Universe.get().getWorld(worldName).validateDeleteOnRemove();
        }

        print(commandContext, "[KOTH] Stopped the active KOTH match!");
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

    public static Map<String, KOTTMatch> getMatchesList() { return matchesList; }

    public Vector3i getMatchStartPos() { return this.matchStartPos; }
}
