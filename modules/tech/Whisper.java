// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
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

/**
 * "tp-request" — sends a randomised TP-request whisper to a pearlbot player when enabled,
 * then disables itself. The message length is configurable.
 */
public class Whisper extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: FvaNWO

    private final Setting<Integer> length = sgGeneral.add(new IntSetting.Builder() // was: psJq59YIbp3Z
        .name("length").description("The length of the generated message").defaultValue(12).min(1).sliderRange(1, 31).build());
    private final Setting<String> playerName = this.settings.getDefaultGroup().add(new StringSetting.Builder() // was: SOYyh5IPg26f7F
        .name("Player name").description("IGN of the player you want to send a message to.").defaultValue("").build());

    public Whisper() {
        super(musheor.MAIN, "tp-request", "Used for sending TP requests to pearlbots");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.isActive()) {
            PlayerUtils.sendTeleportMessage(this.playerName.get(), this.length.get());
            this.toggle();
        }
    }
}
