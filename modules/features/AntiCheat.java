// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.Packet;
import net.minecraft.CloseScreenS2CPacket;

public class AntiCheat
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> airplaceBypass;
    public final Setting<Boolean> noScreenClose;

    public AntiCheat() {
        super(musheor.MAIN, "anti-cheat", "Anti-cheat bypasses for 2b2t.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.airplaceBypass = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("air-place-bypass")).description("Bypasses default meteor place methods with an airplace method across all modules")).defaultValue((Object)true)).visible(() -> false)).build());
        this.noScreenClose = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-screen-close")).description("Prevents the server from closing your inventory / GUI screens")).defaultValue((Object)true)).build());
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive receive) {
        if (!((Boolean)this.noScreenClose.get()).booleanValue() || !this.isActive()) {
            return;
        }
        Packet Packet2 = receive.packet;
        if (Packet2 instanceof CloseScreenS2CPacket) {
            CloseScreenS2CPacket CloseScreenS2CPacket2 = (CloseScreenS2CPacket)Packet2;
            receive.cancel();
        }
    }
}

