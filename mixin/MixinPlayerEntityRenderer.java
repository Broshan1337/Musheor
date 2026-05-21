// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.features.MoreTags;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.ScreenHandler;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1007.class})
public class MixinPlayerEntityRenderer {
    @Inject(method={"renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void cancelDefaultNametag(class_10055 class_100552, ScreenHandler ScreenHandler2, class_4587 class_45872, class_4597 class_45972, int n, CallbackInfo callbackInfo) {
        MoreTags moreTags = (MoreTags)Modules.get().get(MoreTags.class);
        if (moreTags != null && moreTags.isActive() && ((Boolean)moreTags.playerTags.get()).booleanValue()) {
            callbackInfo.cancel();
        }
    }
}

