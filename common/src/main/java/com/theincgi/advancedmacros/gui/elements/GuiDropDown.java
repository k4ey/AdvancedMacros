package com.theincgi.advancedmacros.gui.elements;

import com.theincgi.advancedmacros.gui.Color;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedmacros.misc.PropertyPalette;
import com.theincgi.advancedmacros.misc.Settings;
import net.minecraft.client.gui.DrawContext;
import org.luaj.vm2_v3_0_1.LuaValue;

public class GuiDropDown implements Drawable, InputSubscriber, Moveable {

    ListManager listManager;
    GuiRect txtBG/*, listBG*/;
    GuiButton dropButton;
    String dispText = "";
    int index = 0;
    //Property colorText;

    private static final String defaultTableName = "colors.standardDropDownBox";
    private WidgetID wID;
    private int x, y, width, height, maxHeight;
    String nullOrPropTableName;
    private OnClickHandler och;

    PropertyPalette propertyPalette;
    PropertyPalette optionPalette;

    public GuiDropDown(int x, int y, int width, int height, int maxHeight, String... propPath) {
        this(x, y, width, height, maxHeight, propPath.length == 0 ? new PropertyPalette() : new PropertyPalette(propPath));
    }

    public GuiDropDown(int x, int y, int width, int height, int maxHeight) {
        this(x, y, width, height, maxHeight, new PropertyPalette());
    }

    public GuiDropDown(int x, int y, int width, int height, int maxHeight, PropertyPalette propertyPalette) {
        this.propertyPalette = propertyPalette;
        this.optionPalette = propertyPalette.propertyPaletteOf("options");
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHeight = maxHeight;

        dropButton = new GuiButton(x + width - height, y,
                height, height,
                Settings.getTextureID("resource:whitedowntri.png"), LuaValue.NIL, propertyPalette.propertyPaletteOf("dropButton"));
        txtBG = new GuiRect(x, y, width - dropButton.getWid(), height, propertyPalette.propertyPaletteOf("textBackground"));
        propertyPalette.addColorIfNil(Color.WHITE, "colors", "text");
        //	}
        //	public GuiDropDown(WidgetID wID, int x, int y, int width, int height, int maxHeight, String nullOrPropTableName) {
        //		this.wID = wID;
        //		this.x = x; this.y = y;
        //		this.width = width; this.height = height;
        //		this.maxHeight = maxHeight;
        //		this.nullOrPropTableName = nullOrPropTableName = nullOrPropTableName==null?defaultTableName:nullOrPropTableName;
        //
        //
        //		dropButton = new GuiButton(wID,
        //				x+width-height, 	y,
        //				height, 		height,
        //				Settings.getTextureID("resource:whiteDownTri.png"), LuaValue.NIL, //"resource:whiteDownTri.png"
        //				nullOrPropTableName,
        //				Color.BLACK, Color.WHITE, Color.WHITE);
        //		//dropButton = new GuiButton()
        //		txtBG = new GuiRect(wID, x, y, width-dropButton.getWid(), height, nullOrPropTableName, Color.BLACK, Color.WHITE);
        listManager = new ListManager(x, y + height, width, maxHeight - height, /*wID, nullOrPropTableName*/ propertyPalette);
        listManager.scrollBar.setWid(height);
        //listBG = new GuiRect(wID, listManager.getX(), listManager.getY(), listManager.getItemWidth(), listManager.getItemHeight(), null, Color.BLACK, Color.WHITE);
        //colorText = new Property(nullOrPropTableName+".textColor", Color.WHITE.toLuaValue(), "textColor", wID);

        och = new OnClickHandler() {
            @Override
            public void onClick(int button, GuiButton sButton) {
                index = listManager.find(sButton);
                dispText = sButton.getText();
                close();
                if (onSelect != null) {
                    onSelect.onSelect(index, dispText);
                }
            }
        };

        dropButton.setOnClick(new OnClickHandler() {
            @Override
            public void onClick(int button, GuiButton sButton) {
                open();
            }
        });
        listManager.setVisible(false);
        listManager.setSpacing(0);
        dropButton.doAnimation = false;
        addOption("");
    }

    public void addOption(String s) {
        //-listManager.scrollBar.getItemWidth()
        //		GuiButton b = new GuiButton(wID, x, y, txtBG.getWid(), height, LuaValue.NIL, LuaValue.NIL, nullOrPropTableName, Color.BLACK, Color.WHITE, Color.WHITE);
        GuiButton b = new GuiButton(x, y, txtBG.getWid(), height, LuaValue.NIL, LuaValue.NIL, optionPalette);
        b.changeTexture(null);
        b.setScaleTo(1.05);
        listManager.add(b);
        b.setOnClick(och);
        b.setText(s);
    }

    public GuiButton makeOption(String s) {
        //		GuiButton b = new GuiButton(wID, x, y, txtBG.getWid(), height, LuaValue.NIL, LuaValue.NIL, nullOrPropTableName, Color.BLACK, Color.WHITE, Color.WHITE);
        GuiButton b = new GuiButton(x, y, txtBG.getWid(), height, LuaValue.NIL, LuaValue.NIL, optionPalette);
        b.changeTexture(null);
        b.setScaleTo(1.05);
        b.setOnClick(och);
        b.setText(s);
        return b;
    }

    public void removeOption(int opt) {
        listManager.remove(opt);
    }

    public void clear(boolean addEmpty) {
        listManager.clear();
        if (addEmpty) {
            addOption("");
        }
    }

    public String getText(int index) {
        Moveable m = listManager.getItem(index);
        if (m instanceof GuiButton) {
            return ((GuiButton) m).getText();
        }
        return "";
    }

    //	Runnable onChoice;
    //	public void setOnChoice(Runnable onChoice) {
    //		this.onChoice = onChoice;
    //	}

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        txtBG.move(x, y);
        dropButton.move(txtBG.getX() + txtBG.getWid(), y);
        listManager.setPos(x, y + txtBG.getHei());
        //System.out.println("SET "+x);
    }

    @Override
    public void setX(int x) {
        setPos(x, y);
    }

    @Override
    public void setY(int y) {
        setPos(x, y);
    }

    private boolean isVisible = true;

    @Override
    public void setVisible(boolean b) {
        txtBG.setVisible(b);
        dropButton.setVisible(b);
        isVisible = b;
    }

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public int getItemHeight() {
        return txtBG.getHei();
    }

    @Override
    public int getItemWidth() {
        return txtBG.getWid() + dropButton.getWid();
    }

    private OnSelectHandler onSelect;

    public void setOnSelect(OnSelectHandler oSel) {
        onSelect = oSel;
    }

    public boolean isInBounds(double x, double y) {
        if (txtBG.getX() <= x && x <= dropButton.getX() + dropButton.getWid() && txtBG.getY() <= y) {
            if (listManager.isVisible()) {
                return y <= listManager.getY() + listManager.getItemHeight();
            } else {
                return y <= txtBG.getY() + txtBG.getHei();
            }
        }
        return false;
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui gui, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible) {
            return;
        }
        //if(gui.height< listManager.getY()+listManager.getTotalHeight()){
        //listManager.setPos(x, x-Math.min(listManager.getTotalHeight(), maxHeight));
        //}else{
        listManager.setPos(x, y + height);
        //}
        //System.out.println(x);
        txtBG.setPos(x, y);
        txtBG.setWidth(width);
        txtBG.onDraw(drawContext, gui, mouseX, mouseY, partialTicks);
        if (listManager.isVisible()) {
            gui.drawLast = drawTop;
        }
        dropButton.setPos(x + width - dropButton.getWid(), y);
        dropButton.onDraw(drawContext, gui, mouseX, mouseY, partialTicks);
        if (dispText != null) {
            gui.drawCenteredString(drawContext, gui.getFontRend(), dispText, txtBG.getX() + txtBG.getWid() / 2, txtBG.getY() + txtBG.getHei() / 2, propertyPalette.getColor("colors", "text").toInt());
        }

    }

    public String getSelection() {
        return dispText;
    }

    private Drawable drawTop = new Drawable() {
        @Override
        public void onDraw(DrawContext drawContext, Gui gui, int mouseX, int mouseY, float partialTicks) {
            if (listManager.isVisible()) {
                gui.renderBackground(drawContext, 10, mouseY, partialTicks);
                //				listBG.setPos(x, listManager.getY());
                //				listBG.setWidth(listManager.getItemWidth()-listManager.scrollBar.getItemHeight());
                //				listBG.setHeight(listManager.getTotalHeight());
                //				listBG.onDraw(gui, mouseX, mouseY, partialTicks);
                //System.out.printf("MaxHeight: %s, Height: %s = %s\n", maxHeight, height, maxHeight-height);
                //listManager.setHeight(maxHeight-height);

                listManager.onDraw(drawContext, gui, mouseX, mouseY, partialTicks);
            }
        }
    };

    @Override
    public boolean onScroll(Gui gui, double i) {
        //if(dropButton.onScroll(gui, i)){return true;}
        i = Math.signum(i);
        if (listManager.isVisible()) {
            if (listManager.onScroll(gui, i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (dropButton.onMouseClick(gui, x, y, buttonNum)) {
            return true;
        }
        if (listManager.isVisible()) {
            if (listManager.onMouseClick(gui, x, y, buttonNum)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        if (dropButton.onMouseRelease(gui, x, y, state)) {
            return true;
        }
        if (listManager.isVisible()) {
            if (listManager.onMouseRelease(gui, x, y, state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        if (dropButton.onMouseClickMove(gui, x, y, buttonNum, q, r)) {
            return true;
        }
        if (listManager.isVisible()) {
            if (listManager.onMouseClickMove(gui, x, y, buttonNum, q, r)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (dropButton.onKeyPressed(gui, keyCode, scanCode, modifiers)) {
            return true;
        }
        if (listManager.isVisible()) {
            if (listManager.onKeyPressed(gui, keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCharTyped(Gui gui, char typedChar, int mods) {
        if (dropButton.onCharTyped(gui, typedChar, mods)) {
            return true;
        }
        if (listManager.isVisible()) {
            if (listManager.onCharTyped(gui, typedChar, mods)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (dropButton.onKeyRelease(gui, keyCode, scanCode, modifiers)) {
            return true;
        }
        if (listManager.isVisible()) {
            if (listManager.onKeyRelease(gui, keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
        if (dropButton.onKeyRepeat(gui, keyCode, scanCode, modifiers, n)) {
            return true;
        }
        if (listManager.isVisible()) {
            if (listManager.onKeyRepeat(gui, keyCode, scanCode, modifiers, n)) {
                return true;
            }
        }
        return false;
    }

    public static abstract class OnSelectHandler {

        abstract public void onSelect(int index, String text);

    }

    @Override
    public void setWidth(int i) {
        this.width = i;
        txtBG.setWidth(i - dropButton.getWid());
        listManager.setWidth(i);
    }

    public void open() {
        listManager.setVisible(true);
        listManager.scrollBar.focusToItem(index);
    }

    public void close() {
        listManager.setVisible(false);
    }

    public boolean isOpen() {
        return listManager.isVisible();
    }

    @Override
    public void setHeight(int i) {
        this.height = i;
    }

    /**
     * be sure to set some code to call open and close
     */
    public void setOnOpen(OnClickHandler och) {
        dropButton.setOnClick(och);
    }

    public OnClickHandler getonOpen() {
        return dropButton.getOnClickHandler();
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        //System.out.printf("Max height set to %s (boxes = %s)\n", (maxHeight-height), (maxHeight-height)/12);
        listManager.setHeight(maxHeight - height);
        //listManager.setHeight(300);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public void setScrollSpeed(int i) {
        listManager.scrollBar.setScrollPos(i);
    }

    //TODO select by int

    public void select(String string) {
        for (int i = 0; i < listManager.getItems().size(); i++) {
            if (getText(i).equals(string)) {
                dispText = string;
                return;
            }
        }
        System.out.println("Drop down " + string + " not found");
        for (int i = 0; i < listManager.getItems().size(); i++) {
            System.out.println(getText(i));
        }
    }

}
