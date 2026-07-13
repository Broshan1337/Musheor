// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable; Minecraft intermediary ids are annotated inline.
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
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

/**
 * Hardens Meteor's AutoEat against getting stuck: it defers eating for a few ticks after the hotbar
 * swap (so the food item is actually held), and if health/hunger stop improving for ~200 ticks it
 * runs a recovery sequence that briefly swaps to another slot and re-presses use to unstick the eat.
 */
@Mixin(value = AutoEat.class, remap = false)
public abstract class AutoEatMixin extends Module {
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
   private float lastHealth = -1.0F;
   @Unique
   private int lastFood = -1;
   @Unique
   private boolean resetting = false;
   @Unique
   private int resetStep = 0;
   @Unique
   private int savedFoodSlot = -1;

   public AutoEatMixin(Category category, String name, String description) {
      super(category, name, description);
   }

   @Inject(method = "startEating", at = @At("HEAD"))
   private void onStartEating(CallbackInfo ci) {
      this.savedFoodSlot = this.slot;
      this.waitingToEat = true;
      this.swapDelay = 5;
      this.stuckTicks = 0;
      this.lastHealth = -1.0F;
      this.lastFood = -1;
      this.resetting = false;
      this.resetStep = 0;
   }

   @Inject(method = "eat", at = @At("HEAD"), cancellable = true)
   private void onEat(CallbackInfo ci) {
      if (this.waitingToEat || this.resetting) {
         this.eating = true;
         ci.cancel();
      }
   }

   @Unique
   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.field_1724 != null && this.eating) { // field_1724 = player
         if (!((AutoGap) Modules.get().get(AutoGap.class)).isEating()) {
            if (this.resetting) {
               this.resetStep++;
               if (this.resetStep == 1) {
                  int tempSlot = this.savedFoodSlot != 0 ? 0 : 1;
                  InvUtils.swap(tempSlot, false);
                  this.mc.field_1690.field_1904.method_23481(false); // options.useKey.setPressed(false)
               } else if (this.resetStep >= 3) {
                  this.resetting = false;
                  this.resetStep = 0;
                  this.waitingToEat = true;
                  this.swapDelay = 5;
                  this.stuckTicks = 0;
                  this.lastHealth = -1.0F;
                  this.lastFood = -1;
                  InvUtils.swap(this.savedFoodSlot, false);
               }
            } else if (this.waitingToEat) {
               if (--this.swapDelay <= 0) {
                  this.waitingToEat = false;
               }
            } else {
               float health = this.mc.field_1724.method_6032(); // getHealth
               int food = this.mc.field_1724.method_7344().method_7586(); // getHungerManager().getFoodLevel()
               if (this.lastHealth < 0.0F) {
                  this.lastHealth = health;
                  this.lastFood = food;
               } else {
                  if (!(health > this.lastHealth) && food <= this.lastFood) {
                     if (++this.stuckTicks >= 200) {
                        this.savedFoodSlot = this.slot;
                        this.resetting = true;
                        this.resetStep = 0;
                     }
                  } else {
                     this.stuckTicks = 0;
                     this.lastHealth = health;
                     this.lastFood = food;
                  }
               }
            }
         }
      }
   }
}
