package styles.commands.test;

import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

@Deprecated
public class TestCommand extends AbstractAsyncPlayerCommand {
    public TestCommand() {
        super("test", "");
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        TransientPath paths = new TransientPath();
        paths.addWaypoint(new Vector3d(0, 80, 0), new Vector3f(0, 0, 0));

        print(playerRef, "Creating bot...");
        Pair<Ref<EntityStore>, INonPlayerCharacter> npc = NPCPlugin.get().spawnNPC(
                store,
                "FighterNPC",
                null,
                playerRef.getTransform().getPosition(),
                new Vector3f(0, 0, 0)
        );

        if (npc != null) {
            print(playerRef, "Bot created!");
            NPCEntity npcComponent = store.getComponent(npc.first(), Objects.requireNonNull(NPCEntity.getComponentType()));
            npcComponent.getPathManager().setTransientPath(paths);
            if (npcComponent.getPathManager().isFollowingPath()) {
                print(playerRef, "Bot following path...");
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
