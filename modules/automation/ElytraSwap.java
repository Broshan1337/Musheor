// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.modules.automation;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import musheor.musheor;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

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
        if (this.mc.player == null || this.mc.world == null) return;

        // Only act if an elytra is equipped in the CHEST slot
        if (!this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getStack() // getEquippedStack(CHEST).getItem()
                .equals(Items.ELYTRA)) return;  // Items.ELYTRA

        // Calculate remaining durability = maxDurability − damage
        int worn = this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getMaxDamage() // getMaxDamage
                 - this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getDamage(); // getDamage

        if (worn <= (Integer) this.durabilityThreshold.get()) {
            // Find a fresh elytra in the main inventory
            for (int i = 0; i < this.mc.player.getInventory().main.size(); ++i) {
                ItemStack stack = this.mc.player.getInventory().getStack(i); // getStack(i)
                int remaining = stack.getMaxDamage() - stack.getDamage(); // maxDamage − damage
                if (!stack.getItem().equals(Items.ELYTRA)
                        || remaining <= (Integer) this.durabilityThreshold.get()) continue;
                // Move fresh elytra to chest armor slot (slot index 2)
                InvUtils.move().from(this.mc.player.getInventory().getSlotWithStack(stack)).toArmor(2);
                break;
            }
        }
    }
}
