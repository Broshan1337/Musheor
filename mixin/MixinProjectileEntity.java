// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.features.MoreTags;
import net.minecraft.EntityType;
import net.minecraft.ProjectileEntity;
import net.minecraft.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ProjectileEntity.class})
public class MixinProjectileEntity {
    @Inject(method={"method_31471"}, at={@At(value="TAIL")})
    private void cacheOwnerEntityId(PacketByteBuf PacketByteBuf2, CallbackInfo callbackInfo) {
        if (((ProjectileEntity)this).getRemovalReason() != EntityType.PERSISTENT) {
            return;
        }
        int n = PacketByteBuf2.readInt();
        if (n == 0) {
            return;
        }
        MoreTags moreTags = (MoreTags)Modules.get().get(MoreTags.class);
        if (moreTags != null) {
            moreTags.TAdu5cndwWu3A1(((ProjectileEntity)this).getId(), n);
        }
    }
}

