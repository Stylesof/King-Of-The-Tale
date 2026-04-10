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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

public class DeathSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        DeathComponent death = archetypeChunk.getComponent(i, DeathComponent.getComponentType());

        if (death != null) {
            NPCEntity npc = archetypeChunk.getComponent(i, Objects.requireNonNull(NPCEntity.getComponentType()));
            Player player = archetypeChunk.getComponent(i, Player.getComponentType());
            World world;
            KOTTMatch match;

            Damage damage = death.getDeathInfo();

            if (npc != null && npc.getNPCTypeId().equals("FighterNPC") && damage != null) {
                // NPC Died
                world = npc.getWorld();
                assert world != null;
                match = KOTTMatch.getMatch(world.getName());
            } else if (player != null && damage != null) {
                // Play Died
                world = player.getWorld();
                assert world != null;
                match = KOTTMatch.getMatch(world.getName());
                if (match != null) {
                    assert player.getReference() != null;
                    PlayerRef playerRef = world.getEntityStore().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());
                    match.getScoreBoard().addDeath(playerRef);
                }
            } else {
                return;
            }

            if (match == null) return;

            if (damage.getSource() instanceof Damage.EntitySource entitySource) {
                world.execute(() -> {
                    PlayerRef killer = world.getEntityStore().getStore().getComponent(entitySource.getRef(), PlayerRef.getComponentType());
                    if (killer != null) {
                        match.getScoreBoard().addKills(killer, 1);
                        printChat(killer, "Index: " + death);
                    }
                });
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Query.or(Player.getComponentType(), NPCEntity.getComponentType()), DeathComponent.getComponentType());
    }
}
