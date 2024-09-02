package com.orleansmc.realms.quests.objectives;

import com.orleansmc.realms.models.data.RealmModel;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.Quests;

import me.pikamug.quests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CreateRealmObjective extends BukkitCustomObjective {
    public static CreateRealmObjective instance;
    // Get the Quests plugin
    private static final Quests quests = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");

    public CreateRealmObjective() {
        setName("Realm Creation Objective");
        setAuthor("MustqfaCan");
        setItem("DIAMOND_PICKAXE", (byte) 0);
        setShowCount(true);
        addStringPrompt(
                "Diyar Oluşturma Görevi",
                "Diyarınızı oluşturun",
                "Bir diyar oluşturun"
        );
        setCountPrompt("1 olarak ayarlayın.");
        setDisplay("Diyarını Oluştur");
        instance = this;
    }

    static void load() {
        if (quests == null) {
            throw new RuntimeException("Quests plugin not found!");
        }
        CreateRealmObjective objective = new CreateRealmObjective();
        quests.getCustomObjectives().removeIf(customObjective -> customObjective.getName().equals(objective.getName()));
        quests.getCustomObjectives().add(objective);
    }

    public void onCreatedRealm(RealmModel realm) {
        Player player = Bukkit.getPlayer(realm.owner);
        if (player == null) {
            quests.getPluginLogger().info("Player not found for realm owner " + realm.owner);
            return;
        }
        if (quests == null) {
            throw new RuntimeException("Quests plugin not found!");
        }
        quests.getPluginLogger().info("Realm created by " + player.getName());
        for (Quest q : quests.getQuester(player.getUniqueId()).getCurrentQuests().keySet()) {
            if (q.getStage(0).getCustomObjectives().stream().anyMatch(co -> co.getName().equals(instance.getName()))) {
                quests.getPluginLogger().info("Completing quest for " + player.getName());
                Bukkit.getScheduler().runTask((JavaPlugin) quests, () -> {
                    q.completeQuest(quests.getQuester(player.getUniqueId()));
                });
            }
            return;
        }
    }
}