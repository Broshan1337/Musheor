// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import musheor.utils.Handlers;
import net.minecraft.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AutoGap.class}, remap=false)
public abstract class MixinAutoGap {
    @Shadow
    private SettingGroup sgPotions;
    @Unique
    private Setting<Boolean> burnEat;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo callbackInfo) {
        this.burnEat = this.sgPotions.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("burning")).description("Eat if you are burning and don't have Fire Resistance.")).defaultValue((Object)false)).build());
    }

    @Inject(method={"shouldEatPotions"}, at={@At(value="RETURN")}, cancellable=true)
    private void onShouldEatPotions(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (((Boolean)callbackInfoReturnable.getReturnValue()).booleanValue()) {
            return;
        }
        if (!((Boolean)this.burnEat.get()).booleanValue()) {
            return;
        }
        if (Handlers.r9l7h0HpZAuA.player != null && Handlers.r9l7h0HpZAuA.player.method_5809() && !Handlers.r9l7h0HpZAuA.player.method_6059(Hand.field_5918)) {
            callbackInfoReturnable.setReturnValue((Object)true);
        }
    }
}

