package net.Servmine.HeroesSkillTree.language;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public enum LangList {
    CONSOLE_ENABLING("[HeroesSkillTree] Version B1.0 By wiedzmin137 has been enabled!"),
    CONSOLE_DISABLING("[HeroesSkillTree] Version B1.0 By wiedzmin137 has been disabled!"),
    COMMAND_SKILLPOINTS("[SkillTree] You currently have %1%!"),
    COMMAND_ADMIN_NOT_ENOUGH_ARGUMENTS("Not enough arguments: /skilladmin <command> (amount) [sender]"),
    PERMISSION_DENIED("You don\'t have enough permissions"),
    YOU_NOT_IN_GAME("You are not in game!");

    private String value;

    private LangList(String value) {
        set(value);
    }

    void set(String value) {
        this.value = value;
    }

    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public String format(String s) {
        return toString().replace("%", s);
    }

    public static void load(ConfigurationSection config) {
        for (LangList msg : values()) {
            String key = msg.name().toLowerCase().replace("_","-");
            msg.set(config.getString(key, ""));
        }
    }

    public static YamlConfiguration toYaml() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (LangList msg : values()) {
            String key = msg.name().replace("_","-").toLowerCase();
            yaml.set(key, msg.value);
        }
        return yaml;
    }
}