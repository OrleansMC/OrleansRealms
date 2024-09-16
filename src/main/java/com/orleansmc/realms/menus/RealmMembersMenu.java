package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.enums.RealmMember;
import com.orleansmc.realms.models.data.RealmMemberModel;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.utils.Util;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.unnm3d.rediseconomy.currency.Currency;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RealmMembersMenu extends SuperMenu {
    private final Player player;
    private final ChestGui gui;
    private final boolean reload;
    private StaticPane memberPane;
    private RealmModel realm;

    public RealmMembersMenu(Player player, OrleansRealms plugin, ChestGui gui) {
        super(plugin);
        this.player = player;
        this.reload = gui == null;
        this.gui = Objects.requireNonNullElseGet(gui, () -> new ChestGui(5, ""));
    }

    public void open() {
        realm = plugin.realmsManager.getRealm(player.getName());
        if (realm == null) {
            player.sendMessage(Util.getExclamation() + "§cBir hata oluştu. Diyarınız bulunamamış olabilir.");
            return;
        }
        gui.setRows(5);
        gui.setTitle(getTitle());
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

        StaticPane backNavigator = new StaticPane(2, 0, 5, 3);
        ItemStack backButton = getEmptyItem(titleLegacy("Geri Dön"), null);
        backNavigator.fillWith(backButton);
        backNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            new RealmSettingsMenu(player, plugin, gui).open();
        });
        gui.addPane(backNavigator);
        memberPane = new StaticPane(1, 4, 7, 1);
        updateMemberButtons();
    }

    private void updateMemberButtons() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            memberPane.clear();
            GuiItem addMemberButton = new GuiItem(
                    getEmptyItem(titleLegacy("Üye Ekle"),
                            List.of(green("Üye eklemek için tıklayın."))
                    ),
                    event -> {
                        if (realm.members.size() >= Settings.getAllowedMemberCount(realm)) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                            player.sendMessage(Util.getExclamation() + "§cDiyarınızın üye limitine ulaştınız.");
                            return;
                        }
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        player.closeInventory();
                        player.showTitle(
                                Title.title(
                                        Component.text("§a§lÜye Ekle!"),
                                        Component.text("§7/diyar üye ekle <oyuncu> <rütbesi>"),
                                        Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(5), Duration.ofMillis(100))
                                )
                        );
                    }
            );

            List<Component> addLimitLore = new ArrayList<>();
            Currency creditCurrency = plugin.getCreditCurrency();
            addLimitLore.add(green("Slotu satın alarak limiti artır."));
            addLimitLore.add(Component.empty());
            addLimitLore.add(description("Ücret: <color:#EEC427>" + Settings.REALM_MEMBER_COUNT_INCREASE_PRICE + "<yellow/> \uF801\uF801<white>" + creditCurrency.getCurrencySingular()));
            GuiItem addMemberLimitButton = new GuiItem(
                    getItemsAdderItem("ui:locked", titleLegacy("Üye Limitini Artır"), addLimitLore),
                    event -> {
                        if (creditCurrency.getBalance(player.getName()) < Settings.REALM_MEMBER_COUNT_INCREASE_PRICE) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                            player.sendMessage(Util.getExclamation() + "§cYeterli krediniz bulunmamaktadır.");
                            return;
                        }
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        creditCurrency.withdrawPlayer(player.getName(), Settings.REALM_MEMBER_COUNT_INCREASE_PRICE);
                        if (realm.allowed_member_count == 0) {
                            realm.allowed_member_count = Settings.DEFAULT_REALM_MEMBER_COUNT;
                        }
                        realm.allowed_member_count++;
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                        player.sendMessage("§aÜye limitiniz başarıyla arttırıldı.");
                        gui.setTitle(getTitle());
                        updateMemberButtons();
                    }
            );

            for (int i = 0; i < 7; i++) {
                memberPane.addItem(addMemberLimitButton, i, 0);
            }

            for (int i = realm.members.size(); i < Settings.getAllowedMemberCount(realm); i++) {
                memberPane.addItem(addMemberButton, i, 0);
            }

            for (RealmMemberModel member : realm.members) {
                int memberIndex = realm.members.indexOf(member);
                ItemStack memberHead = getMemberHead(member);
                GuiItem memberItem = new GuiItem(memberHead, event -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    if (event.isRightClick()) {
                        if (realm.owner.equalsIgnoreCase(player.getName())) {
                            if (member.name.equalsIgnoreCase(realm.owner)) {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                                player.sendMessage(Util.getExclamation() + "§cBu oyuncuyu atamazsınız.");
                                return;
                            }
                            if (member.rank == RealmMember.MEMBER) {
                                realm.members.remove(member);
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                                player.sendMessage("§aOyuncu başarıyla atıldı.");
                                updateMemberButtons();
                            }
                        }
                    } else if (event.isLeftClick()) {
                        if (realm.owner.equalsIgnoreCase(player.getName())) {
                            List<RealmMember> ranks = Arrays.stream(RealmMember.values()).toList();
                            int index = ranks.indexOf(member.rank);
                            int nextIndex = (index + 1) % ranks.size();
                            member.rank = ranks.get(nextIndex);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                            player.sendMessage("§aOyuncunun rütbesi başarıyla değiştirildi.");
                            updateMemberButtons();
                        }
                    }
                });
                memberPane.addItem(memberItem, memberIndex, 0);
            }
            gui.addPane(memberPane);
            Bukkit.getScheduler().runTask(plugin, gui::update);
        });
    }

    private ItemStack getMemberHead(RealmMemberModel memberModel) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        String playerType = memberModel.rank.name().toLowerCase();
        String rankTag = PlaceholderAPI.setPlaceholders(player, "%img_realm_" + playerType + "_nameplate" + "%");

        if (skullMeta != null) {
            skullMeta.setOwner(memberModel.name);
            String hexColor = Util.getHexColorFromRealmMemberRank(memberModel.rank);
            skullMeta.displayName(title("<white>" + Util.removeHexCodes(rankTag) + "</white> " + "<color:" + hexColor + ">" + memberModel.name + "</color>"));
            playerHead.setItemMeta(skullMeta);
        }

        List<Component> lore = new ArrayList<>();
        lore.add(title("Rütbesini değiştirmek için sol tıklayın."));
        lore.add(title("Atmak için sağ tıklayın."));
        lore.add(Component.empty());
        lore.add(description("Şu Anki Rütbe: <white>" + rankTag));

        playerHead.lore(lore);
        return playerHead;
    }

    private String getTitle() {
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:realm_members");
        List<String> chars = new ArrayList<>();
        int gemAmount = (int) plugin.getCreditCurrency().getBalance(player.getName());
        String prettyGemAmount = Util.prettyNumber(gemAmount);
        int prettyGemDotCount = prettyGemAmount.length() - prettyGemAmount.replace(".", "").length();
        int firstCharOffset = -64 - ((prettyGemAmount.length() - prettyGemDotCount + 1) * 6) - prettyGemDotCount * 2;

        for (char c : prettyGemAmount.toCharArray()) {
            FontImageWrapper charWrapper = new FontImageWrapper(
                    c == '.' ? "ui:realm_members_char_dot" : "ui:realm_members_digit_" + c
            );
            chars.add(charWrapper.applyPixelsOffset(firstCharOffset));
        }

        FontImageWrapper creditIcon = new FontImageWrapper("ui:realm_members_credit");
        chars.add("§f" + creditIcon.applyPixelsOffset(firstCharOffset + 1));

        return "§f" + fontImageWrapper.applyPixelsOffset(-25) + String.join("", chars);
    }
}

