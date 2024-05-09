package com.theincgi.advancedmacros.gui.elements;

import com.theincgi.advancedmacros.gui.Color;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.Gui.InputSubscriber;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.lwjgl.glfw.GLFW;

public class PopupPrompt implements InputSubscriber, Drawable {

    private int x, y, width, height;
    private String msg;
    GuiButton ok, cancel;
    TextFieldWidget inputBox;
    boolean isVisible = false;
    private Answer ans;
    private int fill = Color.TEXT_8.toInt();
    private int frame = Color.WHITE.toInt();
    private int okFill = new Color(166, 226, 158).toInt();
    private int okFrame = new Color(226, 163, 158).toInt();
    private int textColor = Color.WHITE.toInt();

    private GuiDropDown choicePicker;

    private WidgetID wID;
    private Gui gui;

    public PopupPrompt(WidgetID wID, int x, int y, int width, int height, final Gui gui) {
        this.gui = gui;
        this.wID = wID;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        //ok = new GuiButton(wID, x, y+height*2/3, width/2, 12, LuaValue.NIL, LuaValue.NIL, "colors.popup.ok", Color.BLACK, Color.WHITE, Color.WHITE);
        ok = new GuiButton(x, y + height * 2 / 3, width / 2, 12, LuaValue.NIL, LuaValue.NIL, "popupPrompt", "colors", "okButton");
        //cancel = new GuiButton(wID, x+width/2, y+height*2/3, width/2, 12, LuaValue.NIL, LuaValue.NIL, "colors.popup.cancel", Color.BLACK, Color.WHITE, Color.WHITE);
        cancel = new GuiButton(x + width / 2, y + height * 2 / 3, width / 2, 12, LuaValue.NIL, LuaValue.NIL, "popupPrompt", "colors", "cancelButton");
        //choicePicker = new GuiDropDown(wID, x+1, ok.getY()-12, width-2, 12, gui.height-15-(ok.getY()-12), "colors.popup.dropdown");
        choicePicker = new GuiDropDown(x + 1, ok.getY() - 12, width - 2, 12, gui.height - 15 - (ok.getY() - 12), "popupPrompt", "colors", "choicePicker");

        ok.setText("Ok");
        cancel.setText("Cancel");
        inputBox = new TextFieldWidget(gui.getFontRend(), x + 1, ok.getY() - 12, width - 2, 12, Text.literal(""));
        ok.setOnClick(new OnClickHandler() {
            @Override
            public void onClick(int button, GuiButton sButton) {
                System.out.println("Oki!");
                ans = new Answer(Choice.OK, inputBox.getText());
                isVisible = false;
                if (gui.firstSubsciber == PopupPrompt.this) {
                    gui.firstSubsciber = null;
                }
                if (onAns != null) {
                    onAns.run();
                }
            }
        });
        cancel.setOnClick(new OnClickHandler() {
            @Override
            public void onClick(int button, GuiButton sButton) {
                System.out.println("Cancelated");
                ans = new Answer(Choice.CANCEL, null);
                isVisible = false;
                if (gui.firstSubsciber == PopupPrompt.this) {
                    gui.firstSubsciber = null;
                }
                if (onAns != null) {
                    onAns.run();
                }
            }
        });

    }

    private Runnable onAns;

    public void setOnAns(Runnable onAns) {
        this.onAns = onAns;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        ok.setPos(x, y + height * 2 / 3);
        cancel.setPos(x + width / 2, y + height * 2 / 3);
    }

    public void resize(int wid, int hei) {
        this.width = wid;
        this.height = hei;
        ok.setWidth(wid / 2);
        ok.setHeight(12);
        cancel.setWidth(wid / 2);
        ok.setPos(x, y + hei - 12);
        cancel.setHeight(12);
        cancel.setPos(x + width / 2, y + height - 12);
        inputBox.setWidth(wid - 2);
        inputBox.setX(x + 1);
        inputBox.setY(ok.getY() - 12);
        //new GuiTextField(0, gui.getFontRend(), x+1, ok.getY()-12, width-2, 12);
    }

    public void prompt(String msg) {
        this.msg = msg;
        isVisible = true;
        ans = null;
        inputBox.setText("");
        inputBox.setCursor(0, false);
        inputBox.setFocused(true);
        gui.drawLast = this;
        //gui.nextKeyListen=null;
        gui.firstSubsciber = this;
        inputBox.setVisible(true);
        cancel.setVisible(true);
        choicePicker.setVisible(false);
    }

    public void promptError(String string) {
        this.msg = string;
        isVisible = true;
        ans = null;
        inputBox.setText("");
        inputBox.setCursor(0, false);
        inputBox.setFocused(false);
        gui.drawLast = this;
        //gui.nextKeyListen=null;
        gui.firstSubsciber = this;
        inputBox.setVisible(false);
        cancel.setVisible(false);
        choicePicker.setVisible(false);
    }

    public void promptConfirm(String string) {
        this.msg = string;
        isVisible = true;
        ans = null;
        inputBox.setText("");
        inputBox.setCursor(0, false);
        inputBox.setFocused(false);
        gui.drawLast = this;
        //gui.nextKeyListen=null;
        gui.firstSubsciber = this;
        inputBox.setVisible(false);
        cancel.setVisible(true);
        choicePicker.setVisible(false);
    }

    public void promptChoice(String msg, String... options) {
        this.msg = msg;
        isVisible = true;
        ans = null;
        inputBox.setText("");
        inputBox.setCursor(0, false);
        inputBox.setFocused(false);
        gui.drawLast = this;
        //gui.nextKeyListen=null;
        gui.firstSubsciber = this;
        inputBox.setVisible(false);
        cancel.setVisible(true);
        choicePicker.setVisible(true);
        choicePicker.clear(false);
        for (String c : options) {
            choicePicker.addOption(c);
        }
        choicePicker.select(options[0]); //TODO Select by int
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui gui, int mouseX, int mouseY, float partialTicks) {
        if (isVisible) {
            gui.renderBackground(drawContext, mouseX, mouseY, partialTicks);
            gui.drawBoxedRectangle(drawContext, x, y, width, height - 1, frame, fill);
            drawContext.drawText(gui.getFontRend(), msg, x + 3, y + 2, textColor, false);
            inputBox.render(drawContext, mouseX, mouseY, partialTicks);
            ok.onDraw(drawContext, gui, mouseX, mouseY, partialTicks);
            cancel.onDraw(drawContext, gui, mouseX, mouseY, partialTicks);
        }
    }

    public Answer checkAns() {
        if (!isVisible) {
            return ans;
        }
        return null;
    }

    @Override
    public boolean onScroll(Gui gui, double i) {
        return isVisible;
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (ok.onMouseClick(gui, x, y, buttonNum)) {
            return true;
        }
        if (cancel.onMouseClick(gui, x, y, buttonNum)) {
            return true;
        }
        if (inputBox.isVisible()) {
            inputBox.mouseClicked(x, y, buttonNum);
        }
        return isVisible;//GuiButton.isInBounds(x, y, this.x, this.y, this.width, this.height);
    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        if (ok.onMouseRelease(gui, x, y, state)) {
            return true;
        }
        if (cancel.onMouseRelease(gui, x, y, state)) {
            return true;
        }
        return isVisible;
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        return isVisible;
    }

    @Override
    public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            ok.onClick.onClick(-1, ok);
        }
        if (inputBox.isFocused()) {
            return inputBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return isVisible;
    }

    @Override
    public boolean onCharTyped(Gui gui, char typedChar, int mods) {
        if (inputBox.isVisible()) {
            inputBox.charTyped(typedChar, mods);
        }
        return isVisible;
    }

    @Override
    public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
        return isVisible;
    }

    @Override
    public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
        return onKeyPressed(gui, keyCode, scanCode, modifiers);
    }

    public static class Answer {

        public Choice c;
        public String answer;

        public Answer(Choice c, String answer) {
            super();
            this.c = c;
            this.answer = answer;
        }

    }

    public enum Choice {
        OK,
        CANCEL;
    }

}
