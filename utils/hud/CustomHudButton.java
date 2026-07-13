// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import musheor.compat.VersionHelper;
import musheor.utils.hud.actions.ButtonAction;
import musheor.utils.hud.actions.CompositeAction;
import musheor.utils.system.MusheorSystem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

/**
 * A user-defined HUD button shown on inventory/container screens: a label, cyclable text
 * colour, position (default or dragged/saved), size, screen target, and a list of
 * {@link ButtonAction}s run on click. Serializes to/from NBT for {@link MusheorSystem}.
 */
public class CustomHudButton {
    public static final int[] TEXT_COLORS = { // was: FvaNWO
        16777215, 16777045, 5636095, 5635925, 16733695, 16733525, 11184810, 5592405, 5592575, 43520, 43690, 11141120, 11141290, 16755200, 170, 0};
    public static final String[] COLOR_NAMES = { // was: Q90GLXQ0Pef
        "White", "Yellow", "Aqua", "Green", "Light Purple", "Red", "Gray", "Dark Gray", "Blue",
        "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple", "Gold", "Dark Blue", "Black"};

    public String id;                        // was: psJq59YIbp3Z
    public String label;                     // was: SOYyh5IPg26f7F
    public int textColorIndex = 0;           // was: rKbT3Ifwo
    public int defaultX;                     // was: r7hOYIKN2
    public int defaultY;                     // was: oZHMlTL
    public int width = 40;                   // was: xQr5FhbwpQPWgIQ
    public int height = 20;                  // was: OMMZL1F3q
    public ScreenTarget screenTarget = ScreenTarget.INVENTORY; // was: zu3a44xDeMFMCRwm
    public int savedX = Integer.MIN_VALUE;   // was: krxNb5lcQuWA
    public int savedY = Integer.MIN_VALUE;   // was: nt0HZnvBBp
    public final List<ButtonAction> actions = new ArrayList<>(); // was: amz3UB1vE

    public CustomHudButton(String id, String label, int defaultX, int defaultY) {
        this.id = id;
        this.label = label;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
    }

    public static CustomHudButton create(String label, int defaultX, int defaultY) { // was: FvaNWO(String,int,int)
        return new CustomHudButton(UUID.randomUUID().toString(), label, defaultX, defaultY);
    }

    public void cycleTextColor() { // was: FvaNWO()
        this.textColorIndex = (this.textColorIndex + 1) % TEXT_COLORS.length;
    }

    public int getTextColor() { // was: Q90GLXQ0Pef()
        return TEXT_COLORS[this.textColorIndex];
    }

    public int getX() { // was: psJq59YIbp3Z()
        return this.savedX != Integer.MIN_VALUE ? this.savedX : this.defaultX;
    }

    public int getY() { // was: SOYyh5IPg26f7F()
        return this.savedY != Integer.MIN_VALUE ? this.savedY : this.defaultY;
    }

    /** Builds the draggable widget for this button, wiring its actions and a position-save callback. */
    public DraggableButtonWidget createWidget(ScreenHandler handler) { // was: FvaNWO(ScreenHandler)
        CompositeAction composite = new CompositeAction(this.actions);
        return new DraggableButtonWidget(
            this.getX(), this.getY(), this.width, this.height, Text.literal(this.label), this.getTextColor(),
            btn -> composite.execute(handler),
            (newX, newY) -> {
                this.savedX = newX;
                this.savedY = newY;
                MusheorSystem.get().save();
            });
    }

    public NbtCompound toTag() { // was: rKbT3Ifwo()
        NbtCompound tag = new NbtCompound();
        tag.putString("id", this.id);
        tag.putString("label", this.label);
        tag.putInt("textColorIndex", this.textColorIndex);
        tag.putInt("defaultX", this.defaultX);
        tag.putInt("defaultY", this.defaultY);
        tag.putInt("width", this.width);
        tag.putInt("height", this.height);
        tag.putInt("savedX", this.savedX);
        tag.putInt("savedY", this.savedY);
        tag.putString("screenTarget", this.screenTarget.name());
        NbtList actionList = new NbtList();
        for (ButtonAction action : this.actions) actionList.add(action.toTag());
        tag.put("actions", actionList);
        return tag;
    }

    public static CustomHudButton fromTag(NbtCompound tag) { // was: FvaNWO(NbtCompound)
        CustomHudButton btn = new CustomHudButton(
            VersionHelper.get().getString(tag, "id"), VersionHelper.get().getString(tag, "label"),
            VersionHelper.get().getInt(tag, "defaultX", 0), VersionHelper.get().getInt(tag, "defaultY", 0));
        if (tag.contains("textColorIndex")) btn.textColorIndex = VersionHelper.get().getInt(tag, "textColorIndex", 0);
        if (tag.contains("width")) btn.width = VersionHelper.get().getInt(tag, "width", 40);
        if (tag.contains("height")) btn.height = VersionHelper.get().getInt(tag, "height", 20);
        if (tag.contains("savedX")) btn.savedX = VersionHelper.get().getInt(tag, "savedX", Integer.MIN_VALUE);
        if (tag.contains("savedY")) btn.savedY = VersionHelper.get().getInt(tag, "savedY", Integer.MIN_VALUE);
        if (tag.contains("screenTarget")) {
            try {
                btn.screenTarget = ScreenTarget.valueOf(VersionHelper.get().getString(tag, "screenTarget"));
            } catch (IllegalArgumentException ignored) {
                btn.screenTarget = ScreenTarget.INVENTORY;
            }
        }
        for (NbtElement el : VersionHelper.get().getList(tag, "actions", 10)) {
            ButtonAction action = ButtonAction.fromTag((NbtCompound) el);
            if (action != null) btn.actions.add(action);
        }
        return btn;
    }

    /** Which screen(s) a button appears on. */ // was: enum ScreenTarget {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z}
    public enum ScreenTarget {
        // FvaNWO used by ScreenHandlerMixin (container screens); Q90GLXQ0Pef used by ChatScreenMixin.
        INVENTORY, CHAT, ALL;

        /** True if this target applies on {@code context} (i.e. it is ALL, or matches). */
        public boolean matches(ScreenTarget context) { // was: FvaNWO(ScreenTarget)
            return this == ALL || this == context;
        }
    }
}
