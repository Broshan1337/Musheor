// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.VGn8YrSOy)
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
import net.minecraft.server.command.ServerCommandSource;

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
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        // .kit create <name>
        builder.then(KitCommand.literal("create")
            .then(KitCommand.argument("name", (ArgumentType) StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString((CommandContext) ctx, "name");
                    ((ReKit) Modules.get().get(ReKit.class)).saveKit(name); // was: FvaNWO(String)
                    return 1;
                })));

        // .kit load <name>
        builder.then(KitCommand.literal("load")
            .then(KitCommand.argument("name", (ArgumentType) StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString((CommandContext) ctx, "name");
                    ReKit reKit = (ReKit) Modules.get().get(ReKit.class);
                    if (!reKit.hasKit(name)) { // was: rKbT3Ifwo(String)
                        ChatUtils.error("Kit '§b" + name + "§c' not found. Use §b.kit create§c to save one.", new Object[0]);
                        return 1;
                    }
                    reKit.loadout.set(name);       // was: psJq59YIbp3Z
                    reKit.loadKit(name);           // was: Q90GLXQ0Pef(String) — loads from disk; the sort runs on next container open
                    ChatUtils.info("Active kit set to '§b" + name + "§r'.", new Object[0]);
                    return 1;
                })));

        // .kit delete <name>
        builder.then(KitCommand.literal("delete")
            .then(KitCommand.argument("name", (ArgumentType) StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString((CommandContext) ctx, "name");
                    ((ReKit) Modules.get().get(ReKit.class)).deleteKit(name); // was: SOYyh5IPg26f7F(String)
                    return 1;
                })));

        // .kit list
        builder.then(KitCommand.literal("list")
            .executes(ctx -> {
                List<String> kits = ((ReKit) Modules.get().get(ReKit.class)).listKits(); // was: Q90GLXQ0Pef()
                if (kits.isEmpty()) {
                    ChatUtils.info("No kits saved.", new Object[0]);
                } else {
                    ChatUtils.info("Saved kits: §b" + String.join("§r, §b", kits) + "§r", new Object[0]);
                }
                return 1;
            }));
    }
}
