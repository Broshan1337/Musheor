// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud.actions;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import musheor.compat.VersionHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;

/** Button action that runs a Meteor command ({@code .}), a server command ({@code /}), or plain text as a command. */
public class RunCommandAction implements ButtonAction {
    public String command;                                                    // was: FvaNWO
    private static final MinecraftClient mc = MinecraftClient.getInstance();   // was: Q90GLXQ0Pef

    public RunCommandAction(String command) {
        this.command = command;
    }

    @Override
    public void execute(ScreenHandler handler) { // was: FvaNWO(ScreenHandler)
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (this.command.startsWith(".")) {
            ChatUtils.sendPlayerMsg(this.command);
        } else if (this.command.startsWith("/")) {
            VersionHelper.get().sendCommand(this.command.substring(1));
        } else {
            VersionHelper.get().sendCommand(this.command);
        }
    }

    @Override
    public String getType() { // was: FvaNWO()
        return "run_command";
    }

    @Override
    public NbtCompound toTag() { // was: Q90GLXQ0Pef()
        NbtCompound tag = new NbtCompound();
        tag.putString("type", this.getType());
        tag.putString("command", this.command);
        return tag;
    }

    public static RunCommandAction fromTag(NbtCompound tag) { // was: Q90GLXQ0Pef(NbtCompound)
        return new RunCommandAction(VersionHelper.get().getString(tag, "command"));
    }
}
