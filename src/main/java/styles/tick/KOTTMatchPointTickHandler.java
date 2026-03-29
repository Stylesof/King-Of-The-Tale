package styles.tick;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.team.KOTTTeam;
import styles.ui.KOTTPointsUI;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static styles.util.PrintMacros.printL;

public class KOTTMatchPointTickHandler extends TickingSystem<EntityStore> {

    private long start, end;

    public KOTTMatchPointTickHandler() { start = System.currentTimeMillis(); }

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store) {
        end = System.currentTimeMillis();

        if (end - start >= KOTTMatch.timeToPoint) {
            start = end;

            for (World world : Universe.get().getWorlds().values()) {
                KOTTMatch match = KOTTMatch.getMatchesList().get(world.getName());
                if (match != null && match.getKOTTMatchStatus() && match.getCanMarkPoint()) {

                    // get team with more players in area
                    Map<KOTTTeam, Integer> teamPlayersCount = new HashMap<>(); // player count per team
                    KOTTTeam firstTeam = null, secondTeam = null;
                    for (PlayerRef playerRef : match.getZone().getPlayersInZone()) {
                        if (!teamPlayersCount.containsKey(match.getPlayerTeam(playerRef))) {
                            teamPlayersCount.put(match.getPlayerTeam(playerRef), 0);
                        }

                        int playerCount = teamPlayersCount.get(match.getPlayerTeam(playerRef));
                        teamPlayersCount.put(match.getPlayerTeam(playerRef), playerCount + 1);
                        playerCount += 1;

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
                    printL("Team " + firstTeam.getDisplayName() + " marked a point! (" + firstTeam.teamPoints + " / 100)");

                    for (PlayerRef playerRef : match.getPlayersInMatch().values()) {
                        world.execute(() -> {
                            Player player = world.getEntityStore().getStore().getComponent(Objects.requireNonNull(playerRef.getReference()), Player.getComponentType());
                            player.getHudManager().resetHud(playerRef);
                            player.getHudManager().setCustomHud(playerRef, new KOTTPointsUI(playerRef, match, match.getPlayersInMatch().containsValue(playerRef)));
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
