// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Interface and members were already readable.
package musheor.accessor;

import meteordevelopment.meteorclient.settings.Setting;
import musheor.utils.system.MusheorSystem;

/**
 * Mixin accessor into Meteor's BetterTab module, exposing its tab-list display-mode setting
 * so Musheor's tab-list rendering can respect it.
 */
public interface BetterTabAccessor {
    Setting<MusheorSystem.TabListMode> musheor$getDisplayMode();
}
