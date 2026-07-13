// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable.
package musheor.mixin;

import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.modules.features.CoordHider;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** While CoordHider is active, strips coordinate lines out of the F3 debug text. */
@Mixin(DebugHud.class)
public class DebugHudMixin {
    @Inject(method = "method_51745", at = @At("HEAD")) // drawText
    private void filterCoords(DrawContext context, List<String> text, boolean left, CallbackInfo ci) {
        CoordHider ch = (CoordHider) Modules.get().get(CoordHider.class);
        if (ch == null || !ch.isActive()) return;
        if (left) {
            text.removeIf(line -> line != null && (line.startsWith("XYZ:") || line.startsWith("Block:") || line.startsWith("Chunk:")));
        } else {
            text.removeIf(line -> line != null && (line.contains("Targeted Block") || line.contains("Targeted Fluid")));
        }
    }
}
