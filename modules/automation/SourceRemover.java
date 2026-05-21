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
import net.minecraft.InteractionHand;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.ItemStack;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.PlayerAbilities;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.BlockState;
import net.minecraft.BlockView;
import net.minecraft.Fluids;

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
    private final List<BlockPos> VGuQlVJMpGYGr;
    private int vV6cbpE7KWBI;
    private int FIc3lket;
    public static boolean NvJtheFonk;

    public static boolean fXEQFU() {
        return NvJtheFonk;
    }

    public SourceRemover() {
        super(musheor.AUTOMATION, "source-filler", "Fill up lava and or water source blocks");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.fillLava = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lava")).description("Fills lava source blocks")).defaultValue((Object)true)).build());
        this.fillWater = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("water")).description("Fills water source blocks")).defaultValue((Object)true)).build());
        this.listMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("list-mode")).description("Selection mode.")).defaultValue((Object)ListMode.GKJXi60M)).build());
        this.whitelist = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("The allowed blocks that it will use to fill up the lava.")).defaultValue(new Block[]{Blocks.field_10515, Blocks.field_10445, Blocks.field_10566, Blocks.field_23869, Blocks.field_10540}).visible(() -> this.listMode.get() == ListMode.Yxt2PggV46LE0)).build());
        this.blacklist = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blacklist")).description("The denied blocks that it not will use to fill up the lava.")).visible(() -> this.listMode.get() == ListMode.tmPRU8P4PChtIMWY)).build());
        this.allowRender = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Render source blocks in specific range")).defaultValue((Object)true)).build());
        this.renderRange = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-range")).description("Maximum range at which sourceblocks are rendered")).defaultValue((Object)16)).sliderRange(1, 64).visible(() -> this.allowRender.get())).build());
        this.VGuQlVJMpGYGr = new ArrayList<BlockPos>();
        this.FIc3lket = 0;
    }

    public void onActivate() {
        HighwayState.LmpuWjra().Os3dd8a().clear();
        this.vV6cbpE7KWBI = 0;
        this.FIc3lket = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (WorldUtils.btLCQHvKVR()) {
            return;
        }
        if (HighwayBuilder.zl2vxyh()) {
            return;
        }
        ++this.vV6cbpE7KWBI;
        WorldUtils.jOdDDFXSeWl4(this.vV6cbpE7KWBI, Blocks.LAVA);
        if (++this.FIc3lket > 5 && ((Boolean)this.allowRender.get()).booleanValue()) {
            this.FIc3lket = 0;
            this.XydsV5qWOWTk7gA();
        }
        ItemStack ItemStack2 = null;
        for (int i = 0; i < 9; ++i) {
            Object object;
            ItemStack ItemStack3;
            ItemStack ItemStack2 = this.mc.player.getId().method_5438(i);
            if (!(this.listMode.get() == ListMode.tmPRU8P4PChtIMWY && !((List)this.blacklist.get()).contains(Block.method_9503((ItemStack)ItemStack2.getStack())) || this.listMode.get() == ListMode.Yxt2PggV46LE0 && ((List)this.whitelist.get()).contains(Block.method_9503((ItemStack)ItemStack2.getStack()))) && (this.listMode.get() != ListMode.GKJXi60M || !((ItemStack3 = ItemStack2.getStack()) instanceof AbstractClientPlayerEntity) || !(object = (AbstractClientPlayerEntity)ItemStack3).method_7711().method_9564().method_51367())) continue;
            ItemStack2 = ItemStack2.getStack();
        }
        if (ItemStack2 == null) {
            return;
        }
        List<BlockPos> list = this.jOdDDFXSeWl4((Double)MusheorSystem.Manager.placementRange.get());
        list.sort(Comparator.comparingDouble(BlockPos2 -> this.mc.player.method_5707(Vec3d.ofCenter((BlockPos)BlockPos2))));
        boolean bl = false;
        for (BlockPos BlockPos3 : list) {
            if (!BlockUtils.canPlace((BlockPos)BlockPos3, (boolean)true) || !WorldUtils.KDNrzlU9qtrEv(BlockPos3)) continue;
            meteordevelopment.meteorclient.utils.render.RenderUtils.renderTickingBlock((BlockPos)BlockPos3, (Color)Color.PINK, (Color)Color.PINK, (ShapeMode)ShapeMode.Lines, (int)0, (int)1, (boolean)false, (boolean)false);
            bl = true;
            break;
        }
        if (bl) {
            if (this.mc.player.method_6047().getStack() != ItemStack2) {
                InventoryManager.L5CF0C6jx0T17H4I(ItemStack2);
            }
            WorldUtils.l3ot1CwoJ9CsS();
            for (BlockPos BlockPos4 : list) {
                if (!BlockUtils.canPlace((BlockPos)BlockPos4, (boolean)true) || !WorldUtils.KDNrzlU9qtrEv(BlockPos4)) continue;
                if (!RateController.qy8UwM99rVr()) break;
                WorldUtils.jOdDDFXSeWl4(InteractionHand.field_5810, WorldUtils.mp3zoXQFKUKYj5(BlockPos4, Direction.field_11033));
                HighwayState.LmpuWjra().Os3dd8a().put(BlockPos4, this.vV6cbpE7KWBI);
            }
            WorldUtils.l3ot1CwoJ9CsS();
        }
    }

    private void XydsV5qWOWTk7gA() {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        this.VGuQlVJMpGYGr.clear();
        BlockPos BlockPos2 = this.mc.player.getBlockPos();
        int n = (Integer)this.renderRange.get();
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockState BlockState2;
                    BlockView BlockView2;
                    BlockPos BlockPos3 = BlockPos2.method_10069(i, j, k);
                    Vec3d Vec3d2 = new Vec3d((double)BlockPos3.getX() + 0.5, (double)BlockPos3.getY() + 0.5, (double)BlockPos3.getZ() + 0.5);
                    if (!(this.mc.player.method_33571().method_1022(Vec3d2) <= (double)n) || !(BlockView2 = (BlockState2 = this.mc.world.getBlockState(BlockPos3)).method_26227()).method_15771()) continue;
                    this.VGuQlVJMpGYGr.add(BlockPos3);
                }
            }
        }
    }

    private List<BlockPos> jOdDDFXSeWl4(double d) {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        Vec3d Vec3d2 = Vec3d.method_24953((BlockPos)BlockPos.method_49638((PlayerAbilities)this.mc.player.method_33571()));
        int n = (int)Math.floor(d);
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos BlockPos2 = BlockPos.method_49637((double)(Vec3d2.x + (double)i), (double)(Vec3d2.y + (double)j), (double)(Vec3d2.z + (double)k));
                    Vec3d Vec3d3 = new Vec3d((double)BlockPos2.getX() + 0.5, (double)BlockPos2.getY() + 0.5, (double)BlockPos2.getZ() + 0.5);
                    if (!(Vec3d2.method_1022(Vec3d3) <= d)) continue;
                    BlockView BlockView2 = this.mc.world.getBlockState(BlockPos2).method_26227();
                    if (((Boolean)this.fillWater.get()).booleanValue() && BlockView2.getFluid() == Fluids.field_15910) {
                        arrayList.add(BlockPos2);
                        continue;
                    }
                    if (!((Boolean)this.fillLava.get()).booleanValue() || BlockView2.getFluid() != Fluids.EMPTY) continue;
                    arrayList.add(BlockPos2);
                }
            }
        }
        return arrayList;
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
        if (!((Boolean)this.allowRender.get()).booleanValue() || this.VGuQlVJMpGYGr.isEmpty() || this.mc.world == null) {
            return;
        }
        RenderUtils.mp3zoXQFKUKYj5(render3DEvent, this.VGuQlVJMpGYGr);
    }

    public static final class ListMode
    extends Enum<ListMode> {
        public static final /* enum */ ListMode GKJXi60M = new ListMode();
        public static final /* enum */ ListMode Yxt2PggV46LE0 = new ListMode();
        public static final /* enum */ ListMode tmPRU8P4PChtIMWY = new ListMode();
        private static final /* synthetic */ ListMode[] eXeVFInA3vzeCfG;

        public static ListMode[] values() {
            return (ListMode[])eXeVFInA3vzeCfG.clone();
        }

        public static ListMode valueOf(String string) {
            return Enum.valueOf(ListMode.class, string);
        }

        private static /* synthetic */ ListMode[] KrscIKJwTdc() {
            return new ListMode[]{GKJXi60M, Yxt2PggV46LE0, tmPRU8P4PChtIMWY};
        }

        static {
            eXeVFInA3vzeCfG = ListMode.KrscIKJwTdc();
        }
    }
}

