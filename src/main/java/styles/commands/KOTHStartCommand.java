package styles.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTHMatch;

import javax.annotation.Nonnull;

import java.util.concurrent.CompletableFuture;

import static styles.util.Utils.print;

public class KOTHStartCommand extends AbstractAsyncPlayerCommand {

    private final RequiredArg<Integer> team_qnt;
    private final RequiredArg<Integer> area_size;
    private final OptionalArg<String> world_name;

    public KOTHStartCommand() {
        super("start", "Create an KOTH game session!");

        this.team_qnt = this.withRequiredArg("team_qnt", "Quantity of Teams", ArgTypes.INTEGER);
        this.area_size = this.withRequiredArg("area_size", "Area size to conquer (in blocks)", ArgTypes.INTEGER);
        this.world_name = this.withOptionalArg("world_name", "World to create the KOTH (leave empty for use the actual)", ArgTypes.STRING);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if(KOTHMatch.getKOTHMatchStatus()){
            print(commandContext, "[KOTH] Has already an KOTH Match happening!");
            return CompletableFuture.completedFuture(null);
        }

        int _team_qnt = team_qnt.get(commandContext);
        int _area_size = area_size.get(commandContext);
        String _world_name = world_name.get(commandContext);

        if(_area_size < 100 || _area_size > 1000) {
            print(commandContext, "[KOTH] Invalid area size value! Use: (min: 100, max: 1000)");
            return CompletableFuture.completedFuture(null);
        }

        if(_team_qnt < 2 || _team_qnt > 5) {
            print(commandContext, "[KOTH] Invalid team quantity value! Use: (min: 1, max: 5)");
            return CompletableFuture.completedFuture(null);
        }

        if(_world_name == null){
            _world_name = world.getName();
        }

        //print(commandContext, "WorldName: " + _world_name);

        if(Universe.get().getWorld(_world_name) == null) {
            print(commandContext, "[KOTH] Invalid World! Use: /world list to see all worlds available");
            return CompletableFuture.completedFuture(null);
        }else{
            if(Universe.get().isWorldLoadable(_world_name)) {
                // World exists and or loaded
                World worldStart = Universe.get().getWorld(_world_name);
                KOTHMatch.start(
                        playerRef.getTransform().getPosition().toVector3i(),
                        _team_qnt,
                        _area_size,
                        world.getPlayerRefs().stream().toList(),
                        worldStart
                );
                print(commandContext, "[KOTH] Started an KOTH match!");
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
