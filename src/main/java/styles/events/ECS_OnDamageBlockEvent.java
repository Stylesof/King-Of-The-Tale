package styles.events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static styles.util.MessageHandler.printLog;

public class ECS_OnDamageBlockEvent extends EntityEventSystem<EntityStore, DamageBlockEvent> {
    public ECS_OnDamageBlockEvent() {
        super(DamageBlockEvent.class);
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull DamageBlockEvent damageBlockEvent) {
        World world = commandBuffer.getExternalData().getWorld();
        KOTTMatch match = KOTTMatch.getMatchesList().get(world.getName());
        if (match != null) {
            for (KOTTTeam teams : match.getTeams()) {
                if (teams.getBaseZone().isInside(damageBlockEvent.getTargetBlock().toVector3d())) {
                    damageBlockEvent.setCancelled(true);
                }
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
