// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the WorldUtils call and AntiCheat setting were obfuscated.
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import musheor.modules.features.AntiCheat;
import musheor.utils.WorldUtils;
import net.minecraft.class_2338; // BlockPos
import net.minecraft.class_2350; // Direction
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When AntiCheat's "air-place bypass" is on, reroutes Meteor's BlockUtils.place through Musheor's own
 * offhand placement (always placing against the DOWN face) so placements pass the server's checks
 * instead of using Meteor's default path.
 */
@Mixin(value = BlockUtils.class, remap = false)
public class MixinBlockUtilsBypass {
   @Inject(method = "place(Lnet/minecraft/class_2338;Lmeteordevelopment/meteorclient/utils/player/FindItemResult;ZIZZ)Z", at = @At("HEAD"), cancellable = true)
   private static void onPlace(
      class_2338 blockPos,
      FindItemResult findItemResult,
      boolean rotate,
      int rotationPriority,
      boolean swingHand,
      boolean checkEntities,
      CallbackInfoReturnable<Boolean> cir
   ) {
      AntiCheat antiCheat = (AntiCheat) Modules.get().get(AntiCheat.class);
      if (antiCheat.airPlaceBypass.get()) { // was: antiCheat.FvaNWO.get()
         boolean result = WorldUtils.placeBlockOffhand(blockPos, class_2350.field_11033); // was: HFbqnT1FEh2q.FvaNWO(pos, Direction.DOWN)
         cir.setReturnValue(result);
         cir.cancel();
      }
   }
}
