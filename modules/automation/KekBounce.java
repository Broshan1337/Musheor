// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.VersionHelper;
import musheor.compat.XearoHelper;
import musheor.modules.automation.highway.HighwayNavigator;
import musheor.modules.automation.highway.HighwayRouter;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.WorldUtils;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.HighwayNetworkManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.PlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.BlockPos;
import net.minecraft.PlayerAbilities;
import net.minecraft.Vec3d;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class KekBounce
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRepair;
    private final SettingGroup sgNavigate;
    private final Setting<Mode> mode;
    private final Setting<Boolean> yMotion;
    private final Setting<Double> speed;
    private final Setting<Integer> yMotionDelay;
    private final Setting<Boolean> lockPitch;
    private final Setting<Double> pitch;
    private final Setting<Boolean> simpleObstaclePasser;
    private final Setting<StartPos> startPosType;
    private final Setting<BlockPos> startPos;
    private final Setting<Integer> distance;
    private final Setting<Integer> targetY;
    private final Setting<Boolean> toggleElytra;
    private final Setting<Boolean> showEta;
    private final Setting<Boolean> renderPathOnMap;
    private final Setting<Double> approachRadius;
    private final Setting<Double> arriveRadius;
    private final Setting<Integer> alignTimeoutTicks;
    private final Setting<Double> yawToleranceDeg;
    private final Setting<Integer> transitionGoalDist;
    private final Setting<Integer> settleTicks;
    private final Setting<Boolean> repairElytra;
    private final Setting<Integer> elytraMinThresholdPercent;
    private final Setting<Integer> elytraMaxThresholdPercent;
    private final Setting<String> server;
    private WorldUtils.Direction8 currentDirection;
    private int stuckTicks;
    boolean isPathing;
    private boolean isRepairing;
    private HighwayNavigator navigator;
    XearoHelper.WaypointData currentWaypoint;
    private int lastWaypointCount;
    private int bounceTimerTicks;
    private BlockPos alignPos;
    private Vec3d lastPos;

    public KekBounce() {
        super(musheor.AUTOMATION, "kek-bounce", "Elytra bounce module with some extra juice.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRepair = this.settings.createGroup("Auto-Repair");
        this.sgNavigate = this.settings.createGroup("2bNav");
        this.mode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Simple: freeform bounce. Navigate: Set and follows temporary waypoints created using Xearo")).defaultValue((Object)Mode.Simple)).build());
        this.yMotion = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("y-motion")).description("Zoom really fast by cancelling your vertical momentum")).defaultValue((Object)false)).visible(() -> this.mode.get() == Mode.Simple)).build());
        this.speed = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).description("The speed in blocks per second to keep you at.")).defaultValue(100.0).sliderRange(20.0, 250.0).visible(() -> this.yMotion.get())).build());
        this.yMotionDelay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("y-motion-delay")).description("Seconds to bounce normally before y-motion kicks in. Resets when the obstacle passer intervenes.")).defaultValue((Object)5)).sliderRange(1, 30).visible(() -> this.yMotion.get())).build());
        this.lockPitch = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lock-pitch")).description("Whether to lock your pitch when bouncing.")).defaultValue((Object)true)).build());
        this.pitch = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pitch")).description("Degrees of pitch to lock to")).defaultValue(72.4).sliderRange(-90.0, 90.0).decimalPlaces(2).visible(() -> this.lockPitch.get())).build());
        this.simpleObstaclePasser = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("simple-obstacle-passer")).description("Uses baritone to pass obstacles and aligns you to your starting position when enabling the module")).defaultValue((Object)false)).build());
        this.startPosType = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("start-pos-type")).description("Automatic or custom starting position for aligning with obstacle-passer")).defaultValue((Object)StartPos.Automatic)).visible(() -> this.mode.get() == Mode.Simple && (Boolean)this.simpleObstaclePasser.get() != false)).build());
        this.startPos = this.sgGeneral.add((Setting)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("custom-start-pos")).description("The position to align with when using the custom starting position option.")).defaultValue((Object)new BlockPos(0, 0, 0))).visible(() -> this.mode.get() == Mode.Simple && (Boolean)this.simpleObstaclePasser.get() != false && this.startPosType.get() == StartPos.Custom)).onChanged(BlockPos2 -> {
            this.alignPos = BlockPos2;
        })).build());
        this.distance = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("distance")).description("The distance to set the baritone goal for path realignment.")).defaultValue((Object)8)).visible(() -> this.simpleObstaclePasser.get())).build());
        this.targetY = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("y-level")).description("The Y level to bounce at.")).defaultValue((Object)120)).visible(() -> this.simpleObstaclePasser.get())).build());
        this.toggleElytra = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-elytra")).description("Equips an elytra on activate, and a chestplate on deactivate.")).defaultValue((Object)true)).build());
        this.showEta = this.sgNavigate.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-eta")).description("Displays and calculates arrival time above the player's hotbar.")).defaultValue((Object)true)).visible(() -> XearoHelper.isLoaded() && FabricLoader.getInstance().isModLoaded("xaeroplus") && this.mode.get() == Mode.Navigate)).build());
        this.renderPathOnMap = this.sgNavigate.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-path-on-map")).description("Displays your current navigation path on your minimap / worldmap.")).defaultValue((Object)true)).visible(() -> XearoHelper.isLoaded() && FabricLoader.getInstance().isModLoaded("xaeroplus") && this.mode.get() == Mode.Navigate)).build());
        this.approachRadius = this.sgNavigate.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("approach-radius")).description("Start slowing down when within this many blocks of a waypoint.")).defaultValue(64.0).sliderRange(16.0, 256.0).decimalPlaces(0).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.Navigate)).build());
        this.arriveRadius = this.sgNavigate.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("arrive-radius")).description("Consider a waypoint reached when within this many blocks.")).defaultValue(32.0).sliderRange(8.0, 128.0).decimalPlaces(0).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.Navigate)).build());
        this.alignTimeoutTicks = this.sgNavigate.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("align-timeout")).description("Ticks to spend aligning yaw before bouncing anyway.")).defaultValue((Object)40)).sliderRange(10, 100).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.Navigate)).build());
        this.yawToleranceDeg = this.sgNavigate.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("yaw-tolerance")).description("Degrees of yaw error considered close enough to start bouncing.")).defaultValue(4.0).sliderRange(1.0, 20.0).decimalPlaces(1).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.Navigate)).build());
        this.transitionGoalDist = this.sgNavigate.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("transition-distance")).description("Baritone goal distance when walking an intersection.")).defaultValue((Object)8)).sliderRange(1, 32).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.Navigate)).build());
        this.settleTicks = this.sgNavigate.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("settle-ticks")).description("Ticks to wait at an intersection after arriving before resuming bounce.")).defaultValue((Object)10)).sliderRange(0, 40).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.Navigate)).build());
        this.repairElytra = this.sgRepair.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("repair-elytra")).description("Uses experience bottles to repair your elytra.")).defaultValue((Object)false)).build());
        this.elytraMinThresholdPercent = this.sgRepair.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("damage-threshold-%")).description("At what durability percentage should it start repairing the elytra.")).defaultValue((Object)20)).sliderRange(0, 100).visible(() -> this.repairElytra.get())).build());
        this.elytraMaxThresholdPercent = this.sgRepair.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("repair-threshold-%")).description("At what durability percentage should it stop repairing the elytra.")).defaultValue((Object)80)).sliderRange(0, 100).visible(() -> this.repairElytra.get())).build());
        this.server = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("server")).description("Which server's highway network to use. Must match a key in the remote config (e.g. \"2b2t\").")).defaultValue((Object)"2b2t")).onChanged(string -> {
            HighwayNetworkManager highwayNetworkManager = HighwayNetworkManager.getInstance();
            if (!highwayNetworkManager.isLoaded()) {
                this.warning("Highway configs are still loading \u2014 server key will be validated once loaded.", new Object[0]);
                return;
            }
            if (!highwayNetworkManager.getServerNames().contains(string)) {
                this.error("Unknown server \"(highlight)%s(default)\". Known servers: (highlight)%s(default).", new Object[]{string, String.join((CharSequence)", ", highwayNetworkManager.getServerNames())});
            }
        })).visible(() -> false)).build());
        this.navigator = null;
        this.currentWaypoint = null;
        this.lastWaypointCount = -1;
    }

    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        this.currentDirection = WorldUtils.getPlayerFacing();
        this.stuckTicks = 0;
        this.isPathing = false;
        this.isRepairing = false;
        this.bounceTimerTicks = 0;
        this.alignPos = this.startPosType.get() == StartPos.Automatic ? BlockPos.ofFloored((PlayerAbilities)VersionHelper.get().getPlayerPos()) : (BlockPos)this.startPos.get();
        this.lastPos = VersionHelper.get().getPlayerPos();
        if (((Boolean)this.toggleElytra.get()).booleanValue() && !this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getStack().equals(Items.ELYTRA)) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
        }
        if (this.mode.get() == Mode.Navigate) {
            if (((Boolean)this.renderPathOnMap.get()).booleanValue() && !FabricLoader.getInstance().isModLoaded("xaeroplus")) {
                this.warning("Cannot render path on map, %sXaeroPlus %srequired but not installed!", new Object[]{Formatting.YELLOW, Formatting.GRAY});
                this.renderPathOnMap.set((Object)false);
            }
            if (((Boolean)this.showEta.get()).booleanValue() && !FabricLoader.getInstance().isModLoaded("xaeroplus")) {
                this.warning("Cannot show ETA, %sXaeroPlus %srequired but not installed!", new Object[]{Formatting.YELLOW, Formatting.GRAY});
                this.showEta.set((Object)false);
            }
            if (!XearoHelper.isLoaded()) {
                this.warning("AdvancedNavigation requires %sXaero%s. Install it or switch to Simple mode.", new Object[]{Formatting.RED, Formatting.GRAY});
                this.toggle();
                return;
            }
            this.initNavigateMode();
        } else {
            this.resetState();
        }
    }

    public void onDeactivate() {
        if (this.navigator != null) {
            this.navigator.stop();
            this.navigator = null;
        }
        this.currentWaypoint = null;
        this.lastWaypointCount = -1;
        if (XearoHelper.isLoaded()) {
            XearoHelper.get().clearLinesOnMap();
        }
        if (((Boolean)this.toggleElytra.get()).booleanValue()) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
        }
        if (PathingHelper.isAlreadyPathing()) {
            PathingHelper.stopPathing();
        }
    }

    private void resetState() {
        this.currentDirection = WorldUtils.getPlayerFacing();
        this.stuckTicks = 0;
        this.isPathing = false;
        this.isRepairing = false;
    }

    private void initNavigateMode() {
        XearoHelper.WaypointData waypointData = XearoHelper.get().getOldestWaypoint(true);
        if (waypointData == null) {
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + "No temp waypoints found!"), (Object[])new Object[0]);
            this.toggle();
            return;
        }
        this.navigateTo(waypointData);
    }

    void navigateTo(XearoHelper.WaypointData waypointData) {
        this.currentWaypoint = waypointData;
        double d = this.mc.player.getX();
        double d2 = this.mc.player.getZ();
        double d3 = waypointData.x();
        double d4 = waypointData.z();
        ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + "Navigating to waypoint: " + String.valueOf(Formatting.GRAY) + waypointData.name() + String.valueOf(Formatting.GRAY) + " (" + (int)d3 + ", " + (int)d4 + ")"), (Object[])new Object[0]);
        HighwayRouter.Route route = HighwayRouter.findRoute(d, d2, d3, d4);
        if (route == null) {
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + "No highway route found to waypoint: " + waypointData.name()), (Object[])new Object[0]);
            this.toggle();
            return;
        }
        this.drawRouteOnMap(route);
        HighwayNavigator.Config config = new HighwayNavigator.Config((Double)this.approachRadius.get(), (Double)this.arriveRadius.get(), (Integer)this.alignTimeoutTicks.get(), ((Double)this.yawToleranceDeg.get()).floatValue(), (Integer)this.transitionGoalDist.get(), (Integer)this.settleTicks.get());
        this.navigator = new HighwayNavigator(route, config, new HighwayNavigator.BounceController(){

            @Override
            public void setBouncing(boolean bl) {
                KekBounce.this.isPathing = !bl;
            }
        }, new HighwayNavigator.Listener(){

            @Override
            public void onStateChange(HighwayNavigator.State state, String string) {
                ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + string), (Object[])new Object[0]);
            }

            @Override
            public void onWaypointReached() {
                XearoHelper.get().deleteCurrentWaypoint(KekBounce.this.currentWaypoint);
                KekBounce.this.currentWaypoint = null;
                XearoHelper.WaypointData waypointData = XearoHelper.get().getOldestWaypoint(true);
                if (waypointData == null) {
                    ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + "No waypoints found, assuming destination was reached!"), (Object[])new Object[0]);
                    KekBounce.this.toggle();
                    return;
                }
                KekBounce.this.navigateTo(waypointData);
            }
        });
    }

    @EventHandler
    private void playerMoveEvent(PlayerMoveEvent playerMoveEvent) {
        if (this.mc.player == null || playerMoveEvent.type != MovementType.SELF || !this.canBounce() || !((Boolean)this.yMotion.get()).booleanValue() || this.mode.get() == Mode.Navigate) {
            return;
        }
        double d = VersionHelper.get().getPlayerPos().subtract(this.lastPos).multiply(20.0, 0.0, 20.0).horizontalLength();
        Timer timer = (Timer)Modules.get().get(Timer.class);
        if (timer.isActive()) {
            d *= timer.getMultiplier();
        }
        if (this.mc.player.isOnGround() && this.mc.player.isSprinting() && this.bounceTimerTicks >= (Integer)this.yMotionDelay.get() * 20 && d < (Double)this.speed.get()) {
            ((IVec3d)playerMoveEvent.movement).meteor$setY(0.0);
            this.mc.player.setVelocity(this.mc.player.getVelocity().x, 0.0, this.mc.player.getVelocity().z);
        }
        this.lastPos = VersionHelper.get().getPlayerPos();
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.mode.get() == Mode.Navigate) {
            this.tickNavigateMode();
        } else {
            this.tickSimpleMode();
        }
    }

    private void tickNavigateMode() {
        if (this.isRepairing) {
            this.tickRepairElytra();
            return;
        }
        if (this.shouldRepairElytra()) {
            this.isRepairing = true;
            return;
        }
        if (this.mode.get() == Mode.Navigate && ((Boolean)this.renderPathOnMap.get()).booleanValue()) {
            this.refreshMapPath();
        }
        if (((Boolean)this.showEta.get()).booleanValue()) {
            this.updateEtaDisplay();
        }
        if (this.navigator == null) {
            return;
        }
        if (((Boolean)this.simpleObstaclePasser.get()).booleanValue() && this.navigator.getState() == HighwayNavigator.State.GLaGIbduHdrl) {
            this.tickObstaclePasser();
        }
        this.navigator.tick();
        if (this.canBounce()) {
            this.mc.player.setSprinting(true);
            if (((Boolean)this.lockPitch.get()).booleanValue() && !PathingHelper.isAlreadyPathing()) {
                this.mc.player.setPitch(((Double)this.pitch.get()).floatValue());
            }
            if (this.mc.player.isOnGround() && !this.isPathing) {
                this.mc.player.startFallFlying();
            }
            this.sendJumpPacket();
        }
    }

    private void tickSimpleMode() {
        if (this.canBounce()) {
            this.mc.player.setSprinting(true);
        }
        if (((Boolean)this.simpleObstaclePasser.get()).booleanValue()) {
            this.tickObstaclePasser();
        }
        if (!this.isPathing && !PathingHelper.isAlreadyPathing()) {
            ++this.bounceTimerTicks;
        }
        if (this.isRepairing) {
            this.tickRepairElytra();
            return;
        }
        if (this.shouldRepairElytra()) {
            this.isRepairing = true;
            return;
        }
        if (((Boolean)this.lockPitch.get()).booleanValue() && !PathingHelper.isAlreadyPathing()) {
            this.mc.player.setPitch(((Double)this.pitch.get()).floatValue());
        }
        if (this.mc.player.isOnGround() && !this.isPathing && !PathingHelper.isAlreadyPathing()) {
            this.mc.player.startFallFlying();
        }
        if (this.canBounce()) {
            this.sendJumpPacket();
        }
    }

    private void tickObstaclePasser() {
        boolean bl;
        if (PathingHelper.isAlreadyPathing()) {
            return;
        }
        if (this.isPathing) {
            this.isPathing = false;
            this.stuckTicks = 0;
            return;
        }
        boolean bl2 = bl = this.mode.get() != Mode.Navigate && (VersionHelper.get().getPlayerPos().getY() < (double)((Integer)this.targetY.get()).intValue() || VersionHelper.get().getPlayerPos().getY() > (double)((Integer)this.targetY.get() + 2));
        if (bl || this.mc.player.isFallFlying && !this.mc.player.isOnGround()) {
            this.isPathing = true;
            this.bounceTimerTicks = 0;
            PathingHelper.setGoal(this.getAlignGoalPos((Integer)this.distance.get()));
        } else if (this.isStuck()) {
            if (++this.stuckTicks > 20) {
                this.isPathing = true;
                this.stuckTicks = 0;
                this.bounceTimerTicks = 0;
                PathingHelper.setGoal(this.getAlignGoalPos((Integer)this.distance.get()));
            }
        } else {
            this.stuckTicks = 0;
            this.isPathing = false;
        }
    }

    private boolean isStuck() {
        if (this.mc.player == null) {
            return false;
        }
        Vec3d Vec3d2 = this.mc.player.getVelocity();
        return Math.sqrt(Vec3d2.x * Vec3d2.x + Vec3d2.z * Vec3d2.z) < 0.2;
    }

    private float getElytraDurabilityPercent() {
        ItemStack ItemStack2 = this.mc.player.getEquippedStack(EquipmentSlot.CHEST);
        return (1.0f - (float)ItemStack2.getDamage() / (float)ItemStack2.getMaxDamage()) * 100.0f;
    }

    private boolean shouldRepairElytra() {
        if (!((Boolean)this.repairElytra.get()).booleanValue()) {
            return false;
        }
        if (!this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getStack().equals(Items.ELYTRA)) {
            return false;
        }
        if (((AutoEat)Modules.get().get(AutoEat.class)).eating) {
            return false;
        }
        return this.getElytraDurabilityPercent() <= (float)((Integer)this.elytraMinThresholdPercent.get()).intValue();
    }

    private void tickRepairElytra() {
        if (((AutoEat)Modules.get().get(AutoEat.class)).eating) {
            return;
        }
        if (this.getElytraDurabilityPercent() >= (float)((Integer)this.elytraMaxThresholdPercent.get()).intValue()) {
            this.isRepairing = false;
            return;
        }
        int n = InventoryManager.findItemSlot(Items.EXPERIENCE_BOTTLE);
        if (n == -1) {
            ChatUtils.info((String)"%sNo experience bottles found, waiting ...", (Object[])new Object[]{Formatting.YELLOW});
            return;
        }
        if (n > 8) {
            InventoryManager.moveItemToHotbar(Items.EXPERIENCE_BOTTLE);
            return;
        }
        this.mc.player.getInventory().selectedSlot = n;
        this.mc.player.setPitch(90.0f);
        this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
    }

    private BlockPos getAlignGoalPos(int n) {
        int n2 = this.mc.player.getX();
        int n3 = (Integer)this.targetY.get();
        int n4 = this.mc.player.getZ();
        if (this.mode.get() == Mode.Navigate) {
            return switch (this.currentDirection) {
                default -> throw new MatchException(null, null);
                case WorldUtils.Direction8.NORTH -> new BlockPos(n2, n3, n4 - n);
                case WorldUtils.Direction8.SOUTH -> new BlockPos(n2, n3, n4 + n);
                case WorldUtils.Direction8.EAST -> new BlockPos(n2 + n, n3, n4);
                case WorldUtils.Direction8.WEST -> new BlockPos(n2 - n, n3, n4);
                case WorldUtils.Direction8.NORTH_EAST -> new BlockPos(n2 + n, n3, n4 - n);
                case WorldUtils.Direction8.NORTH_WEST -> new BlockPos(n2 - n, n3, n4 - n);
                case WorldUtils.Direction8.SOUTH_EAST -> new BlockPos(n2 + n, n3, n4 + n);
                case WorldUtils.Direction8.SOUTH_WEST -> new BlockPos(n2 - n, n3, n4 + n);
            };
        }
        int n5 = this.alignPos.getX();
        int n6 = this.alignPos.getZ();
        return switch (this.currentDirection) {
            default -> throw new MatchException(null, null);
            case WorldUtils.Direction8.NORTH -> new BlockPos(n5, n3, n4 - n);
            case WorldUtils.Direction8.SOUTH -> new BlockPos(n5, n3, n4 + n);
            case WorldUtils.Direction8.EAST -> new BlockPos(n2 + n, n3, n6);
            case WorldUtils.Direction8.WEST -> new BlockPos(n2 - n, n3, n6);
            case WorldUtils.Direction8.NORTH_EAST -> {
                int var7_7 = n2 + n;
                yield new BlockPos(var7_7, n3, n5 + n6 - var7_7);
            }
            case WorldUtils.Direction8.NORTH_WEST -> {
                int var7_8 = n2 - n;
                yield new BlockPos(var7_8, n3, var7_8 - (n5 - n6));
            }
            case WorldUtils.Direction8.SOUTH_EAST -> {
                int var7_9 = n2 + n;
                yield new BlockPos(var7_9, n3, var7_9 - (n5 - n6));
            }
            case WorldUtils.Direction8.SOUTH_WEST -> {
                int var7_10 = n2 - n;
                yield new BlockPos(var7_10, n3, n5 + n6 - var7_10);
            }
        };
    }

    private void drawRouteOnMap(HighwayRouter.Route route) {
        if (!XearoHelper.isLoaded()) {
            return;
        }
        ArrayList<XearoHelper.LineData> arrayList = new ArrayList<XearoHelper.LineData>();
        List<WorldUtils.Vec2d> list = route.waypoints;
        int n = 0;
        while (n + 1 < list.size()) {
            arrayList.add(new XearoHelper.LineData((int)list.get(n).x(), (int)list.get(n).z(), (int)list.get(n + 1).x(), (int)list.get(n + 1).z()));
            ++n;
        }
        XearoHelper.get().drawLinesOnMap(arrayList, -16733441);
    }

    private void refreshMapPath() {
        if (!XearoHelper.isLoaded()) {
            return;
        }
        List<XearoHelper.WaypointData> list = XearoHelper.get().getWaypoints(true);
        if (list == null) {
            return;
        }
        if (list.size() == this.lastWaypointCount) {
            return;
        }
        this.lastWaypointCount = list.size();
        ArrayList<XearoHelper.WaypointData> arrayList = new ArrayList<XearoHelper.WaypointData>(list);
        arrayList.sort(Comparator.comparingLong(XearoHelper.WaypointData::createdAt));
        ArrayList<XearoHelper.LineData> arrayList2 = new ArrayList<XearoHelper.LineData>();
        double d = this.mc.player.getX();
        double d2 = this.mc.player.getZ();
        for (XearoHelper.WaypointData waypointData : arrayList) {
            HighwayRouter.Route route = HighwayRouter.findRoute(d, d2, waypointData.x(), waypointData.z());
            if (route != null) {
                List<WorldUtils.Vec2d> list2 = route.waypoints;
                int n = 0;
                while (n + 1 < list2.size()) {
                    arrayList2.add(new XearoHelper.LineData((int)list2.get(n).x(), (int)list2.get(n).z(), (int)list2.get(n + 1).x(), (int)list2.get(n + 1).z()));
                    ++n;
                }
            }
            d = waypointData.x();
            d2 = waypointData.z();
        }
        XearoHelper.get().drawLinesOnMap(arrayList2, -16733441);
    }

    private void updateEtaDisplay() {
        if (this.currentWaypoint == null) {
            return;
        }
        String string = XearoHelper.get().getEtaSuffix(this.currentWaypoint);
        if (string == null || string.isBlank()) {
            string = "...";
        }
        this.mc.player.sendMessage((MutableText)Text.literal("[2bNav] ").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)).append((MutableText)Text.literal("ETA ").setStyle(Style.EMPTY.withColor(Formatting.AQUA))).append((MutableText)Text.literal(string).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(true))), true);
    }

    public boolean canBounce() {
        return this.isActive() && !this.isPathing && !this.isRepairing && !PathingHelper.isAlreadyPathing() && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getStack().equals(Items.ELYTRA);
    }

    private void sendJumpPacket() {
        if (this.mc.player == null) {
            return;
        }
        this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
    }

    public static final class Mode
    extends Enum<Mode> {
        public static final /* enum */ Mode Simple = new Mode();
        public static final /* enum */ Mode Navigate = new Mode();
        private static final /* synthetic */ Mode[] $VALUES;

        public static Mode[] values() {
            return (Mode[])$VALUES.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private static /* synthetic */ Mode[] $init() {
            return new Mode[]{dOw8Pbbaj, v1nokUkHXYjAGxn};
        }

        static {
            $VALUES = Mode.$init();
        }
    }

    static final class StartPos
    extends Enum<StartPos> {
        public static final /* enum */ StartPos Automatic = new StartPos();
        public static final /* enum */ StartPos Custom = new StartPos();
        private static final /* synthetic */ StartPos[] $VALUES;

        public static StartPos[] values() {
            return (StartPos[])$VALUES.clone();
        }

        public static StartPos valueOf(String string) {
            return Enum.valueOf(StartPos.class, string);
        }

        private static /* synthetic */ StartPos[] $init() {
            return new StartPos[]{bukSnj0c, HWVVSgN};
        }

        static {
            $VALUES = StartPos.$init();
        }
    }
}

