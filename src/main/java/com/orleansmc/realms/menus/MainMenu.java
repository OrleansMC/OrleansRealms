package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.orleansmc.realms.OrleansRealms;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MainMenu extends SuperMenu {
    private final Player player;
    private final ChestGui gui;
    private final boolean reload;

    public MainMenu(Player player, OrleansRealms plugin, ChestGui gui) {
        super(plugin);
        this.player = player;
        this.reload = gui == null;
        this.gui = Objects.requireNonNullElseGet(gui, () -> new ChestGui(6, ""));
    }

    public void open() {
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:mainest_menu");
        gui.getPanes().clear();
        gui.setRows(6);
        gui.setTitle("§f" + fontImageWrapper.applyPixelsOffset(-25));
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.setOnGlobalDrag(event -> event.setCancelled(true));
        if (reload) {
            gui.show(player);
        }
        StaticPane realmNavigator = new StaticPane(0, 0, 3, 3);
        ItemStack realmButton = getEmptyItem(titleLegacy("Diyarları Görüntüle"), null);
        realmNavigator.fillWith(realmButton);
        realmNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            new RealmMenu(player, plugin, gui).open();
        });
        gui.addPane(realmNavigator);

        StaticPane creditMarketNavigator = new StaticPane(0, 3, 3, 3);
        ItemStack creditMarketButton = getEmptyItem(titleLegacy("Kredi Market"), null);
        creditMarketNavigator.fillWith(creditMarketButton);
        creditMarketNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.performCommand("kredi-market");
        });
        gui.addPane(creditMarketNavigator);

        StaticPane questsNavigator = new StaticPane(3, 3, 3, 3);
        ItemStack questsButton = getEmptyItem(titleLegacy("Görevler"), null);
        questsNavigator.fillWith(questsButton);
        questsNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            new QuestMenu(player, plugin, gui).open();
        });
        gui.addPane(questsNavigator);

        StaticPane spawnNavigator = new StaticPane(3, 0, 3, 3);
        ItemStack spawnButton = getEmptyItem(titleLegacy("Spawna Dön"), null);
        spawnNavigator.fillWith(spawnButton);
        spawnNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.performCommand("helper:spawn");
        });
        gui.addPane(spawnNavigator);

        StaticPane shopsNavigator = new StaticPane(6, 0, 3, 3);
        ItemStack shopsButton = getEmptyItem(titleLegacy("Marketler"), null);
        shopsNavigator.fillWith(shopsButton);
        shopsNavigator.setOnClick(event -> {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Bukkit.getServer().dispatchCommand(player, "shops");
        });
        gui.addPane(shopsNavigator);

        StaticPane outlandNavigator = new StaticPane(6, 3, 3, 3);
        ItemStack outlandButton = getEmptyItem(titleLegacy("Vahşi Dünya"), null);
        outlandNavigator.fillWith(outlandButton);
        outlandNavigator.setOnClick(event -> {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Bukkit.getServer().dispatchCommand(player, "outland");
        });
        gui.addPane(outlandNavigator);
        gui.update();
    }
}
