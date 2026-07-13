// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.GZpL)
package musheor.utils;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import musheor.modules.automation.EchestFarmer;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.InventoryManager;
import musheor.modules.automation.KekBounce;
import musheor.modules.automation.KekNuker;
import musheor.modules.automation.SourceRemover;
import musheor.utils.internal.HighwayLocator;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * The per-tick "brain" of the HighwayBuilder. Contains the pave/dig loops for both
 * cardinal and diagonal highways, the Auto-mode combined loop, ring/diamond corner
 * turning, elytra-bounce management, and fall-recovery pathing. All heavy block
 * scanning/placement is delegated to {@link WorldUtils} and {@link BlockPositions}.
 */
public class Handlers {
    public static final MinecraftClient mc = MinecraftClient.getInstance(); // was: FvaNWO (field)
    private static final int CONST_TWO = 2;         // was: psJq59YIbp3Z
    private static final int CONST_ONE = 1;         // was: SOYyh5IPg26f7F
    private static boolean bouncing = false;        // was: rKbT3Ifwo
    private static int bounceCooldownTicks = 0;     // was: r7hOYIKN2
    private static final int BOUNCE_COOLDOWN = 20;   // was: oZHMlTL
    private static final int SCAN_32 = 32;           // was: xQr5FhbwpQPWgIQ
    private static boolean fellOff = false;          // was: OMMZL1F3q
    private static int offHighwayTicks = 0;          // was: zu3a44xDeMFMCRwm
    private static int repathTicks = 0;              // was: krxNb5lcQuWA
    private static final int FALL_GRACE = 15;        // was: nt0HZnvBBp
    private static final int REPATH_INTERVAL = 40;   // was: amz3UB1vE
    private static final int RECOVERY_RANGE = 24;    // was: sBBIyQG5NWq0K

    /** Starts the echest farmer to mine obsidian from ender chests. */
    public static void runEchestFarmer() { // was: FvaNWO()
        HighwayState state = HighwayState.getInstance();
        PlayerUtils.setModuleSetting(EchestFarmer.class, "self-toggle", true);
        state.setLastPlayerBlockPos(mc.player.getBlockPos());
        if (WorldUtils.unusedFalse()) {
            PlayerUtils.setAutoWalk(false);
            if (!PathingHelper.isPathing()) WorldUtils.returnToHighway();
        } else if (!state.isFlag12()) {
            assert mc.player != null;
            int remaining = InventoryManager.countItemInInventory(Items.ENDER_CHEST) - 8;
            if (remaining > 0) {
                PlayerUtils.setModuleSetting(EchestFarmer.class, "amount", Math.min(remaining, InventoryManager.countEmptyInventorySlots() * 8));
            }
            PlayerUtils.setAutoWalk(false);
            state.setFlag12(true);
            Module echestFarmer = Modules.get().get(EchestFarmer.class);
            if (!echestFarmer.isActive()) {
                state.setFlag11(true);
                echestFarmer.toggle();
            }
        }
    }

    /** After echest farming: mine the placed ender chest back up and reset restock flags. */
    public static void runPostEchestFarmer() { // was: Q90GLXQ0Pef()
        HighwayState state = HighwayState.getInstance();
        if (!HighwayBuilder.isEchestFarmerActive() && state.isFlag11()) {
            if (EchestFarmer.targetPos != null && mc.world.getBlockState(EchestFarmer.targetPos).getBlock() == Blocks.ENDER_CHEST) {
                MusheorSystem.debug("Attempting to break incorrect block...");
                BlockUtils.breakBlock(EchestFarmer.targetPos, true);
                return;
            }
            if (PlayerUtils.isGatheringItem()) return;
            PlayerUtils.gatherItem(Items.OBSIDIAN, false);
            state.setFlag12(false);
            state.setFlag11(false);
        }
    }

    // ----------------------------------------------------------------------
    // PAVE — cardinal
    // ----------------------------------------------------------------------
    public static void paveCardinal() { // was: psJq59YIbp3Z()
        assert mc.player != null && mc.world != null;
        HighwayState state = HighwayState.getInstance();
        if (state.getDirection() == null || state.getCenterX() == null || state.getCenterY() == null || state.getCenterZ() == null) return;
        if (handleFallRecovery(state)) return;

        Module echestFarmer = Modules.get().get(EchestFarmer.class);
        Map<String, Boolean> conditions = new LinkedHashMap<>();
        conditions.put("BetterEchestFarmer", echestFarmer.isActive());
        conditions.put("Gathering Items", PlayerUtils.isGatheringItem());
        conditions.put("Removing Lava", SourceRemover.isRemoving());
        if (echestFarmer.isActive() || PlayerUtils.isGatheringItem() || SourceRemover.isRemoving()) {
            PlayerUtils.setAutoWalk(false);
            for (Map.Entry<String, Boolean> entry : conditions.entrySet())
                if (entry.getValue()) MusheorSystem.debug(entry.getKey() + " was triggered.");
            return;
        }

        if (KekNuker.isBusyInBuildZone() || HighwayBuilder.isEating() || HighwayBuilder.isKillAuraAttacking() || InventoryManager.isPending) {
            stopBounce();
            PlayerUtils.setAutoWalk(false);
        }
        if (HighwayBuilder.advancedSourceFiller()) WorldUtils.handleLavaRemoval();

        if (HighwayBuilder.isAutoBounceEnabled() && HighwayBuilder.getMode() == HighwayBuilder.Mode.SEMI) {
            if (bounceCooldownTicks > 0) { bounceCooldownTicks--; return; }
            boolean interrupted = KekNuker.isBusyInBuildZone() || HighwayBuilder.isEating() || HighwayBuilder.isKillAuraAttacking()
                || InventoryManager.isPending || PlayerUtils.isGatheringItem() || SourceRemover.isRemoving()
                || echestFarmer.isActive() || PathingHelper.isPathing();
            if (!interrupted && isPathClearAhead(true, HighwayBuilder.getBounceDistanceCheck())) startBounce();
            else stopBounce();
            if (bouncing) { PlayerUtils.faceHighwayDirection(); return; }
        }

        if (HighwayBuilder.getMode() == HighwayBuilder.Mode.SEMI) {
            PlayerUtils.correctDrift();
            WorldUtils.checkFrontCollision();
            PlayerUtils.faceHighwayDirectionPitchDown();
            PlayerUtils.strafeToCenterline();
        }

        BlockPos[] positions = BlockPositions.cardinalFloor(2, 3, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail());
        if (HighwayBuilder.INSTANCE.toggleKekNuker.get()) KekNuker.extraBreakQueue.clear();

        state.setFlag6(false);
        WorldUtils.spleefEntities(positions);
        List<Block> blacklistedBlocks = KekNuker.getBlacklist();
        state.setFlag5(false);
        BlockPos nonObsidianBlock = null;
        boolean foundBlockage = false;
        BlockPos blockage = null;
        if (HighwayBuilder.getMode() == HighwayBuilder.Mode.SEMI) {
            for (BlockPos currentPos : BlockPositions.cardinalFloor(-3, 5, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail())) {
                Block block = mc.world.getBlockState(currentPos).getBlock();
                if ((HighwayBuilder.replaceCryingObsidian() || block != Blocks.RESPAWN_ANCHOR)
                    && block != HighwayBuilder.getFillBlock() && StatsHandler.getDistanceTravelled() > 10 && !blacklistedBlocks.contains(block)) {
                    state.setFlag5(true); nonObsidianBlock = currentPos; break;
                }
            }
            if (HighwayBuilder.hasCeiling()) {
                for (BlockPos currentPos : BlockPositions.cardinalCeiling(-3, 4, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail())) {
                    Block block = mc.world.getBlockState(currentPos).getBlock();
                    if ((HighwayBuilder.replaceCryingObsidian() || block != Blocks.RESPAWN_ANCHOR)
                        && block != Blocks.BEDROCK && block == Blocks.AIR && StatsHandler.getDistanceTravelled() > 10 && !blacklistedBlocks.contains(block)) {
                        state.setFlag5(true); nonObsidianBlock = currentPos; break;
                    }
                }
            }
            for (BlockPos currentPos : BlockPositions.cardinalWallScan()) {
                Block block = mc.world.getBlockState(currentPos).getBlock();
                if (!(block instanceof FluidBlock) && StatsHandler.getDistanceTravelled() > 10 && !blacklistedBlocks.contains(block)) {
                    foundBlockage = true; blockage = currentPos; break;
                }
            }
        }

        if (foundBlockage) {
            PlayerUtils.setAutoWalk(false);
            MusheorSystem.debug("Detected blockage behind player... %s", mc.world.getBlockState(blockage).getBlock().getName());
            assert blockage != null;
            Goal goal = switch (HighwayBuilder.getDirection()) {
                case NORTH -> new GoalBlock(state.getCenterX(), state.getCenterY(), blockage.getZ() + 2);
                case EAST  -> new GoalBlock(blockage.getX() - 2, state.getCenterY(), state.getCenterZ());
                case SOUTH -> new GoalBlock(state.getCenterX(), state.getCenterY(), blockage.getZ() - 2);
                case WEST  -> new GoalBlock(blockage.getX() + 2, state.getCenterY(), state.getCenterZ());
                default    -> new GoalBlock(state.getCenterX(), state.getCenterY(), state.getCenterZ());
            };
            PathingHelper.setGoal(goal);
        } else {
            if (state.isFlag5()) {
                PlayerUtils.setAutoWalk(false);
                MusheorSystem.debug("Found missing block, going back...");
                assert nonObsidianBlock != null;
                Goal goal = switch (HighwayBuilder.getDirection()) {
                    case NORTH, SOUTH -> new GoalBlock(state.getCenterX(), state.getCenterY(), nonObsidianBlock.getZ());
                    case EAST, WEST   -> new GoalBlock(nonObsidianBlock.getX(), state.getCenterY(), state.getCenterZ());
                    default           -> new GoalBlock(nonObsidianBlock.getX(), state.getCenterY(), nonObsidianBlock.getZ());
                };
                PathingHelper.setGoal(goal);
            }
            if (HighwayBuilder.INSTANCE.toggleKekNuker.get()) {
                for (BlockPos pos : positions) {
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (block != Blocks.VOID_AIR && block != Blocks.BEDROCK
                        && (HighwayBuilder.replaceCryingObsidian() || block != Blocks.RESPAWN_ANCHOR)
                        && WorldUtils.shouldBreak(pos, HighwayBuilder.getFillBlock())) {
                        KekNuker.extraBreakQueue.add(pos.toImmutable());
                    }
                }
                for (BlockPos pos : BlockPositions.cardinalTunnel(3, 2)) {
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (block != Blocks.VOID_AIR && block != Blocks.BEDROCK && WorldUtils.shouldBreak(pos, Blocks.AIR)) {
                        KekNuker.extraBreakQueue.add(pos.toImmutable());
                    }
                }
            }
            state.setFlag4(false);
            WorldUtils.placeHighwayBlocks(positions, false);
            if (HighwayBuilder.hasCeiling()) {
                WorldUtils.placeHighwayBlocks(BlockPositions.cardinalCeiling(2, 2, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail()), false);
            }
            if (!state.isFlag4() || !state.isFlag3() || !state.isFlag5() || !KekNuker.isBusyInBuildZone() || !state.isFlag6()) {
                PlayerUtils.setAutoWalk(true);
            }
        }
    }

    // ----------------------------------------------------------------------
    // PAVE — diagonal
    // ----------------------------------------------------------------------
    public static void paveDiagonal() { // was: SOYyh5IPg26f7F()
        assert mc.player != null && mc.world != null;
        HighwayState state = HighwayState.getInstance();
        if (state.getDirection() == null || state.getCenterX() == null || state.getCenterY() == null || state.getCenterZ() == null) return;
        if (handleFallRecovery(state)) return;

        double dx = mc.player.getBlockX() - state.getCenterPos().getX();
        double dz = mc.player.getBlockZ() - state.getCenterPos().getZ();
        if (Math.abs(dx) == Math.abs(dz)) {
            state.setCenterZ(mc.player.getBlockZ());
            state.setCenterX(mc.player.getBlockX());
        }
        if (WorldUtils.unusedFalse()) {
            PlayerUtils.setAutoWalk(false);
            if (!PathingHelper.isPathing() && state.getReturnGoalPos() == null && state.getPendingBreakPos() == null) WorldUtils.returnToHighway();
        }

        Module echestFarmer = Modules.get().get(EchestFarmer.class);
        if (echestFarmer.isActive() || PlayerUtils.isGatheringItem() || SourceRemover.isRemoving()) return;

        if (KekNuker.isBusyInBuildZone() || HighwayBuilder.isEating() || HighwayBuilder.isKillAuraAttacking() || InventoryManager.isPending) {
            stopBounce();
            PlayerUtils.setAutoWalk(false);
        }
        if (HighwayBuilder.advancedSourceFiller()) WorldUtils.handleLavaRemoval();

        if (HighwayBuilder.isAutoBounceEnabled() && HighwayBuilder.getMode() == HighwayBuilder.Mode.SEMI) {
            if (bounceCooldownTicks > 0) { bounceCooldownTicks--; return; }
            boolean interrupted = KekNuker.isBusyInBuildZone() || HighwayBuilder.isEating() || HighwayBuilder.isKillAuraAttacking()
                || InventoryManager.isPending || PlayerUtils.isGatheringItem() || SourceRemover.isRemoving()
                || echestFarmer.isActive() || PathingHelper.isPathing();
            if (!interrupted && isPathClearAhead(false, HighwayBuilder.getBounceDistanceCheck())) startBounce();
            else stopBounce();
            if (bouncing) { PlayerUtils.faceHighwayDirection(); return; }
        }

        if (HighwayBuilder.getMode() == HighwayBuilder.Mode.SEMI) {
            PlayerUtils.alignToHighway();
            WorldUtils.checkFrontCollision();
            PlayerUtils.faceHighwayDirectionPitchDown();
        }

        List<Block> blacklistedBlocks = KekNuker.getBlacklist();
        boolean foundBlockage = false;
        BlockPos blockage = null;
        if (HighwayBuilder.getMode() == HighwayBuilder.Mode.SEMI) {
            for (BlockPos currentPos : BlockPositions.diagonalTunnel(-2, 5)) {
                Block block = mc.world.getBlockState(currentPos).getBlock();
                if (!(block instanceof FluidBlock) && StatsHandler.getDistanceTravelled() > 10 && !blacklistedBlocks.contains(block)) {
                    foundBlockage = true; blockage = currentPos; break;
                }
            }
            if (foundBlockage) {
                PlayerUtils.setAutoWalk(false);
                MusheorSystem.debug("Detected blockage behind player... " + Registries.BLOCK.getId(mc.world.getBlockState(blockage).getBlock()).getPath());
                assert blockage != null;
                int distance = 4;
                Goal goal = switch (HighwayBuilder.getDirection()) {
                    case NORTH_EAST -> new GoalBlock(state.getCenterX() - distance, state.getCenterY(), state.getCenterZ() + distance);
                    case NORTH_WEST -> new GoalBlock(state.getCenterX() + distance, state.getCenterY(), state.getCenterZ() + distance);
                    case SOUTH_EAST -> new GoalBlock(state.getCenterX() - distance, state.getCenterY(), state.getCenterZ() - distance);
                    case SOUTH_WEST -> new GoalBlock(state.getCenterX() + distance, state.getCenterY(), state.getCenterZ() - distance);
                    default         -> new GoalBlock(blockage.getX(), state.getCenterY(), blockage.getZ());
                };
                PathingHelper.setGoal(goal);
                return;
            }
            state.setFlag5(false);
            BlockPos nonObsidianBlock = null;
            for (BlockPos currentPos : BlockPositions.diagonalFloor(-3, 4, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail())) {
                Block block = mc.world.getBlockState(currentPos).getBlock();
                if ((HighwayBuilder.replaceCryingObsidian() || block != Blocks.RESPAWN_ANCHOR)
                    && block != HighwayBuilder.getFillBlock() && StatsHandler.getDistanceTravelled() > 10
                    && (block != Blocks.RESPAWN_ANCHOR || HighwayBuilder.replaceCryingObsidian())
                    && block != Blocks.BEDROCK && !blacklistedBlocks.contains(block)) {
                    state.setFlag5(true); nonObsidianBlock = currentPos; break;
                }
            }
            if (HighwayBuilder.hasCeiling()) {
                for (BlockPos currentPos : BlockPositions.diagonalCeiling(-3, 4, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail())) {
                    Block block = mc.world.getBlockState(currentPos).getBlock();
                    if ((HighwayBuilder.replaceCryingObsidian() || block != Blocks.RESPAWN_ANCHOR)
                        && block == Blocks.AIR && StatsHandler.getDistanceTravelled() > 10 && !blacklistedBlocks.contains(block)) {
                        state.setFlag5(true); nonObsidianBlock = currentPos; break;
                    }
                }
            }
            if (state.isFlag5()) {
                PlayerUtils.setAutoWalk(false);
                MusheorSystem.debug("Found missing block, going back...");
                assert nonObsidianBlock != null;
                Goal goal = switch (HighwayBuilder.getDirection()) {
                    case NORTH_EAST -> new GoalBlock(state.getCenterX() - 8, state.getCenterY(), state.getCenterZ() + 8);
                    case NORTH_WEST -> new GoalBlock(state.getCenterX() + 8, state.getCenterY(), state.getCenterZ() + 8);
                    case SOUTH_EAST -> new GoalBlock(state.getCenterX() - 8, state.getCenterY(), state.getCenterZ() - 8);
                    case SOUTH_WEST -> new GoalBlock(state.getCenterX() + 8, state.getCenterY(), state.getCenterZ() - 8);
                    default         -> new GoalBlock(nonObsidianBlock.getX(), state.getCenterY(), nonObsidianBlock.getZ());
                };
                PathingHelper.setGoal(goal);
                return;
            }
        }

        BlockPos[] positions = BlockPositions.diagonalFloor(2, 2, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail());
        if (HighwayBuilder.INSTANCE.toggleKekNuker.get()) KekNuker.extraBreakQueue.clear();
        state.setFlag6(false);
        WorldUtils.spleefEntities(positions);
        if (HighwayBuilder.INSTANCE.toggleKekNuker.get()) {
            for (BlockPos pos : positions) {
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block != Blocks.VOID_AIR && block != Blocks.BEDROCK
                    && (HighwayBuilder.replaceCryingObsidian() || block != Blocks.RESPAWN_ANCHOR)
                    && WorldUtils.shouldBreak(pos, HighwayBuilder.getFillBlock())) {
                    KekNuker.extraBreakQueue.add(pos.toImmutable());
                }
            }
            for (BlockPos pos : BlockPositions.diagonalTunnel(2, 1)) {
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block != Blocks.VOID_AIR && block != Blocks.BEDROCK && WorldUtils.shouldBreak(pos, Blocks.AIR)) {
                    KekNuker.extraBreakQueue.add(pos.toImmutable());
                }
            }
        }
        state.setFlag4(false);
        WorldUtils.placeHighwayBlocks(positions, false);
        BlockPos[] ceilingPositions = BlockPositions.diagonalCeiling(2, 1, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail());
        if (HighwayBuilder.hasCeiling()) {
            for (BlockPos pos : ceilingPositions)
                if (WorldUtils.isWithinPlacementRange(pos) && BlockUtils.canPlace(pos, true)) PlayerUtils.setAutoWalk(false);
            WorldUtils.placeHighwayBlocks(ceilingPositions, false);
        }
        if (!state.isFlag4() || !state.isFlag3() || !state.isFlag5() || !KekNuker.isBusyInBuildZone() || !state.isFlag6()) {
            PlayerUtils.setAutoWalk(true);
        }
    }

    // ----------------------------------------------------------------------
    // DIG — cardinal
    // ----------------------------------------------------------------------
    public static void digCardinal() { // was: rKbT3Ifwo()
        if (mc.player == null || mc.world == null) return;
        HighwayState state = HighwayState.getInstance();
        if (PlayerUtils.isGatheringItem() || HighwayBuilder.isEating() || InventoryManager.isPending
            || HighwayBuilder.isKillAuraAttacking()
            || (SourceRemover.isRemoving() && Modules.get().get("source-remover").isActive())) {
            PlayerUtils.setAutoWalk(false);
            return;
        }
        if (mc.player.getY() < state.getCenterY()) PlayerUtils.setAutoWalk(false);
        if (HighwayBuilder.advancedSourceFiller()) WorldUtils.handleLavaRemoval();
        PlayerUtils.faceHighwayDirectionPitchDown();
        PlayerUtils.strafeToCenterline();
        WorldUtils.checkFrontCollision();

        if (HighwayBuilder.getScaffoldMode() != HighwayBuilder.ScaffoldMode.NONE) {
            if (InventoryManager.findItemStack(HighwayBuilder.getScaffoldBlock().asItem()) == null) {
                state.setMissingMaterialGoalPos(mc.player.getBlockPos());
                WorldUtils.findAndPickupItem(HighwayBuilder.getScaffoldBlock().asItem());
                return;
            }
            if (state.getMissingMaterialGoalPos() != null && InventoryManager.findItemStack(HighwayBuilder.getScaffoldBlock().asItem()) != null) {
                PathingHelper.gotoBlock(state.getMissingMaterialGoalPos());
            }
            state.setMissingMaterialGoalPos(null);
            if (PathingHelper.isPathing()) return;
        }
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.ADVANCED) {
            WorldUtils.placeHighwayBlocks(BlockPositions.cardinalFloor(3, 2, HighwayBuilder.scaffoldLeftRail(), HighwayBuilder.scaffoldRightRail()), true);
        }
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.NORMAL) {
            WorldUtils.fillWalkwayBelow();
        }

        for (BlockPos currentPos : BlockPositions.cardinalTunnel(3, 1)) {
            BlockState blockState = mc.world.getBlockState(currentPos);
            if (!(blockState.getBlock() instanceof FluidBlock) && blockState.getBlock() != Blocks.VOID_AIR
                && blockState.getBlock() != Blocks.BEDROCK && WorldUtils.shouldBreak(currentPos, Blocks.AIR)) {
                KekNuker.extraBreakQueue.add(currentPos.toImmutable());
            }
        }
        for (BlockPos currentPos : BlockPositions.cardinalWallScan()) {
            if (!(mc.world.getBlockState(currentPos).getBlock() instanceof FluidBlock) && StatsHandler.cachedDistance > 5) {
                boolean hasLavaNeighbor = false;
                for (Direction direction : Direction.values()) {
                    if (mc.world.getFluidState(currentPos.offset(direction)).getFluid() == Fluids.LAVA) { hasLavaNeighbor = true; break; }
                }
                if (!hasLavaNeighbor) {
                    PlayerUtils.setAutoWalk(false);
                    GoalXZ goal = switch (HighwayBuilder.getDirection()) {
                        case NORTH, SOUTH -> new GoalXZ(state.getCenterX(), currentPos.getZ());
                        case EAST, WEST   -> new GoalXZ(currentPos.getX(), state.getCenterZ());
                        default           -> new GoalXZ(currentPos.getX(), currentPos.getZ());
                    };
                    PathingHelper.setGoal(goal);
                    return;
                }
            } else {
                PlayerUtils.setAutoWalk(true);
            }
        }
    }

    // ----------------------------------------------------------------------
    // DIG — diagonal
    // ----------------------------------------------------------------------
    public static void digDiagonal() { // was: r7hOYIKN2()
        if (mc.player == null || mc.world == null) return;
        HighwayState state = HighwayState.getInstance();
        if (state.getCenterPos() == null) {
            ChatUtils.error("Set player center using .center", new Object[0]);
            PlayerUtils.toggleHighwayBuilder();
            return;
        }
        double dx = mc.player.getBlockX() - state.getCenterPos().getX();
        double dz = mc.player.getBlockZ() - state.getCenterPos().getZ();
        if (Math.abs(dx) == Math.abs(dz)) {
            state.setCenterX(mc.player.getBlockX());
            state.setCenterZ(mc.player.getBlockZ());
        }
        List<Block> nukerBlacklist = KekNuker.getBlacklist();
        Map<String, Boolean> conditions = new LinkedHashMap<>();
        conditions.put("Gathering Items", PlayerUtils.isGatheringItem());
        conditions.put("Removing Lava", SourceRemover.isRemoving());
        conditions.put("Eating", HighwayBuilder.isEating());
        if (HighwayBuilder.isEating() || PlayerUtils.isGatheringItem() || SourceRemover.isRemoving() || HighwayBuilder.isKillAuraAttacking()) {
            PlayerUtils.setAutoWalk(false);
            for (Map.Entry<String, Boolean> entry : conditions.entrySet())
                if (entry.getValue()) MusheorSystem.debug(entry.getKey() + " was triggered.");
            return;
        }
        if (HighwayBuilder.advancedSourceFiller()) WorldUtils.handleLavaRemoval();
        PlayerUtils.faceHighwayDirectionPitchDown();
        PlayerUtils.alignToHighway();
        WorldUtils.checkFrontCollision();

        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.ADVANCED) {
            if (InventoryManager.findItemStack(Items.NETHERRACK) == null) WorldUtils.findAndPickupItem(Items.NETHERRACK); // was: class_1802.field_8328 (NETHERRACK)
            WorldUtils.placeHighwayBlocks(BlockPositions.diagonalFloor(2, 1, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail()), true);
        }
        if (HighwayBuilder.getScaffoldMode() == HighwayBuilder.ScaffoldMode.NORMAL) {
            if (InventoryManager.findItemStack(Items.NETHERRACK) == null) WorldUtils.findAndPickupItem(Items.NETHERRACK); // was: class_1802.field_8328 (NETHERRACK)
            WorldUtils.fillWalkwayBelow();
        }

        if (!KekNuker.isBusyInBuildZone()) {
            if (WorldUtils.placeDiagonalSupport()) {
                MusheorSystem.debug("Placing block so player doesn't fall...");
                PlayerUtils.setAutoWalk(false);
            } else {
                for (BlockPos currentPos : BlockPositions.diagonalTunnel(3, 1)) {
                    BlockState blockState = mc.world.getBlockState(currentPos);
                    if (!(blockState.getBlock() instanceof FluidBlock) && blockState.getBlock() != Blocks.VOID_AIR
                        && blockState.getBlock() != Blocks.BEDROCK && WorldUtils.shouldBreak(currentPos, Blocks.AIR)) {
                        KekNuker.extraBreakQueue.add(currentPos.toImmutable());
                    }
                }
                for (BlockPos currentPos : BlockPositions.diagonalTunnel(-2, 5)) {
                    if (!(mc.world.getBlockState(currentPos).getBlock() instanceof FluidBlock) && StatsHandler.cachedDistance > 5) {
                        boolean hasLavaNeighbor = false;
                        for (Direction direction : Direction.values()) {
                            if (mc.world.getFluidState(currentPos.offset(direction)).getFluid() == Fluids.LAVA) { hasLavaNeighbor = true; break; }
                        }
                        if (!hasLavaNeighbor && !nukerBlacklist.contains(mc.world.getBlockState(currentPos).getBlock())) {
                            MusheorSystem.debug("Detected obstruction behind player... "
                                + Registries.BLOCK.getId(mc.world.getBlockState(currentPos).getBlock()).getPath() + "at %s %s %s",
                                currentPos.getX(), currentPos.getY(), currentPos.getZ());
                            PlayerUtils.setAutoWalk(false);
                            PathingHelper.setGoal(new GoalXZ(currentPos.getX(), currentPos.getX()));
                            return;
                        }
                    } else {
                        PlayerUtils.setAutoWalk(true);
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Ring/diamond corner turning
    // ----------------------------------------------------------------------
    /** True once the player has travelled past the end of the current ring/diamond leg. */
    private static boolean reachedRingCorner(HighwayLocator.Checkpoint d, int px, int pz) { // was: FvaNWO(Checkpoint,int,int)
        double D = Math.abs(d.axisValue);
        double K = d.axisValue;
        return switch (d.direction) {
            case NORTH -> pz <= -D + 2.0;
            case EAST  -> px >= D - 2.0;
            case SOUTH -> pz >= D - 2.0;
            case WEST  -> px <= -D + 2.0;
            case NORTH_EAST -> K > 0.0 ? pz <= 2 : px >= -2;
            case NORTH_WEST -> K > 0.0 ? px <= 2 : pz <= 2;
            case SOUTH_EAST -> K > 0.0 ? pz >= -2 : px >= -2;
            case SOUTH_WEST -> K > 0.0 ? px <= 2 : pz >= -2;
        };
    }

    /** Returns the {x, z} corner coordinate where the current leg meets the next. */
    private static int[] getRingCorner(HighwayLocator.Checkpoint d) { // was: FvaNWO(Checkpoint)
        int D = (int) Math.abs(d.axisValue);
        int K = (int) d.axisValue;
        int ax = (int) d.axisValue;
        return switch (d.direction) {
            case NORTH -> new int[]{ax, -D};
            case EAST  -> new int[]{D, ax};
            case SOUTH -> new int[]{ax, D};
            case WEST  -> new int[]{-D, ax};
            case NORTH_EAST -> K > 0 ? new int[]{K, 0} : new int[]{0, K};
            case NORTH_WEST -> K > 0 ? new int[]{0, -K} : new int[]{K, 0};
            case SOUTH_EAST -> K > 0 ? new int[]{K, 0} : new int[]{0, -K};
            case SOUTH_WEST -> K > 0 ? new int[]{0, K} : new int[]{K, 0};
        };
    }

    /** Direction of the next leg after turning a ring/diamond corner. */
    private static WorldUtils.Direction8 getNextLegDirection(HighwayLocator.Checkpoint d) { // was: Q90GLXQ0Pef(Checkpoint)
        boolean pos = d.axisValue > 0.0;
        return switch (d.direction) {
            case NORTH, SOUTH -> pos ? WorldUtils.Direction8.WEST : WorldUtils.Direction8.EAST;
            case EAST, WEST   -> pos ? WorldUtils.Direction8.NORTH : WorldUtils.Direction8.SOUTH;
            case NORTH_EAST, SOUTH_WEST -> pos ? WorldUtils.Direction8.NORTH_WEST : WorldUtils.Direction8.SOUTH_EAST;
            case NORTH_WEST, SOUTH_EAST -> pos ? WorldUtils.Direction8.SOUTH_WEST : WorldUtils.Direction8.NORTH_EAST;
        };
    }

    /** Handles pathing to and turning at a ring/diamond corner. Returns true if handling a turn. */
    private static boolean handleRingTurn(HighwayLocator.Checkpoint detected, HighwayState state) { // was: FvaNWO(Checkpoint,HighwayState)
        assert mc.player != null;
        int px = mc.player.getBlockX();
        int pz = mc.player.getBlockZ();
        if (state.getCenterY() == null) return false;
        int py = state.getCenterY();
        if (!reachedRingCorner(detected, px, pz)) return false;
        int[] corner = getRingCorner(detected);
        if (corner == null) return false;
        int cx = corner[0], cz = corner[1];
        double dist = Math.hypot(px - cx, pz - cz);
        if (dist > 1.0) {
            PlayerUtils.setAutoWalk(false);
            PathingHelper.setGoal(new GoalBlock(cx, py, cz));
            return true;
        }
        WorldUtils.Direction8 nextDir = getNextLegDirection(detected);
        if (nextDir == null) return false;
        HighwayLocator.Checkpoint newDetected = HighwayLocator.locateNearest(mc.player.getBlockX(), mc.player.getBlockZ(), py, nextDir);
        if (newDetected == null) return false;
        state.setCurrentCheckpoint(newDetected);
        state.setDirection(newDetected.direction);
        state.setStartX(newDetected.startX);
        state.setStartZ(newDetected.startZ);
        state.setLastX(newDetected.alignX);
        state.setLastZ(newDetected.alignZ);
        state.setCenterX(mc.player.getBlockX());
        state.setCenterZ(mc.player.getBlockZ());
        PlayerUtils.faceHighwayDirectionPitchDown();
        return true;
    }

    public static boolean isBouncing() { return bouncing; } // was: oZHMlTL()

    // ----------------------------------------------------------------------
    // Elytra bounce management
    // ----------------------------------------------------------------------
    /** True if the floor+tunnel ahead (out to {@code distance}) is already clear. */
    private static boolean isPathClearAhead(boolean isCardinal, int distance) { // was: FvaNWO(boolean,int)
        if (mc.world == null) return false;
        List<Block> blacklist = KekNuker.getBlacklist();
        BlockPos[] floor = isCardinal
            ? BlockPositions.cardinalFloor(distance, 1, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail())
            : BlockPositions.diagonalFloor(distance, 1, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail());
        for (BlockPos pos : floor) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!blacklist.contains(block) && (HighwayBuilder.replaceCryingObsidian() || block != Blocks.RESPAWN_ANCHOR)
                && block != Blocks.BEDROCK && block != HighwayBuilder.getFillBlock()) {
                return false;
            }
        }
        BlockPos[] tunnel = isCardinal ? BlockPositions.cardinalTunnel(distance, 1) : BlockPositions.diagonalTunnel(distance, 1);
        for (BlockPos pos : tunnel) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!blacklist.contains(block) && block != Blocks.BEDROCK && block != Blocks.VOID_AIR
                && !mc.world.getBlockState(pos).isAir()) {
                return false;
            }
        }
        return true;
    }

    /** True if the player is within {@code m}=32 blocks of the current leg's end. */
    private static boolean isNearLegEnd(HighwayLocator.Checkpoint d) { // was: psJq59YIbp3Z(Checkpoint)
        if (mc.player == null) return false;
        int px = mc.player.getBlockX();
        int pz = mc.player.getBlockZ();
        double D = Math.abs(d.axisValue);
        double K = d.axisValue;
        int m = SCAN_32;
        return switch (d.direction) {
            case NORTH -> pz <= -D + m;
            case EAST  -> px >= D - m;
            case SOUTH -> pz >= D - m;
            case WEST  -> px <= -D + m;
            case NORTH_EAST -> K > 0.0 ? pz <= m : px >= -m;
            case NORTH_WEST -> K > 0.0 ? px <= m : pz <= m;
            case SOUTH_EAST -> K > 0.0 ? pz >= -m : px >= -m;
            case SOUTH_WEST -> K > 0.0 ? px <= m : pz >= -m;
        };
    }

    private static void configureBounce() { // was: krxNb5lcQuWA()
        PlayerUtils.setModuleSetting(KekBounce.class, "mode", KekBounce.Mode.SIMPLE);
        PlayerUtils.setModuleSetting(KekBounce.class, "y-motion", false);
        PlayerUtils.setModuleSetting(KekBounce.class, "simple-obstacle-passer", true);
        if (mc.player != null) PlayerUtils.setModuleSetting(KekBounce.class, "y-level", mc.player.getBlockY());
    }

    /** Enables the elytra bounce (only if the player has an elytra). */
    private static void startBounce() { // was: nt0HZnvBBp()
        if (bouncing || mc.player == null) return;
        boolean hasElytra = false;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ELYTRA) { hasElytra = true; break; }
        }
        if (hasElytra) {
            configureBounce();
            Module kekBounce = Modules.get().get(KekBounce.class);
            if (!kekBounce.isActive()) kekBounce.toggle();
            bouncing = true;
            bounceCooldownTicks = 0;
        }
    }

    /** Disables the elytra bounce and starts the cooldown. */
    public static void stopBounce() { // was: xQr5FhbwpQPWgIQ()
        if (bouncing) {
            Module kekBounce = Modules.get().get(KekBounce.class);
            if (kekBounce.isActive()) kekBounce.toggle();
            bouncing = false;
            bounceCooldownTicks = BOUNCE_COOLDOWN;
        }
    }

    /** Resets all bounce/fall-recovery state (called on deactivate). */
    public static void reset() { // was: OMMZL1F3q()
        bouncing = false;
        bounceCooldownTicks = 0;
        fellOff = false;
        offHighwayTicks = 0;
        repathTicks = 0;
    }

    // ----------------------------------------------------------------------
    // Fall recovery
    // ----------------------------------------------------------------------
    /** If the player fell off the highway, paths back to it. Returns true while recovering. */
    public static boolean handleFallRecovery(HighwayState state) { // was: FvaNWO(HighwayState)
        if (mc.player == null || mc.world == null) return false;
        if (state.getCenterY() == null || state.getDirection() == null) return false;
        Module echestFarmer = Modules.get().get(EchestFarmer.class);
        if (echestFarmer.isActive() || PlayerUtils.isGatheringItem() || SourceRemover.isRemoving() || InventoryManager.isPending
            || state.isFlag2() || state.isFlag7() || state.isFlag8() || state.isFlag9() || state.isFlag10()) {
            offHighwayTicks = 0;
            fellOff = false;
            return false;
        }
        if ((bouncing || bounceCooldownTicks > 0) && !fellOff) {
            offHighwayTicks = 0;
            return false;
        }
        int targetY = state.getCenterY();
        boolean atCorrectY = mc.player.getBlockY() == targetY;
        if (!fellOff) {
            if (atCorrectY) { offHighwayTicks = 0; return false; }
            if (++offHighwayTicks < FALL_GRACE) return false;
            fellOff = true;
            repathTicks = 0;
            stopBounce();
            PlayerUtils.setAutoWalk(false);
            MusheorSystem.debug("Fell off the highway (Y %s != %s) — pathing back...", mc.player.getBlockY(), targetY);
        }
        if (atCorrectY && mc.player.isOnGround()) {
            fellOff = false;
            offHighwayTicks = 0;
            if (PathingHelper.isPathing()) PathingHelper.cancelEverything();
            MusheorSystem.debug("Back on the highway — resuming paving.");
            return false;
        } else {
            if (repathTicks <= 0) { pathBackToHighway(state, targetY); repathTicks = REPATH_INTERVAL; }
            else repathTicks--;
            return true;
        }
    }

    private static void pathBackToHighway(HighwayState state, int targetY) { // was: FvaNWO(HighwayState,int)
        BlockPos nearest = findNearestStandable(targetY, RECOVERY_RANGE);
        if (nearest != null) {
            PathingHelper.setGoal(new GoalBlock(nearest));
        } else if (state.getCenterX() != null && state.getCenterZ() != null) {
            PathingHelper.setGoal(new GoalBlock(state.getCenterX(), targetY, state.getCenterZ()));
        }
    }

    /** Finds the nearest standable spot on the pavement at {@code targetY} within {@code range}. */
    private static BlockPos findNearestStandable(int targetY, int range) { // was: FvaNWO(int,int)
        if (mc.player == null || mc.world == null) return null;
        BlockPos p = mc.player.getBlockPos();
        Block pavement = HighwayBuilder.getFillBlock();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                BlockPos stand = new BlockPos(p.getX() + x, targetY, p.getZ() + z);
                if (mc.world.getBlockState(stand.down()).getBlock() == pavement
                    && mc.world.getBlockState(stand).isAir() && mc.world.getBlockState(stand.up()).isAir()) {
                    double distSq = p.getSquaredDistance(stand);
                    if (distSq < bestDistSq) { bestDistSq = distSq; best = stand; }
                }
            }
        }
        return best;
    }

    // ----------------------------------------------------------------------
    // AUTO mode — combined loop (detects highway, follows rings/diamonds, paves+digs)
    // ----------------------------------------------------------------------
    public static void runAutoBuild() { // was: zu3a44xDeMFMCRwm()
        assert mc.player != null && mc.world != null;
        HighwayState state = HighwayState.getInstance();
        if (state.getCenterY() == null || state.getDirection() == null) return;
        HighwayLocator.Checkpoint detected = state.getCurrentCheckpoint();
        if (detected == null) return;
        if (handleFallRecovery(state)) return;

        boolean isCardinal = detected.type == HighwayBuilder.HighwayType.CARDINAL;
        boolean isRingOrDiamond = detected.category == HighwayLocator.Category.RING || detected.category == HighwayLocator.Category.DIAMOND;
        if (isRingOrDiamond && handleRingTurn(detected, state)) stopBounce();

        Module echestFarmer = Modules.get().get(EchestFarmer.class);
        if (echestFarmer.isActive() || PlayerUtils.isGatheringItem() || SourceRemover.isRemoving()) {
            stopBounce();
            PlayerUtils.setAutoWalk(false);
            return;
        }
        if (KekNuker.isBusyInBuildZone() || HighwayBuilder.isEating() || HighwayBuilder.isKillAuraAttacking() || InventoryManager.isPending) {
            stopBounce();
            PlayerUtils.setAutoWalk(false);
        }
        if (HighwayBuilder.advancedSourceFiller()) WorldUtils.handleLavaRemoval();

        if (HighwayBuilder.isAutoBounceEnabled()) {
            if (bounceCooldownTicks > 0) { bounceCooldownTicks--; return; }
            boolean nearLegEnd = isRingOrDiamond && isNearLegEnd(detected);
            boolean interrupted = KekNuker.isBusyInBuildZone() || HighwayBuilder.isEating() || HighwayBuilder.isKillAuraAttacking()
                || InventoryManager.isPending || PlayerUtils.isGatheringItem() || SourceRemover.isRemoving()
                || echestFarmer.isActive() || PathingHelper.isPathing();
            if (!nearLegEnd && !interrupted && isPathClearAhead(isCardinal, HighwayBuilder.getBounceDistanceCheck())) startBounce();
            else stopBounce();
            if (bouncing) { PlayerUtils.faceHighwayDirection(); return; }
        }

        if (isCardinal) {
            WorldUtils.checkFrontCollision();
            PlayerUtils.faceHighwayDirectionPitchDown();
            PlayerUtils.strafeToCenterline();
        } else {
            boolean isNESW = state.getDirection() == WorldUtils.Direction8.NORTH_EAST || state.getDirection() == WorldUtils.Direction8.SOUTH_WEST;
            int playerDiagVal = isNESW ? mc.player.getBlockX() + mc.player.getBlockZ() : mc.player.getBlockX() - mc.player.getBlockZ();
            if (playerDiagVal == (int) detected.axisValue) {
                state.setCenterX(mc.player.getBlockX());
                state.setCenterZ(mc.player.getBlockZ());
            }
            PlayerUtils.alignToHighway();
            WorldUtils.checkFrontCollision();
            PlayerUtils.faceHighwayDirectionPitchDown();
        }

        BlockPos[] allPositions = isCardinal
            ? BlockPositions.cardinalFloor(3, 2, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail())
            : BlockPositions.diagonalFloor(2, 2, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail());
        List<BlockPos> placePositions = new ArrayList<>();
        List<BlockPos> clearPositions = new ArrayList<>();
        for (BlockPos pos : allPositions) {
            if (pos.getY() != state.getCenterY() || !HighwayLocator.isRailOnAnyHighway(pos, detected)) placePositions.add(pos);
            else if (HighwayLocator.isOnDetectedHighway(pos, detected)) clearPositions.add(pos);
        }
        if (HighwayBuilder.INSTANCE.toggleKekNuker.get()) KekNuker.extraBreakQueue.clear();

        state.setFlag6(false);
        WorldUtils.spleefEntities(allPositions);
        if (HighwayBuilder.INSTANCE.toggleKekNuker.get()) {
            for (BlockPos pos : placePositions) {
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block != Blocks.VOID_AIR && block != Blocks.BEDROCK
                    && (HighwayBuilder.replaceCryingObsidian() || block != Blocks.RESPAWN_ANCHOR)
                    && WorldUtils.shouldBreak(pos, HighwayBuilder.getFillBlock())) {
                    KekNuker.extraBreakQueue.add(pos.toImmutable());
                }
            }
            BlockPos[] tunnelClear = isCardinal ? BlockPositions.cardinalTunnel(3, 1) : BlockPositions.diagonalTunnel(2, 1);
            for (BlockPos pos : tunnelClear) {
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block != Blocks.VOID_AIR && block != Blocks.BEDROCK && WorldUtils.shouldBreak(pos, Blocks.AIR))
                    KekNuker.extraBreakQueue.add(pos.toImmutable());
            }
            for (BlockPos pos : clearPositions) {
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block != Blocks.VOID_AIR && block != Blocks.BEDROCK && WorldUtils.shouldBreak(pos, Blocks.AIR))
                    KekNuker.extraBreakQueue.add(pos.toImmutable());
            }
        }
        state.setFlag4(false);
        WorldUtils.placeHighwayBlocks(placePositions.toArray(new BlockPos[0]), false);
        if (HighwayBuilder.hasCeiling()) {
            BlockPos[] ceilingPositions = isCardinal
                ? BlockPositions.cardinalCeiling(2, 2, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail())
                : BlockPositions.diagonalCeiling(2, 1, HighwayBuilder.placeLeftRail(), HighwayBuilder.placeRightRail());
            WorldUtils.placeHighwayBlocks(ceilingPositions, false);
        }
        if (!state.isFlag4() || !state.isFlag3() || !WorldUtils.needsPlacement() || !state.isFlag5() || !KekNuker.isBusyInBuildZone() || !state.isFlag6()) {
            PlayerUtils.setAutoWalk(true);
        }
    }
}
