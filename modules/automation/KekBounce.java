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
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.DamageSource;
import net.minecraft.PlayerInventory;
import net.minecraft.PlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.BlockPos;
import net.minecraft.PlayerAbilities;
import net.minecraft.Vec3d;
import net.minecraft.ScreenHandler;
import net.minecraft.Slot;
import net.minecraft.Packet;
import net.minecraft.EntityTrackerEntry;

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
    private WorldUtils.Direction8 wgso2Z6Z8lq9TnZ;
    private int stuckTicks;
    boolean o6zpkIIG9;
    private boolean kvBwtsHxL8DN;
    private HighwayNavigator nBniX5v;
    XearoHelper.WaypointData Pa3aVwRtUo45jMG;
    private int t018N0;
    private int fBdtt0EoATtS;
    private BlockPos UJeG0VvjslB;
    private Vec3d KMpX0B;

    public KekBounce() {
        super(musheor.AUTOMATION, "kek-bounce", "Elytra bounce module with some extra juice.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRepair = this.settings.createGroup("Auto-Repair");
        this.sgNavigate = this.settings.createGroup("2bNav");
        this.mode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Simple: freeform bounce. Navigate: Set and follows temporary waypoints created using Xearo")).defaultValue((Object)Mode.dOw8Pbbaj)).build());
        this.yMotion = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("y-motion")).description("Zoom really fast by cancelling your vertical momentum")).defaultValue((Object)false)).visible(() -> this.mode.get() == Mode.dOw8Pbbaj)).build());
        this.speed = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).description("The speed in blocks per second to keep you at.")).defaultValue(100.0).sliderRange(20.0, 250.0).visible(() -> this.yMotion.get())).build());
        this.yMotionDelay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("y-motion-delay")).description("Seconds to bounce normally before y-motion kicks in. Resets when the obstacle passer intervenes.")).defaultValue((Object)5)).sliderRange(1, 30).visible(() -> this.yMotion.get())).build());
        this.lockPitch = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lock-pitch")).description("Whether to lock your pitch when bouncing.")).defaultValue((Object)true)).build());
        this.pitch = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pitch")).description("Degrees of pitch to lock to")).defaultValue(72.4).sliderRange(-90.0, 90.0).decimalPlaces(2).visible(() -> this.lockPitch.get())).build());
        this.simpleObstaclePasser = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("simple-obstacle-passer")).description("Uses baritone to pass obstacles and aligns you to your starting position when enabling the module")).defaultValue((Object)false)).build());
        this.startPosType = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("start-pos-type")).description("Automatic or custom starting position for aligning with obstacle-passer")).defaultValue((Object)StartPos.bukSnj0c)).visible(() -> this.mode.get() == Mode.dOw8Pbbaj && (Boolean)this.simpleObstaclePasser.get() != false)).build());
        this.startPos = this.sgGeneral.add((Setting)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("custom-start-pos")).description("The position to align with when using the custom starting position option.")).defaultValue((Object)new BlockPos(0, 0, 0))).visible(() -> this.mode.get() == Mode.dOw8Pbbaj && (Boolean)this.simpleObstaclePasser.get() != false && this.startPosType.get() == StartPos.HWVVSgN)).onChanged(BlockPos2 -> {
            this.UJeG0VvjslB = BlockPos2;
        })).build());
        this.distance = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("distance")).description("The distance to set the baritone goal for path realignment.")).defaultValue((Object)8)).visible(() -> this.simpleObstaclePasser.get())).build());
        this.targetY = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("y-level")).description("The Y level to bounce at.")).defaultValue((Object)120)).visible(() -> this.simpleObstaclePasser.get())).build());
        this.toggleElytra = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-elytra")).description("Equips an elytra on activate, and a chestplate on deactivate.")).defaultValue((Object)true)).build());
        this.showEta = this.sgNavigate.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-eta")).description("Displays and calculates arrival time above the player's hotbar.")).defaultValue((Object)true)).visible(() -> XearoHelper.isLoaded() && FabricLoader.getInstance().isModLoaded("xaeroplus") && this.mode.get() == Mode.v1nokUkHXYjAGxn)).build());
        this.renderPathOnMap = this.sgNavigate.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-path-on-map")).description("Displays your current navigation path on your minimap / worldmap.")).defaultValue((Object)true)).visible(() -> XearoHelper.isLoaded() && FabricLoader.getInstance().isModLoaded("xaeroplus") && this.mode.get() == Mode.v1nokUkHXYjAGxn)).build());
        this.approachRadius = this.sgNavigate.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("approach-radius")).description("Start slowing down when within this many blocks of a waypoint.")).defaultValue(64.0).sliderRange(16.0, 256.0).decimalPlaces(0).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.v1nokUkHXYjAGxn)).build());
        this.arriveRadius = this.sgNavigate.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("arrive-radius")).description("Consider a waypoint reached when within this many blocks.")).defaultValue(32.0).sliderRange(8.0, 128.0).decimalPlaces(0).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.v1nokUkHXYjAGxn)).build());
        this.alignTimeoutTicks = this.sgNavigate.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("align-timeout")).description("Ticks to spend aligning yaw before bouncing anyway.")).defaultValue((Object)40)).sliderRange(10, 100).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.v1nokUkHXYjAGxn)).build());
        this.yawToleranceDeg = this.sgNavigate.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("yaw-tolerance")).description("Degrees of yaw error considered close enough to start bouncing.")).defaultValue(4.0).sliderRange(1.0, 20.0).decimalPlaces(1).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.v1nokUkHXYjAGxn)).build());
        this.transitionGoalDist = this.sgNavigate.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("transition-distance")).description("Baritone goal distance when walking an intersection.")).defaultValue((Object)8)).sliderRange(1, 32).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.v1nokUkHXYjAGxn)).build());
        this.settleTicks = this.sgNavigate.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("settle-ticks")).description("Ticks to wait at an intersection after arriving before resuming bounce.")).defaultValue((Object)10)).sliderRange(0, 40).visible(() -> XearoHelper.isLoaded() && this.mode.get() == Mode.v1nokUkHXYjAGxn)).build());
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
        this.nBniX5v = null;
        this.Pa3aVwRtUo45jMG = null;
        this.t018N0 = -1;
    }

    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        this.wgso2Z6Z8lq9TnZ = WorldUtils.eQlnaotm4pUDUmJT();
        this.stuckTicks = 0;
        this.o6zpkIIG9 = false;
        this.kvBwtsHxL8DN = false;
        this.fBdtt0EoATtS = 0;
        this.UJeG0VvjslB = this.startPosType.get() == StartPos.bukSnj0c ? BlockPos.method_49638((PlayerAbilities)VersionHelper.get().getPlayerPos()) : (BlockPos)this.startPos.get();
        this.KMpX0B = VersionHelper.get().getPlayerPos();
        if (((Boolean)this.toggleElytra.get()).booleanValue() && !this.mc.player.method_6118(DamageSource.field_6174).getStack().equals(Items.field_8833)) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
        }
        if (this.mode.get() == Mode.v1nokUkHXYjAGxn) {
            if (((Boolean)this.renderPathOnMap.get()).booleanValue() && !FabricLoader.getInstance().isModLoaded("xaeroplus")) {
                this.warning("Cannot render path on map, %sXaeroPlus %srequired but not installed!", new Object[]{Formatting.field_1065, Formatting.field_1054});
                this.renderPathOnMap.set((Object)false);
            }
            if (((Boolean)this.showEta.get()).booleanValue() && !FabricLoader.getInstance().isModLoaded("xaeroplus")) {
                this.warning("Cannot show ETA, %sXaeroPlus %srequired but not installed!", new Object[]{Formatting.field_1065, Formatting.field_1054});
                this.showEta.set((Object)false);
            }
            if (!XearoHelper.isLoaded()) {
                this.warning("AdvancedNavigation requires %sXaero%s. Install it or switch to Simple mode.", new Object[]{Formatting.RED, Formatting.field_1054});
                this.toggle();
                return;
            }
            this.pVxy7PIGPRYXRSbk();
        } else {
            this.qLxZ6NFp0a();
        }
    }

    public void onDeactivate() {
        if (this.nBniX5v != null) {
            this.nBniX5v.xRVyNRV3cB7();
            this.nBniX5v = null;
        }
        this.Pa3aVwRtUo45jMG = null;
        this.t018N0 = -1;
        if (XearoHelper.isLoaded()) {
            XearoHelper.get().clearLinesOnMap();
        }
        if (((Boolean)this.toggleElytra.get()).booleanValue()) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
        }
        if (PathingHelper.LcPVM4w5KCoKSxGs()) {
            PathingHelper.xRVyNRV3cB7();
        }
    }

    private void qLxZ6NFp0a() {
        this.wgso2Z6Z8lq9TnZ = WorldUtils.eQlnaotm4pUDUmJT();
        this.stuckTicks = 0;
        this.o6zpkIIG9 = false;
        this.kvBwtsHxL8DN = false;
    }

    private void pVxy7PIGPRYXRSbk() {
        XearoHelper.WaypointData waypointData = XearoHelper.get().getOldestWaypoint(true);
        if (waypointData == null) {
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + "No temp waypoints found!"), (Object[])new Object[0]);
            this.toggle();
            return;
        }
        this.jOdDDFXSeWl4(waypointData);
    }

    void jOdDDFXSeWl4(XearoHelper.WaypointData waypointData) {
        this.Pa3aVwRtUo45jMG = waypointData;
        double d = this.mc.player.getX();
        double d2 = this.mc.player.getZ();
        double d3 = waypointData.x();
        double d4 = waypointData.z();
        ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + "Navigating to waypoint: " + String.valueOf(Formatting.field_1054) + waypointData.name() + String.valueOf(Formatting.GRAY) + " (" + (int)d3 + ", " + (int)d4 + ")"), (Object[])new Object[0]);
        HighwayRouter.Route route = HighwayRouter.jOdDDFXSeWl4(d, d2, d3, d4);
        if (route == null) {
            ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + "No highway route found to waypoint: " + waypointData.name()), (Object[])new Object[0]);
            this.toggle();
            return;
        }
        this.jOdDDFXSeWl4(route);
        HighwayNavigator.Config config = new HighwayNavigator.Config((Double)this.approachRadius.get(), (Double)this.arriveRadius.get(), (Integer)this.alignTimeoutTicks.get(), ((Double)this.yawToleranceDeg.get()).floatValue(), (Integer)this.transitionGoalDist.get(), (Integer)this.settleTicks.get());
        this.nBniX5v = new HighwayNavigator(route, config, new HighwayNavigator.BounceController(){

            @Override
            public void Gt56Sj4a6BWhgB(boolean bl) {
                KekBounce.this.o6zpkIIG9 = !bl;
            }
        }, new HighwayNavigator.Listener(){

            @Override
            public void jOdDDFXSeWl4(HighwayNavigator.State state, String string) {
                ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + string), (Object[])new Object[0]);
            }

            @Override
            public void o6zpkIIG9() {
                XearoHelper.get().deleteCurrentWaypoint(KekBounce.this.Pa3aVwRtUo45jMG);
                KekBounce.this.Pa3aVwRtUo45jMG = null;
                XearoHelper.WaypointData waypointData = XearoHelper.get().getOldestWaypoint(true);
                if (waypointData == null) {
                    ChatUtils.info((String)(String.valueOf(Formatting.YELLOW) + "[2bNav] " + String.valueOf(Formatting.GRAY) + "No waypoints found, assuming destination was reached!"), (Object[])new Object[0]);
                    KekBounce.this.toggle();
                    return;
                }
                KekBounce.this.jOdDDFXSeWl4(waypointData);
            }
        });
    }

    @EventHandler
    private void playerMoveEvent(PlayerMoveEvent playerMoveEvent) {
        if (this.mc.player == null || playerMoveEvent.type != PlayerInventory.field_6308 || !this.jokapWphssQ() || !((Boolean)this.yMotion.get()).booleanValue() || this.mode.get() == Mode.v1nokUkHXYjAGxn) {
            return;
        }
        double d = VersionHelper.get().getPlayerPos().method_1020(this.KMpX0B).method_18805(20.0, 0.0, 20.0).method_1033();
        Timer timer = (Timer)Modules.get().get(Timer.class);
        if (timer.isActive()) {
            d *= timer.getMultiplier();
        }
        if (this.mc.player.method_24828() && this.mc.player.isSprinting() && this.fBdtt0EoATtS >= (Integer)this.yMotionDelay.get() * 20 && d < (Double)this.speed.get()) {
            ((IVec3d)playerMoveEvent.movement).meteor$setY(0.0);
            this.mc.player.method_18800(this.mc.player.method_18798().x, 0.0, this.mc.player.method_18798().z);
        }
        this.KMpX0B = VersionHelper.get().getPlayerPos();
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.mode.get() == Mode.v1nokUkHXYjAGxn) {
            this.gkM0N3m();
        } else {
            this.BoRaO9Zi();
        }
    }

    private void gkM0N3m() {
        if (this.kvBwtsHxL8DN) {
            this.kjzgWev2Gyw8K();
            return;
        }
        if (this.LFK4tb0B()) {
            this.kvBwtsHxL8DN = true;
            return;
        }
        if (this.mode.get() == Mode.v1nokUkHXYjAGxn && ((Boolean)this.renderPathOnMap.get()).booleanValue()) {
            this.rPJDpX();
        }
        if (((Boolean)this.showEta.get()).booleanValue()) {
            this.Hp3MRJIoQ();
        }
        if (this.nBniX5v == null) {
            return;
        }
        if (((Boolean)this.simpleObstaclePasser.get()).booleanValue() && this.nBniX5v.QhaZQGwqdeFQp() == HighwayNavigator.State.GLaGIbduHdrl) {
            this.OpVr2TJWAhubiF2();
        }
        this.nBniX5v.tick();
        if (this.jokapWphssQ()) {
            this.mc.player.method_5728(true);
            if (((Boolean)this.lockPitch.get()).booleanValue() && !PathingHelper.LcPVM4w5KCoKSxGs()) {
                this.mc.player.method_36457(((Double)this.pitch.get()).floatValue());
            }
            if (this.mc.player.method_24828() && !this.o6zpkIIG9) {
                this.mc.player.method_6043();
            }
            this.wgso2Z6Z8lq9TnZ();
        }
    }

    private void BoRaO9Zi() {
        if (this.jokapWphssQ()) {
            this.mc.player.method_5728(true);
        }
        if (((Boolean)this.simpleObstaclePasser.get()).booleanValue()) {
            this.OpVr2TJWAhubiF2();
        }
        if (!this.o6zpkIIG9 && !PathingHelper.LcPVM4w5KCoKSxGs()) {
            ++this.fBdtt0EoATtS;
        }
        if (this.kvBwtsHxL8DN) {
            this.kjzgWev2Gyw8K();
            return;
        }
        if (this.LFK4tb0B()) {
            this.kvBwtsHxL8DN = true;
            return;
        }
        if (((Boolean)this.lockPitch.get()).booleanValue() && !PathingHelper.LcPVM4w5KCoKSxGs()) {
            this.mc.player.method_36457(((Double)this.pitch.get()).floatValue());
        }
        if (this.mc.player.method_24828() && !this.o6zpkIIG9 && !PathingHelper.LcPVM4w5KCoKSxGs()) {
            this.mc.player.method_6043();
        }
        if (this.jokapWphssQ()) {
            this.wgso2Z6Z8lq9TnZ();
        }
    }

    private void OpVr2TJWAhubiF2() {
        boolean bl;
        if (PathingHelper.LcPVM4w5KCoKSxGs()) {
            return;
        }
        if (this.o6zpkIIG9) {
            this.o6zpkIIG9 = false;
            this.stuckTicks = 0;
            return;
        }
        boolean bl2 = bl = this.mode.get() != Mode.v1nokUkHXYjAGxn && (VersionHelper.get().getPlayerPos().method_10214() < (double)((Integer)this.targetY.get()).intValue() || VersionHelper.get().getPlayerPos().method_10214() > (double)((Integer)this.targetY.get() + 2));
        if (bl || this.mc.player.field_5976 && !this.mc.player.field_34927) {
            this.o6zpkIIG9 = true;
            this.fBdtt0EoATtS = 0;
            PathingHelper.l92qSNnpKrYO(this.Gt56Sj4a6BWhgB((Integer)this.distance.get()));
        } else if (this.RG4EUBK1NAGPn74()) {
            if (++this.stuckTicks > 20) {
                this.o6zpkIIG9 = true;
                this.stuckTicks = 0;
                this.fBdtt0EoATtS = 0;
                PathingHelper.l92qSNnpKrYO(this.Gt56Sj4a6BWhgB((Integer)this.distance.get()));
            }
        } else {
            this.stuckTicks = 0;
            this.o6zpkIIG9 = false;
        }
    }

    private boolean RG4EUBK1NAGPn74() {
        if (this.mc.player == null) {
            return false;
        }
        Vec3d Vec3d2 = this.mc.player.method_18798();
        return Math.sqrt(Vec3d2.x * Vec3d2.x + Vec3d2.z * Vec3d2.z) < 0.2;
    }

    private float ydtYMNpam8iL7Z8() {
        ItemStack ItemStack2 = this.mc.player.method_6118(DamageSource.field_6174);
        return (1.0f - (float)ItemStack2.method_7919() / (float)ItemStack2.method_7936()) * 100.0f;
    }

    private boolean LFK4tb0B() {
        if (!((Boolean)this.repairElytra.get()).booleanValue()) {
            return false;
        }
        if (!this.mc.player.method_6118(DamageSource.field_6174).getStack().equals(Items.field_8833)) {
            return false;
        }
        if (((AutoEat)Modules.get().get(AutoEat.class)).eating) {
            return false;
        }
        return this.ydtYMNpam8iL7Z8() <= (float)((Integer)this.elytraMinThresholdPercent.get()).intValue();
    }

    private void kjzgWev2Gyw8K() {
        if (((AutoEat)Modules.get().get(AutoEat.class)).eating) {
            return;
        }
        if (this.ydtYMNpam8iL7Z8() >= (float)((Integer)this.elytraMaxThresholdPercent.get()).intValue()) {
            this.kvBwtsHxL8DN = false;
            return;
        }
        int n = InventoryManager.KP44bk(Items.field_8287);
        if (n == -1) {
            ChatUtils.info((String)"%sNo experience bottles found, waiting ...", (Object[])new Object[]{Formatting.field_1065});
            return;
        }
        if (n > 8) {
            InventoryManager.UgB10d(Items.field_8287);
            return;
        }
        this.mc.player.getId().field_7545 = n;
        this.mc.player.method_36457(90.0f);
        this.mc.field_1761.method_2919((PlayerEntity)this.mc.player, InteractionHand.field_5808);
    }

    private BlockPos Gt56Sj4a6BWhgB(int n) {
        int n2 = this.mc.player.getX();
        int n3 = (Integer)this.targetY.get();
        int n4 = this.mc.player.getZ();
        if (this.mode.get() == Mode.v1nokUkHXYjAGxn) {
            return switch (this.wgso2Z6Z8lq9TnZ) {
                default -> throw new MatchException(null, null);
                case WorldUtils.Direction8.vSouwXdh7 -> new BlockPos(n2, n3, n4 - n);
                case WorldUtils.Direction8.Q5FUNqd0ALfl -> new BlockPos(n2, n3, n4 + n);
                case WorldUtils.Direction8.S8iuqKQCrJM02b -> new BlockPos(n2 + n, n3, n4);
                case WorldUtils.Direction8.v5UhyO9eEd7n -> new BlockPos(n2 - n, n3, n4);
                case WorldUtils.Direction8.ZOY41p -> new BlockPos(n2 + n, n3, n4 - n);
                case WorldUtils.Direction8.aiRs4cu -> new BlockPos(n2 - n, n3, n4 - n);
                case WorldUtils.Direction8.E8moug3IELf8 -> new BlockPos(n2 + n, n3, n4 + n);
                case WorldUtils.Direction8.CsEhJrV -> new BlockPos(n2 - n, n3, n4 + n);
            };
        }
        int n5 = this.UJeG0VvjslB.getX();
        int n6 = this.UJeG0VvjslB.getZ();
        return switch (this.wgso2Z6Z8lq9TnZ) {
            default -> throw new MatchException(null, null);
            case WorldUtils.Direction8.vSouwXdh7 -> new BlockPos(n5, n3, n4 - n);
            case WorldUtils.Direction8.Q5FUNqd0ALfl -> new BlockPos(n5, n3, n4 + n);
            case WorldUtils.Direction8.S8iuqKQCrJM02b -> new BlockPos(n2 + n, n3, n6);
            case WorldUtils.Direction8.v5UhyO9eEd7n -> new BlockPos(n2 - n, n3, n6);
            case WorldUtils.Direction8.ZOY41p -> {
                int var7_7 = n2 + n;
                yield new BlockPos(var7_7, n3, n5 + n6 - var7_7);
            }
            case WorldUtils.Direction8.aiRs4cu -> {
                int var7_8 = n2 - n;
                yield new BlockPos(var7_8, n3, var7_8 - (n5 - n6));
            }
            case WorldUtils.Direction8.E8moug3IELf8 -> {
                int var7_9 = n2 + n;
                yield new BlockPos(var7_9, n3, var7_9 - (n5 - n6));
            }
            case WorldUtils.Direction8.CsEhJrV -> {
                int var7_10 = n2 - n;
                yield new BlockPos(var7_10, n3, n5 + n6 - var7_10);
            }
        };
    }

    private void jOdDDFXSeWl4(HighwayRouter.Route route) {
        if (!XearoHelper.isLoaded()) {
            return;
        }
        ArrayList<XearoHelper.LineData> arrayList = new ArrayList<XearoHelper.LineData>();
        List<WorldUtils.Vec2d> list = route.UsO18QwQES9yS8g;
        int n = 0;
        while (n + 1 < list.size()) {
            arrayList.add(new XearoHelper.LineData((int)list.get(n).mcAmeo(), (int)list.get(n).ckqstPn4Gd(), (int)list.get(n + 1).mcAmeo(), (int)list.get(n + 1).ckqstPn4Gd()));
            ++n;
        }
        XearoHelper.get().drawLinesOnMap(arrayList, -16733441);
    }

    private void rPJDpX() {
        if (!XearoHelper.isLoaded()) {
            return;
        }
        List<XearoHelper.WaypointData> list = XearoHelper.get().getWaypoints(true);
        if (list == null) {
            return;
        }
        if (list.size() == this.t018N0) {
            return;
        }
        this.t018N0 = list.size();
        ArrayList<XearoHelper.WaypointData> arrayList = new ArrayList<XearoHelper.WaypointData>(list);
        arrayList.sort(Comparator.comparingLong(XearoHelper.WaypointData::createdAt));
        ArrayList<XearoHelper.LineData> arrayList2 = new ArrayList<XearoHelper.LineData>();
        double d = this.mc.player.getX();
        double d2 = this.mc.player.getZ();
        for (XearoHelper.WaypointData waypointData : arrayList) {
            HighwayRouter.Route route = HighwayRouter.jOdDDFXSeWl4(d, d2, waypointData.x(), waypointData.z());
            if (route != null) {
                List<WorldUtils.Vec2d> list2 = route.UsO18QwQES9yS8g;
                int n = 0;
                while (n + 1 < list2.size()) {
                    arrayList2.add(new XearoHelper.LineData((int)list2.get(n).mcAmeo(), (int)list2.get(n).ckqstPn4Gd(), (int)list2.get(n + 1).mcAmeo(), (int)list2.get(n + 1).ckqstPn4Gd()));
                    ++n;
                }
            }
            d = waypointData.x();
            d2 = waypointData.z();
        }
        XearoHelper.get().drawLinesOnMap(arrayList2, -16733441);
    }

    private void Hp3MRJIoQ() {
        if (this.Pa3aVwRtUo45jMG == null) {
            return;
        }
        String string = XearoHelper.get().getEtaSuffix(this.Pa3aVwRtUo45jMG);
        if (string == null || string.isBlank()) {
            string = "...";
        }
        this.mc.player.method_7353((ScreenHandler)ScreenHandler.method_43470((String)"[2bNav] ").method_10862(Slot.field_24360.method_10977(Formatting.YELLOW)).method_10852((ScreenHandler)ScreenHandler.method_43470((String)"ETA ").method_10862(Slot.field_24360.method_10977(Formatting.AQUA))).method_10852((ScreenHandler)ScreenHandler.method_43470((String)string).method_10862(Slot.field_24360.method_10977(Formatting.GRAY).method_10982(Boolean.valueOf(true)))), true);
    }

    public boolean jokapWphssQ() {
        return this.isActive() && !this.o6zpkIIG9 && !this.kvBwtsHxL8DN && !PathingHelper.LcPVM4w5KCoKSxGs() && this.mc.player.method_6118(DamageSource.field_6174).getStack().equals(Items.field_8833);
    }

    private void wgso2Z6Z8lq9TnZ() {
        if (this.mc.player == null) {
            return;
        }
        this.mc.getNetworkHandler().method_52787((Packet)new EntityTrackerEntry((Entity)this.mc.player, EntityTrackerEntry.class_2849.field_12982));
    }

    public static final class Mode
    extends Enum<Mode> {
        public static final /* enum */ Mode dOw8Pbbaj = new Mode();
        public static final /* enum */ Mode v1nokUkHXYjAGxn = new Mode();
        private static final /* synthetic */ Mode[] xGt2Gp7CVs8T;

        public static Mode[] values() {
            return (Mode[])xGt2Gp7CVs8T.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private static /* synthetic */ Mode[] kvBwtsHxL8DN() {
            return new Mode[]{dOw8Pbbaj, v1nokUkHXYjAGxn};
        }

        static {
            xGt2Gp7CVs8T = Mode.kvBwtsHxL8DN();
        }
    }

    static final class StartPos
    extends Enum<StartPos> {
        public static final /* enum */ StartPos bukSnj0c = new StartPos();
        public static final /* enum */ StartPos HWVVSgN = new StartPos();
        private static final /* synthetic */ StartPos[] Lm4xX5QT0OxvyV;

        public static StartPos[] values() {
            return (StartPos[])Lm4xX5QT0OxvyV.clone();
        }

        public static StartPos valueOf(String string) {
            return Enum.valueOf(StartPos.class, string);
        }

        private static /* synthetic */ StartPos[] nBniX5v() {
            return new StartPos[]{bukSnj0c, HWVVSgN};
        }

        static {
            Lm4xX5QT0OxvyV = StartPos.nBniX5v();
        }
    }
}

