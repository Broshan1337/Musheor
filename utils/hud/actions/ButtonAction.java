// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud.actions;

import musheor.compat.VersionHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;

/**
 * An action bound to a custom HUD button. Serializes to/from NBT by a {@code type} tag.
 */
public interface ButtonAction {
    void execute(ScreenHandler handler); // was: FvaNWO(ScreenHandler)

    String getType(); // was: FvaNWO()

    NbtCompound toTag(); // was: Q90GLXQ0Pef()

    /** Reconstructs the concrete action from its saved {@code type}, or null if unknown. */
    static ButtonAction fromTag(NbtCompound tag) { // was: FvaNWO(NbtCompound)
        if (!tag.contains("type")) return null;
        return switch (VersionHelper.get().getString(tag, "type")) {
            case "toggle_module" -> ToggleModuleAction.fromTag(tag);
            case "send_message" -> SendMessageAction.fromTag(tag);
            case "run_command" -> RunCommandAction.fromTag(tag);
            case "change_setting" -> ChangeSettingAction.fromTag(tag);
            default -> null;
        };
    }
}
