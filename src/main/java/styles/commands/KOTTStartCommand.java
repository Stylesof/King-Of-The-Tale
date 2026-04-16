package styles.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.util.MessageHandler;
import styles.util.log.LogTypes;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

/*
    SOME PROBLEMS TO CLONE WORLDS, SO INSTEAD OF CLONING,
    I WILL JUST CREATE A TOTALLY NEW ONE

 */
public class KOTTStartCommand extends AbstractAsyncPlayerCommand {

    private final DefaultArg<Integer> team_count;
    private final DefaultArg<Integer> zone_radius;
    private final OptionalArg<String> world_name;

    private final OptionalArg<Vector3i> world_pos;
    private final FlagArg safe;
    private final FlagArg loop;

    public KOTTStartCommand() {
        super("start", "Create an KOTT game session!");

        this.team_count = this.withDefaultArg("teamCount", "Count of Teams.", ArgTypes.INTEGER, 1, "(1 Team by default)");
        this.zone_radius = this.withDefaultArg("zoneSize", "Area size to conquer (in blocks).", ArgTypes.INTEGER, 100, "(100 Zone size by default");
        this.world_name = this.withOptionalArg("world", "World to create the KOTT (leave empty for use the actual).", ArgTypes.STRING);

        this.world_pos = this.withOptionalArg("worldPos", "Position of the defined world to spawn the MainZone (Actual position by default).", ArgTypes.VECTOR3I);
        this.safe = this.withFlagArg("safe", "Creates the match on a temporary World (Deleted after the end of the match).");
        this.loop = this.withFlagArg("loop", "Restart the match after the end. WIP, after the end of the match, a temporary world is created and started a match on it.");
    }

    /*===========================================================
        FUNCTION TO CREATE AN KOTT MATCH AND SET THE PLAYERS
        OF THE DEFINED WORLD INTO THE MATCH TEAMS
    ===========================================================*/
    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        int _team_count = team_count.get(commandContext);
        int _zone_radius = zone_radius.get(commandContext);
        String _world_name = world_name.get(commandContext);

        Vector3i _world_pos = world_pos.get(commandContext);
        boolean _safe = safe.get(commandContext);
        boolean _loop = loop.get(commandContext);

        // Verify if the inserted world name is valid
        if (_world_name == null) {
            if (!commandContext.isPlayer()) {
                MessageHandler.printLog("ERROR: To use this command as not Player, you need to insert an world!");
                return CompletableFuture.completedFuture(null);
            }
            _world_name = world.getName();
        }

        // Verify if the inserted pos is valid
        if (_world_pos == null) {
            if (!commandContext.isPlayer()) {
                printLog("To use this command as non-player, you need to insert an position!");
                return CompletableFuture.completedFuture(null);
            }
            _world_pos = playerRef.getTransform().getPosition().toVector3i();
        }

        // Verify if the world is valid
        World _world = Universe.get().getWorld(_world_name);
        if (_world == null) {
            printChat(playerRef, LogTypes.KOTTInvalidWorld, Color.RED);
            return CompletableFuture.completedFuture(null);
        }

        return KOTTMatch.tryCreateMatch(_world_pos, _team_count, _zone_radius, _safe, _loop, playerRef, commandContext, _world, world, playerRef.getTransform().getPosition().toVector3i()).thenRun(() -> {});
    }
}
