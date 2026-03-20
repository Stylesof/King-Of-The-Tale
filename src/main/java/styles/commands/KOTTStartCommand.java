package styles.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.commands.world.chunk.ChunkLoadCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.util.log.LogTypes;
import styles.world.KOTTMatch;

import javax.annotation.Nonnull;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;
import static styles.util.log.PrintLog.printLog;

/*
    SOME PROBLEMS TO CLONE WORLDS, SO INSTEAD OF CLONING,
    I WILL JUST CREATE A TOTALLY NEW ONE

 */
public class KOTTStartCommand extends AbstractAsyncPlayerCommand {

    private final DefaultArg<Integer> team_count;
    private final DefaultArg<Integer> area_size;
    private final OptionalArg<String> world_name;

    private final OptionalArg<Vector3i> world_pos;
    private final FlagArg safe;
    private final FlagArg loop;

    public KOTTStartCommand() {
        super("start", "Create an KOTT game session!");

        this.team_count = this.withDefaultArg("team_count", "Count of Teams.", ArgTypes.INTEGER, 1, "(1 Team by default)");
        this.area_size = this.withDefaultArg("zone_size", "Area size to conquer (in blocks).", ArgTypes.INTEGER, 100, "(100 Zone size by default");
        this.world_name = this.withOptionalArg("world", "World to create the KOTT (leave empty for use the actual).", ArgTypes.STRING);

        this.world_pos = this.withOptionalArg("world_pos", "Position of the defined world to spawn the MainZone (Actual position by default).", ArgTypes.VECTOR3I);
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
        int _zone_size = area_size.get(commandContext);
        String _world_name = world_name.get(commandContext);

        Vector3i _world_pos = world_pos.get(commandContext);
        boolean _safe = safe.get(commandContext);
        boolean _loop = loop.get(commandContext);

        // Verify if the inserted world name is valid
        if (_world_name == null) {
            if (!commandContext.isPlayer()) {
                printL("[KOTT Debug] Error: To use this command as not Player, you need to insert an world!");
                return CompletableFuture.completedFuture(null);
            }
            _world_name = world.getName();
        }

        // Verify if the inserted pos is valid
        if (_world_pos == null) {
            if (!commandContext.isPlayer()) {
                printL("[KOTT Debug] Error: To use this command as not Player, you need to insert an position!");
                return CompletableFuture.completedFuture(null);
            }
            _world_pos = playerRef.getTransform().getPosition().toVector3i();
        }

        // Verify area size
        if (_zone_size < 100 || _zone_size > 1000) {
            printLog(playerRef, LogTypes.KOTTInvalidAreaSize);
            return CompletableFuture.completedFuture(null);
        }

        // Verify number of teams
        if (_team_count < 1 || _team_count > 5) {
            printLog(playerRef, LogTypes.KOTTInvalidTeamCount);
            return CompletableFuture.completedFuture(null);
        }

        // Verify if the world is valid
        World _world = Universe.get().getWorld(_world_name);
        if (_world == null) {
            printLog(playerRef, LogTypes.KOTTInvalidWorld);
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<World> fun;
        if (_safe) {
            UUID uuid = UUID.randomUUID();
            String tempWorldName = "temp_" + uuid;
            fun = Universe.get().addWorld(tempWorldName);
        } else {
            fun = CompletableFuture.completedFuture(_world);
        }

        Vector3i final_world_pos = _world_pos;
        return CompletableFuture.completedFuture(null).thenRun(() -> {
            // Other World
            if (_safe) {
                fun.thenAccept(world1 -> {
                    world1.getChunkAsync(final_world_pos.x, final_world_pos.z).thenAccept(WorldChunk::markNeedsSaving).thenRun(() -> {
                        if (!KOTTMatch.createMatch(world1.getName())) {
                            printLog(playerRef, LogTypes.KOTTMatchAlreadyRunning, "World name: \"" + world1.getName() + "\"!");
                            return;
                        }
                        KOTTMatch.getMatchesList().get(world1.getName()).start(
                                final_world_pos,
                                _team_count,
                                _zone_size,
                                _loop,
                                true,
                                playerRef,
                                commandContext,
                                world1
                        );
                    });
                });
                return;
            }

            // Actual World
            _world.getChunkAsync(final_world_pos.x, final_world_pos.z).thenAccept(WorldChunk::markNeedsSaving).thenRun(() -> {
                if (!KOTTMatch.createMatch(_world.getName())) {
                    printLog(playerRef, LogTypes.KOTTMatchAlreadyRunning, "World name: \"" + _world.getName() + "\"!");
                    return;
                }
                KOTTMatch.getMatchesList().get(_world.getName()).start(
                        final_world_pos,
                        _team_count,
                        _zone_size,
                        _loop,
                        false,
                        playerRef,
                        commandContext,
                        _world
                );
            });
        });
    }
}
