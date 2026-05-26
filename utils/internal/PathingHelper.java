// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.internal;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;

/** Thin wrapper around the Baritone API for common pathing operations. */
public class PathingHelper {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: ypiXcEk, method_1551

    private PathingHelper() {}

    /** Returns true if Baritone is currently navigating toward a goal. */
    public static boolean isAlreadyPathing() { // was: LcPVM4w5KCoKSxGs
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();
    }

    /** Cancels all active Baritone pathing and goals. */
    public static void stopPathing() { // was: xRVyNRV3cB7
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    /** Resumes Baritone pathing by executing the "resume" command. */
    public static void startPathing() { // was: gaJr0zjHBLiO
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
    }

    /** Sets a custom Goal and immediately starts pathing toward it. */
    public static void setBaritoneGoal(Goal goal) { // was: jOdDDFXSeWl4(Goal)
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
    }

    /** Sets a GoalNear and immediately starts pathing toward it. */
    public static void setGoalNear(GoalNear goalNear) { // was: jOdDDFXSeWl4(GoalNear)
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal) goalNear);
    }

    /**
     * Starts the Baritone elytra process toward (x, z) if it is not already active.
     * Returns true if pathing was started, false if it was already running.
     */
    public static boolean startElytraPath(int x, int z) { // was: UgB10d(int,int)
        if (!BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().isActive()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().pathTo((Goal) new GoalXZ(x, z));
            return true;
        }
        return false;
    }

    /** Wraps a BlockPos in a GoalBlock and starts pathing to it. */
    public static void setGoal(BlockPos pos) { // was: l92qSNnpKrYO(BlockPos)
        PathingHelper.setBaritoneGoal((Goal) new GoalBlock(pos));
    }

    /** Executes an arbitrary Baritone command string (e.g. "resume", "cancel"). */
    public static void executeBaritoneCommand(String command) { // was: J9PiTNS(String)
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
    }

    /** Paths to a BlockPos using Baritone's "goto x y z" command. */
    public static void pathToPos(BlockPos pos) { // was: S7TLszvzENsW7(BlockPos)
        PathingHelper.executeBaritoneCommand(
            String.format("goto %d %d %d",
                pos.getX(),   // getX
                pos.getY(),   // getY
                pos.getZ())); // getZ
    }
}
