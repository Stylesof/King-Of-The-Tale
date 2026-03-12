package styles.world;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.worldmap.CreateUserMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarkerComponent;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import styles.team.KOTTTeam;
import styles.team.name.TeamNameGenerator;
import styles.util.MathHelper;
import styles.world.util.WorldBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class KOTTMatch {

    private static final Map<World, KOTTMatch> matchesList = new HashMap<>();

    //private World matchWorld;

    private boolean KOTHMatchStatus = false;
    //private boolean toStop = false;
    private final LinkedHashMap<UUID, KOTTTeam> Teams = new LinkedHashMap<>();

    private KOTTZone Zone;
    //private static final List<KOTHTeamZone> TeamZones = new ArrayList<>();

    // MATCH START
    public void start(@Nonnull Vector3i startPos, int teamCount, int areaRadius, @Nonnull PlayerRef playerRef, @Nonnull World world, @Nonnull CommandContext commandContext) {
        Zone = new KOTTZone(startPos);
        //matchWorld = world;

        // WIP
        UserMapMarkersStore store = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());
        UserMapMarker marker = new UserMapMarker();
        marker.setId("user_shared_" + UUID.randomUUID());
        marker.setPosition(Zone.getPosition().x, Zone.getPosition().z);
        marker.setName("Attack Zone");
        marker.setIcon("Common/Icons/ItemCategories/Items-Weapons.png");
        store.addUserMapMarker(marker);
        // WIP

        float angleBetweenBases = 360.0f / teamCount;
        float startAngle = 0.0f;
        int distanceZoneBase = areaRadius + KOTTTeam.distanceBaseFromZone + KOTTZone.zoneRadius;

        // Calculates the first base position
        Vector3i baseLocation = new Vector3i(distanceZoneBase, 0, 0);
        baseLocation = MathHelper.vectorSum(baseLocation.toVector3d(), Zone.getPosition().toVector3d()).ceil().toVector3i();

        // Using a pre-defined name template, get an list with random names
        List<String> nameList = TeamNameGenerator.genRandomNameList(teamCount);

        int i = 0;
        while (i < teamCount) {
            baseLocation = WorldBuilder.alignVectorToWorldSurface(baseLocation, world); // get the highest position of floor before sky
            if (baseLocation == null) {
                if (commandContext.isPlayer()) {
                    print(commandContext, "[KOTH] Failed to define Base Position, try in another place!");
                }else {
                    printL("[KOTH Debug] ERROR: Invalid Base position!");
                }
                stop();
                return;
            }

            // Create %teamCount% teams
            if (KOTTTeam.createTeam(Teams, UUID.randomUUID(), nameList.get(i), baseLocation)) {

                print(playerRef, "[KOTH] Base of team \"" + getLastTeamAdded().getDisplayName() + "\" created on: (" + baseLocation.x + ", " + baseLocation.y + ", " + baseLocation.z + ")");

                if (i < teamCount - 1) {
                    startAngle += angleBetweenBases;
                    Vector3d other = MathHelper.convertAngleToUnitVector(startAngle);
                    other = MathHelper.scalarVector(other, distanceZoneBase);
                    baseLocation = MathHelper.vectorSum(other, Zone.getPosition().toVector3d()).ceil().toVector3i();
                }

                // Clear an specified area position with a specific size, in a square shape
                Vector3i basePos = getLastTeamAdded().getBaseZone().getPosition();
                WorldBuilder.clearAreaSquare(basePos, 10, world);
                BlockType block = BlockType.fromString("Rock_Stone");
                if(block != null) {
                    print(playerRef, "Tudo certo " + block.getId());
                }
                WorldBuilder.createFillSquare(new Vector3i(basePos.x - 4, basePos.y - 1, basePos.z - 5), new Vector3i(basePos.x -4, basePos.y - 1, basePos.z + 5), block, world);

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

        KOTTTeam choosenTeam = (KOTTTeam) Teams.values().toArray()[0];
        int playerCounter = Universe.get().getPlayerCount();
        for (KOTTTeam team : Teams.values()) {
            if(team.getPlayerCount() < playerCounter){
                playerCounter = team.getPlayerCount();
                choosenTeam = team;
            }
        }

        choosenTeam.addPlayerRef(playerRef);

        return true;
    }

    // MATCH TICK
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

    }

    // MATCH STOP
    public void stop() {
        setKOTHMatchStatus(false);
        Teams.clear();
        Zone = null;
    }

    //public boolean getToStop() { return this.toStop; }

    public boolean getKOTHMatchStatus() {
        return KOTHMatchStatus;
    }

    public void setKOTHMatchStatus(boolean state) {
        KOTHMatchStatus = state;
    }

    public Map<UUID, KOTTTeam> getTeams(){ return this.Teams; }

    public KOTTTeam getLastTeamAdded() { return this.Teams.lastEntry().getValue(); }

    //public int getTeamPlayerCount(KOTTTeam team) { return team.getPlayerCount(); }

    //public static List<KOTHTeamZone> getTeamZones() { return TeamZones; }

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

    //public World getWorld() { return matchWorld; }

    public static Map<World, KOTTMatch> getMatchesList() { return matchesList; }
}
