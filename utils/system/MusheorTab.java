// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils.system;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import musheor.compat.VersionHelper;
import musheor.utils.hud.screen.CustomButtonManagerScreen;
import net.minecraft.MinecraftClient;  // MinecraftClient
import net.minecraft.class_437;  // Screen

/**
 * Adds a "Musheor" tab to Meteor's GUI tab bar.
 *
 * The tab screen displays:
 *   - All MusheorSystem settings groups (Placement, Break, Packet, Render)
 *   - A "Custom HUD Buttons" button (only shown when the version helper reports support)
 */
public class MusheorTab extends Tab {
    public MusheorTab() {
        super("Musheor");
    }

    @Override
    public TabScreen createScreen(GuiTheme guiTheme) {
        return new MusheorScreen(guiTheme, this);
    }

    @Override
    public boolean isScreen(class_437 screen) { // Screen
        return screen instanceof MusheorScreen;
    }

    public static class MusheorScreen extends WindowTabScreen {
        public MusheorScreen(GuiTheme guiTheme, Tab tab) {
            super(guiTheme, tab);
        }

        @Override
        public void initWidgets() {
            this.add(this.theme.settings(MusheorSystem.get().getSettings())).expandX();
            if (VersionHelper.get().supportsHudButtons()) {
                ((WButton) this.add((WWidget) this.theme.button("Custom HUD Buttons")).widget()).action =
                    () -> MinecraftClient.method_1551().method_1507( // MinecraftClient.getInstance().setScreen()
                        (class_437) new CustomButtonManagerScreen(MinecraftClient.method_1551().field_1755)); // currentScreen
            }
        }

        @Override
        public void method_25393() { // tick / render (intermediary name)
            if (MusheorSystem.Manager.update) {
                MusheorSystem.Manager.update = false;
                this.reload();
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard((ISerializable) MusheorSystem.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard((ISerializable) MusheorSystem.get());
        }
    }
}
