// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.mixin;

import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.features.CoordHider;
import net.minecraft.class_332;
import net.minecraft.class_340;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_340.class})
public class DebugHudMixin {
    @Inject(method={"method_51745"}, at={@At(value="HEAD")})
    private void filterCoords(class_332 class_3322, List<String> list, boolean bl, CallbackInfo callbackInfo) {
        CoordHider coordHider = (CoordHider)Modules.get().get(CoordHider.class);
        if (coordHider == null || !coordHider.isActive()) {
            return;
        }
        if (bl) {
            list.removeIf(string -> string != null && (string.startsWith("XYZ:") || string.startsWith("Block:") || string.startsWith("Chunk:")));
        } else {
            list.removeIf(string -> string != null && (string.contains("Targeted Block") || string.contains("Targeted Fluid")));
        }
    }
}

