// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import musheor.modules.features.AntiCheat;
import musheor.utils.WorldUtils;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockUtils.class}, remap=false)
public class MixinBlockUtilsBypass {
    @Inject(method={"place(Lnet/minecraft/BlockPos;Lmeteordevelopment/meteorclient/utils/player/FindItemResult;ZIZZ)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onPlace(BlockPos BlockPos2, FindItemResult findItemResult, boolean bl, int n, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        AntiCheat antiCheat = (AntiCheat)Modules.get().get(AntiCheat.class);
        if (!((Boolean)antiCheat.airplaceBypass.get()).booleanValue()) {
            return;
        }
        boolean bl4 = WorldUtils.jOdDDFXSeWl4(BlockPos2, Direction.field_11033);
        callbackInfoReturnable.setReturnValue((Object)bl4);
        callbackInfoReturnable.cancel();
    }
}

