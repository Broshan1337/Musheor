// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.modules.features.KekFly;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.EquipmentSlot;

/**
 * "elytra-tweakz" — quality-of-life elytra helpers: double-tap-jump to equip an elytra
 * and take off, auto-equip a chestplate on landing, auto-swap a broken/low-durability
 * elytra, and a keybind to fire rockets. Deactivates itself while KekFly/KekBounce run.
 */
public class ElytraTweakz extends Module {
    private final Setting<Boolean> doubleJumpEquip = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: FvaNWO
        .name("double-jump-equip-elytra").description("Equip your elytra and start gliding when double-tapping spacebar while airborne")
        .defaultValue(true).build());
    private final Setting<Boolean> landEquipChestplate = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: Q90GLXQ0Pef
        .name("land-equip-chestplate").description("Automatically equip your chestplate when landing after gliding")
        .defaultValue(true).build());
    private final Setting<Keybind> fireRocketKey = this.settings.getDefaultGroup().add(new KeybindSetting.Builder() // was: psJq59YIbp3Z
        .name("fire-rocket-keybind").description("Fires a rocket when pressed/held. Does nothing if there is already an active rocket")
        .defaultValue(Keybind.none()).build());
    private final Setting<Boolean> autoSwapBroken = this.settings.getDefaultGroup().add(new BoolSetting.Builder() // was: SOYyh5IPg26f7F
        .name("auto-swap-broken").description("Automatically swap with a new elytra when your current one is broken / damaged")
        .defaultValue(true).build());
    private final Setting<Integer> durabilityThreshold = this.settings.getDefaultGroup().add(new IntSetting.Builder() // was: rKbT3Ifwo
        .name("durability-threshold").defaultValue(10).sliderRange(1, 100).visible(autoSwapBroken::get).build());

    private boolean wasJumpPressed;   // was: r7hOYIKN2
    private boolean wasOnGround;      // was: oZHMlTL
    private boolean wasGliding;       // was: xQr5FhbwpQPWgIQ
    private long lastJumpTime;        // was: OMMZL1F3q
    private int startGlideTicks;      // was: zu3a44xDeMFMCRwm (ticks to keep triggering take-off after a chest swap)
    private int rocketCooldown;       // was: krxNb5lcQuWA

    public ElytraTweakz() {
        super(musheor.MAIN, "elytra-tweakz", "Useful elytra enhancements and tweaks");
    }

    @Override
    public void onActivate() {
        this.wasJumpPressed = false;
        this.wasOnGround = false;
        this.wasGliding = false;
        this.lastJumpTime = 0L;
        this.startGlideTicks = 0;
        this.rocketCooldown = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        if (((KekFly) Modules.get().get(KekFly.class)).isActive()) return;
        if (((KekBounce) Modules.get().get(KekBounce.class)).isActive()) return;

        boolean onGround = this.mc.player.isOnGround();
        boolean gliding = this.mc.player.isGliding();
        boolean jumpPressed = this.mc.options.jumpKey.isPressed();
        boolean jumpJustPressed = jumpPressed && !this.wasJumpPressed;

        // Auto-swap a low-durability elytra for a fresh one from the inventory.
        if (this.autoSwapBroken.get()) {
            ItemStack chest = this.mc.player.getEquippedStack(EquipmentSlot.CHEST);
            if (chest.isOf(Items.ELYTRA)) {
                int dura = chest.getMaxDamage() - chest.getDamage();
                if (dura <= this.durabilityThreshold.get()) {
                    for (int i = 0; i < this.mc.player.getInventory().main.size(); i++) {
                        ItemStack stack = this.mc.player.getInventory().getStack(i);
                        if (stack.isOf(Items.ELYTRA) && stack.getMaxDamage() - stack.getDamage() > this.durabilityThreshold.get()) {
                            InvUtils.move().from(i).toArmor(2);
                            break;
                        }
                    }
                }
            }
        }

        // Double-tap jump while airborne: equip elytra (chest swap) then take off.
        if (this.doubleJumpEquip.get()) {
            if (jumpJustPressed) {
                long now = System.currentTimeMillis();
                if (!onGround && now - this.lastJumpTime < 300L) {
                    ItemStack chest = this.mc.player.getEquippedStack(EquipmentSlot.CHEST);
                    if (!chest.isOf(Items.ELYTRA)) {
                        boolean hasElytra = this.mc.player.getInventory().main.stream()
                            .anyMatch(s -> s.isOf(Items.ELYTRA) && s.getMaxDamage() - s.getDamage() > this.durabilityThreshold.get());
                        if (hasElytra) {
                            ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
                            this.startGlideTicks = 2;
                        }
                    } else if (chest.getMaxDamage() - chest.getDamage() > this.durabilityThreshold.get()) {
                        KekFly.INSTANCE.startFlying();
                    }
                }
                this.lastJumpTime = now;
            }

            if (this.startGlideTicks > 0) {
                this.startGlideTicks--;
                KekFly.INSTANCE.startFlying();
            }
        }

        // On landing after a glide, re-equip the chestplate.
        if (this.landEquipChestplate.get() && !this.wasOnGround && onGround && this.wasGliding
            && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            ((ChestSwap) Modules.get().get(ChestSwap.class)).swap();
        }

        // Rocket keybind while gliding.
        if (this.mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && this.mc.player.isGliding()) {
            if (this.rocketCooldown > 0) this.rocketCooldown--;
            if (this.fireRocketKey.get().isPressed() && this.rocketCooldown == 0) {
                KekFly.INSTANCE.fireRocket();
                this.rocketCooldown = KekFly.INSTANCE.rocketTimeout.get();
            }
        }

        this.wasJumpPressed = jumpPressed;
        this.wasOnGround = onGround;
        this.wasGliding = gliding;
    }
}
