// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.J0sjSCk; inner types were J0sjSCk$FDb5 / J0sjSCk$aY0a71o)
package musheor.utils.internal;

import musheor.modules.automation.HighwayBuilder;
import musheor.utils.WorldUtils;
import musheor.utils.system.MusheorSystem;
import net.minecraft.util.math.BlockPos;

/**
 * Locates the nearest named highway "checkpoint" (axis line, ring, diamond, or 5k
 * grid line) for the player's position and travel direction, and tests whether a
 * given rail position lies on any known highway. Pure 2b2t highway-network geometry.
 */
public class HighwayLocator {
    /** Cardinal ring radii (blocks from spawn). */ // was: FvaNWO (double[])
    private static final double[] RING_DISTANCES = {
        200.0, 500.0, 1000.0, 1500.0, 2000.0, 2500.0, 7500.0, 50000.0, 55000.0, 62500.0,
        100000.0, 125000.0, 250000.0, 500000.0, 750000.0, 1000000.0, 1250000.0,
        1568852.0, 1875000.0, 2500000.0, 3750000.0
    };
    /** Diagonal "diamond" radii. */ // was: Q90GLXQ0Pef (double[])
    private static final double[] DIAMOND_DISTANCES = {2500.0, 5000.0, 25000.0, 50000.0, 125000.0, 250000.0, 500000.0, 3750000.0};
    /** Max snap distance for diagonal detection (8 * sqrt(2)). */ // was: psJq59YIbp3Z (double)
    private static final double DIAGONAL_SNAP = 8.0 * Math.sqrt(2.0);

    /** Highway checkpoint categories. */ // was: enum aY0a71o
    public enum Category {
        AXIS,           // was: FvaNWO   — cardinal axis line at 0
        RING,           // was: Q90GLXQ0Pef — cardinal ring
        DIAGONAL,       // was: psJq59YIbp3Z — diagonal axis at 0
        DIAMOND,        // was: SOYyh5IPg26f7F — diagonal ring ("diamond")
        GRID            // was: rKbT3Ifwo — 5k grid line
    }

    /** Routes to the cardinal or diagonal locator based on the travel direction. */
    public static Checkpoint locateNearest(int px, int pz, int py, WorldUtils.Direction8 dir) { // was: FvaNWO(int,int,int,Direction8)
        return isCardinal(dir) ? locateCardinal(px, pz, py, dir) : locateDiagonal(px, pz, py, dir);
    }

    private static boolean isCardinal(WorldUtils.Direction8 dir) { // was: FvaNWO(Direction8)
        return dir == WorldUtils.Direction8.NORTH || dir == WorldUtils.Direction8.SOUTH
            || dir == WorldUtils.Direction8.EAST || dir == WorldUtils.Direction8.WEST;
    }

    /** Nearest cardinal checkpoint (axis / ring / grid) along the perpendicular coordinate. */
    private static Checkpoint locateCardinal(int px, int pz, int py, WorldUtils.Direction8 dir) { // was: Q90GLXQ0Pef(...)
        boolean travelX = dir == WorldUtils.Direction8.EAST || dir == WorldUtils.Direction8.WEST;
        int perpCoord = travelX ? pz : px;
        double bestDist = 8.0;
        String bestId = null, bestLabel = null;
        Category bestCat = null;
        double bestAxis = 0.0;

        double d = Math.abs(perpCoord);
        if (d < bestDist) {
            bestDist = d;
            bestId = travelX ? (dir == WorldUtils.Direction8.EAST ? "+X" : "-X") : (dir == WorldUtils.Direction8.SOUTH ? "+Z" : "-Z");
            bestLabel = travelX ? (dir == WorldUtils.Direction8.EAST ? "+X (East)" : "-X (West)") : (dir == WorldUtils.Direction8.SOUTH ? "+Z (South)" : "-Z (North)");
            bestCat = Category.AXIS;
            bestAxis = 0.0;
        }

        for (double dist : RING_DISTANCES) {
            for (double candidate : new double[]{dist, -dist}) {
                d = Math.abs(perpCoord - candidate);
                if (d < bestDist) {
                    int parallelCoord = travelX ? px : pz;
                    if (Math.abs(parallelCoord) <= dist + 8.0) {
                        bestDist = d;
                        bestId = "ring_" + formatK(dist);
                        bestLabel = formatDistance(dist) + " Ring";
                        bestCat = Category.RING;
                        bestAxis = candidate;
                    }
                }
            }
        }

        for (int n = 1; n * 5000 < 50000; n++) {
            int pCoord = n * 5000;
            int nCoord = -(n * 5000 + 1);
            for (double candidate : new double[]{pCoord, nCoord}) {
                d = Math.abs(perpCoord - candidate);
                if (d < bestDist) {
                    int parallelCoord = travelX ? px : pz;
                    if (Math.abs(parallelCoord) <= 50008.0) {
                        bestDist = d;
                        long cLong = (long) (n * 5000) * (candidate > 0.0 ? 1 : -1);
                        bestId = (travelX ? "grid_ns_" : "grid_ew_") + cLong;
                        bestLabel = (candidate > 0.0 ? "+" : "-") + formatDistance(n * 5000) + (travelX ? " N/S Grid" : " E/W Grid");
                        bestCat = Category.GRID;
                        bestAxis = candidate;
                    }
                }
            }
        }

        if (bestId == null) return null;

        int width = getWidth(bestCat);
        boolean isEvenWidth = width % 2 == 0;
        double alignX, alignZ;
        int startX, startZ;
        if (travelX) {
            alignX = px;
            alignZ = isEvenWidth ? bestAxis : bestAxis + 0.5;
            startX = px;
            startZ = isEvenWidth && dir == WorldUtils.Direction8.WEST ? (int) bestAxis - 1 : (int) bestAxis;
        } else {
            alignX = isEvenWidth ? bestAxis : bestAxis + 0.5;
            alignZ = pz;
            startX = isEvenWidth && dir == WorldUtils.Direction8.SOUTH ? (int) bestAxis - 1 : (int) bestAxis;
            startZ = pz;
        }
        return new Checkpoint(bestId, bestLabel, bestCat, HighwayBuilder.HighwayType.CARDINAL, dir, width, bestAxis, alignX, alignZ, startX, startZ);
    }

    /** Nearest diagonal checkpoint (diagonal axis / diamond) along the diagonal coordinate. */
    private static Checkpoint locateDiagonal(int px, int pz, int py, WorldUtils.Direction8 dir) { // was: psJq59YIbp3Z(...)
        boolean isNESW = dir == WorldUtils.Direction8.NORTH_EAST || dir == WorldUtils.Direction8.SOUTH_WEST;
        double diagVal = isNESW ? px + pz : px - pz;
        double bestDist = DIAGONAL_SNAP;
        String bestId = null, bestLabel = null;
        Category bestCat = null;
        double bestK = 0.0;

        double d = Math.abs(diagVal);
        if (d < bestDist) {
            bestDist = d;
            bestId = isNESW ? (dir == WorldUtils.Direction8.NORTH_EAST ? "NE" : "SW") : (dir == WorldUtils.Direction8.NORTH_WEST ? "NW" : "SE");
            bestLabel = isNESW
                ? (dir == WorldUtils.Direction8.NORTH_EAST ? "NE Diagonal" : "SW Diagonal")
                : (dir == WorldUtils.Direction8.NORTH_WEST ? "NW Diagonal" : "SE Diagonal");
            bestCat = Category.DIAGONAL;
            bestK = 0.0;
        }

        for (double dist : DIAMOND_DISTANCES) {
            for (double candidate : new double[]{dist, -dist}) {
                d = Math.abs(diagVal - candidate);
                if (d < bestDist) {
                    bestDist = d;
                    bestId = "diamond_" + formatK(dist);
                    bestLabel = formatDistance(dist) + " Diamond";
                    bestCat = Category.DIAMOND;
                    bestK = candidate;
                }
            }
        }

        if (bestId == null) return null;

        int width = getWidth(bestCat);
        int blockK = isNESW ? (int) bestK - 1 : (int) bestK;
        double cx, cz;
        if (isNESW) {
            cx = (px - pz + blockK) / 2.0;
            cz = blockK - cx;
        } else {
            cx = (px + pz + blockK) / 2.0;
            cz = cx - blockK;
        }
        int startX = (int) Math.round(cx);
        int startZ = isNESW ? blockK - startX : startX - blockK;
        return new Checkpoint(bestId, bestLabel, bestCat, HighwayBuilder.HighwayType.DIAGONAL, dir, width, blockK, cx + 0.5, cz + 0.5, startX, startZ);
    }

    /** Highway width for a checkpoint category (from settings; defaults if unset). */
    private static int getWidth(Category cat) { // was: FvaNWO(Category)
        MusheorSystem m = MusheorSystem.Manager;
        if (m == null) return 4;
        return switch (cat) {
            case AXIS, RING -> m.cardinalHighwayWidth.get();
            case DIAGONAL, DIAMOND -> m.diagonalHighwayWidth.get();
            case GRID -> m.gridHighwayWidth.get();
        };
    }

    /** True if a rail position lies on ANY known highway (used to validate rail placement). */
    public static boolean isRailOnAnyHighway(BlockPos railPos, Checkpoint detected) { // was: FvaNWO(BlockPos, Checkpoint)
        int rx = railPos.getX();
        int rz = railPos.getZ();
        MusheorSystem m = MusheorSystem.Manager;
        int cardinalW = m != null ? (Integer) m.cardinalHighwayWidth.get() : 4;
        int diagonalW = m != null ? (Integer) m.diagonalHighwayWidth.get() : 9;
        int gridW = m != null ? (Integer) m.gridHighwayWidth.get() : 3;
        int diagHalf = diagonalW / 2 + 1;
        boolean travelX = detected.direction == WorldUtils.Direction8.EAST || detected.direction == WorldUtils.Direction8.WEST;
        boolean travelZ = detected.direction == WorldUtils.Direction8.NORTH || detected.direction == WorldUtils.Direction8.SOUTH;
        boolean travelNESW = detected.direction == WorldUtils.Direction8.NORTH_EAST || detected.direction == WorldUtils.Direction8.SOUTH_WEST;
        boolean travelNWSE = detected.direction == WorldUtils.Direction8.NORTH_WEST || detected.direction == WorldUtils.Direction8.SOUTH_EAST;
        int cardinalHalf = cardinalW / 2;
        int gridHalf = gridW / 2;

        if (!travelZ) {
            if (isWithinWidth(rx, 0, cardinalW)) return true;
            for (double dist : RING_DISTANCES) {
                if (Math.abs(rz) <= dist + cardinalHalf) {
                    if (isWithinWidth(rx, (int) dist, cardinalW)) return true;
                    if (isWithinWidth(rx, -((int) dist), cardinalW)) return true;
                }
            }
            if (Math.abs(rz) <= 50000 + gridHalf) {
                for (int n = 1; n * 5000 < 50000; n++) {
                    if (isWithinWidth(rx, n * 5000, gridW)) return true;
                    if (isWithinWidth(rx, -(n * 5000 + 1), gridW)) return true;
                }
            }
        }
        if (!travelX) {
            if (isWithinWidth(rz, 0, cardinalW)) return true;
            for (double dist : RING_DISTANCES) {
                if (Math.abs(rx) <= dist + cardinalHalf) {
                    if (isWithinWidth(rz, (int) dist, cardinalW)) return true;
                    if (isWithinWidth(rz, -((int) dist), cardinalW)) return true;
                }
            }
            if (Math.abs(rx) <= 50000 + gridHalf) {
                for (int n = 1; n * 5000 < 50000; n++) {
                    if (isWithinWidth(rz, n * 5000, gridW)) return true;
                    if (isWithinWidth(rz, -(n * 5000 + 1), gridW)) return true;
                }
            }
        }
        if (!travelNESW) {
            int nesw = rx + rz;
            if (Math.abs(nesw + 1) <= diagHalf) return true;
            for (double dist : DIAMOND_DISTANCES) {
                if (Math.abs(nesw - ((int) dist - 1)) <= diagHalf && rx >= 0 && rz >= 0) return true;
                if (Math.abs(nesw + (int) dist + 1) <= diagHalf && rx <= 0 && rz <= 0) return true;
            }
        }
        if (!travelNWSE) {
            int nwse = rx - rz;
            if (Math.abs(nwse) <= diagHalf) return true;
            for (double dist : DIAMOND_DISTANCES) {
                if (Math.abs(nwse - (int) dist) <= diagHalf && rx >= 0 && rz <= 0) return true;
                if (Math.abs(nwse + (int) dist) <= diagHalf && rx <= 0 && rz >= 0) return true;
            }
        }
        return false;
    }

    /** True if {@code pos} lies within the detected highway's width band. */
    public static boolean isOnDetectedHighway(BlockPos pos, Checkpoint detected) { // was: Q90GLXQ0Pef(BlockPos, Checkpoint)
        if (detected.type == HighwayBuilder.HighwayType.CARDINAL) {
            boolean travelX = detected.direction == WorldUtils.Direction8.EAST || detected.direction == WorldUtils.Direction8.WEST;
            int perpCoord = travelX ? pos.getZ() : pos.getX();
            return isWithinWidth(perpCoord, (int) detected.axisValue, detected.width);
        }
        boolean isNESW = detected.direction == WorldUtils.Direction8.NORTH_EAST || detected.direction == WorldUtils.Direction8.SOUTH_WEST;
        int K = (int) detected.axisValue;
        int u = isNESW ? pos.getX() + pos.getZ() - K : pos.getX() - pos.getZ() - K;
        int halfWidth = (detected.width - 1) / 2;
        return Math.abs(u) <= halfWidth;
    }

    /** True if {@code coord} is within a band of {@code width} centred on {@code center}. */
    private static boolean isWithinWidth(int coord, int center, int width) { // was: FvaNWO(int,int,int)
        int halfLeft = width / 2;
        int halfRight = width % 2 == 0 ? halfLeft - 1 : halfLeft;
        return coord >= center - halfLeft && coord <= center + halfRight;
    }

    /** Abbreviates a distance: 1_000_000 → "1M", 2_500 → "2.5k". */
    public static String formatDistance(double d) { // was: FvaNWO(double)
        if (d >= 1_000_000.0) return stripTrailingZeros(String.format("%.3f", d / 1_000_000.0)) + "M";
        return d >= 1_000.0 ? stripTrailingZeros(String.format("%.3f", d / 1_000.0)) + "k" : String.valueOf((long) d);
    }

    private static String stripTrailingZeros(String s) { // was: FvaNWO(String)
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static String formatK(double d) { // was: Q90GLXQ0Pef(double)
        long l = (long) d;
        return l == d ? String.valueOf(l) : String.valueOf(d);
    }

    /** An identified highway checkpoint (immutable). */ // was: inner class FDb5
    public static class Checkpoint {
        public final String id;                          // was: FvaNWO
        public final String label;                       // was: Q90GLXQ0Pef
        public final Category category;                  // was: psJq59YIbp3Z
        public final HighwayBuilder.HighwayType type;    // was: SOYyh5IPg26f7F
        public final WorldUtils.Direction8 direction;    // was: rKbT3Ifwo
        public final int width;                          // was: r7hOYIKN2
        public final double axisValue;                   // was: oZHMlTL
        public final double alignX;                      // was: xQr5FhbwpQPWgIQ
        public final double alignZ;                      // was: OMMZL1F3q
        public final int startX;                         // was: zu3a44xDeMFMCRwm
        public final int startZ;                         // was: krxNb5lcQuWA

        public Checkpoint(String id, String label, Category category, HighwayBuilder.HighwayType type,
                          WorldUtils.Direction8 direction, int width, double axisValue,
                          double alignX, double alignZ, int startX, int startZ) {
            this.id = id;
            this.label = label;
            this.category = category;
            this.type = type;
            this.direction = direction;
            this.width = width;
            this.axisValue = axisValue;
            this.alignX = alignX;
            this.alignZ = alignZ;
            this.startX = startX;
            this.startZ = startZ;
        }
    }
}
