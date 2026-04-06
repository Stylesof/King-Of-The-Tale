package styles.tick.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProjectileDetectionSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float v, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        World world = commandBuffer.getExternalData().getWorld();
        KOTTMatch match = KOTTMatch.getMatch(world.getName());

        if (match != null) {
            for (int i = archetypeChunk.getArchetype().getMinIndex(); i < archetypeChunk.getArchetype().length(); i++) {
                if (archetypeChunk.getArchetype().get(i) == ProjectileComponent.getComponentType() || archetypeChunk.getArchetype().get(i) == Projectile.getComponentType()) {
                    world.execute(() -> {
                        if (index < archetypeChunk.size() && index >= 0) {
                            TransformComponent tr = archetypeChunk.getReferenceTo(index).getStore().getComponent(archetypeChunk.getReferenceTo(index), TransformComponent.getComponentType());
                            if (tr != null) {
                                for (KOTTTeam team : match.getTeams()) {
                                    if (team.getBaseZone().isInside(tr.getPosition())) {
                                        world.getEntityStore().getStore().removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }

    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.not(Player.getComponentType());
    }
}
