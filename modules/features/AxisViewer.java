// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.features;

import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.util.math.Vec3d;

/**
 * "axis-viewer" — renders the 2b2t highway network as lines around the player: the cardinal
 * axes, the two 45° diagonals, square ring roads, the 5k grid, and diamond highways. Axis
 * type, render height and colour are configurable per dimension. All geometry is clipped to
 * a spherical {@code render-range} around the player.
 */
public class AxisViewer extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();     // was: FvaNWO
    private final SettingGroup sgOverworld = this.settings.createGroup("Overworld"); // was: Q90GLXQ0Pef
    private final SettingGroup sgNether = this.settings.createGroup("Nether");   // was: psJq59YIbp3Z
    private final SettingGroup sgEnd = this.settings.createGroup("End");         // was: SOYyh5IPg26f7F

    private final Setting<Integer> renderRange = sgGeneral.add(new IntSetting.Builder() // was: rKbT3Ifwo
        .name("render-range").description("How far the lines should extend from the player.").defaultValue(128).min(8).sliderMax(256).build());
    private final Setting<Boolean> renderRingRoads = sgGeneral.add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("render-ring-roads").description("Display square ring roads at specific intervals.").defaultValue(true).build());
    private final Setting<Boolean> renderDiamondHighways = sgGeneral.add(new BoolSetting.Builder() // was: oZHMlTL
        .name("render-diamond-highways").description("Display diamond-shaped highways connecting cardinals.").defaultValue(true).build());
    private final Setting<Boolean> renderGrid = sgGeneral.add(new BoolSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("render-grid").description("Display grid highways every 5000 blocks.").defaultValue(true).build());

    private final Setting<AxisType> overworldAxis = sgOverworld.add(new EnumSetting.Builder<AxisType>() // was: OMMZL1F3q
        .name("render").description("Which axes to display.").defaultValue(AxisType.OFF).build());
    private final Setting<Integer> overworldHeight = sgOverworld.add(new IntSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("height").defaultValue(63).sliderMin(-64).sliderMax(319).visible(() -> overworldAxis.get() != AxisType.OFF).build());
    private final Setting<SettingColor> overworldColor = sgOverworld.add(new ColorSetting.Builder() // was: krxNb5lcQuWA
        .name("color").description("The line color.").defaultValue(new SettingColor(25, 25, 225, 255)).visible(() -> overworldAxis.get() != AxisType.OFF).build());

    private final Setting<AxisType> netherAxis = sgNether.add(new EnumSetting.Builder<AxisType>() // was: nt0HZnvBBp
        .name("render").description("Which axes to display.").defaultValue(AxisType.BOTH).build());
    private final Setting<Integer> netherHeight = sgNether.add(new IntSetting.Builder() // was: amz3UB1vE
        .name("height").defaultValue(120).sliderMin(0).sliderMax(255).visible(() -> netherAxis.get() != AxisType.OFF).build());
    private final Setting<SettingColor> netherColor = sgNether.add(new ColorSetting.Builder() // was: sBBIyQG5NWq0K
        .name("color").description("The line color.").defaultValue(new SettingColor(225, 25, 25, 255)).visible(() -> netherAxis.get() != AxisType.OFF).build());

    private final Setting<AxisType> endAxis = sgEnd.add(new EnumSetting.Builder<AxisType>() // was: sZkZ1izAy
        .name("render").description("Which axes to display.").defaultValue(AxisType.OFF).build());
    private final Setting<Integer> endHeight = sgEnd.add(new IntSetting.Builder() // was: QYKUhjp
        .name("height").defaultValue(64).sliderMin(0).sliderMax(255).visible(() -> endAxis.get() != AxisType.OFF).build());
    private final Setting<SettingColor> endColor = sgEnd.add(new ColorSetting.Builder() // was: NIz4xic3Js9
        .name("color").description("The line color.").defaultValue(new SettingColor(225, 25, 25, 255)).visible(() -> endAxis.get() != AxisType.OFF).build());

    private final List<Double> ringRoadDistances = List.of( // was: u1WFwbQRSKa
        500.0, 1000.0, 1500.0, 2000.0, 2500.0, 7500.5, 55000.0, 62500.0, 100000.0, 125000.0,
        250000.0, 500000.0, 750000.0, 1000000.0, 1250000.0, 1875000.0, 2500000.0, 3750000.0);
    private final List<Double> diamondDistances = List.of(2500.0, 5000.0, 25000.0, 50000.0, 125000.0, 250000.0, 500000.0, 3750000.0); // was: LGDfbZq

    public AxisViewer() {
        super(musheor.MAIN, "axis-viewer", "Render highway axis'.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        if (this.mc.options.hudHidden || this.mc.player == null || this.mc.world == null) return;
        AxisType axisType;
        int y;
        Color lineColor;
        switch (PlayerUtils.getDimension()) {
            case Overworld -> { axisType = this.overworldAxis.get(); y = this.overworldHeight.get(); lineColor = this.overworldColor.get(); }
            case Nether -> { axisType = this.netherAxis.get(); y = this.netherHeight.get(); lineColor = this.netherColor.get(); }
            case End -> { axisType = this.endAxis.get(); y = this.endHeight.get(); lineColor = this.endColor.get(); }
            default -> throw new IllegalStateException("Unexpected value: " + PlayerUtils.getDimension());
        }
        if (axisType == AxisType.OFF) return;

        double renderY = y;
        double range = this.renderRange.get();
        double rangeSq = range * range;
        double px = this.mc.player.getX();
        double pz = this.mc.player.getZ();

        if (axisType.includesCardinal()) {
            double distToLineSqX = pz * pz;
            if (distToLineSqX < rangeSq) {
                double offset = Math.sqrt(rangeSq - distToLineSqX);
                this.drawLine(event, new Vec3d(px - offset, renderY, 0.0), new Vec3d(px + offset, renderY, 0.0), lineColor);
            }
            double distToLineSqZ = px * px;
            if (distToLineSqZ < rangeSq) {
                double offset = Math.sqrt(rangeSq - distToLineSqZ);
                this.drawLine(event, new Vec3d(0.0, renderY, pz - offset), new Vec3d(0.0, renderY, pz + offset), lineColor);
            }
        }

        if (axisType.includesDiagonal()) {
            this.renderCardinalDiagonal(event, px, pz, renderY, range, true, lineColor);
            this.renderCardinalDiagonal(event, px, pz, renderY, range, false, lineColor);
        }

        if (this.renderRingRoads.get()) {
            for (double dist : this.ringRoadDistances) {
                this.renderRingRoad(event, px, pz, -dist, true, renderY, range, lineColor);
                this.renderRingRoad(event, px, pz, dist, true, renderY, range, lineColor);
                this.renderRingRoad(event, px, pz, -dist, false, renderY, range, lineColor);
                this.renderRingRoad(event, px, pz, dist, false, renderY, range, lineColor);
            }
        }

        if (this.renderGrid.get()) {
            this.renderGrid(event, px, pz, renderY, range, lineColor);
        }

        if (this.renderDiamondHighways.get()) {
            for (double dist : this.diamondDistances) {
                this.renderDiamond(event, px, pz, dist, true, true, renderY, range, lineColor);
                this.renderDiamond(event, px, pz, -dist, false, false, renderY, range, lineColor);
                this.renderDiamond(event, px, pz, -dist, true, false, renderY, range, lineColor);
                this.renderDiamond(event, px, pz, dist, false, true, renderY, range, lineColor);
            }
        }
    }

    /** Draws one of the two 45° diagonals (X=Z or X=-Z) clipped to the render sphere. */
    private void renderCardinalDiagonal(Render3DEvent event, double px, double pz, double y, double range, boolean isPositive, Color color) { // was: FvaNWO(...,boolean,Color)
        double cx = isPositive ? (px + pz) / 2.0 : (px - pz) / 2.0;
        double cz = isPositive ? cx : -cx;
        double dx = px - cx;
        double dz = pz - cz;
        double distToLineSq = dx * dx + dz * dz;
        if (distToLineSq < range * range) {
            double halfChord = Math.sqrt(range * range - distToLineSq);
            double spread = halfChord / Math.sqrt(2.0);
            this.drawLine(event, new Vec3d(cx - spread, y, cz - (isPositive ? spread : -spread)),
                new Vec3d(cx + spread, y, cz + (isPositive ? spread : -spread)), color);
        }
    }

    /** Draws the 5k grid lines (clamped to ±50000.5) clipped to the render sphere. */
    private void renderGrid(Render3DEvent event, double px, double pz, double y, double range, Color color) { // was: FvaNWO(event,double,double,double,double,Color)
        double rangeSq = range * range;
        for (int i = 1; i <= 10; i++) {
            double coord = i * 5000.0 + 0.5;
            for (double c : new double[]{coord, -coord}) {
                double distZSq = (pz - c) * (pz - c);
                if (distZSq < rangeSq) {
                    double offset = Math.sqrt(rangeSq - distZSq);
                    double startX = Math.max(-50000.5, px - offset);
                    double endX = Math.min(50000.5, px + offset);
                    if (startX < endX) this.drawLine(event, new Vec3d(startX, y, c), new Vec3d(endX, y, c), color);
                }
                double distXSq = (px - c) * (px - c);
                if (distXSq < rangeSq) {
                    double offset = Math.sqrt(rangeSq - distXSq);
                    double startZ = Math.max(-50000.5, pz - offset);
                    double endZ = Math.min(50000.5, pz + offset);
                    if (startZ < endZ) this.drawLine(event, new Vec3d(c, y, startZ), new Vec3d(c, y, endZ), color);
                }
            }
        }
    }

    /** Draws a square ring-road segment at {@code coord} (horizontal or vertical), clamped to its extent. */
    private void renderRingRoad(Render3DEvent event, double px, double pz, double coord, boolean isHorizontal, double y, double range, Color color) { // was: FvaNWO(...,boolean,double,double,Color)
        double distToLineSq = isHorizontal ? Math.pow(pz - coord, 2.0) : Math.pow(px - coord, 2.0);
        if (distToLineSq < range * range) {
            double offset = Math.sqrt(range * range - distToLineSq);
            double limit = Math.abs(coord);
            if (isHorizontal) {
                double startX = Math.max(-limit, px - offset);
                double endX = Math.min(limit, px + offset);
                if (startX < endX) this.drawLine(event, new Vec3d(startX, y, coord), new Vec3d(endX, y, coord), color);
            } else {
                double startZ = Math.max(-limit, pz - offset);
                double endZ = Math.min(limit, pz + offset);
                if (startZ < endZ) this.drawLine(event, new Vec3d(coord, y, startZ), new Vec3d(coord, y, endZ), color);
            }
        }
    }

    /** Draws a diamond-highway segment (X±Z=K) clipped to the render sphere and the correct quadrant. */
    private void renderDiamond(Render3DEvent event, double px, double pz, double K, boolean isSum, boolean xMustBePositive, double y, double range, Color color) { // was: FvaNWO(...,boolean,boolean,double,double,Color)
        double cx = isSum ? (px - pz + K) / 2.0 : (px + pz + K) / 2.0;
        double cz = isSum ? (pz - px + K) / 2.0 : (pz + px - K) / 2.0;
        double dx = px - cx;
        double dz = pz - cz;
        double distSq = dx * dx + dz * dz;
        if (distSq < range * range) {
            double halfChord = Math.sqrt(range * range - distSq);
            double spread = halfChord / Math.sqrt(2.0);
            double x1 = cx - spread;
            double x2 = cx + spread;
            double lowerX = xMustBePositive ? 0.0 : -Math.abs(K);
            double upperX = xMustBePositive ? Math.abs(K) : 0.0;
            double finalX1 = Math.max(lowerX, Math.min(upperX, x1));
            double finalX2 = Math.max(lowerX, Math.min(upperX, x2));
            double finalZ1 = isSum ? K - finalX1 : finalX1 - K;
            double finalZ2 = isSum ? K - finalX2 : finalX2 - K;
            if (Math.abs(finalX1 - finalX2) > 0.01) {
                this.drawLine(event, new Vec3d(finalX1, y, finalZ1), new Vec3d(finalX2, y, finalZ2), color);
            }
        }
    }

    private void drawLine(Render3DEvent event, Vec3d start, Vec3d end, Color color) { // was: FvaNWO(event,Vec3d,Vec3d,Color)
        event.renderer.line(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), color);
    }

    /** Which highway axes to render. */ // was: enum AxisType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z, SOYyh5IPg26f7F}
    public enum AxisType {
        BOTH, CARDINAL, DIAGONAL, OFF;

        boolean includesCardinal() { return this == BOTH || this == CARDINAL; } // was: FvaNWO()
        boolean includesDiagonal() { return this == BOTH || this == DIAGONAL; } // was: Q90GLXQ0Pef()
    }
}
