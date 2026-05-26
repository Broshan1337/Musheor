// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import musheor.utils.InventoryManager;
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
                    int count = InventoryManager.countItemIncludingShulkers(stack.getItem()); // was: ZbTtF5KYyGL9YXed
                    this.info("%s %s items found!", new Object[]{count, stack.getItem().getName().getString()});
                }
                return 1;
            }));
    }
}