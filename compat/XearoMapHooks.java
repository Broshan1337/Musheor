// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class and members were already readable.
package musheor.compat;

import java.util.ArrayList;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

/**
 * Static hook point (invoked from a Xaero mixin) that lets the addon add custom right-click
 * options to Xaero's world-map context menu. The actual provider is only registered when
 * Xaero is present.
 */
public class XearoMapHooks {
    private static RightClickOptionsProvider provider = null;

    public static void setProvider(RightClickOptionsProvider p) {
        provider = p;
    }

    public static void apply(ArrayList<RightClickOption> options, IRightClickableElement element, MapTileSelection selection) {
        if (provider != null) {
            provider.addOptions(options, element, selection);
        }
    }

    @FunctionalInterface
    public interface RightClickOptionsProvider {
        void addOptions(ArrayList<RightClickOption> options, IRightClickableElement element, MapTileSelection selection);
    }
}
