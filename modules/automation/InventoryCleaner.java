// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import musheor.utils.InventoryManager;
import musheor.utils.PlayerUtils;
import musheor.utils.WorldUtils;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Automatically drops unwanted items from the player's inventory.
 *
 * Supports Blacklist mode (drop selected items) and Whitelist mode (keep only selected items).
 * The "Blacklist inventory" button scans the current inventory and blacklists everything in it.
 * Items can optionally be thrown backward (rotateDrop) to avoid them landing in front of the player.
 */
public class InventoryCleaner extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: method_1551, RG4EUBK1NAGPn74

    private final SettingGroup sgGeneral;
    private final Setting<Boolean> dropEmptyShulkers;
    private final Setting<Boolean> ignoreHotbar;
    private final Setting<ItemFilterMode> itemFilter;  // was: itemFilter (type was ItemFilterList)
    private final Setting<List<Item>> dropItemList;
    private final Setting<Integer> dropDelay;
    private final Setting<Boolean> rotateDrop;

    private int ticksSinceLastDrop = 0; // was: ydtYMNpam8iL7Z8
    List<Item> cachedDropList;    // was: LFK4tb0B

    public InventoryCleaner() {
        super(musheor.AUTOMATION, "inv-cleaner", "Automatically dispose of unwanted items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.dropEmptyShulkers = this.sgGeneral.add(new BoolSetting.Builder()
            .name("drop-empty-shulker")
            .description("Allow the paver to dispose of empty shulkerboxes.")
            .defaultValue(true).build());
        this.ignoreHotbar = this.sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-hotbar")
            .description("Ignore items in the player's hotbar.")
            .defaultValue(true).build());
        this.itemFilter = this.sgGeneral.add(new EnumSetting.Builder<ItemFilterMode>()
            .name("item-filter")
            .description("Whitelist keeps selected items in the inventory, Blacklist throws selected items out.")
            .defaultValue(ItemFilterMode.BLACKLIST) // was: rPJDpX
            .build());
        this.dropItemList = this.sgGeneral.add(new ItemListSetting.Builder()
            .name("drop-item-list")
            .description("List of items.")
            .defaultValue(new Item[]{Items.NETHERRACK})
            .build());
        this.dropDelay = this.sgGeneral.add(new IntSetting.Builder()
            .name("drop-delay")
            .description("Delay between dropping items.")
            .defaultValue(2).sliderRange(1, 20).build());
        this.rotateDrop = this.sgGeneral.add(new BoolSetting.Builder()
            .name("rotate-drop")
            .description("Drop items behind you")
            .defaultValue(true).build());
        this.ticksSinceLastDrop = 0;
        this.cachedDropList = new ArrayList<Item>((Collection) this.dropItemList.get());
    }

    @Override
    public WWidget getWidget(GuiTheme guiTheme) {
        WVerticalList list = guiTheme.verticalList();
        WButton button = (WButton) list.add((WWidget) guiTheme.button("Blacklist inventory")).widget();
        button.action = this::blacklistCurrentInventory; // was: qfVsw28lZNgTVJ
        return list;
    }

    @Override
    public void onDeactivate() {
        this.ticksSinceLastDrop = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (mc.player == null || mc.world == null) return;
        if (WorldUtils.checkForLag()) return;

        int startSlot = ((Boolean) this.ignoreHotbar.get()) ? 9 : 0;
        for (int i = startSlot; i < mc.player.getInventory().main.size(); ++i) {
            if (this.ticksSinceLastDrop < (Integer) this.dropDelay.get()) {
                ++this.ticksSinceLastDrop;
                return;
            }
            ItemStack stack = mc.player.getInventory().getStack(i); // getStack(i)

            // Drop empty shulker boxes
            if (stack.getItem() instanceof BlockItem
                    && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock
                    && ((Boolean) this.dropEmptyShulkers.get())
                    && InventoryManager.isShulkerEmpty(stack)) { // was: UgB10d(ItemStack)
                if ((Boolean) this.rotateDrop.get()) {
                    dropBehind(i);
                } else {
                    InvUtils.drop().slot(i);
                }
                this.ticksSinceLastDrop = 0;
            }

            // Apply blacklist/whitelist filter
            boolean shouldDrop = (this.itemFilter.get() == ItemFilterMode.BLACKLIST
                    && ((List<?>) this.dropItemList.get()).contains(stack.getItem()))
                || (this.itemFilter.get() == ItemFilterMode.WHITELIST
                    && !((List<?>) this.dropItemList.get()).contains(stack.getItem()));

            if (stack.isEmpty() || !shouldDrop) continue;

            if ((Boolean) this.rotateDrop.get()) {
                dropBehind(i);
                this.ticksSinceLastDrop = 0;
            } else {
                InvUtils.drop().slot(i);
                this.ticksSinceLastDrop = 0;
            }
        }
    }

    /** Scans the current inventory and adds all item types to the blacklist. */
    private void blacklistCurrentInventory() { // was: qfVsw28lZNgTVJ
        if (mc.player == null || mc.world == null) return;
        ((List<?>) this.dropItemList.get()).clear();
        this.itemFilter.set(ItemFilterMode.BLACKLIST);
        for (int i = 0; i <= mc.player.getInventory().size(); ++i) { // getSize()
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (((List<?>) this.dropItemList.get()).contains(item)) continue;
            ((List<Item>) this.dropItemList.get()).add(item);
        }
        ChatUtils.info("Inventory scanned and blacklisted!", new Object[0]);
    }

    /**
     * Temporarily turns the player 180° to drop the item behind them,
     * then restores the original yaw.
     */
    private void dropBehind(int slot) { // was: mp3zoXQFKUKYj5(int)
        assert mc.player != null;
        float originalYaw = mc.player.getYaw();
        PlayerUtils.setAutoWalkActive(false);
        mc.player.networkHandler.sendPacket(
            (Packet<?>) new PlayerMoveC2SPacket.LookAndOnGround(
                WorldUtils.Direction8.getOppositeDirection(mc.player.getYaw()),
                -20.0f,
                mc.player.isOnGround(),
                false));
        InvUtils.drop().slot(slot);
        mc.player.setYaw(originalYaw);
    }

    /** Controls whether the item list is treated as a blacklist or a whitelist. */
    public static final class ItemFilterMode extends Enum<ItemFilterMode> {
        public static final ItemFilterMode BLACKLIST = new ItemFilterMode(); // was: rPJDpX  (ordinal 0)
        public static final ItemFilterMode WHITELIST = new ItemFilterMode(); // was: Hp3MRJIoQ (ordinal 1)
        private static final ItemFilterMode[] $VALUES = new ItemFilterMode[]{BLACKLIST, WHITELIST};

        public static ItemFilterMode[] values()             { return (ItemFilterMode[]) $VALUES.clone(); }
        public static ItemFilterMode valueOf(String string) { return Enum.valueOf(ItemFilterMode.class, string); }
    }
}
