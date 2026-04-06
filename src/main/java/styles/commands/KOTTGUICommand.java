package styles.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.ui.KOTTStartUI;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

public class KOTTGUICommand extends AbstractAsyncPlayerCommand {
    public KOTTGUICommand() {
        super("gui", "Open the GUI.");
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        if (!commandContext.isPlayer()) {
            printLog("Command to player only!");
            return CompletableFuture.completedFuture(null);
        }

        if (KOTTMatch.getMatchesList().containsKey(world.getName())) {
            printChat(playerRef, "There is a match already started");
            return CompletableFuture.completedFuture(null);
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            if (KOTTMatch.getMatch(world.getName()) == null) {
                player.getPageManager().openCustomPage(ref, store, new KOTTStartUI(playerRef, world));
            } else {

            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
