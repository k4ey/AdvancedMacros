package com.theincgi.advancedmacros.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theincgi.advancedmacros.gui.Color;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.Gui.Focusable;
import com.theincgi.advancedmacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedmacros.lua.LuaValTexture;
import com.theincgi.advancedmacros.misc.PropertyPalette;
import com.theincgi.advancedmacros.misc.Settings;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.gui.DrawContext;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import java.awt.image.BufferedImage;

public class GuiButton extends GuiRect implements InputSubscriber, Focusable {

    //	Property colorText;
    //	Property clickSound;
    //	Property buttonImg;
    //	Property buttonText;
    Color textColor;
    private static final Color DEFAULT_TEXT_COLOR = Color.WHITE;
    private LuaValTexture texture;
    private String text;
    private BufferedImage buffImg;
    private boolean isEnabled = true;
    GuiAnimation scaleAnimation = new GuiAnimation(150, Interpolator.smooth);
    private static final int disableColor = new Color(100, 0, 0, 0).toInt();
    OnClickHandler onClick;

    //public boolean doScaleAnimation = true;
    //private static final String defaultPropTableName = "colors.standardButton";
    //GuiRect shade;
    //	PropertyPalette propertyPalette;

    /**
     * Blank propPath will create a un bound propPalette
     */
    public GuiButton(int x, int y, int wid, int hei, LuaValue defaultImg, LuaValue defaultText, String... propPath) {
        this(x, y, wid, hei, propPath.length == 0 ? new PropertyPalette() : new PropertyPalette(propPath));
        texture = Utils.checkTexture(defaultImg);
        text = defaultText.isnil() ? null : defaultText.tojstring();
    }

    public GuiButton(int x, int y, int wid, int hei, LuaValue defaultImg, LuaValue defaultText, PropertyPalette propPal) {
        this(x, y, wid, hei, propPal);
        texture = Utils.checkTexture(defaultImg);
        text = defaultText.isnil() ? null : defaultText.tojstring();
    }

    public GuiButton(int x, int y, int wid, int hei, PropertyPalette propPal) {
        super(x, y, wid, hei, propPal);
        this.propertyPalette = propPal;
        propPal.addColorIfNil(DEFAULT_TEXT_COLOR, "colors", "text");
        textColor = propPal.getColor("colors", "text");
    }

    //	/**@param defaultImg - use Settings.getTextureID(String fileName)*/
    //	public GuiButton(WidgetID wID, int x, int y, int wid, int hei, LuaValue defaultImg, LuaValue defaultText, String nullOrTablePropName) {
    ////		super(wID, x, y, wid, hei,
    ////				nullOrTablePropName = (nullOrTablePropName==null?defaultPropTableName:nullOrTablePropName));
    ////
    //////		LuaTable t = new LuaTable();
    //////		t.set(1, "text");
    //////		t.set(2, txt);
    //////		buttonContent = new Property("widgetContent."+wID.getID(), t, "content", wID);
    ////		buttonImg = new Property("widgetContent."+wID.getID()+".img", defaultImg, "img", wID);
    ////		buttonText = new Property("widgetContent."+wID.getID()+".text", defaultText, "text", wID);
    ////		if(!buttonImg.getPropValue().isnil() && buttonImg.getPropValue() instanceof LuaValTexture){
    ////			texture = (LuaValTexture) buttonImg.getPropValue();
    ////		}
    ////		if(!buttonText.getPropValue().isnil()){
    ////			text = buttonText.getPropValue().checkjstring();
    ////		}
    //		this(wID, x, y, wid, hei, defaultImg, defaultText, nullOrTablePropName, Color.BLACK, Color.WHITE, Color.WHITE);
    //}
    //	public GuiButton(WidgetID wID, int x, int y, int wid, int hei, LuaValue defaultImg, LuaValue defaultText, String nullOrTablePropName, Color fill, Color frame, Color textColor) {
    //		super(wID, x, y, wid, hei,
    //				nullOrTablePropName = (nullOrTablePropName==null?defaultPropTableName:nullOrTablePropName), fill, frame);
    //
    ////		LuaTable t = new LuaTable();
    ////		t.set(1, "text");
    ////		t.set(2, txt);
    ////		buttonContent = new Property("widgetContent."+wID.getID(), t, "content", wID);
    //		buttonImg = new Property("widgetContent."+wID.getID()+".img", defaultImg, "img", wID);
    //		buttonText = new Property("widgetContent."+wID.getID()+".text", defaultText, "text", wID);
    //		if(!buttonImg.getPropValue().isnil() && buttonImg.getPropValue() instanceof LuaValTexture){
    //			texture = (LuaValTexture) buttonImg.getPropValue();
    //		}
    //		if(!buttonText.getPropValue().isnil()){
    //			//System.out.println(buttonText.getPropName());
    //			//System.out.println(Utils.LuaTableToString(buttonText.getPropValue().checktable()));
    //			text = buttonText.getPropValue().checkjstring();
    //		}
    //		this.textColor = textColor;
    //	}

    public GuiButton setImg(String img) {
        if (texture != null) {
            texture.deleteTex();
        }
        if (img != null) {
            LuaValue v = Settings.getTextureID(img);
            if (v instanceof LuaValTexture) {
                texture = (LuaValTexture) v;
            }
        } else {
            texture = null;
        }
        return this;
    }

    public GuiButton setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public void move(int x, int y) {
        super.move(x, y);
        //shade.move(x, y);
    }

    @Override
    public void resize(int wid, int hei) {
        super.resize(wid, hei);
        //shade.resize(wid, hei);
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui gui, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible) {
            return;
        }
        //		RenderSystem.disableAlpha();
        //		RenderSystem.enableAlpha();
        //
        //		RenderSystem.disableBlend();
        //		RenderSystem.enableBlend();
        //		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        int oldHeight = gui.getFontRend().fontHeight;
        //FIXME un-editable: gui.getFontRend().FONT_HEIGHT = this.getHei()-4;
        if (super.doAnimation) {
            if (isInBounds(mouseX, mouseY)) {
                if (!scaleAnimation.isPlaying() && scaleAnimation.isAtStart()) {
                    scaleAnimation.setReverse(false);
                    scaleAnimation.start();
                    //System.out.println("Start");
                }
            } else {
                if (!scaleAnimation.isPlaying() && !scaleAnimation.isAtStart()) {
                    scaleAnimation.setReverse(true);
                    scaleAnimation.start();
                }
            }
        }
        scale(GuiAnimation.map(scaleAnimation.doInterpolate(), 0, 1, 1, scaleTo)); //map p from [0,1] to [1,1.5]
        //shade.scale(getScale());

        if (texture != null) {
            RenderSystem.bindTexture(0);
            //			GL11.glDisable(GL11.GL_ALPHA_TEST);
            //			GL11.glDepthMask(false);
            //			GL11.glDisable(GL11.GL_LIGHTING);
            gui.drawImage(drawContext, texture.getResourceLocation(), getDrawX() + 1, getDrawY() + 1, getDrawWid() - 1, getDrawHei() - 1, 0, 0, 1, 1);
        }
        if (isInBounds(mouseX, mouseY) && isEnabled) {
            drawShade(drawContext, gui);
        }

        if (text != null) {
            gui.drawCenteredString(drawContext, gui.getFontRend(), text, getDrawX() + getDrawWid() / 2, getDrawY() + getDrawHei() / 2, textColor.toInt());
        }

        //FIXME un-editable: gui.getFontRend().FONT_HEIGHT=oldHeight;

        if (!isEnabled) {
            drawContext.fill(getDrawX() + 1, getDrawY() + 1, getDrawX() + getDrawWid(), getDrawY() + getDrawHei(), disableColor);
        }
        //		RenderSystem.popAttrib();
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    private boolean wasInBox = false;

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (!isVisible || !isEnabled) {
            return false;
        }
        if (isInBounds(x, y)) {
            wasInBox = true;
            gui.setFocusItem(this);
            return true;
        } else {
            wasInBox = false;
            return false;
        }

    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        //System.out.printf("In bounds: %s, wasInbounds %s\n",isInBounds(x, y),wasInBox);
        if (wasInBox && isInBounds(x, y)) {
            //System.out.println("In bounds");
            if (onClick != null) {
                //System.out.println("CLIK");
                onClick.onClick(state, this);
            }
        }
        wasInBox = false;
        return gui.getFocusItem() != null && gui.getFocusItem().equals(this);
    }

    @Override
    public boolean onScroll(Gui gui, double i) {
        return false;
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        if (gui.getFocusItem() == null) {
            return false;
        }
        return gui.getFocusItem().equals(this);
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
    public LuaTable getWidgetControls() {
        LuaTable t = super.getWidgetControls();
        return t;
    }

    //	//load props called by super
    //	@Override
    //	protected void loadProps(String nullOrPropTableName) {
    //		nullOrPropTableName = nullOrPropTableName==null?defaultPropTableName:nullOrPropTableName;
    //		colorFill  = new Property(nullOrPropTableName+".fill" , fill.toLuaValue(),    "color.fill",  wID);
    //		colorFrame = new Property(nullOrPropTableName+".frame", frame.toLuaValue(),    "color.frame", wID);
    //		colorShade = new Property(nullOrPropTableName+".shade", DEFAULT_SHADE.toLuaValue(),         "color.shade", wID);
    //		//System.out.println("Blab is null "+textColor);
    //		colorText  = new Property(nullOrPropTableName+".text",  DEFAULT_TEXT_COLOR.toLuaValue(),"color.text",  wID);
    //		clickSound = new Property("sounds.standardButton.click", defaultClickProp, "sound.click", wID);
    //		System.out.println("Some propertys were set up");
    //	}
    public String getText() {
        return text;
    }

    public void setOnClick(OnClickHandler och) {
        this.onClick = och;
    }

    /**
     * can be null
     */
    public void changeTexture(LuaValTexture sTex) {
        texture = sTex;
    }

    private double scaleTo = 1.2;

    public void setScaleTo(double d) {
        scaleTo = d;
    }

    public OnClickHandler getOnClickHandler() {
        return onClick;
    }

    public void setEnabled(boolean b) {
        isEnabled = b;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    public void setFocused(boolean f) {
        //no focus needed
    }

}
