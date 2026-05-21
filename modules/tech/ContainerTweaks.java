// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.tech;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import musheor.musheor;
import net.minecraft.MinecraftClient;

public class ContainerTweaks
extends Module {
    private final SettingGroup sgGeneral;
    private final MinecraftClient mkCP4NkhzFNK;
    public static ContainerTweaks j7OmRvH5go;
    private final Setting<Boolean> onlyShulkers;
    public final Setting<Boolean> showButtons;
    public final Setting<Keybind> moveMatchingKeybind;
    public final Setting<Keybind> moveAllKeybind;
    public final Setting<Boolean> noPacketKick;
    public final Setting<Integer> stealOffsetX;
    public final Setting<Integer> stealOffsetY;
    public final Setting<Integer> dumpOffsetX;
    public final Setting<Integer> dumpOffsetY;

    public ContainerTweaks() {
        super(musheor.MAIN, "container-tweaks", "A more advanced way to steal and store items in containers");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mkCP4NkhzFNK = MinecraftClient.getInstance();
        this.onlyShulkers = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-shulkers")).description("Only moves shulkers.")).defaultValue((Object)true)).build());
        this.showButtons = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-buttons")).description("Shows stealing and dumping buttons.")).defaultValue((Object)true)).build());
        this.moveMatchingKeybind = this.sgGeneral.add((Setting)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("move-matching")).description("Moves all matching items between inventories.")).defaultValue((Object)Keybind.none())).build());
        this.moveAllKeybind = this.sgGeneral.add((Setting)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("move-all")).description("Moves all items from one inventory to the other.")).defaultValue((Object)Keybind.none())).build());
        this.noPacketKick = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-inv-packet-kicks")).description("Prevent inventory slot actions from getting you packet kicked")).defaultValue((Object)true)).build());
        this.stealOffsetX = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("steal-offset-x")).defaultValue((Object)0)).visible(() -> false)).build());
        this.stealOffsetY = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("steal-offset-y")).defaultValue((Object)0)).visible(() -> false)).build());
        this.dumpOffsetX = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("dump-offset-x")).defaultValue((Object)0)).visible(() -> false)).build());
        this.dumpOffsetY = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("dump-offset-y")).defaultValue((Object)0)).visible(() -> false)).build());
        j7OmRvH5go = this;
    }
}

