package styles.tick;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.player.component.KOTTMoney;
import styles.team.KOTTTeam;
import styles.util.MathHelper;
import styles.util.MessageHandler;
import styles.util.item.ItemTypes;
import styles.world.KOTTMatch;
import styles.world.util.WorldBuilder;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static styles.util.MessageHandler.printLog;
import static styles.util.MessageHandler.printNotification;

public class KOTTMatchPointTickHandler extends TickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store) {
        for (KOTTMatch match : KOTTMatch.getMatchesList().values()) {
            if (match != null && match.getKOTTMatchStatus() && match.getCanMarkPoint()) {
                long end = System.currentTimeMillis();

                if (end - match.npcSpawnTimer.get() >= 15000) {
                    if (match.npcSpawnTimer.compareAndSet(match.npcSpawnTimer.get(), end)) {
                        printLog("Time to spawn NPC reached!");

                        // try to spawn an NPC in zone to fight every 15 seconds
                        // Spawn in randow pos with safe 10 blocks from any player
                        // in radius
                        Vector3i pos = getVector3i(match);
                        pos = WorldBuilder.alignVectorToWorldSurface(pos, match.getMatchWorld());

                        int i = 0;
                        if (pos != null) {
                            for (i = 0; i < match.getZone().getPlayersInZone().size(); i++) {
                                double distance = MathHelper.positionDistance(match.getZone().getPlayersInZone().get(i).getTransform().getPosition(), pos.toVector3d());
                                if (distance < 10.0f) {
                                    break;
                                }
                            }
                        }

                        if (i != 0 && i == match.getZone().getPlayersInZone().size()) {
                            AtomicInteger npcCounter = new AtomicInteger(0);
                            match.getMatchWorld().getEntityStore().getStore().forEachEntityParallel((_index, archetypeChunk, commandBuffer) -> {
                                if (!archetypeChunk.getArchetype().contains(Player.getComponentType())) {
                                    NPCEntity npc = archetypeChunk.getComponent(_index, Objects.requireNonNull(NPCEntity.getComponentType()));
                                    if (npc != null && npc.getNPCTypeId().equals("FighterNPC")) {
                                        npcCounter.getAndIncrement();
                                    }
                                }
                            });

                            if (npcCounter.get() < match.npcCounter) {
                                printLog("Spawning NPC in: (" + pos.x + ", " + pos.y + ", " + pos.z + ")");
                                NPCPlugin.get().spawnNPC(
                                        match.getMatchWorld().getEntityStore().getStore(),
                                        "FighterNPC",
                                        null,
                                        pos.toVector3d(),
                                        new Vector3f(0.0f, 0.0f, 0.0f)
                                );
                            }
                        }
                    }
                }

                if (end - match.matchStartTimer.get() < KOTTMatch.timeToPoint) continue;
                if (!match.matchStartTimer.compareAndSet(match.matchStartTimer.get(), end)) continue;

                World world = match.getMatchWorld();

                // get team with more players in area
                Map<KOTTTeam, Integer> teamPlayersCount = new HashMap<>(); // player count per team
                KOTTTeam firstTeam = null, secondTeam = null; // get the first and second team with most players in area
                for (PlayerRef playerRef : match.getZone().getPlayersInZone()) {
                    if (!teamPlayersCount.containsKey(match.getPlayerTeam(playerRef))) {
                        teamPlayersCount.put(match.getPlayerTeam(playerRef), 0);
                    }

                    int playerCount = teamPlayersCount.get(match.getPlayerTeam(playerRef));
                    teamPlayersCount.put(match.getPlayerTeam(playerRef), ++playerCount);

                    if (firstTeam == null) {
                        firstTeam = match.getPlayerTeam(playerRef);
                    } else {
                        if (playerCount > teamPlayersCount.get(firstTeam)) {
                            secondTeam = firstTeam;
                            firstTeam = match.getPlayerTeam(playerRef);
                        } else if (playerCount <= teamPlayersCount.get(firstTeam)) {
                            if (secondTeam != null) {
                                if (playerCount > teamPlayersCount.get(secondTeam)) {
                                    secondTeam = match.getPlayerTeam(playerRef);
                                }
                            } else{
                                secondTeam = match.getPlayerTeam(playerRef);
                            }
                        }
                    }
                }

                if (firstTeam == null || teamPlayersCount.get(firstTeam).equals(teamPlayersCount.get(secondTeam))) continue;

                firstTeam.teamPoints++;
                printLog("Team " + firstTeam.getDisplayName() + " marked a point! (" + firstTeam.teamPoints + " / 100)");

                KOTTTeam finalFirstTeam = firstTeam;
                for (PlayerRef playerRef : match.getPlayersInMatch().values()) {
                    world.execute(() -> {
                        if (playerRef.getReference() != null) {
                            if (match.getZone().getPlayersInZone().contains(playerRef)) {
                                if (finalFirstTeam.containsPlayer(playerRef)){
                                    KOTTMoney.addMoneyToPlayer(playerRef, 10);
                                }
                            }

                            printNotification(
                                    playerRef,
                                    "Marked Point!",
                                    "Team " + finalFirstTeam.getDisplayName() + " marked a point!",
                                    ItemTypes.MITHRIL_SWORD,
                                    MessageHandler.NotificationTypes.SUCCESS
                            );
                        }
                    });
                }

                if (firstTeam.teamPoints == 100) {
                    match.end(firstTeam);
                }
            }
        }
    }

    private static Vector3i getVector3i(KOTTMatch match) {
        Random random = new Random();
        int areaRadius = match.getZone().getZoneRadius();
        int randomX = random.nextInt(-areaRadius, areaRadius);
        int randomZ = random.nextInt(-areaRadius, areaRadius);

        randomX += match.getZone().getPosition().x;
        randomZ += match.getZone().getPosition().z;

        return new Vector3i(randomX, 0, randomZ);
    }
}
