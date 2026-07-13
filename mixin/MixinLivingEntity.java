// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the Handlers/KekFly/KekBounce calls were obfuscated.
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.automation.KekBounce;
import musheor.modules.features.KekFly;
import musheor.modules.features.NoJumpDelay;
import musheor.utils.Handlers;
import net.minecraft.class_1297; // Entity
import net.minecraft.class_1299; // EntityType
import net.minecraft.class_1309; // LivingEntity
import net.minecraft.class_1937; // World
import net.minecraft.class_4095; // Brain
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * For the local player: zeroes the jump cooldown (NoJumpDelay / KekFly / KekBounce) so jumps can be
 * chained without the vanilla delay, and forces the "gliding" flag while KekFly (past its launch
 * delay) or KekBounce is engaged. The player is matched by comparing brains.
 */
@Mixin(class_1309.class)
public abstract class MixinLivingEntity extends class_1297 {
   @Shadow
   private int field_6228; // jumpingCooldown (noJumpDelay)

   public MixinLivingEntity(class_1299<?> type, class_1937 world) {
      super(type, world);
   }

   @Shadow
   public abstract class_4095<?> method_18868(); // getBrain

   @Inject(at = @At("HEAD"), method = "method_6007") // tickMovement
   private void tickMovement(CallbackInfo ci) {
      NoJumpDelay njd = (NoJumpDelay) Modules.get().get(NoJumpDelay.class);
      KekFly fly = (KekFly) Modules.get().get(KekFly.class);
      KekBounce bounce = (KekBounce) Modules.get().get(KekBounce.class);
      if (Handlers.mc.field_1724 != null // was: GZpL.FvaNWO (Handlers.mc); .field_1724 = player
         && Handlers.mc.field_1724.method_18868().equals(this.method_18868())
         && (njd != null && njd.isActive() || fly != null && fly.isActive() || bounce != null && bounce.canBounce())) {
         this.field_6228 = 0; // clear jump cooldown
      }
   }

   @Inject(at = @At("HEAD"), method = "method_6128", cancellable = true) // isGliding (isFallFlying)
   private void isGliding(CallbackInfoReturnable<Boolean> cir) {
      KekFly fly = (KekFly) Modules.get().get(KekFly.class);
      KekBounce bounce = (KekBounce) Modules.get().get(KekBounce.class);
      if (Handlers.mc.field_1724 != null
         && Handlers.mc.field_1724.method_18868().equals(this.method_18868())
         && (fly != null && fly.isActive() && KekFly.isPastLaunchDelay() || bounce != null && bounce.canBounce())) {
         cir.setReturnValue(true);
      }
   }
}
