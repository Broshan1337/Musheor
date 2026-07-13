// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud.actions;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.compat.VersionHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;

/** Button action that parses a value into a named setting of a named module. */
public class ChangeSettingAction implements ButtonAction {
    public String moduleName;  // was: FvaNWO
    public String settingName; // was: Q90GLXQ0Pef
    public String value;       // was: psJq59YIbp3Z

    public ChangeSettingAction(String moduleName, String settingName, String value) {
        this.moduleName = moduleName;
        this.settingName = settingName;
        this.value = value;
    }

    @Override
    public void execute(ScreenHandler handler) { // was: FvaNWO(ScreenHandler)
        Module mod = Modules.get().get(this.moduleName);
        if (mod == null) return;
        mod.settings.forEach(group -> group.forEach(setting -> {
            if (setting.name.equalsIgnoreCase(this.settingName)) setting.parse(this.value);
        }));
    }

    @Override
    public String getType() { // was: FvaNWO()
        return "change_setting";
    }

    @Override
    public NbtCompound toTag() { // was: Q90GLXQ0Pef()
        NbtCompound tag = new NbtCompound();
        tag.putString("type", this.getType());
        tag.putString("module", this.moduleName);
        tag.putString("setting", this.settingName);
        tag.putString("value", this.value);
        return tag;
    }

    public static ChangeSettingAction fromTag(NbtCompound tag) { // was: Q90GLXQ0Pef(NbtCompound)
        return new ChangeSettingAction(
            VersionHelper.get().getString(tag, "module"),
            VersionHelper.get().getString(tag, "setting"),
            VersionHelper.get().getString(tag, "value"));
    }
}
