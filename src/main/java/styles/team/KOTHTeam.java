package styles.team;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.*;

import static styles.utils.Utils.printL;

public class KOTHTeam {

    public final UUID teamID;
    private final Collection<PlayerRef> playerList = new ArrayList<>();
    private String displayName;

    public KOTHTeam(UUID id){
        this.teamID = id;
    }

    // Add player to the Team
    public void addPlayerRef(PlayerRef playerRef) {
        playerList.add(playerRef);
    }

    public boolean containsPlayerRef(PlayerRef playerRef) {
        return playerList.contains(playerRef);
    }

    public static boolean createTeam(UUID id, Map<UUID, KOTHTeam> teamList, String displayName) {
        if(teamList.containsKey(id)){
            printL("[KOTH] There is an Team with that ID already!");
            return false;
        }else{
            teamList.put(id, new KOTHTeam(id));
            teamList.get(id).setDisplayName(displayName);
            return true;
        }
    }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String name) { this.displayName = name; }

}
