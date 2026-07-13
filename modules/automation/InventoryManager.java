// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.e1lTf; registered module name "inventory-manager")
package musheor.modules.automation;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.mixin.ContainerComponentAccessor;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.utils.PlayerUtils; // musheor's PlayerUtils (movement); Meteor's is above
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockItem;
import net.minecraft.block.Blocks;
import net.minecraft.block.BarrelBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * The "inventory-manager" module: the restocking state machine and inventory
 * helpers the HighwayBuilder relies on. Handles counting items (incl. inside
 * shulkers), selecting the best pickaxe/tool, placing & looting a restock shulker,
 * and the multi-tick restock process itself.
 *
 * NOTE: many methods share the obfuscated overloaded name {@code FvaNWO}; the real
 * names below are assigned from behaviour. The static boolean/int fields double as
 * the restock state machine's flags/counters.
 */
public class InventoryManager extends Module {
    private final SettingGroup sgGeneral  = this.settings.getDefaultGroup();   // was: kJfFkD47Vh
    private final SettingGroup sgItems    = this.settings.createGroup("Items"); // was: ubHptFBRn5bO
    private final SettingGroup sgShulkers = this.settings.createGroup("Shulkers"); // was: apOpfoOHr3fJVwT
    private final SettingGroup sgDelays   = this.settings.createGroup("Delays"); // was: hq1pN0qY

    private static final MinecraftClient mc = MinecraftClient.getInstance(); // was: ptxWcpd1WV763T5

    // --- restock state machine counters/flags ---
    private static int delayTicks;                 // was: DnAk86nuI
    private static int afterOpenDelayTicks;        // was: LlN8EpIZKbk
    private static int cachedOpenDelay;            // was: pgjj9cLYUTE5g (delay-before-opening)
    private static int cachedRestockDelay;         // was: IeStEJRJ9eb3l (delay-after-restocking)
    private static int cachedSwapDelay;            // was: sFazojak6ig8QgGq (delay-after-swapping)
    public static BlockPos targetPos;              // was: FvaNWO (field) — where to stand to restock
    private static BlockPos shulkerPlacePos;       // was: ewq603nIlCd9Gbu
    private static int cachedPlaceDelay;           // was: ExGM8SQ9Qni (delay-after-placing)
    private static boolean restockInitialized;     // was: yS4isXf3gAzs
    private static Screen lastScreen;              // was: eC9HV2bWGX (unused)
    private static int currentShulkerSlot = 0;     // was: w9spWeVv3AvI
    public static boolean isRestocking;            // was: Q90GLXQ0Pef (field)
    public static boolean isPending;               // was: psJq59YIbp3Z (field) — interrupt flag
    private static boolean breakingContainer;      // was: HvulV2j9tKjohNgh
    private static boolean shulkerPlaced;          // was: Qco5OF
    private static boolean shulkerMovedToHotbar;   // was: cgqo7J5iR6
    public static boolean postRestock;             // was: SOYyh5IPg26f7F (field)
    private static int stolenCount = 0;            // was: u2kcN4vsQhS46w5s
    private static int stealDelayCounter = 0;      // was: Eos3LxdhEJt
    private static boolean justStole;              // was: eB4Or3cBC2
    public static int restockAmount;               // was: rKbT3Ifwo (field)
    static int cachedToolRestockAmount;            // was: r7hOYIKN2
    static int cachedFoodRestockAmount;            // was: oZHMlTL
    static int cachedMaterialShulkerAmount;        // was: xQr5FhbwpQPWgIQ (field)
    static int cachedToolShulkerAmount;            // was: OMMZL1F3q
    public static InventoryManager INSTANCE;       // was: zu3a44xDeMFMCRwm (static)

    // --- Settings ---
    public final Setting<Integer> toolRestockAmount = sgItems.add(new IntSetting.Builder() // was: krxNb5lcQuWA
        .name("tool-restock-amount").description("How many pickaxes should be taken on restock when the player runs out.")
        .defaultValue(1).sliderRange(1, 20).build());
    public final Setting<Integer> foodRestockAmount = sgItems.add(new IntSetting.Builder() // was: nt0HZnvBBp
        .name("food-restock-amount").description("How many stacks of food should be taken on restock when the player runs out.")
        .defaultValue(1).sliderRange(1, 10).build());
    public final Setting<Integer> materialShulkerRestockAmount = sgShulkers.add(new IntSetting.Builder() // was: amz3UB1vE
        .name("material-shulker-restock-amount").description("How many shulkers of pavement materials should be taken when performing an enderchest restock.")
        .defaultValue(1).sliderRange(1, 20).build());
    public final Setting<Integer> toolShulkerRestockAmount = sgShulkers.add(new IntSetting.Builder() // was: sBBIyQG5NWq0K
        .name("tool-shulker-restock-amount").description("How many shulkers of pickaxes should be taken when performing an enderchest restock.")
        .defaultValue(1).sliderRange(1, 10).build());
    public final Setting<Integer> stealingDelay = sgDelays.add(new IntSetting.Builder() // was: sZkZ1izAy
        .name("stealing-delay").description("Time the player should wait before stealing another slot from a container in ticks")
        .defaultValue(1).sliderRange(1, 20).build());
    public final Setting<Integer> delayAfterPlacingContainer = sgDelays.add(new IntSetting.Builder() // was: QYKUhjp
        .name("delay-after-placing-container").description("Time the player should wait before placing a container in ticks")
        .defaultValue(5).sliderRange(1, 20).build());
    public final Setting<Integer> delayBeforeOpeningContainer = sgDelays.add(new IntSetting.Builder() // was: NIz4xic3Js9
        .name("delay-before-opening-container").description("Time the player should wait before opening a container in ticks")
        .defaultValue(5).sliderRange(1, 20).build());
    public final Setting<Integer> delayAfterOpeningContainer = sgDelays.add(new IntSetting.Builder() // was: u1WFwbQRSKa
        .name("delay-after-opening-container").description("Time the player should wait after opening a container in ticks")
        .defaultValue(5).sliderRange(1, 20).build());
    public final Setting<Integer> delayAfterSwappingItem = sgDelays.add(new IntSetting.Builder() // was: LGDfbZq
        .name("delay-after-swapping-item").description("Time the player should wait before it attempts to swap items")
        .defaultValue(5).sliderRange(1, 20).build());
    public final Setting<Integer> delayAfterRestocking = sgDelays.add(new IntSetting.Builder() // was: to3T8DJCDVX8po
        .name("delay-after-restocking").description("Time the player should wait completing the restocking process")
        .defaultValue(10).sliderRange(1, 20).build());

    public InventoryManager() {
        super(musheor.AUTOMATION, "inventory-manager", "Manager module for inventory related stuff");
        INSTANCE = this;
    }

    /** Caches the delay/amount settings into the static fields used by the state machine. */
    public static void cacheSettings() { // was: FvaNWO()
        cachedPlaceDelay = INSTANCE.delayAfterPlacingContainer.get();
        cachedOpenDelay = INSTANCE.delayBeforeOpeningContainer.get();
        afterOpenDelayTicks = INSTANCE.delayAfterOpeningContainer.get();
        cachedSwapDelay = INSTANCE.delayAfterSwappingItem.get();
        cachedRestockDelay = INSTANCE.delayAfterRestocking.get();
        cachedMaterialShulkerAmount = INSTANCE.materialShulkerRestockAmount.get();
        cachedToolShulkerAmount = INSTANCE.toolShulkerRestockAmount.get();
        cachedToolRestockAmount = INSTANCE.toolRestockAmount.get();
        cachedFoodRestockAmount = INSTANCE.foodRestockAmount.get();
    }

    /** Resets all restock state; if {@code handlePostRestocking}, arms the post-restock cleanup. */
    public static void resetState(boolean handlePostRestocking) { // was: FvaNWO(boolean)
        HighwayState state = HighwayState.getInstance();
        cacheSettings();
        postRestock = false;
        state.setFlag9(false);
        state.setFlag8(false);
        shulkerMovedToHotbar = false;
        shulkerPlaced = false;
        stolenCount = 0;
        currentShulkerSlot = 0;
        stealDelayCounter = 0;
        targetPos = null;
        isRestocking = false;
        state.setFlag6(false);
        state.setBaritoneGoalType(null);
        restockAmount = 0;
        state.setFlag10(false);
        delayTicks = 0;
        if (handlePostRestocking) {
            postRestock = true;
            state.setFlag10(true);
        }
    }

    /** Decides which restock process (echest/tool/food/material) to start, if any. */
    public static void runRestock() { // was: Q90GLXQ0Pef()
        HighwayState state = HighwayState.getInstance();
        if (HighwayBuilder.INSTANCE.mode.get() == HighwayBuilder.Mode.MANUAL) return;
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.PAVE) {
            if (HighwayBuilder.shulkerRestocking() && HighwayBuilder.echestShulkerRestocking()
                && findShulkerWith(Items.ENDER_CHEST) == null && findShulkerWith(HighwayBuilder.getFillBlock().asItem()) == null) {
                isRestocking = true;
                state.setBaritoneGoalType(DataComponentTypes.CONTAINER);
                restockAmount = cachedMaterialShulkerAmount;
                state.setFlag8(true);
                MusheorSystem.debug("Echest shulker restocking process started");
                return;
            }
            if (HighwayBuilder.shulkerRestocking() && HighwayBuilder.toolShulkerRestocking()
                && findShulkerWithComponent(DataComponentTypes.TOOL) == null && countPickaxes(false) == 0) {
                isRestocking = true;
                state.setBaritoneGoalType(DataComponentTypes.TOOL);
                restockAmount = cachedToolShulkerAmount;
                state.setFlag8(true);
                MusheorSystem.debug("Tool shulker restocking process started");
                return;
            }
            if (HighwayBuilder.enableItemRestocking()
                && (countItemInInventory(HighwayBuilder.getFillBlock().asItem()) < 8 || countItemInInventory(Items.ENDER_CHEST) < 8)
                && (findShulkerWith(Items.ENDER_CHEST) != null || findShulkerWith(HighwayBuilder.getFillBlock().asItem()) != null)) {
                isRestocking = true;
                state.setBaritoneGoalType(DataComponentTypes.CONTAINER);
                restockAmount = countEmptyInventorySlots() / 3 * 2;
                state.setFlag9(true);
                MusheorSystem.debug("Material restocking process started");
                return;
            }
            if (HighwayBuilder.enableItemRestocking() && findShulkerWithComponent(DataComponentTypes.TOOL) != null && countPickaxes(false) < 1) {
                isRestocking = true;
                state.setBaritoneGoalType(DataComponentTypes.TOOL);
                restockAmount = cachedToolRestockAmount;
                state.setFlag9(true);
                MusheorSystem.debug("Pickaxe restocking process started");
                return;
            }
            if (HighwayBuilder.enableItemRestocking() && !HighwayBuilder.hasFood() && findShulkerWithComponent(DataComponentTypes.FOOD) != null) {
                isRestocking = true;
                state.setBaritoneGoalType(DataComponentTypes.FOOD);
                restockAmount = cachedFoodRestockAmount;
                state.setFlag9(true);
                MusheorSystem.debug("Food restocking process started");
                return;
            }
        }
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.DIG) {
            if (HighwayBuilder.enableItemRestocking() && findShulkerWithComponent(DataComponentTypes.TOOL) != null && countPickaxes(true) == 0) {
                isRestocking = true;
                state.setBaritoneGoalType(DataComponentTypes.TOOL);
                restockAmount = cachedToolShulkerAmount;
                state.setFlag9(true);
                MusheorSystem.debug("Tool restocking process started");
                return;
            }
            if (HighwayBuilder.enableItemRestocking() && !HighwayBuilder.hasFood()) {
                isRestocking = true;
                state.setBaritoneGoalType(DataComponentTypes.FOOD);
                restockAmount = cachedFoodRestockAmount;
                state.setFlag9(true);
                MusheorSystem.debug("Food restocking process started");
                return;
            }
            if (HighwayBuilder.shulkerRestocking() && HighwayBuilder.toolShulkerRestocking()
                && findShulkerWithComponent(DataComponentTypes.TOOL) == null && countPickaxes(true) == 0) {
                isRestocking = true;
                state.setBaritoneGoalType(DataComponentTypes.TOOL);
                restockAmount = cachedToolShulkerAmount;
                state.setFlag8(true);
                MusheorSystem.debug("Tool shulker restocking process started");
            }
        }
    }

    /** True if the block at {@code pos} is a container (chest/shulker/barrel/dropper/dispenser/hopper). */
    private static boolean isContainerBlock(BlockPos pos) { // was: psJq59YIbp3Z(BlockPos)
        var block = mc.world.getBlockState(pos).getBlock();
        return block instanceof ChestBlock || block instanceof ShulkerBoxBlock || block instanceof BarrelBlock
            || block instanceof DispenserBlock || block instanceof DropperBlock || block instanceof HopperBlock;
    }

    /** True if a container/shulker screen is currently open. */
    public static boolean isContainerOpen() { // was: psJq59YIbp3Z()
        if (mc.player == null) return false;
        ScreenHandler handler = mc.player.currentScreenHandler;
        return handler instanceof GenericContainerScreenHandler || handler instanceof ShulkerBoxScreenHandler;
    }

    /** Right-clicks the container at {@code pos} to open it. */
    public static boolean openContainerAt(BlockPos pos) { // was: FvaNWO(BlockPos)
        if (mc.player == null || mc.world == null) return false;
        if (!isContainerBlock(pos)) return false;
        if (isContainerOpen()) return true;
        Vec3d hitVec = Vec3d.ofCenter(pos);
        WorldUtils.lookAtBlock(pos);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);
        interactWith(hitResult);
        return false;
    }

    public static void interactWith(BlockHitResult bhr) { // was: FvaNWO(BlockHitResult)
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            mc.interactionManager.interactBlock(mc.world, Hand.MAIN_HAND, bhr); // sequenced
        }
    }

    /** True if the tool has more than the configured minimum durability remaining. */
    public static boolean hasDurability(ItemStack stack) { // was: FvaNWO(ItemStack)
        return stack.getMaxDamage() - stack.getDamage() > (Integer) MusheorSystem.Manager.minToolDurability.get();
    }

    /** Moves the first matching item from the main inventory into the first empty hotbar slot. */
    public static void moveToHotbar(Item item) { // was: FvaNWO(Item)
        int slot = 0;
        for (int i = 9; i < mc.player.getInventory().main.size(); i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) { slot = i; break; }
        }
        if (slot != 0) InvUtils.move().from(slot).to(firstEmptyHotbarSlot());
    }

    public static void moveToHotbar(ItemStack stack) { // was: Q90GLXQ0Pef(ItemStack)
        InvUtils.move().from(mc.player.getInventory().getSlotWithStack(stack)).to(firstEmptyHotbarSlot());
    }

    /** Selects the given hotbar slot (0-8) and syncs to the server. */
    public static void selectHotbarSlot(int slot) { // was: FvaNWO(int)
        if (mc.player != null && mc.getNetworkHandler() != null) {
            slot = Math.max(0, Math.min(8, slot));
            if (mc.player.getInventory().selectedSlot != slot) {
                mc.player.getInventory().selectedSlot = slot;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            }
        }
    }

    /** Index of the first empty hotbar slot (0-8), or a random one if none empty. */
    public static int firstEmptyHotbarSlot() { // was: SOYyh5IPg26f7F()
        if (mc.player == null) return 8;
        for (int i = 0; i <= 8; i++) if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        return Utils.random(1, 7);
    }

    /** Index of the first empty main-inventory slot (9-35), or a random one if none empty. */
    public static int firstEmptyInvSlot() { // was: rKbT3Ifwo()
        if (mc.player == null) return 35;
        for (int i = 9; i < 36; i++) if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        return Utils.random(9, 35);
    }

    /** Runs {@code action} with {@code slot} temporarily swapped to the selected slot, then swaps back. */
    public static boolean withSlotSwapped(int slot, Runnable action) { // was: FvaNWO(int, Runnable)
        if (mc.player == null || action == null) return false;
        if (slot < 0 || slot > 35) return false;
        boolean swapped = swapSlotToSelected(slot);
        try { action.run(); return true; }
        finally { if (swapped) swapSlotToSelected(slot); }
    }

    /** Swaps a slot to the currently selected hotbar slot via a SWAP click. */
    public static boolean swapSlotToSelected(int slot) { // was: Q90GLXQ0Pef(int)
        int currentSlot = mc.player.getInventory().selectedSlot;
        if (slot == currentSlot) return false;
        int screenSlot = slotToScreenId(slot);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, screenSlot, currentSlot, SlotActionType.SWAP, mc.player);
        MusheorSystem.debug("Swapped slot %d to selected in screen %s with ID: %d",
            slot, mc.player.currentScreenHandler.toString(), mc.player.currentScreenHandler.syncId);
        return true;
    }

    /** Player-inventory slot index → screen-handler slot id. */
    public static int slotToScreenId(int slot) { // was: psJq59YIbp3Z(int)
        return slot >= 0 && slot <= 8 ? 36 + slot : slot;
    }

    /** Main-inventory index of the first stack of {@code item}, or -1. */
    public static int findItemSlotIndex(Item item) { // was: Q90GLXQ0Pef(Item)
        for (int i = 0; i < Objects.requireNonNull(mc.player).getInventory().main.size(); i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    /** Drops one or the whole stack from the given slot. */
    public static void throwSlot(int slot, boolean entireStack) { // was: FvaNWO(int, boolean)
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotToScreenId(slot), entireStack ? 1 : 0, SlotActionType.THROW, mc.player);
        }
    }

    /** First ItemStack of {@code item} in the main inventory, or null. */
    public static ItemStack findItemStack(Item item) { // was: psJq59YIbp3Z(Item)
        for (int i = 0; i < Objects.requireNonNull(mc.player).getInventory().main.size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == item) return stack;
        }
        return null;
    }

    /** Counts how many of {@code item} are inside the given shulker stack. */
    public static int countInShulker(ItemStack shulker, Item item) { // was: FvaNWO(ItemStack, Item)
        ItemStack[] containerItems = new ItemStack[27];
        Utils.getItemsInContainerItem(shulker, containerItems);
        int count = 0;
        for (ItemStack stack : containerItems) if (!stack.isEmpty() && stack.getItem() == item) count += stack.getCount();
        return count;
    }

    /** Counts items in the shulker matching a component type. */
    public static int countInShulker(ItemStack shulker, DataComponentType<?> component) { // was: FvaNWO(ItemStack, component)
        ItemStack[] containerItems = new ItemStack[27];
        Utils.getItemsInContainerItem(shulker, containerItems);
        int count = 0;
        for (ItemStack stack : containerItems) if (!stack.isEmpty() && stack.getComponents().contains(component)) count += stack.getCount();
        return count;
    }

    /** Total count of {@code item} loose in the main inventory (not inside shulkers). */
    public static int countItemInInventory(Item item) { // was: SOYyh5IPg26f7F(Item)
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    /** Total count of {@code item}, including those inside shulker boxes. */
    public static int countItemIncludingShulkers(Item item) { // was: rKbT3Ifwo(Item)
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty()) {
                if (itemStack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) count += countInShulker(itemStack, item);
                if (itemStack.getItem() == item) count += itemStack.getCount();
            }
        }
        return count;
    }

    /** First shulker stack containing {@code item}, or null. */
    public static ItemStack findShulkerWith(Item item) { // was: r7hOYIKN2(Item)
        for (int i = 0; i < Objects.requireNonNull(mc.player).getInventory().main.size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock && shulkerContains(stack, item)) return stack;
        }
        return null;
    }

    /** The least-full (but non-empty) shulker containing {@code item}, or null. */
    public static ItemStack findLeastFullShulkerWith(Item item) { // was: oZHMlTL(Item)
        int least = 1729;
        ItemStack stack = null;
        for (int i = 0; i < Objects.requireNonNull(mc.player).getInventory().size(); i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                int count = countInShulker(itemStack, item);
                if (least > count && count > 0) { least = count; stack = itemStack; }
            }
        }
        return stack;
    }

    /** The least-full (non-empty) shulker with an item matching {@code component}, or null. */
    public static ItemStack findLeastFullShulkerWithComponent(DataComponentType<?> component) { // was: FvaNWO(component)
        int least = 99;
        ItemStack stack = null;
        for (int i = 0; i < Objects.requireNonNull(mc.player).getInventory().size(); i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                int count = countInShulker(itemStack, component);
                if (least > count && count > 0) { least = count; stack = itemStack; }
            }
        }
        return stack;
    }

    /** First shulker with an item matching {@code component}, or null. */
    public static ItemStack findShulkerWithComponent(DataComponentType<?> component) { // was: Q90GLXQ0Pef(component)
        for (int i = 0; i < Objects.requireNonNull(mc.player).getInventory().main.size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock && shulkerContainsComponent(stack, component)) return stack;
        }
        return null;
    }

    /** True if the shulker holds an item with {@code component} (tools require >50 durability). */
    public static boolean shulkerContainsComponent(ItemStack shulker, DataComponentType<?> component) { // was: Q90GLXQ0Pef(ItemStack, component)
        ItemStack[] containerItems = new ItemStack[27];
        Utils.getItemsInContainerItem(shulker, containerItems);
        for (ItemStack stack : containerItems) {
            if (!stack.isEmpty() && stack.getComponents().contains(component)) {
                if (component != DataComponentTypes.TOOL) return true;
                if (stack.getMaxDamage() - stack.getDamage() > 50) return true;
            }
        }
        return false;
    }

    /** True if the shulker holds {@code targetItem} (pickaxes require >50 durability). */
    public static boolean shulkerContains(ItemStack shulker, Item targetItem) { // was: Q90GLXQ0Pef(ItemStack, Item)
        ItemStack[] containerItems = new ItemStack[27];
        Utils.getItemsInContainerItem(shulker, containerItems);
        for (ItemStack stack : containerItems) {
            if (!stack.isEmpty() && stack.getItem() == targetItem) {
                if (!VersionHelper.get().isPickaxe(stack)) return true;
                if (stack.getMaxDamage() - stack.getDamage() > 50) return true;
            }
        }
        return false;
    }

    /** True if the shulker box is empty. */
    public static boolean isShulkerEmpty(ItemStack shulker) { // was: psJq59YIbp3Z(ItemStack)
        ComponentMap components = shulker.getComponents();
        if (!components.contains(DataComponentTypes.CONTAINER)) return false;
        ContainerComponentAccessor container = (ContainerComponentAccessor) components.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            for (ItemStack stack : VersionHelper.get().getStacks(container)) if (!stack.isEmpty()) return false;
        }
        return true;
    }

    /** Selects {@code item} from the hotbar, or moves it there first. */
    public static void selectItem(Item item) { // was: xQr5FhbwpQPWgIQ(Item)
        assert mc.player != null;
        FindItemResult result = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == item);
        if (result.found()) InvUtils.swap(result.slot(), false);
        else moveToHotbar(item);
        MusheorSystem.debug("Swapping to %s", Registries.ITEM.getId(item).toString());
    }

    /** Selects the best pickaxe in the hotbar (skips silk touch if {@code silkTouch}). */
    public static void selectBestPickaxe(boolean silkTouch) { // was: Q90GLXQ0Pef(boolean)
        assert mc.player != null;
        double bestScore = -1.0;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!Utils.hasEnchantments(itemStack, Enchantments.SILK_TOUCH) || !silkTouch) {
                double score = itemStack.getMiningSpeedMultiplier(Blocks.ENDER_CHEST.getDefaultState());
                if (score > bestScore) { bestScore = score; bestSlot = i; }
            }
        }
        if (bestSlot != -1) InvUtils.swap(bestSlot, false);
    }

    /** Returns the fastest-mining tool for {@code state} anywhere in the inventory. */
    public static ItemStack findBestTool(BlockState state) { // was: FvaNWO(BlockState)
        if (mc.player == null) return ItemStack.EMPTY;
        ItemStack bestStack = ItemStack.EMPTY;
        double bestSpeed = 0.0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(Boolean) MusheorSystem.Manager.preventToolBreaking.get() || hasDurability(stack)) {
                double speed = stack.getMiningSpeedMultiplier(state);
                if (speed > bestSpeed) { bestSpeed = speed; bestStack = stack; }
            }
        }
        return bestStack;
    }

    /** Selects the best usable tool for the block at {@code blockPos}. */
    public static void selectBestToolFor(BlockPos blockPos) { // was: Q90GLXQ0Pef(BlockPos)
        assert mc.world != null && mc.player != null && blockPos != null;
        BlockState state = mc.world.getBlockState(blockPos);
        double bestSpeed = 0.0;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && (!(Boolean) MusheorSystem.Manager.preventToolBreaking.get() || hasDurability(itemStack))) {
                double speed = itemStack.getMiningSpeedMultiplier(state);
                if (speed > bestSpeed && itemStack.isSuitableFor(state)) { bestSpeed = speed; bestSlot = i; }
            }
        }
        if (bestSlot != -1) {
            mc.player.getInventory().selectedSlot = bestSlot;
        } else if (VersionHelper.get().isTool(mc.player.getMainHandStack()) && !hasDurability(mc.player.getMainHandStack())) {
            InvUtils.move().from(mc.player.getInventory().selectedSlot).to(firstEmptyInvSlot());
        } else {
            for (int i = 9; i < mc.player.getInventory().size(); i++) {
                ItemStack itemStack = mc.player.getInventory().getStack(i);
                if (!itemStack.isEmpty() && (!(Boolean) MusheorSystem.Manager.preventToolBreaking.get() || hasDurability(itemStack))) {
                    double speed = itemStack.getMiningSpeedMultiplier(state);
                    if (speed > bestSpeed && itemStack.isSuitableFor(state)) { bestSpeed = speed; bestSlot = i; }
                }
            }
            if (bestSlot != -1) {
                moveToHotbar(mc.player.getInventory().getStack(bestSlot));
                mc.player.getInventory().selectedSlot = bestSlot;
            }
        }
    }

    /** Counts usable pickaxes ({@code allowSilkTouch} to include silk-touch ones). */
    public static int countPickaxes(boolean allowSilkTouch) { // was: psJq59YIbp3Z(boolean)
        assert mc.player != null;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (VersionHelper.get().isPickaxe(itemStack)) {
                if ((Boolean) MusheorSystem.Manager.preventToolBreaking.get()) {
                    if (!allowSilkTouch && !Utils.hasEnchantments(itemStack, Enchantments.SILK_TOUCH)
                        && itemStack.getMaxDamage() - itemStack.getDamage() > (Integer) MusheorSystem.Manager.minToolDurability.get()) count++;
                    if (allowSilkTouch && itemStack.getMaxDamage() - itemStack.getDamage() > (Integer) MusheorSystem.Manager.minToolDurability.get()) count++;
                } else if (!allowSilkTouch && !Utils.hasEnchantments(itemStack, Enchantments.SILK_TOUCH)) count++;
                else if (allowSilkTouch) count++;
            }
        }
        return count;
    }

    /** Counts pickaxes at/below the minimum durability threshold (broken). */
    public static int countBrokenPickaxes() { // was: r7hOYIKN2()
        assert mc.player != null;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (VersionHelper.get().isPickaxe(itemStack) && itemStack.getMaxDamage() - itemStack.getDamage() <= (Integer) MusheorSystem.Manager.minToolDurability.get()) count++;
        }
        return count;
    }

    /** Container-screen slot id of a broken pickaxe (in the open container), or -1. */
    public static int brokenPickaxeContainerSlot() { // was: oZHMlTL()
        assert mc.player != null;
        for (int i = 0; i < mc.player.currentScreenHandler.slots.size(); i++) {
            ItemStack itemStack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (VersionHelper.get().isPickaxe(itemStack)
                && itemStack.getMaxDamage() - itemStack.getDamage() <= (Integer) MusheorSystem.Manager.minToolDurability.get() && i > 26) return i;
        }
        return -1;
    }

    /** Number of empty slots in the player's main inventory. */
    public static int countEmptyInventorySlots() { // was: xQr5FhbwpQPWgIQ()
        int emptyCount = 0;
        assert mc.player != null;
        for (ItemStack itemStack : mc.player.getInventory().main) if (itemStack.isEmpty()) emptyCount++;
        return emptyCount;
    }

    /** Number of empty slots in the given container's own (non-player) region. */
    public static int countEmptyContainerSlots(ScreenHandler handler) { // was: FvaNWO(ScreenHandler)
        int emptyCount = 0;
        int playerInvOffset = SlotUtils.indexToId(9);
        for (int i = 0; i < playerInvOffset; i++) if (handler.getSlot(i).getStack().isEmpty()) emptyCount++;
        return emptyCount;
    }

    /** Moves a shulker containing {@code itemInShulker} to {@code hotbarSlot}. */
    public static boolean moveShulkerToHotbar(Item itemInShulker, int hotbarSlot) { // was: FvaNWO(Item, int)
        ItemStack stack = findLeastFullShulkerWith(itemInShulker);
        if (stack != null) {
            int slot = mc.player.getInventory().getSlotWithStack(stack);
            if (slot != -1) { InvUtils.move().from(slot).to(hotbarSlot); MusheorSystem.debug("Moving shulker with " + itemInShulker + " to slot " + hotbarSlot); }
            return true;
        }
        MusheorSystem.debug("No shulker with " + itemInShulker + " found in the inventory");
        return false;
    }

    /** Moves a shulker containing an item with {@code type} to {@code hotbarSlot}. */
    public static boolean moveShulkerToHotbar(DataComponentType<?> type, int hotbarSlot) { // was: FvaNWO(component, int)
        ItemStack stack = findLeastFullShulkerWithComponent(type);
        if (stack != null) {
            int slot = mc.player.getInventory().getSlotWithStack(stack);
            if (slot != -1) { InvUtils.move().from(slot).to(hotbarSlot); MusheorSystem.debug("Moving shulker with " + type + " to slot " + hotbarSlot); }
            return true;
        }
        MusheorSystem.debug("No shulker with " + type + " found in the inventory");
        return false;
    }

    /** Returns the item type of a nearby dropped shulker within {@code radius}, or null. */
    public static Item findNearbyShulkerItem(double radius) { // was: FvaNWO(double)
        if (mc.world == null || mc.player == null) return null;
        Box area = mc.player.getBoundingBox().expand(radius);
        for (ItemEntity itemEntity : mc.world.getEntitiesByClass(ItemEntity.class, area, e -> true)) {
            ItemStack stack = itemEntity.getStack();
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) return itemEntity.getStack().getItem();
        }
        return null;
    }

    /**
     * The multi-tick restock state machine: places a shulker at the restock position,
     * opens it, and shift-clicks the requested items (tools/food/materials) out of it.
     */
    public static void runRestockProcess(DataComponentType<?> type, int stacks) { // was: Q90GLXQ0Pef(component, int)
        if (mc.world == null || mc.player == null) return;
        HighwayState state = HighwayState.getInstance();
        if (musheor.utils.PlayerUtils.isGatheringItem() || state.isFlag11()) { isRestocking = false; return; }
        musheor.utils.PlayerUtils.setAutoWalk(false);
        Module nuker = Modules.get().get(KekNuker.class);
        if (nuker.isActive()) nuker.toggle();

        if (delayTicks > 0) { delayTicks--; return; }
        if (!restockInitialized) {
            selectHotbarSlot(8);
            VersionHelper.get().syncInventory();
            restockInitialized = true;
            MusheorSystem.debug("Restocking has been initialized, proceeding...");
        }
        if (mc.player.getBlockPos().getSquaredDistance(targetPos) > 1.5) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(targetPos));
            MusheorSystem.debug("Player is not at restocking position, calling baritone...");
        } else if (!shulkerMovedToHotbar) {
            Item item = null;
            if ((type == DataComponentTypes.TOOL || type == DataComponentTypes.FOOD) && moveShulkerToHotbar(type, 8)) shulkerMovedToHotbar = true;
            if (type == DataComponentTypes.CONTAINER) {
                if (findShulkerWith(HighwayBuilder.getFillBlock().asItem()) != null) item = Items.OBSIDIAN;
                else if (findShulkerWith(Items.ENDER_CHEST) != null) item = Items.ENDER_CHEST;
                if (moveShulkerToHotbar(item, 8)) shulkerMovedToHotbar = true;
            }
            delayTicks = cachedSwapDelay;
        } else if (!shulkerPlaced) {
            shulkerPlacePos = WorldUtils.offsetTwoBlocks(HighwayBuilder.getDirection());
            if (cachedPlaceDelay > 0) {
                musheor.utils.PlayerUtils.setSneak(false);
                cachedPlaceDelay--;
                WorldUtils.lookAtBlockSide(shulkerPlacePos, Direction.DOWN);
                return;
            }
            if (mc.world.getBlockState(shulkerPlacePos).getBlock() instanceof ShulkerBoxBlock) {
                MusheorSystem.debug("Shulker box placed successfully!");
                shulkerPlaced = true;
                delayTicks = cachedPlaceDelay;
            } else if (!BlockUtils.canPlace(shulkerPlacePos, false) || BlockUtils.canPlace(shulkerPlacePos, true)) {
                MusheorSystem.debug("Placing shulkerbox at x: %s y: %s z: %s", shulkerPlacePos.getX(), shulkerPlacePos.getY(), shulkerPlacePos.getZ());
                BlockUtils.place(shulkerPlacePos, Hand.MAIN_HAND, 8, false, 0, true, false, false);
                return;
            } else {
                MusheorSystem.debug("Unable to place shulkerbox...");
                if (mc.world.getBlockState(shulkerPlacePos).getBlock() != Blocks.AIR) {
                    BlockUtils.breakBlock(shulkerPlacePos, true);
                    MusheorSystem.debug("Breaking obstructing block at restocking position");
                }
            }
        } else if (!(mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler)) {
            WorldUtils.lookAtBlock(shulkerPlacePos);
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
                Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(shulkerPlacePos), Direction.UP, shulkerPlacePos, false), 0));
            MusheorSystem.debug("Opening shulkerbox at x: %s y: %s z: %s", shulkerPlacePos.getX(), shulkerPlacePos.getY(), shulkerPlacePos.getZ());
            delayTicks = cachedOpenDelay;
        } else if (afterOpenDelayTicks > 0) {
            afterOpenDelayTicks--;
        } else {
            if (stolenCount < stacks && currentShulkerSlot < 27) {
                ScreenHandler handler = mc.player.currentScreenHandler;
                if (justStole) {
                    if (stealDelayCounter < INSTANCE.stealingDelay.get()) { stealDelayCounter++; return; }
                    justStole = false;
                }
                ItemStack currentStack = handler.getSlot(currentShulkerSlot).getStack();
                int brokenPickaxeSlot = brokenPickaxeContainerSlot();
                if (type == DataComponentTypes.TOOL) {
                    if (VersionHelper.get().isPickaxe(currentStack)) {
                        if (hasDurability(currentStack)) {
                            InvUtils.shiftClick().slotId(currentShulkerSlot);
                            if (HighwayBuilder.swapBrokenPickaxes() && brokenPickaxeSlot != -1) InvUtils.shiftClick().slotId(brokenPickaxeSlot);
                            MusheorSystem.debug("Stealing {%s} from slot [%s]", currentStack.getItem().getName(), currentShulkerSlot);
                            justStole = true; currentShulkerSlot++; stolenCount++; stealDelayCounter = 0;
                        } else { currentShulkerSlot++; MusheorSystem.debug("Checking next slot..."); }
                    }
                } else if (type == DataComponentTypes.FOOD) {
                    if (currentStack.getComponents().contains(DataComponentTypes.FOOD)) {
                        if (!((List<?>) ((AutoEat) Modules.get().get(AutoEat.class)).blacklist.get()).contains(currentStack.getItem())) {
                            InvUtils.shiftClick().slotId(currentShulkerSlot);
                            MusheorSystem.debug("Stealing {%s} from slot [%s]", currentStack.getItem().getName(), currentShulkerSlot);
                            justStole = true; currentShulkerSlot++; stolenCount++; stealDelayCounter = 0;
                        } else { currentShulkerSlot++; MusheorSystem.debug("Checking next slot..."); }
                    }
                } else if (currentStack.getItem() != HighwayBuilder.getFillBlock().asItem() && currentStack.getItem() != Items.ENDER_CHEST) {
                    currentShulkerSlot++; MusheorSystem.debug("Checking next slot...");
                } else {
                    InvUtils.shiftClick().slotId(currentShulkerSlot);
                    MusheorSystem.debug("Stealing {%s} from slot [%s]", currentStack.getItem().getName(), currentShulkerSlot);
                    justStole = true; currentShulkerSlot++; stolenCount++; stealDelayCounter = 0;
                }
            } else {
                MusheorSystem.debug("Completed item restocking process");
                resetState(true);
            }
        }
    }

    /** Post-restock cleanup: closes the shulker, mines it back up and collects it. */
    public static void handlePostRestock() { // was: OMMZL1F3q()
        if (mc.player == null || mc.world == null || shulkerPlacePos == null) return;
        if (isPending || HighwayBuilder.isEchestFarmerActive() || isRestocking) return;
        HighwayState state = HighwayState.getInstance();
        if (mc.currentScreen instanceof HandledScreen && mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
            mc.player.closeHandledScreen();
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
        } else {
            Module nuker = Modules.get().get(KekNuker.class);
            if (!nuker.isActive()) nuker.toggle();
            if (postRestock) {
                MusheorSystem.debug("Post-restocking task running...");
                musheor.utils.PlayerUtils.setAutoWalk(false);
                selectBestToolFor(shulkerPlacePos);
                if (mc.world.getBlockState(shulkerPlacePos).getBlock() instanceof ShulkerBoxBlock
                    || mc.world.getBlockState(shulkerPlacePos).getBlock() instanceof BlockWithEntity) {
                    breakingContainer = true;
                    if (PlayerUtils.isWithinReach(shulkerPlacePos)) {
                        musheor.utils.PlayerUtils.cancelPathing();
                        if (BlockUtils.breakBlock(shulkerPlacePos, true)) return;
                    } else if (!BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(shulkerPlacePos));
                    }
                }
                if (!mc.world.getBlockState(shulkerPlacePos).isAir()) return;
                breakingContainer = false;
                if (cachedRestockDelay > 0) { cachedRestockDelay--; return; }
                Item item = findNearbyShulkerItem(16.0);
                if (item != null) musheor.utils.PlayerUtils.gatherItem(item, false);
                if (mc.world.getBlockState(shulkerPlacePos).getBlock().equals(Blocks.AIR)
                    && !musheor.utils.PlayerUtils.isGatheringItem() && !state.isFlag9() && !state.isFlag7()
                    && !breakingContainer && !state.isFlag10()) {
                    resetState(false);
                }
            }
        }
    }
}
