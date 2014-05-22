package me.Whatshiywl.heroesskilltree.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.WAddonCore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;

public class SkillListCommand {
	//TODO create lanugage support
	public static void skillList(HeroesSkillTree hst, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
			return;
		}
		Hero hero = WAddonCore.heroes.getCharacterManager().getHero((Player)sender);
		
		int j = 0;
		HashMap<String, Skill> skills = new HashMap<String, Skill>();
		ArrayList<String> alphabeticalSkills = new ArrayList<String>();
		if (hero.getHeroClass() != null) {
			for (String skillName : hero.getHeroClass().getSkillNames()) {
				Skill skill = WAddonCore.heroes.getSkillManager().getSkill(skillName);
				if ((!hst.isLocked(hero, skill)) && (hero.canUseSkill(skill))) {
					skills.put(skillName, skill);
					alphabeticalSkills.add(skillName);
				}
			}
		}
		Collections.sort(alphabeticalSkills);
		int k = 0;
		int t = 0;
		if ((args.length > 1) && (!args[1].equalsIgnoreCase("1"))) {
			try {
				t = Integer.parseInt(args[1]);
				t = t < 2 ? 1 : t;
				k = (t - 1) * 10;
			}
			catch (NumberFormatException e) {
				k = 0;
			}
		} else {
			t = 1;
		}
		sender.sendMessage(ChatColor.GOLD + "[HST] Unlocked skills list page " + t + "/" 
				+ Math.round(alphabeticalSkills.size() / 10.0D));
		for (int i = k; (j < 10) && (i < alphabeticalSkills.size()); i++) {
			if (j > 9) {
				break;
			}
			String name = alphabeticalSkills.get(i);
			int maxlevel = hst.getSkillMaxLevel(hero, skills.get(name));
			if (maxlevel >= 0) {
				int level = hst.getSkillLevel(hero, skills.get(name));
				sender.sendMessage(ChatColor.GREEN + name + " (" + level + "/" + maxlevel + "): " + 
						ChatColor.GRAY + skills.get(name).getDescription(hero));
			} else {
				sender.sendMessage(ChatColor.GREEN + name + ": " + ChatColor.GRAY + skills.get(name).getDescription(hero));
			}
			j++;
		}
	}
}

