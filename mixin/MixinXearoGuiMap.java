// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import java.util.ArrayList;
import musheor.compat.XearoMapHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

@Mixin(value={GuiMap.class}, remap=false)
public abstract class MixinXearoGuiMap
implements IRightClickableElement {
    @Shadow
    private MapTileSelection mapTileSelection;

    @Inject(method={"getRightClickOptions"}, at={@At(value="RETURN")})
    private void musheor$addOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> callbackInfoReturnable) {
        MapTileSelection mapTileSelection = this.mapTileSelection;
        if (mapTileSelection == null) {
            return;
        }
        XearoMapHooks.apply((ArrayList)callbackInfoReturnable.getReturnValue(), this, mapTileSelection);
    }
}

