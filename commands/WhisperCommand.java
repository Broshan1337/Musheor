// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.systems.modules.Modules;
import musheor.compat.VersionHelper;
import musheor.modules.tech.Whisper;
import musheor.utils.PlayerUtils;
import net.minecraft.GuiGraphics;  // CommandSource

/**
 * .tp [player]
 *
 * Sends a random teleport-request message (".tp <token>") to the given player.
 * If no player is specified, re-uses the last targeted player.
 *
 * The token length is read from the Whisper module's "length" setting.
 */
public class WhisperCommand extends Command {
    /** Last targeted player name; persists between invocations. */
    public static String lastTarget; // was: ZbTtF5KYyGL9YXed

    /** Cached token length from the Whisper module setting. */
    static int tokenLength; // was: e5oi2ZF

    static {
        tokenLength = (Integer) ((Whisper) Modules.get().get(Whisper.class)).settings.get("length").get();
    }

    public WhisperCommand() {
        super("tp", "Sends a random message to another player.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        // .tp <player> — update the target then send
        builder.then(WhisperCommand.argument("player", (ArgumentType) PlayerListEntryArgumentType.create())
            .executes(ctx -> {
                if (VersionHelper.get().getName(ctx) != null) {
                    lastTarget = VersionHelper.get().getName(ctx);
                }
                tokenLength = (Integer) ((Whisper) Modules.get().get(Whisper.class)).settings.get("length").get();
                PlayerUtils.sendTeleportMessage(lastTarget, tokenLength); // was: jOdDDFXSeWl4(String,int)
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
