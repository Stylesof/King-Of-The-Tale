package styles.tick.system.player;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

import static styles.util.MessageHandler.printLog;

public class DeathSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        DeathComponent death = archetypeChunk.getComponent(i, DeathComponent.getComponentType());

        if (death != null) {
            Damage damage = death.getDeathInfo();
            Message msg = damage.getDeathMessage(archetypeChunk.getComponent(i, NPCEntity.getComponentType()).getReference(), null);
            printLog("Death message: " + msg.getRawText());
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(NPCEntity.getComponentType(), DeathComponent.getComponentType());
    }
}
