// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import java.util.UUID;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.automation.KekBounce;
import musheor.modules.features.KekFly;
import musheor.utils.Handlers;
import net.minecraft.Entity;
import net.minecraft.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public class MixinEntity {
    @Shadow
    protected UUID field_6021;
    @Unique
    KekFly fly = (KekFly)Modules.get().get(KekFly.class);
    KekBounce bounce = (KekBounce)Modules.get().get(KekBounce.class);

    @Inject(at={@At(value="HEAD")}, method={"method_18376"}, cancellable=true)
    private void getPose(CallbackInfoReturnable<EntityPose> callbackInfoReturnable) {
        if (Handlers.r9l7h0HpZAuA.player != null && this.uuid == Handlers.r9l7h0HpZAuA.player.getUuid() && (this.fly != null && this.fly.isActive() && KekFly.SNCRr7EZFUj() || this.bounce != null && this.bounce.jokapWphssQ())) {
            callbackInfoReturnable.setReturnValue((Object)EntityPose.SLEEPING);
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"method_5624"}, cancellable=true)
    private void isSprinting(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (this.bounce != null && this.bounce.jokapWphssQ() && this.uuid == Handlers.r9l7h0HpZAuA.player.getUuid()) {
            callbackInfoReturnable.setReturnValue((Object)true);
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"method_5697"}, cancellable=true)
    private void pushAwayFrom(Entity Entity2, CallbackInfo callbackInfo) {
        if (Handlers.r9l7h0HpZAuA.player != null && this.uuid == Handlers.r9l7h0HpZAuA.player.getUuid() && this.bounce != null && this.bounce.jokapWphssQ() && !Entity2.getUuid().equals(this.uuid)) {
            callbackInfo.cancel();
        }
    }
}

