// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import musheor.compat.XearoHelper;
import net.minecraft.GuiGraphics;  // CommandSource

/**
 * .xaero deleteAllTempWaypoints
 *
 * Delegates to XearoHelper to remove all temporary Xaero's Minimap waypoints.
 */
public class XaeroUtilsCommand extends Command {
    public XaeroUtilsCommand() {
        super("xaero", "Xearo Utils Command", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        builder.then(XaeroUtilsCommand.literal("deleteAllTempWaypoints")
            .executes(ctx -> {
                XearoHelper.get().deleteAllTempWaypoints();
                ChatUtils.info("Deleted all temp waypoints.", new Object[0]);
                return 1;
            }));
    }
}
