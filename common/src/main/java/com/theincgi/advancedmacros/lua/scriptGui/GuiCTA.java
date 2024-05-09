package com.theincgi.advancedmacros.lua.scriptGui;

import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.elements.ColorTextArea;
import com.theincgi.advancedmacros.misc.PropertyPalette;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.gui.DrawContext;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import java.util.HashMap;

/**
 * Script controled color text area
 */
public class GuiCTA extends ScriptGuiElement {

    ColorTextArea cta;

    public GuiCTA(Gui gui, Group parent) {
        super(gui, parent, false);
        PropertyPalette colorPalette = new PropertyPalette();
        cta = new ColorTextArea(colorPalette, gui);
        cta.setVisible(true);
        enableSizeControl();
        cta.setEditable(true);
        //set("setFontSize")
        set("setText", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                cta.setText(arg.checkjstring());
                return GuiCTA.this;
            }
        });
        set("getText", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(cta.getText());
            }
        });
        set("setSyntaxHighlight", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                cta.doSyntaxHighlighting = arg.checkboolean();
                return GuiCTA.this;
            }
        });
        set("isSyntaxHighlight", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(cta.doSyntaxHighlighting);
            }
        });
        set("isEdited", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(cta.isNeedsSave());
            }
        });
        set("isFocused", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(cta.isFocused());
            }
        });
        set("setFocused", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                cta.setFocused(arg.checkboolean());
                gui.setFocusItem(cta);
                return GuiCTA.this;
            }
        });
        set("setEditable", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                cta.setEditable(arg.checkboolean());
                ;
                return GuiCTA.this;
            }
        });
        set("isEditable", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(cta.isEditable());
            }
        });
        set("settings", colorPalette.getTableFromKey());

        //		this.set("setX", new OneArgFunction() {
        //			@Override
        //			public LuaValue call(LuaValue arg) {
        //				cta.setPos( (int) arg.checkdouble(), cta.getY() );
        //				return LuaValue.NONE;
        //			}
        //		});
        //		this.set("getX", new ZeroArgFunction() {
        //			@Override
        //			public LuaValue call() {
        //				return LuaValue.valueOf(cta.getX());
        //			}
        //		});
        //		this.set("setY", new OneArgFunction() {
        //			@Override
        //			public LuaValue call(LuaValue arg) {
        //				cta.setPos( cta.getX(), (int) arg.checkdouble());
        //				return LuaValue.NONE;
        //			}
        //		});
        //		this.set("getY", new ZeroArgFunction() {
        //			@Override
        //			public LuaValue call() {
        //				return LuaValue.valueOf(cta.getY());
        //			}
        //		});
        this.set("setOpacity", NIL);
        this.set("getOpacity", NIL);

        //		this.set("getWidth", new ZeroArgFunction() {
        //			@Override
        //			public LuaValue call() {
        //				return LuaValue.valueOf(cta.getItemWidth());
        //			}
        //		});
        //		this.set("getHeight", new ZeroArgFunction() {
        //			@Override
        //			public LuaValue call() {
        //				return LuaValue.valueOf(cta.getItemHeight());
        //			}
        //		});

        this.set("setHoverTint", NIL);
        this.set("getHoverTint", NIL);

        set("updateKeywords", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                cta.updateKeywords();
                return NONE;
            }
        });
        set("getKeywords", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                LuaTable kw = new LuaTable();
                kw.set("tables", Utils.toTable(cta.tables.keySet()));
                kw.set("functions", Utils.toTable(cta.functions.keySet())); //TODO control tooltip
                kw.set("variables", Utils.toTable(cta.variables.keySet()));
                kw.set("keywords", Utils.toTable(cta.keywords.keySet()));
                return kw;
            }
        });
        set("setKeywords", new TwoArgFunction() {

            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                if (arg1.istable()) {
                    LuaValue n = arg1.next(NIL).arg1();
                    while (!n.isnil()) {
                        setKeywords(n.checkjstring(), arg1.get(n).checktable());
                        n = arg1.next(n).arg1();
                    }
                } else if (arg1.isstring()) {
                    setKeywords(arg1.checkjstring(), arg2.checktable());
                } else {
                    throw new LuaError(String.format("Invalid arguments <table>/<string, table> expected, got <%s, %s>", arg1.typename(), arg2.typename()));
                }
                return NONE;
            }

            public void setKeywords(String type, LuaTable values) {
                HashMap<String, Object> map = null;
                switch (type) {
                    case "tables":
                        map = cta.tables;
                        break;
                    case "functions":
                        map = cta.functions;
                        break;
                    case "variables":
                        map = cta.variables;
                        break;
                    case "keywords":
                        for (int k = 1; k <= values.length(); k++) {
                            cta.keywords.put(values.get(k).tojstring(), true);
                        }
                        break;
                    default:
                        throw new LuaError("Un-used table '" + type + "' [tables/functions/variables/keywords]");
                }
                for (int k = 1; k <= values.length(); k++) {
                    map.put(values.get(k).tojstring(), true);
                }
            }
        });
        set("__class", "advancedMacros.GuiTextArea");
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
        super.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
        if (!visible) {
            return;
        }
        cta.setPos((int) x, (int) y);
        cta.resize((int) wid, (int) hei);
        cta.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
    }

    public ColorTextArea getCTA() {
        return cta;
    }

    @Override
    public int getItemHeight() {
        // TODO Auto-generated method stub
        return (int) hei;
    }

    @Override
    public int getItemWidth() {
        // TODO Auto-generated method stub
        return (int) wid;
    }

    @Override
    public void setWidth(int i) {
        wid = i;
    }

    @Override
    public void setHeight(int i) {
        hei = i;
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        return cta.onMouseClick(gui, x, y, buttonNum);
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        return cta.onMouseClickMove(gui, x, y, buttonNum, q, r);
    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        return cta.onMouseRelease(gui, x, y, state);
    }

    @Override
    public boolean onCharTyped(Gui gui, char typedChar, int mods) {
        return cta.onCharTyped(gui, typedChar, mods);
    }

    @Override
    public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
        return cta.onKeyPressed(gui, keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
        return cta.onKeyPressed(gui, keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
        return cta.onKeyRepeat(gui, keyCode, scanCode, modifiers, n);
    }

    @Override
    public boolean onScroll(Gui g, double i) {
        return cta.onScroll(g, i);
    }

}
