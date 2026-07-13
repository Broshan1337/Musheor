// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation.highway;

import musheor.compat.VersionHelper;
import musheor.utils.WorldUtils.Coord2D;
import musheor.utils.internal.PathingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Drives the player along a {@link HighwayRouter.Route} as a state machine: align yaw
 * toward the next waypoint, bounce (elytra) toward it, approach, hand off to Baritone to
 * settle onto the highway at the transition, then advance to the next leg. Reports state
 * changes and completion through a {@link Listener}; toggles bouncing through a
 * {@link BounceController}.
 */
public class HighwayNavigator {
    private final MinecraftClient mc = MinecraftClient.getInstance(); // was: FvaNWO
    private final HighwayRouter.Route route;         // was: Q90GLXQ0Pef
    private final BounceController bounceController;  // was: psJq59YIbp3Z
    private final Listener listener;                 // was: SOYyh5IPg26f7F
    private final Config config;                     // was: rKbT3Ifwo
    private State state = State.ALIGNING;            // was: r7hOYIKN2
    private int legIndex = 0;                        // was: oZHMlTL
    private int alignTicks = 0;                      // was: xQr5FhbwpQPWgIQ
    private int stuckTicks = 0;                      // was: OMMZL1F3q
    private int settleCountdown = 0;                 // was: zu3a44xDeMFMCRwm
    private Coord2D lastPos = null;                  // was: krxNb5lcQuWA
    private double closestApproach = Double.MAX_VALUE; // was: nt0HZnvBBp

    public HighwayNavigator(HighwayRouter.Route route, Config config, BounceController bounceController, Listener listener) {
        this.route = route;
        this.config = config;
        this.bounceController = bounceController;
        this.listener = listener;
    }

    public State getState() { // was: FvaNWO()
        return this.state;
    }

    /** Advances the state machine by one tick. */
    public void tick() { // was: Q90GLXQ0Pef()
        if (this.mc.player == null || this.mc.world == null) return;
        if (this.state == State.DONE || this.state == State.FAILED) return;
        if (this.route.legs.isEmpty()) {
            this.finish();
            return;
        }
        switch (this.state) {
            case ALIGNING -> this.tickAligning();
            case BOUNCING -> this.tickBouncing();
            case APPROACHING -> this.tickApproaching();
            case TRANSITIONING -> this.tickTransitioning();
            case SETTLING -> this.tickSettling();
        }
    }

    /** Stops navigation: halts bouncing and cancels any active pathing. */
    public void stop() { // was: psJq59YIbp3Z()
        this.stopBouncing();
        if (PathingHelper.isPathing()) PathingHelper.cancelEverything();
    }

    private void tickAligning() { // was: SOYyh5IPg26f7F()
        Coord2D target = this.nextWaypoint();
        float desiredYaw = this.yawTo(target);
        this.mc.player.setYaw(desiredYaw);
        float yawDiff = Math.abs(this.yawDifference(this.mc.player.getYaw(), desiredYaw));
        this.alignTicks++;
        if (yawDiff <= this.config.yawToleranceDeg() || this.alignTicks >= this.config.alignTimeoutTicks()) {
            this.alignTicks = 0;
            this.closestApproach = this.distanceTo(this.nextWaypoint());
            this.startBouncing();
            this.transition(State.BOUNCING, "bouncing toward " + this.formatTarget());
        }
    }

    private void tickBouncing() { // was: rKbT3Ifwo()
        Coord2D target = this.nextWaypoint();
        double dist = this.distanceTo(target);
        if (!PathingHelper.isPathing()) {
            this.mc.player.setYaw(this.yawTo(target));
        }
        this.updateClosestApproach();
        if (dist < this.config.approachRadius() && dist < this.closestApproach) {
            this.transition(State.APPROACHING, "approaching " + this.formatTarget());
        }
    }

    private void tickApproaching() { // was: r7hOYIKN2()
        Coord2D target = this.nextWaypoint();
        double dist = this.distanceTo(target);
        this.mc.player.setYaw(this.yawTo(target));
        if (dist < this.config.arriveRadius()) {
            this.stopBouncing();
            BlockPos goal = this.toBlockPos(target);
            PathingHelper.gotoBlock(goal);
            this.transition(State.TRANSITIONING, "transitioning at " + this.formatTarget());
        }
    }

    private void tickTransitioning() { // was: oZHMlTL()
        if (!PathingHelper.isPathing()) {
            this.settleCountdown = this.config.settleTicks();
            this.transition(State.SETTLING, "settling at " + this.formatTarget());
        }
    }

    private void tickSettling() { // was: xQr5FhbwpQPWgIQ()
        int nextWpIdx = this.legIndex + 2;
        Coord2D faceTarget = nextWpIdx < this.route.waypoints.size() ? this.route.waypoints.get(nextWpIdx) : this.nextWaypoint();
        this.mc.player.setYaw(this.yawTo(faceTarget));
        if (--this.settleCountdown <= 0) {
            this.advanceLeg();
        }
    }

    private void advanceLeg() { // was: OMMZL1F3q()
        this.legIndex++;
        if (this.legIndex >= this.route.legs.size()) {
            this.finish();
        } else {
            this.startBouncing();
            this.transition(State.ALIGNING, "aligning for leg " + this.legIndex + ": " + this.currentLeg().highway().name());
        }
    }

    private void startBouncing() { // was: zu3a44xDeMFMCRwm()
        this.bounceController.setBouncing(true);
    }

    private void stopBouncing() { // was: krxNb5lcQuWA()
        this.bounceController.setBouncing(false);
    }

    /** Tracks the number of ticks the player has been effectively stationary. */
    private void updateClosestApproach() { // was: nt0HZnvBBp()
        Coord2D pos = this.playerPos();
        if (this.lastPos != null) {
            double moved = HighwayNetwork.distance(pos, this.lastPos);
            if (moved < 0.5) {
                this.stuckTicks++;
            } else {
                this.stuckTicks = 0;
            }
        }
        this.lastPos = pos;
    }

    private HighwayRouter.Leg currentLeg() { // was: amz3UB1vE()
        return this.route.legs.get(this.legIndex);
    }

    private Coord2D nextWaypoint() { // was: sBBIyQG5NWq0K()
        return this.route.waypoints.get(this.legIndex + 1);
    }

    private String formatTarget() { // was: sZkZ1izAy()
        Coord2D wp = this.nextWaypoint();
        return String.format("(%.0f, %.0f)", wp.x(), wp.z());
    }

    private double distanceTo(Coord2D target) { // was: FvaNWO(Coord2D)
        return HighwayNetwork.distance(this.playerPos(), target);
    }

    private Coord2D playerPos() { // was: QYKUhjp()
        return new Coord2D(this.mc.player.getX(), this.mc.player.getZ());
    }

    private BlockPos toBlockPos(Coord2D p) { // was: Q90GLXQ0Pef(Coord2D)
        return new BlockPos((int) p.x(), (int) VersionHelper.get().getPlayerPos().getY(), (int) p.z());
    }

    /** Yaw (degrees) pointing from the player toward {@code target}. */
    private float yawTo(Coord2D target) { // was: psJq59YIbp3Z(Coord2D)
        double dx = target.x() - this.mc.player.getX();
        double dz = target.z() - this.mc.player.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    /** Signed shortest angular difference between two yaws, in [-180, 180]. */
    private float yawDifference(float a, float b) { // was: FvaNWO(float,float)
        float diff = (a - b) % 360.0F;
        if (diff > 180.0F) diff -= 360.0F;
        if (diff < -180.0F) diff += 360.0F;
        return diff;
    }

    private void transition(State newState, String description) { // was: FvaNWO(State,String)
        this.state = newState;
        this.listener.onStateChange(newState, description);
    }

    private void finish() { // was: NIz4xic3Js9()
        this.stopBouncing();
        this.state = State.DONE;
        this.listener.onComplete();
    }

    /** Toggles the elytra bounce that propels the player between waypoints. */
    public interface BounceController {
        void setBouncing(boolean bouncing); // was: FvaNWO(boolean)
    }

    /**
     * Navigation tuning. {@code transitionGoalDist} is retained from the original but unused
     * by the navigator's current logic.
     */
    public record Config(double approachRadius, double arriveRadius, int alignTimeoutTicks,
                         float yawToleranceDeg, int transitionGoalDist, int settleTicks) {
    }

    /** Receives navigator state transitions and completion. */
    public interface Listener {
        void onStateChange(State state, String description); // was: FvaNWO(State,String)
        void onComplete();                                   // was: FvaNWO()
    }

    /** Navigator states. */ // was: enum State {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z, SOYyh5IPg26f7F, rKbT3Ifwo, r7hOYIKN2, oZHMlTL}
    public enum State { ALIGNING, BOUNCING, APPROACHING, TRANSITIONING, SETTLING, DONE, FAILED }
}
