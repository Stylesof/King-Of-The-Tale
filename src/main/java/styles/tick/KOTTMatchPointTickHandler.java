package styles.tick;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.player.component.KOTTMoney;
import styles.team.KOTTTeam;
import styles.util.MessageHandler;
import styles.util.item.ItemTypes;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static styles.util.MessageHandler.printLog;
import static styles.util.MessageHandler.printNotification;

public class KOTTMatchPointTickHandler extends TickingSystem<EntityStore> {

    private AtomicLong start = new AtomicLong(System.currentTimeMillis()), end = new AtomicLong();

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store) {
        end.set(System.currentTimeMillis());

        if (end.get() - start.get() >= KOTTMatch.timeToPoint) {
            if (!start.compareAndSet(start.get(), end.get())) return;

            for (World world : Universe.get().getWorlds().values()) {
                KOTTMatch match = KOTTMatch.getMatchesList().get(world.getName());
                if (match != null && match.getKOTTMatchStatus() && match.getCanMarkPoint()) {
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
    }
}
