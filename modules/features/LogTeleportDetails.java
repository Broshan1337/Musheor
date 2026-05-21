// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.Packet;
import net.minecraft.class_2708;

public class LogTeleportDetails
extends Module {
    private final Setting<IdDisplay> idDisplay;

    public LogTeleportDetails() {
        super(musheor.MAIN, "tyler", "Tyler");
        this.idDisplay = this.settings.getDefaultGroup().add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("display")).defaultValue((Object)IdDisplay.GjvUiGg0HmH6I)).build());
    }

    @EventHandler(priority=-200)
    private void onReceivePacket(PacketEvent.Receive receive) {
        Packet Packet2 = receive.packet;
        if (Packet2 instanceof class_2708) {
            class_2708 class_27082 = (class_2708)Packet2;
            Packet2 = class_27082.comp_3228().comp_3148();
            Object object = "Server is trying to teleport you";
            object = (String)object + " to " + Packet2.method_10216() + " " + Packet2.method_10214() + " " + Packet2.method_10215();
            object = (String)object + this.jOdDDFXSeWl4(class_27082);
            this.info((String)object, new Object[0]);
        }
    }

    private String jOdDDFXSeWl4(class_2708 class_27082) {
        Object object = "";
        switch (((IdDisplay)((Object)this.idDisplay.get())).ordinal()) {
            case 0: {
                object = " with ID " + class_27082.comp_3133();
                break;
            }
            case 1: {
                object = " by ?";
                break;
            }
            case 2: {
                object = " with ID " + class_27082.comp_3133() + " by ?";
                break;
            }
        }
        return object;
    }

    static final class IdDisplay
    extends Enum<IdDisplay> {
        public static final /* enum */ IdDisplay GjvUiGg0HmH6I = new IdDisplay();
        public static final /* enum */ IdDisplay cIb0h21P81 = new IdDisplay();
        public static final /* enum */ IdDisplay hJTPuzeVhs9lAR = new IdDisplay();
        public static final /* enum */ IdDisplay LTAva3M = new IdDisplay();
        private static final /* synthetic */ IdDisplay[] y4KXVv64NUgBOpTQ;

        public static IdDisplay[] values() {
            return (IdDisplay[])y4KXVv64NUgBOpTQ.clone();
        }

        public static IdDisplay valueOf(String string) {
            return Enum.valueOf(IdDisplay.class, string);
        }

        private static /* synthetic */ IdDisplay[] VGuQlVJMpGYGr() {
            return new IdDisplay[]{GjvUiGg0HmH6I, cIb0h21P81, hJTPuzeVhs9lAR, LTAva3M};
        }

        static {
            y4KXVv64NUgBOpTQ = IdDisplay.VGuQlVJMpGYGr();
        }
    }
}

