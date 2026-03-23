package styles.tick;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.player.KnockbackPredictionSystems;
import com.hypixel.hytale.server.core.modules.entity.player.KnockbackSimulation;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.Knockback;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTT;
import styles.team.KOTTTeam;
import styles.util.MathHelper;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

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
            World world = Universe.get().getWorld(playerRef.getWorldUuid());
            if (world != null) {
                KOTTMatch match = KOTTMatch.getMatchesList().get(world.getName());
                if (match != null) {
                    if (match.getPlayersInMatch().containsKey(playerRef.getUuid())) {
                        for (KOTTTeam team : match.getTeams().values()) {
                            if (team.getBaseZone().isInside(playerRef.getTransform().getPosition())) {
                                if (!team.getBaseZone().playersInZone.contains(playerRef)) {
                                    team.getBaseZone().addToZone(playerRef);
                                }

                                if (!team.containsPlayer(playerRef)) {
                                    print(playerRef, "You are trespassing enemy base, GET OUT!");
                                    print(playerRef, "Your pos: " + playerRef.getTransform().getPosition());

                                    DamageSystems.executeDamage(playerRef.getReference(), commandBuffer, new Damage(Damage.NULL_SOURCE, DamageCause.OUT_OF_WORLD, 5.0f));

                                    // TODO: (WIP) Push player system
                                    Vector3d enemyPos = playerRef.getTransform().getPosition();
                                    enemyPos = MathHelper.convertVectorToUnitVector(MathHelper.vectorSub(team.getBaseZone().getPosition().toVector3d(), enemyPos), team.getBaseZone().getZoneRadius());

                                    enemyPos.x = -enemyPos.x;
                                    enemyPos.y = -enemyPos.y;
                                    enemyPos.z = -enemyPos.z;

                                    enemyPos = MathHelper.scalarVector(enemyPos, 10);

                                    KnockbackSimulation kb = new KnockbackSimulation();
                                    kb.addRequestedVelocity(enemyPos);

                                    //Teleport tp = Teleport.createForPlayer(new Transform(enemyPos));
                                    commandBuffer.addComponent(playerRef.getReference(), KnockbackSimulation.getComponentType(), kb);
                                    print(playerRef, "Your pos: " + playerRef.getTransform().getPosition());
                                    return;
                                }

                            } else {
                                if (team.getBaseZone().playersInZone.contains(playerRef)) {
                                    team.getBaseZone().removeFromZone(playerRef);
                                    return;
                                }
                            }
                        }
                    }
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
