package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillListCommand {

   public static void skillList(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
      } else {
         Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
         int j = 0;
         HashMap skills = new HashMap();
         ArrayList alphabeticalSkills = new ArrayList();
         if(hero.getHeroClass() != null) {
            Iterator t = hero.getHeroClass().getSkillNames().iterator();

            while(t.hasNext()) {
               String k = (String)t.next();
               Skill i = HeroesSkillTree.heroes.getSkillManager().getSkill(k);
               if(!hst.isLocked(hero, i) && hero.canUseSkill(i)) {
                  skills.put(k, i);
                  alphabeticalSkills.add(k);
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

         sender.sendMessage(ChatColor.GOLD + "[HST] Unlocked skills list page " + var15 + "/" + Math.round((double)alphabeticalSkills.size() / 10.0D));

         for(int var16 = var14; j < 10 && var16 < alphabeticalSkills.size() && j <= 9; ++var16) {
            String name = (String)alphabeticalSkills.get(var16);
            int maxlevel = hst.getSkillMaxLevel(hero, (Skill)skills.get(name));
            if(maxlevel >= 0) {
               int level = hst.getSkillLevel(hero, (Skill)skills.get(name));
               sender.sendMessage(ChatColor.GREEN + name + " (" + level + "/" + maxlevel + "): " + ChatColor.GRAY + ((Skill)skills.get(name)).getDescription(hero));
            } else {
               sender.sendMessage(ChatColor.GREEN + name + ": " + ChatColor.GRAY + ((Skill)skills.get(name)).getDescription(hero));
            }

            ++j;
         }

      }
   }
}
