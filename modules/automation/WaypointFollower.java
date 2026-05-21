// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.XearoHelper;
import musheor.modules.features.KekFly;
import musheor.musheor;
import musheor.utils.PlayerUtils;
import musheor.utils.internal.PathingHelper;
import net.minecraft.Formatting;
import net.minecraft.MinecraftClient;

public class WaypointFollower
extends Module {
    private final MinecraftClient yUTSjfYE2q2du = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<FlightMode> flightMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("flight-mode")).description("Which type of flight mode to use")).defaultValue((Object)FlightMode.kBQdZKStLMVDV)).build());
    private final Setting<Boolean> drawPath = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("draw-path")).description("Draws a path between waypoints on Xearo's map")).defaultValue((Object)true)).visible(() -> XearoHelper.isLoaded() && this.flightMode.get() != FlightMode.TfF42oD7)).build());
    private final Setting<Integer> arrivalRadius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("arrival-radius")).description("XZ distance to a waypoint at which it is considered reached.")).defaultValue((Object)32)).sliderRange(8, 256).build());
    private final Setting<Double> degreesPerTick = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("degrees-per-tick")).description("Max yaw rotation per tick \u2014 limits how sharply the player turns toward a waypoint.")).defaultValue(3.0).sliderRange(0.5, 20.0).decimalPlaces(1).visible(() -> this.flightMode.get() == FlightMode.kBQdZKStLMVDV || this.flightMode.get() == FlightMode.XuSVOP3J5xFv)).build());
    private XearoHelper.WaypointData Y036W9pcsZhAYFUl = null;
    private int t018N0 = -1;

    public WaypointFollower() {
        super(musheor.AUTOMATION, "kekpoint-follower", "Automatically follow waypoints in all dimensions using different flight modes.");
    }

    public void onActivate() {
        if (this.yUTSjfYE2q2du.player == null || this.yUTSjfYE2q2du.world == null) {
            return;
        }
        Dimension dimension = meteordevelopment.meteorclient.utils.player.PlayerUtils.getDimension();
        if (this.flightMode.get() == FlightMode.TfF42oD7 && (dimension == Dimension.End || dimension == Dimension.Overworld)) {
            Formatting Formatting2 = dimension == Dimension.End ? Formatting.field_1064 : Formatting.RED;
            PlayerUtils.jOdDDFXSeWl4(WaypointFollower.class, "flight-mode", FlightMode.kBQdZKStLMVDV);
            ChatUtils.info((String)(String.valueOf(Formatting.field_1065) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Automatically switching to RocketFly: " + String.valueOf(Formatting2) + dimension.name() + String.valueOf(Formatting.GRAY) + " does not support Baritone Elytra Flight."), (Object[])new Object[0]);
            this.toggle();
            return;
        }
    }

    public void onDeactivate() {
        if (this.Y036W9pcsZhAYFUl != null) {
            PathingHelper.xRVyNRV3cB7();
            this.Y036W9pcsZhAYFUl = null;
        }
        if (XearoHelper.isLoaded()) {
            XearoHelper.get().clearLinesOnMap();
        }
        this.t018N0 = -1;
        KekFly kekFly = (KekFly)Modules.get().get(KekFly.class);
        if (kekFly.isActive() && this.flightMode.get() == FlightMode.kBQdZKStLMVDV) {
            kekFly.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.yUTSjfYE2q2du.player == null || this.yUTSjfYE2q2du.world == null) {
            return;
        }
        switch (((FlightMode)((Object)this.flightMode.get())).ordinal()) {
            case 0: {
                this.V9ZG3sNvd2tk4bPq();
                break;
            }
            case 1: {
                this.zJG6cArZPynMblA();
                break;
            }
            case 2: {
                this.eXDG63e();
            }
        }
        if (((Boolean)this.drawPath.get()).booleanValue() && this.flightMode.get() != FlightMode.TfF42oD7) {
            this.NUDC7Q4AxeSjEhHt();
        }
    }

    private void V9ZG3sNvd2tk4bPq() {
        if (this.Y036W9pcsZhAYFUl == null) {
            this.Y036W9pcsZhAYFUl = XearoHelper.get().getOldestWaypoint(true);
            if (this.Y036W9pcsZhAYFUl == null) {
                return;
            }
            this.mp3zoXQFKUKYj5(this.Y036W9pcsZhAYFUl);
            return;
        }
        if (XearoHelper.get().distanceToWaypoint(this.Y036W9pcsZhAYFUl) < (double)((Integer)this.arrivalRadius.get()).intValue()) {
            PathingHelper.xRVyNRV3cB7();
            ChatUtils.info((String)(String.valueOf(Formatting.GREEN) + "Arrived at " + String.valueOf(Formatting.field_1054) + this.Y036W9pcsZhAYFUl.name() + String.valueOf(Formatting.GREEN) + ", moving to next..."), (Object[])new Object[0]);
            XearoHelper.get().deleteCurrentWaypoint(this.Y036W9pcsZhAYFUl);
            this.Y036W9pcsZhAYFUl = null;
        }
    }

    private void zJG6cArZPynMblA() {
        if (!((KekFly)Modules.get().get(KekFly.class)).isActive()) {
            return;
        }
        if (this.Y036W9pcsZhAYFUl == null) {
            this.Y036W9pcsZhAYFUl = XearoHelper.get().getOldestWaypoint(true);
            if (this.Y036W9pcsZhAYFUl == null) {
                return;
            }
            ChatUtils.info((String)(String.valueOf(Formatting.field_1065) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Flying to: " + String.valueOf(Formatting.field_1054) + this.Y036W9pcsZhAYFUl.name()), (Object[])new Object[0]);
        }
        float f = this.Gt56Sj4a6BWhgB(this.Y036W9pcsZhAYFUl);
        float f2 = this.jOdDDFXSeWl4(this.yUTSjfYE2q2du.player.method_36454(), f);
        float f3 = (float)((Double)this.degreesPerTick.get()).doubleValue();
        if (Math.abs(f2) <= f3) {
            this.yUTSjfYE2q2du.player.method_36456(f);
        } else {
            this.yUTSjfYE2q2du.player.method_36456(this.yUTSjfYE2q2du.player.method_36454() - Math.signum(f2) * f3);
        }
        if (XearoHelper.get().distanceToWaypoint(this.Y036W9pcsZhAYFUl) < (double)((Integer)this.arrivalRadius.get()).intValue()) {
            ChatUtils.info((String)(String.valueOf(Formatting.field_1065) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Arrived at " + String.valueOf(Formatting.field_1054) + this.Y036W9pcsZhAYFUl.name() + String.valueOf(Formatting.GRAY) + ", moving to next..."), (Object[])new Object[0]);
            XearoHelper.get().deleteCurrentWaypoint(this.Y036W9pcsZhAYFUl);
            this.Y036W9pcsZhAYFUl = null;
        }
    }

    private void eXDG63e() {
        if (this.Y036W9pcsZhAYFUl == null) {
            this.Y036W9pcsZhAYFUl = XearoHelper.get().getOldestWaypoint(true);
            if (this.Y036W9pcsZhAYFUl == null) {
                return;
            }
            ChatUtils.info((String)(String.valueOf(Formatting.field_1065) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Flying to: " + String.valueOf(Formatting.field_1054) + this.Y036W9pcsZhAYFUl.name()), (Object[])new Object[0]);
        }
        float f = this.Gt56Sj4a6BWhgB(this.Y036W9pcsZhAYFUl);
        float f2 = this.jOdDDFXSeWl4(this.yUTSjfYE2q2du.player.method_36454(), f);
        float f3 = (float)((Double)this.degreesPerTick.get()).doubleValue();
        if (Math.abs(f2) <= f3) {
            this.yUTSjfYE2q2du.player.method_36456(f);
        } else {
            this.yUTSjfYE2q2du.player.method_36456(this.yUTSjfYE2q2du.player.method_36454() - Math.signum(f2) * f3);
        }
        if (XearoHelper.get().distanceToWaypoint(this.Y036W9pcsZhAYFUl) < (double)((Integer)this.arrivalRadius.get()).intValue()) {
            ChatUtils.info((String)(String.valueOf(Formatting.field_1065) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Arrived at " + String.valueOf(Formatting.field_1054) + this.Y036W9pcsZhAYFUl.name() + String.valueOf(Formatting.GRAY) + ", moving to next..."), (Object[])new Object[0]);
            XearoHelper.get().deleteCurrentWaypoint(this.Y036W9pcsZhAYFUl);
            this.Y036W9pcsZhAYFUl = null;
        }
    }

    private void NUDC7Q4AxeSjEhHt() {
        if (!XearoHelper.isLoaded()) {
            return;
        }
        List<XearoHelper.WaypointData> list = XearoHelper.get().getWaypoints(true);
        if (list == null) {
            return;
        }
        if (list.size() == this.t018N0) {
            return;
        }
        this.t018N0 = list.size();
        if (list.isEmpty()) {
            XearoHelper.get().clearLinesOnMap();
            return;
        }
        ArrayList<XearoHelper.WaypointData> arrayList = new ArrayList<XearoHelper.WaypointData>(list);
        arrayList.sort(Comparator.comparingLong(XearoHelper.WaypointData::createdAt));
        ArrayList<XearoHelper.LineData> arrayList2 = new ArrayList<XearoHelper.LineData>();
        double d = this.yUTSjfYE2q2du.player.getX();
        double d2 = this.yUTSjfYE2q2du.player.getZ();
        for (XearoHelper.WaypointData waypointData : arrayList) {
            arrayList2.add(new XearoHelper.LineData((int)d, (int)d2, waypointData.x(), waypointData.z()));
            d = waypointData.x();
            d2 = waypointData.z();
        }
        XearoHelper.get().drawLinesOnMap(arrayList2, -16733441);
    }

    private void mp3zoXQFKUKYj5(XearoHelper.WaypointData waypointData) {
        ChatUtils.info((String)(String.valueOf(Formatting.field_1065) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Flying to next waypoint: " + String.valueOf(Formatting.AQUA) + waypointData.name()), (Object[])new Object[0]);
        PathingHelper.UgB10d(waypointData.x(), waypointData.z());
    }

    private float Gt56Sj4a6BWhgB(XearoHelper.WaypointData waypointData) {
        double d = (double)waypointData.x() - this.yUTSjfYE2q2du.player.getX();
        double d2 = (double)waypointData.z() - this.yUTSjfYE2q2du.player.getZ();
        return (float)Math.toDegrees(Math.atan2(-d, d2));
    }

    private float jOdDDFXSeWl4(float f, float f2) {
        float f3 = (f - f2) % 360.0f;
        if (f3 > 180.0f) {
            f3 -= 360.0f;
        }
        if (f3 < -180.0f) {
            f3 += 360.0f;
        }
        return f3;
    }

    public static final class FlightMode
    extends Enum<FlightMode> {
        public static final /* enum */ FlightMode TfF42oD7 = new FlightMode();
        public static final /* enum */ FlightMode kBQdZKStLMVDV = new FlightMode();
        public static final /* enum */ FlightMode XuSVOP3J5xFv = new FlightMode();
        private static final /* synthetic */ FlightMode[] Y1fGfDLuV;

        public static FlightMode[] values() {
            return (FlightMode[])Y1fGfDLuV.clone();
        }

        public static FlightMode valueOf(String string) {
            return Enum.valueOf(FlightMode.class, string);
        }

        private static /* synthetic */ FlightMode[] zSBNh5WZFCR() {
            return new FlightMode[]{TfF42oD7, kBQdZKStLMVDV, XuSVOP3J5xFv};
        }

        static {
            Y1fGfDLuV = FlightMode.zSBNh5WZFCR();
        }
    }
}

