// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import musheor.utils.hud.screen.CustomButtonManagerScreen;
import musheor.utils.system.MusheorSystem;
import net.minecraft.client.MinecraftClient;

/** Opens the custom HUD-button manager screen when the configured keybind is pressed (only while no screen is open). */
public class HudEditorKeybind {
    private final MinecraftClient mc = MinecraftClient.getInstance(); // was: FvaNWO

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.currentScreen == null && MusheorSystem.get().openEditor.get().isPressed()) {
            this.mc.execute(() -> this.mc.setScreen(new CustomButtonManagerScreen(this.mc.currentScreen)));
        }
    }
}
