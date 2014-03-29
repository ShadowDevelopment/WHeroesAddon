package me.Whatshiywl.heroesskilltree.commands;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.Lang;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;

public class CommandManager implements CommandExecutor {
	private HeroesSkillTree plugin;
	 
	public CommandManager(HeroesSkillTree plugin) { 
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
		String skillPoints = String.valueOf(plugin.getCurrentPoints(hero, hero.getHeroClass()));
		
		if (cmd.getName().equalsIgnoreCase("skilltree")){
        	if (sender instanceof Player) {
        		if (args.length == 0) {
        				sender.sendMessage(Lang.HELP_1.toString());
        				sender.sendMessage(Lang.HELP_2.toString());
        				sender.sendMessage(Lang.HELP_3.toString());
        				sender.sendMessage(Lang.HELP_4.toString());
        				sender.sendMessage(Lang.HELP_5.toString());
        				sender.sendMessage(Lang.HELP_6.toString());
        				sender.sendMessage(Lang.HELP_7.toString());
        				sender.sendMessage(Lang.INFO_SKILLPOINTS.toString().replace("%points%", skillPoints));
        		} else if (args.length > 0) {
        			if (args[1] == "up") {
        				SkillUpCommand.skillUp(plugin, sender, args);
        			}
				}
				return true;
			} else {
				sender.sendMessage("There is some problem with colors in console, use /skilltree IN game, sorry.");
			}
			return true;
		}
		return false;
	}
}
