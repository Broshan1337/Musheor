// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin.meteor;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
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

@Mixin(value={BetterTab.class}, remap=false)
public abstract class BetterTabMixin
implements BetterTabAccessor {
    @Final
    @Shadow
    private SettingGroup sgGeneral;
    @Unique
    private Setting<MusheorSystem.TabListMode> musheor$displayMode;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo callbackInfo) {
        this.musheor$displayMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("display-mode")).description("Display mode for tablist entries.")).defaultValue((Object)MusheorSystem.TabListMode.All)).build());
    }

    @Override
    public Setting<MusheorSystem.TabListMode> musheor$getDisplayMode() {
        return this.musheor$displayMode;
    }
}

