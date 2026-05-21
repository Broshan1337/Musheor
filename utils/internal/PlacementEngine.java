// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.internal;

import net.minecraft.class_1268;   // Hand
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.BlockPos;   // BlockPos
import net.minecraft.Direction;   // Direction
import net.minecraft.class_2382;   // Vec3i
import net.minecraft.class_243;    // Vec3d
import net.minecraft.class_2596;   // Packet
import net.minecraft.class_2846;   // PlayerInteractBlockC2SPacket
import net.minecraft.class_2885;   // PlayerInteractBlockC2SPacket (use)
import net.minecraft.MinecraftClient;    // MinecraftClient
import net.minecraft.Screen;   // BlockHitResult

/**
 * Low-level block placement engine.
 *
 * Places a block at the given position by:
 *   1. Swapping main-hand and off-hand items (to put the block in main hand)
 *   2. Sending a UseItemOnBlock packet for the MAIN_HAND
 *   3. Swapping back
 *
 * This allows placing from the off-hand slot without visually switching the held item.
 */
public class PlacementEngine {
    private static final MinecraftClient mc = MinecraftClient.method_1551(); // MinecraftClient.getInstance() — was: HR1W3IhvN8Q

    private PlacementEngine() {}

    /**
     * Places a block at {@code pos} with the given face {@code direction}.
     * Returns false if the client, player, or world is null.
     */
    public static boolean placeBlock(BlockPos pos, Direction direction) { // was: jOdDDFXSeWl4(BlockPos,Direction)
        if (PlacementEngine.mc.field_1724 == null   // player
                || mc.method_1562() == null          // getNetworkHandler()
                || PlacementEngine.mc.field_1761 == null) { // interactionManager
            return false;
        }
        PlacementEngine.swapCarriedItems();
        PlacementEngine.sendPlacePacket(class_1268.field_5810, PlacementEngine.makeHitResult(pos, direction)); // Hand.MAIN_HAND
        PlacementEngine.swapCarriedItems();
        return true;
    }

    /** Sends a UseItemOnBlock packet for the given hand and hit result. */
    private static void sendPlacePacket(class_1268 hand, Screen hitResult) { // was: mp3zoXQFKUKYj5(Hand,BlockHitResult)
        PlacementEngine.mc.field_1761.method_41931( // interactionManager.interactBlock
            PlacementEngine.mc.field_1687,           // world
            n -> new class_2885(hand, hitResult, n));
    }

    /** Builds a BlockHitResult for placing against the center of the given face. */
    private static Screen makeHitResult(BlockPos pos, Direction direction) { // was: VYEwzRq(BlockPos,Direction)
        return new Screen(
            class_243.method_24953((class_2382) pos), // Vec3d.ofCenter(pos)
            direction,
            pos,
            false);
    }

    /**
     * Swaps the main-hand and off-hand items by:
     *   1. Sending a SWAP_ITEM_WITH_OFFHAND PlayerAction packet
     *   2. Swapping the inventory slot items in the local inventory model
     *
     * Called before and after the place packet to keep off-hand contents in sync.
     */
    private static void swapCarriedItems() { // was: MCTY8c
        if (PlacementEngine.mc.field_1724 == null) return; // player null check
        // Send the SWAP_ITEM_WITH_OFFHAND (action=6) packet
        PlacementEngine.mc.player.field_3944.method_52787( // networkHandler.sendPacket
            (class_2596) new class_2846(
                class_2846.class_2847.field_12969,  // PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND
                BlockPos.field_10980,             // BlockPos.ORIGIN
                Direction.field_11033));            // Direction.DOWN
        // Reflect the swap locally in the player inventory
        ItemStack mainHand = PlacementEngine.mc.player.method_6047();   // getMainHandStack()
        ItemStack offHand  = PlacementEngine.mc.player.method_6079();   // getOffHandStack()
        PlacementEngine.mc.player.getId().method_5447(            // getInventory().setStack(offHandSlot, mainHand)
            PlacementEngine.mc.player.getId().field_7545,         // offHandSlot index
            offHand);
        PlacementEngine.mc.player.getId().method_5447(40, mainHand); // slot 40 = off-hand in vanilla
    }
}
