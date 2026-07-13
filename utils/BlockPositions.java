// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.TKzj7u)
package musheor.utils;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import musheor.modules.automation.HighwayBuilder;
import musheor.utils.internal.HighwayLocator;
import musheor.utils.internal.HighwayState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Generates the block-position patterns the HighwayBuilder places/breaks: floor,
 * ceiling, tunnel, wall-scan, look-ahead and front-clearing zones, in both
 * cardinal and diagonal orientations. Pure coordinate geometry (u/v rotated axes
 * for diagonals). {@code f}/{@code b} are the forward/back extents of the window.
 */
public class BlockPositions {

    /** Cardinal floor row (y-1) with optional side rails. */
    @NotNull
    public static BlockPos[] cardinalFloor(int f, int b, boolean leftRail, boolean rightRail) { // was: FvaNWO(int,int,boolean,boolean)
        HighwayState state = HighwayState.getInstance();
        boolean placeRails = HighwayBuilder.placeRails();
        int width = HighwayBuilder.getWidth();
        WorldUtils.Direction8 direction = HighwayBuilder.getDirection();
        int playerX = MeteorClient.mc.player.getBlockX();
        int playerY = state.getCenterY();
        int playerZ = MeteorClient.mc.player.getBlockZ();
        int dx, dz, start, end, step, sideSign;
        switch (direction) {
            case NORTH -> { dx = 0; dz = 1; start = b; end = -f; step = -1; sideSign = 1; playerX = state.getStartX(); }
            case SOUTH -> { dx = 0; dz = 1; start = -b; end = f; step = 1; sideSign = -1; playerX = state.getStartX(); }
            case EAST -> { dx = 1; dz = 0; start = -b; end = f; step = 1; sideSign = 1; playerZ = state.getStartZ(); }
            case WEST -> { dx = 1; dz = 0; start = b; end = -f; step = -1; sideSign = -1; playerZ = state.getStartZ(); }
            default -> { return new BlockPos[0]; }
        }
        int leftBound, rightBound;
        if (width % 2 == 0) { leftBound = width / 2; rightBound = leftBound - 1; }
        else { leftBound = rightBound = (width - 1) / 2; }

        List<BlockPos> positions = new ArrayList<>();
        for (int i = start; i != end + step; i += step) {
            for (int j = -leftBound; j <= rightBound; j++) {
                int x = playerX + dx * i + dz * j * sideSign;
                int z = playerZ + dz * i + dx * j * sideSign;
                positions.add(new BlockPos(x, playerY - 1, z));
            }
            if (placeRails) {
                int leftRailX = playerX + dx * i + dz * (-leftBound - 1) * sideSign;
                int leftRailZ = playerZ + dz * i + dx * (-leftBound - 1) * sideSign;
                int rightRailX = playerX + dx * i + dz * (rightBound + 1) * sideSign;
                int rightRailZ = playerZ + dz * i + dx * (rightBound + 1) * sideSign;
                if (leftRail) positions.add(new BlockPos(leftRailX, playerY, leftRailZ));
                if (rightRail) positions.add(new BlockPos(rightRailX, playerY, rightRailZ));
            }
        }
        return positions.toArray(new BlockPos[0]);
    }

    /** Cardinal full-height tunnel clearing zone (with optional wall columns). */
    public static BlockPos[] cardinalTunnel(int f, int b) { // was: FvaNWO(int,int)
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        int height = 2;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.DIG) height = 3;
        WorldUtils.Direction8 direction = HighwayBuilder.getDirection();
        int playerX = MeteorClient.mc.player.getBlockX();
        int playerY = state.getCenterY();
        int playerZ = MeteorClient.mc.player.getBlockZ();
        int dx, dz, start, end, step, sideSign;
        switch (direction) {
            case NORTH -> { dx = 0; dz = 1; start = b; end = -f; step = -1; sideSign = 1; playerX = state.getStartX(); }
            case SOUTH -> { dx = 0; dz = 1; start = -b; end = f; step = 1; sideSign = -1; playerX = state.getStartX(); }
            case EAST -> { dx = 1; dz = 0; start = -b; end = f; step = 1; sideSign = 1; playerZ = state.getStartZ(); }
            case WEST -> { dx = 1; dz = 0; start = b; end = -f; step = -1; sideSign = -1; playerZ = state.getStartZ(); }
            default -> { return new BlockPos[0]; }
        }
        int leftBound, rightBound;
        if (width % 2 == 0) { leftBound = width / 2; rightBound = leftBound - 1; }
        else { leftBound = rightBound = (width - 1) / 2; }

        List<BlockPos> positions = new ArrayList<>();
        for (int i = start; i != end + step; i += step) {
            for (int j = 0; j <= height; j++) {
                for (int k = -leftBound; k <= rightBound; k++) {
                    int x = playerX + dx * i + dz * k * sideSign;
                    int z = playerZ + dz * i + dx * k * sideSign;
                    positions.add(new BlockPos(x, playerY + j, z));
                }
                if (HighwayBuilder.mineAboveRails() && j > 0) {
                    int leftRailX = playerX + dx * i + dz * (-leftBound - 1) * sideSign;
                    int leftRailZ = playerZ + dz * i + dx * (-leftBound - 1) * sideSign;
                    int rightRailX = playerX + dx * i + dz * (rightBound + 1) * sideSign;
                    int rightRailZ = playerZ + dz * i + dx * (rightBound + 1) * sideSign;
                    positions.add(new BlockPos(leftRailX, playerY + j, leftRailZ));
                    positions.add(new BlockPos(rightRailX, playerY + j, rightRailZ));
                }
            }
        }
        return positions.toArray(new BlockPos[0]);
    }

    /** Cardinal ceiling row (y+3) with optional rails. */
    public static BlockPos[] cardinalCeiling(int f, int b, boolean leftRail, boolean rightRail) { // was: Q90GLXQ0Pef(int,int,boolean,boolean)
        HighwayState state = HighwayState.getInstance();
        boolean placeRails = HighwayBuilder.placeRails();
        int width = HighwayBuilder.getWidth();
        assert MeteorClient.mc.player != null;
        int playerX = MeteorClient.mc.player.getBlockX();
        int playerY = MeteorClient.mc.player.getBlockY();
        int playerZ = MeteorClient.mc.player.getBlockZ();
        int ceilingY = playerY + 3;
        int leftBound, rightBound;
        if (width % 2 == 0) { leftBound = width / 2; rightBound = leftBound - 1; }
        else { leftBound = rightBound = (width - 1) / 2; }
        WorldUtils.Direction8 direction = HighwayBuilder.getDirection();
        int dx, dz, start, end, step, sideSign;
        switch (direction) {
            case NORTH -> { dx = 0; dz = 1; start = b; end = -f; step = -1; sideSign = 1; playerX = state.getStartX(); }
            case SOUTH -> { dx = 0; dz = 1; start = -b; end = f; step = 1; sideSign = -1; playerX = state.getStartX(); }
            case EAST -> { dx = 1; dz = 0; start = -b; end = f; step = 1; sideSign = 1; playerZ = state.getStartZ(); }
            case WEST -> { dx = 1; dz = 0; start = b; end = -f; step = -1; sideSign = -1; playerZ = state.getStartZ(); }
            default -> { return new BlockPos[0]; }
        }
        List<BlockPos> positions = new ArrayList<>();
        for (int i = start; i != end + step; i += step) {
            for (int j = -leftBound; j <= rightBound; j++) {
                int x = playerX + dx * i + dz * j * sideSign;
                int z = playerZ + dz * i + dx * j * sideSign;
                positions.add(new BlockPos(x, ceilingY, z));
            }
            if (placeRails) {
                if (leftRail) {
                    int x = playerX + dx * i + dz * (-leftBound - 1) * sideSign;
                    int z = playerZ + dz * i + dx * (-leftBound - 1) * sideSign;
                    positions.add(new BlockPos(x, ceilingY, z));
                }
                if (rightRail) {
                    int x = playerX + dx * i + dz * (rightBound + 1) * sideSign;
                    int z = playerZ + dz * i + dx * (rightBound + 1) * sideSign;
                    positions.add(new BlockPos(x, ceilingY, z));
                }
            }
        }
        return positions.toArray(new BlockPos[0]);
    }

    /** Adds diagonal cells (rotated u/v axes) whose parity matches, at height {@code y}. */
    private static void addDiagonalCells(int vMin, int vMax, int uMin, int uMax, int K, boolean isNESW, int y, List<BlockPos> out) { // was: FvaNWO(int,int,int,int,int,boolean,int,List)
        for (int v = vMin; v <= vMax; v++) {
            for (int u = uMin; u <= uMax; u++) {
                if ((u + K + v & 1) == 0) {
                    int x = u + K + v >> 1;
                    int z = isNESW ? u + K - v >> 1 : v - u - K >> 1;
                    out.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    /** Computes {K, halfWidth, vMin, vMax, buildY, isNESW} for a diagonal window, or null. */
    private static int[] computeDiagonalRange(HighwayState state, int f, int b) { // was: FvaNWO(HighwayState,int,int)
        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        if (dir != WorldUtils.Direction8.NORTH_EAST && dir != WorldUtils.Direction8.SOUTH_WEST
            && dir != WorldUtils.Direction8.NORTH_WEST && dir != WorldUtils.Direction8.SOUTH_EAST) {
            return null;
        }
        int px = state.getCenterX();
        int pz = state.getCenterZ();
        boolean isNESW = dir == WorldUtils.Direction8.NORTH_EAST || dir == WorldUtils.Direction8.SOUTH_WEST;
        HighwayLocator.Checkpoint detected = state.getCurrentCheckpoint();
        int K = detected != null ? (int) detected.axisValue : (isNESW ? px + pz : px - pz);
        int HW = (HighwayBuilder.getWidth() - 1) / 2;
        int lpx = MeteorClient.mc.player.getBlockX();
        int lpz = MeteorClient.mc.player.getBlockZ();
        int vPlayer = isNESW ? lpx - lpz : lpx + lpz;
        boolean vInc = dir == WorldUtils.Direction8.NORTH_EAST || dir == WorldUtils.Direction8.SOUTH_EAST;
        int vMin = vInc ? vPlayer - 2 * b : vPlayer - 2 * f;
        int vMax = vInc ? vPlayer + 2 * f : vPlayer + 2 * b;
        return new int[]{K, HW, vMin, vMax, state.getCenterY(), isNESW ? 1 : 0};
    }

    /** Diagonal floor cells (y-1) with optional rails. */
    public static BlockPos[] diagonalFloor(int f, int b, boolean leftRail, boolean rightRail) { // was: psJq59YIbp3Z(int,int,boolean,boolean)
        HighwayState state = HighwayState.getInstance();
        assert MeteorClient.mc.player != null;
        ensureCenter(state);
        int[] s = computeDiagonalRange(state, f, b);
        if (s == null) return new BlockPos[0];
        int K = s[0], HW = s[1], vMin = s[2], vMax = s[3], py = s[4];
        boolean isNESW = s[5] == 1;
        List<BlockPos> positions = new ArrayList<>();
        addDiagonalCells(vMin, vMax, -HW, HW, K, isNESW, py - 1, positions);
        if (HighwayBuilder.placeRails()) {
            int leftU = isNESW ? HW + 1 : -(HW + 1);
            int rightU = isNESW ? -(HW + 1) : HW + 1;
            if (leftRail) addDiagonalCells(vMin, vMax, leftU, leftU, K, isNESW, py, positions);
            if (rightRail) addDiagonalCells(vMin, vMax, rightU, rightU, K, isNESW, py, positions);
        }
        return positions.toArray(new BlockPos[0]);
    }

    /** Diagonal ceiling cells (y+3) with optional rails. */
    public static BlockPos[] diagonalCeiling(int f, int b, boolean leftRail, boolean rightRail) { // was: SOYyh5IPg26f7F(int,int,boolean,boolean)
        HighwayState state = HighwayState.getInstance();
        assert MeteorClient.mc.player != null;
        ensureCenter(state);
        int[] s = computeDiagonalRange(state, f, b);
        if (s == null) return new BlockPos[0];
        int K = s[0], HW = s[1], vMin = s[2], vMax = s[3], py = s[4];
        boolean isNESW = s[5] == 1;
        int ceilingY = py + 3;
        List<BlockPos> positions = new ArrayList<>();
        addDiagonalCells(vMin, vMax, -HW, HW, K, isNESW, ceilingY, positions);
        if (HighwayBuilder.placeRails()) {
            int leftU = isNESW ? HW + 1 : -(HW + 1);
            int rightU = isNESW ? -(HW + 1) : HW + 1;
            if (leftRail) addDiagonalCells(vMin, vMax, leftU, leftU, K, isNESW, ceilingY, positions);
            if (rightRail) addDiagonalCells(vMin, vMax, rightU, rightU, K, isNESW, ceilingY, positions);
        }
        return positions.toArray(new BlockPos[0]);
    }

    /** Diagonal full-height tunnel clearing zone (with optional wall columns). */
    public static BlockPos[] diagonalTunnel(int f, int b) { // was: Q90GLXQ0Pef(int,int)
        HighwayState state = HighwayState.getInstance();
        int height = 2;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.DIG) height = 3;
        ensureCenter(state);
        int[] s = computeDiagonalRange(state, f, b);
        if (s == null) return new BlockPos[0];
        int K = s[0], HW = s[1], vMin = s[2], vMax = s[3], py = s[4];
        boolean isNESW = s[5] == 1;
        List<BlockPos> positions = new ArrayList<>();
        for (int j = 0; j <= height; j++) {
            addDiagonalCells(vMin, vMax, -HW, HW, K, isNESW, py + j, positions);
            if (HighwayBuilder.mineAboveRails() && j > 0) {
                addDiagonalCells(vMin, vMax, -(HW + 1), -(HW + 1), K, isNESW, py + j, positions);
                addDiagonalCells(vMin, vMax, HW + 1, HW + 1, K, isNESW, py + j, positions);
            }
        }
        return positions.toArray(new BlockPos[0]);
    }

    /** Cardinal wall-scan zone (looks a short way ahead at full width+1, both walls). */
    public static BlockPos[] cardinalWallScan() { // was: FvaNWO()
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        int leftBound = 0, rightBound;
        if (HighwayBuilder.getDirection() == null) return new BlockPos[0];
        assert MeteorClient.mc.player != null;
        ensureCenter(state);
        if (width % 2 == 0) leftBound = width / 2 + 1;
        rightBound = leftBound - 1;
        if (width % 2 != 0) leftBound = rightBound = (width - 1) / 2 + 1;
        int height = 2, length = 4;
        int cx = state.getCenterX(), cy = state.getCenterY(), cz = state.getCenterZ();
        int px = MeteorClient.mc.player.getBlockX(), pz = MeteorClient.mc.player.getBlockZ();
        return switch (HighwayBuilder.getDirection()) {
            case NORTH -> {
                List<BlockPos> positions = new ArrayList<>();
                if (HighwayBuilder.mineAboveRails())
                    for (int i = length; i > 0; i--)
                        for (int k = 0; k < height; k++) {
                            positions.add(new BlockPos(cx - leftBound, cy + k + 1, pz + i));
                            positions.add(new BlockPos(cx + rightBound, cy + k + 1, pz + i));
                        }
                for (int i = length; i > 0; i--)
                    for (int j = -leftBound + 1; j < rightBound; j++)
                        for (int k = 0; k <= height; k++)
                            positions.add(new BlockPos(cx + j, cy + k, pz + i));
                yield positions.toArray(new BlockPos[0]);
            }
            case SOUTH -> {
                List<BlockPos> positions = new ArrayList<>();
                if (HighwayBuilder.mineAboveRails())
                    for (int i = -length; i < 0; i++)
                        for (int k = 0; k < height; k++) {
                            positions.add(new BlockPos(cx + leftBound, cy + k + 1, pz + i));
                            positions.add(new BlockPos(cx - rightBound, cy + k + 1, pz + i));
                        }
                for (int i = -length; i < 0; i++)
                    for (int j = leftBound - 1; j > -rightBound; j--)
                        for (int k = 0; k <= height; k++)
                            positions.add(new BlockPos(cx + j, cy + k, pz + i));
                yield positions.toArray(new BlockPos[0]);
            }
            case EAST -> {
                List<BlockPos> positions = new ArrayList<>();
                if (HighwayBuilder.mineAboveRails())
                    for (int i = -length; i < 0; i++)
                        for (int k = 0; k < height; k++) {
                            positions.add(new BlockPos(cx + i, cy + k + 1, pz - leftBound));
                            positions.add(new BlockPos(cx + i, cy + k + 1, pz + rightBound));
                        }
                for (int i = -length; i < 0; i++)
                    for (int j = -leftBound + 1; j < rightBound; j++)
                        for (int k = 0; k <= height; k++)
                            positions.add(new BlockPos(px + i, cy + k, cz + j));
                yield positions.toArray(new BlockPos[0]);
            }
            case WEST -> {
                List<BlockPos> positions = new ArrayList<>();
                if (HighwayBuilder.mineAboveRails())
                    for (int i = length; i > 0; i--)
                        for (int k = 0; k < height; k++) {
                            positions.add(new BlockPos(px + i, cy + k + 1, cz + leftBound));
                            positions.add(new BlockPos(px + i, cy + k + 1, cz - rightBound));
                        }
                for (int i = length; i > 0; i--)
                    for (int j = leftBound - 1; j > -rightBound; j--)
                        for (int k = 0; k <= height; k++)
                            positions.add(new BlockPos(px + i, cy + k, cz + j));
                yield positions.toArray(new BlockPos[0]);
            }
            default -> new BlockPos[0];
        };
    }

    /** Cardinal floor look-ahead row (i = 4..7 ahead), used for pre-paving. */
    public static BlockPos[] cardinalFloorAhead() { // was: Q90GLXQ0Pef()
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        int leftBound = 0, rightBound;
        if (HighwayBuilder.getDirection() == null) return new BlockPos[0];
        assert MeteorClient.mc.player != null;
        ensureCenter(state);
        if (width % 2 == 0) leftBound = width / 2;
        rightBound = leftBound - 1;
        if (width % 2 != 0) leftBound = rightBound = (width - 1) / 2;
        int cx = state.getCenterX(), cy = state.getCenterY(), cz = state.getCenterZ();
        return switch (HighwayBuilder.getDirection()) {
            case NORTH -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = 4; i <= 7; i++) {
                    int z = cz + i;
                    for (int j = -leftBound; j < rightBound; j++) positions.add(new BlockPos(cx + j, cy - 1, z));
                    if (HighwayBuilder.placeRails()) {
                        positions.add(new BlockPos(cx - leftBound - 1, cy, z));
                        positions.add(new BlockPos(cx + rightBound + 1, cy, z));
                    }
                }
                yield positions.toArray(new BlockPos[0]);
            }
            case SOUTH -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = 4; i <= 7; i++) {
                    int z = cz - i;
                    for (int j = leftBound; j > -rightBound; j--) positions.add(new BlockPos(cx + j, cy - 1, z));
                    if (HighwayBuilder.placeRails()) {
                        positions.add(new BlockPos(cx + leftBound + 1, cy, z));
                        positions.add(new BlockPos(cx - rightBound - 1, cy, z));
                    }
                }
                yield positions.toArray(new BlockPos[0]);
            }
            case EAST -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = 4; i <= 7; i++) {
                    int x = cx - i;
                    for (int j = -leftBound; j < rightBound; j++) positions.add(new BlockPos(x, cy - 1, cz + j));
                    if (HighwayBuilder.placeRails()) {
                        positions.add(new BlockPos(x, cy, cz - leftBound - 1));
                        positions.add(new BlockPos(x, cy, cz + rightBound + 1));
                    }
                }
                yield positions.toArray(new BlockPos[0]);
            }
            case WEST -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = 4; i <= 7; i++) {
                    int x = cx + i;
                    for (int j = leftBound; j > -rightBound; j--) positions.add(new BlockPos(x, cy - 1, cz + j));
                    if (HighwayBuilder.placeRails()) {
                        positions.add(new BlockPos(x, cy, cz + leftBound + 1));
                        positions.add(new BlockPos(x, cy, cz - rightBound - 1));
                    }
                }
                yield positions.toArray(new BlockPos[0]);
            }
            default -> new BlockPos[0];
        };
    }

    /** Cardinal front-clearing zone (i = 0..-10, full height) — scanned for obstructing lava. */
    public static BlockPos[] cardinalFrontRow() { // was: psJq59YIbp3Z()
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        int leftBound = 0, rightBound;
        if (HighwayBuilder.getDirection() == null) return new BlockPos[0];
        assert MeteorClient.mc.player != null;
        ensureCenter(state);
        if (width % 2 == 0) leftBound = width / 2 + 1;
        rightBound = leftBound - 1;
        if (width % 2 != 0) leftBound = rightBound = (width - 1) / 2 + 1;
        int height = 0;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.PAVE) height = 3;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.DIG) height = 4;
        int cx = state.getCenterX(), cy = state.getCenterY(), cz = state.getCenterZ();
        int px = MeteorClient.mc.player.getBlockX(), pz = MeteorClient.mc.player.getBlockZ();
        return switch (HighwayBuilder.getDirection()) {
            case NORTH -> {
                List<BlockPos> positions = new ArrayList<>();
                if (HighwayBuilder.mineAboveRails())
                    for (int i = 0; i > -10; i--)
                        for (int k = 0; k < height; k++) {
                            positions.add(new BlockPos(cx - leftBound - 1, cy + k + 1, pz + i));
                            positions.add(new BlockPos(cx + rightBound + 1, cy + k + 1, pz + i));
                            positions.add(new BlockPos(cx - leftBound, cy + k + 1, pz + i));
                            positions.add(new BlockPos(cx + rightBound, cy + k + 1, pz + i));
                        }
                for (int i = 0; i >= -10; i--)
                    for (int j = -leftBound; j < rightBound; j++)
                        for (int k = 0; k <= height; k++)
                            positions.add(new BlockPos(cx + j, cy + k, pz + i));
                yield positions.toArray(new BlockPos[0]);
            }
            case SOUTH -> {
                List<BlockPos> positions = new ArrayList<>();
                if (HighwayBuilder.mineAboveRails())
                    for (int i = 10; i >= 0; i--)
                        for (int k = 0; k < height; k++) {
                            positions.add(new BlockPos(cx + leftBound, cy + k + 1, pz + i));
                            positions.add(new BlockPos(cx - rightBound, cy + k + 1, pz + i));
                            positions.add(new BlockPos(cx + leftBound + 1, cy + k + 1, pz + i));
                            positions.add(new BlockPos(cx - rightBound - 1, cy + k + 1, pz + i));
                        }
                for (int i = 10; i >= 0; i--)
                    for (int j = leftBound; j > -rightBound; j--)
                        for (int k = 0; k <= height; k++)
                            positions.add(new BlockPos(cx + j, cy + k, pz + i));
                yield positions.toArray(new BlockPos[0]);
            }
            case EAST -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = 10; i >= 0; i--)
                    for (int k = 0; k < height; k++) {
                        positions.add(new BlockPos(px + i, cy + k + 1, pz - leftBound - 1));
                        positions.add(new BlockPos(px + i, cy + k + 1, pz + rightBound + 1));
                        positions.add(new BlockPos(px + i, cy + k + 1, pz - leftBound));
                        positions.add(new BlockPos(px + i, cy + k + 1, pz + rightBound));
                    }
                for (int i = 10; i >= 0; i--)
                    for (int j = -leftBound + 1; j < rightBound; j++)
                        for (int k = 0; k <= height; k++)
                            positions.add(new BlockPos(px + i, cy + k, cz + j));
                yield positions.toArray(new BlockPos[0]);
            }
            case WEST -> {
                List<BlockPos> positions = new ArrayList<>();
                if (HighwayBuilder.mineAboveRails())
                    for (int i = -10; i <= 0; i++)
                        for (int k = 0; k < height; k++) {
                            positions.add(new BlockPos(px + i, cy + k + 1, cz + leftBound));
                            positions.add(new BlockPos(px + i, cy + k + 1, cz - rightBound));
                            positions.add(new BlockPos(px + i, cy + k + 1, cz + leftBound + 1));
                            positions.add(new BlockPos(px + i, cy + k + 1, cz - rightBound - 1));
                        }
                for (int i = -10; i <= 0; i++)
                    for (int j = leftBound; j > -rightBound; j--)
                        for (int k = 0; k <= height; k++)
                            positions.add(new BlockPos(px + i, cy + k, cz + j));
                yield positions.toArray(new BlockPos[0]);
            }
            default -> new BlockPos[0];
        };
    }

    /** Diagonal front-clearing zone (i = -8..-3, full height) — scanned for obstructing lava. */
    public static BlockPos[] diagonalFrontRow() { // was: SOYyh5IPg26f7F()
        HighwayState state = HighwayState.getInstance();
        int width = HighwayBuilder.getWidth();
        int leftBound = 0, rightBound;
        int height = 0;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.PAVE) height = 3;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.DIG) height = 4;
        if (width % 2 == 0) leftBound = width / 2;
        rightBound = leftBound - 1;
        if (width % 2 != 0) leftBound = rightBound = (width - 1) / 2;
        assert MeteorClient.mc.player != null;
        ensureCenter(state);
        int cx = state.getCenterX(), cy = state.getCenterY(), cz = state.getCenterZ();
        WorldUtils.Direction8 direction = HighwayBuilder.getDirection();
        return switch (direction) {
            case NORTH_WEST -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = -8; i <= -3; i++)
                    for (int j = 0; j <= height; j++) {
                        for (int left = leftBound; left > 0; left--) positions.add(new BlockPos(cx + i, cy + j, cz + left + i));
                        if (j > 0) {
                            positions.add(new BlockPos(cx + i, cy + j, cz + leftBound + 1 + i));
                            positions.add(new BlockPos(cx + i, cy + j, cz + leftBound + 2 + i));
                        }
                        for (int right = rightBound; right >= 0; right--) positions.add(new BlockPos(cx + right + i, cy + j, cz + i));
                        if (j > 0) {
                            positions.add(new BlockPos(cx + rightBound + 1 + i, cy + j, cz + i));
                            positions.add(new BlockPos(cx + rightBound + 2 + i, cy + j, cz + i));
                        }
                    }
                yield positions.toArray(new BlockPos[0]);
            }
            case NORTH_EAST -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = -8; i <= -3; i++)
                    for (int j = 0; j <= height; j++) {
                        for (int left = leftBound; left > 0; left--) positions.add(new BlockPos(cx - i, cy + j, cz + left + i));
                        if (j > 0) {
                            positions.add(new BlockPos(cx - i, cy + j, cz + leftBound + 1 + i));
                            positions.add(new BlockPos(cx - i, cy + j, cz + leftBound + 2 + i));
                        }
                        for (int right = rightBound; right >= 0; right--) positions.add(new BlockPos(cx - right - i, cy + j, cz + i));
                        if (j > 0) {
                            positions.add(new BlockPos(cx - rightBound - 1 - i, cy + j, cz + i));
                            positions.add(new BlockPos(cx - rightBound - 2 - i, cy + j, cz + i));
                        }
                    }
                yield positions.toArray(new BlockPos[0]);
            }
            case SOUTH_EAST -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = -8; i <= -3; i++)
                    for (int j = 0; j <= height; j++) {
                        for (int left = leftBound; left > 0; left--) positions.add(new BlockPos(cx - i, cy + j, cz - left - i));
                        if (j > 0) {
                            positions.add(new BlockPos(cx - i, cy + j, cz - leftBound - 1 - i));
                            positions.add(new BlockPos(cx - i, cy + j, cz - leftBound - 2 - i));
                        }
                        for (int right = rightBound; right >= 0; right--) positions.add(new BlockPos(cx - right - i, cy + j, cz - i));
                        if (j > 0) {
                            positions.add(new BlockPos(cx - rightBound - 1 - i, cy + j, cz - i));
                            positions.add(new BlockPos(cx - rightBound - 2 - i, cy + j, cz - i));
                        }
                    }
                yield positions.toArray(new BlockPos[0]);
            }
            case SOUTH_WEST -> {
                List<BlockPos> positions = new ArrayList<>();
                for (int i = -8; i <= -3; i++)
                    for (int j = 0; j <= height; j++) {
                        for (int left = leftBound; left > 0; left--) positions.add(new BlockPos(cx + i, cy + j, cz - left - i));
                        if (j > 0) {
                            positions.add(new BlockPos(cx + i, cy + j, cz - leftBound - 1 - i));
                            positions.add(new BlockPos(cx + i, cy + j, cz - leftBound - 2 - i));
                        }
                        for (int right = rightBound; right >= 0; right--) positions.add(new BlockPos(cx + right + i, cy + j, cz - i));
                        if (j > 0) {
                            positions.add(new BlockPos(cx + rightBound + 1 + i, cy + j, cz - i));
                            positions.add(new BlockPos(cx + rightBound + 2 + i, cy + j, cz - i));
                        }
                    }
                yield positions.toArray(new BlockPos[0]);
            }
            default -> new BlockPos[0];
        };
    }

    /** Lazily initialises the highway centre (X/Y/Z) to the player's current position. */
    private static void ensureCenter(HighwayState state) {
        if (state.getCenterX() == null) state.setCenterX(MeteorClient.mc.player.getBlockX());
        if (state.getCenterY() == null) state.setCenterY(MeteorClient.mc.player.getBlockY());
        if (state.getCenterZ() == null) state.setCenterZ(MeteorClient.mc.player.getBlockZ());
    }
}
