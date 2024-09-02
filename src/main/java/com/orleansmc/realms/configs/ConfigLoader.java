package com.orleansmc.realms.configs;

import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.configs.spawn.Spawn;
import com.orleansmc.realms.configs.texts.Texts;
import me.lucko.helper.plugin.ExtendedJavaPlugin;

public class ConfigLoader {
    public static void load(ExtendedJavaPlugin plugin) {
        loadSettings(plugin);
        loadTexts(plugin);
        loadSpawn(plugin);
    }

    public static void loadSettings(ExtendedJavaPlugin plugin) {
        Settings.load(plugin.loadConfig("config.yml"));
    }

    public static void loadTexts(ExtendedJavaPlugin plugin) {
        Texts.load(plugin.loadConfig("texts.yml"));
    }

    public static void loadSpawn(ExtendedJavaPlugin plugin) {
        Spawn.load(plugin.loadConfig("spawn.yml"));
    }
}
