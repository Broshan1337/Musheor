// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AutoEat.class}, remap=false)
public abstract class AutoEatMixin
extends Module {
    @Shadow
    public boolean eating;
    @Shadow
    private int slot;
    @Shadow
    private int prevSlot;
    @Unique
    private boolean waitingToEat = false;
    @Unique
    private int swapDelay = 0;
    @Unique
    private int stuckTicks = 0;
    @Unique
    private float lastHealth = -1.0f;
    @Unique
    private int lastFood = -1;
    @Unique
    private boolean resetting = false;
    @Unique
    private int resetStep = 0;
    @Unique
    private int savedFoodSlot = -1;

    public AutoEatMixin(Category category, String string, String string2) {
        super(category, string, string2);
    }

    @Inject(method={"startEating"}, at={@At(value="HEAD")})
    private void onStartEating(CallbackInfo callbackInfo) {
        this.savedFoodSlot = this.slot;
        this.waitingToEat = true;
        this.swapDelay = 5;
        this.stuckTicks = 0;
        this.lastHealth = -1.0f;
        this.lastFood = -1;
        this.resetting = false;
        this.resetStep = 0;
    }

    @Inject(method={"eat"}, at={@At(value="HEAD")}, cancellable=true)
    private void onEat(CallbackInfo callbackInfo) {
        if (this.waitingToEat || this.resetting) {
            this.eating = true;
            callbackInfo.cancel();
        }
    }

    @Unique
    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || !this.eating) {
            return;
        }
        if (((AutoGap)Modules.get().get(AutoGap.class)).isEating()) {
            return;
        }
        if (this.resetting) {
            ++this.resetStep;
            if (this.resetStep == 1) {
                int n = this.savedFoodSlot != 0 ? 0 : 1;
                InvUtils.swap((int)n, (boolean)false);
                this.mc.options.gameRenderer.setUsingCamera(false);
            } else if (this.resetStep >= 3) {
                this.resetting = false;
                this.resetStep = 0;
                this.waitingToEat = true;
                this.swapDelay = 5;
                this.stuckTicks = 0;
                this.lastHealth = -1.0f;
                this.lastFood = -1;
                InvUtils.swap((int)this.savedFoodSlot, (boolean)false);
            }
            return;
        }
        if (this.waitingToEat) {
            if (--this.swapDelay <= 0) {
                this.waitingToEat = false;
            }
            return;
        }
        float f = this.mc.player.getHealth();
        int n = this.mc.player.getHungerManager().getFoodLevel();
        if (this.lastHealth < 0.0f) {
            this.lastHealth = f;
            this.lastFood = n;
            return;
        }
        if (f > this.lastHealth || n > this.lastFood) {
            this.stuckTicks = 0;
            this.lastHealth = f;
            this.lastFood = n;
        } else if (++this.stuckTicks >= 200) {
            this.savedFoodSlot = this.slot;
            this.resetting = true;
            this.resetStep = 0;
        }
    }
}

