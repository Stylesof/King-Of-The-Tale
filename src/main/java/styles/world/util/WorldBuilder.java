package styles.world.util;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlocksUtil;
import styles.util.ColorHandler;
import styles.util.MathHelper;
import styles.world.util.filter.BlockFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printLog;

public class WorldBuilder {

    public static class PointToPoint {
        public Vector3i start, end;
        public PointToPoint(int x, int y, int z, int x2, int y2, int z2) {
            start = new Vector3i(x, y, z);
            end = new Vector3i(x2, y2, z2);
        }

        public PointToPoint(Vector3i start, Vector3i end) { this(start.x, start.y, start.z, end.x, end.y, end.z); }
        public void addCenter(Vector3i center) {
            start = MathHelper.vectorSum(start, center);
            end = MathHelper.vectorSum(end, center);
        }
    }

    public static class Point {
        public Vector3i pos;
        public Point(int x, int y, int z) { pos = new Vector3i(x, y, z); }
        public void addCenter(Vector3i center) { pos = MathHelper.vectorSum(pos, center); }
    }

    /*=============================================================
        FUNCTION TO PHYSICALLY CLEAN AN SPACE IN SQUARE FORMAT
        Params:
            - pos -> Position in WORLD to clean
            - size -> Size to clean
            - world -> Reference to the world to clean
    =============================================================*/
    public static CompletableFuture<Void> clearAreaSquare(Vector3i pos, int size, World world) {
        Vector3i min = new Vector3i(pos.x - size, pos.y, pos.z - size); // left down corner
        Vector3i max = new Vector3i(pos.x + size, pos.y + size, pos.z + size);

        return world.getChunkAsync(pos.x, pos.z).thenAccept(WorldChunk::addKeepLoaded).thenCompose(unused -> {
            for(int y = min.y; y <= max.y; y++){
                for(int x = min.x; x <= max.x; x++){
                    for(int z = min.z; z <= max.z; z++){
                        setBlock(x, y, z, BlockType.fromString("Empty"), 0, world);
                    }
                }
            }

            return CompletableFuture.completedFuture(null);
        });
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
    ===========================================================*/
    @Nullable
    public static Vector3i alignVectorToWorldSurface(Vector3i originalPos, World world) {
        Vector3i pos = new Vector3i(originalPos);
        pos.y = 320; // Adding the max height of the world, and going down until found some clean space.

        BlockType block = world.getBlockType(pos);
        int fluid = world.getFluidId(pos.x, pos.y, pos.z);
        //int fluid = 0;
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

    /*===========================================================
        FUNCTION TO FILL AN SQUARE AREA WITH AN SPECIFIED BLOCK
        TYPE.
        Params:
            - startPoint -> Start point of the square
            - endPoint -> End point of the square
            - blockType -> Type of Block to use
            - world -> World reference
     ==========================================================*/
    public static void createFillSquare(Vector3i startPoint, Vector3i endPoint, BlockType blockType, World world) { createFillSquare(new PointToPoint(startPoint, endPoint), blockType, world); }

    public static void createFillSquare(PointToPoint space, BlockType blockType, World world) { createFillSquare(space, blockType, 0, world); }

    public static void createFillSquare(PointToPoint space, BlockType blockType, int rotation, World world) {
        int directionX, directionY, directionZ;
        directionX = space.start.x <= space.end.x ? 1 : -1;
        directionY = space.start.y <= space.end.y ? 1 : -1;
        directionZ = space.start.z <= space.end.z ? 1 : -1;

        for(int y = space.start.y; y != space.end.y + directionY; y+=directionY){
            for(int x = space.start.x; x != space.end.x + directionX; x+=directionX){
                for(int z = space.start.z; z != space.end.z + directionZ; z+=directionZ){
                    setBlock(x, y, z, blockType, rotation, world);
                }
            }
        }
    }

    public static void createPillar(@Nonnull Point point, int size, @Nonnull BlockType pillarBaseType, @Nonnull BlockType pillarBlockType, @Nonnull World world) {
        world.setBlock(point.pos.x, point.pos.y, point.pos.z, pillarBaseType.getId());
        for (int i = 0; i < size; i++) world.setBlock(point.pos.x, ++point.pos.y, point.pos.z, pillarBlockType.getId());
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(point.pos.x, point.pos.z));
        if (chunk == null) {
            printLog("[KOTT Debug] Invalid chunk!");
            return;
        }
        chunk.setBlock(point.pos.x, ++point.pos.y, point.pos.z, BlockType.getAssetMap().getIndex(pillarBaseType.getId()), pillarBaseType, 8, 0, 0);
    }

    public static boolean setBlock(int x, int y, int z, @Nonnull BlockType blockType, int rotation, World world) { return setBlock(new Point(x, y, z), blockType, rotation, 0, world); }

    public static boolean setBlock(@Nonnull Point point, @Nonnull BlockType blockType, int rotation, int filler, World world) {
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(point.pos.x, point.pos.z));
        if (chunk == null) {
            printLog("[KOTT Debug] Invalid chunk!");
            return false;
        }

        chunk.setBlock(point.pos.x, point.pos.y, point.pos.z, BlockType.getAssetMap().getIndex(blockType.getId()), blockType, rotation, 0, 0);
        chunk.setTicking(point.pos.x, point.pos.y, point.pos.z, true);
        chunk.getChunkAccessor().performBlockUpdate(point.pos.x, point.pos.y, point.pos.y);

        if (chunk.getBlockChunk() == null) {
            printLog("[KOTT Debug] Invalid block chunk!");
            return false;
        }

        ConnectedBlocksUtil.setConnectedBlockAndNotifyNeighbors(
                BlockType.getAssetMap().getIndex(blockType.getId()),
                RotationTuple.get(rotation),
                new Vector3i(0, 0, 0),
                point.pos,
                chunk,
                chunk.getBlockChunk()
        );

        return true;
    }

    public static CompletableFuture<Boolean> constructTeamBase(@Nonnull Vector3i spawnLocation, @Nonnull ColorHandler.ColorType teamColor, @Nonnull World world) {
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
        BlockType rockAquaBrickPillarBase = BlockType.fromString("Rock_Aqua_Brick_Pillar_Base");
        BlockType rockAquaBrickPillar = BlockType.fromString("Rock_Aqua_Brick_Pillar_Middle");
        BlockType rockAquaBrickWall = BlockType.fromString("Rock_Aqua_Brick_Wall");
        BlockType rockShaleBrickStairs = BlockType.fromString("Rock_Shale_Brick_Stairs");
        BlockType templeLightBench = BlockType.fromString("Furniture_Temple_Light_Bench");
        BlockType goldBrickOrnate = BlockType.fromString("Rock_Gold_Brick_Ornate");
        BlockType rockAquaCobbleRoof = BlockType.fromString("Rock_Aqua_Cobble_Roof");
        BlockType rockAquaCobbleRoofFlat = BlockType.fromString("Rock_Aqua_Cobble_Roof_Flat");

        if (rockShaleBrick == null || rockAquaBrick == null || rockCrystalBlock == null || rockBasaltBrick == null || rockAquaBrickPillarBase == null || rockAquaBrickPillar == null ||
            rockAquaBrickWall == null || rockShaleBrickStairs == null || templeLightBench == null || goldBrickOrnate == null || rockAquaCobbleRoof == null || rockAquaCobbleRoofFlat == null)
            return CompletableFuture.completedFuture(false);

        PointToPoint baseFloor;
        baseFloor = new PointToPoint(-8, -2, -8, 8, -2, 8);
        baseFloor.addCenter(spawnLocation);
        createFillSquare(baseFloor, rockShaleBrick, world);

        PointToPoint centralBaseFloor;
        centralBaseFloor = new PointToPoint(-5, -1, -5, 5, -1, 5);
        centralBaseFloor.addCenter(spawnLocation);
        createFillSquare(centralBaseFloor, rockAquaBrick, world);

        PointToPoint crystalBaseFloor;
        crystalBaseFloor = new PointToPoint(-4, -1, -4, 4, -1, 4);
        crystalBaseFloor.addCenter(spawnLocation);
        createFillSquare(crystalBaseFloor, rockCrystalBlock, world);

        PointToPoint baseLine;
        baseLine = new PointToPoint(1, -1, 0, 4, -1, 0);
        baseLine.addCenter(spawnLocation);
        createFillSquare(baseLine, rockShaleBrick, world);
        baseLine = new PointToPoint(-1, -1, 0, -4, -1, 0);
        baseLine.addCenter(spawnLocation);
        createFillSquare(baseLine, rockShaleBrick, world);
        baseLine = new PointToPoint(0, -1, 1, 0, -1, 4);
        baseLine.addCenter(spawnLocation);
        createFillSquare(baseLine, rockShaleBrick, world);
        baseLine = new PointToPoint(0, -1, -1, 0, -1, -4);
        baseLine.addCenter(spawnLocation);
        createFillSquare(baseLine, rockShaleBrick, world);

        // BASE CENTRAL SQUARES
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

        Point baseCentralPoint;
        baseCentralPoint = new Point(1, -1, -3);
        baseCentralPoint.addCenter(spawnLocation);
        setBlock(baseCentralPoint, rockBasaltBrick, 0, 0, world);
        baseCentralPoint.pos.x += 2;
        baseCentralPoint.pos.z += 2;
        setBlock(baseCentralPoint, rockBasaltBrick, 0, 0, world);

        baseCentralPoint = new Point(1, -1, 3);
        baseCentralPoint.addCenter(spawnLocation);
        setBlock(baseCentralPoint, rockBasaltBrick, 0, 0, world);
        baseCentralPoint.pos.x += 2;
        baseCentralPoint.pos.z -= 2;
        setBlock(baseCentralPoint, rockBasaltBrick, 0, 0, world);

        baseCentralPoint = new Point(-1, -1, -3);
        baseCentralPoint.addCenter(spawnLocation);
        setBlock(baseCentralPoint, rockBasaltBrick, 0, 0, world);
        baseCentralPoint.pos.x -= 2;
        baseCentralPoint.pos.z += 2;
        setBlock(baseCentralPoint, rockBasaltBrick, 0, 0, world);

        baseCentralPoint = new Point(-1, -1, 3);
        baseCentralPoint.addCenter(spawnLocation);
        setBlock(baseCentralPoint, rockBasaltBrick, 0, 0, world);
        baseCentralPoint.pos.x -= 2;
        baseCentralPoint.pos.z -= 2;
        setBlock(baseCentralPoint, rockBasaltBrick, 0, 0, world);

        Point baseCentralPoint2;
        baseCentralPoint2 = new Point(1, -1, -1);
        baseCentralPoint2.addCenter(spawnLocation);
        setBlock(baseCentralPoint2, rockShaleBrick, 0, 0, world);
        baseCentralPoint2 = new Point(1, -1, 1);
        baseCentralPoint2.addCenter(spawnLocation);
        setBlock(baseCentralPoint2, rockShaleBrick, 0, 0, world);
        baseCentralPoint2 = new Point(-1, -1, -1);
        baseCentralPoint2.addCenter(spawnLocation);
        setBlock(baseCentralPoint2, rockShaleBrick, 0, 0, world);
        baseCentralPoint2 = new Point(-1, -1, 1);
        baseCentralPoint2.addCenter(spawnLocation);
        setBlock(baseCentralPoint2, rockShaleBrick, 0, 0, world);


        // GOLDEN BLOCKS
        Point baseGoldenPoint;
        baseGoldenPoint = new Point(3, -1 ,-3);
        baseGoldenPoint.addCenter(spawnLocation);
        setBlock(baseGoldenPoint, goldBrickOrnate, 0, 0, world);
        baseGoldenPoint = new Point(3, -1 ,3);
        baseGoldenPoint.addCenter(spawnLocation);
        setBlock(baseGoldenPoint, goldBrickOrnate, 0, 0, world);
        baseGoldenPoint = new Point(-3, -1 ,-3);
        baseGoldenPoint.addCenter(spawnLocation);
        setBlock(baseGoldenPoint, goldBrickOrnate, 0, 0, world);
        baseGoldenPoint = new Point(-3, -1 ,3);
        baseGoldenPoint.addCenter(spawnLocation);
        setBlock(baseGoldenPoint, goldBrickOrnate, 0, 0, world);

        // PILLARS
        Point pillarBase;
        pillarBase = new Point(5, 0, 5);
        pillarBase.addCenter(spawnLocation);
        createPillar(pillarBase, 3, rockAquaBrickPillarBase, rockAquaBrickPillar, world);
        pillarBase = new Point(5, 0, -5);
        pillarBase.addCenter(spawnLocation);
        createPillar(pillarBase, 3, rockAquaBrickPillarBase, rockAquaBrickPillar, world);
        pillarBase = new Point(-5, 0, 5);
        pillarBase.addCenter(spawnLocation);
        createPillar(pillarBase, 3, rockAquaBrickPillarBase, rockAquaBrickPillar, world);
        pillarBase = new Point(-5, 0, -5);
        pillarBase.addCenter(spawnLocation);
        createPillar(pillarBase, 3, rockAquaBrickPillarBase, rockAquaBrickPillar, world);

        PointToPoint baseLineUp;
        baseLineUp = new PointToPoint(5, 5, 5, -5, 5, 5);
        baseLineUp.addCenter(spawnLocation);
        createFillSquare(baseLineUp, rockAquaBrick, world);
        baseLineUp = new PointToPoint(-5, 5, 5, -5, 5, -5);
        baseLineUp.addCenter(spawnLocation);
        createFillSquare(baseLineUp, rockAquaBrick, world);
        baseLineUp = new PointToPoint(-5, 5, -5, 5, 5, -5);
        baseLineUp.addCenter(spawnLocation);
        createFillSquare(baseLineUp, rockAquaBrick, world);
        baseLineUp = new PointToPoint(5, 5, -5, 5, 5, 5);
        baseLineUp.addCenter(spawnLocation);
        createFillSquare(baseLineUp, rockAquaBrick, world);

        PointToPoint baseWallLine;
        baseWallLine = new PointToPoint(5, 0, 4, 5, 0, 2);
        baseWallLine.addCenter(spawnLocation);
        createFillSquare(baseWallLine, rockAquaBrickWall, world);
        baseWallLine = new PointToPoint(5, 0, 2, 6, 0, 2);
        baseWallLine.addCenter(spawnLocation);
        createFillSquare(baseWallLine, rockAquaBrickWall, world);

        // RIGHT WALLS
        Point baseLineWall = new Point(5, 0, -4);
        baseLineWall.addCenter(spawnLocation);
        setBlock(baseLineWall, rockAquaBrickWall, 1, 0, world);
        baseLineWall.pos.z += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 1, world);
        baseLineWall.pos.z += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 1, world);
        baseLineWall.pos.x += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 0, world);

        baseLineWall.pos.y -= 1;
        setBlock(baseLineWall, rockCrystalBlock, 0, 0, world);

        // RIGHT WALLS 2
        baseLineWall = new Point(5, 0, 4);
        baseLineWall.addCenter(spawnLocation);
        setBlock(baseLineWall, rockAquaBrickWall, 1, 0, world);
        baseLineWall.pos.z -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 1, world);
        baseLineWall.pos.z -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 1, world);
        baseLineWall.pos.x += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 0, world);

        baseLineWall.pos.y -= 1;
        setBlock(baseLineWall, rockCrystalBlock, 0, 0, world);

        // FRONT WALLS
        baseLineWall = new Point(4, 0, -5);
        baseLineWall.addCenter(spawnLocation);
        setBlock(baseLineWall, rockAquaBrickWall, 0, 0, world);
        baseLineWall.pos.x -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 1, world);
        baseLineWall.pos.x -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 1, world);
        baseLineWall.pos.z -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 0, world);

        baseLineWall.pos.y -= 1;
        setBlock(baseLineWall, rockCrystalBlock, 0, 0, world);

        // FRONT WALLS 2
        baseLineWall = new Point(-4, 0, -5);
        baseLineWall.addCenter(spawnLocation);
        setBlock(baseLineWall, rockAquaBrickWall, 0, 0, world);
        baseLineWall.pos.x += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 1, world);
        baseLineWall.pos.x += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 1, world);
        baseLineWall.pos.z -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 0, world);

        baseLineWall.pos.y -= 1;
        setBlock(baseLineWall, rockCrystalBlock, 0, 0, world);

        // LEFT WALLS
        baseLineWall = new Point(-5, 0, -4);
        baseLineWall.addCenter(spawnLocation);
        setBlock(baseLineWall, rockAquaBrickWall, 1, 0, world);
        baseLineWall.pos.z += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 1, world);
        baseLineWall.pos.z += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 1, world);
        baseLineWall.pos.x -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 0, world);

        baseLineWall.pos.y -= 1;
        setBlock(baseLineWall, rockCrystalBlock, 0, 0, world);

        // LEFT WALLS 2
        baseLineWall = new Point(-5, 0, 4);
        baseLineWall.addCenter(spawnLocation);
        setBlock(baseLineWall, rockAquaBrickWall, 1, 0, world);
        baseLineWall.pos.z -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 1, world);
        baseLineWall.pos.z -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 1, world);
        baseLineWall.pos.x -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 0, world);

        baseLineWall.pos.y -= 1;
        setBlock(baseLineWall, rockCrystalBlock, 0, 0, world);

        // BACK WALLS
        baseLineWall = new Point(-4, 0, 5);
        baseLineWall.addCenter(spawnLocation);
        setBlock(baseLineWall, rockAquaBrickWall, 0, 0, world);
        baseLineWall.pos.x += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 1, world);
        baseLineWall.pos.x += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 1, world);
        baseLineWall.pos.z += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 0, world);

        baseLineWall.pos.y -= 1;
        setBlock(baseLineWall, rockCrystalBlock, 0, 0, world);

        // BACK WALLS 2
        baseLineWall = new Point(4, 0, 5);
        baseLineWall.addCenter(spawnLocation);
        setBlock(baseLineWall, rockAquaBrickWall, 0, 0, world);
        baseLineWall.pos.x -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 1, world);
        baseLineWall.pos.x -= 1;
        setBlock(baseLineWall, rockAquaBrickWall, 0, 1, world);
        baseLineWall.pos.z += 1;
        setBlock(baseLineWall, rockAquaBrickWall, 1, 0, world);

        baseLineWall.pos.y -= 1;
        setBlock(baseLineWall, rockCrystalBlock, 0, 0, world);

        // RIGHT STAIRS
        Point baseStairs = new Point(6, -1, -1);
        baseStairs.addCenter(spawnLocation);
        setBlock(baseStairs, rockShaleBrickStairs, 1, 0, world);
        baseStairs.pos.z += 1;
        setBlock(baseStairs, rockShaleBrickStairs, 1, 0, world);
        baseStairs.pos.z += 1;
        setBlock(baseStairs, rockShaleBrickStairs, 1, 0, world);

        // LEFT STAIRS
        baseStairs = new Point(-6, -1, -1);
        baseStairs.addCenter(spawnLocation);
        setBlock(baseStairs, rockShaleBrickStairs, 3, 0, world);
        baseStairs.pos.z += 1;
        setBlock(baseStairs, rockShaleBrickStairs, 3, 0, world);
        baseStairs.pos.z += 1;
        setBlock(baseStairs, rockShaleBrickStairs, 3, 0, world);

        // FRONT STAIRS
        baseStairs = new Point(-1, -1, -6);
        baseStairs.addCenter(spawnLocation);
        setBlock(baseStairs, rockShaleBrickStairs, 2, 0, world);
        baseStairs.pos.x += 1;
        setBlock(baseStairs, rockShaleBrickStairs, 2, 0, world);
        baseStairs.pos.x += 1;
        setBlock(baseStairs, rockShaleBrickStairs, 2, 0, world);

        // BACK STAIRS
        baseStairs = new Point(-1, -1, 6);
        baseStairs.addCenter(spawnLocation);
        setBlock(baseStairs, rockShaleBrickStairs, 0, 0, world);
        baseStairs.pos.x += 1;
        setBlock(baseStairs, rockShaleBrickStairs, 0, 0, world);
        baseStairs.pos.x += 1;
        setBlock(baseStairs, rockShaleBrickStairs, 0, 0, world);

        // BACK BENCH
        Point baseBench = new Point(-3, -1,6);
        baseBench.addCenter(spawnLocation);
        setBlock(baseBench, templeLightBench, 0, 0, world);
        baseBench.pos.x += 7;
        setBlock(baseBench, templeLightBench, 0, 0, world);

        // FRONT BENCH
        baseBench = new Point(-4, -1,-6);
        baseBench.addCenter(spawnLocation);
        setBlock(baseBench, templeLightBench, 2, 0, world);
        baseBench.pos.x += 7;
        setBlock(baseBench, templeLightBench, 2, 0, world);

        // RIGHT BENCH
        baseBench = new Point(6, -1,-4);
        baseBench.addCenter(spawnLocation);
        setBlock(baseBench, templeLightBench, 1, 0, world);
        baseBench.pos.z += 7;
        setBlock(baseBench, templeLightBench, 1, 0, world);

        // LEFT BENCH
        baseBench = new Point(-6, -1,-3);
        baseBench.addCenter(spawnLocation);
        setBlock(baseBench, templeLightBench, 3, 0, world);
        baseBench.pos.z += 7;
        setBlock(baseBench, templeLightBench, 3, 0, world);

        // ROOF RIGHT
        PointToPoint baseRoof = new PointToPoint(5, 6, -5, 5, 6, 5);
        baseRoof.addCenter(spawnLocation);
        createFillSquare(baseRoof, rockAquaCobbleRoof, 1, world);

        // ROOF BACK
        baseRoof = new PointToPoint(-5, 6, 5, 5, 6, 5);
        baseRoof.addCenter(spawnLocation);
        createFillSquare(baseRoof, rockAquaCobbleRoof, 0, world);

        // ROOF LEFT
        baseRoof = new PointToPoint(-5, 6, -5, -5, 6, 5);
        baseRoof.addCenter(spawnLocation);
        createFillSquare(baseRoof, rockAquaCobbleRoof, 3, world);

        // ROOFT FRONT
        baseRoof = new PointToPoint(-5, 6, -5, 5, 6, -5);
        baseRoof.addCenter(spawnLocation);
        createFillSquare(baseRoof, rockAquaCobbleRoof, 2, world);

        // ROOF UP RIGHT CORNER
        Point baseRoof2 = new Point(4, 7, -4);
        baseRoof2.addCenter(spawnLocation);
        setBlock(baseRoof2, rockAquaCobbleRoof, 1, 0, world);
        baseRoof2.pos.z += 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 1, 0, world);
        baseRoof2.pos.z += 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 2, 2, world);
        baseRoof2.pos.z -= 2;
        baseRoof2.pos.x -= 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 2, 0, world);
        baseRoof2.pos.x -= 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 1, 1, world);

        // ROOF DOWN RIGHT CORNER
        baseRoof2 = new Point(4, 7, 4);
        baseRoof2.addCenter(spawnLocation);
        setBlock(baseRoof2, rockAquaCobbleRoof, 1, 0, world);
        baseRoof2.pos.z -= 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 1, 0, world);
        baseRoof2.pos.z -= 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 0, 0, world);
        baseRoof2.pos.z += 2;
        baseRoof2.pos.x -= 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 0, 0, world);
        baseRoof2.pos.x -= 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 1, 0, world);

        // ROOF UP LEFT CORNER
        baseRoof2 = new Point(-4, 7, -4);
        baseRoof2.addCenter(spawnLocation);
        setBlock(baseRoof2, rockAquaCobbleRoof, 3, 0, world);
        baseRoof2.pos.z += 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 3, 0, world);
        baseRoof2.pos.z += 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 2, 0, world);
        baseRoof2.pos.z -= 2;
        baseRoof2.pos.x += 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 2, 0, world);
        baseRoof2.pos.x += 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 3, 0, world);

        // ROOF DOWN LEFT CORNER
        baseRoof2 = new Point(-4, 7, 4);
        baseRoof2.addCenter(spawnLocation);
        setBlock(baseRoof2, rockAquaCobbleRoof, 3, 0, world);
        baseRoof2.pos.z -= 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 3, 0, world);
        baseRoof2.pos.z -= 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 0, 0, world);
        baseRoof2.pos.z += 2;
        baseRoof2.pos.x += 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 0, 0, world);
        baseRoof2.pos.x += 1;
        setBlock(baseRoof2, rockAquaCobbleRoof, 3, 0, world);

        // UP CRYSTAL RIGHT
        Point baseUpCrystal;
        baseUpCrystal = new Point(4, 7, -1);
        baseUpCrystal.addCenter(spawnLocation);
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.z += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.y += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.y -= 1;
        baseUpCrystal.pos.z += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);

        // UP CRYSTAL FRONT
        baseUpCrystal = new Point(-1, 7, -4);
        baseUpCrystal.addCenter(spawnLocation);
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.x += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.y += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.y -= 1;
        baseUpCrystal.pos.x += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);

        // UP CRYSTAL LEFT
        baseUpCrystal = new Point(-4, 7, -1);
        baseUpCrystal.addCenter(spawnLocation);
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.z += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.y += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.y -= 1;
        baseUpCrystal.pos.z += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);

        // UP CRYSTAL BACK
        baseUpCrystal = new Point(-1, 7, 4);
        baseUpCrystal.addCenter(spawnLocation);
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.x += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.y += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);
        baseUpCrystal.pos.y -= 1;
        baseUpCrystal.pos.x += 1;
        setBlock(baseUpCrystal, rockCrystalBlock, 0, 0, world);

        // BASE UP
        PointToPoint baseUp;
        baseUp = new PointToPoint(-4, 6, -4, 4, 6, 4);
        baseUp.addCenter(spawnLocation);
        createFillSquare(baseUp, rockAquaBrick, world);

        baseUp = new PointToPoint(-3, 7, -3, 3, 7, 3);
        baseUp.addCenter(spawnLocation);
        createFillSquare(baseUp, rockCrystalBlock, world);

        baseUp = new PointToPoint(-3, 7, -3, -2, 7, -2);
        baseUp.addCenter(spawnLocation);
        createFillSquare(baseUp, rockShaleBrick, world);

        baseUp = new PointToPoint(3, 7, -3, 2, 7, -2);
        baseUp.addCenter(spawnLocation);
        createFillSquare(baseUp, rockShaleBrick, world);

        baseUp = new PointToPoint(3, 7, 3, 2, 7, 2);
        baseUp.addCenter(spawnLocation);
        createFillSquare(baseUp, rockShaleBrick, world);

        baseUp = new PointToPoint(-3, 7, 3, -2, 7, 2);
        baseUp.addCenter(spawnLocation);
        createFillSquare(baseUp, rockShaleBrick, world);

        // ROOF 3 RIGHT
        Point baseRoof3;
        baseRoof3 = new Point(4, 8, -1);
        baseRoof3.addCenter(spawnLocation);
        setBlock(baseRoof3, rockAquaCobbleRoof, 2, 0, world);
        baseRoof3.pos.z += 2;
        setBlock(baseRoof3, rockAquaCobbleRoof, 0, 0, world);

        // ROOF 3 FRONT
        baseRoof3 = new Point(-1, 8, -4);
        baseRoof3.addCenter(spawnLocation);
        setBlock(baseRoof3, rockAquaCobbleRoof, 3, 0, world);
        baseRoof3.pos.x += 2;
        setBlock(baseRoof3, rockAquaCobbleRoof, 1, 0, world);

        // ROOF 3 LEFT
        baseRoof3 = new Point(-4, 8, -1);
        baseRoof3.addCenter(spawnLocation);
        setBlock(baseRoof3, rockAquaCobbleRoof, 2, 0, world);
        baseRoof3.pos.z += 2;
        setBlock(baseRoof3, rockAquaCobbleRoof, 0, 0, world);

        // ROOF 3 BACk
        baseRoof3 = new Point(-1, 8, 4);
        baseRoof3.addCenter(spawnLocation);
        setBlock(baseRoof3, rockAquaCobbleRoof, 3, 0, world);
        baseRoof3.pos.x += 2;
        setBlock(baseRoof3, rockAquaCobbleRoof, 1, 0, world);

        PointToPoint baseUp2;
        baseUp2 = new PointToPoint(0, 8, 3, 0, 8, -3);
        baseUp2.addCenter(spawnLocation);
        createFillSquare(baseUp2, rockShaleBrick, world);

        baseUp2 = new PointToPoint(-3, 8, 0, 3, 8, 0);
        baseUp2.addCenter(spawnLocation);
        createFillSquare(baseUp2, rockShaleBrick, world);

        Point baseUpDetail;
        baseUpDetail = new Point(3, 8, -1);
        baseUpDetail.addCenter(spawnLocation);
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);

        baseUpDetail.pos.x -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);

        baseUpDetail.pos.z += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);

        baseUpDetail.pos.x += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.x += 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);
        baseUpDetail.pos.z -= 1;
        setBlock(baseUpDetail, rockShaleBrick, 0, 0, world);

        Point baseUpRoofFlat;
        baseUpRoofFlat = new Point(4, 9, 0);
        baseUpRoofFlat.addCenter(spawnLocation);
        setBlock(baseUpRoofFlat, rockAquaCobbleRoofFlat, 1, 0, world);

        baseUpRoofFlat = new Point(0, 9, -4);
        baseUpRoofFlat.addCenter(spawnLocation);
        setBlock(baseUpRoofFlat, rockAquaCobbleRoofFlat, 0, 0, world);

        baseUpRoofFlat = new Point(-4, 9, 0);
        baseUpRoofFlat.addCenter(spawnLocation);
        setBlock(baseUpRoofFlat, rockAquaCobbleRoofFlat, 1, 0, world);

        baseUpRoofFlat = new Point(0, 9, 4);
        baseUpRoofFlat.addCenter(spawnLocation);
        setBlock(baseUpRoofFlat, rockAquaCobbleRoofFlat, 0, 0, world);

        Point finalBlock = new Point(0, 8, 0);
        finalBlock.addCenter(spawnLocation);
        setBlock(finalBlock, rockCrystalBlock, 0, 0, world);

        return CompletableFuture.completedFuture(true);
    }
}
