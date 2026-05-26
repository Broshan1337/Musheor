// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.io.File;
import java.io.IOException;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.server.command.ServerCommandSource;

/**
 * .folder [subfolder]
 *
 * Opens the .minecraft directory (or a named subdirectory) in Windows Explorer.
 * Autocompletes to the list of top-level subdirectories inside .minecraft.
 */
public class FolderCommand extends Command {
    public FolderCommand() {
        super("folder", "Opens a folder inside your .minecraft directory.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        // No argument → open .minecraft root
        builder.executes(ctx -> {
            openFolder(FolderCommand.mc.runDirectory); // was: field_1697
            return 1;
        });

        // Optional subfolder argument with autocomplete
        builder.then(FolderCommand.argument("folder", (ArgumentType) StringArgumentType.word())
            .suggests((ctx, suggestionsBuilder) -> {
                File mcDir = FolderCommand.mc.runDirectory; // was: field_1697
                File[] subdirs = mcDir.listFiles(File::isDirectory);
                if (subdirs != null) {
                    for (File dir : subdirs) {
                        suggestionsBuilder.suggest(dir.getName());
                    }
                }
                return suggestionsBuilder.buildFuture();
            })
            .executes(ctx -> {
                String folderName = StringArgumentType.getString((CommandContext) ctx, "folder");
                File target = new File(FolderCommand.mc.runDirectory, folderName); // was: field_1697
                if (!target.exists() || !target.isDirectory()) {
                    this.error("Folder not found: " + folderName, new Object[0]);
                    return 1;
                }
                openFolder(target);
                return 1;
            }));
    }

    /** Launches Windows Explorer at the given directory path. */
    private void openFolder(File folder) { // was: jOdDDFXSeWl4(File)
        try {
            new ProcessBuilder("explorer.exe", folder.getAbsolutePath()).start();
        } catch (IOException e) {
            this.error("Failed to open folder: " + e.getMessage(), new Object[0]);
        }
    }
}