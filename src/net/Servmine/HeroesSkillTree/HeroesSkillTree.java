package net.Servmine.HeroesSkillTree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.Servmine.HeroesSkillTree.EventListener;
import net.Servmine.HeroesSkillTree.commands.SkillAdminCommand;
import net.Servmine.HeroesSkillTree.commands.SkillDownCommand;
import net.Servmine.HeroesSkillTree.commands.SkillInfoCommand;
import net.Servmine.HeroesSkillTree.commands.SkillListCommand;
import net.Servmine.HeroesSkillTree.commands.SkillLockedCommand;
import net.Servmine.HeroesSkillTree.commands.SkillUpCommand;
import net.Servmine.HeroesSkillTree.language.LangList;
import net.Servmine.HeroesSkillTree.language.LangSender;
import net.Servmine.HeroesSkillTree.language.UtilTest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroesSkillTree extends JavaPlugin {

   public final int VERSION = 1;
   public final double SUBVERSION = 5.2D;
   public static final Logger logger = Logger.getLogger("Minecraft");
   public final EventListener HEventListener = new EventListener(this);
   public HeroesSkillTree plugin;
   public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
   public List SkillStrongParents = new ArrayList();
   public List SkillWeakParents = new ArrayList();
   private HashMap playerSkills = new HashMap();
   private HashMap playerClasses = new HashMap();
   private int pointsPerLevel = 1;
   private HashMap hConfigs = new HashMap();


   public void onDisable() {
      this.saveAll();
      logger.info("[HeroesSkillTree] Has Been Disabled!");
   }

   public void onEnable() {
      PluginManager pm = this.getServer().getPluginManager();
      this.getConfig().options().copyDefaults(true);
      this.saveConfig();
      this.loadConfig();
      this.loadLanguageFile();
      pm.registerEvents(this.HEventListener, this);
      LangSender.info("CONSOLE_ENABLING");
      Player[] var6;
      int var5 = (var6 = Bukkit.getServer().getOnlinePlayers()).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Player player = var6[var4];
         Hero hero = heroes.getCharacterManager().getHero(player);
         this.recalcPlayerPoints(hero, hero.getHeroClass());
      }

   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      if(commandLabel.equalsIgnoreCase("skillup")) {
         SkillUpCommand.skillUp(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skilldown")) {
         SkillDownCommand.skillDown(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skillinfo")) {
         SkillInfoCommand.skillInfo(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skillpoints")) {
         if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
            return true;
         } else {
            Hero hero = heroes.getCharacterManager().getHero((Player)sender);
            if(sender.hasPermission("skilltree.points")) {
               sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You currently have " + this.getPlayerPoints(hero) + " SkillPoints.");
            } else {
               sender.sendMessage(ChatColor.RED + "You don\'t have enough permissions!");
            }

            return true;
         }
      } else if(commandLabel.equalsIgnoreCase("skilladmin")) {
         SkillAdminCommand.skillAdmin(this, sender, args);
         return true;
      } else if(!commandLabel.equalsIgnoreCase("slist") && !commandLabel.equalsIgnoreCase("sl")) {
         if(!commandLabel.equalsIgnoreCase("unlocks") && !commandLabel.equalsIgnoreCase("un")) {
            sender.sendMessage(ChatColor.GOLD + "HeroesSkillTree Help Page:");
            sender.sendMessage(ChatColor.GRAY + "/skillup <skill> [amount] (level up a skill)");
            sender.sendMessage(ChatColor.GRAY + "/skilldown <skill> [amount] (de-levels a skill)");
            sender.sendMessage(ChatColor.GRAY + "/slist (lists all unlocked skills)");
            sender.sendMessage(ChatColor.GRAY + "/unlocks (lists all adjacent unlockable skills)");
            sender.sendMessage(ChatColor.GRAY + "/skillinfo <skill> (all info on a skill)");
            sender.sendMessage(ChatColor.GRAY + "/skilladmin <command> (amount) [player]");
            return true;
         } else {
            SkillLockedCommand.skillList(this, sender, args);
            return true;
         }
      } else {
         SkillListCommand.skillList(this, sender, args);
         return true;
      }
   }

   public void resetPlayer(Player player) {
      String name = player.getName();
      this.playerSkills.put(name, new HashMap());
      this.playerClasses.put(name, new HashMap());
      this.resetPlayerConfig(name);
   }

   private void resetPlayerConfig(String name) {
      File playerFolder = new File(this.getDataFolder(), "data");
      if(!playerFolder.exists()) {
         playerFolder.mkdir();
      }

      File playerFile = new File(playerFolder, name + ".yml");
      if(playerFolder.exists() && !playerFolder.delete()) {
         logger.log(Level.SEVERE, "[HeroesSkillTree] failed to delete " + name + ".yml");
      } else {
         try {
            playerFile.createNewFile();
         } catch (IOException var5) {
            logger.log(Level.SEVERE, "[HeroesSkillTree] failed to create new " + name + ".yml");
         }

      }
   }

   public int getPlayerPoints(Hero hero) {
      return this.playerClasses.get(hero.getPlayer().getName()) != null && ((HashMap)this.playerClasses.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName()) != null?((Integer)((HashMap)this.playerClasses.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName())).intValue():0;
   }

   public void recalcPlayerPoints(Hero hero, HeroClass hClass) {
      String name = hero.getPlayer().getName();
      String className = hClass.getName();
      int points = hero.getLevel(hClass) * this.getPointsPerLevel();
      if(this.playerClasses.get(name) == null) {
         this.playerClasses.put(name, new HashMap());
      }

      if(hero.getPlayer().hasPermission("skilltree.override.usepoints")) {
         ((HashMap)this.playerClasses.get(name)).put(className, Integer.valueOf(points));
      } else if(((HashMap)this.playerClasses.get(name)).get(className) == null) {
         ((HashMap)this.playerClasses.get(name)).put(className, Integer.valueOf(0));
      } else if(this.playerSkills.get(name) == null) {
         this.playerSkills.put(name, new HashMap());
      } else if(((HashMap)this.playerSkills.get(name)).get(className) == null) {
         ((HashMap)this.playerSkills.get(name)).put(className, new HashMap());
      } else {
         Iterator var7 = heroes.getSkillManager().getSkills().iterator();

         while(var7.hasNext()) {
            Skill skill = (Skill)var7.next();
            String skillName = skill.getName();
            if(((HashMap)((HashMap)this.playerSkills.get(name)).get(className)).get(skillName) != null) {
               points -= ((Integer)((HashMap)((HashMap)this.playerSkills.get(name)).get(className)).get(skillName)).intValue();
               if(points < 0) {
                  logger.warning("[HeroesSkillTree] " + name + "\'s skills are at a too high level!");
                  points = 0;
               }
            }
         }

         ((HashMap)this.playerClasses.get(name)).put(className, Integer.valueOf(points));
      }
   }

   public void setPlayerPoints(Hero hero, int i) {
      if(this.playerClasses.get(hero.getPlayer().getName()) == null) {
         this.playerClasses.put(hero.getPlayer().getName(), new HashMap());
      }

      ((HashMap)this.playerClasses.get(hero.getPlayer().getName())).put(hero.getHeroClass().getName(), Integer.valueOf(i));
   }

   public void setPlayerPoints(Hero hero, HeroClass hClass, int i) {
      if(this.playerClasses.get(hero.getPlayer().getName()) == null) {
         this.playerClasses.put(hero.getPlayer().getName(), new HashMap());
      }

      ((HashMap)this.playerClasses.get(hero.getPlayer().getName())).put(hClass.getName(), Integer.valueOf(i));
   }

   public int getSkillLevel(Hero hero, Skill skill) {
      return this.playerSkills.get(hero.getPlayer().getName()) != null && ((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName()) != null && ((HashMap)((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName())).get(skill.getName()) != null?((Integer)((HashMap)((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName())).get(skill.getName())).intValue():0;
   }

   public void setSkillLevel(Hero hero, Skill skill, int i) {
      if(this.playerSkills.get(hero.getPlayer().getName()) == null) {
         this.playerSkills.put(hero.getPlayer().getName(), new HashMap());
      }

      if(((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName()) == null) {
         ((HashMap)this.playerSkills.get(hero.getPlayer().getName())).put(hero.getHeroClass().getName(), new HashMap());
      }

      ((HashMap)((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName())).put(skill.getName(), Integer.valueOf(i));
   }

   public int getSkillMaxLevel(Hero hero, Skill skill) {
      return SkillConfigManager.getSetting(hero.getHeroClass(), skill, "max-level", -1) == -1?SkillConfigManager.getUseSetting(hero, skill, "max-level", -1, false):SkillConfigManager.getSetting(hero.getHeroClass(), skill, "max-level", -1);
   }

   public List getStrongParentSkills(Hero hero, Skill skill) {
      return this.getParentSkills(hero, skill, "strong");
   }

   public List getWeakParentSkills(Hero hero, Skill skill) {
      return this.getParentSkills(hero, skill, "weak");
   }

   public List getParentSkills(Hero hero, Skill skill, String weakOrStrong) {
      FileConfiguration hCConfig = this.getHeroesClassConfig(hero.getHeroClass());
      return hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents") == null?null:hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents").getStringList(weakOrStrong);
   }

   public boolean isLocked(Hero hero, Skill skill) {
      if(skill != null && hero.canUseSkill(skill)) {
         boolean skillLevel = this.getSkillLevel(hero, skill) < 1;
         List strongParents = this.getStrongParentSkills(hero, skill);
         boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
         List weakParents = this.getWeakParentSkills(hero, skill);
         boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
         return skillLevel && (hasStrongParents || hasWeakParents);
      } else {
         return true;
      }
   }

   public boolean isMastered(Hero hero, Skill skill) {
      return hero.hasAccessToSkill(skill)?this.getSkillLevel(hero, skill) >= this.getSkillMaxLevel(hero, skill):false;
   }

   public boolean canUnlock(Hero hero, Skill skill) {
      if(hero.hasAccessToSkill(skill) && hero.canUseSkill(skill)) {
         List strongParents = this.getStrongParentSkills(hero, skill);
         boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
         List weakParents = this.getWeakParentSkills(hero, skill);
         boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
         if(!hasStrongParents && !hasWeakParents) {
            return true;
         } else {
            String name;
            Iterator var8;
            if(hasStrongParents) {
               var8 = this.getStrongParentSkills(hero, skill).iterator();

               while(var8.hasNext()) {
                  name = (String)var8.next();
                  if(!this.isMastered(hero, heroes.getSkillManager().getSkill(name))) {
                     return false;
                  }
               }
            }

            if(!hasWeakParents) {
               return true;
            } else {
               var8 = this.getWeakParentSkills(hero, skill).iterator();

               while(var8.hasNext()) {
                  name = (String)var8.next();
                  if(this.isMastered(hero, heroes.getSkillManager().getSkill(name))) {
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

   public void loadPlayerConfig(String name) {
      YamlConfiguration playerConfig = new YamlConfiguration();
      File playerFolder = new File(this.getDataFolder(), "data");
      if(!playerFolder.exists()) {
         playerFolder.mkdir();
      }

      File playerConfigFile = new File(playerFolder, name + ".yml");
      if(!playerConfigFile.exists()) {
         try {
            playerConfigFile.createNewFile();
         } catch (IOException var9) {
            logger.severe("[HeroesSkillTree] failed to create new " + name + ".yml");
            return;
         }
      }

      try {
         playerConfig.load(playerConfigFile);
         if(!this.playerClasses.containsKey(name)) {
            this.playerClasses.put(name, new HashMap());
         }

         Iterator var6 = playerConfig.getKeys(false).iterator();

         while(var6.hasNext()) {
            String e = (String)var6.next();
            ((HashMap)this.playerClasses.get(name)).put(e, Integer.valueOf(playerConfig.getInt(e + ".points", 0)));
            if(!this.playerSkills.containsKey(e)) {
               this.playerSkills.put(name, new HashMap());
            }

            if(!((HashMap)this.playerSkills.get(name)).containsKey(e)) {
               ((HashMap)this.playerSkills.get(name)).put(e, new HashMap());
            }

            if(playerConfig.getConfigurationSection(e + ".skills") != null) {
               Iterator var8 = playerConfig.getConfigurationSection(e + ".skills").getKeys(false).iterator();

               while(var8.hasNext()) {
                  String st = (String)var8.next();
                  ((HashMap)((HashMap)this.playerSkills.get(name)).get(e)).put(st, Integer.valueOf(playerConfig.getInt(e + ".skills." + st, 0)));
               }
            }
         }
      } catch (Exception var10) {
         logger.severe("[HeroesSkillTree] failed to load " + name + ".yml");
      }

   }

   public FileConfiguration getHeroesClassConfig(HeroClass hClass) {
      if(this.hConfigs.containsKey(hClass.getName())) {
         return (FileConfiguration)this.hConfigs.get(hClass.getName());
      } else {
         File classFolder = new File(heroes.getDataFolder(), "classes");
         File[] var6;
         int var5 = (var6 = classFolder.listFiles()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            File f = var6[var4];
            YamlConfiguration config = new YamlConfiguration();

            try {
               config.load(f);
               String currentClassName = config.getString("name");
               if(currentClassName.equalsIgnoreCase(hClass.getName())) {
                  this.hConfigs.put(hClass.getName(), config);
                  return config;
               }

               if(!this.hConfigs.containsKey(currentClassName)) {
                  this.hConfigs.put(currentClassName, config);
               }
            } catch (Exception var9) {
               ;
            }
         }

         return null;
      }
   }

   private void saveAll() {
      Iterator var2 = this.playerClasses.keySet().iterator();

      while(var2.hasNext()) {
         String s = (String)var2.next();
         this.savePlayerConfig(s);
      }

   }

   public void savePlayerConfig(String s) {
      YamlConfiguration playerConfig = new YamlConfiguration();
      File playerDataFolder = new File(this.getDataFolder(), "data");
      if(!playerDataFolder.exists()) {
         playerDataFolder.mkdir();
      }

      File playerFile = new File(this.getDataFolder() + "/data", s + ".yml");
      String message;
      if(!playerFile.exists()) {
         try {
            playerFile.createNewFile();
         } catch (IOException var9) {
            message = "[HeroesSkillTree] failed to save " + s + ".yml";
            logger.severe(message);
            return;
         }
      }

      try {
         playerConfig.load(playerFile);
         Iterator message1 = ((HashMap)this.playerClasses.get(s)).keySet().iterator();

         while(message1.hasNext()) {
            String e = (String)message1.next();
            playerConfig.set(e + ".points", ((HashMap)this.playerClasses.get(s)).get(e));
            if(this.playerSkills.containsKey(s) && ((HashMap)this.playerSkills.get(s)).containsKey(e)) {
               Iterator var8 = ((HashMap)((HashMap)this.playerSkills.get(s)).get(e)).keySet().iterator();

               while(var8.hasNext()) {
                  String skillName = (String)var8.next();
                  playerConfig.set(e + ".skills." + skillName, ((HashMap)((HashMap)this.playerSkills.get(s)).get(e)).get(skillName));
               }
            }
         }

         playerConfig.save(playerFile);
      } catch (Exception var10) {
         message = "[HeroesSkillTree] failed to save " + s + ".yml";
         logger.severe(message);
      }

   }

   private void loadConfig() {
      File configFile = new File(this.getDataFolder(), "config.yml");
      if(!configFile.exists()) {
         try {
            configFile.createNewFile();
         } catch (IOException var5) {
            logger.severe("[HeroesSkillTree] failed to create new config.yml");
            return;
         }
      }

      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(configFile);
         this.pointsPerLevel = config.getInt("points-per-level", 1);
      } catch (Exception var4) {
         logger.severe("[HeroesSkillTree] failed to load config.yml");
      }

   }

   public int getPointsPerLevel() {
      return this.pointsPerLevel;
   }
   
   private void loadLanguageFile() {
	   // Create if missing
	   File file = new File(getDataFolder(), "lang.yml");
	   try {
	       if (file.createNewFile()) {
	           LangSender.info("lang.yml created.");
	           YamlConfiguration yaml = LangList.toYaml();
	           yaml.save(file);
	           return;
	       }
	   } catch (Exception e) {
	       e.printStackTrace();
	   }

	   // Otherwise, load the announcements from the file
	   try {
	       YamlConfiguration yaml = new YamlConfiguration();
	       yaml.load(file);
	       UtilTest.addMissingRemoveObsolete(file, LangList.toYaml(), yaml);
	       LangList.load(yaml);
	   } catch (Exception e) {
	       e.printStackTrace();
	   }
   	}
}
