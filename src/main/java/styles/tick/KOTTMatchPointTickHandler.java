package styles.tick;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

import static styles.util.PrintMacros.print;

public class KOTTMatchPointTickHandler extends TickingSystem<EntityStore> {

    private int Ticks = 0;

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store) {
        World world = store.getExternalData().getWorld();

        Ticks++;
        float timeInSeconds = (float) Ticks / world.getTps();


        for (PlayerRef playerRef : world.getPlayerRefs()) {
            print(playerRef, "TPS: " + world.getTps());
            if (timeInSeconds >= 5) {
                print(playerRef, "Time elapsed: " + timeInSeconds);
                Ticks = 0;
            }
        }

    }
}
