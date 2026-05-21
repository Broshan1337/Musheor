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
import net.minecraft.Items;   // Items
import net.minecraft.class_2189;   // FluidBlock (lava/water)
import net.minecraft.Blocks;   // Blocks
import net.minecraft.Block;   // Block
import net.minecraft.BlockPos;   // BlockPos
import net.minecraft.Direction;   // Direction
import net.minecraft.class_2680;   // BlockState
import net.minecraft.MinecraftClient;    // MinecraftClient
import net.minecraft.class_3610;   // Chunk
import net.minecraft.class_3612;   // ChunkStatus
import net.minecraft.class_7923;   // Registries

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
    public static final MinecraftClient mc = MinecraftClient.method_1551();        // MinecraftClient — was: r9l7h0HpZAuA
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
        HighwayState state = HighwayState.getInstance(); // was: LmpuWjra
        PlayerUtils.setModuleSetting(EchestFarmer.class, "self-toggle", true);
        state.setEchestFarmPos(mc.player.getBlockPos()); // getBlockPos()
        if (WorldUtils.isLagDetected()) {
            PlayerUtils.setAutoWalkActive(false);
            if (!PathingHelper.isAlreadyPathing()) {
                WorldUtils.returnToHighway();
            }
            return;
        }
        if (!state.isEchestFarming()) { // was: Y775oeIufYz9
            assert mc.field_1724 != null;
            int needed = InventoryManager.countItem(Items.field_8466) - 8; // obsidian count minus buffer
            if (needed > 0) {
                PlayerUtils.setModuleSetting(EchestFarmer.class, "amount",
                    Math.min(needed, InventoryManager.countShulkerBoxes() * 8)); // was: ZeOLrA
            }
            PlayerUtils.setAutoWalkActive(false);
            state.setAutoWalkEnabled(true); // was: MS1x7YGHjIg7eB
            Module module = Modules.get().get(EchestFarmer.class);
            if (!module.isActive()) {
                state.setEchestFarmerEnabled(true); // was: CEOjBr5G5R
                module.toggle();
            }
        }
    }

    /**
     * Called after echest farming finishes.
     * If EchestFarmer left an incorrect block (non-air where the echest was),
     * breaks it. Once the block is clear and GatherItem is done, re-enables
     * cobblestone gathering and clears the farming flags.
     */
    public static void handlePostEchestFarm() { // was: PlefynG
        HighwayState state = HighwayState.getInstance();
        if (!HighwayBuilder.isRestocking() && state.wasEchestFarmerEnabled()) { // was: rpvWtoVonf6GeT
            if (EchestFarmer.echestPos != null // was: zl2vxyh
                    && mc.world.getBlockState(EchestFarmer.echestPos).getBlock() == Blocks.field_10443) { // air
                MusheorSystem.debug("Attempting to break incorrect block...", new Object[0]);
                BlockUtils.breakBlock((BlockPos) EchestFarmer.echestPos, true);
                return;
            }
            if (PlayerUtils.isGatheringItem()) return;
            PlayerUtils.startGatherItem(Items.field_8281, false); // cobblestone
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
        assert mc.field_1724 != null && mc.field_1687 != null;
        HighwayState state = HighwayState.getInstance();

        // Require all position anchors to be set before doing any work
        if (state.getDirection() == null         // was: P7WK4vInkqbLg
                || state.getAlignStartX() == null   // was: Icks58Pk4vQH3
                || state.getHighwayY() == null       // was: KaWPzeyl1xVKHWo
                || state.getAlignStartZ() == null) { // was: A02ApsqZGj
            return;
        }

        Module echestModule = Modules.get().get(EchestFarmer.class);

        // Debug map: log which subsystem paused walking this tick
        LinkedHashMap<String, Boolean> pauseReasons = new LinkedHashMap<String, Boolean>();
        pauseReasons.put("BetterEchestFarmer", echestModule.isActive());
        pauseReasons.put("Gathering Items",    PlayerUtils.isGatheringItem());
        pauseReasons.put("Removing Lava",      SourceRemover.isActive()); // was: fXEQFU

        if (echestModule.isActive() || PlayerUtils.isGatheringItem() || SourceRemover.isActive()) {
            PlayerUtils.setAutoWalkActive(false);
            for (Map.Entry entry : pauseReasons.entrySet()) {
                if (!((Boolean) entry.getValue()).booleanValue()) continue;
                MusheorSystem.debug((String) entry.getKey() + " was triggered.", new Object[0]);
            }
            return;
        }

        // Pause walking while KekNuker is running, restocking, breaking shulker, etc.
        if (KekNuker.isNuking()              // was: Pa3aVwRtUo45jMG
                || HighwayBuilder.isEating()     // was: S7TLszvzENsW7
                || HighwayBuilder.isWaiting()    // was: zl2vxyh
                || InventoryManager.isBreakingShulker) { // was: hdUw3g5aanfWo0
            PlayerUtils.setAutoWalkActive(false);
        }

        // In AutoWalk mode: keep the player facing the highway and strafe to center
        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) { // was: jll9Iyc1Ftxi
            PlayerUtils.alignWithBaritoneXZ(); // was: HUYtvX
            WorldUtils.checkForwardCollisions(); // was: gBxN0D8GSyidOa
            PlayerUtils.alignLookToHighway();
            PlayerUtils.applyStrafing();
        }

        // Floor positions to check/place for the current cross-section (behind=2, ahead=3)
        BlockPos[] floorPositions = BlockPositions.getFloorPositions(
            2, 3, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall()); // was: dmmGdE2RN9C, byNtgqgBf0C
        state.setPlacedFloor(false);     // was: xG2PP8jo4RWLS
        WorldUtils.scanPositions(floorPositions); // was: jOdDDFXSeWl4(BlockPos[])

        List<Block> nukerBlacklist = KekNuker.getBlacklist(); // was: t018N0
        state.setFoundMissingBlock(false); // was: Y9BgxR
        BlockPos missingPos = null;
        boolean blockedBehind = false;
        BlockPos blockedPos = null;

        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) {
            // Scan for missing floor blocks (behind=−3, ahead=+5)
            for (BlockPos pos : BlockPositions.getFloorPositions(-3, 5, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall())) {
                block = mc.world.getBlockState(pos).getBlock(); // getBlockState.getBlock
                if (!HighwayBuilder.allowLava() && block == Blocks.field_22423   // lava
                        || block == HighwayBuilder.getFloorBlock()
                        || StatsHandler.getObsidianCount() <= 10
                        || nukerBlacklist.contains(block)) continue;
                state.setFoundMissingBlock(true);
                missingPos = pos;
                break;
            }
            // Optionally check ceiling positions too
            if (HighwayBuilder.hasCeiling()) { // was: ydklDMif6Ghqm0
                for (BlockPos pos : BlockPositions.getCeilingPositions(-3, 4, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall())) {
                    block = mc.world.getBlockState(pos).getBlock();
                    if (!HighwayBuilder.allowLava() && block == Blocks.field_22423
                            || block == Blocks.field_9987  // air
                            || block != Blocks.field_10124 // glass
                            || StatsHandler.getObsidianCount() <= 10
                            || nukerBlacklist.contains(block)) continue;
                    state.setFoundMissingBlock(true);
                    missingPos = pos;
                    break;
                }
            }
            // Scan for obstacles in the clear (passage) zone behind the player
            for (BlockPos pos : BlockPositions.getForwardClearPositions()) { // was: IHeihwsO8p
                block = mc.world.getBlockState(pos).getBlock();
                if (block instanceof class_2189   // fluid
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
                mc.world.getBlockState(blockedPos).getBlock().method_9518()); // getTranslationKey
            assert blockedPos != null;
            goalBlock = switch (HighwayBuilder.getDirection()) { // was: kLIvClyeu
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
        if (state.foundMissingBlock()) { // was: YnQ4ChsDR
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
            KekNuker.nukerQueue.clear(); // was: gaDbi5D443T6vqgt
            for (BlockPos pos : floorPositions) {
                block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.field_10316  // bedrock
                        || block == Blocks.field_9987   // air
                        || !HighwayBuilder.allowLava() && block == Blocks.field_22423
                        || !WorldUtils.shouldBreak(pos, HighwayBuilder.getFloorBlock())) continue; // was: jOdDDFXSeWl4(BlockPos,Block)
                KekNuker.nukerQueue.add(pos.method_10062()); // toImmutable()
            }
            for (BlockPos pos : BlockPositions.getClearPositions(2, 3)) { // was: vgrtgn5
                block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.field_10316
                        || block == Blocks.field_9987
                        || !WorldUtils.shouldBreak(pos, Blocks.field_10124)) continue; // glass
                KekNuker.nukerQueue.add(pos.method_10062());
            }
        }

        // Place floor and ceiling blocks
        state.setPlacedFloor(false); // was: L5CF0C6jx0T17H4I
        WorldUtils.tryPlaceBlocks(floorPositions, false); // was: jOdDDFXSeWl4(BlockPos[],boolean)
        if (HighwayBuilder.hasCeiling()) {
            WorldUtils.tryPlaceBlocks(BlockPositions.getCeilingPositions(2, 2, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall()), false);
        }

        // Resume walking if nothing is pending
        if (!(state.isFloorPlaced()         // was: F41rraDXnaj
                && state.isCeilingPlaced()   // was: YvaEDE3IjU1
                && state.foundMissingBlock()
                && KekNuker.isNuking()
                && state.isObsidianReady())) { // was: NZkZx8MJ67Zw
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
        assert mc.field_1724 != null && mc.field_1687 != null;
        HighwayState state = HighwayState.getInstance();

        if (state.getDirection() == null || state.getAlignStartX() == null
                || state.getHighwayY() == null || state.getAlignStartZ() == null) {
            return;
        }

        // Track diagonal alignment: when |Δx| == |Δz| we are exactly on the diagonal axis
        double dx = mc.player.getX() - state.getDiagonalCenter().getX(); // was: QTmNF6NCXs
        double dz = mc.player.getZ() - state.getDiagonalCenter().getZ();
        if (Math.abs(dx) == Math.abs(dz)) {
            state.setAlignZ(mc.player.getZ()); // was: vgrtgn5
            state.setAlignX(mc.player.getX()); // was: Gt56Sj4a6BWhgB
        }

        if (WorldUtils.isLagDetected()) {
            PlayerUtils.setAutoWalkActive(false);
            if (!PathingHelper.isAlreadyPathing()
                    && state.getLavaTargetBlock() == null  // was: rUchPoPt
                    && state.getLavaSourceBlock() == null) { // was: HBAiI3pyGGGxpI2b
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

        if (HighwayBuilder.isCheckingSpleef()) { // was: xpLMsAtAuXAx
            WorldUtils.checkSpleef(); // was: selaO6lwe7
        }

        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) {
            PlayerUtils.alignWithBaritone(); // was: uFghvYncEwFBHmJL
            WorldUtils.checkForwardCollisions();
            PlayerUtils.alignLookToHighway();
        }

        List<Block> nukerBlacklist = KekNuker.getBlacklist();
        boolean blockedBehind = false;
        BlockPos blockedBehindPos = null;

        if (HighwayBuilder.getMovementMode() == HighwayBuilder.Mode.AUTOWALK) {
            // Check for obstacles in the passage zone behind the player
            for (BlockPos pos : BlockPositions.getDiagonalClearPositions(-3, 5)) { // was: VYEwzRq
                Block b = mc.world.getBlockState(pos).getBlock();
                if (b instanceof class_2189 || StatsHandler.getObsidianCount() <= 10
                        || nukerBlacklist.contains(b)) continue;
                blockedBehind = true;
                blockedBehindPos = pos;
                break;
            }

            if (blockedBehind) {
                PlayerUtils.setAutoWalkActive(false);
                MusheorSystem.debug("Detected blockage behind player... "
                    + class_7923.field_41175.method_10221(mc.world.getBlockState(blockedBehindPos).getBlock()).method_12832(),
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
            for (BlockPos pos : BlockPositions.getDiagonalFloorPositions(-3, 4, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall())) { // was: Gt56Sj4a6BWhgB
                block = mc.world.getBlockState(pos).getBlock();
                if (!HighwayBuilder.allowLava() && block == Blocks.field_22423
                        || block == HighwayBuilder.getFloorBlock()
                        || StatsHandler.getObsidianCount() <= 10
                        || block == Blocks.field_22423 && !HighwayBuilder.allowLava()
                        || block == Blocks.field_9987
                        || nukerBlacklist.contains(block)) continue;
                state.setFoundMissingBlock(true);
                missingPos = pos;
                break;
            }
            if (HighwayBuilder.hasCeiling()) {
                for (BlockPos pos : BlockPositions.getDiagonalCeilingPositions(-3, 4, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall())) { // was: TAdu5cndwWu3A1
                    block = mc.world.getBlockState(pos).getBlock();
                    if (!HighwayBuilder.allowLava() && block == Blocks.field_22423
                            || block != Blocks.field_10124
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

        // Place floor blocks
        BlockPos[] diagonalFloor = BlockPositions.getDiagonalFloorPositions(2, 2, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall());
        state.setPlacedFloor(false);
        WorldUtils.tryPlaceBlocks((BlockPos[]) diagonalFloor);

        // Build KekNuker queue for diagonal cross-section
        if (((Boolean) HighwayBuilder.INSTANCE.enableNuker.get()).booleanValue()) {
            KekNuker.nukerQueue.clear();
            for (BlockPos pos : diagonalFloor) {
                block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.field_10316 || block == Blocks.field_9987
                        || !HighwayBuilder.allowLava() && block == Blocks.field_22423
                        || !WorldUtils.shouldBreak(pos, HighwayBuilder.getFloorBlock())) continue;
                KekNuker.nukerQueue.add(pos.method_10062());
            }
            for (BlockPos pos : BlockPositions.getDiagonalClearPositions(3, 0)) { // was: VYEwzRq(3,0)
                block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.field_10316 || block == Blocks.field_9987
                        || !WorldUtils.shouldBreak(pos, Blocks.field_10124)) continue;
                KekNuker.nukerQueue.add(pos.method_10062());
            }
        }

        state.setPlacedFloor(false);
        WorldUtils.tryPlaceBlocks((BlockPos[]) diagonalFloor, false);
        BlockPos[] diagonalCeiling = BlockPositions.getDiagonalCeilingPositions(2, 1, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall());
        if (HighwayBuilder.hasCeiling()) {
            // Stop walking if any ceiling position is obstructed but can be placed
            for (BlockPos pos : diagonalCeiling) {
                if (!WorldUtils.isFluid(pos) || !BlockUtils.canPlace((BlockPos) pos, true)) continue; // was: KDNrzlU9qtrEv
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
        if (mc.field_1724 == null || mc.field_1687 == null) return;
        HighwayState state = HighwayState.getInstance();

        if (PlayerUtils.isGatheringItem() || HighwayBuilder.isEating()
                || InventoryManager.isBreakingShulker || HighwayBuilder.isWaiting()
                || SourceRemover.isActive() && Modules.get().get("source-remover").isActive()) {
            PlayerUtils.setAutoWalkActive(false);
            return;
        }

        if (mc.player.getY() < (double) state.getHighwayY().intValue()) { // getY
            PlayerUtils.setAutoWalkActive(false);
        }

        if (HighwayBuilder.isCheckingSpleef()) {
            WorldUtils.checkSpleef();
        }

        PlayerUtils.alignLookToHighway();
        PlayerUtils.applyStrafing();
        WorldUtils.checkForwardCollisions();

        // If using scaffold mode: ensure ice is in inventory, path to restock if needed
        if (HighwayBuilder.getScaffoldMode() != HighwayBuilder.ScaffoldMode.ICE_FLOOR) { // was: oGrnfoe87ZeN
            if (InventoryManager.findItemSlot(HighwayBuilder.getIceItem().method_8389()) == null) { // was: e4uKoS, jWrhVf2psx
                state.setRestockTarget(mc.player.getBlockPos()); // was: V2mbWoNZftH0t
                WorldUtils.returnToHighway(); // was: xG2PP8jo4RWLS (different overload — the "find ice" path)
                return;
            }
            if (state.getRestockGoal() != null // was: E74ay1CfIa1C1X6
                    && InventoryManager.findItemSlot(HighwayBuilder.getIceItem().method_8389()) != null) {
                PathingHelper.setGoal(state.getRestockGoal());
            }
            state.setRestockTarget(null);
            if (PathingHelper.isAlreadyPathing()) return;
        }

        // Place ice scaffold or run IceFloor builder
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.SCAFFOLD) { // was: aLormWyi9q
            WorldUtils.tryPlaceBlocks(
                BlockPositions.getFloorPositions(3, 2, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall()), true); // was: RzemQrYtv7d0h, b76P5ieurZIX
        }
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.ICE_FLOOR) { // was: nQgi06
            WorldUtils.buildIceFloor(); // was: QigP9ftge6
        }

        // Add obstructions in the clear zone to KekNuker's queue
        for (BlockPos pos : BlockPositions.getClearPositions(3, 2)) {
            class_2680 blockState = mc.world.getBlockState(pos);
            if (blockState.getBlock() instanceof class_2189  // fluid
                    || blockState.getBlock() == Blocks.field_10316
                    || blockState.getBlock() == Blocks.field_9987
                    || !WorldUtils.shouldBreak(pos, Blocks.field_10124)) continue;
            KekNuker.nukerQueue.add(pos.method_10062());
        }

        // Detect forward gap: if a block in the passage is floating (no adjacent solid face),
        // path to the gap using GoalXZ so the player walks up to it
        for (BlockPos pos : BlockPositions.getForwardClearPositions()) {
            if (!(mc.world.getBlockState(pos).getBlock() instanceof class_2189)
                    && StatsHandler.ticksWithoutBlock > 5) { // was: kLMA2zkeM3
                boolean hasAdjacentSolid = false;
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = pos.method_10093(dir); // offset(direction)
                    class_3610 chunk = mc.world.method_8316(neighbor);
                    if (chunk.method_15772() != class_3612.field_15908) continue; // ChunkStatus.FULL
                    hasAdjacentSolid = true;
                    break;
                }
                if (hasAdjacentSolid) continue;
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
        if (mc.field_1724 == null || mc.field_1687 == null) return;
        HighwayState state = HighwayState.getInstance();

        if (state.getDiagonalCenter() == null) { // was: QTmNF6NCXs
            ChatUtils.error("Set player center using .center", new Object[0]);
            PlayerUtils.disableHighwayBuilder(); // was: xynAsOKhN7t
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
            WorldUtils.checkSpleef();
        }

        PlayerUtils.alignLookToHighway();
        PlayerUtils.alignWithBaritone();
        WorldUtils.checkForwardCollisions();

        // Scaffold mode: ensure packed ice is in inventory
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.SCAFFOLD) {
            if (InventoryManager.findItemSlot(Items.field_8328) == null) { // packed_ice
                WorldUtils.returnToHighway(); // was: xG2PP8jo4RWLS (ice variant)
            }
            WorldUtils.tryPlaceBlocks(
                BlockPositions.getDiagonalFloorPositions(2, 1, HighwayBuilder.hasLeftWall(), HighwayBuilder.hasRightWall()), true);
        }
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.ICE_FLOOR) {
            if (InventoryManager.findItemSlot(Items.field_8328) == null) {
                WorldUtils.returnToHighway();
            }
            WorldUtils.buildIceFloor();
        }

        if (KekNuker.isNuking()) return;

        // Safety: place a block so the player doesn't fall off the diagonal edge
        if (WorldUtils.placeSafetyBlock()) { // was: PROcSc3gv
            MusheorSystem.debug("Placing block so player doesn't fall...", new Object[0]);
            PlayerUtils.setAutoWalkActive(false);
            return;
        }

        // Add clear-zone obstructions to KekNuker
        for (BlockPos pos : BlockPositions.getDiagonalClearPositions(3, 2)) {
            class_2680 blockState = mc.world.getBlockState(pos);
            if (blockState.getBlock() instanceof class_2189
                    || blockState.getBlock() == Blocks.field_10316
                    || blockState.getBlock() == Blocks.field_9987
                    || !WorldUtils.shouldBreak(pos, Blocks.field_10124)) continue;
            KekNuker.nukerQueue.add(pos.method_10062());
        }

        // Detect obstacle behind player in the diagonal passage zone
        for (BlockPos pos : BlockPositions.getDiagonalClearPositions(-3, 5)) {
            if (!(mc.world.getBlockState(pos).getBlock() instanceof class_2189)
                    && StatsHandler.ticksWithoutBlock > 5) {
                boolean hasAdjacentSolid = false;
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = pos.method_10093(dir);
                    class_3610 chunk = mc.world.method_8316(neighbor);
                    if (chunk.method_15772() != class_3612.field_15908) continue;
                    hasAdjacentSolid = true;
                    break;
                }
                if (hasAdjacentSolid || nukerBlacklist.contains(mc.world.getBlockState(pos).getBlock())) continue;
                MusheorSystem.debug("Detected obstruction behind player... "
                    + class_7923.field_41175.method_10221(mc.world.getBlockState(pos).getBlock()).method_12832()
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
