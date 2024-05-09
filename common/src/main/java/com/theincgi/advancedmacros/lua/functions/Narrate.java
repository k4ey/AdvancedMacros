package com.theincgi.advancedmacros.lua.functions;

import com.mojang.text2speech.Narrator;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

public class Narrate extends TwoArgFunction {

    Narrator narrator = Narrator.getNarrator();

    @Override
    public LuaValue call(LuaValue arg1, LuaValue arg2) {
        narrator.say(arg1.checkjstring(), arg2.optboolean(false));
        return NONE;
    }

}
