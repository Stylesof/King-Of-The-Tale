package styles.tick.system.npc;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.npc.component.BotComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static styles.util.MessageHandler.printLog;

public class BotSystem extends EntityTickingSystem<EntityStore> {

    AtomicLong start = new AtomicLong(System.currentTimeMillis()), end;

    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        if (end == null || end.get() - start.get() >= 10000) {
            if (end == null) {
                end = new AtomicLong(System.currentTimeMillis());
            }
            if (!start.compareAndSet(start.get(), end.get())) return;

            BotComponent botComponent = archetypeChunk.getComponent(i, BotComponent.getComponentType());
            if (botComponent == null) return;

            NPCEntity npcComponent = archetypeChunk.getComponent(i, Objects.requireNonNull(NPCEntity.getComponentType()));
            botComponent.setPathState(!botComponent.getPathState());
            if (botComponent.getPathState()) {
                // Is pathing...
                printLog("Deu certo!?");
                npcComponent.getPathManager().setTransientPath(botComponent.getPaths());
            } else {
                npcComponent.getPathManager().setTransientPath(new TransientPath());
            }
        }

        end.set(System.currentTimeMillis());
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(NPCEntity.getComponentType(), BotComponent.getComponentType(), Query.not(DeathComponent.getComponentType()));
    }
}
