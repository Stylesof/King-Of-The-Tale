package styles.world.match;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import java.util.HashMap;
import java.util.Map;

public class KOTTScoreboard {
    private Map<PlayerRef, Integer> PlayersKillCount;
    private Map<PlayerRef, Integer> PlayersDeathCount;

    public void addKills(PlayerRef playerRef, int quantity) {
        if (this.PlayersKillCount.containsKey(playerRef)) {
            this.PlayersKillCount.put(playerRef, this.PlayersKillCount.get(playerRef) + quantity);
        } else {
            this.PlayersKillCount.put(playerRef, quantity);
        }
    }

    public void addDeath(PlayerRef playerRef) {
        if (this.PlayersDeathCount.containsKey(playerRef)) {
            this.PlayersDeathCount.put(playerRef, this.PlayersDeathCount.get(playerRef) + 1);
        } else {
            this.PlayersDeathCount.put(playerRef, 1);
        }
    }


}
