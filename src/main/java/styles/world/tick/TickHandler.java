package styles.world.tick;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.print;

public class TickHandler extends EntityTickingSystem<EntityStore> {

    // For Entity 2
    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // verify if actual entity is inside any zone
        PlayerRef player = store.getComponent(archetypeChunk.getReferenceTo(index), PlayerRef.getComponentType());

        if (player != null) {

            KOTTMatch match = KOTTMatch.getMatchesList().get(Universe.get().getWorld(player.getWorldUuid()));

            if (match.getPlayerTeam(player).getBaseZone().isInside(player.getTransform().getPosition())) {
                print(player, "[KOTH] You are inside your base!");
            }

        }

    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.or(Query.and(Player.getComponentType(), DeathComponent.getComponentType()), NPCEntity.getComponentType());
    }
}
