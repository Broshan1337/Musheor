// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.util.Hand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

/**
 * .teleportconfirm <id>
 *
 * Manually sends a TeleportConfirm packet followed by a PlayerInteractItem packet
 * with current yaw/pitch. Useful for debugging teleport desync issues.
 */
public class TeleportConfirm extends Command {
    public TeleportConfirm() {
        super("teleportconfirm", "Sends an Teleport Confirm packet with a specific id.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.then(Command.argument("id", (ArgumentType) IntegerArgumentType.integer())
            .executes(ctx -> {
                Integer id = (Integer) ctx.getArgument("id", Integer.class);
                this.info("teleporting with " + id, new Object[0]);
                // Send TeleportConfirmC2SPacket
                TeleportConfirm.mc.player.networkHandler.sendPacket(
                    (Packet) new TeleportConfirmC2SPacket(id.intValue()));
                // Send PlayerInteractItemC2SPacket with current yaw/pitch
                TeleportConfirm.mc.player.networkHandler.sendPacket(
                    (Packet) new PlayerInteractItemC2SPacket(
                        Hand.MAIN_HAND,
                        0,
                        TeleportConfirm.mc.player.getYaw(),
                        TeleportConfirm.mc.player.getPitch()));
                return 1;
            }));
    }
}