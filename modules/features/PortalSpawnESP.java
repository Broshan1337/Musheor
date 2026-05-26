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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;

public class PortalSpawnESP
extends Module {
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private final List<BlockPos> validPositions = new CopyOnWriteArrayList<BlockPos>();
    public final Setting<BlockPos> target = this.settings.getDefaultGroup().add((Setting)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("Target Position")).description("Block position to look around for possible / valid portal spawn locations.")).defaultValue((Object)new BlockPos(0, 0, 0))).build());
    private final Setting<Integer> radius = this.settings.getDefaultGroup().add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("radius")).description("The range at which portal locations are scanned and rendered.")).sliderRange(8, 128).defaultValue((Object)32)).build());
    private final Setting<ShapeMode> shape = this.settings.getDefaultGroup().add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape")).description("How the blocks scheduled for placement are rendered.")).defaultValue((Object)ShapeMode.Both)).build());
    private final Setting<SettingColor> lineColor = this.settings.getDefaultGroup().add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("Color of blocks scheduled for placement.")).defaultValue(new SettingColor(25, 25, 225, 255)).build());
    private final Setting<SettingColor> sideColor = this.settings.getDefaultGroup().add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("render-placements-side-color")).description("Side color of blocks scheduled for placement.")).defaultValue(new SettingColor(25, 25, 225, 25)).build());

    public PortalSpawnESP() {
        super(musheor.AUTOMATION, "portal-spawn-esp", "Client-side scan for where a Nether portal could spawn.");
    }

    public void onDeactivate() {
        this.validPositions.clear();
        this.scanning.set(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.mc.player == null || this.mc.world == null) {
            this.validPositions.clear();
            this.scanning.set(false);
            return;
        }
        if (this.scanning.compareAndSet(false, true)) {
            new Thread(this::runScan, "PortalScanThread").start();
        }
    }

    private void runScan() {
        ClientWorld world = this.mc.world;
        assert (this.mc.player != null && world != null);
        BlockPos center = (BlockPos)this.target.get();
        int n = (Integer)this.radius.get();
        int bottomY = world.getBottomY();
        ArrayList<BlockPos> found = new ArrayList<BlockPos>();
        block0: for (BlockPos.Mutable mutable : BlockPos.iterateInSquare((BlockPos)center, (int)n, (Direction)Direction.EAST, (Direction)Direction.SOUTH)) {
            int n3;
            int x = mutable.getX();
            int z = mutable.getZ();
            int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            for (int i = n3 = Math.min(topY, center.getY() + n); i >= bottomY; --i) {
                mutable.set(x, i, z);
                if (!this.isValidPortalSpawn(mutable)) continue;
                found.add(mutable.toImmutable());
                continue block0;
            }
        }
        this.validPositions.clear();
        this.validPositions.addAll(found);
        this.scanning.set(false);
    }

    private boolean isValidPortalSpawn(BlockPos.Mutable mutable) {
        ArrayList<BlockPos> footprint = new ArrayList<BlockPos>();
        footprint.add(mutable.toImmutable());
        footprint.add(mutable.offset(Direction.NORTH, 1));
        footprint.add(mutable.offset(Direction.NORTH, 2));
        footprint.add(mutable.offset(Direction.NORTH, 3));
        for (BlockPos pos : footprint) {
            assert (this.mc.world != null);
            if (this.mc.world.getBlockState(pos).isSideSolidFullSquare((BlockView)this.mc.world, pos, Direction.UP)) continue;
            return false;
        }
        for (BlockPos pos : footprint) {
            for (int i = 1; i < 5; ++i) {
                if (this.mc.world.getBlockState(pos.up(i)).isAir()) continue;
                return false;
            }
        }
        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent render3DEvent) {
        for (BlockPos BlockPos2 : this.validPositions) {
            for (int i = 0; i < 5; ++i) {
                render3DEvent.renderer.box(BlockPos2.offset(Direction.NORTH, i), (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shape.get(), 0); // was: method_10079(Direction.field_11043, i)
            }
        }
    }
}

