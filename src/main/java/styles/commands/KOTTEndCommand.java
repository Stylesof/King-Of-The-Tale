package styles.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.util.MessageHandler;
import styles.util.item.ItemTypes;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.*;

public class KOTTEndCommand extends AbstractAsyncPlayerCommand {
    private final OptionalArg<String> world_name;

    public KOTTEndCommand() {
        super("end", "Safe end the match in any world!");

        this.world_name = withOptionalArg("world", "World to stop the KOTT match.", ArgTypes.STRING);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String _word_name = world_name.get(commandContext);

        // Verify if the world name is valid
        if (_word_name == null) {
            if (!commandContext.isPlayer()) {
                printLog("[KOTT Debug] Error: To use this command as not Player, you need to insert an World name");
                return CompletableFuture.completedFuture(null);
            }
            _word_name = world.getName();
        }

        KOTTMatch match = KOTTMatch.getMatchesList().get(_word_name);
        if (match == null || match.getIsEnding()) {
            printNotification(
                    playerRef,
                    "Failed to stop the match!",
                    "This world has not a valid match!",
                    ItemTypes.MITHRIL_SWORD,
                    NotificationTypes.ERROR
            );
            printLog("There isn't an match happening in this world!");
            return CompletableFuture.completedFuture(null);
        }

        return match.end(null);
    }
}
