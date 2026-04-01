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
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printLog;

public class KOTTStopCommand extends AbstractAsyncPlayerCommand {

    private final OptionalArg<String> world_name;

    public KOTTStopCommand() {
        super("stop", "Force to end an active KOTH game session in any world!");

        this.world_name = withOptionalArg("world", "World to stop the KOTT match.", ArgTypes.STRING);
    }

    /*===========================================================
        FUNCTION TO STOP ANY ACTIVE KOTT MATCH RUNNING IN THE
        DEFINED WORLD
    /*=========================================================*/
    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String _word_name = world_name.get(commandContext);

        // Verify if the world name is valid
        if(_word_name == null) {
            if (!commandContext.isPlayer()) {
                printLog("[KOTT Debug] Error: To use this command as not Player, you need to insert an World name");
                return CompletableFuture.completedFuture(null);
            }
            _word_name = world.getName();
        }

        return KOTTMatch.stop(_word_name, true, commandContext);
    }
}
