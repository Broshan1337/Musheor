// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import musheor.compat.VersionHelper;
import musheor.musheor;
import musheor.utils.InventoryManager;
import musheor.utils.internal.PathingHelper;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

/**
 * Automatically breeds the selected animal types.
 *
 * Each tick:
 *   1. Expires breed-cooldown entries older than 5 minutes
 *   2. Collects nearby animals that can be fed, sorts by distance
 *   3. If the needed food item is not in-hotbar, equips or picks it up
 *   4. If in range (2.5 blocks), rotates to the animal and sends UseEntityAt packets
 *   5. If out of range, paths to the animal via Baritone
 */
public class AutoBreeder extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance(); // was: BX92A0OIIvD9
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = this.sgGeneral.add(
        new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Specific entities to breed.")
            .defaultValue(new EntityType[0])
            .onlyAttackable()
            .build());

    private final Setting<Double> distance = this.sgGeneral.add(
        new DoubleSetting.Builder()
            .name("distance")
            .description("Maximum distance to pathfind to new mobs.")
            .defaultValue(32.0).sliderRange(4.0, 128.0).decimalPlaces(1)
            .build());

    /** UUID → time of last breed attempt; used to enforce the 5-minute cooldown. */
    private final Map<UUID, Long> breedCooldowns = new HashMap<UUID, Long>(); // was: L5CF0C6jx0T17H4I

    /** UUID of the entity currently being pathed toward. */
    private UUID currentTargetUuid = null; // was: Y9BgxR

    public AutoBreeder() {
        super(musheor.AUTOMATION, "auto-breeder", "Automatically breeds specific animals.");
    }

    @Override
    public void onActivate() {
        this.breedCooldowns.clear();
        this.currentTargetUuid = null;
    }

    @Override
    public void onDeactivate() {
        PathingHelper.stopPathing(); // was: xRVyNRV3cB7
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.player == null || this.mc.world == null) return;

        long now = System.currentTimeMillis();
        // Remove breed entries older than 5 minutes
        this.breedCooldowns.entrySet().removeIf(e -> now - e.getValue() >= 300_000L);

        // Collect candidate animals
        ArrayList<AnimalEntity> candidates = new ArrayList<AnimalEntity>();
        for (Entity entity : this.mc.world.getEntities()) {
            if (!(entity instanceof AnimalEntity)) continue;
            AnimalEntity animal = (AnimalEntity) entity;
            if (!((Set<?>) this.entities.get()).contains(animal.getType())) continue;
            if (this.breedCooldowns.containsKey(animal.getUuid())) continue;
            if (animal.isBaby()) continue;
            if (!animal.isBreedingItem(this.mc.player.getMainHandStack())) continue;
            if (!PlayerUtils.isWithin((Entity) animal, (double) ((Double) this.distance.get()))) continue;
            candidates.add(animal);
        }
        candidates.sort(Comparator.comparingDouble(a -> a.squaredDistanceTo((Entity) this.mc.player)));

        if (!candidates.isEmpty()) {
            AnimalEntity nearest = candidates.getFirst();

            // If the animal can't be fed with the current main-hand item, find the right food
            if (!nearest.isBreedingItem(this.mc.player.getMainHandStack())) {
                for (int i = 0; i < this.mc.player.getInventory().main.size(); ++i) {
                    ItemStack stack = this.mc.player.getInventory().getStack(i);
                    if (nearest.isBreedingItem(stack) && i < 8) {
                        this.mc.player.getInventory().selectedSlot = i;
                    } else if (nearest.isBreedingItem(stack)) {
                        InventoryManager.equipItem(stack.getItem());
                    } else {
                        this.info("No breeding items found in inventory, disabling...", new Object[0]);
                        this.toggle();
                        return;
                    }
                }
                return;
            }

            // Attempt to interact if within range, otherwise path toward the animal
            if (nearest.isInRange((Entity) this.mc.player, 2.5)) {
                PathingHelper.stopPathing();
                this.currentTargetUuid = null;
                float yaw   = (float) Rotations.getYaw((Entity) nearest);
                float pitch = (float) Rotations.getPitch((Entity) nearest);
                Rotations.rotate((double) yaw, (double) pitch);
                // Send PlayerInteractItemC2SPacket (includes yaw/pitch for server-side validation in 1.21)
                this.mc.interactionManager.sendSequencedPacket(this.mc.world, n -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n, yaw, pitch));
                VersionHelper.get().interactEntityAt(nearest, Hand.MAIN_HAND);
                this.mc.interactionManager.sendSequencedPacket(this.mc.world,
                    n -> PlayerInteractEntityC2SPacket.interact((Entity) nearest, false, Hand.MAIN_HAND));
                this.breedCooldowns.put(nearest.getUuid(), System.currentTimeMillis());
            } else {
                UUID uid = nearest.getUuid();
                if (!uid.equals(this.currentTargetUuid) || !PathingHelper.isAlreadyPathing()) {
                    this.currentTargetUuid = uid;
                    PathingHelper.setBaritoneGoal((Goal) new GoalBlock(nearest.getBlockPos()));
                }
            }
        }
    }
}