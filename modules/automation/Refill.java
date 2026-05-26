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
import net.minecraft.util.Hand;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.BlockHitResult;

public class Refill
extends Module {
    private final SettingGroup sgGeneral;
    private final MinecraftClient mc; // was: L7xSAoWJW800
    public static Refill INSTANCE;
    public final Setting<Item> item;
    private final Setting<Boolean> fillCompletely;
    private final Setting<Integer> slotLimit;
    private final Setting<Boolean> placeUpsideDownBelow;
    boolean itemReady;   // was: EZKBvX
    boolean placed;      // was: og2KVvNzA
    boolean screenOpen;  // was: IErgCCM
    int slot;
    int filledSlots;     // was: oQw0r3Nc
    int openDelay;       // was: OyaWN2jsET
    BlockPos placePos;   // was: w6yjUYq
    Direction placeSide; // was: xZ3kyYFbKEKAvqOe

    public Refill() {
        super(musheor.AUTOMATION, "refill", "Places a shulkerbox and refills the inventory with items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mc = MinecraftClient.getInstance();
        this.item = this.sgGeneral.add((Setting)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("item")).description("What block to steal / loot from shulker.")).defaultValue((Object)Items.OBSIDIAN)).build());
        this.fillCompletely = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fill-all")).description("When this setting is enabled, the inventory will be completely filled up until there are no empty slots")).defaultValue((Object)true)).build());
        this.slotLimit = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-slots")).description("How many slots can maximumly be filled up")).defaultValue((Object)10)).visible(() -> (Boolean)this.fillCompletely.get() == false)).build());
        this.placeUpsideDownBelow = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-upside-down-below")).defaultValue((Object)true)).build());
        INSTANCE = this;
    }

    public void onActivate() {
        this.itemReady = false;
        this.placePos  = null;
        this.placeSide = Direction.DOWN;
        this.placed    = false;
        this.screenOpen = false;
        this.slot = 0;
        this.filledSlots = 0;
        this.openDelay = 5;
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.isActive()) {
            int n;
            if (!this.itemReady) {
                ItemStack foundStack = InventoryManager.findLeastFullShulkerWithItem((Item) this.item.get());
                if (foundStack != null) {
                    n = this.mc.player.getInventory().getSlotWithStack(foundStack);
                    if (this.mc.player.getInventory().selectedSlot == n) {
                        this.itemReady = true;
                        return;
                    }
                    for (int i = 0; i < 9; ++i) {
                        if (this.mc.player.getInventory().getStack(i) != foundStack) continue;
                        InventoryManager.switchHotbarSlot(i);
                        this.itemReady = true;
                        return;
                    }
                }
                n = InventoryManager.findEmptyHotbarSlot();
                if (InventoryManager.moveShulkerToSlot((Item) this.item.get(), n)) {
                    InventoryManager.switchHotbarSlot(n);
                    this.itemReady = true;
                    return;
                }
            }
            if (this.placePos == null) {
                if (((Boolean) this.placeUpsideDownBelow.get()).booleanValue()) {
                    this.placePos = this.mc.player.getBlockPos().withY(this.mc.player.getBlockPos().getY() - 2);
                } else {
                    HitResult target = this.mc.crosshairTarget;
                    if (target instanceof BlockHitResult) {
                        BlockHitResult blockHitResult = (BlockHitResult) target;
                        BlockPos hitPos = blockHitResult.getBlockPos();
                        this.placeSide = blockHitResult.getSide();
                        this.placePos  = hitPos.offset(this.placeSide);
                    }
                }
                return;
            }
            if (!this.placed) {
                if (BlockUtils.canPlace((BlockPos) this.placePos, (boolean) true)) {
                    this.placed = WorldUtils.placeBlockPacket(this.placePos, this.placeSide);
                } else {
                    this.toggle();
                    this.info("Cannot place shulkerbox at desired position", new Object[0]);
                }
                return;
            }
            if (this.mc.world.getBlockState(this.placePos).getBlock() instanceof ShulkerBoxBlock) {
                if (!this.screenOpen) {
                    if (!(this.mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler)) {
                        this.mc.player.networkHandler.sendPacket((Packet) new PlayerInteractBlockC2SPacket(
                            Hand.MAIN_HAND,
                            new BlockHitResult(Vec3d.ofCenter(this.placePos), Direction.UP, this.placePos, false),
                            0));
                    } else {
                        this.screenOpen = true;
                    }
                } else {
                    ShulkerBoxScreenHandler handler = (ShulkerBoxScreenHandler) this.mc.player.currentScreenHandler;
                    if (this.openDelay > 0) {
                        --this.openDelay;
                        return;
                    }
                    if (((Boolean) this.fillCompletely.get()).booleanValue()) {
                        for (n = 0; n < 27 && InventoryManager.countEmptySlots() > 0; ++n) {
                            if (handler.getSlot(this.slot).getStack().getItem() == this.item.get()) {
                                InvUtils.shiftClick().slotId(this.slot);
                            }
                            ++this.slot;
                        }
                        this.mc.player.closeHandledScreen();
                        this.toggle();
                    } else if (this.filledSlots < (Integer) this.slotLimit.get() || this.slot < 27) {
                        if (handler.getSlot(this.slot).getStack().getItem() == this.item.get()) {
                            InvUtils.shiftClick().slotId(this.slot);
                            ++this.filledSlots;
                        }
                        ++this.slot;
                    } else {
                        this.mc.player.closeHandledScreen();
                        this.toggle();
                    }
                }
            }
        }
    }
}