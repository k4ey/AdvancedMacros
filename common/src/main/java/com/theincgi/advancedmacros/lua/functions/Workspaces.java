package com.theincgi.advancedmacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.misc.Utils;

public class Workspaces {
	public static class GetWorkspace extends ZeroArgFunction {
    	@Override
    	public LuaValue call() {
    		return valueOf(Utils.currentWorkspace());
    	}
    }
    
    public static class SetWorkspace extends OneArgFunction {
    	@Override
    	public LuaValue call(LuaValue arg) {
    		String name = arg.checkjstring().trim();
    		if(name.isBlank())
    			throw new LuaError("Invalid workspace name \""+arg.checkjstring()+"\"");
    		if(Thread.currentThread() == AdvancedMacros.getMinecraftThread())
    			Utils.setMCThreadWorkspace(name);
    		else
    			LuaDebug.LuaThread.getCurrent().workspace = name;
    		return NONE;
    	}
    }
}
