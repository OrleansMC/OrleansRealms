package com.orleansmc.realms.managers;

import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.utils.Util;
import me.pikamug.quests.Quests;
import me.pikamug.quests.events.quester.BukkitQuesterPostCompleteQuestEvent;
import me.pikamug.quests.player.BukkitQuestProgress;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.quests.components.Stage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class QuestManager implements Listener {
    public static final Quests questsPlugin = (Quests) Objects.requireNonNull(
            Bukkit.getServer().getPluginManager().getPlugin("Quests")
    );

    public QuestManager(OrleansRealms plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public List<Quest> getQuests() {
        return questsPlugin.getLoadedQuests().stream().sorted(
                Comparator.comparingInt(q -> q.getRewards().getQuestPoints())
        ).toList();
    }

    public Quest getRequiredPlayerQuest(Player player) {
        List<Quest> quests = getQuests();
        int requiredQuestIndex = 0;
        for (Quest quest : quests) {
            Quester quester = questsPlugin.getQuester(player.getUniqueId());
            if (quester.getCompletedQuests().stream().anyMatch(q -> q.getName().equals(quest.getName()))) {
                requiredQuestIndex++;
            } else {
                return quests.get(requiredQuestIndex);
            }
        }
        return null;
    }

    public Quest applyRequiredQuest(Player player) {
        Quest quest = getRequiredPlayerQuest(player);
        if (quest == null) {
            return null;
        }
        Quester quester = questsPlugin.getQuester(player.getUniqueId());
        if (quester.getCurrentQuests().isEmpty()) {
            quester.takeQuest(quest, true);
        }
        return quest;
    }

    public Quest getPlayerQuest(Player player) {
        Quester quester = questsPlugin.getQuester(player.getUniqueId());
        Quest quest = quester.getCurrentQuests().keySet().stream().findFirst().orElse(null);
        if (quest == null) {
            return applyRequiredQuest(player);
        }
        return quest;
    }

    public String getPlayerObjectiveTitle(Player player, String questObjective) {
        Quester quester = QuestManager.questsPlugin.getQuester(player.getUniqueId());
        Quest quest = quester.getCurrentQuests().keySet().stream().findFirst().orElse(null);
        if (quest == null) {
            return Util.stripColor(questObjective);
        }
        Stage stage = quester.getCurrentStage(quest);
        if (stage == null) {
            return Util.stripColor(questObjective);
        }
        questObjective = questObjective.split("\n")[0];
        return Util.stripColor(questObjective).replaceAll("%realms_quests_start_message%", Util.stripColor(stage.getStartMessage()));
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater((JavaPlugin) questsPlugin, () -> {
            applyRequiredQuest(event.getPlayer());
        }, 20L);
    }

    @EventHandler
    public void onQuestComplete(BukkitQuesterPostCompleteQuestEvent event) {
        Bukkit.getScheduler().runTaskLater((JavaPlugin) questsPlugin, () -> {
            Quester quester = event.getQuester();
            if (quester.getPlayer() != null) {
                applyRequiredQuest(quester.getPlayer());
            }
        }, 20L * 10);
    }
}
