// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.FDb5)
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import musheor.modules.automation.InventoryManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.CommandRegistryAccess;

/**
 * .countItems <item>
 *
 * Counts how many of the specified item the player has across their inventory,
 * including inside shulker boxes.
 *
 * Note: in 1.6.1 the item-counting helper lives on the InventoryManager module
 * (was: obf.e1lTf) rather than a standalone util class.
 */
public class CountItems extends Command {
    public CountItems() {
        super("countItems", "Counts how many items a player has in the inventory (including shulker boxes)", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.then(CountItems.argument("item", (ArgumentType) ItemStackArgumentType.itemStack((CommandRegistryAccess) REGISTRY_ACCESS))
            .executes(ctx -> {
                ItemStack stack = ItemStackArgumentType.getItemStackArgument((CommandContext) ctx, "item").createStack(1, false);
                if (stack != null && stack.getItem() != Items.AIR) {
                    int count = InventoryManager.countItemIncludingShulkers(stack.getItem()); // was: e1lTf.rKbT3Ifwo(Item)
                    this.info("%s %s items found!", new Object[]{count, stack.getItem().getName().getString()});
                }
                return 1;
            }));
    }
}
