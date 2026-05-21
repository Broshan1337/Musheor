// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.compat;

import java.util.List;
import java.util.Map;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.BlockState;

public interface LitematicaHelper {
    public boolean ensureSchematicAt(String var1, BlockPos var2);

    public void clearAllPlacements();

    public boolean verifySchematic();

    public Map<BlockPos, BlockState> getBlocksInBox(BlockPos var1, BlockPos var2, boolean var3, List<Block> var4, int var5);

    public Map<Block, Integer> getMaterialCounts(List<Block> var1);

    public BlockPos findClosestUnplacedBlock(BlockPos var1, int var2, boolean var3, List<Block> var4);

    public boolean isPositionInRenderLayer(BlockPos var1);

    public List<BlockPos> getWrongSchematicBlocks(double var1, boolean var3);

    public List<BlockPos[]> getSchematicRegionBounds();

    public static boolean isLoaded() {
        return LitematicaHelperHolder.INSTANCE != null;
    }

    public static LitematicaHelper get() {
        return LitematicaHelperHolder.INSTANCE;
    }

    public static void setInstance(LitematicaHelper litematicaHelper) {
        LitematicaHelperHolder.INSTANCE = litematicaHelper;
    }

    public static class LitematicaHelperHolder {
        static LitematicaHelper INSTANCE = null;
    }
}

