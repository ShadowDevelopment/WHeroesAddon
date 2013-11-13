package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillLockedCommand {

   public static void skillList(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
      } else {
         Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
         int j = 0;
         HashMap skills = new HashMap();
         ArrayList alphabeticalSkills = new ArrayList();
         String name;
         if(hero.getHeroClass() != null) {
            Iterator t = hero.getHeroClass().getSkillNames().iterator();

            while(t.hasNext()) {
               String k = (String)t.next();
               Skill i = HeroesSkillTree.heroes.getSkillManager().getSkill(k);
               if(i != null && shouldListSkill(hst, hero, i)) {
                  name = ChatColor.GREEN + k + ChatColor.GRAY;
                  String s;
                  Iterator var12;
                  if(hst.getStrongParentSkills(hero, i) != null) {
                     for(var12 = hst.getStrongParentSkills(hero, i).iterator(); var12.hasNext(); name = name + ", s:" + s) {
                        s = (String)var12.next();
                     }
                  }

                  if(hst.getWeakParentSkills(hero, i) != null) {
                     for(var12 = hst.getWeakParentSkills(hero, i).iterator(); var12.hasNext(); name = name + ", w:" + s) {
                        s = (String)var12.next();
                     }
                  }

                  skills.put(k, i);
                  alphabeticalSkills.add(name);
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
            name = (String)alphabeticalSkills.get(var16);
            sender.sendMessage(name);
            ++j;
         }

      }
   }

   private static boolean shouldListSkill(HeroesSkillTree hst, Hero hero, Skill skill) {
      if(skill == null) {
         return false;
      } else if(!hst.isLocked(hero, skill)) {
         return false;
      } else if(hero.hasAccessToSkill(skill) && hero.canUseSkill(skill)) {
         List strongParents = hst.getStrongParentSkills(hero, skill);
         boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
         List weakParents = hst.getWeakParentSkills(hero, skill);
         boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
         if(!hasStrongParents && !hasWeakParents) {
            return false;
         } else {
            String name;
            Iterator var8;
            if(hasStrongParents) {
               var8 = hst.getStrongParentSkills(hero, skill).iterator();

               while(var8.hasNext()) {
                  name = (String)var8.next();
                  if(hst.isLocked(hero, HeroesSkillTree.heroes.getSkillManager().getSkill(name))) {
                     return false;
                  }
               }
            }

            if(!hasWeakParents) {
               return true;
            } else {
               var8 = hst.getWeakParentSkills(hero, skill).iterator();

               while(var8.hasNext()) {
                  name = (String)var8.next();
                  if(!hst.isLocked(hero, HeroesSkillTree.heroes.getSkillManager().getSkill(name))) {
                     return true;
                  }
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }
}
