package me.Whatshiywl.heroesskilltree.commands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.skill.Skill;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.Lang;
import me.Wiedzmin137.wheroesaddon.Module;
import me.Wiedzmin137.wheroesaddon.Requirement;
import me.Wiedzmin137.wheroesaddon.WAddonCore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SkillUpCommand {
   //TODO translate messages

   public static void skillUp(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(!sender.hasPermission("skilltree.up")) {
         sender.sendMessage(Lang.ERROR_PERMISSION_DENIED.toString());
      } else if(args.length < 2) {
         sender.sendMessage(ChatColor.RED + "No skill given: /skillup (skill) [amount]");
      } else if(!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
      } else {
         Hero hero = WAddonCore.heroes.getCharacterManager().getHero((Player)sender);
         Skill skill = WAddonCore.heroes.getSkillManager().getSkill(args[1]);
         if(skill != null && hero.hasAccessToSkill(skill.getName())) {
            if(hst.getSkillMaxLevel(hero, skill) == -1) {
               sender.sendMessage(ChatColor.RED + "This skill can\'t be increased");
            } else {
               int pointsToIncrease;
               try {
                  pointsToIncrease = (args.length > 2) ? Integer.parseInt(args[2]) : 1;
               } catch (NumberFormatException e) {
                  sender.sendMessage(ChatColor.RED + "Please enter a number of points to increase");
                  return;
               }

               if(hst.getPlayerPoints(hero) < pointsToIncrease) {
                  sender.sendMessage(ChatColor.RED + "You don\'t have enough SkillPoints.");
               } else if(hst.getSkillMaxLevel(hero, skill) < hst.getSkillLevel(hero, skill) + pointsToIncrease) {
                  sender.sendMessage(ChatColor.RED + "This skill has already been mastered.");
               } else if(hst.isLocked(hero, skill) && !hst.canUnlock(hero, skill)) {
                  sender.sendMessage(ChatColor.RED + "You can\'t unlock this skill! /skillinfo (skill) to see requirements.");
               } else {
            	  getCustomRequirements(sender, args);
                  if(!sender.hasPermission("skilltree.override.usepoints") && testRequirements(sender)) {
                     hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) - pointsToIncrease);
                  }
                  
//                  for (Requirement module : WAddonCore.getInstance().getModuleManager().getModules()) {
//                	  module.executeRequirement();
//                  }
                  
                  hst.setSkillLevel(hero, skill, hst.getSkillLevel(hero, skill) + pointsToIncrease);
                  WAddonCore.getInstance().savePlayerConfig(sender.getName());
                  hero.addEffect(new Effect(skill, skill.getName()));
                  if(hst.isLocked(hero, skill)) {
                	  sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have unlocked " + skill.getName() + "! Level: " + hst.getSkillLevel(hero, skill));
                  } else if(hst.isMastered(hero, skill)) {
                	  sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.GREEN + "You have mastered " + skill.getName() + " at level " + hst.getSkillLevel(hero, skill) + "!");
                  } else {
                	  sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + skill.getName() + " leveled up: " + hst.getSkillLevel(hero, skill) + "/" + hst.getSkillMaxLevel(hero, skill));
                  }
               }
            }
         } else {
            sender.sendMessage(ChatColor.RED + "You don\'t have this skill");
         }
      }
   }
   
   private static boolean testRequirements(CommandSender sender) {
 	  Module module = WAddonCore.getInstance().getModuleManager();
 	  for (String req : module.customRequirementsHM.keySet()){
 		  Requirement found = null;              
 		  for (Requirement cr : module.getModules()){
 			  if (cr.getName().equalsIgnoreCase(req)){
 				  found = cr;
 				  break;
 			  }
 		  }
          if(found != null){
  			 if(found.isRequirementPassed((Player)sender, module.customRequirementsHM.get(req)) == false) {
  				 return false;
  			 }
          } else {
 			  WAddonCore.Log.warning("[WAddonCore] Player " + sender + " attempted to upgrade SkillTree but the Requirement could not be found. Does it still exist?");
 			  continue;
 		  }
 	  }
 	  return true;
   }
   
   private static void getCustomRequirements(CommandSender name, String[] args) {
	   Module module = WAddonCore.getInstance().getModuleManager();
	   Hero hero = WAddonCore.heroes.getCharacterManager().getHero((Player)name);
	   HeroClass gc = hero.getHeroClass();
       Skill skill = WAddonCore.heroes.getSkillManager().getSkill(args[1]);
	   Map<String, Object> data = new HashMap<String, Object>();
	   
	   
	   File file = new File(WAddonCore.heroes.getDataFolder() + "/classes/", gc.toString() + ".yml");
	   //FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
	   
	   FileConfiguration conf = new YamlConfiguration();
	   try {
		   conf.load(file);
	   } catch (IOException | InvalidConfigurationException e) {
		   e.printStackTrace();
	   }
	   
	   ConfigurationSection sec = conf.getConfigurationSection("permitted-skills." + skill.getName() + ".requirements");
	   WAddonCore.Log.warning(file.toString());
	   WAddonCore.Log.warning(conf.toString());
	   WAddonCore.Log.warning(sec.toString());
//	   try {
		   for (String path : sec.getKeys(false)){
			   ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
			   if (sec2 != null){
				   for (String dataPath : sec2.getKeys(false)){
					   data.put(dataPath, sec2.get(dataPath));                  
				   }
			   }
			   module.customRequirementsHM.put(name.getName(), data);                          
		   }
//	   } catch (NullPointerException e) {
//		   conf.createSection("permitted-skills." + skill + ".requirements");
//	   }
   }
}
