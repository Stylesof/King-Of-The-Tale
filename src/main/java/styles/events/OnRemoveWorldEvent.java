package styles.events;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.WorldEvent;
import styles.world.KOTTMatch;

public class OnRemoveWorldEvent {

    public static void onRemoveWorld(WorldEvent evt) {
        World world = evt.getWorld();

        if (KOTTMatch.getMatchesList().containsKey(world)) {
            KOTTMatch match = KOTTMatch.getMatchesList().get(world);

            if (match.getKOTHMatchStatus()) {
                match.stop();
            }
        }
    }

}
