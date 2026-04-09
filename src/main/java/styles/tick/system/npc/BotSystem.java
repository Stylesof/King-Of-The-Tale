package styles.tick.system.npc;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import styles.npc.component.BotComponent;
import styles.world.util.WorldBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

// W.I.P. (Problems with bot path finding)
public class BotSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, BotComponent> botComponentType;
    private AtomicLong start = new AtomicLong(System.currentTimeMillis()), end = new AtomicLong();

    public BotSystem(ComponentType<EntityStore, BotComponent> botComponentType) {
        this.botComponentType = botComponentType;
    }

    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        end.set(System.currentTimeMillis());

        if (end.get() - start.get() >= 10000) {
            if (!start.compareAndSet(start.get(), end.get())) return;

            BotComponent botComponent = archetypeChunk.getComponent(i, BotComponent.getComponentType());
            NPCEntity npcComponent = archetypeChunk.getComponent(i, Objects.requireNonNull(NPCEntity.getComponentType()));
            EntityStatMap statMap = archetypeChunk.getComponent(i, EntityStatMap.getComponentType());
            if (statMap == null || npcComponent == null || botComponent == null || botComponent.getMatch() == null || botComponent.getMatch().getZone() == null) return;

            if (botComponent.getPathState()) {
                printLog("Deu certo!?");
                Random random = new Random();

                int radius = botComponent.getMatch().getZone().getZoneRadius();
                int randomX = random.nextInt(-radius, radius);
                int randomZ = random.nextInt(-radius, radius);

                randomX += botComponent.getMatch().getZone().getPosition().x;
                randomZ += botComponent.getMatch().getZone().getPosition().z;

                Vector3d pos = WorldBuilder.alignVectorToWorldSurface(new Vector3i(randomX, 0, randomZ), botComponent.getMatch().getMatchWorld()).toVector3d();
                TransientPath path = new TransientPath();
                path.addWaypoint(pos, new Vector3f(0.0f, 0.0f, 0.0f));

                npcComponent.getPathManager().setTransientPath(path);
            } else{
                npcComponent.getPathManager().setTransientPath(new TransientPath());
            }

            botComponent.setPathState(!botComponent.getPathState());
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(this.botComponentType);
    }
}
