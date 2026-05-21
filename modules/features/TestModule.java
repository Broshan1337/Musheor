// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.Formatting;
import net.minecraft.ScreenHandler;
import net.minecraft.Slot;
import net.minecraft.MinecraftClient;

public class TestModule
extends Module {
    private static final MinecraftClient bsaKtrf = MinecraftClient.getInstance();

    public TestModule() {
        super(musheor.MAIN, "testing-module", "Do not use, testing for musheck...");
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        TestModule.bsaKtrf.player.method_7353((ScreenHandler)ScreenHandler.method_43470((String)"Test").method_10862(Slot.field_24360.method_10977(Formatting.field_1065).method_10982(Boolean.valueOf(true))), true);
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
    }
}

