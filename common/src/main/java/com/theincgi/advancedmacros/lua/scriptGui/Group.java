package com.theincgi.advancedmacros.lua.scriptGui;

import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedmacros.gui.elements.Drawable;
import com.theincgi.advancedmacros.gui.elements.Moveable;
import com.theincgi.advancedmacros.misc.Pair;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import java.util.ArrayList;

public class Group extends LuaTable implements Moveable, InputSubscriber, Drawable {

    //gui group, xy 1,0
    //passes events to child elements/groups
    //child element needs getParent detail functions
    //moving a group should shift all child elements
    ArrayList<Object> children = new ArrayList<>();
    boolean groupVisiblity = true;
    int x = 0, y = 0;
    float z;
    Group parent = null;
    Pair<Integer, Integer> scissorOffset, scissorSize;
    boolean isRemoved;
    //LuaFunction widthCalculate, heightCalculate;

    public Group(Group parent) {
        this();
        changeParent(parent);
    }
    
    public Group(Group parent, int x, int y) {
    	this();
    	changeParent(parent);
    	this.x = x;
    	this.y = y;
    }

    public Group() {
        this.set("setVisible", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                groupVisiblity = arg.checkboolean();
                return LuaValue.NONE;
            }
        });
        this.set("isVisible", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(groupVisiblity);
            }
        });
        this.set("move", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                int dx = arg1.checkint();
                int dy = arg2.checkint();
                move(dx, dy);
                return LuaValue.NONE;
            }
        });
        this.set("setPos", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                setPos(arg1.checkint(), arg2.checkint());
                return LuaValue.NONE;
            }
        });
        this.set("setX", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1) {
                setPos(arg1.checkint(), y);
                return LuaValue.NONE;
            }
        });
        this.set("setY", new OneArgFunction() {
        	@Override
        	public LuaValue call(LuaValue arg1) {
        		setPos(x, arg1.checkint());
        		return LuaValue.NONE;
        	}
        });
        this.set("setZ", new OneArgFunction() {
        	@Override
        	public LuaValue call(LuaValue arg1) {
        		z = (float) arg1.checkdouble();
        		return LuaValue.NONE;
        	}
        });
        this.set("getPos", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs arg) {
                LuaTable t = new LuaTable();
                t.set(1, LuaValue.valueOf(x));
                t.set(2, LuaValue.valueOf(y));
                return t.unpack();
            }
        });
        this.set("getX", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(x);
            }
        });
        this.set("getY", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return valueOf(y);
            }
        });
        this.set("getZ", new ZeroArgFunction() {
        	@Override
        	public LuaValue call() {
        		return valueOf(z);
        	}
        });
        this.set("setParent", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg, LuaValue applyTransforms) {
            	if (arg instanceof Group) {
                	Group group = (Group) arg;
                	if(group == parent)
                		return NONE;
                	if( applyTransforms.optboolean( true )) {
	                	move( group.x, group.y );
	                	if( parent != null ) {
	                		move( -parent.x, -parent.y );
	                	}
                	}
                    changeParent(group);
                } else {
                    throw new LuaError("arg is not GuiGroup");
                }
                return LuaValue.NONE;
            }
        });
        this.set("addSubGroup", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return new Group(Group.this);
            }
        });

        this.set("getChildren", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                LuaTable result = new LuaTable();
                for (Object o : children) {
                    if (o instanceof LuaTable) {
                        result.set(result.length() + 1, (LuaValue) o);
                    }
                }
                return result;
            }
        });

        this.set("setScissor", new VarArgFunction() {
        	@Override
        	public Varargs invoke(Varargs args) {
        		int x = Group.this.getX();
        		int y = Group.this.getY();
        		int width, height;
        		if(args.narg() > 0 && (args.arg1() == FALSE || args.arg1() == NIL)) {
        			scissorOffset = null;
        			scissorSize = null;
        		} else if(args.narg() == 2) {
        			width = args.checkint(1);
        			height = args.checkint(2);
        			scissorSize = new Pair<Integer, Integer>(width, height);
        			scissorOffset = null;
        		} else if(args.narg() == 4) {
        			x = args.checkint(1);
        			y = args.checkint(2);
        			width = args.checkint(3);
        			height = args.checkint(4);
        			scissorOffset = new Pair<Integer, Integer>(x, y);
        			scissorSize = new Pair<Integer, Integer>(width, height);
        		} else {
        			throw new LuaError("Expected args nil/false or <x, y,> width, height");
        		}
        		return NONE;
        	}
		});
        
        this.set("remove", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				synchronized (this) {					
					if (isRemoved) {
	                    return NONE;
	                }
	                isRemoved = true;
	                if(parent != null) {
	                	parent.children.remove(Group.this);
	                }
	                return NONE;
				}
			}
		});
        this.set("unremove", new ZeroArgFunction() { //back from the dead
            @Override
            public LuaValue call() {
                synchronized (this) {
                    if (!isRemoved) {
                        return NONE;
                    }
                    isRemoved = false;
                    if(parent != null) {
                    	parent.children.add(Group.this);
                    }
                    
                    return NONE;
                }
            }
        });
        
        this.set("__class", "advancedMacros.GuiGroup");
        //		controls.set("setWidthCalculate", new OneArgFunction() {
        //			@Override
        //			public LuaValue call(LuaValue arg) {
        //				widthCalculate = arg.checkfunction();
        //				return LuaValue.NONE;
        //			}
        //		});
        //		controls.set("setHeightCalculate", new OneArgFunction() {
        //			@Override
        //			public LuaValue call(LuaValue arg) {
        //				heightCalculate = arg.checkfunction();
        //				return LuaValue.NONE;
        //			}
        //		});
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
        if (groupVisiblity) {
        	if(scissorSize != null) {
        		if(scissorOffset != null) { 
        			drawContext.enableScissor(
        				getX() + scissorOffset.a,
        				getY() + scissorOffset.b,
        				getX() + scissorOffset.a + scissorSize.a,
        				getY() + scissorOffset.b + scissorSize.b        				
        			);
        		} else {
        			drawContext.enableScissor(
            				getX(),
            				getY(),
            				getX() + scissorSize.a,
            				getY() + scissorSize.b        				
            			);
        		}
        	}
        	MatrixStack matrixStack = drawContext.getMatrices();
        	matrixStack.push();
        	matrixStack.translate(0, 0, z);
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i) instanceof Drawable) {
                	Drawable child = (Drawable) children.get(i);
                    child.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
                }
            }
            matrixStack.pop();
            if(scissorSize != null) {
            	drawContext.disableScissor();
            }
        }
    }

    @Override
    public boolean onScroll(Gui gui, double i) {
        int di = (int) Math.signum(i);
        if (groupVisiblity) {
            for (int j = 0; j < children.size(); j++) {
                if (children.get(j) instanceof InputSubscriber) {
                    InputSubscriber child = (InputSubscriber) children.get(di);
                    if (child.onScroll(gui, i)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (groupVisiblity) {
            for (int j = 0; j < children.size(); j++) {
                if (children.get(j) instanceof InputSubscriber) {
                    InputSubscriber child = (InputSubscriber) children.get(j);
                    if (child.onMouseClick(gui, x, y, buttonNum)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        if (groupVisiblity) {
            for (int j = 0; j < children.size(); j++) {
                if (children.get(j) instanceof InputSubscriber) {
                    InputSubscriber child = (InputSubscriber) children.get(j);
                    if (child.onMouseRelease(gui, x, y, state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        if (groupVisiblity) {
            for (int j = 0; j < children.size(); j++) {
                if (children.get(j) instanceof InputSubscriber) {
                    InputSubscriber child = (InputSubscriber) children.get(j);
                    if (child.onMouseClickMove(gui, x, y, buttonNum, q, r)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onCharTyped(Gui gui, char typedChar, int mods) {
        if (groupVisiblity) {
            for (int j = 0; j < children.size(); j++) {
                if (children.get(j) instanceof InputSubscriber) {
                    InputSubscriber child = (InputSubscriber) children.get(j);
                    if (child.onCharTyped(gui, typedChar, mods)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (groupVisiblity) {
            for (int j = 0; j < children.size(); j++) {
                if (children.get(j) instanceof InputSubscriber) {
                    InputSubscriber child = (InputSubscriber) children.get(j);
                    if (child.onKeyPressed(gui, keyCode, scanCode, modifiers)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
        if (groupVisiblity) {
            for (int j = 0; j < children.size(); j++) {
                if (children.get(j) instanceof InputSubscriber) {
                    InputSubscriber child = (InputSubscriber) children.get(j);
                    if (child.onKeyRepeat(gui, keyCode, scanCode, modifiers, n)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (groupVisiblity) {
            for (int j = 0; j < children.size(); j++) {
                if (children.get(j) instanceof InputSubscriber) {
                    InputSubscriber child = (InputSubscriber) children.get(j);
                    if (child.onKeyRelease(gui, keyCode, scanCode, modifiers)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setPos(int x, int y) {
        int dx = x - this.getX();
        int dy = y - this.getY();
        move(dx, dy);
    }

    @Override
    public void setX(int x) {
        setPos(x, 0);
    }

    @Override
    public void setY(int y) {
        setPos(0, y);
    }

    @Override
    public void setVisible(boolean b) {
        this.groupVisiblity = b;
    }

    @Override
    public int getItemHeight() {
        //return heightCalculate.call().checkint();
        return 0;
    }

    @Override
    public int getItemWidth() {
        //return widthCalculate.call().checkint();
        return 0;
    }

    @Override
    public void setWidth(int i) {
        //System.err.println("Group#setWidth called, function is used to calculate width");
    }

    @Override
    public void setHeight(int i) {
        //System.err.println("Group#setHeight called, function is used to calculate height");
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public void move(int dx, int dy) {
        for (int i = 0; i < children.size(); i++) {
        	if (children.get(i) instanceof Group g) {
        		g.move(dx, dy);
        	} else if (children.get(i) instanceof Moveable e) {
                e.setPos(e.getX() + dx, e.getY() + dy);
            }
        }
        x += dx;
        y += dy;
    }

    //	public void setParentControls(ExtendedLuaTableObject element) {
    //		element.set("getParent", controls);
    //	}
    public void setParentControls(LuaTable element) {
        element.set("getParent", new ZeroArgFunction() {	
			@Override
			public LuaValue call() {
				return this;
			}
		}); //TODO inspect
    }

    @Override
    public int type() {
        return LuaValue.TUSERDATA;
    }

    @Override
    public String typename() {
        return LuaValue.TYPE_NAMES[type()];
    }

    //remove from old parent, change controls to use new parent, save, add to list of children in new list
    public void changeParent(Group arg) {
        if (parent != null) {
            this.parent.children.remove(this);
        }
        arg.setParentControls(this);
        this.parent = arg;
        arg.children.add(this);
    }
    
}
