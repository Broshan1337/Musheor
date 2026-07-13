// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; no obfuscated members.
package musheor.modules.features;

import meteordevelopment.meteorclient.systems.modules.Module;
import musheor.musheor;

/**
 * "coord-hider" — a marker module. When active, other modules (and mixins) hide
 * coordinates in the F3 debug screen and Meteor HUD. Has no logic of its own.
 */
public class CoordHider extends Module {
    public CoordHider() {
        super(musheor.MAIN, "coord-hider", "Hides the coordinates in the F3 debug screen and meteor hud.");
    }
}
