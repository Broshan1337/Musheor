// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.systems.friends.Friends;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

/**
 * Utility methods for player entity display:
 * equipment inspection, distance calculation, friend check, and color coding.
 */
public class TagUtils {

    /**
     * Returns a list of the player's 4 armor stacks in slot order:
     * [HEAD, CHEST, LEGS, FEET].
     */
    public static List<ItemStack> getArmorStacks(PlayerEntity player) { // was: jOdDDFXSeWl4(PlayerEntity)
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.add(player.getEquippedStack(EquipmentSlot.HEAD));
        stacks.add(player.getEquippedStack(EquipmentSlot.CHEST));
        stacks.add(player.getEquippedStack(EquipmentSlot.LEGS));
        stacks.add(player.getEquippedStack(EquipmentSlot.FEET));
        return stacks;
    }

    /** Returns the player's main-hand ItemStack. */
    public static ItemStack getMainHandStack(PlayerEntity player) { // was: mp3zoXQFKUKYj5(PlayerEntity)
        return player.getMainHandStack();
    }

    /** Returns the player's off-hand ItemStack. */
    public static ItemStack getOffHandStack(PlayerEntity player) { // was: Gt56Sj4a6BWhgB(PlayerEntity)
        return player.getOffHandStack();
    }

    /**
     * Returns true if the player is holding or wearing any item
     * (main hand, off-hand, or any armor slot is non-empty).
     */
    public static boolean hasAnyItem(PlayerEntity player) { // was: TAdu5cndwWu3A1(PlayerEntity)
        if (!TagUtils.getMainHandStack(player).isEmpty()) return true;
        if (!TagUtils.getOffHandStack(player).isEmpty())  return true;
        for (ItemStack armorStack : TagUtils.getArmorStacks(player)) {
            if (!armorStack.isEmpty()) return true;
        }
        return false;
    }

    /**
     * Maps a health value (0–100 scale) to an RGB colour:
     *   0%  → red   (0xFF0000)
     *   50% → yellow (0xFFFF00)
     *   100%→ green  (0x00FF00)
     *
     * The returned int is packed as 0xRRGGBB (no alpha).
     */
    public static int hpToColor(double hp) { // was: BX92A0OIIvD9(double)
        float t = (float) MathHelper.clamp((double)(hp / 100.0), (double) 0.0, (double) 1.0);
        int red, green;
        if (t < 0.5f) {
            // 0 → 0.5: red=255, green ramps 0→255
            red   = 255;
            green = (int)(255.0f * (t * 2.0f));
        } else {
            // 0.5 → 1.0: green=255, red ramps 255→0
            red   = (int)(255.0f * (1.0f - (t - 0.5f) * 2.0f));
            green = 255;
        }
        return red << 16 | green << 8 | 0; // blue always 0
    }

    /**
     * Linearly interpolates between two packed 0xRRGGBB colours.
     * {@code t=0} returns {@code colorA}, {@code t=1} returns {@code colorB}.
     */
    public static int lerpColor(int colorA, int colorB, float t) { // was: jOdDDFXSeWl4(int,int,float)
        int rA = (colorA >> 16) & 0xFF,  gA = (colorA >> 8) & 0xFF,  bA = colorA & 0xFF;
        int rB = (colorB >> 16) & 0xFF,  gB = (colorB >> 8) & 0xFF,  bB = colorB & 0xFF;
        int r  = (int)((float) rA + (float)(rB - rA) * t);
        int g  = (int)((float) gA + (float)(gB - gA) * t);
        int b  = (int)((float) bA + (float)(bB - bA) * t);
        return r << 16 | g << 8 | b;
    }

    /** Returns the distance from the local player to the given player entity. */
    public static double distanceTo(PlayerEntity player) { // was: vgrtgn5(PlayerEntity)
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0.0;
        return mc.player.distanceTo((Entity) player);
    }

    /** Returns true if the given player is on Meteor's friends list. */
    public static boolean isFriend(PlayerEntity player) { // was: VYEwzRq(PlayerEntity)
        return Friends.get().isFriend(player);
    }
}