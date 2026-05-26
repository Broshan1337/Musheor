// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import net.minecraft.Blocks;   // Blocks
import net.minecraft.BlockPos;   // BlockPos

/**
 * Semi-automated ice highway builder.
 *
 * When activated, sets up start/end positions for the selected axis and
 * each tick fills in:
 *   - Ice blocks (packed_ice) at the floor positions where they are missing
 *   - Obsidian blocks at the wall positions that should be solid
 *
 * The Axis enum selects which of the 4 cardinal directions to build toward.
 * Hard-coded coordinates (Y=115, Z=±200 / X=±200) are the nether highway plane
 * on 2b2t — this module was purpose-built for that server.
 *
 * NOTE: Axis enum ordinal mapping:
 *   0 = NORTH_POS  (ordinal 0 → Z start: 0,115,-200)
 *   1 = NORTH_NEG  (ordinal 1 → Z start: 0,115,-200)
 *   2 = EAST_POS   (ordinal 2 → X start: -200,115,0)
 *   3 = EAST_NEG   (ordinal 3 → X start: -200,115,0)
 */
public class IceRailBuilder extends Module {
    private final Setting<Axis> axis;

    BlockPos startPos;       // was: uKCgvn9Jo
    BlockPos currentPos;     // was: oPbBR3Ndv7FUN

    /** Positions where packed ice needs to be placed. */
    List<BlockPos> iceTodo;   // was: qfVsw28lZNgTVJ
    /** Positions where obsidian walls need to be placed. */
    List<BlockPos> wallTodo;  // was: Rdr7On

    public IceRailBuilder() {
        super(musheor.AUTOMATION, "ice-rail-builder", "Semi-automated ice highway building");
        this.axis = this.settings.getDefaultGroup().add(
            new EnumSetting.Builder<Axis>()
                .name("axis")
                .defaultValue(Axis.EAST_NEG)  // was: BoRaO9Zi
                .build());
        this.iceTodo  = new ArrayList<BlockPos>();
        this.wallTodo = new ArrayList<BlockPos>();
    }

    @Override
    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) return;
        initPositions((Axis) ((Object) this.axis.get()));
    }

    @EventHandler
    public void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) return;
        if (this.currentPos == null) return;

        this.iceTodo.clear();
        this.wallTodo.clear();

        // Find ice positions that are not yet packed_ice
        for (BlockPos pos : getIcePositions(true)) {
            if (this.mc.world.getBlockState(pos).getBlock() == Blocks.BLUE_ICE) continue; // packed_ice
            this.iceTodo.add(pos.toImmutable()); // toImmutable()
        }
        // Find wall positions that should be solid
        for (BlockPos pos : getWallPositions(true)) {
            if (!this.mc.world.getBlockState(pos).isAir()) continue; // isAir()
            this.wallTodo.add(pos.toImmutable());
        }

        // Place walls first (they act as support for the ice)
        if (!this.wallTodo.isEmpty()) {
            WorldUtils.placeBlockList(this.wallTodo, Blocks.NETHERRACK); // was: jOdDDFXSeWl4(List,Block) — obsidian
            return;
        }
        if (!this.iceTodo.isEmpty()) {
            WorldUtils.placeBlockList(this.iceTodo, Blocks.BLUE_ICE); // packed_ice
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.player == null || this.mc.world == null) return;
        RenderUtils.renderBlockList(event, this.wallTodo, Blocks.NETHERRACK); // was: jOdDDFXSeWl4(Render3DEvent,List,Block)
        RenderUtils.renderBlockList(event, this.iceTodo,  Blocks.BLUE_ICE);
    }

    /** Sets start/current positions based on the chosen axis. */
    private void initPositions(Axis ax) { // was: jOdDDFXSeWl4(Axis)
        this.currentPos = null;
        this.startPos   = null;
        switch (ax.ordinal()) {
            case 0: case 1: this.startPos   = new BlockPos(0,    115, -200); break;
            case 2: case 3: this.currentPos = new BlockPos(-200, 115,  0);   break;
        }
    }

    /** Returns the wall block positions adjacent to the current track position. */
    private List<BlockPos> getWallPositions(boolean active) { // was: jOdDDFXSeWl4(boolean)
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        if (active) {
            for (int i = -2; i < 2; ++i) {
                list.add(new BlockPos(this.mc.player.getX() + i, 116, -201));
                list.add(new BlockPos(this.mc.player.getX() + i, 116, -198));
                list.add(new BlockPos(this.mc.player.getX() + i, 113, -199));
            }
        }
        return list;
    }

    /** Returns the ice floor positions near the player on even X coordinates. */
    private List<BlockPos> getIcePositions(boolean active) { // was: mp3zoXQFKUKYj5(boolean)
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        if (active) {
            for (int i = -5; i < 5; ++i) {
                if ((this.mc.player.getX() + i) % 2 != 0) continue;
                list.add(new BlockPos(this.mc.player.getX() + i, 115, -200));
            }
        }
        return list;
    }

    /** The 4 directions the ice rail can be built toward. */
    static final class Axis extends Enum<Axis> {
        public static final Axis NORTH_POS  = new Axis(); // was: qLxZ6NFp0a  (ordinal 0)
        public static final Axis NORTH_NEG  = new Axis(); // was: pVxy7PIGPRYXRSbk (ordinal 1)
        public static final Axis EAST_POS   = new Axis(); // was: gkM0N3m  (ordinal 2)
        public static final Axis EAST_NEG   = new Axis(); // was: BoRaO9Zi (ordinal 3, default)
        private static final Axis[] $VALUES = new Axis[]{NORTH_POS, NORTH_NEG, EAST_POS, EAST_NEG};

        public static Axis[] values()             { return (Axis[]) $VALUES.clone(); }
        public static Axis valueOf(String string) { return Enum.valueOf(Axis.class, string); }
    }
}
