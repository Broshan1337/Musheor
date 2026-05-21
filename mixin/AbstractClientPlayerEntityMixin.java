// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import musheor.utils.system.MutualManager;
import net.minecraft.class_12079;
import net.minecraft.PlayerEntity;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_742.class})
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method={"method_52814"}, at={@At(value="RETURN")}, cancellable=true)
    private void onGetSkinTextures(CallbackInfoReturnable<class_8685> callbackInfoReturnable) {
        class_742 class_7422 = (class_742)this;
        MutualManager mutualManager = MutualManager.getInstance();
        if (mutualManager.hasCape((PlayerEntity)class_7422)) {
            class_8685 class_86852 = (class_8685)callbackInfoReturnable.getReturnValue();
            class_12079.class_10726 class_107262 = new class_12079.class_10726(MutualManager.PLUS_CAPE, MutualManager.PLUS_CAPE);
            class_8685 class_86853 = new class_8685(class_86852.comp_1626(), (class_12079.class_12081)class_107262, class_86852.comp_1628(), class_86852.comp_1629(), class_86852.comp_1630());
            callbackInfoReturnable.setReturnValue((Object)class_86853);
        }
    }
}

