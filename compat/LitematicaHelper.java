// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Interface and members were already readable.
package musheor.compat;

import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Optional-dependency bridge to Litematica. A {@code LitematicaHelperImpl} is installed
 * (via reflection) only when Litematica is present; {@link #isLoaded()} gates every call.
 * Exposes schematic loading, per-block target queries, remaining-material counts, and the
 * "wrong block" positions used by {@link musheor.modules.automation.Printer} / KekNuker.
 */
public interface LitematicaHelper {
    boolean ensureSchematicAt(String name, BlockPos pos);

    void clearAllPlacements();

    boolean verifySchematic();

    Map<BlockPos, BlockState> getBlocksInBox(BlockPos min, BlockPos max, boolean onlyAir, List<Block> ignored, int limit);

    Map<Block, Integer> getMaterialCounts(List<Block> ignored);

    Map<Block, Integer> getRemainingMaterialCounts(List<Block> ignored, boolean onlyAir);

    BlockPos findClosestUnplacedBlock(BlockPos origin, int range, boolean onlyAir, List<Block> ignored);

    boolean isPositionInRenderLayer(BlockPos pos);

    List<BlockPos> getWrongSchematicBlocks(double range, boolean ignoreAir);

    List<BlockPos> getWrongBlocksInBox(BlockPos min, BlockPos max, boolean includeAir, int limit);

    boolean hasRemainingPositions(Block block, boolean onlyAir, List<Block> ignored);

    List<BlockPos[]> getSchematicRegionBounds();

    static boolean isLoaded() {
        return LitematicaHelperHolder.INSTANCE != null;
    }

    static LitematicaHelper get() {
        return LitematicaHelperHolder.INSTANCE;
    }

    static void setInstance(LitematicaHelper instance) {
        LitematicaHelperHolder.INSTANCE = instance;
    }

    class LitematicaHelperHolder {
        static LitematicaHelper INSTANCE = null;
    }
}
