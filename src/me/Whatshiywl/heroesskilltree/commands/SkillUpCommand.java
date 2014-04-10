package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.skill.Skill;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.WAddonCore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillUpCommand {
   //TODO translate messages

   public static void skillUp(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(!sender.hasPermission("skilltree.up")) {
         sender.sendMessage(ChatColor.RED + "You don\'t have enough permissions!");
      } else if(args.length < 2) {
         sender.sendMessage(ChatColor.RED + "No skill given: /skillup (skill) [amount]");
      } else if(!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
      } else {
         Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
         Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(args[1]);
         if(skill != null && hero.hasAccessToSkill(skill.getName())) {
            if(hst.getSkillMaxLevel(hero, skill) == -1) {
               sender.sendMessage(ChatColor.RED + "This skill can\'t be increased");
            } else {
               int pointsToIncrease;
               try {
                  pointsToIncrease = (args.length > 2) ? Integer.parseInt(args[2]) : 1;
               } catch (NumberFormatException var7) {
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
                  if(!sender.hasPermission("skilltree.override.usepoints")) {
                     hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) - pointsToIncrease);
                  }

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
}
