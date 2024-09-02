package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.orleansmc.bukkit.players.models.PlayerModel;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.enums.RealmTime;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.utils.Util;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RealmSettingsMenu extends SuperMenu {
    private final Player player;
    private final ChestGui gui;
    private final boolean reload;
    private StaticPane realmSettingsNavigator;
    private final List<String> pendingRealmDeleteRequests = new ArrayList<>();

    public RealmSettingsMenu(Player player, OrleansRealms plugin, ChestGui gui) {
        super(plugin);
        this.player = player;
        this.reload = gui == null;
        this.gui = Objects.requireNonNullElseGet(gui, () -> new ChestGui(5, ""));
    }

    public void open() {
        RealmModel realm = plugin.realmsManager.getRealm(player.getName());
        if (realm == null) {
            player.sendMessage(Util.getExclamation() + "§cBir hata oluştu. Diyarınız bulunamamış olabilir.");
            return;
        }
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:realm_settings");
        gui.setRows(5);
        gui.setTitle("§f" + fontImageWrapper.applyPixelsOffset(-25));
        gui.getPanes().clear();
        gui.setOnGlobalClick(event -> {
            event.setCancelled(true);
        });
        gui.setOnGlobalDrag(event -> {
            event.setCancelled(true);
        });

        gui.setOnClose(event -> plugin.realmsManager.saveRealm(realm));
        if (reload) {
            gui.show(player);
        }

        StaticPane backNavigator = new StaticPane(1, 0, 5, 3);
        ItemStack backButton = getEmptyItem(titleLegacy("Geri Dön"), null);
        backNavigator.fillWith(backButton);
        backNavigator.setOnClick(event -> {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            new RealmMenu(player, plugin, gui).open();
        });
        gui.addPane(backNavigator);

        StaticPane realmDeletePane = new StaticPane(8, 0, 1, 1);
        realmDeletePane.addItem(
                new GuiItem(
                        getEmptyItem(titleLegacy("Diyarı Sil"), List.of(green("Diyarınızı silmek için iki kez tıklayın."))),
                        event -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            if (plugin.playerDataManager.getRealmDeleteRight(player)) {
                                if (pendingRealmDeleteRequests.contains(player.getName())) {
                                    plugin.realmsManager.deleteRealm(player.getName());
                                    PlayerModel playerModel = plugin.playerDataManager.getPlayerData(player.getName());
                                    playerModel.totalDeletedRealms++;
                                    plugin.playerDataManager.savePlayerData(playerModel);
                                    pendingRealmDeleteRequests.remove(player.getName());
                                    player.closeInventory();
                                    return;
                                }
                                pendingRealmDeleteRequests.add(player.getName());
                                player.sendMessage("§aDiyarınızı silmek için tekrar tıklayın.");
                            } else {
                                player.sendMessage("§cDiyarınızı silme hakkınız kalmadı.");
                            }
                        }
                ), 0, 0
        );
        gui.addPane(realmDeletePane);

        realmSettingsNavigator = new StaticPane(2, 4, 5, 1);
        realmSettingsNavigator.addItem(
                new GuiItem(
                        getEmptyItem(titleLegacy("Üyeleri Yönet"),
                                List.of(green("Üyelerinizi yönetmek için tıklayın."))
                        ),
                        event -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            new RealmMembersMenu(player, plugin, gui).open();
                        }
                ), 0, 0
        );
        realmSettingsNavigator.addItem(
                new GuiItem(
                        getEmptyItem(titleLegacy("Diyar Büyüklüğü"),
                                List.of(green("Diyar büyüklüğünü değiştirmek için tıklayın.")
                                )),
                        event -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            new RealmBorderMenu(player, plugin, gui).open();
                        }
                ), 2, 0
        );
        updateVisitorButton(realm);
        updatemonsterSpawnButton(realm);
        updateRealmTimeButton(realm);
        gui.addPane(realmSettingsNavigator);
        gui.update();
    }

    private void updateVisitorButton(RealmModel realm) {
        List<Component> lore = new ArrayList<>();
        lore.add(green("Diyarınıza girebilsinler mi?"));
        lore.add(Component.empty());
        lore.add(description("Şu an: " + (realm.allow_visitors ? "<green>Evet" : "<red>Hayır")));
        realmSettingsNavigator.addItem(
                new GuiItem(
                        getEmptyItem(titleLegacy("Diyar Ziyaretçileri"), lore),
                        event -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            realm.allow_visitors = !realm.allow_visitors;
                            updateVisitorButton(realm);
                            if (realm.allow_visitors) {
                                plugin.realmsManager.kickVisitors(realm);
                            }
                        }
                ), 1, 0
        );
        gui.addPane(realmSettingsNavigator);
        gui.update();
    }

    private void updatemonsterSpawnButton(RealmModel realm) {
        List<Component> lore = new ArrayList<>();
        lore.add(green("Canavarlar doğabilir mi?"));
        lore.add(Component.empty());
        lore.add(description("Şu an: " + (realm.monster_spawn ? "<green>Evet" : "<red>Hayır")));
        realmSettingsNavigator.addItem(
                new GuiItem(
                        getEmptyItem(titleLegacy("Canavar Doğumu"), lore),
                        event -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            if (!player.hasPermission("orleansmc.realms.set_monster_spawn")) {
                                player.sendMessage(Util.getExclamation() + "§cBu özellik vip oyunculara özeldir.");
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                                return;
                            }
                            realm.monster_spawn = !realm.monster_spawn;
                            updatemonsterSpawnButton(realm);

                            if (!realm.monster_spawn) {
                                plugin.realmsManager.removeMobs(realm);
                            }
                        }
                ), 3, 0
        );
        gui.addPane(realmSettingsNavigator);
        gui.update();
    }

    private void updateRealmTimeButton(RealmModel realm) {
        List<Component> lore = new ArrayList<>();
        lore.add(green("Değiştirmek için tıklayın."));
        lore.add(Component.empty());
        RealmTime realmTime = realm.time;
        List<RealmTime> times = Arrays.stream(RealmTime.values()).toList();
        for (RealmTime time : times) {
            lore.add(description((realmTime == time ? "<green>" : "") + "・" +
                    (time == RealmTime.DAY ? "Gündüz" : time == RealmTime.NIGHT ? "Gece" :
                            time == RealmTime.SUNSET ? "Günbatımı" : time == RealmTime.SUNRISE ? "Şafak" : "Döngü")));

        }

        realmSettingsNavigator.addItem(
                new GuiItem(
                        getEmptyItem(titleLegacy("Diyar Zamanı"), lore),
                        event -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            if (!player.hasPermission("orleansmc.realms.set_time")) {
                                player.sendMessage(Util.getExclamation() + "§cBu özellik vip oyunculara özeldir.");
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                                return;
                            }
                            int index = times.indexOf(realm.time);
                            realm.time = times.get((index + 1) % times.size());
                            if (plugin.serversManager.getCurrentServerType() == ServerType.REALMS) {
                                RealmModel realmModel = plugin.realmsManager.getRealmByLocation(player.getLocation());
                                if (realmModel != null && realmModel.owner.equals(player.getName())) {
                                    player.setPlayerTime(Util.getTimeByRealmTimeType(realm.time), false);
                                }
                            }
                            updateRealmTimeButton(realm);
                        }
                ), 4, 0
        );
        gui.addPane(realmSettingsNavigator);
        gui.update();
    }
}

