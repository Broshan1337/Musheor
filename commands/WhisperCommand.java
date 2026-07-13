// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.JUKhlfIQiiGS)
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.compat.VersionHelper;
import musheor.modules.tech.Whisper;
import musheor.utils.PlayerUtils;
import net.minecraft.server.command.ServerCommandSource;

/**
 * .tp [player]
 *
 * Sends a random teleport-request message ("!tp <token>") to the given player.
 * If no player is specified, re-uses the last targeted player.
 *
 * The token length is read from the Whisper module's "length" setting.
 */
public class WhisperCommand extends Command {
    /** Last targeted player name; persists between invocations. */
    public static String lastTarget; // was: FvaNWO

    /** Cached token length from the Whisper module setting. */
    static int tokenLength = (Integer) ((Whisper) Modules.get().get(Whisper.class)).settings.get("length").get(); // was: Q90GLXQ0Pef

    public WhisperCommand() {
        super("tp", "Sends a random message to another player.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        // .tp <player> — update the target then send
        builder.then(WhisperCommand.argument("player", (ArgumentType) PlayerListEntryArgumentType.create())
            .executes(ctx -> {
                if (VersionHelper.get().getName(ctx) != null) {
                    lastTarget = VersionHelper.get().getName(ctx);
                }
                tokenLength = (Integer) ((Whisper) Modules.get().get(Whisper.class)).settings.get("length").get();
                PlayerUtils.sendTeleportMessage(lastTarget, tokenLength); // was: CUfICea7s.FvaNWO(String,int)
                return 1;
            }));

        // .tp (no args) — re-send to last target
        builder.executes(ctx -> {
            tokenLength = (Integer) ((Whisper) Modules.get().get(Whisper.class)).settings.get("length").get();
            PlayerUtils.sendTeleportMessage(lastTarget, tokenLength);
            return 1;
        });
    }
}
