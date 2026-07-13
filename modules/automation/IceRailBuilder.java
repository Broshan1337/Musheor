// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.RenderUtils;
import musheor.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * "ice-rail-builder" — semi-automated ice-highway builder. Lays a blue-ice rail line
 * with crying-obsidian supports. Note: this module is unfinished — only the Z-axis
 * anchor path is implemented and the target coordinates are hardcoded around
 * (±200, 115, ∓200).
 */
public class IceRailBuilder extends Module {
    private final Setting<Axis> axis = this.settings.getDefaultGroup().add(new EnumSetting.Builder<Axis>() // was: rKbT3Ifwo
        .name("axis").defaultValue(Axis.WEST).build());

    BlockPos xAxisAnchor;                            // was: FvaNWO
    BlockPos zAxisAnchor;                            // was: Q90GLXQ0Pef
    List<BlockPos> iceTargets = new ArrayList<>();   // was: psJq59YIbp3Z (positions needing blue ice)
    List<BlockPos> supportTargets = new ArrayList<>(); // was: SOYyh5IPg26f7F (positions needing crying obsidian)

    public IceRailBuilder() {
        super(musheor.AUTOMATION, "ice-rail-builder", "Semi-automated ice highway building");
    }

    @Override
    public void onActivate() {
        if (this.mc.player != null && this.mc.world != null) {
            this.configureAnchors(this.axis.get());
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        if (this.xAxisAnchor != null) {
            // X-axis path is unimplemented in this build.
        }

        if (this.zAxisAnchor != null) {
            this.iceTargets.clear();
            this.supportTargets.clear();

            for (BlockPos pos : this.getRailPositions(true)) {
                if (this.mc.world.getBlockState(pos).getBlock() != Blocks.BLUE_ICE) {
                    this.iceTargets.add(pos.toImmutable());
                }
            }
            for (BlockPos pos : this.getSupportPositions(true)) {
                if (this.mc.world.getBlockState(pos).isReplaceable()) {
                    this.supportTargets.add(pos.toImmutable());
                }
            }

            if (!this.supportTargets.isEmpty()) {
                WorldUtils.placeBlocks(this.supportTargets, Blocks.CRYING_OBSIDIAN);
                return;
            }
            if (!this.iceTargets.isEmpty()) {
                WorldUtils.placeBlocks(this.iceTargets, Blocks.BLUE_ICE);
                return;
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (this.mc.player == null || this.mc.world == null) return;
        RenderUtils.render(event, this.supportTargets, Blocks.CRYING_OBSIDIAN);
        RenderUtils.render(event, this.iceTargets, Blocks.BLUE_ICE);
    }

    /** Chooses which axis anchor to build from based on {@code axis}. */
    private void configureAnchors(Axis axis) { // was: FvaNWO(Axis)
        this.xAxisAnchor = this.zAxisAnchor = null;
        switch (axis) {
            case NORTH, SOUTH -> this.xAxisAnchor = new BlockPos(0, 115, -200);
            case EAST, WEST -> this.zAxisAnchor = new BlockPos(-200, 115, 0);
        }
    }

    /** Crying-obsidian support positions flanking the rail (±2 along X). */
    private List<BlockPos> getSupportPositions(boolean enabled) { // was: FvaNWO(boolean)
        List<BlockPos> positions = new ArrayList<>();
        if (enabled) {
            for (int i = -2; i < 2; i++) {
                positions.add(new BlockPos(this.mc.player.getBlockX() + i, 116, -201));
                positions.add(new BlockPos(this.mc.player.getBlockX() + i, 116, -198));
                positions.add(new BlockPos(this.mc.player.getBlockX() + i, 113, -199));
            }
        }
        return positions;
    }

    /** Blue-ice rail positions along the Z=-200 line (every other block). */
    private List<BlockPos> getRailPositions(boolean enabled) { // was: Q90GLXQ0Pef(boolean)
        List<BlockPos> positions = new ArrayList<>();
        if (enabled) {
            for (int i = -5; i < 5; i++) {
                if ((this.mc.player.getBlockX() + i) % 2 == 0) {
                    positions.add(new BlockPos(this.mc.player.getBlockX() + i, 115, -200));
                }
            }
        }
        return positions;
    }

    /** Build axis. */ // was: enum Axis {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z, SOYyh5IPg26f7F}
    private enum Axis { NORTH, SOUTH, EAST, WEST }
}
