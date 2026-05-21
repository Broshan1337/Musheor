// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import musheor.modules.hud.HudInfoPlus;
import musheor.modules.tech.ContainerTweaks;
import musheor.utils.internal.RateController;
import net.minecraft.class_2535;
import net.minecraft.Packet;
import net.minecraft.class_2811;
import net.minecraft.class_2813;
import net.minecraft.class_2873;
import net.minecraft.class_2885;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_2535.class})
public abstract class MixinClientConnection {
    @Inject(method={"method_10743"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendPacket(Packet<?> Packet2, CallbackInfo callbackInfo) {
        if (Packet2 instanceof class_2813 || Packet2 instanceof class_2873 || Packet2 instanceof class_2811) {
            if (ContainerTweaks.j7OmRvH5go.isActive() && ((Boolean)ContainerTweaks.j7OmRvH5go.noPacketKick.get()).booleanValue() && !RateController.OwcAnTXUsd()) {
                callbackInfo.cancel();
                return;
            }
            HudInfoPlus.yUTSjfYE2q2du();
        }
        if (Packet2 instanceof class_2885) {
            HudInfoPlus.TfF42oD7();
        }
        HudInfoPlus.Y036W9pcsZhAYFUl();
    }
}

