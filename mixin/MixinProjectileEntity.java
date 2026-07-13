// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the MoreTags call was obfuscated.
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.features.MoreTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** On an ender pearl's spawn packet, records its owner entity id for {@link MoreTags} pearl tracking. */
@Mixin(ProjectileEntity.class)
public class MixinProjectileEntity {
    @Inject(method = "method_31471", at = @At("TAIL")) // onSpawnPacket
    private void cacheOwnerEntityId(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        if (((ProjectileEntity) (Object) this).getType() == EntityType.ENDER_PEARL) {
            int ownerEntityId = packet.getEntityData(); // method_11166
            if (ownerEntityId != 0) {
                MoreTags module = (MoreTags) Modules.get().get(MoreTags.class);
                if (module != null) {
                    module.assignPearlOwner(((ProjectileEntity) (Object) this).getId(), ownerEntityId); // was: FvaNWO(int,int)
                }
            }
        }
    }
}
