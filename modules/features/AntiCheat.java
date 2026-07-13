// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.features;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;

/**
 * "anti-cheat" — 2b2t anti-cheat bypasses. {@code air-place-bypass} (hidden) routes module
 * block placement through an air-place method; {@code no-screen-close} cancels the server's
 * CloseScreen packet so the anticheat can't force your GUI/inventory shut.
 */
public class AntiCheat extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: psJq59YIbp3Z

    public final Setting<Boolean> airPlaceBypass = sgGeneral.add(new BoolSetting.Builder() // was: FvaNWO
        .name("air-place-bypass").description("Bypasses default meteor place methods with an airplace method across all modules")
        .defaultValue(true).visible(() -> false).build());
    public final Setting<Boolean> noScreenClose = sgGeneral.add(new BoolSetting.Builder() // was: Q90GLXQ0Pef
        .name("no-screen-close").description("Prevents the server from closing your inventory / GUI screens").defaultValue(true).build());

    public AntiCheat() {
        super(musheor.MAIN, "anti-cheat", "Anti-cheat bypasses for 2b2t.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) { // was: FvaNWO(Receive)
        if (this.noScreenClose.get() && this.isActive() && event.packet instanceof CloseScreenS2CPacket) {
            event.cancel();
        }
    }
}
