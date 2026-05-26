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
import net.minecraft.util.Formatting;
import net.minecraft.client.MinecraftClient;

public class WaypointFollower
extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<FlightMode> flightMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("flight-mode")).description("Which type of flight mode to use")).defaultValue((Object)FlightMode.RocketFly)).build());
    private final Setting<Boolean> drawPath = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("draw-path")).description("Draws a path between waypoints on Xearo's map")).defaultValue((Object)true)).visible(() -> XearoHelper.isLoaded() && this.flightMode.get() != FlightMode.BaritoneElytra)).build());
    private final Setting<Integer> arrivalRadius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("arrival-radius")).description("XZ distance to a waypoint at which it is considered reached.")).defaultValue((Object)32)).sliderRange(8, 256).build());
    private final Setting<Double> degreesPerTick = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("degrees-per-tick")).description("Max yaw rotation per tick \u2014 limits how sharply the player turns toward a waypoint.")).defaultValue(3.0).sliderRange(0.5, 20.0).decimalPlaces(1).visible(() -> this.flightMode.get() == FlightMode.RocketFly || this.flightMode.get() == FlightMode.ElytraFly)).build());
    private XearoHelper.WaypointData currentWaypoint = null;
    private int lastWaypointCount = -1;

    public WaypointFollower() {
        super(musheor.AUTOMATION, "kekpoint-follower", "Automatically follow waypoints in all dimensions using different flight modes.");
    }

    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        Dimension dimension = meteordevelopment.meteorclient.utils.player.PlayerUtils.getDimension();
        if (this.flightMode.get() == FlightMode.BaritoneElytra && (dimension == Dimension.End || dimension == Dimension.Overworld)) {
            Formatting Formatting2 = dimension == Dimension.End ? Formatting.LIGHT_PURPLE : Formatting.RED;
            PlayerUtils.setModuleSetting(WaypointFollower.class, "flight-mode", FlightMode.RocketFly);
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Automatically switching to RocketFly: " + String.valueOf(Formatting2) + dimension.name() + String.valueOf(Formatting.GRAY) + " does not support Baritone Elytra Flight."), (Object[])new Object[0]);
            this.toggle();
            return;
        }
    }

    public void onDeactivate() {
        if (this.currentWaypoint != null) {
            PathingHelper.stopPathing();
            this.currentWaypoint = null;
        }
        if (XearoHelper.isLoaded()) {
            XearoHelper.get().clearLinesOnMap();
        }
        this.lastWaypointCount = -1;
        KekFly kekFly = (KekFly)Modules.get().get(KekFly.class);
        if (kekFly.isActive() && this.flightMode.get() == FlightMode.RocketFly) {
            kekFly.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        switch (((FlightMode)((Object)this.flightMode.get())).ordinal()) {
            case 0: {
                this.tickBaritoneMode();
                break;
            }
            case 1: {
                this.tickRocketFlyMode();
                break;
            }
            case 2: {
                this.tickElytraFlyMode();
            }
        }
        if (((Boolean)this.drawPath.get()).booleanValue() && this.flightMode.get() != FlightMode.BaritoneElytra) {
            this.refreshMapPath();
        }
    }

    private void tickBaritoneMode() {
        if (this.currentWaypoint == null) {
            this.currentWaypoint = XearoHelper.get().getOldestWaypoint(true);
            if (this.currentWaypoint == null) {
                return;
            }
            this.startElytraPathToWaypoint(this.currentWaypoint);
            return;
        }
        if (XearoHelper.get().distanceToWaypoint(this.currentWaypoint) < (double)((Integer)this.arrivalRadius.get()).intValue()) {
            PathingHelper.stopPathing();
            ChatUtils.info((String)(String.valueOf(Formatting.GREEN) + "Arrived at " + String.valueOf(Formatting.AQUA) + this.currentWaypoint.name() + String.valueOf(Formatting.GREEN) + ", moving to next..."), (Object[])new Object[0]);
            XearoHelper.get().deleteCurrentWaypoint(this.currentWaypoint);
            this.currentWaypoint = null;
        }
    }

    private void tickRocketFlyMode() {
        if (!((KekFly)Modules.get().get(KekFly.class)).isActive()) {
            return;
        }
        if (this.currentWaypoint == null) {
            this.currentWaypoint = XearoHelper.get().getOldestWaypoint(true);
            if (this.currentWaypoint == null) {
                return;
            }
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Flying to: " + String.valueOf(Formatting.AQUA) + this.currentWaypoint.name()), (Object[])new Object[0]);
        }
        float f = this.getYawToWaypoint(this.currentWaypoint);
        float f2 = this.normalizeAngleDiff(this.mc.player.getYaw(), f);
        float f3 = (float)((Double)this.degreesPerTick.get()).doubleValue();
        if (Math.abs(f2) <= f3) {
            this.mc.player.setYaw(f);
        } else {
            this.mc.player.setYaw(this.mc.player.getYaw() - Math.signum(f2) * f3);
        }
        if (XearoHelper.get().distanceToWaypoint(this.currentWaypoint) < (double)((Integer)this.arrivalRadius.get()).intValue()) {
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Arrived at " + String.valueOf(Formatting.AQUA) + this.currentWaypoint.name() + String.valueOf(Formatting.GRAY) + ", moving to next..."), (Object[])new Object[0]);
            XearoHelper.get().deleteCurrentWaypoint(this.currentWaypoint);
            this.currentWaypoint = null;
        }
    }

    private void tickElytraFlyMode() {
        if (this.currentWaypoint == null) {
            this.currentWaypoint = XearoHelper.get().getOldestWaypoint(true);
            if (this.currentWaypoint == null) {
                return;
            }
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Flying to: " + String.valueOf(Formatting.AQUA) + this.currentWaypoint.name()), (Object[])new Object[0]);
        }
        float f = this.getYawToWaypoint(this.currentWaypoint);
        float f2 = this.normalizeAngleDiff(this.mc.player.getYaw(), f);
        float f3 = (float)((Double)this.degreesPerTick.get()).doubleValue();
        if (Math.abs(f2) <= f3) {
            this.mc.player.setYaw(f);
        } else {
            this.mc.player.setYaw(this.mc.player.getYaw() - Math.signum(f2) * f3);
        }
        if (XearoHelper.get().distanceToWaypoint(this.currentWaypoint) < (double)((Integer)this.arrivalRadius.get()).intValue()) {
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Arrived at " + String.valueOf(Formatting.AQUA) + this.currentWaypoint.name() + String.valueOf(Formatting.GRAY) + ", moving to next..."), (Object[])new Object[0]);
            XearoHelper.get().deleteCurrentWaypoint(this.currentWaypoint);
            this.currentWaypoint = null;
        }
    }

    private void refreshMapPath() {
        if (!XearoHelper.isLoaded()) {
            return;
        }
        List<XearoHelper.WaypointData> list = XearoHelper.get().getWaypoints(true);
        if (list == null) {
            return;
        }
        if (list.size() == this.lastWaypointCount) {
            return;
        }
        this.lastWaypointCount = list.size();
        if (list.isEmpty()) {
            XearoHelper.get().clearLinesOnMap();
            return;
        }
        ArrayList<XearoHelper.WaypointData> arrayList = new ArrayList<XearoHelper.WaypointData>(list);
        arrayList.sort(Comparator.comparingLong(XearoHelper.WaypointData::createdAt));
        ArrayList<XearoHelper.LineData> arrayList2 = new ArrayList<XearoHelper.LineData>();
        double d = this.mc.player.getX();
        double d2 = this.mc.player.getZ();
        for (XearoHelper.WaypointData waypointData : arrayList) {
            arrayList2.add(new XearoHelper.LineData((int)d, (int)d2, waypointData.x(), waypointData.z()));
            d = waypointData.x();
            d2 = waypointData.z();
        }
        XearoHelper.get().drawLinesOnMap(arrayList2, -16733441);
    }

    private void startElytraPathToWaypoint(XearoHelper.WaypointData waypointData) {
        ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[WaypointFollower] " + String.valueOf(Formatting.GRAY) + "Flying to next waypoint: " + String.valueOf(Formatting.AQUA) + waypointData.name()), (Object[])new Object[0]);
        PathingHelper.startElytraPath(waypointData.x(), waypointData.z());
    }

    private float getYawToWaypoint(XearoHelper.WaypointData waypointData) {
        double d = (double)waypointData.x() - this.mc.player.getX();
        double d2 = (double)waypointData.z() - this.mc.player.getZ();
        return (float)Math.toDegrees(Math.atan2(-d, d2));
    }

    private float normalizeAngleDiff(float f, float f2) {
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
        public static final /* enum */ FlightMode BaritoneElytra = new FlightMode();
        public static final /* enum */ FlightMode RocketFly = new FlightMode();
        public static final /* enum */ FlightMode ElytraFly = new FlightMode();
        private static final /* synthetic */ FlightMode[] $VALUES;

        public static FlightMode[] values() {
            return (FlightMode[])$VALUES.clone();
        }

        public static FlightMode valueOf(String string) {
            return Enum.valueOf(FlightMode.class, string);
        }

        private static /* synthetic */ FlightMode[] zSBNh5WZFCR() {
            return new FlightMode[]{TfF42oD7, kBQdZKStLMVDV, XuSVOP3J5xFv};
        }

        static {
            $VALUES = FlightMode.zSBNh5WZFCR();
        }
    }
}

