// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
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
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.compat.XearoHelper;
import musheor.modules.features.KekFly;
import musheor.utils.internal.PathingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

/**
 * "kekpoint-follower" — automatically flies to Xaero's-map waypoints (oldest first,
 * deleting each on arrival) using one of three modes: Baritone Elytra (overworld/nether
 * only), KekFly rocket flight, or plain yaw-steering. Optionally draws the planned path
 * on Xaero's minimap.
 */
public class WaypointFollower extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance(); // was: FvaNWO
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: Q90GLXQ0Pef

    private final Setting<FlightMode> flightMode = sgGeneral.add(new EnumSetting.Builder<FlightMode>() // was: psJq59YIbp3Z
        .name("flight-mode").description("Which type of flight mode to use").defaultValue(FlightMode.ROCKET_FLY).build());
    private final Setting<Boolean> drawPath = sgGeneral.add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("draw-path").description("Draws a path between waypoints on Xearo's map").defaultValue(true)
        .visible(() -> XearoHelper.isLoaded() && flightMode.get() != FlightMode.BARITONE_ELYTRA).build());
    private final Setting<Integer> arrivalRadius = sgGeneral.add(new IntSetting.Builder() // was: rKbT3Ifwo
        .name("arrival-radius").description("XZ distance to a waypoint at which it is considered reached.").defaultValue(32).sliderRange(8, 256).build());
    private final Setting<Double> degreesPerTick = sgGeneral.add(new DoubleSetting.Builder() // was: r7hOYIKN2
        .name("degrees-per-tick").description("Max yaw rotation per tick — limits how sharply the player turns toward a waypoint.")
        .defaultValue(3.0).sliderRange(0.5, 20.0).decimalPlaces(1)
        .visible(() -> flightMode.get() == FlightMode.ROCKET_FLY || flightMode.get() == FlightMode.MANUAL).build());

    private XearoHelper.WaypointData currentWaypoint = null; // was: oZHMlTL
    private int lastWaypointCount = -1;                       // was: xQr5FhbwpQPWgIQ

    public WaypointFollower() {
        super(musheor.AUTOMATION, "kekpoint-follower", "Automatically follow waypoints in all dimensions using different flight modes.");
    }

    @Override
    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) return;
        Dimension currentDim = PlayerUtils.getDimension();
        if (this.flightMode.get() == FlightMode.BARITONE_ELYTRA && (currentDim == Dimension.End || currentDim == Dimension.Overworld)) {
            Formatting format = currentDim == Dimension.End ? Formatting.DARK_PURPLE : Formatting.RED;
            musheor.utils.PlayerUtils.setModuleSetting(WaypointFollower.class, "flight-mode", FlightMode.ROCKET_FLY);
            ChatUtils.info(Formatting.GOLD + "[WaypointFollower] " + Formatting.WHITE + "Automatically switching to RocketFly: "
                + format + currentDim.name() + Formatting.WHITE + " does not support Baritone Elytra Flight.");
            this.toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.currentWaypoint != null) {
            PathingHelper.cancelEverything();
            this.currentWaypoint = null;
        }
        if (XearoHelper.isLoaded()) {
            XearoHelper.get().clearLinesOnMap();
        }
        this.lastWaypointCount = -1;
        KekFly fly = (KekFly) Modules.get().get(KekFly.class);
        if (fly.isActive() && this.flightMode.get() == FlightMode.ROCKET_FLY) {
            fly.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        switch (this.flightMode.get()) {
            case BARITONE_ELYTRA -> this.tickBaritoneElytra();
            case ROCKET_FLY -> this.tickRocketFly();
            case MANUAL -> this.tickManual();
        }
        if (this.drawPath.get() && this.flightMode.get() != FlightMode.BARITONE_ELYTRA) {
            this.drawPathOnMap();
        }
    }

    private void tickBaritoneElytra() { // was: FvaNWO()
        if (this.currentWaypoint == null) {
            this.currentWaypoint = XearoHelper.get().getOldestWaypoint(true);
            if (this.currentWaypoint != null) this.startFlyingTo(this.currentWaypoint);
        } else if (XearoHelper.get().distanceToWaypoint(this.currentWaypoint) < this.arrivalRadius.get()) {
            PathingHelper.cancelEverything();
            ChatUtils.info(Formatting.GREEN + "Arrived at " + Formatting.YELLOW + this.currentWaypoint.name() + Formatting.GREEN + ", moving to next...");
            XearoHelper.get().deleteCurrentWaypoint(this.currentWaypoint);
            this.currentWaypoint = null;
        }
    }

    private void tickRocketFly() { // was: Q90GLXQ0Pef()
        if (!((KekFly) Modules.get().get(KekFly.class)).isActive()) return;
        if (this.currentWaypoint == null) {
            this.currentWaypoint = XearoHelper.get().getOldestWaypoint(true);
            if (this.currentWaypoint == null) return;
            ChatUtils.info(Formatting.GOLD + "[WaypointFollower] " + Formatting.WHITE + "Flying to: " + Formatting.YELLOW + this.currentWaypoint.name());
        }
        this.steerToward(this.currentWaypoint);
        if (XearoHelper.get().distanceToWaypoint(this.currentWaypoint) < this.arrivalRadius.get()) {
            ChatUtils.info(Formatting.GOLD + "[WaypointFollower] " + Formatting.WHITE + "Arrived at " + Formatting.YELLOW + this.currentWaypoint.name() + Formatting.WHITE + ", moving to next...");
            XearoHelper.get().deleteCurrentWaypoint(this.currentWaypoint);
            this.currentWaypoint = null;
        }
    }

    private void tickManual() { // was: psJq59YIbp3Z()
        if (this.currentWaypoint == null) {
            this.currentWaypoint = XearoHelper.get().getOldestWaypoint(true);
            if (this.currentWaypoint == null) return;
            ChatUtils.info(Formatting.GOLD + "[WaypointFollower] " + Formatting.WHITE + "Flying to: " + Formatting.YELLOW + this.currentWaypoint.name());
        }
        this.steerToward(this.currentWaypoint);
        if (XearoHelper.get().distanceToWaypoint(this.currentWaypoint) < this.arrivalRadius.get()) {
            ChatUtils.info(Formatting.GOLD + "[WaypointFollower] " + Formatting.WHITE + "Arrived at " + Formatting.YELLOW + this.currentWaypoint.name() + Formatting.WHITE + ", moving to next...");
            XearoHelper.get().deleteCurrentWaypoint(this.currentWaypoint);
            this.currentWaypoint = null;
        }
    }

    /** Rotates the player's yaw toward {@code wp}, capped at degrees-per-tick. */
    private void steerToward(XearoHelper.WaypointData wp) {
        float desiredYaw = this.yawTo(wp);
        float diff = this.yawDifference(this.mc.player.getYaw(), desiredYaw);
        float maxRot = this.degreesPerTick.get().floatValue();
        if (Math.abs(diff) <= maxRot) {
            this.mc.player.setYaw(desiredYaw);
        } else {
            this.mc.player.setYaw(this.mc.player.getYaw() - Math.signum(diff) * maxRot);
        }
    }

    private void drawPathOnMap() { // was: SOYyh5IPg26f7F()
        if (!XearoHelper.isLoaded()) return;
        List<XearoHelper.WaypointData> wps = XearoHelper.get().getWaypoints(true);
        if (wps == null || wps.size() == this.lastWaypointCount) return;
        this.lastWaypointCount = wps.size();
        if (wps.isEmpty()) {
            XearoHelper.get().clearLinesOnMap();
            return;
        }
        List<XearoHelper.WaypointData> sorted = new ArrayList<>(wps);
        sorted.sort(Comparator.comparingLong(XearoHelper.WaypointData::createdAt));
        List<XearoHelper.LineData> lines = new ArrayList<>();
        double fromX = this.mc.player.getX();
        double fromZ = this.mc.player.getZ();
        for (XearoHelper.WaypointData wp : sorted) {
            lines.add(new XearoHelper.LineData((int) fromX, (int) fromZ, wp.x(), wp.z()));
            fromX = wp.x();
            fromZ = wp.z();
        }
        XearoHelper.get().drawLinesOnMap(lines, -16733441);
    }

    private void startFlyingTo(XearoHelper.WaypointData wp) { // was: FvaNWO(WaypointData)
        ChatUtils.info(Formatting.GOLD + "[WaypointFollower] " + Formatting.WHITE + "Flying to next waypoint: " + Formatting.GRAY + wp.name());
        PathingHelper.elytraTo(wp.x(), wp.z());
    }

    /** Yaw (degrees) pointing from the player toward the waypoint. */
    private float yawTo(XearoHelper.WaypointData wp) { // was: Q90GLXQ0Pef(WaypointData)
        double dx = wp.x() - this.mc.player.getX();
        double dz = wp.z() - this.mc.player.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    /** Signed shortest angular difference between two yaws, in [-180, 180]. */
    private float yawDifference(float a, float b) { // was: FvaNWO(float,float)
        float d = (a - b) % 360.0F;
        if (d > 180.0F) d -= 360.0F;
        if (d < -180.0F) d += 360.0F;
        return d;
    }

    /** Flight mode. */ // was: enum FlightMode {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum FlightMode { BARITONE_ELYTRA, ROCKET_FLY, MANUAL }
}
