// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; the MusheorSystem/CustomHudButton calls were obfuscated.
package musheor.mixin;

import musheor.compat.VersionHelper;
import musheor.utils.hud.CustomHudButton;
import musheor.utils.hud.DraggableButtonWidget;
import musheor.utils.system.MusheorSystem;
import net.minecraft.class_11908; // KeyInput (1.21.11 input record)
import net.minecraft.class_11909; // Click (1.21.11 mouse-click record)
import net.minecraft.class_1703; // ScreenHandler
import net.minecraft.class_2561; // Text
import net.minecraft.class_342; // TextFieldWidget
import net.minecraft.class_408; // ChatScreen
import net.minecraft.class_437; // Screen
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds Musheor's custom draggable HUD buttons (those targeting the CHAT screen) to the chat screen,
 * plus the optional Musheor+ IRC button loaded reflectively. When a draggable button is focused the
 * focus is bounced back to the chat input unless Ctrl is held (so the button can be dragged).
 */
@Mixin(class_408.class)
public abstract class ChatScreenMixin extends class_437 {
   @Shadow
   protected class_342 field_2382; // input (chat text field)

   protected ChatScreenMixin(class_2561 title) {
      super(title);
   }

   @Inject(method = "method_25426", at = @At("TAIL")) // init
   private void onInit(CallbackInfo info) {
      // Optional Musheor+ integration: adds an IRC chat button if the plus jar is present.
      try {
         DraggableButtonWidget ircBtn = (DraggableButtonWidget) Class.forName("musheor.plus.PlusChatScreenInit")
            .getMethod("createChatButton", int.class)
            .invoke(null, this.field_22790); // height
         if (ircBtn != null) {
            this.method_37063(ircBtn); // addDrawableChild
         }
      } catch (Exception ignored) {
      }

      for (CustomHudButton btn : MusheorSystem.get().customButtons) {
         if (btn.screenTarget.matches(CustomHudButton.ScreenTarget.CHAT)) { // was: btn.zu3a44xDeMFMCRwm.FvaNWO(ScreenTarget.Q90GLXQ0Pef)
            this.method_37063(btn.createWidget(null)); // was: btn.FvaNWO(null)
         }
      }
   }

   @Inject(method = "method_25402", at = @At("RETURN")) // mouseClicked
   private void onMouseClicked(class_11909 click, boolean bl, CallbackInfoReturnable<Boolean> cir) {
      if (this.method_25399() instanceof DraggableButtonWidget && !VersionHelper.get().hasControlDown()) {
         this.method_25395(this.field_2382); // setFocused(chat input)
      }
   }

   @Inject(method = "method_25404", at = @At("RETURN")) // keyPressed
   private void onKeyPressed(class_11908 key, CallbackInfoReturnable<Boolean> cir) {
      if (this.method_25399() instanceof DraggableButtonWidget && !VersionHelper.get().hasControlDown()) {
         this.method_25395(this.field_2382);
      }
   }
}
