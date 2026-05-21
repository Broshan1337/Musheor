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
import net.minecraft.AbstractClientPlayerEntity;   // BlockItem
import net.minecraft.ItemStack;   // Item
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.Items;   // Items
import net.minecraft.class_2480;   // ShulkerBoxBlock
import net.minecraft.class_2596;   // Packet
import net.minecraft.class_2828;   // PlayerActionC2SPacket
import net.minecraft.MinecraftClient;    // MinecraftClient

/**
 * Automatically drops unwanted items from the player's inventory.
 *
 * Supports Blacklist mode (drop selected items) and Whitelist mode (keep only selected items).
 * The "Blacklist inventory" button scans the current inventory and blacklists everything in it.
 * Items can optionally be thrown backward (rotateDrop) to avoid them landing in front of the player.
 */
public class InventoryCleaner extends Module {
    private static final MinecraftClient mc = MinecraftClient.method_1551(); // was: RG4EUBK1NAGPn74

    private final SettingGroup sgGeneral;
    private final Setting<Boolean> dropEmptyShulkers;
    private final Setting<Boolean> ignoreHotbar;
    private final Setting<ItemFilterMode> itemFilter;  // was: itemFilter (type was ItemFilterList)
    private final Setting<List<ItemStack>> dropItemList;
    private final Setting<Integer> dropDelay;
    private final Setting<Boolean> rotateDrop;

    private int ticksSinceLastDrop = 0; // was: ydtYMNpam8iL7Z8
    List<ItemStack> cachedDropList;    // was: LFK4tb0B

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
            .defaultValue(new ItemStack[]{Items.field_8328}) // packed_ice
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
        this.cachedDropList = new ArrayList<ItemStack>((Collection) this.dropItemList.get());
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
        if (mc.field_1724 == null || mc.field_1687 == null) return;
        if (WorldUtils.isScreenOpen()) return; // was: btLCQHvKVR

        int startSlot = ((Boolean) this.ignoreHotbar.get()) ? 9 : 0;
        for (int i = startSlot; i < mc.player.getId().field_7547.size(); ++i) {
            if (this.ticksSinceLastDrop < (Integer) this.dropDelay.get()) {
                ++this.ticksSinceLastDrop;
                return;
            }
            ItemStack stack = mc.player.getId().method_5438(i); // getStack(i)

            // Drop empty shulker boxes
            if (stack.getStack() instanceof AbstractClientPlayerEntity  // BlockItem
                    && ((AbstractClientPlayerEntity) stack.getStack()).method_7711() instanceof class_2480 // ShulkerBoxBlock
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
                    && ((List<?>) this.dropItemList.get()).contains(stack.getStack()))
                || (this.itemFilter.get() == ItemFilterMode.WHITELIST
                    && !((List<?>) this.dropItemList.get()).contains(stack.getStack()));

            if (stack.getStack() == Items.field_8162 || !shouldDrop) continue; // air = skip

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
        if (mc.field_1724 == null || mc.field_1687 == null) return;
        ((List<?>) this.dropItemList.get()).clear();
        this.itemFilter.set(ItemFilterMode.BLACKLIST);
        for (int i = 0; i <= mc.player.getId().method_5439(); ++i) { // getSize()
            ItemStack item = mc.player.getId().method_5438(i).getStack();
            if (((List<?>) this.dropItemList.get()).contains(item)) continue;
            ((List<ItemStack>) this.dropItemList.get()).add(item);
        }
        ChatUtils.info("Inventory scanned and blacklisted!", new Object[0]);
    }

    /**
     * Temporarily turns the player 180° to drop the item behind them,
     * then restores the original yaw.
     */
    private void dropBehind(int slot) { // was: mp3zoXQFKUKYj5(int)
        assert mc.field_1724 != null;
        float originalYaw = mc.player.method_36454(); // getYaw()
        PlayerUtils.setAutoWalkActive(false); // was: KP44bk(false)
        mc.player.field_3944.method_52787( // sendPacket
            (class_2596) new class_2828.class_2831( // PlayerActionC2SPacket.LookAtEntityS2CPacket (look-only move)
                WorldUtils.Direction8.getOppositeDirection(mc.player.method_36454()), // was: TAdu5cndwWu3A1(float)
                -20.0f,
                mc.player.method_24828(), // isOnGround
                false));
        InvUtils.drop().slot(slot);
        mc.player.method_36456(originalYaw); // setYaw
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
