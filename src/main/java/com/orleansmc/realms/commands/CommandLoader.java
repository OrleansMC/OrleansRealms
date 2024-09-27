package com.orleansmc.realms.commands;

import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.commands.admins.*;
import com.orleansmc.realms.commands.players.*;
import com.orleansmc.realms.commands.vips.ConverterCommand;

public class CommandLoader {
    public static void load(OrleansRealms plugin) {
        RealmCommand.setup(plugin);
        RealmAdminCommand.setup(plugin);
        MenuCommand.setup(plugin);
        ConverterCommand.setup(plugin);
        SpawnCommand.setup(plugin);
        MoneyCommand.setup(plugin);
        CheckGuiCommand.setup(plugin);
        KillEntitiesCommand.setup(plugin);
        QuestCommand.setup(plugin);
        InventoryToStringsCommand.setup(plugin);
        OutlandCommand.setup(plugin);
        SendMessageCommand.setup(plugin);
    }
}
