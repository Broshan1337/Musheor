// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
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
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.compat.XearoHelper;
import musheor.modules.automation.highway.HighwayNavigator;
import musheor.modules.automation.highway.HighwayRouter;
import musheor.utils.WorldUtils;
import musheor.utils.internal.PathingHelper;
import musheor.utils.system.HighwayNetworkManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * "kek-bounce" — an elytra "bounce" highway travel module. On the ground while sprinting
 * it repeatedly sends START_FALL_FLYING to skim forward. Two modes: Simple (freeform, with
 * optional Baritone obstacle-passer and y-motion cancel for extra speed) and Navigate
 * ("2bNav") which routes along the {@link HighwayRouter} network toward Xaero temp
 * waypoints via a {@link HighwayNavigator}. Optional auto-repair of the elytra with XP
 * bottles, ETA overlay, and path rendering on the Xaero map.
 */
public class KekBounce extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();          // was: FvaNWO
    private final SettingGroup sgRepair = this.settings.createGroup("Auto-Repair");  // was: Q90GLXQ0Pef
    private final SettingGroup sgNav = this.settings.createGroup("2bNav");           // was: psJq59YIbp3Z

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>() // was: SOYyh5IPg26f7F
        .name("mode").description("Simple: freeform bounce. Navigate: Set and follows temporary waypoints created using Xearo").defaultValue(Mode.SIMPLE).build());
    private final Setting<Boolean> yMotion = sgGeneral.add(new BoolSetting.Builder() // was: rKbT3Ifwo
        .name("y-motion").description("Zoom really fast by cancelling your vertical momentum").defaultValue(false).visible(() -> mode.get() == Mode.SIMPLE).build());
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder() // was: r7hOYIKN2
        .name("speed").description("The speed in blocks per second to keep you at.").defaultValue(100.0).sliderRange(20.0, 250.0).visible(yMotion::get).build());
    private final Setting<Integer> yMotionDelay = sgGeneral.add(new IntSetting.Builder() // was: oZHMlTL
        .name("y-motion-delay").description("Seconds to bounce normally before y-motion kicks in. Resets when the obstacle passer intervenes.")
        .defaultValue(5).sliderRange(1, 30).visible(yMotion::get).build());
    private final Setting<Boolean> lockPitch = sgGeneral.add(new BoolSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("lock-pitch").description("Whether to lock your pitch when bouncing.").defaultValue(true).build());
    private final Setting<Double> pitch = sgGeneral.add(new DoubleSetting.Builder() // was: OMMZL1F3q
        .name("pitch").description("Degrees of pitch to lock to").defaultValue(72.4).sliderRange(-90.0, 90.0).decimalPlaces(2).visible(lockPitch::get).build());
    private final Setting<Boolean> obstaclePasser = sgGeneral.add(new BoolSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("simple-obstacle-passer").description("Uses baritone to pass obstacles and aligns you to your starting position when enabling the module").defaultValue(false).build());
    private final Setting<StartPos> startPosType = sgGeneral.add(new EnumSetting.Builder<StartPos>() // was: krxNb5lcQuWA
        .name("start-pos-type").description("Automatic or custom starting position for aligning with obstacle-passer").defaultValue(StartPos.AUTOMATIC)
        .visible(() -> mode.get() == Mode.SIMPLE && obstaclePasser.get()).build());
    private final Setting<BlockPos> customStartPos = sgGeneral.add(new BlockPosSetting.Builder() // was: nt0HZnvBBp
        .name("custom-start-pos").description("The position to align with when using the custom starting position option.").defaultValue(new BlockPos(0, 0, 0))
        .visible(() -> mode.get() == Mode.SIMPLE && obstaclePasser.get() && startPosType.get() == StartPos.CUSTOM)
        .onChanged(value -> this.startPos = value).build());
    private final Setting<Integer> passerDistance = sgGeneral.add(new IntSetting.Builder() // was: amz3UB1vE
        .name("distance").description("The distance to set the baritone goal for path realignment.").defaultValue(8).visible(obstaclePasser::get).build());
    private final Setting<Integer> yLevel = sgGeneral.add(new IntSetting.Builder() // was: sBBIyQG5NWq0K
        .name("y-level").description("The Y level to bounce at.").defaultValue(120).sliderRange(-64, 120).visible(obstaclePasser::get).build());
    private final Setting<Boolean> toggleElytra = sgGeneral.add(new BoolSetting.Builder() // was: sZkZ1izAy
        .name("toggle-elytra").description("Equips an elytra on activate, and a chestplate on deactivate.").defaultValue(true).build());

    private final Setting<Boolean> showEta = sgNav.add(new BoolSetting.Builder() // was: QYKUhjp
        .name("show-eta").description("Displays and calculates arrival time above the player's hotbar.").defaultValue(true)
        .visible(() -> XearoHelper.isLoaded() && FabricLoader.getInstance().isModLoaded("xaeroplus") && mode.get() == Mode.NAVIGATE).build());
    private final Setting<Boolean> renderPathOnMap = sgNav.add(new BoolSetting.Builder() // was: NIz4xic3Js9
        .name("render-path-on-map").description("Displays your current navigation path on your minimap / worldmap.").defaultValue(true)
        .visible(() -> XearoHelper.isLoaded() && FabricLoader.getInstance().isModLoaded("xaeroplus") && mode.get() == Mode.NAVIGATE).build());
    private final Setting<Double> approachRadius = sgNav.add(new DoubleSetting.Builder() // was: u1WFwbQRSKa
        .name("approach-radius").description("Start slowing down when within this many blocks of a waypoint.").defaultValue(64.0).sliderRange(16.0, 256.0).decimalPlaces(0)
        .visible(() -> XearoHelper.isLoaded() && mode.get() == Mode.NAVIGATE).build());
    private final Setting<Double> arriveRadius = sgNav.add(new DoubleSetting.Builder() // was: LGDfbZq
        .name("arrive-radius").description("Consider a waypoint reached when within this many blocks.").defaultValue(32.0).sliderRange(8.0, 128.0).decimalPlaces(0)
        .visible(() -> XearoHelper.isLoaded() && mode.get() == Mode.NAVIGATE).build());
    private final Setting<Integer> alignTimeout = sgNav.add(new IntSetting.Builder() // was: to3T8DJCDVX8po
        .name("align-timeout").description("Ticks to spend aligning yaw before bouncing anyway.").defaultValue(40).sliderRange(10, 100)
        .visible(() -> XearoHelper.isLoaded() && mode.get() == Mode.NAVIGATE).build());
    private final Setting<Double> yawTolerance = sgNav.add(new DoubleSetting.Builder() // was: Sd3jEwKuGABy
        .name("yaw-tolerance").description("Degrees of yaw error considered close enough to start bouncing.").defaultValue(4.0).sliderRange(1.0, 20.0).decimalPlaces(1)
        .visible(() -> XearoHelper.isLoaded() && mode.get() == Mode.NAVIGATE).build());
    private final Setting<Integer> transitionDistance = sgNav.add(new IntSetting.Builder() // was: kJfFkD47Vh
        .name("transition-distance").description("Baritone goal distance when walking an intersection.").defaultValue(8).sliderRange(1, 32)
        .visible(() -> XearoHelper.isLoaded() && mode.get() == Mode.NAVIGATE).build());
    private final Setting<Integer> settleTicks = sgNav.add(new IntSetting.Builder() // was: ubHptFBRn5bO
        .name("settle-ticks").description("Ticks to wait at an intersection after arriving before resuming bounce.").defaultValue(10).sliderRange(0, 40)
        .visible(() -> XearoHelper.isLoaded() && mode.get() == Mode.NAVIGATE).build());

    private final Setting<Boolean> repairElytra = sgRepair.add(new BoolSetting.Builder() // was: apOpfoOHr3fJVwT
        .name("repair-elytra").description("Uses experience bottles to repair your elytra.").defaultValue(false).build());
    private final Setting<Integer> damageThreshold = sgRepair.add(new IntSetting.Builder() // was: hq1pN0qY
        .name("damage-threshold-%").description("At what durability percentage should it start repairing the elytra.").defaultValue(20).sliderRange(0, 100).visible(repairElytra::get).build());
    private final Setting<Integer> repairThreshold = sgRepair.add(new IntSetting.Builder() // was: ptxWcpd1WV763T5
        .name("repair-threshold-%").description("At what durability percentage should it stop repairing the elytra.").defaultValue(80).sliderRange(0, 100).visible(repairElytra::get).build());

    private final Setting<String> server = sgGeneral.add(new StringSetting.Builder() // was: DnAk86nuI
        .name("server").description("Which server's highway network to use. Must match a key in the remote config (e.g. \"2b2t\").").defaultValue("2b2t")
        .onChanged(value -> {
            HighwayNetworkManager mgr = HighwayNetworkManager.getInstance();
            if (!mgr.isLoaded()) {
                this.warning("Highway configs are still loading — server key will be validated once loaded.");
            } else if (!mgr.getServerNames().contains(value)) {
                this.error("Unknown server \"(highlight)%s(default)\". Known servers: (highlight)%s(default).", value, String.join(", ", mgr.getServerNames()));
            }
        }).visible(() -> false).build());

    private WorldUtils.Direction8 direction;          // was: LlN8EpIZKbk
    private int stuckTicks;                            // was: pgjj9cLYUTE5g
    private boolean paused;                            // was: IeStEJRJ9eb3l (bounce suspended, e.g. while pathing)
    private boolean repairing;                         // was: sFazojak6ig8QgGq
    private HighwayNavigator navigator = null;         // was: ewq603nIlCd9Gbu
    private XearoHelper.WaypointData navTarget = null; // was: ExGM8SQ9Qni
    private int lastWaypointCount = -1;                // was: yS4isXf3gAzs
    private int groundTicks;                           // was: eC9HV2bWGX (bounce ticks toward the y-motion delay)
    private BlockPos startPos;                         // was: w9spWeVv3AvI
    private Vec3d lastPos;                             // was: HvulV2j9tKjohNgh (previous position, for speed calc)

    public KekBounce() {
        super(musheor.AUTOMATION, "kek-bounce", "Elytra bounce module with some extra juice.");
    }

    @Override
    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) return;
        this.direction = WorldUtils.getMovementDirection();
        this.stuckTicks = 0;
        this.paused = false;
        this.repairing = false;
        this.groundTicks = 0;
        this.startPos = this.startPosType.get() == StartPos.AUTOMATIC
            ? BlockPos.ofFloored(VersionHelper.get().getPlayerPos())
            : this.customStartPos.get();
        this.lastPos = VersionHelper.get().getPlayerPos();

        if (this.toggleElytra.get() && !this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
        }

        if (this.mode.get() == Mode.NAVIGATE) {
            if (this.renderPathOnMap.get() && !FabricLoader.getInstance().isModLoaded("xaeroplus")) {
                this.warning("Cannot render path on map, %sXaeroPlus %srequired but not installed!", Formatting.GOLD, Formatting.YELLOW);
                this.renderPathOnMap.set(false);
            }
            if (this.showEta.get() && !FabricLoader.getInstance().isModLoaded("xaeroplus")) {
                this.warning("Cannot show ETA, %sXaeroPlus %srequired but not installed!", Formatting.GOLD, Formatting.YELLOW);
                this.showEta.set(false);
            }
            if (!XearoHelper.isLoaded()) {
                this.warning("AdvancedNavigation requires %sXaero%s. Install it or switch to Simple mode.", Formatting.RED, Formatting.YELLOW);
                this.toggle();
                return;
            }
            this.startNavigation();
        } else {
            this.resetSimple();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.navigator != null) {
            this.navigator.stop();
            this.navigator = null;
        }
        this.navTarget = null;
        this.lastWaypointCount = -1;
        if (XearoHelper.isLoaded()) XearoHelper.get().clearLinesOnMap();
        if (this.toggleElytra.get()) ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
        if (PathingHelper.isPathing()) PathingHelper.cancelEverything();
    }

    private void resetSimple() { // was: Q90GLXQ0Pef()
        this.direction = WorldUtils.getMovementDirection();
        this.stuckTicks = 0;
        this.paused = false;
        this.repairing = false;
    }

    private void startNavigation() { // was: psJq59YIbp3Z()
        XearoHelper.WaypointData wp = XearoHelper.get().getOldestWaypoint(true);
        if (wp == null) {
            ChatUtils.info(Formatting.AQUA + "[2bNav] " + Formatting.WHITE + "No temp waypoints found!");
            this.toggle();
        } else {
            this.navigateTo(wp);
        }
    }

    private void navigateTo(XearoHelper.WaypointData wp) { // was: FvaNWO(WaypointData)
        this.navTarget = wp;
        double px = this.mc.player.getX();
        double pz = this.mc.player.getZ();
        double gx = wp.x();
        double gz = wp.z();
        ChatUtils.info(Formatting.AQUA + "[2bNav] " + Formatting.WHITE + "Navigating to waypoint: " + Formatting.YELLOW + wp.name()
            + Formatting.WHITE + " (" + (int) gx + ", " + (int) gz + ")");
        HighwayRouter.Route route = HighwayRouter.route(px, pz, gx, gz);
        if (route == null) {
            ChatUtils.info(Formatting.AQUA + "[2bNav] " + Formatting.WHITE + "No highway route found to waypoint: " + wp.name());
            this.toggle();
            return;
        }
        this.drawRouteOnMap(route);
        HighwayNavigator.Config navConfig = new HighwayNavigator.Config(
            this.approachRadius.get(), this.arriveRadius.get(), this.alignTimeout.get(),
            this.yawTolerance.get().floatValue(), this.transitionDistance.get(), this.settleTicks.get());
        this.navigator = new HighwayNavigator(route, navConfig,
            active -> this.paused = !active,
            new HighwayNavigator.Listener() {
                @Override
                public void onStateChange(HighwayNavigator.State newState, String description) {
                    ChatUtils.info(Formatting.AQUA + "[2bNav] " + Formatting.WHITE + description);
                }

                @Override
                public void onComplete() {
                    XearoHelper.get().deleteCurrentWaypoint(KekBounce.this.navTarget);
                    KekBounce.this.navTarget = null;
                    XearoHelper.WaypointData next = XearoHelper.get().getOldestWaypoint(true);
                    if (next == null) {
                        ChatUtils.info(Formatting.AQUA + "[2bNav] " + Formatting.WHITE + "No waypoints found, assuming destination was reached!");
                        KekBounce.this.toggle();
                    } else {
                        KekBounce.this.navigateTo(next);
                    }
                }
            });
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) { // was: FvaNWO(PlayerMoveEvent)
        if (this.mc.player == null || event.type != MovementType.SELF || !this.canBounce()
            || !this.yMotion.get() || this.mode.get() == Mode.NAVIGATE) {
            return;
        }
        double speedBps = VersionHelper.get().getPlayerPos().subtract(this.lastPos).multiply(20.0, 0.0, 20.0).length();
        Timer timer = (Timer) Modules.get().get(Timer.class);
        if (timer.isActive()) speedBps *= timer.getMultiplier();

        if (this.mc.player.isOnGround() && this.mc.player.isSprinting()
            && this.groundTicks >= this.yMotionDelay.get() * 20 && speedBps < this.speed.get()) {
            ((IVec3d) event.movement).meteor$setY(0.0);
            this.mc.player.setVelocity(this.mc.player.getVelocity().x, 0.0, this.mc.player.getVelocity().z);
        }
        this.lastPos = VersionHelper.get().getPlayerPos();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        if (this.mode.get() == Mode.NAVIGATE) {
            this.tickNavigate();
        } else {
            this.tickSimple();
        }
    }

    private void tickNavigate() { // was: SOYyh5IPg26f7F()
        if (this.repairing) {
            this.tickRepair();
        } else if (this.shouldStartRepair()) {
            this.repairing = true;
        } else {
            if (this.mode.get() == Mode.NAVIGATE && this.renderPathOnMap.get()) this.drawAllRoutesOnMap();
            if (this.showEta.get()) this.showEtaOverlay();

            if (this.navigator != null) {
                if (this.obstaclePasser.get() && this.navigator.getState() == HighwayNavigator.State.BOUNCING) {
                    this.tickObstaclePasser();
                }
                this.navigator.tick();
                if (this.canBounce()) {
                    this.mc.player.setSprinting(true);
                    if (this.lockPitch.get() && !PathingHelper.isPathing()) {
                        this.mc.player.setPitch(this.pitch.get().floatValue());
                    }
                    if (this.mc.player.isOnGround() && !this.paused) {
                        this.mc.player.jump();
                    }
                    this.sendBounceJump();
                }
            }
        }
    }

    private void tickSimple() { // was: rKbT3Ifwo()
        if (this.canBounce()) {
            this.mc.player.setSprinting(true);
        }
        if (this.obstaclePasser.get()) {
            this.tickObstaclePasser();
        }
        if (!this.paused && !PathingHelper.isPathing()) {
            this.groundTicks++;
        }

        if (this.repairing) {
            this.tickRepair();
        } else if (this.shouldStartRepair()) {
            this.repairing = true;
        } else {
            if (this.lockPitch.get() && !PathingHelper.isPathing()) {
                this.mc.player.setPitch(this.pitch.get().floatValue());
            }
            if (this.mc.player.isOnGround() && !this.paused && !PathingHelper.isPathing()) {
                this.mc.player.jump();
            }
            if (this.canBounce()) {
                this.sendBounceJump();
            }
        }
    }

    /** When stuck, hands off to Baritone to walk past an obstacle and realign on the highway. */
    private void tickObstaclePasser() { // was: r7hOYIKN2()
        if (PathingHelper.isPathing()) return;
        if (this.paused) {
            this.paused = false;
            this.stuckTicks = 0;
            return;
        }
        boolean yLevelWrong = this.mode.get() != Mode.NAVIGATE
            && (VersionHelper.get().getPlayerPos().getY() < this.yLevel.get()
                || VersionHelper.get().getPlayerPos().getY() > this.yLevel.get() + 2);
        if (!yLevelWrong && (!this.mc.player.horizontalCollision || this.mc.player.minorHorizontalCollision)) {
            if (this.isStuck()) {
                if (++this.stuckTicks > 20) {
                    this.paused = true;
                    this.stuckTicks = 0;
                    this.groundTicks = 0;
                    PathingHelper.gotoBlock(this.computePasserGoal(this.passerDistance.get()));
                }
            } else {
                this.stuckTicks = 0;
                this.paused = false;
            }
        } else {
            this.paused = true;
            this.groundTicks = 0;
            PathingHelper.gotoBlock(this.computePasserGoal(this.passerDistance.get()));
        }
    }

    /** True if horizontal velocity is nearly zero. */
    private boolean isStuck() { // was: oZHMlTL()
        if (this.mc.player == null) return false;
        Vec3d vel = this.mc.player.getVelocity();
        return Math.sqrt(vel.x * vel.x + vel.z * vel.z) < 0.2;
    }

    /** Current elytra durability as a percentage. */
    private float elytraDurabilityPct() { // was: xQr5FhbwpQPWgIQ()
        ItemStack stack = this.mc.player.getEquippedStack(EquipmentSlot.CHEST);
        return (1.0F - (float) stack.getDamage() / stack.getMaxDamage()) * 100.0F;
    }

    /** True if the elytra is worn and below the damage threshold (and not eating). */
    private boolean shouldStartRepair() { // was: OMMZL1F3q()
        if (!this.repairElytra.get()) return false;
        if (!this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) return false;
        if (((AutoEat) Modules.get().get(AutoEat.class)).eating) return false;
        return this.elytraDurabilityPct() <= this.damageThreshold.get();
    }

    /** Throws XP bottles at the ground to repair the elytra until the repair threshold is reached. */
    private void tickRepair() { // was: zu3a44xDeMFMCRwm()
        if (((AutoEat) Modules.get().get(AutoEat.class)).eating) return;
        if (this.elytraDurabilityPct() >= this.repairThreshold.get()) {
            this.repairing = false;
            return;
        }
        int xpSlot = InventoryManager.findItemSlotIndex(Items.EXPERIENCE_BOTTLE);
        if (xpSlot == -1) {
            ChatUtils.info("%sNo experience bottles found, waiting ...", Formatting.GOLD);
        } else if (xpSlot > 8) {
            InventoryManager.moveToHotbar(Items.EXPERIENCE_BOTTLE);
        } else {
            this.mc.player.getInventory().selectedSlot = xpSlot;
            this.mc.player.setPitch(90.0F);
            this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
        }
    }

    /** The Baritone goal position {@code distance} blocks ahead along the current highway direction. */
    private BlockPos computePasserGoal(int distance) { // was: FvaNWO(int)
        int px = this.mc.player.getBlockX();
        int py = this.yLevel.get();
        int pz = this.mc.player.getBlockZ();
        if (this.mode.get() == Mode.NAVIGATE) {
            return switch (this.direction) {
                case NORTH -> new BlockPos(px, py, pz - distance);
                case SOUTH -> new BlockPos(px, py, pz + distance);
                case EAST -> new BlockPos(px + distance, py, pz);
                case WEST -> new BlockPos(px - distance, py, pz);
                case NORTH_EAST -> new BlockPos(px + distance, py, pz - distance);
                case NORTH_WEST -> new BlockPos(px - distance, py, pz - distance);
                case SOUTH_EAST -> new BlockPos(px + distance, py, pz + distance);
                case SOUTH_WEST -> new BlockPos(px - distance, py, pz + distance);
            };
        } else {
            int sx = this.startPos.getX();
            int sz = this.startPos.getZ();
            return switch (this.direction) {
                case NORTH -> new BlockPos(sx, py, pz - distance);
                case SOUTH -> new BlockPos(sx, py, pz + distance);
                case EAST -> new BlockPos(px + distance, py, sz);
                case WEST -> new BlockPos(px - distance, py, sz);
                case NORTH_EAST -> {
                    int gx = px + distance;
                    yield new BlockPos(gx, py, sx + sz - gx);
                }
                case NORTH_WEST -> {
                    int gx = px - distance;
                    yield new BlockPos(gx, py, gx - (sx - sz));
                }
                case SOUTH_EAST -> {
                    int gx = px + distance;
                    yield new BlockPos(gx, py, gx - (sx - sz));
                }
                case SOUTH_WEST -> {
                    int gx = px - distance;
                    yield new BlockPos(gx, py, sx + sz - gx);
                }
            };
        }
    }

    /** Draws a single route's waypoint polyline on the Xaero map. */
    private void drawRouteOnMap(HighwayRouter.Route route) { // was: FvaNWO(Route)
        if (!XearoHelper.isLoaded()) return;
        List<XearoHelper.LineData> lines = new ArrayList<>();
        List<WorldUtils.Coord2D> wps = route.waypoints;
        for (int i = 0; i + 1 < wps.size(); i++) {
            lines.add(new XearoHelper.LineData((int) wps.get(i).x(), (int) wps.get(i).z(), (int) wps.get(i + 1).x(), (int) wps.get(i + 1).z()));
        }
        XearoHelper.get().drawLinesOnMap(lines, -16733441);
    }

    /** Draws the routed polyline through every temp waypoint on the Xaero map. */
    private void drawAllRoutesOnMap() { // was: krxNb5lcQuWA()
        if (!XearoHelper.isLoaded()) return;
        List<XearoHelper.WaypointData> wps = XearoHelper.get().getWaypoints(true);
        if (wps == null || wps.size() == this.lastWaypointCount) return;
        this.lastWaypointCount = wps.size();
        List<XearoHelper.WaypointData> sorted = new ArrayList<>(wps);
        sorted.sort(Comparator.comparingLong(XearoHelper.WaypointData::createdAt));
        List<XearoHelper.LineData> allLines = new ArrayList<>();
        double fromX = this.mc.player.getX();
        double fromZ = this.mc.player.getZ();
        for (XearoHelper.WaypointData wp : sorted) {
            HighwayRouter.Route route = HighwayRouter.route(fromX, fromZ, wp.x(), wp.z());
            if (route != null) {
                List<WorldUtils.Coord2D> pts = route.waypoints;
                for (int i = 0; i + 1 < pts.size(); i++) {
                    allLines.add(new XearoHelper.LineData((int) pts.get(i).x(), (int) pts.get(i).z(), (int) pts.get(i + 1).x(), (int) pts.get(i + 1).z()));
                }
            }
            fromX = wp.x();
            fromZ = wp.z();
        }
        XearoHelper.get().drawLinesOnMap(allLines, -16733441);
    }

    /** Renders the ETA to the current nav target above the hotbar. */
    private void showEtaOverlay() { // was: nt0HZnvBBp()
        if (this.navTarget == null) return;
        String etaStr = XearoHelper.get().getEtaSuffix(this.navTarget);
        if (etaStr == null || etaStr.isBlank()) etaStr = "...";
        this.mc.player.sendMessage(
            Text.literal("[2bNav] ").setStyle(Style.EMPTY.withColor(Formatting.AQUA))
                .append(Text.literal("ETA ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                .append(Text.literal(etaStr).setStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(true))),
            true);
    }

    /** True if the module may bounce this tick (active, not paused/repairing/pathing, elytra worn). */
    public boolean canBounce() { // was: FvaNWO()
        return this.isActive()
            && !this.paused
            && !this.repairing
            && !PathingHelper.isPathing()
            && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA);
    }

    /** Sends START_FALL_FLYING to trigger the elytra bounce off the ground. */
    private void sendBounceJump() { // was: amz3UB1vE()
        if (this.mc.player != null) {
            this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    /** Bounce mode. */ // was: enum Mode {FvaNWO, Q90GLXQ0Pef}
    public enum Mode { SIMPLE, NAVIGATE }

    /** Obstacle-passer alignment origin. */ // was: enum StartPos {FvaNWO, Q90GLXQ0Pef}
    private enum StartPos { AUTOMATIC, CUSTOM }
}
