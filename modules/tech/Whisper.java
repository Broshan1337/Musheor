// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.tech;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.PlayerUtils;
import net.minecraft.MinecraftClient;

public class Whisper
extends Module {
    private final SettingGroup sgGeneral;
    private static final MinecraftClient TcOntB5aoWFhEI = MinecraftClient.getInstance();
    private final Setting<Integer> length;
    private final Setting<String> PlayerIGN;

    public Whisper() {
        super(musheor.MAIN, "tp-request", "Used for sending TP requests to pearlbots");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.length = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("length")).description("The length of the generated message")).defaultValue((Object)12)).min(1).sliderRange(1, 31).build());
        this.PlayerIGN = this.settings.getDefaultGroup().add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("Player name")).description("IGN of the player you want to send a message to.")).defaultValue((Object)"")).build());
    }

    @EventHandler
    public void onTick(TickEvent.Pre pre) {
        if (this.isActive()) {
            PlayerUtils.jOdDDFXSeWl4((String)this.PlayerIGN.get(), (Integer)this.length.get());
            this.toggle();
        }
    }
}

