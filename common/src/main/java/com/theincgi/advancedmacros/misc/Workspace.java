package com.theincgi.advancedmacros.misc;

import org.luaj.vm2_v3_0_1.LuaTable;

public record Workspace(String name, String path) {

	public LuaTable asTable() {
		LuaTable tbl = new LuaTable();
		tbl.set("name", name);
		tbl.set("path", path);
		return tbl;
	}
	
}
