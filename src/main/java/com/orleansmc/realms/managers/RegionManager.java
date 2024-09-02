package com.orleansmc.realms.managers;

import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.enums.RealmClimate;
import com.orleansmc.realms.models.data.RealmModel;
import com.orleansmc.realms.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class RegionManager {
    public static HashMap<String, List<String>> removedRegions = new HashMap<>();

    public static void unloadAndDeleteRegion(World world, int regionX, int regionZ, boolean isRealm) {
        OrleansRealms plugin = (OrleansRealms) Bukkit.getPluginManager().getPlugin("OrleansRealms");
        assert plugin != null;
        for (Player player : world.getPlayers()) {
            RealmModel realm = plugin.realmsManager.getRealmByLocation(player.getLocation());
            RealmModel realmByRegion = plugin.realmsManager.getRealmByRegionCoordinates(regionX, regionZ);
            if (realm != null && realmByRegion != null && realm.owner.equals(realmByRegion.owner)) {
                Bukkit.getScheduler().runTask(plugin, () -> player.kick(Component.empty()));
            }
        }
        if (isRealm) {
            for (int x = -1; x < 1; x++) {
                for (int z = -1; z < 1; z++) {
                    deleteRegion(world, regionX + x, regionZ + z, true);
                }
            }
        } else {
            deleteRegion(world, regionX, regionZ, false);
        }
    }

    public static void deleteRegion(World world, int regionX, int regionZ, boolean unload) {
        if (unload) {
            OrleansRealms plugin = (OrleansRealms) Bukkit.getPluginManager().getPlugin("OrleansRealms");
            assert plugin != null;
            Bukkit.getScheduler().runTask(plugin, () -> Arrays.stream(world.getLoadedChunks()).toList().forEach(chunk -> {
                if (chunk.getX() >= regionX * 32 && chunk.getX() < (regionX + 1) * 32 && chunk.getZ() >= regionZ * 32 && chunk.getZ() < (regionZ + 1) * 32) {
                    chunk.unload(true);
                }
            }));
        }

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String[] directories = {"region", "entities", "itemsadder", "poi"};

            for (String dir : directories) {
                File file = new File(world.getWorldFolder(), dir + "/r." + regionX + "." + regionZ + ".mca");
                if (dir.equals("itemsadder")) {
                    file = new File(world.getWorldFolder(), dir + "/region/r." + regionX + "." + regionZ + ".pregion");
                }
                if (file.exists()) {
                    if (file.delete()) {
                        Bukkit.getLogger().log(Level.INFO, "File deleted: " + file.getPath());
                    } else {
                        Bukkit.getLogger().log(Level.SEVERE, "Failed to delete file: " + file.getPath());
                    }
                } else {
                    Bukkit.getLogger().log(Level.INFO, "File not found: " + file.getPath());
                }
            }
        }).start();
    }

    public static void unloadAndDeleteUnRealmRegions(World world) {
        OrleansRealms plugin = (OrleansRealms) Bukkit.getPluginManager().getPlugin("OrleansRealms");
        assert plugin != null;
        List<int[]> realmRegions = new ArrayList<>();
        for (RealmModel realm : plugin.realmsManager.realms.values()) {
            int[] region = Util.getRegionCoordinatesFromString(realm.region);
            for (int x = -1; x < 1; x++) {
                for (int z = -1; z < 1; z++) {
                    realmRegions.add(new int[]{region[0] + x, region[1] + z});
                }
            }
        }
        plugin.getLogger().info("All regions: " + getRegionCoordinates(new File(world.getWorldFolder(), "region")));
        for (int[] region : getRegionCoordinates(new File(world.getWorldFolder(), "region"))) {
            plugin.getLogger().info("Region: " + region[0] + ", " + region[1]);
            RealmModel realm = plugin.realmsManager.getRealmByRegionCoordinates(region[0], region[1]);
            if (!realmRegions.contains(region) && realm == null) {
                plugin.getLogger().info("Unloading and deleting region: " + region[0] + ", " + region[1]);
                unloadAndDeleteRegion(world, region[0], region[1], false);
            } else if (realmRegions.contains(region) && realm != null && realm.unlocked_radius < 512) {
                plugin.getLogger().info("Unloading and deleting region: " + region[0] + ", " + region[1]);
                int[] mainRegion = Util.getRegionCoordinatesFromString(realm.region);
                if (mainRegion[0] != region[0] && mainRegion[1] != region[1]) {
                    unloadAndDeleteRegion(world, region[0], region[1], false);
                }
            }
        }
    }

    private static List<int[]> getRegionCoordinates(File directory) {
        List<int[]> regions = new ArrayList<>();
        OrleansRealms plugin = (OrleansRealms) Bukkit.getPluginManager().getPlugin("OrleansRealms");

        File[] files = directory.listFiles((dir, name) -> name.startsWith("r.") && name.endsWith(".mca"));

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                // Dosya adını parçala ve koordinatları al
                String[] parts = fileName.split("\\.");
                if (parts.length >= 3) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2].replace(".mca", ""));
                        regions.add(new int[]{x, z});
                    } catch (NumberFormatException e) {
                        System.out.println("Koordinatları ayrıştırırken hata: " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("Dosyalar alınamadı.");
        }

        return regions;
    }

    public static int[] getRegionCoordinatesByClimate(RealmClimate climate, int index) {
        final int stepSize = 9;
        int x = 0;
        int z = 0;

        // Determine which direction to move
        if (index % 2 == 0) {
            x = (index / 2) * stepSize;
            z = ((index - 1) / 2) * stepSize;
        } else {
            z = (index / 2) * stepSize;
            x = ((index - 1) / 2) * stepSize;
        }

        // Calculate the center coordinates of the 9x9 area
        int centerX = x + stepSize / 2;
        int centerZ = z + stepSize / 2;

        List<String> serverRemovedRegions = removedRegions.computeIfAbsent(Settings.SERVER_NAME, k -> new ArrayList<>());

        RealmClimate candidate = null;
        if (climate == RealmClimate.HOT_BARREN || climate == RealmClimate.DRY_VEGETATION) {
            candidate = determineBiomesClimate(centerX, centerZ);
        } else if (climate == RealmClimate.COLD) {
            centerZ = -centerZ;
            candidate = determineBiomesClimate(centerX, centerZ);
        } else if (climate == RealmClimate.HUMID_VEGETATION) {
            centerX = -centerX;
            candidate = determineBiomesClimate(centerX, centerZ);
        } else if (climate == RealmClimate.SNOWY) {
            centerX = -centerX;
            centerZ = -centerZ;
            candidate = determineBiomesClimate(centerX, centerZ);
        }

        if (candidate == climate && !serverRemovedRegions.contains(centerX + "," + centerZ)) {
            return new int[]{centerX, centerZ};
        } else {
            Bukkit.getLogger().info("Region " + centerX + ", " + centerZ + " is not suitable for " + climate);
            return getRegionCoordinatesByClimate(climate, index + 1);
        }
    }

    public static int[][] getRegionBlockCoordinates(int regionX, int regionZ) {
        // Başlangıç blok koordinatları
        int startX = regionX * 16 * 32;
        int startZ = regionZ * 16 * 32;

        // Bitiş blok koordinatları
        int endX = startX + 16 * 32 - 1;
        int endZ = startZ + 16 * 32 - 1;

        return new int[][]{{startX, startZ}, {endX, endZ}};
    }

    public static int[] getRegionFromLocation(int x, int z) {
        int regionX = (int) Math.floor((double) x / (16 * 32));
        int regionZ = (int) Math.floor((double) z / (16 * 32));

        return new int[]{regionX, regionZ};
    }

    private static RealmClimate determineBiomesClimate(int regionX, int regionZ) {
        if (Math.abs(regionX) == regionX && Math.abs(regionZ) == regionZ) {
            if ((regionX + regionZ) % 2 == 0) {
                return RealmClimate.HOT_BARREN;
            } else {
                return RealmClimate.DRY_VEGETATION;
            }
        } else if (Math.abs(regionX) == regionX && Math.abs(regionZ) != regionZ) {
            return RealmClimate.COLD;
        } else if (Math.abs(regionX) != regionX && Math.abs(regionZ) == regionZ) {
            return RealmClimate.HUMID_VEGETATION;
        } else {
            return RealmClimate.SNOWY;
        }
    }

    public static Location getCenterLocation(int regionX, int regionZ) {
        int[][] regionBlockCoordinates = getRegionBlockCoordinates(regionX, regionZ);
        int[] start = regionBlockCoordinates[0];
        int[] end = regionBlockCoordinates[1];

        int centerX = (start[0] + end[0]) / 2;
        int centerZ = (start[1] + end[1]) / 2;

        return new Location(Bukkit.getWorld(Settings.WORLD_NAME), centerX, 0, centerZ);
    }

    public static int getRealmLevelFromRegion(int regionX, int regionZ) {
        World realmWorld = Bukkit.getWorld(Settings.WORLD_NAME);
        if (realmWorld == null) {
            return 0;
        }
        File regionFile = new File(realmWorld.getWorldFolder(), "region/r." + regionX + "." + regionZ + ".mca");
        File itemsadderRegionFile = new File(realmWorld.getWorldFolder(), "itemsadder/region/r." + regionX + "." + regionZ + ".pregion");
        File poiRegionFile = new File(realmWorld.getWorldFolder(), "poi/r." + regionX + "." + regionZ + ".mca");
        File entitiesRegionFile = new File(realmWorld.getWorldFolder(), "entities/r." + regionX + "." + regionZ + ".mca");
        AtomicInteger level = new AtomicInteger(0);

        if (regionFile.exists()) {
            long fileSizeInBytes = regionFile.length();
            double fileSizeInKB = (double) fileSizeInBytes / 1024;
            double fileSizeInMB = fileSizeInKB / 1024;

            int regionLevel = (int) (fileSizeInMB - 7) * 10;
            if (regionLevel > 0) {
                level.addAndGet(regionLevel);
            }
        }

        if (itemsadderRegionFile.exists()) {
            long fileSizeInBytes = itemsadderRegionFile.length();
            double fileSizeInKB = (double) fileSizeInBytes / 1024;

            int itemsAdderLevel = (int) (fileSizeInKB * 2);
            level.addAndGet(itemsAdderLevel);
        }

        if (poiRegionFile.exists()) {
            long fileSizeInBytes = poiRegionFile.length();
            double fileSizeInKB = (double) fileSizeInBytes / 1024;

            int poiLevel = (int) (fileSizeInKB / 10);
            level.addAndGet(poiLevel);
        }

        if (entitiesRegionFile.exists()) {
            long fileSizeInBytes = entitiesRegionFile.length();
            double fileSizeInKB = (double) fileSizeInBytes / 1024;

            int entitiesLevel = (int) (fileSizeInKB - 350) / 10;
            if (entitiesLevel > 0) {
                level.addAndGet(entitiesLevel);
            }
        }

        return level.get();
    }
}
