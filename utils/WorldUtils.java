// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.HFbqnT1FEh2q; enum was HFbqnT1FEh2q$FDb5, record was HFbqnT1FEh2q$aY0a71o)
package musheor.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.utils.player.PlayerUtils; // Meteor's PlayerUtils (isWithin/isWithinReach)
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import musheor.compat.VersionHelper;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.InventoryManager;
import musheor.modules.automation.KekNuker;
import musheor.utils.internal.HighwayLocator;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The geometry/block-manipulation backbone for the highway system: direction math
 * (see {@link Direction8}), block placement/breaking, lava clearing, entity
 * "spleefing", collision checks, item location, and distance helpers.
 *
 * MANY methods here were obfuscated to the same overloaded name ({@code FvaNWO});
 * real names below are assigned from behaviour and call-site usage.
 */
public class WorldUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: Q90GLXQ0Pef (field)

    /** True if the block directly under the player equals {@code block}. */
    public static boolean isStandingOn(Block block) { // was: FvaNWO(Block)
        if (mc.player == null || mc.world == null) return false;
        BlockPos posBelow = mc.player.getBlockPos().down();
        return mc.world.getBlockState(posBelow).getBlock() == block;
    }

    /** Player block position offset two blocks along {@code dir}. */
    public static BlockPos offsetTwoBlocks(Direction8 dir) { // was: FvaNWO(Direction8)
        int x = mc.player.getBlockX(), y = mc.player.getBlockY(), z = mc.player.getBlockZ();
        int dx = 0, dz = 0;
        switch (dir) {
            case SOUTH -> dz = -2;
            case SOUTH_WEST -> { dx = 2; dz = -2; }
            case WEST -> dx = 2;
            case NORTH_WEST -> { dx = 2; dz = 2; }
            case NORTH -> dz = 2;
            case NORTH_EAST -> { dx = -2; dz = 2; }
            case EAST -> dx = -2;
            case SOUTH_EAST -> { dx = -2; dz = -2; }
        }
        return new BlockPos(x + dx, y, z + dz);
    }

    /** Dead helper — always returns false. */
    public static boolean unusedFalse() { return false; } // was: FvaNWO()

    /** Finds the nearest obsidian at the build height within 25 blocks and paths back to it. */
    public static void returnToHighway() { // was: Q90GLXQ0Pef()
        if (mc.player == null || mc.world == null) return;
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        int range = 25;
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                Integer buildY = HighwayState.getInstance().getCenterY();
                BlockPos pos = new BlockPos(playerPos.getX() + x, buildY, playerPos.getZ() + z);
                if (mc.world.getBlockState(pos.withY(buildY - 1)).getBlock() == Blocks.OBSIDIAN) {
                    double distSq = playerPos.toCenterPos().squaredDistanceTo(pos.getX(), buildY, pos.getZ());
                    if (distSq <= 25.0 && distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearest = pos;
                    }
                }
            }
        }
        if (nearest != null) {
            PathingHelper.gotoBlock(nearest);
            MusheorSystem.debug("Going back onto the highway...");
        }
    }

    /** True if any non-DOWN neighbour of {@code pos} is a liquid. */
    public static boolean hasAdjacentLiquid(BlockPos pos) { // was: FvaNWO(BlockPos)
        return Arrays.stream(Direction.values())
            .filter(dir -> dir != Direction.DOWN)
            .anyMatch(dir -> mc.world.getBlockState(pos.offset(dir)).isLiquid());
    }

    /** Clears obstructing lava ahead of the player along the highway. */
    public static void handleLavaRemoval() { // was: psJq59YIbp3Z()
        if (mc.player == null || mc.world == null) return;
        HighwayState state = HighwayState.getInstance();
        if (state.getPendingBreakPos() != null) {
            if (mc.world.getFluidState(state.getPendingBreakPos()).getFluid() != Fluids.LAVA) {
                PathingHelper.cancelEverything();
                state.setPendingBreakPos(null);
            } else {
                PathingHelper.gotoBlock(state.getPendingBreakPos().withY(state.getCenterY()));
            }
        } else if (state.getReturnGoalPos() != null) {
            if (mc.player.getBlockPos().equals(state.getReturnGoalPos())) {
                PathingHelper.cancelEverything();
                state.setReturnGoalPos(null);
            } else {
                PathingHelper.gotoBlock(state.getReturnGoalPos());
            }
        } else {
            if (HighwayBuilder.getHighwayType() == HighwayBuilder.HighwayType.CARDINAL) {
                for (BlockPos pos : BlockPositions.cardinalFrontRow()) {
                    if (PathingHelper.isPathing()) return;
                    if (mc.world.getFluidState(pos).getFluid() != Fluids.EMPTY
                        && mc.world.getFluidState(pos).getFluid() == Fluids.LAVA) {
                        MusheorSystem.debug("Found obstructing lava ahead, removing...");
                        PlayerUtilsHelper.setAutoWalk(false);
                        state.setPendingBreakPos(pos);
                        state.setReturnGoalPos(mc.player.getBlockPos().toImmutable());
                        return;
                    }
                }
            }
            if (HighwayBuilder.getHighwayType() == HighwayBuilder.HighwayType.DIAGONAL) {
                for (BlockPos pos : BlockPositions.diagonalFrontRow()) {
                    if (PathingHelper.isPathing()) return;
                    if (mc.world.getFluidState(pos).getFluid() != Fluids.EMPTY
                        && mc.world.getFluidState(pos).getFluid() == Fluids.LAVA) {
                        MusheorSystem.debug("Found obstructing lava ahead, removing...");
                        PlayerUtilsHelper.setAutoWalk(false);
                        state.setPendingBreakPos(pos);
                        state.setReturnGoalPos(mc.player.getBlockPos().toImmutable());
                        return;
                    }
                }
            }
        }
    }

    /** Returns the non-air block positions an entity is standing on (its footprint). */
    public static List<BlockPos> getBlocksUnderEntity(Entity entity) { // was: FvaNWO(Entity)
        assert mc.world != null;
        Box footing = entity.getBoundingBox().withMinY(entity.getY() - 0.2).withMaxY(entity.getY());
        int minBlockX = MathHelper.floor(footing.getMinPos().getX());
        int maxBlockX = MathHelper.floor(footing.getMaxPos().getX());
        int minBlockZ = MathHelper.floor(footing.getMinPos().getZ());
        int maxBlockZ = MathHelper.floor(footing.getMaxPos().getZ());
        int blockY = MathHelper.floor(entity.getY() - 0.2);
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int x = minBlockX; x <= maxBlockX; x++) {
            for (int z = minBlockZ; z <= maxBlockZ; z++) {
                BlockPos maybePos = new BlockPos(x, blockY, z);
                if (getBlockAt(maybePos) != Blocks.AIR) blocks.add(maybePos);
            }
        }
        return blocks;
    }

    /** Returns the first entity whose bounding box overlaps {@code pos}, or null. */
    public static Entity getEntityAt(World world, BlockPos pos) { // was: FvaNWO(World,BlockPos)
        Box box = new Box(pos);
        List<Entity> entities = world.getEntitiesByClass(Entity.class, box, e -> true);
        return entities.isEmpty() ? null : entities.getFirst();
    }

    /** Places a block against {@code direction} using the off-hand swap trick. */
    public static boolean placeBlockOffhand(BlockPos pos, Direction direction) { // was: FvaNWO(BlockPos,Direction)
        if (mc.player != null && mc.getNetworkHandler() != null && mc.interactionManager != null) {
            swapHands();
            sendInteract(Hand.OFF_HAND, buildHitResult(pos, direction));
            swapHands();
            return true;
        }
        return false;
    }

    public static BlockHitResult buildHitResult(BlockPos pos, Direction direction) { // was: Q90GLXQ0Pef(BlockPos,Direction)
        return new BlockHitResult(Vec3d.ofCenter(pos), direction, pos, false);
    }

    public static void sendInteract(Hand hand, BlockHitResult hit) { // was: FvaNWO(Hand,BlockHitResult)
        mc.interactionManager.interactBlock(mc.world, hand, hit); // sequenced
    }

    /** Swaps main/off-hand around a no-op action so placement uses the desired stack. */
    public static void swapHands() { // was: SOYyh5IPg26f7F()
        if (mc.player != null && mc.world != null) {
            mc.interactionManager.interactBlock(mc.world, Hand.OFF_HAND, null); // sequence: SWAP_ITEM_WITH_OFFHAND no-op
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            ItemStack mainHandStack = mc.player.getMainHandStack();
            ItemStack offHandStack = mc.player.getOffHandStack();
            mc.player.getInventory().setStack(mc.player.getInventory().selectedSlot, offHandStack);
            mc.player.getInventory().setStack(40, mainHandStack);
        }
    }

    /** Selects the block form of {@code item} then places it against {@code direction}. */
    public static boolean placeItemAt(Item item, BlockPos pos, Direction direction) { // was: FvaNWO(Item,BlockPos,Direction)
        if (!BlockUtils.canPlaceBlock(pos, true, Block.getBlockFromItem(item))) return false;
        InventoryManager.selectItem(item);
        return placeBlockOffhand(pos, direction);
    }

    /** Rotates the player to look at the placement side of {@code blockPos}. */
    public static void lookAtBlock(BlockPos blockPos) { // was: Q90GLXQ0Pef(BlockPos)
        Vec3d hitPos = Vec3d.ofCenter(blockPos);
        Direction side = BlockUtils.getPlaceSide(blockPos);
        if (side != null) {
            hitPos = hitPos.add(Vec3d.of(side.getVector()).multiply(0.5));
        }
        assert mc.player != null;
        float[] rotation = getRotationsTo(mc.player, hitPos);
        mc.player.setYaw(rotation[0]);
        mc.player.setPitch(rotation[1]);
    }

    /** Rotates the player to look at a specific side face centre of {@code pos}. */
    public static void lookAtBlockSide(BlockPos pos, Direction side) { // was: psJq59YIbp3Z(BlockPos,Direction)
        Vec3d center = Vec3d.ofCenter(pos);
        float[] rotation = getRotationsTo(mc.player, center.add(Vec3d.of(side.getVector()).multiply(0.5)));
        mc.player.setYaw(rotation[0]);
        mc.player.setPitch(rotation[1]);
    }

    /** Computes [yaw, pitch] from the player's eyes to {@code vec}. */
    static float[] getRotationsTo(ClientPlayerEntity player, Vec3d vec) { // was: FvaNWO(ClientPlayerEntity,Vec3d)
        Vec3d eyesPos = player.getEyePos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double r = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        double yaw = -Math.atan2(diffX, diffZ) / Math.PI * 180.0;
        double pitch = -Math.asin(diffY / r) / Math.PI * 180.0;
        return new float[]{(float) yaw, (float) pitch};
    }

    /** True if EVERY position in {@code locations} has a non-air block within its 3x3 below. */
    public static boolean allHaveGroundBelow(List<BlockPos> locations) { // was: FvaNWO(List<BlockPos>)
        if (locations.isEmpty()) return false;
        assert mc.player != null;
        for (BlockPos pos : locations) {
            boolean hasGroundBelow = false;
            for (int x = -1; x <= 1 && !hasGroundBelow; x++) {
                for (int z = -1; z <= 1 && !hasGroundBelow; z++) {
                    BlockPos checkPos = pos.add(x, -1, z);
                    if (!mc.world.getBlockState(checkPos).isAir()) hasGroundBelow = true;
                }
            }
            if (!hasGroundBelow) return false;
        }
        return true;
    }

    /** True if {@code pos} holds a breakable, non-replaceable block that isn't {@code block} (TNT allowed). */
    public static boolean shouldBreak(BlockPos pos, Block block) { // was: FvaNWO(BlockPos,Block)
        assert mc.world != null;
        return !mc.world.getBlockState(pos).isAir()
            && (!mc.world.getBlockState(pos).isReplaceable() || mc.world.getBlockState(pos).getBlock() instanceof TntBlock)
            && mc.world.getBlockState(pos).getBlock() != block;
    }

    /** "Spleefs" mobs standing on the highway by breaking the blocks under them. */
    public static void spleefEntities(BlockPos[] positions) { // was: FvaNWO(BlockPos[])
        for (BlockPos currentPos : positions) {
            if (PlayerUtils.isWithinReach(currentPos) && BlockUtils.canPlace(currentPos, false) && !BlockUtils.canPlace(currentPos, true)) {
                assert mc.world != null;
                Entity entity = getEntityAt(mc.world, currentPos);
                if (entity == null) return;
                if (entity != mc.player && entity.isAlive() && !(entity instanceof AbstractMinecartEntity) && !(entity instanceof BoatEntity)) {
                    PlayerUtilsHelper.setAutoWalk(false);
                    for (BlockPos block : getBlocksUnderEntity(entity)) {
                        if (!BlockUtils.canPlace(currentPos, true)) {
                            KekNuker.extraBreakQueue.add(block);
                            HighwayState.getInstance().setFlag6(true);
                            MusheorSystem.debug("Spleefing %s at x: %s y: %s z: %s",
                                entity.getName().getString(),
                                entity.getBlockPos().getX(), entity.getBlockPos().getY(), entity.getBlockPos().getZ());
                            return;
                        }
                        HighwayState.getInstance().setFlag6(false);
                    }
                    return;
                }
            }
        }
    }

    /** True if {@code pos} is within the configured placement range of the player. */
    public static boolean isWithinPlacementRange(BlockPos pos) { // was: psJq59YIbp3Z(BlockPos)
        double range = (Double) MusheorSystem.Manager.placementRange.get();
        return mc.player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= range * range;
    }

    /** True if {@code pos} is within {@code range} of the player. */
    public static boolean isWithinRange(BlockPos pos, double range) { // was: FvaNWO(BlockPos,double)
        return mc.player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= range * range;
    }

    /** Prunes placement-timeout records older than the configured timeout. */
    public static void pruneTimedOutPlacements(int currentTick, Block block) { // was: FvaNWO(int,Block)
        HighwayState.getInstance().getBlockBreakAttempts().entrySet()
            .removeIf(entry -> currentTick - entry.getValue() > (Integer) MusheorSystem.Manager.placementTimeout.get());
    }

    /** Places the highway floor blocks (or scaffold) for the current row, respecting rate limits. */
    public static void placeHighwayBlocks(BlockPos[] positions, boolean scaffold) { // was: FvaNWO(BlockPos[],boolean)
        HighwayState state = HighwayState.getInstance();
        if (mc.player == null || mc.world == null || state.isFlag5() || HighwayBuilder.isKillAuraAttacking()) return;

        Block block = scaffold ? HighwayBuilder.getScaffoldBlock() : HighwayBuilder.getFillBlock();
        boolean diagonal = switch (HighwayBuilder.getDirection()) {
            case SOUTH, WEST, NORTH, EAST -> false;
            default -> true;
        };
        double distance = distanceToEdge(HighwayBuilder.getFillBlock());
        MusheorSystem.debug("Distance %s", Math.round(distance * 100.0) / 100.0);
        if (diagonal) {
            if (!(distance <= 2.35) && !needsPlacement()) { state.setFlag3(false); PlayerUtilsHelper.setAutoWalk(true); }
            else { PlayerUtilsHelper.setAutoWalk(false); state.setFlag3(true); }
        } else if (!(distance <= 1.75) && !needsPlacement()) { state.setFlag3(false); PlayerUtilsHelper.setAutoWalk(true); }
        else { PlayerUtilsHelper.setAutoWalk(false); state.setFlag3(true); }

        boolean place = false;
        for (BlockPos pos : positions) {
            if (mc.world.getBlockState(pos).getBlock() != block && BlockUtils.canPlace(pos, true)
                && isWithinPlacementRange(pos) && !state.getBlockBreakAttempts().containsKey(pos)) {
                place = true;
                break;
            }
        }
        if (place) {
            InventoryManager.selectItem(block.asItem());
            swapHands();
            for (BlockPos currentPos : positions) {
                if (!state.getBlockBreakAttempts().containsKey(currentPos) && BlockUtils.canPlace(currentPos, true)) {
                    state.setFlag4(true);
                    if (!RateController.canSendActionPacket()) { state.setFlag4(false); break; }
                    sendInteract(Hand.OFF_HAND, buildHitResult(currentPos, Direction.DOWN));
                    state.getBlockBreakAttempts().put(currentPos, state.getTicksActive());
                    MusheorSystem.debug("placed §5%s §rat %s, %s, %s",
                        Registries.ITEM.getId(block.asItem()).toString(),
                        currentPos.getX(), currentPos.getY(), currentPos.getZ());
                    if (!state.getBlocksToBuild().contains(currentPos)) {
                        state.getBlocksToBuild().add(currentPos);
                        if (HighwayBuilder.getFillBlock().equals(Blocks.OBSIDIAN)) {
                            state.incrementSessionObsidianPlaced();
                        }
                    }
                }
            }
            swapHands();
        }
    }

    /** True if any block in the current floor/ceiling zone still needs to be placed. */
    public static boolean needsPlacement() { // was: rKbT3Ifwo()
        Direction8 dir = HighwayBuilder.getDirection();
        if (dir == null) return false;
        boolean leftRail = HighwayBuilder.placeLeftRail();
        boolean rightRail = HighwayBuilder.placeRightRail();
        boolean cardinal = dir == Direction8.NORTH || dir == Direction8.SOUTH || dir == Direction8.EAST || dir == Direction8.WEST;
        BlockPos[] zone = cardinal ? BlockPositions.cardinalFloor(0, 1, leftRail, rightRail)
                                   : BlockPositions.diagonalFloor(0, 1, leftRail, rightRail);
        if (HighwayBuilder.hasCeiling()) {
            BlockPos[] ceiling = cardinal ? BlockPositions.cardinalCeiling(0, 1, leftRail, rightRail)
                                          : BlockPositions.diagonalCeiling(0, 1, leftRail, rightRail);
            BlockPos[] combined = new BlockPos[zone.length + ceiling.length];
            System.arraycopy(zone, 0, combined, 0, zone.length);
            System.arraycopy(ceiling, 0, combined, zone.length, ceiling.length);
            zone = combined;
        }
        HighwayState state = HighwayState.getInstance();
        HighwayLocator.Checkpoint detected = state.getCurrentCheckpoint();
        int railY = state.getCenterY() != null ? state.getCenterY() : Integer.MIN_VALUE;
        for (BlockPos pos : zone) {
            if ((detected == null || pos.getY() != railY || !HighwayLocator.isRailOnAnyHighway(pos, detected))
                && BlockUtils.canPlace(pos, true)) {
                return true;
            }
        }
        return false;
    }

    /** Places {@code block} at each reachable position in {@code list}. */
    public static void placeBlocks(List<BlockPos> list, Block block) { // was: FvaNWO(List<BlockPos>,Block)
        InventoryManager.selectItem(block.asItem());
        swapHands();
        for (BlockPos currentPos : list) {
            if (BlockUtils.canPlace(currentPos, true) && PlayerUtils.isWithin(currentPos, 4.5)) {
                if (!RateController.canSendActionPacket()) break;
                sendInteract(Hand.OFF_HAND, buildHitResult(currentPos, Direction.DOWN));
            }
        }
        swapHands();
    }

    /** Fills the 1..4 blocks under/ahead of the player with obsidian (walkway support). */
    public static void fillWalkwayBelow() { // was: r7hOYIKN2()
        if (mc.player == null || mc.world == null) return;
        BlockPos playerPos = mc.player.getBlockPos();
        for (int offset = 1; offset <= 4; offset++) {
            BlockPos targetPos = switch (HighwayBuilder.getDirection()) {
                case SOUTH -> playerPos.add(0, -1, offset);
                case SOUTH_WEST -> playerPos.add(-offset, -1, offset);
                case WEST -> playerPos.add(-offset, -1, 0);
                case NORTH_WEST -> playerPos.add(-offset, -1, -offset);
                case NORTH -> playerPos.add(0, -1, -offset);
                case NORTH_EAST -> playerPos.add(offset, -1, -offset);
                case EAST -> playerPos.add(offset, -1, 0);
                case SOUTH_EAST -> playerPos.add(offset, -1, offset);
            };
            if (targetPos == null || mc.getNetworkHandler() == null) return;
            if (mc.world.getBlockState(targetPos).getBlock() == Blocks.COBWEB) {
                BlockUtils.breakBlock(targetPos, false);
            }
            if (mc.world.getBlockState(targetPos).isAir()) {
                placeItemAt(Items.NETHERRACK, targetPos, Direction.DOWN); // was: class_1802.field_8328 (NETHERRACK; earlier pass mislabeled as OBSIDIAN)
            }
        }
    }

    /** Distance from the player to the nearest non-{@code block} edge ahead (10 if none within 5). */
    public static double distanceToEdge(Block block) { // was: Q90GLXQ0Pef(Block)
        int offsetX = 0, offsetZ = 0;
        switch (HighwayBuilder.getDirection()) {
            case SOUTH -> offsetZ = 1;
            case SOUTH_WEST -> { offsetX = -1; offsetZ = 1; }
            case WEST -> offsetX = -1;
            case NORTH_WEST -> { offsetX = -1; offsetZ = -1; }
            case NORTH -> offsetZ = -1;
            case NORTH_EAST -> { offsetX = 1; offsetZ = -1; }
            case EAST -> offsetX = 1;
            case SOUTH_EAST -> { offsetX = 1; offsetZ = 1; }
        }
        assert mc.player != null && mc.world != null;
        Vec3d playerPos = VersionHelper.get().getPlayerPos();
        Vec3d blockPos = null;
        for (int i = 0; i <= 5; i++) {
            double pX = VersionHelper.get().getPlayerPos().getX() + offsetX * i;
            double pZ = VersionHelper.get().getPlayerPos().getZ() + offsetZ * i;
            BlockPos centerPos = BlockPos.ofFloored(new Vec3d(pX, playerPos.getY() - 1.0, pZ));
            if (block == Blocks.OBSIDIAN) {
                if (mc.world.getBlockState(centerPos).getBlock() != Blocks.OBSIDIAN
                    && mc.world.getBlockState(centerPos).getBlock() != Blocks.RESPAWN_ANCHOR) {
                    blockPos = Vec3d.ofCenter(centerPos);
                    break;
                }
            } else if (!mc.world.getBlockState(centerPos).getBlock().getDefaultState().isSolidBlock(mc.world, centerPos)) {
                blockPos = Vec3d.ofCenter(centerPos);
                break;
            }
        }
        return blockPos == null ? 10.0 : horizontalDistance(blockPos, playerPos);
    }

    public static Block getBlockAt(BlockPos pos) { // was: SOYyh5IPg26f7F(BlockPos)
        assert mc.world != null && mc.player != null;
        return mc.world.getBlockState(pos).getBlock();
    }

    /** Sends an ABORT_DESTROY_BLOCK action for {@code pos}. */
    public static void sendAbortDestroy(BlockPos pos, Direction face) { // was: SOYyh5IPg26f7F(BlockPos,Direction)
        Objects.requireNonNull(mc.getNetworkHandler())
            .sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, face));
    }

    /** Horizontal (XZ) distance between two block positions. */
    public static double horizontalDistance(BlockPos bP, BlockPos pP) { // was: FvaNWO(BlockPos,BlockPos)
        double dx = Math.abs(bP.getX() - pP.getX());
        double dz = Math.abs(bP.getZ() - pP.getZ());
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Horizontal (XZ) distance between two vectors. */
    public static double horizontalDistance(Vec3d pos1, Vec3d pos2) { // was: FvaNWO(Vec3d,Vec3d)
        double dx = Math.abs(pos1.getX() - pos2.getX());
        double dz = Math.abs(pos1.getZ() - pos2.getZ());
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Detects a collision directly in front of the player (per build height) and stops walking. */
    public static void checkFrontCollision() { // was: oZHMlTL()
        assert mc.player != null && mc.world != null;
        int offset = 1, offsetX = 0, offsetZ = 0;
        boolean isDiagonal = false;
        switch (HighwayBuilder.getDirection()) {
            case SOUTH -> offsetZ = offset;
            case SOUTH_WEST -> { offsetX = -offset; offsetZ = offset; isDiagonal = true; }
            case WEST -> offsetX = -offset;
            case NORTH_WEST -> { offsetX = -offset; offsetZ = -offset; isDiagonal = true; }
            case NORTH -> offsetZ = -offset;
            case NORTH_EAST -> { offsetX = offset; offsetZ = -offset; isDiagonal = true; }
            case EAST -> offsetX = offset;
            case SOUTH_EAST -> { offsetX = offset; offsetZ = offset; isDiagonal = true; }
        }
        int[][] checks = isDiagonal ? new int[][]{{offsetX, offsetZ}, {offsetX, 0}, {0, offsetZ}} : new int[][]{{offsetX, offsetZ}};
        int height = 0;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.PAVE) height = 3;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.DIG) height = 4;
        for (int[] check : checks) {
            for (int i = 0; i < height; i++) {
                BlockPos checkPos = mc.player.getBlockPos().add(check[0], i, check[1]);
                if (!mc.world.getBlockState(checkPos).getCollisionShape(mc.world, checkPos).isEmpty()) {
                    PlayerUtilsHelper.setAutoWalk(false);
                    MusheorSystem.debug("Collision detected in front of the player");
                }
            }
        }
    }

    /** Places an obsidian support block under the diagonal offset if the spot is air. */
    public static boolean placeDiagonalSupport() { // was: xQr5FhbwpQPWgIQ()
        assert mc.player != null && mc.world != null;
        int offsetX = 0, offsetZ = 0;
        switch (HighwayBuilder.getDirection()) {
            case SOUTH_WEST -> { offsetX = -1; offsetZ = 1; }
            case NORTH_WEST -> { offsetX = -1; offsetZ = -1; }
            case NORTH_EAST -> { offsetX = 1; offsetZ = -1; }
            case SOUTH_EAST -> { offsetX = 1; offsetZ = 1; }
            default -> { }
        }
        if (mc.world.getBlockState(mc.player.getBlockPos().add(offsetX, -1, offsetZ)).isAir()) {
            BlockPos position = mc.player.getBlockPos().add(offsetX, -1, offsetZ);
            InventoryManager.selectItem(Items.NETHERRACK); // was: class_1802.field_8328 (NETHERRACK)
            placeBlockOffhand(position, Direction.DOWN);
            return true;
        }
        return false;
    }

    /** True if a dropped item entity of {@code targetItem} exists within 10 blocks (up to y=122). */
    public static boolean isItemNearby(Item targetItem) { // was: FvaNWO(Item)
        if (mc.world == null || mc.player == null) return false;
        int SEARCH_RADIUS = 10, MAX_Y = 122;
        Box searchArea = new Box(
            mc.player.getX() - SEARCH_RADIUS, mc.player.getY() - SEARCH_RADIUS, mc.player.getZ() - SEARCH_RADIUS,
            mc.player.getX() + SEARCH_RADIUS, MAX_Y, mc.player.getZ() + SEARCH_RADIUS);
        List<ItemEntity> items = mc.world.getEntitiesByClass(ItemEntity.class, searchArea,
            itemEntity -> itemEntity.getStack().getItem() == targetItem);
        return !items.isEmpty();
    }

    /** Paths (Baritone "goto") to the nearest dropped {@code item}, or errors if none found. */
    public static void findAndPickupItem(Item item) { // was: Q90GLXQ0Pef(Item)
        if (mc.player == null || mc.world == null) return;
        ItemEntity nearestItemEntity = null;
        double closestDistanceSq = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ItemEntity itemEntity && itemEntity.getStack().getItem() == item) {
                double distSq = entity.squaredDistanceTo(mc.player);
                if (distSq < closestDistanceSq) {
                    closestDistanceSq = distSq;
                    nearestItemEntity = itemEntity;
                }
            }
        }
        if (nearestItemEntity != null) {
            PathingHelper.gotoCommand(nearestItemEntity.getBlockPos());
        } else {
            ChatUtils.error("No matching item found nearby.", new Object[0]);
        }
    }

    /** Cardinal facing from yaw/pitch (DOWN if steeply down, UP if steeply up). */
    public static Direction getFacing(float yaw, float pitch) { // was: FvaNWO(float,float)
        if (pitch > 60.0F) return Direction.DOWN;
        if (pitch < -60.0F) return Direction.UP;
        yaw %= 360.0F;
        if (yaw < 0.0F) yaw += 360.0F;
        if (yaw >= 315.0F || yaw < 45.0F) return Direction.SOUTH;
        if (yaw >= 45.0F && yaw < 135.0F) return Direction.WEST;
        return yaw >= 135.0F && yaw < 225.0F ? Direction.NORTH : Direction.EAST;
    }

    /** Current 8-way movement direction from the player's yaw. */
    public static Direction8 getMovementDirection() { // was: OMMZL1F3q()
        assert mc.player != null;
        return Direction8.fromYaw(mc.player.getYaw());
    }

    /**
     * 8-way compass direction. Ordinal maps to yaw as {@code ordinal * 45°}
     * (Minecraft yaw: 0°=South, 90°=West, 180°=North, 270°=East).
     */ // was: enum HFbqnT1FEh2q$FDb5
    public enum Direction8 {
        SOUTH,       // was: FvaNWO       (0°)
        SOUTH_WEST,  // was: Q90GLXQ0Pef  (45°)
        WEST,        // was: psJq59YIbp3Z (90°)
        NORTH_WEST,  // was: SOYyh5IPg26f7F(135°)
        NORTH,       // was: rKbT3Ifwo    (180°)
        NORTH_EAST,  // was: r7hOYIKN2    (225°)
        EAST,        // was: oZHMlTL      (270°)
        SOUTH_EAST;  // was: xQr5FhbwpQPWgIQ(315°)

        /** Nearest Direction8 to a yaw. */
        public static Direction8 fromYaw(float yaw) { // was: FvaNWO(float)
            float normalized = (yaw % 360.0F + 360.0F) % 360.0F;
            int i = Math.round(normalized / 45.0F) % 8;
            return values()[i];
        }

        /** The opposite direction. */
        public Direction8 opposite() { // was: FvaNWO()
            return values()[(this.ordinal() + 4) % 8];
        }

        /** The opposite of a yaw value. */
        public static float oppositeYaw(float currentYaw) { // was: Q90GLXQ0Pef(float)
            float normalized = (currentYaw % 360.0F + 360.0F) % 360.0F;
            return (normalized + 180.0F) % 360.0F;
        }

        /** The yaw for a direction. */
        public static float toYaw(Direction8 direction) { // was: FvaNWO(Direction8)
            return direction.ordinal() * 45.0F;
        }
    }

    /** Simple (x, z) coordinate pair. */ // was: record HFbqnT1FEh2q$aY0a71o
    public record Coord2D(double x, double z) { }

    // Bridge to the movement helpers on musheor.utils.PlayerUtils to avoid the
    // name clash with Meteor's PlayerUtils imported above.
    private static final class PlayerUtilsHelper {
        static void setAutoWalk(boolean active) { musheor.utils.PlayerUtils.setAutoWalk(active); }
    }
}
