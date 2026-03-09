package styles;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.team.KOTHTeam;
import styles.team.name.TeamNameGenerator;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.util.*;

public class KOTHMatch {

    private static boolean KOTHMatchStatus = false;
    private static final Map<UUID, KOTHTeam> Teams = new HashMap<>();

    private static Vector3i zonePosition;

    public static void start(Vector3i startPos, int teamCount, int areaSize, Collection<PlayerRef> playerRefList, World world) {
        setKOTHMatchStatus(true);

        List<String> nameList = TeamNameGenerator.genRandomNameList(teamCount);

        int i = 0;
        while(i < teamCount){
            if(KOTHTeam.createTeam(UUID.randomUUID(), Teams, nameList.get(i))) {
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

    public static boolean join() {
        if(getKOTHMatchStatus()){



        }else{
            return false;
        }

        return true;
    }

    public static void stop() {
        setKOTHMatchStatus(false);
        Teams.clear();
    }


    public static boolean getKOTHMatchStatus() {
        return KOTHMatchStatus;
    }

    public static void setKOTHMatchStatus(boolean state) {
        KOTHMatchStatus = state;
    }

    public static Map<UUID, KOTHTeam> getTeams(){ return Teams; }

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
