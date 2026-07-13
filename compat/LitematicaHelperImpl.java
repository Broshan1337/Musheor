// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class/members readable; only Minecraft class refs and one WorldUtils call were intermediary.
package musheor.compat;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement.RequiredEnabled;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.FileType;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.util.LayerRange;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import musheor.utils.WorldUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * The active {@link LitematicaHelper} — bridges directly to Litematica's schematic placement
 * data, comparing the loaded schematic against the world. Installed only when Litematica is
 * present. Schematic file loading falls back through reflection to handle API differences
 * across Litematica versions.
 */
public class LitematicaHelperImpl implements LitematicaHelper {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static File getSchematicsDirectory() {
        return new File(mc.runDirectory, "schematics");
    }

    private static File findSchematicByName(String name) {
        if (name == null || name.isEmpty()) return null;
        File schematicsDir = getSchematicsDirectory();
        if (!schematicsDir.exists()) return null;
        if (!name.endsWith(".litematic") && !name.endsWith(".nbt")) {
            File litematicFile = searchDirectory(schematicsDir, name + ".litematic");
            if (litematicFile != null) return litematicFile;
            File nbtFile = searchDirectory(schematicsDir, name + ".nbt");
            return nbtFile != null ? nbtFile : searchDirectory(schematicsDir, name);
        }
        return searchDirectory(schematicsDir, name);
    }

    private static File searchDirectory(File dir, String fileName) {
        if (!dir.isDirectory()) return null;
        File[] files = dir.listFiles();
        if (files == null) return null;
        for (File file : files) {
            if (file.isFile() && file.getName().equalsIgnoreCase(fileName)) return file;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                File found = searchDirectory(file, fileName);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Loads a schematic, trying the Path-based then File-based {@code createFromFile} overload. */
    private static LitematicaSchematic loadSchematic(File file) {
        FileType fileType = FileType.fromFile(file);
        try {
            Method m = LitematicaSchematic.class.getMethod("createFromFile", Path.class, String.class, FileType.class);
            return (LitematicaSchematic) m.invoke(null, file.getParentFile().toPath(), file.getName(), fileType);
        } catch (NoSuchMethodException notPath) {
            try {
                Method m = LitematicaSchematic.class.getMethod("createFromFile", File.class, String.class, FileType.class);
                return (LitematicaSchematic) m.invoke(null, file.getParentFile(), file.getName(), fileType);
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean ensureSchematicAt(String schematicName, BlockPos pos) {
        if (schematicName == null || schematicName.isEmpty()) return false;
        SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();

        for (SchematicPlacement placement : manager.getAllSchematicsPlacements()) {
            String name = placement.getName().toLowerCase();
            String search = schematicName.replace(".litematic", "").replace(".nbt", "").toLowerCase();
            if (name.contains(search)) {
                placement.setOrigin(pos, null);
                manager.setSelectedSchematicPlacement(placement);
                ChatUtils.info("Schematic found in world, moved to desired position");
                return true;
            }
        }

        File file = findSchematicByName(schematicName);
        if (file == null) {
            ChatUtils.info("Schematic not found");
            return false;
        }
        try {
            LitematicaSchematic schematic = loadSchematic(file);
            if (schematic == null) return false;
            SchematicPlacement placement = SchematicPlacement.createFor(schematic, pos, file.getName(), true, true);
            manager.addSchematicPlacement(placement, true);
            manager.setSelectedSchematicPlacement(placement);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void clearAllPlacements() {
        SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();
        for (SchematicPlacement placement : manager.getAllSchematicsPlacements()) {
            if (placement != null) manager.removeSchematicPlacement(placement);
        }
    }

    @Override
    public boolean verifySchematic() {
        SchematicPlacement placement = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        return placement != null && worldSchematic != null;
    }

    @Override
    public Map<BlockPos, BlockState> getBlocksInBox(BlockPos min, BlockPos max, boolean onlyAir, List<Block> ignoredBlocks, int maxResults) {
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        if (mc.world == null) return result;

        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (placement == null || worldSchematic == null) continue;
            Map<String, Box> subRegions = placement.getSubRegionBoxes(RequiredEnabled.ANY);
            if (subRegions == null || subRegions.isEmpty()) continue;
            nextBox:
            for (Box box : subRegions.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos p1 = box.getPos1();
                BlockPos p2 = box.getPos2();
                int scanMinX = Math.max(Math.min(p1.getX(), p2.getX()), min.getX());
                int scanMaxX = Math.min(Math.max(p1.getX(), p2.getX()), max.getX());
                int scanMinY = Math.max(Math.min(p1.getY(), p2.getY()), min.getY());
                int scanMaxY = Math.min(Math.max(p1.getY(), p2.getY()), max.getY());
                int scanMinZ = Math.max(Math.min(p1.getZ(), p2.getZ()), min.getZ());
                int scanMaxZ = Math.min(Math.max(p1.getZ(), p2.getZ()), max.getZ());
                if (scanMinX > scanMaxX || scanMinY > scanMaxY || scanMinZ > scanMaxZ) continue;
                for (int x = scanMinX; x <= scanMaxX; x++) {
                    for (int y = scanMinY; y <= scanMaxY; y++) {
                        for (int z = scanMinZ; z <= scanMaxZ; z++) {
                            if (result.size() >= maxResults) continue nextBox;
                            BlockPos worldPos = new BlockPos(x, y, z);
                            BlockState schematicState = worldSchematic.getBlockState(worldPos);
                            if (schematicState != null && !schematicState.isAir()
                                && (ignoredBlocks == null || !ignoredBlocks.contains(schematicState.getBlock()))) {
                                BlockState currentState = mc.world.getBlockState(worldPos);
                                if (onlyAir ? currentState.getBlock() instanceof AirBlock : currentState.getBlock() != schematicState.getBlock()) {
                                    result.put(worldPos, schematicState);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map<Block, Integer> getRemainingMaterialCounts(List<Block> ignoredBlocks, boolean onlyAir) {
        Map<Block, Integer> counts = new HashMap<>();
        if (mc.world == null) return counts;

        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (placement == null || worldSchematic == null) continue;
            Map<String, Box> subRegions = placement.getSubRegionBoxes(RequiredEnabled.ANY);
            if (subRegions == null || subRegions.isEmpty()) continue;
            for (Box box : subRegions.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos p1 = box.getPos1();
                BlockPos p2 = box.getPos2();
                int minX = Math.min(p1.getX(), p2.getX()), maxX = Math.max(p1.getX(), p2.getX());
                int minY = Math.min(p1.getY(), p2.getY()), maxY = Math.max(p1.getY(), p2.getY());
                int minZ = Math.min(p1.getZ(), p2.getZ()), maxZ = Math.max(p1.getZ(), p2.getZ());
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!mc.world.isChunkLoaded(x >> 4, z >> 4)) continue;
                        for (int y = minY; y <= maxY; y++) {
                            BlockPos worldPos = new BlockPos(x, y, z);
                            BlockState schematicState = worldSchematic.getBlockState(worldPos);
                            if (schematicState != null && !schematicState.isAir()) {
                                Block block = schematicState.getBlock();
                                if (ignoredBlocks == null || !ignoredBlocks.contains(block)) {
                                    BlockState currentState = mc.world.getBlockState(worldPos);
                                    boolean needed = onlyAir ? currentState.getBlock() instanceof AirBlock : currentState.getBlock() != block;
                                    if (needed) counts.merge(block, 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            }
        }
        return counts;
    }

    @Override
    public Map<Block, Integer> getMaterialCounts(List<Block> ignoredBlocks) {
        Map<Block, Integer> counts = new HashMap<>();
        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (placement == null || worldSchematic == null) continue;
            Map<String, Box> subRegions = placement.getSubRegionBoxes(RequiredEnabled.ANY);
            if (subRegions == null || subRegions.isEmpty()) continue;
            for (Box box : subRegions.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos p1 = box.getPos1();
                BlockPos p2 = box.getPos2();
                int minX = Math.min(p1.getX(), p2.getX()), maxX = Math.max(p1.getX(), p2.getX());
                int minY = Math.min(p1.getY(), p2.getY()), maxY = Math.max(p1.getY(), p2.getY());
                int minZ = Math.min(p1.getZ(), p2.getZ()), maxZ = Math.max(p1.getZ(), p2.getZ());
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            BlockState schematicState = worldSchematic.getBlockState(new BlockPos(x, y, z));
                            if (schematicState != null && !schematicState.isAir()) {
                                Block block = schematicState.getBlock();
                                if (ignoredBlocks == null || !ignoredBlocks.contains(block)) counts.merge(block, 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }
        return counts;
    }

    @Override
    public BlockPos findClosestUnplacedBlock(BlockPos center, int radius, boolean onlyAir, List<Block> ignoredBlocks) {
        if (mc.world == null) return null;
        BlockPos min = center.add(-radius, -radius, -radius);
        BlockPos max = center.add(radius, radius, radius);
        BlockPos closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (placement == null || worldSchematic == null) continue;
            Map<String, Box> subRegions = placement.getSubRegionBoxes(RequiredEnabled.ANY);
            if (subRegions == null || subRegions.isEmpty()) continue;
            for (Box box : subRegions.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos p1 = box.getPos1();
                BlockPos p2 = box.getPos2();
                int scanMinX = Math.max(Math.min(p1.getX(), p2.getX()), min.getX());
                int scanMaxX = Math.min(Math.max(p1.getX(), p2.getX()), max.getX());
                int scanMinY = Math.max(Math.min(p1.getY(), p2.getY()), min.getY());
                int scanMaxY = Math.min(Math.max(p1.getY(), p2.getY()), max.getY());
                int scanMinZ = Math.max(Math.min(p1.getZ(), p2.getZ()), min.getZ());
                int scanMaxZ = Math.min(Math.max(p1.getZ(), p2.getZ()), max.getZ());
                if (scanMinX > scanMaxX || scanMinY > scanMaxY || scanMinZ > scanMaxZ) continue;
                for (int x = scanMinX; x <= scanMaxX; x++) {
                    for (int y = scanMinY; y <= scanMaxY; y++) {
                        for (int z = scanMinZ; z <= scanMaxZ; z++) {
                            BlockPos worldPos = new BlockPos(x, y, z);
                            BlockState schematicState = worldSchematic.getBlockState(worldPos);
                            if (schematicState != null && !schematicState.isAir()
                                && (ignoredBlocks == null || !ignoredBlocks.contains(schematicState.getBlock()))) {
                                BlockState currentState = mc.world.getBlockState(worldPos);
                                if (onlyAir ? currentState.getBlock() instanceof AirBlock : currentState.getBlock() != schematicState.getBlock()) {
                                    double distSq = center.getSquaredDistance(worldPos);
                                    if (distSq < closestDistSq) {
                                        closestDistSq = distSq;
                                        closest = worldPos;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return closest;
    }

    @Override
    public boolean isPositionInRenderLayer(BlockPos pos) {
        LayerRange renderRange = DataManager.getRenderLayerRange();
        return renderRange.isPositionWithinRange(pos);
    }

    @Override
    public List<BlockPos> getWrongBlocksInBox(BlockPos min, BlockPos max, boolean ignoreAir, int maxResults) {
        List<BlockPos> result = new ArrayList<>();
        if (mc.world == null) return result;

        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (placement == null || worldSchematic == null) continue;
            Map<String, Box> subRegions = placement.getSubRegionBoxes(RequiredEnabled.ANY);
            if (subRegions == null || subRegions.isEmpty()) continue;
            nextBox:
            for (Box box : subRegions.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos p1 = box.getPos1();
                BlockPos p2 = box.getPos2();
                int scanMinX = Math.max(Math.min(p1.getX(), p2.getX()), min.getX());
                int scanMaxX = Math.min(Math.max(p1.getX(), p2.getX()), max.getX());
                int scanMinY = Math.max(Math.min(p1.getY(), p2.getY()), min.getY());
                int scanMaxY = Math.min(Math.max(p1.getY(), p2.getY()), max.getY());
                int scanMinZ = Math.max(Math.min(p1.getZ(), p2.getZ()), min.getZ());
                int scanMaxZ = Math.min(Math.max(p1.getZ(), p2.getZ()), max.getZ());
                if (scanMinX > scanMaxX || scanMinY > scanMaxY || scanMinZ > scanMaxZ) continue;
                for (int x = scanMinX; x <= scanMaxX; x++) {
                    for (int y = scanMinY; y <= scanMaxY; y++) {
                        for (int z = scanMinZ; z <= scanMaxZ; z++) {
                            if (result.size() >= maxResults) continue nextBox;
                            BlockPos worldPos = new BlockPos(x, y, z);
                            BlockState currentState = mc.world.getBlockState(worldPos);
                            if (!currentState.isAir()) {
                                BlockState schematicState = worldSchematic.getBlockState(worldPos);
                                if ((!ignoreAir || schematicState != null && !schematicState.isAir()) && schematicState != currentState) {
                                    result.add(worldPos);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasRemainingPositions(Block block, boolean onlyAir, List<Block> ignoredBlocks) {
        if (mc.world == null) return false;
        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (placement == null || worldSchematic == null) continue;
            Map<String, Box> subRegions = placement.getSubRegionBoxes(RequiredEnabled.ANY);
            if (subRegions == null || subRegions.isEmpty()) continue;
            for (Box box : subRegions.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos p1 = box.getPos1();
                BlockPos p2 = box.getPos2();
                int minX = Math.min(p1.getX(), p2.getX()), maxX = Math.max(p1.getX(), p2.getX());
                int minY = Math.min(p1.getY(), p2.getY()), maxY = Math.max(p1.getY(), p2.getY());
                int minZ = Math.min(p1.getZ(), p2.getZ()), maxZ = Math.max(p1.getZ(), p2.getZ());
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            BlockPos worldPos = new BlockPos(x, y, z);
                            BlockState schematicState = worldSchematic.getBlockState(worldPos);
                            if (schematicState != null && !schematicState.isAir() && schematicState.getBlock() == block
                                && (ignoredBlocks == null || !ignoredBlocks.contains(block))) {
                                BlockState currentState = mc.world.getBlockState(worldPos);
                                if (onlyAir) {
                                    if (currentState.getBlock() instanceof AirBlock) return true;
                                } else if (currentState.getBlock() != block) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<BlockPos[]> getSchematicRegionBounds() {
        List<BlockPos[]> bounds = new ArrayList<>();
        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            Map<String, Box> subRegions = placement.getSubRegionBoxes(RequiredEnabled.ANY);
            if (subRegions == null) continue;
            for (Box box : subRegions.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos p1 = box.getPos1();
                BlockPos p2 = box.getPos2();
                bounds.add(new BlockPos[]{
                    new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ())),
                    new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()))
                });
            }
        }
        return bounds;
    }

    @Override
    public List<BlockPos> getWrongSchematicBlocks(double range, boolean ignoreAir) {
        List<BlockPos> wrongBlocks = new ArrayList<>();
        if (mc.player == null || mc.world == null) return wrongBlocks;

        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (placement == null || worldSchematic == null) return wrongBlocks;
            Map<String, Box> subRegions = placement.getSubRegionBoxes(RequiredEnabled.ANY);
            if (subRegions == null || subRegions.isEmpty()) return wrongBlocks;
            for (Box box : subRegions.values()) {
                if (box == null || box.getPos1() == null || box.getPos2() == null) continue;
                BlockPos pos1 = box.getPos1();
                BlockPos pos2 = box.getPos2();
                int regionMinX = Math.min(pos1.getX(), pos2.getX()), regionMaxX = Math.max(pos1.getX(), pos2.getX());
                int regionMinY = Math.min(pos1.getY(), pos2.getY()), regionMaxY = Math.max(pos1.getY(), pos2.getY());
                int regionMinZ = Math.min(pos1.getZ(), pos2.getZ()), regionMaxZ = Math.max(pos1.getZ(), pos2.getZ());
                int px = mc.player.getBlockX(), py = mc.player.getBlockY(), pz = mc.player.getBlockZ();
                int minX = Math.max(regionMinX, px - 16), maxX = Math.min(regionMaxX, px + 16);
                int minY = Math.max(regionMinY, py - 16), maxY = Math.min(regionMaxY, py + 16);
                int minZ = Math.max(regionMinZ, pz - 16), maxZ = Math.min(regionMaxZ, pz + 16);
                if (minX > maxX || minY > maxY || minZ > maxZ) continue;
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            BlockPos worldPos = new BlockPos(x, y, z);
                            BlockState schematicState = worldSchematic.getBlockState(worldPos);
                            BlockState currentState = mc.world.getBlockState(worldPos);
                            if (!currentState.isAir() && (!ignoreAir || !schematicState.isAir())
                                && schematicState != currentState && WorldUtils.isWithinRange(worldPos, range)) {
                                wrongBlocks.add(worldPos);
                            }
                        }
                    }
                }
            }
        }
        return wrongBlocks;
    }
}
