// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.CUfICea7s)
package musheor.utils;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import meteordevelopment.meteorclient.settings.BlockSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWalk;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import musheor.compat.VersionHelper;
import musheor.modules.automation.GatherItem;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.tech.MessageInteract;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Broad player/movement helper for the highway system: reach checks, camera and
 * strafe control, module-setting mutation, direction facing, and the ".tp"
 * teleport-request message. (Distinct from Meteor's own {@code PlayerUtils}, which
 * is fully-qualified where used below.)
 */
public class PlayerUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: Q90GLXQ0Pef (field)

    /** True if (x,y,z) is within the player's block-interaction range. */
    public static boolean isWithinReach(double x, double y, double z) { // was: FvaNWO(double,double,double)
        double reach = mc.player.getBlockInteractionRange();
        return meteordevelopment.meteorclient.utils.player.PlayerUtils.squaredDistance(
            mc.player.getX(), mc.player.getEyeY(), mc.player.getZ(), x, y, z) <= reach * reach;
    }

    public static boolean isWithinReach(BlockPos blockPos) { // was: FvaNWO(BlockPos)
        return isWithinReach(blockPos.toCenterPos().getX(), blockPos.toCenterPos().getY(), blockPos.toCenterPos().getZ());
    }

    public static void cancelPathing() { // was: FvaNWO() (void)
        PathingHelper.cancelEverything();
    }

    /** Sends "[Musheor]: <reason>" to the server as a chat message. */
    public static void sendChatMessage(String reason) { // was: FvaNWO(String)
        assert mc.player != null;
        MutableText text = Text.literal("[Musheor]: " + reason);
        // Original: player.networkHandler.method_52781(new class_2661(text)) — outgoing chat.
        mc.player.networkHandler.sendChatMessage(text.getString());
    }

    /** Toggles Meteor's AutoWalk to match {@code active}, only in AUTOWALK highway mode. */
    public static void setAutoWalk(boolean active) { // was: FvaNWO(boolean)
        if (HighwayBuilder.getMode() == HighwayBuilder.Mode.AUTO) {
            Module autoWalk = Modules.get().get(AutoWalk.class);
            if (active && !autoWalk.isActive()) autoWalk.toggle();
            if (!active && autoWalk.isActive()) autoWalk.toggle();
        }
    }

    public static void setSneak(boolean flag) { // was: Q90GLXQ0Pef(boolean)
        mc.options.sneakKey.setPressed(flag);
    }

    /** Disables helper modules then toggles the HighwayBuilder module off. */
    public static void toggleHighwayBuilder() { // was: Q90GLXQ0Pef() (void)
        HighwayBuilder.disableHelperModules();
        Modules.get().get(HighwayBuilder.class).toggle();
    }

    /** Enables Meteor FreeLook in camera mode looking slightly down (for overview). */
    public static void enableFreeLookCamera() { // was: psJq59YIbp3Z()
        if (mc.player != null && mc.world != null) {
            setModuleSetting(FreeLook.class, "mode", FreeLook.Mode.Camera);
            setModuleSetting(FreeLook.class, "camera-sensitivity", 8.0);
            setModuleSetting(FreeLook.class, "arrows-control-opposite", false);
            mc.player.setPitch(15.0F);
            ((FreeLook) Modules.get().get(FreeLook.class)).toggle();
        }
    }

    /** Generic setter for a module setting of any supported type. */
    public static <M extends Module, T> void setModuleSetting(Class<M> module, String setting, T value) { // was: FvaNWO(Class,String,T)
        if (Modules.get().get(module) == null) {
            ChatUtils.warning("Module %d not found", new Object[]{module});
            return;
        }
        Setting<?> s = Modules.get().get(module).settings.get(setting);
        if (s == null) {
            ChatUtils.warning("Setting %d not found", new Object[]{setting});
            return;
        }
        try {
            switch (value) {
                case Integer integer -> ((IntSetting) s).set(integer);
                case Double d       -> ((DoubleSetting) s).set(d);
                case String string  -> ((StringSetting) s).set(string);
                case Boolean bool   -> ((BoolSetting) s).set(bool);
                case Item item      -> ((ItemSetting) s).set(item);
                case Block block    -> ((BlockSetting) s).set(block);
                case Enum<?> es     -> ((EnumSetting) s).set(es);
                case null, default  -> { }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Faces the current highway direction with a 30° downward pitch. */
    public static void faceHighwayDirectionPitchDown() { // was: SOYyh5IPg26f7F()
        assert mc.player != null;
        mc.player.setPitch(30.0F);
        WorldUtils.Direction8 direction = HighwayBuilder.getDirection();
        mc.player.setYaw(WorldUtils.Direction8.toYaw(direction));
    }

    /** Faces the current highway direction (yaw only). */
    public static void faceHighwayDirection() { // was: rKbT3Ifwo()
        assert mc.player != null;
        WorldUtils.Direction8 direction = HighwayBuilder.getDirection();
        mc.player.setYaw(WorldUtils.Direction8.toYaw(direction));
    }

    /** Enables the GatherItem module to collect the given item. */
    public static void gatherItem(Item item, boolean paveAfterwards) { // was: FvaNWO(Item,boolean)
        setModuleSetting(GatherItem.class, "item", item);
        Module gatherItem = Modules.get().get(GatherItem.class);
        if (!isGatheringItem()) gatherItem.toggle();
    }

    public static void unusedPaveHook(boolean paveAfter) { } // was: psJq59YIbp3Z(boolean) (empty)

    public static void setKeyPressed(KeyBinding key, boolean pressed) { // was: FvaNWO(KeyBinding,boolean)
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    public static boolean isGatheringItem() { // was: r7hOYIKN2()
        return ((GatherItem) Modules.get().get(GatherItem.class)).isActive();
    }

    /** Presses A/D to keep the player centered on the highway's build line. */
    public static void strafeToCenterline() { // was: oZHMlTL()
        if (mc.player == null || mc.world == null) return;
        HighwayState state = HighwayState.getInstance();
        boolean pressD = false, pressA = false;
        WorldUtils.Direction8 dir = state.getDirection();
        if (dir == WorldUtils.Direction8.NORTH || dir == WorldUtils.Direction8.SOUTH) {
            double currentX = VersionHelper.get().getPlayerPos().getX();
            if (dir == WorldUtils.Direction8.NORTH) {
                if (currentX > state.getLastX() + 0.15) pressA = true;
                else if (currentX < state.getLastX() - 0.15) pressD = true;
            } else if (currentX < state.getLastX() - 0.15) pressA = true;
            else if (currentX > state.getLastX() + 0.15) pressD = true;
        } else if (dir == WorldUtils.Direction8.EAST || dir == WorldUtils.Direction8.WEST) {
            double currentZ = VersionHelper.get().getPlayerPos().getZ();
            if (dir == WorldUtils.Direction8.EAST) {
                if (currentZ > state.getLastZ() + 0.15) pressA = true;
                else if (currentZ < state.getLastZ() - 0.15) pressD = true;
            } else if (currentZ < state.getLastZ() - 0.15) pressA = true;
            else if (currentZ > state.getLastZ() + 0.15) pressD = true;
        }
        setKeyPressed(mc.options.rightKey, pressD);
        setKeyPressed(mc.options.leftKey, pressA);
    }

    /** If off-axis, paths back onto the highway centre block. */
    public static void alignToHighway() { // was: xQr5FhbwpQPWgIQ()
        HighwayState state = HighwayState.getInstance();
        if (state.getPendingBreakPos() != null) return;
        assert mc.player != null;
        if (state.getCenterX() != null && state.getCenterY() != null && state.getCenterZ() != null) {
            int ax = state.getCenterX(), ay = state.getCenterY(), az = state.getCenterZ();
            int ddx = mc.player.getBlockX() - ax;
            int ddz = mc.player.getBlockZ() - az;
            if (Math.abs(ddx) != Math.abs(ddz)) {
                PathingHelper.setGoal(new GoalBlock(ax, ay, az));
                MusheorSystem.debug("Aligning player to the highway");
            }
        }
    }

    /** Corrects lateral drift off the highway line using a Baritone goal. */
    public static void correctDrift() { // was: OMMZL1F3q()
        assert mc.player != null;
        HighwayState state = HighwayState.getInstance();
        if (mc.player.getBlockZ() != 0
            && (WorldUtils.getMovementDirection() == WorldUtils.Direction8.EAST
             || WorldUtils.getMovementDirection() == WorldUtils.Direction8.WEST)) {
            double difference = mc.player.getZ() - state.getLastZ();
            if (difference > 0.6) {
                Goal goal = new GoalBlock(mc.player.getBlockX(), state.getCenterY(), state.getLastZ().intValue());
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
                MusheorSystem.debug("Aligning player to the highway");
                return;
            }
        }
        if (mc.player.getBlockX() != 0
            && (WorldUtils.getMovementDirection() == WorldUtils.Direction8.NORTH
             || WorldUtils.getMovementDirection() == WorldUtils.Direction8.SOUTH)) {
            double difference = mc.player.getX() - state.getLastX();
            if (difference > 0.6) {
                Goal goal = new GoalXZ(state.getLastX().intValue(), mc.player.getBlockZ());
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
                MusheorSystem.debug("Aligning player to the highway");
                return;
            }
        }
    }

    /** Sends a "!tp <token>" teleport-request message to another player (used by .tp). */
    public static void sendTeleportMessage(String playerIGN, int length) { // was: FvaNWO(String,int)
        String command = "!tp " + MessageInteract.randomToken(length);
        if (!VersionHelper.get().onSameServer(playerIGN)) {
            ChatUtils.error("Cannot find %s, not online?!", new Object[]{playerIGN});
            return;
        }
        try {
            // Optional paid "plus" module: send via IRC PM if present.
            Class.forName("musheor.plus.PlusPlayerUtils")
                .getMethod("trySendIrcPm", String.class, String.class)
                .invoke(null, playerIGN, command);
        } catch (Exception ignored) {
            // Fallback: normal /msg command.
            mc.player.networkHandler.sendPacket(new CommandExecutionC2SPacket("msg " + playerIGN + " " + command));
        }
    }
}
