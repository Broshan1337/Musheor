// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.hud;

import java.util.ArrayList;
import java.util.Locale;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import musheor.modules.features.CoordHider;
import musheor.musheor;
import net.minecraft.DimensionType;
import net.minecraft.MinecraftClient;

public class DimensionalPosition
extends HudElement {
    private final MinecraftClient Cd1WW3smp32e = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Boolean> showOppositeDimension = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-opposite-dimension")).description("Show relative position in opposite dimension.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> horizontal = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-horizontal")).description("Displays opposite dimension horizontally instead of vertically.")).defaultValue((Object)true)).visible(() -> this.showOppositeDimension.get())).build());
    private final Setting<Double> scale = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("text-scale")).description("Scale of the text.")).defaultValue(1.0).sliderRange(0.1, 3.0).decimalPlaces(1).build());
    private final Setting<Boolean> showDecimalPlaces = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-decimal-places")).description("Displays the decimal places of the coordinates.")).defaultValue((Object)true)).visible(() -> this.showOppositeDimension.get())).build());
    private final Setting<SettingColor> overworldColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("overworld-color")).description("Color for overworld coordinates.")).defaultValue(new SettingColor(0, 255, 0, 255)).build());
    private final Setting<SettingColor> netherColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("nether-color")).description("Color for nether coordinates.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
    private final Setting<SettingColor> endColor = this.sgRender.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("end-color")).description("Color for end coordinates.")).defaultValue(new SettingColor(255, 0, 255, 255)).build());
    public static final HudElementInfo<DimensionalPosition> nkSQ6R = new HudElementInfo(musheor.MUSHEOR_HUD, "dimensional-position", "A hud module that displays your position", DimensionalPosition::new);

    public DimensionalPosition() {
        super(nkSQ6R);
    }

    public void render(HudRenderer hudRenderer) {
        if (this.Cd1WW3smp32e.player == null || this.Cd1WW3smp32e.world == null) {
            return;
        }
        CoordHider coordHider = (CoordHider)Modules.get().get(CoordHider.class);
        boolean bl = coordHider != null && coordHider.isActive();
        double d = this.Cd1WW3smp32e.player.getX();
        double d2 = this.Cd1WW3smp32e.player.getY();
        double d3 = this.Cd1WW3smp32e.player.getZ();
        String string = bl ? "?" : this.KP44bk(d);
        String string2 = bl ? "?" : this.KP44bk(d2);
        String string3 = bl ? "?" : this.KP44bk(d3);
        ArrayList<String[]> arrayList = new ArrayList<String[]>();
        ArrayList<SettingColor> arrayList2 = new ArrayList<SettingColor>();
        if (this.Cd1WW3smp32e.world.getDimension() == DimensionType.OVERWORLD) {
            arrayList.add(new String[]{"Overworld:", string, string2, string3});
            arrayList2.add((SettingColor)this.overworldColor.get());
            if (((Boolean)this.showOppositeDimension.get()).booleanValue()) {
                arrayList.add(new String[]{"Nether:", bl ? "?" : this.KP44bk(d / 8.0), string2, bl ? "?" : this.KP44bk(d3 / 8.0)});
                arrayList2.add((SettingColor)this.netherColor.get());
            }
        } else if (this.Cd1WW3smp32e.world.getDimension() == DimensionType.NETHER) {
            arrayList.add(new String[]{"Nether:", string, string2, string3});
            arrayList2.add((SettingColor)this.netherColor.get());
            if (((Boolean)this.showOppositeDimension.get()).booleanValue()) {
                arrayList.add(new String[]{"Overworld:", bl ? "?" : this.KP44bk(d * 8.0), string2, bl ? "?" : this.KP44bk(d3 * 8.0)});
                arrayList2.add((SettingColor)this.overworldColor.get());
            }
        } else if (this.Cd1WW3smp32e.world.getDimension() == DimensionType.END) {
            arrayList.add(new String[]{"End:", string, string2, string3});
            arrayList2.add((SettingColor)this.endColor.get());
        }
        if (arrayList.isEmpty()) {
            return;
        }
        double d4 = hudRenderer.textHeight(true, ((Double)this.scale.get()).doubleValue());
        double d5 = hudRenderer.textWidth(", ", true, ((Double)this.scale.get()).doubleValue());
        double d6 = hudRenderer.textWidth(" ", true, ((Double)this.scale.get()).doubleValue());
        if (arrayList.size() == 2 && ((Boolean)this.horizontal.get()).booleanValue()) {
            double d7 = this.x;
            for (int i = 0; i < arrayList.size(); ++i) {
                if (i > 0) {
                    d7 += hudRenderer.textWidth("   ", true, ((Double)this.scale.get()).doubleValue());
                }
                d7 = this.jOdDDFXSeWl4(hudRenderer, (String[])arrayList.get(i), (SettingColor)arrayList2.get(i), d7, this.y, d5, d6);
            }
            this.setSize(d7 - (double)this.x, d4);
        } else {
            double d8 = 0.0;
            double d9 = 0.0;
            double d10 = 0.0;
            double d11 = 0.0;
            for (String[] stringArray : arrayList) {
                d8 = Math.max(d8, hudRenderer.textWidth(stringArray[0], true, ((Double)this.scale.get()).doubleValue()));
                d9 = Math.max(d9, hudRenderer.textWidth(stringArray[1], true, ((Double)this.scale.get()).doubleValue()));
                d10 = Math.max(d10, hudRenderer.textWidth(stringArray[2], true, ((Double)this.scale.get()).doubleValue()));
                d11 = Math.max(d11, hudRenderer.textWidth(stringArray[3], true, ((Double)this.scale.get()).doubleValue()));
            }
            double d12 = d8 + d6 + d9 + d5 + d10 + d5 + d11;
            double d13 = this.y;
            for (int i = 0; i < arrayList.size(); ++i) {
                String[] stringArray = (String[])arrayList.get(i);
                SettingColor settingColor = (SettingColor)arrayList2.get(i);
                double d14 = this.x;
                hudRenderer.text(stringArray[0], d14 + d8 - hudRenderer.textWidth(stringArray[0], true, ((Double)this.scale.get()).doubleValue()), d13, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
                hudRenderer.text(stringArray[1], (d14 += d8 + d6) + d9 - hudRenderer.textWidth(stringArray[1], true, ((Double)this.scale.get()).doubleValue()), d13, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
                hudRenderer.text(" ", d14 += d9, d13, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
                hudRenderer.text(stringArray[2], (d14 += d5) + d10 - hudRenderer.textWidth(stringArray[2], true, ((Double)this.scale.get()).doubleValue()), d13, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
                hudRenderer.text(" ", d14 += d10, d13, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
                hudRenderer.text(stringArray[3], (d14 += d5) + d11 - hudRenderer.textWidth(stringArray[3], true, ((Double)this.scale.get()).doubleValue()), d13, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
                d13 += d4;
            }
            this.setSize(d12, d13 - (double)this.y);
        }
    }

    private double jOdDDFXSeWl4(HudRenderer hudRenderer, String[] stringArray, SettingColor settingColor, double d, double d2, double d3, double d4) {
        double d5 = d;
        hudRenderer.text(stringArray[0], d5, d2, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
        hudRenderer.text(stringArray[1], d5 += hudRenderer.textWidth(stringArray[0], true, ((Double)this.scale.get()).doubleValue()) + d4, d2, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
        hudRenderer.text(" ", d5 += hudRenderer.textWidth(stringArray[1], true, ((Double)this.scale.get()).doubleValue()), d2, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
        hudRenderer.text(stringArray[2], d5 += d3, d2, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
        hudRenderer.text(" ", d5 += hudRenderer.textWidth(stringArray[2], true, ((Double)this.scale.get()).doubleValue()), d2, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
        hudRenderer.text(stringArray[3], d5 += d3, d2, (Color)settingColor, true, ((Double)this.scale.get()).doubleValue());
        return d5 += hudRenderer.textWidth(stringArray[3], true, ((Double)this.scale.get()).doubleValue());
    }

    private String KP44bk(double d) {
        if (!((Boolean)this.showDecimalPlaces.get()).booleanValue()) {
            return String.format(Locale.US, "%,.0f", d);
        }
        return String.format(Locale.US, "%,.1f", d);
    }
}

