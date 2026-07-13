// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.utils.PlayerUtils;
import musheor.utils.system.MusheorSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * "hotbar-replenish" — keeps each hotbar (and offhand) slot topped up with a configured
 * item, pulling replacements from the main inventory when a slot empties or drops below a
 * threshold. In "highway-mode" it instead keeps a usable (non-silk-touch, durable) pickaxe
 * in the first slot, matching HighwayBuilder's PAVE/DIG behaviour.
 */
public class HotbarReplenish extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: Q90GLXQ0Pef
    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: psJq59YIbp3Z

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder() // was: SOYyh5IPg26f7F
        .name("threshold").description("The threshold of items left to trigger replenishment.").defaultValue(16).min(1).sliderRange(1, 63).build());
    private final Setting<Boolean> highwayMode = sgGeneral.add(new BoolSetting.Builder() // was: rKbT3Ifwo
        .name("highway-mode").description("Enable this if you are using Better-Highway-Builder").defaultValue(false).build());
    private final Setting<Item> offhandItem = sgGeneral.add(new ItemSetting.Builder() // was: r7hOYIKN2
        .name("offhand-item").description("Item to maintain offhand slot..").defaultValue(Items.AIR).visible(() -> !highwayMode.get()).build());
    private final Setting<Item> slot1Item = sgGeneral.add(new ItemSetting.Builder() // was: oZHMlTL
        .name("slot-1-item").description("Item to maintain in the first hotbar slot.").defaultValue(Items.AIR).visible(() -> !highwayMode.get()).build());
    private final Setting<Item> slot2Item = sgGeneral.add(new ItemSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("slot-2-item").description("Item to maintain in the second hotbar slot.").defaultValue(Items.AIR).build());
    private final Setting<Item> slot3Item = sgGeneral.add(new ItemSetting.Builder() // was: OMMZL1F3q
        .name("slot-3-item").description("Item to maintain in the third hotbar slot.").defaultValue(Items.AIR).build());
    private final Setting<Item> slot4Item = sgGeneral.add(new ItemSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("slot-4-item").description("Item to maintain in the fourth hotbar slot.").defaultValue(Items.AIR).build());
    private final Setting<Item> slot5Item = sgGeneral.add(new ItemSetting.Builder() // was: krxNb5lcQuWA
        .name("slot-5-item").description("Item to maintain in the fifth hotbar slot.").defaultValue(Items.AIR).build());
    private final Setting<Item> slot6Item = sgGeneral.add(new ItemSetting.Builder() // was: nt0HZnvBBp
        .name("slot-6-item").description("Item to maintain in the sixth hotbar slot.").defaultValue(Items.AIR).build());
    private final Setting<Item> slot7Item = sgGeneral.add(new ItemSetting.Builder() // was: amz3UB1vE
        .name("slot-7-item").description("Item to maintain in the seventh hotbar slot.").defaultValue(Items.AIR).build());
    private final Setting<Item> slot8Item = sgGeneral.add(new ItemSetting.Builder() // was: sBBIyQG5NWq0K
        .name("slot-8-item").description("Item to maintain in the eighth hotbar slot.").defaultValue(Items.AIR).build());
    private final Setting<Item> slot9Item = sgGeneral.add(new ItemSetting.Builder() // was: sZkZ1izAy
        .name("slot-9-item").description("Item to maintain in the ninth hotbar slot.").defaultValue(Items.AIR).visible(() -> !highwayMode.get()).build());

    public HotbarReplenish() {
        super(musheor.AUTOMATION, "hotbar-replenish", "Automatically refills specific items in each hotbar slot. Each slot independantly configurable.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (mc.player == null || mc.world == null) return;
        if (this.highwayMode.get()) {
            // Slots 1 and 9 are reserved for the highway builder; force them empty.
            PlayerUtils.setModuleSetting(HotbarReplenish.class, "slot-1-item", Items.AIR);
            PlayerUtils.setModuleSetting(HotbarReplenish.class, "slot-9-item", Items.AIR);
            Item[] itemsToCheck = {
                this.slot2Item.get(), this.slot3Item.get(), this.slot4Item.get(), this.slot5Item.get(),
                this.slot6Item.get(), this.slot7Item.get(), this.slot8Item.get()
            };
            this.maintainPickaxe();
            for (int i = 1; i <= 7; i++) {
                this.replenishSlot(i, itemsToCheck[i - 1]);
            }
        } else {
            Item[] itemsToCheck = {
                this.slot1Item.get(), this.slot2Item.get(), this.slot3Item.get(), this.slot4Item.get(), this.slot5Item.get(),
                this.slot6Item.get(), this.slot7Item.get(), this.slot8Item.get(), this.slot9Item.get(), this.offhandItem.get()
            };
            for (int i = 0; i <= 7; i++) {
                this.replenishSlot(i, itemsToCheck[i]);
            }
            this.replenishSlot(45, this.offhandItem.get());
        }
    }

    /** Refills {@code slot} from the inventory if it is empty or below the threshold. */
    private void replenishSlot(int slot, Item desiredItem) { // was: FvaNWO(int,Item)
        assert mc.player != null;
        if (desiredItem == Items.AIR) return;

        ItemStack currentStack = slot == 45 ? mc.player.getOffHandStack() : mc.player.getInventory().getStack(slot);
        if (currentStack.isEmpty()) {
            int foundSlot = this.findRefillSlot(desiredItem, slot, 1);
            if (foundSlot != -1) this.moveItem(slot, foundSlot);
        } else if (currentStack.getItem() == desiredItem && currentStack.isStackable() && currentStack.getCount() <= this.threshold.get()) {
            int foundSlot = this.findRefillSlot(desiredItem, slot, this.threshold.get() - currentStack.getCount() + 1);
            if (foundSlot != -1) {
                ItemStack foundStack = mc.player.getInventory().getStack(foundSlot);
                if (ItemStack.areItemsAndComponentsEqual(currentStack, foundStack)) this.moveItem(slot, foundSlot);
            }
        }
    }

    /** Finds the inventory slot holding the most of {@code item} (>= {@code goodEnoughCount} short-circuits), skipping managed slots. */
    private int findRefillSlot(Item item, int excludedSlot, int goodEnoughCount) { // was: FvaNWO(Item,int,int)
        int slot = -1;
        int count = 0;
        assert mc.player != null;
        for (int i = 35; i >= 0; i--) {
            if (i != excludedSlot && !this.isManagedSlot(i)) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getItem() == item && stack.getCount() > count) {
                    slot = i;
                    count = stack.getCount();
                    if (count >= goodEnoughCount) break;
                }
            }
        }
        return slot;
    }

    /** True if {@code slot} is managed by a configured (non-AIR) setting and should not be raided. */
    private boolean isManagedSlot(int slot) { // was: FvaNWO(int)
        return slot == 0 && this.slot1Item.get() != Items.AIR
            || slot == 1 && this.slot2Item.get() != Items.AIR
            || slot == 2 && this.slot3Item.get() != Items.AIR
            || slot == 3 && this.slot4Item.get() != Items.AIR
            || slot == 4 && this.slot5Item.get() != Items.AIR
            || slot == 5 && this.slot6Item.get() != Items.AIR
            || slot == 6 && this.slot7Item.get() != Items.AIR
            || slot == 7 && this.slot8Item.get() != Items.AIR
            || slot == 8 && this.slot9Item.get() != Items.AIR
            || slot == 40 && this.offhandItem.get() != Items.AIR;
    }

    private void moveItem(int to, int from) { // was: FvaNWO(int,int)
        InvUtils.move().from(from).to(to);
    }

    /** Keeps a usable pickaxe in the first hotbar slot for highway building (PAVE/DIG aware). */
    private void maintainPickaxe() { // was: FvaNWO()
        assert mc.player != null;
        Inventory inventory = mc.player.getInventory();
        int firstHotbarSlot = 0;
        ItemStack firstHotbarStack = inventory.getStack(firstHotbarSlot);

        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.PAVE && VersionHelper.get().isPickaxe(firstHotbarStack)) {
            if (MusheorSystem.Manager.preventToolBreaking.get()) {
                if (firstHotbarStack.getMaxDamage() - firstHotbarStack.getDamage() > MusheorSystem.Manager.minToolDurability.get()
                    && !Utils.hasEnchantments(firstHotbarStack, Enchantments.SILK_TOUCH)) {
                    return;
                }
            } else if (!Utils.hasEnchantments(firstHotbarStack, Enchantments.SILK_TOUCH)) {
                return;
            }
        }

        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.DIG && VersionHelper.get().isPickaxe(firstHotbarStack)) {
            if (!MusheorSystem.Manager.preventToolBreaking.get()) return;
            if (firstHotbarStack.getMaxDamage() - firstHotbarStack.getDamage() > MusheorSystem.Manager.minToolDurability.get()) return;
        }

        int bestSlot = -1;
        for (int i = 0; i < inventory.size(); i++) {
            if (i == firstHotbarSlot) continue;
            ItemStack stack = inventory.getStack(i);
            if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.PAVE && VersionHelper.get().isPickaxe(stack)) {
                if (MusheorSystem.Manager.preventToolBreaking.get()) {
                    if (stack.getMaxDamage() - stack.getDamage() > MusheorSystem.Manager.minToolDurability.get()
                        && !Utils.hasEnchantments(stack, Enchantments.SILK_TOUCH)) {
                        bestSlot = i;
                        break;
                    }
                } else if (!Utils.hasEnchantments(stack, Enchantments.SILK_TOUCH)) {
                    bestSlot = i;
                    break;
                }
            }

            if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.DIG && VersionHelper.get().isPickaxe(stack)) {
                if (!MusheorSystem.Manager.preventToolBreaking.get()) {
                    bestSlot = i;
                    break;
                }
                if (stack.getMaxDamage() - stack.getDamage() > MusheorSystem.Manager.minToolDurability.get()) {
                    bestSlot = i;
                    break;
                }
            }
        }

        if (bestSlot != -1) {
            InvUtils.move().from(bestSlot).toHotbar(firstHotbarSlot);
        }
    }
}
