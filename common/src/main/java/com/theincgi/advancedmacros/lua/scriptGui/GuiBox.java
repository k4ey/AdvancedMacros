package com.theincgi.advancedmacros.lua.scriptGui;

import com.theincgi.advancedmacros.gui.Gui;
import net.minecraft.client.gui.DrawContext;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class GuiBox extends ScriptGuiElement {

    public float thickness = 1;

    public GuiBox(Gui gui, Group parent) {
        super(gui, parent);
        enableColorControl();
        enableSizeControl();
        set("setThickness", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                thickness = (float) arg.checkdouble();
                return NONE;
            }
        });
        set("getThickness", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(thickness);
            }
        });
        set("__class", "advancedMacros.GuiBox");
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
        super.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
        if (!visible) {
            return;
        }

        GuiRectangle.drawRectangle(drawContext, x, y, wid, thickness, color, z);
        GuiRectangle.drawRectangle(drawContext, x, y, thickness, hei, color, z);
        GuiRectangle.drawRectangle(drawContext, x + wid - 1, y, thickness, hei + thickness - 1, color, z);
        GuiRectangle.drawRectangle(drawContext, x, y + hei - 1, wid + thickness - 1, thickness, color, z);
    }

    @Override
    public int getItemHeight() {
        return (int) hei;
    }

    @Override
    public int getItemWidth() {
        return (int) wid;
    }

    @Override
    public void setWidth(int i) {
        this.wid = i;
    }

    @Override
    public void setHeight(int i) {
        this.hei = i;
    }

}
