// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud.screen;

import java.util.List;
import musheor.utils.hud.CustomHudButton;
import musheor.utils.system.MusheorSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Lists the user's custom HUD buttons with Edit/Delete controls and a "+ New Button" action.
 * Opened from the Musheor tab / editor keybind.
 */
public class CustomButtonManagerScreen extends Screen {
    private final Screen parent;              // was: FvaNWO
    private List<CustomHudButton> buttons;    // was: Q90GLXQ0Pef

    public CustomButtonManagerScreen(Screen parent) {
        super(Text.literal("Custom HUD Buttons"));
        this.parent = parent;
    }

    @Override
    protected void init() { // was: method_25426()
        this.buttons = MusheorSystem.get().customButtons;
        this.rebuild();
    }

    /** Rebuilds the row of Edit/Delete buttons plus the New/Back controls. */
    private void rebuild() { // was: FvaNWO()
        this.clearChildren();
        int cx = this.width / 2;
        int y = 40;

        for (CustomHudButton btn : this.buttons) {
            CustomHudButton b = btn;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("✎ Edit"),
                button -> this.client.setScreen(new EditButtonScreen(this, b, this.buttons)))
                .position(cx + 188, y).size(50, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("✗ Delete"), button -> {
                this.buttons.remove(b);
                MusheorSystem.get().save();
                this.init();
            }).position(cx + 240, y).size(50, 20).build());
            y += 26;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("+ New Button"), bx -> {
            CustomHudButton newBtn = CustomHudButton.create("New", this.width / 2 - 20, 40);
            this.buttons.add(newBtn);
            MusheorSystem.get().save();
            this.client.setScreen(new EditButtonScreen(this, newBtn, this.buttons));
        }).position(cx - 50, this.height - 48).size(100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), bx -> this.client.setScreen(this.parent))
            .position(cx - 25, this.height - 24).size(50, 16).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { // was: method_25420
        context.fill(0, 0, this.width, this.height, -1072689136);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) { // was: method_25394
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, -1);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Label"), this.width / 2 - 150, 28, -5592406);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Actions"), this.width / 2 + 50, 28, -5592406);
        int y = 40;
        for (CustomHudButton btn : this.buttons) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(btn.label), this.width / 2 - 150, y + 5, -1);
            context.drawTextWithShadow(this.textRenderer, Text.literal(btn.actions.size() + " action(s)"), this.width / 2 + 50, y + 5, -7798904);
            y += 26;
        }
        if (this.buttons.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No buttons yet. Press '+ New Button' to create one."), this.width / 2, 50, -7829368);
        }
    }

    @Override
    public boolean shouldPause() { // was: method_25421()
        return false;
    }
}
