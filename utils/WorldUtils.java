// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.KekNuker;
import musheor.utils.BlockPositions;
import musheor.utils.InventoryManager;
import musheor.utils.PlayerUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SignBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class WorldUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int gatherItemIndex = 0; // was: slvxzlssLu

    /** Returns true if the block directly below the player's feet is the given block type. */
    public static boolean isBlockAtFeet(Block block) { // was: Gt56Sj4a6BWhgB(Block)
        if (WorldUtils.mc.player == null || WorldUtils.mc.world == null) {
            return false;
        }
        BlockPos pos = WorldUtils.mc.player.getBlockPos().down();
        return WorldUtils.mc.world.getBlockState(pos).getBlock() == block;
    }

    /** Returns a BlockPos 2 blocks ahead of the player in the given Direction8. */
    public static BlockPos getOffset2AheadPos(Direction8 direction8) { // was: jOdDDFXSeWl4(Direction8)
        int x = (int) WorldUtils.mc.player.getX();
        int y = (int) WorldUtils.mc.player.getY();
        int z = (int) WorldUtils.mc.player.getZ();
        int dx = 0;
        int dz = 0;
        switch (direction8.ordinal()) {
            case 4: { dz = 2;  break; }   // SOUTH
            case 0: { dz = -2; break; }   // NORTH
            case 6: { dx = -2; break; }   // WEST
            case 2: { dx = 2;  break; }   // EAST
            case 5: { dx = -2; dz = 2;  break; } // SOUTH_WEST
            case 3: { dx = 2;  dz = 2;  break; } // SOUTH_EAST
            case 7: { dx = -2; dz = -2; break; } // NORTH_WEST
            case 1: { dx = 2;  dz = -2; break; } // NORTH_EAST
        }
        return new BlockPos(x + dx, y, z + dz);
    }

    /** Lag detection stub — always returns false (feature disabled). */
    public static boolean isLagDetected() { // was: aZElPUDuPV3EqLnc
        return false;
    }

    /**
     * Finds the nearest obsidian block at highway Y-level within 25 blocks and
     * tells Baritone to path to it ("return to highway").
     */
    public static void returnToHighway() { // was: WvQP0Zr
        if (WorldUtils.mc.player == null || WorldUtils.mc.world == null) {
            return;
        }
        BlockPos playerPos = WorldUtils.mc.player.getBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        int radius = 25;
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                BlockPos candidate = new BlockPos(
                    playerPos.getX() + i,
                    HighwayState.getInstance().getHighwayY().intValue(),
                    playerPos.getZ() + j);
                if (WorldUtils.mc.world.getBlockState(
                        candidate.withY(HighwayState.getInstance().getHighwayY() - 1))
                        .getBlock() != Blocks.OBSIDIAN) continue;
                double dist = playerPos.toCenterPos().squaredDistanceTo(
                    (double) candidate.getX(),
                    (double) HighwayState.getInstance().getHighwayY().intValue(),
                    (double) candidate.getZ());
                if (dist > 25.0 || !(dist < bestDist)) continue;
                bestDist = dist;
                best = candidate;
            }
        }
        if (best != null) {
            PathingHelper.setGoal(best);
            PathingHelper.startPathing();
            MusheorSystem.debug("Going back onto the highway...", new Object[0]);
        }
    }

    /** Returns true if any adjacent face (excluding UP) of the given BlockPos is liquid. */
    public static boolean hasAdjacentSolid(BlockPos pos) { // was: CEOjBr5G5R
        return Arrays.stream(Direction.values())
            .filter(d -> d != Direction.UP)
            .anyMatch(d -> WorldUtils.mc.world.getBlockState(pos.offset(d)).isLiquid());
    }

    /**
     * Detects lava blocks ahead of the player and initiates lava-removal procedure
     * by setting a target block in HighwayState.
     */
    public static void handleLavaRemoval() { // was: selaO6lwe7
        if (WorldUtils.mc.player == null || WorldUtils.mc.world == null) {
            return;
        }
        if (HighwayState.getInstance().getLavaTargetBlock() != null) {
            if (WorldUtils.mc.world.getFluidState(
                    HighwayState.getInstance().getLavaTargetBlock()).getFluid()
                    != Fluids.LAVA) {
                PathingHelper.stopPathing();
                HighwayState.getInstance().setLavaTargetBlock(null);
            } else {
                PathingHelper.setGoal(
                    HighwayState.getInstance().getLavaTargetBlock()
                        .withY(HighwayState.getInstance().getHighwayY().intValue()));
            }
            return;
        }
        if (HighwayState.getInstance().getSavedReturnPos() != null) {
            if (WorldUtils.mc.player.getBlockPos()
                    .equals(HighwayState.getInstance().getSavedReturnPos())) {
                PathingHelper.stopPathing();
                HighwayState.getInstance().setSavedReturnPos(null);
            } else {
                PathingHelper.setGoal(HighwayState.getInstance().getSavedReturnPos());
            }
            return;
        }
        if (HighwayBuilder.getHighwayType() == HighwayBuilder.HighwayType.CARDINAL) {
            for (BlockPos pos : BlockPositions.getCardinalObstructionPositions()) {
                if (PathingHelper.isAlreadyPathing()) return;
                if (WorldUtils.mc.world.getFluidState(pos).getFluid() == Fluids.FLOWING_LAVA
                        || WorldUtils.mc.world.getFluidState(pos).getFluid() != Fluids.LAVA) continue;
                MusheorSystem.debug("Found obstructing lava ahead, removing...", new Object[0]);
                PlayerUtils.setAutoWalkActive(false);
                HighwayState.getInstance().setLavaTargetBlock(pos);
                HighwayState.getInstance().setSavedReturnPos(
                    WorldUtils.mc.player.getBlockPos().toImmutable());
                return;
            }
        }
        if (HighwayBuilder.getHighwayType() == HighwayBuilder.HighwayType.DIAGONAL) {
            for (BlockPos pos : BlockPositions.getDiagonalObstructionPositions()) {
                if (PathingHelper.isAlreadyPathing()) return;
                if (WorldUtils.mc.world.getFluidState(pos).getFluid() == Fluids.FLOWING_LAVA
                        || WorldUtils.mc.world.getFluidState(pos).getFluid() != Fluids.LAVA) continue;
                MusheorSystem.debug("Found obstructing lava ahead, removing...", new Object[0]);
                PlayerUtils.setAutoWalkActive(false);
                HighwayState.getInstance().setLavaTargetBlock(pos);
                HighwayState.getInstance().setSavedReturnPos(
                    WorldUtils.mc.player.getBlockPos().toImmutable());
                return;
            }
        }
    }

    /** Returns all non-air BlockPos positions occupied by the entity's bounding box foot region. */
    public static List<BlockPos> getEntityFootBlocks(Entity entity) { // was: jOdDDFXSeWl4(Entity)
        assert (WorldUtils.mc.world != null);
        ArrayList<BlockPos> result = new ArrayList<>();
        Box box = entity.getBoundingBox().withMinY(entity.getY() - 0.2).withMaxY(entity.getY());
        int minX = MathHelper.floor((double) box.getMinPos().getX());
        int maxX = MathHelper.floor((double) box.getMaxPos().getX());
        int minZ = MathHelper.floor((double) box.getMinPos().getZ());
        int maxZ = MathHelper.floor((double) box.getMaxPos().getZ());
        int y    = MathHelper.floor((double)(entity.getY() - 0.2));
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                BlockPos pos = new BlockPos(x, y, z);
                if (WorldUtils.getBlockAt(pos) == Blocks.AIR) continue;
                result.add(pos);
            }
        }
        return result;
    }

    /** Returns the first Entity found at the given BlockPos, or null. */
    public static Entity getEntityAt(World world, BlockPos pos) { // was: Gt56Sj4a6BWhgB(World,BlockPos)
        Box box = new Box(pos);
        List<?> list = world.getEntitiesByClass(Entity.class, box, e -> true);
        return list.isEmpty() ? null : (Entity) list.getFirst();
    }

    /**
     * Sends a block-interact (place) packet to the server for the given position and face.
     * Returns true on success.
     */
    public static boolean placeBlockPacket(BlockPos pos, Direction side) { // was: jOdDDFXSeWl4(BlockPos,Direction)
        if (WorldUtils.mc.player == null || mc.getNetworkHandler() == null || WorldUtils.mc.interactionManager == null) {
            return false;
        }
        WorldUtils.swapCarriedItems();
        WorldUtils.sendPlacePacket(Hand.OFF_HAND, WorldUtils.makeHitResult(pos, side));
        WorldUtils.swapCarriedItems();
        return true;
    }

    /** Creates a BlockHitResult aimed at the center of the given face of the given BlockPos. */
    public static BlockHitResult makeHitResult(BlockPos pos, Direction side) { // was: mp3zoXQFKUKYj5(BlockPos,Direction)
        return new BlockHitResult(Vec3d.ofCenter((Vec3i) pos), side, pos, false);
    }

    /** Sends a PlayerInteractBlockC2SPacket for the given hand and hit result. */
    public static void sendPlacePacket(Hand hand, BlockHitResult hitResult) { // was: jOdDDFXSeWl4(Hand,BlockHitResult)
        WorldUtils.mc.interactionManager.sendSequencedPacket(WorldUtils.mc.world,
            n -> new PlayerInteractBlockC2SPacket(hand, hitResult, n));
    }

    /**
     * Swaps the main-hand and off-hand items using a PlayerActionC2SPacket,
     * then swaps inventory hotbar slots 0 and 40 (main/offhand mirror).
     */
    public static void swapCarriedItems() { // was: l3ot1CwoJ9CsS
        if (WorldUtils.mc.player == null || WorldUtils.mc.world == null) {
            return;
        }
        WorldUtils.mc.interactionManager.sendSequencedPacket(WorldUtils.mc.world,
            n -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos.ORIGIN, Direction.DOWN));
        ItemStack mainHand = WorldUtils.mc.player.getMainHandStack();
        ItemStack offHand  = WorldUtils.mc.player.getOffHandStack();
        WorldUtils.mc.player.getInventory().setStack(
            WorldUtils.mc.player.getInventory().selectedSlot, offHand);
        WorldUtils.mc.player.getInventory().setStack(40, mainHand);
    }

    /**
     * Equips the given item and places a block at the position with the given face,
     * if Meteor's BlockUtils says placement is allowed.
     */
    public static boolean placeBlockWithItem(Item item, BlockPos pos, Direction side) { // was: jOdDDFXSeWl4(Item,BlockPos,Direction)
        if (!BlockUtils.canPlaceBlock(pos, true, Block.getBlockFromItem(item))) {
            return false;
        }
        InventoryManager.equipItem(item);
        return WorldUtils.placeBlockPacket(pos, side);
    }

    /** Points the player's view toward the center of the given BlockPos, choosing the best face. */
    public static void lookAtBlock(BlockPos pos) { // was: MS1x7YGHjIg7eB
        Vec3d hitVec = Vec3d.ofCenter((Vec3i) pos);
        Direction side = BlockUtils.getPlaceSide(pos);
        if (side != null) {
            pos.offset(side);
            hitVec = hitVec.add(Vec3d.of((Vec3i) side.getVector()).multiply(0.5));
        }
        assert (WorldUtils.mc.player != null);
        float[] angles = WorldUtils.calcAngles(WorldUtils.mc.player, hitVec);
        WorldUtils.mc.player.setYaw(angles[0]);
        WorldUtils.mc.player.setPitch(angles[1]);
    }

    /** Points the player's view toward the center of the given face on the given BlockPos. */
    public static void lookAtBlockFace(BlockPos pos, Direction side) { // was: Gt56Sj4a6BWhgB(BlockPos,Direction)
        Vec3d hitVec = Vec3d.ofCenter((Vec3i) pos)
            .add(Vec3d.of((Vec3i) side.getVector()).multiply(0.5));
        float[] angles = WorldUtils.calcAngles(WorldUtils.mc.player, hitVec);
        WorldUtils.mc.player.setYaw(angles[0]);
        WorldUtils.mc.player.setPitch(angles[1]);
    }

    /** Calculates yaw and pitch angles needed for the given entity to look at targetPos. */
    static float[] calcAngles(ClientPlayerEntity entity, Vec3d targetPos) { // was: jOdDDFXSeWl4(Entity,Vec3d)
        Vec3d eyePos = entity.getEyePos();
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double yaw   = -Math.atan2(dx, dz) / Math.PI * 180.0;
        double pitch = -Math.asin(dy / dist) / Math.PI * 180.0;
        return new float[]{(float) yaw, (float) pitch};
    }

    /** Returns true if every BlockPos in the list has at least one solid neighbor below/adjacent. */
    public static boolean allHaveSupport(List<BlockPos> positions) { // was: UgB10d(List)
        if (positions.isEmpty()) return false;
        assert (WorldUtils.mc.player != null);
        for (BlockPos pos : positions) {
            boolean hasSupport = false;
            for (int i = -1; i <= 1 && !hasSupport; ++i) {
                for (int j = -1; j <= 1 && !hasSupport; ++j) {
                    BlockPos below = pos.add(i, -1, j);
                    if (WorldUtils.mc.world.getBlockState(below).isAir()) continue;
                    hasSupport = true;
                }
            }
            if (hasSupport) continue;
            return false;
        }
        return true;
    }

    /** Returns true if the BlockPos is not air, not replaceable, and not the given block type. */
    public static boolean needsPlacement(BlockPos pos, Block block) { // was: jOdDDFXSeWl4(BlockPos,Block)
        assert (WorldUtils.mc.world != null);
        return !WorldUtils.mc.world.getBlockState(pos).isAir()
            && !WorldUtils.mc.world.getBlockState(pos).isReplaceable()
            && WorldUtils.mc.world.getBlockState(pos).getBlock() != block;
    }

    /**
     * Spleef detection: if a non-player, non-end-crystal, non-boat entity is standing on
     * a block in the array, mine that block to drop the griefer.
     */
    public static void detectAndHandleSpleef(BlockPos[] positions) { // was: jOdDDFXSeWl4(BlockPos[])
        for (BlockPos pos : positions) {
            if (!meteordevelopment.meteorclient.utils.player.PlayerUtils.isWithinReach(pos)
                    || !BlockUtils.canPlace(pos, false)
                    || BlockUtils.canPlace(pos, true)) continue;
            PlayerUtils.setAutoWalkActive(false);
            MusheorSystem.debug("Spleefing = %s", HighwayState.getInstance().isSpleefing());
            assert (WorldUtils.mc.world != null);
            Entity entity = WorldUtils.getEntityAt((World) WorldUtils.mc.world, pos);
            if (entity == null) return;
            if (entity == WorldUtils.mc.player
                    || !entity.isAlive()
                    || entity instanceof EndCrystalEntity
                    || entity instanceof BoatEntity) continue;
            List<BlockPos> footBlocks = WorldUtils.getEntityFootBlocks(entity);
            for (BlockPos footPos : footBlocks) {
                if (!BlockUtils.canPlace(pos, true)) {
                    boolean inArray = false;
                    for (BlockPos p : positions) {
                        if (!p.equals(footPos)) continue;
                        inArray = true;
                        break;
                    }
                    InventoryManager.equipBestToolForBlock(footPos);
                    BlockUtils.breakBlock(footPos, true);
                    HighwayState.getInstance().setSpleefing(true);
                    MusheorSystem.debug("Spleefing %s at x: %s y: %s z: %s",
                        entity.getName().getString(),
                        entity.getBlockPos().getX(),
                        entity.getBlockPos().getY(),
                        entity.getBlockPos().getZ());
                    return;
                }
                HighwayState.getInstance().setSpleefing(false);
            }
            return;
        }
    }

    /** Returns true if the BlockPos is within the configured placement range. */
    public static boolean isInPlacementRange(BlockPos pos) { // was: KDNrzlU9qtrEv
        double range = (Double) MusheorSystem.Manager.placementRange.get();
        return WorldUtils.mc.player.squaredDistanceTo(
            (double) pos.getX(),
            (double) pos.getY(),
            (double) pos.getZ()) <= range * range;
    }

    /** Returns true if the BlockPos is within the given distance (squared check). */
    public static boolean isWithinDistance(BlockPos pos, double distance) { // was: jOdDDFXSeWl4(BlockPos,double)
        return WorldUtils.mc.player.squaredDistanceTo(
            (double) pos.getX(),
            (double) pos.getY(),
            (double) pos.getZ()) <= distance * distance;
    }

    /**
     * Removes stale entries from the placement-tracking cache.
     * Entries older than placementTimeout ticks or matching the given block type are removed.
     */
    public static void cleanPlacementCache(int currentTick, Block block) { // was: jOdDDFXSeWl4(int,Block)
        HighwayState.getInstance().getPlacementCache().entrySet().removeIf(entry -> {
            if (block != null && WorldUtils.mc.world.getBlockState(
                    (BlockPos) entry.getKey()).getBlock() == block) {
                return true;
            }
            return currentTick - (Integer) entry.getValue()
                > (Integer) MusheorSystem.Manager.placementTimeout.get();
        });
    }

    /**
     * Main block-placement loop. Places all blocks in the array that need placing,
     * subject to rate control. {@code isFloor} true = floor block, false = ceiling block.
     */
    public static void tryPlaceBlocks(BlockPos[] positions, boolean isFloor) { // was: jOdDDFXSeWl4(BlockPos[],boolean)
        HighwayState state = HighwayState.getInstance();
        if (WorldUtils.mc.player == null || WorldUtils.mc.world == null
                || state.isSpleefing()
                || KekNuker.isActive()
                || HighwayBuilder.isWaiting()) {
            return;
        }
        Block targetBlock = isFloor ? HighwayBuilder.getFloorBlock()
                                    : HighwayBuilder.getPavingBlock();
        double distToSolid = WorldUtils.getDistanceToFirstSolidAhead(HighwayBuilder.getPavingBlock());

        if (HighwayBuilder.getHighwayType() == HighwayBuilder.HighwayType.CARDINAL) {
            if (distToSolid <= 1.0) {
                PlayerUtils.setAutoWalkActive(false);
                state.setWalkingBlocked(true);
            } else {
                state.setWalkingBlocked(false);
                PlayerUtils.setAutoWalkActive(true);
            }
        } else if (distToSolid <= 1.5) {
            PlayerUtils.setAutoWalkActive(false);
            state.setWalkingBlocked(true);
        } else {
            state.setWalkingBlocked(false);
            PlayerUtils.setAutoWalkActive(true);
        }

        boolean anyNeedPlacing = false;
        for (BlockPos pos : positions) {
            if (WorldUtils.mc.world.getBlockState(pos).getBlock() == targetBlock
                    || !BlockUtils.canPlace(pos, true)
                    || !WorldUtils.isInPlacementRange(pos)) continue;
            anyNeedPlacing = true;
            break;
        }

        if (anyNeedPlacing) {
            InventoryManager.equipItem(targetBlock.asItem());
            WorldUtils.swapCarriedItems();
            for (BlockPos pos : positions) {
                if (!BlockUtils.canPlace(pos, true)) continue;
                state.setCurrentlyPlacing(true);
                if (RateController.checkPlaceRate()) {
                    WorldUtils.sendPlacePacket(Hand.OFF_HAND,
                        WorldUtils.makeHitResult(pos, Direction.DOWN));
                    state.getPlacementCache().put(pos, state.getCurrentTick());
                    MusheorSystem.debug("placed \u00a75%s \u00a7rat %s, %s, %s",
                        Registries.ITEM.getId(targetBlock.asItem()).toString(),
                        pos.getX(), pos.getY(), pos.getZ());
                    if (state.getRecentlyPlaced().contains(pos)) continue;
                    state.getRecentlyPlaced().add(pos);
                    if (!HighwayBuilder.getPavingBlock().equals(Blocks.OBSIDIAN)) continue;
                    state.incrementBlocksPlaced();
                    continue;
                }
                state.setCurrentlyPlacing(false);
                break;
            }
            WorldUtils.swapCarriedItems();
        }
    }

    /** Places blocks from a list at positions within 4.5 blocks, using the given block type. */
    public static void placeBlockList(List<BlockPos> positions, Block block) { // was: jOdDDFXSeWl4(List,Block)
        InventoryManager.equipItem(block.asItem());
        WorldUtils.swapCarriedItems();
        for (BlockPos pos : positions) {
            if (!BlockUtils.canPlace(pos, true)
                    || !meteordevelopment.meteorclient.utils.player.PlayerUtils.isWithin(pos, 4.5)) continue;
            if (!RateController.checkPlaceRate()) break;
            WorldUtils.sendPlacePacket(Hand.OFF_HAND,
                WorldUtils.makeHitResult(pos, Direction.DOWN));
        }
        WorldUtils.swapCarriedItems();
    }

    /** Places netherrack blocks below the player's feet in the four forward positions for ice-floor highways. */
    public static void buildIceFloor() { // was: QigP9ftge6
        if (WorldUtils.mc.player == null || WorldUtils.mc.world == null) return;
        BlockPos playerPos = WorldUtils.mc.player.getBlockPos();
        for (int i = 1; i <= 4; ++i) {
            BlockPos target = null;
            switch (HighwayBuilder.getDirection().ordinal()) {
                case 4: target = playerPos.add(0, -1, -i); break;
                case 0: target = playerPos.add(0, -1,  i); break;
                case 6: target = playerPos.add( i, -1, 0); break;
                case 2: target = playerPos.add(-i, -1, 0); break;
                case 5: target = playerPos.add( i, -1, -i); break;
                case 3: target = playerPos.add(-i, -1, -i); break;
                case 7: target = playerPos.add( i, -1,  i); break;
                case 1: target = playerPos.add(-i, -1,  i); break;
            }
            if (target == null) return;
            if (mc.getNetworkHandler() == null) return;
            if (WorldUtils.mc.world.getBlockState(target).getBlock() == Blocks.SOUL_SAND) {
                BlockUtils.breakBlock(target, false);
            }
            if (!WorldUtils.mc.world.getBlockState(target).isAir()) continue;
            WorldUtils.placeBlockWithItem(Items.NETHERRACK, target, Direction.DOWN);
        }
    }

    /** Checks for server-side lag using TickRate; pauses AutoWalk if lag is detected. */
    public static boolean checkForLag() { // was: btLCQHvKVR
        float timeSinceTick;
        if (HighwayBuilder.isLagDetectionEnabled()
                && (timeSinceTick = TickRate.INSTANCE.getTimeSinceLastTick())
                    > (float) HighwayBuilder.getLagThreshold()) {
            MusheorSystem.debug("Lag detected, pausing...", new Object[0]);
            PlayerUtils.setAutoWalkActive(false);
            return true;
        }
        return false;
    }

    /**
     * Steps through a list of items to gather, enabling the GatherItem module for
     * the next item each call. Resets when all items are collected.
     */
    public static void gatherNextItem(List<ItemStack> items) { // was: KP44bk(List)
        if (gatherItemIndex >= items.size()) {
            gatherItemIndex = 0;
            return;
        }
        if (!PlayerUtils.isGatheringItem()) {
            ItemStack item = items.get(gatherItemIndex);
            PlayerUtils.setAutoWalkActive(false);
            PlayerUtils.startGatherItem(item, false);
            MusheorSystem.debug("Gathering item: %s", item);
            ++gatherItemIndex;
        }
    }

    /**
     * Scans forward (up to 8 blocks) for the first block whose default state is not a
     * full-solid cube, and returns the horizontal distance to it from the player's feet.
     */
    public static double getDistanceToFirstSolidAhead(Block block) { // was: TAdu5cndwWu3A1(Block)
        int dx = 0, dz = 0;
        Direction8 dir = HighwayBuilder.getDirection();
        switch (dir.ordinal()) {
            case 4: dz = -1; break;
            case 0: dz =  1; break;
            case 6: dx =  1; break;
            case 2: dx = -1; break;
            case 5: dx =  1; dz = -1; break;
            case 1: dx = -1; dz =  1; break;
            case 3: dz = -1; dx = -1; break;
            case 7: dz =  1; dx =  1; break;
        }
        assert (WorldUtils.mc.player != null && WorldUtils.mc.world != null);
        BlockPos feetPos = WorldUtils.mc.player.getBlockPos()
            .withY((int)(WorldUtils.mc.player.getY() - 1));
        BlockPos solidPos = null;
        for (int i = 0; i <= 8; ++i) {
            int nx = (int) WorldUtils.mc.player.getX() + dx * i;
            int nz = (int) WorldUtils.mc.player.getZ() + dz * i;
            BlockPos candidate = new BlockPos(nx, (int)(WorldUtils.mc.player.getY() - 1), nz);
            if (WorldUtils.mc.world.getBlockState(candidate).getBlock()
                    .getDefaultState().isSolid()) continue;
            solidPos = candidate;
            break;
        }
        if (solidPos == null) return 10.0;
        return WorldUtils.horizontalDistance(solidPos, feetPos);
    }

    /** Returns the Block at the given BlockPos. */
    public static Block getBlockAt(BlockPos pos) { // was: WOqvNwnejoKApoa
        assert (WorldUtils.mc.world != null && WorldUtils.mc.player != null);
        return WorldUtils.mc.world.getBlockState(pos).getBlock();
    }

    /** Sends a STOP_DESTROY_BLOCK packet to the server. */
    public static void sendDigPacket(BlockPos pos, Direction side) { // was: TAdu5cndwWu3A1(BlockPos,Direction)
        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(
            (Packet<?>) new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, side));
    }

    /** Returns the 2D horizontal distance (XZ plane) between two BlockPos. */
    public static double horizontalDistance(BlockPos a, BlockPos b) { // was: Gt56Sj4a6BWhgB(BlockPos,BlockPos)
        double dx = Math.abs((double)(a.getX() - b.getX()));
        double dz = Math.abs((double)(a.getZ() - b.getZ()));
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Checks a column of blocks ahead of the player (height 3 for normal, 4 for
     * elytra mode) for non-air, non-sign obstructions and stops walking if found.
     */
    public static void checkForwardCollisions() { // was: gBxN0D8GSyidOa
        assert (WorldUtils.mc.player != null && WorldUtils.mc.world != null);
        int dx = 0, dz = 0;
        Direction8 dir = HighwayBuilder.getDirection();
        switch (dir.ordinal()) {
            case 5: dx =  1; dz = -1; break;
            case 1: dx = -1; dz =  1; break;
            case 3: dx = dz = -1;     break;
            case 7: dx = dz =  1;     break;
            case 4: dz = -1;           break;
            case 6: dx =  1;           break;
            case 0: dz =  1;           break;
            case 2: dx = -1;           break;
        }
        int height = 0;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.NORMAL) height = 3;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.ELYTRA)  height = 4;
        for (int i = 0; i < height; ++i) {
            BlockPos ahead = WorldUtils.mc.player.getBlockPos().add(dx, i, dz);
            if (WorldUtils.mc.world.getBlockState(ahead).isAir()
                    || WorldUtils.mc.world.getBlockState(ahead).getBlock() instanceof SignBlock) continue;
            if (WorldUtils.mc.world.getFluidState(ahead).getFluid() != Fluids.LAVA
                    || WorldUtils.mc.world.getFluidState(ahead).getFluid() != Fluids.FLOWING_LAVA
                    || WorldUtils.mc.world.getBlockState(ahead).getBlock() != Blocks.FIRE) {
                PlayerUtils.setAutoWalkActive(false);
            }
            MusheorSystem.debug("Collision detected in front of the player", new Object[0]);
        }
    }

    /**
     * For diagonal highways: places a safety block below-and-ahead to prevent the
     * player from falling into a gap. Returns true if a block was placed.
     */
    public static boolean placeSafetyBlock() { // was: PROcSc3gv
        assert (WorldUtils.mc.player != null && WorldUtils.mc.world != null);
        int dx = 0, dz = 0;
        Direction8 dir = HighwayBuilder.getDirection();
        switch (dir.ordinal()) {
            case 5: dx =  1; dz = -1; break;
            case 1: dx = -1; dz =  1; break;
            case 3: dx = dz = -1;     break;
            case 7: dx = dz =  1;     break;
        }
        BlockPos below = WorldUtils.mc.player.getBlockPos().add(dx, -1, dz);
        if (WorldUtils.mc.world.getBlockState(below).isAir()) {
            InventoryManager.equipItem(Items.NETHERRACK);
            WorldUtils.placeBlockPacket(below, Direction.DOWN);
            return true;
        }
        return false;
    }

    /** Returns true if there is an ItemEntity with the given item type within 10 blocks. */
    public static boolean isItemNearby(Item item) { // was: Y9BgxR
        if (WorldUtils.mc.world == null || WorldUtils.mc.player == null) return false;
        int radius = 10;
        Box box = new Box(
            WorldUtils.mc.player.getX() - radius,
            WorldUtils.mc.player.getY() - radius,
            WorldUtils.mc.player.getZ() - radius,
            WorldUtils.mc.player.getX() + radius, 122,
            WorldUtils.mc.player.getZ() + radius);
        List<?> list = WorldUtils.mc.world.getEntitiesByClass(ItemEntity.class, box,
            entity -> entity.getStack().getItem() == item);
        return !list.isEmpty();
    }

    /** Finds the nearest ItemEntity with the given item type and tells Baritone to path to it. */
    public static void pathToNearestItem(Item item) { // was: xG2PP8jo4RWLS(Item)
        if (WorldUtils.mc.player == null || WorldUtils.mc.world == null) return;
        ItemEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Entity entity : WorldUtils.mc.world.getEntities()) {
            double dist;
            if (!(entity instanceof ItemEntity itemEntity)
                    || itemEntity.getStack().getItem() != item
                    || !((dist = entity.squaredDistanceTo((Entity) WorldUtils.mc.player)) < nearestDist)) continue;
            nearestDist = dist;
            nearest = itemEntity;
        }
        if (nearest != null) {
            PathingHelper.pathToPos(nearest.getBlockPos());
        } else {
            ChatUtils.error("No matching item found nearby.", new Object[0]);
        }
    }

    /** Converts yaw/pitch angles to a Minecraft Direction (the direction the player is facing). */
    public static Direction getFacingFromAngles(float yaw, float pitch) { // was: mp3zoXQFKUKYj5(float,float)
        if (pitch > 60.0f)  return Direction.DOWN;
        if (pitch < -60.0f) return Direction.UP;
        if ((yaw %= 360.0f) < 0.0f) yaw += 360.0f;
        if (yaw >= 315.0f || yaw < 45.0f)   return Direction.SOUTH;
        if (yaw >= 45.0f  && yaw < 135.0f)  return Direction.WEST;
        if (yaw >= 135.0f && yaw < 225.0f)  return Direction.NORTH;
        return Direction.EAST;
    }

    /** Returns the player's current facing direction as a Direction8 (8-cardinal enum). */
    public static Direction8 getPlayerFacing() { // was: eQlnaotm4pUDUmJT
        assert (WorldUtils.mc.player != null);
        return Direction8.fromYaw(WorldUtils.mc.player.getYaw());
    }

    // -------------------------------------------------------------------------
    // Inner enum: Direction8 — 8-direction compass for highway directions
    // -------------------------------------------------------------------------
    public static final class Direction8 extends Enum<Direction8> {
        public static final /* enum */ Direction8 SOUTH      = new Direction8(); // ordinal 0
        public static final /* enum */ Direction8 SOUTH_EAST = new Direction8(); // ordinal 1
        public static final /* enum */ Direction8 EAST       = new Direction8(); // ordinal 2
        public static final /* enum */ Direction8 NORTH_EAST = new Direction8(); // ordinal 3
        public static final /* enum */ Direction8 NORTH      = new Direction8(); // ordinal 4
        public static final /* enum */ Direction8 NORTH_WEST = new Direction8(); // ordinal 5
        public static final /* enum */ Direction8 WEST       = new Direction8(); // ordinal 6
        public static final /* enum */ Direction8 SOUTH_WEST = new Direction8(); // ordinal 7

        private static final /* synthetic */ Direction8[] VALUES;

        public static Direction8[] values() {
            return (Direction8[]) VALUES.clone();
        }

        public static Direction8 valueOf(String name) {
            return Enum.valueOf(Direction8.class, name);
        }

        /** Converts a Minecraft yaw angle to the nearest Direction8. */
        public static Direction8 fromYaw(float yaw) { // was: Gt56Sj4a6BWhgB(float)
            float normalized = (yaw % 360.0f + 360.0f) % 360.0f;
            int index = Math.round(normalized / 45.0f) % 8;
            return Direction8.values()[index];
        }

        /** Returns the Direction8 directly opposite this one (180°). */
        public Direction8 opposite() { // was: gkoa4kDDOuwRuB64
            return Direction8.values()[(this.ordinal() + 4) % 8];
        }

        /** Reverses a yaw angle (adds 180°, wraps to 0-360). */
        public static float reverseYaw(float yaw) { // was: TAdu5cndwWu3A1(float)
            float normalized = (yaw % 360.0f + 360.0f) % 360.0f;
            return (normalized + 180.0f) % 360.0f;
        }

        /** Converts a Direction8 to its yaw angle in degrees (0° = ordinal 0, each step = 45°). */
        public static float toYaw(Direction8 direction) { // was: mp3zoXQFKUKYj5(Direction8)
            return (float) direction.ordinal() * 45.0f;
        }

        private static /* synthetic */ Direction8[] buildValues() {
            return new Direction8[]{SOUTH, SOUTH_EAST, EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST, SOUTH_WEST};
        }

        static {
            VALUES = Direction8.buildValues();
        }
    }

    // -------------------------------------------------------------------------
    // Inner record: Vec2d — 2D double vector (X/Z components)
    // -------------------------------------------------------------------------
    public static final class Vec2d extends Record {
        private final double x; // was: qohQQq68PJ
        private final double z; // was: idcUBr

        public Vec2d(double x, double z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString",
                new MethodHandle[]{Vec2d.class, "x;z", "x", "z"}, this);
        }

        @Override
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap("hashCode",
                new MethodHandle[]{Vec2d.class, "x;z", "x", "z"}, this);
        }

        @Override
        public final boolean equals(Object other) {
            return (boolean) ObjectMethods.bootstrap("equals",
                new MethodHandle[]{Vec2d.class, "x;z", "x", "z"}, this, other);
        }

        public double x() { return this.x; } // was: mcAmeo
        public double z() { return this.z; } // was: ckqstPn4Gd
    }
}