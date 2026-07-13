// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

/**
 * "portal-spawn-esp" — client-side scan for where a Nether portal could spawn around a
 * target position. On a background thread it spirals over columns within {@code radius},
 * finding the highest floor of four contiguous solid blocks (running north) with 4 blocks
 * of air above each, and renders the resulting portal footprints.
 */
public class PortalSpawnESP extends Module {
    private final AtomicBoolean scanning = new AtomicBoolean(false);       // was: psJq59YIbp3Z
    private final List<BlockPos> portalBases = new CopyOnWriteArrayList<>(); // was: SOYyh5IPg26f7F

    public final Setting<BlockPos> targetPos = this.settings.getDefaultGroup().add(new BlockPosSetting.Builder() // was: FvaNWO
        .name("Target Position").description("Block position to look around for possible / valid portal spawn locations.").defaultValue(new BlockPos(0, 0, 0)).build());
    private final Setting<Integer> radius = this.settings.getDefaultGroup().add(new IntSetting.Builder() // was: rKbT3Ifwo
        .name("radius").description("The range at which portal locations are scanned and rendered.").sliderRange(8, 128).defaultValue(32).build());
    private final Setting<ShapeMode> shapeMode = this.settings.getDefaultGroup().add(new EnumSetting.Builder<ShapeMode>() // was: r7hOYIKN2
        .name("shape").description("How the blocks scheduled for placement are rendered.").defaultValue(ShapeMode.Both).build());
    private final Setting<SettingColor> lineColor = this.settings.getDefaultGroup().add(new ColorSetting.Builder() // was: oZHMlTL
        .name("line-color").description("Color of blocks scheduled for placement.").defaultValue(new SettingColor(25, 25, 225, 255)).build());
    private final Setting<SettingColor> sideColor = this.settings.getDefaultGroup().add(new ColorSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("render-placements-side-color").description("Side color of blocks scheduled for placement.").defaultValue(new SettingColor(25, 25, 225, 25)).build());

    public PortalSpawnESP() {
        super(musheor.AUTOMATION, "portal-spawn-esp", "Client-side scan for where a Nether portal could spawn.");
    }

    @Override
    public void onDeactivate() {
        this.portalBases.clear();
        this.scanning.set(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) { // was: FvaNWO(Post)
        if (this.mc.player != null && this.mc.world != null) {
            if (this.scanning.compareAndSet(false, true)) {
                new Thread(this::scan, "PortalScanThread").start();
            }
        } else {
            this.portalBases.clear();
            this.scanning.set(false);
        }
    }

    /** Background thread: spirals over the columns and records valid portal footprints. */
    private void scan() { // was: FvaNWO()
        World world = this.mc.world;
        assert this.mc.player != null && world != null;
        BlockPos center = this.targetPos.get();
        int horizontalRadius = this.radius.get();
        int yMin = world.getBottomY();
        List<BlockPos> newPortals = new ArrayList<>();

        for (BlockPos.Mutable candidate : BlockPos.iterateInSquare(center, horizontalRadius, Direction.EAST, Direction.SOUTH)) {
            int x = candidate.getX();
            int z = candidate.getZ();
            int surfaceY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
            int yMax = Math.min(surfaceY, center.getY() + horizontalRadius);
            for (int y = yMax; y >= yMin; y--) {
                candidate.set(x, y, z);
                if (this.isValidPortalSpawn(candidate)) {
                    newPortals.add(candidate.toImmutable());
                    break;
                }
            }
        }

        this.portalBases.clear();
        this.portalBases.addAll(newPortals);
        this.scanning.set(false);
    }

    /** True if {@code basePos} and the three blocks north form a solid floor with 4 blocks of air above. */
    private boolean isValidPortalSpawn(BlockPos.Mutable basePos) { // was: FvaNWO(BlockPos.Mutable)
        List<BlockPos> requiredSolidBlocks = new ArrayList<>();
        requiredSolidBlocks.add(basePos);
        requiredSolidBlocks.add(basePos.offset(Direction.NORTH, 1));
        requiredSolidBlocks.add(basePos.offset(Direction.NORTH, 2));
        requiredSolidBlocks.add(basePos.offset(Direction.NORTH, 3));

        for (BlockPos pos : requiredSolidBlocks) {
            assert this.mc.world != null;
            if (!this.mc.world.getBlockState(pos).isSideSolidFullSquare(this.mc.world, pos, Direction.UP)) return false;
        }
        for (BlockPos pos : requiredSolidBlocks) {
            for (int i = 1; i < 5; i++) {
                if (!this.mc.world.getBlockState(pos.up(i)).isAir()) return false;
            }
        }
        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        for (BlockPos portalBase : this.portalBases) {
            for (int i = 0; i < 5; i++) {
                event.renderer.box(portalBase.offset(Direction.NORTH, i), this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
            }
        }
    }
}
