package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.WAddonCore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillDownCommand {
   //TODO create lanugage support
   public static void skillDown(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(!sender.hasPermission("skilltree.down")) {
         sender.sendMessage(ChatColor.RED + "You don\'t have enough permissions!");
      } else if(args.length < 2) {
         sender.sendMessage(ChatColor.RED + "No skill given: /skilldown (skill) [amount]");
      } else if(!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
      } else {
         Hero hero = WAddonCore.heroes.getCharacterManager().getHero((Player)sender);
         if(!hero.hasAccessToSkill(args[1])) {
            sender.sendMessage(ChatColor.RED + "You don\'t have this skill");
         } else {
            Skill skill = WAddonCore.heroes.getSkillManager().getSkill(args[1]);
            int pointsDecrease;
            try {
               pointsDecrease = (args.length > 2) ? Integer.parseInt(args[2]) : 1;
            } catch (NumberFormatException var7) {
               sender.sendMessage(ChatColor.RED + "Please enter a number of points to increase");
               return;
            }

            if(hst.getSkillLevel(hero, skill) < pointsDecrease) {
               sender.sendMessage(ChatColor.RED + "This skill is not a high enough level");
            } else {
               if(hst.getSkillLevel(hero, skill) - pointsDecrease < 2) {
                  if(!sender.hasPermission("skilltree.lock")) {
                     sender.sendMessage(ChatColor.RED + "You don\'t have enough permissions!");
                     return;
                  }

                  if(!sender.hasPermission("skilltree.override.usepoints")) {
                     hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) + pointsDecrease);
                  }

                  hst.setSkillLevel(hero, skill, hst.getSkillLevel(hero, skill) - pointsDecrease);
                  hero.removeEffect(hero.getEffect(skill.getName()));
                  WAddonCore.getInstance().savePlayerConfig(sender.getName());
                  sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You have locked " + skill.getName() + "!");
               } else {
                  if(!sender.hasPermission("skilltree.down")) {
                     sender.sendMessage(ChatColor.RED + "You don\'t have enough permissions!");
                     return;
                  }

                  if(!sender.hasPermission("skilltree.override.usepoints")) {
                     hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) + pointsDecrease);
                  }

                  hst.setSkillLevel(hero, skill, hst.getSkillLevel(hero, skill) - pointsDecrease);
                  WAddonCore.getInstance().savePlayerConfig(sender.getName());
                  sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + skill.getName() + "leveled down: " + hst.getSkillLevel(hero, skill) + "/" + hst.getSkillMaxLevel(hero, skill));
               }

            }
         }
      }
   }
}
