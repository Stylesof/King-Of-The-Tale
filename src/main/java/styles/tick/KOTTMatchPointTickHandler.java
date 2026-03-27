package styles.tick;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import java.util.UUID;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

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
                    UUID pointTeam = null;
                    for (KOTTTeam team : match.getTeams().values()) {
                        if (pointTeam == null) {
                            pointTeam = team.teamID;
                        } else {
                            if (team.getBaseZone().playersInZone.size() > match.getTeams().get(pointTeam).getBaseZone().playersInZone.size()) {
                                pointTeam = team.teamID;
                            }
                        }
                    }
                    if (pointTeam != null) {
                        printL("Maked point!");
                        match.getTeams().get(pointTeam).teamPoints++;
                    }
                }
            }
        }
    }
}
