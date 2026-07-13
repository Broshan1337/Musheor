// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; no obfuscated members.
package musheor.modules.features;

import meteordevelopment.meteorclient.systems.modules.Module;
import musheor.musheor;

/**
 * "no-jump-delay" — a marker module. When active, a mixin removes the cooldown between
 * consecutive jumps. Has no logic of its own.
 */
public class NoJumpDelay extends Module {
    public NoJumpDelay() {
        super(musheor.MAIN, "no-jump-delay", "Removes the delay between jumps.");
    }
}
