// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import musheor.utils.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.RateController;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class GrimScaffold
extends Module {
    private final SettingGroup sgGeneral;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final Setting<Boolean> instantPlace;
    private final Setting<List<Block>> allowBlocklist;
    private final Setting<Boolean> expand;
    private final Setting<Double> expandRange;
    private final Setting<Boolean> expandOnlyForward;
    private final Setting<Boolean> render;
    private final List<BlockPos> placementTargets;
    private Block scaffoldBlock;

    public GrimScaffold() {
        super(musheor.MAIN, "grim-scaffold", "Place blocks under your feet, even if you are in the air");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.instantPlace = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("instant-place")).description("Instantly place the block client-side before server confirms")).defaultValue((Object)true)).build());
        this.allowBlocklist = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("allowed-blocks")).description("Which blocks scaffold is allowed to place")).defaultValue(new Block[]{Blocks.OBSIDIAN, Blocks.NETHERRACK}).build());
        this.expand = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("expand")).description("Extend the scaffold area beyond directly under your feet")).defaultValue((Object)false)).build());
        this.expandRange = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("expand-range")).description("How many blocks to expand the scaffold area")).defaultValue(2.0).sliderRange(1.0, 5.0).decimalPlaces(1).visible(() -> this.expand.get())).build());
        this.expandOnlyForward = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("expand-forward-only")).description("Only expand in the direction you are currently facing")).defaultValue((Object)true)).visible(() -> this.expand.get())).build());
        this.render = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where blocks will be placed")).defaultValue((Object)true)).build());
        this.placementTargets = new ArrayList<BlockPos>();
        this.scaffoldBlock = null;
    }

    public void onActivate() {
        this.placementTargets.clear();
        this.scaffoldBlock = null;
    }

    public void onDeactivate() {
        this.placementTargets.clear();
        this.scaffoldBlock = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (GrimScaffold.mc.player == null || GrimScaffold.mc.world == null) {
            return;
        }
        this.scaffoldBlock = this.findScaffoldBlock();
        this.placementTargets.clear();
        List<BlockPos> list = this.getPlacementPositions();
        list.removeIf(pos -> !BlockUtils.canPlace((BlockPos)pos, (boolean)true));
        if (((Boolean)this.render.get()).booleanValue()) {
            this.placementTargets.addAll(list);
        }
        if (this.scaffoldBlock == null || list.isEmpty()) {
            return;
        }
        Item mainHandItem = GrimScaffold.mc.player.getMainHandStack().getItem();
        if (!(mainHandItem instanceof BlockItem) || ((BlockItem)mainHandItem).getBlock() != this.scaffoldBlock) {
            InventoryManager.equipItem(this.scaffoldBlock.asItem());
        }
        WorldUtils.swapCarriedItems();
        for (BlockPos blockPos : list) {
            if (!RateController.checkPlaceRate()) break;
            BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(blockPos), Direction.DOWN, blockPos, false);
            WorldUtils.sendPlacePacket(Hand.OFF_HAND, hitResult);
            if (!((Boolean)this.instantPlace.get()).booleanValue()) continue;
            GrimScaffold.mc.world.setBlockState(blockPos, this.scaffoldBlock.getDefaultState(), 3);
        }
        WorldUtils.swapCarriedItems();
    }

    private Block findScaffoldBlock() {
        ItemStack mainStack = GrimScaffold.mc.player.getMainHandStack();
        Item mainItem = mainStack.getItem();
        if (mainItem instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)mainItem;
            if (((List<Block>)this.allowBlocklist.get()).contains(blockItem.getBlock()) && !mainStack.isEmpty()) {
                return blockItem.getBlock();
            }
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack slotStack = GrimScaffold.mc.player.getInventory().getStack(i);
            Item slotItem = slotStack.getItem();
            if (!(slotItem instanceof BlockItem)) continue;
            BlockItem slotBlockItem = (BlockItem)slotItem;
            if (!((List<Block>)this.allowBlocklist.get()).contains(slotBlockItem.getBlock()) || slotStack.isEmpty()) continue;
            return slotBlockItem.getBlock();
        }
        return null;
    }

    private List<BlockPos> getPlacementPositions() {
        ArrayList<BlockPos> arrayList;
        block6: {
            arrayList = new ArrayList<BlockPos>();
            HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
            BlockPos basePos = GrimScaffold.mc.player.getBlockPos().down();
            if (hashSet.add(basePos)) {
                arrayList.add(basePos);
            }
            if (!((Boolean)this.expand.get()).booleanValue()) break block6;
            int n = (int)Math.ceil((Double)this.expandRange.get());
            double d = (Double)this.expandRange.get() * (Double)this.expandRange.get();
            Direction facing = GrimScaffold.mc.player.getHorizontalFacing();
            if (((Boolean)this.expandOnlyForward.get()).booleanValue()) {
                for (int i = 1; i <= n; ++i) {
                    BlockPos pos = new BlockPos(basePos.getX() + facing.getOffsetX() * i, basePos.getY(), basePos.getZ() + facing.getOffsetZ() * i);
                    if (!hashSet.add(pos)) continue;
                    arrayList.add(pos);
                }
            } else {
                for (int i = -n; i <= n; ++i) {
                    for (int j = -n; j <= n; ++j) {
                        BlockPos pos2;
                        if (i == 0 && j == 0 || (double)(i * i + j * j) > d || !hashSet.add(pos2 = basePos.add(i, 0, j))) continue;
                        arrayList.add(pos2);
                    }
                }
            }
        }
        return arrayList;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        if (!((Boolean)this.render.get()).booleanValue() || this.placementTargets.isEmpty()) {
            return;
        }
        Block block = this.scaffoldBlock != null ? this.scaffoldBlock : Blocks.OBSIDIAN;
        RenderUtils.jOdDDFXSeWl4(render3DEvent, this.placementTargets, block);
    }
}