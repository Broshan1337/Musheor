// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.class_1922;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Heightmap;
import net.minecraft.class_638;

public class PortalSpawnESP
extends Module {
    private final AtomicBoolean cHJtj8k = new AtomicBoolean(false);
    private final List<BlockPos> douPMO = new CopyOnWriteArrayList<BlockPos>();
    public final Setting<BlockPos> target = this.settings.getDefaultGroup().add((Setting)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("Target Position")).description("Block position to look around for possible / valid portal spawn locations.")).defaultValue((Object)new BlockPos(0, 0, 0))).build());
    private final Setting<Integer> radius = this.settings.getDefaultGroup().add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("radius")).description("The range at which portal locations are scanned and rendered.")).sliderRange(8, 128).defaultValue((Object)32)).build());
    private final Setting<ShapeMode> shape = this.settings.getDefaultGroup().add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape")).description("How the blocks scheduled for placement are rendered.")).defaultValue((Object)ShapeMode.Both)).build());
    private final Setting<SettingColor> lineColor = this.settings.getDefaultGroup().add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("Color of blocks scheduled for placement.")).defaultValue(new SettingColor(25, 25, 225, 255)).build());
    private final Setting<SettingColor> sideColor = this.settings.getDefaultGroup().add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("render-placements-side-color")).description("Side color of blocks scheduled for placement.")).defaultValue(new SettingColor(25, 25, 225, 25)).build());

    public PortalSpawnESP() {
        super(musheor.AUTOMATION, "portal-spawn-esp", "Client-side scan for where a Nether portal could spawn.");
    }

    public void onDeactivate() {
        this.douPMO.clear();
        this.cHJtj8k.set(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.mc.player == null || this.mc.world == null) {
            this.douPMO.clear();
            this.cHJtj8k.set(false);
            return;
        }
        if (this.cHJtj8k.compareAndSet(false, true)) {
            new Thread(this::DjHFvVBXZHN0k, "PortalScanThread").start();
        }
    }

    private void DjHFvVBXZHN0k() {
        class_638 class_6382 = this.mc.world;
        assert (this.mc.player != null && class_6382 != null);
        BlockPos BlockPos2 = (BlockPos)this.target.get();
        int n = (Integer)this.radius.get();
        int n2 = class_6382.getBottomY();
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        block0: for (BlockPos.class_2339 class_23392 : BlockPos.method_30512((BlockPos)BlockPos2, (int)n, (Direction)Direction.field_11034, (Direction)Direction.field_11035)) {
            int n3;
            int n4 = class_23392.getX();
            int n5 = class_23392.getZ();
            int n6 = class_6382.getTopY(Heightmap.class_2903.WORLD_SURFACE, n4, n5);
            for (int i = n3 = Math.min(n6, BlockPos2.getY() + n); i >= n2; --i) {
                class_23392.method_10103(n4, i, n5);
                if (!this.jOdDDFXSeWl4(class_23392)) continue;
                arrayList.add(class_23392.mutableCopy());
                continue block0;
            }
        }
        this.douPMO.clear();
        this.douPMO.addAll(arrayList);
        this.cHJtj8k.set(false);
    }

    private boolean jOdDDFXSeWl4(BlockPos.class_2339 class_23392) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(class_23392);
        arrayList.add(class_23392.method_10079(Direction.field_11043, 1));
        arrayList.add(class_23392.method_10079(Direction.field_11043, 2));
        arrayList.add(class_23392.method_10079(Direction.field_11043, 3));
        for (BlockPos BlockPos2 : arrayList) {
            assert (this.mc.world != null);
            if (this.mc.world.getBlockState(BlockPos2).method_26206((class_1922)this.mc.world, BlockPos2, Direction.field_11036)) continue;
            return false;
        }
        for (BlockPos BlockPos2 : arrayList) {
            for (int i = 1; i < 5; ++i) {
                if (this.mc.world.getBlockState(BlockPos2.method_10086(i)).isAir()) continue;
                return false;
            }
        }
        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        for (BlockPos BlockPos2 : this.douPMO) {
            for (int i = 0; i < 5; ++i) {
                render3DEvent.renderer.box(BlockPos2.method_10079(Direction.field_11043, i), (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shape.get(), 0);
            }
        }
    }
}

