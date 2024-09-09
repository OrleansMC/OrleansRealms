package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.managers.server.QuestManager;
import com.orleansmc.realms.utils.Util;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import me.pikamug.quests.module.BukkitCustomReward;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Objective;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuestMenu extends SuperMenu {
    private final Player player;
    private final ChestGui gui;
    private final boolean reload;
    private StaticPane questsPane;
    private final Quest currentQuest;
    private final AtomicInteger page = new AtomicInteger(0);
    private final int[] slots = new int[]{
            0, 1, 10, 19, 20, 21,
            12, 3, 4, 5, 14, 23, 24, 25,
            16, 7, 8
    };
    private final StaticPane nextPageNavigator = new StaticPane(8, 5, 1, 1);
    private final StaticPane previousPageNavigator = new StaticPane(0, 5, 1, 1);

    public QuestMenu(Player player, OrleansRealms plugin, ChestGui gui) {
        super(plugin);
        this.reload = gui == null;
        if (gui == null) {
            this.gui = new ChestGui(6, "");
        } else this.gui = gui;

        this.player = player;
        this.currentQuest = plugin.questManager.getPlayerQuest(player);
        int currentQuestIndex = plugin.questManager.getQuests().indexOf(currentQuest);
        if (currentQuestIndex != -1) {
            page.set(currentQuestIndex / slots.length);
        }
    }

    public void open() {
        gui.setRows(6);
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:quests_menu");
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

        StaticPane backNavigator = new StaticPane(2, 0, 5, 3);
        ItemStack backButton = getEmptyItem(titleLegacy("Geri Dön"), null);
        backNavigator.fillWith(backButton);
        backNavigator.setOnClick(event -> {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            new MainMenu(player, plugin, gui).open();
        });

        previousPageNavigator.addItem(new GuiItem(getEmptyItem(titleLegacy(""), null), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            page.decrementAndGet();
            updatePage();
        }), 0, 0);

        nextPageNavigator.addItem(new GuiItem(getEmptyItem(titleLegacy(""), null), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            page.incrementAndGet();
            updatePage();
        }), 0, 0);

        gui.addPane(nextPageNavigator);
        gui.addPane(previousPageNavigator);


        questsPane = new StaticPane(0, 3, 9, 3);
        gui.addPane(questsPane);
        this.updatePage();
    }

    private void updatePage() {
        questsPane.clear();
        int questLimit = slots.length;
        List<Quest> quests = plugin.questManager.getQuests();

        int totalPages = (int) Math.ceil((double) quests.size() / questLimit);
        if (page.get() < 0) {
            page.set(totalPages - 1);
            updatePage();
            return;
        } else if (page.get() >= totalPages) {
            if (totalPages != 0) {
                page.set(0);
                updatePage();
                return;
            }
        }

        nextPageNavigator.getItems().stream().findFirst().ifPresent(item -> {
            item.setItem(getItemsAdderItem("ui:icon_next_purple", titleLegacy("Sayfa " +
                    ((page.get() + 1) == totalPages ? 1 : page.get() + 2)
            ), null));
        });

        previousPageNavigator.getItems().stream().findFirst().ifPresent(item -> {
            item.setItem(getItemsAdderItem("ui:icon_back_purple", titleLegacy("Sayfa " +
                    ((page.get() + 1) == 1 ? totalPages : page.get())
            ), null));
        });

        List<Quest> subQuests = quests.subList(page.get() * questLimit,
                Math.min(quests.size(), (page.get() + 1) * questLimit));

        Quester quester = QuestManager.questsPlugin.getQuester(player.getUniqueId());
        for (int i = 0; i < subQuests.size(); i++) {
            Quest quest = subQuests.get(i);
            GuiItem questItem;
            if (currentQuest != null && currentQuest.getName().equals(quest.getName())) {
                List<Component> lore = new ArrayList<>();
                lore.add(green("=> Görevi tamamlamak için:"));
                for (Objective objective : quester.getCurrentObjectives(quest, false, false)) {
                    lore.add(description("・" + plugin.questManager.getPlayerObjectiveTitle(player, objective.getMessage())));
                }
                addRewardsToLore(quest, lore);
                questItem = new GuiItem(getItemsAdderItem(
                        "ui:parchment",
                        titleLegacy(quest.getName()) + titleLegacy(" [") + greenLegacy("Aktif") + titleLegacy("]"),
                        lore
                ), event -> {
                    event.setCancelled(true);
                });
            } else if (quester.getCompletedQuests().stream().anyMatch(q -> q.getName().equals(quest.getName()))) {
                questItem = new GuiItem(getItemsAdderItem("ui:parchment_ticked",
                        titleLegacy(quest.getName()) + titleLegacy(" [") + greenLegacy("Tamamlandı") + titleLegacy("]"),
                        List.of(description("・Görevi tamamladınız."))), event -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                });
            } else {
                List<Component> lore = new ArrayList<>();
                lore.add(green("=> Görevi almak için:"));
                lore.add(description("・Bir önceki görevi tamamlamış olmalısınız."));
                addRewardsToLore(quest, lore);
                questItem = new GuiItem(getItemsAdderItem("ui:locked",
                        titleLegacy(quest.getName()) + titleLegacy(" [") + redLegacy("Kilitli") + titleLegacy("]"),
                        lore), event -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                });
            }
            questsPane.addItem(questItem, Slot.fromIndex(slots[i]));
        }
        gui.update();
    }

    private void addRewardsToLore(Quest quest, List<Component> lore) {
        lore.add(Component.empty());
        lore.add(green("=> Görevi tamamladığınızda:"));
        lore.add(description("・" + plugin.questManager.getPlayerObjectiveTitle(player, quest.getRewards().getQuestPoints() + " Görev Puanı")));

        String gemIcon = plugin.getGemCurrency().getCurrencySingular();
        if (!quest.getRewards().getDetailsOverride().isEmpty()) {
            for (var entry : quest.getRewards().getDetailsOverride()) {
                lore.add(description("・" + plugin.questManager.getPlayerObjectiveTitle(player, entry)));
            }
        }
        if (quest.getRewards().getMoney() > 0) {
            lore.add(description("・")
                    .append(purple(plugin.questManager.getPlayerObjectiveTitle(player, String.valueOf(quest.getRewards().getMoney()))))
                    .append(white(gemIcon)));
        }
        if (quest.getRewards().getExp() > 0) {
            lore.add(description("・" + plugin.questManager.getPlayerObjectiveTitle(player, quest.getRewards().getExp() + " Tecrübe Puanı")));
        }
        for (var rewards : quest.getRewards().getCustomRewards().values()) {
            for (Object reward : rewards.values()) {
                if (reward instanceof BukkitCustomReward customReward) {
                    lore.add(description("・" + plugin.questManager.getPlayerObjectiveTitle(player, customReward.getDescriptions().values().stream().findFirst().orElse(""))));
                }
            }
        }
        for (var item : quest.getRewards().getItems()) {
            ItemStack itemStack = (ItemStack) item;
            lore.add(description("・" + itemStack.getAmount() + "x ").append(
                    description(Util.stripColor(
                            LegacyComponentSerializer.legacySection().serialize(
                                    itemStack.getItemMeta().displayName() != null ?
                                            itemStack.getItemMeta().displayName() :
                                            Component.text(itemStack.getType().name().toLowerCase().replace("_", " "))
                            )
                    ))
            ));
        }
    }
}
