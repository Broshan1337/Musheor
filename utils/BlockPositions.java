// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import java.util.ArrayList;
import meteordevelopment.meteorclient.MeteorClient;
import musheor.modules.automation.HighwayBuilder;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Generates arrays of BlockPos for the various regions of a highway cross-section:
 * floor, ceiling, and clear-space positions, for both cardinal and diagonal directions.
 *
 * All methods take offset parameters (n = behind, n2 = ahead relative to player)
 * and return only positions relevant to the current highway direction and width.
 */
public class BlockPositions {

    /**
     * Returns floor positions (y = highwayY - 1) for cardinal directions (N/S/E/W).
     * Optionally includes left/right wall positions at y = highwayY if walls are enabled.
     *
     * @param behind  number of blocks behind the player to include
     * @param ahead   number of blocks ahead of the player to include
     * @param leftWall  include left wall positions
     * @param rightWall include right wall positions
     */
    @NotNull
    public static BlockPos[] getFloorPositions(int behind, int ahead, boolean leftWall, boolean rightWall) { // was: jOdDDFXSeWl4(int,int,boolean,boolean)
        int halfHigh, halfLow;
        int axisX, axisZ;  // which axis is the forward axis (1=X, 0=X component unused)
        int forwardStart, forwardEnd, forwardStep;
        int lateralSign;
        HighwayState state = HighwayState.getInstance();
        boolean hasWalls = HighwayBuilder.hasWalls();     // was: fGLoB1zTvFf
        int width        = HighwayBuilder.getWidth();     // was: oq3TU4VRVWuh
        WorldUtils.Direction8 dir = HighwayBuilder.getDirection(); // was: kLIvClyeu
        int playerX = MeteorClient.mc.player.getX(); // getBlockX
        int highwayY = state.getHighwayY();               // was: KaWPzeyl1xVKHWo
        int playerZ = MeteorClient.mc.player.getZ(); // getBlockZ

        switch (dir) {
            case NORTH: { // was: vSouwXdh7
                axisZ = 0; axisX = 1;
                forwardStart = ahead;  forwardEnd = -behind; forwardStep = -1;
                lateralSign = 1;
                playerX = state.getAlignX(); // was: lYl0U01zxBqO9u — snapped X position
                break;
            }
            case SOUTH: { // was: Q5FUNqd0ALfl
                axisZ = 0; axisX = 1;
                forwardStart = -ahead; forwardEnd = behind; forwardStep = 1;
                lateralSign = -1;
                playerX = state.getAlignX();
                break;
            }
            case WEST: { // was: S8iuqKQCrJM02b — note: mapped as "WEST" by ordinal but acts as +X forward
                axisZ = 1; axisX = 0;
                forwardStart = -ahead; forwardEnd = behind; forwardStep = 1;
                lateralSign = 1;
                playerZ = state.getAlignZ(); // was: U6GoOdyLiE04 — snapped Z position
                break;
            }
            case EAST: { // was: v5UhyO9eEd7n — acts as -X forward
                axisZ = 1; axisX = 0;
                forwardStart = ahead;  forwardEnd = -behind; forwardStep = -1;
                lateralSign = -1;
                playerZ = state.getAlignZ();
                break;
            }
            default: return new BlockPos[0];
        }

        // Compute lateral half-widths
        if (width % 2 == 0) {
            halfHigh = width / 2;
            halfLow  = halfHigh - 1;
        } else {
            halfHigh = halfLow = (width - 1) / 2;
        }

        ArrayList<BlockPos> result = new ArrayList<>();
        for (int i = forwardStart; i != forwardEnd + forwardStep; i += forwardStep) {
            int wx, wz;
            // Main floor strip
            for (int lat = -halfHigh; lat <= halfLow; ++lat) {
                wx = playerX + axisZ * i + axisX * (lat * lateralSign);
                wz = playerZ + axisX * i + axisZ * (lat * lateralSign);
                result.add(new BlockPos(wx, highwayY - 1, wz));
            }
            if (!hasWalls) continue;
            // Left wall position
            int leftX = playerX + axisZ * i + axisX * ((-halfHigh - 1) * lateralSign);
            int leftZ = playerZ + axisX * i + axisZ * ((-halfHigh - 1) * lateralSign);
            // Right wall position
            int rightX = playerX + axisZ * i + axisX * ((halfLow + 1) * lateralSign);
            int rightZ = playerZ + axisX * i + axisZ * ((halfLow + 1) * lateralSign);
            if (leftWall)  result.add(new BlockPos(leftX,  highwayY, leftZ));
            if (rightWall) result.add(new BlockPos(rightX, highwayY, rightZ));
        }
        return result.toArray(new BlockPos[0]);
    }

    /**
     * Returns the positions to be cleared (air column) for cardinal directions.
     * Height is 2 for normal mode, 3 for elytra mode.
     *
     * @param behind number of blocks behind the player
     * @param ahead  number of blocks ahead of the player
     */
    public static BlockPos[] getClearPositions(int behind, int ahead) { // was: vgrtgn5(int,int)
        int halfHigh, halfLow;
        int axisX, axisZ;
        int forwardStart, forwardEnd, forwardStep;
        int lateralSign;
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        int clearHeight = 2;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.ELYTRA) clearHeight = 3; // was: flZYoiXwrl
        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        int playerX = MeteorClient.mc.player.getX();
        int highwayY = state.getHighwayY();
        int playerZ = MeteorClient.mc.player.getZ();

        switch (dir) {
            case NORTH: { axisZ = 0; axisX = 1; forwardStart = ahead;  forwardEnd = -behind; forwardStep = -1; lateralSign =  1; playerX = state.getAlignX(); break; }
            case SOUTH: { axisZ = 0; axisX = 1; forwardStart = -ahead; forwardEnd =  behind; forwardStep =  1; lateralSign = -1; playerX = state.getAlignX(); break; }
            case WEST:  { axisZ = 1; axisX = 0; forwardStart = -ahead; forwardEnd =  behind; forwardStep =  1; lateralSign =  1; playerZ = state.getAlignZ(); break; }
            case EAST:  { axisZ = 1; axisX = 0; forwardStart = ahead;  forwardEnd = -behind; forwardStep = -1; lateralSign = -1; playerZ = state.getAlignZ(); break; }
            default: return new BlockPos[0];
        }

        if (width % 2 == 0) { halfHigh = width / 2; halfLow = halfHigh - 1; }
        else                 { halfHigh = halfLow = (width - 1) / 2; }

        ArrayList<BlockPos> result = new ArrayList<>();
        for (int i = forwardStart; i != forwardEnd + forwardStep; i += forwardStep) {
            for (int h = 0; h <= clearHeight; ++h) {
                for (int lat = -halfHigh; lat <= halfLow; ++lat) {
                    int wx = playerX + axisZ * i + axisX * (lat * lateralSign);
                    int wz = playerZ + axisX * i + axisZ * (lat * lateralSign);
                    result.add(new BlockPos(wx, highwayY + h, wz));
                }
                if (!HighwayBuilder.hasWalls() || h <= 0) continue; // was: CduCWLxmO
                // Include wall blocks in clear zone
                int leftX  = playerX + axisZ * i + axisX * ((-halfHigh - 1) * lateralSign);
                int leftZ  = playerZ + axisX * i + axisZ * ((-halfHigh - 1) * lateralSign);
                int rightX = playerX + axisZ * i + axisX * ((halfLow  + 1) * lateralSign);
                int rightZ = playerZ + axisX * i + axisZ * ((halfLow  + 1) * lateralSign);
                result.add(new BlockPos(leftX,  highwayY + h, leftZ));
                result.add(new BlockPos(rightX, highwayY + h, rightZ));
            }
        }
        return result.toArray(new BlockPos[0]);
    }

    /**
     * Returns ceiling positions (y = playerY + 3) for cardinal directions.
     * Used for ceiling highways (build above the player).
     */
    public static BlockPos[] getCeilingPositions(int behind, int ahead, boolean leftWall, boolean rightWall) { // was: mp3zoXQFKUKYj5(int,int,boolean,boolean)
        int halfHigh, halfLow;
        int axisX, axisZ;
        int forwardStart, forwardEnd, forwardStep;
        int lateralSign;
        HighwayState state = HighwayState.getInstance();
        boolean hasWalls = HighwayBuilder.hasWalls();
        int width = HighwayBuilder.getWidth();
        assert (MeteorClient.mc.player != null);
        int playerX = MeteorClient.mc.player.getX();
        int playerY = MeteorClient.mc.player.getY();
        int playerZ = MeteorClient.mc.player.getZ();
        int ceilY = playerY + 3;

        if (width % 2 == 0) { halfHigh = width / 2; halfLow = halfHigh - 1; }
        else                 { halfHigh = halfLow = (width - 1) / 2; }

        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        switch (dir) {
            case NORTH: { axisZ = 0; axisX = 1; forwardStart = ahead;  forwardEnd = -behind; forwardStep = -1; lateralSign =  1; playerX = state.getAlignX(); break; }
            case SOUTH: { axisZ = 0; axisX = 1; forwardStart = -ahead; forwardEnd =  behind; forwardStep =  1; lateralSign = -1; playerX = state.getAlignX(); break; }
            case WEST:  { axisZ = 1; axisX = 0; forwardStart = -ahead; forwardEnd =  behind; forwardStep =  1; lateralSign =  1; playerZ = state.getAlignZ(); break; }
            case EAST:  { axisZ = 1; axisX = 0; forwardStart = ahead;  forwardEnd = -behind; forwardStep = -1; lateralSign = -1; playerZ = state.getAlignZ(); break; }
            default: return new BlockPos[0];
        }

        ArrayList<BlockPos> result = new ArrayList<>();
        for (int i = forwardStart; i != forwardEnd + forwardStep; i += forwardStep) {
            // Main ceiling strip
            for (int lat = -halfHigh; lat <= halfLow; ++lat) {
                int wx = playerX + axisZ * i + axisX * (lat * lateralSign);
                int wz = playerZ + axisX * i + axisZ * (lat * lateralSign);
                result.add(new BlockPos(wx, ceilY, wz));
            }
            if (!hasWalls) continue;
            // Left/right ceiling wall blocks
            if (leftWall) {
                int lx = playerX + axisZ * i + axisX * ((-halfHigh - 1) * lateralSign);
                int lz = playerZ + axisX * i + axisZ * ((-halfHigh - 1) * lateralSign);
                result.add(new BlockPos(lx, ceilY, lz));
            }
            if (rightWall) {
                int rx = playerX + axisZ * i + axisX * ((halfLow + 1) * lateralSign);
                int rz = playerZ + axisX * i + axisZ * ((halfLow + 1) * lateralSign);
                result.add(new BlockPos(rx, ceilY, rz));
            }
        }
        return result.toArray(new BlockPos[0]);
    }

    /**
     * Returns floor positions (y = highwayY - 1) for diagonal directions (NE/NW/SE/SW).
     * Uses the saved highway origin coordinates from HighwayState.
     */
    public static BlockPos[] getDiagonalFloorPositions(int behind, int ahead, boolean leftWall, boolean rightWall) { // was: Gt56Sj4a6BWhgB(int,int,boolean,boolean)
        int halfHigh, halfLow;
        int fwdX, fwdZ;    // forward direction multipliers
        int latX, latZ;    // lateral (perpendicular) direction multipliers
        HighwayState state = HighwayState.getInstance();
        boolean hasWalls = HighwayBuilder.hasWalls();
        int width = HighwayBuilder.getWidth();
        assert (MeteorClient.mc.player != null);

        // Initialize origin from player position if not set
        if (state.getAlignStartX() == null) state.setAlignStartX(MeteorClient.mc.player.getX());
        if (state.getHighwayY()     == null) state.setHighwayY(MeteorClient.mc.player.getY());
        if (state.getAlignStartZ()  == null) state.setAlignStartZ(MeteorClient.mc.player.getZ());

        if (width % 2 == 0) { halfHigh = width / 2; halfLow = halfHigh - 1; }
        else                 { halfHigh = halfLow = (width - 1) / 2; }

        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        latX = 1; latZ = 1;
        switch (dir) {
            case NORTH_EAST: { fwdX =  1; fwdZ =  1; break; }                       // was: aiRs4cu
            case NORTH_WEST: { fwdX = -1; fwdZ =  1; latX = -1; break; }            // was: ZOY41p
            case SOUTH_WEST: { fwdX = -1; fwdZ = -1; latX = -1; latZ = -1; break; } // was: E8moug3IELf8
            case SOUTH_EAST: { fwdX =  1; fwdZ = -1; latZ = -1; break; }            // was: CsEhJrV
            default: return new BlockPos[0];
        }

        ArrayList<BlockPos> result = new ArrayList<>();
        for (int i = ahead; i >= -behind; --i) {
            // Along-Z strip (perpendicular in Z, forward in X)
            for (int lat = 1; lat <= halfHigh; ++lat) {
                int wx = state.getAlignStartX() + fwdX * i;
                int wz = state.getAlignStartZ() + fwdZ * i + lat * latZ;
                result.add(new BlockPos(wx, state.getHighwayY() - 1, wz));
            }
            if (hasWalls && leftWall) {
                int wx = state.getAlignStartX() + fwdX * i;
                int wz = state.getAlignStartZ() + fwdZ * i + (halfHigh + 1) * latZ;
                result.add(new BlockPos(wx, state.getHighwayY().intValue(), wz));
            }
            // Along-X strip (perpendicular in X, forward in Z)
            for (int lat = 0; lat <= halfLow; ++lat) {
                int wx = state.getAlignStartX() + fwdX * i + lat * latX;
                int wz = state.getAlignStartZ() + fwdZ * i;
                result.add(new BlockPos(wx, state.getHighwayY() - 1, wz));
            }
            if (!hasWalls || !rightWall) continue;
            int wx = state.getAlignStartX() + fwdX * i + (halfLow + 1) * latX;
            int wz = state.getAlignStartZ() + fwdZ * i;
            result.add(new BlockPos(wx, state.getHighwayY().intValue(), wz));
        }
        return result.toArray(new BlockPos[0]);
    }

    /**
     * Returns ceiling positions (y = highwayY + 3) for diagonal directions.
     */
    public static BlockPos[] getDiagonalCeilingPositions(int behind, int ahead, boolean leftWall, boolean rightWall) { // was: TAdu5cndwWu3A1(int,int,boolean,boolean)
        int halfHigh, halfLow;
        int fwdX, fwdZ, latX, latZ;
        HighwayState state = HighwayState.getInstance();
        boolean hasWalls = HighwayBuilder.hasWalls();
        int width = HighwayBuilder.getWidth();
        assert (MeteorClient.mc.player != null);

        if (state.getAlignStartX() == null) state.setAlignStartX(MeteorClient.mc.player.getX());
        if (state.getHighwayY()     == null) state.setHighwayY(MeteorClient.mc.player.getY());
        if (state.getAlignStartZ()  == null) state.setAlignStartZ(MeteorClient.mc.player.getZ());

        int ceilY = state.getHighwayY() + 3;

        if (width % 2 == 0) { halfHigh = width / 2; halfLow = halfHigh - 1; }
        else                 { halfHigh = halfLow = (width - 1) / 2; }

        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        latX = 1; latZ = 1;
        switch (dir) {
            case NORTH_EAST: { fwdX =  1; fwdZ =  1; break; }
            case NORTH_WEST: { fwdX = -1; fwdZ =  1; latX = -1; break; }
            case SOUTH_WEST: { fwdX = -1; fwdZ = -1; latX = -1; latZ = -1; break; }
            case SOUTH_EAST: { fwdX =  1; fwdZ = -1; latZ = -1; break; }
            default: return new BlockPos[0];
        }

        ArrayList<BlockPos> result = new ArrayList<>();
        for (int i = ahead; i >= -behind; --i) {
            for (int lat = 1; lat <= halfHigh; ++lat) {
                int wx = state.getAlignStartX() + fwdX * i;
                int wz = state.getAlignStartZ() + fwdZ * i + lat * latZ;
                result.add(new BlockPos(wx, ceilY, wz));
            }
            if (hasWalls && leftWall) {
                int wx = state.getAlignStartX() + fwdX * i;
                int wz = state.getAlignStartZ() + fwdZ * i + (halfHigh + 1) * latZ;
                result.add(new BlockPos(wx, ceilY, wz));
            }
            for (int lat = 0; lat <= halfLow; ++lat) {
                int wx = state.getAlignStartX() + fwdX * i + lat * latX;
                int wz = state.getAlignStartZ() + fwdZ * i;
                result.add(new BlockPos(wx, ceilY, wz));
            }
            if (!hasWalls || !rightWall) continue;
            int wx = state.getAlignStartX() + fwdX * i + (halfLow + 1) * latX;
            int wz = state.getAlignStartZ() + fwdZ * i;
            result.add(new BlockPos(wx, ceilY, wz));
        }
        return result.toArray(new BlockPos[0]);
    }

    /**
     * Returns clear-space positions (air column) for diagonal directions.
     * Height is 2 for normal mode, 3 for elytra mode.
     */
    public static BlockPos[] getDiagonalClearPositions(int behind, int ahead) { // was: VYEwzRq(int,int)
        int halfHigh, halfLow;
        int fwdX, fwdZ, latX, latZ;
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        int clearHeight = 2;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.ELYTRA) clearHeight = 3;

        if (state.getAlignStartX() == null) state.setAlignStartX(MeteorClient.mc.player.getX());
        if (state.getHighwayY()     == null) state.setHighwayY(MeteorClient.mc.player.getY());
        if (state.getAlignStartZ()  == null) state.setAlignStartZ(MeteorClient.mc.player.getZ());

        if (width % 2 == 0) { halfHigh = width / 2; halfLow = halfHigh - 1; }
        else                 { halfHigh = halfLow = (width - 1) / 2; }

        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        latX = 1; latZ = 1;
        switch (dir) {
            case NORTH_EAST: { fwdX =  1; fwdZ =  1; break; }
            case NORTH_WEST: { fwdX = -1; fwdZ =  1; latX = -1; break; }
            case SOUTH_WEST: { fwdX = -1; fwdZ = -1; latX = -1; latZ = -1; break; }
            case SOUTH_EAST: { fwdX =  1; fwdZ = -1; latZ = -1; break; }
            default: return new BlockPos[0];
        }

        ArrayList<BlockPos> result = new ArrayList<>();
        for (int i = ahead; i >= -behind; --i) {
            for (int h = 0; h <= clearHeight; ++h) {
                // Z-perpendicular arm
                for (int lat = 1; lat <= halfHigh; ++lat) {
                    int wx = state.getAlignStartX() + fwdX * i;
                    int wz = state.getAlignStartZ() + fwdZ * i + lat * latZ;
                    result.add(new BlockPos(wx, state.getHighwayY() + h, wz));
                }
                if (HighwayBuilder.hasWalls() && h > 0) {
                    int wx = state.getAlignStartX() + fwdX * i;
                    int wz = state.getAlignStartZ() + fwdZ * i + (halfHigh + 1) * latZ;
                    result.add(new BlockPos(wx, state.getHighwayY() + h, wz));
                }
                // X-perpendicular arm
                for (int lat = 0; lat <= halfLow; ++lat) {
                    int wx = state.getAlignStartX() + fwdX * i + lat * latX;
                    int wz = state.getAlignStartZ() + fwdZ * i;
                    result.add(new BlockPos(wx, state.getHighwayY() + h, wz));
                }
                if (!HighwayBuilder.hasWalls() || h <= 0) continue;
                int wx = state.getAlignStartX() + fwdX * i + (halfLow + 1) * latX;
                int wz = state.getAlignStartZ() + fwdZ * i;
                result.add(new BlockPos(wx, state.getHighwayY() + h, wz));
            }
        }
        return result.toArray(new BlockPos[0]);
    }

    /**
     * Returns the clear-space positions directly ahead of the player (4-5 blocks ahead),
     * including optional wall columns. Used by the forward-collision checker.
     */
    public static BlockPos[] getForwardClearPositions() { // was: IHeihwsO8p
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        if (HighwayBuilder.getDirection() == null) return new BlockPos[0];
        assert (MeteorClient.mc.player != null);

        if (state.getAlignStartX() == null) state.setAlignStartX(MeteorClient.mc.player.getX());
        if (state.getHighwayY()     == null) state.setHighwayY(MeteorClient.mc.player.getY());
        if (state.getAlignStartZ()  == null) state.setAlignStartZ(MeteorClient.mc.player.getZ());

        int halfHigh, halfLow;
        if (width % 2 == 0) { halfHigh = width / 2 + 1; halfLow = halfHigh - 1; }
        else                 { halfHigh = halfLow = (width - 1) / 2 + 1; }

        int clearHeight = 2;
        int scanDepth   = 4; // 4 blocks ahead

        return switch (HighwayBuilder.getDirection()) {
            case NORTH -> { // was: vSouwXdh7
                ArrayList<BlockPos> list = new ArrayList<>();
                if (HighwayBuilder.hasWalls()) {
                    for (int fwd = scanDepth; fwd > 0; --fwd) {
                        for (int h = 0; h < clearHeight; ++h) {
                            list.add(new BlockPos(state.getAlignStartX() - halfHigh, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + fwd));
                            list.add(new BlockPos(state.getAlignStartX() + halfLow,  state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + fwd));
                        }
                    }
                }
                for (int fwd = scanDepth; fwd > 0; --fwd) {
                    for (int lat = -halfHigh + 1; lat < halfLow; ++lat) {
                        for (int h = 0; h <= clearHeight; ++h) {
                            list.add(new BlockPos(state.getAlignStartX() + lat, state.getHighwayY() + h, MeteorClient.mc.player.getZ() + fwd));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case WEST -> { // was: S8iuqKQCrJM02b
                ArrayList<BlockPos> list = new ArrayList<>();
                if (HighwayBuilder.hasWalls()) {
                    for (int fwd = -scanDepth; fwd < 0; ++fwd) {
                        for (int h = 0; h < clearHeight; ++h) {
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + fwd, state.getHighwayY() + h + 1, state.getAlignStartZ() - halfHigh));
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + fwd, state.getHighwayY() + h + 1, state.getAlignStartZ() + halfLow));
                        }
                    }
                }
                for (int fwd = -scanDepth; fwd < 0; ++fwd) {
                    for (int lat = -halfHigh + 1; lat < halfLow; ++lat) {
                        for (int h = 0; h <= clearHeight; ++h) {
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + fwd, state.getHighwayY() + h, state.getAlignStartZ() + lat));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case SOUTH -> { // was: Q5FUNqd0ALfl
                ArrayList<BlockPos> list = new ArrayList<>();
                if (HighwayBuilder.hasWalls()) {
                    for (int fwd = -scanDepth; fwd < 0; ++fwd) {
                        for (int h = 0; h < clearHeight; ++h) {
                            list.add(new BlockPos(state.getAlignStartX() + halfHigh, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + fwd));
                            list.add(new BlockPos(state.getAlignStartX() - halfLow,  state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + fwd));
                        }
                    }
                }
                for (int fwd = -scanDepth; fwd < 0; ++fwd) {
                    for (int lat = halfHigh - 1; lat > -halfLow; --lat) {
                        for (int h = 0; h <= clearHeight; ++h) {
                            list.add(new BlockPos(state.getAlignStartX() + lat, state.getHighwayY() + h, MeteorClient.mc.player.getZ() + fwd));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case EAST -> { // was: v5UhyO9eEd7n
                ArrayList<BlockPos> list = new ArrayList<>();
                if (HighwayBuilder.hasWalls()) {
                    for (int fwd = scanDepth; fwd > 0; --fwd) {
                        for (int h = 0; h < clearHeight; ++h) {
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + fwd, state.getHighwayY() + h + 1, state.getAlignStartZ() + halfHigh));
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + fwd, state.getHighwayY() + h + 1, state.getAlignStartZ() - halfLow));
                        }
                    }
                }
                for (int fwd = scanDepth; fwd > 0; --fwd) {
                    for (int lat = halfHigh - 1; lat > -halfLow; --lat) {
                        for (int h = 0; h <= clearHeight; ++h) {
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + fwd, state.getHighwayY() + h, state.getAlignStartZ() + lat));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            default -> new BlockPos[]{};
        };
    }

    /**
     * Returns floor positions 4-7 blocks ahead (pavement positions about to be walked on).
     * Used to identify blocks that need placing before the player reaches them.
     */
    public static BlockPos[] getPavementPositionsAhead() { // was: PoixtDMvQM
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        if (HighwayBuilder.getDirection() == null) return new BlockPos[0];
        assert (MeteorClient.mc.player != null);

        if (state.getAlignStartX() == null) state.setAlignStartX(MeteorClient.mc.player.getX());
        if (state.getHighwayY()     == null) state.setHighwayY(MeteorClient.mc.player.getY());
        if (state.getAlignStartZ()  == null) state.setAlignStartZ(MeteorClient.mc.player.getZ());

        int halfHigh, halfLow;
        if (width % 2 == 0) { halfHigh = halfLow = width / 2; }
        else                 { halfHigh = halfLow = (width - 1) / 2; }

        return switch (HighwayBuilder.getDirection()) {
            case NORTH -> { // was: vSouwXdh7 — looking toward +Z
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int fwd = 4; fwd <= 7; ++fwd) {
                    int z = state.getAlignStartZ() + fwd;
                    for (int lat = -halfHigh; lat < halfLow; ++lat) {
                        list.add(new BlockPos(state.getAlignStartX() + lat, state.getHighwayY() - 1, z));
                    }
                    if (!HighwayBuilder.hasWalls()) continue;
                    list.add(new BlockPos(state.getAlignStartX() - halfHigh - 1, state.getHighwayY().intValue(), z));
                    list.add(new BlockPos(state.getAlignStartX() + halfLow  + 1, state.getHighwayY().intValue(), z));
                }
                yield list.toArray(new BlockPos[0]);
            }
            case WEST -> { // was: S8iuqKQCrJM02b — looking toward -X
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int fwd = 4; fwd <= 7; ++fwd) {
                    int x = state.getAlignStartX() - fwd;
                    for (int lat = -halfHigh; lat < halfLow; ++lat) {
                        list.add(new BlockPos(x, state.getHighwayY() - 1, state.getAlignStartZ() + lat));
                    }
                    if (!HighwayBuilder.hasWalls()) continue;
                    list.add(new BlockPos(x, state.getHighwayY().intValue(), state.getAlignStartZ() - halfHigh - 1));
                    list.add(new BlockPos(x, state.getHighwayY().intValue(), state.getAlignStartZ() + halfLow  + 1));
                }
                yield list.toArray(new BlockPos[0]);
            }
            case SOUTH -> { // was: Q5FUNqd0ALfl — looking toward -Z
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int fwd = 4; fwd <= 7; ++fwd) {
                    int z = state.getAlignStartZ() - fwd;
                    for (int lat = halfHigh; lat > -halfLow; --lat) {
                        list.add(new BlockPos(state.getAlignStartX() + lat, state.getHighwayY() - 1, z));
                    }
                    if (!HighwayBuilder.hasWalls()) continue;
                    list.add(new BlockPos(state.getAlignStartX() + halfHigh + 1, state.getHighwayY().intValue(), z));
                    list.add(new BlockPos(state.getAlignStartX() - halfLow  - 1, state.getHighwayY().intValue(), z));
                }
                yield list.toArray(new BlockPos[0]);
            }
            case EAST -> { // was: v5UhyO9eEd7n — looking toward +X
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int fwd = 4; fwd <= 7; ++fwd) {
                    int x = state.getAlignStartX() + fwd;
                    for (int lat = halfHigh; lat > -halfLow; --lat) {
                        list.add(new BlockPos(x, state.getHighwayY() - 1, state.getAlignStartZ() + lat));
                    }
                    if (!HighwayBuilder.hasWalls()) continue;
                    list.add(new BlockPos(x, state.getHighwayY().intValue(), state.getAlignStartZ() + halfHigh + 1));
                    list.add(new BlockPos(x, state.getHighwayY().intValue(), state.getAlignStartZ() - halfLow  - 1));
                }
                yield list.toArray(new BlockPos[0]);
            }
            default -> new BlockPos[]{};
        };
    }

    /**
     * Returns obstruction-scan positions 0-10 blocks behind the player for cardinal directions.
     * Used by WorldUtils.handleLavaRemoval() to detect lava that must be removed.
     */
    public static BlockPos[] getCardinalObstructionPositions() { // was: txFOGrboKBXQp
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        if (HighwayBuilder.getDirection() == null) return new BlockPos[0];
        assert (MeteorClient.mc.player != null);

        if (state.getAlignStartX() == null) state.setAlignStartX(MeteorClient.mc.player.getX());
        if (state.getHighwayY()     == null) state.setHighwayY(MeteorClient.mc.player.getY());
        if (state.getAlignStartZ()  == null) state.setAlignStartZ(MeteorClient.mc.player.getZ());

        int halfHigh, halfLow;
        if (width % 2 == 0) { halfHigh = width / 2 + 1; halfLow = halfHigh - 1; }
        else                 { halfHigh = halfLow = (width - 1) / 2 + 1; }

        int clearHeight = 0;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.NORMAL) clearHeight = 3;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.ELYTRA) clearHeight = 4;

        return switch (HighwayBuilder.getDirection()) {
            case NORTH -> { // was: vSouwXdh7
                ArrayList<BlockPos> list = new ArrayList<>();
                if (HighwayBuilder.hasWalls()) {
                    for (int off = 0; off > -10; --off) {
                        for (int h = 0; h < clearHeight; ++h) {
                            list.add(new BlockPos(state.getAlignStartX() - halfHigh - 1, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + off));
                            list.add(new BlockPos(state.getAlignStartX() + halfLow  + 1, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + off));
                            list.add(new BlockPos(state.getAlignStartX() - halfHigh,     state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + off));
                            list.add(new BlockPos(state.getAlignStartX() + halfLow,      state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + off));
                        }
                    }
                }
                for (int off = 0; off >= -10; --off) {
                    for (int lat = -halfHigh; lat < halfLow; ++lat) {
                        for (int h = 0; h <= clearHeight; ++h) {
                            list.add(new BlockPos(state.getAlignStartX() + lat, state.getHighwayY() + h, MeteorClient.mc.player.getZ() + off));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case WEST -> { // was: S8iuqKQCrJM02b
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int off = 10; off >= 0; --off) {
                    for (int h = 0; h < clearHeight; ++h) {
                        list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() - halfHigh - 1));
                        list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + halfLow  + 1));
                        list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() - halfHigh));
                        list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + halfLow));
                    }
                }
                for (int off = 10; off >= 0; --off) {
                    for (int lat = -halfHigh + 1; lat < halfLow; ++lat) {
                        for (int h = 0; h <= clearHeight; ++h) {
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h, state.getAlignStartZ() + lat));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case SOUTH -> { // was: Q5FUNqd0ALfl
                ArrayList<BlockPos> list = new ArrayList<>();
                if (HighwayBuilder.hasWalls()) {
                    for (int off = 10; off >= 0; --off) {
                        for (int h = 0; h < clearHeight; ++h) {
                            list.add(new BlockPos(state.getAlignStartX() + halfHigh,     state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + off));
                            list.add(new BlockPos(state.getAlignStartX() - halfLow,      state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + off));
                            list.add(new BlockPos(state.getAlignStartX() + halfHigh + 1, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + off));
                            list.add(new BlockPos(state.getAlignStartX() - halfLow  - 1, state.getHighwayY() + h + 1, MeteorClient.mc.player.getZ() + off));
                        }
                    }
                }
                for (int off = 10; off >= 0; --off) {
                    for (int lat = halfHigh; lat > -halfLow; --lat) {
                        for (int h = 0; h <= clearHeight; ++h) {
                            list.add(new BlockPos(state.getAlignStartX() + lat, state.getHighwayY() + h, MeteorClient.mc.player.getZ() + off));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case EAST -> { // was: v5UhyO9eEd7n
                ArrayList<BlockPos> list = new ArrayList<>();
                if (HighwayBuilder.hasWalls()) {
                    for (int off = -10; off <= 0; ++off) {
                        for (int h = 0; h < clearHeight; ++h) {
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h + 1, state.getAlignStartZ() + halfHigh));
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h + 1, state.getAlignStartZ() - halfLow));
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h + 1, state.getAlignStartZ() + halfHigh + 1));
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h + 1, state.getAlignStartZ() - halfLow  - 1));
                        }
                    }
                }
                for (int off = -10; off <= 0; ++off) {
                    for (int lat = halfHigh; lat > -halfLow; --lat) {
                        for (int h = 0; h <= clearHeight; ++h) {
                            list.add(new BlockPos(MeteorClient.mc.player.getX() + off, state.getHighwayY() + h, state.getAlignStartZ() + lat));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            default -> new BlockPos[]{};
        };
    }

    /**
     * Returns obstruction-scan positions for diagonal directions (3-8 blocks diagonally behind).
     * Used by WorldUtils.handleLavaRemoval() for diagonal highways.
     */
    public static BlockPos[] getDiagonalObstructionPositions() { // was: HP7CUOuiyLUHkEkD
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        int clearHeight = 0;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.NORMAL) clearHeight = 3;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.ELYTRA) clearHeight = 4;

        int halfHigh, halfLow;
        if (width % 2 == 0) { halfHigh = halfLow = width / 2; }
        else                 { halfHigh = halfLow = (width - 1) / 2; }

        assert (MeteorClient.mc.player != null);
        if (state.getAlignStartX() == null) state.setAlignStartX(MeteorClient.mc.player.getX());
        if (state.getHighwayY()     == null) state.setHighwayY(MeteorClient.mc.player.getY());
        if (state.getAlignStartZ()  == null) state.setAlignStartZ(MeteorClient.mc.player.getZ());

        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        return switch (dir) {
            case NORTH_EAST -> { // was: aiRs4cu
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int i = -8; i <= -3; ++i) {
                    for (int h = 0; h <= clearHeight; ++h) {
                        for (int lat = halfHigh; lat > 0; --lat)
                            list.add(new BlockPos(state.getAlignStartX() + i, state.getHighwayY() + h, state.getAlignStartZ() + lat + i));
                        if (h > 0) {
                            list.add(new BlockPos(state.getAlignStartX() + i, state.getHighwayY() + h, state.getAlignStartZ() + halfHigh + 1 + i));
                            list.add(new BlockPos(state.getAlignStartX() + i, state.getHighwayY() + h, state.getAlignStartZ() + halfHigh + 2 + i));
                        }
                        for (int lat = halfLow; lat >= 0; --lat)
                            list.add(new BlockPos(state.getAlignStartX() + lat + i, state.getHighwayY() + h, state.getAlignStartZ() + i));
                        if (h > 0) {
                            list.add(new BlockPos(state.getAlignStartX() + halfLow + 1 + i, state.getHighwayY() + h, state.getAlignStartZ() + i));
                            list.add(new BlockPos(state.getAlignStartX() + halfLow + 2 + i, state.getHighwayY() + h, state.getAlignStartZ() + i));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case NORTH_WEST -> { // was: ZOY41p
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int i = -8; i <= -3; ++i) {
                    for (int h = 0; h <= clearHeight; ++h) {
                        for (int lat = halfHigh; lat > 0; --lat)
                            list.add(new BlockPos(state.getAlignStartX() - i, state.getHighwayY() + h, state.getAlignStartZ() + lat + i));
                        if (h > 0) {
                            list.add(new BlockPos(state.getAlignStartX() - i, state.getHighwayY() + h, state.getAlignStartZ() + halfHigh + 1 + i));
                            list.add(new BlockPos(state.getAlignStartX() - i, state.getHighwayY() + h, state.getAlignStartZ() + halfHigh + 2 + i));
                        }
                        for (int lat = halfLow; lat >= 0; --lat)
                            list.add(new BlockPos(state.getAlignStartX() - lat - i, state.getHighwayY() + h, state.getAlignStartZ() + i));
                        if (h > 0) {
                            list.add(new BlockPos(state.getAlignStartX() - halfLow - 1 - i, state.getHighwayY() + h, state.getAlignStartZ() + i));
                            list.add(new BlockPos(state.getAlignStartX() - halfLow - 2 - i, state.getHighwayY() + h, state.getAlignStartZ() + i));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case SOUTH_WEST -> { // was: E8moug3IELf8
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int i = -8; i <= -3; ++i) {
                    for (int h = 0; h <= clearHeight; ++h) {
                        for (int lat = halfHigh; lat > 0; --lat)
                            list.add(new BlockPos(state.getAlignStartX() - i, state.getHighwayY() + h, state.getAlignStartZ() - lat - i));
                        if (h > 0) {
                            list.add(new BlockPos(state.getAlignStartX() - i, state.getHighwayY() + h, state.getAlignStartZ() - halfHigh - 1 - i));
                            list.add(new BlockPos(state.getAlignStartX() - i, state.getHighwayY() + h, state.getAlignStartZ() - halfHigh - 2 - i));
                        }
                        for (int lat = halfLow; lat >= 0; --lat)
                            list.add(new BlockPos(state.getAlignStartX() - lat - i, state.getHighwayY() + h, state.getAlignStartZ() - i));
                        if (h > 0) {
                            list.add(new BlockPos(state.getAlignStartX() - halfLow - 1 - i, state.getHighwayY() + h, state.getAlignStartZ() - i));
                            list.add(new BlockPos(state.getAlignStartX() - halfLow - 2 - i, state.getHighwayY() + h, state.getAlignStartZ() - i));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            case SOUTH_EAST -> { // was: CsEhJrV
                ArrayList<BlockPos> list = new ArrayList<>();
                for (int i = -8; i <= -3; ++i) {
                    for (int h = 0; h <= clearHeight; ++h) {
                        for (int lat = halfHigh; lat > 0; --lat)
                            list.add(new BlockPos(state.getAlignStartX() + i, state.getHighwayY() + h, state.getAlignStartZ() - lat - i));
                        if (h > 0) {
                            list.add(new BlockPos(state.getAlignStartX() + i, state.getHighwayY() + h, state.getAlignStartZ() - halfHigh - 1 - i));
                            list.add(new BlockPos(state.getAlignStartX() + i, state.getHighwayY() + h, state.getAlignStartZ() - halfHigh - 2 - i));
                        }
                        for (int lat = halfLow; lat >= 0; --lat)
                            list.add(new BlockPos(state.getAlignStartX() + lat + i, state.getHighwayY() + h, state.getAlignStartZ() - i));
                        if (h > 0) {
                            list.add(new BlockPos(state.getAlignStartX() + halfLow + 1 + i, state.getHighwayY() + h, state.getAlignStartZ() - i));
                            list.add(new BlockPos(state.getAlignStartX() + halfLow + 2 + i, state.getHighwayY() + h, state.getAlignStartZ() - i));
                        }
                    }
                }
                yield list.toArray(new BlockPos[0]);
            }
            default -> new BlockPos[]{};
        };
    }
}
