// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.class_1268;   // Hand
import net.minecraft.GuiGraphics;   // CommandSource
import net.minecraft.class_2596;   // Packet
import net.minecraft.class_2793;   // TeleportConfirmC2SPacket
import net.minecraft.class_2886;   // PlayerMoveC2SPacket

/**
 * .teleportconfirm <id>
 *
 * Manually sends a TeleportConfirm packet followed by a position packet.
 * Useful for debugging teleport desync issues.
 */
public class TeleportConfirm extends Command {
    public TeleportConfirm() {
        super("teleportconfirm", "Sends an Teleport Confirm packet with a specific id.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        builder.then(Command.argument("id", (ArgumentType) IntegerArgumentType.integer())
            .executes(ctx -> {
                Integer id = (Integer) ctx.getArgument("id", Integer.class);
                this.info("teleporting with " + id, new Object[0]);
                // Send TeleportConfirmC2SPacket
                TeleportConfirm.mc.player.field_3944.method_52787( // networkHandler.sendPacket
                    (class_2596) new class_2793(id.intValue()));
                // Send PlayerMoveC2SPacket (LookOnly) with current yaw/pitch
                TeleportConfirm.mc.player.field_3944.method_52787(
                    (class_2596) new class_2886(
                        class_1268.field_5808,                         // Hand.MAIN_HAND
                        0,
                        TeleportConfirm.mc.player.method_36454(),  // getYaw()
                        TeleportConfirm.mc.player.method_36455())); // getPitch()
                return 1;
            }));
    }
}
