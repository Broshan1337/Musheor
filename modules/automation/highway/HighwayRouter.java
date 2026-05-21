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
    public static Route jOdDDFXSeWl4(double d, double d2, double d3, double d4) {
        WorldUtils.Vec2d vec2d = new WorldUtils.Vec2d(d, d2);
        HighwayNetwork.Highway highway = HighwayRouter.mp3zoXQFKUKYj5(d, d2);
        if (highway == null) {
            return null;
        }
        SnapResult snapResult = HighwayRouter.Gt56Sj4a6BWhgB(d3, d4);
        if (snapResult == null) {
            return null;
        }
        WorldUtils.Vec2d vec2d2 = snapResult.gfAIDmJ7f;
        HighwayNetwork.Highway highway2 = snapResult.kfGx5x4Y;
        if (highway.equals(highway2)) {
            return new Route(List.of(new Leg(vec2d, vec2d2, highway, HighwayNetwork.jOdDDFXSeWl4(vec2d, vec2d2))));
        }
        WorldUtils.Vec2d vec2d3 = HighwayRouter.jOdDDFXSeWl4(highway, highway2, vec2d, vec2d2);
        if (vec2d3 != null) {
            Leg leg = new Leg(vec2d, vec2d3, highway, HighwayNetwork.jOdDDFXSeWl4(vec2d, vec2d3));
            Leg leg2 = new Leg(vec2d3, vec2d2, highway2, HighwayNetwork.jOdDDFXSeWl4(vec2d3, vec2d2));
            return new Route(List.of(leg, leg2));
        }
        return HighwayRouter.jOdDDFXSeWl4(vec2d, highway, vec2d2, highway2);
    }

    private static HighwayNetwork.Highway mp3zoXQFKUKYj5(double d, double d2) {
        HighwayNetwork.Highway highway = null;
        double d3 = Double.MAX_VALUE;
        WorldUtils.Vec2d vec2d = new WorldUtils.Vec2d(d, d2);
        for (HighwayNetwork.Highway highway2 : HighwayNetwork.AoH6MX) {
            double d4 = HighwayNetwork.jOdDDFXSeWl4(highway2.jOdDDFXSeWl4(d, d2), vec2d);
            if (!(d4 < d3)) continue;
            d3 = d4;
            highway = highway2;
        }
        return d3 <= 150.0 ? highway : null;
    }

    private static SnapResult Gt56Sj4a6BWhgB(double d, double d2) {
        HighwayNetwork.Highway highway = null;
        WorldUtils.Vec2d vec2d = null;
        double d3 = Double.MAX_VALUE;
        WorldUtils.Vec2d vec2d2 = new WorldUtils.Vec2d(d, d2);
        for (HighwayNetwork.Highway highway2 : HighwayNetwork.AoH6MX) {
            WorldUtils.Vec2d vec2d3 = highway2.jOdDDFXSeWl4(d, d2);
            double d4 = HighwayNetwork.jOdDDFXSeWl4(vec2d3, vec2d2);
            if (!(d4 < d3)) continue;
            d3 = d4;
            vec2d = vec2d3;
            highway = highway2;
        }
        return highway != null ? new SnapResult(vec2d, highway) : null;
    }

    private static WorldUtils.Vec2d jOdDDFXSeWl4(HighwayNetwork.Highway highway, HighwayNetwork.Highway highway2, WorldUtils.Vec2d vec2d, WorldUtils.Vec2d vec2d2) {
        ArrayList<WorldUtils.Vec2d> arrayList = new ArrayList<WorldUtils.Vec2d>();
        WorldUtils.Vec2d vec2d3 = HighwayNetwork.jOdDDFXSeWl4(highway, highway2);
        if (vec2d3 != null && highway.jOdDDFXSeWl4(vec2d3.mcAmeo(), vec2d3.ckqstPn4Gd(), 1.0) && highway2.jOdDDFXSeWl4(vec2d3.mcAmeo(), vec2d3.ckqstPn4Gd(), 1.0)) {
            arrayList.add(vec2d3);
        }
        for (HighwayNetwork.Intersection intersection : HighwayNetwork.kl5Nqm9U9tT9R48) {
            if (!highway.jOdDDFXSeWl4(intersection.t4IlnBm0D().mcAmeo(), intersection.t4IlnBm0D().ckqstPn4Gd(), 1.0) || !highway2.jOdDDFXSeWl4(intersection.t4IlnBm0D().mcAmeo(), intersection.t4IlnBm0D().ckqstPn4Gd(), 1.0)) continue;
            arrayList.add(intersection.t4IlnBm0D());
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        Object object = null;
        double d = Double.MAX_VALUE;
        for (WorldUtils.Vec2d vec2d4 : arrayList) {
            double d2;
            double d3 = HighwayNetwork.jOdDDFXSeWl4(vec2d, vec2d4) + HighwayNetwork.jOdDDFXSeWl4(vec2d4, vec2d2);
            if (d3 > (d2 = HighwayNetwork.jOdDDFXSeWl4(vec2d, vec2d2)) * 1.5 || !(d3 < d)) continue;
            d = d3;
            object = vec2d4;
        }
        return object;
    }

    private static Route jOdDDFXSeWl4(WorldUtils.Vec2d vec2d, HighwayNetwork.Highway highway, WorldUtils.Vec2d vec2d2, HighwayNetwork.Highway highway2) {
        Record record;
        int n;
        Object object;
        int n2 = HighwayRouter.jOdDDFXSeWl4(highway, vec2d, vec2d2);
        int n3 = HighwayRouter.mp3zoXQFKUKYj5(highway2, vec2d2);
        if (n2 < 0 || n3 < 0) {
            return null;
        }
        if (n2 == n3) {
            return Route.yF2JzAqyBTfec();
        }
        Map<Integer, List<HighwayNetwork.Edge>> map = HighwayNetwork.TF0ZUa0QN41EJWaC();
        int n4 = HighwayNetwork.kl5Nqm9U9tT9R48.size();
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
                double d2 = d + edge.r0hCSR0();
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
            object.add(new Leg(HighwayNetwork.kl5Nqm9U9tT9R48.get(n5).t4IlnBm0D(), HighwayNetwork.kl5Nqm9U9tT9R48.get(n).t4IlnBm0D(), ((HighwayNetwork.Edge)record).Z8PfWilTZRV(), ((HighwayNetwork.Edge)record).r0hCSR0()));
            n = n5;
        }
        Collections.reverse(object);
        WorldUtils.Vec2d vec2d3 = HighwayNetwork.kl5Nqm9U9tT9R48.get(n2).t4IlnBm0D();
        if (HighwayNetwork.jOdDDFXSeWl4(vec2d, vec2d3) > 10.0) {
            object.add(0, new Leg(vec2d, vec2d3, highway, HighwayNetwork.jOdDDFXSeWl4(vec2d, vec2d3)));
        }
        Record record2 = record = object.isEmpty() ? vec2d : ((Leg)object.get(object.size() - 1)).VcecHi2glQUu1VZB();
        if (HighwayNetwork.jOdDDFXSeWl4((WorldUtils.Vec2d)record, vec2d2) > 10.0) {
            object.add(new Leg((WorldUtils.Vec2d)record, vec2d2, highway2, HighwayNetwork.jOdDDFXSeWl4((WorldUtils.Vec2d)record, vec2d2)));
        }
        return new Route((List<Leg>)object);
    }

    private static int jOdDDFXSeWl4(HighwayNetwork.Highway highway, WorldUtils.Vec2d vec2d, WorldUtils.Vec2d vec2d2) {
        List<Integer> list = HighwayNetwork.mp3zoXQFKUKYj5(highway);
        int n = -1;
        double d = Double.MAX_VALUE;
        for (int n2 : list) {
            WorldUtils.Vec2d vec2d3 = HighwayNetwork.kl5Nqm9U9tT9R48.get(n2).t4IlnBm0D();
            double d2 = HighwayNetwork.jOdDDFXSeWl4(vec2d, vec2d3) + HighwayNetwork.jOdDDFXSeWl4(vec2d3, vec2d2);
            if (!(d2 < d)) continue;
            d = d2;
            n = n2;
        }
        return n;
    }

    private static int mp3zoXQFKUKYj5(HighwayNetwork.Highway highway, WorldUtils.Vec2d vec2d) {
        List<Integer> list = HighwayNetwork.mp3zoXQFKUKYj5(highway);
        int n = -1;
        double d = Double.MAX_VALUE;
        for (int n2 : list) {
            double d2 = HighwayNetwork.jOdDDFXSeWl4(vec2d, HighwayNetwork.kl5Nqm9U9tT9R48.get(n2).t4IlnBm0D());
            if (!(d2 < d)) continue;
            d = d2;
            n = n2;
        }
        return n;
    }

    static final class SnapResult
    extends Record {
        final WorldUtils.Vec2d gfAIDmJ7f;
        final HighwayNetwork.Highway kfGx5x4Y;

        SnapResult(WorldUtils.Vec2d vec2d, HighwayNetwork.Highway highway) {
            this.gfAIDmJ7f = vec2d;
            this.kfGx5x4Y = highway;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SnapResult.class, "point;highway", "gfAIDmJ7f", "kfGx5x4Y"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SnapResult.class, "point;highway", "gfAIDmJ7f", "kfGx5x4Y"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SnapResult.class, "point;highway", "gfAIDmJ7f", "kfGx5x4Y"}, this, object);
        }
    }

    public static class Route {
        public final List<Leg> LkopaVK1It4L;
        public final double wLT7SXJWTWWAfaMb;
        public final List<WorldUtils.Vec2d> UsO18QwQES9yS8g;

        Route(List<Leg> list) {
            this.LkopaVK1It4L = Collections.unmodifiableList(Route.vgrtgn5(list));
            this.wLT7SXJWTWWAfaMb = this.LkopaVK1It4L.stream().mapToDouble(Leg::r0hCSR0).sum();
            ArrayList<WorldUtils.Vec2d> arrayList = new ArrayList<WorldUtils.Vec2d>();
            if (!this.LkopaVK1It4L.isEmpty()) {
                arrayList.add(this.LkopaVK1It4L.get(0).Rd1eOmBQPxISFki());
            }
            for (Leg leg : this.LkopaVK1It4L) {
                arrayList.add(leg.VcecHi2glQUu1VZB());
            }
            this.UsO18QwQES9yS8g = Collections.unmodifiableList(arrayList);
        }

        public static Route yF2JzAqyBTfec() {
            return new Route(List.of());
        }

        private static List<Leg> vgrtgn5(List<Leg> list) {
            if (list.size() < 2) {
                return new ArrayList<Leg>(list);
            }
            ArrayList<Leg> arrayList = new ArrayList<Leg>();
            Leg leg = list.get(0);
            for (int i = 1; i < list.size(); ++i) {
                Leg leg2 = list.get(i);
                if (leg.Z8PfWilTZRV().equals(leg2.Z8PfWilTZRV()) && Route.jOdDDFXSeWl4(leg, leg2)) {
                    leg = new Leg(leg.Rd1eOmBQPxISFki(), leg2.VcecHi2glQUu1VZB(), leg.Z8PfWilTZRV(), leg.r0hCSR0() + leg2.r0hCSR0());
                    continue;
                }
                arrayList.add(leg);
                leg = leg2;
            }
            arrayList.add(leg);
            return arrayList;
        }

        private static boolean jOdDDFXSeWl4(Leg leg, Leg leg2) {
            double d;
            double d2 = leg.VcecHi2glQUu1VZB().mcAmeo() - leg.Rd1eOmBQPxISFki().mcAmeo();
            double d3 = leg.VcecHi2glQUu1VZB().ckqstPn4Gd() - leg.Rd1eOmBQPxISFki().ckqstPn4Gd();
            double d4 = leg2.VcecHi2glQUu1VZB().mcAmeo() - leg2.Rd1eOmBQPxISFki().mcAmeo();
            return d2 * d4 + d3 * (d = leg2.VcecHi2glQUu1VZB().ckqstPn4Gd() - leg2.Rd1eOmBQPxISFki().ckqstPn4Gd()) > 0.0;
        }
    }

    public static final class Leg
    extends Record {
        private final WorldUtils.Vec2d quFLaIBj1UQn6g;
        private final WorldUtils.Vec2d uFghvYncEwFBHmJL;
        private final HighwayNetwork.Highway HUYtvX;
        private final double BhO5G7;

        public Leg(WorldUtils.Vec2d vec2d, WorldUtils.Vec2d vec2d2, HighwayNetwork.Highway highway, double d) {
            this.quFLaIBj1UQn6g = vec2d;
            this.uFghvYncEwFBHmJL = vec2d2;
            this.HUYtvX = highway;
            this.BhO5G7 = d;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Leg.class, "from;to;highway;distance", "quFLaIBj1UQn6g", "uFghvYncEwFBHmJL", "HUYtvX", "BhO5G7"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Leg.class, "from;to;highway;distance", "quFLaIBj1UQn6g", "uFghvYncEwFBHmJL", "HUYtvX", "BhO5G7"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Leg.class, "from;to;highway;distance", "quFLaIBj1UQn6g", "uFghvYncEwFBHmJL", "HUYtvX", "BhO5G7"}, this, object);
        }

        public WorldUtils.Vec2d Rd1eOmBQPxISFki() {
            return this.quFLaIBj1UQn6g;
        }

        public WorldUtils.Vec2d VcecHi2glQUu1VZB() {
            return this.uFghvYncEwFBHmJL;
        }

        public HighwayNetwork.Highway Z8PfWilTZRV() {
            return this.HUYtvX;
        }

        public double r0hCSR0() {
            return this.BhO5G7;
        }
    }
}

