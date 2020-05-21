package com.theincgi.advancedMacros.hud.hud2D;import org.luaj.vm2_v3_0_1.LuaError;import org.luaj.vm2_v3_0_1.LuaValue;import org.luaj.vm2_v3_0_1.lib.OneArgFunction;import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;import com.mojang.blaze3d.platform.GlStateManager;import com.theincgi.advancedMacros.AdvancedMacros;import com.theincgi.advancedMacros.misc.Utils;import net.minecraft.client.gui.FontRenderer;import net.minecraft.client.renderer.ItemRenderer;import net.minecraft.client.renderer.RenderHelper;import net.minecraft.item.ItemStack;public class Hud2d_itemIcon extends Hud2DItem{	static ItemRenderer itemRender = AdvancedMacros.getMinecraft().getItemRenderer();	static FontRenderer fontRenderer = AdvancedMacros.getMinecraft().fontRenderer;	int wid, hei;	ItemStack itemStack;	public Hud2d_itemIcon() {		super();		itemStack = ItemStack.EMPTY;		controls.set("setItem", new TwoArgFunction() {			@Override			public LuaValue call(LuaValue item, LuaValue quantity) {				if(item.isnil()) {					throw new LuaError("Item cannot be nil");				}				setStack(item.checkjstring());				if(!quantity.isnil()) {					itemStack.setCount(quantity.checkint());				}				return NONE;			}		});		controls.set("setCount", new OneArgFunction() {			@Override			public LuaValue call(LuaValue arg) {				itemStack.setCount(arg.checkint());				return NONE;			}		});		wid = hei = 16;	}	public void setStack(String text) {		//Item i = Item.getByNameOrId(text);		if(text==null) {			itemStack = ItemStack.EMPTY;			return;		}		if(!text.contains(":")) {			text = "minecraft:"+text;		}		int indx = text.lastIndexOf(":");		String end = text.substring(indx+1);		try {			int dmg = Integer.parseInt(end);			itemStack = Utils.itemStackFromName(text.substring(0, indx)); //TESTME			itemStack.setDamage(dmg);		}catch (NumberFormatException e) {			itemStack = Utils.itemStackFromName(text);		}	}	public void setCount(int optint) {		itemStack.setCount(1);	}	@Override	public void render(float partialTicks) {		GlStateManager.pushMatrix();		applyTransformation();		RenderHelper.disableStandardItemLighting();		RenderHelper.enableStandardItemLighting();		itemRender.zLevel = z;		itemRender.renderItemIntoGUI(itemStack, (int)0, (int)0);		RenderHelper.disableStandardItemLighting();		GlStateManager.bindTexture(0);		GlStateManager.popMatrix();	}}