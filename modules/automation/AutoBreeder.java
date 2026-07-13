// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import baritone.api.pathing.goals.GoalBlock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.compat.VersionHelper;
import musheor.utils.internal.PathingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

/**
 * "auto-breeder" — breeds the configured animal types automatically: paths to the
 * nearest eligible adult (Baritone), selects a breeding item, and sends the interact
 * packets, then puts that animal on a 5-minute cooldown to avoid re-breeding.
 */
public class AutoBreeder extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance(); // was: FvaNWO
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: Q90GLXQ0Pef

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder() // was: psJq59YIbp3Z
        .name("entities").description("Specific entities to breed.").defaultValue().onlyAttackable().build());
    private final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder() // was: SOYyh5IPg26f7F
        .name("distance").description("Maximum distance to pathfind to new mobs.").defaultValue(32.0).sliderRange(4.0, 128.0).decimalPlaces(1).build());

    private final Map<UUID, Long> bredCooldowns = new HashMap<>(); // was: rKbT3Ifwo (uuid -> last-bred millis)
    private UUID pathTargetUuid = null;                            // was: r7hOYIKN2

    public AutoBreeder() {
        super(musheor.AUTOMATION, "auto-breeder", "Automatically breeds specific animals.");
    }

    @Override
    public void onActivate() {
        this.bredCooldowns.clear();
        this.pathTargetUuid = null;
    }

    @Override
    public void onDeactivate() {
        PathingHelper.cancelEverything();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        long now = System.currentTimeMillis();
        this.bredCooldowns.entrySet().removeIf(e -> now - e.getValue() >= 300000L);
        List<AnimalEntity> candidates = new ArrayList<>();

        for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof AnimalEntity animal
                && this.entities.get().contains(animal.getType())
                && !this.bredCooldowns.containsKey(animal.getUuid())
                && !animal.isBaby()
                && animal.isBreedingItem(this.mc.player.getMainHandStack())
                && PlayerUtils.isWithin(animal, this.distance.get())) {
                candidates.add(animal);
            }
        }

        candidates.sort(Comparator.comparingDouble(a -> a.squaredDistanceTo(this.mc.player)));
        if (!candidates.isEmpty()) {
            AnimalEntity first = candidates.getFirst();
            if (!first.isBreedingItem(this.mc.player.getMainHandStack())) {
                for (int slot = 0; slot < this.mc.player.getInventory().main.size(); slot++) {
                    ItemStack stack = this.mc.player.getInventory().getStack(slot);
                    if (first.isBreedingItem(stack) && slot < 8) {
                        this.mc.player.getInventory().selectedSlot = slot;
                    } else {
                        if (!first.isBreedingItem(stack)) {
                            this.info("No breeding items found in inventory, disabling...");
                            this.toggle();
                            return;
                        }
                        InventoryManager.moveToHotbar(stack.getItem());
                    }
                }
                return;
            }
        }

        if (!candidates.isEmpty()) {
            AnimalEntity animal = candidates.getFirst();
            if (animal.isInRange(this.mc.player, 2.5)) {
                PathingHelper.cancelEverything();
                this.pathTargetUuid = null;
                float yaw = (float) Rotations.getYaw(animal);
                float pitch = (float) Rotations.getPitch(animal);
                Rotations.rotate(yaw, pitch);
                this.mc.interactionManager.sendSequencedPacket(this.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, yaw, pitch));
                VersionHelper.get().interactEntityAt(animal, Hand.MAIN_HAND);
                this.mc.interactionManager.sendSequencedPacket(this.mc.world, sequence -> PlayerInteractEntityC2SPacket.interact(animal, false, Hand.MAIN_HAND));
                this.bredCooldowns.put(animal.getUuid(), System.currentTimeMillis());
            } else {
                UUID uuid = animal.getUuid();
                if (!uuid.equals(this.pathTargetUuid) || !PathingHelper.isPathing()) {
                    this.pathTargetUuid = uuid;
                    PathingHelper.setGoal(new GoalBlock(animal.getBlockPos()));
                }
            }
        }
    }
}
