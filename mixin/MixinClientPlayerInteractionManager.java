// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import musheor.modules.features.KekMine;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.MinecraftClient;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_636.class})
public class MixinClientPlayerInteractionManager {
    @Inject(method={"method_2910"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAttackBlock(BlockPos BlockPos2, Direction Direction2, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        KekMine kekMine = (KekMine)Modules.get().get(KekMine.class);
        if (kekMine.isActive() && !MinecraftClient.getInstance().player.method_68878()) {
            if (BlockUtils.canInstaBreak((BlockPos)BlockPos2)) {
                KekMine.e5oi2ZF(BlockPos2);
            } else {
                kekMine.usJLOV0subXO3(BlockPos2);
            }
            callbackInfoReturnable.setReturnValue((Object)true);
        }
    }

    @Inject(method={"method_2902"}, at={@At(value="HEAD")}, cancellable=true)
    private void onUpdateBlockBreakingProgress(BlockPos BlockPos2, Direction Direction2, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        KekMine kekMine = (KekMine)Modules.get().get(KekMine.class);
        if (kekMine.isActive() && !MinecraftClient.getInstance().player.method_68878()) {
            kekMine.usJLOV0subXO3(BlockPos2);
            callbackInfoReturnable.setReturnValue((Object)true);
        }
    }
}

