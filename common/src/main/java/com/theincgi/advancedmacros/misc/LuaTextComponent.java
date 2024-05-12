package com.theincgi.advancedmacros.misc;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;

import java.util.List;

public class LuaTextComponent implements Text {

    private String text;
    private LuaValue action;
    private boolean allowHover;

    private final Text internalText;

    public LuaTextComponent(String text, LuaValue action, boolean allowHover) {
    	this(text, action, allowHover, null);
    }
	public LuaTextComponent(String text, LuaValue action, boolean allowHover, Style style) {
		this.text = text;
		this.action = action;
		this.allowHover = allowHover;
		style = style == null ? Style.EMPTY : style; 
		if (action.isCallable()) {
			style = style.withClickEvent(new LuaTextComponentClickEvent(action, this));
		}
		
		if ((action.isstring() || action.istable()) && allowHover) {
			if (action.istable() && !action.get("click").isnil()) {
				if (!action.get("click").isCallable()) {
					throw new LuaError("'click' value of clickable text component table must be callable.");
				}
				style = style.withClickEvent(new LuaTextComponentClickEvent(action.get("click"), this));
			}
			if (action.istable() && !action.get("hover").isnil()) {
				style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.toTextComponent(action.get("hover").tojstring(), null, false).a));
			} else {
				style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.toTextComponent(action.tojstring(), null, false).a));
			}
		}
        
		internalText = style == null ? Text.literal(text) : Text.literal(text).getWithStyle(style).get(0);
    }
    
    @Override
    public List<Text> getWithStyle(Style style) {
    	return List.of(
			new LuaTextComponent(text, action, allowHover, style)
    	);
    }

    @Override
    public Style getStyle() {
        return internalText.getStyle();
    }

    @Override
    public TextContent getContent() {
        return internalText.getContent();
    }

    @Override
    public List<Text> getSiblings() {
        return internalText.getSiblings();
    }

    @Override
    public OrderedText asOrderedText() {
        return internalText.asOrderedText();
    }

    public String getText() {
        return text;
    }

}
