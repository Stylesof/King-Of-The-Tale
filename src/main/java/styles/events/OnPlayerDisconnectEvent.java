package styles.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class OnPlayerDisconnectEvent {
    // SingleplayerModule.getUuid() == null -> It's a server hosted (not player hosted)
    public static void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent evt) {
        if (SingleplayerModule.getUsername().equals(evt.getPlayerRef().getUsername())) { // host quit
            for (World world : Universe.get().getWorlds().values()) { // get all world of the server
                if (KOTTMatch.getMatchesList().containsKey(world.getName())) { // verify if they have a match happening
                    KOTTMatch.stop(world.getName(), true, null, true);
                }
            }
        }else{
            if (evt.getDisconnectReason().getServerDisconnectReason() != null) {
                if (evt.getDisconnectReason().getServerDisconnectReason().equals("Stopping server!")) {
                    PlayerRef playerRef = evt.getPlayerRef();
                    if (playerRef.getWorldUuid() == null) {
                        printL("[KOTT Debug] Fatal error! Invalid World UUID for the player");
                        return;
                    }

                    World world = Universe.get().getWorld(playerRef.getWorldUuid());
                    if (world == null) {
                        printL("[KOTT Debug] Fatal error! Invalid World for the player");
                        return;
                    }

                    if (KOTTMatch.getMatchesList().containsKey(world.getName())) {
                        KOTTMatch.stop(world.getName(), true, null, true);
                    }
                }
            }
        }
    }
}
