package me.Whatshiywl.heroesskilltree;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang {
    TITLE("titleName", "&1[&9SkillTree&1]&r:"),
    CONSOLE_ENABLING("consoleEnabling", "Version B1.6 By wiedzmin137 has been enabled!"),
    CONSOLE_DISABLING("consoleDisabling", "Version B1.6 By wiedzmin137 has been disabled!"),
    INFO_SKILLPOINTS("infoSkillPoints", "&b[&rYou currently have &9%points% &rSkillPoints.&b]"),
    SERVRE_FAILED_CREATE("servreFailedCreate", "failed to create new %name.yml"),
    SERVRE_FAILED_DELETE("servreFailedDelete", "failed to delete %name.yml"),
    WARNING_TOO_HIGH_LEVEL("warningTooHighLevel", "%player%'s skills are at a too high level!"),
    ERROR_ADMIN_NOT_ENOUGH_ARGUMENTS("infoAdminNotEnoughArguments", "&cNot enough arguments: /skilladmin <command> (amount) [sender]"),
    ERROR_IN_CONSOLE_DENIED("infoConsoleDenied", "&cYou must be in game to use this command"),
    ERROR_PERMISSION_DENIED("infoPermissionDenied", "&cYou don't have enough permissions"),
    HELP_1("HELP_1", "&1[&9Help&1] &9SkillTree&r:"),
    HELP_2("HELP_2", "&9/skillup &b<skill> [amount] &r- level up a skill"),
    HELP_3("HELP_3", "&9/skilldown &b<skill> [amount] &r- de-levels a skill"),
    HELP_4("HELP_4", "&9/slist &r- ists all unlocked skills"),
    HELP_5("HELP_5", "&9/unlocks &r- lists all adjacent unlockable skills"),
    HELP_6("HELP_6", "&9/skillinfo &r<skill> (all info on a skill)"),
    HELP_7("HELP_7", "&9/skilladmin &b<command> (amount) [player]");

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
    
