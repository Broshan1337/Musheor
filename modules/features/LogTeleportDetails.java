// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.entity.EntityPosition;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class LogTeleportDetails
extends Module {
    private final Setting<IdDisplay> idDisplay;

    public LogTeleportDetails() {
        super(musheor.MAIN, "tyler", "Tyler");
        this.idDisplay = this.settings.getDefaultGroup().add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("display")).defaultValue((Object)IdDisplay.SHOW_ID)).build());
    }

    @EventHandler(priority=-200)
    private void onReceivePacket(PacketEvent.Receive receive) {
        Packet<?> packet = receive.packet;
        if (packet instanceof PlayerPositionLookS2CPacket teleportPacket) {
            Vec3d pos = teleportPacket.change().position();
            Object object = "Server is trying to teleport you";
            object = (String)object + " to " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
            object = (String)object + this.formatTeleportFlags(teleportPacket);
            this.info((String)object, new Object[0]);
        }
    }

    private String formatTeleportFlags(PlayerPositionLookS2CPacket teleportPacket) {
        Object object = "";
        switch (((IdDisplay)((Object)this.idDisplay.get())).ordinal()) {
            case 0: {
                object = " with ID " + teleportPacket.teleportId();
                break;
            }
            case 1: {
                object = " by ?";
                break;
            }
            case 2: {
                object = " with ID " + teleportPacket.teleportId() + " by ?";
                break;
            }
        }
        return object;
    }

    static final class IdDisplay
    extends Enum<IdDisplay> {
        public static final /* enum */ IdDisplay SHOW_ID = new IdDisplay();
        public static final /* enum */ IdDisplay SHOW_CAUSE = new IdDisplay();
        public static final /* enum */ IdDisplay SHOW_BOTH = new IdDisplay();
        public static final /* enum */ IdDisplay NONE = new IdDisplay();
        private static final /* synthetic */ IdDisplay[] $VALUES;

        public static IdDisplay[] values() {
            return (IdDisplay[])$VALUES.clone();
        }

        public static IdDisplay valueOf(String string) {
            return Enum.valueOf(IdDisplay.class, string);
        }

        private static /* synthetic */ IdDisplay[] $values() {
            return new IdDisplay[]{SHOW_ID, SHOW_CAUSE, SHOW_BOTH, NONE};
        }

        static {
            $VALUES = IdDisplay.$values();
        }
    }
}

