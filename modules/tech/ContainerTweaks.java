// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.tech;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import musheor.musheor;
import musheor.modules.automation.InventoryManager;
import musheor.utils.internal.RateController;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * "container-tweaks" — advanced container stealing/dumping. Adds steal/dump/bundle buttons
 * to container screens and keybinds to move all items or only items matching a clicked slot
 * between the player inventory and the open container (optionally shulkers only). Moves run
 * on Meteor's executor and are throttled to avoid inventory packet kicks.
 */
public class ContainerTweaks extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();   // was: sZkZ1izAy
    private final MinecraftClient mc = MinecraftClient.getInstance();         // was: QYKUhjp
    public static ContainerTweaks INSTANCE;                                   // was: FvaNWO (static)

    private final Setting<Boolean> onlyShulkers = sgGeneral.add(new BoolSetting.Builder() // was: NIz4xic3Js9
        .name("only-shulkers").description("Only moves shulkers.").defaultValue(true).build());
    public final Setting<Boolean> showButtons = sgGeneral.add(new BoolSetting.Builder() // was: Q90GLXQ0Pef
        .name("show-buttons").description("Shows stealing and dumping buttons.").defaultValue(true).build());
    public final Setting<Keybind> moveMatchingKey = sgGeneral.add(new KeybindSetting.Builder() // was: psJq59YIbp3Z
        .name("move-matching").description("Moves all matching items between inventories.").defaultValue(Keybind.none()).build());
    public final Setting<Keybind> moveAllKey = sgGeneral.add(new KeybindSetting.Builder() // was: SOYyh5IPg26f7F
        .name("move-all").description("Moves all items from one inventory to the other.").defaultValue(Keybind.none()).build());
    public final Setting<List<Item>> bundleWhitelist = sgGeneral.add(new ItemListSetting.Builder() // was: rKbT3Ifwo
        .name("bundle-item-whitelist").description("Items that are allowed to be bundled / unbundled").defaultValue(new ArrayList<>()).build());
    public final Setting<Boolean> noInvPacketKicks = sgGeneral.add(new BoolSetting.Builder() // was: r7hOYIKN2
        .name("no-inv-packet-kicks").description("Prevent inventory slot actions from getting you packet kicked").defaultValue(true).build());
    public final Setting<Integer> stealOffsetX = sgGeneral.add(new IntSetting.Builder().name("steal-offset-x").defaultValue(0).visible(() -> false).build());     // was: oZHMlTL
    public final Setting<Integer> stealOffsetY = sgGeneral.add(new IntSetting.Builder().name("steal-offset-y").defaultValue(0).visible(() -> false).build());     // was: xQr5FhbwpQPWgIQ
    public final Setting<Integer> dumpOffsetX = sgGeneral.add(new IntSetting.Builder().name("dump-offset-x").defaultValue(0).visible(() -> false).build());       // was: OMMZL1F3q
    public final Setting<Integer> dumpOffsetY = sgGeneral.add(new IntSetting.Builder().name("dump-offset-y").defaultValue(0).visible(() -> false).build());       // was: zu3a44xDeMFMCRwm
    public final Setting<Integer> bundleOffsetX = sgGeneral.add(new IntSetting.Builder().name("bundle-offset-x").defaultValue(0).visible(() -> false).build());   // was: krxNb5lcQuWA
    public final Setting<Integer> bundleOffsetY = sgGeneral.add(new IntSetting.Builder().name("bundle-offset-y").defaultValue(0).visible(() -> false).build());   // was: nt0HZnvBBp
    public final Setting<Integer> unbundleOffsetX = sgGeneral.add(new IntSetting.Builder().name("unbundle-offset-x").defaultValue(0).visible(() -> false).build()); // was: amz3UB1vE
    public final Setting<Integer> unbundleOffsetY = sgGeneral.add(new IntSetting.Builder().name("unbundle-offset-y").defaultValue(0).visible(() -> false).build()); // was: sBBIyQG5NWq0K

    public ContainerTweaks() {
        super(musheor.MAIN, "container-tweaks", "A more advanced way to steal and store items in containers");
        INSTANCE = this;
    }

    /** Quick-moves slots in [start, end); {@code steal}=true pulls into the player inventory. */
    private void moveRange(ScreenHandler handler, int start, int end, boolean steal) { // was: FvaNWO(ScreenHandler,int,int,boolean)
        for (int i = start; i < end && !this.checkPacketKick()
            && (InventoryManager.countEmptyInventorySlots() != 0 || !steal)
            && (InventoryManager.countEmptyContainerSlots(handler) != 0 || steal); i++) {
            if (handler.getSlot(i).hasStack()) {
                ItemStack stack = handler.getSlot(i).getStack();
                if (this.isShulker(stack) || !this.onlyShulkers.get()) {
                    if (this.mc.currentScreen == null || !Utils.canUpdate()) break;
                    this.mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                }
            }
        }
    }

    /** Moves every item matching the clicked slot's item across to the other inventory. */
    public void moveMatching(ScreenHandler handler, Slot clickedSlot) { // was: FvaNWO(ScreenHandler,Slot)
        ItemStack target = clickedSlot.getStack().copy();
        if (target.isEmpty()) return;
        boolean clickedInPlayerInv = clickedSlot.inventory == this.mc.player.getInventory();
        MeteorExecutor.execute(() -> {
            for (int i = 0; i < handler.slots.size()
                && (InventoryManager.countEmptyInventorySlots() != 0 || clickedInPlayerInv)
                && (InventoryManager.countEmptyContainerSlots(handler) != 0 || !clickedInPlayerInv)
                && !this.checkPacketKick(); i++) {
                Slot slot = handler.getSlot(i);
                if ((clickedInPlayerInv ? slot.inventory == this.mc.player.getInventory() : slot.inventory != this.mc.player.getInventory())
                    && slot.hasStack() && (this.isShulker(slot.getStack()) || !this.onlyShulkers.get())
                    && ItemStack.areItemsAndComponentsEqual(slot.getStack(), target)) {
                    if (this.mc.currentScreen == null || !Utils.canUpdate()) break;
                    this.mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                }
            }
        });
    }

    /** Moves all items in the clicked slot's inventory across to the other inventory. */
    public void moveAll(ScreenHandler handler, Slot clickedSlot) { // was: Q90GLXQ0Pef(ScreenHandler,Slot)
        ItemStack target = clickedSlot.getStack();
        if (target.isEmpty()) return;
        boolean clickedInPlayerInv = clickedSlot.inventory == this.mc.player.getInventory();
        MeteorExecutor.execute(() -> {
            for (int i = 0; i < handler.slots.size()
                && (InventoryManager.countEmptyInventorySlots() != 0 || clickedInPlayerInv)
                && (InventoryManager.countEmptyContainerSlots(handler) != 0 || !clickedInPlayerInv)
                && !this.checkPacketKick(); i++) {
                Slot slot = handler.getSlot(i);
                if ((clickedInPlayerInv ? slot.inventory == this.mc.player.getInventory() : slot.inventory != this.mc.player.getInventory())
                    && slot.hasStack() && (this.isShulker(slot.getStack()) || !this.onlyShulkers.get())) {
                    if (this.mc.currentScreen == null || !Utils.canUpdate()) break;
                    this.mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                }
            }
        });
    }

    /** Steals the container contents into the player inventory. */
    public void steal(ScreenHandler handler) { // was: FvaNWO(ScreenHandler)
        MeteorExecutor.execute(() -> this.moveRange(handler, 0, SlotUtils.indexToId(9), true));
    }

    /** Dumps the player inventory into the container. */
    public void dump(ScreenHandler handler) { // was: Q90GLXQ0Pef(ScreenHandler)
        int playerInvOffset = SlotUtils.indexToId(9);
        MeteorExecutor.execute(() -> this.moveRange(handler, playerInvOffset, playerInvOffset + 36, false));
    }

    /** True (and warns) if the inventory-packet rate limit would be exceeded. */
    private boolean checkPacketKick() { // was: FvaNWO()
        if (!RateController.canSendInventoryPacket()) {
            this.warning("§cCancelled slot movement to prevent packet kick!");
            return true;
        }
        return false;
    }

    /** True if {@code stack} is a shulker box. */
    boolean isShulker(ItemStack stack) { // was: FvaNWO(ItemStack)
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
    }
}
