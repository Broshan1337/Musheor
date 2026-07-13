// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// Class name was already readable; internal members were obfuscated.
package musheor.utils.hud.screen;

import java.util.List;
import musheor.utils.hud.CustomHudButton;
import musheor.utils.hud.actions.ButtonAction;
import musheor.utils.hud.actions.ChangeSettingAction;
import musheor.utils.hud.actions.RunCommandAction;
import musheor.utils.hud.actions.SendMessageAction;
import musheor.utils.hud.actions.ToggleModuleAction;
import musheor.utils.system.MusheorSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.Click;
import net.minecraft.text.Text;

/**
 * Editor for a single {@link CustomHudButton}: rename, cycle text colour, choose screen
 * target and size, and add/remove {@link ButtonAction}s (toggle module / send message /
 * run command / change setting) via type-specific input fields.
 */
public class EditButtonScreen extends Screen {
    private final Screen parent;                 // was: FvaNWO
    private final CustomHudButton button;        // was: Q90GLXQ0Pef
    private final List<CustomHudButton> allButtons; // was: psJq59YIbp3Z
    private TextFieldWidget labelField;          // was: SOYyh5IPg26f7F
    private ActionType selectedActionType = ActionType.MODULE; // was: rKbT3Ifwo
    private TextFieldWidget field1;              // was: r7hOYIKN2
    private TextFieldWidget field2;              // was: oZHMlTL
    private TextFieldWidget field3;              // was: xQr5FhbwpQPWgIQ

    public EditButtonScreen(Screen parent, CustomHudButton button, List<CustomHudButton> allButtons) {
        super(Text.literal("Edit Button: " + button.label));
        this.parent = parent;
        this.button = button;
        this.allButtons = allButtons;
    }

    @Override
    protected void init() { // was: method_25426()
        int cx = this.width / 2;
        this.labelField = new TextFieldWidget(this.textRenderer, cx - 75, 30, 150, 18, Text.literal("Label"));
        this.labelField.setText(this.button.label);
        this.labelField.setMaxLength(32);
        this.addDrawableChild(this.labelField);
        this.addDrawableChild(ButtonWidget.builder(Text.literal(""), b -> {
            this.button.cycleTextColor();
            MusheorSystem.get().save();
        }).position(cx - 75 + 152, 30).size(20, 20).build());

        for (CustomHudButton.ScreenTarget target : CustomHudButton.ScreenTarget.values()) {
            CustomHudButton.ScreenTarget t = target;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(target.name()), b -> {
                this.button.screenTarget = t;
                MusheorSystem.get().save();
            }).position(cx - 75 + target.ordinal() * 52, 62).size(50, 16).build());
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("−"), b -> {
            this.button.width = Math.max(20, this.button.width - 2);
            MusheorSystem.get().save();
        }).position(cx - 75, 88).size(16, 16).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            this.button.width = Math.min(200, this.button.width + 2);
            MusheorSystem.get().save();
        }).position(cx - 75 + 50, 88).size(16, 16).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("−"), b -> {
            this.button.height = Math.max(10, this.button.height - 2);
            MusheorSystem.get().save();
        }).position(cx + 10, 88).size(16, 16).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            this.button.height = Math.min(40, this.button.height + 2);
            MusheorSystem.get().save();
        }).position(cx + 10 + 50, 88).size(16, 16).build());

        for (ActionType type : ActionType.values()) {
            ActionType t = type;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(type.name()), b -> {
                this.selectedActionType = t;
                this.rebuildActionFields();
            }).position(cx - 150 + type.ordinal() * 78, 116).size(76, 16).build());
        }

        this.rebuildActionFields();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add Action"), b -> this.addAction())
            .position(cx - 80, this.height - 55).size(76, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), b -> {
            if (!this.labelField.getText().isBlank()) this.button.label = this.labelField.getText();
            MusheorSystem.get().save();
            this.client.setScreen(this.parent);
        }).position(cx + 4, this.height - 55).size(76, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), b -> this.client.setScreen(this.parent))
            .position(cx - 25, this.height - 28).size(50, 16).build());
    }

    /** Rebuilds the input field(s) for the currently-selected action type. */
    private void rebuildActionFields() { // was: FvaNWO()
        if (this.field1 != null) this.remove(this.field1);
        if (this.field2 != null) this.remove(this.field2);
        if (this.field3 != null) this.remove(this.field3);

        int cx = this.width / 2;
        int fy = 136;
        switch (this.selectedActionType) {
            case MODULE -> {
                this.field1 = this.createTextField(cx - 75, fy, "Module name (e.g. KillAura)");
                this.field2 = null;
                this.field3 = null;
            }
            case MESSAGE -> {
                this.field1 = this.createTextField(cx - 75, fy, "Message text");
                this.field2 = null;
                this.field3 = null;
            }
            case COMMAND -> {
                this.field1 = this.createTextField(cx - 75, fy, "Command (e.g. .tp or /gamemode)");
                this.field2 = null;
                this.field3 = null;
            }
            case SETTING -> {
                this.field1 = this.createTextField(cx - 75, fy, "Module name");
                this.field2 = this.createTextField(cx - 75, fy + 24, "Setting name");
                this.field3 = this.createTextField(cx - 75, fy + 48, "Value");
            }
        }

        if (this.field1 != null) this.addDrawableChild(this.field1);
        if (this.field2 != null) this.addDrawableChild(this.field2);
        if (this.field3 != null) this.addDrawableChild(this.field3);
    }

    private TextFieldWidget createTextField(int x, int y, String placeholder) { // was: FvaNWO(int,int,String)
        TextFieldWidget f = new TextFieldWidget(this.textRenderer, x, y, 150, 20, Text.literal(placeholder));
        f.setPlaceholder(Text.literal(placeholder));
        f.setMaxLength(128);
        return f;
    }

    /** Builds a {@link ButtonAction} from the current fields and appends it to the button. */
    private void addAction() { // was: Q90GLXQ0Pef()
        if (this.field1 == null || this.field1.getText().isBlank()) return;
        ButtonAction action = switch (this.selectedActionType) {
            case MODULE -> new ToggleModuleAction(this.field1.getText().trim());
            case MESSAGE -> new SendMessageAction(this.field1.getText().trim());
            case COMMAND -> new RunCommandAction(this.field1.getText().trim());
            case SETTING -> this.field2 == null || this.field3 == null ? null
                : (!this.field2.getText().isBlank() && !this.field3.getText().isBlank()
                    ? new ChangeSettingAction(this.field1.getText().trim(), this.field2.getText().trim(), this.field3.getText().trim())
                    : null);
        };
        if (action != null) {
            this.button.actions.add(action);
            this.field1.setText("");
            if (this.field2 != null) this.field2.setText("");
            if (this.field3 != null) this.field3.setText("");
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { // was: method_25420
        context.fill(0, 0, this.width, this.height, -1072689136);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) { // was: method_25394
        super.render(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 10, -1);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Label:"), cx - 75, 20, -5592406);
        int swatchX = cx - 75 + 152;
        context.fill(swatchX + 2, 32, swatchX + 18, 48, 0xFF000000 | this.button.getTextColor());
        if (mouseX >= swatchX && mouseX <= swatchX + 20 && mouseY >= 30 && mouseY <= 50) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(CustomHudButton.COLOR_NAMES[this.button.textColorIndex]), swatchX + 22, 37, 0xFF000000 | this.button.getTextColor());
        }

        context.drawTextWithShadow(this.textRenderer, Text.literal("Show on:"), cx - 75, 52, -5592406);
        for (CustomHudButton.ScreenTarget target : CustomHudButton.ScreenTarget.values()) {
            if (this.button.screenTarget == target) {
                int bx = cx - 75 + target.ordinal() * 52;
                context.fill(bx, 62, bx + 50, 78, -2011907099);
            }
        }

        context.drawTextWithShadow(this.textRenderer, Text.literal("Size:"), cx - 75, 80, -5592406);
        context.drawTextWithShadow(this.textRenderer, Text.literal("W:"), cx - 75 + 18, 90, -5592406);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(String.valueOf(this.button.width)), cx - 75 + 33, 90, -1);
        context.drawTextWithShadow(this.textRenderer, Text.literal("H:"), cx + 10 + 18, 90, -5592406);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(String.valueOf(this.button.height)), cx + 10 + 33, 90, -1);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Action type:"), cx - 75, 106, -5592406);

        for (ActionType type : ActionType.values()) {
            if (this.selectedActionType == type) {
                int bx = cx - 150 + type.ordinal() * 78;
                context.fill(bx, 116, bx + 76, 132, -2011907099);
            }
        }

        context.drawTextWithShadow(this.textRenderer, Text.literal("── Current actions ──"), cx - 75, 208, -5592406);
        int ay = 220;
        for (int i = 0; i < this.button.actions.size(); i++) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(i + 1 + ". " + this.describeAction(this.button.actions.get(i))), cx - 75, ay, -1);
            int removeX = cx + 80;
            if (mouseX >= removeX && mouseX <= removeX + 30 && mouseY >= ay - 2 && mouseY <= ay + 10) {
                context.fill(removeX, ay - 2, removeX + 30, ay + 10, -1996541133);
            }
            context.drawTextWithShadow(this.textRenderer, Text.literal("[x]"), removeX, ay, -48060);
            ay += 14;
        }
        if (this.button.actions.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("No actions yet."), cx - 75, ay, -7829368);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) { // was: method_25402(Click, boolean)
        int cx = this.width / 2;
        int ay = 220;
        double mouseX = click.x();
        double mouseY = click.y();
        for (int i = 0; i < this.button.actions.size(); i++) {
            int removeX = cx + 80;
            if (mouseX >= removeX && mouseX <= removeX + 30 && mouseY >= ay - 2 && mouseY <= ay + 10) {
                this.button.actions.remove(i);
                return true;
            }
            ay += 14;
        }
        return super.mouseClicked(click, doubled);
    }

    /** A short human-readable summary of an action for the list. */
    private String describeAction(ButtonAction action) { // was: FvaNWO(ButtonAction)
        return switch (action) {
            case ToggleModuleAction a -> "Toggle: " + a.moduleName;
            case SendMessageAction a -> "Message: " + this.truncate(a.message, 24);
            case RunCommandAction a -> "Command: " + this.truncate(a.command, 24);
            case ChangeSettingAction a -> "Setting: " + a.moduleName + "." + a.settingName + "=" + a.value;
            default -> action.getType();
        };
    }

    private String truncate(String s, int max) { // was: FvaNWO(String,int)
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    @Override
    public boolean shouldPause() { // was: method_25421()
        return false;
    }

    /** Action editor tabs. */ // was: enum ActionType {FvaNWO, Q90GLXQ0Pef, psJq59YIbp3Z, SOYyh5IPg26f7F}
    private enum ActionType { MODULE, MESSAGE, COMMAND, SETTING }
}
