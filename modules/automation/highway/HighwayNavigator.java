// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation.highway;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import musheor.compat.VersionHelper;
import musheor.modules.automation.highway.HighwayNetwork;
import musheor.modules.automation.highway.HighwayRouter;
import musheor.utils.WorldUtils;
import musheor.utils.internal.PathingHelper;
import net.minecraft.BlockPos;
import net.minecraft.MinecraftClient;

public class HighwayNavigator {
    private final MinecraftClient pC75hPFWhX6l9fO = MinecraftClient.getInstance();
    private final HighwayRouter.Route ZR5lph5QmbF4;
    private final BounceController rsx7hBWYw;
    private final Listener BJiJWZC;
    private final Config zFc9E6nf;
    private State Tlldfou = State.urju0X;
    private int SMYZpUvCuykws2 = 0;
    private int Rvjkko3BhJ = 0;
    private int stuckTicks = 0;
    private int FERZ92ROAgUmnlta = 0;
    private WorldUtils.Vec2d LR7hJDRhkI = null;
    private double PvLNVHs2LlOde76 = Double.MAX_VALUE;

    public HighwayNavigator(HighwayRouter.Route route, Config config, BounceController bounceController, Listener listener) {
        this.ZR5lph5QmbF4 = route;
        this.zFc9E6nf = config;
        this.rsx7hBWYw = bounceController;
        this.BJiJWZC = listener;
    }

    public State QhaZQGwqdeFQp() {
        return this.Tlldfou;
    }

    public void tick() {
        if (this.pC75hPFWhX6l9fO.player == null || this.pC75hPFWhX6l9fO.world == null) {
            return;
        }
        if (this.Tlldfou == State.Pmh3HuqB53i0Y || this.Tlldfou == State.eNsdDMk8mJXTb) {
            return;
        }
        if (this.ZR5lph5QmbF4.LkopaVK1It4L.isEmpty()) {
            this.m9RUHINs8();
            return;
        }
        switch (this.Tlldfou.ordinal()) {
            case 0: {
                this.wyf4MqVc0fll();
                break;
            }
            case 1: {
                this.zOg3JjefgZ();
                break;
            }
            case 2: {
                this.JZDYaLhvUPKPZH();
                break;
            }
            case 3: {
                this.rBGedpmjQyZ();
                break;
            }
            case 4: {
                this.mR2Jt8P();
            }
        }
    }

    public void xRVyNRV3cB7() {
        this.OyaWN2jsET();
        if (PathingHelper.LcPVM4w5KCoKSxGs()) {
            PathingHelper.xRVyNRV3cB7();
        }
    }

    private void wyf4MqVc0fll() {
        WorldUtils.Vec2d vec2d = this.NwHqgBmOLP();
        float f = this.Gt56Sj4a6BWhgB(vec2d);
        this.pC75hPFWhX6l9fO.player.method_36456(f);
        float f2 = Math.abs(this.jOdDDFXSeWl4(this.pC75hPFWhX6l9fO.player.method_36454(), f));
        ++this.Rvjkko3BhJ;
        if (f2 <= this.zFc9E6nf.JxUbzYJNdp9UvTN() || this.Rvjkko3BhJ >= this.zFc9E6nf.xs9d08DSpSt()) {
            this.Rvjkko3BhJ = 0;
            this.PvLNVHs2LlOde76 = this.jOdDDFXSeWl4(this.NwHqgBmOLP());
            this.bhy0Ddon9H6();
            this.mp3zoXQFKUKYj5(State.GLaGIbduHdrl, "bouncing toward " + this.aP5dDWz());
        }
    }

    private void zOg3JjefgZ() {
        WorldUtils.Vec2d vec2d = this.NwHqgBmOLP();
        double d = this.jOdDDFXSeWl4(vec2d);
        if (!PathingHelper.LcPVM4w5KCoKSxGs()) {
            this.pC75hPFWhX6l9fO.player.method_36456(this.Gt56Sj4a6BWhgB(vec2d));
        }
        this.Pg9t6rCsTkuc();
        if (d < this.zFc9E6nf.pyVwRgYkI() && d < this.PvLNVHs2LlOde76) {
            this.mp3zoXQFKUKYj5(State.o1Qz0II6HvwyAED, "approaching " + this.aP5dDWz());
        }
    }

    private void JZDYaLhvUPKPZH() {
        WorldUtils.Vec2d vec2d = this.NwHqgBmOLP();
        double d = this.jOdDDFXSeWl4(vec2d);
        this.pC75hPFWhX6l9fO.player.method_36456(this.Gt56Sj4a6BWhgB(vec2d));
        if (d < this.zFc9E6nf.X6N4Qf2Uc()) {
            this.OyaWN2jsET();
            BlockPos BlockPos2 = this.mp3zoXQFKUKYj5(vec2d);
            PathingHelper.l92qSNnpKrYO(BlockPos2);
            this.mp3zoXQFKUKYj5(State.gANxWblT, "transitioning at " + this.aP5dDWz());
        }
    }

    private void rBGedpmjQyZ() {
        if (!PathingHelper.LcPVM4w5KCoKSxGs()) {
            this.FERZ92ROAgUmnlta = this.zFc9E6nf.dzkD9N();
            this.mp3zoXQFKUKYj5(State.PlefynG, "settling at " + this.aP5dDWz());
        }
    }

    private void mR2Jt8P() {
        int n = this.SMYZpUvCuykws2 + 2;
        WorldUtils.Vec2d vec2d = n < this.ZR5lph5QmbF4.UsO18QwQES9yS8g.size() ? this.ZR5lph5QmbF4.UsO18QwQES9yS8g.get(n) : this.NwHqgBmOLP();
        this.pC75hPFWhX6l9fO.player.method_36456(this.Gt56Sj4a6BWhgB(vec2d));
        if (--this.FERZ92ROAgUmnlta <= 0) {
            this.ISyBYC52zF();
        }
    }

    private void ISyBYC52zF() {
        ++this.SMYZpUvCuykws2;
        if (this.SMYZpUvCuykws2 >= this.ZR5lph5QmbF4.LkopaVK1It4L.size()) {
            this.m9RUHINs8();
            return;
        }
        this.bhy0Ddon9H6();
        this.mp3zoXQFKUKYj5(State.urju0X, "aligning for leg " + this.SMYZpUvCuykws2 + ": " + this.YCvJj8imMAxxu().Z8PfWilTZRV().name());
    }

    private void bhy0Ddon9H6() {
        this.rsx7hBWYw.Gt56Sj4a6BWhgB(true);
    }

    private void OyaWN2jsET() {
        this.rsx7hBWYw.Gt56Sj4a6BWhgB(false);
    }

    private void Pg9t6rCsTkuc() {
        WorldUtils.Vec2d vec2d = this.Tr234Br();
        if (this.LR7hJDRhkI != null) {
            double d = HighwayNetwork.jOdDDFXSeWl4(vec2d, this.LR7hJDRhkI);
            this.stuckTicks = d < 0.5 ? ++this.stuckTicks : 0;
        }
        this.LR7hJDRhkI = vec2d;
    }

    private HighwayRouter.Leg YCvJj8imMAxxu() {
        return this.ZR5lph5QmbF4.LkopaVK1It4L.get(this.SMYZpUvCuykws2);
    }

    private WorldUtils.Vec2d NwHqgBmOLP() {
        return this.ZR5lph5QmbF4.UsO18QwQES9yS8g.get(this.SMYZpUvCuykws2 + 1);
    }

    private String aP5dDWz() {
        WorldUtils.Vec2d vec2d = this.NwHqgBmOLP();
        return String.format("(%.0f, %.0f)", vec2d.mcAmeo(), vec2d.ckqstPn4Gd());
    }

    private double jOdDDFXSeWl4(WorldUtils.Vec2d vec2d) {
        WorldUtils.Vec2d vec2d2 = this.Tr234Br();
        return HighwayNetwork.jOdDDFXSeWl4(vec2d2, vec2d);
    }

    private WorldUtils.Vec2d Tr234Br() {
        return new WorldUtils.Vec2d(this.pC75hPFWhX6l9fO.player.getX(), this.pC75hPFWhX6l9fO.player.getZ());
    }

    private BlockPos mp3zoXQFKUKYj5(WorldUtils.Vec2d vec2d) {
        return new BlockPos((int)vec2d.mcAmeo(), (int)VersionHelper.get().getPlayerPos().method_10214(), (int)vec2d.ckqstPn4Gd());
    }

    private float Gt56Sj4a6BWhgB(WorldUtils.Vec2d vec2d) {
        double d = vec2d.mcAmeo() - this.pC75hPFWhX6l9fO.player.getX();
        double d2 = vec2d.ckqstPn4Gd() - this.pC75hPFWhX6l9fO.player.getZ();
        return (float)Math.toDegrees(Math.atan2(-d, d2));
    }

    private float jOdDDFXSeWl4(float f, float f2) {
        float f3 = (f - f2) % 360.0f;
        if (f3 > 180.0f) {
            f3 -= 360.0f;
        }
        if (f3 < -180.0f) {
            f3 += 360.0f;
        }
        return f3;
    }

    private void mp3zoXQFKUKYj5(State state, String string) {
        this.Tlldfou = state;
        this.BJiJWZC.jOdDDFXSeWl4(state, string);
    }

    private void m9RUHINs8() {
        this.OyaWN2jsET();
        this.Tlldfou = State.Pmh3HuqB53i0Y;
        this.BJiJWZC.o6zpkIIG9();
    }

    public static final class State
    extends Enum<State> {
        public static final /* enum */ State urju0X = new State();
        public static final /* enum */ State GLaGIbduHdrl = new State();
        public static final /* enum */ State o1Qz0II6HvwyAED = new State();
        public static final /* enum */ State gANxWblT = new State();
        public static final /* enum */ State PlefynG = new State();
        public static final /* enum */ State Pmh3HuqB53i0Y = new State();
        public static final /* enum */ State eNsdDMk8mJXTb = new State();
        private static final /* synthetic */ State[] Z6nxChaWC9ymwoio;

        public static State[] values() {
            return (State[])Z6nxChaWC9ymwoio.clone();
        }

        public static State valueOf(String string) {
            return Enum.valueOf(State.class, string);
        }

        private static /* synthetic */ State[] gJZa6Zx1Rzm() {
            return new State[]{urju0X, GLaGIbduHdrl, o1Qz0II6HvwyAED, gANxWblT, PlefynG, Pmh3HuqB53i0Y, eNsdDMk8mJXTb};
        }

        static {
            Z6nxChaWC9ymwoio = State.gJZa6Zx1Rzm();
        }
    }

    public static final class Config
    extends Record {
        private final double IHeihwsO8p;
        private final double PoixtDMvQM;
        private final int txFOGrboKBXQp;
        private final float HP7CUOuiyLUHkEkD;
        private final int EyGoWQcn;
        private final int wkzyCzfggXudWEsa;

        public Config(double d, double d2, int n, float f, int n2, int n3) {
            this.IHeihwsO8p = d;
            this.PoixtDMvQM = d2;
            this.txFOGrboKBXQp = n;
            this.HP7CUOuiyLUHkEkD = f;
            this.EyGoWQcn = n2;
            this.wkzyCzfggXudWEsa = n3;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Config.class, "approachRadius;arriveRadius;alignTimeoutTicks;yawToleranceDeg;transitionGoalDist;settleTicks", "IHeihwsO8p", "PoixtDMvQM", "txFOGrboKBXQp", "HP7CUOuiyLUHkEkD", "EyGoWQcn", "wkzyCzfggXudWEsa"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Config.class, "approachRadius;arriveRadius;alignTimeoutTicks;yawToleranceDeg;transitionGoalDist;settleTicks", "IHeihwsO8p", "PoixtDMvQM", "txFOGrboKBXQp", "HP7CUOuiyLUHkEkD", "EyGoWQcn", "wkzyCzfggXudWEsa"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Config.class, "approachRadius;arriveRadius;alignTimeoutTicks;yawToleranceDeg;transitionGoalDist;settleTicks", "IHeihwsO8p", "PoixtDMvQM", "txFOGrboKBXQp", "HP7CUOuiyLUHkEkD", "EyGoWQcn", "wkzyCzfggXudWEsa"}, this, object);
        }

        public double pyVwRgYkI() {
            return this.IHeihwsO8p;
        }

        public double X6N4Qf2Uc() {
            return this.PoixtDMvQM;
        }

        public int xs9d08DSpSt() {
            return this.txFOGrboKBXQp;
        }

        public float JxUbzYJNdp9UvTN() {
            return this.HP7CUOuiyLUHkEkD;
        }

        public int dzkD9N() {
            return this.wkzyCzfggXudWEsa;
        }
    }

    public static interface BounceController {
        public void Gt56Sj4a6BWhgB(boolean var1);
    }

    public static interface Listener {
        public void jOdDDFXSeWl4(State var1, String var2);

        public void o6zpkIIG9();
    }
}

