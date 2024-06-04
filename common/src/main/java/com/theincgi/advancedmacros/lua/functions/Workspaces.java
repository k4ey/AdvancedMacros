package com.theincgi.advancedmacros.lua.functions;

import java.util.Optional;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.misc.Settings;
import com.theincgi.advancedmacros.misc.Utils;

public class Workspaces {
	public static class GetWorkspaceName extends ZeroArgFunction {
    	@Override
    	public LuaValue call() {
    		return valueOf(Utils.currentWorkspaceName());
    	}
    }
	
	public static class GetWorkspacePath extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			return valueOf(Utils.currentWorkspacePath());
		}
	}
    
    public static class SetWorkspaceByName extends OneArgFunction {
    	@Override
    	public LuaValue call(LuaValue arg) {
    		String name = arg.checkjstring().trim();
    		if(name.isBlank())
    			throw new LuaError("Invalid workspace name \""+arg.checkjstring()+"\"");
    		
    		Optional<String> path = Settings.getWorkspacePath( name );
    		if( path.isEmpty() )
    			throw new LuaError("Workspace '"+name+"' is not defined in getSettings().workspaces");
    		
    		if(Thread.currentThread() == AdvancedMacros.getMinecraftThread()) {
    			
    			Utils.setMCThreadWorkspace(path.get());
    		} else {
    			LuaDebug.LuaThread.getCurrent().workspace = path.get();
    		}
    		return NONE;
    	}
    }
}
