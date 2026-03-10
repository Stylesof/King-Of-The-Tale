package styles;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.team.KOTHTeam;
import styles.team.name.TeamNameGenerator;
import styles.utils.MathHelper;
import styles.world.KOTHTeamZone;
import styles.world.KOTHZone;

import javax.annotation.Nullable;
import java.util.*;

import static styles.utils.Utils.print;
import static styles.utils.Utils.printL;

public class KOTHMatch {

    private static boolean KOTHMatchStatus = false;
    private static final LinkedHashMap<UUID, KOTHTeam> Teams = new LinkedHashMap<>();

    private static KOTHZone Zone;
    private static final List<KOTHTeamZone> TeamZones = new ArrayList<>();

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
            if(KOTHTeam.createTeam(Teams, UUID.randomUUID(), nameList.get(i))) {
                TeamZones.add(new KOTHTeamZone(baseLocation, getLastTeamAdded()));

                print(tempPlayerRef, "[KOTH] Base of team \"" + getLastTeamAdded().getDisplayName() + "\" created on: (" + baseLocation.x + ", " + baseLocation.y + ", " + baseLocation.z + ")");

                if(i < teamCount - 1) {
                    startAngle += angleBetweenBases;
                    Vector3d other = MathHelper.convertAngleToVector(startAngle);
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

            if(!(i < Teams.size())){
                i = 0;
            }
        }
    }

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

    public static void tick() {
        if(!KOTHMatch.getKOTHMatchStatus()) return;


    }

    public static void stop() {
        setKOTHMatchStatus(false);
        TeamZones.clear();
        Teams.clear();
        Zone = null;
    }


    public static boolean getKOTHMatchStatus() {
        return KOTHMatchStatus;
    }

    public static void setKOTHMatchStatus(boolean state) {
        KOTHMatchStatus = state;
    }

    public static Map<UUID, KOTHTeam> getTeams(){ return Teams; }

    public static int getTeamPlayerCount(KOTHTeam team) { return team.getPlayerCount(); }

    public static KOTHTeam getLastTeamAdded() { return Teams.lastEntry().getValue(); }

    @Nullable
    public static KOTHTeam findPlayerTeam(PlayerRef playerRef){
        if(getKOTHMatchStatus()){
            for(KOTHTeam team : Teams.values()){
                if(team.containsPlayerRef(playerRef)){
                    return team;
                }
            }
        }

        return  null;
    }

}
