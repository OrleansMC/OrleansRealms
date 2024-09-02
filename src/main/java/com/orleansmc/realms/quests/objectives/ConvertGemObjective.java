package com.orleansmc.realms.quests.objectives;

import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.Quests;

import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConvertGemObjective extends BukkitCustomObjective {
    public static ConvertGemObjective instance;
    private static final Quests quests = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");

    public ConvertGemObjective() {
        setName("Convert Gem Objective");
        setAuthor("MustqfaCan");
        setItem("DIAMOND", (short) 0);
        setShowCount(true);
        setCountPrompt("Dönüştürülmesi gereken gem miktarını girin:");
        setDisplay("Mücevher Dönüştür %count%");
        instance = this;
    }

    static void load() {
        if (quests == null) {
            throw new RuntimeException("Quests plugin not found!");
        }
        ConvertGemObjective objective = new ConvertGemObjective();
        quests.getCustomObjectives().removeIf(customObjective -> customObjective.getName().equals(objective.getName()));
        quests.getCustomObjectives().add(objective);
    }

    public void onPlayerGemConvert(Player player, int amount) {
        assert quests != null;
        Quester quester = quests.getQuester(player.getUniqueId());
        for (Quest quest : quester.getCurrentQuests().keySet()) {
            if (amount > 0) {
                incrementObjective(quester.getUUID(), this, quest, amount);
                quester.dispatchMultiplayerEverything(quest, ObjectiveType.CUSTOM,
                        (final Quester q, final Quest cq) -> {
                            incrementObjective(q.getUUID(), this, quest, amount);
                            return null;
                        });
            }
        }
    }
}