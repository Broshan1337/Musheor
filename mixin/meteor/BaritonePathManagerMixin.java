// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable (Meteor/Baritone are not obfuscated; remap = false).
package musheor.mixin.meteor;

import baritone.api.BaritoneAPI;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import meteordevelopment.meteorclient.pathing.BaritonePathManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes Meteor's BaritonePathManager report Baritone's own target rotations instead of Meteor's, so
 * modules that read the path rotation follow Baritone. The VarHandle redirect neutralises Meteor's
 * reflective field grab (which fails on this environment) by returning null.
 */
@Mixin(value = BaritonePathManager.class, remap = false)
public class BaritonePathManagerMixin {
   @Redirect(
      method = "<init>",
      require = 0,
      at = @At(value = "INVOKE", target = "Ljava/lang/invoke/MethodHandles$Lookup;unreflectVarHandle(Ljava/lang/reflect/Field;)Ljava/lang/invoke/VarHandle;")
   )
   private VarHandle redirectUnreflectVarHandle(Lookup lookup, Field field) {
      return null;
   }

   @Overwrite
   public float getTargetYaw() {
      return BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().playerRotations().getYaw();
   }

   @Overwrite
   public float getTargetPitch() {
      return BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().playerRotations().getPitch();
   }
}
