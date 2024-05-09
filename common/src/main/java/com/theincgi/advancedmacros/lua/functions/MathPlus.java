package com.theincgi.advancedmacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

public class MathPlus {

    public static LuaValue const_e = LuaValue.valueOf(Math.E);

    public static class Log extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            if (arg2.isnil()) {
                return LuaValue.valueOf(Math.log10(arg1.checkdouble()));
            } else {
                return LuaValue.valueOf(Math.log10(arg2.checkdouble()) / Math.log10(arg1.checkdouble()));
            }
        }

    }

}
