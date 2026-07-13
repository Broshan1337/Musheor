// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class/members readable. Some 1.21.11 render/input methods (newer than the mapping used to
// verify names) are annotated with their intermediary id, e.g. // method_75752.
package musheor.utils.hud;

import java.util.function.Supplier;
import meteordevelopment.meteorclient.settings.Setting;
import musheor.compat.VersionHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.Click;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/**
 * A {@link ButtonWidget} that can be dragged to a new position while Ctrl is held (a move
 * cursor and highlight are shown), persisting its position through a {@link PositionSaveCallback}.
 * Otherwise behaves as a normal button, rendering its coloured label.
 */
public class DraggableButtonWidget extends ButtonWidget {
    private boolean dragging = false;         // was: FvaNWO
    private int dragOffsetX;                  // was: Q90GLXQ0Pef
    private int dragOffsetY;                  // was: psJq59YIbp3Z
    private final Text label;                 // was: SOYyh5IPg26f7F
    private final Supplier<Text> labelSupplier; // was: rKbT3Ifwo
    private final int textColor;              // was: r7hOYIKN2
    private final PositionSaveCallback onPositionSaved; // was: oZHMlTL

    public DraggableButtonWidget(int x, int y, int width, int height, Text label, PressAction action, Setting<Integer> settingX, Setting<Integer> settingY) {
        super(x, y, width, height, Text.empty(), action, DEFAULT_NARRATION_SUPPLIER);
        this.label = label;
        this.labelSupplier = null;
        this.textColor = 0xFFFFFF;
        this.onPositionSaved = (nx, ny) -> {
            settingX.set(nx);
            settingY.set(ny);
        };
    }

    public DraggableButtonWidget(int x, int y, int width, int height, Text label, int textColor, PressAction action, PositionSaveCallback onPositionSaved) {
        super(x, y, width, height, Text.empty(), action, DEFAULT_NARRATION_SUPPLIER);
        this.label = label;
        this.labelSupplier = null;
        this.textColor = textColor;
        this.onPositionSaved = onPositionSaved;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) { // was: method_25402(Click, boolean)
        if (VersionHelper.get().hasControlDown() && this.isMouseOver(click.x(), click.y())) {
            this.dragging = true;
            this.dragOffsetX = (int) click.x() - this.getX();
            this.dragOffsetY = (int) click.y() - this.getY();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) { // was: method_25403(Click, double, double)
        if (this.dragging && VersionHelper.get().hasControlDown()) {
            this.setX((int) click.x() - this.dragOffsetX);
            this.setY((int) click.y() - this.dragOffsetY);
            return true;
        }
        if (this.dragging) this.dragging = false;
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) { // was: method_25406(Click)
        if (this.dragging) {
            this.dragging = false;
            this.onPositionSaved.save(this.getX(), this.getY());
            return true;
        }
        return super.mouseReleased(click);
    }

    /** Draws the label (a move glyph while dragging), coloured with {@link #textColor}. */
    @Override
    protected void drawMessage(net.minecraft.client.gui.widget.ClickableWidget.MessageRenderer consumer) { // was: method_75793(class_12225)
        Text text;
        if (VersionHelper.get().hasControlDown()) {
            text = Text.literal("✥");
        } else {
            Text src = this.labelSupplier != null ? this.labelSupplier.get() : this.label;
            text = Text.literal(src.getString()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF000000 | this.textColor & 0xFFFFFF)));
        }
        this.renderMessageText(consumer, text, 2); // method_75799
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) { // was: method_75752
        this.renderBackground(context); // method_75794
        this.drawMessage(context.getMessageRenderer(this)); // method_75787(this, class_12228.field_63850)
        if (VersionHelper.get().hasControlDown()) {
            context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), -2011907099);
        }
    }

    /** Persists a dragged position. */
    public interface PositionSaveCallback {
        void save(int x, int y);
    }
}
