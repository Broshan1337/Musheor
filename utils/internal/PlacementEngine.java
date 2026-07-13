// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.QLktfq)
package musheor.utils.internal;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Low-level block placement helper. Places using the off-hand by temporarily
 * swapping main/off hand around the interaction so the placement uses the desired
 * stack without disturbing the visible hotbar selection.
 */
public class PlacementEngine {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: FvaNWO (field)

    /** Places a block against {@code direction} at {@code pos}. Returns false if not in world. */
    public static boolean placeBlock(BlockPos pos, Direction direction) { // was: FvaNWO(BlockPos,Direction)
        if (mc.player != null && mc.getNetworkHandler() != null && mc.interactionManager != null) {
            swapHands();
            interact(Hand.OFF_HAND, buildHitResult(pos, direction));
            swapHands();
            return true;
        }
        return false;
    }

    private static void interact(Hand hand, BlockHitResult hit) { // was: FvaNWO(Hand,BlockHitResult)
        mc.interactionManager.interactBlock(mc.world, hand, hit); // sequenced
    }

    private static BlockHitResult buildHitResult(BlockPos pos, Direction direction) { // was: Q90GLXQ0Pef(BlockPos,Direction)
        return new BlockHitResult(Vec3d.ofCenter(pos), direction, pos, false);
    }

    /** Swaps main-hand and off-hand stacks (via a no-op swap-item action + inventory move). */
    private static void swapHands() { // was: FvaNWO()
        if (mc.player != null) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            ItemStack mainHand = mc.player.getMainHandStack();
            ItemStack offHand = mc.player.getOffHandStack();
            mc.player.getInventory().setStack(mc.player.getInventory().selectedSlot, offHand);
            mc.player.getInventory().setStack(40, mainHand); // slot 40 = off-hand
        }
    }
}
