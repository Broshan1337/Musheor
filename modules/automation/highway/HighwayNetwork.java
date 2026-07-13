// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation.highway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import musheor.utils.WorldUtils.Coord2D;
import musheor.utils.system.HighwayNetworkManager;

/**
 * A static model of the 2b2t highway network (axes, ring roads, diagonals, diamonds and
 * the 5k grid) as a graph of {@link Highway} lines, their pairwise {@link Intersection}s,
 * and the {@link Edge}s between adjacent intersections. Ring/diamond radii are loaded
 * from {@link HighwayNetworkManager}. {@link HighwayRouter} runs Dijkstra over this graph.
 */
public class HighwayNetwork {
    public static final List<Highway> HIGHWAYS = buildHighways();                 // was: FvaNWO
    public static final List<Intersection> INTERSECTIONS = computeIntersections(HIGHWAYS); // was: Q90GLXQ0Pef
    public static final List<Edge> EDGES = computeEdges(HIGHWAYS, INTERSECTIONS);  // was: psJq59YIbp3Z

    private static double[] ringDistances() { // was: Q90GLXQ0Pef()
        return HighwayNetworkManager.getInstance().getRingDistances("2b2t");
    }

    private static double[] diamondDistances() { // was: psJq59YIbp3Z()
        return HighwayNetworkManager.getInstance().getDiamondDistances("2b2t");
    }

    private static List<Highway> buildHighways() { // was: SOYyh5IPg26f7F()
        List<Highway> h = new ArrayList<>();
        h.add(Highway.axisX());
        h.add(Highway.axisZ());
        h.add(Highway.diagonalXEqualsZ());
        h.add(Highway.diagonalXEqualsNegZ());

        for (double d : ringDistances()) {
            h.add(Highway.ringZ(d));
            h.add(Highway.ringZ(-d));
            h.add(Highway.ringX(d));
            h.add(Highway.ringX(-d));
        }

        for (double d : diamondDistances()) {
            h.add(Highway.diamond(true, d));
            h.add(Highway.diamond(true, -d));
            h.add(Highway.diamond(false, d));
            h.add(Highway.diamond(false, -d));
        }

        for (int i = 1; i <= 10; i++) {
            double coord = i * 5000.0 + 0.5;
            h.add(Highway.gridZ(coord));
            h.add(Highway.gridZ(-coord));
            h.add(Highway.gridX(coord));
            h.add(Highway.gridX(-coord));
        }

        return Collections.unmodifiableList(h);
    }

    private static List<Intersection> computeIntersections(List<Highway> highways) { // was: FvaNWO(List)
        Map<String, List<Highway>> byPos = new LinkedHashMap<>();
        for (int i = 0; i < highways.size(); i++) {
            for (int j = i + 1; j < highways.size(); j++) {
                Highway a = highways.get(i);
                Highway b = highways.get(j);
                Coord2D pt = intersect(a, b);
                if (pt != null && a.contains(pt.x(), pt.z(), 1.0) && b.contains(pt.x(), pt.z(), 1.0)) {
                    String key = posKey(pt);
                    byPos.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
                    byPos.get(key).add(b);
                }
            }
        }

        List<Intersection> nodes = new ArrayList<>();
        for (Map.Entry<String, List<Highway>> entry : byPos.entrySet()) {
            Coord2D pos = parsePos(entry.getKey());
            List<Highway> unique = entry.getValue().stream().distinct().toList();
            nodes.add(new Intersection(pos, unique));
        }
        return Collections.unmodifiableList(nodes);
    }

    private static List<Edge> computeEdges(List<Highway> highways, List<Intersection> nodes) { // was: FvaNWO(List,List)
        List<Edge> edges = new ArrayList<>();
        for (Highway hw : highways) {
            List<Integer> onHw = new ArrayList<>();
            for (int i = 0; i < nodes.size(); i++) {
                Coord2D p = nodes.get(i).pos();
                if (hw.contains(p.x(), p.z(), 1.0)) onHw.add(i);
            }

            onHw.sort((a, b) -> {
                Coord2D pa = nodes.get(a).pos();
                Coord2D pb = nodes.get(b).pos();
                return Double.compare(projectParam(hw, pa), projectParam(hw, pb));
            });

            for (int k = 0; k < onHw.size() - 1; k++) {
                int from = onHw.get(k);
                int to = onHw.get(k + 1);
                double dist = distance(nodes.get(from).pos(), nodes.get(to).pos());
                edges.add(new Edge(from, to, dist, hw));
                edges.add(new Edge(to, from, dist, hw));
            }
        }
        return Collections.unmodifiableList(edges);
    }

    /** The intersection point of two highway lines, or null if parallel. */
    static Coord2D intersect(Highway a, Highway b) { // was: FvaNWO(Highway,Highway)
        double[] la = lineCoefficients(a);
        double[] lb = lineCoefficients(b);
        if (la == null || lb == null) return null;
        double det = la[0] * lb[1] - lb[0] * la[1];
        if (Math.abs(det) < 1.0E-9) return null;
        double x = (la[2] * lb[1] - lb[2] * la[1]) / det;
        double z = (la[0] * lb[2] - lb[0] * la[2]) / det;
        return new Coord2D(x, z);
    }

    /** Coefficients {a, b, c} of the line a*x + b*z = c for a highway. */
    private static double[] lineCoefficients(Highway hw) { // was: Q90GLXQ0Pef(Highway)
        return switch (hw.type()) {
            case AXIS, RING, GRID -> hw.isHorizontal() ? new double[]{0.0, 1.0, hw.constCoord()} : new double[]{1.0, 0.0, hw.constCoord()};
            case DIAGONAL -> hw.isHorizontal() ? new double[]{1.0, 1.0, hw.constCoord()} : new double[]{1.0, -1.0, hw.constCoord()};
            case DIAMOND -> hw.isHorizontal() ? new double[]{1.0, 1.0, hw.constCoord()} : new double[]{1.0, -1.0, hw.constCoord()};
        };
    }

    /** A monotone parameter for a point's position along a highway (used to order nodes). */
    private static double projectParam(Highway hw, Coord2D p) { // was: FvaNWO(Highway,Coord2D)
        return hw.isDiagonalOriented() ? p.x() : (hw.isHorizontal() ? p.x() : p.z());
    }

    /** Euclidean distance between two 2D points. */
    public static double distance(Coord2D a, Coord2D b) { // was: FvaNWO(Coord2D,Coord2D)
        double dx = a.x() - b.x();
        double dz = a.z() - b.z();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static String posKey(Coord2D p) { // was: FvaNWO(Coord2D)
        return Math.round(p.x()) + "," + Math.round(p.z());
    }

    private static Coord2D parsePos(String key) { // was: FvaNWO(String)
        String[] parts = key.split(",");
        return new Coord2D(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    /** Indices of all intersections that lie on the given highway. */
    public static List<Integer> intersectionsOn(Highway hw) { // was: FvaNWO(Highway)
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < INTERSECTIONS.size(); i++) {
            Coord2D p = INTERSECTIONS.get(i).pos();
            if (hw.contains(p.x(), p.z(), 1.0)) result.add(i);
        }
        return result;
    }

    /** Adjacency list of node index -> outgoing edges. */
    public static Map<Integer, List<Edge>> buildAdjacency() { // was: FvaNWO()
        Map<Integer, List<Edge>> adj = new HashMap<>();
        for (int i = 0; i < INTERSECTIONS.size(); i++) adj.put(i, new ArrayList<>());
        for (Edge e : EDGES) adj.get(e.from()).add(e);
        return adj;
    }

    /** A directed edge between two intersection indices along a highway. */
    public record Edge(int from, int to, double distance, Highway highway) { } // was: record with FvaNWO/Q90GLXQ0Pef/psJq59YIbp3Z/SOYyh5IPg26f7F

    /** Classification of a highway line. */ // was: enum HighwayType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z, SOYyh5IPg26f7F, rKbT3Ifwo}
    public enum HighwayType { AXIS, DIAGONAL, RING, DIAMOND, GRID }

    /** An intersection point and the highways passing through it. */
    public record Intersection(Coord2D pos, List<Highway> highways) { } // was: FvaNWO(pos)/Q90GLXQ0Pef(highways)

    /**
     * A single highway line. {@code isHorizontal} selects orientation; for diagonals/diamonds
     * it selects the X+Z vs X-Z family. {@code constCoord} is the fixed value of the line and
     * {@code minCoord}/{@code maxCoord} bound its extent.
     */
    public record Highway(String name, HighwayType type, boolean isHorizontal, double constCoord, double minCoord, double maxCoord) {
        static Highway axisX() { return new Highway("+X/-X axis (Z=0)", HighwayType.AXIS, true, 0.0, -Double.MAX_VALUE, Double.MAX_VALUE); }        // was: FvaNWO()
        static Highway axisZ() { return new Highway("+Z/-Z axis (X=0)", HighwayType.AXIS, false, 0.0, -Double.MAX_VALUE, Double.MAX_VALUE); }        // was: Q90GLXQ0Pef()
        static Highway diagonalXEqualsZ() { return new Highway("diagonal (X=Z)", HighwayType.DIAGONAL, false, 0.0, -Double.MAX_VALUE, Double.MAX_VALUE); }   // was: psJq59YIbp3Z()
        static Highway diagonalXEqualsNegZ() { return new Highway("diagonal (X=-Z)", HighwayType.DIAGONAL, true, 0.0, -Double.MAX_VALUE, Double.MAX_VALUE); } // was: SOYyh5IPg26f7F()

        static Highway ringZ(double coord) { // was: FvaNWO(double)
            double limit = Math.abs(coord);
            return new Highway(String.format("ring road Z=%.0f", coord), HighwayType.RING, true, coord, -limit, limit);
        }

        static Highway ringX(double coord) { // was: Q90GLXQ0Pef(double)
            double limit = Math.abs(coord);
            return new Highway(String.format("ring road X=%.0f", coord), HighwayType.RING, false, coord, -limit, limit);
        }

        static Highway diamond(boolean isSum, double K) { // was: FvaNWO(boolean,double)
            double minX = Math.min(0.0, K);
            double maxX = Math.max(0.0, K);
            String eq = isSum ? String.format("diamond X+Z=%.0f", K) : String.format("diamond X-Z=%.0f", K);
            return new Highway(eq, HighwayType.DIAMOND, isSum, K, minX, maxX);
        }

        static Highway gridZ(double coord) { // was: psJq59YIbp3Z(double)
            return new Highway(String.format("grid Z=%.1f", coord), HighwayType.GRID, true, coord, -50000.5, 50000.5);
        }

        static Highway gridX(double coord) { // was: SOYyh5IPg26f7F(double)
            return new Highway(String.format("grid X=%.1f", coord), HighwayType.GRID, false, coord, -50000.5, 50000.5);
        }

        /** True for diagonal/diamond lines (which run at 45°). */
        boolean isDiagonalOriented() { // was: rKbT3Ifwo()
            return this.type == HighwayType.DIAGONAL || this.type == HighwayType.DIAMOND;
        }

        /** True if (x, z) lies on this highway within {@code tol}. */
        boolean contains(double x, double z, double tol) { // was: FvaNWO(double,double,double)
            return switch (this.type) {
                case AXIS, RING, GRID -> this.isHorizontal
                    ? Math.abs(z - this.constCoord) <= tol && x >= this.minCoord - tol && x <= this.maxCoord + tol
                    : Math.abs(x - this.constCoord) <= tol && z >= this.minCoord - tol && z <= this.maxCoord + tol;
                case DIAGONAL -> {
                    double val = this.isHorizontal ? x + z : x - z;
                    yield Math.abs(val - this.constCoord) <= tol;
                }
                case DIAMOND -> {
                    double val = this.isHorizontal ? x + z : x - z;
                    yield Math.abs(val - this.constCoord) <= tol && x >= this.minCoord - tol && x <= this.maxCoord + tol;
                }
            };
        }

        /** Clamps a coordinate to this highway's extent. */
        double clamp(double coord) { // was: rKbT3Ifwo(double)
            return Math.max(this.minCoord, Math.min(this.maxCoord, coord));
        }

        /** The point on this highway closest to (x, z). */
        Coord2D closestPoint(double x, double z) { // was: FvaNWO(double,double)
            return switch (this.type) {
                case AXIS, RING, GRID -> {
                    if (this.isHorizontal) {
                        yield new Coord2D(this.clamp(x), this.constCoord);
                    } else {
                        yield new Coord2D(this.constCoord, this.clamp(z));
                    }
                }
                case DIAGONAL -> {
                    if (this.isHorizontal) {
                        double px = (x - z) / 2.0;
                        yield new Coord2D(px, -px);
                    } else {
                        double px = (x + z) / 2.0;
                        yield new Coord2D(px, px);
                    }
                }
                case DIAMOND -> {
                    boolean isSum = this.isHorizontal;
                    double cx = isSum ? (x - z + this.constCoord) / 2.0 : (x + z + this.constCoord) / 2.0;
                    cx = this.clamp(cx);
                    double cz = isSum ? this.constCoord - cx : cx - this.constCoord;
                    yield new Coord2D(cx, cz);
                }
            };
        }
    }
}
