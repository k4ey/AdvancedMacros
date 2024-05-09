package com.theincgi.advancedmacros.hud.hud2D;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class Hud2D_Box extends Hud2D_Rectangle {

    float thickness = 1, lastThickness = 1;

    public Hud2D_Box() {
        super();
        getControls().set("setThickness", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                thickness = (float) arg.checkdouble();
                return LuaValue.NONE;
            }
        });
        getControls().set("getThickness", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(thickness);
            }
        });
        super.enableColorControl();
    }

    @Override
    public void render(DrawContext drawContext, float partialTicks) {
        MatrixStack matrixStack = drawContext.getMatrices();

        matrixStack.push();
        //TODO 1.19 Update: RenderSystem.pushTextureAttributes();
        applyTransformation(matrixStack);
        float dx = 0, dy = 0, dw = wid, dh = hei;
        if (allowFrameInterpolation) {
            dx = interpolate(dx, lastX, partialTicks);
            dy = interpolate(dy, lastY, partialTicks);
            dw = interpolate(dw, lastWid, partialTicks);
            dh = interpolate(dh, lastHei, partialTicks);
        }

        drawRectangle(dx, dy, dw, thickness, color, z);
        drawRectangle(dx, dy, thickness, dh, color, z);
        drawRectangle(dx + dw - 1, dy, thickness, dh, color, z);
        drawRectangle(dx, dy + dh - 1, dw, thickness, color, z);

        matrixStack.pop();
    }

    @Override
    public void updateLastPos() {
        super.updateLastPos();
        lastThickness = thickness;
    }

}
