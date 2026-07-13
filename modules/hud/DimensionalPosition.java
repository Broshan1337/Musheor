// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.hud;

import java.util.ArrayList;
import java.util.List;
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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import musheor.musheor;
import musheor.modules.features.CoordHider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

/**
 * "dimensional-position" HUD — shows the player's coordinates coloured by dimension, and
 * optionally the equivalent position in the opposite dimension (÷8 overworld→nether, ×8
 * nether→overworld), laid out horizontally or vertically. Coordinates are replaced with
 * "?" while CoordHider is active.
 */
public class DimensionalPosition extends HudElement {
    private final MinecraftClient mc = MinecraftClient.getInstance();          // was: Q90GLXQ0Pef
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();    // was: psJq59YIbp3Z
    private final SettingGroup sgRender = this.settings.createGroup("Render");  // was: SOYyh5IPg26f7F

    private final Setting<Boolean> showOppositeDimension = sgGeneral.add(new BoolSetting.Builder() // was: rKbT3Ifwo
        .name("show-opposite-dimension").description("Show relative position in opposite dimension.").defaultValue(true).build());
    private final Setting<Boolean> displayHorizontal = sgGeneral.add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("display-horizontal").description("Displays opposite dimension horizontally instead of vertically.").defaultValue(true).visible(showOppositeDimension::get).build());
    private final Setting<Double> textScale = sgGeneral.add(new DoubleSetting.Builder() // was: oZHMlTL
        .name("text-scale").description("Scale of the text.").defaultValue(1.0).sliderRange(0.1, 3.0).decimalPlaces(1).build());
    private final Setting<Boolean> showDecimalPlaces = sgGeneral.add(new BoolSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("show-decimal-places").description("Displays the decimal places of the coordinates.").defaultValue(true).visible(showOppositeDimension::get).build());

    private final Setting<SettingColor> overworldColor = sgRender.add(new ColorSetting.Builder() // was: OMMZL1F3q
        .name("overworld-color").description("Color for overworld coordinates.").defaultValue(new SettingColor(0, 255, 0, 255)).build());
    private final Setting<SettingColor> netherColor = sgRender.add(new ColorSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("nether-color").description("Color for nether coordinates.").defaultValue(new SettingColor(255, 0, 0, 255)).build());
    private final Setting<SettingColor> endColor = sgRender.add(new ColorSetting.Builder() // was: krxNb5lcQuWA
        .name("end-color").description("Color for end coordinates.").defaultValue(new SettingColor(255, 0, 255, 255)).build());

    public static final HudElementInfo<DimensionalPosition> INFO = new HudElementInfo<>( // was: FvaNWO
        musheor.MUSHEOR_HUD, "dimensional-position", "A hud module that displays your position", DimensionalPosition::new);

    public DimensionalPosition() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (this.mc.player == null || this.mc.world == null) return;
        CoordHider ch = (CoordHider) Modules.get().get(CoordHider.class);
        boolean hiding = ch != null && ch.isActive();
        double px = this.mc.player.getX();
        double py = this.mc.player.getY();
        double pz = this.mc.player.getZ();
        String sx = hiding ? "?" : this.formatCoord(px);
        String sy = hiding ? "?" : this.formatCoord(py);
        String sz = hiding ? "?" : this.formatCoord(pz);
        List<String[]> rows = new ArrayList<>();
        List<SettingColor> colors = new ArrayList<>();

        if (this.mc.world.getRegistryKey() == World.OVERWORLD) {
            rows.add(new String[]{"Overworld:", sx, sy, sz});
            colors.add(this.overworldColor.get());
            if (this.showOppositeDimension.get()) {
                rows.add(new String[]{"Nether:", hiding ? "?" : this.formatCoord(px / 8.0), sy, hiding ? "?" : this.formatCoord(pz / 8.0)});
                colors.add(this.netherColor.get());
            }
        } else if (this.mc.world.getRegistryKey() == World.NETHER) {
            rows.add(new String[]{"Nether:", sx, sy, sz});
            colors.add(this.netherColor.get());
            if (this.showOppositeDimension.get()) {
                rows.add(new String[]{"Overworld:", hiding ? "?" : this.formatCoord(px * 8.0), sy, hiding ? "?" : this.formatCoord(pz * 8.0)});
                colors.add(this.overworldColor.get());
            }
        } else if (this.mc.world.getRegistryKey() == World.END) {
            rows.add(new String[]{"End:", sx, sy, sz});
            colors.add(this.endColor.get());
        }

        if (rows.isEmpty()) return;
        double lineHeight = renderer.textHeight(true, this.textScale.get());
        double sepW = renderer.textWidth(", ", true, this.textScale.get());
        double gapW = renderer.textWidth(" ", true, this.textScale.get());

        if (rows.size() == 2 && this.displayHorizontal.get()) {
            double cx = this.x;
            for (int i = 0; i < rows.size(); i++) {
                if (i > 0) cx += renderer.textWidth("   ", true, this.textScale.get());
                cx = this.renderRowHorizontal(renderer, rows.get(i), colors.get(i), cx, this.y, sepW, gapW);
            }
            this.setSize(cx - this.x, lineHeight);
        } else {
            double labelW = 0.0, xW = 0.0, yW = 0.0, zW = 0.0;
            for (String[] row : rows) {
                labelW = Math.max(labelW, renderer.textWidth(row[0], true, this.textScale.get()));
                xW = Math.max(xW, renderer.textWidth(row[1], true, this.textScale.get()));
                yW = Math.max(yW, renderer.textWidth(row[2], true, this.textScale.get()));
                zW = Math.max(zW, renderer.textWidth(row[3], true, this.textScale.get()));
            }
            double totalWidth = labelW + gapW + xW + sepW + yW + sepW + zW;
            double currentY = this.y;
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                SettingColor color = colors.get(i);
                double cx = this.x;
                renderer.text(row[0], cx + labelW - renderer.textWidth(row[0], true, this.textScale.get()), currentY, color, true, this.textScale.get());
                cx += labelW + gapW;
                renderer.text(row[1], cx + xW - renderer.textWidth(row[1], true, this.textScale.get()), currentY, color, true, this.textScale.get());
                cx += xW;
                renderer.text(" ", cx, currentY, color, true, this.textScale.get());
                cx += sepW;
                renderer.text(row[2], cx + yW - renderer.textWidth(row[2], true, this.textScale.get()), currentY, color, true, this.textScale.get());
                cx += yW;
                renderer.text(" ", cx, currentY, color, true, this.textScale.get());
                cx += sepW;
                renderer.text(row[3], cx + zW - renderer.textWidth(row[3], true, this.textScale.get()), currentY, color, true, this.textScale.get());
                currentY += lineHeight;
            }
            this.setSize(totalWidth, currentY - this.y);
        }
    }

    /** Renders one "label x y z" row horizontally, returning the ending X. */
    private double renderRowHorizontal(HudRenderer renderer, String[] row, SettingColor color, double startX, double startY, double sepW, double gapW) { // was: FvaNWO(...)
        double cx = startX;
        renderer.text(row[0], cx, startY, color, true, this.textScale.get());
        cx += renderer.textWidth(row[0], true, this.textScale.get()) + gapW;
        renderer.text(row[1], cx, startY, color, true, this.textScale.get());
        cx += renderer.textWidth(row[1], true, this.textScale.get());
        renderer.text(" ", cx, startY, color, true, this.textScale.get());
        cx += sepW;
        renderer.text(row[2], cx, startY, color, true, this.textScale.get());
        cx += renderer.textWidth(row[2], true, this.textScale.get());
        renderer.text(" ", cx, startY, color, true, this.textScale.get());
        cx += sepW;
        renderer.text(row[3], cx, startY, color, true, this.textScale.get());
        return cx + renderer.textWidth(row[3], true, this.textScale.get());
    }

    /** Formats a coordinate with thousands separators, with or without one decimal place. */
    private String formatCoord(double v) { // was: FvaNWO(double)
        return !this.showDecimalPlaces.get() ? String.format(Locale.US, "%,.0f", v) : String.format(Locale.US, "%,.1f", v);
    }
}
