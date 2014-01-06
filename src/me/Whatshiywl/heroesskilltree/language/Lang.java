package me.Whatshiywl.heroesskilltree.language;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang {
    TITLE("titleName", "&1[&9SkillTree&1]&r:"),
    CONSOLE_ENABLING("consoleEnabling", "Version A1.6 By wiedzmin137 has been enabled!"),
    CONSOLE_DISABLING("consoleDisabling", "Version A1.6 By wiedzmin137 has been disabled!"),
    INFO_SKILLPOINTS("infoSkillPoints", "You currently have %points SkillPoints."),
    SERVRE_FAILED_CREATE("servreFailedCreate", "failed to create new %name.yml"),
    SERVRE_FAILED_DELETE("servreFailedDelete", "[HeroesSkillTree] failed to delete %name.yml"),
    ERROR_ADMIN_NOT_ENOUGH_ARGUMENTS("infoAdminNotEnoughArguments", "Not enough arguments: /skilladmin <command> (amount) [sender]"),
    ERROR_IN_CONSOLE_DENIED("infoConsoleDenied", "&cYou must be in game to use this command"),
    ERROR_PERMISSION_DENIED("infoPermissionDenied", "You don't have enough permissions"),
    HELP_1("HELP_1", "&1[&9Help&1] &bSkillTree&r:"),
    HELP_2("HELP_2", "&b/skillup &r<skill> [amount] (level up a skill)"),
    HELP_3("HELP_3", "&b/skilldown &r<skill> [amount] (de-levels a skill)"),
    HELP_4("HELP_4", "&b/slist &r(lists all unlocked skills)"),
    HELP_5("HELP_5", "&b/unlocks &r(lists all adjacent unlockable skills)"),
    HELP_6("HELP_6", "&b/skillinfo &r<skill> (all info on a skill)"),
    HELP_7("HELP_7", "&b/skilladmin &r<command> (amount) [player]");

    private String path;
    private String def;
    private static YamlConfiguration LANG;
    
    Lang(String path, String start) {
    	this.path = path;
    	this.def = start;
    }
    
    public static void setFile(YamlConfiguration config) {
    	LANG = config;
    }
    
    @Override
    public String toString() {
    	if (this == TITLE)
    		return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def)) + " ";
    	return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }
    
    public String getDefault() {
    	return this.def;
    }
    
    public String getPath() {
    	return this.path;
    }
}
    
