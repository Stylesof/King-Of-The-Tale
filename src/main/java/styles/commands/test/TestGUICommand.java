package styles.commands.test;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.ui.KOTTPointsUI;
import styles.ui.KOTTStartUI;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

@Deprecated
public class TestGUICommand extends AbstractAsyncPlayerCommand {
    public TestGUICommand() {
        super("testgui", "");
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        Player player = store.getComponent(ref, Player.getComponentType());
        KOTTMatch match = KOTTMatch.getMatchesList().get(world.getName());
        if (match != null) {
            player.getHudManager().setCustomHud(playerRef, new KOTTPointsUI(playerRef, match));
        }
        return CompletableFuture.completedFuture(null);
    }
}
