// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable.
package musheor.mixin;

import musheor.utils.system.MusheorSystem;
import musheor.utils.system.MutualManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cosmetic-only: makes Musheor community members ("mutuals") glow, coloured green for "post"
 * tier and aqua for "kek" tier. Skipped when the user disabled mutual cosmetics or the HUD is
 * hidden.
 */
@Mixin(Entity.class)
public class MixinPlayerOutline {
    @Inject(method = "method_5851", at = @At("HEAD"), cancellable = true) // isGlowing
    private void makeGlow(CallbackInfoReturnable<Boolean> cir) {
        if (MusheorSystem.Manager.disableMutuals.get()) return;
        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity player && MutualManager.mutualManager.shouldGlow(player) && !MinecraftClient.getInstance().options.hudHidden) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "method_22861", at = @At("HEAD"), cancellable = true) // getTeamColorValue
    private void getTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        if (MusheorSystem.Manager.disableMutuals.get()) return;
        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            if (MutualManager.mutualManager.isPost(player)) {
                cir.setReturnValue(Formatting.GREEN.getColorValue());
            } else if (MutualManager.mutualManager.isKek(player)) {
                cir.setReturnValue(Formatting.AQUA.getColorValue());
            }
        }
    }
}
