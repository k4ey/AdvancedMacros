package com.theincgi.advancedmacros.misc;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

public class JarLibSearcher extends OneArgFunction {

    public static LuaTable loaders = new LuaTable();

    @Override
    public LuaValue call(LuaValue name) {
        return loaders.get(name);
    }

}
