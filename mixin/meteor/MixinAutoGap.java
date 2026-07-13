// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the Handlers.mc.player references were obfuscated.
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import musheor.utils.Handlers;
import net.minecraft.class_1294; // StatusEffects
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a "burning" option to Meteor's AutoGap module: when enabled, forces eating if the player is on
 * fire and lacks Fire Resistance (so a gapple is eaten to survive the burn).
 */
@Mixin(value = AutoGap.class, remap = false)
public abstract class MixinAutoGap {
   @Shadow
   private SettingGroup sgPotions;
   @Unique
   private Setting<Boolean> burnEat;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(CallbackInfo ci) {
      this.burnEat = this.sgPotions
         .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("burning")).description("Eat if you are burning and don't have Fire Resistance."))
                  .defaultValue(false))
               .build()
         );
   }

   @Inject(method = "shouldEatPotions", at = @At("RETURN"), cancellable = true)
   private void onShouldEatPotions(CallbackInfoReturnable<Boolean> cir) {
      if (!cir.getReturnValue()) {
         if (this.burnEat.get()) {
            // was: GZpL.FvaNWO (Handlers.mc); .field_1724 = player, method_5809 = isOnFire,
            // method_6059 = hasStatusEffect, class_1294.field_5918 = StatusEffects.FIRE_RESISTANCE
            if (Handlers.mc.field_1724 != null && Handlers.mc.field_1724.method_5809() && !Handlers.mc.field_1724.method_6059(class_1294.field_5918)) {
               cir.setReturnValue(true);
            }
         }
      }
   }
}
