// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.WorldUtils;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.BlockState;
import net.minecraft.BlockView;
import net.minecraft.Fluids;

public class SpongeBob
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPreview;
    private final Setting<Integer> scanDistance;
    private final Setting<Integer> placeReach;
    private final Setting<Integer> placeDelay;
    private final Setting<Boolean> previewEnabled;
    private final Setting<ShapeMode> previewShape;
    private final Setting<SettingColor> previewSideColor;
    private final Setting<SettingColor> previewLineColor;
    private final Setting<Integer> previewRefresh;
    private final List<BlockPos> oP3Nqfl3FWY;
    private int LG9c9vjIy;
    private int DjHFvVBXZHN0k;

    public SpongeBob() {
        super(musheor.AUTOMATION, "spongebob", "Clears water using optimized sponge logic.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPreview = this.settings.createGroup("Preview");
        this.scanDistance = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-distance")).description("Scan radius.")).defaultValue((Object)16)).min(4).sliderRange(4, 32).build());
        this.placeReach = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-reach")).description("Maximum airPlace reach.")).defaultValue((Object)5)).min(1).sliderRange(1, 8).build());
        this.placeDelay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("Ticks between sponge placements.")).defaultValue((Object)5)).min(0).sliderRange(0, 20).build());
        this.previewEnabled = this.sgPreview.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enabled")).defaultValue((Object)true)).build());
        this.previewShape = this.sgPreview.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape")).defaultValue((Object)ShapeMode.Lines)).build());
        this.previewSideColor = this.sgPreview.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).defaultValue(new SettingColor(255, 255, 0, 50)).visible(() -> this.previewShape.get() == ShapeMode.Sides || this.previewShape.get() == ShapeMode.Both)).build());
        this.previewLineColor = this.sgPreview.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).defaultValue(new SettingColor(255, 255, 0, 255)).visible(() -> this.previewShape.get() == ShapeMode.Lines || this.previewShape.get() == ShapeMode.Both)).build());
        this.previewRefresh = this.sgPreview.add((Setting)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("refresh-delay")).defaultValue((Object)10)).min(1).sliderRange(1, 60).build());
        this.oP3Nqfl3FWY = new ArrayList<BlockPos>();
        this.LG9c9vjIy = 0;
        this.DjHFvVBXZHN0k = 0;
    }

    public void onActivate() {
        this.oP3Nqfl3FWY.clear();
        this.LG9c9vjIy = 0;
        this.DjHFvVBXZHN0k = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (((Boolean)this.previewEnabled.get()).booleanValue()) {
            this.h9MViB();
            return;
        }
        this.kAT8CfWuaGb();
    }

    private void h9MViB() {
        if (++this.LG9c9vjIy < (Integer)this.previewRefresh.get()) {
            return;
        }
        this.LG9c9vjIy = 0;
        this.oP3Nqfl3FWY.clear();
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>(this.uUngom5N());
        if (hashSet.isEmpty()) {
            return;
        }
        List<List<BlockPos>> list = this.mp3zoXQFKUKYj5(new ArrayList<BlockPos>(hashSet));
        if (list.isEmpty()) {
            return;
        }
        for (List<BlockPos> list2 : list) {
            BlockPos BlockPos2 = this.Gt56Sj4a6BWhgB(list2);
            BlockPos BlockPos3 = this.UgB10d(BlockPos2);
            if (BlockPos3 == null) continue;
            this.oP3Nqfl3FWY.add(BlockPos3);
            this.jOdDDFXSeWl4(hashSet, BlockPos3);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
        if (!((Boolean)this.previewEnabled.get()).booleanValue() || this.oP3Nqfl3FWY.isEmpty()) {
            return;
        }
        for (BlockPos BlockPos2 : this.oP3Nqfl3FWY) {
            render3DEvent.renderer.box(BlockPos2, (Color)this.previewSideColor.get(), (Color)this.previewLineColor.get(), (ShapeMode)this.previewShape.get(), 0);
        }
    }

    private BlockPos jOdDDFXSeWl4(List<BlockPos> list) {
        BlockPos BlockPos22;
        if (list.isEmpty()) {
            return null;
        }
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        for (BlockPos BlockPos22 : list) {
            n += BlockPos22.getX();
            n2 += BlockPos22.getY();
            n3 += BlockPos22.getZ();
        }
        BlockPos BlockPos3 = new BlockPos(n / list.size(), n2 / list.size(), n3 / list.size());
        if (this.mc.world.getFluidState(BlockPos3).getFluid() == Fluids.field_15910 && this.mc.world.getFluidState(BlockPos3).method_15771()) {
            return BlockPos3;
        }
        BlockPos22 = null;
        double d = Double.MAX_VALUE;
        Vec3d Vec3d2 = Vec3d.method_24953((BlockPos)BlockPos3);
        for (BlockPos BlockPos4 : list) {
            double d2 = Vec3d2.method_1025(Vec3d.method_24953((BlockPos)BlockPos4));
            if (!(d2 < d)) continue;
            d = d2;
            BlockPos22 = BlockPos4;
        }
        return BlockPos22;
    }

    private void kAT8CfWuaGb() {
        List<BlockPos> list = this.uUngom5N();
        if (list.isEmpty()) {
            return;
        }
        List<List<BlockPos>> list2 = this.mp3zoXQFKUKYj5(list);
        for (List<BlockPos> list3 : list2) {
            Vec3d Vec3d2;
            BlockPos BlockPos2 = this.jOdDDFXSeWl4(list3);
            if (BlockPos2 == null || (Vec3d2 = this.mc.player.method_33571()).method_1022(Vec3d.method_24953((BlockPos)BlockPos2)) > (double)((Integer)this.placeReach.get()).intValue()) continue;
            if (++this.DjHFvVBXZHN0k < (Integer)this.placeDelay.get()) {
                return;
            }
            this.DjHFvVBXZHN0k = 0;
            WorldUtils.jOdDDFXSeWl4(BlockPos2, Direction.field_11033);
        }
    }

    private List<BlockPos> uUngom5N() {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        BlockPos BlockPos2 = this.mc.player.getBlockPos();
        int n = (Integer)this.scanDistance.get();
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos BlockPos3 = BlockPos2.method_10069(i, j, k);
                    BlockView BlockView2 = this.mc.world.getFluidState(BlockPos3);
                    if (!BlockView2.method_15771() || BlockView2.getFluid() != Fluids.field_15910) continue;
                    arrayList.add(BlockPos3);
                }
            }
        }
        return arrayList;
    }

    private List<List<BlockPos>> mp3zoXQFKUKYj5(List<BlockPos> list) {
        ArrayList<List<BlockPos>> arrayList = new ArrayList<List<BlockPos>>();
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>(list);
        HashSet<BlockPos> hashSet2 = new HashSet<BlockPos>();
        int n = 7;
        for (BlockPos BlockPos2 : hashSet) {
            if (hashSet2.contains(BlockPos2)) continue;
            ArrayDeque<BlockPos> arrayDeque = new ArrayDeque<BlockPos>();
            ArrayList<BlockPos> arrayList2 = new ArrayList<BlockPos>();
            hashSet2.add(BlockPos2);
            arrayDeque.add(BlockPos2);
            while (!arrayDeque.isEmpty()) {
                BlockPos BlockPos3 = (BlockPos)arrayDeque.poll();
                arrayList2.add(BlockPos3);
                for (Direction Direction2 : Direction.values()) {
                    BlockPos BlockPos4 = BlockPos3.offset(Direction2);
                    if (!hashSet.contains(BlockPos4) || hashSet2.contains(BlockPos4) || this.jOdDDFXSeWl4(BlockPos2, BlockPos4) > n) continue;
                    hashSet2.add(BlockPos4);
                    arrayDeque.add(BlockPos4);
                }
            }
            arrayList.add(arrayList2);
        }
        return arrayList;
    }

    private int jOdDDFXSeWl4(BlockPos BlockPos2, BlockPos BlockPos3) {
        return Math.abs(BlockPos2.getX() - BlockPos3.getX()) + Math.abs(BlockPos2.getY() - BlockPos3.getY()) + Math.abs(BlockPos2.getZ() - BlockPos3.getZ());
    }

    private BlockPos Gt56Sj4a6BWhgB(List<BlockPos> list) {
        long l = 0L;
        long l2 = 0L;
        long l3 = 0L;
        int n = list.size();
        for (BlockPos BlockPos2 : list) {
            l += (long)BlockPos2.getX();
            l2 += (long)BlockPos2.getY();
            l3 += (long)BlockPos2.getZ();
        }
        return new BlockPos((int)(l / (long)n), (int)(l2 / (long)n), (int)(l3 / (long)n));
    }

    private BlockPos UgB10d(BlockPos BlockPos2) {
        int n;
        Vec3d Vec3d2 = this.mc.player.method_33571();
        if (this.jOdDDFXSeWl4(BlockPos2, Vec3d2, n = ((Integer)this.placeReach.get()).intValue())) {
            return BlockPos2;
        }
        for (int i = -4; i <= 4; ++i) {
            for (int j = -4; j <= 4; ++j) {
                for (int k = -4; k <= 4; ++k) {
                    BlockPos BlockPos3 = BlockPos2.method_10069(i, j, k);
                    if (!this.jOdDDFXSeWl4(BlockPos3, Vec3d2, n)) continue;
                    return BlockPos3;
                }
            }
        }
        return null;
    }

    private boolean jOdDDFXSeWl4(BlockPos BlockPos2, Vec3d Vec3d2, double d) {
        if (Vec3d2.method_1022(Vec3d.method_24953((BlockPos)BlockPos2)) > d) {
            return false;
        }
        BlockState BlockState2 = this.mc.world.getBlockState(BlockPos2);
        return BlockState2.method_26227().method_15771() || BlockState2.method_45474();
    }

    private void jOdDDFXSeWl4(Set<BlockPos> set, BlockPos BlockPos2) {
        int n = 7;
        set.removeIf(BlockPos3 -> this.jOdDDFXSeWl4((BlockPos)BlockPos3, BlockPos2) <= 7);
    }
}

