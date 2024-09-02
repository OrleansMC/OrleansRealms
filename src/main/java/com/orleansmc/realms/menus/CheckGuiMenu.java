package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.orleansmc.realms.OrleansRealms;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import org.bukkit.entity.Player;

public class CheckGuiMenu extends SuperMenu {
    private final Player player;
    private final String namespacedID;

    public CheckGuiMenu(Player player, OrleansRealms plugin, String namespacedID) {
        super(plugin);
        this.player = player;
        this.namespacedID = namespacedID;
    }

    public void open() {
        try {
            player.closeInventory();
            FontImageWrapper fontImageWrapper = new FontImageWrapper(namespacedID);
            ChestGui gui = new ChestGui(6, "§f" + fontImageWrapper.applyPixelsOffset(-25));
            gui.show(player);
        } catch (Exception e) {
            player.sendMessage("§cBir hata oluştu. Menü bulunamamış olabilir.");
        }
    }
}

