// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.class_1304;   // EquipmentSlot
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.Items;   // Items

/**
 * Automatically swaps the worn elytra with a fresh one from the player's
 * inventory when its remaining durability drops below the configured threshold.
 */
public class ElytraSwap extends Module {
    private final Setting<Integer> durabilityThreshold;

    public ElytraSwap() {
        super(musheor.MAIN, "elytra-swap", "Automatically swaps your elytra when low durability");
        this.durabilityThreshold = this.settings.getDefaultGroup().add(
            new IntSetting.Builder()
                .name("durability-threshold")
                .defaultValue(5)
                .sliderRange(1, 100)
                .build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre pre) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) return;

        // Only act if an elytra is equipped in the CHEST slot
        if (!this.mc.player.method_6118(class_1304.field_6174).getStack() // getEquippedStack(CHEST).getItem()
                .equals(Items.field_8833)) return;  // Items.ELYTRA

        // Calculate remaining durability = maxDurability − damage
        int worn = this.mc.player.method_6118(class_1304.field_6174).method_7936() // getMaxDamage
                 - this.mc.player.method_6118(class_1304.field_6174).method_7919(); // getDamage

        if (worn <= (Integer) this.durabilityThreshold.get()) {
            // Find a fresh elytra in the main inventory
            for (int i = 0; i < this.mc.player.getId().field_7547.size(); ++i) {
                ItemStack stack = this.mc.player.getId().method_5438(i); // getStack(i)
                int remaining = stack.method_7936() - stack.method_7919(); // maxDamage − damage
                if (!stack.getStack().equals(Items.field_8833)
                        || remaining <= (Integer) this.durabilityThreshold.get()) continue;
                // Move fresh elytra to chest armor slot (slot index 2)
                InvUtils.move().from(this.mc.player.getId().method_7395(stack)).toArmor(2);
                break;
            }
        }
    }
}
