// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import musheor.utils.system.MusheorSystem;
import musheor.utils.system.MutualManager;
import net.minecraft.Formatting;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public class MixinPlayerOutline {
    @Inject(method={"method_5851"}, at={@At(value="HEAD")}, cancellable=true)
    private void makeGlow(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        PlayerEntity PlayerEntity2;
        if (((Boolean)MusheorSystem.Manager.disableMutuals.get()).booleanValue()) {
            return;
        }
        Entity Entity2 = (Entity)this;
        if (Entity2 instanceof PlayerEntity && MutualManager.mutualManager.shouldGlow(PlayerEntity2 = (PlayerEntity)Entity2) && !MinecraftClient.getInstance().options.spectator) {
            callbackInfoReturnable.setReturnValue((Object)true);
        }
    }

    @Inject(method={"method_22861"}, at={@At(value="HEAD")}, cancellable=true)
    private void getTeamColorValue(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        if (((Boolean)MusheorSystem.Manager.disableMutuals.get()).booleanValue()) {
            return;
        }
        Entity Entity2 = (Entity)this;
        if (Entity2 instanceof PlayerEntity) {
            PlayerEntity PlayerEntity2 = (PlayerEntity)Entity2;
            if (MutualManager.mutualManager.isPost(PlayerEntity2)) {
                callbackInfoReturnable.setReturnValue((Object)Formatting.GREEN.getColorValue());
            } else if (MutualManager.mutualManager.isKek(PlayerEntity2)) {
                callbackInfoReturnable.setReturnValue((Object)Formatting.YELLOW.getColorValue());
            }
        }
    }
}

