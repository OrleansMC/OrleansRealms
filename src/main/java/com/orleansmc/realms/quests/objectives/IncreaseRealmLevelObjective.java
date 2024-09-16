package com.orleansmc.realms.quests.objectives;


import com.orleansmc.realms.models.data.RealmModel;
import me.pikamug.quests.Quests;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class IncreaseRealmLevelObjective extends BukkitCustomObjective {
    public static IncreaseRealmLevelObjective instance;
    private static final Quests quests = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");

    public IncreaseRealmLevelObjective() {
        setName("Increase Realm Level Objective");
        setAuthor("MustqfaCan");
        setItem("DIAMOND", (short) 0);
        setShowCount(true);
        setCountPrompt("Diyar seviyesini girin:");
        setDisplay("Diyar Seviyeni Yükselt %count%");
        instance = this;
    }

    static void load() {
        if (quests == null) {
            throw new RuntimeException("Quests plugin not found!");
        }
        IncreaseRealmLevelObjective objective = new IncreaseRealmLevelObjective();
        quests.getCustomObjectives().removeIf(customObjective -> customObjective.getName().equals(objective.getName()));
        quests.getCustomObjectives().add(objective);
    }

    public void onRealmLevelChange(Player player, int increment) {
        assert quests != null;
        Quester quester = quests.getQuester(player.getUniqueId());
        for (Quest quest : quester.getCurrentQuests().keySet()) {
            if (increment > 0) {
                incrementObjective(quester.getUUID(), this, quest, increment);
                quester.dispatchMultiplayerEverything(quest, ObjectiveType.CUSTOM,
                        (final Quester q, final Quest cq) -> {
                            incrementObjective(q.getUUID(), this, quest, increment);
                            return null;
                        });
            }
        }
    }
}