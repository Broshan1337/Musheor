// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.EzpHtsX2O)
package musheor.utils;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.systems.friends.Friends;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

/**
 * Helpers for the MoreTags feature: reading a player's equipment, distance-based
 * colour gradients for the tag, and friend checks.
 */
public class TagUtils {

    /** Returns the player's 4 armour pieces (head, chest, legs, feet). */
    public static List<ItemStack> getArmor(PlayerEntity player) { // was: FvaNWO(PlayerEntity)
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(player.getEquippedStack(EquipmentSlot.HEAD));
        stacks.add(player.getEquippedStack(EquipmentSlot.CHEST));
        stacks.add(player.getEquippedStack(EquipmentSlot.LEGS));
        stacks.add(player.getEquippedStack(EquipmentSlot.FEET));
        return stacks;
    }

    public static ItemStack getMainHand(PlayerEntity player) { // was: Q90GLXQ0Pef(PlayerEntity)
        return player.getMainHandStack();
    }

    public static ItemStack getOffHand(PlayerEntity player) { // was: psJq59YIbp3Z(PlayerEntity)
        return player.getOffHandStack();
    }

    /** True if the player has any non-empty hand or armour item. */
    public static boolean hasAnyEquipment(PlayerEntity player) { // was: SOYyh5IPg26f7F(PlayerEntity)
        if (!getMainHand(player).isEmpty()) return true;
        if (!getOffHand(player).isEmpty()) return true;
        for (ItemStack s : getArmor(player)) {
            if (!s.isEmpty()) return true;
        }
        return false;
    }

    /** Distance→colour gradient (red near → green far, over 0..100 blocks). Returns 0xRRGGBB. */
    public static int distanceColor(double distance) { // was: FvaNWO(double)
        float t = (float) MathHelper.clamp(distance / 100.0, 0.0, 1.0);
        int r, g;
        if (t < 0.5F) {
            r = 255;
            g = (int) (255.0F * (t * 2.0F));
        } else {
            r = (int) (255.0F * (1.0F - (t - 0.5F) * 2.0F));
            g = 255;
        }
        return r << 16 | g << 8;
    }

    /** Linear interpolation between two 0xRRGGBB colours. */
    public static int lerpColor(int colorA, int colorB, float t) { // was: FvaNWO(int,int,float)
        int ar = colorA >> 16 & 0xFF, ag = colorA >> 8 & 0xFF, ab = colorA & 0xFF;
        int br = colorB >> 16 & 0xFF, bg = colorB >> 8 & 0xFF, bb = colorB & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int b = (int) (ab + (bb - ab) * t);
        return r << 16 | g << 8 | b;
    }

    /** Distance from the local player to the target (0 if no local player). */
    public static double distanceTo(PlayerEntity target) { // was: rKbT3Ifwo(PlayerEntity)
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.player == null ? 0.0 : mc.player.distanceTo(target);
    }

    public static boolean isFriend(PlayerEntity player) { // was: r7hOYIKN2(PlayerEntity)
        return Friends.get().isFriend(player);
    }
}
