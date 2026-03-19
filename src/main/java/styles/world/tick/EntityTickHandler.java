package styles.world.tick;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class EntityTickHandler extends EntityTickingSystem<EntityStore> {

    // For Entity 2
    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // verify if actual entity is inside any zone
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());

        if (playerRef != null) {

        }
/*
        if (playerRef != null) {

            KOTTMatch match = KOTTMatch.getMatchesList().get(Universe.get().getWorld(playerRef.getWorldUuid()));

            if (match.getPlayerTeam(playerRef).getBaseZone().isInside(playerRef.getTransform().getPosition())) {
                print(playerRef, "[KOTH] You are inside your base!");
            } else {
                print(playerRef, "Algo aconteceu!");
            }

        }
*/
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.or(Query.and(Player.getComponentType(), DeathComponent.getComponentType()), NPCEntity.getComponentType());
    }
}
