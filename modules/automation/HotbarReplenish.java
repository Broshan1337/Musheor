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
import net.minecraft.class_1661;
import net.minecraft.ItemStack;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.class_1893;
import net.minecraft.MinecraftClient;
import net.minecraft.class_5321;

public class HotbarReplenish
extends Module {
    private final SettingGroup sgGeneral;
    private static final MinecraftClient MJaOMSip8bg = MinecraftClient.getInstance();
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
        this.offhandSlot = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("offhand-item")).description("Item to maintain offhand slot..")).defaultValue((Object)Items.field_8162)).visible(() -> (Boolean)this.highwayMode.get() == false)).build());
        this.slot1Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-1-item")).description("Item to maintain in the first hotbar slot.")).defaultValue((Object)Items.field_8162)).visible(() -> (Boolean)this.highwayMode.get() == false)).build());
        this.slot2Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-2-item")).description("Item to maintain in the second hotbar slot.")).defaultValue((Object)Items.field_8162)).build());
        this.slot3Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-3-item")).description("Item to maintain in the third hotbar slot.")).defaultValue((Object)Items.field_8162)).build());
        this.slot4Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-4-item")).description("Item to maintain in the fourth hotbar slot.")).defaultValue((Object)Items.field_8162)).build());
        this.slot5Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-5-item")).description("Item to maintain in the fifth hotbar slot.")).defaultValue((Object)Items.field_8162)).build());
        this.slot6Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-6-item")).description("Item to maintain in the sixth hotbar slot.")).defaultValue((Object)Items.field_8162)).build());
        this.slot7Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-7-item")).description("Item to maintain in the seventh hotbar slot.")).defaultValue((Object)Items.field_8162)).build());
        this.slot8Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-8-item")).description("Item to maintain in the eighth hotbar slot.")).defaultValue((Object)Items.field_8162)).build());
        this.slot9Item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("slot-9-item")).description("Item to maintain in the ninth hotbar slot.")).defaultValue((Object)Items.field_8162)).visible(() -> (Boolean)this.highwayMode.get() == false)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (HotbarReplenish.MJaOMSip8bg.player == null || HotbarReplenish.MJaOMSip8bg.world == null) {
            return;
        }
        if (WorldUtils.btLCQHvKVR()) {
            return;
        }
        if (((Boolean)this.highwayMode.get()).booleanValue()) {
            PlayerUtils.jOdDDFXSeWl4(HotbarReplenish.class, "slot-1-item", Items.field_8162);
            PlayerUtils.jOdDDFXSeWl4(HotbarReplenish.class, "slot-9-item", Items.field_8162);
            ItemStack[] ItemStackArray = new ItemStack[]{(ItemStack)this.slot2Item.get(), (ItemStack)this.slot3Item.get(), (ItemStack)this.slot4Item.get(), (ItemStack)this.slot5Item.get(), (ItemStack)this.slot6Item.get(), (ItemStack)this.slot7Item.get(), (ItemStack)this.slot8Item.get()};
            this.uKCgvn9Jo();
            for (int i = 1; i <= 7; ++i) {
                this.jOdDDFXSeWl4(i, ItemStackArray[i - 1]);
            }
        } else {
            ItemStack[] ItemStackArray = new ItemStack[]{(ItemStack)this.slot1Item.get(), (ItemStack)this.slot2Item.get(), (ItemStack)this.slot3Item.get(), (ItemStack)this.slot4Item.get(), (ItemStack)this.slot5Item.get(), (ItemStack)this.slot6Item.get(), (ItemStack)this.slot7Item.get(), (ItemStack)this.slot8Item.get(), (ItemStack)this.slot9Item.get(), (ItemStack)this.offhandSlot.get()};
            for (int i = 0; i <= 7; ++i) {
                this.jOdDDFXSeWl4(i, ItemStackArray[i]);
            }
            this.jOdDDFXSeWl4(45, (ItemStack)this.offhandSlot.get());
        }
    }

    private void jOdDDFXSeWl4(int n, ItemStack ItemStack2) {
        ItemStack ItemStack2;
        int n2;
        assert (HotbarReplenish.MJaOMSip8bg.player != null);
        if (ItemStack2 == Items.field_8162) {
            return;
        }
        ItemStack ItemStack3 = n == 45 ? HotbarReplenish.MJaOMSip8bg.player.method_6079() : HotbarReplenish.MJaOMSip8bg.player.getId().method_5438(n);
        if (ItemStack3.setStack()) {
            int n3 = this.jOdDDFXSeWl4(ItemStack2, n, 1);
            if (n3 != -1) {
                this.mp3zoXQFKUKYj5(n, n3);
            }
        } else if (ItemStack3.getStack() == ItemStack2 && ItemStack3.method_7946() && ItemStack3.method_7947() <= (Integer)this.threshold.get() && (n2 = this.jOdDDFXSeWl4(ItemStack2, n, (Integer)this.threshold.get() - ItemStack3.method_7947() + 1)) != -1 && ItemStack.method_31577((ItemStack)ItemStack3, (ItemStack)(ItemStack2 = HotbarReplenish.MJaOMSip8bg.player.getId().method_5438(n2)))) {
            this.mp3zoXQFKUKYj5(n, n2);
        }
    }

    private int jOdDDFXSeWl4(ItemStack ItemStack2, int n, int n2) {
        int n3 = -1;
        int n4 = 0;
        assert (HotbarReplenish.MJaOMSip8bg.player != null);
        for (int i = 35; i >= 0; --i) {
            ItemStack ItemStack2;
            if (i == n || this.jOdDDFXSeWl4(i) || (ItemStack2 = HotbarReplenish.MJaOMSip8bg.player.getId().method_5438(i)).getStack() != ItemStack2 || ItemStack2.method_7947() <= n4) continue;
            n3 = i;
            n4 = ItemStack2.method_7947();
            if (n4 >= n2) break;
        }
        return n3;
    }

    private boolean jOdDDFXSeWl4(int n) {
        return n == 0 && this.slot1Item.get() != Items.field_8162 || n == 1 && this.slot2Item.get() != Items.field_8162 || n == 2 && this.slot3Item.get() != Items.field_8162 || n == 3 && this.slot4Item.get() != Items.field_8162 || n == 4 && this.slot5Item.get() != Items.field_8162 || n == 5 && this.slot6Item.get() != Items.field_8162 || n == 6 && this.slot7Item.get() != Items.field_8162 || n == 7 && this.slot8Item.get() != Items.field_8162 || n == 8 && this.slot9Item.get() != Items.field_8162 || n == 40 && this.offhandSlot.get() != Items.field_8162;
    }

    private void mp3zoXQFKUKYj5(int n, int n2) {
        InvUtils.move().from(n2).to(n);
    }

    private void uKCgvn9Jo() {
        assert (HotbarReplenish.MJaOMSip8bg.player != null);
        class_1661 class_16612 = HotbarReplenish.MJaOMSip8bg.player.getId();
        int n = 0;
        ItemStack ItemStack2 = class_16612.method_5438(n);
        if (HighwayBuilder.cghz8iox35K() == HighwayBuilder.BuildMode.e4uKoS && VersionHelper.get().isPickaxe(ItemStack2) && ((Boolean)MusheorSystem.Manager.preventToolBreaking.get() != false ? ItemStack2.method_7936() - ItemStack2.method_7919() > (Integer)MusheorSystem.Manager.minToolDurability.get() && !Utils.hasEnchantments((ItemStack)ItemStack2, (class_5321[])new class_5321[]{class_1893.field_9099}) : !Utils.hasEnchantments((ItemStack)ItemStack2, (class_5321[])new class_5321[]{class_1893.field_9099}))) {
            return;
        }
        if (HighwayBuilder.cghz8iox35K() == HighwayBuilder.BuildMode.flZYoiXwrl && VersionHelper.get().isPickaxe(ItemStack2)) {
            if (((Boolean)MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()) {
                if (ItemStack2.method_7936() - ItemStack2.method_7919() > (Integer)MusheorSystem.Manager.minToolDurability.get()) {
                    return;
                }
            } else {
                return;
            }
        }
        int n2 = -1;
        for (int i = 0; i < class_16612.method_5439(); ++i) {
            if (i == n) continue;
            ItemStack ItemStack3 = class_16612.method_5438(i);
            if (HighwayBuilder.cghz8iox35K() == HighwayBuilder.BuildMode.e4uKoS && VersionHelper.get().isPickaxe(ItemStack3)) {
                if (((Boolean)MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()) {
                    if (ItemStack3.method_7936() - ItemStack3.method_7919() > (Integer)MusheorSystem.Manager.minToolDurability.get() && !Utils.hasEnchantments((ItemStack)ItemStack3, (class_5321[])new class_5321[]{class_1893.field_9099})) {
                        n2 = i;
                        break;
                    }
                } else if (!Utils.hasEnchantments((ItemStack)ItemStack3, (class_5321[])new class_5321[]{class_1893.field_9099})) {
                    n2 = i;
                    break;
                }
            }
            if (HighwayBuilder.cghz8iox35K() != HighwayBuilder.BuildMode.flZYoiXwrl || !VersionHelper.get().isPickaxe(ItemStack3)) continue;
            if (((Boolean)MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()) {
                if (ItemStack3.method_7936() - ItemStack3.method_7919() <= (Integer)MusheorSystem.Manager.minToolDurability.get()) continue;
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

