/*

    DISABLED BY NOW

 */
package styles.events;

import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.world.KOTTMatch;

import static styles.util.MessageHandler.printChat;

@Deprecated
public class OnAddPlayerToWorldEvent {
    public static void onAddPlayerToWorld(AddPlayerToWorldEvent evt) {
        PlayerRef playerRef = evt.getHolder().getComponent(PlayerRef.getComponentType());
        if (playerRef != null) {
            World world = evt.getWorld();

            if (!KOTTMatch.getMatchesList().containsKey(world.getName())) {
                print(playerRef, "There isn't happening an match on this world!");
                return;
            }

            KOTTMatch.getMatchesList().get(world.getName()).join(playerRef);
        }
    }
}
