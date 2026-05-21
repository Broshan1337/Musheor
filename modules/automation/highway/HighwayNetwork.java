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
    public static final List<Highway> AoH6MX = HighwayNetwork.gKa5NJsmT();
    public static final List<Intersection> kl5Nqm9U9tT9R48 = HighwayNetwork.TAdu5cndwWu3A1(AoH6MX);
    public static final List<Edge> Dzj74FIoxmie = HighwayNetwork.jOdDDFXSeWl4(AoH6MX, kl5Nqm9U9tT9R48);

    private static double[] n70VJ4CE5nwNu() {
        return HighwayNetworkManager.getInstance().getRingDistances("2b2t");
    }

    private static double[] gfosOAUCOp8Yq() {
        return HighwayNetworkManager.getInstance().getDiamondDistances("2b2t");
    }

    private static List<Highway> gKa5NJsmT() {
        ArrayList<Highway> arrayList = new ArrayList<Highway>();
        arrayList.add(Highway.cjuOUcp2TVL3());
        arrayList.add(Highway.mWal2KsneArWJnz());
        arrayList.add(Highway.byVifkEYgkzY1E());
        arrayList.add(Highway.ZuBA2SJemxMpFD1());
        for (double d : HighwayNetwork.n70VJ4CE5nwNu()) {
            arrayList.add(Highway.mp3zoXQFKUKYj5(d));
            arrayList.add(Highway.mp3zoXQFKUKYj5(-d));
            arrayList.add(Highway.Gt56Sj4a6BWhgB(d));
            arrayList.add(Highway.Gt56Sj4a6BWhgB(-d));
        }
        for (double d : HighwayNetwork.gfosOAUCOp8Yq()) {
            arrayList.add(Highway.jOdDDFXSeWl4(true, d));
            arrayList.add(Highway.jOdDDFXSeWl4(true, -d));
            arrayList.add(Highway.jOdDDFXSeWl4(false, d));
            arrayList.add(Highway.jOdDDFXSeWl4(false, -d));
        }
        for (int i = 1; i <= 10; ++i) {
            double d = (double)i * 5000.0 + 0.5;
            arrayList.add(Highway.TAdu5cndwWu3A1(d));
            arrayList.add(Highway.TAdu5cndwWu3A1(-d));
            arrayList.add(Highway.vgrtgn5(d));
            arrayList.add(Highway.vgrtgn5(-d));
        }
        return Collections.unmodifiableList(arrayList);
    }

    private static List<Intersection> TAdu5cndwWu3A1(List<Highway> list) {
        Object object;
        Record record;
        LinkedHashMap<String, List> linkedHashMap = new LinkedHashMap<String, List>();
        for (int i = 0; i < list.size(); ++i) {
            for (int j = i + 1; j < list.size(); ++j) {
                Highway object2 = list.get(i);
                object = HighwayNetwork.jOdDDFXSeWl4(object2, (Highway)(record = list.get(j)));
                if (object == null || !object2.jOdDDFXSeWl4(((WorldUtils.Vec2d)object).mcAmeo(), ((WorldUtils.Vec2d)object).ckqstPn4Gd(), 1.0) || !((Highway)record).jOdDDFXSeWl4(((WorldUtils.Vec2d)object).mcAmeo(), ((WorldUtils.Vec2d)object).ckqstPn4Gd(), 1.0)) continue;
                String string2 = HighwayNetwork.TAdu5cndwWu3A1((WorldUtils.Vec2d)object);
                linkedHashMap.computeIfAbsent(string2, string -> new ArrayList()).add(object2);
                ((List)linkedHashMap.get(string2)).add(record);
            }
        }
        ArrayList<Intersection> arrayList = new ArrayList<Intersection>();
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            record = HighwayNetwork.jWrhVf2psx((String)entry.getKey());
            object = ((List)entry.getValue()).stream().distinct().toList();
            arrayList.add(new Intersection((WorldUtils.Vec2d)record, (List<Highway>)object));
        }
        return Collections.unmodifiableList(arrayList);
    }

    private static List<Edge> jOdDDFXSeWl4(List<Highway> list, List<Intersection> list2) {
        ArrayList<Edge> arrayList = new ArrayList<Edge>();
        for (Highway highway : list) {
            int n3;
            ArrayList<Integer> arrayList2 = new ArrayList<Integer>();
            for (n3 = 0; n3 < list2.size(); ++n3) {
                WorldUtils.Vec2d vec2d = list2.get(n3).t4IlnBm0D();
                if (!highway.jOdDDFXSeWl4(vec2d.mcAmeo(), vec2d.ckqstPn4Gd(), 1.0)) continue;
                arrayList2.add(n3);
            }
            arrayList2.sort((n, n2) -> {
                WorldUtils.Vec2d vec2d = ((Intersection)list2.get((int)n)).t4IlnBm0D();
                WorldUtils.Vec2d vec2d2 = ((Intersection)list2.get((int)n2)).t4IlnBm0D();
                double d = HighwayNetwork.jOdDDFXSeWl4(highway, vec2d);
                double d2 = HighwayNetwork.jOdDDFXSeWl4(highway, vec2d2);
                return Double.compare(d, d2);
            });
            for (n3 = 0; n3 < arrayList2.size() - 1; ++n3) {
                int n4 = (Integer)arrayList2.get(n3);
                int n5 = (Integer)arrayList2.get(n3 + 1);
                WorldUtils.Vec2d vec2d = list2.get(n4).t4IlnBm0D();
                WorldUtils.Vec2d vec2d2 = list2.get(n5).t4IlnBm0D();
                double d = HighwayNetwork.jOdDDFXSeWl4(vec2d, vec2d2);
                arrayList.add(new Edge(n4, n5, d, highway));
                arrayList.add(new Edge(n5, n4, d, highway));
            }
        }
        return Collections.unmodifiableList(arrayList);
    }

    static WorldUtils.Vec2d jOdDDFXSeWl4(Highway highway, Highway highway2) {
        double[] dArray = HighwayNetwork.jOdDDFXSeWl4(highway);
        double[] dArray2 = HighwayNetwork.jOdDDFXSeWl4(highway2);
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

    private static double[] jOdDDFXSeWl4(Highway highway) {
        double[] dArray;
        switch (highway.sVkASV().ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: 
            case 2: 
            case 4: {
                if (highway.ISNvq0uvdjAugvE()) {
                    double[] dArray2 = new double[3];
                    dArray2[0] = 0.0;
                    dArray2[1] = 1.0;
                    dArray = dArray2;
                    dArray2[2] = highway.ZhoaRNJV1pNk();
                    break;
                }
                double[] dArray3 = new double[3];
                dArray3[0] = 1.0;
                dArray3[1] = 0.0;
                dArray = dArray3;
                dArray3[2] = highway.ZhoaRNJV1pNk();
                break;
            }
            case 1: {
                if (highway.ISNvq0uvdjAugvE()) {
                    double[] dArray4 = new double[3];
                    dArray4[0] = 1.0;
                    dArray4[1] = 1.0;
                    dArray = dArray4;
                    dArray4[2] = highway.ZhoaRNJV1pNk();
                    break;
                }
                double[] dArray5 = new double[3];
                dArray5[0] = 1.0;
                dArray5[1] = -1.0;
                dArray = dArray5;
                dArray5[2] = highway.ZhoaRNJV1pNk();
                break;
            }
            case 3: {
                if (highway.ISNvq0uvdjAugvE()) {
                    double[] dArray6 = new double[3];
                    dArray6[0] = 1.0;
                    dArray6[1] = 1.0;
                    dArray = dArray6;
                    dArray6[2] = highway.ZhoaRNJV1pNk();
                    break;
                }
                double[] dArray7 = new double[3];
                dArray7[0] = 1.0;
                dArray7[1] = -1.0;
                dArray = dArray7;
                dArray7[2] = highway.ZhoaRNJV1pNk();
            }
        }
        return dArray;
    }

    private static double jOdDDFXSeWl4(Highway highway, WorldUtils.Vec2d vec2d) {
        return highway.gvp3bKzV() ? vec2d.mcAmeo() : (highway.ISNvq0uvdjAugvE() ? vec2d.mcAmeo() : vec2d.ckqstPn4Gd());
    }

    public static double jOdDDFXSeWl4(WorldUtils.Vec2d vec2d, WorldUtils.Vec2d vec2d2) {
        double d = vec2d.mcAmeo() - vec2d2.mcAmeo();
        double d2 = vec2d.ckqstPn4Gd() - vec2d2.ckqstPn4Gd();
        return Math.sqrt(d * d + d2 * d2);
    }

    private static String TAdu5cndwWu3A1(WorldUtils.Vec2d vec2d) {
        return Math.round(vec2d.mcAmeo()) + "," + Math.round(vec2d.ckqstPn4Gd());
    }

    private static WorldUtils.Vec2d jWrhVf2psx(String string) {
        String[] stringArray = string.split(",");
        return new WorldUtils.Vec2d(Double.parseDouble(stringArray[0]), Double.parseDouble(stringArray[1]));
    }

    public static List<Integer> mp3zoXQFKUKYj5(Highway highway) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < kl5Nqm9U9tT9R48.size(); ++i) {
            WorldUtils.Vec2d vec2d = kl5Nqm9U9tT9R48.get(i).t4IlnBm0D();
            if (!highway.jOdDDFXSeWl4(vec2d.mcAmeo(), vec2d.ckqstPn4Gd(), 1.0)) continue;
            arrayList.add(i);
        }
        return arrayList;
    }

    public static Map<Integer, List<Edge>> TF0ZUa0QN41EJWaC() {
        HashMap<Integer, List<Edge>> hashMap = new HashMap<Integer, List<Edge>>();
        for (int i = 0; i < kl5Nqm9U9tT9R48.size(); ++i) {
            hashMap.put(i, new ArrayList());
        }
        for (Edge edge : Dzj74FIoxmie) {
            ((List)hashMap.get(edge.hXpkL9u())).add(edge);
        }
        return hashMap;
    }

    public static final class Highway
    extends Record {
        private final String Lal2Zyi076;
        private final HighwayType ZeOLrA;
        private final boolean H02kTTf;
        private final double v9AU7qJqNC84x;
        private final double QAoBqV;
        private final double WDd4dOS9;

        public Highway(String string, HighwayType highwayType, boolean bl, double d, double d2, double d3) {
            this.Lal2Zyi076 = string;
            this.ZeOLrA = highwayType;
            this.H02kTTf = bl;
            this.v9AU7qJqNC84x = d;
            this.QAoBqV = d2;
            this.WDd4dOS9 = d3;
        }

        static Highway cjuOUcp2TVL3() {
            return new Highway("+X/-X axis (Z=0)", HighwayType.TMdT6kYQyv0It, true, 0.0, -1.7976931348623157E308, Double.MAX_VALUE);
        }

        static Highway mWal2KsneArWJnz() {
            return new Highway("+Z/-Z axis (X=0)", HighwayType.TMdT6kYQyv0It, false, 0.0, -1.7976931348623157E308, Double.MAX_VALUE);
        }

        static Highway byVifkEYgkzY1E() {
            return new Highway("diagonal (X=Z)", HighwayType.yVhVr2zkw, false, 0.0, -1.7976931348623157E308, Double.MAX_VALUE);
        }

        static Highway ZuBA2SJemxMpFD1() {
            return new Highway("diagonal (X=-Z)", HighwayType.yVhVr2zkw, true, 0.0, -1.7976931348623157E308, Double.MAX_VALUE);
        }

        static Highway mp3zoXQFKUKYj5(double d) {
            double d2 = Math.abs(d);
            return new Highway(String.format("ring road Z=%.0f", d), HighwayType.ZjVmRLiAeys38, true, d, -d2, d2);
        }

        static Highway Gt56Sj4a6BWhgB(double d) {
            double d2 = Math.abs(d);
            return new Highway(String.format("ring road X=%.0f", d), HighwayType.ZjVmRLiAeys38, false, d, -d2, d2);
        }

        static Highway jOdDDFXSeWl4(boolean bl, double d) {
            double d2 = Math.min(0.0, d);
            double d3 = Math.max(0.0, d);
            String string = bl ? String.format("diamond X+Z=%.0f", d) : String.format("diamond X-Z=%.0f", d);
            return new Highway(string, HighwayType.JaevRTUQKWIx5LQ, bl, d, d2, d3);
        }

        static Highway TAdu5cndwWu3A1(double d) {
            return new Highway(String.format("grid Z=%.1f", d), HighwayType.xynAsOKhN7t, true, d, -50000.5, 50000.5);
        }

        static Highway vgrtgn5(double d) {
            return new Highway(String.format("grid X=%.1f", d), HighwayType.xynAsOKhN7t, false, d, -50000.5, 50000.5);
        }

        boolean gvp3bKzV() {
            return this.ZeOLrA == HighwayType.yVhVr2zkw || this.ZeOLrA == HighwayType.JaevRTUQKWIx5LQ;
        }

        boolean jOdDDFXSeWl4(double d, double d2, double d3) {
            return switch (this.ZeOLrA.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2, 4 -> {
                    if (this.H02kTTf) {
                        if (Math.abs(d2 - this.v9AU7qJqNC84x) <= d3 && d >= this.QAoBqV - d3 && d <= this.WDd4dOS9 + d3) {
                            yield true;
                        }
                        yield false;
                    }
                    if (Math.abs(d - this.v9AU7qJqNC84x) <= d3 && d2 >= this.QAoBqV - d3 && d2 <= this.WDd4dOS9 + d3) {
                        yield true;
                    }
                    yield false;
                }
                case 1 -> {
                    double var7_4;
                    double v1 = var7_4 = this.H02kTTf ? d + d2 : d - d2;
                    if (Math.abs(var7_4 - this.v9AU7qJqNC84x) <= d3) {
                        yield true;
                    }
                    yield false;
                }
                case 3 -> {
                    double var7_5;
                    double v2 = var7_5 = this.H02kTTf ? d + d2 : d - d2;
                    if (Math.abs(var7_5 - this.v9AU7qJqNC84x) <= d3 && d >= this.QAoBqV - d3 && d <= this.WDd4dOS9 + d3) {
                        yield true;
                    }
                    yield false;
                }
            };
        }

        double VYEwzRq(double d) {
            return Math.max(this.QAoBqV, Math.min(this.WDd4dOS9, d));
        }

        WorldUtils.Vec2d jOdDDFXSeWl4(double d, double d2) {
            return switch (this.ZeOLrA.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2, 4 -> {
                    if (this.H02kTTf) {
                        double var5_3 = this.VYEwzRq(d);
                        yield new WorldUtils.Vec2d(var5_3, this.v9AU7qJqNC84x);
                    }
                    double var5_4 = this.VYEwzRq(d2);
                    yield new WorldUtils.Vec2d(this.v9AU7qJqNC84x, var5_4);
                }
                case 1 -> {
                    double var5_6;
                    if (this.H02kTTf) {
                        double var5_5 = (d - d2) / 2.0;
                        double var7_8 = -var5_5;
                        yield new WorldUtils.Vec2d(var5_5, var7_8);
                    }
                    double var7_9 = var5_6 = (d + d2) / 2.0;
                    yield new WorldUtils.Vec2d(var5_6, var7_9);
                }
                case 3 -> {
                    boolean var5_7 = this.H02kTTf;
                    double var6_10 = var5_7 ? (d - d2 + this.v9AU7qJqNC84x) / 2.0 : (d + d2 + this.v9AU7qJqNC84x) / 2.0;
                    var6_10 = this.VYEwzRq(var6_10);
                    double var8_11 = var5_7 ? this.v9AU7qJqNC84x - var6_10 : var6_10 - this.v9AU7qJqNC84x;
                    yield new WorldUtils.Vec2d(var6_10, var8_11);
                }
            };
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Highway.class, "name;type;isHorizontal;constCoord;minCoord;maxCoord", "Lal2Zyi076", "ZeOLrA", "H02kTTf", "v9AU7qJqNC84x", "QAoBqV", "WDd4dOS9"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Highway.class, "name;type;isHorizontal;constCoord;minCoord;maxCoord", "Lal2Zyi076", "ZeOLrA", "H02kTTf", "v9AU7qJqNC84x", "QAoBqV", "WDd4dOS9"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Highway.class, "name;type;isHorizontal;constCoord;minCoord;maxCoord", "Lal2Zyi076", "ZeOLrA", "H02kTTf", "v9AU7qJqNC84x", "QAoBqV", "WDd4dOS9"}, this, object);
        }

        public String name() {
            return this.Lal2Zyi076;
        }

        public HighwayType sVkASV() {
            return this.ZeOLrA;
        }

        public boolean ISNvq0uvdjAugvE() {
            return this.H02kTTf;
        }

        public double ZhoaRNJV1pNk() {
            return this.v9AU7qJqNC84x;
        }
    }

    public static final class Intersection
    extends Record {
        private final WorldUtils.Vec2d Bocqo9ajQ;
        private final List<Highway> BT1BimvycZZjsYS;

        public Intersection(WorldUtils.Vec2d vec2d, List<Highway> list) {
            this.Bocqo9ajQ = vec2d;
            this.BT1BimvycZZjsYS = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Intersection.class, "pos;highways", "Bocqo9ajQ", "BT1BimvycZZjsYS"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Intersection.class, "pos;highways", "Bocqo9ajQ", "BT1BimvycZZjsYS"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Intersection.class, "pos;highways", "Bocqo9ajQ", "BT1BimvycZZjsYS"}, this, object);
        }

        public WorldUtils.Vec2d t4IlnBm0D() {
            return this.Bocqo9ajQ;
        }
    }

    public static final class Edge
    extends Record {
        private final int FeGlqzs7Rjvi;
        private final int H4b9BDTz5I9d4B1z;
        private final double oIn3mVM8z;
        private final Highway abVxPfXsrl5;

        public Edge(int n, int n2, double d, Highway highway) {
            this.FeGlqzs7Rjvi = n;
            this.H4b9BDTz5I9d4B1z = n2;
            this.oIn3mVM8z = d;
            this.abVxPfXsrl5 = highway;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Edge.class, "from;to;distance;highway", "FeGlqzs7Rjvi", "H4b9BDTz5I9d4B1z", "oIn3mVM8z", "abVxPfXsrl5"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Edge.class, "from;to;distance;highway", "FeGlqzs7Rjvi", "H4b9BDTz5I9d4B1z", "oIn3mVM8z", "abVxPfXsrl5"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Edge.class, "from;to;distance;highway", "FeGlqzs7Rjvi", "H4b9BDTz5I9d4B1z", "oIn3mVM8z", "abVxPfXsrl5"}, this, object);
        }

        public int hXpkL9u() {
            return this.FeGlqzs7Rjvi;
        }

        public int oosx8z2R() {
            return this.H4b9BDTz5I9d4B1z;
        }

        public double r0hCSR0() {
            return this.oIn3mVM8z;
        }

        public Highway Z8PfWilTZRV() {
            return this.abVxPfXsrl5;
        }
    }

    public static final class HighwayType
    extends Enum<HighwayType> {
        public static final /* enum */ HighwayType TMdT6kYQyv0It = new HighwayType();
        public static final /* enum */ HighwayType yVhVr2zkw = new HighwayType();
        public static final /* enum */ HighwayType ZjVmRLiAeys38 = new HighwayType();
        public static final /* enum */ HighwayType JaevRTUQKWIx5LQ = new HighwayType();
        public static final /* enum */ HighwayType xynAsOKhN7t = new HighwayType();
        private static final /* synthetic */ HighwayType[] Gd2ks78ySQq40;

        public static HighwayType[] values() {
            return (HighwayType[])Gd2ks78ySQq40.clone();
        }

        public static HighwayType valueOf(String string) {
            return Enum.valueOf(HighwayType.class, string);
        }

        private static /* synthetic */ HighwayType[] VnBeu9FFeHHM() {
            return new HighwayType[]{TMdT6kYQyv0It, yVhVr2zkw, ZjVmRLiAeys38, JaevRTUQKWIx5LQ, xynAsOKhN7t};
        }

        static {
            Gd2ks78ySQq40 = HighwayType.VnBeu9FFeHHM();
        }
    }
}

