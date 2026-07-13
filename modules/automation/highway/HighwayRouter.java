// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation.highway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import musheor.utils.WorldUtils.Coord2D;

/**
 * Plans a {@link Route} of {@link Leg}s along the {@link HighwayNetwork} from the player's
 * position to a goal. Tries, in order: same highway (single leg), a shared intersection
 * (two legs), then Dijkstra over the intersection graph. Collinear same-highway legs are
 * merged.
 */
public class HighwayRouter {
    /** Builds a route from (px, pz) to (gx, gz), or null if none can be found. */
    public static Route route(double px, double pz, double gx, double gz) { // was: FvaNWO(double,double,double,double)
        Coord2D player = new Coord2D(px, pz);
        HighwayNetwork.Highway playerHighway = nearestHighway(px, pz);
        if (playerHighway == null) return null;

        SnapResult goalSnap = snapToNearest(gx, gz);
        if (goalSnap == null) return null;

        Coord2D goalPos = goalSnap.point;
        HighwayNetwork.Highway goalHighway = goalSnap.highway;
        if (playerHighway.equals(goalHighway)) {
            return new Route(List.of(new Leg(player, goalPos, playerHighway, HighwayNetwork.distance(player, goalPos))));
        }

        Coord2D intersection = findConnectingPoint(playerHighway, goalHighway, player, goalPos);
        if (intersection != null) {
            Leg leg1 = new Leg(player, intersection, playerHighway, HighwayNetwork.distance(player, intersection));
            Leg leg2 = new Leg(intersection, goalPos, goalHighway, HighwayNetwork.distance(intersection, goalPos));
            return new Route(List.of(leg1, leg2));
        }
        return dijkstraRoute(player, playerHighway, goalPos, goalHighway);
    }

    /** The highway whose closest point is nearest to (x, z), within 150 blocks, or null. */
    private static HighwayNetwork.Highway nearestHighway(double x, double z) { // was: FvaNWO(double,double)
        HighwayNetwork.Highway best = null;
        double bestDist = Double.MAX_VALUE;
        Coord2D pos = new Coord2D(x, z);
        for (HighwayNetwork.Highway hw : HighwayNetwork.HIGHWAYS) {
            double d = HighwayNetwork.distance(hw.closestPoint(x, z), pos);
            if (d < bestDist) {
                bestDist = d;
                best = hw;
            }
        }
        return bestDist <= 150.0 ? best : null;
    }

    /** Snaps (x, z) to the nearest point on any highway. */
    private static SnapResult snapToNearest(double x, double z) { // was: Q90GLXQ0Pef(double,double)
        HighwayNetwork.Highway best = null;
        Coord2D bestPoint = null;
        double bestDist = Double.MAX_VALUE;
        Coord2D pos = new Coord2D(x, z);
        for (HighwayNetwork.Highway hw : HighwayNetwork.HIGHWAYS) {
            Coord2D nearest = hw.closestPoint(x, z);
            double d = HighwayNetwork.distance(nearest, pos);
            if (d < bestDist) {
                bestDist = d;
                bestPoint = nearest;
                best = hw;
            }
        }
        return best != null ? new SnapResult(bestPoint, best) : null;
    }

    /** The lowest-cost point shared by two highways (direct crossing or a common node), if the detour is reasonable. */
    private static Coord2D findConnectingPoint(HighwayNetwork.Highway hwA, HighwayNetwork.Highway hwB, Coord2D player, Coord2D goal) { // was: FvaNWO(Highway,Highway,Coord2D,Coord2D)
        List<Coord2D> candidates = new ArrayList<>();
        Coord2D direct = HighwayNetwork.intersect(hwA, hwB);
        if (direct != null && hwA.contains(direct.x(), direct.z(), 1.0) && hwB.contains(direct.x(), direct.z(), 1.0)) {
            candidates.add(direct);
        }

        for (HighwayNetwork.Intersection node : HighwayNetwork.INTERSECTIONS) {
            if (hwA.contains(node.pos().x(), node.pos().z(), 1.0) && hwB.contains(node.pos().x(), node.pos().z(), 1.0)) {
                candidates.add(node.pos());
            }
        }

        if (candidates.isEmpty()) return null;

        Coord2D best = null;
        double bestCost = Double.MAX_VALUE;
        for (Coord2D candidate : candidates) {
            double cost = HighwayNetwork.distance(player, candidate) + HighwayNetwork.distance(candidate, goal);
            double directDist = HighwayNetwork.distance(player, goal);
            if (!(cost > directDist * 1.5) && cost < bestCost) {
                bestCost = cost;
                best = candidate;
            }
        }
        return best;
    }

    /** Dijkstra over the intersection graph, with entry/exit legs from the player/goal. */
    private static Route dijkstraRoute(Coord2D player, HighwayNetwork.Highway playerHighway, Coord2D goal, HighwayNetwork.Highway goalHighway) { // was: FvaNWO(Coord2D,Highway,Coord2D,Highway)
        int startNode = nearestNodeByCost(playerHighway, player, goal);
        int goalNode = nearestNodeByDistance(goalHighway, goal);
        if (startNode < 0 || goalNode < 0) return null;
        if (startNode == goalNode) return Route.empty();

        Map<Integer, List<HighwayNetwork.Edge>> adj = HighwayNetwork.buildAdjacency();
        int n = HighwayNetwork.INTERSECTIONS.size();
        double[] dist = new double[n];
        int[] prev = new int[n];
        HighwayNetwork.Edge[] prevEdge = new HighwayNetwork.Edge[n];
        boolean[] visited = new boolean[n];
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[startNode] = 0.0;
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> Double.longBitsToDouble(a[0])));
        pq.offer(new long[]{Double.doubleToLongBits(0.0), startNode});

        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            int curr = (int) top[1];
            double d = Double.longBitsToDouble(top[0]);
            if (visited[curr]) continue;
            visited[curr] = true;
            if (curr == goalNode) break;

            for (HighwayNetwork.Edge edge : adj.getOrDefault(curr, List.of())) {
                double nd = d + edge.distance();
                if (nd < dist[edge.to()]) {
                    dist[edge.to()] = nd;
                    prev[edge.to()] = curr;
                    prevEdge[edge.to()] = edge;
                    pq.offer(new long[]{Double.doubleToLongBits(nd), edge.to()});
                }
            }
        }

        if (dist[goalNode] == Double.MAX_VALUE) return null;

        List<Leg> legs = new ArrayList<>();
        int cur = goalNode;
        while (prev[cur] != -1) {
            int from = prev[cur];
            HighwayNetwork.Edge edge = prevEdge[cur];
            legs.add(new Leg(HighwayNetwork.INTERSECTIONS.get(from).pos(), HighwayNetwork.INTERSECTIONS.get(cur).pos(), edge.highway(), edge.distance()));
            cur = from;
        }
        Collections.reverse(legs);

        Coord2D startPos = HighwayNetwork.INTERSECTIONS.get(startNode).pos();
        if (HighwayNetwork.distance(player, startPos) > 10.0) {
            legs.add(0, new Leg(player, startPos, playerHighway, HighwayNetwork.distance(player, startPos)));
        }

        Coord2D lastPos = legs.isEmpty() ? player : legs.get(legs.size() - 1).to();
        if (HighwayNetwork.distance(lastPos, goal) > 10.0) {
            legs.add(new Leg(lastPos, goal, goalHighway, HighwayNetwork.distance(lastPos, goal)));
        }

        return new Route(legs);
    }

    /** The on-highway node minimizing (distance from + distance to goal). */
    private static int nearestNodeByCost(HighwayNetwork.Highway hw, Coord2D from, Coord2D goal) { // was: FvaNWO(Highway,Coord2D,Coord2D)
        List<Integer> candidates = HighwayNetwork.intersectionsOn(hw);
        int best = -1;
        double bestCost = Double.MAX_VALUE;
        for (int idx : candidates) {
            Coord2D p = HighwayNetwork.INTERSECTIONS.get(idx).pos();
            double cost = HighwayNetwork.distance(from, p) + HighwayNetwork.distance(p, goal);
            if (cost < bestCost) {
                bestCost = cost;
                best = idx;
            }
        }
        return best;
    }

    /** The on-highway node closest to {@code target}. */
    private static int nearestNodeByDistance(HighwayNetwork.Highway hw, Coord2D target) { // was: FvaNWO(Highway,Coord2D)
        List<Integer> candidates = HighwayNetwork.intersectionsOn(hw);
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int idx : candidates) {
            double d = HighwayNetwork.distance(target, HighwayNetwork.INTERSECTIONS.get(idx).pos());
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }

    /** One straight segment of a route, along a single highway. */
    public record Leg(Coord2D from, Coord2D to, HighwayNetwork.Highway highway, double distance) { } // was: FvaNWO/Q90GLXQ0Pef/psJq59YIbp3Z/SOYyh5IPg26f7F

    /** A planned route: its legs, total distance, and the ordered waypoint list. */
    public static class Route {
        public final List<Leg> legs;            // was: FvaNWO
        public final double totalDistance;      // was: Q90GLXQ0Pef
        public final List<Coord2D> waypoints;   // was: psJq59YIbp3Z

        private Route(List<Leg> legs) {
            this.legs = Collections.unmodifiableList(mergeCollinear(legs));
            this.totalDistance = this.legs.stream().mapToDouble(Leg::distance).sum();
            List<Coord2D> wp = new ArrayList<>();
            if (!this.legs.isEmpty()) wp.add(this.legs.get(0).from());
            for (Leg leg : this.legs) wp.add(leg.to());
            this.waypoints = Collections.unmodifiableList(wp);
        }

        public static Route empty() { return new Route(List.of()); } // was: FvaNWO()

        /** Merges consecutive legs that share a highway and continue in the same direction. */
        private static List<Leg> mergeCollinear(List<Leg> legs) { // was: FvaNWO(List)
            if (legs.size() < 2) return new ArrayList<>(legs);
            List<Leg> out = new ArrayList<>();
            Leg cur = legs.get(0);
            for (int i = 1; i < legs.size(); i++) {
                Leg next = legs.get(i);
                if (cur.highway().equals(next.highway()) && sameDirection(cur, next)) {
                    cur = new Leg(cur.from(), next.to(), cur.highway(), cur.distance() + next.distance());
                } else {
                    out.add(cur);
                    cur = next;
                }
            }
            out.add(cur);
            return out;
        }

        /** True if the two legs' displacement vectors point the same way (positive dot product). */
        private static boolean sameDirection(Leg a, Leg b) { // was: FvaNWO(Leg,Leg)
            double ax = a.to().x() - a.from().x();
            double az = a.to().z() - a.from().z();
            double bx = b.to().x() - b.from().x();
            double bz = b.to().z() - b.from().z();
            return ax * bx + az * bz > 0.0;
        }
    }

    /** The nearest point on a highway and that highway. */
    private record SnapResult(Coord2D point, HighwayNetwork.Highway highway) { } // was: FvaNWO/Q90GLXQ0Pef
}
