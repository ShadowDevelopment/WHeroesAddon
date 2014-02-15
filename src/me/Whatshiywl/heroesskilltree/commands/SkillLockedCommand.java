package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillLockedCommand {
   //TODO create lanugage support
   public static void skillList(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
      } else {
         Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
         int j = 0;
         HashMap<String, Skill> skills = new HashMap<String, Skill>();
         ArrayList<String> alphabeticalSkills = new ArrayList<String>();
         String name;
         if (hero.getHeroClass() != null) {
             for (String skillName : hero.getHeroClass().getSkillNames())
             {
               Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(skillName);
               if ((skill != null) && (shouldListSkill(hst, hero, skill)))
               {
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
         int var14 = 0;
         int var15 = 0;
         if(args.length > 0 && !args[0].equalsIgnoreCase("1")) {
            try {
               var15 = Integer.parseInt(args[0]);
               var15 = var15 < 2?1:var15;
               var14 = (var15 - 1) * 10;
            } catch (NumberFormatException var13) {
               var14 = 0;
            }
         } else {
            var15 = 1;
         }

         sender.sendMessage(ChatColor.GOLD + "[HST] Unlockable skills list page " + var15 + "/" + Math.round((double)alphabeticalSkills.size() / 10.0D));

         for(int var16 = var14; j < 10 && var16 < alphabeticalSkills.size() && j <= 9; ++var16) {
            name = alphabeticalSkills.get(var16);
            sender.sendMessage(name);
            ++j;
         }

      }
   }

   private static boolean shouldListSkill(HeroesSkillTree hst, Hero hero, Skill skill)
   {
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
         if (hst.isLocked(hero, HeroesSkillTree.heroes.getSkillManager().getSkill(name))) {
           return false;
         }
       }
     }
     if (hasWeakParents)
     {
       for (String name : hst.getWeakParentSkills(hero, skill)) {
         if (!hst.isLocked(hero, HeroesSkillTree.heroes.getSkillManager().getSkill(name))) {
           return true;
         }
       }
       return false;
     }
     return true;
   }
 }
