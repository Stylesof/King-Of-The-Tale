package styles.world.match;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import java.util.HashMap;
import java.util.Map;

public class KOTTScoreboard {
    private final Map<PlayerRef, Integer> PlayersKillCount = new HashMap<>();
    private final Map<PlayerRef, Integer> PlayersDeathCount = new HashMap<>();
    private int playerCount = 0;

    public void addKills(PlayerRef playerRef, int quantity) {
        if (this.PlayersKillCount.containsKey(playerRef)) {
            this.PlayersKillCount.put(playerRef, this.PlayersKillCount.get(playerRef) + quantity);
        } else {
            this.PlayersKillCount.put(playerRef, quantity);
            playerCount++;
        }
    }

    public void addDeath(PlayerRef playerRef) {
        if (this.PlayersDeathCount.containsKey(playerRef)) {
            this.PlayersDeathCount.put(playerRef, this.PlayersDeathCount.get(playerRef) + 1);
        } else {
            this.PlayersDeathCount.put(playerRef, 1);
            playerCount++;
        }
    }

    public Map<PlayerRef, Integer> getPlayersKillCount() { return this.PlayersKillCount; }

    public Map<PlayerRef, Integer> getPlayersDeathCount() { return this.PlayersDeathCount; }

    public int getPlayerCount() { return this.playerCount; }
}