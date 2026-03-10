package styles;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.command.commands.world.chunk.ChunkForceTickCommand;
import com.hypixel.hytale.server.core.command.commands.world.chunk.ChunkLoadCommand;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.commands.block.BlockSetTickingCommand;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.team.KOTHTeam;
import styles.team.name.TeamNameGenerator;
import styles.util.MathHelper;
import styles.world.KOTHTeamZone;
import styles.world.KOTHZone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class KOTHMatch {

    private static boolean KOTHMatchStatus = false;
    private static boolean toStop = false;
    private static final LinkedHashMap<UUID, KOTHTeam> Teams = new LinkedHashMap<>();

    private static KOTHZone Zone;
    //private static final List<KOTHTeamZone> TeamZones = new ArrayList<>();

    // MATCH START
    public static void start(Vector3i startPos, int teamCount, int areaRadius, List<PlayerRef> playerRefList, World world) {
        setKOTHMatchStatus(true);
        Zone = new KOTHZone(startPos);

        PlayerRef tempPlayerRef = playerRefList.getLast();

        float angleBetweenBases = 360.0f / teamCount;
        float startAngle = 0.0f;
        int distanceZoneBase = areaRadius + KOTHTeam.distanceBaseFromZone + KOTHZone.zoneRadius;

        Vector3i baseLocation = new Vector3i(distanceZoneBase, 0, 0);
        baseLocation = MathHelper.vectorSum(baseLocation.toVector3d(), Zone.getPosition().toVector3d()).ceil().toVector3i();

        List<String> nameList = TeamNameGenerator.genRandomNameList(teamCount);
        int i = 0;
        while(i < teamCount){
            if(KOTHTeam.createTeam(Teams, UUID.randomUUID(), nameList.get(i), baseLocation)) {

                print(tempPlayerRef, "[KOTH] Base of team \"" + getLastTeamAdded().getDisplayName() + "\" created on: (" + baseLocation.x + ", " + baseLocation.y + ", " + baseLocation.z + ")");

                if(i < teamCount - 1) {
                    startAngle += angleBetweenBases;
                    Vector3d other = MathHelper.convertAngleToUnitVector(startAngle);
                    other = MathHelper.scalarVector(other, distanceZoneBase);
                    baseLocation = MathHelper.vectorSum(other, Zone.getPosition().toVector3d()).ceil().toVector3i();
                }

                i++;
            }
        }

        i = 0;
        for(PlayerRef playerRef : playerRefList) {
            KOTHTeam team = (KOTHTeam) Teams.values().toArray()[i++];
            team.addPlayerRef(playerRef);

            // ==============================
            // Use that to prepare the region for the
            // Team Base (if its in air, try catch the ground,
            // if doesn't have an ground, return failed to
            // create a base and stop the match
            Vector3i pos = team.getBaseZone().getPosition();
            BlockType block = world.getBlockType(pos);
            int fluid = world.getFluidId(pos.x, pos.y, pos.z);

            if(block != null){
                print(playerRef, "[KOTH] Your Base block is: " + block.getId());

                Vector3i newPos = new Vector3i(pos);

                while(block.getId().equals("Empty") && fluid == 0) {

                    newPos.subtract(0, 1, 0);
                    block = world.getBlockType(newPos);
                    fluid = world.getFluidId(newPos.x, newPos.y, newPos.z);

                }
            }
            // ==============================
        }
    }

    // MATCH PLAYER JOIN
    public static boolean join(PlayerRef playerRef) {
        if(!getKOTHMatchStatus()){
            return false;
        }

        KOTHTeam choosenTeam = (KOTHTeam) Teams.values().toArray()[0];
        int playerCounter = Universe.get().getPlayerCount();
        for(KOTHTeam team : Teams.values()){
            if(team.getPlayerCount() < playerCounter){
                playerCounter = team.getPlayerCount();
                choosenTeam = team;
            }
        }

        choosenTeam.addPlayerRef(playerRef);

        return true;
    }

    // MATCH TICK
    public static void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if(!KOTHMatch.getKOTHMatchStatus()) return;
        if(toStop) {
            KOTHMatch.setKOTHMatchStatus(false);
            Teams.clear();
            Zone = null;
            toStop = false;
            return;
        }

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        PlayerRef player = store.getComponent(ref, PlayerRef.getComponentType());
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());

        for(KOTHTeam team : Teams.values()) {
            KOTHTeamZone zone = team.getBaseZone();
            if (player != null && player.isValid()) {
                Vector3d position = player.getTransform().getPosition();
                if (zone.isInside(position)) {
                    print(player, "[KOTH] You are inside of an base!");

                    zone.getPlayersInZone().add(player);

                    if (KOTHMatch.getPlayerTeam(player) != null) {
                        if (zone.getOwner() != KOTHMatch.getPlayerTeam(player)) {
                            print(player, "[KOTH] But it's not your base!");
                        }
                    } else {
                        print(player, "[KOTH] You are not, in a team!");
                    }
                }else{
                    zone.getPlayersInZone().remove(player);
                }
            } else if (npc != null) {
                //Vector3d position = npc.getOldPosition();
                //if (zone.isInside(position)) {
                //    zone.getNpcsInZone().add(npc);
                //}else{
                //    zone.getNpcsInZone().remove(npc);
                //}
            }
        }

        if(player != null && Zone.isInside(player.getTransform().getPosition())) {
            print(player, "[KOTH] You are inside of the main Zone!");
        }
    }

    // MATCH STOP
    public static void stop() {
        toStop = true;
    }

    public static boolean getKOTHMatchStatus() {
        return KOTHMatchStatus;
    }

    public static void setKOTHMatchStatus(boolean state) {
        KOTHMatchStatus = state;
    }

    public static Map<UUID, KOTHTeam> getTeams(){ return Teams; }

    public static KOTHTeam getLastTeamAdded() { return Teams.lastEntry().getValue(); }

    public static int getTeamPlayerCount(KOTHTeam team) { return team.getPlayerCount(); }

    //public static List<KOTHTeamZone> getTeamZones() { return TeamZones; }

    @Nullable
    public static KOTHTeam getPlayerTeam(PlayerRef playerRef) {
        if(getKOTHMatchStatus()){
            for(KOTHTeam team : Teams.values()){
                if(team.containsPlayer(playerRef)){
                    return team;
                }
            }
        }

        return null;
    }
}
