// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.compat;

import java.util.ArrayList;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

public class XearoMapHooks {
    private static RightClickOptionsProvider provider = null;

    public static void setProvider(RightClickOptionsProvider rightClickOptionsProvider) {
        provider = rightClickOptionsProvider;
    }

    public static void apply(ArrayList<RightClickOption> arrayList, IRightClickableElement iRightClickableElement, MapTileSelection mapTileSelection) {
        if (provider != null) {
            provider.addOptions(arrayList, iRightClickableElement, mapTileSelection);
        }
    }

    @FunctionalInterface
    public static interface RightClickOptionsProvider {
        public void addOptions(ArrayList<RightClickOption> var1, IRightClickableElement var2, MapTileSelection var3);
    }
}

