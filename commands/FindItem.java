// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.D0Jn)
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
 * Delegates to WorldUtils.findAndPickupItem() (was: HFbqnT1FEh2q.Q90GLXQ0Pef).
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
                    WorldUtils.findAndPickupItem(stack.getItem()); // was: HFbqnT1FEh2q.Q90GLXQ0Pef(Item)
                }
                return 1;
            }));
    }
}
