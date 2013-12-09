package me.Whatshiywl.heroesskilltree.language;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Set;

public class UtilTest {

    public static void addMissingRemoveObsolete(Plugin plugin, String resource, ConfigurationSection section) {
        process(plugin, resource, section, false, true);
    }

    public static void addMissingRemoveObsolete(File file, YamlConfiguration defaults, FileConfiguration config) {
        try {
            process(defaults, config, false, true);
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void process(Plugin plugin, String resource, ConfigurationSection section, boolean addOnlyIfEmpty, boolean removeObsolete) {
        try {
            YamlConfiguration defaults = new YamlConfiguration();
            defaults.load(plugin.getResource("res/" + resource));

            process(defaults, section, addOnlyIfEmpty, removeObsolete);
            plugin.saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void process(YamlConfiguration defaults, ConfigurationSection section, boolean addOnlyIfEmpty, boolean removeObsolete) {
        Set<String> present = section.getKeys(true);
        Set<String> required = defaults.getKeys(true);
        if (!addOnlyIfEmpty || present.isEmpty()) {
            for (String req : required) {
                if (!present.remove(req)) {
                    section.set(req, defaults.get(req));
                }
            }
        }
        if (removeObsolete) {
            for (String obs : present) {
                section.set(obs, null);
            }
        }
    }
}



//private void loadLanguageFile() {
//    // Create if missing
//    File file = new File(getDataFolder(), "lang.yml");
//    try {
//        if (file.createNewFile()) {
//            LangSender.info("lang.yml created.");
//            YamlConfiguration yaml = LangList.toYaml();
//            yaml.save(file);
//            return;
//        }
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//
//    // Otherwise, load the announcements from the file
//    try {
//        YamlConfiguration yaml = new YamlConfiguration();
//        yaml.load(file);
//        UtilTest.addMissingRemoveObsolete(file, LangList.toYaml(), yaml);
//        LangList.load(yaml);
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//}
