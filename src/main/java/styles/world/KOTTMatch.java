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

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class KOTTMatch {

    // [World Name] [KOTTMatch]
    private static final Map<String, KOTTMatch> matchesList = new HashMap<>();

    private boolean KOTHMatchStatus = false;
    private final LinkedHashMap<UUID, KOTTTeam> Teams = new LinkedHashMap<>();
    private KOTTZone Zone;
    //private UserMapMarkersStore userMapMarkerStore;
    //private static final List<KOTHTeamZone> TeamZones = new ArrayList<>();

    // MATCH START
    public void start(@Nonnull Vector3i startPos, int teamCount, int areaRadius, @Nonnull PlayerRef playerRef, @Nonnull World world, @Nonnull CommandContext commandContext) {
        UserMapMarkersStore userMapMarkerStore = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());
        UserMapMarker zoneMarker = new UserMapMarker();
            zoneMarker.setId(UUID.randomUUID().toString());
            zoneMarker.setPosition(startPos.x, startPos.z);
            zoneMarker.setName("Attack Zone");
            zoneMarker.setIcon("UserF.png");
            zoneMarker.setColorTint(new Color((byte) 255, (byte) 0, (byte) 0));
        Zone = new KOTTZone(startPos, zoneMarker);
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

        int i = 0;
        while (i < teamCount) {
            baseLocation = WorldBuilder.alignVectorToWorldSurface(baseLocation, world); // get the highest position of floor before sky
            if (baseLocation == null) {
                if (commandContext.isPlayer()) {
                    print(commandContext, "[KOTH] Failed to define Base Position, try in another place!");
                }else {
                    printL("[KOTH Debug] ERROR: Invalid Base position!");
                }
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
            if (KOTTTeam.createTeam(Teams, UUID.randomUUID(), nameList.get(i), baseLocation, zoneMarker2)) {

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
                WorldBuilder.constructTeamBase(basePos, ColorHandler.ColorType.BLUE, world);

                i++;
            }
        }

        // For every player in the match world, add to a team (order mode) and teleport them to their base
        i = 0;
        for (PlayerRef _playerRef : world.getPlayerRefs()) {
            KOTTTeam team = (KOTTTeam) Teams.values().toArray()[i++];
            team.addPlayerRef(playerRef);

            Transform transform = playerRef.getTransform();
            transform.setPosition(team.getBaseZone().getPosition().toVector3d());

            Teleport tp = Teleport.createForPlayer(transform);

            assert playerRef.getReference() != null;
            world.getEntityStore().getStore().addComponent(playerRef.getReference(), Teleport.getComponentType(), tp);
        }

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

        return true;
    }

    // MATCH STOP
    public static void stop(@Nonnull String worldName, @Nullable CommandContext commandContext) {
        World world = Universe.get().getWorld(worldName);
        if (world == null) {
            printL("[KOTT Debug] Invalid world!");
            print(commandContext, "[KOTT] Invalid World!");
            return;
        }

        if (!KOTTMatch.getMatchesList().containsKey(worldName)) {
            printL("[KOTT Debug] Invalid match!");
            print(commandContext, "[KOTT] Invalid match!");
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
        }

        store.removeUserMapMarker(match.Zone.getZoneMarker().getId());

        match.Teams.clear();
        match.Zone = null;
        match.setKOTHMatchStatus(false);
        //KOTTMatch.getMatchesList().remove(worldName);

        print(commandContext, "[KOTH] Stopped the active KOTH match!");
    }

    public static void stop(@Nonnull String worldName) { stop(worldName, null); }

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
}
