package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.orleansmc.realms.OrleansRealms;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectClimateMenu extends SuperMenu {
    private final Player player;
    private final ChestGui gui;
    private final boolean reload;

    public SelectClimateMenu(Player player, OrleansRealms plugin, ChestGui gui) {
        super(plugin);
        this.player = player;
        this.reload = gui == null;
        this.gui = Objects.requireNonNullElseGet(gui, () -> new ChestGui(6, ""));
    }

    public void open() {
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:climate_selection");
        gui.getPanes().clear();
        gui.setRows(6);
        gui.setTitle("§f" + fontImageWrapper.applyPixelsOffset(-25));

        gui.setOnGlobalClick(event -> {
            event.setCancelled(true);
        });
        gui.setOnGlobalDrag(event -> {
            event.setCancelled(true);
        });

        if (reload) {
            gui.show(player);
        }
        StaticPane desertNavigator = new StaticPane(1, 0, 3, 2);
        List<Component> desertLore = new ArrayList<>();
        desertLore.add(green("=> Bu biyomlar rastgele seçilir:"));
        desertLore.add(Component.empty());
        desertLore.add(description("・Çöl"));
        desertLore.add(description("・Çöl Tepeleri"));
        desertLore.add(description("・Kötü Arazi Kanyonu"));
        ItemStack desertButton = getEmptyItem(titleLegacy("Çöl İklimi"), desertLore);
        desertNavigator.fillWith(desertButton);
        desertNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            Bukkit.getServer().dispatchCommand(player, "realm create desert");
        });
        gui.addPane(desertNavigator);

        StaticPane snowyNavigator = new StaticPane(5, 0, 3, 2);
        List<Component> snowyLore = new ArrayList<>();
        snowyLore.add(green("=> Bu biyomlar rastgele seçilir:"));
        snowyLore.add(Component.empty());
        snowyLore.add(description("・Karlı Ovalar"));
        snowyLore.add(description("・Karlı Dağlar"));
        snowyLore.add(description("・Karlı Ladin Ormanı"));
        snowyLore.add(description("・Buz Dikenleri"));
        ItemStack snowyButton = getEmptyItem(titleLegacy("Kar İklimi"), snowyLore);
        snowyNavigator.fillWith(snowyButton);
        snowyNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            Bukkit.getServer().dispatchCommand(player, "realm create snowy");
        });
        gui.addPane(snowyNavigator);

        StaticPane humidNavigator = new StaticPane(3, 2, 3, 2);
        List<Component> humidLore = new ArrayList<>();
        humidLore.add(green("=> Bu biyomlar rastgele seçilir:"));
        humidLore.add(Component.empty());
        humidLore.add(description("・Mangrov Ormanı"));
        humidLore.add(description("・Huş Ormanı"));
        humidLore.add(description("・Karanlık Orman"));
        humidLore.add(description("・Bambulu Orman"));
        humidLore.add(description("・Bataklık"));
        ItemStack humidButton = getEmptyItem(titleLegacy("Nemli İklim"), humidLore);
        humidNavigator.fillWith(humidButton);
        humidNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            Bukkit.getServer().dispatchCommand(player, "realm create humid");
        });
        gui.addPane(humidNavigator);

        StaticPane savannahNavigator = new StaticPane(1, 4, 3, 2);
        List<Component> savannahLore = new ArrayList<>();
        savannahLore.add(green("=> Bu biyomlar rastgele seçilir:"));
        savannahLore.add(Component.empty());
        savannahLore.add(description("・Savana"));
        savannahLore.add(description("・Savana Tepeleri"));
        savannahLore.add(description("・Savana Ormanı"));
        savannahLore.add(description("・Çamurlu Bataklık"));
        ItemStack savannahButton = getEmptyItem(titleLegacy("Savan İklimi"), savannahLore);
        savannahNavigator.fillWith(savannahButton);
        savannahNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            Bukkit.getServer().dispatchCommand(player, "realm create savannah");
        });
        gui.addPane(savannahNavigator);

        StaticPane continentalNavigator = new StaticPane(5, 4, 3, 2);
        List<Component> continentalLore = new ArrayList<>();
        continentalLore.add(green("=> Bu biyomlar rastgele seçilir:"));
        continentalLore.add(Component.empty());
        continentalLore.add(description("・Huş Ağacı Dağları"));
        continentalLore.add(description("・Kızıl Orman"));
        continentalLore.add(description("・Ladin Ormanı"));
        continentalLore.add(description("・Bataklık Sahili"));
        continentalLore.add(description("・Kiraz Korusu"));
        ItemStack continentalButton = getEmptyItem(titleLegacy("Karasal İklim"), continentalLore);
        continentalNavigator.fillWith(continentalButton);
        continentalNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            Bukkit.getServer().dispatchCommand(player, "realm create continental");
        });
        gui.addPane(continentalNavigator);

        gui.update();
    }
}
