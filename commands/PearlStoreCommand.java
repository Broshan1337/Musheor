// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import musheor.compat.VersionHelper;
import musheor.utils.PearlStore;
import net.minecraft.GuiGraphics;  // CommandSource

/**
 * .pearlstore clear|remove <player>
 *
 * Utility command for managing the PearlStore.
 *   clear            — removes all tracked pearls
 *   remove <player>  — removes all pearls owned by the given player name
 */
public class PearlStoreCommand extends Command {
    public PearlStoreCommand() {
        super("pearlstore", "Util command for Pearl Store", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        builder.then(PearlStoreCommand.literal("clear")
            .executes(ctx -> {
                PearlStore.clearAll(); // was: TMdT6kYQyv0It
                return 1;
            }));

        builder.then(PearlStoreCommand.literal("remove")
            .then(PearlStoreCommand.argument("player", (ArgumentType) PlayerListEntryArgumentType.create())
                .executes(ctx -> {
                    PearlStore.removeByOwner(VersionHelper.get().getName(ctx)); // was: xG2PP8jo4RWLS
                    return 1;
                })));
    }
}
