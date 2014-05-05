package me.Whatshiywl.heroesskilltree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import me.Wiedzmin137.wheroesaddon.WAddonCore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

public class HeroesSkillTree implements Listener {
   public HashMap<String, FileConfiguration> hConfigs = new LinkedHashMap<String, FileConfiguration>();
   
   public HashMap<String, HashMap<String, HashMap<String, Integer>>> playerSkills 
  	= new LinkedHashMap<String, HashMap<String, HashMap<String, Integer>>>();
   public HashMap<String, HashMap<String, Integer>> playerClasses = new LinkedHashMap<String, HashMap<String, Integer>>();

   public List<Skill> SkillStrongParents = new ArrayList<Skill>();
   public List<Skill> SkillWeakParents = new ArrayList<Skill>();
   
   //FIXME error on /hero reset (can't delete player.yml by WHA)
   
   public void loadPlayerConfig(String name) {
	   FileConfiguration playerConfig = new YamlConfiguration();
	   File playerFolder = new File(WAddonCore.getInstance().getDataFolder(), "data");
	   if (!playerFolder.exists()) {
		   playerFolder.mkdir();
	   }
	   File playerConfigFile = new File(playerFolder, name + ".yml");
	   if (!playerConfigFile.exists()) {
		   try {
			   playerConfigFile.createNewFile();
		   }
		   catch (IOException ex) {
			   WAddonCore.Log.severe("[WHeroesAddon] Failed to create new" + name + ".yml");
			   return;
		   }
	   }
	   try {
		   playerConfig.load(playerConfigFile);
		   if (!playerClasses.containsKey(name)) {
			   playerClasses.put(name, new HashMap<String, Integer>());
		   }
		   for (String s : playerConfig.getKeys(false)) {
			   playerClasses.get(name).put(s, Integer.valueOf(playerConfig.getInt(s + ".points", 0)));
			   if (!playerSkills.containsKey(s)) {
				   playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
			   }
			   if (!playerSkills.get(name).containsKey(s)) {
	    	    	playerSkills.get(name).put(s, new HashMap<String, Integer>());
			   }
			   if (playerConfig.getConfigurationSection(s + ".skills") != null) {
				   for (String st : playerConfig.getConfigurationSection(s + ".skills").getKeys(false)) {
					   playerSkills.get(name).get(s).put(st, Integer.valueOf(playerConfig.getInt(s + ".skills." + st, 0)));
				   }
			   }
		   }
	   }
	   catch (Exception e) {
		   WAddonCore.Log.severe("[WHeroesAddon] failed to load " + name + ".yml");
	   }
   }
   
   public void resetPlayerSkillTree(String name) {
	   File playerFolder = new File(WAddonCore.getInstance().getDataFolder(), "data");
	   if (!playerFolder.exists()) {
		   playerFolder.mkdir();
	   }
	   File playerFile = new File(playerFolder, name + ".yml");
	   if (playerFolder.exists() && !playerFolder.delete()) {
		   WAddonCore.Log.severe("[WHeroesAddon] Failed to delete new" + name + ".yml");
		   return;
	   }
	   try {
		   playerFile.createNewFile();
	   } catch (IOException e) {
		   WAddonCore.Log.severe("[WHeroesAddon] Failed to create new" + name + ".yml");
	   }
   }
   
   //TODO finish cleaning it
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
		   playerSkills.get(name).put(className, new HashMap<String, Integer>());
		   return;
	   }
	   for (Skill skill : WAddonCore.heroes.getSkillManager().getSkills()) {
		   String skillName = skill.getName();
		   if (playerSkills.get(name).get(className).get(skillName) != null) {
			   points -= playerSkills.get(name).get(className).get(skillName).intValue();
			   if (points < 0) {
				   WAddonCore.Log.warning("[HeroesSkillTree] " + name + "'s skills are at a too high level!");
				   points = 0;
			   }
		   }
	   }
	   playerClasses.get(name).put(className, Integer.valueOf(points));
   }
   
	public void resetPlayer(Player player) {
		//FIXME error on /Hero reset
		String name = player.getName();
		playerSkills.put(name, playerClasses);
		playerClasses.put(name, new HashMap<String, Integer>());
		resetPlayerSkillTree(name);
	} 

   public boolean isLocked(Hero hero, Skill skill) {
	   if (skill != null && hero.canUseSkill(skill)) {
		   List<String> strongParents = getStrongParentSkills(hero, skill);
		   List<String> weakParents = getWeakParentSkills(hero, skill);
		   boolean skillLevel = getSkillLevel(hero, skill) < 1;
		   boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
		   boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
		   
		   return skillLevel && hasStrongParents || hasWeakParents;
	   } else {
		   return true;
	   }
   }

   public boolean isMastered(Hero hero, Skill skill) {
	   return hero.hasAccessToSkill(skill)
			   ? getSkillLevel(hero, skill) >= getSkillMaxLevel(hero, skill) : false;
   }
   
   public boolean canUnlock(Hero hero, Skill skill) {
	   if (!hero.hasAccessToSkill(skill) || !hero.canUseSkill(skill)) {
		   return false;
	   }
	   List<String> strongParents = getStrongParentSkills(hero, skill);
	   boolean hasStrongParents = (strongParents != null) && (!strongParents.isEmpty());
	   List<String> weakParents = getWeakParentSkills(hero, skill);
	   boolean hasWeakParents = (weakParents != null) && (!weakParents.isEmpty());
	   if (!hasStrongParents && !hasWeakParents) {
		   return true;
	   }
	   if (hasStrongParents) {
		   for (String name : getStrongParentSkills(hero, skill)) {
			   if (!isMastered(hero, WAddonCore.heroes.getSkillManager().getSkill(name))) {
 	         return false;
			   }
		   }
	   }
	   if (hasWeakParents) {
		   for (String name : getWeakParentSkills(hero, skill)) {
			   if (isMastered(hero, WAddonCore.heroes.getSkillManager().getSkill(name))) {
				   return true;
			   }
		   }
		   return false;
	   }
	   return true;
   }
   
   public void setSkillLevel(Hero hero, Skill skill, int i) {
	   if(playerSkills.get(hero.getPlayer().getName()) == null) {
		   playerSkills.put(hero.getPlayer().getName(), new HashMap<String, HashMap<String, Integer>>());
	   }
	   
	   if(playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()) == null) {
		   playerSkills.get(hero.getPlayer().getName()).put(hero.getHeroClass().getName(), new HashMap<String, Integer>());
	   }

	   playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName())
	   		.put(skill.getName(), Integer.valueOf(i));
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
   
   //TODO clean up this mess
   public int getSkillLevel(Hero hero, Skill skill) {
	   return playerSkills.get(hero.getPlayer().getName())
    		  != null && playerSkills
    		  	      .get(hero.getPlayer().getName())
    		  		  .get(hero.getHeroClass().getName())
    		  != null && playerSkills
    				  .get(hero.getPlayer().getName())
    				  .get(hero.getHeroClass().getName())
    				  .get(skill.getName())
    	      != null ? playerSkills
    	    		  .get(hero.getPlayer().getName())
    	    		  .get(hero.getHeroClass().getName())
    	    		  .get(skill.getName()).intValue() : 0;
   }
   
   public int getPlayerPoints(Hero hero) {
	   if (hero.getPlayer().hasPermission("skilltree.override.usepoints")) {
		   return -1;
	   } else {
		   return playerClasses.get(hero.getPlayer().getName()) != null && playerClasses
				   .get(hero.getPlayer().getName())
				   .get(hero.getHeroClass().getName()) != null ? playerClasses
						   .get(hero.getPlayer().getName())
						   .get(hero.getHeroClass().getName()).intValue() : 0;
	   }
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
	   return (hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents") == null) ? null 
			   : hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents")
			   			 .getStringList(weakOrStrong);
   }
   
   public FileConfiguration getHeroesClassConfig(HeroClass hClass) {
	   if(hConfigs.containsKey(hClass.getName())) {
		   return hConfigs.get(hClass.getName());
	   }
	   File classFolder = new File(WAddonCore.heroes.getDataFolder(), "classes");
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
		   catch (Exception localException) {
			   WAddonCore.Log.warning("[WHeroesAddon] Failed try to load HeroClasses from Heroes plugin.");
		   }
	   }
	   return null;
   }
}
