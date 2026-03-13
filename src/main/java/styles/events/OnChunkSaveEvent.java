package styles.events;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.ecs.ChunkSaveEvent;
import styles.world.KOTTMatch;

import static styles.util.PrintMacros.printL;

public class OnChunkSaveEvent {
    public static void onChunkSave(ChunkSaveEvent evt) {
        World world = evt.getChunk().getWorld();
        printL("Mundo name: " + world.getName());

        if (KOTTMatch.getMatchesList().containsKey(world.getName())) {
           printL("Mundo Tem Um Match!");
           KOTTMatch match = KOTTMatch.getMatchesList().get(world.getName());

           if (match.getKOTHMatchStatus()) {
              printL("Match ativa!");
              KOTTMatch.stop(world.getName());
              printL("Acho q paro!");
           }
        }else {
            printL("Tem nao");
        }
    }
}
