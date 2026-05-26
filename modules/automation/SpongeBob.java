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
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
    private final List<BlockPos> spongeTargets;
    private int previewTick;
    private int placeTick;

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
        this.spongeTargets = new ArrayList<BlockPos>();
        this.previewTick = 0;
        this.placeTick = 0;
    }

    public void onActivate() {
        this.spongeTargets.clear();
        this.previewTick = 0;
        this.placeTick = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (((Boolean)this.previewEnabled.get()).booleanValue()) {
            this.tickPreview();
            return;
        }
        this.tickPlacement();
    }

    private void tickPreview() {
        if (++this.previewTick < (Integer)this.previewRefresh.get()) {
            return;
        }
        this.previewTick = 0;
        this.spongeTargets.clear();
        HashSet<BlockPos> waterSet = new HashSet<BlockPos>(this.findWaterBlocks());
        if (waterSet.isEmpty()) {
            return;
        }
        List<List<BlockPos>> clusters = this.clusterWaterBlocks(new ArrayList<BlockPos>(waterSet));
        if (clusters.isEmpty()) {
            return;
        }
        for (List<BlockPos> cluster : clusters) {
            BlockPos centroid = this.getCentroid(cluster);
            BlockPos target = this.findReachablePosition(centroid);
            if (target == null) continue;
            this.spongeTargets.add(target);
            this.removeCoveredBlocks(waterSet, target);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
        if (!((Boolean)this.previewEnabled.get()).booleanValue() || this.spongeTargets.isEmpty()) {
            return;
        }
        for (BlockPos BlockPos2 : this.spongeTargets) {
            render3DEvent.renderer.box(BlockPos2, (Color)this.previewSideColor.get(), (Color)this.previewLineColor.get(), (ShapeMode)this.previewShape.get(), 0);
        }
    }

    private BlockPos getBestTarget(List<BlockPos> list) {
        BlockPos closest;
        if (list.isEmpty()) {
            return null;
        }
        int x = 0, y = 0, z = 0;
        for (BlockPos pos : list) {
            x += pos.getX();
            y += pos.getY();
            z += pos.getZ();
        }
        BlockPos center = new BlockPos(x / list.size(), y / list.size(), z / list.size());
        FluidState centerFluid = this.mc.world.getFluidState(center);
        if (centerFluid.getFluid() == Fluids.WATER && centerFluid.isStill()) {
            return center;
        }
        closest = null;
        double d = Double.MAX_VALUE;
        Vec3d centerVec = Vec3d.ofCenter(center);
        for (BlockPos pos : list) {
            double d2 = centerVec.squaredDistanceTo(Vec3d.ofCenter(pos));
            if (!(d2 < d)) continue;
            d = d2;
            closest = pos;
        }
        return closest;
    }

    private void tickPlacement() {
        List<BlockPos> waterBlocks = this.findWaterBlocks();
        if (waterBlocks.isEmpty()) {
            return;
        }
        List<List<BlockPos>> clusters = this.clusterWaterBlocks(waterBlocks);
        for (List<BlockPos> cluster : clusters) {
            BlockPos target = this.getBestTarget(cluster);
            if (target == null || this.mc.player.getPos().distanceTo(Vec3d.ofCenter(target)) > (double)((Integer)this.placeReach.get()).intValue()) continue;
            if (++this.placeTick < (Integer)this.placeDelay.get()) {
                return;
            }
            this.placeTick = 0;
            WorldUtils.placeBlockPacket(target, Direction.DOWN);
        }
    }

    private List<BlockPos> findWaterBlocks() {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        BlockPos origin = this.mc.player.getBlockPos();
        int n = (Integer)this.scanDistance.get();
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos pos = origin.add(i, j, k);
                    FluidState fluid = this.mc.world.getFluidState(pos);
                    if (!fluid.isStill() || fluid.getFluid() != Fluids.WATER) continue;
                    result.add(pos);
                }
            }
        }
        return result;
    }

    private List<List<BlockPos>> clusterWaterBlocks(List<BlockPos> list) {
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
                    if (!hashSet.contains(BlockPos4) || hashSet2.contains(BlockPos4) || this.manhattanDistance(BlockPos2, BlockPos4) > n) continue;
                    hashSet2.add(BlockPos4);
                    arrayDeque.add(BlockPos4);
                }
            }
            arrayList.add(arrayList2);
        }
        return arrayList;
    }

    private int manhattanDistance(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private BlockPos getCentroid(List<BlockPos> list) {
        long x = 0L, y = 0L, z = 0L;
        int n = list.size();
        for (BlockPos pos : list) {
            x += (long)pos.getX();
            y += (long)pos.getY();
            z += (long)pos.getZ();
        }
        return new BlockPos((int)(x / (long)n), (int)(y / (long)n), (int)(z / (long)n));
    }

    private BlockPos findReachablePosition(BlockPos center) {
        int n;
        Vec3d playerPos = this.mc.player.getPos();
        if (this.isReachable(center, playerPos, n = ((Integer)this.placeReach.get()).intValue())) {
            return center;
        }
        for (int i = -4; i <= 4; ++i) {
            for (int j = -4; j <= 4; ++j) {
                for (int k = -4; k <= 4; ++k) {
                    BlockPos candidate = center.add(i, j, k);
                    if (!this.isReachable(candidate, playerPos, n)) continue;
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean isReachable(BlockPos pos, Vec3d playerPos, double maxDist) {
        if (playerPos.distanceTo(Vec3d.ofCenter(pos)) > maxDist) {
            return false;
        }
        BlockState state = this.mc.world.getBlockState(pos);
        return state.getCollisionShape(null, null).isEmpty() || state.isAir();
    }

    private void removeCoveredBlocks(Set<BlockPos> set, BlockPos spongePos) {
        set.removeIf(pos -> this.manhattanDistance((BlockPos)pos, spongePos) <= 7);
    }
}

