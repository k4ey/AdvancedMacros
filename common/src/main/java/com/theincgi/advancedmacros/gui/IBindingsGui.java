package com.theincgi.advancedmacros.gui;

import com.theincgi.advancedmacros.gui.elements.Drawable;
import com.theincgi.advancedmacros.gui.elements.GuiBinding.EventMode;
import com.theincgi.advancedmacros.lua.LuaDebug.OnScriptFinish;
import org.luaj.vm2_v3_0_1.Varargs;

import java.util.LinkedList;

public interface IBindingsGui {

    /**
     * Exists, and is enabled
     */
    public boolean doesEventExist(String eventName);

    public void reloadCurrentProfile();

    //public void removeBinding(int index);
    public void removeBinding(IBinding binding);

    /**
     * Ignore isKeydown for non key events, doesn't matter,
     *
     * @param isKeyEvent simply, is this a key event?<br>
     * @param eventName  this should match the name in the trigger, key name or event, doesn't
     *                   matter, just needs to match<br>
     * @param args       launch args for script, includes how the script was called<br>
     * @param isKeyDown, if key is down for key event
     */
    public void fireEvent(boolean isKeyEvent, String eventName, Varargs args, boolean isKeyDown, OnScriptFinish osf);

    public LinkedList<String> getMatchingScripts(boolean isKey, String EventName, boolean isKeyDown);

    public void updateProfileList();

    /**
     * False if none found by that name
     */
    public boolean loadProfile(String profile);

    public String getSelectedProfile();

    public Gui getGui();

    public void onGuiOpened();

    public static interface IBinding {

        public boolean isEnabled();

        public boolean isDisabled();

        public EventMode getEventMode();

        /**
         * key name for key mode
         */
        public String getEventName();

        public String getScriptName();

        public Drawable getDrawableElement();

    }

}
