// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.utils;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
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
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import musheor.compat.VersionHelper;
import musheor.modules.automation.HighwayBuilder;
import musheor.modules.automation.KekNuker;
import musheor.modules.features.KekMine;
import musheor.musheor;
import musheor.utils.PlayerUtils;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.system.MusheorSystem;
import net.minecraft.class_1268;   // Hand
import net.minecraft.class_1542;   // ItemEntity
import net.minecraft.class_1657;   // PlayerEntity (for clickSlot)
import net.minecraft.Text;   // ScreenHandler
import net.minecraft.class_1707;   // ShulkerBoxScreenHandler
import net.minecraft.ClientPlayerEntity;   // SlotActionType
import net.minecraft.class_1733;   // GenericContainerScreenHandler
import net.minecraft.AbstractClientPlayerEntity;   // BlockItem
import net.minecraft.ItemStack;   // Item
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.Items;   // Items
import net.minecraft.class_1893;   // Enchantments
import net.minecraft.Blocks;   // Blocks
import net.minecraft.Block;   // Block
import net.minecraft.class_2281;   // TrappedChestBlock (or similar container block)
import net.minecraft.class_2315;   // BarrelBlock (or similar)
import net.minecraft.class_2325;   // HopperBlock (or similar)
import net.minecraft.class_2336;   // AbstractBannerBlock / dropped shulker
import net.minecraft.BlockPos;   // BlockPos
import net.minecraft.Direction;   // Direction
import net.minecraft.class_2377;   // ChestBlock (or similar)
import net.minecraft.class_238;    // Box
import net.minecraft.class_2382;   // Vec3i
import net.minecraft.class_243;    // Vec3d
import net.minecraft.class_2480;   // ShulkerBoxBlock
import net.minecraft.class_2596;   // Packet
import net.minecraft.class_2680;   // BlockState
import net.minecraft.class_2815;   // CloseHandledScreenC2SPacket
import net.minecraft.class_2868;   // UpdateSelectedSlotC2SPacket
import net.minecraft.MinecraftClient;    // MinecraftClient
import net.minecraft.class_3708;   // EnderChestBlock (or similar)
import net.minecraft.Screen;   // BlockHitResult
import net.minecraft.class_437;    // Screen
import net.minecraft.class_465;    // HandledScreen
import net.minecraft.class_5321;   // RegistryKey<Enchantment>
import net.minecraft.class_7923;   // Registries
import net.minecraft.class_9323;   // ComponentMap
import net.minecraft.class_9331;   // DataComponentType<?>
import net.minecraft.MutableText;   // DataComponentTypes
                                   //   .field_49622 = CONTAINER
                                   //   .field_50077 = TOOL (pickaxe tag)
                                   //   .field_50075 = FOOD

public class InventoryManager extends Module {

    // Setting groups
    private final SettingGroup generalGroup;   // was: cyoLu6eIt6
    private final SettingGroup itemsGroup;     // was: qRmSSlpJD45K
    private final SettingGroup shulkersGroup;  // was: cEziIX6T1FH51zp
    private final SettingGroup delaysGroup;    // was: LyKbtsWfUTOp

    // MinecraftClient singleton
    private static final MinecraftClient mc = MinecraftClient.method_1551(); // was: tenSxlKMdS

    // ---- Runtime delay counters (populated by loadSettings() each restock cycle) ----
    private static int delayAfterOpeningContainer;  // was: u2aIMe
    private static int delayBeforeOpeningContainer; // was: YUloy8bsbqV1W
    private static int delayAfterRestocking;        // was: LZHVZ8lp
    private static int delayBeforeSwapping;         // was: Mzh8fL

    // ---- Public state flags ----
    public static BlockPos restockPosition;   // was: NrfIVPgqB9 — where the player must stand to restock
    private static BlockPos shulkerPlacePos;  // was: rCDhCuDWChNyN — where the shulker box is placed
    private static int delayBeforePlacingContainer; // was: ffp8odtY
    private static boolean inventorySynced;     // was: zs6ClAzOtn
    private static class_437 savedScreen;       // was: Yg7bRB31A9 — screen saved before restocking
    private static int shulkerSlotIndex;        // was: kEAufup4dB4C5gWu — which shulker slot we're stealing from
    public static boolean isRestocking;         // was: QFWUbSJ63lmQY
    public static boolean isBreakingShulker;    // was: hdUw3g5aanfWo0
    private static boolean placing;             // was: P6yYYHTR8E86Zxd4
    private static boolean wcuFlag;             // was: WcuFGU7h9UWk (purpose unclear, likely unused)
    private static boolean akpFlag;             // was: aKpZs7PoVp3   (purpose unclear)
    private static boolean isBreakingShulker2;  // was: eFjumaf
    private static boolean shulkerPlaced;       // was: Exep6vc
    private static boolean shulkerMoved;        // was: KzDewjZPFQs7Sa
    public static boolean isReady;              // was: S6m8k9j
    private static boolean kdrFlag;             // was: KDrrObsCOepIjWs6
    private static boolean lxFlag;              // was: LX9onpO
    public static boolean isDoingPostRestock;   // was: NZ3iHWsF
    private static List<ItemStack> itemList;   // was: K1QOv72
    private static int stealingDelay;           // was: FogPkUI07lx
    private static int itemsStolenCount;        // was: ovCoOS
    private static int stealingDelayCounter;    // was: IdtkXqRa7u15
    private static boolean waitingForDelay;     // was: wyAYo0HfI
    public static int targetRestockAmount;      // was: LrAtLm
    public static boolean o3Flag;               // was: o3zXdkGsGBwu
    public static boolean jxFlag;               // was: jxlAMMdZFfChL4z

    // ---- Cached setting values (read from module settings on each restock) ----
    static int toolRestockAmount;         // was: joEKEM5qXHgDb
    static int foodRestockAmount;         // was: xiTcr7xO
    static int materialShulkerRestockAmt; // was: IKmIJhKqSsB
    static int toolShulkerRestockAmt;     // was: Oa1nryPuWhQ

    /** Singleton instance. */
    public static InventoryManager INSTANCE; // was: BOhrdyrKEar

    // ---- Persistent settings ----
    public final Setting<Integer> toolRestockAmountSetting;       // was: fhsNZCzfh5
    public final Setting<Integer> foodRestockAmountSetting;       // was: KhoTk6jQ7SYpWnO
    public final Setting<Integer> materialShulkerRestockSetting;  // was: tDasWp7g5oT
    public final Setting<Integer> toolShulkerRestockSetting;      // was: jnIsKkwW
    public final Setting<Integer> stealingDelaySetting;           // was: HYqvAIB8kJyKP9MP
    public final Setting<Integer> delayBeforePlacingSetting;      // was: lCQE4G
    public final Setting<Integer> delayBeforeOpeningSetting;      // was: Ss5IiCQ1LWLdtIZ
    public final Setting<Integer> delayAfterOpeningSetting;       // was: nqXWHiZIUs11V
    public final Setting<Integer> delayBeforeSwappingSetting;     // was: siusW1GwinB
    public final Setting<Integer> delayAfterRestockingSetting;    // was: K5GiP9c

    public InventoryManager() {
        super(musheor.AUTOMATION, "inventory-manager", "Manager module for inventory related stuff");
        this.generalGroup  = this.settings.getDefaultGroup();
        this.itemsGroup    = this.settings.createGroup("Items");
        this.shulkersGroup = this.settings.createGroup("Shulkers");
        this.delaysGroup   = this.settings.createGroup("Delays");

        this.toolRestockAmountSetting = this.itemsGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("tool-restock-amount"))
                .description("How many pickaxes should be taken on restock when the player runs out."))
                .defaultValue((Object)1)).sliderRange(1, 20).build());

        this.foodRestockAmountSetting = this.itemsGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("food-restock-amount"))
                .description("How many stacks of food should be taken on restock when the player runs out."))
                .defaultValue((Object)1)).sliderRange(1, 10).build());

        this.materialShulkerRestockSetting = this.shulkersGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("material-shulker-restock-amount"))
                .description("How many shulkers of pavement materials should be taken when performing an enderchest restock."))
                .defaultValue((Object)1)).sliderRange(1, 20).build());

        this.toolShulkerRestockSetting = this.shulkersGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("tool-shulker-restock-amount"))
                .description("How many shulkers of pickaxes should be taken when performing an enderchest restock."))
                .defaultValue((Object)1)).sliderRange(1, 10).build());

        this.stealingDelaySetting = this.delaysGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("stealing-delay"))
                .description("Time the player should wait before stealing another slot from a container in ticks"))
                .defaultValue((Object)2)).sliderRange(1, 20).build());

        this.delayBeforePlacingSetting = this.delaysGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("delay-before-placing-container"))
                .description("Time the player should wait before placing a container in ticks"))
                .defaultValue((Object)5)).sliderRange(1, 20).build());

        this.delayBeforeOpeningSetting = this.delaysGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("delay-before-opening-container"))
                .description("Time the player should wait before opening a container in ticks"))
                .defaultValue((Object)5)).sliderRange(1, 20).build());

        this.delayAfterOpeningSetting = this.delaysGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("delay-after-opening-container"))
                .description("Time the player should wait after opening a container in ticks"))
                .defaultValue((Object)5)).sliderRange(1, 20).build());

        this.delayBeforeSwappingSetting = this.delaysGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("delay-before-swapping-item"))
                .description("Time the player should wait before it attempts to swap items"))
                .defaultValue((Object)5)).sliderRange(1, 20).build());

        this.delayAfterRestockingSetting = this.delaysGroup.add((Setting)
            ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)
                new IntSetting.Builder().name("delay-after-restocking"))
                .description("Time the player should wait completing the restocking process"))
                .defaultValue((Object)10)).sliderRange(1, 20).build());

        INSTANCE = this;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WButton btnPavement = (WButton) list.add((WWidget) theme.button("Test pavement block restock")).widget();
        WButton btnTool     = (WButton) list.add((WWidget) theme.button("Test tool restock")).widget();
        WButton btnFood     = (WButton) list.add((WWidget) theme.button("Test food restock")).widget();
        WButton btnShulker  = (WButton) list.add((WWidget) theme.button("Test pavement block shulker restock")).widget();

        btnPavement.action = () -> {
            restockPosition = InventoryManager.mc.player.getBlockPos(); // getBlockPos
            InventoryManager.doRestockFromShulker(MutableText.field_49622, InventoryManager.countEmptySlots() / 2); // CONTAINER
        };
        btnTool.action = () -> {
            restockPosition = InventoryManager.mc.player.getBlockPos();
            InventoryManager.doRestockFromShulker(MutableText.field_50077, toolRestockAmount); // TOOL
        };
        btnFood.action = () -> {
            restockPosition = InventoryManager.mc.player.getBlockPos();
            InventoryManager.doRestockFromShulker(MutableText.field_50075, foodRestockAmount); // FOOD
        };
        return list;
    }

    /** Reads all delay/amount settings from the module and caches them in static fields. */
    public static void loadSettings() { // was: kl5Nqm9U9tT9R48
        Module m = Modules.get().get("inventory-manager");
        delayBeforePlacingContainer = (Integer) m.settings.get("delay-before-placing-container").get();
        delayBeforeOpeningContainer = (Integer) m.settings.get("delay-before-opening-container").get();
        delayAfterOpeningContainer  = (Integer) m.settings.get("delay-after-opening-container").get();
        delayBeforeSwapping         = (Integer) m.settings.get("delay-before-swapping-item").get();
        delayAfterRestocking        = (Integer) m.settings.get("delay-after-restocking").get();
        stealingDelay               = (Integer) m.settings.get("stealing-delay").get();
        materialShulkerRestockAmt   = (Integer) m.settings.get("material-shulker-restock-amount").get();
        toolShulkerRestockAmt       = (Integer) m.settings.get("tool-shulker-restock-amount").get();
        toolRestockAmount           = (Integer) m.settings.get("tool-restock-amount").get();
        foodRestockAmount           = (Integer) m.settings.get("food-restock-amount").get();
    }

    /** Resets all restock state. Pass {@code retainPostRestockFlag=true} to keep isDoingPostRestock set. */
    public static void resetState(boolean retainPostRestockFlag) { // was: TAdu5cndwWu3A1(boolean)
        HighwayState state = HighwayState.getInstance();
        InventoryManager.loadSettings();
        isDoingPostRestock = false;
        state.setRestockingMaterials(false);    // was: J2pm2c07elEb5G
        state.setRestockingFromEchest(false);   // was: LoFK6z05DRRnOV
        shulkerMoved = false;
        shulkerPlaced = false;
        itemsStolenCount = 0;
        shulkerSlotIndex = 0;
        stealingDelayCounter = 0;
        isReady = true;
        placing = false;
        wcuFlag = false;
        restockPosition = null;
        kdrFlag = false;
        lxFlag = false;
        isRestocking = false;
        state.setContainerOpen(false);          // was: ZbTtF5KYyGL9YXed
        state.setTargetRestockType(null);       // was: Gt56Sj4a6BWhgB(DataComponentType)
        targetRestockAmount = 0;
        state.setRestockPending(false);         // was: J9PiTNS
        if (retainPostRestockFlag) {
            isDoingPostRestock = true;
            state.setRestockPending(true);
        }
    }

    /**
     * Checks whether a restock is needed and initiates the appropriate restock sequence.
     * Called each tick while HighwayBuilder is running.
     */
    public static void checkRestockNeeded() { // was: Dzj74FIoxmie
        HighwayState state = HighwayState.getInstance();
        if (HighwayBuilder.INSTANCE.mode.get() == HighwayBuilder.Mode.SPECTATOR) { // was: OUSKA4lEld
            return;
        }
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.NORMAL) { // was: e4uKoS
            // Echest shulker restocks (need to grab material shulkers from echest)
            if (HighwayBuilder.isEchestRestockEnabled()  // was: dEyMylfkRxcem4F
                    && HighwayBuilder.isOutOfMaterials()  // was: oGrnfoe87ZeN
                    && InventoryManager.findShulkerWithItem(Items.field_8466) == null   // no obsidian shulker
                    && InventoryManager.findShulkerWithItem(HighwayBuilder.getPavingBlock().method_8389()) == null) {
                isRestocking = true;
                state.setTargetRestockType(MutableText.field_49622); // CONTAINER
                targetRestockAmount = materialShulkerRestockAmt;
                state.setRestockingFromEchest(true);
                MusheorSystem.debug("Echest shulker restocking process started", new Object[0]);
                return;
            }
            if (HighwayBuilder.isEchestRestockEnabled()
                    && HighwayBuilder.isOutOfTools()       // was: nQgi06
                    && InventoryManager.findShulkerWithComponent(MutableText.field_50077) == null // no tool shulker
                    && InventoryManager.countPickaxes(false) == 0) {
                isRestocking = true;
                state.setTargetRestockType(MutableText.field_50077); // TOOL
                targetRestockAmount = toolShulkerRestockAmt;
                state.setRestockingFromEchest(true);
                MusheorSystem.debug("Tool shulker restocking process started", new Object[0]);
                return;
            }
            // Material restock from shulker (player has shulker, needs to unload into inventory)
            if (HighwayBuilder.isAutoWalkMode()  // was: jll9Iyc1Ftxi
                    && (InventoryManager.findShulkerWithItem(Items.field_8466) != null
                     || InventoryManager.findShulkerWithItem(HighwayBuilder.getPavingBlock().method_8389()) != null)) {
                isRestocking = true;
                state.setTargetRestockType(MutableText.field_49622); // CONTAINER
                targetRestockAmount = InventoryManager.countEmptySlots() / 2;
                state.setRestockingMaterials(true);
                MusheorSystem.debug("Material restocking process started", new Object[0]);
                return;
            }
            if (HighwayBuilder.isAutoWalkMode()
                    && InventoryManager.findShulkerWithComponent(MutableText.field_50077) != null
                    && InventoryManager.countPickaxes(false) == 0) {
                isRestocking = true;
                state.setTargetRestockType(MutableText.field_50077); // TOOL
                targetRestockAmount = toolRestockAmount;
                state.setRestockingMaterials(true);
                MusheorSystem.debug("Pickaxe restocking process started", new Object[0]);
                return;
            }
            if (HighwayBuilder.isAutoWalkMode()
                    && InventoryManager.findShulkerWithComponent(MutableText.field_50075) != null) { // FOOD
                isRestocking = true;
                state.setTargetRestockType(MutableText.field_50075);
                targetRestockAmount = foodRestockAmount;
                state.setRestockingMaterials(true);
                MusheorSystem.debug("Food restocking process started", new Object[0]);
                return;
            }
        }
        if (HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.ELYTRA) { // was: flZYoiXwrl
            if (HighwayBuilder.isAutoWalkMode()
                    && InventoryManager.findShulkerWithComponent(MutableText.field_50077) != null
                    && InventoryManager.countPickaxes(true) == 0) {
                isRestocking = true;
                state.setTargetRestockType(MutableText.field_50077);
                targetRestockAmount = toolShulkerRestockAmt;
                state.setRestockingMaterials(true);
                MusheorSystem.debug("Tool restocking process started", new Object[0]);
                return;
            }
            if (HighwayBuilder.isAutoWalkMode() && !HighwayBuilder.hasEnoughFood()) { // was: l92qSNnpKrYO
                isRestocking = true;
                state.setTargetRestockType(MutableText.field_50075);
                targetRestockAmount = foodRestockAmount;
                state.setRestockingMaterials(true);
                MusheorSystem.debug("Food restocking process started", new Object[0]);
                return;
            }
            if (HighwayBuilder.isEchestRestockEnabled()
                    && HighwayBuilder.isOutOfTools()
                    && InventoryManager.findShulkerWithComponent(MutableText.field_50077) == null
                    && InventoryManager.countPickaxes(true) == 0) {
                isRestocking = true;
                state.setTargetRestockType(MutableText.field_50077);
                targetRestockAmount = toolShulkerRestockAmt;
                state.setRestockingFromEchest(true);
                MusheorSystem.debug("Tool shulker restocking process started", new Object[0]);
                return;
            }
        }
    }

    /** Returns true if the block at the given position is a container type (chest, barrel, etc.). */
    private static boolean isContainerBlock(BlockPos pos) { // was: xG2PP8jo4RWLS(BlockPos)
        Block block = InventoryManager.mc.world.getBlockState(pos).getBlock();
        return block instanceof class_2281 // TrappedChestBlock
            || block instanceof class_2480 // ShulkerBoxBlock
            || block instanceof class_3708 // EnderChestBlock
            || block instanceof class_2377 // ChestBlock
            || block instanceof class_2315 // BarrelBlock
            || block instanceof class_2325; // HopperBlock
    }

    /** Returns true if a container screen (shulker box or generic chest) is currently open. */
    public static boolean isContainerOpen() { // was: FeGlqzs7Rjvi
        if (InventoryManager.mc.field_1724 == null) return false;
        Text screen = InventoryManager.mc.player.field_7512; // currentScreenHandler
        return screen instanceof class_1707 || screen instanceof class_1733;
    }

    /**
     * If the block at the given position is a container and no screen is open,
     * sends a USE_BLOCK packet to open it. Returns true if screen is already open.
     */
    public static boolean openContainerAt(BlockPos pos) { // was: LoFK6z05DRRnOV
        if (InventoryManager.mc.field_1724 == null || InventoryManager.mc.field_1687 == null) return false;
        if (!InventoryManager.isContainerBlock(pos)) return false;
        if (InventoryManager.isContainerOpen()) return true;
        class_243 hitVec = class_243.method_24953((class_2382) pos); // Vec3d.of
        Screen hitResult = new Screen(hitVec, Direction.field_11036, pos, false); // Direction.UP
        InventoryManager.sendUsePacket(hitResult);
        return false;
    }

    /** Sends a PlayerInteractBlockC2SPacket (USE_BLOCK action) for the given hit result. */
    public static void sendUsePacket(Screen hitResult) { // was: jOdDDFXSeWl4(BlockHitResult)
        if (InventoryManager.mc.field_1724 == null || InventoryManager.mc.field_1687 == null
                || InventoryManager.mc.field_1761 == null) return;
        InventoryManager.mc.field_1761.method_41931(
            InventoryManager.mc.field_1687,
            n -> new net.minecraft.class_2885(class_1268.field_5808, hitResult, n)); // Hand.MAIN_HAND
    }

    /** Returns true if the given ItemStack tool has durability above the configured minimum. */
    public static boolean hasEnoughDurability(ItemStack stack) { // was: vgrtgn5(ItemStack)
        return stack.method_7936() - stack.method_7919() > // getMaxDamage - getDamage
               (Integer) MusheorSystem.Manager.minToolDurability.get();
    }

    /** Moves the first hotbar/inventory slot containing the given item to a free hotbar slot. */
    public static void moveItemToHotbar(ItemStack item) { // was: UgB10d(Item)
        int slot = 0;
        for (int i = 9; i < InventoryManager.mc.player.getId().field_7547.size(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (stack.getStack() != item) continue; // getItem
            slot = i;
            break;
        }
        if (slot != 0) {
            InvUtils.move().from(slot).to(InventoryManager.findEmptyHotbarSlot());
        }
    }

    /** Moves the given ItemStack to a free hotbar slot. */
    public static void moveStackToHotbar(ItemStack stack) { // was: VYEwzRq(ItemStack)
        InvUtils.move()
            .from(InventoryManager.mc.player.getId().method_7395(stack)) // indexOf
            .to(InventoryManager.findEmptyHotbarSlot());
    }

    /** Switches the active hotbar slot to the given index (clamped 0-8). */
    public static void switchHotbarSlot(int slot) { // was: KP44bk(int)
        if (InventoryManager.mc.field_1724 == null || mc.method_1562() == null) return;
        slot = Math.max(0, Math.min(8, slot));
        if (InventoryManager.mc.player.getId().field_7545 == slot) return; // selectedSlot
        InventoryManager.mc.player.getId().field_7545 = slot;
        mc.method_1562().method_52787((class_2596) new class_2868(slot)); // UpdateSelectedSlotC2SPacket
    }

    /** Returns the first empty hotbar slot index (0-8), or a random slot 1-7 if none found. */
    public static int findEmptyHotbarSlot() { // was: H4b9BDTz5I9d4B1z
        if (InventoryManager.mc.field_1724 == null) return 8;
        for (int i = 0; i <= 8; ++i) {
            if (InventoryManager.mc.player.getId().method_5438(i).setStack()) return i; // isEmpty
        }
        return Utils.random(1, 7);
    }

    /** Returns the first empty inventory slot index (9-35), or a random slot if none found. */
    public static int findEmptyInventorySlot() { // was: oIn3mVM8z
        if (InventoryManager.mc.field_1724 == null) return 35;
        for (int i = 9; i < 36; ++i) {
            if (InventoryManager.mc.player.getId().method_5438(i).setStack()) return i;
        }
        return Utils.random(9, 35);
    }

    /**
     * Executes the given action with the hotbar temporarily switched to {@code slot},
     * restoring the original slot after. Returns true on success.
     */
    public static boolean withHotbarSlot(int slot, Runnable action) { // was: jOdDDFXSeWl4(int,Runnable)
        if (InventoryManager.mc.field_1724 == null || action == null) return false;
        if (slot < 0 || slot > 35) return false;
        boolean swapped = InventoryManager.swapSlotToHotbar(slot);
        try {
            action.run();
            return true;
        } finally {
            if (swapped) InventoryManager.swapSlotToHotbar(slot);
        }
    }

    /**
     * Swaps the inventory slot {@code slot} into the currently-selected hotbar slot
     * via a SWAP click packet. Returns false if already selected.
     */
    public static boolean swapSlotToHotbar(int slot) { // was: jWrhVf2psx(int)
        int current = InventoryManager.mc.player.getId().field_7545;
        if (slot == current) return false;
        int screenSlot = InventoryManager.toScreenSlotId(slot);
        InventoryManager.mc.field_1761.method_2906(
            InventoryManager.mc.player.field_7512.field_7763, // syncId
            screenSlot, current,
            ClientPlayerEntity.field_7791, // SlotActionType.SWAP
            (class_1657) InventoryManager.mc.field_1724);
        MusheorSystem.debug("Swapped slot %d to selected in screen %s with ID: %d",
            slot,
            InventoryManager.mc.player.field_7512.toString(),
            InventoryManager.mc.player.field_7512.field_7763);
        return true;
    }

    /** Converts a player inventory slot index to the screen slot ID used in click packets. */
    public static int toScreenSlotId(int slot) { // was: usJLOV0subXO3(int)
        if (slot >= 0 && slot <= 8) return 36 + slot;
        return slot;
    }

    /** Returns the slot index of the first occurrence of the given item in the inventory, or -1. */
    public static int findItemSlot(ItemStack item) { // was: KP44bk(Item)
        for (int i = 0; i < Objects.requireNonNull(InventoryManager.mc.field_1724).getId().field_7547.size(); ++i) {
            if (InventoryManager.mc.player.getId().method_5438(i).getStack() == item) return i;
        }
        return -1;
    }

    /** Drops the item in the given slot. {@code dropAll=true} drops the whole stack. */
    public static void dropSlot(int slot, boolean dropAll) { // was: jOdDDFXSeWl4(int,boolean)
        if (InventoryManager.mc.field_1724 == null || mc.method_1562() == null) return;
        InventoryManager.mc.field_1761.method_2906(
            InventoryManager.mc.player.field_7512.field_7763,
            InventoryManager.toScreenSlotId(slot),
            dropAll ? 1 : 0,
            ClientPlayerEntity.field_7795, // SlotActionType.THROW
            (class_1657) InventoryManager.mc.field_1724);
    }

    /** Returns the first ItemStack in the inventory with the given item type, or null. */
    public static ItemStack getItemStack(ItemStack item) { // was: jWrhVf2psx(Item)
        for (int i = 0; i < Objects.requireNonNull(InventoryManager.mc.field_1724).getId().field_7547.size(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (stack.getStack() == item) return stack;
        }
        return null;
    }

    /** Counts all items of the given type stored inside the given shulker box stack. */
    public static int countItemInShulker(ItemStack shulkerStack, ItemStack item) { // was: jOdDDFXSeWl4(ItemStack,Item)
        ItemStack[] contents = new ItemStack[27];
        Utils.getItemsInContainerItem(shulkerStack, contents);
        int count = 0;
        for (ItemStack s : contents) {
            if (s.setStack() || s.getStack() != item) continue;
            count += s.method_7947(); // getCount
        }
        return count;
    }

    /** Counts all components of the given type stored inside the given shulker box stack. */
    public static int countComponentInShulker(ItemStack shulkerStack, class_9331<?> component) { // was: jOdDDFXSeWl4(ItemStack,DataComponentType)
        ItemStack[] contents = new ItemStack[27];
        Utils.getItemsInContainerItem(shulkerStack, contents);
        int count = 0;
        for (ItemStack s : contents) {
            if (s.setStack() || !s.method_57353().method_57832(component)) continue; // getComponents().contains
            count += s.method_7947();
        }
        return count;
    }

    /** Counts total items of the given type across all inventory slots. */
    public static int countItemInInventory(ItemStack item) { // was: usJLOV0subXO3(Item)
        int count = 0;
        for (int i = 0; i < InventoryManager.mc.player.getId().field_7547.size(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (stack.setStack() || stack.getStack() != item) continue;
            count += stack.method_7947();
        }
        return count;
    }

    /** Counts items including those stored inside any shulker boxes in the inventory. */
    public static int countItemIncludingShulkers(ItemStack item) { // was: ZbTtF5KYyGL9YXed
        int count = 0;
        for (int i = 0; i < InventoryManager.mc.player.getId().field_7547.size(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (stack.setStack()) continue;
            if (stack.getStack() instanceof AbstractClientPlayerEntity blockItem // BlockItem
                    && ((AbstractClientPlayerEntity) stack.getStack()).method_7711() instanceof class_2480) { // ShulkerBoxBlock
                count += InventoryManager.countItemInShulker(stack, item);
            }
            if (stack.getStack() != item) continue;
            count += stack.method_7947();
        }
        return count;
    }

    /** Returns a shulker box ItemStack that contains at least one of the given item, or null. */
    public static ItemStack findShulkerWithItem(ItemStack item) { // was: e5oi2ZF
        for (int i = 0; i < Objects.requireNonNull(InventoryManager.mc.field_1724).getId().field_7547.size(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (!(stack.getStack() instanceof AbstractClientPlayerEntity)
                    || !(((AbstractClientPlayerEntity) stack.getStack()).method_7711() instanceof class_2480)
                    || !InventoryManager.shulkerHasItem(stack, item)) continue;
            return stack;
        }
        return null;
    }

    /** Returns the shulker box with the fewest (but >0) items of the given type, or null. */
    public static ItemStack findLeastFullShulkerWithItem(ItemStack item) { // was: BX92A0OIIvD9
        int minCount = 1729;
        ItemStack best = null;
        for (int i = 0; i < Objects.requireNonNull(InventoryManager.mc.field_1724).getId().method_5439(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            int n;
            if (!(stack.getStack() instanceof AbstractClientPlayerEntity)
                    || !(((AbstractClientPlayerEntity) stack.getStack()).method_7711() instanceof class_2480)
                    || minCount <= (n = InventoryManager.countItemInShulker(stack, item))
                    || n <= 0) continue;
            minCount = n;
            best = stack;
        }
        return best;
    }

    /** Returns the shulker box with the fewest (but >0) items matching the given component, or null. */
    public static ItemStack findLeastFullShulkerWithComponent(class_9331<?> component) { // was: jOdDDFXSeWl4(DataComponentType)
        int minCount = 99;
        ItemStack best = null;
        for (int i = 0; i < Objects.requireNonNull(InventoryManager.mc.field_1724).getId().method_5439(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            int n;
            if (!(stack.getStack() instanceof AbstractClientPlayerEntity)
                    || !(((AbstractClientPlayerEntity) stack.getStack()).method_7711() instanceof class_2480)
                    || minCount <= (n = InventoryManager.countComponentInShulker(stack, component))
                    || n <= 0) continue;
            minCount = n;
            best = stack;
        }
        return best;
    }

    /** Returns a shulker box ItemStack that contains items matching the given component, or null. */
    public static ItemStack findShulkerWithComponent(class_9331<?> component) { // was: mp3zoXQFKUKYj5(DataComponentType)
        for (int i = 0; i < Objects.requireNonNull(InventoryManager.mc.field_1724).getId().field_7547.size(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (!(stack.getStack() instanceof AbstractClientPlayerEntity)
                    || !(((AbstractClientPlayerEntity) stack.getStack()).method_7711() instanceof class_2480)
                    || !InventoryManager.shulkerHasComponent(stack, component)) continue;
            return stack;
        }
        return null;
    }

    /**
     * Returns true if the shulker box contains at least one item with the given component.
     * For TOOL components, also checks that durability is above 50.
     */
    public static boolean shulkerHasComponent(ItemStack shulkerStack, class_9331<?> component) { // was: mp3zoXQFKUKYj5(ItemStack,DataComponentType)
        ItemStack[] contents = new ItemStack[27];
        Utils.getItemsInContainerItem(shulkerStack, contents);
        for (ItemStack s : contents) {
            if (s.setStack() || !s.method_57353().method_57832(component)) continue;
            if (component == MutableText.field_50077) { // TOOL
                if (s.method_7936() - s.method_7919() <= 50) continue;
                return true;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns true if the shulker box contains at least one item of the given type.
     * For pickaxes, also checks that durability is above 50.
     */
    public static boolean shulkerHasItem(ItemStack shulkerStack, ItemStack item) { // was: mp3zoXQFKUKYj5(ItemStack,Item)
        ItemStack[] contents = new ItemStack[27];
        Utils.getItemsInContainerItem(shulkerStack, contents);
        for (ItemStack s : contents) {
            if (s.setStack() || s.getStack() != item) continue;
            if (VersionHelper.get().isPickaxe(s)) {
                if (s.method_7936() - s.method_7919() <= 50) continue;
                return true;
            }
            return true;
        }
        return false;
    }

    /** Returns true if the shulker box ItemStack contains only empty slots. */
    public static boolean isShulkerEmpty(ItemStack shulkerStack) { // was: UgB10d(ItemStack)
        class_9323 components = shulkerStack.method_57353(); // getComponents
        if (components.method_57832(MutableText.field_49622)) { // contains(CONTAINER)
            ContainerComponentAccessor accessor =
                (ContainerComponentAccessor) components.method_58694(MutableText.field_49622); // get
            if (accessor != null) {
                for (ItemStack s : VersionHelper.get().getStacks(accessor)) {
                    if (!s.setStack()) return false;
                }
            }
            return true;
        }
        return false;
    }

    /** Equips the given item to the main hand (checks hotbar first, then moves from inventory). */
    public static void equipItem(ItemStack item) { // was: L5CF0C6jx0T17H4I
        assert (InventoryManager.mc.field_1724 != null);
        FindItemResult result = InvUtils.findInHotbar(stack -> stack.getStack() == item);
        if (result.found()) {
            InvUtils.swap(result.slot(), false);
        } else {
            InventoryManager.moveItemToHotbar(item);
        }
        MusheorSystem.debug("Swapping to %s",
            class_7923.field_41178.method_10221(item.method_8389()).toString()); // Registries.ITEM.getId
    }

    /** Equips the best pickaxe in the hotbar, optionally requiring Silk Touch. */
    public static void equipBestPickaxe(boolean requireSilkTouch) { // was: vgrtgn5(boolean)
        assert (InventoryManager.mc.field_1724 != null);
        double bestSpeed = -1.0;
        int bestSlot = -1;
        for (int i = 0; i < 9; ++i) {
            double speed;
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (Utils.hasEnchantments(stack, new class_5321[]{class_1893.field_9099}) && requireSilkTouch // SILK_TOUCH
                    || !((speed = (double) stack.method_7924(Blocks.field_10443.method_9564())) > bestSpeed)) continue; // mining speed on Netherrack
            bestSpeed = speed;
            bestSlot = i;
        }
        if (bestSlot != -1) {
            InvUtils.swap(bestSlot, false);
        }
    }

    /** Returns the best ItemStack tool in the inventory for breaking the given block state. */
    public static ItemStack getBestToolForBlock(class_2680 blockState) { // was: TAdu5cndwWu3A1(BlockState)
        if (InventoryManager.mc.field_1724 == null) return ItemStack.field_8037; // ItemStack.EMPTY
        ItemStack best = ItemStack.field_8037;
        double bestSpeed = 0.0;
        for (int i = 0; i < InventoryManager.mc.player.getId().method_5439(); ++i) {
            double speed;
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (((Boolean) MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()
                    && !InventoryManager.hasEnoughDurability(stack)
                    || !((speed = (double) stack.method_7924(blockState)) > bestSpeed)) continue;
            bestSpeed = speed;
            best = stack;
        }
        return best;
    }

    /** Equips the best tool for breaking the block at the given position. */
    public static void equipBestToolForBlock(BlockPos pos) { // was: J2pm2c07elEb5G(BlockPos)
        assert (InventoryManager.mc.field_1687 != null && InventoryManager.mc.field_1724 != null && pos != null);
        class_2680 blockState = InventoryManager.mc.world.getBlockState(pos); // getBlockState
        double bestSpeed = 0.0;
        int bestSlot = -1;
        // Search hotbar first
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            double speed;
            if (stack.setStack()
                    || ((Boolean) MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()
                        && !InventoryManager.hasEnoughDurability(stack)
                    || !((speed = (double) stack.method_7924(blockState)) > bestSpeed)
                    || !stack.method_7951(blockState)) continue; // isSuitableFor
            bestSpeed = speed;
            bestSlot = i;
        }
        if (bestSlot != -1) {
            InventoryManager.mc.player.getId().field_7545 = bestSlot;
            KekMine.YnQ4ChsDR.hasEnoughDurability(bestSlot); // notifies KekMine of the slot change
        } else if (VersionHelper.get().isTool(InventoryManager.mc.player.method_6047()) // getMainHandStack
                && !InventoryManager.hasEnoughDurability(InventoryManager.mc.player.method_6047())) {
            // Current tool is broken — move it away
            InvUtils.move()
                .from(InventoryManager.mc.player.getId().field_7545)
                .to(InventoryManager.findEmptyInventorySlot());
        } else {
            // Search full inventory
            for (int i = 9; i < InventoryManager.mc.player.getId().method_5439(); ++i) {
                ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
                double speed;
                if (stack.setStack()
                        || ((Boolean) MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()
                            && !InventoryManager.hasEnoughDurability(stack)
                        || !((speed = (double) stack.method_7924(blockState)) > bestSpeed)
                        || !stack.method_7951(blockState)) continue;
                bestSpeed = speed;
                bestSlot = i;
            }
            if (bestSlot != -1) {
                InventoryManager.moveStackToHotbar(
                    InventoryManager.mc.player.getId().method_5438(bestSlot));
                InventoryManager.mc.player.getId().field_7545 = bestSlot;
            }
        }
    }

    /**
     * Counts pickaxes in the inventory.
     * {@code requireSilkTouch=true} counts only Silk Touch pickaxes with sufficient durability.
     */
    public static int countPickaxes(boolean requireSilkTouch) { // was: VYEwzRq(boolean)
        assert (InventoryManager.mc.field_1724 != null);
        int count = 0;
        for (int i = 0; i < InventoryManager.mc.player.getId().method_5439(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (!VersionHelper.get().isPickaxe(stack)) continue;
            if (((Boolean) MusheorSystem.Manager.preventToolBreaking.get()).booleanValue()) {
                if (!requireSilkTouch
                        && !Utils.hasEnchantments(stack, new class_5321[]{class_1893.field_9099})
                        && stack.method_7936() - stack.method_7919() > (Integer) MusheorSystem.Manager.minToolDurability.get()) ++count;
                if (requireSilkTouch && stack.method_7936() - stack.method_7919() > (Integer) MusheorSystem.Manager.minToolDurability.get()) ++count;
                continue;
            }
            if (!requireSilkTouch && !Utils.hasEnchantments(stack, new class_5321[]{class_1893.field_9099})) { ++count; continue; }
            if (requireSilkTouch) ++count;
        }
        return count;
    }

    /** Counts pickaxes at or below the minimum durability threshold. */
    public static int countBrokenPickaxes() { // was: abVxPfXsrl5
        assert (InventoryManager.mc.field_1724 != null);
        int count = 0;
        for (int i = 0; i < InventoryManager.mc.player.getId().field_7547.size(); ++i) {
            ItemStack stack = InventoryManager.mc.player.getId().method_5438(i);
            if (!VersionHelper.get().isPickaxe(stack)
                    || stack.method_7936() - stack.method_7919() > (Integer) MusheorSystem.Manager.minToolDurability.get()) continue;
            ++count;
        }
        return count;
    }

    /**
     * Returns the slot index (in the open container) of the first broken pickaxe (durability ≤ min),
     * or -1 if none found. Only considers slots above index 26.
     */
    public static int findBrokenPickaxeInContainer() { // was: Lal2Zyi076
        assert (InventoryManager.mc.field_1724 != null);
        for (int i = 0; i < InventoryManager.mc.player.field_7512.field_7761.size(); ++i) { // currentScreenHandler.slots
            ItemStack stack = InventoryManager.mc.player.field_7512.method_7611(i).method_7677(); // getSlot(i).getStack()
            if (!VersionHelper.get().isPickaxe(stack)
                    || stack.method_7936() - stack.method_7919() > (Integer) MusheorSystem.Manager.minToolDurability.get()
                    || i <= 26) continue;
            return i;
        }
        return -1;
    }

    /** Returns the number of empty slots in the player inventory. */
    public static int countEmptySlots() { // was: ZeOLrA
        int count = 0;
        assert (InventoryManager.mc.field_1724 != null);
        for (ItemStack stack : InventoryManager.mc.player.getId().field_7547) {
            if (!stack.setStack()) continue;
            ++count;
        }
        return count;
    }

    /** Returns the number of empty slots in the given open container screen handler. */
    public static int countEmptyContainerSlots(Text handler) { // was: Gt56Sj4a6BWhgB(ScreenHandler)
        int count = 0;
        int maxSlot = SlotUtils.indexToId(9);
        for (int i = 0; i < maxSlot; ++i) {
            if (!handler.method_7611(i).method_7677().setStack()) continue;
            ++count;
        }
        return count;
    }

    /** Moves a shulker box containing the given item to the specified inventory slot. */
    public static boolean moveShulkerToSlot(ItemStack item, int targetSlot) { // was: jOdDDFXSeWl4(Item,int)
        ItemStack shulker = InventoryManager.findLeastFullShulkerWithItem(item);
        if (shulker != null) {
            int slot = InventoryManager.mc.player.getId().method_7395(shulker);
            if (slot != -1) {
                InvUtils.move().from(slot).to(targetSlot);
                MusheorSystem.debug("Moving shulker with " + item + " to slot " + targetSlot, new Object[0]);
            }
            if (slot == targetSlot) return true;
        } else {
            MusheorSystem.debug("No shulker with " + item + " found in the inventory", new Object[0]);
            return false;
        }
        return true;
    }

    /** Moves a shulker box matching the given component to the specified inventory slot. */
    public static boolean moveShulkerComponentToSlot(class_9331<?> component, int targetSlot) { // was: jOdDDFXSeWl4(DataComponentType,int)
        ItemStack shulker = InventoryManager.findLeastFullShulkerWithComponent(component);
        if (shulker != null) {
            int slot = InventoryManager.mc.player.getId().method_7395(shulker);
            if (slot != -1) {
                InvUtils.move().from(slot).to(targetSlot);
                MusheorSystem.debug("Moving shulker with " + component + " to slot " + targetSlot, new Object[0]);
            }
            if (slot == targetSlot) return true;
        } else {
            MusheorSystem.debug("No shulker with " + component + " found in the inventory", new Object[0]);
            return false;
        }
        return true;
    }

    /** Returns the item type of the first shulker box ItemEntity within the given radius, or null. */
    public static ItemStack getNearbyShulkerItem(double radius) { // was: jWrhVf2psx(double)
        if (InventoryManager.mc.field_1687 == null || InventoryManager.mc.field_1724 == null) return null;
        class_238 box = InventoryManager.mc.player.method_5829().method_1014(radius); // getBoundingBox().expand
        for (class_1542 itemEntity : InventoryManager.mc.world.method_8390(class_1542.class, box, e -> true)) {
            ItemStack stack = itemEntity.method_6983(); // getStack
            ItemStack item  = stack.getStack();
            if (!(item instanceof AbstractClientPlayerEntity)
                    || !(((AbstractClientPlayerEntity) item).method_7711() instanceof class_2480)) continue; // ShulkerBoxBlock
            return itemEntity.method_6983().getStack();
        }
        return null;
    }

    /**
     * Main restock-from-shulker state machine. Called each tick while restocking.
     * Navigates the player to restockPosition, places a shulker, opens it, and
     * shift-clicks the target items ({@code amount} total) into the inventory.
     */
    public static void doRestockFromShulker(class_9331<?> targetComponent, int amount) { // was: mp3zoXQFKUKYj5(DataComponentType,int)
        if (InventoryManager.mc.field_1687 == null || InventoryManager.mc.field_1724 == null) return;
        HighwayState state = HighwayState.getInstance();

        if (PlayerUtils.isGatheringItem() || state.isTeleporting()) { // was: Y775oeIufYz9
            isRestocking = false;
            return;
        }

        PlayerUtils.setAutoWalkActive(false);
        Module kekNuker = Modules.get().get(KekNuker.class);
        if (kekNuker.isActive()) kekNuker.toggle();

        // If lag detected in normal mode, wait
        if (WorldUtils.isLagDetected() && HighwayBuilder.getBuildMode() == HighwayBuilder.BuildMode.NORMAL) {
            PlayerUtils.setAutoWalkActive(false);
            if (!BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
                WorldUtils.returnToHighway();
            }
            return;
        }

        // Step 1: Sync inventory & save screen
        if (!inventorySynced) {
            InventoryManager.switchHotbarSlot(8);
            VersionHelper.get().syncInventory();
            savedScreen = InventoryManager.mc.field_1755; // currentScreen
            inventorySynced = true;
            MusheorSystem.debug("Screen was saved and inventory has been synced", new Object[0]);
        }

        // Step 2: Navigate to restock position
        if (InventoryManager.mc.player.getBlockPos() != restockPosition) {
            BaritoneAPI.getProvider().getPrimaryBaritone()
                .getCustomGoalProcess().setGoalAndPath((Goal) new GoalBlock(restockPosition));
            PlayerUtils.resumeBaritone();
            MusheorSystem.debug("Player is not at restocking position, calling baritone...", new Object[0]);
            return;
        }

        // Step 3: Wait for swap delay
        if (delayBeforeSwapping > 0) { --delayBeforeSwapping; return; }

        // Step 4: Move the target shulker to hotbar slot 8
        if (!shulkerMoved) {
            ItemStack shulkerItem = null;
            if ((targetComponent == MutableText.field_50077 || targetComponent == MutableText.field_50075)
                    && InventoryManager.moveShulkerComponentToSlot(targetComponent, 8)) {
                shulkerMoved = true;
            }
            if (targetComponent == MutableText.field_49622) { // CONTAINER
                if (InventoryManager.findShulkerWithItem(HighwayBuilder.getPavingBlock().method_8389()) != null)
                    shulkerItem = Items.field_8281; // cobblestone or paving block
                else if (InventoryManager.findShulkerWithItem(Items.field_8466) != null)
                    shulkerItem = Items.field_8466; // obsidian
                if (InventoryManager.moveShulkerToSlot(shulkerItem, 8)) {
                    shulkerMoved = true;
                }
            }
            return;
        }

        // Step 5: Place the shulker box
        if (!shulkerPlaced) {
            shulkerPlacePos = WorldUtils.getOffset2AheadPos(HighwayBuilder.getDirection()); // was: jOdDDFXSeWl4(Direction8)
            if (delayBeforePlacingContainer > 0) {
                PlayerUtils.setSneaking(false);
                --delayBeforePlacingContainer;
                WorldUtils.lookAtBlockFace(shulkerPlacePos, Direction.field_11033); // DOWN
                return;
            }
            if (InventoryManager.mc.world.getBlockState(shulkerPlacePos).getBlock() instanceof class_2480) {
                MusheorSystem.debug("Shulker box placed successfully!", new Object[0]);
                shulkerPlaced = true;
            } else if (BlockUtils.canPlace(shulkerPlacePos, false) && !BlockUtils.canPlace(shulkerPlacePos, true)) {
                MusheorSystem.debug("Unable to place shulkerbox...", new Object[0]);
                if (InventoryManager.mc.world.getBlockState(shulkerPlacePos).getBlock() != Blocks.field_10124) {
                    BlockUtils.breakBlock(shulkerPlacePos, true);
                    MusheorSystem.debug("Breaking obstructing block at restocking position", new Object[0]);
                }
            } else {
                MusheorSystem.debug("Placing shulkerbox at x: %s y: %s z: %s",
                    shulkerPlacePos.getX(), shulkerPlacePos.getY(), shulkerPlacePos.getZ());
                BlockUtils.place(shulkerPlacePos, class_1268.field_5808, 8, false, 0, true, false, false);
                return;
            }
        }

        // Step 6: Wait for container to register as opened
        if (!state.isContainerOpen()) { // was: yjhDfCpm
            InventoryManager.switchHotbarSlot(0);
            state.setContainerOpen(true);
            return;
        }

        // Step 7: Wait for open delay
        if (delayBeforeOpeningContainer > 0) { --delayBeforeOpeningContainer; return; }

        // Step 8: Open the shulker screen if not already open
        if (!(InventoryManager.mc.player.field_7512 instanceof class_1733)) { // GenericContainerScreenHandler
            WorldUtils.lookAtBlock(shulkerPlacePos);
            InventoryManager.mc.player.field_3944.method_52787((class_2596) new class_2885(
                class_1268.field_5808,
                new Screen(class_243.method_24953((class_2382) shulkerPlacePos),
                    Direction.field_11036, shulkerPlacePos, false), 0)); // Direction.UP
            MusheorSystem.debug("Opening shulkerbox at x: %s y: %s z: %s",
                shulkerPlacePos.getX(), shulkerPlacePos.getY(), shulkerPlacePos.getZ());
            delayBeforeOpeningContainer = 5;
            return;
        }

        // Step 9: Wait for post-open delay
        if (delayAfterOpeningContainer > 0) { --delayAfterOpeningContainer; return; }

        // Step 10: Steal items from the shulker
        if (itemsStolenCount >= amount || shulkerSlotIndex >= 27) {
            MusheorSystem.debug("Completed item restocking process", new Object[0]);
            InventoryManager.resetState(true);
        } else {
            Text handler = InventoryManager.mc.player.field_7512;
            if (waitingForDelay) {
                if (stealingDelayCounter < stealingDelay) { ++stealingDelayCounter; return; }
                waitingForDelay = false;
            }
            ItemStack slotStack = handler.method_7611(shulkerSlotIndex).method_7677();
            int brokenSlot = InventoryManager.findBrokenPickaxeInContainer();

            if (targetComponent == MutableText.field_50077) { // TOOL — steal pickaxes
                if (VersionHelper.get().isPickaxe(slotStack)) {
                    if (InventoryManager.hasEnoughDurability(slotStack)) {
                        InvUtils.shiftClick().slotId(shulkerSlotIndex);
                        if (HighwayBuilder.isSpectatorMode() && brokenSlot != -1) { // was: OUSKA4lEld
                            InvUtils.shiftClick().slotId(brokenSlot);
                        }
                        MusheorSystem.debug("Stealing {%s} from slot [%s]",
                            slotStack.getStack().method_63680(), shulkerSlotIndex);
                        waitingForDelay = true; ++shulkerSlotIndex; ++itemsStolenCount; stealingDelayCounter = 0;
                    } else {
                        ++shulkerSlotIndex;
                        MusheorSystem.debug("Checking next slot...", new Object[0]);
                    }
                }
            } else if (targetComponent == MutableText.field_50075) { // FOOD — steal food items
                if (slotStack.method_57353().method_57832(MutableText.field_50075)) {
                    if (!((List<?>) Modules.get().get(AutoEat.class).blacklist.get()).contains(slotStack.getStack())) {
                        InvUtils.shiftClick().slotId(shulkerSlotIndex);
                        MusheorSystem.debug("Stealing {%s} from slot [%s]",
                            slotStack.getStack().method_63680(), shulkerSlotIndex);
                        waitingForDelay = true; ++shulkerSlotIndex; ++itemsStolenCount; stealingDelayCounter = 0;
                    } else {
                        ++shulkerSlotIndex;
                        MusheorSystem.debug("Checking next slot...", new Object[0]);
                    }
                }
            } else if (slotStack.getStack() == HighwayBuilder.getPavingBlock().method_8389()
                    || slotStack.getStack() == Items.field_8466) { // CONTAINER — steal paving blocks
                InvUtils.shiftClick().slotId(shulkerSlotIndex);
                MusheorSystem.debug("Stealing {%s} from slot [%s]",
                    slotStack.getStack().method_63680(), shulkerSlotIndex);
                waitingForDelay = true; ++shulkerSlotIndex; ++itemsStolenCount; stealingDelayCounter = 0;
            } else {
                ++shulkerSlotIndex;
                MusheorSystem.debug("Checking next slot...", new Object[0]);
            }
        }
    }

    /**
     * Post-restock cleanup: closes the container screen, re-enables KekNuker,
     * breaks the placed shulker box, picks up dropped shulker items, then resets.
     * Called each tick while {@code isDoingPostRestock} is true.
     */
    public static void handlePostRestock() { // was: H02kTTf
        if (InventoryManager.mc.field_1724 == null || InventoryManager.mc.field_1687 == null
                || shulkerPlacePos == null) return;
        if (isBreakingShulker || HighwayBuilder.isWaitingForPath() || isRestocking) return; // was: IuR8CfqY

        HighwayState state = HighwayState.getInstance();

        // Close any lingering handled screen
        if (InventoryManager.mc.field_1755 instanceof class_465 // HandledScreen
                && InventoryManager.mc.player.field_7512 != InventoryManager.mc.player.field_7498) { // playerScreenHandler
            InventoryManager.mc.player.method_7346(); // closeHandledScreen
            InventoryManager.mc.player.field_3944.method_52787(
                (class_2596) new class_2815(InventoryManager.mc.player.field_7498.field_7763)); // CloseHandledScreenC2SPacket
            return;
        }

        // Re-enable KekNuker
        Module kekNuker = Modules.get().get(KekNuker.class);
        if (!kekNuker.isActive()) kekNuker.toggle();

        if (isDoingPostRestock) {
            MusheorSystem.debug("Post-restocking task running...", new Object[0]);
            PlayerUtils.setAutoWalkActive(false);
            InventoryManager.equipBestToolForBlock(shulkerPlacePos);

            // Break the placed shulker
            if (InventoryManager.mc.world.getBlockState(shulkerPlacePos).getBlock() instanceof class_2480 // ShulkerBoxBlock
                    || InventoryManager.mc.world.getBlockState(shulkerPlacePos).getBlock() instanceof class_2336) { // dropped shulker
                isBreakingShulker2 = true;
                if (meteordevelopment.meteorclient.utils.player.PlayerUtils.isWithinReach(shulkerPlacePos)) {
                    PlayerUtils.stopBaritone();
                    if (BlockUtils.breakBlock(shulkerPlacePos, true)) return;
                } else if (!BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
                    BaritoneAPI.getProvider().getPrimaryBaritone()
                        .getCustomGoalProcess().setGoalAndPath((Goal) new GoalBlock(shulkerPlacePos));
                }
            }

            // Wait for shulker block to be gone
            if (!InventoryManager.mc.world.getBlockState(shulkerPlacePos).isAir()) return; // !isAir

            isBreakingShulker2 = false;

            // Wait for post-restock delay
            if (delayAfterRestocking > 0) { --delayAfterRestocking; return; }

            // Pick up nearby dropped shulker item
            ItemStack nearbyShulker = InventoryManager.getNearbyShulkerItem(16.0);
            if (nearbyShulker != null) {
                PlayerUtils.startGatherItem(nearbyShulker, false);
            }

            // Done: clear state if nothing else is pending
            if (!(!InventoryManager.mc.world.getBlockState(shulkerPlacePos).getBlock()
                        .equals(Blocks.field_10124) // Blocks.AIR
                    || PlayerUtils.isGatheringItem()
                    || state.isPathing()              // was: OIExXGL6BNv
                    || state.isAligning()             // was: jIXFBaSwUWYqAc9
                    || isBreakingShulker2
                    || state.isTeleporting())) {
                InventoryManager.resetState(false);
            }
        }
    }

    static {
        shulkerSlotIndex    = 0;
        stealingDelay       = 0;
        itemsStolenCount    = 0;
        stealingDelayCounter = 0;
    }
}
