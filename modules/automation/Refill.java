// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.WorldUtils;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * "refill" — places a shulker box (either two blocks below the player or against the
 * looked-at block), opens it, and shift-clicks the configured item back into the
 * inventory until full (or up to a slot limit), then closes and disables itself.
 */
public class Refill extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: zu3a44xDeMFMCRwm
    private final MinecraftClient mc = MinecraftClient.getInstance();       // was: krxNb5lcQuWA
    public static Refill INSTANCE;

    public final Setting<Item> item = sgGeneral.add(new ItemSetting.Builder() // was: FvaNWO
        .name("item").description("What block to steal / loot from shulker.").defaultValue(Items.OBSIDIAN).build());
    private final Setting<Boolean> fillAll = sgGeneral.add(new BoolSetting.Builder() // was: nt0HZnvBBp
        .name("fill-all").description("When this setting is enabled, the inventory will be completely filled up until there are no empty slots")
        .defaultValue(true).build());
    private final Setting<Integer> maxSlots = sgGeneral.add(new IntSetting.Builder() // was: amz3UB1vE
        .name("max-slots").description("How many slots can maximumly be filled up").defaultValue(10).visible(() -> !fillAll.get()).build());
    private final Setting<Boolean> placeBelow = sgGeneral.add(new BoolSetting.Builder() // was: sBBIyQG5NWq0K
        .name("place-upside-down-below").defaultValue(true).build());

    boolean hasSelectedItem;   // was: Q90GLXQ0Pef
    boolean hasPlacedShulker;  // was: psJq59YIbp3Z
    boolean containerOpen;     // was: SOYyh5IPg26f7F
    int slotIndex;             // was: rKbT3Ifwo (current container slot being scanned)
    int filledCount;           // was: r7hOYIKN2
    int openDelay;             // was: oZHMlTL
    BlockPos placePos;         // was: xQr5FhbwpQPWgIQ
    Direction placeDirection;  // was: OMMZL1F3q

    public Refill() {
        super(musheor.AUTOMATION, "refill", "Places a shulkerbox and refills the inventory with items.");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        this.hasSelectedItem = false;
        this.placePos = null;
        this.placeDirection = Direction.DOWN;
        this.hasPlacedShulker = false;
        this.containerOpen = false;
        this.slotIndex = 0;
        this.filledCount = 0;
        this.openDelay = 5;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) { // was: FvaNWO(Post)
        if (this.mc.player == null || this.mc.world == null || !this.isActive()) return;

        // 1) Select a hotbar shulker that holds the target item (or move one into the hotbar).
        if (!this.hasSelectedItem) {
            ItemStack lookingFor = InventoryManager.findLeastFullShulkerWith(this.item.get());
            if (lookingFor != null) {
                int slot = this.mc.player.getInventory().getSlotWithStack(lookingFor);
                if (this.mc.player.getInventory().selectedSlot == slot) {
                    this.hasSelectedItem = true;
                    return;
                }
                for (int i = 0; i < 9; i++) {
                    if (this.mc.player.getInventory().getStack(i) == lookingFor) {
                        InventoryManager.selectHotbarSlot(i);
                        this.hasSelectedItem = true;
                        return;
                    }
                }
            }

            int emptySlot = InventoryManager.firstEmptyHotbarSlot();
            if (InventoryManager.moveShulkerToHotbar(this.item.get(), emptySlot)) {
                InventoryManager.selectHotbarSlot(emptySlot);
                this.hasSelectedItem = true;
                return;
            }
        }

        // 2) Choose where to place the shulker.
        if (this.placePos == null) {
            if (this.placeBelow.get()) {
                this.placePos = this.mc.player.getBlockPos().withY(this.mc.player.getBlockPos().getY() - 2);
            } else if (this.mc.crosshairTarget instanceof BlockHitResult bhr) {
                BlockPos adjacent = bhr.getBlockPos();
                this.placeDirection = bhr.getSide();
                this.placePos = adjacent.offset(this.placeDirection);
            }
            return;
        }

        // 3) Place the shulker.
        if (!this.hasPlacedShulker) {
            if (BlockUtils.canPlace(this.placePos, true)) {
                this.hasPlacedShulker = WorldUtils.placeBlockOffhand(this.placePos, this.placeDirection);
            } else {
                this.toggle();
                this.info("Cannot place shulkerbox at desired position");
            }
            return;
        }

        // 4) Open the shulker and shift-click the item back out.
        if (this.mc.world.getBlockState(this.placePos).getBlock() instanceof ShulkerBoxBlock) {
            if (!this.containerOpen) {
                if (!(this.mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler)) {
                    this.mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ofCenter(this.placePos), Direction.UP, this.placePos, false), 0));
                } else {
                    this.containerOpen = true;
                }
            } else {
                ScreenHandler handler = this.mc.player.currentScreenHandler;
                if (this.openDelay > 0) {
                    this.openDelay--;
                    return;
                }

                if (this.fillAll.get()) {
                    for (int i = 0; i < 27 && InventoryManager.countEmptyInventorySlots() > 0; i++) {
                        if (handler.getSlot(this.slotIndex).getStack().getItem() == this.item.get()) {
                            InvUtils.shiftClick().slotId(this.slotIndex);
                        }
                        this.slotIndex++;
                    }
                    this.mc.player.closeHandledScreen();
                    this.toggle();
                } else if (this.filledCount >= this.maxSlots.get() && this.slotIndex >= 27) {
                    this.mc.player.closeHandledScreen();
                    this.toggle();
                } else {
                    if (handler.getSlot(this.slotIndex).getStack().getItem() == this.item.get()) {
                        InvUtils.shiftClick().slotId(this.slotIndex);
                        this.filledCount++;
                    }
                    this.slotIndex++;
                }
            }
        }
    }
}
