// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.features;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.modules.automation.InventoryManager;
import musheor.utils.system.MusheorSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * "kek-fly" — elytra flight. CRUISE mode auto-takes-off, auto-fires rockets and can hold a
 * target Y (lock-y); DIRECTIONAL mode steers the glide with WASD/jump/sneak and hovers in
 * place (yaw- or pitch-wobble, or by freezing position) when idle. Handles elytra chest-swap
 * on activate/deactivate.
 */
public class KekFly extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: r7hOYIKN2

    public final Setting<FlyMode> flyMode = sgGeneral.add(new EnumSetting.Builder<FlyMode>() // was: FvaNWO
        .name("fly-mode").defaultValue(FlyMode.CRUISE).build());
    private final Setting<Boolean> autoRocket = sgGeneral.add(new BoolSetting.Builder() // was: oZHMlTL
        .name("auto-rocket").description("Fires a rocket automatically whenever the previous one despawns.").defaultValue(true).build());
    private final Setting<Double> rocketDelay = sgGeneral.add(new DoubleSetting.Builder() // was: xQr5FhbwpQPWgIQ
        .name("rocket-delay").description("Seconds between rockets when auto-rocket is off.").defaultValue(3.5).sliderRange(0.5, 15.0).decimalPlaces(1).visible(() -> !autoRocket.get()).build());
    private final Setting<HoverMode> hoverMode = sgGeneral.add(new EnumSetting.Builder<HoverMode>() // was: OMMZL1F3q
        .name("hover").defaultValue(HoverMode.YAW).visible(() -> flyMode.get() == FlyMode.DIRECTIONAL).build());
    private final Setting<Boolean> autoSwap = sgGeneral.add(new BoolSetting.Builder() // was: zu3a44xDeMFMCRwm
        .name("auto-swap").description("Automatically swaps to your elytra when activating").defaultValue(true).build());
    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder() // was: krxNb5lcQuWA
        .name("swap-back").description("Automatically swaps back to your chestplate when deactivating").defaultValue(true).visible(autoSwap::get).build());
    private final Setting<Integer> launchDelay = sgGeneral.add(new IntSetting.Builder() // was: nt0HZnvBBp
        .name("launch-delay").description("Delay in ticks before launching the player from the ground").defaultValue(0).sliderRange(0, 5).build());
    public final Setting<Integer> rocketTimeout = sgGeneral.add(new IntSetting.Builder() // was: Q90GLXQ0Pef
        .name("rocket-timeout").description("Timeout in ticks before assuming a rocket has been fired, increase this if you are on higher ping").defaultValue(3).sliderRange(0, 10).build());
    public final Setting<Boolean> lockY = sgGeneral.add(new BoolSetting.Builder() // was: psJq59YIbp3Z
        .name("lock-y").description("Steer the player's pitch each tick to maintain a target Y level while gliding.").defaultValue(false).visible(() -> flyMode.get() == FlyMode.CRUISE).build());
    public final Setting<Integer> yLevel = sgGeneral.add(new IntSetting.Builder() // was: SOYyh5IPg26f7F
        .name("y-level").description("The Y level to maintain when lock-y is enabled.").defaultValue(350).sliderRange(64, 512).visible(() -> flyMode.get() == FlyMode.CRUISE && lockY.get()).build());

    public static KekFly INSTANCE;         // was: rKbT3Ifwo
    private int rocketWaitTicks;           // was: amz3UB1vE
    private static int launchTickCounter;  // was: sBBIyQG5NWq0K
    private float savedYaw;                // was: sZkZ1izAy
    private float savedPitch;              // was: QYKUhjp
    private boolean rotationOverridden;    // was: NIz4xic3Js9 (rotation forced this tick, restored post-tick)
    private boolean hoverToggle;           // was: u1WFwbQRSKa
    private Vec3d hoverPos;                // was: LGDfbZq
    private long lastRocketTime;           // was: to3T8DJCDVX8po

    public KekFly() {
        super(musheor.MAIN, "kek-fly", "ZOOM thru the air, or even on ground...");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) return;
        this.rocketWaitTicks = 0;
        this.rotationOverridden = false;
        if (this.autoSwap.get() && !this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
        }
        if (this.mc.player.isOnGround()) this.mc.player.jump();
        launchTickCounter = -1;
        this.hoverToggle = false;
        this.hoverPos = null;
        this.lastRocketTime = 0L;
    }

    @Override
    public void onDeactivate() {
        if (this.mc.player == null || this.mc.world == null) return;
        this.mc.player.setNoGravity(false);
        this.hoverPos = null;
        if (this.autoSwap.get() && this.swapBack.get()) {
            ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
        } else {
            this.startFlying();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null) return;
        this.rotationOverridden = false;
        if (launchTickCounter++ < this.launchDelay.get()) return;

        this.startFlying();
        if (this.rocketWaitTicks > 0) {
            this.rocketWaitTicks--;
            if (this.rocketWaitTicks == 0) this.fireRocket();
            return;
        }

        if (this.flyMode.get() == FlyMode.CRUISE) {
            this.launchFromGround();
        } else {
            boolean up = this.mc.options.jumpKey.isPressed();
            boolean down = this.mc.options.sneakKey.isPressed();
            boolean w = this.mc.options.forwardKey.isPressed();
            boolean a = this.mc.options.leftKey.isPressed();
            boolean s = this.mc.options.backKey.isPressed();
            boolean d = this.mc.options.rightKey.isPressed();
            boolean any = up || down || w || a || s || d;
            this.savedYaw = this.mc.player.getYaw();
            this.savedPitch = this.mc.player.getPitch();
            float flightYaw = this.savedYaw;
            float flightPitch = -1.0F;

            if (!any) {
                if (this.hoverMode.get() != HoverMode.OFF) {
                    if (this.hasActiveRocket()) {
                        if (this.hoverMode.get() == HoverMode.YAW) {
                            if (this.hoverToggle) flightYaw += 180.0F;
                        } else if (this.hoverMode.get() == HoverMode.PITCH) {
                            flightPitch = this.hoverToggle ? -90.0F : 90.0F;
                        }
                        this.hoverToggle = !this.hoverToggle;
                    } else {
                        this.hoverInPlace();
                    }
                }
            } else {
                if (this.hoverPos != null) {
                    this.mc.player.setNoGravity(false);
                    this.mc.player.resetPosition();
                    this.hoverPos = null;
                }
                if (w && a) flightYaw -= 45.0F;
                else if (w && d) flightYaw += 45.0F;
                else if (s && a) flightYaw -= 135.0F;
                else if (s && d) flightYaw += 135.0F;
                else if (a) flightYaw -= 90.0F;
                else if (d) flightYaw += 90.0F;
                else if (s) flightYaw += 180.0F;

                if (up && (w || a || s || d)) {
                    flightPitch = -45.0F;
                } else if (up) {
                    flightPitch = -90.0F;
                } else if (down && (w || a || s || d)) {
                    flightPitch = 45.0F;
                } else if (down) {
                    flightPitch = 90.0F;
                }
                this.fireRocket();
            }

            this.mc.player.setYaw(flightYaw);
            this.mc.player.setPitch(flightPitch);
            this.rotationOverridden = true;
        }

        if (this.flyMode.get() == FlyMode.CRUISE) {
            if (this.lockY.get()) this.steerToYLevel();
            this.fireRocket();
        }
    }

    /** Pitches the player toward the configured Y level while gliding (except in the Nether). */
    private void steerToYLevel() { // was: rKbT3Ifwo()
        if (this.mc.player.isOnGround()) return;
        if (PlayerUtils.getDimension() == Dimension.Nether) return;
        double deltaY = this.yLevel.get() - this.mc.player.getY();
        float desiredPitch = (float) Math.toDegrees(Math.atan2(-deltaY, 16.0));
        desiredPitch = Math.max(-45.0F, Math.min(45.0F, desiredPitch));
        this.mc.player.setPitch(desiredPitch);
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) { // was: FvaNWO(Post)
        if (this.mc.player != null && this.rotationOverridden) {
            this.mc.player.setYaw(this.savedYaw);
            this.mc.player.setPitch(this.savedPitch);
            this.rotationOverridden = false;
        }
    }

    /** Freezes the player in place (no gravity, position held) to hover. */
    private void hoverInPlace() { // was: r7hOYIKN2()
        if (this.hoverPos == null) this.hoverPos = VersionHelper.get().getPlayerPos();
        this.mc.player.setVelocity(Vec3d.ZERO);
        this.mc.player.setNoGravity(true);
        this.mc.player.updatePosition(this.hoverPos.x, this.hoverPos.y, this.hoverPos.z);
    }

    /** Sends START_FALL_FLYING to begin gliding (if wearing an elytra and not underwater). */
    public void startFlying() { // was: FvaNWO()
        if (this.mc.player == null || this.mc.getNetworkHandler() == null) return;
        if (this.mc.player.isSubmergedInWater()) return;
        if (this.mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            this.mc.player.startFallFlying();
        }
    }

    /** In CRUISE mode: jumps off the ground and fires a rocket when there isn't already one active. */
    private void launchFromGround() { // was: oZHMlTL()
        if (this.mc.player.isOnGround() && !this.hasActiveRocket()) {
            this.mc.player.jump();
            this.rocketWaitTicks = this.rocketTimeout.get();
        }
    }

    /** True if a firework rocket owned by the player is currently in the world (or within the rocket-delay window). */
    public boolean hasActiveRocket() { // was: Q90GLXQ0Pef()
        if (this.autoRocket.get()) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (entity instanceof FireworkRocketEntity rocket && rocket.getOwner() == this.mc.player) return true;
            }
        }
        return !this.autoRocket.get() && System.currentTimeMillis() - this.lastRocketTime < this.rocketDelay.get() * 1000.0;
    }

    /** Fires a firework rocket from the inventory (silent slot swap), if none is active. */
    public void fireRocket() { // was: psJq59YIbp3Z()
        if (this.hasActiveRocket()) return;
        int rocket = InventoryManager.findItemSlotIndex(Items.FIREWORK_ROCKET);
        if (rocket == -1) return;
        InventoryManager.withSlotSwapped(rocket, () -> this.mc.interactionManager.sendSequencedPacket(this.mc.world,
            sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, this.mc.player.getYaw(), this.mc.player.getPitch())));
        MusheorSystem.debug("%sAttempted to fire a rocket!", Formatting.AQUA);
        this.rocketWaitTicks = this.rocketTimeout.get();
        this.lastRocketTime = System.currentTimeMillis();
    }

    /** True once past the launch-delay window (used by ElytraTweakz to gate take-off). */
    public static boolean isPastLaunchDelay() { // was: SOYyh5IPg26f7F()
        return launchTickCounter > INSTANCE.launchDelay.get();
    }

    /** Flight control mode. */ // was: enum FlyMode {FvaNWO, Q90GLXQ0Pef}
    public enum FlyMode { CRUISE, DIRECTIONAL }

    /** Idle-hover behaviour (DIRECTIONAL mode). */ // was: enum HoverMode {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum HoverMode { OFF, YAW, PITCH }
}
