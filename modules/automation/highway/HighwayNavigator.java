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
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final HighwayRouter.Route route;
    private final BounceController bounceController;
    private final Listener listener;
    private final Config config;
    private State state = State.Aligning;
    private int legIndex = 0;
    private int alignTicks = 0;
    private int stuckTicks = 0;
    private int settleTicksRemaining = 0;
    private WorldUtils.Vec2d lastPos2d = null;
    private double approachDistance = Double.MAX_VALUE;

    public HighwayNavigator(HighwayRouter.Route route, Config config, BounceController bounceController, Listener listener) {
        this.route = route;
        this.config = config;
        this.bounceController = bounceController;
        this.listener = listener;
    }

    public State getState() {
        return this.state;
    }

    public void tick() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.state == State.Done || this.state == State.Stopped) {
            return;
        }
        if (this.route.legs.isEmpty()) {
            this.completeRoute();
            return;
        }
        switch (this.state.ordinal()) {
            case 0: {
                this.tickAligning();
                break;
            }
            case 1: {
                this.tickBouncing();
                break;
            }
            case 2: {
                this.tickApproaching();
                break;
            }
            case 3: {
                this.tickTransitioning();
                break;
            }
            case 4: {
                this.tickSettling();
            }
        }
    }

    public void stop() {
        this.disableBouncing();
        if (PathingHelper.isAlreadyPathing()) {
            PathingHelper.stopPathing();
        }
    }

    private void tickAligning() {
        WorldUtils.Vec2d vec2d = this.getNextWaypoint();
        float f = this.getYawToward(vec2d);
        this.mc.player.setYaw(f);
        float f2 = Math.abs(this.normalizeAngleDiff(this.mc.player.getYaw(), f));
        ++this.alignTicks;
        if (f2 <= this.config.yawToleranceDeg() || this.alignTicks >= this.config.alignTimeoutTicks()) {
            this.alignTicks = 0;
            this.approachDistance = this.distanceToWaypoint(this.getNextWaypoint());
            this.enableBouncing();
            this.setState(State.Bouncing, "bouncing toward " + this.getNextWaypointStr());
        }
    }

    private void tickBouncing() {
        WorldUtils.Vec2d vec2d = this.getNextWaypoint();
        double d = this.distanceToWaypoint(vec2d);
        if (!PathingHelper.isAlreadyPathing()) {
            this.mc.player.setYaw(this.getYawToward(vec2d));
        }
        this.updateStuckDetection();
        if (d < this.config.approachRadius() && d < this.approachDistance) {
            this.setState(State.Approaching, "approaching " + this.getNextWaypointStr());
        }
    }

    private void tickApproaching() {
        WorldUtils.Vec2d vec2d = this.getNextWaypoint();
        double d = this.distanceToWaypoint(vec2d);
        this.mc.player.setYaw(this.getYawToward(vec2d));
        if (d < this.config.arriveRadius()) {
            this.disableBouncing();
            BlockPos BlockPos2 = this.toBlockPos(vec2d);
            PathingHelper.setGoal(BlockPos2);
            this.setState(State.Transitioning, "transitioning at " + this.getNextWaypointStr());
        }
    }

    private void tickTransitioning() {
        if (!PathingHelper.isAlreadyPathing()) {
            this.settleTicksRemaining = this.config.settleTicks();
            this.setState(State.Settling, "settling at " + this.getNextWaypointStr());
        }
    }

    private void tickSettling() {
        int n = this.legIndex + 2;
        WorldUtils.Vec2d vec2d = n < this.route.waypoints.size() ? this.route.waypoints.get(n) : this.getNextWaypoint();
        this.mc.player.setYaw(this.getYawToward(vec2d));
        if (--this.settleTicksRemaining <= 0) {
            this.advanceLeg();
        }
    }

    private void advanceLeg() {
        ++this.legIndex;
        if (this.legIndex >= this.route.legs.size()) {
            this.completeRoute();
            return;
        }
        this.enableBouncing();
        this.setState(State.Aligning, "aligning for leg " + this.legIndex + ": " + this.getCurrentLeg().highway().name());
    }

    private void enableBouncing() {
        this.bounceController.setBouncing(true);
    }

    private void disableBouncing() {
        this.bounceController.setBouncing(false);
    }

    private void updateStuckDetection() {
        WorldUtils.Vec2d vec2d = this.getPlayerPos2d();
        if (this.lastPos2d != null) {
            double d = HighwayNetwork.distance(vec2d, this.lastPos2d);
            this.stuckTicks = d < 0.5 ? ++this.stuckTicks : 0;
        }
        this.lastPos2d = vec2d;
    }

    private HighwayRouter.Leg getCurrentLeg() {
        return this.route.legs.get(this.legIndex);
    }

    private WorldUtils.Vec2d getNextWaypoint() {
        return this.route.waypoints.get(this.legIndex + 1);
    }

    private String getNextWaypointStr() {
        WorldUtils.Vec2d vec2d = this.getNextWaypoint();
        return String.format("(%.0f, %.0f)", vec2d.x(), vec2d.z());
    }

    private double distanceToWaypoint(WorldUtils.Vec2d vec2d) {
        WorldUtils.Vec2d vec2d2 = this.getPlayerPos2d();
        return HighwayNetwork.distance(vec2d2, vec2d);
    }

    private WorldUtils.Vec2d getPlayerPos2d() {
        return new WorldUtils.Vec2d(this.mc.player.getX(), this.mc.player.getZ());
    }

    private BlockPos toBlockPos(WorldUtils.Vec2d vec2d) {
        return new BlockPos((int)vec2d.x(), (int)VersionHelper.get().getPlayerPos().getY(), (int)vec2d.z());
    }

    private float getYawToward(WorldUtils.Vec2d vec2d) {
        double d = vec2d.x() - this.mc.player.getX();
        double d2 = vec2d.z() - this.mc.player.getZ();
        return (float)Math.toDegrees(Math.atan2(-d, d2));
    }

    private float normalizeAngleDiff(float f, float f2) {
        float f3 = (f - f2) % 360.0f;
        if (f3 > 180.0f) {
            f3 -= 360.0f;
        }
        if (f3 < -180.0f) {
            f3 += 360.0f;
        }
        return f3;
    }

    private void setState(State state, String string) {
        this.state = state;
        this.listener.onStateChange(state, string);
    }

    private void completeRoute() {
        this.disableBouncing();
        this.state = State.Done;
        this.listener.onWaypointReached();
    }

    public static final class State
    extends Enum<State> {
        public static final /* enum */ State Aligning = new State();
        public static final /* enum */ State Bouncing = new State();
        public static final /* enum */ State Approaching = new State();
        public static final /* enum */ State Transitioning = new State();
        public static final /* enum */ State Settling = new State();
        public static final /* enum */ State Done = new State();
        public static final /* enum */ State Stopped = new State();
        private static final /* synthetic */ State[] $VALUES;

        public static State[] values() {
            return (State[])$VALUES.clone();
        }

        public static State valueOf(String string) {
            return Enum.valueOf(State.class, string);
        }

        private static /* synthetic */ State[] $init() {
            return new State[]{Aligning, Bouncing, Approaching, Transitioning, Settling, Done, Stopped};
        }

        static {
            $VALUES = State.$init();
        }
    }

    public static final class Config
    extends Record {
        private final double approachRadius;
        private final double arriveRadius;
        private final int alignTimeoutTicks;
        private final float yawToleranceDeg;
        private final int transitionGoalDist;
        private final int settleTicks;

        public Config(double d, double d2, int n, float f, int n2, int n3) {
            this.approachRadius = d;
            this.arriveRadius = d2;
            this.alignTimeoutTicks = n;
            this.yawToleranceDeg = f;
            this.transitionGoalDist = n2;
            this.settleTicks = n3;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Config.class, "approachRadius;arriveRadius;alignTimeoutTicks;yawToleranceDeg;transitionGoalDist;settleTicks", "approachRadius", "arriveRadius", "alignTimeoutTicks", "yawToleranceDeg", "transitionGoalDist", "settleTicks"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Config.class, "approachRadius;arriveRadius;alignTimeoutTicks;yawToleranceDeg;transitionGoalDist;settleTicks", "approachRadius", "arriveRadius", "alignTimeoutTicks", "yawToleranceDeg", "transitionGoalDist", "settleTicks"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Config.class, "approachRadius;arriveRadius;alignTimeoutTicks;yawToleranceDeg;transitionGoalDist;settleTicks", "approachRadius", "arriveRadius", "alignTimeoutTicks", "yawToleranceDeg", "transitionGoalDist", "settleTicks"}, this, object);
        }

        public double approachRadius() {
            return this.approachRadius;
        }

        public double arriveRadius() {
            return this.arriveRadius;
        }

        public int alignTimeoutTicks() {
            return this.alignTimeoutTicks;
        }

        public float yawToleranceDeg() {
            return this.yawToleranceDeg;
        }

        public int settleTicks() {
            return this.settleTicks;
        }
    }

    public static interface BounceController {
        public void setBouncing(boolean var1);
    }

    public static interface Listener {
        public void onStateChange(State var1, String var2);

        public void onWaypointReached();
    }
}

