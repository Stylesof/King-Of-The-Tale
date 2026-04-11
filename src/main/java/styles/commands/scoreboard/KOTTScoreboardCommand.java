package styles.commands.scoreboard;

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

public class KOTTScoreboardCommand extends AbstractAsyncPlayerCommand {
    public KOTTScoreboardCommand() {
        super("scoreboard", "Open the Scoreboard of the actual match!");
        this.addAliases("sb");
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (!commandContext.isPlayer()) {
            printChat(commandContext, "Command to player(s) only!");
            return CompletableFuture.completedFuture(null);
        }

        KOTTMatch match = KOTTMatch .getMatch(world.getName());

        if (match == null) {
            printChat(playerRef, "There isn't any match happening right now!");
            return CompletableFuture.completedFuture(null);
        }

        world.execute(() -> {
            Player player = store.getComponent(ref, Player.getComponentType());

            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, new KOTTScoreBoardUI(playerRef, match));
            } else {
                printChat(playerRef, "Failed to open the GUI!");
            }
        });

        return CompletableFuture.completedFuture(null);
    }
}
