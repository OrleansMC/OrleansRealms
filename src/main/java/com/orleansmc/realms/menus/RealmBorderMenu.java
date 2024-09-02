package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.quests.objectives.IncreaseRealmSizeObjective;
import com.orleansmc.realms.utils.Util;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.unnm3d.rediseconomy.currency.Currency;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RealmBorderMenu extends SuperMenu {
    private final Player player;
    private final ChestGui gui;
    private final boolean reload;
    private RealmModel realm;

    public RealmBorderMenu(Player player, OrleansRealms plugin, ChestGui gui) {
        super(plugin);
        this.player = player;
        this.reload = gui == null;
        this.gui = Objects.requireNonNullElseGet(gui, () -> new ChestGui(3, ""));
    }

    public void open() {
        realm = plugin.realmsManager.getRealm(player.getName());
        if (realm == null) {
            player.sendMessage(Util.getExclamation() + "§cBir hata oluştu. Diyarınız bulunamamış olabilir.");
            return;
        }
        gui.setRows(3);
        gui.setTitle(getTitle());
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
        int unlockedRealmRadius = Settings.getUnlockedRealmRadius(realm);

        StaticPane backNavigator = new StaticPane(0, 2, 1, 1);
        ItemStack backButton = getItemsAdderItem("ui:icon_back_purple", titleLegacy("Geri Dön"), null);
        backNavigator.fillWith(backButton);
        backNavigator.setOnClick(event -> {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            new RealmSettingsMenu(player, plugin, gui).open();
        });
        gui.addPane(backNavigator);

        Currency creditCurrency = plugin.getCreditCurrency();
        Currency gemCurrency = plugin.getGemCurrency();

        StaticPane radius150Pane = new StaticPane(0, 1, 3, 1);
        List<Component> radius150Lore = new ArrayList<>();
        radius150Lore.add(blue("Sınırı 300x300 olarak ayarlar"));
        if (unlockedRealmRadius < 150) {
            radius150Lore.add(Component.empty());
            radius150Lore.add(description("Ücret: <color:#AE89EB>" + Settings.REALM_RADIUS_INCREASE_150_PRICE + "</color><white>" + gemCurrency.getCurrencySingular()));
        }
        ItemStack radius150Button = getEmptyItem(titleLegacy(
                        "300x300 [") + (unlockedRealmRadius < 150 ? redLegacy("Kilitli") : greenLegacy("Açık")) + titleLegacy("]")
                , radius150Lore);
        radius150Pane.fillWith(radius150Button);
        radius150Pane.setOnClick(event -> {
            event.setCancelled(true);
            if (unlockedRealmRadius < 150) {
                int gemAmount = (int) gemCurrency.getBalance(player.getName());
                if (gemAmount < Settings.REALM_RADIUS_INCREASE_150_PRICE) {
                    player.sendMessage(Util.getExclamation() + "§cYeterli miktarda mücevheriniz yok.");
                    return;
                }
                realm.unlocked_radius = 150;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                gemCurrency.withdrawPlayer(player.getName(), Settings.REALM_RADIUS_INCREASE_150_PRICE);
            }
            plugin.realmsManager.realms.put(player.getName(), realm);
            plugin.realmsManager.changeRealmRadius(player.getName(), 150);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            IncreaseRealmSizeObjective.instance.onComplete(realm);
            player.closeInventory();
        });
        gui.addPane(radius150Pane);

        StaticPane radius250Pane = new StaticPane(6, 1, 3, 1);
        List<Component> radius250Lore = new ArrayList<>();
        radius250Lore.add(blue("Sınırı 500x500 olarak ayarlar"));
        if (unlockedRealmRadius < 250) {
            radius250Lore.add(Component.empty());
            radius250Lore.add(description("Ücret: <color:#EEC427>" + Settings.REALM_RADIUS_INCREASE_250_PRICE + "</color><white> \uF801\uF801" + creditCurrency.getCurrencySingular()));
        }
        ItemStack radius250Button = getEmptyItem(titleLegacy(
                        "500x500 [") + (unlockedRealmRadius < 250 ? redLegacy("Kilitli") : greenLegacy("Açık")) + titleLegacy("]")
                , radius250Lore);
        radius250Pane.fillWith(radius250Button);
        radius250Pane.setOnClick(event -> {
            event.setCancelled(true);
            if (unlockedRealmRadius < 250) {
                int creditAmount = (int) creditCurrency.getBalance(player.getName());
                if (creditAmount < Settings.REALM_RADIUS_INCREASE_250_PRICE) {
                    player.sendMessage(Util.getExclamation() + "§cYeterli miktarda krediniz yok.");
                    return;
                }
                realm.unlocked_radius = 250;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                creditCurrency.withdrawPlayer(player.getName(), Settings.REALM_RADIUS_INCREASE_250_PRICE);
            }
            plugin.realmsManager.realms.put(player.getName(), realm);
            plugin.realmsManager.changeRealmRadius(player.getName(), 250);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
        });
        gui.addPane(radius250Pane);

        StaticPane radius500Pane = new StaticPane(3, 2, 3, 1);
        List<Component> radius500Lore = new ArrayList<>();
        radius500Lore.add(blue("Sınırı 1000x1000 olarak ayarlar"));
        if (unlockedRealmRadius < 500) {
            radius500Lore.add(Component.empty());
            radius500Lore.add(description("Ücret: <color:#EEC427>" + Settings.REALM_RADIUS_INCREASE_500_PRICE + "</color><white> \uF801\uF801" + creditCurrency.getCurrencySingular()));
        }
        ItemStack radius500Button = getEmptyItem(titleLegacy(
                        "1000x1000 [") + (unlockedRealmRadius < 500 ? redLegacy("Kilitli") : greenLegacy("Açık")) + titleLegacy("]")
                , radius500Lore);
        radius500Pane.fillWith(radius500Button);
        radius500Pane.setOnClick(event -> {
            event.setCancelled(true);
            if (unlockedRealmRadius < 500) {
                int creditAmount = (int) creditCurrency.getBalance(player.getName());
                if (creditAmount < Settings.REALM_RADIUS_INCREASE_500_PRICE) {
                    player.sendMessage(Util.getExclamation() + "§cYeterli miktarda krediniz yok.");
                    return;
                }
                realm.unlocked_radius = 500;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                creditCurrency.withdrawPlayer(player.getName(), Settings.REALM_RADIUS_INCREASE_500_PRICE);
            }
            plugin.realmsManager.realms.put(player.getName(), realm);
            plugin.realmsManager.changeRealmRadius(player.getName(), 500);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
        });
        gui.addPane(radius500Pane);
        gui.update();
    }

    private String getTitle() {
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:realm_border");

        List<String> creditChars = new ArrayList<>();
        int creditAmount = (int) plugin.getCreditCurrency().getBalance(player.getName());
        String prettyCreditAmount = Util.prettyNumber(creditAmount);
        int prettyCreditDotCount = prettyCreditAmount.length() - prettyCreditAmount.replace(".", "").length();
        int creditAmountWidth = (prettyCreditAmount.length() - prettyCreditDotCount + 1) * 6 + prettyCreditDotCount * 2;
        int firstCreditCharOffset = -47 - creditAmountWidth;

        List<String> gemChars = new ArrayList<>();
        int gemAmount = (int) plugin.getGemCurrency().getBalance(player.getName());
        int firstGemCharOffset = -204 - creditAmountWidth;

        FontImageWrapper gemIcon = new FontImageWrapper("ui:realm_border_gem");
        gemChars.add("§f" + gemIcon.applyPixelsOffset(firstGemCharOffset - 2));

        for (char c : Util.prettyNumber(gemAmount).toCharArray()) {
            FontImageWrapper charWrapper = new FontImageWrapper(
                    c == '.' ? "ui:realm_border_char_dot" : "ui:realm_border_digit_" + c
            );
            gemChars.add(charWrapper.applyPixelsOffset(firstGemCharOffset));
        }

        for (char c : prettyCreditAmount.toCharArray()) {
            FontImageWrapper charWrapper = new FontImageWrapper(
                    c == '.' ? "ui:realm_border_char_dot" : "ui:realm_border_digit_" + c
            );
            creditChars.add(charWrapper.applyPixelsOffset(firstCreditCharOffset));
        }

        FontImageWrapper creditIcon = new FontImageWrapper("ui:realm_border_credit");
        creditChars.add("§f" + creditIcon.applyPixelsOffset(firstCreditCharOffset + 2));

        return "§f" + fontImageWrapper.applyPixelsOffset(-25) + String.join("", creditChars) + String.join("", gemChars);
    }
}

