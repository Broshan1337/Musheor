// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation.highway;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import musheor.utils.WorldUtils;
import musheor.utils.system.HighwayNetworkManager;

public class HighwayNetwork {
    public static final List<Highway> HIGHWAYS = HighwayNetwork.buildHighwayList();
    public static final List<Intersection> INTERSECTIONS = HighwayNetwork.buildIntersections(HIGHWAYS);
    public static final List<Edge> EDGES = HighwayNetwork.buildEdges(HIGHWAYS, INTERSECTIONS);

    private static double[] getRingDistances() {
        return HighwayNetworkManager.getInstance().getRingDistances("2b2t");
    }

    private static double[] getDiamondDistances() {
        return HighwayNetworkManager.getInstance().getDiamondDistances("2b2t");
    }

    private static List<Highway> buildHighwayList() {
        ArrayList<Highway> arrayList = new ArrayList<Highway>();
        arrayList.add(Highway.xAxis());
        arrayList.add(Highway.zAxis());
        arrayList.add(Highway.diagonalXeqZ());
        arrayList.add(Highway.diagonalXeqNegZ());
        for (double d : HighwayNetwork.getRingDistances()) {
            arrayList.add(Highway.ringRoadZ(d));
            arrayList.add(Highway.ringRoadZ(-d));
            arrayList.add(Highway.ringRoadX(d));
            arrayList.add(Highway.ringRoadX(-d));
        }
        for (double d : HighwayNetwork.getDiamondDistances()) {
            arrayList.add(Highway.diamondHighway(true, d));
            arrayList.add(Highway.diamondHighway(true, -d));
            arrayList.add(Highway.diamondHighway(false, d));
            arrayList.add(Highway.diamondHighway(false, -d));
        }
        for (int i = 1; i <= 10; ++i) {
            double d = (double)i * 5000.0 + 0.5;
            arrayList.add(Highway.gridZ(d));
            arrayList.add(Highway.gridZ(-d));
            arrayList.add(Highway.gridX(d));
            arrayList.add(Highway.gridX(-d));
        }
        return Collections.unmodifiableList(arrayList);
    }

    private static List<Intersection> buildIntersections(List<Highway> list) {
        Object object;
        Record record;
        LinkedHashMap<String, List> linkedHashMap = new LinkedHashMap<String, List>();
        for (int i = 0; i < list.size(); ++i) {
            for (int j = i + 1; j < list.size(); ++j) {
                Highway object2 = list.get(i);
                object = HighwayNetwork.findIntersectionPoint(object2, (Highway)(record = list.get(j)));
                if (object == null || !object2.contains(((WorldUtils.Vec2d)object).x(), ((WorldUtils.Vec2d)object).z(), 1.0) || !((Highway)record).contains(((WorldUtils.Vec2d)object).x(), ((WorldUtils.Vec2d)object).z(), 1.0)) continue;
                String string2 = HighwayNetwork.vecToKey((WorldUtils.Vec2d)object);
                linkedHashMap.computeIfAbsent(string2, string -> new ArrayList()).add(object2);
                ((List)linkedHashMap.get(string2)).add(record);
            }
        }
        ArrayList<Intersection> arrayList = new ArrayList<Intersection>();
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            record = HighwayNetwork.keyToVec((String)entry.getKey());
            object = ((List)entry.getValue()).stream().distinct().toList();
            arrayList.add(new Intersection((WorldUtils.Vec2d)record, (List<Highway>)object));
        }
        return Collections.unmodifiableList(arrayList);
    }

    private static List<Edge> buildEdges(List<Highway> list, List<Intersection> list2) {
        ArrayList<Edge> arrayList = new ArrayList<Edge>();
        for (Highway highway : list) {
            int n3;
            ArrayList<Integer> arrayList2 = new ArrayList<Integer>();
            for (n3 = 0; n3 < list2.size(); ++n3) {
                WorldUtils.Vec2d vec2d = list2.get(n3).pos();
                if (!highway.contains(vec2d.x(), vec2d.z(), 1.0)) continue;
                arrayList2.add(n3);
            }
            arrayList2.sort((n, n2) -> {
                WorldUtils.Vec2d vec2d = ((Intersection)list2.get((int)n)).pos();
                WorldUtils.Vec2d vec2d2 = ((Intersection)list2.get((int)n2)).pos();
                double d = HighwayNetwork.getAxisCoord(highway, vec2d);
                double d2 = HighwayNetwork.getAxisCoord(highway, vec2d2);
                return Double.compare(d, d2);
            });
            for (n3 = 0; n3 < arrayList2.size() - 1; ++n3) {
                int n4 = (Integer)arrayList2.get(n3);
                int n5 = (Integer)arrayList2.get(n3 + 1);
                WorldUtils.Vec2d vec2d = list2.get(n4).pos();
                WorldUtils.Vec2d vec2d2 = list2.get(n5).pos();
                double d = HighwayNetwork.distance(vec2d, vec2d2);
                arrayList.add(new Edge(n4, n5, d, highway));
                arrayList.add(new Edge(n5, n4, d, highway));
            }
        }
        return Collections.unmodifiableList(arrayList);
    }

    static WorldUtils.Vec2d findIntersectionPoint(Highway highway, Highway highway2) {
        double[] dArray = HighwayNetwork.toLineCoeffs(highway);
        double[] dArray2 = HighwayNetwork.toLineCoeffs(highway2);
        if (dArray == null || dArray2 == null) {
            return null;
        }
        double d = dArray[0] * dArray2[1] - dArray2[0] * dArray[1];
        if (Math.abs(d) < 1.0E-9) {
            return null;
        }
        double d2 = (dArray[2] * dArray2[1] - dArray2[2] * dArray[1]) / d;
        double d3 = (dArray[0] * dArray2[2] - dArray2[0] * dArray[2]) / d;
        return new WorldUtils.Vec2d(d2, d3);
    }

    private static double[] toLineCoeffs(Highway highway) {
        double[] dArray;
        switch (highway.type().ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: 
            case 2: 
            case 4: {
                if (highway.isHorizontal()) {
                    double[] dArray2 = new double[3];
                    dArray2[0] = 0.0;
                    dArray2[1] = 1.0;
                    dArray = dArray2;
                    dArray2[2] = highway.constCoord();
                    break;
                }
                double[] dArray3 = new double[3];
                dArray3[0] = 1.0;
                dArray3[1] = 0.0;
                dArray = dArray3;
                dArray3[2] = highway.constCoord();
                break;
            }
            case 1: {
                if (highway.isHorizontal()) {
                    double[] dArray4 = new double[3];
                    dArray4[0] = 1.0;
                    dArray4[1] = 1.0;
                    dArray = dArray4;
                    dArray4[2] = highway.constCoord();
                    break;
                }
                double[] dArray5 = new double[3];
                dArray5[0] = 1.0;
                dArray5[1] = -1.0;
                dArray = dArray5;
                dArray5[2] = highway.constCoord();
                break;
            }
            case 3: {
                if (highway.isHorizontal()) {
                    double[] dArray6 = new double[3];
                    dArray6[0] = 1.0;
                    dArray6[1] = 1.0;
                    dArray = dArray6;
                    dArray6[2] = highway.constCoord();
                    break;
                }
                double[] dArray7 = new double[3];
                dArray7[0] = 1.0;
                dArray7[1] = -1.0;
                dArray = dArray7;
                dArray7[2] = highway.constCoord();
            }
        }
        return dArray;
    }

    private static double getAxisCoord(Highway highway, WorldUtils.Vec2d vec2d) {
        return highway.isDiagonal() ? vec2d.x() : (highway.isHorizontal() ? vec2d.x() : vec2d.z());
    }

    public static double distance(WorldUtils.Vec2d vec2d, WorldUtils.Vec2d vec2d2) {
        double d = vec2d.x() - vec2d2.x();
        double d2 = vec2d.z() - vec2d2.z();
        return Math.sqrt(d * d + d2 * d2);
    }

    private static String vecToKey(WorldUtils.Vec2d vec2d) {
        return Math.round(vec2d.x()) + "," + Math.round(vec2d.z());
    }

    private static WorldUtils.Vec2d keyToVec(String string) {
        String[] stringArray = string.split(",");
        return new WorldUtils.Vec2d(Double.parseDouble(stringArray[0]), Double.parseDouble(stringArray[1]));
    }

    public static List<Integer> getIntersectionIndices(Highway highway) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < kl5Nqm9U9tT9R48.size(); ++i) {
            WorldUtils.Vec2d vec2d = kl5Nqm9U9tT9R48.get(i).pos();
            if (!highway.contains(vec2d.x(), vec2d.z(), 1.0)) continue;
            arrayList.add(i);
        }
        return arrayList;
    }

    public static Map<Integer, List<Edge>> buildEdgesByIntersection() {
        HashMap<Integer, List<Edge>> hashMap = new HashMap<Integer, List<Edge>>();
        for (int i = 0; i < kl5Nqm9U9tT9R48.size(); ++i) {
            hashMap.put(i, new ArrayList());
        }
        for (Edge edge : Dzj74FIoxmie) {
            ((List)hashMap.get(edge.from())).add(edge);
        }
        return hashMap;
    }

    public static final class Highway
    extends Record {
        private final String name;
        private final HighwayType type;
        private final boolean isHorizontal;
        private final double constCoord;
        private final double minCoord;
        private final double maxCoord;

        public Highway(String string, HighwayType highwayType, boolean bl, double d, double d2, double d3) {
            this.name = string;
            this.type = highwayType;
            this.isHorizontal = bl;
            this.constCoord = d;
            this.minCoord = d2;
            this.maxCoord = d3;
        }

        static Highway xAxis() {
            return new Highway("+X/-X axis (Z=0)", HighwayType.Axis, true, 0.0, -1.7976931348623157E308, Double.MAX_VALUE);
        }

        static Highway zAxis() {
            return new Highway("+Z/-Z axis (X=0)", HighwayType.Axis, false, 0.0, -1.7976931348623157E308, Double.MAX_VALUE);
        }

        static Highway diagonalXeqZ() {
            return new Highway("diagonal (X=Z)", HighwayType.MainDiagonal, false, 0.0, -1.7976931348623157E308, Double.MAX_VALUE);
        }

        static Highway diagonalXeqNegZ() {
            return new Highway("diagonal (X=-Z)", HighwayType.MainDiagonal, true, 0.0, -1.7976931348623157E308, Double.MAX_VALUE);
        }

        static Highway ringRoadZ(double d) {
            double d2 = Math.abs(d);
            return new Highway(String.format("ring road Z=%.0f", d), HighwayType.Ring, true, d, -d2, d2);
        }

        static Highway ringRoadX(double d) {
            double d2 = Math.abs(d);
            return new Highway(String.format("ring road X=%.0f", d), HighwayType.Ring, false, d, -d2, d2);
        }

        static Highway diamondHighway(boolean bl, double d) {
            double d2 = Math.min(0.0, d);
            double d3 = Math.max(0.0, d);
            String string = bl ? String.format("diamond X+Z=%.0f", d) : String.format("diamond X-Z=%.0f", d);
            return new Highway(string, HighwayType.Diamond, bl, d, d2, d3);
        }

        static Highway gridZ(double d) {
            return new Highway(String.format("grid Z=%.1f", d), HighwayType.Grid, true, d, -50000.5, 50000.5);
        }

        static Highway gridX(double d) {
            return new Highway(String.format("grid X=%.1f", d), HighwayType.Grid, false, d, -50000.5, 50000.5);
        }

        boolean isDiagonal() {
            return this.type == HighwayType.MainDiagonal || this.type == HighwayType.Diamond;
        }

        boolean contains(double d, double d2, double d3) {
            return switch (this.type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2, 4 -> {
                    if (this.isHorizontal) {
                        if (Math.abs(d2 - this.constCoord) <= d3 && d >= this.minCoord - d3 && d <= this.maxCoord + d3) {
                            yield true;
                        }
                        yield false;
                    }
                    if (Math.abs(d - this.constCoord) <= d3 && d2 >= this.minCoord - d3 && d2 <= this.maxCoord + d3) {
                        yield true;
                    }
                    yield false;
                }
                case 1 -> {
                    double var7_4;
                    double v1 = var7_4 = this.isHorizontal ? d + d2 : d - d2;
                    if (Math.abs(var7_4 - this.constCoord) <= d3) {
                        yield true;
                    }
                    yield false;
                }
                case 3 -> {
                    double var7_5;
                    double v2 = var7_5 = this.isHorizontal ? d + d2 : d - d2;
                    if (Math.abs(var7_5 - this.constCoord) <= d3 && d >= this.minCoord - d3 && d <= this.maxCoord + d3) {
                        yield true;
                    }
                    yield false;
                }
            };
        }

        double clampCoord(double d) {
            return Math.max(this.minCoord, Math.min(this.maxCoord, d));
        }

        WorldUtils.Vec2d nearestPoint(double d, double d2) {
            return switch (this.type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2, 4 -> {
                    if (this.isHorizontal) {
                        double var5_3 = this.clampCoord(d);
                        yield new WorldUtils.Vec2d(var5_3, this.constCoord);
                    }
                    double var5_4 = this.clampCoord(d2);
                    yield new WorldUtils.Vec2d(this.constCoord, var5_4);
                }
                case 1 -> {
                    double var5_6;
                    if (this.isHorizontal) {
                        double var5_5 = (d - d2) / 2.0;
                        double var7_8 = -var5_5;
                        yield new WorldUtils.Vec2d(var5_5, var7_8);
                    }
                    double var7_9 = var5_6 = (d + d2) / 2.0;
                    yield new WorldUtils.Vec2d(var5_6, var7_9);
                }
                case 3 -> {
                    boolean var5_7 = this.isHorizontal;
                    double var6_10 = var5_7 ? (d - d2 + this.constCoord) / 2.0 : (d + d2 + this.constCoord) / 2.0;
                    var6_10 = this.clampCoord(var6_10);
                    double var8_11 = var5_7 ? this.constCoord - var6_10 : var6_10 - this.constCoord;
                    yield new WorldUtils.Vec2d(var6_10, var8_11);
                }
            };
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Highway.class, "name;type;isHorizontal;constCoord;minCoord;maxCoord", "name", "type", "isHorizontal", "constCoord", "minCoord", "maxCoord"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Highway.class, "name;type;isHorizontal;constCoord;minCoord;maxCoord", "name", "type", "isHorizontal", "constCoord", "minCoord", "maxCoord"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Highway.class, "name;type;isHorizontal;constCoord;minCoord;maxCoord", "name", "type", "isHorizontal", "constCoord", "minCoord", "maxCoord"}, this, object);
        }

        public String name() {
            return this.name;
        }

        public HighwayType type() {
            return this.type;
        }

        public boolean isHorizontal() {
            return this.isHorizontal;
        }

        public double constCoord() {
            return this.constCoord;
        }
    }

    public static final class Intersection
    extends Record {
        private final WorldUtils.Vec2d pos;
        private final List<Highway> highways;

        public Intersection(WorldUtils.Vec2d vec2d, List<Highway> list) {
            this.pos = vec2d;
            this.highways = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Intersection.class, "pos;highways", "pos", "highways"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Intersection.class, "pos;highways", "pos", "highways"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Intersection.class, "pos;highways", "pos", "highways"}, this, object);
        }

        public WorldUtils.Vec2d pos() {
            return this.pos;
        }
    }

    public static final class Edge
    extends Record {
        private final int from;
        private final int to;
        private final double distance;
        private final Highway highway;

        public Edge(int n, int n2, double d, Highway highway) {
            this.from = n;
            this.to = n2;
            this.distance = d;
            this.highway = highway;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Edge.class, "from;to;distance;highway", "from", "to", "distance", "highway"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Edge.class, "from;to;distance;highway", "from", "to", "distance", "highway"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Edge.class, "from;to;distance;highway", "from", "to", "distance", "highway"}, this, object);
        }

        public int from() {
            return this.from;
        }

        public int to() {
            return this.to;
        }

        public double distance() {
            return this.distance;
        }

        public Highway highway() {
            return this.highway;
        }
    }

    public static final class HighwayType
    extends Enum<HighwayType> {
        public static final /* enum */ HighwayType Axis = new HighwayType();
        public static final /* enum */ HighwayType MainDiagonal = new HighwayType();
        public static final /* enum */ HighwayType Ring = new HighwayType();
        public static final /* enum */ HighwayType Diamond = new HighwayType();
        public static final /* enum */ HighwayType Grid = new HighwayType();
        private static final /* synthetic */ HighwayType[] $VALUES;

        public static HighwayType[] values() {
            return (HighwayType[])$VALUES.clone();
        }

        public static HighwayType valueOf(String string) {
            return Enum.valueOf(HighwayType.class, string);
        }

        private static /* synthetic */ HighwayType[] $init() {
            return new HighwayType[]{Axis, MainDiagonal, Ring, Diamond, Grid};
        }

        static {
            $VALUES = HighwayType.$init();
        }
    }
}

