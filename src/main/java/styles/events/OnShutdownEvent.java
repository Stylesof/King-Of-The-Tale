package styles.events;

import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

public class OnPlayerDisconnectEvent {

    public static void OnPlayerDisconnect(PlayerDisconnectEvent evt) {

        World world = Universe.get().getWorld(evt.getPlayerRef().getWorldUuid());

        if (world != null) {

            if (world.play)

        }

    }

}
