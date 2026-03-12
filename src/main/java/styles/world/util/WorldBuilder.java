package styles.world.util;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import styles.world.util.filter.BlockFilter;
import styles.world.util.filter.FilterTypes;

import javax.annotation.Nullable;

public class WorldBuilder {

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

    /*===========================================================
        FUNCTION TO MAKE AN VECTOR ALIGNED TO THE WORLD SURFACE
        Params:
            - originalPos -> Reference to the not aligned vector
                             position
            - world -> Reference to the WORLD to align
        Returns:
            - Vector3i -> Vector with the position above the
                          WORLD surface
    ========================================================== */
    @Nullable
    public static Vector3i alignVectorToWorldSurface(Vector3i originalPos, World world) {
        Vector3i pos = new Vector3i(originalPos);
        pos.y = 320; // Adding the max height of the world, and going down until found some clean space.

        BlockType block = world.getBlockType(pos);
        int fluid = world.getFluidId(pos.x, pos.y, pos.z);

        // returns null in case of a world of water, or a world with a roof
        if (block == null || (!block.getId().equals("Empty") || fluid > 0)) return null;

        while ((block.getId().equals("Empty") && fluid <= 0) || BlockFilter.applyFilter(block, FilterTypes.PLANT)) {
            pos.subtract(0, 1, 0);
            block = world.getBlockType(pos);
            fluid = world.getFluidId(pos.x, pos.y, pos.z);
        }

        pos.add(0, 1, 0);

        return pos;
    }

    public static void createFillSquare(Vector3i startPoint, Vector3i finalPoint, BlockType blockType, World world) {
        for(int y = startPoint.y; y <= finalPoint.y; y++){
            for(int x = startPoint.x; x <= finalPoint.x; x++){
                for(int z = startPoint.z; z <= finalPoint.z; z++){
                    world.setBlock(x, y, z, blockType.getId());
                }
            }
        }
    }
}
