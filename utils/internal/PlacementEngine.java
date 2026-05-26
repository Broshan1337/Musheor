// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.internal;

import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Low-level block placement engine.
 *
 * Places a block at the given position by:
 *   1. Swapping main-hand and off-hand items (to move the block to off-hand)
 *   2. Sending a UseItemOnBlock (PlayerInteractBlockC2SPacket) for the OFF_HAND
 *   3. Swapping back
 *
 * This allows placing from the main-hand slot without visually switching the held item.
 */
public class PlacementEngine {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: HR1W3IhvN8Q

    private PlacementEngine() {}

    /**
     * Places a block at {@code pos} with the given face {@code direction}.
     * Returns false if the client, player, or world is null.
     */
    public static boolean placeBlock(BlockPos pos, Direction direction) { // was: jOdDDFXSeWl4(BlockPos,Direction)
        if (PlacementEngine.mc.player == null
                || mc.getNetworkHandler() == null
                || PlacementEngine.mc.interactionManager == null) {
            return false;
        }
        PlacementEngine.swapCarriedItems();
        PlacementEngine.sendPlacePacket(Hand.OFF_HAND, PlacementEngine.makeHitResult(pos, direction));
        PlacementEngine.swapCarriedItems();
        return true;
    }

    /** Sends a UseItemOnBlock packet for the given hand and hit result. */
    private static void sendPlacePacket(Hand hand, BlockHitResult hitResult) { // was: mp3zoXQFKUKYj5(Hand,BlockHitResult)
        PlacementEngine.mc.interactionManager.sendSequencedPacket(
            PlacementEngine.mc.world,
            n -> new PlayerInteractBlockC2SPacket(hand, hitResult, n));
    }

    /** Builds a BlockHitResult targeting the center of the given face. */
    private static BlockHitResult makeHitResult(BlockPos pos, Direction direction) { // was: VYEwzRq(BlockPos,Direction)
        return new BlockHitResult(
            Vec3d.ofCenter((Vec3i) pos),
            direction,
            pos,
            false);
    }

    /**
     * Swaps the main-hand and off-hand items by:
     *   1. Sending a SWAP_ITEM_WITH_OFFHAND PlayerAction packet
     *   2. Swapping the inventory slot items in the local inventory model
     *
     * The selected hotbar slot receives the off-hand item; slot 40 (off-hand) receives
     * the main-hand item. Called before and after the place packet to keep inventory in sync.
     */
    private static void swapCarriedItems() { // was: MCTY8c
        if (PlacementEngine.mc.player == null) return;
        // Notify the server of the swap
        PlacementEngine.mc.player.networkHandler.sendPacket(
            (Packet) new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos.ORIGIN,
                Direction.DOWN));
        // Reflect the swap locally in the player inventory
        ItemStack mainHand = PlacementEngine.mc.player.getMainHandStack();
        ItemStack offHand  = PlacementEngine.mc.player.getOffHandStack();
        PlacementEngine.mc.player.getInventory().setStack(
            PlacementEngine.mc.player.getInventory().selectedSlot,
            offHand);
        PlacementEngine.mc.player.getInventory().setStack(40, mainHand); // slot 40 = off-hand
    }
}