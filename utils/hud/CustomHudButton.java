// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.hud;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NbtCompound;

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
    public NbtCompound toTag() {
        return new NbtCompound();
    }

    /** Deserialises a CustomHudButton from the given NBT compound. */
    public static CustomHudButton fromTag(NbtCompound tag) { // was: mp3zoXQFKUKYj5(NbtCompound)
        return new CustomHudButton();
    }
}