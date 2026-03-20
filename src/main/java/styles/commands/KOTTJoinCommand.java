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
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class KOTTJoinCommand extends AbstractAsyncPlayerCommand {

    private final OptionalArg<String> worldName;

    public KOTTJoinCommand() {
        super("join", "Join a ongoing match!");

        this.worldName = withOptionalArg("world", "Specify the world with an active match.", ArgTypes.STRING);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        if (!commandContext.isPlayer()) {
            printL("[KOTT Debug] Command only for players!");
            return CompletableFuture.completedFuture(null);
        }

        String _worldName = this.worldName.get(commandContext);
        if (_worldName == null) _worldName = world.getName();

        if (!KOTTMatch.getMatchesList().containsKey(_worldName)) {
            print(commandContext, "[KOTT Debug] There isn't a match happening in this world!");
            return CompletableFuture.completedFuture(null);
        }

        KOTTMatch match = KOTTMatch.getMatchesList().get(_worldName);

        match.join(playerRef);

        return CompletableFuture.completedFuture(null);
    }
}
