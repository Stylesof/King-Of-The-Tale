package styles.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class KOTTClearMarkersCommand extends AbstractAsyncPlayerCommand {
    public KOTTClearMarkersCommand() {
        super("clear-markers", "Remove all UserMapMarkers from this World.");
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UserMapMarkersStore userMapMarkersStore = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());
        for (UserMapMarker marker : userMapMarkersStore.getUserMapMarkers()) {
            userMapMarkersStore.removeUserMapMarker(marker.getId());
        }

        return CompletableFuture.completedFuture(null);
    }
}
