// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.NKyC2E)
package musheor.utils.internal;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/** Thin wrapper around the Baritone API for the highway/collection modules. */
public class PathingHelper {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: FvaNWO (field)

    public static boolean isPathing() { // was: FvaNWO()
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();
    }

    public static boolean hasPath() { // was: Q90GLXQ0Pef()
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath();
    }

    public static void cancelEverything() { // was: psJq59YIbp3Z()
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    public static void setGoal(Goal goal) { // was: FvaNWO(Goal)
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
    }

    public static void setGoal(GoalNear goal) { // was: FvaNWO(GoalNear)
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
    }

    /** Starts elytra pathing toward an XZ target; returns false if already elytra-pathing. */
    public static boolean elytraTo(int x, int z) { // was: FvaNWO(int,int)
        if (!BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().isActive()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().pathTo(new GoalXZ(x, z));
            return true;
        }
        return false;
    }

    public static void gotoBlock(BlockPos pos) { // was: FvaNWO(BlockPos)
        setGoal(new GoalBlock(pos));
    }

    public static void runCommand(String command) { // was: FvaNWO(String)
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
    }

    /** Issues a Baritone "goto x y z" command to the given position. */
    public static void gotoCommand(BlockPos itemPos) { // was: Q90GLXQ0Pef(BlockPos)
        runCommand(String.format("goto %d %d %d", itemPos.getX(), itemPos.getY(), itemPos.getZ()));
    }
}
