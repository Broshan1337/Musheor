// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.tech;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.modules.automation.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PlacementEngine;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * "auto-portal" — automatically builds and lights a nether portal in front of the player.
 * Computes the frame/interior positions from the player's facing and pitch, places the
 * obsidian frame (rate-limited via {@link PlacementEngine}), ignites it with flint & steel,
 * then verifies the portal formed. Optionally pauses on movement input and self-disables.
 */
public class AutoPortal extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: FvaNWO
    private static final SettingColor BLOCKED_LINE_COLOR = new SettingColor(255, 45, 45, 220); // was: Q90GLXQ0Pef
    private static final SettingColor BLOCKED_SIDE_COLOR = new SettingColor(255, 45, 45, 60);  // was: psJq59YIbp3Z

    private final Setting<Boolean> autoDisable = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("auto-disable").description("Automatically disables the module when a portal is built").defaultValue(true).build());
    private final Setting<Boolean> pauseOnInput = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: rKbT3Ifwo
        .name("pause-on-input").description("Pauses the building process when you are moving, sneaking or jumping").defaultValue(true).build());
    private final Setting<Boolean> renderPortal = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("render-portal").description("Renders the portal's position").defaultValue(true).build());
    private final Setting<SettingColor> renderColor = this.settings.getDefaultGroup().add(new ColorSetting.Builder() // was: oZHMlTL
        .name("render-color").defaultValue(new SettingColor(0, 225, 255, 200)).description("Custom color for rendering (lines / wireframe)").visible(renderPortal::get).build());

    private boolean building;                            // was: xQr5FhbwpQPWgIQ
    private Phase phase;                                 // was: OMMZL1F3q
    private final List<BlockPos> frameBlocks = new ArrayList<>();    // was: zu3a44xDeMFMCRwm
    private final List<BlockPos> interiorBlocks = new ArrayList<>(); // was: krxNb5lcQuWA
    private BlockPos lightPos;                           // was: nt0HZnvBBp
    private int swapDelayTicks;                          // was: amz3UB1vE
    private int tickCounter;                             // was: sBBIyQG5NWq0K

    public AutoPortal() {
        super(musheor.MAIN, "auto-portal", "Automatically builds and lights a nether portal");
    }

    @Override
    public void onActivate() {
        this.building = false;
        this.phase = Phase.PLACING;
        this.frameBlocks.clear();
        this.interiorBlocks.clear();
        this.lightPos = null;
        this.swapDelayTicks = 0;
        this.tickCounter = 0;
        HighwayState.getInstance().getBlockBreakAttempts().clear();
    }

    @Override
    public void onDeactivate() {
        this.frameBlocks.clear();
        this.interiorBlocks.clear();
        this.lightPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!this.building) this.computePortalPositions();

        HighwayState.getInstance().getBlockBreakAttempts().entrySet().removeIf(entry -> {
            BlockState targetState = mc.world.getBlockState(entry.getKey());
            return targetState != null && mc.world.getBlockState(entry.getKey()).getBlock() == Blocks.OBSIDIAN
                || this.tickCounter - entry.getValue() > MusheorSystem.Manager.placementTimeout.get();
        });
        if (this.swapDelayTicks > 0) this.swapDelayTicks--;

        if (this.pauseOnInput.get() && this.hasMovementInput()) return;

        if (!this.building) {
            int obsidianCount = InventoryManager.countItemIncludingShulkers(Items.OBSIDIAN);
            if (obsidianCount < 10) {
                this.error("Not enough obsidian (need %d, have %d), disabling.", 10, obsidianCount);
                this.toggle();
                return;
            }
            if (!InvUtils.find(stack -> stack.getItem() == Items.FLINT_AND_STEEL).found()) {
                this.error("No flint and steel found in inventory, disabling.");
                this.toggle();
                return;
            }
            this.building = true;
            this.phase = Phase.PLACING;
            this.info("Position locked, building portal...");
        }

        switch (this.phase) {
            case PLACING -> this.placeFrame(this.tickCounter++);
            case LIGHTING -> this.ignitePortal();
            case VERIFYING -> this.verifyPortal();
        }
    }

    /** Places the obsidian frame blocks, one rate-limited batch per tick. */
    private void placeFrame(int currentTick) { // was: FvaNWO(int)
        boolean anyPlaceable = false;
        for (BlockPos pos : this.frameBlocks) {
            if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos).isReplaceable() && WorldUtils.isWithinPlacementRange(pos)) {
                anyPlaceable = true;
                break;
            }
        }

        if (!anyPlaceable) {
            if (this.isFrameComplete()) {
                this.phase = Phase.LIGHTING;
                this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
            }
        } else if (this.swapDelayTicks <= 0) {
            FindItemResult inHotbar = InvUtils.findInHotbar(stack -> stack.getItem() == Items.OBSIDIAN);
            if (!inHotbar.found()) {
                InventoryManager.moveToHotbar(Items.OBSIDIAN);
                this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
            } else {
                InventoryManager.selectItem(Items.OBSIDIAN);
                for (BlockPos pos : this.frameBlocks) {
                    if (!RateController.canSendActionPacket()) break;
                    if (!HighwayState.getInstance().getBlockBreakAttempts().containsKey(pos)
                        && mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN
                        && mc.world.getBlockState(pos).isReplaceable()
                        && WorldUtils.isWithinPlacementRange(pos)) {
                        HighwayState.getInstance().getBlockBreakAttempts().put(pos, currentTick);
                        PlacementEngine.placeBlock(pos, Direction.UP);
                    }
                }
            }
        }
    }

    /** Right-clicks the ignition block with flint & steel. */
    private void ignitePortal() { // was: FvaNWO()
        if (this.lightPos == null || !WorldUtils.isWithinPlacementRange(this.lightPos)) return;
        if (this.swapDelayTicks > 0 || !RateController.canSendActionPacket()) return;
        FindItemResult inHotbar = InvUtils.findInHotbar(stack -> stack.getItem() == Items.FLINT_AND_STEEL);
        if (inHotbar.found()) {
            InvUtils.swap(inHotbar.slot(), false);
            BlockPos lp = this.lightPos;
            var hit = new net.minecraft.util.hit.BlockHitResult(Vec3d.ofCenter(lp).add(0.0, 0.5, 0.0), Direction.UP, lp, false);
            mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, sequence));
            this.phase = Phase.VERIFYING;
        } else if (!InvUtils.find(stack -> stack.getItem() == Items.FLINT_AND_STEEL).found()) {
            this.error("Lost flint and steel during build, disabling.");
            this.toggle();
        } else {
            InventoryManager.moveToHotbar(Items.FLINT_AND_STEEL);
            this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
        }
    }

    /** Confirms the portal lit; otherwise falls back to lighting again. */
    private void verifyPortal() { // was: Q90GLXQ0Pef()
        if (this.isPortalLit()) {
            this.info("Portal built successfully!");
            if (this.autoDisable.get()) this.toggle();
        } else {
            this.phase = Phase.LIGHTING;
        }
    }

    /** True if every frame position is obsidian. */
    private boolean isFrameComplete() { // was: psJq59YIbp3Z()
        for (BlockPos pos : this.frameBlocks) {
            if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN) return false;
        }
        return true;
    }

    /** True if any interior position is a nether portal block. */
    private boolean isPortalLit() { // was: SOYyh5IPg26f7F()
        for (BlockPos pos : this.interiorBlocks) {
            if (mc.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL) return true;
        }
        return false;
    }

    /** True if any movement key is currently pressed. */
    private boolean hasMovementInput() { // was: rKbT3Ifwo()
        return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed()
            || mc.options.rightKey.isPressed() || mc.options.jumpKey.isPressed() || mc.options.sneakKey.isPressed();
    }

    /** The cardinal facing nearest to the given yaw. */
    private Direction yawToFacing(float yaw) { // was: FvaNWO(float)
        float n = (yaw % 360.0F + 360.0F) % 360.0F;
        if (n >= 315.0F || n < 45.0F) return Direction.SOUTH;
        if (n < 135.0F) return Direction.WEST;
        return n < 225.0F ? Direction.NORTH : Direction.EAST;
    }

    /** Converts a pitch into a vertical build offset in [-5, 5]. */
    private int pitchToYOffset(float pitch) { // was: Q90GLXQ0Pef(float)
        float clamped = Math.max(-45.0F, Math.min(45.0F, pitch));
        int step = Math.round(-clamped / 9.0F);
        return Math.max(-5, Math.min(5, step));
    }

    /** Computes the frame/interior positions and ignition block from the player's facing and pitch. */
    private void computePortalPositions() { // was: r7hOYIKN2()
        this.frameBlocks.clear();
        this.interiorBlocks.clear();
        this.lightPos = null;
        BlockPos foot = mc.player.getBlockPos();
        Direction facing = this.yawToFacing(mc.player.getYaw());
        int yOffset = this.pitchToYOffset(mc.player.getPitch());
        int fx = facing.getOffsetX();
        int fz = facing.getOffsetZ();
        int rx = -fz;
        int rz = fx;
        int planeX = foot.getX() + fx * 2;
        int planeZ = foot.getZ() + fz * 2;
        int baseY = foot.getY() + yOffset;

        for (int h = 0; h < 5; h++) {
            for (int w = -1; w <= 2; w++) {
                BlockPos pos = new BlockPos(planeX + rx * w, baseY + h, planeZ + rz * w);
                boolean isCorner = (h == 0 || h == 4) && (w == -1 || w == 2);
                boolean isInterior = h >= 1 && h <= 3 && w >= 0 && w <= 1;
                if (!isCorner) {
                    if (isInterior) this.interiorBlocks.add(pos);
                    else this.frameBlocks.add(pos);
                }
            }
        }

        for (BlockPos pos : this.frameBlocks) {
            if (pos.getY() == baseY) {
                this.lightPos = pos;
                break;
            }
        }
    }

    /** True if the frame/interior is obstructed by non-replaceable, non-portal blocks. */
    private boolean isObstructed() { // was: oZHMlTL()
        for (BlockPos pos : this.frameBlocks) {
            BlockState state = mc.world.getBlockState(pos);
            if (state.getBlock() != Blocks.OBSIDIAN && !state.isReplaceable()) return true;
        }
        for (BlockPos pos : this.interiorBlocks) {
            BlockState state = mc.world.getBlockState(pos);
            if (state.getBlock() != Blocks.NETHER_PORTAL && state.getBlock() != Blocks.FIRE && !state.isReplaceable()) return true;
        }
        return false;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (mc.player == null || mc.world == null || !this.renderPortal.get()) return;
        if (this.frameBlocks.isEmpty() && this.interiorBlocks.isEmpty()) return;
        boolean blocked = this.isObstructed();
        SettingColor lineColor = blocked ? BLOCKED_LINE_COLOR : this.renderColor.get();
        SettingColor sideColor = blocked ? BLOCKED_SIDE_COLOR : this.renderColor.get();
        List<BlockPos> all = new ArrayList<>(this.frameBlocks.size() + this.interiorBlocks.size());
        all.addAll(this.frameBlocks);
        all.addAll(this.interiorBlocks);
        RenderUtils.render(event, all, lineColor, sideColor, ShapeMode.Lines);
    }

    /** Portal-build phase. */ // was: enum Phase {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    private enum Phase { PLACING, LIGHTING, VERIFYING }
}
