// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockItem;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * "source-filler" — fills lava/water source blocks near the player with placeable
 * blocks (to clear the highway path). Whitelist/blacklist/default filler selection.
 */
public class SourceRemover extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: Q90GLXQ0Pef

    private final Setting<Boolean> onlySources = sgGeneral.add(new BoolSetting.Builder() // was: psJq59YIbp3Z
        .name("only-sources").description("Only fills source blocks, not flowing fluids").defaultValue(true).build());
    private final Setting<Boolean> fillLava = sgGeneral.add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("lava").description("Fills lava source blocks").defaultValue(true).build());
    private final Setting<Boolean> fillWater = sgGeneral.add(new BoolSetting.Builder() // was: rKbT3Ifwo
        .name("water").description("Fills water source blocks").defaultValue(false).build());
    private final Setting<Boolean> fillBubbleColumn = sgGeneral.add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("bubble-column").description("Fills bubble column source blocks").defaultValue(false).visible(fillWater::get).build());
    private final Setting<ListMode> listMode = sgGeneral.add(new EnumSetting.Builder<ListMode>() // was: oZHMlTL
        .name("list-mode").description("Selection mode.").defaultValue(ListMode.DEFAULT).build());
    private final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("whitelist").description("The allowed blocks that it will use to fill up the lava.")
        .defaultValue(Blocks.CRYING_OBSIDIAN, Blocks.COBBLESTONE, Blocks.DIRT, Blocks.BLACKSTONE, Blocks.OBSIDIAN)
        .visible(() -> listMode.get() == ListMode.WHITELIST).build());
    private final Setting<List<Block>> blacklist = sgGeneral.add(new BlockListSetting.Builder() // was: OMMZL1F3q
        .name("blacklist").description("The denied blocks that it not will use to fill up the lava.")
        .visible(() -> listMode.get() == ListMode.BLACKLIST).build());
    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("render").description("Render source blocks in specific range").defaultValue(true).build());
    private final Setting<Integer> renderRange = sgGeneral.add(new IntSetting.Builder() // was: krxNb5lcQuWA
        .name("render-range").description("Maximum range at which sourceblocks are rendered").defaultValue(16).sliderRange(1, 64).visible(render::get).build());

    private final List<BlockPos> renderPositions = new ArrayList<>(); // was: nt0HZnvBBp
    private int tickCounter;      // was: amz3UB1vE
    private int swapDelayTicks;   // was: sBBIyQG5NWq0K
    private int renderCounter = 0; // was: sZkZ1izAy
    public static boolean active;  // was: FvaNWO (static field)

    /** True while the source-remover is actively filling. */
    public static boolean isRemoving() { return active; } // was: FvaNWO()

    public SourceRemover() {
        super(musheor.AUTOMATION, "source-filler", "Fill up lava and or water source blocks");
    }

    @Override
    public void onActivate() {
        HighwayState.getInstance().getBlockBreakAttempts().clear();
        this.tickCounter = 0;
        this.renderCounter = 0;
        this.swapDelayTicks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        if (HighwayBuilder.isKillAuraAttacking()) return;
        this.tickCounter++;
        WorldUtils.pruneTimedOutPlacements(this.tickCounter, Blocks.AIR);
        if (++this.renderCounter > 5 && this.render.get()) {
            this.renderCounter = 0;
            this.updateRenderPositions();
        }

        Item bestItem = null;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = this.mc.player.getInventory().getStack(i);
            if (this.isAllowedFiller(itemStack)) bestItem = itemStack.getItem();
        }

        if (bestItem == null) {
            for (int i = 9; i < this.mc.player.getInventory().main.size(); i++) {
                ItemStack itemStack = this.mc.player.getInventory().getStack(i);
                if (this.isAllowedFiller(itemStack)) { InventoryManager.moveToHotbar(itemStack.getItem()); return; }
            }
            return;
        }

        List<BlockPos> positions = this.findSourcePositions((Double) MusheorSystem.Manager.placementRange.get());
        positions.sort(Comparator.comparingDouble(pos -> this.mc.player.squaredDistanceTo(Vec3d.of(pos))));
        boolean place = false;
        for (BlockPos pos : positions) {
            if (BlockUtils.canPlace(pos, true) && WorldUtils.isWithinPlacementRange(pos)) {
                RenderUtils.renderTickingBlock(pos, Color.PINK, Color.PINK, ShapeMode.Lines, 0, 1, false, false);
                place = true;
                break;
            }
        }

        if (this.swapDelayTicks > 0) { this.swapDelayTicks--; return; }
        if (place) {
            if (this.mc.player.getMainHandStack().getItem() != bestItem) {
                InventoryManager.selectItem(bestItem);
                this.swapDelayTicks = MusheorSystem.Manager.swapDelay.get();
                return;
            }
            WorldUtils.swapHands();
            for (BlockPos pos : positions) {
                if (BlockUtils.canPlace(pos, true) && WorldUtils.isWithinPlacementRange(pos)) {
                    if (!RateController.canSendActionPacket()) break;
                    WorldUtils.sendInteract(Hand.OFF_HAND, WorldUtils.buildHitResult(pos, Direction.DOWN));
                    HighwayState.getInstance().getBlockBreakAttempts().put(pos, this.tickCounter);
                }
            }
            WorldUtils.swapHands();
        }
    }

    /** True if the stack is an allowed filler block per the current list mode. */
    private boolean isAllowedFiller(ItemStack stack) { // was: FvaNWO(ItemStack)
        return this.listMode.get() == ListMode.BLACKLIST && !this.blacklist.get().contains(Block.getBlockFromItem(stack.getItem()))
            || this.listMode.get() == ListMode.WHITELIST && this.whitelist.get().contains(Block.getBlockFromItem(stack.getItem()))
            || this.listMode.get() == ListMode.DEFAULT && stack.getItem() instanceof BlockItem blockItem
               && blockItem.getBlock().getDefaultState().isSolidBlock(null, null);
    }

    /** Refreshes the list of nearby source blocks for rendering. */
    private void updateRenderPositions() { // was: Q90GLXQ0Pef()
        if (this.mc.world == null || this.mc.player == null) return;
        this.renderPositions.clear();
        BlockPos playerPos = this.mc.player.getBlockPos();
        int dist = this.renderRange.get();
        for (int x = -dist; x <= dist; x++) {
            for (int y = -dist; y <= dist; y++) {
                for (int z = -dist; z <= dist; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (this.mc.player.getEyePos().distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) <= dist) {
                        BlockState state = this.mc.world.getBlockState(pos);
                        FluidState fluid = state.getFluidState();
                        if (!this.onlySources.get() || fluid.isStill()) {
                            if (this.fillLava.get() && (fluid.getFluid() == Fluids.LAVA || fluid.getFluid() == Fluids.FLOWING_LAVA)) {
                                this.renderPositions.add(pos);
                            }
                            if (this.fillWater.get() && (fluid.getFluid() == Fluids.WATER || fluid.getFluid() == Fluids.FLOWING_WATER)
                                && (!(state.getBlock() instanceof BubbleColumnBlock) || this.fillBubbleColumn.get())) {
                                this.renderPositions.add(pos);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Collects source-block positions to fill within {@code radius} of the player's eyes. */
    private List<BlockPos> findSourcePositions(double radius) { // was: FvaNWO(double)
        List<BlockPos> positions = new ArrayList<>();
        Vec3d eyePos = Vec3d.ofCenter(BlockPos.ofFloored(this.mc.player.getEyePos()));
        int blockRadius = (int) Math.floor(radius);
        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int y = -blockRadius; y <= blockRadius; y++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    BlockPos pos = BlockPos.ofFloored(eyePos.x + x, eyePos.y + y, eyePos.z + z);
                    if (eyePos.distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) <= radius) {
                        BlockState blockState = this.mc.world.getBlockState(pos);
                        FluidState fluid = blockState.getFluidState();
                        if (!this.onlySources.get() || fluid.isStill()) {
                            if (this.fillWater.get() && (fluid.getFluid() == Fluids.WATER || fluid.getFluid() == Fluids.FLOWING_WATER)) {
                                if (!(blockState.getBlock() instanceof BubbleColumnBlock) || this.fillBubbleColumn.get()) positions.add(pos);
                            } else if (this.fillLava.get() && (fluid.getFluid() == Fluids.LAVA || fluid.getFluid() == Fluids.FLOWING_LAVA)) {
                                positions.add(pos);
                            }
                        }
                    }
                }
            }
        }
        return positions;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (this.render.get() && !this.renderPositions.isEmpty() && this.mc.world != null) {
            musheor.utils.RenderUtils.render(event, this.renderPositions);
        }
    }

    /** Filler-block selection mode. */ // was: enum ListMode {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum ListMode { DEFAULT, WHITELIST, BLACKLIST }
}
