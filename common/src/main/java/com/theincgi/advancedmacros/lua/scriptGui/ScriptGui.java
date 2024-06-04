package com.theincgi.advancedmacros.lua.scriptGui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.TaskDispatcher;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedmacros.gui.elements.GuiScrollBar.Orientation;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.lua.functions.Workspaces;
import com.theincgi.advancedmacros.misc.CallableTable;
import com.theincgi.advancedmacros.misc.HIDUtils;
import com.theincgi.advancedmacros.misc.HIDUtils.Mouse;
import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.misc.Workspace;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class ScriptGui extends LuaTable implements InputSubscriber {

    private static int counter = 1;
    String guiName = "generic_" + counter++;
    Gui gui;
    //LuaTable controls = new LuaTable();
    LuaFunction onScroll, onMouseClick, onMouseRelease, onMouseDrag, onKeyPressed, onCharTyped, onKeyReleased, onKeyRepeated, onGuiClose,
            onGuiOpen, onResize;
    Group guiGroup = new Group();
    ScriptGui parentGui = null;
    private boolean isOpen = false;
    private boolean pausesGame = true;
    private double lastMouseX, lastMouseY;
    Workspace workspace; //for MC thread events

    public ScriptGui() {
    	workspace = Utils.currentWorkspace();
        gui = new TheGui();
        //gui.inputSubscribers.add(this); //tell yourself everything!
        gui.addInputSubscriber(guiGroup);
        gui.addDrawable(guiGroup);

        for (OpCodes op : OpCodes.values()) {
            set(op.name(), new CallableTable(op.getDocLocation(), new DoOperation(op)));
        }
        set("__class", "advancedMacros.ScriptGui");
        addInputControls(this);
    }

    public void resize(MinecraftClient mcIn, int w, int h) {
        if (parentGui != null) {
            parentGui.resize(mcIn, w, h);
        }
        //super.resize(mcIn, w, h);
        if (onResize != null) {
            Utils.pcall(onResize, valueOf(w), valueOf(h));
        }
    }

    public class DoOperation extends VarArgFunction {

        OpCodes opCode;

        public DoOperation(OpCodes opCode) {
            super();
            this.opCode = opCode;
        }

        @Override
        public Varargs invoke(Varargs args) {
            switch (opCode) {
                case close:
                    MinecraftClient.getInstance().setScreen(null);
                    return NONE;
                case getName:
                    return LuaValue.valueOf(guiName);
                case getParentGui:
                    return parentGui == null ? NIL : parentGui;
                case getSize: {
                    LuaTable temp = new LuaTable();
                    MinecraftClient mc = MinecraftClient.getInstance();

                    temp.set(1, LuaValue.valueOf(mc.getWindow().getScaledWidth()));
                    temp.set(2, LuaValue.valueOf(mc.getWindow().getScaledHeight()));
                    temp.set(3, valueOf(mc.getWindow().getFramebufferWidth()));
                    temp.set(4, valueOf(mc.getWindow().getFramebufferHeight()));
                    return temp.unpack();
                }
                case newBox:
                    GuiBox r = new GuiBox(gui, guiGroup);
                    r.x = (float) args.optdouble(1, 0);
                    r.y = (float) args.optdouble(2, 0);
                    r.wid = (float) args.optdouble(3, 0);
                    r.hei = (float) args.optdouble(4, 0);
                    r.thickness = (float) args.optdouble(5, 1);
                    return r;
                case newGroup:
                    return new Group(guiGroup, 
                    		args.optint(1, 0), //x 
                    		args.optint(2, 0)  //y
                    );
                case newImage: {
                    GuiImage t = new GuiImage(gui, guiGroup);
                    t.setTexture(args.optvalue(1, LuaValue.NIL));
                    t.x = (float) args.optdouble(2, 0);
                    t.y = (float) args.optdouble(3, 0);
                    t.wid = (float) args.optdouble(4, 0);
                    t.hei = (float) args.optdouble(5, 0);
                    return t;
                }
                case newItem:
                    GuiItemIcon gii = new GuiItemIcon(gui, guiGroup);
                    if (!args.arg1().isnil()) {
                        gii.setStack(args.checkjstring(1));
                    }
                    gii.setPos(args.optint(2, 0), args.optint(3, 0));
                    gii.setCount(args.optint(4, 1));
                    return gii;
                case newMinecraftTextField:
                    MCTextBar text = new MCTextBar(gui, guiGroup);
                    text.setPos((int) args.optdouble(1, 0), (int) args.optdouble(2, 0));
                    text.setWidth(args.optint(3, 0));
                    text.setHeight(args.optint(4, 20));
                    text.setText(args.optstring(5, valueOf("")).tojstring());
                    return text;
                case newRectangle:
                    GuiRectangle rect = new GuiRectangle(gui, guiGroup);
                    rect.x = (float) args.optdouble(1, 0);
                    rect.y = (float) args.optdouble(2, 0);
                    rect.wid = (float) args.optdouble(3, 0);
                    rect.hei = (float) args.optdouble(4, 0);
                    return rect;
                case newScrollBar:
                    int x = args.optint(1, 0);
                    int y = args.optint(2, 0);
                    int wid = args.optint(3, 0);
                    int len = args.optint(4, 0);
                    Orientation orient = Orientation.from(args.arg(5).optjstring("vertical"));
                    if (orient.isLEFTRIGHT()) {
                        int swap = wid;
                        wid = len;
                        len = swap;
                    }
                    return new ScriptGuiScrollBar(gui, guiGroup, x, y, wid, len, orient);
                case newText: {
                    ScriptGuiText t = new ScriptGuiText(gui, guiGroup);
                    t.setText(args.optjstring(1, ""));
                    t.x = (float) args.optdouble(2, 0);
                    t.y = (float) args.optdouble(3, 0);
                    t.textSize = args.optint(4, 12);
                    return t;
                }
                case newTextArea:
                    GuiCTA cta = new GuiCTA(gui, guiGroup);
                    cta.setPos((int) args.optdouble(1, 0), (int) args.optdouble(2, 0));
                    cta.setWidth(args.optint(3, 0));
                    cta.setHeight(args.optint(4, 0));
                    cta.getCTA().setText(args.optstring(5, valueOf("")).tojstring());
                    return cta;
                case open: {
                    //AdvancedMacros.forgeEventHandler.releaseAllKeys();
                    Integer mx = null, my = null;
                    if (args.isnumber(1) && args.isnumber(2)) {
                        mx = args.checkint(1);
                        my = args.checkint(2);
                        MinecraftClient mc = MinecraftClient.getInstance();
                        //ScaledResolution scaled = new ScaledResolution(mc);
                        mx = mx * (mc.getWindow().getFramebufferWidth() / mc.getWindow().getScaledWidth());
                        my = mc.getWindow().getFramebufferHeight() - my * (mc.getWindow().getFramebufferHeight() / mc.getWindow().getScaledHeight());
                    }
                    final Integer fmx = mx, fmy = my;
                    TaskDispatcher.addTask(() -> {
                        MinecraftClient.getInstance().setScreen(gui);
                        Mouse.setGrabbed(false);
                        if (fmx != null && fmy != null) {
                            Mouse.setCursor(fmx, fmy);
                        }
                        //MinecraftClient.getInstance().mouseHelper.ungrabMouseCursor();
                    });
                    if (onGuiOpen != null) {
                        onGuiOpen.call();
                    }
                    isOpen = true;
                    return LuaValue.NONE;
                }
                case isOpen:
                    return valueOf(isOpen);
                case setName:
                    guiName = args.arg1().checkjstring();
                    return NONE;
                case setParentGui: {
                    LuaValue arg = args.arg1();
                    if (arg.isnil()) {
                        parentGui = null;
                    } else if (arg instanceof ScriptGui) {
                        parentGui = (ScriptGui) arg;
                    } else {
                        throw new LuaError("Gui expected");
                    }
                    return NONE;
                }
                case isDefaultBackground:
                    return valueOf(gui.getDrawDefaultBackground());
                case setDefaultBackground:
                    gui.setDrawDefaultBackground(args.checkboolean(1));
                    return NONE;
                case getMousePos: {
                    return Utils.varargs(valueOf(lastMouseX), valueOf(lastMouseY));
                }
                case grabMouse:
                    TaskDispatcher.addTask(() -> {
                        //MinecraftClient.getInstance().mouseHelper.grabMouseCursor();
                        Mouse.setGrabbed(true);
                    });
                    return NONE;
                case ungrabMouse:
                    TaskDispatcher.addTask(() -> {
                        Mouse.setGrabbed(false);//MinecraftClient.getInstance().mouseHelper.ungrabMouseCursor();
                    });
                    return NONE;
                case setPausesGame:
                    pausesGame = args.checkboolean(1);
                    return NONE;
                case isPausesGame:
                    return valueOf(pausesGame);
                case clear:
                    gui.clearInputSubscribers();
                    gui.clearDrawables();
                    return NONE;
                case setEventWorkspace:
                	workspace = Workspaces.getWorkspaceByName( args.checkjstring(1) );
                	return workspace.asTable();
                case getEventWorkspace:
                	return workspace.asTable();
                
                default:
                    throw new LuaError("This function hasn't been implemented D:");
            }
        }

    }

    public static enum OpCodes {
        clear,
        //remove,
        newRectangle,
        newBox,
        newGroup,
        newText,
        newTextArea,
        newScrollBar,
        newMinecraftTextField,
        newImage,
        newItem,
        open,
        close,
        setParentGui,
        getParentGui,
        getSize,
        setName,
        getName,
        setDefaultBackground,
        isDefaultBackground,
        isOpen,
        getMousePos,
        grabMouse, ungrabMouse, isPausesGame, setPausesGame,
        setEventWorkspace, getEventWorkspace;

        public String[] getDocLocation() {
            String[] out = new String[3];
            out[0] = "gui";
            out[1] = "new()";
            out[2] = this.name();
            return out;
        }

    }

    //TODO resize event

    public static class CreateScriptGui extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            return new ScriptGui();
        }

    }

    public void addInputControls(LuaTable s) {
        s.set("setOnResize", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onResize = arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnScroll", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onScroll = arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnMouseClick", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onMouseClick = arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnMouseRelease", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onMouseRelease = arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnMouseDrag", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onMouseDrag = arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnKeyPressed", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onKeyPressed = arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnKeyReleased", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onKeyReleased = arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnCharTyped", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onCharTyped = arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnOpen", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onGuiOpen = arg.checkfunction();
                return NONE;
            }
        });
        s.set("setOnClose", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onGuiClose = arg.checkfunction();
                return NONE;
            }
        });
    }

    @Override
    public boolean onScroll(Gui gui, double i) {
        if (onScroll != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onScroll, LuaValue.valueOf(i)).optboolean(false);
        }
        return false;
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (onMouseClick != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum)).optboolean(false);
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        if (onMouseRelease != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onMouseRelease, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(state)).optboolean(false);
        }
        return false;
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        if (onMouseDrag != null) {
            LuaTable args = new LuaTable();
            args.set(1, x);
            args.set(2, y);
            args.set(3, buttonNum);
            args.set(4, q);
            args.set(5, r);
            Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onMouseDrag, args.unpack()).arg1().optboolean(false);
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (onKeyPressed != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onKeyPressed,
                    LuaValue.valueOf(HIDUtils.Keyboard.nameOf(keyCode)),
                    LuaValue.valueOf(scanCode),
                    HIDUtils.Keyboard.modifiersToLuaTable(modifiers)
            ).optboolean(false);
        }
        return false;
    }

    @Override
    public boolean onCharTyped(Gui gui, char typedChar, int mods) {
        if (onCharTyped != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onCharTyped,
                    LuaValue.valueOf(typedChar),
                    HIDUtils.Keyboard.modifiersToLuaTable(mods)
            ).optboolean(false);
        }
        return false;
    }

    @Override
    public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
        if (onKeyRepeated != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onKeyRepeated, LuaValue.valueOf(HIDUtils.Keyboard.nameOf(keyCode)), LuaValue.valueOf(scanCode), LuaValue.valueOf(n), HIDUtils.Keyboard.modifiersToLuaTable(modifiers)).optboolean(false);
        }
        return false;
    }

    @Override
    public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (onKeyReleased != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onKeyReleased, LuaValue.valueOf(HIDUtils.Keyboard.nameOf(keyCode)), LuaValue.valueOf(scanCode), HIDUtils.Keyboard.modifiersToLuaTable(modifiers)).optboolean(false);
        }
        return false;
    }

    public class TheGui extends Gui {

        @Override
        public void close() {
            super.close();
            if (onGuiClose != null) {
            	Utils.setMCThreadWorkspace(workspace);
                Utils.pcall(onGuiClose);
            }
            isOpen = false;
            synchronized (getInputSubscribers()) {
                for (InputSubscriber s : getInputSubscribers()) {
                    if (s instanceof ScriptGuiElement) {
                        ScriptGuiElement sge = (ScriptGuiElement) s;
                        ScriptGuiElement.resetMouseOver();
                    }
                }
            }
            //GuiScreen tmp = MinecraftClient.getInstance().currentScreen;
        }

        @Override
        public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            MatrixStack matrixStack = drawContext.getMatrices();

            if (parentGui != null) {
                
                parentGui.gui.render(drawContext, mouseX, mouseY, partialTicks);
                //setDrawDefaultBackground(false);
            } else {
                //setDrawDefaultBackground(true);
            }
            matrixStack.push();
            matrixStack.translate(0, 0, nParents() * 50);
            super.render(drawContext, mouseX, mouseY, partialTicks);
            matrixStack.pop();
        }
        
        
        public int nParents() {
        	if(parentGui == null)
        		return 0;
        	if(parentGui instanceof ScriptGui)
        		if(((ScriptGui)parentGui).gui instanceof TheGui tg)
        			return tg.nParents() + 1;
        	return 1;
        }
        
        @Override
        public Text getTitle() {
            return Text.literal(guiName);
        }

        @Override
        public void resize(MinecraftClient mcIn, int w, int h) {
            ScriptGui.this.resize(mcIn, w, h);
        }

        @Override
        public boolean shouldPause() {
            return pausesGame;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
            if (ScriptGui.this.onMouseClick(this, mouseX, mouseY, mouseButton)) {
                return true;
            }
            if (parentGui != null) {
                return parentGui.onMouseClick(parentGui.gui, mouseX, mouseY, mouseButton);
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int buttonNum, double q, double r) {
            if (super.mouseDragged(mouseX, mouseY, buttonNum, q, r)) {
                return true;
            }
            if (ScriptGui.this.onMouseClickMove(this, mouseX, mouseY, buttonNum, q, r)) {
                return true;
            }
            if (parentGui != null) {
                return parentGui.onMouseClickMove(parentGui.gui, mouseX, mouseY, buttonNum, q, r);
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int state) {
            if (super.mouseReleased(mouseX, mouseY, state)) {
                return true;
            }
            if (ScriptGui.this.onMouseRelease(this, mouseX, mouseY, state)) {
                return true;
            }
            if (parentGui != null) {
                return parentGui.onMouseRelease(parentGui.gui, mouseX, mouseY, state);
            }
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return super.keyPressed(keyCode, scanCode, modifiers) ||
                    ScriptGui.this.onKeyPressed(this, keyCode, scanCode, modifiers) ||
                    (parentGui != null && parentGui.onKeyPressed(this, keyCode, scanCode, modifiers));
        }

        @Override
        public boolean charTyped(char typedChar, int mods) {
            return super.charTyped(typedChar, mods) ||
                    ScriptGui.this.onCharTyped(gui, typedChar, mods) ||
                    (parentGui != null && parentGui.onCharTyped(gui, typedChar, mods));
        }

        //
        //		@Override
        //		public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
        //			return super.onKeyRepeated(gui, keyCode, scanCode, modifiers, n) ||
        //					ScriptGui.this.onKeyRepeat(gui, keyCode, scanCode, modifiers, n) ||
        //					(parentGui!=null && parentGui.onKeyRepeat(parentGui.gui, keyCode, scanCode, modifiers, n));
        //		}
        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return super.onKeyRelease(gui, keyCode, scanCode, modifiers) ||
                    ScriptGui.this.onKeyRelease(this, keyCode, scanCode, modifiers) ||
                    (parentGui != null && parentGui.onKeyRelease(parentGui.gui, keyCode, scanCode, modifiers));
        }

        @Override
        public boolean mouseScrolled(double x, double y, double horizontalScroll, double verticalScroll) {
            return super.onScroll(gui, verticalScroll) ||
                    ScriptGui.this.onScroll(this, verticalScroll) ||
                    (parentGui != null && parentGui.onScroll(parentGui.gui, verticalScroll));
        }

        @Override
        public String toString() {
            return "ScriptGui:" + guiName;
        }

    }

}
