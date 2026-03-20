package styles.events;

import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

public class OnAddPlayerToWorldEvent {
    public static void onAddPlayerToWorld(AddPlayerToWorldEvent evt) {
        PlayerRef playerRef = evt.getHolder().getComponent(PlayerRef.getComponentType());
        if (playerRef != null) {

            World world = evt.getWorld();

            KOTTMatch actualMatch = KOTTMatch.getMatchesList().get(world.getName());

            for (KOTTMatch match : KOTTMatch.getMatchesList().values()) {
                if (match.getPlayersInZone().contains(playerRef)) {
                    for (KOTTTeam team : match.getTeams().values()) {
                        team.getPlayerList().remove(playerRef);
                    }

                    match.getPlayersInZone().remove(playerRef);
                }
            }

            if (actualMatch != null) {
                actualMatch.join(playerRef);
            }
        }
    }
}
