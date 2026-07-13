// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWalk;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.utils.BlockPositions;
import musheor.utils.DiscordRPC;
import musheor.utils.Handlers;
import musheor.utils.PlayerUtils;
import musheor.utils.RenderUtils;
import musheor.utils.StatsCollector;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayLocator;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.MusheorSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

/**
 * Automated highway builder for 2b2t. Orchestrates the whole paving/digging loop:
 * material checks, helper-module toggling (nuker/auto-eat/kill-aura/restock/echest
 * farmer), highway detection (Auto mode), and per-tick dispatch to the pave/dig
 * handlers in {@link Handlers}. This class is the module shell + settings + state
 * machine; the heavy per-tick block logic lives in {@link Handlers}.
 */
public class HighwayBuilder extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance(); // was: rKbT3Ifwo (field)

    private final SettingGroup sgGeneral    = this.settings.getDefaultGroup();     // was: r7hOYIKN2
    private final SettingGroup sgPlacement  = this.settings.createGroup("Placement"); // was: oZHMlTL
    private final SettingGroup sgRestocking = this.settings.createGroup("Restocking"); // was: xQr5FhbwpQPWgIQ
    private final SettingGroup sgAutoEat    = this.settings.createGroup("Auto Eat"); // was: OMMZL1F3q
    private final SettingGroup sgNuker      = this.settings.createGroup("Nuker");   // was: zu3a44xDeMFMCRwm
    private final SettingGroup sgKillAura   = this.settings.createGroup("Kill Aura"); // was: krxNb5lcQuWA
    private final SettingGroup sgSafety     = this.settings.createGroup("Safety");  // was: nt0HZnvBBp
    private final SettingGroup sgInventory  = this.settings.createGroup("Inventory"); // was: amz3UB1vE
    private final SettingGroup sgMisc       = this.settings.createGroup("Miscellaneous"); // was: sBBIyQG5NWq0K

    // --- General ---
    private final Setting<BuildMode> buildMode = sgGeneral.add(new EnumSetting.Builder<BuildMode>() // was: sZkZ1izAy
        .name("build-mode").description("Pave mode places blocks - Dig mode digs tunnels.")
        .defaultValue(BuildMode.PAVE).build());
    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>() // was: FvaNWO
        .name("mode").description("Which mode to run the highwaybuilder in; Auto = fully automatic alignment, width, etc... - Semi = Chosen highway type and other parameters - Manual = full manual control")
        .defaultValue(Mode.AUTO).build());
    private final Setting<HighwayType> highwayType = sgGeneral.add(new EnumSetting.Builder<HighwayType>() // was: QYKUhjp
        .name("highway-type").description("Cardinal or diagonal highway type.")
        .defaultValue(HighwayType.CARDINAL).visible(() -> mode.get() != Mode.AUTO).build());
    private final Setting<Integer> pavementWidth = sgGeneral.add(new IntSetting.Builder() // was: NIz4xic3Js9
        .name("pavement-width").description("Width of the pavement below the player's feet.")
        .defaultValue(4).sliderRange(3, 9).visible(() -> mode.get() != Mode.AUTO).build());
    private final Setting<Boolean> autoBounce = sgGeneral.add(new BoolSetting.Builder() // was: u1WFwbQRSKa
        .name("auto-bounce").description("Automatically start bouncing when no blockages or missing blocks can be found ahead")
        .defaultValue(true).visible(() -> mode.get() != Mode.MANUAL).build());
    private final Setting<Integer> bounceDistanceCheck = sgGeneral.add(new IntSetting.Builder() // was: LGDfbZq
        .name("bounce-distance-check").description("How many blocks to scan ahead and check before allowing the paver to start bouncing")
        .defaultValue(24).sliderRange(1, 64).visible(() -> mode.get() != Mode.MANUAL && autoBounce.get()).build());

    // --- Placement ---
    private final Setting<Block> pavementBlock = sgPlacement.add(new BlockSetting.Builder() // was: to3T8DJCDVX8po
        .name("pavement-block").description("Block that is used to build highway pavement with.")
        .defaultValue(Blocks.OBSIDIAN).visible(() -> buildMode.get() == BuildMode.PAVE).build());
    private final Setting<ScaffoldMode> scaffoldMode = sgPlacement.add(new EnumSetting.Builder<ScaffoldMode>() // was: Sd3jEwKuGABy
        .name("scaffold-mode").description("What type of scaffolding to use when going over caves and open area's.")
        .defaultValue(ScaffoldMode.NORMAL).visible(() -> buildMode.get() == BuildMode.DIG).build());
    private final Setting<Block> scaffoldBlock = sgPlacement.add(new BlockSetting.Builder() // was: kJfFkD47Vh
        .name("scaffold-block").description("Block that is used to fix the flooring with.")
        .defaultValue(Blocks.CRYING_OBSIDIAN).visible(() -> buildMode.get() == BuildMode.DIG && scaffoldMode.get() != ScaffoldMode.NONE).build());
    private final Setting<Boolean> scaffoldLeftRail = sgPlacement.add(new BoolSetting.Builder() // was: ubHptFBRn5bO
        .name("scaffold-left-rail").description("Places the railing on the left side of the player.")
        .defaultValue(true).visible(() -> buildMode.get() == BuildMode.DIG).build());
    private final Setting<Boolean> scaffoldRightRail = sgPlacement.add(new BoolSetting.Builder() // was: apOpfoOHr3fJVwT
        .name("scaffold-right-rail").description("Places the railing on the right side of the player.")
        .defaultValue(true).visible(() -> buildMode.get() == BuildMode.DIG).build());
    private final Setting<Boolean> placeRails = sgPlacement.add(new BoolSetting.Builder() // was: hq1pN0qY
        .name("place-rails").description("Places railings on both sides of the highway 1 block above the pavement.")
        .defaultValue(true).visible(() -> buildMode.get() == BuildMode.PAVE && mode.get() != Mode.AUTO).build());
    private final Setting<Boolean> placeLeftRail = sgPlacement.add(new BoolSetting.Builder() // was: ptxWcpd1WV763T5
        .name("place-left-rail").description("Places railings on the left side sides of the highway 1 block above the pavement.")
        .defaultValue(true).visible(() -> buildMode.get() == BuildMode.PAVE && placeRails.get() && mode.get() != Mode.AUTO).build());
    private final Setting<Boolean> placeRightRail = sgPlacement.add(new BoolSetting.Builder() // was: DnAk86nuI
        .name("place-right-rail").description("Places railings on the right sides of the highway 1 block above the pavement.")
        .defaultValue(true).visible(() -> buildMode.get() == BuildMode.PAVE && placeRails.get() && mode.get() != Mode.AUTO).build());
    private final Setting<Boolean> replaceCryingObsidian = sgPlacement.add(new BoolSetting.Builder() // was: LlN8EpIZKbk
        .name("replace-crying-obsidian").description("Mines and replaces crying obsidian if part of the pavement positions.")
        .defaultValue(false).visible(() -> buildMode.get() == BuildMode.PAVE).build());
    private final Setting<Boolean> fillCeiling = sgPlacement.add(new BoolSetting.Builder() // was: pgjj9cLYUTE5g
        .name("fill-ceiling").description("Fills the ceiling at Y level 123 with blocks.")
        .defaultValue(false).visible(() -> buildMode.get() == BuildMode.PAVE).build());

    // --- Restocking ---
    private final Setting<Boolean> echestFarming = sgRestocking.add(new BoolSetting.Builder() // was: IeStEJRJ9eb3l
        .name("echest-farming").description("Allow the player to farm obsidian by mining enderchests.")
        .defaultValue(true).visible(() -> buildMode.get() == BuildMode.PAVE && pavementBlock.get() == Blocks.OBSIDIAN).build());
    private final Setting<Boolean> enableItemRestocking = sgRestocking.add(new BoolSetting.Builder() // was: sFazojak6ig8QgGq
        .name("enable-item-restocking").description("Allow the restocking process to grab items from shulkers.")
        .defaultValue(false).build());
    private final Setting<Boolean> swapBrokenPickaxes = sgRestocking.add(new BoolSetting.Builder() // was: ewq603nIlCd9Gbu
        .name("swap-broken-pickaxes").description("Swaps broken pickaxes with new ones when restocking these, allowing you to repair them later.")
        .defaultValue(true).visible(enableItemRestocking::get).build());
    private final Setting<Boolean> shulkerRestocking = sgRestocking.add(new BoolSetting.Builder() // was: ExGM8SQ9Qni
        .name("shulker-restocking").description("Allow the restocking process to grab shulkers from the player's enderchest.")
        .defaultValue(false).visible(() -> false).build());
    private final Setting<Boolean> echestShulkerRestocking = sgRestocking.add(new BoolSetting.Builder() // was: yS4isXf3gAzs
        .name("echest-shulker-restocking").description("Allow the restocking process to grab echest shulkers from the player's enderchest.")
        .defaultValue(false).visible(shulkerRestocking::get).build());
    private final Setting<Boolean> toolShulkerRestocking = sgRestocking.add(new BoolSetting.Builder() // was: eC9HV2bWGX
        .name("tool-shulker-restocking").description("Allow the restocking process to grab tool shulkers from the player's enderchest.")
        .defaultValue(false).visible(shulkerRestocking::get).build());

    // --- Auto Eat ---
    private final Setting<Boolean> toggleAutoEat = sgAutoEat.add(new BoolSetting.Builder() // was: w9spWeVv3AvI
        .name("toggle-auto-eat").description("Pauses the current task and automatically eats.").defaultValue(true).build());
    private final Setting<Boolean> toggleAutoGap = sgAutoEat.add(new BoolSetting.Builder() // was: HvulV2j9tKjohNgh
        .name("toggle-auto-gap").description("Allow the player to eat golden apples when required").defaultValue(true).build());

    // --- Nuker ---
    public final Setting<Boolean> toggleKekNuker = sgNuker.add(new BoolSetting.Builder() // was: Q90GLXQ0Pef
        .name("toggle-kek-nuker").description("Allows the paver to use the nuker module.").defaultValue(true).build());
    private final Setting<Boolean> mineAboveRails = sgNuker.add(new BoolSetting.Builder() // was: Qco5OF
        .name("mine-above-rails").description("Cleans and removes blocks above rails").defaultValue(false).build());

    // --- Kill Aura ---
    private final Setting<Boolean> toggleKillAura = sgKillAura.add(new BoolSetting.Builder() // was: cgqo7J5iR6
        .name("toggle-kill-aura").description("Automatically attack nearby hostile entities.").defaultValue(false).build());

    // --- Safety ---
    private final Setting<Boolean> disconnectIfNoMaterials = sgSafety.add(new BoolSetting.Builder() // was: u2kcN4vsQhS46w5s
        .name("disconnect-if-no-materials").description("Automatically disconnect when materials are low (less than 8 Obsidian/E-Chests).").defaultValue(false).build());
    private final Setting<Boolean> toggleSourceFiller = sgSafety.add(new BoolSetting.Builder() // was: Eos3LxdhEJt
        .name("toggle-source-filler").description("Automatically remove lava sources when you can reach them.").defaultValue(true).build());
    private final Setting<Boolean> advancedSourceFiller = sgSafety.add(new BoolSetting.Builder() // was: eB4Or3cBC2
        .name("advanced-source-filler").description("Uses baritone to path to lava sources ahead of the player. Not recommended.").defaultValue(false).build());

    // --- Inventory ---
    private final Setting<Boolean> toggleAutoReplenish = sgInventory.add(new BoolSetting.Builder() // was: ITVesx8a
        .name("toggle-auto-replenish").description("Automatically move items from inventory to hotbar when slots are empty.").defaultValue(true).build());
    private final Setting<Boolean> toggleInventoryCleaner = sgInventory.add(new BoolSetting.Builder() // was: ymaK1v
        .name("toggle-inventory-cleaner").description("Allow the paver to dispose of unwanted items.").defaultValue(false).build());

    // --- Misc ---
    private final Setting<Boolean> discordRpc = sgMisc.add(new BoolSetting.Builder() // was: WRxnOUhRut1YD0z
        .name("discord-rpc").description("Send paver stats to discord activity.").defaultValue(true).build());
    private final Setting<Boolean> enableFreeLook = sgMisc.add(new BoolSetting.Builder() // was: jusZpYdy95sR
        .name("enable-freelook").description("Freelook when using the paver.").defaultValue(false).build());

    /** Singleton reference (set in ctor). */
    public static HighwayBuilder INSTANCE; // was: psJq59YIbp3Z (static)
    private boolean starting = false;      // was: yNlQL5pBA2em

    public HighwayBuilder() {
        super(musheor.AUTOMATION, "HighwayBuilder", "Automated highway builder for 2b2t.");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        this.starting = true;
        HighwayState.getInstance().setTicksActive(0);
    }

    /** One-shot startup: validates materials, sets up state, enables helper modules. */
    private void startBuild() { // was: IeStEJRJ9eb3l()
        HighwayState state = HighwayState.getInstance();
        state.reset();
        boolean noPavementBlockPresent = pavementBlock.get() == Blocks.OBSIDIAN
            && InventoryManager.countItemInInventory(getFillBlock().asItem()) < 1;
        boolean noPickaxePresent = InventoryManager.countPickaxes(false) < 1;
        if (buildMode.get() != BuildMode.PAVE || (!noPavementBlockPresent && !noPickaxePresent)) {
            boolean noNonSilkPickaxePresent = InventoryManager.countPickaxes(true) < 1;
            if (buildMode.get() == BuildMode.DIG) {
                if (noNonSilkPickaxePresent) this.error("No pickaxe found in inventory...!", new Object[0]);
                PlayerUtils.toggleHighwayBuilder();
            } else {
                this.starting = false;
                state.setDirection(WorldUtils.getMovementDirection());
                state.setTicksActive(0);
                InventoryManager.resetState(false);
                if (!this.initHighwayState()) {
                    PlayerUtils.toggleHighwayBuilder();
                } else {
                    PlayerUtils.setModuleSetting(KekNuker.class, "nuker-mode", KekNuker.NukerMode.SMART);
                    PlayerUtils.setModuleSetting(KekNuker.class, "range", 5.5);
                    this.enableHelperModules();
                    StatsCollector.update();
                    state.getBlocksToBuild().clear();
                    state.getBlocksToBuild().add(mc.player.getBlockPos());
                    state.getBlockBreakAttempts().clear();
                    PlayerUtils.setModuleSetting(AutoWalk.class, "mode", AutoWalk.Mode.Simple);
                    PlayerUtils.setModuleSetting(AutoWalk.class, "simple-direction", AutoWalk.Direction.Forwards);
                }
            }
        } else {
            if (noPavementBlockPresent) this.error("No pavement material found in inventory...", new Object[0]);
            if (noPickaxePresent) this.error("No non-silk touch pickaxe found in inventory...!", new Object[0]);
            PlayerUtils.toggleHighwayBuilder();
        }
    }

    @Override
    public void onDeactivate() {
        this.starting = false;
        Handlers.reset();
        PlayerUtils.setAutoWalk(false);
        PathingHelper.cancelEverything();
        disableHelperModules();
        DiscordRPC.stop();
        HighwayState.getInstance().reset();
        this.info("Deactivated.", new Object[0]);
    }

    @EventHandler
    public void onTickPre(TickEvent.Pre event) { // was: FvaNWO(Pre)
        HighwayState state = HighwayState.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            state.setTicksActive(0);
        } else if (mc.player.isSpectator()) {
            this.info("Player is likely in queue, waiting to proceed with highwaybuilder state checks...", new Object[0]);
            state.setTicksActive(0);
        } else if (this.starting) {
            this.startBuild();
        } else {
            state.incrementTicksActive();
            WorldUtils.pruneTimedOutPlacements(state.getTicksActive(), getFillBlock());
            if (state.getPendingBreakPos() != null) PlayerUtils.setAutoWalk(false);

            state.incrementTicksSinceLastAction();
            if (state.getTicksSinceLastAction() >= 200) {
                state.setTicksSinceLastAction(0);
                StatsCollector.collectStats();
                if (discordRpc.get()) DiscordRPC.update();
                else DiscordRPC.stop();
            }

            Module echestFarmer = Modules.get().get(EchestFarmer.class);
            boolean isGapEating = ((AutoGap) Modules.get().get(AutoGap.class)).isEating();
            if (!isEating() && !isGapEating && !isEchestFarmerActive() && !isKillAuraAttacking()
                && !PlayerUtils.isGatheringItem() && !state.isFlag11()) {
                int enderChestCount = pavementBlock.get() == Blocks.OBSIDIAN ? InventoryManager.countItemInInventory(Items.ENDER_CHEST) : 64;
                int obsidianCount = InventoryManager.countItemInInventory(((Block) pavementBlock.get()).asItem());
                // NOTE: breakProgress/swapDelayTicks int slots are reused here as scratch pickaxe counters.
                state.setBreakProgress(InventoryManager.countPickaxes(false)); // non-silk pickaxe count
                state.setSwapDelayTicks(InventoryManager.countPickaxes(true)); // any pickaxe count
                if (buildMode.get() == BuildMode.PAVE) {
                    state.setFlag2(enderChestCount < 8 || obsidianCount < 8 || state.getBreakProgress() < 1 || !hasFood());
                }
                if (buildMode.get() == BuildMode.DIG) {
                    state.setFlag2(state.getSwapDelayTicks() < 1 || !hasFood());
                }

                if (state.isFlag10()) {
                    InventoryManager.handlePostRestock();
                } else if (state.isFlag8()) {
                    InventoryManager.runRestockProcess(state.getBaritoneGoalType(), InventoryManager.restockAmount);
                } else if (state.isFlag9()) {
                    InventoryManager.runRestockProcess(state.getBaritoneGoalType(), InventoryManager.restockAmount);
                    MusheorSystem.debug("Restocking %s; Amount: %s", state.getBaritoneGoalType(), InventoryManager.restockAmount);
                } else if (state.isFlag11() && !echestFarmer.isActive() && echestFarming.get()) {
                    Handlers.runPostEchestFarmer();
                    MusheorSystem.debug("Running post-echest farmer...");
                } else if (this.shouldRunEchestFarmer(obsidianCount, enderChestCount) && echestFarming.get() && buildMode.get() == BuildMode.PAVE) {
                    Handlers.runEchestFarmer();
                    MusheorSystem.debug("Running echest farmer...");
                } else if (!state.isFlag2() || (enableItemRestocking.get() && mode.get() != Mode.MANUAL)) {
                    if (state.isFlag2()) {
                        if (InventoryManager.isRestocking || InventoryManager.postRestock) return;
                        InventoryManager.targetPos = mc.player.getBlockPos();
                        PlayerUtils.setAutoWalk(false);
                        InventoryManager.runRestock();
                    }
                    if (buildMode.get() == BuildMode.PAVE) {
                        if (mode.get() == Mode.AUTO) {
                            Handlers.runAutoBuild();
                        } else {
                            switch ((HighwayType) highwayType.get()) {
                                case CARDINAL -> Handlers.paveCardinal();
                                case DIAGONAL -> Handlers.paveDiagonal();
                            }
                        }
                    }
                    if (buildMode.get() == BuildMode.DIG) {
                        switch ((HighwayType) highwayType.get()) {
                            case CARDINAL -> Handlers.digCardinal();
                            case DIAGONAL -> Handlers.digDiagonal();
                        }
                    }
                } else {
                    String reason = "Player is low on materials... Missing: ";
                    if (enderChestCount < 8) reason += "Enderchest count: " + enderChestCount + "/8 ";
                    if (obsidianCount < 1) reason += "Obsidian count: " + obsidianCount + "/8 ";
                    if (!hasFood()) reason += "Food - golden apples blacklisted in auto-eat module? ";
                    if (getBuildMode() == BuildMode.PAVE && state.getSwapDelayTicks() < 1) reason += "Non SilkTouch pickaxe.";
                    if (getBuildMode() == BuildMode.DIG && state.getBreakProgress() < 1) reason += "Pickaxe.";
                    PlayerUtils.toggleHighwayBuilder();
                    if (disconnectIfNoMaterials.get()) PlayerUtils.sendChatMessage(reason);
                    else this.error(reason, new Object[0]);
                }
            } else {
                PlayerUtils.setAutoWalk(false);
            }
        }
    }

    /** True if the echest farmer should run (obsidian low, echest shulkers available, pavement=obsidian). */
    private boolean shouldRunEchestFarmer(int obsidianCount, int echestCount) { // was: FvaNWO(int,int)
        HighwayState state = HighwayState.getInstance();
        if (!state.isFlag7() && !state.isFlag6() && !state.isFlag8() && !PlayerUtils.isGatheringItem()) {
            return pavementBlock.get() == Blocks.OBSIDIAN
                && echestCount > 8 && InventoryManager.countEmptyInventorySlots() > 0 && obsidianCount <= 8;
        }
        return false;
    }

    /** Sets up highway direction/center/checkpoint state (Auto mode detects the highway). */
    private boolean initHighwayState() { // was: sFazojak6ig8QgGq()
        assert mc.player != null;
        HighwayState state = HighwayState.getInstance();
        state.setDirection(WorldUtils.getMovementDirection());
        state.setStartX(mc.player.getBlockX());
        state.setStartZ(mc.player.getBlockZ());
        state.setLastX(VersionHelper.get().getPlayerPos().getX());
        state.setLastZ(VersionHelper.get().getPlayerPos().getZ());
        state.setCenterX(mc.player.getBlockX());
        state.setCenterY(mc.player.getBlockY());
        state.setCenterZ(mc.player.getBlockZ());
        if (mode.get() == Mode.AUTO) {
            HighwayLocator.Checkpoint detected = HighwayLocator.locateNearest(
                mc.player.getBlockX(), mc.player.getBlockZ(), mc.player.getBlockY(), state.getDirection());
            if (detected == null) {
                this.error("Auto mode could not detect a highway. Stand on a highway or use Semi mode.", new Object[0]);
                return false;
            }
            state.setCurrentCheckpoint(detected);
            state.setStartX(detected.startX);
            state.setStartZ(detected.startZ);
            state.setLastX(detected.alignX);
            state.setLastZ(detected.alignZ);
            state.setCenterX(detected.startX);
            state.setCenterZ(detected.startZ);
            this.info("Detected: §b" + detected.label + "§r | Width: §e" + detected.width + "§r | Dir: §a" + detected.direction, new Object[0]);
        }
        return true;
    }

    /** Enables all the helper modules the paver relies on (per settings). */
    private void enableHelperModules() { // was: ewq603nIlCd9Gbu()
        Module kekNuker = Modules.get().get(KekNuker.class);
        Module autoEat = Modules.get().get(AutoEat.class);
        Module autoGap = Modules.get().get(AutoGap.class);
        Module killAura = Modules.get().get(KillAura.class);
        Module sourceRemover = Modules.get().get(SourceRemover.class);
        Module inventoryCleaner = Modules.get().get(InventoryCleaner.class);
        Module hotbarReplenish = Modules.get().get(HotbarReplenish.class);
        Module freeLook = Modules.get().get(FreeLook.class);
        if (toggleKekNuker.get() && !kekNuker.isActive()) kekNuker.toggle();
        if (toggleAutoEat.get() && !autoEat.isActive()) autoEat.toggle();
        if (toggleAutoGap.get() && !autoGap.isActive()) autoGap.toggle();
        if (toggleKillAura.get() && !killAura.isActive()) killAura.toggle();
        if (toggleInventoryCleaner.get() && !inventoryCleaner.isActive()) inventoryCleaner.toggle();
        if (enableFreeLook.get() && !freeLook.isActive()) PlayerUtils.enableFreeLookCamera();
        if (toggleAutoReplenish.get() && !hotbarReplenish.isActive()) hotbarReplenish.toggle();
        if (discordRpc.get()) DiscordRPC.start();
        if (toggleSourceFiller.get() && !sourceRemover.isActive()) sourceRemover.toggle();
    }

    /** Disables the helper modules the paver toggled on. */
    public static void disableHelperModules() { // was: FvaNWO() (static)
        Module[] alwaysDisable = {
            Modules.get().get(EchestFarmer.class), Modules.get().get(KekNuker.class),
            Modules.get().get(AutoWalk.class), Modules.get().get(HotbarReplenish.class),
            Modules.get().get(FreeLook.class), Modules.get().get(SourceRemover.class),
            Modules.get().get(InventoryCleaner.class), Modules.get().get(KekBounce.class)
        };
        for (Module module : alwaysDisable) if (module.isActive()) module.toggle();

        Module killAura = Modules.get().get(KillAura.class);
        Module autoEat = Modules.get().get(AutoEat.class);
        Module autoGap = Modules.get().get(AutoGap.class);
        if (INSTANCE.toggleKillAura.get() && killAura.isActive()) killAura.toggle();
        if (INSTANCE.toggleAutoEat.get() && autoEat.isActive()) autoEat.toggle();
        if (INSTANCE.toggleAutoGap.get() && autoGap.isActive()) autoGap.toggle();
    }

    /** True if the player has any non-blacklisted food (or an enchanted golden apple). */
    public static boolean hasFood() { // was: Q90GLXQ0Pef() (static)
        List<Item> blacklistedFood = ((AutoEat) Modules.get().get(AutoEat.class)).blacklist.get();
        for (int i = 0; i < Objects.requireNonNull(MinecraftClient.getInstance().player).getInventory().main.size(); i++) {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            if (stack.getComponents().contains(DataComponentTypes.FOOD)
                && !(stack.getItem() instanceof ShulkerBoxBlock)
                && !blacklistedFood.contains(stack.getItem())) {
                return true;
            }
            if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE && !blacklistedFood.contains(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    /** Increments the session mined-block counters based on the broken block's type. */
    public static void countBrokenBlock(BlockState s) { // was: FvaNWO(BlockState) (static)
        HighwayState state = HighwayState.getInstance();
        if (s.getBlock() == Blocks.OBSIDIAN) state.incrementSessionObsidianMined();
        if (s.getBlock() == Blocks.CRYING_OBSIDIAN) state.incrementSessionLavaBuckets();
        if (s.getBlock() == Blocks.ENDER_CHEST) state.incrementSessionMiscMined();
    }

    public static boolean isEating() { // was: psJq59YIbp3Z() (static)
        return ((AutoEat) Modules.get().get(AutoEat.class)).eating || ((AutoGap) Modules.get().get(AutoGap.class)).isEating();
    }

    public static boolean isKillAuraAttacking() { // was: SOYyh5IPg26f7F() (static)
        return ((KillAura) Modules.get().get(KillAura.class)).attacking;
    }

    // --- Accessors used by Handlers / WorldUtils / BlockPositions ---
    public static WorldUtils.Direction8 getDirection() { return HighwayState.getInstance().getDirection(); } // was: rKbT3Ifwo()
    public static boolean isEchestFarmerActive() { return EchestFarmer.INSTANCE.isActive(); } // was: r7hOYIKN2()
    public static BuildMode getBuildMode() { return INSTANCE.buildMode.get(); } // was: oZHMlTL()
    public static Mode getMode() { return INSTANCE.mode.get(); } // was: xQr5FhbwpQPWgIQ()
    public static HighwayType getHighwayType() { // was: OMMZL1F3q()
        if (INSTANCE.mode.get() == Mode.AUTO) {
            HighwayLocator.Checkpoint d = HighwayState.getInstance().getCurrentCheckpoint();
            if (d != null) return d.type;
        }
        return INSTANCE.highwayType.get();
    }
    public static int getWidth() { // was: zu3a44xDeMFMCRwm()
        if (INSTANCE.mode.get() == Mode.AUTO) {
            HighwayLocator.Checkpoint d = HighwayState.getInstance().getCurrentCheckpoint();
            if (d != null) return d.width;
        }
        return INSTANCE.pavementWidth.get();
    }
    public static boolean mineAboveRails() { return INSTANCE.mineAboveRails.get(); } // was: krxNb5lcQuWA()
    public static boolean replaceCryingObsidian() { return INSTANCE.replaceCryingObsidian.get(); } // was: nt0HZnvBBp()
    public static Block getFillBlock() { return INSTANCE.pavementBlock.get(); } // was: amz3UB1vE()
    public static boolean placeRails() { return INSTANCE.placeRails.get(); } // was: sBBIyQG5NWq0K()
    public static boolean placeLeftRail() { return INSTANCE.placeLeftRail.get(); } // was: sZkZ1izAy()
    public static boolean placeRightRail() { return INSTANCE.placeRightRail.get(); } // was: QYKUhjp()
    public static Block getScaffoldBlock() { return INSTANCE.scaffoldBlock.get(); } // was: NIz4xic3Js9()
    public static ScaffoldMode getScaffoldMode() { return INSTANCE.scaffoldMode.get(); } // was: u1WFwbQRSKa()
    public static boolean scaffoldLeftRail() { return INSTANCE.scaffoldLeftRail.get(); } // was: LGDfbZq()
    public static boolean scaffoldRightRail() { return INSTANCE.scaffoldRightRail.get(); } // was: to3T8DJCDVX8po()
    public static boolean advancedSourceFiller() { return INSTANCE.advancedSourceFiller.get(); } // was: Sd3jEwKuGABy()
    public static boolean hasCeiling() { return INSTANCE.fillCeiling.get(); } // was: kJfFkD47Vh()
    public static boolean enableItemRestocking() { return INSTANCE.enableItemRestocking.get(); } // was: ubHptFBRn5bO()
    public static boolean swapBrokenPickaxes() { return INSTANCE.swapBrokenPickaxes.get(); } // was: apOpfoOHr3fJVwT()
    public static boolean shulkerRestocking() { return INSTANCE.shulkerRestocking.get(); } // was: hq1pN0qY()
    public static boolean echestShulkerRestocking() { return INSTANCE.echestShulkerRestocking.get(); } // was: ptxWcpd1WV763T5()
    public static boolean toolShulkerRestocking() { return INSTANCE.toolShulkerRestocking.get(); } // was: DnAk86nuI()
    public static boolean isAutoBounceEnabled() { return INSTANCE.autoBounce.get() && INSTANCE.buildMode.get() == BuildMode.PAVE; } // was: LlN8EpIZKbk()
    public static int getBounceDistanceCheck() { return INSTANCE.bounceDistanceCheck.get(); } // was: pgjj9cLYUTE5g()

    /** Renders the pending pavement positions as an ESP (Semi/Manual modes). */
    @EventHandler
    private void onRender(Render3DEvent event) { // was: FvaNWO(Render3DEvent)
        HighwayState state = HighwayState.getInstance();
        assert mc.player != null;
        if (state.getDirection() == null || state.getCenterX() == null || state.getCenterY() == null || state.getCenterZ() == null
            || state.getLastZ() == null || state.getLastX() == null) return;
        if (!MusheorSystem.Manager.placeRender.get() || buildMode.get() != BuildMode.PAVE) return;

        HighwayLocator.Checkpoint detected = HighwayLocator.locateNearest(
            mc.player.getBlockX(), mc.player.getBlockZ(), mc.player.getBlockY(), getDirection());
        int railY = state.getCenterY();
        List<BlockPos> cardinalPositions = new ArrayList<>();
        for (BlockPos pos : BlockPositions.cardinalFloor(3, 2, placeLeftRail(), placeRightRail())) {
            if ((detected == null || pos.getY() != railY || !HighwayLocator.isRailOnAnyHighway(pos, detected)) && BlockUtils.canPlace(pos, true))
                cardinalPositions.add(pos);
        }
        List<BlockPos> diagonalPositions = new ArrayList<>();
        for (BlockPos pos : BlockPositions.diagonalFloor(2, 2, placeLeftRail(), placeRightRail())) {
            if ((detected == null || pos.getY() != railY || !HighwayLocator.isRailOnAnyHighway(pos, detected)) && BlockUtils.canPlace(pos, true))
                diagonalPositions.add(pos);
        }
        if (getHighwayType() == HighwayType.CARDINAL) RenderUtils.render(event, cardinalPositions, getFillBlock());
        if (getHighwayType() == HighwayType.DIAGONAL) RenderUtils.render(event, diagonalPositions, getFillBlock());
    }

    /** Pave vs. Dig. */ // was: enum BuildMode {FvaNWO, Q90GLXQ0Pef}
    public enum BuildMode { PAVE, DIG }

    /** Cardinal vs. Diagonal highway. */ // was: enum HighwayType {FvaNWO, Q90GLXQ0Pef}
    public enum HighwayType { CARDINAL, DIAGONAL }

    /** Auto (full detection) / Semi (chosen type) / Manual (full manual control). */ // was: enum Mode {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum Mode { AUTO, SEMI, MANUAL }

    /** Scaffolding style for dig mode: none / normal / advanced. */ // was: enum ScaffoldMode {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum ScaffoldMode { NONE, NORMAL, ADVANCED }
}
