// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud.actions;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.compat.VersionHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;

/** Button action that toggles a module by name. */
public class ToggleModuleAction implements ButtonAction {
    public String moduleName; // was: FvaNWO

    public ToggleModuleAction(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void execute(ScreenHandler handler) { // was: FvaNWO(ScreenHandler)
        Module mod = Modules.get().get(this.moduleName);
        if (mod != null) mod.toggle();
    }

    @Override
    public String getType() { // was: FvaNWO()
        return "toggle_module";
    }

    @Override
    public NbtCompound toTag() { // was: Q90GLXQ0Pef()
        NbtCompound tag = new NbtCompound();
        tag.putString("type", this.getType());
        tag.putString("module", this.moduleName);
        return tag;
    }

    public static ToggleModuleAction fromTag(NbtCompound tag) { // was: Q90GLXQ0Pef(NbtCompound)
        return new ToggleModuleAction(VersionHelper.get().getString(tag, "module"));
    }
}
