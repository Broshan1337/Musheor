// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.hud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import musheor.musheor;
import musheor.modules.automation.HighwayBuilder;
import musheor.utils.StatsHandler;
import musheor.utils.internal.HighwayState;
import net.minecraft.client.MinecraftClient;

/**
 * "stats-viewer" HUD — shows HighwayBuilder statistics: the latest session (runtime,
 * direction, distance), block counts (session and lifetime obsidian/echest/netherrack),
 * performance rates (placements/breaks/distance per second/hour), and info (distance/ETA
 * to next section, material count, refill ETA). Each group can be toggled independently.
 */
public class StatsViewer extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: FvaNWO
    public static final HudElementInfo<StatsViewer> INFO = new HudElementInfo<>(
        musheor.MUSHEOR_HUD, "stats-viewer", "View your stats while using the HighwayBuilder module.", StatsViewer::new);

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder() // was: Q90GLXQ0Pef
        .name("scale").description("I mean cmon do I really have to explain??").defaultValue(1.0).build());
    private final Setting<Integer> sectionSize = sgGeneral.add(new IntSetting.Builder() // was: psJq59YIbp3Z
        .name("section-size").description("Distance or size of a single highway section").defaultValue(10000).build());
    private final Setting<Boolean> displaySession = sgGeneral.add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("display-session").description("Display statistics of the current session.").defaultValue(true).build());
    private final Setting<Boolean> displayBlocks = sgGeneral.add(new BoolSetting.Builder() // was: rKbT3Ifwo
        .name("display-blocks").description("Display statistics of blocks mined and placed.").defaultValue(true).build());
    private final Setting<Boolean> displayPerformance = sgGeneral.add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("display-performance").description("Display statistics of paver's performance.").defaultValue(true).build());
    private final Setting<Boolean> displayInfo = sgGeneral.add(new BoolSetting.Builder() // was: oZHMlTL
        .name("display-info").description("Display extra information about the environment, inventory and ETA.").defaultValue(false).build());
    private final Setting<Boolean> displayLifetime = sgGeneral.add(new BoolSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("display-lifetime").description("Display lifetime statistics about the paver.").defaultValue(true).build());
    private final Setting<SettingColor> prefixColor = sgGeneral.add(new ColorSetting.Builder() // was: OMMZL1F3q
        .name("prefix-color").description("Text color of the hud info lines.").defaultValue(new SettingColor(255, 255, 225, 255)).build());
    private final Setting<SettingColor> valueColor = sgGeneral.add(new ColorSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("value-color").description("Text color of the hud info lines.").defaultValue(new SettingColor(255, 0, 208, 255)).build());

    public StatsViewer() {
        super(INFO);
    }

    /** Builds the enabled stat-line groups as {label, value} rows. */
    private String[][] buildLines() { // was: FvaNWO()
        List<String[]> lines = new ArrayList<>();
        HighwayState state = HighwayState.getInstance();
        String[][] session = {
            {"Latest session: ", ""},
            {"  Runtime: ", StatsHandler.formatTicksAsClock(state.getTicksActive())},
            {"  Direction: ", String.valueOf(HighwayBuilder.getDirection()).toLowerCase()},
            {"  Distance travelled: ", String.valueOf(StatsHandler.getDistanceTravelled())}
        };
        String[][] blocks = {
            {"Blocks", ""},
            {"  Obsidian Placed: ", StatsHandler.abbreviate(state.getSessionObsidianPlaced())},
            {"  Obsidian Mined: ", StatsHandler.abbreviate(state.getSessionObsidianMined())},
            {"  Enderchests mined: ", StatsHandler.abbreviate(state.getSessionMiscMined())},
            {"  Netherrack mined: ", StatsHandler.abbreviate(state.getSessionLavaBuckets())},
            {"  Total mined: ", StatsHandler.abbreviate(state.getSessionObsidianMined() + state.getSessionLavaBuckets() + state.getSessionMiscMined())}
        };
        String[][] performance = {
            {"Performance", ""},
            {"  Placements / s: ", StatsHandler.formatPlacementsPerSecond(StatsHandler.getBlocksPlacedPerSecond())},
            {"  Placements / h: ", StatsHandler.formatPlacementsPerHour(StatsHandler.getBlocksPlacedPerSecond())},
            {"  Breaks / s: ", StatsHandler.formatBreakingPerSecond(StatsHandler.getBlocksMinedPerSecond())},
            {"  Distance / s: ", StatsHandler.formatDistancePerSecond(StatsHandler.getDistanceTravelled())},
            {"  Distance / h: ", StatsHandler.formatDistancePerHour(StatsHandler.getDistanceTravelled())}
        };
        String[][] info = {
            {"Info", ""},
            {"  Distance to next section: ", String.valueOf(StatsHandler.getBlocksLeftInSection(this.sectionSize.get()))},
            {"  Percentage completed: ", StatsHandler.getPercentComplete(this.sectionSize.get())},
            {"  Material Count: ", String.valueOf(StatsHandler.getAvailableObsidian())},
            {"  ETA to next section: ", StatsHandler.formatSectionEta(this.sectionSize.get())},
            {"  ETA to next refill: ", StatsHandler.formatMiningEta()}
        };
        String[][] lifetime = {
            {"Lifetime: ", ""},
            {"  Obsidian Placed: ", StatsHandler.abbreviate(state.getLifetimeObsidianPlaced())},
            {"  Obsidian Mined: ", StatsHandler.abbreviate(state.getLifetimeObsidianMined())},
            {"  Enderchests mined: ", StatsHandler.abbreviate(state.getLifetimeEchests())},
            {"  Netherrack mined: ", StatsHandler.abbreviate(state.getLifetimeMiscBlocks())},
            {"  Total mined: ", StatsHandler.abbreviate(state.getLifetimeTotalMined())}
        };
        if (this.displaySession.get()) lines.addAll(Arrays.asList(session));
        if (this.displayBlocks.get()) lines.addAll(Arrays.asList(blocks));
        if (this.displayPerformance.get()) lines.addAll(Arrays.asList(performance));
        if (this.displayInfo.get()) lines.addAll(Arrays.asList(info));
        if (this.displayLifetime.get()) lines.addAll(Arrays.asList(lifetime));
        return lines.toArray(new String[0][]);
    }

    @Override
    public void render(HudRenderer renderer) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        String[][] lines = this.buildLines();
        double lineHeight = renderer.textHeight(true, this.scale.get());
        double width = 0.0;
        for (String[] line : lines) {
            double lineWidth = renderer.textWidth(line[0], true) + renderer.textWidth(": ", true) + renderer.textWidth(line[1], true, this.scale.get());
            width = Math.max(width, lineWidth);
        }
        this.setSize(width, lineHeight * lines.length);

        double currentY = this.y;
        for (String[] line : lines) {
            double currentX = this.x;
            renderer.text(line[0], currentX, currentY, this.prefixColor.get(), true, this.scale.get());
            currentX += renderer.textWidth(line[0], true, this.scale.get());
            renderer.text("", currentX, currentY, this.prefixColor.get(), true, this.scale.get());
            currentX += renderer.textWidth("", true, this.scale.get());
            if (line[0].equals("Progress")) {
                String percentageText = line[1];
                String doneText = " done";
                double percentageWidth = renderer.textWidth(percentageText, true);
                renderer.textWidth(doneText, true, this.scale.get());
                renderer.text(percentageText, currentX, currentY, this.valueColor.get(), true, this.scale.get());
                currentX += percentageWidth;
                renderer.text(doneText, currentX, currentY, this.prefixColor.get(), true, this.scale.get());
            } else {
                renderer.text(line[1], currentX, currentY, this.valueColor.get(), true, this.scale.get());
            }
            currentY += lineHeight;
        }
    }
}
