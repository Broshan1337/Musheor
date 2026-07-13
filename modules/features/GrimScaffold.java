// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.modules.automation.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.RateController;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * "grim-scaffold" — places blocks under the player's feet (even while airborne) using the
 * off-hand swap trick, rate-limited to bypass Grim. Optionally expands the placed area
 * around/ahead of the player and mirrors placements client-side for instant feedback.
 */
public class GrimScaffold extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();  // was: FvaNWO
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: Q90GLXQ0Pef

    private final Setting<Boolean> instantPlace = sgGeneral.add(new BoolSetting.Builder() // was: psJq59YIbp3Z
        .name("instant-place").description("Instantly place the block client-side before server confirms").defaultValue(true).build());
    private final Setting<List<Block>> allowedBlocks = sgGeneral.add(new BlockListSetting.Builder() // was: SOYyh5IPg26f7F
        .name("allowed-blocks").description("Which blocks scaffold is allowed to place").defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN).build());
    private final Setting<Boolean> expand = sgGeneral.add(new BoolSetting.Builder() // was: rKbT3Ifwo
        .name("expand").description("Extend the scaffold area beyond directly under your feet").defaultValue(false).build());
    private final Setting<Double> expandRange = sgGeneral.add(new DoubleSetting.Builder() // was: r7hOYIKN2
        .name("expand-range").description("How many blocks to expand the scaffold area").defaultValue(2.0).sliderRange(1.0, 5.0).decimalPlaces(1).visible(expand::get).build());
    private final Setting<Boolean> expandForwardOnly = sgGeneral.add(new BoolSetting.Builder() // was: oZHMlTL
        .name("expand-forward-only").description("Only expand in the direction you are currently facing").defaultValue(true).visible(expand::get).build());
    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("render").description("Renders an overlay where blocks will be placed").defaultValue(true).build());

    private final List<BlockPos> renderPositions = new ArrayList<>(); // was: OMMZL1F3q
    private Block currentBlock = null;                                // was: zu3a44xDeMFMCRwm

    public GrimScaffold() {
        super(musheor.MAIN, "grim-scaffold", "Place blocks under your feet, even if you are in the air");
    }

    @Override
    public void onActivate() {
        this.renderPositions.clear();
        this.currentBlock = null;
    }

    @Override
    public void onDeactivate() {
        this.renderPositions.clear();
        this.currentBlock = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) { // was: FvaNWO(Post)
        if (mc.player == null || mc.world == null) return;
        this.currentBlock = this.findScaffoldBlock();
        this.renderPositions.clear();
        List<BlockPos> toPlace = this.getScaffoldPositions();
        toPlace.removeIf(pos -> !BlockUtils.canPlace(pos, true));
        if (this.render.get()) this.renderPositions.addAll(toPlace);

        if (this.currentBlock != null && !toPlace.isEmpty()) {
            if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem bi && bi.getBlock() == this.currentBlock)) {
                InventoryManager.selectItem(this.currentBlock.asItem());
            }
            WorldUtils.swapHands();
            for (BlockPos pos : toPlace) {
                if (!RateController.canSendActionPacket()) break;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.DOWN, pos, false);
                WorldUtils.sendInteract(Hand.OFF_HAND, hit);
                if (this.instantPlace.get()) mc.world.setBlockState(pos, this.currentBlock.getDefaultState(), 3);
            }
            WorldUtils.swapHands();
        }
    }

    /** The first allowed scaffold block in the hotbar (main hand preferred), or null. */
    private Block findScaffoldBlock() { // was: FvaNWO()
        ItemStack main = mc.player.getMainHandStack();
        if (main.getItem() instanceof BlockItem bi && this.allowedBlocks.get().contains(bi.getBlock()) && !main.isEmpty()) {
            return bi.getBlock();
        }
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() instanceof BlockItem bi && this.allowedBlocks.get().contains(bi.getBlock()) && !s.isEmpty()) {
                return bi.getBlock();
            }
        }
        return null;
    }

    /** The block position under the player, plus any expansion positions. */
    private List<BlockPos> getScaffoldPositions() { // was: Q90GLXQ0Pef()
        List<BlockPos> positions = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        BlockPos below = mc.player.getBlockPos().down();
        if (seen.add(below)) positions.add(below);

        if (this.expand.get()) {
            int range = (int) Math.ceil(this.expandRange.get());
            double rangeSq = this.expandRange.get() * this.expandRange.get();
            Direction facing = mc.player.getHorizontalFacing();
            if (this.expandForwardOnly.get()) {
                for (int i = 1; i <= range; i++) {
                    BlockPos p = new BlockPos(below.getX() + facing.getOffsetX() * i, below.getY(), below.getZ() + facing.getOffsetZ() * i);
                    if (seen.add(p)) positions.add(p);
                }
            } else {
                for (int x = -range; x <= range; x++) {
                    for (int z = -range; z <= range; z++) {
                        if ((x != 0 || z != 0) && !(x * x + z * z > rangeSq)) {
                            BlockPos p = below.add(x, 0, z);
                            if (seen.add(p)) positions.add(p);
                        }
                    }
                }
            }
        }
        return positions;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (this.render.get() && !this.renderPositions.isEmpty()) {
            Block b = this.currentBlock != null ? this.currentBlock : Blocks.OBSIDIAN;
            RenderUtils.render(event, this.renderPositions, b);
        }
    }
}
