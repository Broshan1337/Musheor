// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud.actions;

import musheor.compat.VersionHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;

/** Button action that sends a chat message to the server. */
public class SendMessageAction implements ButtonAction {
    public String message;                                                    // was: FvaNWO
    private static final MinecraftClient mc = MinecraftClient.getInstance();   // was: Q90GLXQ0Pef

    public SendMessageAction(String message) {
        this.message = message;
    }

    @Override
    public void execute(ScreenHandler handler) { // was: FvaNWO(ScreenHandler)
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatMessage(this.message);
        }
    }

    @Override
    public String getType() { // was: FvaNWO()
        return "send_message";
    }

    @Override
    public NbtCompound toTag() { // was: Q90GLXQ0Pef()
        NbtCompound tag = new NbtCompound();
        tag.putString("type", this.getType());
        tag.putString("message", this.message);
        return tag;
    }

    public static SendMessageAction fromTag(NbtCompound tag) { // was: Q90GLXQ0Pef(NbtCompound)
        return new SendMessageAction(VersionHelper.get().getString(tag, "message"));
    }
}
