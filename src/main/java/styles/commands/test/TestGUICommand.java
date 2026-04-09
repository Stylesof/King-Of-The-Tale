package styles.commands.test;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.ui.KOTTScoreBoardUI;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

@Deprecated (
        forRemoval = true
)
public class TestGUICommand extends AbstractAsyncPlayerCommand {
    public TestGUICommand() {
        super("testgui", "");
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        KOTTMatch match = KOTTMatch.getMatch(world.getName());

        if (match != null) {
            printChat(playerRef, "Deu bom");
            world.execute(() -> {
                Player player = store.getComponent(ref, Player.getComponentType());
                player.getPageManager().openCustomPage(ref, store, new KOTTScoreBoardUI(playerRef, match));
            });
        } else {
            printChat(playerRef, "Deu ruim");
        }

        return CompletableFuture.completedFuture(null);
    }
}
