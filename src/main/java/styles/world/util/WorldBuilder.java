package styles.world.util;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import org.jline.jansi.io.Colors;
import styles.util.ColorHandler;
import styles.util.MathHelper;
import styles.world.util.filter.BlockFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldBuilder {

    public static class PointToPoint {

        private Vector3i start, end;

        public PointToPoint(int x, int y, int z, int x2, int y2, int z2) {
            start = new Vector3i(x, y, z);
            end = new Vector3i(x2, y2, z2);
        }

        public void addCenter(Vector3i center) {
            start = MathHelper.vectorSum(start, center);
            end = MathHelper.vectorSum(end, center);
        }

        public Vector3i getStart() { return start; }

        public Vector3i getEnd() { return end; }
    }

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

        for(int y = min.y; y <= max.y; y++){
            for(int x = min.x; x <= max.x; x++){
                for(int z = min.z; z <= max.z; z++){
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

        while ((block.getId().equals("Empty") && fluid <= 0) || BlockFilter.applyFilter(block, BlockFilter.FilterTypes.PLANT)) {
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
    public static void createFillSquare(PointToPoint space, BlockType blockType, World world) {
        for(int y = space.start.y; y <= space.end.y; y++){
            for(int x = space.start.x; x <= space.end.x; x++){
                for(int z = space.start.z; z <= space.end.z; z++){
                    world.setBlock(x, y, z, blockType.getId());
                }
            }
        }
    }

    public static void constructTeamBase(@Nonnull Vector3i spawnLocation, @Nonnull ColorHandler.ColorType teamColor, @Nonnull World world) {
        String color = switch (teamColor) {
            case WHITE -> "White";
            case GREEN -> "Green";
            case BLUE -> "Blue";
            case YELLOW -> "Yellow";
            case CYAN -> "Cyan";
            case PURPLE -> "Purple";
        };

        BlockType rockShaleBrick = BlockType.fromString("Rock_Shale_Brick");
        BlockType rockAquaBrick = BlockType.fromString("Rock_Aqua_Brick_Decorative");
        BlockType rockCrystalBlock = BlockType.fromString("Rock_Crystal_" + color + "_Block");
        BlockType rockBasaltBrick = BlockType.fromString("Rock_Basalt_Brick");

        PointToPoint baseFloor = new PointToPoint(-8, -2, -8, 8, -2, 8);
        baseFloor.addCenter(spawnLocation);
        createFillSquare(baseFloor, rockShaleBrick, world);

        PointToPoint centralBaseFloor = new PointToPoint(-5, -1, -5, 5, -1, 5);
        centralBaseFloor.addCenter(spawnLocation);
        createFillSquare(centralBaseFloor, rockAquaBrick, world);

        PointToPoint crystalBaseFloor = new PointToPoint(-4, -1, -4, 4, -1, 4);
        crystalBaseFloor.addCenter(spawnLocation);
        createFillSquare(crystalBaseFloor, rockCrystalBlock, world);

        PointToPoint baseLine;
        baseLine = new PointToPoint(1, -1, 0, 4, -1, 0);
        baseLine.addCenter(spawnLocation);
        createFillSquare(baseLine, rockAquaBrick, world);
        baseLine = new PointToPoint(-1, -1, 0, -4, -1, 0);
        baseLine.addCenter(spawnLocation);
        createFillSquare(baseLine, rockAquaBrick, world);
        baseLine = new PointToPoint(0, -1, 1, 0, -1, 4);
        baseLine.addCenter(spawnLocation);
        createFillSquare(baseLine, rockAquaBrick, world);
        baseLine = new PointToPoint(0, -1, -1, 0, -1, -4);
        baseLine.addCenter(spawnLocation);
        createFillSquare(baseLine, rockAquaBrick, world);

        PointToPoint baseCentralSquares;
        baseCentralSquares = new PointToPoint(1, -1, 1, 2, -1, 2);
        baseCentralSquares.addCenter(spawnLocation);
        createFillSquare(baseCentralSquares, rockBasaltBrick, world);
        baseCentralSquares = new PointToPoint(-1, -1, 1, -2, -1, 2);
        baseCentralSquares.addCenter(spawnLocation);
        createFillSquare(baseCentralSquares, rockBasaltBrick, world);
        baseCentralSquares = new PointToPoint(1, -1, -1, 2, -1, -2);
        baseCentralSquares.addCenter(spawnLocation);
        createFillSquare(baseCentralSquares, rockBasaltBrick, world);
        baseCentralSquares = new PointToPoint(-1, -1, -1, -2, -1, -2);
        baseCentralSquares.addCenter(spawnLocation);
        createFillSquare(baseCentralSquares, rockBasaltBrick, world);


    }
}
