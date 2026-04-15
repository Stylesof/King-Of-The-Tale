package styles.util;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

import static styles.util.MessageHandler.printLog;

public class TeleportProvider {

    public static void TeleportPlayer(@Nonnull PlayerRef playerRef, @Nonnull Vector3d pos, @Nonnull World worldDst) {TeleportPlayer(playerRef, pos, new Vector3f(0.0f, 0.0f, 0.0f), worldDst);}
    public static void TeleportPlayer(@Nonnull PlayerRef playerRef, @Nonnull Vector3d pos, @Nonnull Vector3f rot, @Nonnull World worldDst) {
        Teleport tp = Teleport.createForPlayer(worldDst, new Transform(pos, rot));

        if (playerRef.getWorldUuid() != null) {
            World worldSrc = Universe.get().getWorld(playerRef.getWorldUuid());

            if (worldSrc == null) {
                printLog("Invalid World Source!");
                return;
            }

            worldSrc.execute(() -> worldSrc.getEntityStore()
            .getStore()
            .addComponent(
                    Objects.requireNonNull(playerRef.getReference()),
                    Teleport.getComponentType(),
                    tp
            ));
        }
    }
}
