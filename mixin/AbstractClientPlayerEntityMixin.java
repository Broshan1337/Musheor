// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class readable. Some 1.21.11 skin/cape asset types are annotated with their intermediary id.
package musheor.mixin;

import musheor.utils.system.MutualManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures; // class_8685
import net.minecraft.client.util.SkinTextures.Asset.Direct; // class_12079$class_10726
import net.minecraft.client.util.SkinTextures.Asset; // class_12079$class_12081
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Cosmetic-only: gives Musheor "mutual" players the Musheor+ cape by swapping the cape asset in their skin textures. */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method = "method_52814", at = @At("RETURN"), cancellable = true) // getSkinTextures
    private void onGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        MutualManager m = MutualManager.getInstance();
        if (!m.hasCape(player)) return;
        SkinTextures original = cir.getReturnValue();
        Asset capeAsset = new Direct(MutualManager.PLUS_CAPE, MutualManager.PLUS_CAPE); // class_10726
        // Reconstruct with the mutual cape, keeping the other skin components (comp_1626/1628/1629/1630).
        SkinTextures modified = new SkinTextures(original.body(), capeAsset, original.elytra(), original.model(), original.secure());
        cir.setReturnValue(modified);
    }
}
