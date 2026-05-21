// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.automation.KekBounce;
import musheor.modules.features.KekFly;
import musheor.modules.features.NoJumpDelay;
import musheor.utils.Handlers;
import net.minecraft.Entity;
import net.minecraft.EntityType;
import net.minecraft.LivingEntity;
import net.minecraft.DimensionType;
import net.minecraft.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class MixinLivingEntity
extends Entity {
    @Shadow
    private int field_6228;
    @Unique
    NoJumpDelay njd = (NoJumpDelay)Modules.get().get(NoJumpDelay.class);
    @Unique
    KekFly fly = (KekFly)Modules.get().get(KekFly.class);
    KekBounce bounce = (KekBounce)Modules.get().get(KekBounce.class);

    public MixinLivingEntity(EntityType<?> EntityType2, DimensionType DimensionType2) {
        super(EntityType2, DimensionType2);
    }

    @Shadow
    public abstract EquipmentSlot<?> method_18868();

    @Inject(at={@At(value="HEAD")}, method={"method_6007"})
    private void tickMovement(CallbackInfo callbackInfo) {
        if (Handlers.r9l7h0HpZAuA.player != null && Handlers.r9l7h0HpZAuA.player.getActiveStatusEffects().equals(this.getActiveStatusEffects()) && (this.njd != null && this.njd.isActive() || this.fly != null && this.fly.isActive() || this.bounce != null && this.bounce.jokapWphssQ())) {
            this.jumpingCooldown = 0;
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"method_6128"}, cancellable=true)
    private void isGliding(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (Handlers.r9l7h0HpZAuA.player != null && Handlers.r9l7h0HpZAuA.player.getActiveStatusEffects().equals(this.getActiveStatusEffects()) && (this.fly != null && this.fly.isActive() && KekFly.SNCRr7EZFUj() || this.bounce != null && this.bounce.jokapWphssQ())) {
            callbackInfoReturnable.setReturnValue((Object)true);
        }
    }
}

