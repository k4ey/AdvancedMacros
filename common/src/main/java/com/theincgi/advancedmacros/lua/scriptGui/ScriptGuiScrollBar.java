package com.theincgi.advancedmacros.lua.scriptGui;

import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.elements.GuiScrollBar;
import com.theincgi.advancedmacros.gui.elements.GuiScrollBar.Orientation;
import com.theincgi.advancedmacros.misc.PropertyPalette;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.gui.DrawContext;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class ScriptGuiScrollBar extends ScriptGuiElement {

    GuiScrollBar bar;
    PropertyPalette propPal;

    public ScriptGuiScrollBar(Gui gui, Group parent, int x, int y, int wid, int len, Orientation orient) {
        super(gui, parent, true);
        propPal = new PropertyPalette();
        bar = new GuiScrollBar(x, y, wid, len, orient, propPal) {
            @Override
            public boolean isVisible() {
                return ScriptGuiScrollBar.this.visible;
            }

        };
        this.set("properties", propPal.settings); //color.colors was too ....ugh
        this.set("setMaxItems", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                bar.setItemCount(arg.checkint());
                return NONE;
            }
        });
        this.set("getMaxItems", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(bar.getItems());
            }
        });
        this.set("setVisibleItems", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                bar.setVisibleItems(arg.checkint());
                return NONE;
            }
        });
        this.set("getVisibleItems", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(bar.getVisibleItems());
            }
        });
        this.set("setScrollPos", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                bar.setScrollPos(arg.checkdouble());
                return NONE;
            }
        });
        this.set("focusTo", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                bar.focusToItem(arg.checkdouble());
                return NONE;
            }
        });
        this.set("getScrollPos", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                return valueOf(bar.getOffset());
            }
        });
        this.set("setScrollSpeed", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                bar.setScrollSpeed(arg.checkdouble());
                return null;
            }
        });
        this.set("getScrollSpeed", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(bar.getScrollSpeed());
            }
        });
        this.set("scroll", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue sign) {
                bar.onScroll(gui, sign.checkint());
                return NONE;
            }
        });
        //		this.set("setWidth", new OneArgFunction() {
        //			@Override
        //			public LuaValue call(LuaValue arg) {
        //				bar.setWid(arg.checkint());
        //				return NONE;
        //			}
        //		});this.set("setLength", new OneArgFunction() {
        //			@Override
        //			public LuaValue call(LuaValue arg) {
        //				bar.setLen(arg.checkint());
        //				return NONE;
        //			}
        //		});
        //		this.set("getWidth", new OneArgFunction() {
        //			@Override
        //			public LuaValue call(LuaValue arg) {
        //				return valueOf(bar.getOffset());
        //			}
        //		});
        //		this.set("getLength", new OneArgFunction() {
        //			@Override
        //			public LuaValue call(LuaValue arg) {
        //				return valueOf(bar.getOffset());
        //			}
        //		});
        this.set("getOrientation", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                return valueOf(bar.getOrientation().toString());
            }
        });
        this.set("setOrientation", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                bar.setOrientation(Orientation.from(arg.checkjstring()));
                return NONE;
            }
        });
        set("__class", "advancedMacros.GuiScrollBar");
        enableSizeControl();

    }

    @Override
    public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
        super.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
        bar.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (bar.onMouseClick(gui, x, y, buttonNum)) {
            if (onMouseClick != null) {
                Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        if (bar.onMouseClickMove(gui, x, y, buttonNum, q, r)) {
            if (onMouseDrag != null) {
                LuaTable args = new LuaTable();
                args.set(1, x);
                args.set(2, y);
                args.set(3, buttonNum);
                args.set(4, q);
                args.set(5, r);
                Utils.pcall(onMouseDrag, args.unpack());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        if (bar.onMouseRelease(gui, x, y, state)) {
            if (onMouseRelease != null) {
                Utils.pcall(onMouseRelease, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(state));
            }
            return true;
        }
        return false;
    }

    //	@Override
    //	public boolean onScroll(Gui gui, int i) {
    //			if(onScroll!=null)
    //				return Utils.pcall(onScroll, LuaValue.valueOf(i)).toboolean();
    //			return false;
    //	}
    @Override
    public void setX(int x) {
        bar.setX(x);
    }

    @Override
    public void setY(int y) {
        bar.setY(y);
    }

    @Override
    public void setX(double x) {
        bar.setX((int) x);
    }

    @Override
    public void setY(double y) {
        bar.setY((int) y);
    }

    @Override
    public int getItemHeight() {
        return bar.getItemHeight();
    }

    @Override
    public int getItemWidth() {
        return bar.getItemWidth();
    }

    @Override
    public void setWidth(int i) {
        bar.setWidth(i - 1); //frame wasn't included...
    }

    @Override
    public void setHeight(int i) {
        bar.setHeight(i - 1);
    }

    @Override
    public void setPos(int x, int y) {
        bar.setPos(x, y);
    }

    @Override
    public int getX() {
        return bar.getX();
    }

    @Override
    public int getY() {
        return bar.getY();
    }

}
