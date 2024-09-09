package com.orleansmc.realms.managers.common;

import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;

public class GameRulesManager {
    public static void setup(OrleansRealms plugin) {
        if (plugin.serversManager.getCurrentServerType() == ServerType.REALMS) {
            World realm = plugin.getServer().getWorld(Settings.WORLD_NAME);
            if (realm == null) {
                plugin.getLogger().warning("World " + Settings.WORLD_NAME + " not found");
                return;
            }
            realm.setGameRule(GameRule.DO_FIRE_TICK, false);
            realm.setGameRule(GameRule.MOB_GRIEFING, false);
            realm.setGameRule(GameRule.SPAWN_RADIUS, 0);
            realm.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
            realm.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            realm.setGameRule(GameRule.DO_INSOMNIA, false);
            realm.setGameRule(GameRule.DISABLE_RAIDS, true);
            realm.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
            realm.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            realm.getWorldBorder().setWarningDistance(0);
            realm.setDifficulty(Difficulty.EASY);
        }

        if (plugin.serversManager.getCurrentServerType() == ServerType.REALMS_SPAWN) {
            World spawn = plugin.getServer().getWorld(Settings.WORLD_NAME);
            if (spawn == null) {
                plugin.getLogger().warning("World " + Settings.WORLD_NAME + " not found");
                return;
            }
            spawn.setGameRule(GameRule.DO_FIRE_TICK, false);
            spawn.setGameRule(GameRule.MOB_GRIEFING, false);
            spawn.setGameRule(GameRule.SPAWN_RADIUS, 0);
            spawn.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
            spawn.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            spawn.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            spawn.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            spawn.setGameRule(GameRule.DO_MOB_LOOT, false);
            spawn.setGameRule(GameRule.DO_TILE_DROPS, false);
            spawn.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            spawn.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            spawn.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            spawn.setGameRule(GameRule.DO_INSOMNIA, false);
            spawn.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }

        if (plugin.serversManager.getCurrentServerType() == ServerType.REALMS_OUTLAND) {
            World outland = plugin.getServer().getWorld(Settings.WORLD_NAME);
            if (outland == null) {
                plugin.getLogger().warning("World " + Settings.WORLD_NAME + " not found");
                return;
            }
            outland.setGameRule(GameRule.DO_FIRE_TICK, false);
            outland.setGameRule(GameRule.MOB_GRIEFING, true);
            outland.setGameRule(GameRule.SPAWN_RADIUS, 0);
            outland.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
            outland.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            outland.setGameRule(GameRule.DO_INSOMNIA, false);
            outland.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            outland.setDifficulty(Difficulty.NORMAL);
        }
    }
}
