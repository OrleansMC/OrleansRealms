package com.orleansmc.realms.models.config;
import org.bukkit.configuration.file.YamlConfiguration;

public class BenefitModel {
    public static class BenefitBooleanModel {
        public String name;
        public boolean legend;
        public boolean elite;
        public boolean titan;
        public boolean lord;

        public BenefitBooleanModel(String name, boolean legend, boolean elite, boolean titan, boolean lord) {
            this.name = name;
            this.legend = legend;
            this.elite = elite;
            this.titan = titan;
            this.lord = lord;
        }
    }

    public static class BenefitIntegerModel {
        public String name;
        public int legend;
        public int elite;
        public int titan;
        public int lord;

        public BenefitIntegerModel(String name, int legend, int elite, int titan, int lord) {
            this.name = name;
            this.legend = legend;
            this.elite = elite;
            this.titan = titan;
            this.lord = lord;
        }
    }

    public static class BenefitStringModel {
        public String name;
        public String legend;
        public String elite;
        public String titan;
        public String lord;

        public BenefitStringModel(String name, String legend, String elite, String titan, String lord) {
            this.name = name;
            this.legend = legend;
            this.elite = elite;
            this.titan = titan;
            this.lord = lord;
        }
    }

    public static BenefitBooleanModel getBooleanModel(String name, YamlConfiguration configFile) {
        return new BenefitBooleanModel(
                name,
                configFile.getBoolean(name + ".legend", false),
                configFile.getBoolean(name + ".elite", false),
                configFile.getBoolean(name + ".titan", false),
                configFile.getBoolean(name + ".lord", false)
        );
    }

    public static BenefitIntegerModel getIntegerModel(String name, YamlConfiguration configFile) {
        return new BenefitIntegerModel(
                name,
                configFile.getInt(name + ".legend", 0),
                configFile.getInt(name + ".elite", 0),
                configFile.getInt(name + ".titan", 0),
                configFile.getInt(name + ".lord", 0)
        );
    }

    public static BenefitStringModel getStringModel(String name, YamlConfiguration configFile) {
        return new BenefitStringModel(
                name,
                configFile.getString(name + ".legend", ""),
                configFile.getString(name + ".elite", ""),
                configFile.getString(name + ".titan", ""),
                configFile.getString(name + ".lord", "")
        );
    }
}
