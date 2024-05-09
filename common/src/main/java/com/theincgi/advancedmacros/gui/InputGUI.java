package com.theincgi.advancedmacros.gui;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.TaskDispatcher;
import com.theincgi.advancedmacros.gui.elements.Drawable;
import com.theincgi.advancedmacros.gui.elements.GuiRect;
import com.theincgi.advancedmacros.gui.elements.ListManager;
import com.theincgi.advancedmacros.gui.elements.Moveable;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.misc.HIDUtils.Mouse;
import com.theincgi.advancedmacros.misc.PropertyPalette;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class InputGUI extends Gui {

    private Thread threadCheck;
    InputType inputType;
    private LuaDebug debug;
    TextFieldWidget textInput = new TextFieldWidget(fontRend, 5, 5, 30, 12, Text.literal(""));
    private Collection<Item> itemList = Registries.ITEM.stream().toList();
    private final int WHITE = Color.WHITE.toInt();
    private String prompt;
    private boolean answered = true;
    private Semaphore semaphore;
    private LuaValue answer = LuaValue.NIL;
    PropertyPalette propPalette = new PropertyPalette(new String[]{"promptGui"});
    private ListManager listItemPicker = new ListManager(5, 19, 30, 30, /*new WidgetID(800), "colors.promptGUI"*/ propPalette);
    private ListManager choices = new ListManager(5, 19, 30, 30, /*new WidgetID(800), "colors.promptGUI"*/ propPalette);
    private static ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

    public InputGUI(LuaDebug debug) {
        this.debug = debug;
        listItemPicker.setDrawBG(false);
        listItemPicker.setAlwaysShowScroll(true);
        listItemPicker.setSpacing(3);
        Iterator<Item> itemItter = itemList.iterator();
        for (int i = 0; itemItter.hasNext(); i++) {
            Item item = itemItter.next();
            //argument for maxDamage is literally discarded
            //for(int d = 0; d<item.getMaxDamage(ItemStack.EMPTY); d++) {
            listItemPicker.add(new ItemOption(new ItemStack(item, 1)));
            //}
        }
        addInputSubscriber(listItemPicker);
        addInputSubscriber(choices);
        addInputSubscriber(new InputSubscriber() {
            @Override
            public boolean onScroll(Gui gui, double i) {
                return false;
            }

            @Override
            public boolean onMouseRelease(Gui gui, double x, double y, int state) {
                return false;
            }

            @Override
            public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
                return false;
            }

            @Override
            public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
                return false;
            }

            @Override
            public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
                return (inputType == InputType.TEXT) && (n % 2 == 0 && textInput.keyPressed(keyCode, scanCode, modifiers));
            }

            @Override
            public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
                return false;
            }

            @Override
            public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
                if (inputType == InputType.TEXT) {
                    return textInput.keyPressed(keyCode, scanCode, modifiers);
                }
                return false;
            }

            @Override
            public boolean onCharTyped(Gui gui, char typedChar, int mods) {
                if (inputType == InputType.TEXT) {
                    return textInput.charTyped(typedChar, mods);
                }
                return false;
            }
        });
        listItemPicker.setScrollSpeed(10);
        choices.setScrollSpeed(10);
        choices.setSpacing(3);
        textInput.setMaxLength(Integer.MAX_VALUE);
    }

    public void setInputType(InputType inputType, String prompt) {
        synchronized (this) {
            if (threadCheck == null) {
                threadCheck = Thread.currentThread();
                this.inputType = inputType;
                this.prompt = prompt;
                answered = false;
                textInput.setFocused(inputType == InputType.TEXT);
                textInput.setVisible(inputType == InputType.TEXT);
                textInput.setText("");
                listItemPicker.setVisible(inputType == InputType.ITEM);

            }
        }
    }

    @Override
    public Text getTitle() {
        return Text.literal("Input Screen");
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
        super.render(drawContext, mouseX, mouseY, partialTicks);
        switch (inputType) {
            case TEXT -> {
                drawContext.fill(1, height - 49, width - 1, height - 1, 0xDD000000);
                int drawHei = height - 5;
                textInput.setY(drawHei -= 12);
                drawHei -= 5;
                drawHei -= getFontRend().fontHeight;
                drawContext.drawText(getFontRend(), prompt, 5, drawHei, WHITE, false);
                drawHei -= 5;
                drawHei -= getFontRend().fontHeight;
                drawContext.drawText(getFontRend(), LuaDebug.getLabel(threadCheck), 5, drawHei, WHITE, false);
                textInput.render(drawContext, mouseX, mouseY, partialTicks);
            }
            case ITEM -> {
                drawContext.drawText(getFontRend(), LuaDebug.getLabel(threadCheck), 5, 5, WHITE, false);
                drawContext.drawText(getFontRend(), prompt, 5, 15, WHITE, false);
                listItemPicker.onDraw(drawContext, this, mouseX, mouseY, partialTicks);
            }
            case CHOICE -> {
                drawContext.drawText(getFontRend(), LuaDebug.getLabel(threadCheck), 5, 5, WHITE, false);
                drawContext.drawText(getFontRend(), prompt, 5, 15, WHITE, false);
                choices.onDraw(drawContext, this, mouseX, mouseY, partialTicks);
            }
            default -> {
            }
        }
    }

    @Override
    public void resize(MinecraftClient mc, int width, int height) {
        super.resize(mc, width, height);
        textInput.setWidth(width - 10);
        listItemPicker.setPos(5, 25);
        listItemPicker.setWidth(width / 2 - 10);
        listItemPicker.setHeight(height - 60);
        choices.setPos(5, 25);
        choices.setWidth(width / 2 - 10);
        choices.setHeight(height - 60);

    }

    @Override
    public boolean charTyped(char typedChar, int mods) {

        return super.charTyped(typedChar, mods);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close(LuaValue.NIL);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            close(LuaValue.valueOf(textInput.getText()));
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onKeyRepeated(Gui gui, int keyCode, int scanCode, int mods, int n) {
        return super.onKeyRepeated(gui, keyCode, scanCode, mods, n);
    }

    //	@Override
    //	public boolean keyRepeated(char typedChar, int keyCode, int mod) {
    //		super.keyRepeated(typedChar, keyCode, mod);
    //		if(inputType==InputType.TEXT) {
    //			if(mod%2==0)
    //				textInput.charTyped(typedChar, keyCode);
    //			return textInput.isFocused();
    //		}
    //		return false;
    //	}
    private void close(LuaValue value) {
        answer = value;
        answered = true;
        semaphore.release();
        textInput.setFocused(false);
        MinecraftClient.getInstance().player.closeScreen();
    }

    //BOOKMARK implement some fancy code here
    public static enum InputType {
        TEXT,
        ITEM,
        //NUMBER, //TODO more features! (Input types)
        //LOCATION,
        //PLAYER,
        //TIME,
        //FILE,
        CHOICE
    }

    private Prompt promptFunc = new Prompt();

    public Prompt getPrompt() {
        return promptFunc;
    }

    private static int BGColor = Color.WHITE.toInt(),
            FILLColor = Color.TEXT_8.toInt(),
            TEXTCOLOR = Color.WHITE.toInt(),
            HEIGHLIGHT = 0x550010F0;

    private class ItemOption implements Moveable, InputSubscriber, Drawable {

        ItemStack stack;
        private int x, y, width, height;

        private boolean isVisible = false;

        public ItemOption(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean onScroll(Gui gui, double i) {
            return false;
        }

        @Override
        public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
            if (GuiRect.isInBounds(x, y, this.x, this.y, width, 20) && isVisible) {
                close(Utils.itemStackToLuatable(stack));
                return true;
            }
            return false;
        }

        @Override
        public boolean onMouseRelease(Gui gui, double x, double y, int state) {
            return false;
        }

        @Override
        public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
            return false;
        }

        @Override
        public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean onCharTyped(Gui gui, char typedChar, int mods) {
            return false;
        }

        @Override
        public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
            return false;
        }

        @Override
        public void setPos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void setX(int x) {
            this.x = x;
        }

        @Override
        public void setY(int y) {
            this.y = y;
        }

        @Override
        public void setVisible(boolean b) {
            isVisible = b;
        }

        @Override
        public int getItemHeight() {
            return 20;
        }

        @Override
        public int getItemWidth() {
            return width;
        }

        @Override
        public void setWidth(int i) {
            width = i;
        }

        @Override
        public void setHeight(int i) {
            //this is constant
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
            if (!isVisible) {
                return;
            }
            g.drawBoxedRectangle(drawContext, x, y, width, 20, BGColor, FILLColor);
            DiffuseLighting.disableGuiDepthLighting();
            DiffuseLighting.enableGuiDepthLighting();
            drawContext.drawItemInSlot(textRenderer, stack, x + 3, y + 2);

            if (GuiRect.isInBounds(mouseX, mouseY, this.x, this.y, width, 20)) {
                drawContext.fill(x, y, x + width, y + 20, HEIGHLIGHT);
            }
            drawContext.drawText(g.getFontRend(), stack.getName(), x + 25, y + 5, TEXTCOLOR, false);

        }

    }

    private class CustomOption implements Moveable, InputSubscriber, Drawable {

        private int x, y, width, height;
        String option;

        private boolean isVisible = false;

        public CustomOption(String option) {
            this.option = option;
        }

        @Override
        public boolean onScroll(Gui gui, double i) {
            return false;
        }

        @Override
        public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
            if (GuiRect.isInBounds(x, y, this.x, this.y, width, 20) && isVisible) {
                close(LuaValue.valueOf(option));
                return true;
            }
            return false;
        }

        @Override
        public boolean onMouseRelease(Gui gui, double x, double y, int state) {
            return false;
        }

        @Override
        public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
            return false;
        }

        @Override
        public boolean onCharTyped(Gui gui, char typedChar, int mods) {
            return false;
        }

        @Override
        public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
            return false;
        }

        @Override
        public void setPos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void setX(int x) {
            setPos(x, y);
        }

        @Override
        public void setY(int y) {
            setPos(x, y);
        }

        @Override
        public void setVisible(boolean b) {
            isVisible = b;
        }

        @Override
        public int getItemHeight() {
            return 20;
        }

        @Override
        public int getItemWidth() {
            return width;
        }

        @Override
        public void setWidth(int i) {
            width = i;
        }

        @Override
        public void setHeight(int i) {
            //this is constant
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
            if (!isVisible) {
                return;
            }
            g.drawBoxedRectangle(drawContext, x, y, width, 20, BGColor, FILLColor);

            if (GuiRect.isInBounds(mouseX, mouseY, this.x, this.y, width, 20)) {
                drawContext.fill(x, y, x + width, y + 20, HEIGHLIGHT);
            }
            drawContext.drawText(g.getFontRend(), option, x + 7, y + 5, TEXTCOLOR, false);

        }

    }

    private class Prompt extends VarArgFunction {

        @Override
        public LuaValue invoke(Varargs args) {
            while (MinecraftClient.getInstance().currentScreen == AdvancedMacros.runningScriptsGui) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new LuaError(e);
                }
            }
            LuaValue arg0 = args.arg1();
            LuaValue type = args.arg(2);
            //System.out.println("Block Waiting");
            //			AdvancedMacros.forgeEventHandler.releaseAllKeys();
            synchronized (InputGUI.this) {
                //System.out.println("Block Entered!");
                try {
                    boolean stoped = false;

                    inputType = InputType.valueOf(type.optjstring("TEXT").toUpperCase());

                    if (inputType == InputType.CHOICE) {
                        choices.clear();
                        for (int i = 3; i <= args.narg(); i++) {
                            choices.add(new CustomOption(args.arg(i).checkjstring()));
                        }
                    }

                    while (!stoped && (threadCheck != Thread.currentThread())) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            stoped = true;
                        }
                        setInputType(inputType, arg0.tojstring()); //seems to effect the thread check
                    }
                    TaskDispatcher.addTask(() -> Mouse.setGrabbed(false));

                    semaphore = new Semaphore(0);

                    try {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().setScreen(AdvancedMacros.inputGUI);
                        });
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stoped = true;
                    //System.out.println("Not grabbed");
/*                    while (!answered && !stoped && (threadCheck != Thread.currentThread() || MinecraftClient.getInstance().currentScreen == InputGUI.this)) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            stoped = true;
                        }
                    }*/

                } catch (Exception e) {
                    threadCheck = null;
                    throw e;
                }
                LuaValue ourAns = answer; //used incase next thread happens to call and wipe out answer
                threadCheck = null;
                return ourAns;

            }
        }

    }

}
