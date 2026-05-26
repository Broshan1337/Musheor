// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import java.util.ArrayList;
import java.util.List;
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
import meteordevelopment.meteorclient.systems.modules.combat.AutoTotem;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWalk;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.modules.automation.EchestFarmer;
import musheor.modules.automation.HotbarReplenish;
import musheor.modules.automation.InventoryCleaner;
import musheor.modules.automation.KekNuker;
import musheor.modules.automation.SourceRemover;
import musheor.musheor;
import musheor.utils.BlockPositions;
import musheor.utils.DiscordRPC;
import musheor.utils.Handlers;
import musheor.utils.InventoryManager;
import musheor.utils.PlayerUtils;
import musheor.utils.RenderUtils;
import musheor.utils.StatsHandler;
import musheor.utils.WorldUtils;
import musheor.utils.internal.HighwayState;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.MusheorSystem;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.SuspiciousStewItem;
import net.minecraft.BlockState;
import net.minecraft.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.SuspiciousStewItem;

public class HighwayBuilder
extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgPlacement = this.settings.createGroup("Placement");
    private final SettingGroup sgRestocking = this.settings.createGroup("Restocking");
    private final SettingGroup sgAutoEat = this.settings.createGroup("Auto Eat");
    private final SettingGroup sgNuker = this.settings.createGroup("Nuker");
    private final SettingGroup sgKillAura = this.settings.createGroup("Kill Aura");
    private final SettingGroup sgSafety = this.settings.createGroup("Safety");
    private final SettingGroup sgInventory = this.settings.createGroup("Inventory");
    private final SettingGroup sgMisc = this.settings.createGroup("Miscellaneous");
    private final Setting<BuildMode> buildMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("build-mode")).description("Pave mode places blocks - Dig mode digs tunnels.")).defaultValue((Object)BuildMode.Pave)).build());
    public final Setting<Mode> mode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Handle walking and aligning automatically or ignore in manual mode")).defaultValue((Object)Mode.Auto)).build());
    private final Setting<HighwayType> highwayType = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("highway-type")).description("Cardinal or diagonal highway type.")).defaultValue((Object)HighwayType.Cardinal)).build());
    private final Setting<Integer> pavementWidth = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pavement-width")).description("Width of the pavement below the player's feet.")).defaultValue((Object)4)).sliderRange(3, 9).build());
    private final Setting<Block> pavementBlock = this.sgPlacement.add((Setting)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)new BlockSetting.Builder().name("pavement-block")).description("Block that is used to build highway pavement with.")).defaultValue((Object)Blocks.OBSIDIAN)).visible(() -> this.buildMode.get() == BuildMode.Pave)).build());
    private final Setting<ScaffoldMode> scaffoldMode = this.sgPlacement.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("scaffold-mode")).description("What type of scaffolding to use when going over caves and open area's.")).defaultValue((Object)ScaffoldMode.GrimScaffold)).visible(() -> this.buildMode.get() == BuildMode.Dig)).build());
    private final Setting<Block> scaffoldBlock = this.sgPlacement.add((Setting)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)new BlockSetting.Builder().name("scaffold-block")).description("Block that is used to fix the flooring with.")).defaultValue((Object)Blocks.NETHERRACK)).visible(() -> this.buildMode.get() == BuildMode.Dig && this.scaffoldMode.get() != ScaffoldMode.None)).build());
    private final Setting<Boolean> scaffoldLeftRail = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("scaffold-left-rail")).description("Places the railing on the left side of the player.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.Dig)).build());
    private final Setting<Boolean> scaffoldRightRail = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("scaffold-right-rail")).description("Places the railing on the right side of the player.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.Dig)).build());
    private final Setting<Boolean> placeRails = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-rails")).description("Places railings on both sides of the highway 1 block above the pavement.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.Pave)).build());
    private final Setting<Boolean> placeLeftRails = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-left-rail")).description("Places railings on the left side sides of the highway 1 block above the pavement.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.Pave && (Boolean)this.placeRails.get() != false)).build());
    private final Setting<Boolean> placeRightRails = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-right-rail")).description("Places railings on the right sides of the highway 1 block above the pavement.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.Pave && (Boolean)this.placeRails.get() != false)).build());
    private final Setting<Boolean> replaceCryingObsidian = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("replace-crying-obsidian")).description("Mines and replaces crying obsidian if part of the pavement positions.")).defaultValue((Object)false)).visible(() -> this.buildMode.get() == BuildMode.Pave)).build());
    private final Setting<Boolean> fillCeiling = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fill-ceiling")).description("Fills the ceiling at Y level 123 with blocks.")).defaultValue((Object)false)).visible(() -> this.buildMode.get() == BuildMode.Pave)).build());
    private final Setting<Boolean> allowEchestFarming = this.sgRestocking.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("echest-farming")).description("Allow the player to farm obsidian by mining enderchests.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.Pave && this.pavementBlock.get() == Blocks.OBSIDIAN)).build());
    private final Setting<Boolean> allowItemRestocking = this.sgRestocking.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("item-restocking")).description("Allow the restocking process to grab items from shulkers.")).defaultValue((Object)false)).build());
    private final Setting<Boolean> storeBrokenPickaxes = this.sgRestocking.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("store-broken-pickaxes")).description("Swaps broken pickaxes with new ones when restocking these, allowing you to repair them later.")).defaultValue((Object)true)).visible(() -> this.allowItemRestocking.get())).build());
    private final Setting<Boolean> allowShulkerRestocking = this.sgRestocking.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shulker-restocking")).description("Allow the restocking process to grab shulkers from the player's enderchest.")).defaultValue((Object)false)).build());
    private final Setting<Boolean> allowEchestShulkerRestocking = this.sgRestocking.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("echest-shulker-restocking")).description("Allow the restocking process to grab echest shulkers from the player's enderchest.")).defaultValue((Object)false)).visible(() -> this.allowShulkerRestocking.get())).visible(() -> false)).build());
    private final Setting<Boolean> allowToolShulkerRestocking = this.sgRestocking.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("tool-shulker-restocking")).description("Allow the restocking process to grab tool shulkers from the player's enderchest.")).defaultValue((Object)false)).visible(() -> this.allowShulkerRestocking.get())).build());
    private final Setting<Boolean> autoEat = this.sgAutoEat.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-eat")).description("Pauses the current task and automatically eats.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> disableAutoEat = this.sgAutoEat.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-auto-eat")).description("Disables auto-eat when deactivating the paver.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> autoGap = this.sgAutoEat.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("eat-gap")).description("Allow the player to eat golden apples when required.")).defaultValue((Object)true)).visible(() -> this.autoEat.get())).build());
    private final Setting<Boolean> disableAutoGap = this.sgAutoEat.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-auto-gap")).description("Disables auto-gap when deactivating the paver.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> enableAutoTotem = this.sgSafety.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-totem")).description("Enable Autototem when activating this module.")).defaultValue((Object)true)).visible(() -> this.autoGap.get())).build());
    private final Setting<Boolean> disableAutoTotemAfterDeactivating = this.sgSafety.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-auto-totem-on-toggle")).description("Disable Autototem when deactivating this module.")).defaultValue((Object)false)).build());
    private final Setting<Boolean> enableDiscordRPC = this.sgMisc.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("discord-rpc")).description("Send paver stats to discord activity.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> enableFreeLook = this.sgMisc.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enable-freelook")).description("Freelook when using the paver.")).defaultValue((Object)true)).build());
    public final Setting<Boolean> enableNuker = this.sgNuker.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enable-nuker")).description("Allows the paver to use the nuker module.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> removeBlocksAboveRails = this.sgNuker.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mine-above-rails")).description("Removes and blocks that above the railings.")).defaultValue((Object)false)).build());
    private final Setting<Boolean> enableKillAura = this.sgKillAura.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("kill-aura")).description("Automatically attack nearby hostile entities.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> disableKillAuraAfterDeactivating = this.sgKillAura.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-kill-aura-on-toggle")).description("Disables kill aura after deactivating.")).defaultValue((Object)true)).visible(() -> this.enableKillAura.get())).build());
    private final Setting<Boolean> isDisconnect = this.sgSafety.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disconnect-if-no-materials")).description("Automatically disconnect when materials are low (less than 8 Obsidian/E-Chests).")).defaultValue((Object)false)).build());
    private final Setting<Boolean> enableSourceRemover = this.sgSafety.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("source-remover")).description("Automatically remove lava sources when you can reach them.")).defaultValue((Object)false)).build());
    private final Setting<Boolean> removeAnnoyingLava = this.sgSafety.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("advanced-source-remover")).description("Uses baritone to path to lava sources ahead of the player.")).defaultValue((Object)false)).visible(null)).build());
    private final Setting<Boolean> autoReplenish = this.sgInventory.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-replenish")).description("Automatically move items from inventory to hotbar when slots are empty.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> inventoryCleaner = this.sgInventory.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("inventory-cleaner")).description("Allow the paver to dispose of unwanted items.")).defaultValue((Object)false)).build());
    private final Setting<Boolean> pauseOnLag = this.sgMisc.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-lag")).description("Stops the player from paving when the server is lagging.")).defaultValue((Object)false)).build());
    private final Setting<Integer> lagThreshold = this.sgMisc.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("lag-threshold")).description("Lag threshold before forcing the player to pause.")).defaultValue((Object)3)).visible(() -> this.pauseOnLag.get())).build());
    public static HighwayBuilder INSTANCE;

    public HighwayBuilder() {
        super(musheor.AUTOMATION, "HighwayBuilder", "Automated highway builder for 2b2t.");
        INSTANCE = this;
    }

    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        HighwayState highwayState = HighwayState.getInstance();
        if (this.mc.player.isSpectator()) {
            MusheorSystem.debug("Player is probably in queue, waiting...", new Object[0]);
            highwayState.setTicksActive(0);
        } else {
            boolean bl;
            highwayState.reset();
            boolean bl2 = this.pavementBlock.get() == Blocks.OBSIDIAN && InventoryManager.usJLOV0subXO3(Items.ENDER_CHEST) < 1;
            boolean bl3 = this.pavementBlock.get() == Blocks.OBSIDIAN && InventoryManager.usJLOV0subXO3(HighwayBuilder.getPavementBlock().asItem()) < 8;
            boolean bl4 = InventoryManager.VYEwzRq(false) < 1;
            boolean bl5 = bl = InventoryManager.VYEwzRq(true) < 1;
            if (this.buildMode.get() == BuildMode.Pave && (bl2 || bl3 || bl4 || !HighwayBuilder.hasFood())) {
                if (bl2) {
                    this.error("No enderchests found in inventory...", new Object[0]);
                }
                if (bl3) {
                    this.error("No pavement material found in inventory...", new Object[0]);
                }
                if (!HighwayBuilder.hasFood()) {
                    this.error("No food found in inventory...", new Object[0]);
                }
                if (bl4) {
                    this.error("No non-silk touch pickaxe found in inventory...!", new Object[0]);
                }
                PlayerUtils.xynAsOKhN7t();
                return;
            }
            if (this.buildMode.get() == BuildMode.Dig && (bl || !HighwayBuilder.hasFood())) {
                if (bl) {
                    this.error("No pickaxe found in inventory...!", new Object[0]);
                }
                if (!HighwayBuilder.hasFood()) {
                    this.error("No food found in inventory...", new Object[0]);
                }
                PlayerUtils.xynAsOKhN7t();
                return;
            }
            highwayState.setCenterPos(this.mc.player.getBlockPos());
            highwayState.setTicksActive(0);
            InventoryManager.TAdu5cndwWu3A1(false);
            this.initHighwayPosition();
            PlayerUtils.jOdDDFXSeWl4(KekNuker.class, "nuker-mode", KekNuker.NukerMode.h9MViB);
            this.enableCompanionModules();
            highwayState.setCsvData(StatsHandler.BhO5G7());
            highwayState.loadLifetimeStatsFromCsv();
            highwayState.getBlocksToBuild().clear();
            highwayState.getBlocksToBuild().add(this.mc.player.getBlockPos());
            highwayState.getBlockBreakAttempts().clear();
            PlayerUtils.jOdDDFXSeWl4(AutoWalk.class, "mode", AutoWalk.Mode.Simple);
            PlayerUtils.jOdDDFXSeWl4(AutoWalk.class, "simple-direction", AutoWalk.Direction.Forwards);
        }
    }

    public void onDeactivate() {
        HighwayState.getInstance().saveLifetimeStatsToCsv();
        PlayerUtils.KP44bk(false);
        PathingHelper.xRVyNRV3cB7();
        HighwayBuilder.disableCompanionModules();
        DiscordRPC.stop();
        HighwayState.getInstance().reset();
        this.info("Deactivated, saving statistics...", new Object[0]);
    }

    @EventHandler
    public void onTick(TickEvent.Pre pre) {
        HighwayState highwayState = HighwayState.getInstance();
        if (this.mc.player == null || this.mc.world == null || this.mc.interactionManager == null) {
            highwayState.setTicksActive(0);
            return;
        }
        if (this.mc.player.isSpectator()) {
            MusheorSystem.debug("Player is probably in queue, waiting...", new Object[0]);
            highwayState.setTicksActive(0);
            return;
        }
        highwayState.incrementTicksActive();
        WorldUtils.jOdDDFXSeWl4(highwayState.getTicksActive(), HighwayBuilder.getPavementBlock());
        if (WorldUtils.btLCQHvKVR()) {
            return;
        }
        if (highwayState.getPendingBreakPos() != null) {
            PlayerUtils.KP44bk(false);
        }
        if (((Boolean)this.enableDiscordRPC.get()).booleanValue()) {
            highwayState.incrementTicksSinceLastAction();
            if (highwayState.getTicksSinceLastAction() >= 200) {
                highwayState.setTicksSinceLastAction(0);
                DiscordRPC.updateActivity();
            }
        } else {
            DiscordRPC.stop();
        }
        Module module = Modules.get().get(EchestFarmer.class);
        boolean bl = ((AutoGap)Modules.get().get(AutoGap.class)).isEating();
        if (HighwayBuilder.isEating() || bl || HighwayBuilder.isEchestFarming() || HighwayBuilder.isAttacking() || PlayerUtils.BT1BimvycZZjsYS() || highwayState.isResupplyActive()) {
            PlayerUtils.KP44bk(false);
            return;
        }
        int n = this.pavementBlock.get() == Blocks.OBSIDIAN ? InventoryManager.usJLOV0subXO3(Items.ENDER_CHEST) : 64;
        int n2 = this.pavementBlock.get() == Blocks.OBSIDIAN ? InventoryManager.usJLOV0subXO3(((Block)this.pavementBlock.get()).asItem()) : 64;
        highwayState.setBreakProgress(InventoryManager.VYEwzRq(false));
        highwayState.setSwapDelayTicks(InventoryManager.VYEwzRq(true));
        if (this.buildMode.get() == BuildMode.Pave) {
            highwayState.setNeedsMaterials(n < 8 || n2 < 8 || highwayState.getBreakProgress() < 1 || !HighwayBuilder.hasFood());
        }
        if (this.buildMode.get() == BuildMode.Dig) {
            highwayState.setNeedsMaterials(highwayState.getSwapDelayTicks() < 1 || !HighwayBuilder.hasFood());
        }
        if (highwayState.isInventoryBusy()) {
            InventoryManager.H02kTTf();
            return;
        }
        if (highwayState.isWaitingForRestock()) {
            InventoryManager.mp3zoXQFKUKYj5(highwayState.getBaritoneGoal(), InventoryManager.LrAtLm);
            return;
        }
        if (highwayState.isRestocking()) {
            InventoryManager.mp3zoXQFKUKYj5(highwayState.getBaritoneGoal(), InventoryManager.LrAtLm);
            MusheorSystem.debug("Restocking %s; Amount: %s", highwayState.getBaritoneGoal(), InventoryManager.LrAtLm);
            return;
        }
        if (highwayState.isResupplying() && !module.isActive() && ((Boolean)this.allowEchestFarming.get()).booleanValue()) {
            Handlers.PlefynG();
            MusheorSystem.debug("Running post-echest farmer...", new Object[0]);
            return;
        }
        if (this.shouldResupplyEchest(n2, n) && ((Boolean)this.allowEchestFarming.get()).booleanValue() && this.buildMode.get() == BuildMode.Pave) {
            Handlers.gANxWblT();
            MusheorSystem.debug("Running echest farmer...", new Object[0]);
            return;
        }
        if (highwayState.needsMaterials() && (!((Boolean)this.allowItemRestocking.get()).booleanValue() || this.mode.get() == Mode.Manual)) {
            Object object = "Player is low on materials... Missing: ";
            if (n < 8) {
                object = (String)object + "Enderchest count: " + n + "/8 ";
            }
            if (n2 < 8) {
                object = (String)object + "Obsidian count: " + n2 + "/8 ";
            }
            if (!HighwayBuilder.hasFood()) {
                object = (String)object + "Food (golden apples blacklisted?) ";
            }
            if (HighwayBuilder.getBuildMode() == BuildMode.Pave && highwayState.getSwapDelayTicks() < 1) {
                object = (String)object + "Non SilkTouch pickaxe.";
            }
            if (HighwayBuilder.getBuildMode() == BuildMode.Dig && highwayState.getBreakProgress() < 1) {
                object = (String)object + "Pickaxe.";
            }
            PlayerUtils.xynAsOKhN7t();
            if (((Boolean)this.isDisconnect.get()).booleanValue()) {
                PlayerUtils.setStartX((String)object);
            } else {
                this.error((String)object, new Object[0]);
            }
            return;
        }
        if (highwayState.needsMaterials()) {
            if (InventoryManager.QFWUbSJ63lmQY || InventoryManager.NZ3iHWsF) {
                return;
            }
            InventoryManager.NrfIVPgqB9 = this.mc.player.getBlockPos();
            PlayerUtils.KP44bk(false);
            InventoryManager.Dzj74FIoxmie();
        }
        if (this.buildMode.get() == BuildMode.Pave) {
            switch (((HighwayType)((Object)this.highwayType.get())).ordinal()) {
                case 0: {
                    Handlers.Pmh3HuqB53i0Y();
                    break;
                }
                case 1: {
                    Handlers.eNsdDMk8mJXTb();
                }
            }
        }
        if (this.buildMode.get() == BuildMode.Dig) {
            switch (((HighwayType)((Object)this.highwayType.get())).ordinal()) {
                case 0: {
                    Handlers.Z6nxChaWC9ymwoio();
                    break;
                }
                case 1: {
                    Handlers.AoH6MX();
                }
            }
        }
    }

    private boolean shouldResupplyEchest(int obsidianCount, int echestCount) {
        HighwayState highwayState = HighwayState.getInstance();
        if (highwayState.isRestocking() || highwayState.jIXFBaSwUWYqAc9() || highwayState.isInventoryBusy() || PlayerUtils.BT1BimvycZZjsYS()) {
            return false;
        }
        if (this.pavementBlock.get() != Blocks.OBSIDIAN) {
            return false;
        }
        return n2 > 8 && InventoryManager.ZeOLrA() > 0 && n <= 8;
    }

    private void initHighwayPosition() {
        assert (this.mc.player != null);
        HighwayState highwayState = HighwayState.getInstance();
        highwayState.setDirection(WorldUtils.eQlnaotm4pUDUmJT());
        highwayState.setStartX(this.mc.player.getX());
        highwayState.setStartZ(this.mc.player.getZ());
        highwayState.setLastX((double)this.mc.player.getX() + 0.5);
        highwayState.setLastZ((double)this.mc.player.getZ() + 0.5);
        highwayState.setCenterX(this.mc.player.getX());
        highwayState.setCenterY(this.mc.player.getY());
        highwayState.setCenterZ(this.mc.player.getZ());
    }

    private void enableCompanionModules() {
        Module module = Modules.get().get(KekNuker.class);
        Module module2 = Modules.get().get(AutoEat.class);
        Module module3 = Modules.get().get(AutoGap.class);
        Module module4 = Modules.get().get(KillAura.class);
        Module module5 = Modules.get().get(SourceRemover.class);
        Module module6 = Modules.get().get(InventoryCleaner.class);
        Module module7 = Modules.get().get(HotbarReplenish.class);
        Module module8 = Modules.get().get(AutoTotem.class);
        if (((Boolean)this.enableNuker.get()).booleanValue() && !module.isActive()) {
            module.toggle();
        }
        if (((Boolean)this.autoEat.get()).booleanValue() && !module2.isActive()) {
            module2.toggle();
        }
        if (((Boolean)this.autoGap.get()).booleanValue() && !module3.isActive()) {
            module3.toggle();
        }
        if (((Boolean)this.enableKillAura.get()).booleanValue() && !module4.isActive()) {
            module4.toggle();
        }
        if (((Boolean)this.inventoryCleaner.get()).booleanValue() && !module6.isActive()) {
            module6.toggle();
        }
        if (((Boolean)this.autoReplenish.get()).booleanValue() && !module7.isActive()) {
            module7.toggle();
        }
        if (((Boolean)this.enableDiscordRPC.get()).booleanValue()) {
            DiscordRPC.start();
        }
        if (((Boolean)this.enableFreeLook.get()).booleanValue()) {
            PlayerUtils.Gd2ks78ySQq40();
        }
        if (((Boolean)this.enableSourceRemover.get()).booleanValue() && !module5.isActive()) {
            module5.toggle();
        }
        if (((Boolean)this.enableAutoTotem.get()).booleanValue() && !module8.isActive()) {
            module8.toggle();
        }
    }

    public static void disableCompanionModules() {
        Module module3;
        Module module2;
        for (Module module3 : module2 = new Module[]{Modules.get().get(EchestFarmer.class), Modules.get().get(KekNuker.class), Modules.get().get(AutoWalk.class), Modules.get().get(HotbarReplenish.class), Modules.get().get(FreeLook.class), Modules.get().get(SourceRemover.class), Modules.get().get(InventoryCleaner.class)}) {
            if (!module3.isActive()) continue;
            module3.toggle();
        }
        Module module4 = Modules.get().get(KillAura.class);
        Module module5 = Modules.get().get(AutoTotem.class);
        Module module6 = Modules.get().get(AutoEat.class);
        module3 = Modules.get().get(AutoGap.class);
        if (((Boolean)HighwayBuilder.INSTANCE.disableKillAuraAfterDeactivating.get()).booleanValue() && module4.isActive()) {
            module4.toggle();
        }
        if (((Boolean)HighwayBuilder.INSTANCE.disableAutoTotemAfterDeactivating.get()).booleanValue() && module5.isActive()) {
            module5.toggle();
        }
        if (((Boolean)HighwayBuilder.INSTANCE.disableAutoEat.get()).booleanValue() && module6.isActive()) {
            module6.toggle();
        }
        if (((Boolean)HighwayBuilder.INSTANCE.disableAutoGap.get()).booleanValue() && module3.isActive()) {
            module3.toggle();
        }
    }

    public static boolean hasFood() {
        List list = (List)((AutoEat)Modules.get().get(AutoEat.class)).blacklist.get();
        for (int i = 0; i < Objects.requireNonNull(MinecraftClient.getInstance().player).getInventory().main.size(); ++i) {
            ItemStack ItemStack2 = MinecraftClient.getInstance().player.getInventory().getStack(i);
            if (ItemStack2.getItem().contains(DataComponentTypes.FOOD) && !(ItemStack2.getItem() instanceof SuspiciousStewItem) && !list.contains(ItemStack2.getItem())) {
                return true;
            }
            if (ItemStack2.getStack() != Items.GOLDEN_APPLE || list.contains(ItemStack2.getItem())) continue;
            return true;
        }
        return false;
    }

    public static void onBlockMined(BlockState BlockState2) {
        HighwayState highwayState = HighwayState.getInstance();
        if (BlockState2.getBlock() == Blocks.OBSIDIAN) {
            highwayState.incrementSessionObsidianMined();
        }
        if (BlockState2.getBlock() == Blocks.NETHERRACK) {
            highwayState.incrementSessionLavaBuckets();
        }
        if (BlockState2.getBlock() == Blocks.ENDER_CHEST) { // was: field_10443
            highwayState.incrementSessionMiscMined();
        }
    }

    public static boolean isEating() {
        return ((AutoEat)Modules.get().get(AutoEat.class)).eating || ((AutoGap)Modules.get().get(AutoGap.class)).isEating();
    }

    public static boolean isAttacking() {
        return ((KillAura)Modules.get().get(KillAura.class)).attacking;
    }

    public static WorldUtils.Direction8 getDirection() {
        return HighwayState.getInstance().getDirection();
    }

    public static boolean isEchestFarming() {
        return EchestFarmer.Nr0B0YDZRAaA.isActive();
    }

    public static BuildMode getBuildMode() {
        return (BuildMode)((Object)HighwayBuilder.INSTANCE.buildMode.get());
    }

    public static Mode getMode() {
        return (Mode)((Object)HighwayBuilder.INSTANCE.mode.get());
    }

    public static HighwayType getHighwayType() {
        return (HighwayType)((Object)HighwayBuilder.INSTANCE.highwayType.get());
    }

    public static int getPavementWidth() {
        return (Integer)HighwayBuilder.INSTANCE.pavementWidth.get();
    }

    public static boolean isRemoveBlocksAboveRails() {
        return (Boolean)HighwayBuilder.INSTANCE.removeBlocksAboveRails.get();
    }

    public static boolean isReplaceCryingObsidian() {
        return (Boolean)HighwayBuilder.INSTANCE.replaceCryingObsidian.get();
    }

    public static boolean isPauseOnLag() {
        return (Boolean)HighwayBuilder.INSTANCE.pauseOnLag.get();
    }

    public static int getLagThreshold() {
        return (Integer)HighwayBuilder.INSTANCE.lagThreshold.get();
    }

    public static Block getPavementBlock() {
        return (Block)HighwayBuilder.INSTANCE.pavementBlock.get();
    }

    public static boolean isPlaceRails() {
        return (Boolean)HighwayBuilder.INSTANCE.placeRails.get();
    }

    public static boolean isPlaceLeftRails() {
        return (Boolean)HighwayBuilder.INSTANCE.placeLeftRails.get();
    }

    public static boolean isPlaceRightRails() {
        return (Boolean)HighwayBuilder.INSTANCE.placeRightRails.get();
    }

    public static Block getScaffoldBlock() {
        return (Block)HighwayBuilder.INSTANCE.scaffoldBlock.get();
    }

    public static ScaffoldMode getScaffoldMode() {
        return (ScaffoldMode)((Object)HighwayBuilder.INSTANCE.scaffoldMode.get());
    }

    public static boolean isScaffoldLeftRail() {
        return (Boolean)HighwayBuilder.INSTANCE.scaffoldLeftRail.get();
    }

    public static boolean isScaffoldRightRail() {
        return (Boolean)HighwayBuilder.INSTANCE.scaffoldRightRail.get();
    }

    public static boolean isRemoveAnnoyingLava() {
        return (Boolean)HighwayBuilder.INSTANCE.removeAnnoyingLava.get();
    }

    public static boolean isFillCeiling() {
        return (Boolean)HighwayBuilder.INSTANCE.fillCeiling.get();
    }

    public static boolean isAllowItemRestocking() {
        return (Boolean)HighwayBuilder.INSTANCE.allowItemRestocking.get();
    }

    public static boolean isStoreBrokenPickaxes() {
        return (Boolean)HighwayBuilder.INSTANCE.storeBrokenPickaxes.get();
    }

    public static boolean isAllowShulkerRestocking() {
        return (Boolean)HighwayBuilder.INSTANCE.allowShulkerRestocking.get();
    }

    public static boolean isAllowEchestShulkerRestocking() {
        return (Boolean)HighwayBuilder.INSTANCE.allowEchestShulkerRestocking.get();
    }

    public static boolean isAllowToolShulkerRestocking() {
        return (Boolean)HighwayBuilder.INSTANCE.allowToolShulkerRestocking.get();
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
        HighwayState highwayState = HighwayState.getInstance();
        assert (this.mc.player != null);
        if (highwayState.getDirection() == null) {
            return;
        }
        if (HighwayState.getInstance().getCenterX() == null || HighwayState.getInstance().getCenterY() == null || HighwayState.getInstance().getCenterZ() == null) {
            return;
        }
        if (highwayState.getLastX() == null || highwayState.getLastZ() == null) {
            return;
        }
        if (((Boolean)MusheorSystem.Manager.placeRender.get()).booleanValue()) {
            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>(List.of());
            for (BlockPos BlockPos2 : BlockPositions.jOdDDFXSeWl4(2, 3, HighwayBuilder.isPlaceLeftRails(), HighwayBuilder.isPlaceRightRails())) {
                if (!BlockUtils.canPlace((BlockPos)BlockPos2, (boolean)true)) continue;
                arrayList.add(BlockPos2);
            }
            ArrayList arrayList2 = new ArrayList(List.of());
            for (BlockPos BlockPos3 : BlockPositions.Gt56Sj4a6BWhgB(2, 2, HighwayBuilder.isPlaceLeftRails(), HighwayBuilder.isPlaceRightRails())) {
                if (!BlockUtils.canPlace((BlockPos)BlockPos3, (boolean)true)) continue;
                arrayList2.add(BlockPos3);
            }
            if (this.buildMode.get() == BuildMode.Pave) {
                if (this.highwayType.get() == HighwayType.Cardinal) {
                    RenderUtils.jOdDDFXSeWl4(render3DEvent, arrayList, HighwayBuilder.getPavementBlock());
                }
                if (this.highwayType.get() == HighwayType.Diagonal) {
                    RenderUtils.jOdDDFXSeWl4(render3DEvent, arrayList2, HighwayBuilder.getPavementBlock());
                }
            }
        }
    }

    public static final class BuildMode
    extends Enum<BuildMode> {
        public static final /* enum */ BuildMode Pave = new BuildMode();
        public static final /* enum */ BuildMode Dig = new BuildMode();
        private static final /* synthetic */ BuildMode[] $VALUES;

        public static BuildMode[] values() {
            return (BuildMode[])$VALUES.clone();
        }

        public static BuildMode valueOf(String string) {
            return Enum.valueOf(BuildMode.class, string);
        }

        private static /* synthetic */ BuildMode[] $init() {
            return new BuildMode[]{Pave, Dig};
        }

        static {
            $VALUES = BuildMode.$init();
        }
    }

    public static final class Mode
    extends Enum<Mode> {
        public static final /* enum */ Mode Auto = new Mode();
        public static final /* enum */ Mode Manual = new Mode();
        private static final /* synthetic */ Mode[] $VALUES;

        public static Mode[] values() {
            return (Mode[])$VALUES.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private static /* synthetic */ Mode[] $init() {
            return new Mode[]{Auto, Manual};
        }

        static {
            $VALUES = Mode.$init();
        }
    }

    public static final class HighwayType
    extends Enum<HighwayType> {
        public static final /* enum */ HighwayType Cardinal = new HighwayType();
        public static final /* enum */ HighwayType Diagonal = new HighwayType();
        private static final /* synthetic */ HighwayType[] $VALUES;

        public static HighwayType[] values() {
            return (HighwayType[])$VALUES.clone();
        }

        public static HighwayType valueOf(String string) {
            return Enum.valueOf(HighwayType.class, string);
        }

        private static /* synthetic */ HighwayType[] $init() {
            return new HighwayType[]{Cardinal, Diagonal};
        }

        static {
            $VALUES = HighwayType.$init();
        }
    }

    public static final class ScaffoldMode
    extends Enum<ScaffoldMode> {
        public static final /* enum */ ScaffoldMode None = new ScaffoldMode();
        public static final /* enum */ ScaffoldMode GrimScaffold = new ScaffoldMode();
        public static final /* enum */ ScaffoldMode AirPlace = new ScaffoldMode();
        private static final /* synthetic */ ScaffoldMode[] $VALUES;

        public static ScaffoldMode[] values() {
            return (ScaffoldMode[])$VALUES.clone();
        }

        public static ScaffoldMode valueOf(String string) {
            return Enum.valueOf(ScaffoldMode.class, string);
        }

        private static /* synthetic */ ScaffoldMode[] $init() {
            return new ScaffoldMode[]{None, GrimScaffold, AirPlace};
        }

        static {
            $VALUES = ScaffoldMode.$init();
        }
    }
}

