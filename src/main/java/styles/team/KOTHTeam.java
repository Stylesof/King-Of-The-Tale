package styles.team;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.world.KOTHTeamZone;
import styles.world.KOTHZone;

import java.util.*;

import static styles.util.PrintMacros.printL;

public class KOTHTeam {

    public final UUID teamID;
    private final Collection<PlayerRef> playerList = new ArrayList<>();
    private final String displayName;

    private final KOTHTeamZone baseZone;

    public static final int distanceBaseFromZone = 100;

    public KOTHTeam(UUID id, String displayName, Vector3i basePosition) {
        this.teamID = id;
        this.displayName = displayName;
        this.baseZone = new KOTHTeamZone(basePosition, this);
    }

    // Add player to the Team
    public void addPlayerRef(PlayerRef playerRef) {
        playerList.add(playerRef);
    }

    public boolean containsPlayer(PlayerRef playerRef) {
        return playerList.contains(playerRef);
    }

    public static boolean createTeam(Map<UUID, KOTHTeam> teamListRef, UUID id, String displayName, Vector3i basePosition) {
        if(teamListRef.containsKey(id)){
            printL("[KOTH] There is an Team with that ID already!");
            return false;
        }else{
            teamListRef.put(id, new KOTHTeam(id, displayName, basePosition));
            return true;
        }
    }

    public KOTHTeamZone getBaseZone() { return this.baseZone; }

    public String getDisplayName() { return displayName; }

    public Collection<PlayerRef> getPlayerList() { return playerList; }

    public int getPlayerCount() { return playerList.size(); }

}
