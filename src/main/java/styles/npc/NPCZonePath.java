package styles.npc;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.world.util.WorldBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static styles.util.MessageHandler.printLog;

public class NPCZonePath {

    private final List<Vector3d> Paths = new ArrayList<>();

    public NPCZonePath(@Nonnull World world, Vector3d zonePos, int zoneRadius, int pathCount) {
        Random random = new Random();

        for (int i = 0; i < pathCount; i++) {
            int x = random.nextInt(-zoneRadius, zoneRadius);
            int z = random.nextInt(-zoneRadius, zoneRadius);

            x += (int) zonePos.x;
            z += (int) zonePos.y;

            Paths.add(WorldBuilder.alignVectorToWorldSurface(new Vector3i(x, 0, z), world).toVector3d());
        }
    }

    public List<Vector3d> getPaths() { return this.Paths; }
}