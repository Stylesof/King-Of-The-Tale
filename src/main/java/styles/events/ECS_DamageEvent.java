package styles.events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.player.component.InvulnerabilityComponent;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static styles.util.MessageHandler.printChat;

public class ECS_DamageEvent extends EntityEventSystem<EntityStore, Damage> {
    public ECS_DamageEvent() {
        super(Damage.class);
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {
        PlayerRef playerRef = commandBuffer.getComponent(archetypeChunk.getReferenceTo(i), PlayerRef.getComponentType());
        World world = commandBuffer.getExternalData().getWorld();
        KOTTMatch match = KOTTMatch.getMatch(world.getName());
        if (match != null && playerRef != null && playerRef.getReference() != null) {
            for (KOTTTeam teams : match.getTeams()) {
                if (teams.getBaseZone().isInside(playerRef.getTransform().getPosition()) || match.getIsEnding()) {
                    world.execute(() -> {
                        InvulnerabilityComponent ic = store.getComponent(playerRef.getReference(), InvulnerabilityComponent.getComponentType());
                        if (ic != null) {
                            if (ic.getActiveStatus()) {
                                EntityStatMap statMap = store.getComponent(playerRef.getReference(), EntityStatMap.getComponentType());
                                if (statMap != null) {
                                    statMap.maximizeStatValue(DefaultEntityStatTypes.getHealth());
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
        return Query.and(Player.getComponentType(), Query.not(DeathComponent.getComponentType()));
    }
}
