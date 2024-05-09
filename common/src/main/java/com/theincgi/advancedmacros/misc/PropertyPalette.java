package com.theincgi.advancedmacros.misc;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.gui.Color;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

public class PropertyPalette {

    public final String[] key;
    public LuaTable settings;

    public PropertyPalette() {
        settings = new LuaTable();
        key = null;
    }

    //	public PropertyPalette(String[] key, LuaTable t) {
    //		this.key = key;
    //		this.settings = t;
    //	}
    public PropertyPalette(String[] key) {
        //settings = new LuaTable(); //TODO 1.19 Update: doesn't load colors if this is not added. Global settings don't seem to work
        this.key = key;
    }

    public PropertyPalette addColor(String key, Color c) {
        getTableFromKey().set(key, c.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255));
        return this;
    }

    public PropertyPalette addColor(Color c, String... keyPath) {
        setProp(c.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255), keyPath);
        return this;
    }

    public PropertyPalette setProp(LuaValue c, String... keyPath) {
        LuaTable t = getTableFromKey();
        for (int i = 0; i < keyPath.length - 1; i++) {
            if (t.get(keyPath[i]).isnil()) {
                t.set(keyPath[i], new LuaTable());
            }
            t = t.get(keyPath[i]).checktable();
        }
        t.set(keyPath[keyPath.length - 1], c);
        return this;
    }

    public Color getColor(String key) {
        return Utils.parseColor(getTableFromKey().get(key), AdvancedMacros.COLOR_SPACE_IS_255);
    }

    public Color getColor(String... keyPath) {
        return Utils.parseColor(getValue(keyPath), AdvancedMacros.COLOR_SPACE_IS_255);
    }

    public LuaValue getValue(String key) {
        return getTableFromKey().get(key);
    }

    public LuaValue getValue(String... keyPath) {
        LuaTable t = getTableFromKey();
        for (int i = 0; i < keyPath.length - 1; i++) {
            if (t.get(keyPath[i]).isnil()) {
                t.set(keyPath[i], new LuaTable());
            }
            t = t.get(keyPath[i]).checktable();
        }
        return t.get(keyPath[keyPath.length - 1]);
    }

    public LuaTable getTableFromKey() {
        LuaTable t = settings == null ? Settings.settings : settings;
        if (key != null) {
            for (String k : this.key) {
                if (t.get(k).isnil()) {
                    t.set(k, new LuaTable());
                }
                t = t.get(k).checktable();
            }
        }
        return t;
    }

    public PropertyPalette addColorIfNil(String key, Color color) {
        LuaTable t = getTableFromKey();
        if (t.get(key).isnil()) {
            t.set(key, color.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255));
        }
        return this;
    }

    public PropertyPalette addColorIfNil(Color color, String... keyPath) {
        return setPropIfNil(color.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255), keyPath);
    }

    public PropertyPalette setPropIfNil(LuaValue prop, String... keyPath) {
        LuaTable t = getTableFromKey();
        for (int i = 0; i < keyPath.length - 1; i++) {
            if (t.get(keyPath[i]).isnil()) {
                t.set(keyPath[i], new LuaTable());
            }
            t = t.get(keyPath[i]).checktable();
        }
        if (t.get(keyPath[keyPath.length - 1]).isnil()) {
            t.set(keyPath[keyPath.length - 1], prop);
        }
        return this;
    }

    public PropertyPalette propertyPaletteOf(String... keyPath) {
        if (key == null) {
            return new PropertyPalette(keyPath);
        }
        String[] path2 = new String[key.length + keyPath.length];
        System.arraycopy(key, 0, path2, 0, key.length);
        System.arraycopy(keyPath, 0, path2, key.length, keyPath.length);
        return new PropertyPalette(path2);
    }

}
