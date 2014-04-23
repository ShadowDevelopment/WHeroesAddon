package me.Whatshiywl.heroesskilltree.commands;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.Lang;
import me.Wiedzmin137.wheroesaddon.WAddonCore;
import me.Wiedzmin137.wheroesaddon.addons.ItemGUI;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;

public class CommandManager implements CommandExecutor {
	private HeroesSkillTree HST;
	 
	public CommandManager(HeroesSkillTree hst) { 
		this.HST = hst;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (cmd.getName().equalsIgnoreCase("skilltree")) {
					if (args.length > 0) {
						switch (args[0]) {
							case "up": SkillUpCommand.skillUp(HST, sender, args); break;
							case "down": SkillDownCommand.skillDown(HST, sender, args); break;
							case "list": SkillListCommand.skillList(HST, sender, args); break;
							case "unlocks": SkillLockedCommand.skillList(HST, sender, args); break;
							case "info": SkillInfoCommand.skillInfo(HST, sender, args); ; break;
							case "gui": ItemGUI.createSkillTree(sender, WAddonCore.heroes, HST); break;
							case "admin": SkillAdminCommand.skillAdmin(HST, sender, args); break;
							case "save": WAddonCore.getInstance().savePlayerConfig(sender.getName()); break;
							case "hashmap": HST.loadPlayerConfig(sender.getName());
											HST.recalcPlayerPoints(WAddonCore.heroes.getCharacterManager().getHero((Player)sender), 
													WAddonCore.heroes.getCharacterManager().getHero((Player)sender).getHeroClass());
											sender.sendMessage(HST.playerSkills.values().toString()); 
											sender.sendMessage(HST.playerClasses.values().toString()); break;
							//case "try": tryModules(sender); break;
							//case "req": tryRequirement(sender); break;
							default: showInfoList(sender); break;
						} 
					} else if (args.length == 0) {
						showInfoList(sender);
						return true;
					}
			} else if (cmd.getName().equalsIgnoreCase("choose")) {
				ItemGUI.createClassChoose((Player)sender);
				return true;
			}
		} else {
			sender.sendMessage("There is some problem with colors in console, use that command IN game, sorry.");
			return true;
		}
		return false;
	}

	private void showInfoList(CommandSender sender) {
		Hero hero = WAddonCore.heroes.getCharacterManager().getHero((Player)sender);
		String skillPoints = String.valueOf(HST.getPlayerPoints(hero));
		
		sender.sendMessage(Lang.HELP_1.toString());
		sender.sendMessage(Lang.HELP_2.toString());
		sender.sendMessage(Lang.HELP_3.toString());
		sender.sendMessage(Lang.HELP_4.toString());
		sender.sendMessage(Lang.HELP_5.toString());
		sender.sendMessage(Lang.HELP_6.toString());
		sender.sendMessage(Lang.HELP_7.toString());
		sender.sendMessage(Lang.HELP_8.toString());
		sender.sendMessage(Lang.INFO_SKILLPOINTS.toString().replace("%points%", skillPoints));
	}
	
	private void tryModules(CommandSender sender) {
		String requirements = WAddonCore.getInstance().getModuleManager().getModules().toString();
		sender.sendMessage(requirements);
	}
	
//	private void tryRequirement(CommandSender sender) {
//        for (String s : HST.customRequirements.keySet()){
//        	Module module = WAddonCore.getInstance().getModuleManager();
//        	Requirement found = null;
//        	for (Requirement cr : module.customRequirements){
//        		if(cr.getName().equalsIgnoreCase(s)){
//        			found = cr;
//        			break;
//        		}
//        	}
//        	
//        	if(found != null){
//        		if(found.isRequirementPassed((Player)sender, HST.customRequirements.get(s)) == false)
//        			WAddonCore.Log.warning("Nope! ;(");
//        			return;
//        	} else {
//        		WAddonCore.Log.warning("[WHeroesAddon] Player " + sender + " attempted to upgrade SkillTree but the Requirement could not be found. Does it still exist?");
//        	}
//        }
//	}
}
