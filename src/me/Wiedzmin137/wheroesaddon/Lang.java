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
	//TODO compare this list with lang.yml 
    TITLE("Title.MainTitle", "&1[&bSkillTree&1]&r"),
    TITLE_ITEM_GUI("Title.ItemGUI", "&1[&9 %class% &1]&r"),
    GUI_INVAILD_SKILLS("GUI.InvaildSkills", "Skill %skill% has no valid identifiers and can not be used on the menu! Please contact the author to fix the skill."),
    GUI_LORE("GUI.Lore", "&eClick for use!"),
    GUI_LORE_LEVEL("GUI.LoreLevel", "&f&oSkillLevel: %level%/%maxLevel%"),
    GUI_LORE_MANA("GUI.LoreMana", "&f&oMana: %manaCost%"),
    GUI_TITLE_SKILL("GUI.TitleSkill", "&2&l[&r&a%skill%&2&l]"),
    GUI_TITLE_CHOOSE("GUI.TitleChoose", "&1== &bChoose your class! &1=="),
    HOLOGRAM_MESSAGE_EXP_GAINED("Hologram.MessageEXPCurrent", "&a&l+ &2&o%gained%XP"),
    HOLOGRAM_MESSAGE_EXP_MAX("Hologram.MessageEXPMax", "&2[ &9%current%&1/&9%needed% &2]"),
    CONSOLE_ENABLED("Console.Enabled", "[WHeroesAddon] Version B1.6 By wiedzmin137 and Bednar12 has been enabled!"),
    CONSOLE_DISABLED("Console.Disabled", "[WHeroesAddon] Version B1.6 By wiedzmin137 and Bednar12 has been disabled!"),
    CONSOLE_SMS_ENABLED("Console.SMSEnabled", "[WHeroesAddon] ScrollingMenuSign integration is enabled; menus created"),
    CONSOLE_AUTHME_ENABLED("Console.AuthMeEnabled", "[WHeroesAddon] AuthMe integration is enabled"),
    INFO_SKILLPOINTS("Info.SkillPoints", "&b[&rYou currently have &9%points% &rSkillPoints&b]"),
    SERVRE_FAILED_CREATE("Servre.FailedCreate", "failed to create new %name.yml"),
    SERVRE_FAILED_DELETE("Servre.FailedDelete", "failed to delete %name.yml"),
    WARNING_TOO_HIGH_LEVEL("Warning.TooHighLevel", "%player%'s skills are at a too high level!"),
    ERROR_ADMIN_NOT_ENOUGH_ARGUMENTS("Error.AdminNotEnoughArguments", "&cNot enough arguments: /skilladmin <command> (amount) [sender]"),
    ERROR_CONSOLE_DENIED("Error.ConsoleDenied", "&cYou must be in game to use this command"),
    ERROR_PERMISSION_DENIED("Error.PermissionDenied", "&cYou don't have enough permissions"),
    ERROR_COMMAND_USAGE("Error.CommandUsage", "&cCorrect usage: /skilladmin (set/give/remove/clear/reset) <nick>"),
    ERROR_PLAYER_OFFLINE("Error.PlayerOffline", "&cSorry, player %player% is offline."),
    ADMIN_PLAYER_RESET_SUCCESS("Admin.PlayerResetSuccess", "&2You have reseted %player%'s skills"),
    ADMIN_SELF_RESET_SUCCESS("Admin.SelfResetSuccess", "&2You have reseted your character"),
    ADMIN_SKILLPOINTS_ADD_SUCCESS("Admin.SkillPointsAddSuccess", "You have added %player% %skillpoints% skillpoint(s)."),
    ADMIN_SKILLPOINTS_REMOVE_SUCCESS("Admin.SkillPointsRemoveSuccess", "You have removed %skillpoints% skillpoint(s) from %player%."),
    HELP_1("Help.1", "&9&l=._______==&1[&b&oSkillTree&1]&9&l==_______.="),
    HELP_2("Help.2", "&9/SkillTree Up &b<&oskill&b&b> [&oamount&b] &r- level up a skill"),
    HELP_3("Help.3", "&9/SkillTree Down &b<&oskill&b> [&oamount&b] &r- de-levels a skill"),
    HELP_4("Help.4", "&9/SkillTree List &r- list of all unlocked skills"),
    HELP_5("Help.5", "&9/SkillTree Unlocks &r- lists all adjacent unlockable skills"),
    HELP_6("Help.6", "&9/SkillTree Info &b<&oskill&b> &r- all info on a skill"),
    HELP_7("Help.7", "&9/SkillTree GUI &r- show upgrading skills GUI (DEV)"),
    HELP_8("Help.8", "&9/SkillTree Admin &b<&ocommand&b> (&oamount&b) [&oplayer&b]");

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
    
