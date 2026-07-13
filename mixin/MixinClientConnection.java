// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; ContainerTweaks/RateController/HudInfoPlus calls were obfuscated.
package musheor.mixin;

import musheor.modules.hud.HudInfoPlus;
import musheor.modules.tech.ContainerTweaks;
import musheor.utils.internal.RateController;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feeds the packet-limit HUD counters and enforces ContainerTweaks' inventory-packet limiter:
 * inventory packets bump the inventory + global counters (cancelled if the limiter would trip),
 * block-interact packets bump the interaction counter, and everything bumps the global counter.
 */
@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {
    @Inject(method = "method_10743", at = @At("HEAD"), cancellable = true) // send
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClickSlotC2SPacket || packet instanceof CreativeInventoryActionC2SPacket || packet instanceof ButtonClickC2SPacket) {
            if (ContainerTweaks.INSTANCE.isActive() && ContainerTweaks.INSTANCE.noInvPacketKicks.get() && !RateController.canSendInventoryPacket()) {
                ci.cancel();
                return;
            }
            HudInfoPlus.recordInvPacket(); // was: HudInfoPlus.FvaNWO()
        }

        if (packet instanceof PlayerInteractBlockC2SPacket) {
            HudInfoPlus.recordSentPacket(); // was: HudInfoPlus.psJq59YIbp3Z()
        }

        HudInfoPlus.recordPacket(); // was: HudInfoPlus.Q90GLXQ0Pef()
    }
}
