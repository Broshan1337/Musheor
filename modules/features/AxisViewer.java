// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.util.math.Vec3d;

public class AxisViewer
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgOverworld;
    private final SettingGroup sgNether;
    private final SettingGroup sgEnd;
    private final Setting<Integer> renderRange;
    private final Setting<Boolean> renderRingRoads;
    private final Setting<Boolean> renderDiamondHighways;
    private final Setting<Boolean> renderGrid;
    private final Setting<AxisType> overworldAxisTypes;
    private final Setting<Integer> overworldY;
    private final Setting<SettingColor> overworldColor;
    private final Setting<AxisType> netherAxisTypes;
    private final Setting<Integer> netherY;
    private final Setting<SettingColor> netherColor;
    private final Setting<AxisType> endAxisTypes;
    private final Setting<Integer> endY;
    private final Setting<SettingColor> endColor;
    private final List<Double> selaO6lwe7;
    private final List<Double> l3ot1CwoJ9CsS;

    public AxisViewer() {
        super(musheor.MAIN, "axis-viewer", "Render highway axis'.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgOverworld = this.settings.createGroup("Overworld");
        this.sgNether = this.settings.createGroup("Nether");
        this.sgEnd = this.settings.createGroup("End");
        this.renderRange = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-range")).description("How far the lines should extend from the player.")).defaultValue((Object)128)).min(8).sliderMax(256).build());
        this.renderRingRoads = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-ring-roads")).description("Display square ring roads at specific intervals.")).defaultValue((Object)true)).build());
        this.renderDiamondHighways = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-diamond-highways")).description("Display diamond-shaped highways connecting cardinals.")).defaultValue((Object)true)).build());
        this.renderGrid = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-grid")).description("Display grid highways every 5000 blocks.")).defaultValue((Object)true)).build());
        this.overworldAxisTypes = this.sgOverworld.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("render")).description("Which axes to display.")).defaultValue((Object)AxisType.eQlnaotm4pUDUmJT)).build());
        this.overworldY = this.sgOverworld.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("height")).defaultValue((Object)63)).sliderMin(-64).sliderMax(319).visible(() -> this.overworldAxisTypes.get() != AxisType.eQlnaotm4pUDUmJT)).build());
        this.overworldColor = this.sgOverworld.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The line color.")).defaultValue(new SettingColor(25, 25, 225, 255)).visible(() -> this.overworldAxisTypes.get() != AxisType.eQlnaotm4pUDUmJT)).build());
        this.netherAxisTypes = this.sgNether.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("render")).description("Which axes to display.")).defaultValue((Object)AxisType.btLCQHvKVR)).build());
        this.netherY = this.sgNether.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("height")).defaultValue((Object)120)).sliderMin(0).sliderMax(255).visible(() -> this.netherAxisTypes.get() != AxisType.eQlnaotm4pUDUmJT)).build());
        this.netherColor = this.sgNether.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The line color.")).defaultValue(new SettingColor(225, 25, 25, 255)).visible(() -> this.netherAxisTypes.get() != AxisType.eQlnaotm4pUDUmJT)).build());
        this.endAxisTypes = this.sgEnd.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("render")).description("Which axes to display.")).defaultValue((Object)AxisType.eQlnaotm4pUDUmJT)).build());
        this.endY = this.sgEnd.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("height")).defaultValue((Object)64)).sliderMin(0).sliderMax(255).visible(() -> this.endAxisTypes.get() != AxisType.eQlnaotm4pUDUmJT)).build());
        this.endColor = this.sgEnd.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The line color.")).defaultValue(new SettingColor(225, 25, 25, 255)).visible(() -> this.endAxisTypes.get() != AxisType.eQlnaotm4pUDUmJT)).build());
        this.selaO6lwe7 = List.of(500.0, 1000.0, 1500.0, 2000.0, 2500.0, 7500.5, 55000.0, 62500.0, 100000.0, 125000.0, 250000.0, 500000.0, 750000.0, 1000000.0, 1250000.0, 1875000.0, 2500000.0, 3750000.0);
        this.l3ot1CwoJ9CsS = List.of(Double.valueOf(2500.0), Double.valueOf(5000.0), Double.valueOf(25000.0), Double.valueOf(50000.0), Double.valueOf(125000.0), Double.valueOf(250000.0), Double.valueOf(500000.0), Double.valueOf(3750000.0));
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
        int n;
        AxisType axisType;
        if (this.mc.options.spectator || this.mc.player == null || this.mc.world == null) {
            return;
        }
        Color color = switch (PlayerUtils.getDimension()) {
            case Dimension.Overworld -> {
                axisType = (AxisType)((Object)this.overworldAxisTypes.get());
                n = (Integer)this.overworldY.get();
                yield (Color)this.overworldColor.get();
            }
            case Dimension.Nether -> {
                axisType = (AxisType)((Object)this.netherAxisTypes.get());
                n = (Integer)this.netherY.get();
                yield (Color)this.netherColor.get();
            }
            case Dimension.End -> {
                axisType = (AxisType)((Object)this.endAxisTypes.get());
                n = (Integer)this.endY.get();
                yield (Color)this.endColor.get();
            }
            default -> throw new IllegalStateException("Unexpected value: " + String.valueOf(PlayerUtils.getDimension()));
        };
        if (axisType == AxisType.eQlnaotm4pUDUmJT) {
            return;
        }
        double d = n;
        double d2 = ((Integer)this.renderRange.get()).intValue();
        double d3 = d2 * d2;
        double d4 = this.mc.player.getX();
        double d5 = this.mc.player.getZ();
        if (axisType.ni1UVTBDGbU3()) {
            double d6;
            double d7 = d5 * d5;
            if (d7 < d3) {
                d6 = Math.sqrt(d3 - d7);
                this.jOdDDFXSeWl4(render3DEvent, new Vec3d(d4 - d6, d, 0.0), new Vec3d(d4 + d6, d, 0.0), color);
            }
            if ((d6 = d4 * d4) < d3) {
                double d8 = Math.sqrt(d3 - d6);
                this.jOdDDFXSeWl4(render3DEvent, new Vec3d(0.0, d, d5 - d8), new Vec3d(0.0, d, d5 + d8), color);
            }
        }
        if (axisType.sdcDUaa()) {
            this.jOdDDFXSeWl4(render3DEvent, d4, d5, d, d2, true, color);
            this.jOdDDFXSeWl4(render3DEvent, d4, d5, d, d2, false, color);
        }
        if (((Boolean)this.renderRingRoads.get()).booleanValue()) {
            for (double d9 : this.selaO6lwe7) {
                this.jOdDDFXSeWl4(render3DEvent, d4, d5, -d9, true, d, d2, color);
                this.jOdDDFXSeWl4(render3DEvent, d4, d5, d9, true, d, d2, color);
                this.jOdDDFXSeWl4(render3DEvent, d4, d5, -d9, false, d, d2, color);
                this.jOdDDFXSeWl4(render3DEvent, d4, d5, d9, false, d, d2, color);
            }
        }
        if (((Boolean)this.renderGrid.get()).booleanValue()) {
            this.jOdDDFXSeWl4(render3DEvent, d4, d5, d, d2, color);
        }
        if (((Boolean)this.renderDiamondHighways.get()).booleanValue()) {
            for (double d9 : this.l3ot1CwoJ9CsS) {
                this.jOdDDFXSeWl4(render3DEvent, d4, d5, d9, true, true, d, d2, color);
                this.jOdDDFXSeWl4(render3DEvent, d4, d5, -d9, false, false, d, d2, color);
                this.jOdDDFXSeWl4(render3DEvent, d4, d5, -d9, true, false, d, d2, color);
                this.jOdDDFXSeWl4(render3DEvent, d4, d5, d9, false, true, d, d2, color);
            }
        }
    }

    private void jOdDDFXSeWl4(Render3DEvent render3DEvent, double d, double d2, double d3, double d4, boolean bl, Color color) {
        double d5;
        double d6;
        double d7 = bl ? (d + d2) / 2.0 : (d - d2) / 2.0;
        double d8 = d - d7;
        double d9 = d8 * d8 + (d6 = d2 - (d5 = bl ? d7 : -d7)) * d6;
        if (d9 < d4 * d4) {
            double d10 = Math.sqrt(d4 * d4 - d9);
            double d11 = d10 / Math.sqrt(2.0);
            this.jOdDDFXSeWl4(render3DEvent, new Vec3d(d7 - d11, d3, d5 - (bl ? d11 : -d11)), new Vec3d(d7 + d11, d3, d5 + (bl ? d11 : -d11)), color);
        }
    }

    private void jOdDDFXSeWl4(Render3DEvent render3DEvent, double d, double d2, double d3, double d4, Color color) {
        double d5 = d4 * d4;
        for (int i = 1; i <= 10; ++i) {
            double d6 = (double)i * 5000.0 + 0.5;
            for (double d7 : new double[]{d6, -d6}) {
                double d8;
                double d9;
                double d10;
                double d11;
                double d12 = (d2 - d7) * (d2 - d7);
                if (d12 < d5 && (d11 = Math.max(-50000.5, d - (d10 = Math.sqrt(d5 - d12)))) < (d9 = Math.min(50000.5, d + d10))) {
                    this.jOdDDFXSeWl4(render3DEvent, new Vec3d(d11, d3, d7), new Vec3d(d9, d3, d7), color);
                }
                if (!((d10 = (d - d7) * (d - d7)) < d5) || !((d9 = Math.max(-50000.5, d2 - (d11 = Math.sqrt(d5 - d10)))) < (d8 = Math.min(50000.5, d2 + d11)))) continue;
                this.jOdDDFXSeWl4(render3DEvent, new Vec3d(d7, d3, d9), new Vec3d(d7, d3, d8), color);
            }
        }
    }

    private void jOdDDFXSeWl4(Render3DEvent render3DEvent, double d, double d2, double d3, boolean bl, double d4, double d5, Color color) {
        double d6;
        double d7 = d6 = bl ? Math.pow(d2 - d3, 2.0) : Math.pow(d - d3, 2.0);
        if (d6 < d5 * d5) {
            double d8 = Math.sqrt(d5 * d5 - d6);
            double d9 = Math.abs(d3);
            if (bl) {
                double d10;
                double d11 = Math.max(-d9, d - d8);
                if (d11 < (d10 = Math.min(d9, d + d8))) {
                    this.jOdDDFXSeWl4(render3DEvent, new Vec3d(d11, d4, d3), new Vec3d(d10, d4, d3), color);
                }
            } else {
                double d12;
                double d13 = Math.max(-d9, d2 - d8);
                if (d13 < (d12 = Math.min(d9, d2 + d8))) {
                    this.jOdDDFXSeWl4(render3DEvent, new Vec3d(d3, d4, d13), new Vec3d(d3, d4, d12), color);
                }
            }
        }
    }

    private void jOdDDFXSeWl4(Render3DEvent render3DEvent, double d, double d2, double d3, boolean bl, boolean bl2, double d4, double d5, Color color) {
        double d6;
        double d7;
        double d8 = bl ? (d - d2 + d3) / 2.0 : (d + d2 + d3) / 2.0;
        double d9 = d - d8;
        double d10 = d9 * d9 + (d7 = d2 - (d6 = bl ? (d2 - d + d3) / 2.0 : (d2 + d - d3) / 2.0)) * d7;
        if (d10 < d5 * d5) {
            double d11;
            double d12 = Math.sqrt(d5 * d5 - d10);
            double d13 = d12 / Math.sqrt(2.0);
            double d14 = d8 - d13;
            double d15 = bl ? d3 - d14 : d14 - d3;
            double d16 = d8 + d13;
            double d17 = bl ? d3 - d16 : d16 - d3;
            double d18 = bl2 ? 0.0 : -Math.abs(d3);
            double d19 = bl2 ? Math.abs(d3) : 0.0;
            double d20 = Math.max(d18, Math.min(d19, d14));
            double d21 = Math.max(d18, Math.min(d19, d16));
            double d22 = bl ? d3 - d20 : d20 - d3;
            double d23 = d11 = bl ? d3 - d21 : d21 - d3;
            if (Math.abs(d20 - d21) > 0.01) {
                this.jOdDDFXSeWl4(render3DEvent, new Vec3d(d20, d4, d22), new Vec3d(d21, d4, d11), color);
            }
        }
    }

    private void jOdDDFXSeWl4(Render3DEvent render3DEvent, Vec3d Vec3d2, Vec3d Vec3d3, Color color) {
        render3DEvent.renderer.line(Vec3d2.getX(), Vec3d2.getY(), Vec3d2.getZ(), Vec3d3.getX(), Vec3d3.getY(), Vec3d3.getZ(), color);
    }

    public static final class AxisType
    extends Enum<AxisType> {
        public static final /* enum */ AxisType btLCQHvKVR = new AxisType();
        public static final /* enum */ AxisType gBxN0D8GSyidOa = new AxisType();
        public static final /* enum */ AxisType PROcSc3gv = new AxisType();
        public static final /* enum */ AxisType eQlnaotm4pUDUmJT = new AxisType();
        private static final /* synthetic */ AxisType[] gkoa4kDDOuwRuB64;

        public static AxisType[] values() {
            return (AxisType[])gkoa4kDDOuwRuB64.clone();
        }

        public static AxisType valueOf(String string) {
            return Enum.valueOf(AxisType.class, string);
        }

        boolean ni1UVTBDGbU3() {
            return this == btLCQHvKVR || this == gBxN0D8GSyidOa;
        }

        boolean sdcDUaa() {
            return this == btLCQHvKVR || this == PROcSc3gv;
        }

        private static /* synthetic */ AxisType[] keJiIXfi6Ivt7zUB() {
            return new AxisType[]{btLCQHvKVR, gBxN0D8GSyidOa, PROcSc3gv, eQlnaotm4pUDUmJT};
        }

        static {
            gkoa4kDDOuwRuB64 = AxisType.keJiIXfi6Ivt7zUB();
        }
    }
}

