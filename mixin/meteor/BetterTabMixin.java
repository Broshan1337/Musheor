// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable (Meteor is not obfuscated; remap = false).
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import musheor.accessor.BetterTabAccessor;
import musheor.utils.system.MusheorSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a "display-mode" setting ({@link MusheorSystem.TabListMode}) to Meteor's BetterTab module and
 * exposes it through {@link BetterTabAccessor}, so Musheor's tab-list rendering (see PlayerListHudMixin)
 * can honour the chosen mode.
 */
@Mixin(value = BetterTab.class, remap = false)
public abstract class BetterTabMixin implements BetterTabAccessor {
   @Final
   @Shadow
   private SettingGroup sgGeneral;
   @Unique
   private Setting<MusheorSystem.TabListMode> musheor$displayMode;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(CallbackInfo ci) {
      this.musheor$displayMode = this.sgGeneral
         .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("display-mode")).description("Display mode for tablist entries."))
                  .defaultValue(MusheorSystem.TabListMode.All))
               .build()
         );
   }

   @Override
   public Setting<MusheorSystem.TabListMode> musheor$getDisplayMode() {
      return this.musheor$displayMode;
   }
}
