package styles.events;

import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.world.KOTTMatch;

import java.util.List;
import java.util.Map;

public class OnShutdownEvent {
    public static void onShutdown(ShutdownEvent evt){
        Map<String, World> worlds = Universe.get().getWorlds();

        for (World world : worlds.values()) {
            if (KOTTMatch.getMatchesList().containsKey(world)) {
                if(KOTTMatch.getMatchesList().get(world).getKOTHMatchStatus()) {
                    KOTTMatch.getMatchesList().get(world).stop();
                }
            }
        }
    }
}
