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
import net.minecraft.class_2480;
import net.minecraft.BlockState;
import net.minecraft.MinecraftClient;
import net.minecraft.MutableText;

public class HighwayBuilder
extends Module {
    private final MinecraftClient dmmGdE2RN9C = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgPlacement = this.settings.createGroup("Placement");
    private final SettingGroup sgRestocking = this.settings.createGroup("Restocking");
    private final SettingGroup sgAutoEat = this.settings.createGroup("Auto Eat");
    private final SettingGroup sgNuker = this.settings.createGroup("Nuker");
    private final SettingGroup sgKillAura = this.settings.createGroup("Kill Aura");
    private final SettingGroup sgSafety = this.settings.createGroup("Safety");
    private final SettingGroup sgInventory = this.settings.createGroup("Inventory");
    private final SettingGroup sgMisc = this.settings.createGroup("Miscellaneous");
    private final Setting<BuildMode> buildMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("build-mode")).description("Pave mode places blocks - Dig mode digs tunnels.")).defaultValue((Object)BuildMode.e4uKoS)).build());
    public final Setting<Mode> mode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Handle walking and aligning automatically or ignore in manual mode")).defaultValue((Object)Mode.jll9Iyc1Ftxi)).build());
    private final Setting<HighwayType> highwayType = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("highway-type")).description("Cardinal or diagonal highway type.")).defaultValue((Object)HighwayType.b76P5ieurZIX)).build());
    private final Setting<Integer> pavementWidth = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pavement-width")).description("Width of the pavement below the player's feet.")).defaultValue((Object)4)).sliderRange(3, 9).build());
    private final Setting<Block> pavementBlock = this.sgPlacement.add((Setting)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)new BlockSetting.Builder().name("pavement-block")).description("Block that is used to build highway pavement with.")).defaultValue((Object)Blocks.field_10540)).visible(() -> this.buildMode.get() == BuildMode.e4uKoS)).build());
    private final Setting<ScaffoldMode> scaffoldMode = this.sgPlacement.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("scaffold-mode")).description("What type of scaffolding to use when going over caves and open area's.")).defaultValue((Object)ScaffoldMode.nQgi06)).visible(() -> this.buildMode.get() == BuildMode.flZYoiXwrl)).build());
    private final Setting<Block> scaffoldBlock = this.sgPlacement.add((Setting)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)((BlockSetting.Builder)new BlockSetting.Builder().name("scaffold-block")).description("Block that is used to fix the flooring with.")).defaultValue((Object)Blocks.field_10515)).visible(() -> this.buildMode.get() == BuildMode.flZYoiXwrl && this.scaffoldMode.get() != ScaffoldMode.oGrnfoe87ZeN)).build());
    private final Setting<Boolean> scaffoldLeftRail = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("scaffold-left-rail")).description("Places the railing on the left side of the player.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.flZYoiXwrl)).build());
    private final Setting<Boolean> scaffoldRightRail = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("scaffold-right-rail")).description("Places the railing on the right side of the player.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.flZYoiXwrl)).build());
    private final Setting<Boolean> placeRails = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-rails")).description("Places railings on both sides of the highway 1 block above the pavement.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.e4uKoS)).build());
    private final Setting<Boolean> placeLeftRails = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-left-rail")).description("Places railings on the left side sides of the highway 1 block above the pavement.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.e4uKoS && (Boolean)this.placeRails.get() != false)).build());
    private final Setting<Boolean> placeRightRails = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-right-rail")).description("Places railings on the right sides of the highway 1 block above the pavement.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.e4uKoS && (Boolean)this.placeRails.get() != false)).build());
    private final Setting<Boolean> replaceCryingObsidian = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("replace-crying-obsidian")).description("Mines and replaces crying obsidian if part of the pavement positions.")).defaultValue((Object)false)).visible(() -> this.buildMode.get() == BuildMode.e4uKoS)).build());
    private final Setting<Boolean> fillCeiling = this.sgPlacement.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fill-ceiling")).description("Fills the ceiling at Y level 123 with blocks.")).defaultValue((Object)false)).visible(() -> this.buildMode.get() == BuildMode.e4uKoS)).build());
    private final Setting<Boolean> allowEchestFarming = this.sgRestocking.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("echest-farming")).description("Allow the player to farm obsidian by mining enderchests.")).defaultValue((Object)true)).visible(() -> this.buildMode.get() == BuildMode.e4uKoS && this.pavementBlock.get() == Blocks.field_10540)).build());
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
        if (this.dmmGdE2RN9C.player == null || this.dmmGdE2RN9C.world == null) {
            return;
        }
        HighwayState highwayState = HighwayState.LmpuWjra();
        if (this.dmmGdE2RN9C.player.method_7325()) {
            MusheorSystem.debug("Player is probably in queue, waiting...", new Object[0]);
            highwayState.CEOjBr5G5R(0);
        } else {
            boolean bl;
            highwayState.merMGMToO0ZYtkEn();
            boolean bl2 = this.pavementBlock.get() == Blocks.field_10540 && InventoryManager.usJLOV0subXO3(Items.ENDER_CHEST) < 1;
            boolean bl3 = this.pavementBlock.get() == Blocks.field_10540 && InventoryManager.usJLOV0subXO3(HighwayBuilder.yaVvWAqooeFn().asItem()) < 8;
            boolean bl4 = InventoryManager.VYEwzRq(false) < 1;
            boolean bl5 = bl = InventoryManager.VYEwzRq(true) < 1;
            if (this.buildMode.get() == BuildMode.e4uKoS && (bl2 || bl3 || bl4 || !HighwayBuilder.l92qSNnpKrYO())) {
                if (bl2) {
                    this.error("No enderchests found in inventory...", new Object[0]);
                }
                if (bl3) {
                    this.error("No pavement material found in inventory...", new Object[0]);
                }
                if (!HighwayBuilder.l92qSNnpKrYO()) {
                    this.error("No food found in inventory...", new Object[0]);
                }
                if (bl4) {
                    this.error("No non-silk touch pickaxe found in inventory...!", new Object[0]);
                }
                PlayerUtils.xynAsOKhN7t();
                return;
            }
            if (this.buildMode.get() == BuildMode.flZYoiXwrl && (bl || !HighwayBuilder.l92qSNnpKrYO())) {
                if (bl) {
                    this.error("No pickaxe found in inventory...!", new Object[0]);
                }
                if (!HighwayBuilder.l92qSNnpKrYO()) {
                    this.error("No food found in inventory...", new Object[0]);
                }
                PlayerUtils.xynAsOKhN7t();
                return;
            }
            highwayState.Tne1O2a8S2sVbX(this.dmmGdE2RN9C.player.getBlockPos());
            highwayState.CEOjBr5G5R(0);
            InventoryManager.TAdu5cndwWu3A1(false);
            this.ULOAMKfWE3NZZTj8();
            PlayerUtils.jOdDDFXSeWl4(KekNuker.class, "nuker-mode", KekNuker.NukerMode.h9MViB);
            this.gsYdyKVgv();
            highwayState.jWrhVf2psx(StatsHandler.BhO5G7());
            highwayState.Tz7qNAG6();
            highwayState.Mz2EP5().clear();
            highwayState.Mz2EP5().add(this.dmmGdE2RN9C.player.getBlockPos());
            highwayState.Os3dd8a().clear();
            PlayerUtils.jOdDDFXSeWl4(AutoWalk.class, "mode", AutoWalk.Mode.Simple);
            PlayerUtils.jOdDDFXSeWl4(AutoWalk.class, "simple-direction", AutoWalk.Direction.Forwards);
        }
    }

    public void onDeactivate() {
        HighwayState.LmpuWjra().yQzOzveN8BjKJN5();
        PlayerUtils.KP44bk(false);
        PathingHelper.xRVyNRV3cB7();
        HighwayBuilder.V2mbWoNZftH0t();
        DiscordRPC.wkzyCzfggXudWEsa();
        HighwayState.LmpuWjra().merMGMToO0ZYtkEn();
        this.info("Deactivated, saving statistics...", new Object[0]);
    }

    @EventHandler
    public void onTick(TickEvent.Pre pre) {
        HighwayState highwayState = HighwayState.LmpuWjra();
        if (this.dmmGdE2RN9C.player == null || this.dmmGdE2RN9C.world == null || this.dmmGdE2RN9C.field_1761 == null) {
            highwayState.CEOjBr5G5R(0);
            return;
        }
        if (this.dmmGdE2RN9C.player.method_7325()) {
            MusheorSystem.debug("Player is probably in queue, waiting...", new Object[0]);
            highwayState.CEOjBr5G5R(0);
            return;
        }
        highwayState.fjsJhTJB1Q6qDp4F();
        WorldUtils.jOdDDFXSeWl4(highwayState.yIXEDGFGtS9H(), HighwayBuilder.yaVvWAqooeFn());
        if (WorldUtils.btLCQHvKVR()) {
            return;
        }
        if (highwayState.HBAiI3pyGGGxpI2b() != null) {
            PlayerUtils.KP44bk(false);
        }
        if (((Boolean)this.enableDiscordRPC.get()).booleanValue()) {
            highwayState.cIb0h21P81();
            if (highwayState.GjvUiGg0HmH6I() >= 200) {
                highwayState.MS1x7YGHjIg7eB(0);
                DiscordRPC.urju0X();
            }
        } else {
            DiscordRPC.wkzyCzfggXudWEsa();
        }
        Module module = Modules.get().get(EchestFarmer.class);
        boolean bl = ((AutoGap)Modules.get().get(AutoGap.class)).isEating();
        if (HighwayBuilder.S7TLszvzENsW7() || bl || HighwayBuilder.IuR8CfqY() || HighwayBuilder.zl2vxyh() || PlayerUtils.BT1BimvycZZjsYS() || highwayState.Y775oeIufYz9()) {
            PlayerUtils.KP44bk(false);
            return;
        }
        int n = this.pavementBlock.get() == Blocks.field_10540 ? InventoryManager.usJLOV0subXO3(Items.ENDER_CHEST) : 64;
        int n2 = this.pavementBlock.get() == Blocks.field_10540 ? InventoryManager.usJLOV0subXO3(((Block)this.pavementBlock.get()).asItem()) : 64;
        highwayState.KDNrzlU9qtrEv(InventoryManager.VYEwzRq(false));
        highwayState.WOqvNwnejoKApoa(InventoryManager.VYEwzRq(true));
        if (this.buildMode.get() == BuildMode.e4uKoS) {
            highwayState.e5oi2ZF(n < 8 || n2 < 8 || highwayState.hJTPuzeVhs9lAR() < 1 || !HighwayBuilder.l92qSNnpKrYO());
        }
        if (this.buildMode.get() == BuildMode.flZYoiXwrl) {
            highwayState.e5oi2ZF(highwayState.LTAva3M() < 1 || !HighwayBuilder.l92qSNnpKrYO());
        }
        if (highwayState.XMj1R1A1()) {
            InventoryManager.H02kTTf();
            return;
        }
        if (highwayState.gsu3U1()) {
            InventoryManager.mp3zoXQFKUKYj5(highwayState.lzRYRnZcMXfWy6t(), InventoryManager.LrAtLm);
            return;
        }
        if (highwayState.OIExXGL6BNv()) {
            InventoryManager.mp3zoXQFKUKYj5(highwayState.lzRYRnZcMXfWy6t(), InventoryManager.LrAtLm);
            MusheorSystem.debug("Restocking %s; Amount: %s", highwayState.lzRYRnZcMXfWy6t(), InventoryManager.LrAtLm);
            return;
        }
        if (highwayState.rpvWtoVonf6GeT() && !module.isActive() && ((Boolean)this.allowEchestFarming.get()).booleanValue()) {
            Handlers.PlefynG();
            MusheorSystem.debug("Running post-echest farmer...", new Object[0]);
            return;
        }
        if (this.jOdDDFXSeWl4(n2, n) && ((Boolean)this.allowEchestFarming.get()).booleanValue() && this.buildMode.get() == BuildMode.e4uKoS) {
            Handlers.gANxWblT();
            MusheorSystem.debug("Running echest farmer...", new Object[0]);
            return;
        }
        if (highwayState.XWpV9Q7() && (!((Boolean)this.allowItemRestocking.get()).booleanValue() || this.mode.get() == Mode.OUSKA4lEld)) {
            Object object = "Player is low on materials... Missing: ";
            if (n < 8) {
                object = (String)object + "Enderchest count: " + n + "/8 ";
            }
            if (n2 < 8) {
                object = (String)object + "Obsidian count: " + n2 + "/8 ";
            }
            if (!HighwayBuilder.l92qSNnpKrYO()) {
                object = (String)object + "Food (golden apples blacklisted?) ";
            }
            if (HighwayBuilder.cghz8iox35K() == BuildMode.e4uKoS && highwayState.LTAva3M() < 1) {
                object = (String)object + "Non SilkTouch pickaxe.";
            }
            if (HighwayBuilder.cghz8iox35K() == BuildMode.flZYoiXwrl && highwayState.hJTPuzeVhs9lAR() < 1) {
                object = (String)object + "Pickaxe.";
            }
            PlayerUtils.xynAsOKhN7t();
            if (((Boolean)this.isDisconnect.get()).booleanValue()) {
                PlayerUtils.J2pm2c07elEb5G((String)object);
            } else {
                this.error((String)object, new Object[0]);
            }
            return;
        }
        if (highwayState.XWpV9Q7()) {
            if (InventoryManager.QFWUbSJ63lmQY || InventoryManager.NZ3iHWsF) {
                return;
            }
            InventoryManager.NrfIVPgqB9 = this.dmmGdE2RN9C.player.getBlockPos();
            PlayerUtils.KP44bk(false);
            InventoryManager.Dzj74FIoxmie();
        }
        if (this.buildMode.get() == BuildMode.e4uKoS) {
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
        if (this.buildMode.get() == BuildMode.flZYoiXwrl) {
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

    private boolean jOdDDFXSeWl4(int n, int n2) {
        HighwayState highwayState = HighwayState.LmpuWjra();
        if (highwayState.OIExXGL6BNv() || highwayState.jIXFBaSwUWYqAc9() || highwayState.XMj1R1A1() || PlayerUtils.BT1BimvycZZjsYS()) {
            return false;
        }
        if (this.pavementBlock.get() != Blocks.field_10540) {
            return false;
        }
        return n2 > 8 && InventoryManager.ZeOLrA() > 0 && n <= 8;
    }

    private void ULOAMKfWE3NZZTj8() {
        assert (this.dmmGdE2RN9C.player != null);
        HighwayState highwayState = HighwayState.LmpuWjra();
        highwayState.Gt56Sj4a6BWhgB(WorldUtils.eQlnaotm4pUDUmJT());
        highwayState.J2pm2c07elEb5G(this.dmmGdE2RN9C.player.getX());
        highwayState.J9PiTNS(this.dmmGdE2RN9C.player.getZ());
        highwayState.jOdDDFXSeWl4((double)this.dmmGdE2RN9C.player.getX() + 0.5);
        highwayState.mp3zoXQFKUKYj5((double)this.dmmGdE2RN9C.player.getZ() + 0.5);
        highwayState.Gt56Sj4a6BWhgB(this.dmmGdE2RN9C.player.getX());
        highwayState.TAdu5cndwWu3A1(this.dmmGdE2RN9C.player.getY());
        highwayState.vgrtgn5(this.dmmGdE2RN9C.player.getZ());
    }

    private void gsYdyKVgv() {
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
            DiscordRPC.EyGoWQcn();
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

    public static void V2mbWoNZftH0t() {
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

    public static boolean l92qSNnpKrYO() {
        List list = (List)((AutoEat)Modules.get().get(AutoEat.class)).blacklist.get();
        for (int i = 0; i < Objects.requireNonNull(MinecraftClient.getInstance().player).getId().field_7547.size(); ++i) {
            ItemStack ItemStack2 = MinecraftClient.getInstance().player.getId().method_5438(i);
            if (ItemStack2.method_57353().method_57832(MutableText.field_50075) && !(ItemStack2.method_57353() instanceof class_2480) && !list.contains(ItemStack2.getStack())) {
                return true;
            }
            if (ItemStack2.getStack() != Items.field_8367 || list.contains(ItemStack2.getStack())) continue;
            return true;
        }
        return false;
    }

    public static void jOdDDFXSeWl4(BlockState BlockState2) {
        HighwayState highwayState = HighwayState.LmpuWjra();
        if (BlockState2.getBlock() == Blocks.field_10540) {
            highwayState.orXwdS7X2l();
        }
        if (BlockState2.getBlock() == Blocks.field_10515) {
            highwayState.qMP0ctta2esan3W();
        }
        if (BlockState2.getBlock() == Blocks.field_10443) {
            highwayState.J9PESj();
        }
    }

    public static boolean S7TLszvzENsW7() {
        return ((AutoEat)Modules.get().get(AutoEat.class)).eating || ((AutoGap)Modules.get().get(AutoGap.class)).isEating();
    }

    public static boolean zl2vxyh() {
        return ((KillAura)Modules.get().get(KillAura.class)).attacking;
    }

    public static WorldUtils.Direction8 kLIvClyeu() {
        return HighwayState.LmpuWjra().P7WK4vInkqbLg();
    }

    public static boolean IuR8CfqY() {
        return EchestFarmer.Nr0B0YDZRAaA.isActive();
    }

    public static BuildMode cghz8iox35K() {
        return (BuildMode)((Object)HighwayBuilder.INSTANCE.buildMode.get());
    }

    public static Mode CcyVC0KkRVrmqA() {
        return (Mode)((Object)HighwayBuilder.INSTANCE.mode.get());
    }

    public static HighwayType Nr0B0YDZRAaA() {
        return (HighwayType)((Object)HighwayBuilder.INSTANCE.highwayType.get());
    }

    public static int oq3TU4VRVWuh() {
        return (Integer)HighwayBuilder.INSTANCE.pavementWidth.get();
    }

    public static boolean CduCWLxmO() {
        return (Boolean)HighwayBuilder.INSTANCE.removeBlocksAboveRails.get();
    }

    public static boolean aYWh0ZNA4Rd() {
        return (Boolean)HighwayBuilder.INSTANCE.replaceCryingObsidian.get();
    }

    public static boolean oknfyMh() {
        return (Boolean)HighwayBuilder.INSTANCE.pauseOnLag.get();
    }

    public static int J6PuzyzqvmhV() {
        return (Integer)HighwayBuilder.INSTANCE.lagThreshold.get();
    }

    public static Block yaVvWAqooeFn() {
        return (Block)HighwayBuilder.INSTANCE.pavementBlock.get();
    }

    public static boolean fGLoB1zTvFf() {
        return (Boolean)HighwayBuilder.INSTANCE.placeRails.get();
    }

    public static boolean dmmGdE2RN9C() {
        return (Boolean)HighwayBuilder.INSTANCE.placeLeftRails.get();
    }

    public static boolean byNtgqgBf0C() {
        return (Boolean)HighwayBuilder.INSTANCE.placeRightRails.get();
    }

    public static Block e4uKoS() {
        return (Block)HighwayBuilder.INSTANCE.scaffoldBlock.get();
    }

    public static ScaffoldMode flZYoiXwrl() {
        return (ScaffoldMode)((Object)HighwayBuilder.INSTANCE.scaffoldMode.get());
    }

    public static boolean RzemQrYtv7d0h() {
        return (Boolean)HighwayBuilder.INSTANCE.scaffoldLeftRail.get();
    }

    public static boolean b76P5ieurZIX() {
        return (Boolean)HighwayBuilder.INSTANCE.scaffoldRightRail.get();
    }

    public static boolean xpLMsAtAuXAx() {
        return (Boolean)HighwayBuilder.INSTANCE.removeAnnoyingLava.get();
    }

    public static boolean ydklDMif6Ghqm0() {
        return (Boolean)HighwayBuilder.INSTANCE.fillCeiling.get();
    }

    public static boolean jll9Iyc1Ftxi() {
        return (Boolean)HighwayBuilder.INSTANCE.allowItemRestocking.get();
    }

    public static boolean OUSKA4lEld() {
        return (Boolean)HighwayBuilder.INSTANCE.storeBrokenPickaxes.get();
    }

    public static boolean dEyMylfkRxcem4F() {
        return (Boolean)HighwayBuilder.INSTANCE.allowShulkerRestocking.get();
    }

    public static boolean oGrnfoe87ZeN() {
        return (Boolean)HighwayBuilder.INSTANCE.allowEchestShulkerRestocking.get();
    }

    public static boolean nQgi06() {
        return (Boolean)HighwayBuilder.INSTANCE.allowToolShulkerRestocking.get();
    }

    @EventHandler
    private void onRender3D(Render3DEvent render3DEvent) {
        HighwayState highwayState = HighwayState.LmpuWjra();
        assert (this.dmmGdE2RN9C.player != null);
        if (highwayState.P7WK4vInkqbLg() == null) {
            return;
        }
        if (HighwayState.LmpuWjra().Icks58Pk4vQH3() == null || HighwayState.LmpuWjra().KaWPzeyl1xVKHWo() == null || HighwayState.LmpuWjra().A02ApsqZGj() == null) {
            return;
        }
        if (highwayState.JDwgf5() == null || highwayState.yyKeW1d7hG() == null) {
            return;
        }
        if (((Boolean)MusheorSystem.Manager.placeRender.get()).booleanValue()) {
            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>(List.of());
            for (BlockPos BlockPos2 : BlockPositions.jOdDDFXSeWl4(2, 3, HighwayBuilder.dmmGdE2RN9C(), HighwayBuilder.byNtgqgBf0C())) {
                if (!BlockUtils.canPlace((BlockPos)BlockPos2, (boolean)true)) continue;
                arrayList.add(BlockPos2);
            }
            ArrayList arrayList2 = new ArrayList(List.of());
            for (BlockPos BlockPos3 : BlockPositions.Gt56Sj4a6BWhgB(2, 2, HighwayBuilder.dmmGdE2RN9C(), HighwayBuilder.byNtgqgBf0C())) {
                if (!BlockUtils.canPlace((BlockPos)BlockPos3, (boolean)true)) continue;
                arrayList2.add(BlockPos3);
            }
            if (this.buildMode.get() == BuildMode.e4uKoS) {
                if (this.highwayType.get() == HighwayType.b76P5ieurZIX) {
                    RenderUtils.jOdDDFXSeWl4(render3DEvent, arrayList, HighwayBuilder.yaVvWAqooeFn());
                }
                if (this.highwayType.get() == HighwayType.xpLMsAtAuXAx) {
                    RenderUtils.jOdDDFXSeWl4(render3DEvent, arrayList2, HighwayBuilder.yaVvWAqooeFn());
                }
            }
        }
    }

    public static final class BuildMode
    extends Enum<BuildMode> {
        public static final /* enum */ BuildMode e4uKoS = new BuildMode();
        public static final /* enum */ BuildMode flZYoiXwrl = new BuildMode();
        private static final /* synthetic */ BuildMode[] RzemQrYtv7d0h;

        public static BuildMode[] values() {
            return (BuildMode[])RzemQrYtv7d0h.clone();
        }

        public static BuildMode valueOf(String string) {
            return Enum.valueOf(BuildMode.class, string);
        }

        private static /* synthetic */ BuildMode[] aLormWyi9q() {
            return new BuildMode[]{e4uKoS, flZYoiXwrl};
        }

        static {
            RzemQrYtv7d0h = BuildMode.aLormWyi9q();
        }
    }

    public static final class Mode
    extends Enum<Mode> {
        public static final /* enum */ Mode jll9Iyc1Ftxi = new Mode();
        public static final /* enum */ Mode OUSKA4lEld = new Mode();
        private static final /* synthetic */ Mode[] dEyMylfkRxcem4F;

        public static Mode[] values() {
            return (Mode[])dEyMylfkRxcem4F.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private static /* synthetic */ Mode[] MJaOMSip8bg() {
            return new Mode[]{jll9Iyc1Ftxi, OUSKA4lEld};
        }

        static {
            dEyMylfkRxcem4F = Mode.MJaOMSip8bg();
        }
    }

    public static final class HighwayType
    extends Enum<HighwayType> {
        public static final /* enum */ HighwayType b76P5ieurZIX = new HighwayType();
        public static final /* enum */ HighwayType xpLMsAtAuXAx = new HighwayType();
        private static final /* synthetic */ HighwayType[] ydklDMif6Ghqm0;

        public static HighwayType[] values() {
            return (HighwayType[])ydklDMif6Ghqm0.clone();
        }

        public static HighwayType valueOf(String string) {
            return Enum.valueOf(HighwayType.class, string);
        }

        private static /* synthetic */ HighwayType[] HMBsFFw4lHD() {
            return new HighwayType[]{b76P5ieurZIX, xpLMsAtAuXAx};
        }

        static {
            ydklDMif6Ghqm0 = HighwayType.HMBsFFw4lHD();
        }
    }

    public static final class ScaffoldMode
    extends Enum<ScaffoldMode> {
        public static final /* enum */ ScaffoldMode oGrnfoe87ZeN = new ScaffoldMode();
        public static final /* enum */ ScaffoldMode nQgi06 = new ScaffoldMode();
        public static final /* enum */ ScaffoldMode aLormWyi9q = new ScaffoldMode();
        private static final /* synthetic */ ScaffoldMode[] HMBsFFw4lHD;

        public static ScaffoldMode[] values() {
            return (ScaffoldMode[])HMBsFFw4lHD.clone();
        }

        public static ScaffoldMode valueOf(String string) {
            return Enum.valueOf(ScaffoldMode.class, string);
        }

        private static /* synthetic */ ScaffoldMode[] ofyUrdkXD264() {
            return new ScaffoldMode[]{oGrnfoe87ZeN, nQgi06, aLormWyi9q};
        }

        static {
            HMBsFFw4lHD = ScaffoldMode.ofyUrdkXD264();
        }
    }
}

