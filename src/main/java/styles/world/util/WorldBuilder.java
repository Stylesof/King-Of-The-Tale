package styles.world.util;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;

public class AreaCleaner {

    /*=============================================================
        FUNCTION TO PHYSICALLY CLEAN AN SPACE IN SQUARE FORMAT
        Params:
            - pos -> Position in WORLD to clean
            - size -> Size to clean
            - world -> Reference to the world to clean
    =============================================================*/
    public static void clearAreaSquare(Vector3i pos, int size, World world) {

        Vector3i min = new Vector3i(pos.x - size, pos.y, pos.z - size); // left down corner
        Vector3i max = new Vector3i(pos.x + size, pos.y + size, pos.z + size);

        for(int y = min.y; y < max.y; y++){
            for(int x = min.x; x < max.x; x++){
                for(int z = min.z; z < max.z; z++){
                    world.setBlock(x, y, z, "Empty");
                }
            }

        }

    }

}
