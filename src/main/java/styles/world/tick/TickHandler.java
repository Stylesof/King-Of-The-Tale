package styles.world.tick;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.KOTHMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class TickHandler extends EntityTickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        KOTHMatch.tick(dt, index, archetypeChunk, store, commandBuffer);

    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.or(Query.and(Player.getComponentType(), DeathComponent.getComponentType()), NPCEntity.getComponentType());
    }
}
