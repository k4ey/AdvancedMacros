package com.theincgi.advancedmacros.fabric;

import com.theincgi.advancedmacros.AdvancedMacros;
import net.fabricmc.api.ClientModInitializer;

public class AdvancedMacrosFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AdvancedMacros.init();
    }

}
