// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import musheor.utils.InventoryManager;
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.Items;   // Items
import net.minecraft.GuiGraphics;   // CommandSource
import net.minecraft.class_2287;   // ItemStackArgument (item argument)
import net.minecraft.class_7157;   // RegistryWrapper

/**
 * .countItems <item>
 *
 * Counts how many of the specified item the player has across their inventory,
 * including inside shulker boxes.
 */
public class CountItems extends Command {
    public CountItems() {
        super("countItems", "Counts how many items a player has in the inventory (including shulker boxes)", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        builder.then(CountItems.argument("item", (ArgumentType) class_2287.method_9776((class_7157) REGISTRY_ACCESS))
            .executes(ctx -> {
                ItemStack stack = class_2287.method_9777((CommandContext) ctx, "item").method_9781(1, false);
                if (stack != null && stack.getStack() != Items.field_8162) { // !Items.AIR
                    int count = InventoryManager.countItemIncludingShulkers(stack.getStack()); // was: ZbTtF5KYyGL9YXed
                    this.info("%s %s items found!", new Object[]{count, stack.getStack().method_63680().getString()});
                }
                return 1;
            }));
    }
}
