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
import static styles.util.PrintMacros.printL;

public class KOTTMatchPointTickHandler extends TickingSystem<EntityStore> {

    private int Ticks = 0;

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store) {
        World world = store.getExternalData().getWorld();

        float timeInSeconds = (float) Ticks / world.getTps();

        if (timeInSeconds >= 5) {
            printL("Passou uns tempo ai");
            printL("Tick: " + Ticks);
            Ticks = 0;
        }

        Ticks++;
    }
}
