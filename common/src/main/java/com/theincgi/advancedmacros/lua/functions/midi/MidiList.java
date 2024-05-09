package com.theincgi.advancedmacros.lua.functions.midi;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaUserdata;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;

public class MidiList extends ZeroArgFunction {

    @Override
    public LuaValue call() {
        return getInfoList();
    }

    public static LuaTable getInfoList() {
        LuaTable tab = new LuaTable();

        MidiDevice.Info[] infoArr = MidiSystem.getMidiDeviceInfo();
        for (Info info : infoArr) {
            tab.set(tab.length() + 1, toTable(info));
        }
        return tab;
    }

    private static LuaValue toTable(Info info) {
        LuaTable t = new LuaTable();
        t.set("name", info.getName());
        t.set("description", info.getDescription());
        t.set("vendor", info.getVendor());
        t.set("version", info.getVersion());
        t.set("userdata", new LuaUserdata(info) {
            @Override
            public String toString() {
                return "userdata:midiDevice";
            }
        });
        t.set("type", info.getClass().getSimpleName());
        return t;
    }

}
