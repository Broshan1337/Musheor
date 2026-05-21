// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin.meteor;

import musheor.modules.features.DepthInteract;
import net.minecraft.InteractionHand;
import net.minecraft.class_1269;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.Screen;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_636.class})
public class ClientPlayerInteractionManagerMixin {
    @Unique
    private static boolean depthInteract$spoofing;

    @Inject(method={"method_2896"}, at={@At(value="HEAD")}, cancellable=true)
    private void depthInteract$swapHit(class_746 class_7462, InteractionHand InteractionHand2, Screen Screen2, CallbackInfoReturnable<class_1269> callbackInfoReturnable) {
        DepthInteract depthInteract = DepthInteract.INSTANCE;
        if (depthInteract == null || !depthInteract.isActive()) {
            return;
        }
        if (depthInteract$spoofing) {
            return;
        }
        if (depthInteract.mcAmeo == null) {
            return;
        }
        BlockPos BlockPos2 = depthInteract.mcAmeo;
        Screen Screen3 = new Screen(Vec3d.method_24953((BlockPos)BlockPos2), Direction.field_11036, BlockPos2, false);
        depthInteract$spoofing = true;
        class_636 class_6362 = (class_636)this;
        callbackInfoReturnable.setReturnValue((Object)class_6362.method_2896(class_7462, InteractionHand2, Screen3));
        depthInteract$spoofing = false;
    }
}

