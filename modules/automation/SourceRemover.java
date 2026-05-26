// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.modules.automation.HighwayBuilder;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.util.Hand;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SourceRemover
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> fillLava;
    private final Setting<Boolean> fillWater;
    private final Setting<ListMode> listMode;
    private final Setting<List<Block>> whitelist;
    private final Setting<List<Block>> blacklist;
    private final Setting<Boolean> allowRender;
    private final Setting<Integer> renderRange;
    private final List<BlockPos> renderPositions; // was: VGuQlVJMpGYGr
    private int tickCounter;                       // was: vV6cbpE7KWBI
    private int renderUpdateTick;                  // was: FIc3lket
    public static boolean isActive;               // was: NvJtheFonk

    public static boolean isActive() { // was: fXEQFU()
        return isActive;
    }

    public SourceRemover() {
        super(musheor.AUTOMATION, "source-filler", "Fill up lava and or water source blocks");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.fillLava = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lava")).description("Fills lava source blocks")).defaultValue((Object)true)).build());
        this.fillWater = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("water")).description("Fills water source blocks")).defaultValue((Object)true)).build());
        this.listMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("list-mode")).description("Selection mode.")).defaultValue((Object)ListMode.None)).build());
        this.whitelist = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("The allowed blocks that it will use to fill up the lava.")).defaultValue(new Block[]{Blocks.NETHERRACK, Blocks.COBBLESTONE, Blocks.DIRT, Blocks.BLACKSTONE, Blocks.OBSIDIAN}).visible(() -> this.listMode.get() == ListMode.Whitelist)).build());
        this.blacklist = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blacklist")).description("The denied blocks that it not will use to fill up the lava.")).visible(() -> this.listMode.get() == ListMode.Blacklist)).build());
        this.allowRender = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Render source blocks in specific range")).defaultValue((Object)true)).build());
        this.renderRange = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-range")).description("Maximum range at which sourceblocks are rendered")).defaultValue((Object)16)).sliderRange(1, 64).visible(() -> this.allowRender.get())).build());
        this.renderPositions = new ArrayList<BlockPos>();
        this.renderUpdateTick = 0;
    }

    public void onActivate() {
        HighwayState.getInstance().getBlockBreakAttempts().clear();
        this.tickCounter = 0;
        this.renderUpdateTick = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (WorldUtils.checkForLag()) {
            return;
        }
        if (HighwayBuilder.isEchestFarming()) { // was: zl2vxyh
            return;
        }
        ++this.tickCounter;
        WorldUtils.cleanPlacementCache(this.tickCounter, Blocks.LAVA);
        if (++this.renderUpdateTick > 5 && ((Boolean)this.allowRender.get()).booleanValue()) {
            this.renderUpdateTick = 0;
            this.updateRenderList();
        }
        ItemStack fillStack = null;
        for (int i = 0; i < 9; ++i) {
            ItemStack check;
            BlockItem blockItem;
            ItemStack slotStack = this.mc.player.getInventory().getStack(i);
            if (!(this.listMode.get() == ListMode.Blacklist && !((List)this.blacklist.get()).contains(Block.getBlockFromItem(slotStack.getItem())) || this.listMode.get() == ListMode.Whitelist && ((List)this.whitelist.get()).contains(Block.getBlockFromItem(slotStack.getItem()))) && (this.listMode.get() != ListMode.None || !((check = slotStack) instanceof BlockItem) || !(blockItem = (BlockItem)check).getBlock().getDefaultState().isSolid())) continue;
            fillStack = slotStack;
        }
        if (fillStack == null) {
            return;
        }
        List<BlockPos> list = this.findSourceBlocks((Double)MusheorSystem.Manager.placementRange.get());
        list.sort(Comparator.comparingDouble(pos -> this.mc.player.squaredDistanceTo(Vec3d.ofCenter((BlockPos)pos))));
        boolean bl = false;
        for (BlockPos pos : list) {
            if (!BlockUtils.canPlace((BlockPos)pos, (boolean)true) || !WorldUtils.isInPlacementRange(pos)) continue;
            meteordevelopment.meteorclient.utils.render.RenderUtils.renderTickingBlock((BlockPos)pos, (Color)Color.PINK, (Color)Color.PINK, (ShapeMode)ShapeMode.Lines, (int)0, (int)1, (boolean)false, (boolean)false);
            bl = true;
            break;
        }
        if (bl) {
            if (this.mc.player.getMainHandStack() != fillStack) {
                InventoryManager.equipItem(fillStack);
            }
            WorldUtils.swapCarriedItems();
            for (BlockPos pos : list) {
                if (!BlockUtils.canPlace((BlockPos)pos, (boolean)true) || !WorldUtils.isInPlacementRange(pos)) continue;
                if (!RateController.checkPlaceRate()) break;
                WorldUtils.sendPlacePacket(Hand.OFF_HAND, WorldUtils.makeHitResult(pos, Direction.DOWN));
                HighwayState.getInstance().getBlockBreakAttempts().put(pos, this.tickCounter);
            }
            WorldUtils.swapCarriedItems();
        }
    }

    private void updateRenderList() { // was: XydsV5qWOWTk7gA
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        this.renderPositions.clear();
        BlockPos origin = this.mc.player.getBlockPos();
        int n = (Integer)this.renderRange.get();
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos pos = origin.add(i, j, k);
                    Vec3d center = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                    FluidState fluidState = this.mc.world.getBlockState(pos).getFluidState();
                    if (!(this.mc.player.getEyePos().distanceTo(center) <= (double)n) || !fluidState.isStill()) continue;
                    this.renderPositions.add(pos);
                }
            }
        }
    }

    private List<BlockPos> findSourceBlocks(double d) { // was: jOdDDFXSeWl4(double)
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        Vec3d eyePos = Vec3d.ofCenter(BlockPos.ofFloored(this.mc.player.getEyePos()));
        int n = (int)Math.floor(d);
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos pos = BlockPos.ofFloored((double)(eyePos.x + (double)i), (double)(eyePos.y + (double)j), (double)(eyePos.z + (double)k));
                    Vec3d center = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                    if (!(eyePos.distanceTo(center) <= d)) continue;
                    FluidState fluidState = this.mc.world.getBlockState(pos).getFluidState();
                    if (((Boolean)this.fillWater.get()).booleanValue() && fluidState.getFluid() == Fluids.WATER) {
                        arrayList.add(pos);
                        continue;
                    }
                    if (!((Boolean)this.fillLava.get()).booleanValue() || fluidState.getFluid() != Fluids.EMPTY) continue;
                    arrayList.add(pos);
                }
            }
        }
        return arrayList;
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
        if (!((Boolean)this.allowRender.get()).booleanValue() || this.renderPositions.isEmpty() || this.mc.world == null) {
            return;
        }
        RenderUtils.renderBlocks(render3DEvent, this.renderPositions);
    }

    public static final class ListMode
    extends Enum<ListMode> {
        public static final /* enum */ ListMode None      = new ListMode(); // was: GKJXi60M
        public static final /* enum */ ListMode Whitelist = new ListMode(); // was: Yxt2PggV46LE0
        public static final /* enum */ ListMode Blacklist = new ListMode(); // was: tmPRU8P4PChtIMWY
        private static final /* synthetic */ ListMode[] $VALUES; // was: eXeVFInA3vzeCfG

        public static ListMode[] values() {
            return (ListMode[])$VALUES.clone();
        }

        public static ListMode valueOf(String string) {
            return Enum.valueOf(ListMode.class, string);
        }

        private static /* synthetic */ ListMode[] $values() { // was: KrscIKJwTdc
            return new ListMode[]{None, Whitelist, Blacklist};
        }

        static {
            $VALUES = ListMode.$values();
        }
    }
}