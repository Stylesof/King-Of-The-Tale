package styles.world.tick;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.KOTHMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TickHandler extends EntityTickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if(!KOTHMatch.getKOTHMatchStatus()) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        PlayerRef player = store.getComponent(ref, PlayerRef.getComponentType());
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        

    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.or(Player.getComponentType(), NPCEntity.getComponentType());
    }
}
