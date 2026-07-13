// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the MoreTags calls were obfuscated.
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.features.MoreTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EnderPearlEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes tracked stasis pearls glow with the MoreTags-configured outline colour (ESP). */
@Mixin(Entity.class)
public class MixinPearlOutline {
    @Inject(method = "method_5851", at = @At("HEAD"), cancellable = true) // isGlowing
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof EnderPearlEntity pearl) {
            MoreTags moreTags = (MoreTags) Modules.get().get(MoreTags.class);
            if (moreTags != null && moreTags.shouldRenderPearl(pearl)) { // was: FvaNWO(pearl)
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "method_22861", at = @At("HEAD"), cancellable = true) // getTeamColorValue
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof EnderPearlEntity pearl) {
            MoreTags moreTags = (MoreTags) Modules.get().get(MoreTags.class);
            if (moreTags != null && moreTags.shouldRenderPearl(pearl)) { // was: FvaNWO(pearl)
                cir.setReturnValue(moreTags.getPearlOutlineColor(pearl)); // was: Q90GLXQ0Pef(pearl)
            }
        }
    }
}
