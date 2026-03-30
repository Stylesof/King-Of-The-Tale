package styles.tick;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.ui.KOTTPointsUI;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class KOTTMatchZoneCompassTickHandler extends TickingSystem<EntityStore> {

    private long start, end;

    public KOTTMatchZoneCompassTickHandler() { start = System.currentTimeMillis(); }

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store) {
        end = System.currentTimeMillis();

        if (end - start >= 100) {
            // update icon after 0.1 second
            start = System.currentTimeMillis();

            CompletableFuture.runAsync(() -> {
                for (KOTTMatch match : KOTTMatch.getMatchesList().values()) {
                    if (match == null || !match.getKOTTMatchStatus() || !match.getCanMarkPoint()) continue;
                    for (PlayerRef playerRef : match.getPlayersInMatch().values()) {{
                        match.getZone().getWorld().execute(() -> {
                            Player player = match.getZone().getWorld().getEntityStore().getStore().getComponent(Objects.requireNonNull(playerRef.getReference()), Player.getComponentType());
                            if (player != null) {
                                player.getHudManager().setCustomHud(playerRef, new KOTTPointsUI(playerRef, match, match.getZone().getPlayersInZone().contains(playerRef)));
                            }
                        });
                    }}
                }
            });
        }
    }
}
