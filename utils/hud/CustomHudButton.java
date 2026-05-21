// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.hud;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2487;  // NbtCompound

/**
 * Represents a single custom button rendered on the Musheor HUD overlay.
 *
 * NOTE: The decompiled bytecode for this class was largely stripped; only the
 * skeleton survived. The `data` list and NBT methods are stub implementations.
 */
public class CustomHudButton {
    /** Internal data payload for this button (type details not available in decompiled output). */
    public List<Object> data = new ArrayList<Object>(); // was: WOXOz6IehZdtYFz

    /** Serialises this button to an NBT compound for persistence. */
    public class_2487 toTag() { // NbtCompound
        return new class_2487();
    }

    /** Deserialises a CustomHudButton from the given NBT compound. */
    public static CustomHudButton fromTag(class_2487 tag) { // was: mp3zoXQFKUKYj5(NbtCompound)
        return new CustomHudButton();
    }
}
