package styles.tick;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
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
import java.util.concurrent.atomic.AtomicReference;

import static styles.util.MessageHandler.*;

public class KOTTMatchTickHandler extends TickingSystem<EntityStore> {

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
                        match.getMatchWorld().execute(() -> {
                            Vector3i pos = getRandomPos(match);
                            pos = WorldBuilder.alignVectorToWorldSurface(pos, match.getMatchWorld());

                            int i = 0;
                            if (pos != null) {
                                for (i = 0; i < match.getZone().getPlayersInZone().size(); i++) {
                                    double distance = MathHelper.positionDistance(match.getZone().getPlayersInZone().get(i).getTransform().getPosition(), pos.toVector3d());
                                    if (distance < 10.0f) {
                                        break;
                                    }
                                }
                            } else {
                                printLog("Invalid NPC spawn position");
                            }

                            if (i != 0 && i == match.getZone().getPlayersInZone().size()) {
                                AtomicInteger npcCounter = new AtomicInteger(0);
                                match.getMatchWorld().execute(() -> match.getMatchWorld().getEntityStore().getStore().forEachChunk(NPCEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
                                    for(int _index = 0; _index < archetypeChunk.size(); _index++) {
                                        NPCEntity npc = archetypeChunk.getComponent(_index, Objects.requireNonNull(NPCEntity.getComponentType()));

                                        if (npc != null) {
                                            if (npc.getNPCTypeId().equals("FighterNPC")) {
                                                npcCounter.getAndIncrement();
                                            }
                                        }
                                    }
                                }));

                                if (npcCounter.get() < match.npcCounter) {
                                    printLog("Spawning NPC in: (" + pos.x + ", " + pos.y + ", " + pos.z + ")");
                                    NPCPlugin.get().spawnNPC(
                                            match.getMatchWorld().getEntityStore().getStore(),
                                            "FighterNPC",
                                            null,
                                            pos.toVector3d(),
                                            new Vector3f(0.0f, 0.0f, 0.0f)
                                    );
                                } else {
                                    printLog("NPC in Zone limit reached!");
                                }
                            }
                        });
                    }
                }

                if (end - match.matchStartTimer.get() < KOTTMatch.timeToPoint) continue;
                if (!match.matchStartTimer.compareAndSet(match.matchStartTimer.get(), end)) continue;

                World world = match.getMatchWorld();

                // get team with more players in area
                KOTTTeam firstTeam = null, secondTeam = null; // get the first and second team with most players in area
                for (KOTTTeam team : match.getTeamCountInZone().keySet()) {
                    if (firstTeam == null) {
                        firstTeam = team;
                        continue;
                    }

                    if (match.getTeamCountInZone().get(team) > match.getTeamCountInZone().get(firstTeam)) {
                        secondTeam = firstTeam;
                        firstTeam = team;
                    } else {
                        if (secondTeam == null) {
                            secondTeam = team;
                        } else {
                            if (match.getTeamCountInZone().get(team) > match.getTeamCountInZone().get(secondTeam)) {
                                secondTeam = team;
                            }
                        }
                    }
                }

                if (firstTeam == null || match.getTeamCountInZone().get(firstTeam).equals(match.getTeamCountInZone().get(secondTeam))) continue;

                firstTeam.teamPoints++;
                printLog("Team " + firstTeam.getDisplayName() + " marked a point! (" + firstTeam.teamPoints + " / 100)");

                KOTTTeam finalFirstTeam = firstTeam;
                for (PlayerRef playerRef : match.getPlayersInMatch().values()) {
                    world.execute(() -> {
                        if (playerRef.getReference() != null) {
                            if (match.getZone().getPlayersInZone().contains(playerRef)) {
                                if (finalFirstTeam.containsPlayer(playerRef)){
                                    KOTTMoney.addMoneyToPlayer(playerRef, 100);
                                    printChat(playerRef, "+$100.00 for marking point!");
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

    private static Vector3i getRandomPos(KOTTMatch match) {
        Random random = new Random();
        int areaRadius = match.getZone().getZoneRadius();
        int randomX = random.nextInt(-areaRadius, areaRadius);
        int randomZ = random.nextInt(-areaRadius, areaRadius);

        randomX += match.getZone().getPosition().x;
        randomZ += match.getZone().getPosition().z;

        return new Vector3i(randomX, 0, randomZ);
    }
}
