package com.orleansmc.realms.quests.objectives;

import com.orleansmc.realms.models.data.RealmModel;
import me.pikamug.quests.Quests;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class IncreaseRealmSizeObjective extends BukkitCustomObjective {
    public static IncreaseRealmSizeObjective instance;
    private static final Quests quests = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");

    public IncreaseRealmSizeObjective() {
        setName("Increase Realm Size Objective");
        setAuthor("MustqfaCan");
        setItem("DIAMOND_PICKAXE", (byte) 0);
        setShowCount(true);
        addStringPrompt(
                "Diyar Boyutu Artırma Görevi",
                "Diyarınızın boyutunu 300x300 yapın.",
                "Diyarınızın boyutunu 300x300 yapın."
        );
        setCountPrompt("1 olarak ayarlayın.");
        setDisplay("Diyarınızın boyutunu 300x300 yapın.");
        instance = this;
    }

    static void load() {
        if (quests == null) {
            throw new RuntimeException("Quests plugin not found!");
        }
        IncreaseRealmSizeObjective objective = new IncreaseRealmSizeObjective();
        quests.getCustomObjectives().removeIf(customObjective -> customObjective.getName().equals(objective.getName()));
        quests.getCustomObjectives().add(objective);
    }

    public void onComplete(RealmModel realm) {
        Player player = Bukkit.getPlayer(realm.owner);
        if (player == null) {
            quests.getPluginLogger().info("Player not found for realm owner " + realm.owner);
            return;
        }
        if (quests == null) {
            throw new RuntimeException("Quests plugin not found!");
        }
        for (Quest q : quests.getQuester(player.getUniqueId()).getCurrentQuests().keySet()) {
            if (q.getStage(0).getCustomObjectives().stream().anyMatch(co -> co.getName().equals(instance.getName()))) {
                Bukkit.getScheduler().runTask((JavaPlugin) quests, () -> {
                    q.completeQuest(quests.getQuester(player.getUniqueId()));
                });
            }
            return;
        }
    }
}