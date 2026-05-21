// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.compat;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.util.LayerRange;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import musheor.compat.LitematicaHelper;
import musheor.compat.VersionHelper;
import musheor.utils.WorldUtils;
import net.minecraft.FluidBlock;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.BlockPos;
import net.minecraft.BlockState;
import net.minecraft.MinecraftClient;

public class LitematicaHelperImpl
implements LitematicaHelper {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static File getSchematicsDirectory() {
        return new File(LitematicaHelperImpl.mc.field_1697, "schematics");
    }

    private static File findSchematicByName(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        File file = LitematicaHelperImpl.getSchematicsDirectory();
        if (!file.exists()) {
            return null;
        }
        if (!string.endsWith(".litematic") && !string.endsWith(".nbt")) {
            File file2 = LitematicaHelperImpl.searchDirectory(file, string + ".litematic");
            if (file2 != null) {
                return file2;
            }
            File file3 = LitematicaHelperImpl.searchDirectory(file, string + ".nbt");
            if (file3 != null) {
                return file3;
            }
            return LitematicaHelperImpl.searchDirectory(file, string);
        }
        return LitematicaHelperImpl.searchDirectory(file, string);
    }

    private static File searchDirectory(File file, String string) {
        if (!file.isDirectory()) {
            return null;
        }
        File[] fileArray = file.listFiles();
        if (fileArray == null) {
            return null;
        }
        for (File file2 : fileArray) {
            if (!file2.isFile() || !file2.getName().equalsIgnoreCase(string)) continue;
            return file2;
        }
        for (File file2 : fileArray) {
            File file3;
            if (!file2.isDirectory() || (file3 = LitematicaHelperImpl.searchDirectory(file2, string)) == null) continue;
            return file3;
        }
        return null;
    }

    @Override
    public boolean ensureSchematicAt(String string, BlockPos BlockPos2) {
        String string2;
        if (string == null || string.isEmpty()) {
            return false;
        }
        SchematicPlacementManager schematicPlacementManager = DataManager.getSchematicPlacementManager();
        for (SchematicPlacement schematicPlacement : schematicPlacementManager.getAllSchematicsPlacements()) {
            String string3;
            string2 = schematicPlacement.getName().toLowerCase();
            if (!string2.contains(string3 = string.replace(".litematic", "").replace(".nbt", "").toLowerCase())) continue;
            schematicPlacement.setOrigin(BlockPos2, null);
            schematicPlacementManager.setSelectedSchematicPlacement(schematicPlacement);
            ChatUtils.info((String)"Schematic found in world, moved to desired position", (Object[])new Object[0]);
            return true;
        }
        File file = LitematicaHelperImpl.findSchematicByName(string);
        if (file == null) {
            ChatUtils.info((String)"Schematic not found", (Object[])new Object[0]);
            return false;
        }
        try {
            SchematicPlacement schematicPlacement;
            schematicPlacement = (LitematicaSchematic)VersionHelper.get().getSchematicFromFile(file, file.getName());
            if (schematicPlacement == null) {
                return false;
            }
            string2 = SchematicPlacement.createFor((LitematicaSchematic)schematicPlacement, (BlockPos)BlockPos2, (String)file.getName(), (boolean)true, (boolean)true);
            schematicPlacementManager.addSchematicPlacement((SchematicPlacement)string2, true);
            schematicPlacementManager.setSelectedSchematicPlacement((SchematicPlacement)string2);
            return true;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public void clearAllPlacements() {
        SchematicPlacementManager schematicPlacementManager = DataManager.getSchematicPlacementManager();
        for (SchematicPlacement schematicPlacement : schematicPlacementManager.getAllSchematicsPlacements()) {
            if (schematicPlacement == null) continue;
            schematicPlacementManager.removeSchematicPlacement(schematicPlacement);
        }
    }

    @Override
    public boolean verifySchematic() {
        SchematicPlacement schematicPlacement = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        return schematicPlacement != null && worldSchematic != null;
    }

    @Override
    public Map<BlockPos, BlockState> getBlocksInBox(BlockPos BlockPos2, BlockPos BlockPos3, boolean bl, List<Block> list, int n) {
        LinkedHashMap<BlockPos, BlockState> linkedHashMap = new LinkedHashMap<BlockPos, BlockState>();
        if (LitematicaHelperImpl.mc.world == null) {
            return linkedHashMap;
        }
        for (SchematicPlacement schematicPlacement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            ImmutableMap immutableMap;
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (schematicPlacement == null || worldSchematic == null || (immutableMap = schematicPlacement.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY)) == null || immutableMap.isEmpty()) continue;
            block1: for (Box box : immutableMap.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos BlockPos4 = box.getPos1();
                BlockPos BlockPos5 = box.getPos2();
                int n2 = Math.max(Math.min(BlockPos4.getX(), BlockPos5.getX()), BlockPos2.getX());
                int n3 = Math.min(Math.max(BlockPos4.getX(), BlockPos5.getX()), BlockPos3.getX());
                int n4 = Math.max(Math.min(BlockPos4.getY(), BlockPos5.getY()), BlockPos2.getY());
                int n5 = Math.min(Math.max(BlockPos4.getY(), BlockPos5.getY()), BlockPos3.getY());
                int n6 = Math.max(Math.min(BlockPos4.getZ(), BlockPos5.getZ()), BlockPos2.getZ());
                int n7 = Math.min(Math.max(BlockPos4.getZ(), BlockPos5.getZ()), BlockPos3.getZ());
                if (n2 > n3 || n4 > n5 || n6 > n7) continue;
                for (int i = n2; i <= n3; ++i) {
                    for (int j = n4; j <= n5; ++j) {
                        for (int k = n6; k <= n7; ++k) {
                            if (linkedHashMap.size() >= n) continue block1;
                            BlockPos BlockPos6 = new BlockPos(i, j, k);
                            BlockState BlockState2 = worldSchematic.getBlockState(BlockPos6);
                            if (BlockState2 == null || BlockState2.isAir() || list != null && list.contains(BlockState2.getBlock())) continue;
                            BlockState BlockState3 = LitematicaHelperImpl.mc.world.getBlockState(BlockPos6);
                            if (bl ? !(BlockState3.getBlock() instanceof FluidBlock) : BlockState3.getBlock() == BlockState2.getBlock()) continue;
                            linkedHashMap.put(BlockPos6, BlockState2);
                        }
                    }
                }
            }
        }
        return linkedHashMap;
    }

    @Override
    public Map<Block, Integer> getMaterialCounts(List<Block> list) {
        HashMap<Block, Integer> hashMap = new HashMap<Block, Integer>();
        for (SchematicPlacement schematicPlacement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            ImmutableMap immutableMap;
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (schematicPlacement == null || worldSchematic == null || (immutableMap = schematicPlacement.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY)) == null || immutableMap.isEmpty()) continue;
            for (Box box : immutableMap.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos BlockPos2 = box.getPos1();
                BlockPos BlockPos3 = box.getPos2();
                int n = Math.min(BlockPos2.getX(), BlockPos3.getX());
                int n2 = Math.max(BlockPos2.getX(), BlockPos3.getX());
                int n3 = Math.min(BlockPos2.getY(), BlockPos3.getY());
                int n4 = Math.max(BlockPos2.getY(), BlockPos3.getY());
                int n5 = Math.min(BlockPos2.getZ(), BlockPos3.getZ());
                int n6 = Math.max(BlockPos2.getZ(), BlockPos3.getZ());
                for (int i = n; i <= n2; ++i) {
                    for (int j = n3; j <= n4; ++j) {
                        for (int k = n5; k <= n6; ++k) {
                            BlockState BlockState2 = worldSchematic.getBlockState(new BlockPos(i, j, k));
                            if (BlockState2 == null || BlockState2.isAir()) continue;
                            Block Block2 = BlockState2.getBlock();
                            if (list != null && list.contains(Block2)) continue;
                            hashMap.merge(Block2, 1, Integer::sum);
                        }
                    }
                }
            }
        }
        return hashMap;
    }

    @Override
    public BlockPos findClosestUnplacedBlock(BlockPos BlockPos2, int n, boolean bl, List<Block> list) {
        if (LitematicaHelperImpl.mc.world == null) {
            return null;
        }
        BlockPos BlockPos3 = BlockPos2.method_10069(-n, -n, -n);
        BlockPos BlockPos4 = BlockPos2.method_10069(n, n, n);
        BlockPos BlockPos5 = null;
        double d = Double.MAX_VALUE;
        for (SchematicPlacement schematicPlacement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            ImmutableMap immutableMap;
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (schematicPlacement == null || worldSchematic == null || (immutableMap = schematicPlacement.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY)) == null || immutableMap.isEmpty()) continue;
            for (Box box : immutableMap.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos BlockPos6 = box.getPos1();
                BlockPos BlockPos7 = box.getPos2();
                int n2 = Math.max(Math.min(BlockPos6.getX(), BlockPos7.getX()), BlockPos3.getX());
                int n3 = Math.min(Math.max(BlockPos6.getX(), BlockPos7.getX()), BlockPos4.getX());
                int n4 = Math.max(Math.min(BlockPos6.getY(), BlockPos7.getY()), BlockPos3.getY());
                int n5 = Math.min(Math.max(BlockPos6.getY(), BlockPos7.getY()), BlockPos4.getY());
                int n6 = Math.max(Math.min(BlockPos6.getZ(), BlockPos7.getZ()), BlockPos3.getZ());
                int n7 = Math.min(Math.max(BlockPos6.getZ(), BlockPos7.getZ()), BlockPos4.getZ());
                if (n2 > n3 || n4 > n5 || n6 > n7) continue;
                for (int i = n2; i <= n3; ++i) {
                    for (int j = n4; j <= n5; ++j) {
                        for (int k = n6; k <= n7; ++k) {
                            double d2;
                            BlockPos BlockPos8 = new BlockPos(i, j, k);
                            BlockState BlockState2 = worldSchematic.getBlockState(BlockPos8);
                            if (BlockState2 == null || BlockState2.isAir() || list != null && list.contains(BlockState2.getBlock())) continue;
                            BlockState BlockState3 = LitematicaHelperImpl.mc.world.getBlockState(BlockPos8);
                            if ((!bl ? BlockState3.getBlock() == BlockState2.getBlock() : !(BlockState3.getBlock() instanceof FluidBlock)) || !((d2 = BlockPos2.method_10262((BlockPos)BlockPos8)) < d)) continue;
                            d = d2;
                            BlockPos5 = BlockPos8;
                        }
                    }
                }
            }
        }
        return BlockPos5;
    }

    @Override
    public boolean isPositionInRenderLayer(BlockPos BlockPos2) {
        LayerRange layerRange = DataManager.getRenderLayerRange();
        return layerRange.isPositionWithinRange(BlockPos2);
    }

    @Override
    public List<BlockPos[]> getSchematicRegionBounds() {
        ArrayList<BlockPos[]> arrayList = new ArrayList<BlockPos[]>();
        for (SchematicPlacement schematicPlacement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            ImmutableMap immutableMap = schematicPlacement.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY);
            if (immutableMap == null) continue;
            for (Box box : immutableMap.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos BlockPos2 = box.getPos1();
                BlockPos BlockPos3 = box.getPos2();
                arrayList.add(new BlockPos[]{new BlockPos(Math.min(BlockPos2.getX(), BlockPos3.getX()), Math.min(BlockPos2.getY(), BlockPos3.getY()), Math.min(BlockPos2.getZ(), BlockPos3.getZ())), new BlockPos(Math.max(BlockPos2.getX(), BlockPos3.getX()), Math.max(BlockPos2.getY(), BlockPos3.getY()), Math.max(BlockPos2.getZ(), BlockPos3.getZ()))});
            }
        }
        return arrayList;
    }

    @Override
    public List<BlockPos> getWrongSchematicBlocks(double d, boolean bl) {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        if (LitematicaHelperImpl.mc.player == null || LitematicaHelperImpl.mc.world == null) {
            return arrayList;
        }
        for (SchematicPlacement schematicPlacement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (schematicPlacement == null || worldSchematic == null) {
                return arrayList;
            }
            ImmutableMap immutableMap = schematicPlacement.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY);
            if (immutableMap == null || immutableMap.isEmpty()) {
                return arrayList;
            }
            for (Box box : immutableMap.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos BlockPos2 = box.getPos1();
                BlockPos BlockPos3 = box.getPos2();
                int n = Math.min(BlockPos2.getX(), BlockPos3.getX());
                int n2 = Math.max(BlockPos2.getX(), BlockPos3.getX());
                int n3 = Math.min(BlockPos2.getY(), BlockPos3.getY());
                int n4 = Math.max(BlockPos2.getY(), BlockPos3.getY());
                int n5 = Math.min(BlockPos2.getZ(), BlockPos3.getZ());
                int n6 = Math.max(BlockPos2.getZ(), BlockPos3.getZ());
                int n7 = LitematicaHelperImpl.mc.player.getX();
                int n8 = LitematicaHelperImpl.mc.player.getY();
                int n9 = LitematicaHelperImpl.mc.player.getZ();
                int n10 = Math.max(n, n7 - 16);
                int n11 = Math.min(n2, n7 + 16);
                int n12 = Math.max(n3, n8 - 16);
                int n13 = Math.min(n4, n8 + 16);
                int n14 = Math.max(n5, n9 - 16);
                int n15 = Math.min(n6, n9 + 16);
                if (n10 > n11 || n12 > n13 || n14 > n15) continue;
                for (int i = n10; i <= n11; ++i) {
                    for (int j = n12; j <= n13; ++j) {
                        for (int k = n14; k <= n15; ++k) {
                            BlockPos BlockPos4 = new BlockPos(i, j, k);
                            BlockState BlockState2 = worldSchematic.getBlockState(BlockPos4);
                            BlockState BlockState3 = LitematicaHelperImpl.mc.world.getBlockState(BlockPos4);
                            if (BlockState3.isAir() || bl && BlockState2.isAir() || BlockState2 == BlockState3 || !WorldUtils.jOdDDFXSeWl4(BlockPos4, d)) continue;
                            arrayList.add(BlockPos4);
                        }
                    }
                }
            }
        }
        return arrayList;
    }
}

