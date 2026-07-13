// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the KekMine calls were obfuscated.
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import musheor.modules.features.KekMine;
import net.minecraft.class_2338; // BlockPos
import net.minecraft.class_2350; // Direction
import net.minecraft.class_310; // MinecraftClient
import net.minecraft.class_636; // ClientPlayerInteractionManager
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes vanilla block-breaking through KekMine's packet miner while it is active: instant-breakable
 * blocks are broken directly, everything else is queued, and the vanilla path is cancelled so the
 * game does not also try to break the block. Skipped in spectator mode.
 */
@Mixin(class_636.class)
public class MixinClientPlayerInteractionManager {
   @Inject(method = "method_2910", at = @At("HEAD"), cancellable = true) // attackBlock
   private void onAttackBlock(class_2338 pos, class_2350 direction, CallbackInfoReturnable<Boolean> cir) {
      KekMine kekMine = (KekMine) Modules.get().get(KekMine.class);
      if (kekMine.isActive() && !class_310.method_1551().field_1724.method_68878()) { // !player.isSpectator() (method_68878)
         if (BlockUtils.canInstaBreak(pos)) {
            KekMine.breakBlock(pos); // was: KekMine.psJq59YIbp3Z(pos)
         } else {
            kekMine.mine(pos); // was: kekMine.FvaNWO(pos)
         }

         cir.setReturnValue(true);
      }
   }

   @Inject(method = "method_2902", at = @At("HEAD"), cancellable = true) // continueDestroyBlock
   private void onUpdateBlockBreakingProgress(class_2338 pos, class_2350 direction, CallbackInfoReturnable<Boolean> cir) {
      KekMine kekMine = (KekMine) Modules.get().get(KekMine.class);
      if (kekMine.isActive() && !class_310.method_1551().field_1724.method_68878()) { // !player.isSpectator()
         kekMine.mine(pos); // was: kekMine.FvaNWO(pos)
         cir.setReturnValue(true);
      }
   }
}
