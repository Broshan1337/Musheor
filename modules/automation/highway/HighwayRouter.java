// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation.highway;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import musheor.modules.automation.highway.HighwayNetwork;
import musheor.utils.WorldUtils;

public class HighwayRouter {
    public static Route findRoute(double d, double d2, double d3, double d4) {
        WorldUtils.Vec2d vec2d = new WorldUtils.Vec2d(d, d2);
        HighwayNetwork.Highway highway = HighwayRouter.findNearestHighway(d, d2);
        if (highway == null) {
            return null;
        }
        SnapResult snapResult = HighwayRouter.findSnapResult(d3, d4);
        if (snapResult == null) {
            return null;
        }
        WorldUtils.Vec2d vec2d2 = snapResult.point;
        HighwayNetwork.Highway highway2 = snapResult.highway;
        if (highway.equals(highway2)) {
            return new Route(List.of(new Leg(vec2d, vec2d2, highway, HighwayNetwork.distance(vec2d, vec2d2))));
        }
        WorldUtils.Vec2d vec2d3 = HighwayRouter.findRoute(highway, highway2, vec2d, vec2d2);
        if (vec2d3 != null) {
            Leg leg = new Leg(vec2d, vec2d3, highway, HighwayNetwork.distance(vec2d, vec2d3));
            Leg leg2 = new Leg(vec2d3, vec2d2, highway2, HighwayNetwork.distance(vec2d3, vec2d2));
            return new Route(List.of(leg, leg2));
        }
        return HighwayRouter.findRoute(vec2d, highway, vec2d2, highway2);
    }

    private static HighwayNetwork.Highway findNearestHighway(double d, double d2) {
        HighwayNetwork.Highway highway = null;
        double d3 = Double.MAX_VALUE;
        WorldUtils.Vec2d vec2d = new WorldUtils.Vec2d(d, d2);
        for (HighwayNetwork.Highway highway2 : HighwayNetwork.HIGHWAYS) {
            double d4 = HighwayNetwork.distance(highway2.nearestPoint(d, d2), vec2d);
            if (!(d4 < d3)) continue;
            d3 = d4;
            highway = highway2;
        }
        return d3 <= 150.0 ? highway : null;
    }

    private static SnapResult findSnapResult(double d, double d2) {
        HighwayNetwork.Highway highway = null;
        WorldUtils.Vec2d vec2d = null;
        double d3 = Double.MAX_VALUE;
        WorldUtils.Vec2d vec2d2 = new WorldUtils.Vec2d(d, d2);
        for (HighwayNetwork.Highway highway2 : HighwayNetwork.HIGHWAYS) {
            WorldUtils.Vec2d vec2d3 = highway2.nearestPoint(d, d2);
            double d4 = HighwayNetwork.distance(vec2d3, vec2d2);
            if (!(d4 < d3)) continue;
            d3 = d4;
            vec2d = vec2d3;
            highway = highway2;
        }
        return highway != null ? new SnapResult(vec2d, highway) : null;
    }

    private static WorldUtils.Vec2d findDirectTransfer(HighwayNetwork.Highway highway, HighwayNetwork.Highway highway2, WorldUtils.Vec2d vec2d, WorldUtils.Vec2d vec2d2) {
        ArrayList<WorldUtils.Vec2d> arrayList = new ArrayList<WorldUtils.Vec2d>();
        WorldUtils.Vec2d vec2d3 = HighwayNetwork.findIntersectionPoint(highway, highway2);
        if (vec2d3 != null && highway.contains(vec2d3.x(), vec2d3.z(), 1.0) && highway2.contains(vec2d3.x(), vec2d3.z(), 1.0)) {
            arrayList.add(vec2d3);
        }
        for (HighwayNetwork.Intersection intersection : HighwayNetwork.INTERSECTIONS) {
            if (!highway.contains(intersection.t4IlnBm0D().x(), intersection.t4IlnBm0D().z(), 1.0) || !highway2.contains(intersection.t4IlnBm0D().x(), intersection.t4IlnBm0D().z(), 1.0)) continue;
            arrayList.add(intersection.t4IlnBm0D());
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        Object object = null;
        double d = Double.MAX_VALUE;
        for (WorldUtils.Vec2d vec2d4 : arrayList) {
            double d2;
            double d3 = HighwayNetwork.distance(vec2d, vec2d4) + HighwayNetwork.distance(vec2d4, vec2d2);
            if (d3 > (d2 = HighwayNetwork.distance(vec2d, vec2d2)) * 1.5 || !(d3 < d)) continue;
            d = d3;
            object = vec2d4;
        }
        return object;
    }

    private static Route findDijkstraRoute(WorldUtils.Vec2d vec2d, HighwayNetwork.Highway highway, WorldUtils.Vec2d vec2d2, HighwayNetwork.Highway highway2) {
        Record record;
        int n;
        Object object;
        int n2 = HighwayRouter.findRoute(highway, vec2d, vec2d2);
        int n3 = HighwayRouter.findNearestIntersectionIndex(highway2, vec2d2);
        if (n2 < 0 || n3 < 0) {
            return null;
        }
        if (n2 == n3) {
            return Route.empty();
        }
        Map<Integer, List<HighwayNetwork.Edge>> map = HighwayNetwork.buildEdgesByIntersection();
        int n4 = HighwayNetwork.INTERSECTIONS.size();
        double[] dArray = new double[n4];
        int[] nArray = new int[n4];
        HighwayNetwork.Edge[] edgeArray = new HighwayNetwork.Edge[n4];
        boolean[] blArray = new boolean[n4];
        Arrays.fill(dArray, Double.MAX_VALUE);
        Arrays.fill(nArray, -1);
        dArray[n2] = 0.0;
        PriorityQueue<long[]> priorityQueue = new PriorityQueue<long[]>(Comparator.comparingDouble(lArray -> Double.longBitsToDouble(lArray[0])));
        priorityQueue.offer(new long[]{Double.doubleToLongBits(0.0), n2});
        while (!priorityQueue.isEmpty()) {
            object = priorityQueue.poll();
            n = (int)object[1];
            double d = Double.longBitsToDouble(object[0]);
            if (blArray[n]) continue;
            blArray[n] = true;
            if (n == n3) break;
            for (HighwayNetwork.Edge edge : map.getOrDefault(n, List.of())) {
                double d2 = d + edge.distance();
                if (!(d2 < dArray[edge.oosx8z2R()])) continue;
                dArray[edge.oosx8z2R()] = d2;
                nArray[edge.oosx8z2R()] = n;
                edgeArray[edge.oosx8z2R()] = edge;
                priorityQueue.offer(new long[]{Double.doubleToLongBits(d2), edge.oosx8z2R()});
            }
        }
        if (dArray[n3] == Double.MAX_VALUE) {
            return null;
        }
        object = new ArrayList();
        n = n3;
        while (nArray[n] != -1) {
            int n5 = nArray[n];
            record = edgeArray[n];
            object.add(new Leg(HighwayNetwork.INTERSECTIONS.get(n5).t4IlnBm0D(), HighwayNetwork.INTERSECTIONS.get(n).t4IlnBm0D(), ((HighwayNetwork.Edge)record).highway(), ((HighwayNetwork.Edge)record).distance()));
            n = n5;
        }
        Collections.reverse(object);
        WorldUtils.Vec2d vec2d3 = HighwayNetwork.INTERSECTIONS.get(n2).t4IlnBm0D();
        if (HighwayNetwork.distance(vec2d, vec2d3) > 10.0) {
            object.add(0, new Leg(vec2d, vec2d3, highway, HighwayNetwork.distance(vec2d, vec2d3)));
        }
        Record record2 = record = object.isEmpty() ? vec2d : ((Leg)object.get(object.size() - 1)).to();
        if (HighwayNetwork.distance((WorldUtils.Vec2d)record, vec2d2) > 10.0) {
            object.add(new Leg((WorldUtils.Vec2d)record, vec2d2, highway2, HighwayNetwork.distance((WorldUtils.Vec2d)record, vec2d2)));
        }
        return new Route((List<Leg>)object);
    }

    private static int findBestStartIntersection(HighwayNetwork.Highway highway, WorldUtils.Vec2d vec2d, WorldUtils.Vec2d vec2d2) {
        List<Integer> list = HighwayNetwork.getIntersectionIndices(highway);
        int n = -1;
        double d = Double.MAX_VALUE;
        for (int n2 : list) {
            WorldUtils.Vec2d vec2d3 = HighwayNetwork.INTERSECTIONS.get(n2).t4IlnBm0D();
            double d2 = HighwayNetwork.distance(vec2d, vec2d3) + HighwayNetwork.distance(vec2d3, vec2d2);
            if (!(d2 < d)) continue;
            d = d2;
            n = n2;
        }
        return n;
    }

    private static int findNearestIntersectionIndex(HighwayNetwork.Highway highway, WorldUtils.Vec2d vec2d) {
        List<Integer> list = HighwayNetwork.getIntersectionIndices(highway);
        int n = -1;
        double d = Double.MAX_VALUE;
        for (int n2 : list) {
            double d2 = HighwayNetwork.distance(vec2d, HighwayNetwork.INTERSECTIONS.get(n2).t4IlnBm0D());
            if (!(d2 < d)) continue;
            d = d2;
            n = n2;
        }
        return n;
    }

    static final class SnapResult
    extends Record {
        final WorldUtils.Vec2d point;
        final HighwayNetwork.Highway highway;

        SnapResult(WorldUtils.Vec2d vec2d, HighwayNetwork.Highway highway) {
            this.point = vec2d;
            this.highway = highway;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SnapResult.class, "point;highway", "point", "highway"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SnapResult.class, "point;highway", "point", "highway"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SnapResult.class, "point;highway", "point", "highway"}, this, object);
        }
    }

    public static class Route {
        public final List<Leg> legs;
        public final double totalDistance;
        public final List<WorldUtils.Vec2d> waypoints;

        Route(List<Leg> list) {
            this.legs = Collections.unmodifiableList(Route.mergeSameHighwayLegs(list));
            this.totalDistance = this.legs.stream().mapToDouble(Leg::distance).sum();
            ArrayList<WorldUtils.Vec2d> arrayList = new ArrayList<WorldUtils.Vec2d>();
            if (!this.legs.isEmpty()) {
                arrayList.add(this.legs.get(0).from());
            }
            for (Leg leg : this.legs) {
                arrayList.add(leg.to());
            }
            this.waypoints = Collections.unmodifiableList(arrayList);
        }

        public static Route empty() {
            return new Route(List.of());
        }

        private static List<Leg> mergeSameHighwayLegs(List<Leg> list) {
            if (list.size() < 2) {
                return new ArrayList<Leg>(list);
            }
            ArrayList<Leg> arrayList = new ArrayList<Leg>();
            Leg leg = list.get(0);
            for (int i = 1; i < list.size(); ++i) {
                Leg leg2 = list.get(i);
                if (leg.highway().equals(leg2.highway()) && Route.areSameDirection(leg, leg2)) {
                    leg = new Leg(leg.from(), leg2.to(), leg.highway(), leg.distance() + leg2.distance());
                    continue;
                }
                arrayList.add(leg);
                leg = leg2;
            }
            arrayList.add(leg);
            return arrayList;
        }

        private static boolean areSameDirection(Leg leg, Leg leg2) {
            double d;
            double d2 = leg.to().x() - leg.from().x();
            double d3 = leg.to().z() - leg.from().z();
            double d4 = leg2.to().x() - leg2.from().x();
            return d2 * d4 + d3 * (d = leg2.to().z() - leg2.from().z()) > 0.0;
        }
    }

    public static final class Leg
    extends Record {
        private final WorldUtils.Vec2d from;
        private final WorldUtils.Vec2d to;
        private final HighwayNetwork.Highway highway;
        private final double distance;

        public Leg(WorldUtils.Vec2d vec2d, WorldUtils.Vec2d vec2d2, HighwayNetwork.Highway highway, double d) {
            this.from = vec2d;
            this.to = vec2d2;
            this.highway = highway;
            this.distance = d;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Leg.class, "from;to;highway;distance", "from", "to", "highway", "distance"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Leg.class, "from;to;highway;distance", "from", "to", "highway", "distance"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Leg.class, "from;to;highway;distance", "from", "to", "highway", "distance"}, this, object);
        }

        public WorldUtils.Vec2d from() {
            return this.from;
        }

        public WorldUtils.Vec2d to() {
            return this.to;
        }

        public HighwayNetwork.Highway highway() {
            return this.highway;
        }

        public double distance() {
            return this.distance;
        }
    }
}

