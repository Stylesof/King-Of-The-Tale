package styles.events;

import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMap;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.WorldEvent;
import styles.world.KOTTMatch;

import static styles.util.PrintMacros.printL;

public class OnRemoveWorldEvent {

    public static void onRemoveWorld(WorldEvent evt) {
        World world = evt.getWorld();

        printL("World beign removed aa: " + world.getName());

        for(String world_name : KOTTMatch.getMatchesList().keySet()) {
            printL("World availables: " + world_name);
        }

        world.init().thenRun(() -> {
            if (KOTTMatch.getMatchesList().containsKey(world.getName())) {
                KOTTMatch match = KOTTMatch.getMatchesList().get(world.getName());

                if (match.getKOTHMatchStatus()) {
                    KOTTMatch.stop(world.getName());
                    printL("Finished kott");
                } else {
                    printL("Cannot finish the kott");
                }
            }else{
                printL("legal nao");
            }
        });


        printL("cuzinho");
    }

}
