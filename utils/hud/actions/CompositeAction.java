// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud.actions;

import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;

/** Runs several {@link ButtonAction}s in sequence. Not itself persisted. */
public class CompositeAction implements ButtonAction {
    private final List<ButtonAction> actions; // was: FvaNWO

    public CompositeAction(List<ButtonAction> actions) {
        this.actions = actions;
    }

    @Override
    public void execute(ScreenHandler handler) { // was: FvaNWO(ScreenHandler)
        for (ButtonAction action : this.actions) action.execute(handler);
    }

    @Override
    public String getType() { // was: FvaNWO()
        return "composite";
    }

    @Override
    public NbtCompound toTag() { // was: Q90GLXQ0Pef()
        return new NbtCompound();
    }
}
