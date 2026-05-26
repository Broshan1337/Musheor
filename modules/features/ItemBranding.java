// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.features;

import java.util.ArrayDeque;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;

public class ItemBranding
extends Module {
    private final Setting<List<Item>> list;
    private final Setting<String> name;
    private final Setting<Integer> packetLimit;
    private final Setting<Boolean> closeOnDone;
    private final Setting<Boolean> disableOnDone;

    public ItemBranding() {
        super(musheor.MAIN, "item-branding", "Renames specific items really fast in anvils");
        this.list = this.settings.getDefaultGroup().add((Setting)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("items")).description("Items to automatically rename (or exclude from being renamed, if blacklist mode is enabled.)")).build());
        this.name = this.settings.getDefaultGroup().add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("item-name")).description("The name you want to give the item(s).")).defaultValue((Object)"")).onChanged(string -> {
            if (string.length() > 50) {
                ChatUtils.warning((String)"\u00a74Custom name exceeds max length!", (Object[])new Object[]{this.name});
            }
        })).build());
        this.packetLimit = this.settings.getDefaultGroup().add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("packet-limit")).description("Decrease this if the server is kicking you.")).min(20).sliderMax(100).defaultValue((Object)60)).build());
        this.closeOnDone = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("close-anvil")).description("Automatically close the anvil screen when no more items can be renamed.")).defaultValue((Object)true)).build());
        this.disableOnDone = this.settings.getDefaultGroup().add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-on-done")).description("Automatically disable the module when no more items can be renamed.")).defaultValue((Object)false)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        ScreenHandler screenHandler;
        if (this.mc.player == null || this.mc.world == null || this.mc.currentScreen == null) {
            return;
        }
        if (!(this.mc.currentScreen instanceof AnvilScreen) || !((screenHandler = this.mc.player.currentScreenHandler) instanceof AnvilScreenHandler)) {
            return;
        }
        AnvilScreenHandler anvilHandler = (AnvilScreenHandler)screenHandler;
        ItemStack stack = anvilHandler.getSlot(0).getStack();
        if (stack.isEmpty()) {
            ArrayDeque arrayDeque = new ArrayDeque();
        }
    }
}