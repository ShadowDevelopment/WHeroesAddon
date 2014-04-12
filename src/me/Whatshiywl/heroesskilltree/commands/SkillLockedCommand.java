package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.WAddonCore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillLockedCommand {
   //TODO create lanugage support
   public static void skillList(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
         return;
      } else {
         Hero hero = WAddonCore.heroes.getCharacterManager().getHero((Player)sender);
         int j = 0;
         HashMap<String, Skill> skills = new HashMap<String, Skill>();
         ArrayList<String> alphabeticalSkills = new ArrayList<String>();
         String name;
         if (hero.getHeroClass() != null) {
             for (String skillName : hero.getHeroClass().getSkillNames()) {
               Skill skill = WAddonCore.heroes.getSkillManager().getSkill(skillName);
               if ((skill != null) && (shouldListSkill(hst, hero, skill))) {
                 String message = ChatColor.GREEN + skillName + ChatColor.GRAY;
                 if (hst.getStrongParentSkills(hero, skill) != null) {
                   for (String s : hst.getStrongParentSkills(hero, skill)) {
                     message = message + ", s:" + s;
                   }
                 }
                 if (hst.getWeakParentSkills(hero, skill) != null) {
                   for (String s : hst.getWeakParentSkills(hero, skill)) {
                     message = message + ", w:" + s;
                   }
                 }
                 skills.put(skillName, skill);
                 alphabeticalSkills.add(message);
               }
             }
           }

         Collections.sort(alphabeticalSkills);
         int k = 0;
         int t = 0;
         if(args.length > 1 && !args[1].equalsIgnoreCase("1")) {
            try {
               t = Integer.parseInt(args[1]);
               t = (t < 2) ? 1 : t;
               k = (t - 1) * 10;
            } catch (NumberFormatException e) {
               k = 0;
            }
         } else {
            t = 1;
         }

         sender.sendMessage(ChatColor.GOLD + "[HST] Unlockable skills list page " + t + "/" + Math.round((double)alphabeticalSkills.size() / 10.0D));

         for(int i = k; j < 10 && i < alphabeticalSkills.size() && j <= 9; ++i) {
            name = alphabeticalSkills.get(i);
            sender.sendMessage(name);
            ++j;
         }

      }
   }

   private static boolean shouldListSkill(HeroesSkillTree hst, Hero hero, Skill skill) {
     if (skill == null) {
       return false;
     }
     if (!hst.isLocked(hero, skill)) {
       return false;
     }
     if ((!hero.hasAccessToSkill(skill)) || (!hero.canUseSkill(skill))) {
       return false;
     }
     List<String> strongParents = hst.getStrongParentSkills(hero, skill);
     boolean hasStrongParents = (strongParents != null) && (!strongParents.isEmpty());
     List<String> weakParents = hst.getWeakParentSkills(hero, skill);
     boolean hasWeakParents = (weakParents != null) && (!weakParents.isEmpty());
     if ((!hasStrongParents) && (!hasWeakParents)) {
       return false;
     }
     if (hasStrongParents) {
       for (String name : hst.getStrongParentSkills(hero, skill)) {
         if (hst.isLocked(hero, WAddonCore.heroes.getSkillManager().getSkill(name))) {
           return false;
         }
       }
     }
     if (hasWeakParents) {
       for (String name : hst.getWeakParentSkills(hero, skill)) {
         if (!hst.isLocked(hero, WAddonCore.heroes.getSkillManager().getSkill(name))) {
           return true;
         }
       }
       return false;
     }
     return true;
   }
 }
