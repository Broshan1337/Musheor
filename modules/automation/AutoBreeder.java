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
import net.minecraft.class_1268;   // Hand
import net.minecraft.class_1297;   // Entity
import net.minecraft.class_1299;   // EntityType
import net.minecraft.class_1429;   // AnimalEntity
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.class_2596;   // Packet
import net.minecraft.class_2824;   // PlayerInteractEntityC2SPacket
import net.minecraft.class_2886;   // PlayerMoveC2SPacket (look)
import net.minecraft.MinecraftClient;    // MinecraftClient

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
    private final MinecraftClient mc = MinecraftClient.method_1551(); // was: BX92A0OIIvD9
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<Set<class_1299<?>>> entities = this.sgGeneral.add(
        new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Specific entities to breed.")
            .defaultValue(new class_1299[0])
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
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) return;

        long now = System.currentTimeMillis();
        // Remove breed entries older than 5 minutes
        this.breedCooldowns.entrySet().removeIf(e -> now - e.getValue() >= 300_000L);

        // Collect candidate animals
        ArrayList<class_1429> candidates = new ArrayList<class_1429>();
        for (class_1297 entity : this.mc.world.method_18112()) { // getEntities()
            if (!(entity instanceof class_1429)) continue;
            class_1429 animal = (class_1429) entity;
            if (!((Set<?>) this.entities.get()).contains(animal.method_5864())) continue; // getType()
            if (this.breedCooldowns.containsKey(animal.method_5667())) continue;          // getUuid()
            if (animal.method_6109()) continue;                                             // isBaby()
            if (!animal.method_6481(this.mc.player.method_6047())) continue;          // isBreedingItem(mainHand)
            if (!PlayerUtils.isWithin((class_1297) animal, (double) ((Double) this.distance.get()))) continue;
            candidates.add(animal);
        }
        candidates.sort(Comparator.comparingDouble(a -> a.method_5858((class_1297) this.mc.field_1724))); // squaredDistanceTo

        if (!candidates.isEmpty()) {
            class_1429 nearest = candidates.getFirst();

            // If the animal can't be fed with the current main-hand item, find the right food
            if (!nearest.method_6481(this.mc.player.method_6047())) {
                for (int i = 0; i < this.mc.player.getId().field_7547.size(); ++i) {
                    ItemStack stack = this.mc.player.getId().method_5438(i);
                    if (nearest.method_6481(stack) && i < 8) {
                        this.mc.player.getId().field_7545 = i; // selectedSlot
                    } else if (nearest.method_6481(stack)) {
                        InventoryManager.equipItem(stack.getStack()); // was: UgB10d(Item)
                    } else {
                        this.info("No breeding items found in inventory, disabling...", new Object[0]);
                        this.toggle();
                        return;
                    }
                }
                return;
            }

            // Attempt to interact if within range, otherwise path toward the animal
            if (nearest.method_24516((class_1297) this.mc.field_1724, 2.5)) { // isWithinInteractionRange
                PathingHelper.stopPathing();
                this.currentTargetUuid = null;
                float yaw   = (float) Rotations.getYaw((class_1297) nearest);
                float pitch = (float) Rotations.getPitch((class_1297) nearest);
                Rotations.rotate((double) yaw, (double) pitch);
                this.mc.field_1761.method_41931(this.mc.field_1687, n -> new class_2886(class_1268.field_5808, n, yaw, pitch)); // PlayerMoveC2SPacket look
                VersionHelper.get().interactEntityAt(nearest, class_1268.field_5808);
                this.mc.field_1761.method_41931(this.mc.field_1687,
                    n -> class_2824.method_34207((class_1297) nearest, false, class_1268.field_5808)); // UseEntityPacket
                this.breedCooldowns.put(nearest.method_5667(), System.currentTimeMillis());
            } else {
                UUID uid = nearest.method_5667();
                if (!uid.equals(this.currentTargetUuid) || !PathingHelper.isAlreadyPathing()) {
                    this.currentTargetUuid = uid;
                    PathingHelper.setBaritoneGoal((Goal) new GoalBlock(nearest.getBlockPos())); // getBlockPos()
                }
            }
        }
    }
}
