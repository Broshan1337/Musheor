// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.SyqMxK)
package musheor.utils.internal;

import musheor.modules.hud.HudInfoPlus;
import musheor.utils.system.MusheorSystem;
import net.minecraft.client.MinecraftClient;

/**
 * Packet rate-limiting gate. Before sending inventory/interaction packets, callers
 * check these to avoid exceeding the per-tick packet budget (which would trip
 * anti-cheat / cause desync). Creative-mode players are never throttled.
 */
public class RateController {

    /** True if it's safe to send another action packet this tick. */
    public static boolean canSendActionPacket() { // was: FvaNWO()
        return MinecraftClient.getInstance().player.isCreative()
            || HudInfoPlus.getSentPacketCount() < 9;
    }

    /** True if it's safe to send another inventory packet (under the configured limit). */
    public static boolean canSendInventoryPacket() { // was: Q90GLXQ0Pef()
        return MinecraftClient.getInstance().player.isCreative()
            || HudInfoPlus.getInvPacketCount() < (Integer) MusheorSystem.Manager.invPacketLimit.get();
    }
}
