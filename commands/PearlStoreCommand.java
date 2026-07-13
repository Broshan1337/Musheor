// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.zQn6)
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import musheor.compat.VersionHelper;
import musheor.utils.PearlStore;
import net.minecraft.server.command.ServerCommandSource;

/**
 * .pearlstore clear|remove <player>
 *
 * Utility command for managing the PearlStore (tracked ender pearls).
 *   clear            — removes all tracked pearls
 *   remove <player>  — removes all pearls owned by the given player name
 */
public class PearlStoreCommand extends Command {
    public PearlStoreCommand() {
        super("pearlstore", "Util command for Pearl Store", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.then(PearlStoreCommand.literal("clear")
            .executes(ctx -> {
                PearlStore.clearAll(); // was: Mj77A.Q90GLXQ0Pef()
                return 1;
            }));

        builder.then(PearlStoreCommand.literal("remove")
            .then(PearlStoreCommand.argument("player", (ArgumentType) PlayerListEntryArgumentType.create())
                .executes(ctx -> {
                    PearlStore.removeByOwner(VersionHelper.get().getName(ctx)); // was: Mj77A.FvaNWO(String)
                    return 1;
                })));
    }
}
