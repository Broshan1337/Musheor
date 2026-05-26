// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import musheor.modules.automation.EchestFarmer;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.KekNuker;
import musheor.modules.automation.SourceRemover;
import musheor.utils.BlockPositions;
import musheor.utils.InventoryManager;
import musheor.utils.PlayerUtils;
import musheor.utils.StatsHandler;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Per-tick logic dispatcher for all highway-building modes.
 *
 * HighwayBuilder's onTick calls one of:
 *   handleCardinalHighwayTick()   — straight N/S/E/W highways
 *   handleDiagonalHighwayTick()   — diagonal highways
 *   handleIceRailTick()           — ice-floor cardinal highways
 *   handleDiagonalIceRailTick()   — ice-floor diagonal highways
 *
 * Echest farming interruption is handled by handleEchestFarming() / handlePostEchestFarm().
 */
public class Handlers {
    public static final MinecraftClient mc = MinecraftClient.getInstance();        // was: r9l7h0HpZAuA
    public static final List<BlockPos> visitedPositions = new ArrayList<BlockPos>(); // was: D2cyo0

    // -------------------------------------------------------------------------
    // Echest farming helpers
    // -------------------------------------------------------------------------

    /**
     * Called when the highway builder decides to farm echests.
     * Enables EchestFarmer, sets the target amount, and handles lag detection
     * by pausing AutoWalk and returning to the highway if Baritone isn't pathing.
     */
    public static void handleEchestFarming() { // was: gANxWblT
        HighwayState state = HighwayState.getInstance();
        PlayerUtils.setModuleSetting(EchestFarmer.class, "self-toggle", true);
        state.setEchestFarmPos(mc.player.getBlockPos());
        if (WorldUtils.isLagDetected()) {
            PlayerUtils.setAutoWalkActive(false);
            if (!PathingHelper.isAlreadyPathing()) {
                WorldUtils.returnToHighway();
            }
            return;
        }
        if (!state.isEchestFarming()) {
            assert mc.player != null;
            int needed = InventoryManager.countItem(Items.ENDER_CHEST) - 8;
            if (needed > 0) {
                PlayerUtils.setModuleSetting(EchestFarmer.class, "amount",
                    Math.min(needed, InventoryManager.countShulkerBoxes() * 8));
            }
            PlayerUtils.setAutoWalkActive(false);
            state.setAutoWalkEnabled(true);
            Module module = Modules.get().get(EchestFarmer.class);
            if (!module.isActive()) {
                state.setEchestFarmerEnabled(true);
                module.toggle();
            }
        }
    }

    /**
     * Called after echest farming finishes.
     * If EchestFarmer left an incorrect block (ender chest still placed),
     * breaks it. Once the block is clear and GatherItem is done, re-enables
     * obsidian gathering and clears the farming flags.
     */
    public static void handlePostEchestFarm() { // was: PlefynG
        HighwayState state = HighwayState.getInstance();
        if (!HighwayBuilder.isRestocking() && state.wasEchestFarmerEnabled()) {
            if (EchestFarmer.echestPos != null
                    && mc.world.getBlockState(EchestFarmer.echestPos).getBlock() == Blocks.ENDER_CHEST) {
                MusheorSystem.debug("Attempting to break incorrect block...", new Object[0]);
                BlockUtils.breakBlock((BlockPos) EchestFarmer.echestPos, true);
                return;
            }
            if (PlayerUtils.isGatheringItem()) return;
            PlayerUtils.startGatherItem(Items.OBSIDIAN, false);
            state.setAutoWalkEnabled(false);
            state.setEchestFarmerEnabled(false);
        }
    }

    // -------------------------------------------------------------------------
    // Cardinal highway tick
    // -------------------------------------------------------------------------

    /**
     * Main tick handler for straight (cardinal N/S/E/W) highway building.
     *
     * Each tick:
     *  1. Returns early if HighwayState is not fully initialised
     *  2. Pauses walking if EchestFarmer, GatherItem, or SourceRemover is running
     *  3. Scans floor positions (−3 to +5 ahead) for missing blocks → path back
     *  4. Scans clear positions for blockages behind the player → path forward
     *  5. Fills KekNuker's queue with blocks that need to be removed
     *  6. Places floor and ceiling blocks via WorldUtils.tryPlaceBlocks()
     *  7. Enables AutoWalk when all placement/nuking is idle
     */
    public static void handleCardinalHighwayTick() { // was: Pmh3HuqB53i0Y
        GoalBlock goalBlock;
        Block block;
        assert mc.player != null && mc.world != null;
        HighwayState state = HighwayState.getInstance();

        // Require all position anchors to be set before doing any work
        if (state.getDirection() == null
                || state.getAlignStartX() == null
                || state.getHighwayY() == null
                || state.getAlignStartZ() == null) {
            return;
        }

        Module echestModule = Modules.get().get(EchestFarmer.class);

        // Debug map: log which subsystem paused walking this tick
        LinkedHashMap<String, Boolean> pauseReasons = new LinkedHashMap<String, Boolean>();
        pauseReasons.put("BetterEchestFarmer", echestModule.isActive());
        pauseReasons.put("Gathering Items",    PlayerUtils.isGatheringItem());
        pauseReasons.put("Removing Lava",      SourceRemover.isActive());

        if (echestModule.isActive() || PlayerUtils.isGatheringItem() || SourceRemover.isActive()) {
            PlayerUtils.setAutoWalkActive(false);
            for (Map.Entry entry : pauseReasons.entrySet()) {
                if (!((Boolean) entry.getValue()).booleanValue()) continue;
                MusheorSystem.debug((String) entry.getKey() + " was triggered.", new Object[0]);
            }
            return;
        }

        // Pause walking while KekNuker is running, restocking, breaking shulker, etc.
        if (KekNuker.isNuking()
                || HighwayBuilder.isEating()
                || HighwayBuilder.isWaiting()
                || InventoryManager.isBreakingShulker) {
            PlayerUtils.setAutoWalkActive(false);
        }

        // In AutoWalk mode: keep the player facing the highway and strafe to center
        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) {
            PlayerUtils.alignWithBaritoneXZ();
            WorldUtils.checkForwardCollisions();
            PlayerUtils.alignLookToHighway();
            PlayerUtils.applyStrafing();
        }

        // Floor positions to check/place for the current cross-section (behind=2, ahead=3)
        BlockPos[] floorPositions = BlockPositions.getFloorPositions(
            2, 3, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall());
        state.setPlacedFloor(false);
        WorldUtils.detectAndHandleSpleef(floorPositions); // was: jOdDDFXSeWl4(BlockPos[])

        List<Block> nukerBlacklist = KekNuker.getBlacklist();
        state.setFoundMissingBlock(false);
        BlockPos missingPos = null;
        boolean blockedBehind = false;
        BlockPos blockedPos = null;

        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) {
            // Scan for missing floor blocks (behind=−3, ahead=+5)
            for (BlockPos pos : BlockPositions.getFloorPositions(-3, 5, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall())) {
                block = mc.world.getBlockState(pos).getBlock();
                if (!HighwayBuilder.allowLava() && block == Blocks.CRYING_OBSIDIAN
                        || block == HighwayBuilder.getFloorBlock()
                        || StatsHandler.getObsidianCount() <= 10
                        || nukerBlacklist.contains(block)) continue;
                state.setFoundMissingBlock(true);
                missingPos = pos;
                break;
            }
            // Optionally check ceiling positions too
            if (HighwayBuilder.hasCeiling()) {
                for (BlockPos pos : BlockPositions.getCeilingPositions(-3, 4, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall())) {
                    block = mc.world.getBlockState(pos).getBlock();
                    if (!HighwayBuilder.allowLava() && block == Blocks.CRYING_OBSIDIAN
                            || block == Blocks.BEDROCK
                            || block != Blocks.AIR
                            || StatsHandler.getObsidianCount() <= 10
                            || nukerBlacklist.contains(block)) continue;
                    state.setFoundMissingBlock(true);
                    missingPos = pos;
                    break;
                }
            }
            // Scan for obstacles in the clear (passage) zone behind the player
            for (BlockPos pos : BlockPositions.getForwardClearPositions()) {
                block = mc.world.getBlockState(pos).getBlock();
                if (block instanceof AirBlock
                        || StatsHandler.getObsidianCount() <= 10
                        || nukerBlacklist.contains(block)) continue;
                blockedBehind = true;
                blockedPos = pos;
                break;
            }
        }

        // If there's a block behind blocking movement, path just past it
        if (blockedBehind) {
            PlayerUtils.setAutoWalkActive(false);
            MusheorSystem.debug("Detected blockage behind player... %s",
                mc.world.getBlockState(blockedPos).getBlock().getName());
            assert blockedPos != null;
            goalBlock = switch (HighwayBuilder.getDirection()) {
                case WorldUtils.Direction8.NORTH -> new GoalBlock(state.getAlignStartX().intValue(), state.getHighwayY().intValue(), blockedPos.getZ() + 2);
                case WorldUtils.Direction8.WEST  -> new GoalBlock(blockedPos.getX() - 2,     state.getHighwayY().intValue(), state.getAlignStartZ().intValue());
                case WorldUtils.Direction8.SOUTH -> new GoalBlock(state.getAlignStartX().intValue(), state.getHighwayY().intValue(), blockedPos.getZ() - 2);
                case WorldUtils.Direction8.EAST  -> new GoalBlock(blockedPos.getX() + 2,     state.getHighwayY().intValue(), state.getAlignStartZ().intValue());
                default -> new GoalBlock(state.getAlignStartX().intValue(), state.getHighwayY().intValue(), state.getAlignStartZ().intValue());
            };
            PathingHelper.setBaritoneGoal((Goal) goalBlock);
            PathingHelper.startPathing();
            return;
        }

        // If there's a missing floor block, path back to it
        if (state.foundMissingBlock()) {
            PlayerUtils.setAutoWalkActive(false);
            MusheorSystem.debug("Found missing block, going back...", new Object[0]);
            assert missingPos != null;
            goalBlock = switch (HighwayBuilder.getDirection()) {
                case WorldUtils.Direction8.NORTH, WorldUtils.Direction8.SOUTH ->
                    new GoalBlock(state.getAlignStartX().intValue(), state.getHighwayY().intValue(), missingPos.getZ());
                case WorldUtils.Direction8.WEST,  WorldUtils.Direction8.EAST ->
                    new GoalBlock(missingPos.getX(), state.getHighwayY().intValue(), state.getAlignStartZ().intValue());
                default ->
                    new GoalBlock(missingPos.getX(), state.getHighwayY().intValue(), missingPos.getZ());
            };
            PathingHelper.setBaritoneGoal((Goal) goalBlock);
            PathingHelper.startPathing();
        }

        // Build KekNuker's queue: anything in the floor or clear zone that needs breaking
        if (((Boolean) HighwayBuilder.INSTANCE.enableNuker.get()).booleanValue()) {
            KekNuker.nukerQueue.clear();
            for (BlockPos pos : floorPositions) {
                block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.NETHER_PORTAL
                        || block instanceof AirBlock
                        || !HighwayBuilder.allowLava() && block == Blocks.CRYING_OBSIDIAN
                        || !WorldUtils.needsPlacement(pos, HighwayBuilder.getFloorBlock())) continue;
                KekNuker.nukerQueue.add(pos.toImmutable());
            }
            for (BlockPos pos : BlockPositions.getClearPositions(2, 3)) {
                block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.NETHER_PORTAL
                        || block instanceof AirBlock
                        || !WorldUtils.needsPlacement(pos, Blocks.AIR)) continue;
                KekNuker.nukerQueue.add(pos.toImmutable());
            }
        }

        // Place floor and ceiling blocks
        state.setPlacedFloor(false);
        WorldUtils.tryPlaceBlocks(floorPositions, false);
        if (HighwayBuilder.hasCeiling()) {
            WorldUtils.tryPlaceBlocks(BlockPositions.getCeilingPositions(2, 2, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall()), false);
        }

        // Resume walking if nothing is pending
        if (!(state.isFloorPlaced()
                && state.isCeilingPlaced()
                && state.foundMissingBlock()
                && KekNuker.isNuking()
                && state.isObsidianReady())) {
            PlayerUtils.setAutoWalkActive(true);
        }
    }

    // -------------------------------------------------------------------------
    // Diagonal highway tick
    // -------------------------------------------------------------------------

    /**
     * Main tick handler for diagonal highway building (NW/NE/SW/SE).
     *
     * Mirrors handleCardinalHighwayTick() with:
     *  - Diagonal BlockPositions methods used instead of cardinal ones
     *  - Diagonal alignment tracking (equal X/Z delta → update align coords)
     *  - GoalBlock offsets use ±8 instead of ±2 when returning to missing blocks
     */
    public static void handleDiagonalHighwayTick() { // was: eNsdDMk8mJXTb
        BlockPos blockedPos;
        Block block;
        BlockPos missingPos;
        Module echestModule;
        assert mc.player != null && mc.world != null;
        HighwayState state = HighwayState.getInstance();

        if (state.getDirection() == null || state.getAlignStartX() == null
                || state.getHighwayY() == null || state.getAlignStartZ() == null) {
            return;
        }

        // Track diagonal alignment: when |Δx| == |Δz| we are exactly on the diagonal axis
        double dx = mc.player.getX() - state.getDiagonalCenter().getX();
        double dz = mc.player.getZ() - state.getDiagonalCenter().getZ();
        if (Math.abs(dx) == Math.abs(dz)) {
            state.setAlignZ(mc.player.getZ());
            state.setAlignX(mc.player.getX());
        }

        if (WorldUtils.isLagDetected()) {
            PlayerUtils.setAutoWalkActive(false);
            if (!PathingHelper.isAlreadyPathing()
                    && state.getLavaTargetBlock() == null
                    && state.getLavaSourceBlock() == null) {
                WorldUtils.returnToHighway();
            }
        }

        if ((echestModule = Modules.get().get(EchestFarmer.class)).isActive()
                || PlayerUtils.isGatheringItem()
                || SourceRemover.isActive()) {
            PlayerUtils.setAutoWalkActive(false);
            return;
        }

        if (KekNuker.isNuking() || HighwayBuilder.isEating() || HighwayBuilder.isWaiting()
                || InventoryManager.isBreakingShulker) {
            PlayerUtils.setAutoWalkActive(false);
        }

        if (HighwayBuilder.isCheckingSpleef()) {
            WorldUtils.handleLavaRemoval(); // was: selaO6lwe7
        }

        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) {
            PlayerUtils.alignWithBaritone();
            WorldUtils.checkForwardCollisions();
            PlayerUtils.alignLookToHighway();
        }

        List<Block> nukerBlacklist = KekNuker.getBlacklist();
        boolean blockedBehind = false;
        BlockPos blockedBehindPos = null;

        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) {
            // Check for obstacles in the passage zone behind the player
            for (BlockPos pos : BlockPositions.getDiagonalClearPositions(-3, 5)) {
                Block b = mc.world.getBlockState(pos).getBlock();
                if (b instanceof AirBlock || StatsHandler.getObsidianCount() <= 10
                        || nukerBlacklist.contains(b)) continue;
                blockedBehind = true;
                blockedBehindPos = pos;
                break;
            }

            if (blockedBehind) {
                PlayerUtils.setAutoWalkActive(false);
                MusheorSystem.debug("Detected blockage behind player... "
                    + Registries.BLOCK.getId(mc.world.getBlockState(blockedBehindPos).getBlock()).getPath(),
                    new Object[0]);
                assert blockedBehindPos != null;
                int offset = 4;
                GoalBlock goalBlock = switch (HighwayBuilder.getDirection()) {
                    case WorldUtils.Direction8.NORTH_WEST -> new GoalBlock(state.getAlignStartX() - offset, state.getHighwayY().intValue(), state.getAlignStartZ() + offset);
                    case WorldUtils.Direction8.NORTH_EAST -> new GoalBlock(state.getAlignStartX() + offset, state.getHighwayY().intValue(), state.getAlignStartZ() + offset);
                    case WorldUtils.Direction8.SOUTH_WEST -> new GoalBlock(state.getAlignStartX() - offset, state.getHighwayY().intValue(), state.getAlignStartZ() - offset);
                    case WorldUtils.Direction8.SOUTH_EAST -> new GoalBlock(state.getAlignStartX() + offset, state.getHighwayY().intValue(), state.getAlignStartZ() - offset);
                    default -> new GoalBlock(blockedBehindPos.getX(), state.getHighwayY().intValue(), blockedBehindPos.getZ());
                };
                PathingHelper.setBaritoneGoal((Goal) goalBlock);
                PathingHelper.startPathing();
                return;
            }

            // Scan diagonal floor for missing blocks
            state.setFoundMissingBlock(false);
            missingPos = null;
            for (BlockPos pos : BlockPositions.getDiagonalFloorPositions(-3, 4, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall())) {
                block = mc.world.getBlockState(pos).getBlock();
                if (!HighwayBuilder.allowLava() && block == Blocks.CRYING_OBSIDIAN
                        || block == HighwayBuilder.getFloorBlock()
                        || StatsHandler.getObsidianCount() <= 10
                        || block == Blocks.CRYING_OBSIDIAN && !HighwayBuilder.allowLava()
                        || block instanceof AirBlock
                        || nukerBlacklist.contains(block)) continue;
                state.setFoundMissingBlock(true);
                missingPos = pos;
                break;
            }
            if (HighwayBuilder.hasCeiling()) {
                for (BlockPos pos : BlockPositions.getDiagonalCeilingPositions(-3, 4, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall())) {
                    block = mc.world.getBlockState(pos).getBlock();
                    if (!HighwayBuilder.allowLava() && block == Blocks.CRYING_OBSIDIAN
                            || block != Blocks.AIR
                            || StatsHandler.getObsidianCount() <= 10
                            || nukerBlacklist.contains(block)) continue;
                    state.setFoundMissingBlock(true);
                    missingPos = pos;
                    break;
                }
            }

            if (state.foundMissingBlock()) {
                PlayerUtils.setAutoWalkActive(false);
                MusheorSystem.debug("Found missing block, going back...", new Object[0]);
                assert missingPos != null;
                RenderUtils.renderTickingBlock((BlockPos) missingPos, (Color) Color.WHITE, (Color) Color.WHITE,
                    (ShapeMode) ShapeMode.Lines, 0, 20, true, false);
                BlockPos goal = switch (HighwayBuilder.getDirection()) {
                    case WorldUtils.Direction8.NORTH_WEST -> new GoalBlock(state.getAlignStartX() - 8, state.getHighwayY().intValue(), state.getAlignStartZ() + 8);
                    case WorldUtils.Direction8.NORTH_EAST -> new GoalBlock(state.getAlignStartX() + 8, state.getHighwayY().intValue(), state.getAlignStartZ() + 8);
                    case WorldUtils.Direction8.SOUTH_WEST -> new GoalBlock(state.getAlignStartX() - 8, state.getHighwayY().intValue(), state.getAlignStartZ() - 8);
                    case WorldUtils.Direction8.SOUTH_EAST -> new GoalBlock(state.getAlignStartX() + 8, state.getHighwayY().intValue(), state.getAlignStartZ() - 8);
                    default -> new GoalBlock(missingPos.getX(), state.getHighwayY().intValue(), missingPos.getZ());
                };
                PathingHelper.setBaritoneGoal((Goal) goal);
                PathingHelper.startPathing();
                return;
            }
        }

        // Spleef-check diagonal floor positions before placing
        BlockPos[] diagonalFloor = BlockPositions.getDiagonalFloorPositions(2, 2, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall());
        state.setPlacedFloor(false);
        WorldUtils.detectAndHandleSpleef(diagonalFloor); // was: jOdDDFXSeWl4(BlockPos[])

        // Build KekNuker queue for diagonal cross-section
        if (((Boolean) HighwayBuilder.INSTANCE.enableNuker.get()).booleanValue()) {
            KekNuker.nukerQueue.clear();
            for (BlockPos pos : diagonalFloor) {
                block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.NETHER_PORTAL || block instanceof AirBlock
                        || !HighwayBuilder.allowLava() && block == Blocks.CRYING_OBSIDIAN
                        || !WorldUtils.needsPlacement(pos, HighwayBuilder.getFloorBlock())) continue;
                KekNuker.nukerQueue.add(pos.toImmutable());
            }
            for (BlockPos pos : BlockPositions.getDiagonalClearPositions(3, 0)) {
                block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.NETHER_PORTAL || block instanceof AirBlock
                        || !WorldUtils.needsPlacement(pos, Blocks.AIR)) continue;
                KekNuker.nukerQueue.add(pos.toImmutable());
            }
        }

        state.setPlacedFloor(false);
        WorldUtils.tryPlaceBlocks((BlockPos[]) diagonalFloor, false);
        BlockPos[] diagonalCeiling = BlockPositions.getDiagonalCeilingPositions(2, 1, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall());
        if (HighwayBuilder.hasCeiling()) {
            // Stop walking if any ceiling position is within range and can be placed
            for (BlockPos pos : diagonalCeiling) {
                if (!WorldUtils.isInPlacementRange(pos) || !BlockUtils.canPlace((BlockPos) pos, true)) continue; // was: KDNrzlU9qtrEv
                PlayerUtils.setAutoWalkActive(false);
            }
            WorldUtils.tryPlaceBlocks((BlockPos[]) diagonalCeiling, false);
        }

        if (!(state.isFloorPlaced() && state.isCeilingPlaced() && state.foundMissingBlock()
                && KekNuker.isNuking() && state.isObsidianReady())) {
            PlayerUtils.setAutoWalkActive(true);
        }
    }

    // -------------------------------------------------------------------------
    // Ice rail tick (cardinal)
    // -------------------------------------------------------------------------

    /**
     * Tick handler for cardinal ice-highway building.
     *
     * Manages ice-floor placement using either the Scaffold or IceFloor strategy
     * (controlled by HighwayBuilder.getScaffoldMode()):
     *   - SCAFFOLD: places blocks from inventory
     *   - ICE_FLOOR: uses WorldUtils.buildIceFloor()
     *
     * Also feeds KekNuker with obstructions in the clear zone,
     * and detects the floating-gap pattern that requires Baritone re-alignment.
     */
    public static void handleIceRailTick() { // was: Z6nxChaWC9ymwoio
        if (mc.player == null || mc.world == null) return;
        HighwayState state = HighwayState.getInstance();

        if (PlayerUtils.isGatheringItem() || HighwayBuilder.isEating()
                || InventoryManager.isBreakingShulker || HighwayBuilder.isWaiting()
                || SourceRemover.isActive() && Modules.get().get("source-remover").isActive()) {
            PlayerUtils.setAutoWalkActive(false);
            return;
        }

        if (mc.player.getY() < (double) state.getHighwayY().intValue()) {
            PlayerUtils.setAutoWalkActive(false);
        }

        if (HighwayBuilder.isCheckingSpleef()) {
            WorldUtils.handleLavaRemoval(); // was: selaO6lwe7
        }

        PlayerUtils.alignLookToHighway();
        PlayerUtils.applyStrafing();
        WorldUtils.checkForwardCollisions();

        // If using scaffold mode: ensure ice is in inventory, path to restock if needed
        if (HighwayBuilder.getScaffoldMode() != HighwayBuilder.ScaffoldMode.ICE_FLOOR) {
            if (InventoryManager.findItemSlot(HighwayBuilder.getIceItem().asItem()) == null) {
                state.setRestockTarget(mc.player.getBlockPos());
                WorldUtils.returnToHighway();
                return;
            }
            if (state.getRestockGoal() != null
                    && InventoryManager.findItemSlot(HighwayBuilder.getIceItem().asItem()) != null) {
                PathingHelper.setGoal(state.getRestockGoal());
            }
            state.setRestockTarget(null);
            if (PathingHelper.isAlreadyPathing()) return;
        }

        // Place ice scaffold or run IceFloor builder
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.SCAFFOLD) {
            WorldUtils.tryPlaceBlocks(
                BlockPositions.getFloorPositions(3, 2, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall()), true);
        }
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.ICE_FLOOR) {
            WorldUtils.buildIceFloor();
        }

        // Add obstructions in the clear zone to KekNuker's queue
        for (BlockPos pos : BlockPositions.getClearPositions(3, 2)) {
            BlockState blockState = mc.world.getBlockState(pos);
            if (blockState.getBlock() instanceof AirBlock
                    || blockState.getBlock() == Blocks.NETHER_PORTAL
                    || blockState.getBlock() instanceof AirBlock
                    || !WorldUtils.needsPlacement(pos, Blocks.AIR)) continue;
            KekNuker.nukerQueue.add(pos.toImmutable());
        }

        // Detect forward gap: if a block in the passage is floating (no adjacent lava face),
        // path to the gap using GoalXZ so the player walks up to it
        for (BlockPos pos : BlockPositions.getForwardClearPositions()) {
            if (!(mc.world.getBlockState(pos).getBlock() instanceof AirBlock)
                    && StatsHandler.ticksWithoutBlock > 5) {
                boolean hasAdjacentLava = false;
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = pos.offset(dir);
                    FluidState fluidState = mc.world.getFluidState(neighbor);
                    if (fluidState.getFluid() != Fluids.LAVA) continue;
                    hasAdjacentLava = true;
                    break;
                }
                if (hasAdjacentLava) continue;
                PlayerUtils.setAutoWalkActive(false);
                GoalXZ goal = switch (HighwayBuilder.getDirection()) {
                    case WorldUtils.Direction8.NORTH, WorldUtils.Direction8.SOUTH ->
                        new GoalXZ(state.getAlignStartX().intValue(), pos.getZ());
                    case WorldUtils.Direction8.WEST, WorldUtils.Direction8.EAST ->
                        new GoalXZ(pos.getX(), state.getAlignStartZ().intValue());
                    default ->
                        new GoalXZ(pos.getX(), pos.getZ());
                };
                PathingHelper.setBaritoneGoal((Goal) goal);
                PathingHelper.startPathing();
                return;
            }
            PlayerUtils.setAutoWalkActive(true);
        }
    }

    // -------------------------------------------------------------------------
    // Ice rail tick (diagonal)
    // -------------------------------------------------------------------------

    /**
     * Tick handler for diagonal ice-highway building.
     *
     * Like handleIceRailTick() but uses diagonal BlockPositions methods.
     * Additionally runs placeSafetyBlock() to prevent the player falling off
     * the diagonal edge during construction.
     */
    public static void handleDiagonalIceRailTick() { // was: AoH6MX
        if (mc.player == null || mc.world == null) return;
        HighwayState state = HighwayState.getInstance();

        if (state.getDiagonalCenter() == null) {
            ChatUtils.error("Set player center using .center", new Object[0]);
            PlayerUtils.disableHighwayBuilder();
            return;
        }

        // Track diagonal alignment
        double dx = mc.player.getX() - state.getDiagonalCenter().getX();
        double dz = mc.player.getZ() - state.getDiagonalCenter().getZ();
        if (Math.abs(dx) == Math.abs(dz)) {
            state.setAlignX(mc.player.getX());
            state.setAlignZ(mc.player.getZ());
        }

        List<Block> nukerBlacklist = KekNuker.getBlacklist();

        LinkedHashMap<String, Boolean> pauseReasons = new LinkedHashMap<String, Boolean>();
        pauseReasons.put("Gathering Items", PlayerUtils.isGatheringItem());
        pauseReasons.put("Removing Lava",   SourceRemover.isActive());
        pauseReasons.put("Eating",          HighwayBuilder.isEating());

        if (HighwayBuilder.isEating() || PlayerUtils.isGatheringItem()
                || SourceRemover.isActive() || HighwayBuilder.isWaiting()) {
            PlayerUtils.setAutoWalkActive(false);
            for (Map.Entry entry : pauseReasons.entrySet()) {
                if (!((Boolean) entry.getValue()).booleanValue()) continue;
                MusheorSystem.debug((String) entry.getKey() + " was triggered.", new Object[0]);
            }
            return;
        }

        if (HighwayBuilder.isCheckingSpleef()) {
            WorldUtils.handleLavaRemoval(); // was: selaO6lwe7
        }

        PlayerUtils.alignLookToHighway();
        PlayerUtils.alignWithBaritone();
        WorldUtils.checkForwardCollisions();

        // Scaffold mode: ensure netherrack is in inventory
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.SCAFFOLD) {
            if (InventoryManager.findItemSlot(Items.NETHERRACK) == null) {
                WorldUtils.returnToHighway();
            }
            WorldUtils.tryPlaceBlocks(
                BlockPositions.getDiagonalFloorPositions(2, 1, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall()), true);
        }
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.ICE_FLOOR) {
            if (InventoryManager.findItemSlot(Items.NETHERRACK) == null) {
                WorldUtils.returnToHighway();
            }
            WorldUtils.buildIceFloor();
        }

        if (KekNuker.isNuking()) return;

        // Safety: place a block so the player doesn't fall off the diagonal edge
        if (WorldUtils.placeSafetyBlock()) {
            MusheorSystem.debug("Placing block so player doesn't fall...", new Object[0]);
            PlayerUtils.setAutoWalkActive(false);
            return;
        }

        // Add clear-zone obstructions to KekNuker
        for (BlockPos pos : BlockPositions.getDiagonalClearPositions(3, 2)) {
            BlockState blockState = mc.world.getBlockState(pos);
            if (blockState.getBlock() instanceof AirBlock
                    || blockState.getBlock() == Blocks.NETHER_PORTAL
                    || blockState.getBlock() instanceof AirBlock
                    || !WorldUtils.needsPlacement(pos, Blocks.AIR)) continue;
            KekNuker.nukerQueue.add(pos.toImmutable());
        }

        // Detect obstacle behind player in the diagonal passage zone
        for (BlockPos pos : BlockPositions.getDiagonalClearPositions(-3, 5)) {
            if (!(mc.world.getBlockState(pos).getBlock() instanceof AirBlock)
                    && StatsHandler.ticksWithoutBlock > 5) {
                boolean hasAdjacentLava = false;
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = pos.offset(dir);
                    FluidState fluidState = mc.world.getFluidState(neighbor);
                    if (fluidState.getFluid() != Fluids.LAVA) continue;
                    hasAdjacentLava = true;
                    break;
                }
                if (hasAdjacentLava || nukerBlacklist.contains(mc.world.getBlockState(pos).getBlock())) continue;
                MusheorSystem.debug("Detected obstruction behind player... "
                    + Registries.BLOCK.getId(mc.world.getBlockState(pos).getBlock()).getPath()
                    + "at %s %s %s",
                    pos.getX(), pos.getY(), pos.getZ());
                PlayerUtils.setAutoWalkActive(false);
                PathingHelper.setBaritoneGoal((Goal) new GoalXZ(pos.getX(), pos.getX()));
                PathingHelper.startPathing();
                return;
            }
            PlayerUtils.setAutoWalkActive(true);
        }
    }
}