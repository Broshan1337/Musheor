// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import musheor.compat.VersionHelper;
import musheor.modules.automation.HighwayBuilder;
import musheor.musheor;
import musheor.utils.PlayerUtils;
import musheor.utils.WorldUtils;
import musheor.utils.system.MusheorSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;

public class HotbarReplenish
extends Module {
    private final SettingGroup sgGeneral;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final Setting<Integer> threshold;
    private final Setting<Boolean> highwayMode;
    private final Setting<ItemStack> offhandSlot;
    private final Setting<ItemStack> slot1Item;
    private final Setting<ItemStack> slot2Item;
    private final Setting<ItemStack> slot3Item;
    private final Setting<ItemStack> slot4Item;
    private final Setting<ItemStack> slot5Item;
    private final Setting<ItemStack> slot6Item;
    private final Setting<ItemStack> slot7Item;
    private final Setting<ItemStack> slot8Item;
    private final Setting<ItemStack> slot9Item;

    public HotbarReplenish() {
        super(musheor.AUTOMATION, "hotbar-replenish", "Automatically refills specific items in each hotbar slot. Each slot independantly configurable.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.threshold = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("threshold")).description("The threshold of items left to trigger replenishment.")).defaultValue((Object)16)).min(1).sliderRange(1, 63).build());
        this.highwayMode = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("highway-mode")).description("Enable this if you are using Better-Highway-Builder")).defaultValue((Object)false)).build());
        this.offhandSlot = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("offhand-item")).description("Item to maintain offhand slot..")).defaultValue((Object)Items.AIR)).visible(() -> (Boolean)this.highwayMode.get() == false)).build());
        this.slot1Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-1-item")).description("Item to maintain in the first hotbar slot.")).defaultValue((Object)Items.AIR)).visible(() -> (Boolean)this.highwayMode.get() == false)).build());
        this.slot2Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-2-item")).description("Item to maintain in the second hotbar slot.")).defaultValue((Object)Items.AIR)).build());
        this.slot3Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-3-item")).description("Item to maintain in the third hotbar slot.")).defaultValue((Object)Items.AIR)).build());
        this.slot4Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-4-item")).description("Item to maintain in the fourth hotbar slot.")).defaultValue((Object)Items.AIR)).build());
        this.slot5Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-5-item")).description("Item to maintain in the fifth hotbar slot.")).defaultValue((Object)Items.AIR)).build());
        this.slot6Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-6-item")).description("Item to maintain in the sixth hotbar slot.")).defaultValue((Object)Items.AIR)).build());
        this.slot7Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-7-item")).description("Item to maintain in the seventh hotbar slot.")).defaultValue((Object)Items.AIR)).build());
        this.slot8Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-8-item")).description("Item to maintain in the eighth hotbar slot.")).defaultValue((Object)Items.AIR)).build());
        this.slot9Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-9-item")).description("Item to maintain in the ninth hotbar slot.")).defaultValue((Object)Items.AIR)).visible(() -> (Boolean)this.highwayMode.get() == false)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (HotbarReplenish.mc.player == null || HotbarReplenish.mc.world == null) {
            return;
        }
        if (WorldUtils.checkForLag()) {
            return;
        }
        if (((Boolean)this.highwayMode.get()).booleanValue()) {
            PlayerUtils.setModuleSetting(HotbarReplenish.class, "slot-1-item", Items.AIR);
            PlayerUtils.setModuleSetting(HotbarReplenish.class, "slot-9-item", Items.AIR);
            ItemStack[] ItemStackArray = new ItemStack[]{(ItemStack)this.slot2Item.get(), (ItemStack)this.slot3Item.get(), (ItemStack)this.slot4Item.get(), (ItemStack)this.slot5Item.get(), (ItemStack)this.slot6Item.get(), (ItemStack)this.slot7Item.get(), (ItemStack)this.slot8Item.get()};
            this.maintainPickaxe();
            for (int i = 1; i <= 7; ++i) {
                this.replenishSlot(i, ItemStackArray[i - 1]);
            }
        } else {
            ItemStack[] ItemStackArray = new ItemStack[]{(ItemStack)this.slot1Item.get(), (ItemStack)this.slot2Item.get(), (ItemStack)this.slot3Item.get(), (ItemStack)this.slot4Item.get(), (ItemStack)this.slot5Item.get(), (ItemStack)this.slot6Item.get(), (ItemStack)this.slot7Item.get(), (ItemStack)this.slot8Item.get(), (ItemStack)this.slot9Item.get(), (ItemStack)this.offhandSlot.get()};
            for (int i = 0; i <= 7; ++i) {
                this.replenishSlot(i, ItemStackArray[i]);
            }
            this.replenishSlot(45, (ItemStack)this.offhandSlot.get());
        }
    }

    private void replenishSlot(int n, ItemStack ItemStack2) {
        ItemStack ItemStack2;
        int n2;
        assert (HotbarReplenish.mc.player != null);
        if (ItemStack2 == Items.AIR) {
            return;
        }
        ItemStack ItemStack3 = n == 45 ? HotbarReplenish.mc.player.getOffHandStack() : HotbarReplenish.mc.player.getInventory().getStack(n);
        if (ItemStack3.isEmpty()) {
            int n3 = this.findBestSourceSlot(ItemStack2, n, 1);
            if (n3 != -1) {
                this.moveItemToSlot(n, n3);
            }
        } else if (ItemStack3.getItem() == ItemStack2 && ItemStack3.isStackable() && ItemStack3.getCount() <= (Integer)this.threshold.get() && (n2 = this.findBestSourceSlot(ItemStack2, n, (Integer)this.threshold.get() - ItemStack3.getCount() + 1)) != -1 && ItemStack.areItemsEqual(ItemStack3, (ItemStack2 = HotbarReplenish.mc.player.getInventory().getStack(n2)))) {
            this.moveItemToSlot(n, n2);
        }
    }

    private int findBestSourceSlot(ItemStack ItemStack2, int n, int n2) {
        int n3 = -1;
        int n4 = 0;
        assert (HotbarReplenish.mc.player != null);
        for (int i = 35; i >= 0; --i) {
            ItemStack ItemStack2;
            if (i == n || this.isSlotManaged(i) || (ItemStack2 = HotbarReplenish.mc.player.getInventory().getStack(i)).getStack() != ItemStack2 || ItemStack2.getCount() <= n4) continue;
            n3 = i;
            n4 = ItemStack2.getCount();
            if (n4 >= n2) break;
        }
        return n3;
    }

    private boolean isSlotManaged(int n) {
        return n == 0 && this.slot1Item.get() != Items.AIR || n == 1 && this.slot2Item.get() != Items.AIR || n == 2 && this.slot3Item.get() != Items.AIR || n == 3 && this.slot4Item.get() != Items.AIR || n == 4 && this.slot5Item.get() != Items.AIR || n == 5 && this.slot6Item.get() != Items.AIR || n == 6 && this.slot7Item.get() != Items.AIR || n == 7 && this.slot8Item.get() != Items.AIR || n == 8 && this.slot9Item.get() != Items.AIR || n == 40 && this.offhandSlot.get() != Items.AIR;
    }

    private void moveItemToSlot(int n, int n2) {
        InvUtils.move().from(n2).to(n);
    }

    private void maintainPickaxe() {
        assert (HotbarReplenish.mc.player != null);
        PlayerInventory playerInv = HotbarReplenish.mc.player.getInventory();
        int n = 0;
        ItemStack ItemStack2 = playerInv.getStack(n);
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.Pave && VersionHelper.get().isPickaxe(ItemStack2) && ((Boolean)MusheorSystem.Manager.preventToolBreaking.get() != false ? ItemStack2.getMaxDamage() - ItemStack2.getDamage() > (Integer)MusheorSystem.Manager.minToolDurability.get() && !Utils.hasEnchantments((ItemStack)ItemStack2, (RegistryKey[])new RegistryKey[]{Enchantments.SILK_TOUCH}) : !Utils.hasEnchantments((ItemStack)ItemStack2, (RegistryKey[])new RegistryKey[]{Enchantments.SILK_TOUCH}))) {
            return;
        }
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.Dig && VersionHelper.get().isPickaxe(ItemStack2)) {
            if (((Boolean)MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()) {
                if (ItemStack2.getMaxDamage() - ItemStack2.getDamage() > (Integer)MusheorSystem.Manager.minToolDurability.get()) {
                    return;
                }
            } else {
                return;
            }
        }
        int n2 = -1;
        for (int i = 0; i < playerInv.size(); ++i) {
            if (i == n) continue;
            ItemStack ItemStack3 = playerInv.getStack(i);
            if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.Pave && VersionHelper.get().isPickaxe(ItemStack3)) {
                if (((Boolean)MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()) {
                    if (ItemStack3.getMaxDamage() - ItemStack3.getDamage() > (Integer)MusheorSystem.Manager.minToolDurability.get() && !Utils.hasEnchantments((ItemStack)ItemStack3, (RegistryKey[])new RegistryKey[]{Enchantments.SILK_TOUCH})) {
                        n2 = i;
                        break;
                    }
                } else if (!Utils.hasEnchantments((ItemStack)ItemStack3, (RegistryKey[])new RegistryKey[]{Enchantments.SILK_TOUCH})) {
                    n2 = i;
                    break;
                }
            }
            if (HighwayBuilder.getBuildMode() != HighwayBuilder.BuildMode.Dig || !VersionHelper.get().isPickaxe(ItemStack3)) continue;
            if (((Boolean)MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()) {
                if (ItemStack3.getMaxDamage() - ItemStack3.getDamage() <= (Integer)MusheorSystem.Manager.minToolDurability.get()) continue;
                n2 = i;
                break;
            }
            n2 = i;
            break;
        }
        if (n2 != -1) {
            InvUtils.move().from(n2).toHotbar(n);
        }
    }
}

