package styles.team;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.*;

import static styles.utils.Utils.printL;

public class KOTHTeam {

    public final UUID teamID;
    private final Collection<PlayerRef> playerList = new ArrayList<>();
    private final String displayName;

    public static final int distanceBaseFromZone = 100;

    public KOTHTeam(UUID id, String displayName){
        this.teamID = id;
        this.displayName = displayName;
    }

    // Add player to the Team
    public void addPlayerRef(PlayerRef playerRef) {
        playerList.add(playerRef);
    }

    public boolean containsPlayerRef(PlayerRef playerRef) {
        return playerList.contains(playerRef);
    }

    public static boolean createTeam(Map<UUID, KOTHTeam> teamListRef, UUID id, String displayName) {
        if(teamListRef.containsKey(id)){
            printL("[KOTH] There is an Team with that ID already!");
            return false;
        }else{
            teamListRef.put(id, new KOTHTeam(id, displayName));
            return true;
        }
    }

    public String getDisplayName() { return displayName; }

    public Collection<PlayerRef> getPlayerList() { return playerList; }

    public int getPlayerCount() { return playerList.size(); }

}
