package com.orleansmc.realms.listeners;

import com.orleansmc.realms.OrleansRealms;

public class ListenerLoader {
    public static void load(OrleansRealms plugin) {
        new MainListener(plugin);
        new RealmListener(plugin);
    }
}
