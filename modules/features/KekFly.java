// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
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
import musheor.compat.VersionHelper;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.system.MusheorSystem;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

public class KekFly
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<FlyMode> flyMode;
    private final Setting<Boolean> autoRocket;
    private final Setting<Double> rocketDelay;
    private final Setting<Boolean> hoverMidAir;
    private final Setting<Boolean> autoSwap;
    private final Setting<Boolean> autoSwapBack;
    private final Setting<Integer> launchDelay;
    public final Setting<Integer> rocketTimeout;
    public final Setting<Boolean> lockY;
    public final Setting<Integer> targetY;
    public static KekFly fly;
    private int rocketDelayTicks;
    private static int launchTick;
    private float prevYaw;
    private float prevPitch;
    private boolean isOverridingRotation;
    private boolean hoverToggle;
    private Vec3d hoverPos;
    private long lastRocketTime;

    public KekFly() {
        super(musheor.MAIN, "kek-fly", "ZOOM thru the air, or even on ground...");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.flyMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("fly-mode")).defaultValue((Object)FlyMode.Elytra)).build());
        this.autoRocket = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-rocket")).description("Fires a rocket automatically whenever the previous one despawns.")).defaultValue((Object)true)).build());
        this.rocketDelay = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rocket-delay")).description("Seconds between rockets when auto-rocket is off.")).defaultValue(3.5).sliderRange(0.5, 15.0).decimalPlaces(1).visible(() -> (Boolean)this.autoRocket.get() == false)).build());
        this.hoverMidAir = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hover")).defaultValue((Object)true)).visible(() -> this.flyMode.get() == FlyMode.Creative)).build());
        this.autoSwap = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-swap")).description("Automatically swaps to your elytra when activating")).defaultValue((Object)true)).build());
        this.autoSwapBack = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swap-back")).description("Automatically swaps back to your chestplate when deactivating")).defaultValue((Object)true)).visible(() -> this.autoSwap.get())).build());
        this.launchDelay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("launch-delay")).description("Delay in ticks before launching the player from the ground")).defaultValue((Object)0)).sliderRange(0, 5).build());
        this.rocketTimeout = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rocket-timeout")).description("Timeout in ticks before assuming a rocket has been fired, increase this if you are on higher ping")).defaultValue((Object)3)).sliderRange(0, 10).build());
        this.lockY = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lock-y")).description("Steer the player's pitch each tick to maintain a target Y level while gliding.")).defaultValue((Object)false)).visible(() -> this.flyMode.get() == FlyMode.Elytra)).build());
        this.targetY = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("y-level")).description("The Y level to maintain when lock-y is enabled.")).defaultValue((Object)350)).sliderRange(64, 512).visible(() -> this.flyMode.get() == FlyMode.Elytra && (Boolean)this.lockY.get() != false)).build());
        fly = this;
    }

    public void onActivate() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        this.rocketDelayTicks = 0;
        this.isOverridingRotation = false;
        if (((Boolean)this.autoSwap.get()).booleanValue() && !this.mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
        }
        if (this.mc.player.isOnGround()) {
            this.mc.player.startFallFlying();
        }
        launchTick = -1;
        this.hoverToggle = false;
        this.hoverPos = null;
        this.lastRocketTime = 0L;
    }

    public void onDeactivate() {
        if (this.mc.player != null) {
            this.mc.player.setSprinting(false);
        }
        this.hoverPos = null;
        if (((Boolean)this.autoSwap.get()).booleanValue() && ((Boolean)this.autoSwapBack.get()).booleanValue()) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
        } else {
            this.ensureElytraFlying();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null) {
            return;
        }
        this.isOverridingRotation = false;
        if (launchTick++ < (Integer)this.launchDelay.get()) {
            return;
        }
        this.ensureElytraFlying();
        if (this.rocketDelayTicks > 0) {
            --this.rocketDelayTicks;
            if (this.rocketDelayTicks == 0) {
                this.fireRocket();
            }
            return;
        }
        if (this.flyMode.get() == FlyMode.Elytra) {
            this.tickElytraMode();
        } else {
            boolean bl = this.mc.options.jumpKey.isPressed();
            boolean bl2 = this.mc.options.sneakKey.isPressed();
            boolean bl3 = this.mc.options.forwardKey.isPressed();
            boolean bl4 = this.mc.options.leftKey.isPressed();
            boolean bl5 = this.mc.options.backKey.isPressed();
            boolean bl6 = this.mc.options.rightKey.isPressed();
            boolean bl7 = bl || bl2 || bl3 || bl4 || bl5 || bl6;
            this.prevYaw = this.mc.player.getYaw();
            this.prevPitch = this.mc.player.getPitch();
            float f = this.prevYaw;
            float f2 = -1.0f;
            if (!bl7) {
                if (((Boolean)this.hoverMidAir.get()).booleanValue()) {
                    if (this.hasActiveRocket()) {
                        if (this.hoverToggle) {
                            f += 180.0f;
                        }
                        this.hoverToggle = !this.hoverToggle;
                    } else {
                        this.tickHoverFreeze();
                    }
                }
            } else {
                if (this.hoverPos != null) {
                    this.mc.player.setSprinting(false);
                    this.mc.player.resetVelocity();
                    this.hoverPos = null;
                }
                if (bl3 && bl4) {
                    f -= 45.0f;
                } else if (bl3 && bl6) {
                    f += 45.0f;
                } else if (bl5 && bl4) {
                    f -= 135.0f;
                } else if (bl5 && bl6) {
                    f += 135.0f;
                } else if (bl4) {
                    f -= 90.0f;
                } else if (bl6) {
                    f += 90.0f;
                } else if (bl5) {
                    f += 180.0f;
                }
                if (bl && (bl3 || bl4 || bl5 || bl6)) {
                    f2 = -45.0f;
                } else if (bl) {
                    f2 = -90.0f;
                } else if (bl2 && (bl3 || bl4 || bl5 || bl6)) {
                    f2 = 45.0f;
                } else if (bl2) {
                    f2 = 90.0f;
                }
                this.fireRocket();
            }
            this.mc.player.setYaw(f);
            this.mc.player.setPitch(f2);
            this.isOverridingRotation = true;
        }
        if (this.flyMode.get() == FlyMode.Elytra) {
            if (((Boolean)this.lockY.get()).booleanValue()) {
                this.tickLockY();
            }
            this.fireRocket();
        }
    }

    private void tickLockY() {
        if (this.mc.player.isOnGround()) {
            return;
        }
        if (PlayerUtils.getDimension() == Dimension.Nether) {
            return;
        }
        double d = (double)((Integer)this.targetY.get()).intValue() - this.mc.player.getY();
        float f = (float)Math.toDegrees(Math.atan2(-d, 16.0));
        f = Math.max(-45.0f, Math.min(45.0f, f));
        this.mc.player.setPitch(f);
    }

    @EventHandler
    private void onTick(TickEvent.Post post) {
        if (this.mc.player == null || !this.isOverridingRotation) {
            return;
        }
        this.mc.player.setYaw(this.prevYaw);
        this.mc.player.setPitch(this.prevPitch);
        this.isOverridingRotation = false;
    }

    private void tickHoverFreeze() {
        if (this.hoverPos == null) {
            this.hoverPos = VersionHelper.get().getPlayerPos();
        }
        this.mc.player.setVelocity(Vec3d.ZERO);
        this.mc.player.setSprinting(true);
        this.mc.player.requestTeleport(this.hoverPos.x, this.hoverPos.y, this.hoverPos.z);
    }

    private void ensureElytraFlying() {
        if (this.mc.player == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        if (this.mc.player.isUsingItem()) {
            return;
        }
        if (!this.mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            return;
        }
        this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        this.mc.player.startFallFlying(); // client-side flag
    }

    private void tickElytraMode() {
        if (this.mc.player.isOnGround()) {
            if (!this.hasActiveRocket()) {
                this.mc.player.startFallFlying();
                this.rocketDelayTicks = (Integer)this.rocketTimeout.get();
            }
            return;
        }
    }

    public boolean hasActiveRocket() {
        if (((Boolean)this.autoRocket.get()).booleanValue()) {
            for (Entity Entity2 : this.mc.world.getEntities()) {
                FireworkRocketEntity firework;
                if (!(Entity2 instanceof FireworkRocketEntity) || (firework = (FireworkRocketEntity)Entity2).getOwner() != this.mc.player) continue;
                return true;
            }
        }
        return (Boolean)this.autoRocket.get() == false && (double)(System.currentTimeMillis() - this.lastRocketTime) < (Double)this.rocketDelay.get() * 1000.0;
    }

    public void fireRocket() {
        if (this.hasActiveRocket()) {
            return;
        }
        int n = InventoryManager.findItemSlot(Items.FIREWORK_ROCKET);
        if (n == -1) {
            return;
        }
        InventoryManager.withHotbarSlot(n, () -> this.mc.interactionManager.sendSequencedPacket(this.mc.world, n -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, n, this.mc.player.getYaw(), this.mc.player.getPitch())));
        MusheorSystem.debug("%sAttempted to fire a rocket!", Formatting.YELLOW);
        this.rocketDelayTicks = (Integer)this.rocketTimeout.get();
        this.lastRocketTime = System.currentTimeMillis();
    }

    public static boolean isLaunched() {
        return launchTick > (Integer)KekFly.fly.launchDelay.get();
    }

    public static final class FlyMode
    extends Enum<FlyMode> {
        public static final /* enum */ FlyMode Elytra = new FlyMode();
        public static final /* enum */ FlyMode Creative = new FlyMode();
        private static final /* synthetic */ FlyMode[] $VALUES;

        public static FlyMode[] values() {
            return (FlyMode[])$VALUES.clone();
        }

        public static FlyMode valueOf(String string) {
            return Enum.valueOf(FlyMode.class, string);
        }

        private static /* synthetic */ FlyMode[] $values() {
            return new FlyMode[]{Elytra, Creative};
        }

        static {
            $VALUES = FlyMode.$values();
        }
    }
}

