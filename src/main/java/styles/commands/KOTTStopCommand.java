package styles.commands;

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
import styles.util.log.LogTypes;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.print;
import static styles.util.log.PrintLog.printLog;

public class KOTTStopCommand extends AbstractAsyncPlayerCommand {

    private final OptionalArg<String> world_name;

    public KOTTStopCommand() {
        super("stop", "Stop an active KOTH game session in any world!");

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
        World _world;

        // Verify if the world name is valid
        if(_word_name == null) {
            _word_name = world.getName();
        }

        // Verify if the world is valid
        _world = Universe.get().getWorld(_word_name);
        if(_world == null) {
            printLog(playerRef, LogTypes.KOTTInvalidWorld);
            return CompletableFuture.completedFuture(null);
        }

        // Verify if the world has an active match
        KOTTMatch match = KOTTMatch.getMatchesList().get(_world);
        if(match == null || !match.getKOTHMatchStatus()){
            print(commandContext, "[KOTH] There isn't any match happening in the moment!");
        }else{
            KOTTMatch.getMatchesList().get(_world).stop();
            print(commandContext, "[KOTH] Stopped the active KOTH match!");
        }

        return  CompletableFuture.completedFuture(null);
    }
}
