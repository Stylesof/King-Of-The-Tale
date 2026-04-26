package styles.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printChat;

public class KOTTLeaveCommand extends AbstractAsyncPlayerCommand {
    public KOTTLeaveCommand() {
        super("leave", "Leave the match to the match Lobby");
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (!commandContext.isPlayer()) {
            printChat(commandContext, "Command to player(s) only!");
            return CompletableFuture.completedFuture(null);
        }

        KOTTMatch match = KOTTMatch.getMatch(world.getName());
        if (match != null) {
            match.leave(playerRef);
        }

        return CompletableFuture.completedFuture(null);
    }
}
