package me.Wiedzmin137.wheroesaddon;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
* An enum for requesting strings from the language file.
* @author Gomeow
* @author Wiedzmin137
* @author Bednar12
*/
public enum Lang {
    TITLE("titleName", "&1[&bSkillTree&1]&r:"),
    TITLE_ITEM_GUI("itemGUITitle", "&1[&9 SkillTree of %class% &1]&r"),
    GUI_INVAILD_SKILLS("GUIInvaildSkills", "Skill %skill% has no valid identifiers and can not be used on the menu! Please contact the author to fix the skill."),
    GUI_LORE("GUILore", "&eClick for use!"),
    GUI_TITLE_SKILL("GUITitleSkill", "&2&l[&r&a%skill%&2&l]"),
    CONSOLE_ENABLED("consoleEnabled", "[WHeroesAddon] Version B1.6 By wiedzmin137 and Bednar12 has been enabled!"),
    CONSOLE_DISABLED("consoleDisabled", "[WHeroesAddon] Version B1.6 By wiedzmin137 and Bednar12 has been disabled!"),
    CONSOLE_SMS_ENABLED("consoleSMSEnabled", "[WHeroesAddon] ScrollingMenuSign integration is enabled; menus created"),
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
    HELP_7("HELP_7", "&9/skilladmin &b<command> (amount) [player]"),
    ADMIN_RESET_SUCCESS("ResetCommandSuccess", "&2You have reseted %player%'s skills"),
    ERROR_PLAYER_OFFLINE("playerIsOffline", "&cSorry, player %player% is offline."),
    SELF_RESET_SUCCESS("selfResetSuccess", "&2You have reseted your character"),
    WRONG_CMD_USAGE("wrongCommandUsage", "&cCorrect usage: /skilladmin (set/give/remove/clear/reset) <nick>"),
    ADMIN_SKILLPOINTS_ADD_SUCCESS("adminSkillpointAddSuccess", "You have added %player% %skillpoints% skillpoint(s)."),
    ADMIN_SKILLPOINTS_REMOVE_SUCCESS("adminSkillpointRemoveSuccess", "You have removed %skillpoints% skillpoint(s) from %player%.");

    private String path;
    private String def;
    private static YamlConfiguration LANG;
    
    /**
     * Lang enum constructor.
     * @param path The string path.
     * @param start The default string.
     */
    Lang(String path, String start) {
    	this.path = path;
    	this.def = start;
    }
    
    /**
     * Set the {@code YamlConfiguration} to use.
     * @param config The config to set.
     */
    public static void setFile(YamlConfiguration config) {
    	LANG = config;
    }
    
    @Override
    public String toString() {
    	if (this == TITLE)
    		return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def)) + " ";
    	return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }
    
    /**
     * Get the default value of the path.
     * @return The default value of the path.
     */
    public String getDefault() {
    	return this.def;
    }
    
    /**
     * Get the path to the string.
     * @return The path to the string.
     */
    public String getPath() {
    	return this.path;
    }
}
    
