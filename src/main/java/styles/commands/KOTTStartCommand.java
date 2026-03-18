package styles.commands;

import com.hypixel.hytale.builtin.path.commands.WorldPathSaveCommand;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.WorldConfigProvider;
import com.hypixel.hytale.server.core.universe.world.commands.world.WorldAddCommand;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.util.log.LogTypes;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;
import static styles.util.log.PrintLog.printLog;

/*
    SOME PROBLEMS TO CLONE WORLDS, SO INSTEAD OF CLONING,
    I WILL JUST CREATE A TOTALLY NEW ONE

 */
public class KOTTStartCommand extends AbstractAsyncPlayerCommand {

    private final RequiredArg<Integer> team_count;
    private final RequiredArg<Integer> area_size;
    private final OptionalArg<String> world_name;

    private final OptionalArg<Vector3i> world_pos;
    //private final OptionalArg<Boolean> clone;
    private final OptionalArg<Boolean> new_world;
    private final OptionalArg<Boolean> loop;

    public KOTTStartCommand() {
        super("start", "Create an KOTT game session!");

        this.team_count = this.withRequiredArg("team_qnt", "Quantity of Teams", ArgTypes.INTEGER);
        this.area_size = this.withRequiredArg("area_size", "Area size to conquer (in blocks)", ArgTypes.INTEGER);
        this.world_name = this.withOptionalArg("world_name", "World to create the KOTT (leave empty for use the actual)", ArgTypes.STRING);

        this.world_pos = this.withOptionalArg("world_pos", "Position of the defined world to spawn the MainZone (Actual position by default)", ArgTypes.VECTOR3I);
        //this.clone = this.withOptionalArg("clone", "Clone the world before start? (This allow to play without making permanent modifications in the world)", ArgTypes.BOOLEAN);
        this.new_world = this.withOptionalArg("new_world", "Create a totally new world to start the match? (To avoid your own world destruction)", ArgTypes.BOOLEAN);
        this.loop = this.withOptionalArg("loop", "Should restart the match after the end? (Affected by the --clone option)", ArgTypes.BOOLEAN);
    }

    /*===========================================================
        FUNCTION TO CREATE AN KOTT MATCH AND SET THE PLAYERS
        OF THE DEFINED WORLD INTO THE MATCH TEAMS
    ===========================================================*/
    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        int _team_qnt = team_count.get(commandContext);
        int _area_size = area_size.get(commandContext);
        String _world_name = world_name.get(commandContext);

        Vector3i _world_pos = world_pos.get(commandContext);
        //Boolean _clone = clone.get(commandContext);
        Boolean _new_world = new_world.get(commandContext);
        Boolean _loop = loop.get(commandContext);

        // Verify if the inserted world name is valid
        if (_world_name == null) {
            if (!commandContext.isPlayer()) {
                printL("[KOTT Debug] To use this command as not Player, you need to insert an world!");
                return CompletableFuture.completedFuture(null);
            }
            _world_name = world.getName();
        }

        // Verify if the inserted pos is valid
        if (_world_pos == null) {
            if (!commandContext.isPlayer()) {
                printL("[KOTT Debug] To use this command as not Player, you need to insert an position!");
                return CompletableFuture.completedFuture(null);
            }
            _world_pos = playerRef.getTransform().getPosition().toVector3i();
        }

        // Verify area size
        if (_area_size < 100 || _area_size > 1000) {
            printLog(playerRef, LogTypes.KOTTInvalidAreaSize);
            return CompletableFuture.completedFuture(null);
        }

        // Verify number of teams
        if (_team_qnt < 2 || _team_qnt > 5) {
            printLog(playerRef, LogTypes.KOTTInvalidTeamCount);
            return CompletableFuture.completedFuture(null);
        }

        // Verify if the world is valid
        World _world = Universe.get().getWorld(_world_name);
        if (_world == null) {
            printLog(playerRef, LogTypes.KOTTInvalidWorld);
            return CompletableFuture.completedFuture(null);
        }

        /*
        if (_clone == null) {
            _clone = false;
        }
        */

        /*
        if (_clone) {
            UUID uuid = UUID.randomUUID();
            _world_name = _world_name + "_clone_temp_" + uuid;
            WorldConfig _world_config = _world.getWorldConfig();
            CompletableFuture<World> fun = Universe.get().addWorld(_world_name);
            fun.join();
        }
        */

        if (_new_world == null) {
            _new_world = false;
        }

        if (_loop == null) {
            _loop = false;
        }


        // Verify if has an active match in the world
        if (!KOTTMatch.createMatch(_world_name)) {
            printLog(playerRef, LogTypes.KOTTMatchAlreadyRunning, "World name: \"" + _world_name + "\"!");
            return CompletableFuture.completedFuture(null);
        }

        // World exists and or loaded
        KOTTMatch.getMatchesList().get(_world_name).start(
                _world_pos,
                _team_qnt,
                _area_size,
                _loop,
                _new_world,
                playerRef,
                _world,
                commandContext
        );
        printLog(playerRef, LogTypes.KOTTMatchStarted, "World name: \"" + _world_name + "\"!");

        return CompletableFuture.completedFuture(null);
    }
}
