// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the ContainerTweaks/RateController/CustomHudButton calls were obfuscated.
package musheor.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import musheor.modules.tech.ContainerTweaks;
import musheor.utils.hud.CustomHudButton;
import musheor.utils.hud.DraggableButtonWidget;
import musheor.utils.internal.RateController;
import musheor.utils.system.MusheorSystem;
import net.minecraft.class_1703; // ScreenHandler
import net.minecraft.class_1713; // SlotActionType
import net.minecraft.class_1735; // Slot
import net.minecraft.class_2561; // Text
import net.minecraft.class_3936; // ScreenHandlerProvider
import net.minecraft.class_437; // Screen
import net.minecraft.class_465; // HandledScreen
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wires ContainerTweaks into container screens: adds the draggable "Steal"/"Dump" buttons (plus any
 * custom HUD buttons targeting the INVENTORY context) on init, enforces the inventory-packet limiter
 * on slot clicks, and implements the move-all / move-matching keybinds (Ctrl-click style shortcuts).
 */
@Mixin(class_465.class)
public abstract class ScreenHandlerMixin<T extends class_1703> extends class_437 implements class_3936<T> {
   @Shadow
   protected T field_2797; // handler
   @Shadow
   protected int field_2776; // x (left edge)
   @Shadow
   protected int field_2800; // y (top edge)

   @Shadow
   public abstract T method_17577(); // getScreenHandler

   public ScreenHandlerMixin(class_2561 title) {
      super(title);
   }

   @Inject(method = "method_25426", at = @At("TAIL")) // init
   private void onInit(CallbackInfo info) {
      ContainerTweaks ct = (ContainerTweaks) Modules.get().get(ContainerTweaks.class);
      if (ct.isActive() && ct.showButtons.get()) { // was: ct.Q90GLXQ0Pef
         int stealX = ct.stealOffsetX.get() != 0 ? ct.stealOffsetX.get() : this.field_2776;
         int stealY = ct.stealOffsetY.get() != 0 ? ct.stealOffsetY.get() : this.field_2800 - 22;
         int dumpX = ct.dumpOffsetX.get() != 0 ? ct.dumpOffsetX.get() : this.field_2776 + 42;
         int dumpY = ct.dumpOffsetY.get() != 0 ? ct.dumpOffsetY.get() : this.field_2800 - 22;
         this.method_37063(
            new DraggableButtonWidget(
               stealX, stealY, 40, 20, class_2561.method_43470("Steal"),
               button -> ct.steal(this.method_17577()), ct.stealOffsetX, ct.stealOffsetY // was: ct.FvaNWO(handler)
            )
         );
         this.method_37063(
            new DraggableButtonWidget(
               dumpX, dumpY, 40, 20, class_2561.method_43470("Dump"),
               button -> ct.dump(this.method_17577()), ct.dumpOffsetX, ct.dumpOffsetY // was: ct.Q90GLXQ0Pef(handler)
            )
         );
      }

      for (CustomHudButton btn : MusheorSystem.get().customButtons) {
         if (btn.screenTarget.matches(CustomHudButton.ScreenTarget.INVENTORY)) { // was: .FvaNWO(ScreenTarget.FvaNWO)
            this.method_37063(btn.createWidget(this.method_17577())); // was: btn.FvaNWO(handler)
         }
      }
   }

   @Inject(method = "method_2383", at = @At("HEAD"), cancellable = true) // onMouseClick (slot click)
   private void onMouseClick(class_1735 slot, int slotId, int button, class_1713 actionType, CallbackInfo ci) {
      // Inventory-packet limiter: swallow the click if sending it now would trip the rate limit.
      if (ContainerTweaks.INSTANCE.isActive() && ContainerTweaks.INSTANCE.noInvPacketKicks.get() && !RateController.canSendInventoryPacket()) {
         ci.cancel(); // was: SyqMxK.Q90GLXQ0Pef() = RateController.canSendInventoryPacket()
      } else if (ContainerTweaks.INSTANCE != null && ContainerTweaks.INSTANCE.isActive()) {
         if (slot != null && slot.method_7681()) { // hasStack
            if (ContainerTweaks.INSTANCE.moveAllKey.get().isPressed()) { // was: .SOYyh5IPg26f7F
               ContainerTweaks.INSTANCE.moveAll(this.field_2797, slot); // was: .Q90GLXQ0Pef(handler,slot)
               ci.cancel();
            }

            if (ContainerTweaks.INSTANCE.moveMatchingKey.get().isPressed()) { // was: .psJq59YIbp3Z
               ContainerTweaks.INSTANCE.moveMatching(this.field_2797, slot); // was: .FvaNWO(handler,slot)
               ci.cancel();
            }
         }
      }
   }
}
