// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.modules.automation;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import java.util.Comparator;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import musheor.utils.PlayerUtils;
import musheor.utils.system.MusheorSystem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * "pickup-item" — a helper that scans for the configured dropped item nearby and
 * Baritone-paths to the closest one, picking them up until the inventory is full or
 * none remain. Used by the highway paver to reclaim dropped obsidian.
 */
public class GatherItem extends Module {
    private static boolean paveAfterwards = true;   // was: FvaNWO (passed to PlayerUtils.unusedPaveHook on finish)

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup(); // was: Q90GLXQ0Pef
    private final Setting<Item> item = sgGeneral.add(new ItemSetting.Builder() // was: psJq59YIbp3Z
        .name("item").description("Item to gather").defaultValue(Items.OBSIDIAN).build());
    private final Setting<Integer> searchRadius = sgGeneral.add(new IntSetting.Builder() // was: SOYyh5IPg26f7F
        .name("search-radius").description("Maximum radius to scan for items").defaultValue(16).sliderRange(1, 128).build());
    private final Setting<Boolean> ignoreBelowY120 = sgGeneral.add(new BoolSetting.Builder() // was: rKbT3Ifwo
        .name("ignore-below-y120").description("Ignores items below Y-level 120 to prevent the paver from digging underground")
        .defaultValue(true).build());

    private ItemEntity targetItem = null;   // was: r7hOYIKN2
    private BlockPos currentGoal = null;     // was: oZHMlTL
    private int rescanTimer = 20;            // was: xQr5FhbwpQPWgIQ

    public GatherItem() {
        super(musheor.AUTOMATION, "pickup-item", "A helper module that finds and picks up nearby items.");
    }

    @Override
    public void onActivate() {
        this.targetItem = null;
        this.currentGoal = null;
        this.rescanTimer = 20;
    }

    @Override
    public void onDeactivate() {
        PlayerUtils.cancelPathing();
        this.targetItem = null;
        this.currentGoal = null;
    }

    /** Finds the closest matching dropped item within the search radius, or null. */
    private ItemEntity findNearestItem() { // was: FvaNWO()
        if (this.mc.player == null || this.mc.world == null) return null;
        int radius = this.searchRadius.get();
        Box searchArea = new Box(
            this.mc.player.getX() - radius, this.mc.player.getY() - radius, this.mc.player.getZ() - radius,
            this.mc.player.getX() + radius, this.mc.player.getY() + radius, this.mc.player.getZ() + radius);
        double radiusSq = (double) radius * radius;
        return this.mc.world.getEntitiesByClass(ItemEntity.class, searchArea,
                e -> e.getStack().getItem() == this.item.get()
                    && e.squaredDistanceTo(this.mc.player) <= radiusSq
                    && (!this.ignoreBelowY120.get() || e.getBlockY() >= 120))
            .stream()
            .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(this.mc.player)))
            .orElse(null);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) { // was: FvaNWO(Pre)
        if (this.mc.player == null || this.mc.world == null) return;
        Module echestFarmer = Modules.get().get(EchestFarmer.class);
        if (echestFarmer != null && echestFarmer.isActive() && HighwayBuilder.isEchestFarmerActive()) return;

        if (InventoryManager.countEmptyInventorySlots() == 0) {
            PlayerUtils.unusedPaveHook(paveAfterwards);
            MusheorSystem.debug("Inventory full, toggling off gather item...");
            this.toggle();
            return;
        }

        this.rescanTimer++;
        if (this.rescanTimer >= 20 || this.targetItem == null || !this.targetItem.isAlive()) {
            this.targetItem = this.findNearestItem();
            this.rescanTimer = 0;
        }

        if (this.targetItem == null) {
            ChatUtils.info("No more " + this.item.get().getName().getString() + " found nearby.");
            PlayerUtils.unusedPaveHook(paveAfterwards);
            this.toggle();
        } else {
            BlockPos itemPos = this.targetItem.getBlockPos();
            if (!itemPos.equals(this.currentGoal)) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(itemPos));
                this.currentGoal = itemPos;
            }
        }
    }
}
