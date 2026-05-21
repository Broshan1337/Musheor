// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import net.minecraft.class_2487;  // NbtCompound
import net.minecraft.class_2499;  // NbtList
import net.minecraft.class_2520;  // NbtElement

/**
 * Musheor's global System singleton, registered alongside Meteor's systems.
 *
 * Accessible via {@code MusheorSystem.get()} or the convenience alias
 * {@code MusheorSystem.Manager} (set in the constructor).
 *
 * Persists to the Meteor NBT save file and contains:
 *   - Global placement/breaking/packet/render settings
 *   - Custom HUD button definitions
 *   - RestockConfig container list (serialised separately)
 */
public class MusheorSystem extends System<MusheorSystem> {
    public final Settings settings = new Settings();

    /** Convenience static reference set during construction. Same as {@code get()}. */
    public static MusheorSystem Manager;

    /** Set to true when a render setting changes so the settings tab knows to reload. */
    public boolean update;

    /** User-defined HUD buttons managed through the Custom HUD Button editor screen. */
    public final List<CustomHudButton> customButtons = new ArrayList<CustomHudButton>();

    // -------------------------------------------------------------------------
    // Setting groups
    // -------------------------------------------------------------------------
    SettingGroup placementManagerSettings = this.settings.createGroup("Placement-Manager");
    SettingGroup breakManagerSettings     = this.settings.createGroup("Break-Manager");
    SettingGroup packetManagerSettings    = this.settings.createGroup("Packet-Manager");
    SettingGroup renderManagerSettings    = this.settings.createGroup("Render-Manager");

    // ---- Default group ----
    public final Setting<Boolean> enableChatDebugging =
        this.settings.getDefaultGroup().add(new BoolSetting.Builder()
            .name("debug-mode")
            .description("Enable debug messages in chat.")
            .defaultValue(false)
            .build());

    public final Setting<Boolean> disableMutuals =
        this.settings.getDefaultGroup().add(new BoolSetting.Builder()
            .name("disable-capes-and-glowing")
            .description("Stops your client from rendering your own and other people's Musheor capes and glowing effect.")
            .defaultValue(false)
            .build());

    public final Setting<Keybind> openEditor =
        this.settings.getDefaultGroup().add(new KeybindSetting.Builder()
            .name("open-button-editor-bind")
            .description("Open the hud-button editor.")
            .defaultValue(Keybind.fromKey(345))
            .build());

    // ---- Placement-Manager ----
    public final Setting<Double> placementRange =
        this.placementManagerSettings.add(new DoubleSetting.Builder()
            .name("place-range")
            .description("In what range the player can / should place blocks")
            .defaultValue(4.5).min(1.0).max(8.0).decimalPlaces(1)
            .build());

    public final Setting<Integer> placementTimeout =
        this.placementManagerSettings.add(new IntSetting.Builder()
            .name("placement-timeout")
            .description("Ticks to wait before retrying a failed placement (higher = safer for high ping)")
            .defaultValue(5).min(1).max(20)
            .build());

    public final Setting<Integer> swapDelay =
        this.placementManagerSettings.add(new IntSetting.Builder()
            .name("swap-delay")
            .description("Ticks to wait after swapping to an item (higher = safer for high ping)")
            .defaultValue(3).min(1).max(10)
            .build());

    // ---- Break-Manager ----
    public final Setting<Boolean> grimBypass =
        this.breakManagerSettings.add(new BoolSetting.Builder()
            .name("grim-bypass")
            .defaultValue(true)
            .build());

    public final Setting<Double> breakThreshold =
        this.breakManagerSettings.add(new DoubleSetting.Builder()
            .name("break-threshold")
            .defaultValue(0.7).min(0.1).max(1.0).decimalPlaces(2)
            .build());

    public final Setting<Boolean> doubleBreak =
        this.breakManagerSettings.add(new BoolSetting.Builder()
            .name("double-break")
            .defaultValue(true)
            .build());

    public final Setting<Boolean> validateBreak =
        this.breakManagerSettings.add(new BoolSetting.Builder()
            .name("validate-break")
            .description("Waits for the server to validate the break. Disable when you have high ping (may cause rubberbanding)")
            .defaultValue(true)
            .build());

    public final Setting<Integer> rebreakTimeout =
        this.breakManagerSettings.add(new IntSetting.Builder()
            .name("break-timeout")
            .description("Time in ticks to wait before attempting to break the same block again (higher = safer for high ping)")
            .defaultValue(3).min(1).max(20)
            .build());

    public final Setting<Boolean> preventToolBreaking =
        this.breakManagerSettings.add(new BoolSetting.Builder()
            .name("prevent-tool-breaking")
            .description("Prevents you from breaking your tool items")
            .defaultValue(true)
            .build());

    public final Setting<Integer> minToolDurability =
        this.breakManagerSettings.add(new IntSetting.Builder()
            .name("min-tool-durability")
            .description("The minimum durability threshold of a usable tool")
            .defaultValue(10).sliderRange(0, 250)
            .visible(() -> this.preventToolBreaking.get())
            .build());

    // ---- Packet-Manager ----
    public final Setting<Integer> globalPacketLimit =
        this.packetManagerSettings.add(new IntSetting.Builder()
            .name("global-packet-limit")
            .description("Packet limit before the server kicks you")
            .defaultValue(1349).sliderMax(1500)
            .build());

    public final Setting<Integer> invPacketLimit =
        this.packetManagerSettings.add(new IntSetting.Builder()
            .name("inventory-packet-limit")
            .description("Inventory action limiter before the server kicks you")
            .defaultValue(79).sliderMax(150)
            .build());

    // ---- Render-Manager ----
    public final Setting<Boolean> placeRender =
        this.renderManagerSettings.add(new BoolSetting.Builder()
            .name("enable-placement-rendering")
            .description("Renders the positions of blocks in relation to placing")
            .defaultValue(true)
            .onChanged(bl -> { this.update = true; })
            .build());

    public final Setting<Boolean> breakRender =
        this.renderManagerSettings.add(new BoolSetting.Builder()
            .name("enable-break-rendering")
            .description("Renders the positions of blocks in relation to breaking")
            .defaultValue(true)
            .onChanged(bl -> { this.update = true; })
            .build());

    public final Setting<RenderType> renderType =
        this.renderManagerSettings.add(new EnumSetting.Builder<RenderType>()
            .name("render-type")
            .description("Choose what type of rendering applies, either single-color or automatic color mapping")
            .defaultValue(RenderType.Static)
            .visible(() -> (Boolean) this.placeRender.get() != false || (Boolean) this.breakRender.get() != false)
            .onChanged(renderType -> { this.update = true; })
            .build());

    public final Setting<ShapeMode> renderShape =
        this.renderManagerSettings.add(new EnumSetting.Builder<ShapeMode>()
            .name("render-shape")
            .description("What kind of shape (lines, sides or both) block positions get rendered as")
            .defaultValue(ShapeMode.Lines)
            .visible(() -> (Boolean) this.placeRender.get() != false || (Boolean) this.breakRender.get() != false)
            .onChanged(shapeMode -> { this.update = true; })
            .build());

    public final Setting<Boolean> renderLines =
        this.renderManagerSettings.add(new BoolSetting.Builder()
            .name("render-lines")
            .description("Renders the outline of blocks / positions")
            .defaultValue(true)
            .visible(() -> this.renderShape.get() == ShapeMode.Lines
                       || this.renderShape.get() == ShapeMode.Both
                           && ((Boolean) this.placeRender.get() != false || (Boolean) this.breakRender.get() != false))
            .onChanged(bl -> { this.update = true; })
            .build());

    public final Setting<SettingColor> renderLineColor =
        this.renderManagerSettings.add(new ColorSetting.Builder()
            .name("render-line-color")
            .description("Line color of rendered blocks")
            .defaultValue(new SettingColor(0, 225, 255, 200))
            .visible(() -> !(this.renderShape.get() != ShapeMode.Lines && this.renderShape.get() != ShapeMode.Both
                           || (Boolean) this.renderLines.get() == false
                           || this.renderType.get() != RenderType.Static
                           || (Boolean) this.placeRender.get() == false && (Boolean) this.breakRender.get() == false))
            .build());

    public final Setting<Integer> renderLineAlpha =
        this.renderManagerSettings.add(new IntSetting.Builder()
            .name("render-line-alpha")
            .description("Line color intensity of rendered blocks")
            .defaultValue(200).min(0).max(255).sliderMin(0).sliderMax(255)
            .visible(() -> !(this.renderShape.get() != ShapeMode.Lines && this.renderShape.get() != ShapeMode.Both
                           || (Boolean) this.renderLines.get() == false
                           || this.renderType.get() != RenderType.Mapped
                           || (Boolean) this.placeRender.get() == false && (Boolean) this.breakRender.get() == false))
            .build());

    public final Setting<Boolean> renderSides =
        this.renderManagerSettings.add(new BoolSetting.Builder()
            .name("render-sides")
            .description("Renders the sides of blocks / positions")
            .defaultValue(false)
            .visible(() -> this.renderShape.get() == ShapeMode.Sides
                       || this.renderShape.get() == ShapeMode.Both
                           && ((Boolean) this.placeRender.get() != false || (Boolean) this.breakRender.get() != false))
            .onChanged(bl -> { this.update = true; })
            .build());

    public final Setting<SettingColor> renderSideColor =
        this.renderManagerSettings.add(new ColorSetting.Builder()
            .name("render-side-color")
            .description("Side color of rendered blocks")
            .defaultValue(new SettingColor(0, 200, 255, 100))
            .visible(() -> !(this.renderShape.get() != ShapeMode.Sides && this.renderShape.get() != ShapeMode.Both
                           || (Boolean) this.renderSides.get() == false
                           || this.renderType.get() != RenderType.Static
                           || (Boolean) this.placeRender.get() == false && (Boolean) this.breakRender.get() == false))
            .build());

    public final Setting<Integer> renderSideAlpha =
        this.renderManagerSettings.add(new IntSetting.Builder()
            .name("render-side-alpha")
            .description("Side color intensity of rendered blocks")
            .defaultValue(100).min(0).max(255).sliderMin(0).sliderMax(255)
            .visible(() -> !(this.renderShape.get() != ShapeMode.Sides && this.renderShape.get() != ShapeMode.Both
                           || (Boolean) this.renderSides.get() == false
                           || this.renderType.get() != RenderType.Mapped
                           || (Boolean) this.placeRender.get() == false && (Boolean) this.breakRender.get() == false))
            .build());

    // -------------------------------------------------------------------------

    public MusheorSystem() {
        super("musheor");
        Manager = this;
    }

    public static MusheorSystem get() {
        return (MusheorSystem) Systems.get(MusheorSystem.class);
    }

    @Override
    public Settings getSettings() {
        return this.settings;
    }

    @Override
    public class_2487 toTag() { // NbtCompound
        class_2487 tag = new class_2487();
        tag.method_10566("settings", (class_2520) this.settings.toTag());
        tag.method_10566("restock_containers", (class_2520) RestockConfig.toTag());
        class_2499 buttonList = new class_2499();
        for (CustomHudButton button : this.customButtons) {
            buttonList.add(button.toTag());
        }
        tag.method_10566("screen-button-editor", (class_2520) buttonList);
        return tag;
    }

    @Override
    public MusheorSystem fromTag(class_2487 tag) {
        if (tag.method_10545("settings")) {
            this.settings.fromTag(VersionHelper.get().getCompound(tag, "settings"));
        }
        if (tag.method_10545("restock_containers")) {
            RestockConfig.fromTag(VersionHelper.get().getCompound(tag, "restock_containers")); // was: jOdDDFXSeWl4
        }
        this.customButtons.clear();
        if (VersionHelper.get().supportsHudButtons() && tag.method_10545("custom_hud_buttons")) {
            class_2499 list = VersionHelper.get().getList(tag, "custom_hud_buttons", 10);
            for (class_2520 element : list) {
                CustomHudButton button = CustomHudButton.fromTag((class_2487) element); // was: mp3zoXQFKUKYj5
                this.customButtons.add(button);
            }
        }
        return this;
    }

    /**
     * Logs a formatted debug message to chat if debug-mode is enabled.
     * Silently skips if any argument is null or the message exceeds 255 characters.
     */
    public static void debug(String format, Object... args) {
        if (format == null || args == null) return;
        for (Object arg : args) {
            if (arg == null) return;
        }
        String message = String.format(format, args);
        String time    = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String out     = String.format("[%s] %s", time, message);
        if ((Boolean) MusheorSystem.Manager.enableChatDebugging.get() && out.length() < 256) {
            ChatUtils.info(out, new Object[0]);
        }
    }

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    /** Controls whether block render colours are user-defined (Static) or position-mapped (Mapped). */
    public static final class RenderType extends Enum<RenderType> {
        public static final RenderType Static = new RenderType();
        public static final RenderType Mapped = new RenderType();
        private static final RenderType[] $VALUES = new RenderType[]{Static, Mapped};

        public static RenderType[] values()             { return (RenderType[]) $VALUES.clone(); }
        public static RenderType valueOf(String string) { return Enum.valueOf(RenderType.class, string); }
    }

    /** Controls which players appear in the Musheor tab-list overlay: All or Friends only. */
    public static final class TabListMode extends Enum<TabListMode> {
        public static final TabListMode All     = new TabListMode();
        public static final TabListMode Friends = new TabListMode();
        private static final TabListMode[] $VALUES = new TabListMode[]{All, Friends};

        public static TabListMode[] values()             { return (TabListMode[]) $VALUES.clone(); }
        public static TabListMode valueOf(String string) { return Enum.valueOf(TabListMode.class, string); }
    }
}
