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
import java.util.UUID;

public class KOTTMatchPointTickHandler extends TickingSystem<EntityStore> {

    private long start = 0, end = 0;

    public KOTTMatchPointTickHandler() {}

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store) {
        end = System.currentTimeMillis();

        if (end - start >= KOTTMatch.timeToPoint) {
            start = end;

            for (World world : Universe.get().getWorlds().values()) {
                KOTTMatch match = KOTTMatch.getMatchesList().get(world.getName());
                if (match != null && match.getKOTTMatchStatus()) {

                    // get team with more players in area
                    Map<KOTTTeam, Integer> teamPlayersCount = new HashMap<>(); // player count per team
                    for (PlayerRef playerRef : match.getZone().playersInZone) {
                        if (!teamPlayersCount.containsKey(match.getPlayerTeam(playerRef))) {
                            teamPlayersCount.put(match.getPlayerTeam(playerRef), 1);
                        } else {
                            int playerCount = teamPlayersCount.get(match.getPlayerTeam(playerRef));
                            teamPlayersCount.put(match.getPlayerTeam(playerRef), playerCount + 1);
                        }
                    }

                    // Sort the teams an verify the 2 highest, if equal, null, if not, return highest


                    for (PlayerRef playerRef : match.getPlayersInMatch().values()) {
                        if (team != null) {
                            Player player = world.getEntityStore().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                            world.execute(() -> {
                                player.getHudManager().setCustomHud(playerRef, new KOTTPointsUI(playerRef, match));
                            });
                        }
                    }
                }
            }
        }
    }
}
