package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.utils.Util;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.MojangSkinDataResult;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RealmMenu extends SuperMenu {
    private final Player player;
    private final boolean reload;
    private final ChestGui gui;
    AtomicInteger page = new AtomicInteger();

    public RealmMenu(Player player, OrleansRealms plugin, ChestGui gui) {
        super(plugin);
        this.player = player;
        this.reload = gui == null;
        this.gui = Objects.requireNonNullElseGet(gui, () -> new ChestGui(6, ""));
    }

    public void open() {
        final AtomicBoolean showOwnRealms = new AtomicBoolean(false);
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:realm_menu");
        gui.setRows(6);
        gui.setTitle("§f" + fontImageWrapper.applyPixelsOffset(-25));
        gui.getPanes().clear();
        gui.setOnGlobalClick(event -> {
            event.setCancelled(true);
        });
        gui.setOnGlobalDrag(event -> {
            event.setCancelled(true);
        });
        if (reload) {
            gui.show(player);
        }
        StaticPane realmTeleportNavigator = new StaticPane(1, 0, 4, 2);
        ItemStack realmTeleportButton = getEmptyItem(titleLegacy(
                plugin.realmsManager.getRealm(player.getName()) == null ? "Diyar Oluştur" : "Diyarına Git"
        ), null);
        realmTeleportNavigator.fillWith(realmTeleportButton);
        realmTeleportNavigator.setOnClick(event -> {
            RealmModel realm = plugin.realmsManager.getRealm(player.getName());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            if (realm == null) {
                new SelectClimateMenu(player, plugin, gui).open();
                return;
            }
            player.performCommand("realm teleport");
        });
        gui.addPane(realmTeleportNavigator);

        StaticPane realmSettingsNavigator = new StaticPane(6, 0, 2, 1);
        ItemStack realmSettingsButton = getEmptyItem(titleLegacy("Diyar Ayarları"), null);
        realmSettingsNavigator.fillWith(realmSettingsButton);
        realmSettingsNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            new RealmSettingsMenu(player, plugin, gui).open();
        });
        gui.addPane(realmSettingsNavigator);

        StaticPane realmsNavigator = new StaticPane(1, 2, 7, 3);
        gui.addPane(realmsNavigator);

        StaticPane pageNavigator = new StaticPane(1, 5, 7, 1);

        ItemStack previousPageButton = getItemsAdderItem("ui:icon_back_purple", titleLegacy("Sayfa " + (page.get() > 0 ? page.get() : 1)), null);
        ItemStack nextPageButton = getItemsAdderItem("ui:icon_next_purple", titleLegacy("Sayfa " + (page.get() + 2)), null);

        pageNavigator.addItem(new GuiItem(previousPageButton, event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            showPage(realmsNavigator, showOwnRealms.get());
        }), 0, 0);

        pageNavigator.addItem(new GuiItem(nextPageButton, event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            showPage(realmsNavigator, showOwnRealms.get());
        }), 6, 0);

        gui.addPane(pageNavigator);

        StaticPane filterNavigator = new StaticPane(4, 5, 1, 1);
        filterNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            showOwnRealms.set(!showOwnRealms.get());
            filterNavigator.removeItem(0, 0);
            ItemStack filterRealmsButton = getItemsAdderItem(showOwnRealms.get() ? "ui:spectator_view" : "ui:spectator_view_disabled", titleLegacy(showOwnRealms.get() ? "Tüm Diyarları Göster" : "Diyarlarımı Göster"), null);
            filterNavigator.fillWith(filterRealmsButton);
            showPage(realmsNavigator, showOwnRealms.get());
        });
        ItemStack filterRealmsButton = getItemsAdderItem(showOwnRealms.get() ? "ui:spectator_view" : "ui:spectator_view_disabled", titleLegacy(showOwnRealms.get() ? "Tüm Diyarları Göster" : "Diyarlarımı Göster"), null);
        filterNavigator.fillWith(filterRealmsButton);
        gui.addPane(filterNavigator);

        this.showPage(realmsNavigator, showOwnRealms.get());
    }

    private void showPage(StaticPane realmsNavigator, boolean showOwnRealms) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            realmsNavigator.clear();
            List<RealmModel> realmModels = new ArrayList<>();
            RealmModel playerRealm = plugin.realmsManager.getRealm(player.getName());
            if (playerRealm != null && page.get() == 0) {
                realmModels.add(playerRealm);
            }

            int realmLimit = 21;
            int finalPage = page.get();
            Util.getAllPlayers(plugin).stream().map(plugin.realmsManager::getRealm).filter(Objects::nonNull).filter(realm -> {
                if (realm.owner.equals(player.getName()) && finalPage == 0) {
                    return false;
                }
                if (showOwnRealms) {
                    return realm.members.stream().anyMatch(member -> member.name.equalsIgnoreCase(player.getName()));
                }
                return realm.allow_visitors && realm.banned_players.stream().noneMatch(bannedPlayer -> bannedPlayer.equalsIgnoreCase(player.getName()));
            }).sorted(Comparator.comparingInt(realm -> realm.level)).forEach(realmModels::add);

            realmModels = realmModels.subList(page.get() * realmLimit, Math.min(realmModels.size(), (page.get() + 1) * realmLimit));

            int totalPages = (int) Math.ceil((double) realmModels.size() / realmLimit);
            if (page.get() < 0) {
                page.set(totalPages - 1);
            } else if (page.get() >= totalPages) {
                page.set(0);
            }
            for (int i = 0; i < realmModels.size(); i++) {
                RealmModel realmModel = realmModels.get(i);
                GuiItem realmHead;
                try {
                    realmHead = new GuiItem(getRealmHead(realmModel), event -> {
                        player.closeInventory();
                        player.performCommand("realm teleport " + realmModel.owner);
                    });
                } catch (DataRequestException e) {
                    throw new RuntimeException(e);
                }
                realmsNavigator.addItem(realmHead, i % 7, i / 7);
            }

            Bukkit.getScheduler().runTask(plugin, gui::update);
        });
    }

    private ItemStack getRealmHead(RealmModel realmModel) throws DataRequestException {
        ItemStack playerHead = //new ItemStack(Material.PLAYER_HEAD);
        createPlayerHead(realmModel.owner);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(realmModel.owner);
            //skullMeta.setOwningPlayer(owner);
            String ownerPrefix = PlaceholderAPI.setPlaceholders(owner, "%realms_prefix%");
            String ownerColorCode = Util.getColorCodeFromPlayerPrefix(realmModel.owner);
            String hexColor = ownerColorCode.replace("§x", "#").replaceAll("§", "");
            skullMeta.displayName(title("<white>" + Util.removeHexCodes(ownerPrefix) + "</white> " + "<color:" + hexColor + ">" + realmModel.owner + "</color>"));
            playerHead.setItemMeta(skullMeta);
        }

        List<Component> lore = new ArrayList<>();
        lore.add(title("Seviye: ").append(description(String.valueOf(realmModel.level))));
        lore.add(title("Üyeler:"));
        for (RealmMemberModel member : realmModel.members) {
            if (member.name.equals(realmModel.owner)) continue;
            String playerType = member.rank.name().toLowerCase();
            String hexColor = Util.getHexColorFromRealmMemberRank(member.rank);
            String rankTag = PlaceholderAPI.setPlaceholders(player, "%img_realm_" + playerType + "_nameplate" + "%");
            lore.add(description("・<white>" + rankTag + " </white>" + "<color:" + hexColor + ">" + member.name + "</color>"));
        }
        if (realmModel.members.size() == 1) {
            lore.add(description("・Henüz üyesi yok."));
        }

        playerHead.lore(lore);
        return playerHead;
    }

    public static String getPlayerHeadTexture(String playerName) throws DataRequestException {
        SkinsRestorer skinRestorer = SkinsRestorerProvider.get();
        MojangSkinDataResult skinData = skinRestorer.getSkinStorage().getPlayerSkin(playerName, true).orElse(null);

        if (skinData != null) {
            return skinData.getSkinProperty().getValue(); // Base64 encoded texture
        } else {
            return null; // Eğer skin verisi bulunamazsa null döner
        }
    }

    public static ItemStack createPlayerHead(String playerName) throws DataRequestException {
        String base64Texture = getPlayerHeadTexture(playerName);
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD); // Kafa itemi oluştur
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (base64Texture != null && !base64Texture.isEmpty()) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), playerName);
            profile.getProperties().put("textures", new Property("textures", base64Texture));

            try {
                Field profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(skullMeta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            skull.setItemMeta(skullMeta);
        }

        return skull;
    }
}
