// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name and members were already readable; only a couple of helper calls were obfuscated.
package musheor.utils.system;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import musheor.commands.RestockConfig;
import musheor.compat.VersionHelper;
import musheor.utils.hud.CustomHudButton;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Musheor's global settings/config {@link System}. Groups all cross-module tuning:
 * placement (range/timeout/swap-delay), breaking (grim-bypass, thresholds, tool safety),
 * packet limits, block-render styling, and highway widths — all consumed statically via
 * {@link #Manager}. Also persists the custom HUD buttons and the restock-container config.
 * (Members were not obfuscated; only the RestockConfig/CustomHudButton helper calls were.)
 */
public class MusheorSystem extends System<MusheorSystem> {
    public final Settings settings = new Settings();
    public static MusheorSystem Manager;
    public boolean update;
    public final List<CustomHudButton> customButtons = new ArrayList<>();

    SettingGroup placementManagerSettings = this.settings.createGroup("Placement-Manager");
    SettingGroup breakManagerSettings = this.settings.createGroup("Break-Manager");
    SettingGroup packetManagerSettings = this.settings.createGroup("Packet-Manager");
    SettingGroup renderManagerSettings = this.settings.createGroup("Render-Manager");

    public final Setting<Boolean> enableChatDebugging = this.settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("debug-mode").description("Enable debug messages in chat.").defaultValue(false).build());
    public final Setting<Boolean> disableMutuals = this.settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("disable-capes-and-glowing").description("Stops your client from rendering your own and other people's Musheor capes and glowing effect.").defaultValue(false).build());
    public final Setting<Keybind> openEditor = this.settings.getDefaultGroup().add(new KeybindSetting.Builder()
        .name("open-button-editor-bind").description("Open the hud-button editor.").defaultValue(Keybind.fromKey(345)).build());

    public final Setting<Double> placementRange = this.placementManagerSettings.add(new DoubleSetting.Builder()
        .name("place-range").description("In what range the player can / should place blocks").defaultValue(4.5).min(1.0).max(8.0).decimalPlaces(1).build());
    public final Setting<Integer> placementTimeout = this.placementManagerSettings.add(new IntSetting.Builder()
        .name("placement-timeout").description("Ticks to wait before retrying a failed placement (higher = safer for high ping)").defaultValue(5).min(1).max(20).build());
    public final Setting<Integer> swapDelay = this.placementManagerSettings.add(new IntSetting.Builder()
        .name("swap-delay").description("Ticks to wait after swapping to an item (higher = safer for high ping)").defaultValue(3).min(1).max(10).build());

    public final Setting<Boolean> grimBypass = this.breakManagerSettings.add(new BoolSetting.Builder().name("grim-bypass").defaultValue(true).build());
    public final Setting<Double> breakThreshold = this.breakManagerSettings.add(new DoubleSetting.Builder()
        .name("break-threshold").defaultValue(0.7).min(0.1).max(1.0).decimalPlaces(2).build());
    public final Setting<Boolean> doubleBreak = this.breakManagerSettings.add(new BoolSetting.Builder().name("double-break").defaultValue(true).build());
    public final Setting<Boolean> validateBreak = this.breakManagerSettings.add(new BoolSetting.Builder()
        .name("validate-break").description("Waits for the server to validate the break. Disable when you have high ping (may cause rubberbanding)").defaultValue(true).build());
    public final Setting<Integer> rebreakTimeout = this.breakManagerSettings.add(new IntSetting.Builder()
        .name("break-timeout").description("Time in ticks to wait before attempting to break the same block again (higher = safer for high ping)").defaultValue(3).min(1).max(20).build());
    public final Setting<Integer> holdTicks = this.breakManagerSettings.add(new IntSetting.Builder()
        .name("hold-ticks").description("Ticks to keep the tool selected after a silent break before restoring. Prevents wrong-tool drops when sequenced block packets and slot packets race (0 = restore immediately)").defaultValue(3).min(0).max(10).build());
    public final Setting<Boolean> preventToolBreaking = this.breakManagerSettings.add(new BoolSetting.Builder()
        .name("prevent-tool-breaking").description("Prevents you from breaking your tool items").defaultValue(true).build());
    public final Setting<Integer> minToolDurability = this.breakManagerSettings.add(new IntSetting.Builder()
        .name("min-tool-durability").description("The minimum durability threshold of a usable tool").defaultValue(10).sliderRange(0, 250).visible(preventToolBreaking::get).build());

    public final Setting<Integer> globalPacketLimit = this.packetManagerSettings.add(new IntSetting.Builder()
        .name("global-packet-limit").description("Packet limit before the server kicks you").defaultValue(1349).sliderMax(1500).build());
    public final Setting<Integer> invPacketLimit = this.packetManagerSettings.add(new IntSetting.Builder()
        .name("inventory-packet-limit").description("Inventory action limiter before the server kicks you").defaultValue(79).sliderMax(150).build());

    public final Setting<Boolean> placeRender = this.renderManagerSettings.add(new BoolSetting.Builder()
        .name("enable-placement-rendering").description("Renders the positions of blocks in relation to placing").defaultValue(true).onChanged(i -> this.update = true).build());
    public final Setting<Boolean> breakRender = this.renderManagerSettings.add(new BoolSetting.Builder()
        .name("enable-break-rendering").description("Renders the positions of blocks in relation to breaking").defaultValue(true).onChanged(i -> this.update = true).build());
    public final Setting<RenderType> renderType = this.renderManagerSettings.add(new EnumSetting.Builder<RenderType>()
        .name("render-type").description("Choose what type of rendering applies, either single-color or automatic color mapping").defaultValue(RenderType.Static)
        .visible(() -> placeRender.get() || breakRender.get()).onChanged(i -> this.update = true).build());
    public final Setting<ShapeMode> renderShape = this.renderManagerSettings.add(new EnumSetting.Builder<ShapeMode>()
        .name("render-shape").description("What kind of shape (lines, sides or both) block positions get rendered as").defaultValue(ShapeMode.Lines)
        .visible(() -> placeRender.get() || breakRender.get()).onChanged(i -> this.update = true).build());
    public final Setting<Boolean> renderLines = this.renderManagerSettings.add(new BoolSetting.Builder()
        .name("render-lines").description("Renders the outline of blocks / positions").defaultValue(true)
        .visible(() -> renderShape.get() == ShapeMode.Lines || renderShape.get() == ShapeMode.Both && (placeRender.get() || breakRender.get())).onChanged(i -> this.update = true).build());
    public final Setting<SettingColor> renderLineColor = this.renderManagerSettings.add(new ColorSetting.Builder()
        .name("render-line-color").description("Line color of rendered blocks").defaultValue(new SettingColor(0, 225, 255, 200))
        .visible(() -> (renderShape.get() == ShapeMode.Lines || renderShape.get() == ShapeMode.Both) && renderLines.get() && renderType.get() == RenderType.Static && (placeRender.get() || breakRender.get())).build());
    public final Setting<Integer> renderLineAlpha = this.renderManagerSettings.add(new IntSetting.Builder()
        .name("render-line-alpha").description("Line color intensity of rendered blocks").defaultValue(200).min(0).max(255).sliderMin(0).sliderMax(255)
        .visible(() -> (renderShape.get() == ShapeMode.Lines || renderShape.get() == ShapeMode.Both) && renderLines.get() && renderType.get() == RenderType.Mapped && (placeRender.get() || breakRender.get())).build());
    public final Setting<Boolean> renderSides = this.renderManagerSettings.add(new BoolSetting.Builder()
        .name("render-sides").description("Renders the sides of blocks / positions").defaultValue(false)
        .visible(() -> renderShape.get() == ShapeMode.Sides || renderShape.get() == ShapeMode.Both && (placeRender.get() || breakRender.get())).onChanged(i -> this.update = true).build());
    public final Setting<SettingColor> renderSideColor = this.renderManagerSettings.add(new ColorSetting.Builder()
        .name("render-side-color").description("Side color of rendered blocks").defaultValue(new SettingColor(0, 200, 255, 100))
        .visible(() -> (renderShape.get() == ShapeMode.Sides || renderShape.get() == ShapeMode.Both) && renderSides.get() && renderType.get() == RenderType.Static && (placeRender.get() || breakRender.get())).build());
    public final Setting<Integer> renderSideAlpha = this.renderManagerSettings.add(new IntSetting.Builder()
        .name("render-side-alpha").description("Side color intensity of rendered blocks").defaultValue(100).min(0).max(255).sliderMin(0).sliderMax(255)
        .visible(() -> (renderShape.get() == ShapeMode.Sides || renderShape.get() == ShapeMode.Both) && renderSides.get() && renderType.get() == RenderType.Mapped && (placeRender.get() || breakRender.get())).build());

    SettingGroup highwaySettings = this.settings.createGroup("Highways");
    public final Setting<Integer> cardinalHighwayWidth = this.highwaySettings.add(new IntSetting.Builder()
        .name("cardinal-width").description("Pavement width for cardinal and ring road highways.").defaultValue(4).min(1).sliderMax(16).build());
    public final Setting<Integer> diagonalHighwayWidth = this.highwaySettings.add(new IntSetting.Builder()
        .name("diagonal-width").description("Pavement width for diagonal and diamond highways.").defaultValue(9).min(1).sliderMax(16).build());
    public final Setting<Integer> gridHighwayWidth = this.highwaySettings.add(new IntSetting.Builder()
        .name("grid-width").description("Pavement width for grid highways.").defaultValue(3).min(1).sliderMax(16).build());

    public MusheorSystem() {
        super("musheor");
        Manager = this;
    }

    public static MusheorSystem get() {
        return Systems.get(MusheorSystem.class);
    }

    @Override
    public Settings getSettings() {
        return this.settings;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("settings", this.settings.toTag());
        tag.put("restock_containers", RestockConfig.toTag()); // was: UkMm7uisd.FvaNWO()
        NbtList buttonList = new NbtList();
        for (CustomHudButton btn : this.customButtons) buttonList.add(btn.toTag()); // was: btn.rKbT3Ifwo()
        tag.put("screen-button-editor", buttonList);
        return tag;
    }

    @Override
    public MusheorSystem fromTag(NbtCompound tag) {
        if (tag.contains("settings")) {
            this.settings.fromTag(VersionHelper.get().getCompound(tag, "settings"));
        }
        if (tag.contains("restock_containers")) {
            RestockConfig.fromTag(VersionHelper.get().getCompound(tag, "restock_containers")); // was: UkMm7uisd.FvaNWO(NbtCompound)
        }
        this.customButtons.clear();
        if (tag.contains("screen-button-editor")) {
            for (NbtElement el : VersionHelper.get().getList(tag, "screen-button-editor", 10)) {
                this.customButtons.add(CustomHudButton.fromTag((NbtCompound) el)); // was: CustomHudButton.FvaNWO(NbtCompound)
            }
        }
        return this;
    }

    /** Logs a timestamped, formatted message to chat when debug-mode is enabled (and no args are null). */
    public static void debug(String format, Object... args) {
        if (format == null || args == null) return;
        for (Object arg : args) {
            if (arg == null) return;
        }
        String formattedMessage = String.format(format, args);
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String messageWithTimestamp = String.format("[%s] %s", timestamp, formattedMessage);
        if (Manager.enableChatDebugging.get() && messageWithTimestamp.length() < 256) {
            ChatUtils.info(messageWithTimestamp);
        }
    }

    /** Block-render colouring mode: a single static colour or automatic per-block colour mapping. */
    public enum RenderType { Static, Mapped }

    /** Tab-list rendering scope. */
    public enum TabListMode { All, Friends }
}
