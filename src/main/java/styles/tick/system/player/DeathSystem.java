package styles.tick.system.player;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.player.component.KOTTMoney;
import styles.player.util.InventoryManagement;
import styles.team.KOTTTeam;
import styles.thread.ThreadSafetyProvider;
import styles.util.ColorHandler;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static styles.util.MessageHandler.printChat;

public class DeathSystem extends EntityTickingSystem<EntityStore> {

    private final static Map<PlayerRef, AtomicBoolean> playerCanChangeScore = new HashMap<>();

    @Deprecated
    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        DeathComponent death = archetypeChunk.getComponent(i, DeathComponent.getComponentType());

        if (death != null) {
            NPCEntity npc = archetypeChunk.getComponent(i, Objects.requireNonNull(NPCEntity.getComponentType()));
            Player player = archetypeChunk.getComponent(i, Player.getComponentType());
            PlayerRef playerRef = null;

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
                    playerRef = world.getEntityStore().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());

                    if (!playerCanChangeScore.containsKey(playerRef)) {
                        playerCanChangeScore.put(playerRef, new AtomicBoolean(true));
                    }

                    if (playerCanChangeScore.get(playerRef).get()) {
                        if (!playerCanChangeScore.get(playerRef).compareAndExchange(true, false)) return;
                        match.getScoreBoard().addDeath(playerRef);
                        PlayerRef finalPlayerRef = playerRef;

                        ThreadSafetyProvider.DeathSystemScheduler.schedule(() -> playerCanChangeScore.remove(finalPlayerRef), 3, TimeUnit.SECONDS);
                    }

                    if (playerRef != null) {
                        KOTTTeam team = match.getPlayerTeam(playerRef);
                        if (team != null) {
                            InventoryManagement.KOTTStarterKit.applyKit(player, team);
                        }
                    }
                }
            } else {
                return;
            }

            if (match == null) return;

            if (damage.getSource() instanceof Damage.EntitySource entitySource) {
                world.execute(() -> {
                    PlayerRef killer = world.getEntityStore().getStore().getComponent(entitySource.getRef(), PlayerRef.getComponentType());
                    if (killer != null) {
                        if (!playerCanChangeScore.containsKey(killer)) {
                            playerCanChangeScore.put(killer, new AtomicBoolean(true));
                        }

                        if (playerCanChangeScore.get(killer).get()) {
                            if (!playerCanChangeScore.get(killer).compareAndExchange(true, false)) return;
                            match.getScoreBoard().addKills(killer, 1);
                            KOTTMoney.addMoneyToPlayer(killer, 100);
                            printChat(killer, "+$100.00 for the kill!");

                            ThreadSafetyProvider.DeathSystemScheduler.schedule(() -> playerCanChangeScore.remove(killer), 3, TimeUnit.SECONDS);
                        }
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
