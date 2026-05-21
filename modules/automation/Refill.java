// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.WorldUtils;
import net.minecraft.InteractionHand;
import net.minecraft.class_1733;
import net.minecraft.ItemStack;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.class_2480;
import net.minecraft.Packet;
import net.minecraft.class_2885;
import net.minecraft.MinecraftClient;
import net.minecraft.Screen;

public class Refill
extends Module {
    private final SettingGroup sgGeneral;
    private final MinecraftClient L7xSAoWJW800;
    public static Refill INSTANCE;
    public final Setting<ItemStack> item;
    private final Setting<Boolean> fillCompletely;
    private final Setting<Integer> slotLimit;
    private final Setting<Boolean> placeUpsideDownBelow;
    boolean EZKBvX;
    boolean og2KVvNzA;
    boolean IErgCCM;
    int slot;
    int oQw0r3Nc;
    int OyaWN2jsET;
    BlockPos w6yjUYq;
    Direction xZ3kyYFbKEKAvqOe;

    public Refill() {
        super(musheor.AUTOMATION, "refill", "Places a shulkerbox and refills the inventory with items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.L7xSAoWJW800 = MinecraftClient.getInstance();
        this.item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("item")).description("What block to steal / loot from shulker.")).defaultValue((Object)Items.OBSIDIAN)).build());
        this.fillCompletely = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fill-all")).description("When this setting is enabled, the inventory will be completely filled up until there are no empty slots")).defaultValue((Object)true)).build());
        this.slotLimit = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-slots")).description("How many slots can maximumly be filled up")).defaultValue((Object)10)).visible(() -> (Boolean)this.fillCompletely.get() == false)).build());
        this.placeUpsideDownBelow = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-upside-down-below")).defaultValue((Object)true)).build());
        INSTANCE = this;
    }

    public void onActivate() {
        this.EZKBvX = false;
        this.w6yjUYq = null;
        this.xZ3kyYFbKEKAvqOe = Direction.field_11033;
        this.og2KVvNzA = false;
        this.IErgCCM = false;
        this.slot = 0;
        this.oQw0r3Nc = 0;
        this.OyaWN2jsET = 5;
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.L7xSAoWJW800.player == null || this.L7xSAoWJW800.world == null) {
            return;
        }
        if (this.isActive()) {
            int n;
            ItemStack ItemStack2;
            if (!this.EZKBvX) {
                ItemStack2 = InventoryManager.BX92A0OIIvD9((ItemStack)this.item.get());
                if (ItemStack2 != null) {
                    n = this.L7xSAoWJW800.player.getId().method_7395(ItemStack2);
                    if (this.L7xSAoWJW800.player.getId().field_7545 == n) {
                        this.EZKBvX = true;
                        return;
                    }
                    for (int i = 0; i < 9; ++i) {
                        if (this.L7xSAoWJW800.player.getId().method_5438(i) != ItemStack2) continue;
                        InventoryManager.KP44bk(i);
                        this.EZKBvX = true;
                        return;
                    }
                }
                n = InventoryManager.H4b9BDTz5I9d4B1z();
                if (InventoryManager.jOdDDFXSeWl4((ItemStack)this.item.get(), n)) {
                    InventoryManager.KP44bk(n);
                    this.EZKBvX = true;
                    return;
                }
            }
            if (this.w6yjUYq == null) {
                if (((Boolean)this.placeUpsideDownBelow.get()).booleanValue()) {
                    this.w6yjUYq = this.L7xSAoWJW800.player.getBlockPos().method_33096(this.L7xSAoWJW800.player.getBlockPos().getY() - 2);
                } else {
                    ItemStack2 = this.L7xSAoWJW800.field_1765;
                    if (ItemStack2 instanceof Screen) {
                        Screen Screen2 = (Screen)ItemStack2;
                        BlockPos BlockPos2 = Screen2.method_17777();
                        this.xZ3kyYFbKEKAvqOe = Screen2.method_17780();
                        this.w6yjUYq = BlockPos2.offset(this.xZ3kyYFbKEKAvqOe);
                    }
                }
                return;
            }
            if (!this.og2KVvNzA) {
                if (BlockUtils.canPlace((BlockPos)this.w6yjUYq, (boolean)true)) {
                    this.og2KVvNzA = WorldUtils.jOdDDFXSeWl4(this.w6yjUYq, this.xZ3kyYFbKEKAvqOe);
                } else {
                    this.toggle();
                    this.info("Cannot place shulkerbox at desired position", new Object[0]);
                }
                return;
            }
            if (this.L7xSAoWJW800.world.getBlockState(this.w6yjUYq).getBlock() instanceof class_2480) {
                if (!this.IErgCCM) {
                    if (!(this.L7xSAoWJW800.player.field_7512 instanceof class_1733)) {
                        this.L7xSAoWJW800.player.field_3944.method_52787((Packet)new class_2885(InteractionHand.field_5808, new Screen(Vec3d.method_24953((BlockPos)this.w6yjUYq), Direction.field_11036, this.w6yjUYq, false), 0));
                    } else {
                        this.IErgCCM = true;
                    }
                } else {
                    ItemStack2 = this.L7xSAoWJW800.player.field_7512;
                    if (this.OyaWN2jsET > 0) {
                        --this.OyaWN2jsET;
                        return;
                    }
                    if (((Boolean)this.fillCompletely.get()).booleanValue()) {
                        for (n = 0; n < 27 && InventoryManager.ZeOLrA() > 0; ++n) {
                            if (ItemStack2.method_7611(this.slot).method_7677().getStack() == this.item.get()) {
                                InvUtils.shiftClick().slotId(this.slot);
                            }
                            ++this.slot;
                        }
                        this.L7xSAoWJW800.player.method_7346();
                        this.toggle();
                    } else if (this.oQw0r3Nc < (Integer)this.slotLimit.get() || this.slot < 27) {
                        if (ItemStack2.method_7611(this.slot).method_7677().getStack() == this.item.get()) {
                            InvUtils.shiftClick().slotId(this.slot);
                            ++this.oQw0r3Nc;
                        }
                        ++this.slot;
                    } else {
                        this.L7xSAoWJW800.player.method_7346();
                        this.toggle();
                    }
                }
            }
        }
    }
}

