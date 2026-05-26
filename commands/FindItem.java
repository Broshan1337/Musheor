// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import musheor.utils.WorldUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.CommandRegistryAccess;

/**
 * .find <item>
 *
 * Enables Baritone-based item collection for the specified item type.
 * Delegates to WorldUtils.findAndPickupItem() (was: xG2PP8jo4RWLS).
 */
public class FindItem extends Command {
    public FindItem() {
        super("find", "Find and pickup specific items using Baritone.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.then(FindItem.argument("item", (ArgumentType) ItemStackArgumentType.itemStack((CommandRegistryAccess) REGISTRY_ACCESS))
            .executes(ctx -> {
                ItemStack stack = ItemStackArgumentType.getItemStackArgument((CommandContext) ctx, "item").createStack(1, false);
                if (stack != null && stack.getItem() != Items.AIR) {
                    WorldUtils.findAndPickupItem(stack.getItem()); // was: xG2PP8jo4RWLS(Item)
                }
                return 1;
            }));
    }
}