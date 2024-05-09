package com.theincgi.advancedmacros.lua.functions.os;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class GetOSMilliseconds extends ZeroArgFunction {

    @Override
    public LuaValue call() {
        return LuaValue.valueOf(System.currentTimeMillis());
    }

}
