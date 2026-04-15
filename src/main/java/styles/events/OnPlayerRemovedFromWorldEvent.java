package styles.events;

import com.hypixel.hytale.server.core.event.events.player.RemovedPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import styles.world.KOTTMatch;

public class OnPlayerRemovedFromWorldEvent {
    public static void onPlayerRemovedFromWorld(RemovedPlayerFromWorldEvent evt) {
        KOTTMatch match = KOTTMatch.getMatch(evt.getWorld().getName());

        if (match != null) {
            PlayerRef playerRef = evt.getHolder().getComponent(PlayerRef.getComponentType());
            if (playerRef != null) {
                match.leave(playerRef, evt.getHolder());
            }
        }
    }
}
