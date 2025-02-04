package com.theincgi.advancedmacros.lua.functions;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedmacros.event.TaskDispatcher;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class GetPlayerList extends ZeroArgFunction {

    @Override
    public LuaValue call() {

        ListenableFuture<LuaTable> f = TaskDispatcher.addTask(new Callable<LuaTable>() {
            @Override
            public LuaTable call() throws Exception {
                LuaTable table = new LuaTable();
                int i = 1;
                //				NetworkPlayerInfo[] list = (NetworkPlayerInfo[]) MinecraftClient.getInstance().player.connection.getPlayerInfoMap().toArray();
                //
                //				for(int j = 0; j<list.length; j++) {
                //					NetworkPlayerInfo player = list[j];
                //					table.set(i++, player.getGameProfile().getName());
                //				}

                MinecraftClient mc = MinecraftClient.getInstance();
                Iterator<PlayerListEntry> iter = mc.getNetworkHandler().getPlayerList().iterator();
                while (iter.hasNext()) {
                    PlayerListEntry playerInfo = iter.next();
                    String name = playerInfo.getDisplayName().getString();
                    if (name != null) {
                        name = Utils.fromMinecraftColorCodes(name);
                        table.set(i++, name);
                    }
                }
                return table;
            }
        });
        while (!f.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                throw new LuaError("Thread interrupted");
            }
        }
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new LuaError(e);
        }
    }

}
