// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.internal;

import musheor.modules.hud.HudInfoPlus;
import musheor.utils.system.MusheorSystem;
import net.minecraft.MinecraftClient;  // MinecraftClient

/**
 * Guards block-place and inventory-slot packets against exceeding server rate limits.
 * Creative-mode players are always allowed (no packet counting applies).
 */
public class RateController {
    private RateController() {}

    /**
     * Returns true if a block-place packet may be sent this tick.
     * Allowed when: player is in creative mode, OR the HudInfoPlus place-packet
     * counter is below 9 (the hard cap before anti-cheat triggers).
     */
    public static boolean checkPlaceRate() { // was: qy8UwM99rVr
        if (MinecraftClient.method_1551().player.method_68878()) { // player.isCreative()
            return true;
        }
        return HudInfoPlus.getPlacePacketCount() < 9; // was: Y1fGfDLuV()
    }

    /**
     * Returns true if an inventory-slot packet may be sent this tick.
     * Allowed when: player is in creative mode, OR the inventory packet counter is
     * below the user-configured limit in MusheorSystem settings.
     */
    public static boolean checkInventoryRate() { // was: OwcAnTXUsd
        if (MinecraftClient.method_1551().player.method_68878()) { // player.isCreative()
            return true;
        }
        return HudInfoPlus.getInvPacketCount() < (Integer) MusheorSystem.Manager.invPacketLimit.get(); // was: kBQdZKStLMVDV()
    }
}
