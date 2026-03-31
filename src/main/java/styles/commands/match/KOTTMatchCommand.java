package styles.commands.match;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.print;

public class KOTTMatchCommand extends AbstractCommandCollection {
    public KOTTMatchCommand() {
        super("match", "Match commands");

        this.addSubCommand(new KOTTMatchListCommand());
    }

    public static class KOTTMatchListCommand extends AbstractAsyncPlayerCommand {

        public KOTTMatchListCommand() {
            super("list", "List all available matches!");
        }

        @Nonnull
        @Override
        protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
            print(playerRef, "[KOTT] Available matches:");
            int i = 1;
            for (String worldName : KOTTMatch.getMatchesList().keySet()) {
                print(playerRef, i + ". World: " + worldName);
                i++;
            }

            return CompletableFuture.completedFuture(null);
        }
    }
}
