// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import musheor.utils.WorldUtils;
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.Items;   // Items
import net.minecraft.GuiGraphics;   // CommandSource
import net.minecraft.class_2287;   // ItemStackArgument
import net.minecraft.class_7157;   // RegistryWrapper

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
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        builder.then(FindItem.argument("item", (ArgumentType) class_2287.method_9776((class_7157) REGISTRY_ACCESS))
            .executes(ctx -> {
                ItemStack stack = class_2287.method_9777((CommandContext) ctx, "item").method_9781(1, false);
                if (stack != null && stack.getStack() != Items.field_8162) {
                    WorldUtils.findAndPickupItem(stack.getStack()); // was: xG2PP8jo4RWLS(Item)
                }
                return 1;
            }));
    }
}
