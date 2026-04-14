package styles.ui;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TeleportProvider {

    public static void TeleportPlayer(@Nonnull PlayerRef playerRef, @Nonnull Vector3d pos, @Nonnull World world) {TeleportPlayer(playerRef, pos, new Vector3f(0.0f, 0.0f, 0.0f), world);}
    public static void TeleportPlayer(@Nonnull PlayerRef playerRef, @Nonnull Vector3d pos, @Nonnull Vector3f rot, @Nonnull World world) {
        Teleport tp = Teleport.createForPlayer(world, new Transform(pos, rot));
        if (playerRef.getWorldUuid() != null) {
            Objects.requireNonNull(Universe.get().getWorld(playerRef.getWorldUuid()))
                    .getEntityStore()
                    .getStore()
                    .addComponent(
                            Objects.requireNonNull(playerRef.getReference()),
                            Teleport.getComponentType(),
                            tp
                    );
        }
    }
}
