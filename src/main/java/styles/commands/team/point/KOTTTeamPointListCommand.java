package styles.commands.team.point;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.team.KOTTTeam;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printChat;

public class KOTTTeamPointListCommand extends AbstractAsyncPlayerCommand {

    private final OptionalArg<String> worldName;

    public KOTTTeamPointListCommand() {
        super("list", "Show the teams points in the match.");

        this.worldName = withOptionalArg("world", "List point of team in the selected world", ArgTypes.STRING);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String _worldName = this.worldName.get(commandContext);

        if (_worldName != null && Universe.get().getWorld(_worldName) == null) {
            print(playerRef, "Invalid world!");
            return CompletableFuture.completedFuture(null);
        } else if (_worldName == null) {
            _worldName = world.getName();
        }

        KOTTMatch match = KOTTMatch.getMatchesList().get(_worldName);
        if (match != null) {
            if (match.getKOTTMatchStatus()) {
                for (KOTTTeam team : match.getTeams()) {
                    print(playerRef, team.getDisplayName() + ": (" + team.teamPoints + "/100) points");
                }
            } else {
                print(playerRef, "The match haven't started yet!");
            }
        }else {
            print(playerRef, "There isn't any matching happening in this World!");
        }

        return CompletableFuture.completedFuture(null);
    }
}
