// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.features.MoreTags;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public class MixinPearlOutline {
    @Inject(method={"method_5851"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Entity Entity2 = (Entity)this;
        if (!(Entity2 instanceof LivingEntity)) {
            return;
        }
        LivingEntity LivingEntity2 = (LivingEntity)Entity2;
        MoreTags moreTags = (MoreTags)Modules.get().get(MoreTags.class);
        if (moreTags != null && moreTags.TAdu5cndwWu3A1(LivingEntity2)) {
            callbackInfoReturnable.setReturnValue((Object)true);
        }
    }

    @Inject(method={"method_22861"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        Entity Entity2 = (Entity)this;
        if (!(Entity2 instanceof LivingEntity)) {
            return;
        }
        LivingEntity LivingEntity2 = (LivingEntity)Entity2;
        MoreTags moreTags = (MoreTags)Modules.get().get(MoreTags.class);
        if (moreTags != null && moreTags.TAdu5cndwWu3A1(LivingEntity2)) {
            callbackInfoReturnable.setReturnValue((Object)moreTags.vgrtgn5(LivingEntity2));
        }
    }
}

