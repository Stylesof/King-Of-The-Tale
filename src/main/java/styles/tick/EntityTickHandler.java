package styles.tick;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.team.KOTTTeam;
import styles.ui.KOTTPointsUI;
import styles.util.MathHelper;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

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

                    if (match.getPlayersInMatch().containsKey(playerRef.getUuid()) && match.getKOTTMatchStatus() && match.getCanMarkPoint()) {
                        Vector3d playerPos;
                        Vector3d areaPos;

                        for (KOTTTeam team : match.getTeams()) {
                            if (team.getBaseZone().isInside(playerRef.getTransform().getPosition())) {
                                if (!team.getBaseZone().getPlayersInZone().contains(playerRef)) {
                                    team.getBaseZone().addToZone(playerRef);
                                }

                                if (!team.containsPlayer(playerRef)) {
                                    print(playerRef, "You are trespassing enemy base, GET OUT!");

                                    DamageSystems.executeDamage(playerRef.getReference(), commandBuffer, new Damage(Damage.NULL_SOURCE, DamageCause.OUT_OF_WORLD, 2.5f));

                                    playerPos = playerRef.getTransform().getPosition();
                                    playerPos = MathHelper.convertVectorToUnitVector(MathHelper.vectorSub(team.getBaseZone().getPosition().toVector3d(), playerPos), team.getBaseZone().getZoneRadius());
                                    playerPos = MathHelper.scalarVector(playerPos, 20);
                                    playerPos.y = 0;

                                    Vector3d finalPlayerPos = playerPos;
                                    world.execute(() -> {
                                        Velocity vel = commandBuffer.getComponent(playerRef.getReference(), Velocity.getComponentType());
                                        vel.addInstruction(finalPlayerPos, new VelocityConfig(), ChangeVelocityType.Add);
                                    });
                                    return;
                                }

                            } else {
                                if (team.getBaseZone().getPlayersInZone().contains(playerRef)) {
                                    team.getBaseZone().removeFromZone(playerRef);
                                    return;
                                }
                            }
                        }

                        if (!match.getZone().isInside(playerRef.getTransform().getPosition())) {
                            // It's outside the main zone
                            match.getZone().getPlayersInZone().remove(playerRef);
                            world.execute(() -> {
                            Player player = store.getComponent(Objects.requireNonNull(playerRef.getReference()), Player.getComponentType());
                                if (player != null) {
                                    CustomUIHud hud = player.getHudManager().getCustomHud();
                                    if (hud instanceof KOTTPointsUI kphud) {
                                        if (kphud.isInZone) {
                                            player.getHudManager().resetHud(playerRef);
                                            player.getHudManager().setCustomHud(playerRef, new KOTTPointsUI(playerRef, match, false));
                                        }
                                    }
                                }
                            });

                            playerPos = playerRef.getTransform().getPosition();
                            playerPos.y = 0;
                            areaPos = match.getZone().getPosition().toVector3d();
                            areaPos.y = 0;

                            // Main Zone radius + space between the base zone + team zone diameter + extra space
                            double borderSize = match.getZone().getZoneRadius() * 3 + KOTTTeam.distanceBaseFromZone + 20;

                            if (MathHelper.positionDistance(playerPos, areaPos) >= borderSize) {
                                print(playerRef, "You can't leave the match zone!");

                                // Send player back
                                playerPos = MathHelper.convertVectorToUnitVector(MathHelper.vectorSub(areaPos, playerPos), borderSize);
                                playerPos = MathHelper.scalarVector(playerPos, -10);

                                Vector3d finalPlayerPos1 = playerPos;
                                world.execute(() -> {
                                    Velocity vel = commandBuffer.getComponent(playerRef.getReference(), Velocity.getComponentType());
                                    vel.addInstruction(finalPlayerPos1, new VelocityConfig(), ChangeVelocityType.Add);
                                });
                            }
                        } else {
                            if (!match.getZone().getPlayersInZone().contains(playerRef)) {
                                match.getZone().addToZone(playerRef);
                            }

                            world.execute(() -> {
                                Player player = store.getComponent(Objects.requireNonNull(playerRef.getReference()), Player.getComponentType());
                                if (player == null) return;

                                CustomUIHud hud = player.getHudManager().getCustomHud();
                                if (hud instanceof KOTTPointsUI kphud) {
                                    if (!kphud.isInZone) {
                                        player.getHudManager().resetHud(playerRef);
                                        player.getHudManager().setCustomHud(playerRef, new KOTTPointsUI(playerRef, match, true));
                                    }
                                }
                            });

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

    /*
    else {
                            if (!match.getZone().playersInZone.contains(playerRef)) {
                                match.getZone().playersInZone.add(playerRef);
                            }

                            // Player is inside the zone
                            KOTTTeam team = match.getPlayerTeam(playerRef);
                            if (team != null) {
                                print(playerRef, "You marked a point for the Team " + team.getDisplayName());
                            }
                        }
     */
}
