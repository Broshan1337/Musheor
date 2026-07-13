// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable.
package musheor.utils.system;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import musheor.compat.VersionHelper;
import musheor.utils.hud.screen.CustomButtonManagerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * The "Musheor" tab in the Meteor GUI. Shows the {@link MusheorSystem} settings and (where
 * supported) a button to open the custom HUD-button manager. Reloads itself when the system
 * flags a render-setting change, and supports copy/paste of the whole config via clipboard.
 */
public class MusheorTab extends Tab {
    public MusheorTab() {
        super("Musheor");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new MusheorScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof MusheorScreen;
    }

    public static class MusheorScreen extends WindowTabScreen {
        public MusheorScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            this.add(this.theme.settings(MusheorSystem.get().getSettings())).expandX();
            if (VersionHelper.get().supportsHudButtons()) {
                this.add(this.theme.button("Custom HUD Buttons")).widget().action =
                    () -> MinecraftClient.getInstance().setScreen(new CustomButtonManagerScreen(MinecraftClient.getInstance().currentScreen));
            }
        }

        @Override
        public void tick() { // was: method_25393()
            if (MusheorSystem.Manager.update) {
                MusheorSystem.Manager.update = false;
                this.reload();
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(MusheorSystem.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(MusheorSystem.get());
        }
    }
}
