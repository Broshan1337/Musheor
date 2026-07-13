// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the DepthInteract target field was obfuscated.
package musheor.mixin.meteor;

import musheor.modules.features.DepthInteract;
import net.minecraft.class_1268; // Hand
import net.minecraft.class_1269; // ActionResult
import net.minecraft.class_2338; // BlockPos
import net.minecraft.class_2350; // Direction
import net.minecraft.class_243;  // Vec3d
import net.minecraft.class_3965; // BlockHitResult
import net.minecraft.class_636;  // ClientPlayerInteractionManager
import net.minecraft.class_746;  // ClientPlayerEntity
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * DepthInteract: when active with a stored target block, replaces the interaction's hit result with
 * one centred on that target block (against the UP face) so the player interacts with a block behind
 * others. A re-entrancy guard prevents the spoofed interaction from recursing.
 */
@Mixin(class_636.class)
public class ClientPlayerInteractionManagerMixin {
   @Unique
   private static boolean depthInteract$spoofing;

   @Inject(method = "method_2896", at = @At("HEAD"), cancellable = true) // interactBlock
   private void depthInteract$swapHit(class_746 player, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> cir) {
      DepthInteract mod = DepthInteract.INSTANCE;
      if (mod != null && mod.isActive()) {
         if (!depthInteract$spoofing) {
            if (mod.targetPos != null) { // was: mod.FvaNWO
               class_2338 pos = mod.targetPos;
               class_3965 newHit = new class_3965(class_243.method_24953(pos), class_2350.field_11036, pos, false); // Vec3d.ofCenter, Direction.UP
               depthInteract$spoofing = true;
               class_636 self = (class_636) (Object) this;
               cir.setReturnValue(self.method_2896(player, hand, newHit));
               depthInteract$spoofing = false;
            }
         }
      }
   }
}
