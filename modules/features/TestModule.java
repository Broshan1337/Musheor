// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        TestModule.bsaKtrf.player.sendMessage(Text.literal("Test").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(Boolean.valueOf(true))), true);
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
    }
}

