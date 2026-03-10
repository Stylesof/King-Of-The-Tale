package styles.world.tick;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTHMatch;
import styles.world.KOTHZone;

import javax.annotation.Nonnull;

public class GlobalUpdateSystem extends TickingSystem<EntityStore> {
    @Override
    public void tick(float v, int i, @Nonnull Store<EntityStore> store) {

        KOTHMatch.tick();

    }
}
