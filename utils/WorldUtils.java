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
import net.minecraft.class_1268;   // Hand
import net.minecraft.class_1297;   // Entity
import net.minecraft.class_1511;   // ArmorStandEntity
import net.minecraft.class_1542;   // ItemEntity
import net.minecraft.class_1690;   // Boat/vehicle entity
import net.minecraft.ItemStack;   // Item
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.Items;   // Items
import net.minecraft.class_1937;   // World
import net.minecraft.Blocks;   // Blocks
import net.minecraft.Block;   // Block
import net.minecraft.BlockPos;   // BlockPos
import net.minecraft.Direction;   // Direction
import net.minecraft.class_238;    // Box
import net.minecraft.class_2382;   // Vec3i
import net.minecraft.class_243;    // Vec3d
import net.minecraft.class_2508;   // FluidBlock
import net.minecraft.class_2596;   // Packet
import net.minecraft.class_2846;   // PlayerActionC2SPacket
import net.minecraft.class_2885;   // PlayerInteractBlockC2SPacket
import net.minecraft.MinecraftClient;    // MinecraftClient
import net.minecraft.class_3532;   // MathHelper
import net.minecraft.class_3612;   // Fluid
import net.minecraft.Screen;   // BlockHitResult
import net.minecraft.class_746;    // Entity (for angle calc)
import net.minecraft.class_7923;   // Registries

public class WorldUtils {
    private static final MinecraftClient mc = MinecraftClient.method_1551(); // MinecraftClient.getInstance()
    private static int gatherItemIndex = 0; // was: slvxzlssLu

    /** Returns true if the block directly below the player's feet is the given block type. */
    public static boolean isBlockAtFeet(Block block) { // was: Gt56Sj4a6BWhgB(Block)
        if (WorldUtils.mc.field_1724 == null || WorldUtils.mc.field_1687 == null) { // player, world
            return false;
        }
        BlockPos pos = WorldUtils.mc.player.getBlockPos().method_10074(); // getBlockPos().down()
        return WorldUtils.mc.world.getBlockState(pos).getBlock() == block; // getBlockState().getBlock()
    }

    /** Returns a BlockPos 2 blocks ahead of the player in the given Direction8. */
    public static BlockPos getOffset2AheadPos(Direction8 direction8) { // was: jOdDDFXSeWl4(Direction8)
        int x = WorldUtils.mc.player.getX(); // getBlockX()
        int y = WorldUtils.mc.player.getY(); // getBlockY()
        int z = WorldUtils.mc.player.getZ(); // getBlockZ()
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
        if (WorldUtils.mc.field_1724 == null || WorldUtils.mc.field_1687 == null) {
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
                // Check block below candidate is obsidian and candidate is within radius
                if (WorldUtils.mc.world.getBlockState(
                        candidate.method_33096(HighwayState.getInstance().getHighwayY() - 1)) // withY(y-1)
                        .getBlock() != Blocks.field_10540) continue; // != Blocks.OBSIDIAN
                double dist = playerPos.method_46558().method_1028( // toCenterPos().distanceTo()
                    (double) candidate.getX(),
                    (double) HighwayState.getInstance().getHighwayY().intValue(),
                    (double) candidate.getZ());
                if (dist > 25.0 || !(dist < bestDist)) continue;
                bestDist = dist;
                best = candidate;
            }
        }
        if (best != null) {
            PathingHelper.setGoal(best); // was: l92qSNnpKrYO
            PathingHelper.startPathing(); // was: gaJr0zjHBLiO
            MusheorSystem.debug("Going back onto the highway...", new Object[0]);
        }
    }

    /** Returns true if any horizontally-adjacent face of the given BlockPos is solid. */
    public static boolean hasAdjacentSolid(BlockPos pos) { // was: CEOjBr5G5R
        return Arrays.stream(Direction.values())
            .filter(d -> d != Direction.field_11036) // != Direction.UP
            .anyMatch(d -> WorldUtils.mc.world.getBlockState(pos.method_10093(d)).method_51176()); // offset(d).isSolid()
    }

    /**
     * Detects lava blocks ahead of the player and initiates lava-removal procedure
     * by setting a target block in HighwayState.
     */
    public static void handleLavaRemoval() { // was: selaO6lwe7
        if (WorldUtils.mc.field_1724 == null || WorldUtils.mc.field_1687 == null) {
            return;
        }
        // If we already have a target lava block, check if it's still lava
        if (HighwayState.getInstance().getLavaTargetBlock() != null) { // was: HBAiI3pyGGGxpI2b
            if (WorldUtils.mc.world.method_8316(
                    HighwayState.getInstance().getLavaTargetBlock()).method_15772() // getFluidState().getFluid()
                    != class_3612.field_15908) { // != Fluids.WATER (fluid state empty means no fluid)
                PathingHelper.stopPathing(); // was: xRVyNRV3cB7
                HighwayState.getInstance().setLavaTargetBlock(null); // was: ULOAMKfWE3NZZTj8
            } else {
                PathingHelper.setGoal( // was: l92qSNnpKrYO
                    HighwayState.getInstance().getLavaTargetBlock()
                        .method_33096(HighwayState.getInstance().getHighwayY().intValue())); // withY
            }
            return;
        }
        // If we have a saved position to return to, check if we've reached it
        if (HighwayState.getInstance().getSavedReturnPos() != null) { // was: rUchPoPt
            if (WorldUtils.mc.player.getBlockPos()
                    .equals(HighwayState.getInstance().getSavedReturnPos())) {
                PathingHelper.stopPathing();
                HighwayState.getInstance().setSavedReturnPos(null); // was: gsYdyKVgv
            } else {
                PathingHelper.setGoal(HighwayState.getInstance().getSavedReturnPos());
            }
            return;
        }
        // Scan for lava ahead (cardinal highway)
        if (HighwayBuilder.getHighwayType() == HighwayBuilder.HighwayType.CARDINAL) { // was: b76P5ieurZIX
            for (BlockPos pos : BlockPositions.getCardinalObstructionPositions()) { // was: txFOGrboKBXQp
                if (PathingHelper.isAlreadyPathing()) return; // was: LcPVM4w5KCoKSxGs
                if (WorldUtils.mc.world.method_8316(pos).method_15772() == class_3612.field_15907
                        || WorldUtils.mc.world.method_8316(pos).method_15772() != class_3612.field_15908) continue;
                MusheorSystem.debug("Found obstructing lava ahead, removing...", new Object[0]);
                PlayerUtils.setAutoWalkActive(false); // was: KP44bk
                HighwayState.getInstance().setLavaTargetBlock(pos);
                HighwayState.getInstance().setSavedReturnPos(
                    WorldUtils.mc.player.getBlockPos().method_10062()); // getBlockPos().down()
                return;
            }
        }
        // Scan for lava ahead (diagonal highway)
        if (HighwayBuilder.getHighwayType() == HighwayBuilder.HighwayType.DIAGONAL) { // was: xpLMsAtAuXAx
            for (BlockPos pos : BlockPositions.getDiagonalObstructionPositions()) { // was: HP7CUOuiyLUHkEkD
                if (PathingHelper.isAlreadyPathing()) return;
                if (WorldUtils.mc.world.method_8316(pos).method_15772() == class_3612.field_15907
                        || WorldUtils.mc.world.method_8316(pos).method_15772() != class_3612.field_15908) continue;
                MusheorSystem.debug("Found obstructing lava ahead, removing...", new Object[0]);
                PlayerUtils.setAutoWalkActive(false);
                HighwayState.getInstance().setLavaTargetBlock(pos);
                HighwayState.getInstance().setSavedReturnPos(
                    WorldUtils.mc.player.getBlockPos().method_10062());
                return;
            }
        }
    }

    /** Returns all non-air BlockPos positions occupied by the entity's bounding box foot region. */
    public static List<BlockPos> getEntityFootBlocks(class_1297 entity) { // was: jOdDDFXSeWl4(Entity)
        assert (WorldUtils.mc.field_1687 != null);
        ArrayList<BlockPos> result = new ArrayList<>();
        class_238 box = entity.method_5829().method_35575(entity.getY() - 0.2).method_35578(entity.getY()); // getBoundingBox
        int minX = class_3532.method_15357((double) box.method_61125().method_10216()); // MathHelper.floor
        int maxX = class_3532.method_15357((double) box.method_61126().method_10216());
        int minZ = class_3532.method_15357((double) box.method_61125().method_10215());
        int maxZ = class_3532.method_15357((double) box.method_61126().method_10215());
        int y    = class_3532.method_15357((double)(entity.getY() - 0.2));
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                BlockPos pos = new BlockPos(x, y, z);
                if (WorldUtils.getBlockAt(pos) == Blocks.field_10124) continue; // Blocks.AIR
                result.add(pos);
            }
        }
        return result;
    }

    /** Returns the first Entity found at the given BlockPos, or null. */
    public static class_1297 getEntityAt(class_1937 world, BlockPos pos) { // was: Gt56Sj4a6BWhgB(World,BlockPos)
        class_238 box = new class_238(pos);
        List<?> list = world.method_8390(class_1297.class, box, e -> true); // getEntitiesByType
        return list.isEmpty() ? null : (class_1297) list.getFirst();
    }

    /**
     * Sends a block-interact (place) packet to the server for the given position and face.
     * Returns true on success.
     */
    public static boolean placeBlockPacket(BlockPos pos, Direction side) { // was: jOdDDFXSeWl4(BlockPos,Direction)
        if (WorldUtils.mc.field_1724 == null || mc.method_1562() == null || WorldUtils.mc.field_1761 == null) {
            return false;
        }
        WorldUtils.swapCarriedItems();
        WorldUtils.sendPlacePacket(class_1268.field_5810, WorldUtils.makeHitResult(pos, side)); // Hand.OFF_HAND
        WorldUtils.swapCarriedItems();
        return true;
    }

    /** Creates a BlockHitResult aimed at the center of the given face of the given BlockPos. */
    public static Screen makeHitResult(BlockPos pos, Direction side) { // was: mp3zoXQFKUKYj5(BlockPos,Direction)
        return new Screen(class_243.method_24953((class_2382) pos), side, pos, false); // Vec3d.of(pos)
    }

    /** Sends a PlayerInteractBlockC2SPacket for the given hand and hit result. */
    public static void sendPlacePacket(class_1268 hand, Screen hitResult) { // was: jOdDDFXSeWl4(Hand,BlockHitResult)
        WorldUtils.mc.field_1761.method_41931(WorldUtils.mc.field_1687,
            n -> new class_2885(hand, hitResult, n));
    }

    /**
     * Swaps the main-hand and off-hand items using a PlayerActionC2SPacket,
     * then swaps inventory hotbar slots 0 and 40 (main/offhand mirror).
     */
    public static void swapCarriedItems() { // was: l3ot1CwoJ9CsS
        if (WorldUtils.mc.field_1724 == null || WorldUtils.mc.field_1687 == null) {
            return;
        }
        WorldUtils.mc.field_1761.method_41931(WorldUtils.mc.field_1687,
            n -> new class_2846(class_2846.class_2847.field_12969, // Action.SWAP_ITEM_WITH_OFFHAND
                BlockPos.field_10980, Direction.field_11033)); // BlockPos.ORIGIN, Direction.DOWN
        ItemStack mainHand = WorldUtils.mc.player.method_6047(); // getMainHandStack
        ItemStack offHand  = WorldUtils.mc.player.method_6079(); // getOffHandStack
        WorldUtils.mc.player.getId().method_5447(
            WorldUtils.mc.player.getId().field_7545, offHand);  // getInventory().setStack(selected, offhand)
        WorldUtils.mc.player.getId().method_5447(40, mainHand); // slot 40 = offhand
    }

    /**
     * Equips the given item and places a block at the position with the given face,
     * if Meteor's BlockUtils says placement is allowed.
     */
    public static boolean placeBlockWithItem(ItemStack item, BlockPos pos, Direction side) { // was: jOdDDFXSeWl4(Item,BlockPos,Direction)
        if (!BlockUtils.canPlaceBlock(pos, true, Block.method_9503(item))) { // Block.getBlockFromItem
            return false;
        }
        InventoryManager.equipItem(item); // was: L5CF0C6jx0T17H4I
        return WorldUtils.placeBlockPacket(pos, side);
    }

    /** Points the player's view toward the center of the given BlockPos, choosing the best face. */
    public static void lookAtBlock(BlockPos pos) { // was: MS1x7YGHjIg7eB
        class_243 hitVec = class_243.method_24953((class_2382) pos); // Vec3d.of
        Direction side = BlockUtils.getPlaceSide(pos);
        if (side != null) {
            pos.method_10093(side); // offset(side)
            hitVec = hitVec.method_1019(class_243.method_24954((class_2382) side.method_62675()).method_1021(0.5)); // add(vec * 0.5)
        }
        assert (WorldUtils.mc.field_1724 != null);
        float[] angles = WorldUtils.calcAngles(WorldUtils.mc.field_1724, hitVec);
        WorldUtils.mc.player.method_36456(angles[0]); // setYaw
        WorldUtils.mc.player.method_36457(angles[1]); // setPitch
    }

    /** Points the player's view toward the center of the given face on the given BlockPos. */
    public static void lookAtBlockFace(BlockPos pos, Direction side) { // was: Gt56Sj4a6BWhgB(BlockPos,Direction)
        class_243 hitVec = class_243.method_24953((class_2382) pos)
            .method_1019(class_243.method_24954((class_2382) side.method_62675()).method_1021(0.5));
        float[] angles = WorldUtils.calcAngles(WorldUtils.mc.field_1724, hitVec);
        WorldUtils.mc.player.method_36456(angles[0]);
        WorldUtils.mc.player.method_36457(angles[1]);
    }

    /** Calculates yaw and pitch angles needed for the given entity to look at targetPos. */
    static float[] calcAngles(class_746 entity, class_243 targetPos) { // was: jOdDDFXSeWl4(Entity,Vec3d)
        class_243 eyePos = entity.method_33571(); // getEyePos
        double dx = targetPos.field_1352 - eyePos.field_1352; // x
        double dy = targetPos.field_1351 - eyePos.field_1351; // y
        double dz = targetPos.field_1350 - eyePos.field_1350; // z
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double yaw   = -Math.atan2(dx, dz) / Math.PI * 180.0;
        double pitch = -Math.asin(dy / dist) / Math.PI * 180.0;
        return new float[]{(float) yaw, (float) pitch};
    }

    /** Returns true if every BlockPos in the list has at least one solid neighbor below/adjacent. */
    public static boolean allHaveSupport(List<BlockPos> positions) { // was: UgB10d(List)
        if (positions.isEmpty()) return false;
        assert (WorldUtils.mc.field_1724 != null);
        for (BlockPos pos : positions) {
            boolean hasSupport = false;
            for (int i = -1; i <= 1 && !hasSupport; ++i) {
                for (int j = -1; j <= 1 && !hasSupport; ++j) {
                    BlockPos below = pos.method_10069(i, -1, j); // add(dx, -1, dz)
                    if (WorldUtils.mc.world.getBlockState(below).isAir()) continue; // isAir
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
        assert (WorldUtils.mc.field_1687 != null);
        return !WorldUtils.mc.world.getBlockState(pos).isAir()   // !isAir
            && !WorldUtils.mc.world.getBlockState(pos).method_45474()   // !isReplaceable
            && WorldUtils.mc.world.getBlockState(pos).getBlock() != block;
    }

    /**
     * Spleef detection: if a non-player, non-armor-stand, non-vehicle entity is standing on
     * a block in the array, mine that block to drop the griefer.
     */
    public static void detectAndHandleSpleef(BlockPos[] positions) { // was: jOdDDFXSeWl4(BlockPos[])
        for (BlockPos pos : positions) {
            if (!meteordevelopment.meteorclient.utils.player.PlayerUtils.isWithinReach(pos)
                    || !BlockUtils.canPlace(pos, false)
                    || BlockUtils.canPlace(pos, true)) continue;
            PlayerUtils.setAutoWalkActive(false);
            MusheorSystem.debug("Spleefing = %s", HighwayState.getInstance().isSpleefing()); // was: NZkZx8MJ67Zw
            assert (WorldUtils.mc.field_1687 != null);
            class_1297 entity = WorldUtils.getEntityAt((class_1937) WorldUtils.mc.field_1687, pos);
            if (entity == null) return;
            if (entity == WorldUtils.mc.field_1724
                    || !entity.method_5805() // isAlive
                    || entity instanceof class_1511  // ArmorStandEntity
                    || entity instanceof class_1690) continue; // boat/vehicle
            List<BlockPos> footBlocks = WorldUtils.getEntityFootBlocks(entity);
            for (BlockPos footPos : footBlocks) {
                if (!BlockUtils.canPlace(pos, true)) {
                    boolean inArray = false;
                    for (BlockPos p : positions) {
                        if (!p.equals(footPos)) continue;
                        inArray = true;
                        break;
                    }
                    InventoryManager.equipBestToolForBlock(footPos); // was: J2pm2c07elEb5G
                    BlockUtils.breakBlock(footPos, true);
                    HighwayState.getInstance().setSpleefing(true); // was: xG2PP8jo4RWLS
                    MusheorSystem.debug("Spleefing %s at x: %s y: %s z: %s",
                        entity.method_5477().getString(), // getDisplayName
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
        return WorldUtils.mc.player.method_5649( // squaredDistanceTo
            (double) pos.getX(),
            (double) pos.getY(),
            (double) pos.getZ()) <= range * range;
    }

    /** Returns true if the BlockPos is within the given distance (squared check). */
    public static boolean isWithinDistance(BlockPos pos, double distance) { // was: jOdDDFXSeWl4(BlockPos,double)
        return WorldUtils.mc.player.method_5649(
            (double) pos.getX(),
            (double) pos.getY(),
            (double) pos.getZ()) <= distance * distance;
    }

    /**
     * Removes stale entries from the placement-tracking cache.
     * Entries older than placementTimeout ticks or matching the given block type are removed.
     */
    public static void cleanPlacementCache(int currentTick, Block block) { // was: jOdDDFXSeWl4(int,Block)
        HighwayState.getInstance().getPlacementCache().entrySet().removeIf(entry -> { // was: Os3dd8a
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
        if (WorldUtils.mc.field_1724 == null || WorldUtils.mc.field_1687 == null
                || state.isSpleefing()           // was: NZkZx8MJ67Zw
                || KekNuker.isActive()            // was: Pa3aVwRtUo45jMG
                || HighwayBuilder.isWaiting()) {  // was: zl2vxyh
            return;
        }
        Block targetBlock = isFloor ? HighwayBuilder.getFloorBlock()    // was: e4uKoS
                                         : HighwayBuilder.getPavingBlock();  // was: yaVvWAqooeFn
        double distToSolid = WorldUtils.getDistanceToFirstSolidAhead(HighwayBuilder.getPavingBlock());

        if (HighwayBuilder.getHighwayType() == HighwayBuilder.HighwayType.CARDINAL) {
            if (distToSolid <= 1.0) {
                PlayerUtils.setAutoWalkActive(false);
                state.setWalkingBlocked(true); // was: BX92A0OIIvD9
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
            InventoryManager.equipItem(targetBlock.method_8389()); // asItem
            WorldUtils.swapCarriedItems();
            for (BlockPos pos : positions) {
                if (!BlockUtils.canPlace(pos, true)) continue;
                state.setCurrentlyPlacing(true); // was: L5CF0C6jx0T17H4I(true)
                if (RateController.checkPlaceRate()) { // was: qy8UwM99rVr
                    WorldUtils.sendPlacePacket(class_1268.field_5810, // Hand.OFF_HAND
                        WorldUtils.makeHitResult(pos, Direction.field_11033)); // Direction.DOWN
                    state.getPlacementCache().put(pos, state.getCurrentTick()); // was: yIXEDGFGtS9H
                    MusheorSystem.debug("placed \u00a75%s \u00a7rat %s, %s, %s",
                        class_7923.field_41178.method_10221(targetBlock.method_8389()).toString(), // Registries.ITEM.getId
                        pos.getX(), pos.getY(), pos.getZ());
                    if (state.getRecentlyPlaced().contains(pos)) continue; // was: Mz2EP5
                    state.getRecentlyPlaced().add(pos);
                    if (!HighwayBuilder.getPavingBlock().equals(Blocks.field_10540)) continue; // Blocks.OBSIDIAN
                    state.incrementBlocksPlaced(); // was: s6I5Zvj
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
        InventoryManager.equipItem(block.method_8389());
        WorldUtils.swapCarriedItems();
        for (BlockPos pos : positions) {
            if (!BlockUtils.canPlace(pos, true)
                    || !meteordevelopment.meteorclient.utils.player.PlayerUtils.isWithin(pos, 4.5)) continue;
            if (!RateController.checkPlaceRate()) break;
            WorldUtils.sendPlacePacket(class_1268.field_5810,
                WorldUtils.makeHitResult(pos, Direction.field_11033));
        }
        WorldUtils.swapCarriedItems();
    }

    /** Places ice blocks below the player's feet in the four forward positions for ice-floor highways. */
    public static void buildIceFloor() { // was: QigP9ftge6
        if (WorldUtils.mc.field_1724 == null || WorldUtils.mc.field_1687 == null) return;
        BlockPos playerPos = WorldUtils.mc.player.getBlockPos();
        for (int i = 1; i <= 4; ++i) {
            BlockPos target = null;
            switch (HighwayBuilder.getDirection().ordinal()) { // was: kLIvClyeu
                case 4: target = playerPos.method_10069(0, -1, -i); break;
                case 0: target = playerPos.method_10069(0, -1,  i); break;
                case 6: target = playerPos.method_10069( i, -1, 0); break;
                case 2: target = playerPos.method_10069(-i, -1, 0); break;
                case 5: target = playerPos.method_10069( i, -1, -i); break;
                case 3: target = playerPos.method_10069(-i, -1, -i); break;
                case 7: target = playerPos.method_10069( i, -1,  i); break;
                case 1: target = playerPos.method_10069(-i, -1,  i); break;
            }
            if (target == null) return;
            if (mc.method_1562() == null) return;
            if (WorldUtils.mc.world.getBlockState(target).getBlock() == Blocks.field_10114) { // Blocks.BEDROCK
                BlockUtils.breakBlock(target, false);
            }
            if (!WorldUtils.mc.world.getBlockState(target).isAir()) continue; // isAir
            WorldUtils.placeBlockWithItem(Items.field_8328, target, Direction.field_11033); // Items.ICE, DOWN
        }
    }

    /** Checks for server-side lag using TickRate; pauses AutoWalk if lag is detected. */
    public static boolean checkForLag() { // was: btLCQHvKVR
        float timeSinceTick;
        if (HighwayBuilder.isLagDetectionEnabled() // was: oknfyMh
                && (timeSinceTick = TickRate.INSTANCE.getTimeSinceLastTick())
                    > (float) HighwayBuilder.getLagThreshold()) { // was: J6PuzyzqvmhV
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
        if (!PlayerUtils.isGatheringItem()) { // was: BT1BimvycZZjsYS
            ItemStack item = items.get(gatherItemIndex);
            PlayerUtils.setAutoWalkActive(false);
            PlayerUtils.startGatherItem(item, false); // was: jOdDDFXSeWl4(Item,boolean)
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
        assert (WorldUtils.mc.field_1724 != null && WorldUtils.mc.field_1687 != null);
        BlockPos feetPos = WorldUtils.mc.player.getBlockPos()
            .method_33096(WorldUtils.mc.player.getY() - 1); // withY(y-1)
        BlockPos solidPos = null;
        for (int i = 0; i <= 8; ++i) {
            int nx = WorldUtils.mc.player.getX() + dx * i;
            int nz = WorldUtils.mc.player.getZ() + dz * i;
            BlockPos candidate = new BlockPos(nx, WorldUtils.mc.player.getY() - 1, nz);
            if (WorldUtils.mc.world.getBlockState(candidate).getBlock()
                    .method_9564().method_51367()) continue; // getDefaultState().isOpaque() - skip solid
            solidPos = candidate;
            break;
        }
        if (solidPos == null) return 10.0;
        return WorldUtils.horizontalDistance(solidPos, feetPos);
    }

    /** Returns the Block at the given BlockPos. */
    public static Block getBlockAt(BlockPos pos) { // was: WOqvNwnejoKApoa
        assert (WorldUtils.mc.field_1687 != null && WorldUtils.mc.field_1724 != null);
        return WorldUtils.mc.world.getBlockState(pos).getBlock(); // getBlockState().getBlock()
    }

    /** Sends a START_DESTROY_BLOCK (dig) packet to the server. */
    public static void sendDigPacket(BlockPos pos, Direction side) { // was: TAdu5cndwWu3A1(BlockPos,Direction)
        Objects.requireNonNull(mc.method_1562()).method_52787( // getNetworkHandler().sendPacket
            (class_2596) new class_2846(class_2846.class_2847.field_12973, pos, side)); // Action.START_DESTROY_BLOCK
    }

    /** Returns the 2D horizontal distance (XZ plane) between two BlockPos. */
    public static double horizontalDistance(BlockPos a, BlockPos b) { // was: Gt56Sj4a6BWhgB(BlockPos,BlockPos)
        double dx = Math.abs((double)(a.getX() - b.getX()));
        double dz = Math.abs((double)(a.getZ() - b.getZ()));
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Checks a column of blocks ahead of the player (height 3 for normal, 4 for
     * elytra mode) for non-air, non-fluid obstructions and stops walking if found.
     */
    public static void checkForwardCollisions() { // was: gBxN0D8GSyidOa
        assert (WorldUtils.mc.field_1724 != null && WorldUtils.mc.field_1687 != null);
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
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.NORMAL) height = 3;    // was: e4uKoS
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.ELYTRA)  height = 4;   // was: flZYoiXwrl
        for (int i = 0; i < height; ++i) {
            BlockPos ahead = WorldUtils.mc.player.getBlockPos().method_10069(dx, i, dz);
            if (WorldUtils.mc.world.getBlockState(ahead).isAir()         // isAir
                    || WorldUtils.mc.world.getBlockState(ahead).getBlock() instanceof class_2508) continue; // FluidBlock
            if (WorldUtils.mc.world.method_8316(ahead).method_15772() != class_3612.field_15908
                    || WorldUtils.mc.world.method_8316(ahead).method_15772() != class_3612.field_15907
                    || WorldUtils.mc.world.getBlockState(ahead).getBlock() != Blocks.field_10036) {
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
        assert (WorldUtils.mc.field_1724 != null && WorldUtils.mc.field_1687 != null);
        int dx = 0, dz = 0;
        Direction8 dir = HighwayBuilder.getDirection();
        switch (dir.ordinal()) {
            case 5: dx =  1; dz = -1; break;
            case 1: dx = -1; dz =  1; break;
            case 3: dx = dz = -1;     break;
            case 7: dx = dz =  1;     break;
        }
        BlockPos below = WorldUtils.mc.player.getBlockPos().method_10069(dx, -1, dz);
        if (WorldUtils.mc.world.getBlockState(below).isAir()) { // isAir
            InventoryManager.equipItem(Items.field_8328); // Items.ICE (or obsidian depending on mode)
            WorldUtils.placeBlockPacket(below, Direction.field_11033); // Direction.DOWN
            return true;
        }
        return false;
    }

    /** Returns true if there is an ItemEntity with the given item type within 10 blocks. */
    public static boolean isItemNearby(ItemStack item) { // was: Y9BgxR
        if (WorldUtils.mc.field_1687 == null || WorldUtils.mc.field_1724 == null) return false;
        int radius = 10;
        class_238 box = new class_238(
            WorldUtils.mc.player.getX() - radius,
            WorldUtils.mc.player.getY() - radius,
            WorldUtils.mc.player.getZ() - radius,
            WorldUtils.mc.player.getX() + radius, 122,
            WorldUtils.mc.player.getZ() + radius);
        List<?> list = WorldUtils.mc.world.method_8390(class_1542.class, box,
            entity -> entity.method_6983().getStack() == item); // getStack().getItem()
        return !list.isEmpty();
    }

    /** Finds the nearest ItemEntity with the given item type and tells Baritone to path to it. */
    public static void pathToNearestItem(ItemStack item) { // was: xG2PP8jo4RWLS(Item)
        if (WorldUtils.mc.field_1724 == null || WorldUtils.mc.field_1687 == null) return;
        class_1542 nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (class_1297 entity : WorldUtils.mc.world.method_18112()) { // getEntities
            double dist;
            if (!(entity instanceof class_1542 itemEntity)
                    || itemEntity.method_6983().getStack() != item
                    || !((dist = entity.method_5858((class_1297) WorldUtils.mc.field_1724)) < nearestDist)) continue;
            nearestDist = dist;
            nearest = itemEntity;
        }
        if (nearest != null) {
            PathingHelper.pathToPos(nearest.getBlockPos()); // was: S7TLszvzENsW7
        } else {
            ChatUtils.error("No matching item found nearby.", new Object[0]);
        }
    }

    /** Converts yaw/pitch angles to a Minecraft Direction (the direction the player is facing). */
    public static Direction getFacingFromAngles(float yaw, float pitch) { // was: mp3zoXQFKUKYj5(float,float)
        if (pitch > 60.0f)  return Direction.field_11033; // DOWN
        if (pitch < -60.0f) return Direction.field_11036; // UP
        if ((yaw %= 360.0f) < 0.0f) yaw += 360.0f;
        if (yaw >= 315.0f || yaw < 45.0f)   return Direction.field_11035; // NORTH
        if (yaw >= 45.0f  && yaw < 135.0f)  return Direction.field_11039; // EAST
        if (yaw >= 135.0f && yaw < 225.0f)  return Direction.field_11043; // SOUTH
        return Direction.field_11034;                                       // WEST
    }

    /** Returns the player's current facing direction as a Direction8 (8-cardinal enum). */
    public static Direction8 getPlayerFacing() { // was: eQlnaotm4pUDUmJT
        assert (WorldUtils.mc.field_1724 != null);
        return Direction8.fromYaw(WorldUtils.mc.player.method_36454()); // getYaw
    }

    // -------------------------------------------------------------------------
    // Inner enum: Direction8 — 8-direction compass for highway directions
    // -------------------------------------------------------------------------
    public static final class Direction8 extends Enum<Direction8> {
        // Ordinal order matches: SOUTH(4), NORTH_EAST(1→? depends on ordinal), EAST(2?)...
        // Mapped by coordinate math in BlockPositions/WorldUtils:
        public static final /* enum */ Direction8 SOUTH      = new Direction8(); // was: Q5FUNqd0ALfl,  ordinal 0
        public static final /* enum */ Direction8 SOUTH_EAST = new Direction8(); // was: CsEhJrV,       ordinal 1
        public static final /* enum */ Direction8 EAST       = new Direction8(); // was: v5UhyO9eEd7n,  ordinal 2 (actually mapped WEST by coord!)
        public static final /* enum */ Direction8 NORTH_EAST = new Direction8(); // was: aiRs4cu,       ordinal 3
        public static final /* enum */ Direction8 NORTH      = new Direction8(); // was: vSouwXdh7,     ordinal 4
        public static final /* enum */ Direction8 NORTH_WEST = new Direction8(); // was: ZOY41p,        ordinal 5
        public static final /* enum */ Direction8 WEST       = new Direction8(); // was: S8iuqKQCrJM02b, ordinal 6 (mapped EAST by coord!)
        public static final /* enum */ Direction8 SOUTH_WEST = new Direction8(); // was: E8moug3IELf8,  ordinal 7

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
