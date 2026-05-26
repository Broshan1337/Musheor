// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.hud;

import java.util.ArrayList;
import java.util.Arrays;
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
import musheor.modules.automation.HighwayBuilder;
import musheor.musheor;
import musheor.utils.StatsHandler;
import musheor.utils.internal.HighwayState;
import net.minecraft.client.MinecraftClient;

public class StatsViewer
extends HudElement {
    private final SettingGroup sgGeneral;
    public static final HudElementInfo<StatsViewer> INFO = new HudElementInfo(musheor.MUSHEOR_HUD, "stats-viewer", "View your stats while using the HighwayBuilder module.", StatsViewer::new);
    private final Setting<Double> scale;
    private final Setting<Integer> sectionSize;
    private final Setting<Boolean> displaySession;
    private final Setting<Boolean> displayBlocks;
    private final Setting<Boolean> displayPerformance;
    private final Setting<Boolean> displayInfo;
    private final Setting<Boolean> displayLifetime;
    private final Setting<SettingColor> prefixColor;
    private final Setting<SettingColor> valueColor;

    public StatsViewer() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("I mean cmon do I really have to explain??")).defaultValue(1.0).build());
        this.sectionSize = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("section-size")).description("Distance or size of a single highway section")).defaultValue((Object)100000)).build());
        this.displaySession = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-session")).description("Display statistics of the current session.")).defaultValue((Object)true)).build());
        this.displayBlocks = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-blocks")).description("Display statistics of blocks mined and placed.")).defaultValue((Object)true)).build());
        this.displayPerformance = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-performance")).description("Display statistics of paver's performance.")).defaultValue((Object)true)).build());
        this.displayInfo = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-info")).description("Display extra information about the environment, inventory and ETA.")).defaultValue((Object)true)).build());
        this.displayLifetime = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-lifetime")).description("Display lifetime statistics about the paver.")).defaultValue((Object)true)).build());
        this.prefixColor = this.sgGeneral.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("prefix-color")).description("Text color of the hud info lines.")).defaultValue(new SettingColor(255, 255, 225, 255)).build());
        this.valueColor = this.sgGeneral.add((Setting)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("value-color")).description("Text color of the hud info lines.")).defaultValue(new SettingColor(255, 0, 208, 255)).build());
    }

    private String[][] buildStatsRows() {
        ArrayList arrayList = new ArrayList();
        HighwayState highwayState = HighwayState.getInstance();
        String[][] stringArrayArray = new String[][]{{"Latest session: ", ""}, {"  Runtime: ", StatsHandler.formatTicksAsTime(highwayState.getTicksActive())}, {"  Direction: ", String.valueOf((Object)HighwayBuilder.getDirection()).toLowerCase()}, {"  Distance travelled: ", String.valueOf(StatsHandler.getDistanceToCheckpoint())}};
        String[][] stringArrayArray2 = new String[][]{{"Blocks", ""}, {"  Obsidian Placed: ", String.valueOf(highwayState.getSessionObsidianPlacedCount())}, {"  Obsidian Mined: ", String.valueOf(highwayState.getSessionObsidianMinedCount())}, {"  Enderchests mined: ", String.valueOf(highwayState.getSessionMiscMinedCount())}, {"  Netherrack mined: ", String.valueOf(highwayState.getSessionLavaBucketCount())}, {"  Total mined: ", String.valueOf(highwayState.getSessionObsidianMinedCount() + highwayState.getSessionLavaBucketCount() + highwayState.getSessionMiscMinedCount())}};
        String[][] stringArrayArray3 = new String[][]{{"Performance", ""}, {"  Placements / s: ", StatsHandler.formatBlocksPerSecond(StatsHandler.getBlocksPlacedPerSecond())}, {"  Placements / h: ", StatsHandler.formatBlocksPerHour(StatsHandler.getBlocksPlacedPerSecond())}, {"  Breaks / s: ", StatsHandler.formatBlocksPerSecond(StatsHandler.getBlocksMinedPerSecond())}, {"  Distance / s: ", StatsHandler.formatDistancePerSecond(StatsHandler.getDistanceToCheckpoint())}, {"  Distance / h: ", StatsHandler.formatDistancePerHour(StatsHandler.getDistanceToCheckpoint())}};
        String[][] stringArrayArray4 = new String[][]{{"Info", ""}, {"  Distance to next section: ", String.valueOf(StatsHandler.getDistanceToNextMultiple((Integer)this.sectionSize.get()))}, {"  Percentage completed: ", StatsHandler.getPercentOffset((Integer)this.sectionSize.get())}, {"  Material Count: ", String.valueOf(StatsHandler.countObsidianBlocks())}, {"  ETA to next section: ", StatsHandler.formatCheckpointETA((Integer)this.sectionSize.get())}, {"  ETA to next refill: ", StatsHandler.formatObsidianETA()}};
        String[][] stringArrayArray5 = new String[][]{{"Lifetime: ", ""}, {"  Obsidian Placed: ", String.valueOf(highwayState.getLifetimeObsidianPlaced())}, {"  Obsidian Mined: ", String.valueOf(highwayState.getLifetimeObsidianMined())}, {"  Enderchests mined: ", String.valueOf(highwayState.getLifetimeLavaBuckets())}, {"  Netherrack mined: ", String.valueOf(highwayState.getLifetimeMiscBlocks())}, {"  Other mined: ", String.valueOf(highwayState.getLifetimeMiscMined())}};
        if (((Boolean)this.displaySession.get()).booleanValue()) {
            arrayList.addAll(Arrays.asList(stringArrayArray));
        }
        if (((Boolean)this.displayBlocks.get()).booleanValue()) {
            arrayList.addAll(Arrays.asList(stringArrayArray2));
        }
        if (((Boolean)this.displayPerformance.get()).booleanValue()) {
            arrayList.addAll(Arrays.asList(stringArrayArray3));
        }
        if (((Boolean)this.displayInfo.get()).booleanValue()) {
            arrayList.addAll(Arrays.asList(stringArrayArray4));
        }
        if (((Boolean)this.displayLifetime.get()).booleanValue()) {
            arrayList.addAll(Arrays.asList(stringArrayArray5));
        }
        return (String[][])arrayList.toArray((T[])new String[0][]);
    }

    public void render(HudRenderer hudRenderer) {
        if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null) {
            return;
        }
        String[][] stringArray = this.buildStatsRows();
        double d = hudRenderer.textHeight(true, ((Double)this.scale.get()).doubleValue());
        double d2 = 0.0;
        for (String[] stringArray2 : stringArray) {
            double d3 = hudRenderer.textWidth(stringArray2[0], true) + hudRenderer.textWidth(": ", true) + hudRenderer.textWidth(stringArray2[1], true, ((Double)this.scale.get()).doubleValue());
            d2 = Math.max(d2, d3);
        }
        this.setSize(d2, d * (double)stringArray.length);
        double d4 = this.y;
        for (String[] stringArray3 : stringArray) {
            double d5 = this.x;
            hudRenderer.text(stringArray3[0], d5, d4, (Color)this.prefixColor.get(), true, ((Double)this.scale.get()).doubleValue());
            hudRenderer.text("", d5 += hudRenderer.textWidth(stringArray3[0], true, ((Double)this.scale.get()).doubleValue()), d4, (Color)this.prefixColor.get(), true, ((Double)this.scale.get()).doubleValue());
            d5 += hudRenderer.textWidth("", true, ((Double)this.scale.get()).doubleValue());
            if (stringArray3[0].equals("Progress")) {
                String string = stringArray3[1];
                String string2 = " done";
                double d6 = hudRenderer.textWidth(string, true);
                hudRenderer.textWidth(string2, true, ((Double)this.scale.get()).doubleValue());
                hudRenderer.text(string, d5, d4, (Color)this.valueColor.get(), true, ((Double)this.scale.get()).doubleValue());
                hudRenderer.text(string2, d5 += d6, d4, (Color)this.prefixColor.get(), true, ((Double)this.scale.get()).doubleValue());
            } else {
                hudRenderer.text(stringArray3[1], d5, d4, (Color)this.valueColor.get(), true, ((Double)this.scale.get()).doubleValue());
            }
            d4 += d;
        }
    }
}

