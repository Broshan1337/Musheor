// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.modules.automation.InventoryManager;
import musheor.utils.PlayerUtils;
import musheor.utils.WorldUtils;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * "inv-cleaner" — automatically drops unwanted items. Supports a whitelist (keep only
 * listed) or blacklist (drop listed) filter, optional disposal of empty shulker boxes
 * after a grace period (to avoid dropping shulkers whose contents haven't synced), and
 * an optional "rotate-drop" that briefly faces away so items land behind the player.
 */
public class InventoryCleaner extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: Q90GLXQ0Pef
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();  // was: psJq59YIbp3Z

    private final Setting<Boolean> dropEmptyShulker = sgGeneral.add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("drop-empty-shulker").description("Allow the paver to dispose of empty shulkerboxes.").defaultValue(true).build());
    private final Setting<Integer> shulkerGraceTicks = sgGeneral.add(new IntSetting.Builder() // was: rKbT3Ifwo
        .name("shulker-grace-ticks")
        .description("Ticks a shulker must appear empty before it is dropped. Prevents dropping freshly picked-up shulkers whose contents haven't synced from the server yet.")
        .defaultValue(5).sliderRange(2, 20).visible(dropEmptyShulker::get).build());
    private final Setting<Boolean> ignoreHotbar = sgGeneral.add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("ignore-hotbar").description("Ignore items in the player's hotbar.").defaultValue(true).build());
    private final Setting<ItemFilterList> itemFilter = sgGeneral.add(new EnumSetting.Builder<ItemFilterList>() // was: oZHMlTL
        .name("item-filter").description("Whitelist keeps selected items in the inventory, Blacklist throws selected items out.")
        .defaultValue(ItemFilterList.Whitelist).build());
    private final Setting<List<Item>> dropList = sgGeneral.add(new ItemListSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("drop-item-list").description("List of items.").defaultValue(Items.NETHERRACK).build()); // was: class_1802.field_8328 (NETHERRACK)
    private final Setting<Integer> dropDelay = sgGeneral.add(new IntSetting.Builder() // was: OMMZL1F3q
        .name("drop-delay").description("Delay between dropping items.").defaultValue(2).sliderRange(1, 20).build());
    private final Setting<Boolean> rotateDrop = sgGeneral.add(new BoolSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("rotate-drop").description("Drop items behind you").defaultValue(false).build());

    private int dropTimer = 0;                                     // was: krxNb5lcQuWA
    private final Map<Integer, Integer> emptyShulkerTicks = new HashMap<>(); // was: nt0HZnvBBp (slot -> consecutive empty ticks)

    public InventoryCleaner() {
        super(musheor.AUTOMATION, "inv-cleaner", "Automatically dispose of unwanted items.");
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WButton scanInventoryAndSetItems = list.add(theme.button("Blacklist inventory")).widget();
        scanInventoryAndSetItems.action = this::scanInventoryToWhitelist;
        return list;
    }

    @Override
    public void onDeactivate() {
        this.dropTimer = 0;
        this.emptyShulkerTicks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (mc.player == null || mc.world == null) return;
        int firstSlot = !this.ignoreHotbar.get() ? 0 : 9;

        // Track how long each shulker slot has continuously appeared empty.
        for (int i = firstSlot; i < mc.player.getInventory().main.size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock && InventoryManager.isShulkerEmpty(stack)) {
                this.emptyShulkerTicks.merge(i, 1, Integer::sum);
            } else {
                this.emptyShulkerTicks.remove(i);
            }
        }

        for (int i = firstSlot; i < mc.player.getInventory().main.size(); i++) {
            if (this.dropTimer < this.dropDelay.get()) {
                this.dropTimer++;
                return;
            }

            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() instanceof BlockItem) || !(((BlockItem) itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock)) {
                if (itemStack.getItem() != Items.AIR
                    && (this.itemFilter.get() != ItemFilterList.Whitelist || !this.dropList.get().contains(itemStack.getItem()))
                    && (this.itemFilter.get() != ItemFilterList.Blacklist || this.dropList.get().contains(itemStack.getItem()))) {
                    if (this.rotateDrop.get()) {
                        this.dropBehind(i);
                    } else {
                        InvUtils.drop().slot(i);
                    }
                    this.dropTimer = 0;
                }
            } else if (this.dropEmptyShulker.get()
                && InventoryManager.isShulkerEmpty(itemStack)
                && this.emptyShulkerTicks.getOrDefault(i, 0) >= this.shulkerGraceTicks.get()) {
                this.emptyShulkerTicks.remove(i);
                if (this.rotateDrop.get()) {
                    this.dropBehind(i);
                } else {
                    InvUtils.drop().slot(i);
                }
                this.dropTimer = 0;
            }
        }
    }

    /** Whitelists every item currently in the inventory (the "Blacklist inventory" button). */
    private void scanInventoryToWhitelist() { // was: FvaNWO()
        if (mc.player == null || mc.world == null) return;
        this.dropList.get().clear();
        this.itemFilter.set(ItemFilterList.Whitelist);

        for (int i = 0; i <= mc.player.getInventory().size(); i++) {
            Item currentItem = mc.player.getInventory().getStack(i).getItem();
            if (!this.dropList.get().contains(currentItem)) {
                this.dropList.get().add(currentItem);
            }
        }
        ChatUtils.info("Inventory scanned and blacklisted!");
    }

    /** Faces away (pitch -20) for a single drop so the item lands behind the player, then restores the yaw. */
    private void dropBehind(int i) { // was: FvaNWO(int)
        assert mc.player != null;
        float previousYaw = mc.player.getYaw();
        PlayerUtils.setAutoWalk(false);
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
            WorldUtils.Direction8.oppositeYaw(mc.player.getYaw()), -20.0F, mc.player.isOnGround(), false));
        InvUtils.drop().slot(i);
        mc.player.setYaw(previousYaw);
    }

    /** Item filter mode. */ // was: enum ItemFilterList {FvaNWO, Q90GLXQ0Pef}
    public enum ItemFilterList { Whitelist, Blacklist }
}
