// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.IBuilderProcess;
import java.lang.runtime.SwitchBootstraps;
import java.util.UUID;
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
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.MusheorSystem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;

public class PlayerUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: XrtBzLhuwKI
    private static final IBuilderProcess baritoneBuilder =                   // was: ZqICw8j
        BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess();

    /** Resumes Baritone's builder process if it is currently paused. */
    public static void resumeBaritone() { // was: ZjVmRLiAeys38
        if (baritoneBuilder.isPaused()) {
            baritoneBuilder.resume();
        }
    }

    /** Stops all Baritone pathing. */
    public static void stopBaritone() { // was: JaevRTUQKWIx5LQ
        PathingHelper.stopPathing();
    }

    /**
     * Shows a disconnect screen with "[Musheor]: " + message as the reason.
     * This is implemented by calling networkHandler.onDisconnect(), which triggers
     * the client-side disconnect UI without actually sending anything to the server.
     */
    public static void sendChatMessage(String message) { // was: J2pm2c07elEb5G(String)
        assert (PlayerUtils.mc.player != null);
        MutableText text = Text.literal("[Musheor]: " + message);
        PlayerUtils.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket((Text) text));
    }

    /** Activates or deactivates Meteor's FreeLook module to match the requested state. */
    public static void setFreeLookActive(boolean active) { // was: UgB10d(boolean)
        Module module = Modules.get().get(FreeLook.class);
        if (active != module.isActive()) {
            module.toggle();
        }
    }

    /**
     * Activates or deactivates Meteor's AutoWalk module (only when highway mode is
     * set to AutoWalk movement mode).
     */
    public static void setAutoWalkActive(boolean active) { // was: KP44bk(boolean)
        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) {
            Module module = Modules.get().get(AutoWalk.class);
            if (active  && !module.isActive()) module.toggle();
            if (!active &&  module.isActive()) module.toggle();
        }
    }

    /** Enables or disables the sneak key state. */
    public static void setSneaking(boolean sneaking) { // was: jWrhVf2psx(boolean)
        PlayerUtils.mc.options.sneakKey.setPressed(sneaking);
    }

    /** Disables the HighwayBuilder module entirely. */
    public static void disableHighwayBuilder() { // was: xynAsOKhN7t
        HighwayBuilder.cleanup();
        Modules.get().get(HighwayBuilder.class).toggle();
    }

    /** Configures FreeLook to Camera mode with pitch 15° for an overhead view, then enables it. */
    public static void enableFreeLookMode() { // was: Gd2ks78ySQq40
        if (PlayerUtils.mc.player != null && PlayerUtils.mc.world != null) {
            PlayerUtils.setModuleSetting(FreeLook.class, "mode", FreeLook.Mode.Camera);
            PlayerUtils.setModuleSetting(FreeLook.class, "camera-sensitivity", 8.0);
            PlayerUtils.setModuleSetting(FreeLook.class, "arrows-control-opposite", false);
            PlayerUtils.mc.player.setPitch(15.0f);
            PlayerUtils.setFreeLookActive(true);
        }
    }

    /**
     * Generic helper to set any Meteor Module setting by name. Handles all
     * common setting types: Integer, Double, String, Boolean, Item, Block, Enum.
     */
    public static <M extends Module, T> void setModuleSetting( // was: jOdDDFXSeWl4(Class,String,T)
            Class<M> moduleClass, String settingName, T value) {
        if (Modules.get().get(moduleClass) != null) {
            Setting<?> setting = Modules.get().get(moduleClass).settings.get(settingName);
            if (setting != null) {
                try {
                    int typeCase = 0;
                    switch (SwitchBootstraps.typeSwitch("typeSwitch",
                            new Object[]{Integer.class, Double.class, String.class, Boolean.class,
                                         Item.class, Block.class, Enum.class},
                            value, typeCase)) {
                        case 0: ((IntSetting) setting).set((Object)(Integer) value);    break;
                        case 1: ((DoubleSetting) setting).set((Object)(Double) value);  break;
                        case 2: ((StringSetting) setting).set((Object)(String) value);  break;
                        case 3: ((BoolSetting) setting).set((Object)(Boolean) value);   break;
                        case 4: ((ItemSetting) setting).set((Object)(Item) value);      break;
                        case 5: ((BlockSetting) setting).set((Object)(Block) value);    break;
                        case 6: ((EnumSetting) setting).set((Object)(Enum<?>) value);   break;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                ChatUtils.warning("Setting %d not found", new Object[]{settingName});
            }
        } else {
            ChatUtils.warning("Module %d not found", new Object[]{moduleClass});
        }
    }

    /** Sets the player's yaw/pitch to align the view with the current highway direction, pitch 30°. */
    public static void alignLookToHighway() { // was: Bocqo9ajQ
        assert (PlayerUtils.mc.player != null);
        PlayerUtils.mc.player.setPitch(30.0f);
        WorldUtils.Direction8 dir = HighwayBuilder.getDirection();
        PlayerUtils.mc.player.setYaw(WorldUtils.Direction8.toYaw(dir));
    }

    /**
     * Configures the GatherItem module to collect the given item, then enables it
     * if it is not already active.
     */
    public static void startGatherItem(Item item, boolean unused) { // was: jOdDDFXSeWl4(Item,boolean)
        PlayerUtils.setModuleSetting(GatherItem.class, "item", item);
        Module module = Modules.get().get(GatherItem.class);
        if (!PlayerUtils.isGatheringItem()) {
            module.toggle();
        }
    }

    /** No-op stub. */
    public static void noOp(boolean unused) {} // was: usJLOV0subXO3(boolean)

    /** Sets the pressed state of the given KeyBinding via Meteor's Input helper. */
    public static void setKeyState(KeyBinding key, boolean pressed) { // was: jOdDDFXSeWl4(KeyBinding,boolean)
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    /** Returns true if the GatherItem module is currently active. */
    public static boolean isGatheringItem() { // was: BT1BimvycZZjsYS
        return Modules.get().get(GatherItem.class).isActive();
    }

    /**
     * Applies strafe (left/right) key presses to keep the player centered on the
     * highway axis. Uses HighwayState's target X/Z alignment coordinates.
     */
    public static void applyStrafing() { // was: quFLaIBj1UQn6g
        if (PlayerUtils.mc.player == null || PlayerUtils.mc.world == null) return;
        HighwayState state = HighwayState.getInstance();
        boolean strafeRight = false;
        boolean strafeLeft  = false;
        // North/South highway: align on X axis
        if (state.getDirection() == WorldUtils.Direction8.NORTH
                || state.getDirection() == WorldUtils.Direction8.SOUTH) {
            double playerX = VersionHelper.get().getPlayerPos().getX();
            if (state.getDirection() == WorldUtils.Direction8.NORTH) {
                if (playerX > state.getAlignX() + 0.13) strafeLeft  = true;
                else if (playerX < state.getAlignX() - 0.13) strafeRight = true;
            } else {
                if (playerX < state.getAlignX() - 0.13) strafeLeft  = true;
                else if (playerX > state.getAlignX() + 0.13) strafeRight = true;
            }
        // East/West highway: align on Z axis
        } else if (state.getDirection() == WorldUtils.Direction8.WEST
                || state.getDirection() == WorldUtils.Direction8.EAST) {
            double playerZ = VersionHelper.get().getPlayerPos().getZ();
            if (state.getDirection() == WorldUtils.Direction8.WEST) {
                if (playerZ > state.getAlignZ() + 0.13) strafeLeft  = true;
                else if (playerZ < state.getAlignZ() - 0.13) strafeRight = true;
            } else {
                if (playerZ < state.getAlignZ() - 0.13) strafeLeft  = true;
                else if (playerZ > state.getAlignZ() + 0.13) strafeRight = true;
            }
        }
        PlayerUtils.setKeyState(PlayerUtils.mc.options.rightKey, strafeRight);
        PlayerUtils.setKeyState(PlayerUtils.mc.options.leftKey,  strafeLeft);
    }

    /**
     * Uses Baritone's GoalBlock to realign the player to the highway grid if they
     * are more than 1 block off-axis.
     */
    public static void alignWithBaritone() { // was: uFghvYncEwFBHmJL
        HighwayState state = HighwayState.getInstance();
        if (state.getLavaTargetBlock() != null) return;
        assert (PlayerUtils.mc.player != null);
        if (state.getAlignStartX() != 0 && state.getHighwayY() != 0
                && PlayerUtils.mc.player.getX() != 0
                && PlayerUtils.mc.player.getZ() != 0
                && (Math.abs(PlayerUtils.mc.player.getX()) % Math.abs(state.getAlignStartX()) > 1
                 || Math.abs(PlayerUtils.mc.player.getZ()) % Math.abs(state.getAlignStartZ()) > 1)) {
            GoalBlock goal = new GoalBlock(
                state.getAlignStartX().intValue(),
                state.getHighwayY().intValue(),
                state.getAlignStartZ().intValue());
            PathingHelper.setBaritoneGoal((Goal) goal);
            PathingHelper.startPathing();
            MusheorSystem.debug("Aligning player to the highway", new Object[0]);
        }
    }

    /**
     * Alternative alignment: if the player is more than 0.5 blocks off-axis (XZ),
     * uses Baritone's custom goal to path to the correct lane position.
     */
    public static void alignWithBaritoneXZ() { // was: HUYtvX
        assert (PlayerUtils.mc.player != null);
        HighwayState state = HighwayState.getInstance();
        double offset;
        // East/West alignment: correct Z position
        if (PlayerUtils.mc.player.getZ() != 0
                && (WorldUtils.getPlayerFacing() == WorldUtils.Direction8.WEST
                 || WorldUtils.getPlayerFacing() == WorldUtils.Direction8.EAST)
                && (offset = PlayerUtils.mc.player.getZ() - state.getAlignZ()) > 0.5) {
            GoalBlock goal = new GoalBlock(
                PlayerUtils.mc.player.getX(),
                state.getHighwayY().intValue(),
                state.getAlignZ().intValue());
            BaritoneAPI.getProvider().getPrimaryBaritone()
                .getCustomGoalProcess().setGoalAndPath((Goal) goal);
            MusheorSystem.debug("Aligning player to the highway", new Object[0]);
            return;
        }
        // North/South alignment: correct X position
        if (PlayerUtils.mc.player.getX() != 0
                && (WorldUtils.getPlayerFacing() == WorldUtils.Direction8.NORTH
                 || WorldUtils.getPlayerFacing() == WorldUtils.Direction8.SOUTH)
                && (offset = PlayerUtils.mc.player.getX() - state.getAlignX()) > 0.5) {
            GoalXZ goal = new GoalXZ(state.getAlignX().intValue(), PlayerUtils.mc.player.getZ());
            BaritoneAPI.getProvider().getPrimaryBaritone()
                .getCustomGoalProcess().setGoalAndPath((Goal) goal);
            MusheorSystem.debug("Aligning player to the highway", new Object[0]);
        }
    }

    /**
     * Sends a teleport-request command ("/msg targetPlayer !tp token") to the server.
     * Uses musheor.plus.PlusPlayerUtils via reflection for IRC-based sending if available,
     * otherwise falls back to a CommandExecutionC2SPacket for the /msg command.
     */
    public static void sendTeleportMessage(String targetPlayer, int tokenLength) { // was: jOdDDFXSeWl4(String,int)
        assert (PlayerUtils.mc.player != null);
        String token = UUID.randomUUID().toString().substring(0, tokenLength);
        String command = "!tp " + token;
        if (!VersionHelper.get().onSameServer(targetPlayer)) {
            ChatUtils.error("Cannot find %s, not online?!", new Object[]{targetPlayer});
            return;
        }
        try {
            Class.forName("musheor.plus.PlusPlayerUtils")
                .getMethod("trySendIrcPm", String.class, String.class)
                .invoke(null, targetPlayer, command);
        } catch (Exception e) {
            PlayerUtils.mc.player.networkHandler.sendPacket(
                (Packet) new CommandExecutionC2SPacket("msg " + targetPlayer + " " + command));
        }
    }
}