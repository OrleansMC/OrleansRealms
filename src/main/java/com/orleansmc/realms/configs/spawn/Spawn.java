package com.orleansmc.realms.configs.spawn;

import com.orleansmc.realms.utils.Util;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class Spawn {
    public static Location LOCATION;
    public static Location WARDROBE_LOCATION;

    public static void load(YamlConfiguration configFile) {
        LOCATION = Util.getLocationFromString(configFile.getString("location", "spawn,0,100,0,0,0")).toCenterLocation();
        WARDROBE_LOCATION = Util.getLocationFromString(configFile.getString("wardrobe-location", "spawn,0,100,0,0,0")).toCenterLocation();
    }
}
