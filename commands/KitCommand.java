// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import musheor.modules.automation.ReKit;
import net.minecraft.GuiGraphics;  // CommandSource

/**
 * .kit create|load|delete|list [name]
 *
 * Manages ReKit loadout slots:
 *   create <name>  — saves current inventory layout as a named kit
 *   load   <name>  — loads and applies the named kit
 *   delete <name>  — removes the named kit
 *   list           — prints all saved kit names
 */
public class KitCommand extends Command {
    public KitCommand() {
        super("kit", "Manage ReKit loadouts. Usage: .kit create|load|delete|list [name]", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        // .kit create <name>
        builder.then(KitCommand.literal("create")
            .then(KitCommand.argument("name", (ArgumentType) StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString((CommandContext) ctx, "name");
                    ((ReKit) Modules.get().get(ReKit.class)).saveKit(name); // was: TAdu5cndwWu3A1(String)
                    return 1;
                })));

        // .kit load <name>
        builder.then(KitCommand.literal("load")
            .then(KitCommand.argument("name", (ArgumentType) StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString((CommandContext) ctx, "name");
                    ReKit reKit = (ReKit) Modules.get().get(ReKit.class);
                    if (!reKit.hasKit(name)) { // was: KP44bk(String)
                        ChatUtils.error("Kit '\u00a7b" + name + "\u00a7c' not found. Use \u00a7b.kit create\u00a7c to save one.", new Object[0]);
                        return 1;
                    }
                    reKit.loadout.set(name);
                    reKit.applyKit(name); // was: vgrtgn5(String)
                    ChatUtils.info("Active kit set to '\u00a7b" + name + "\u00a7r'.", new Object[0]);
                    return 1;
                })));

        // .kit delete <name>
        builder.then(KitCommand.literal("delete")
            .then(KitCommand.argument("name", (ArgumentType) StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString((CommandContext) ctx, "name");
                    ((ReKit) Modules.get().get(ReKit.class)).deleteKit(name); // was: UgB10d(String)
                    return 1;
                })));

        // .kit list
        builder.then(KitCommand.literal("list")
            .executes(ctx -> {
                List<String> kits = ((ReKit) Modules.get().get(ReKit.class)).listKits(); // was: vV6cbpE7KWBI
                if (kits.isEmpty()) {
                    ChatUtils.info("No kits saved.", new Object[0]);
                } else {
                    ChatUtils.info("Saved kits: \u00a7b" + String.join("\u00a7r, \u00a7b", kits) + "\u00a7r", new Object[0]);
                }
                return 1;
            }));
    }
}
