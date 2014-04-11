package me.Whatshiywl.heroesskilltree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import me.Wiedzmin137.wheroesaddon.Lang;
import me.Wiedzmin137.wheroesaddon.WAddonCore;
import me.Wiedzmin137.wheroesaddon.addons.Hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class HeroesSkillTree implements Listener {
   private HashMap<String, FileConfiguration> hConfigs = new LinkedHashMap<String, FileConfiguration>();
   
   public HashMap<String, HashMap<String, HashMap<String, Integer>>> playerSkills 
  	= new LinkedHashMap<String, HashMap<String, HashMap<String, Integer>>>();
   public HashMap<String, HashMap<String, Integer>> playerClasses = new LinkedHashMap<String, HashMap<String, Integer>>();
   
   public static Logger Logger;
   public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");

   public List<Skill> SkillStrongParents = new ArrayList<Skill>();
   public List<Skill> SkillWeakParents = new ArrayList<Skill>();
   
   //FIXME error on /hero reset (can't delete player.yml by WHA)

   public void resetPlayer(Player player) {
	  //FIXME error on /Hero reset
      String name = player.getName();
      playerSkills.put(name, playerClasses);
      playerClasses.put(name, new HashMap<String, Integer>());
      resetPlayerConfig(name);
   } 
   
   private void resetPlayerConfig(String name) {
	 //FIXME error on /Hero reset
     File playerFolder = new File(WAddonCore.getInstance().getDataFolder(), "data");
     if (!playerFolder.exists()) {
       playerFolder.mkdir();
     }
     File playerFile = new File(playerFolder, name + ".yml");
     if ((playerFolder.exists()) && (!playerFolder.delete())) {
       Logger.severe(Lang.SERVRE_FAILED_DELETE.toString().replace("%name%", name));
       return;
     }
     try {
       playerFile.createNewFile();
     } catch (IOException e) {
       Logger.severe(Lang.SERVRE_FAILED_CREATE.toString().replace("%name%", name));
     }
   }
   
   public int getPlayerPoints(Hero hero) {
	   return playerClasses.get(hero.getPlayer().getName()) != null && playerClasses
			   .get(hero.getPlayer().getName())
			   .get(hero.getHeroClass().getName()) != null ? ((Integer)playerClasses
					   .get(hero.getPlayer().getName())
					   .get(hero.getHeroClass().getName())).intValue() : 0;
   }
   
   //FIXME I think it doesn't work properly, I used this SupressWarnings but problems are somewhere in code
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void recalcPlayerPoints(Hero hero, HeroClass hClass) {
	   String name = hero.getPlayer().getName();
	   String className = hClass.getName();
	   int points = hero.getLevel(hClass) * WAddonCore.getInstance().getPointsPerLevel();
	   if (playerClasses.get(name) == null) {
		   playerClasses.put(name, new HashMap<String, Integer>());
	   }
	   if (hero.getPlayer().hasPermission("skilltree.override.usepoints")) {
		   playerClasses.get(name).put(className, Integer.valueOf(points));
		   return;
	   	}
	   if (playerClasses.get(name).get(className) == null) {
		   playerClasses.get(name).put(className, Integer.valueOf(0));
		   return;
	   }
	   if (playerSkills.get(name) == null) {
		   playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
		   return;
	   }
	   if (playerSkills.get(name).get(className) == null) {
		   playerSkills.get(name).put(className, new HashMap());
		   return;
	   }
	   for (Skill skill : heroes.getSkillManager().getSkills()) {
		   String skillName = skill.getName();
		   if (((HashMap<?, ?>)playerSkills.get(name).get(className)).get(skillName) != null) {
			   points -= ((Integer)((HashMap<?, ?>)playerSkills.get(name)
					   .get(className))
					   .get(skillName)).intValue();
			   if (points < 0) {
				   Logger.warning("[HeroesSkillTree] " + name + "'s skills are at a too high level!");
				   points = 0;
			   }
		   }
	   }
	   playerClasses.get(name).put(className, Integer.valueOf(points));
   }
		
   public void setPlayerPoints(Hero hero, int i) {
	   if(playerClasses.get(hero.getPlayer().getName()) == null) {
		   playerClasses.put(hero.getPlayer().getName(), new HashMap<String, Integer>());
	   }
	   playerClasses.get(hero.getPlayer().getName())
	   	.put(hero.getHeroClass().getName(), Integer.valueOf(i));
   }
   
   public void setPlayerPoints(Hero hero, HeroClass hClass, int i) {
	   if(playerClasses.get(hero.getPlayer().getName()) == null) {
		   playerClasses.put(hero.getPlayer().getName(), new HashMap<String, Integer>());
	   }
	   playerClasses.get(hero.getPlayer().getName()).put(hClass.getName(), Integer.valueOf(i));
   }

   //FIXME this probably not good like up
   @SuppressWarnings("rawtypes")
   public int getSkillLevel(Hero hero, Skill skill) {
      return playerSkills.get(hero.getPlayer().getName())
    		  != null && playerSkills
    		  	      .get(hero.getPlayer().getName())
    		  		  .get(hero.getHeroClass().getName())
    		  != null && ((HashMap)playerSkills
    				  .get(hero.getPlayer().getName())
    				  .get(hero.getHeroClass().getName()))
    				  .get(skill.getName())
    	      != null ? ((Integer)((HashMap)playerSkills
    	    		  .get(hero.getPlayer().getName())
    	    		  .get(hero.getHeroClass().getName()))
    	    		  .get(skill.getName())).intValue():0;
   }

   public void setSkillLevel(Hero hero, Skill skill, int i) {
      if(playerSkills.get(hero.getPlayer().getName()) == null) {
         playerSkills.put(hero.getPlayer().getName(), new HashMap<String, HashMap<String, Integer>>());
      }

      if(playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()) == null) {
         playerSkills.get(hero.getPlayer().getName()).put(hero.getHeroClass().getName(), new HashMap<String, Integer>());
      }

      ((HashMap<String, Integer>)playerSkills
    		  .get(hero.getPlayer().getName())
    		  .get(hero.getHeroClass().getName()))
    		  .put(skill.getName(), Integer.valueOf(i));
   }

   public int getSkillMaxLevel(Hero hero, Skill skill) {
      return SkillConfigManager.getSetting(hero.getHeroClass(), skill, "max-level", -1) == -1 ? 
    		 SkillConfigManager.getUseSetting(hero, skill, "max-level", -1, false) : 
    	     SkillConfigManager.getSetting(hero.getHeroClass(), skill, "max-level", -1);
   }

   public List<String> getStrongParentSkills(Hero hero, Skill skill) {
      return getParentSkills(hero, skill, "strong");
   }

   public List<String> getWeakParentSkills(Hero hero, Skill skill) {
      return getParentSkills(hero, skill, "weak");
   }

   public List<String> getParentSkills(Hero hero, Skill skill, String weakOrStrong) {
      FileConfiguration hCConfig = getHeroesClassConfig(hero.getHeroClass());
      return hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents") 
    		  == null ? null : hCConfig
    				  .getConfigurationSection("permitted-skills." + skill.getName() + ".parents")
    				  .getStringList(weakOrStrong);
   }

   public boolean isLocked(Hero hero, Skill skill) {
      if(skill != null && hero.canUseSkill(skill)) {
         boolean skillLevel = getSkillLevel(hero, skill) < 1;
         List<String> strongParents = getStrongParentSkills(hero, skill);
         boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
         List<String> weakParents = getWeakParentSkills(hero, skill);
         boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
         return skillLevel && (hasStrongParents || hasWeakParents);
      } else {
         return true;
      }
   }

   public boolean isMastered(Hero hero, Skill skill) {
      return hero.hasAccessToSkill(skill)
    		  ? getSkillLevel(hero, skill) >= getSkillMaxLevel(hero, skill) : false;
   }

   public boolean canUnlock(Hero hero, Skill skill) {
     if ((!hero.hasAccessToSkill(skill)) || (!hero.canUseSkill(skill))) {
       return false;
     }
     List<String> strongParents = getStrongParentSkills(hero, skill);
     boolean hasStrongParents = (strongParents != null) && (!strongParents.isEmpty());
     List<String> weakParents = getWeakParentSkills(hero, skill);
     boolean hasWeakParents = (weakParents != null) && (!weakParents.isEmpty());
     if ((!hasStrongParents) && (!hasWeakParents)) {
       return true;
     }
     if (hasStrongParents) {
       for (String name : getStrongParentSkills(hero, skill)) {
         if (!isMastered(hero, heroes.getSkillManager().getSkill(name))) {
           return false;
         }
       }
     }
     if (hasWeakParents) {
       for (String name : getWeakParentSkills(hero, skill)) {
         if (isMastered(hero, heroes.getSkillManager().getSkill(name))) {
           return true;
         }
       }
       return false;
     }
     return true;
   }
   
   public FileConfiguration getHeroesClassConfig(HeroClass hClass) {
      if(hConfigs.containsKey(hClass.getName())) {
         return hConfigs.get(hClass.getName());
      }
      File classFolder = new File(heroes.getDataFolder(), "classes");
      for (File f : classFolder.listFiles()) {
        FileConfiguration config = new YamlConfiguration();
        try {
          config.load(f);
          String currentClassName = config.getString("name");
          if (currentClassName.equalsIgnoreCase(hClass.getName())) {
            hConfigs.put(hClass.getName(), config);
            return config;
          }
          if (!hConfigs.containsKey(currentClassName)) {
            hConfigs.put(currentClassName, config);
          }
        }
        catch (Exception localException) {}
      }
      return null;
    }
   
   public static void expMessage(Player p, Location loc, double gained, double needed, double current) {
	   if(gained == 0) { return; }
	   final Hologram holo = new Hologram(
		 Lang.HOLOGRAM_MESSAGE_EXP_GAINED.toString().replace("%gained%", String.valueOf(gained)),
		 Lang.HOLOGRAM_MESSAGE_EXP_MAX.toString()
		   .replace("%current%", String.valueOf(current))
		   .replace("%needed%", String.valueOf(needed)));
	   holo.show(p, loc);
	   Bukkit.getScheduler().scheduleSyncDelayedTask(WAddonCore.getInstance(), new BukkitRunnable() {
		   @Override
		   public void run() {
			   holo.destroy();
		   }
	   }, WAddonCore.getInstance().getHologramTime());
   }
}
