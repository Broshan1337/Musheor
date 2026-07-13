// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the Handlers/KekFly/KekBounce calls were obfuscated.
package musheor.mixin;

import java.util.UUID;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.automation.KekBounce;
import musheor.modules.features.KekFly;
import musheor.utils.Handlers;
import net.minecraft.class_1297; // Entity
import net.minecraft.class_4050; // EntityPose
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the "gliding" body pose and forces the sprint/no-push state on the local player while
 * KekFly (past its launch delay) or KekBounce is engaged, so the player renders and behaves like an
 * elytra flyer. Only affects the local player (matched by uuid).
 */
@Mixin(class_1297.class)
public class MixinEntity {
   @Shadow
   protected UUID field_6021; // uuid

   @Inject(at = @At("HEAD"), method = "method_18376", cancellable = true) // getPose
   private void getPose(CallbackInfoReturnable<class_4050> cir) {
      KekBounce bounce = (KekBounce) Modules.get().get(KekBounce.class);
      KekFly fly = (KekFly) Modules.get().get(KekFly.class);
      if (Handlers.mc.field_1724 != null // was: GZpL.FvaNWO (Handlers.mc); .field_1724 = player
         && this.field_6021 == Handlers.mc.field_1724.method_5667() // getUuid
         && (fly != null && fly.isActive() && KekFly.isPastLaunchDelay() || bounce != null && bounce.canBounce())) {
         cir.setReturnValue(class_4050.field_18077); // EntityPose.FALL_FLYING (gliding)
      }
   }

   @Inject(at = @At("HEAD"), method = "method_5624", cancellable = true) // isSprinting
   private void isSprinting(CallbackInfoReturnable<Boolean> cir) {
      KekBounce bounce = (KekBounce) Modules.get().get(KekBounce.class);
      if (bounce != null && bounce.canBounce() && this.field_6021 == Handlers.mc.field_1724.method_5667()) {
         cir.setReturnValue(true);
      }
   }

   @Inject(at = @At("HEAD"), method = "method_5697", cancellable = true) // push (collision push-away)
   private void pushAwayFrom(class_1297 entity, CallbackInfo ci) {
      KekBounce bounce = (KekBounce) Modules.get().get(KekBounce.class);
      if (Handlers.mc.field_1724 != null
         && this.field_6021 == Handlers.mc.field_1724.method_5667()
         && bounce != null
         && bounce.canBounce()
         && !entity.method_5667().equals(this.field_6021)) {
         ci.cancel();
      }
   }
}
