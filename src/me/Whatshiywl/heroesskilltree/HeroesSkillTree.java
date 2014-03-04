package me.Whatshiywl.heroesskilltree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import me.Whatshiywl.heroesskilltree.EventListener;
import me.Whatshiywl.heroesskilltree.commands.SkillAdminCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillDownCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillInfoCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillListCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillLockedCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillUpCommand;
import me.Wiedzmin137.wheroesaddon.Hologram;
import me.Wiedzmin137.wheroesaddon.ItemGUI;
import me.Wiedzmin137.wheroesaddon.Lang;
import me.desht.scrollingmenusign.SMSHandler;
import me.desht.scrollingmenusign.ScrollingMenuSign;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HeroesSkillTree extends JavaPlugin implements Listener {

   private static HeroesSkillTree instance;
   private HashMap<String, HashMap<String, HashMap<String, Integer>>> playerSkills = new LinkedHashMap<String, HashMap<String, HashMap<String, Integer>>>();
   private HashMap<String, HashMap<String, Integer>> playerClasses = new LinkedHashMap<String, HashMap<String, Integer>>();
   private HashMap<String, FileConfiguration> hConfigs = new LinkedHashMap<String, FileConfiguration>();
   private ItemGUI IGUI;
   
   private int pointsPerLevel = 1;
   private boolean holograms = true;
   private int hologram_time = 2500;
	
   public HeroesSkillTree plugin;
   public final EventListener HEventListener = new EventListener(this);
   public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
   //TODO Take carry about those HashMaps and ArrayLists, they're bugged (saving or loading or both)
   public List<Skill> SkillStrongParents = new ArrayList<Skill>();
   public List<Skill> SkillWeakParents = new ArrayList<Skill>();
   public static YamlConfiguration LANG;
   public static File LANG_FILE;
   public static Logger Logger;
   public static SMSHandler smsHandler;
   
   //TODO add support for "#" character in config.yml & lang.yml
   //TODO cleanup some thing
   //TODO (HARD) make API class instead of huge main class
   //TODO try to make main class WHereosAddon in Wiedzmin137 package
   //TODO add comments and documentation to all classes
   
   public void onDisable() {
	      saveAll();
	      LANG = null;
	      LANG_FILE = null;
       instance = null;
	      HandlerList.unregisterAll(HEventListener);
	      
	      Logger.info(Lang.CONSOLE_DISABLED.toString());
   }
  
   @Override
   public void onEnable() {
      setInstance(this);
      
      PluginManager pm = getServer().getPluginManager();
      pm.registerEvents(this.HEventListener, this);
	  Logger = getServer().getLogger();
      
      getConfig().options().copyDefaults(true);
      saveConfig();
      loadConfig();
      loadLang();
      
      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
        Hero hero = heroes.getCharacterManager().getHero(player);
        recalcPlayerPoints(hero, hero.getHeroClass());
      }
      
      setupSMS(pm);
      
      if (IGUI != null) { IGUI.setAutosave(true); }
      
      Logger.info(Lang.CONSOLE_ENABLED.toString());
   }
   
   private void setInstance(HeroesSkillTree HST) { instance = HST; }
   
   private void setupSMS(PluginManager pm) {
	   Plugin p = pm.getPlugin("ScrollingMenuSign");
       if (p instanceof ScrollingMenuSign && p.isEnabled()) {
    	   IGUI = new ItemGUI((ScrollingMenuSign) p);
    	   Logger.info(Lang.CONSOLE_SMS_ENABLED.toString());
       } else {
    	   // plugin not available
       }
   }
   
   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
	  //TODO fix strange ClassCastException (it's occur even if it shouldn't) casted in console (Spigot)
	  //TODO clean up all commands
      Hero hero = heroes.getCharacterManager().getHero((Player)sender);
      String skillPoints = String.valueOf(this.getPlayerPoints(hero));
      
      if(commandLabel.equalsIgnoreCase("skillup")) {
         SkillUpCommand.skillUp(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skillgui")) {
    	 //TODO clean up commands
         if (sender instanceof Player) {
        	 if (sender.hasPermission("skilltree.skillgui")) {
                 PluginManager pm = getServer().getPluginManager();
                 HeroClass heroclass = hero.getHeroClass();
                 
            	 Plugin p = pm.getPlugin("ScrollingMenuSign");
            	 ItemGUI ex = new ItemGUI((ScrollingMenuSign) p);
            	 ex.createSkillTree(sender, heroclass, heroes, this);
        	 } else {
            	 sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
                 return true;
        	 }
             return true;
         } else {
        	 sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
        	 return true;
         }
      } else if(commandLabel.equalsIgnoreCase("skilldown")) {
         SkillDownCommand.skillDown(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skillinfo")) {
         SkillInfoCommand.skillInfo(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skillpoints")) {
         if(!(sender instanceof Player)) {
        	 sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
            return true;
         } else {
            if(sender.hasPermission("skilltree.points")) {
               sender.sendMessage(Lang.TITLE.toString() + Lang.INFO_SKILLPOINTS.toString().replace("%points%", skillPoints));
            } else {
               sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PERMISSION_DENIED);
            }
            return true;
         }
      } else if(commandLabel.equalsIgnoreCase("skilladmin")) {
         SkillAdminCommand.skillAdmin(this, sender, args);
         return true;
      } else if(!commandLabel.equalsIgnoreCase("slist") && !commandLabel.equalsIgnoreCase("sl")) {
         if(!commandLabel.equalsIgnoreCase("unlocks") && !commandLabel.equalsIgnoreCase("un")) {
        	sender.sendMessage(Lang.HELP_1.toString());
        	sender.sendMessage(Lang.HELP_2.toString());
        	sender.sendMessage(Lang.HELP_3.toString());
        	sender.sendMessage(Lang.HELP_4.toString());
        	sender.sendMessage(Lang.HELP_5.toString());
        	sender.sendMessage(Lang.HELP_6.toString());
        	sender.sendMessage(Lang.HELP_7.toString());
        	if(sender instanceof Player) {
            	sender.sendMessage(Lang.INFO_SKILLPOINTS.toString().replace("%points%", skillPoints));
        	}
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
      this.playerSkills.put(name, playerClasses);
      this.playerClasses.put(name, new HashMap<String, Integer>());
      this.resetPlayerConfig(name);
   } 
   
   private void resetPlayerConfig(String name) {
     File playerFolder = new File(getDataFolder(), "data");
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
      return this.playerClasses.get(hero.getPlayer().getName()) != null && this.playerClasses
    		  .get(hero.getPlayer().getName())
    		  .get(hero.getHeroClass().getName()) != null?((Integer)this.playerClasses
    		  .get(hero.getPlayer().getName())
    		  .get(hero.getHeroClass().getName())).intValue():0;
   }

   //FIXME I think it doesn't work properly, I used this SupressWarnings but problems are in code
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public void recalcPlayerPoints(Hero hero, HeroClass hClass) {
	    String name = hero.getPlayer().getName();
	    String className = hClass.getName();
	    int points = hero.getLevel(hClass) * getPointsPerLevel();
	    if (this.playerClasses.get(name) == null) {
	      this.playerClasses.put(name, new HashMap<String, Integer>());
	    }
	    if (hero.getPlayer().hasPermission("skilltree.override.usepoints")) {
	      this.playerClasses.get(name).put(className, Integer.valueOf(points));
	      return;
	    }
	    if (this.playerClasses.get(name).get(className) == null) {
	      this.playerClasses.get(name).put(className, Integer.valueOf(0));
	      return;
	    }
	    if (this.playerSkills.get(name) == null) {
	      this.playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
	      return;
	    }
	    if (this.playerSkills.get(name).get(className) == null) {
	      this.playerSkills.get(name).put(className, new HashMap());
	      return;
	    }
	    for (Skill skill : heroes.getSkillManager().getSkills()) {
	      String skillName = skill.getName();
	      if (((HashMap)this.playerSkills.get(name).get(className)).get(skillName) != null) {
	        points -= ((Integer)((HashMap)this.playerSkills.get(name)
	        		.get(className))
	        		.get(skillName)).intValue();
	        if (points < 0) {
	          Logger.warning("[HeroesSkillTree] " + name + "'s skills are at a too high level!");
	          points = 0;
	        }
	      }
	    }
	    this.playerClasses.get(name).put(className, Integer.valueOf(points));
   }

   public void setPlayerPoints(Hero hero, int i) {
      if(this.playerClasses.get(hero.getPlayer().getName()) == null) {
         this.playerClasses.put(hero.getPlayer().getName(), new HashMap<String, Integer>());
      }
      this.playerClasses.get(hero.getPlayer().getName()).put(hero.getHeroClass().getName(), Integer.valueOf(i));
   }

   public void setPlayerPoints(Hero hero, HeroClass hClass, int i) {
      if(this.playerClasses.get(hero.getPlayer().getName()) == null) {
         this.playerClasses.put(hero.getPlayer().getName(), new HashMap<String, Integer>());
      }
      this.playerClasses.get(hero.getPlayer().getName()).put(hClass.getName(), Integer.valueOf(i));
   }

   //FIXME this probably not good like up
   @SuppressWarnings("rawtypes")
   public int getSkillLevel(Hero hero, Skill skill) {
      return this.playerSkills.get(hero.getPlayer().getName())
    		  != null && this.playerSkills
    		  	      .get(hero.getPlayer().getName())
    		  		  .get(hero.getHeroClass().getName())
    		  != null && ((HashMap)this.playerSkills
    				  .get(hero.getPlayer().getName())
    				  .get(hero.getHeroClass().getName()))
    				  .get(skill.getName())
    	      != null ? ((Integer)((HashMap)this.playerSkills
    	    		  .get(hero.getPlayer().getName())
    	    		  .get(hero.getHeroClass().getName()))
    	    		  .get(skill.getName())).intValue():0;
   }

   public void setSkillLevel(Hero hero, Skill skill, int i) {
      if(this.playerSkills.get(hero.getPlayer().getName()) == null) {
         this.playerSkills.put(hero.getPlayer().getName(), new HashMap<String, HashMap<String, Integer>>());
      }

      if(this.playerSkills.get(hero.getPlayer().getName()).get(hero.getHeroClass().getName()) == null) {
         this.playerSkills.get(hero.getPlayer().getName()).put(hero.getHeroClass().getName(), new HashMap<String, Integer>());
      }

      ((HashMap<String, Integer>)this.playerSkills
    		  .get(hero.getPlayer().getName())
    		  .get(hero.getHeroClass().getName()))
    		  .put(skill.getName(), Integer.valueOf(i));
   }

   public int getSkillMaxLevel(Hero hero, Skill skill) {
      return SkillConfigManager
    		  .getSetting(hero.getHeroClass(), skill, "max-level", -1) == -1?SkillConfigManager
    		  .getUseSetting(hero, skill, "max-level", -1, false):SkillConfigManager
    		  .getSetting(hero.getHeroClass(), skill, "max-level", -1);
   }

   public List<String> getStrongParentSkills(Hero hero, Skill skill) {
      return this.getParentSkills(hero, skill, "strong");
   }

   public List<String> getWeakParentSkills(Hero hero, Skill skill) {
      return this.getParentSkills(hero, skill, "weak");
   }

   public List<String> getParentSkills(Hero hero, Skill skill, String weakOrStrong) {
      FileConfiguration hCConfig = this.getHeroesClassConfig(hero.getHeroClass());
      return hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents") 
    		  == null ? null : hCConfig
    				  .getConfigurationSection("permitted-skills." + skill.getName() + ".parents")
    				  .getStringList(weakOrStrong);
   }

   public boolean isLocked(Hero hero, Skill skill) {
      if(skill != null && hero.canUseSkill(skill)) {
         boolean skillLevel = this.getSkillLevel(hero, skill) < 1;
         List<String> strongParents = this.getStrongParentSkills(hero, skill);
         boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
         List<String> weakParents = this.getWeakParentSkills(hero, skill);
         boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
         return skillLevel && (hasStrongParents || hasWeakParents);
      } else {
         return true;
      }
   }

   public boolean isMastered(Hero hero, Skill skill) {
      return hero.hasAccessToSkill(skill)
    		  ? this.getSkillLevel(hero, skill) >= this.getSkillMaxLevel(hero, skill) : false;
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


   public void loadPlayerConfig(String name) {
     FileConfiguration playerConfig = new YamlConfiguration();
     File playerFolder = new File(getDataFolder(), "data");
     if (!playerFolder.exists()) {
       playerFolder.mkdir();
     }
     File playerConfigFile = new File(playerFolder, name + ".yml");
     if (!playerConfigFile.exists()) {
       try {
         playerConfigFile.createNewFile();
       }
       catch (IOException ex) {
    	 Logger.severe(Lang.SERVRE_FAILED_CREATE.toString().replace("%name%", name));
         return;
       }
     }
     try {
       playerConfig.load(playerConfigFile);
       if (!this.playerClasses.containsKey(name)) {
         this.playerClasses.put(name, new HashMap<String, Integer>());
       }
       for (String s : playerConfig.getKeys(false)) {
         this.playerClasses.get(name).put(s, Integer.valueOf(playerConfig.getInt(s + ".points", 0)));
         if (!this.playerSkills.containsKey(s)) {
           this.playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
         }
         if (!this.playerSkills.get(name).containsKey(s)) {
           this.playerSkills.get(name).put(s, new HashMap<String, Integer>());
         }
         if (playerConfig.getConfigurationSection(s + ".skills") != null) {
           for (String st : playerConfig.getConfigurationSection(s + ".skills").getKeys(false)) {
             ((HashMap<String, Integer>)this.playerSkills.get(name).get(s))
            		 .put(st, Integer.valueOf(playerConfig.getInt(s + ".skills." + st, 0)));
           }
         }
       }
     }
     catch (Exception e) {
    	 Logger.severe("[HeroesSkillTree] failed to load " + name + ".yml");
     }
   }
   
   public FileConfiguration getHeroesClassConfig(HeroClass hClass) {
      if(this.hConfigs.containsKey(hClass.getName())) {
         return this.hConfigs.get(hClass.getName());
      }
      File classFolder = new File(heroes.getDataFolder(), "classes");
      for (File f : classFolder.listFiles()) {
        FileConfiguration config = new YamlConfiguration();
        try {
          config.load(f);
          String currentClassName = config.getString("name");
          if (currentClassName.equalsIgnoreCase(hClass.getName())) {
            this.hConfigs.put(hClass.getName(), config);
            return config;
          }
          if (!this.hConfigs.containsKey(currentClassName)) {
            this.hConfigs.put(currentClassName, config);
          }
        }
        catch (Exception localException) {}
      }
      return null;
    }

   private void saveAll() {
     for (String s : this.playerClasses.keySet()) {
       savePlayerConfig(s);
     }
   }

   public void savePlayerConfig(String s) {
	  FileConfiguration playerConfig = new YamlConfiguration();
	  File playerDataFolder = new File(getDataFolder(), "data");
      if(!playerDataFolder.exists()) {
         playerDataFolder.mkdir();
      }
      File playerFile = new File(this.getDataFolder() + "/data", s + ".yml");
      String message;
      if(!playerFile.exists()) {
         try {
            playerFile.createNewFile();
         } catch (IOException ioe) {
            message = "[HeroesSkillTree] failed to save " + s + ".yml";
            Logger.severe(message);
            return;
         }
      }
      try {
         playerConfig.load(playerFile);
         Iterator<String> namePlayer = this.playerClasses.get(s).keySet().iterator();
         while(namePlayer.hasNext()) {
            String name = namePlayer.next();
            if(this.playerSkills.containsKey(s) && this.playerSkills.get(s).containsKey(name)) {
               if (playerConfig.getConfigurationSection(s + ".skills") != null) {
                   for (String st : playerConfig.getConfigurationSection(s + ".skills").getKeys(false)) {
                     ((HashMap<String, Integer>)this.playerSkills.get(name).get(s))
                     .put(st, Integer.valueOf(playerConfig.getInt(s + ".skills." + st, 0)));
                   }
               }
            }
         }
         playerConfig.save(playerFile);
      } catch (Exception e) {
         message = "[HeroesSkillTree] failed to save " + s + ".yml";
         Logger.severe(message);
      }
   }

   private void loadConfig() {
      File configFile = new File(this.getDataFolder(), "config.yml");
      if(!configFile.exists()) {
         try {
            configFile.createNewFile();
         } catch (IOException ioe) {
        	Logger.severe("[HeroesSkillTree] failed to create new config.yml");
            return;
         }
      }
      FileConfiguration config = new YamlConfiguration();
      try {
         config.load(configFile);
         this.pointsPerLevel = config.getInt("points-per-level", 1);
         this.holograms = config.getBoolean("holograms");
         this.hologram_time = config.getInt("hologram_time");
      } catch (Exception e) {
    	  Logger.severe("[HeroesSkillTree] failed to load config.yml");
      }
   }
   
   public int getPointsPerLevel() { return this.pointsPerLevel; } 
   public boolean areHologramsEnabled() { return this.holograms; }
   public int getHologramTime() { return this.hologram_time; }
   public static HeroesSkillTree getInstance() { return instance; }
   
   public static void expMessage(Player p, Location loc, double gained, double needed, double current) {
		  if(gained == 0) { return; }
		  final Hologram holo = new Hologram(
				  Lang.HOLOGRAM_MESSAGE_EXP_CURRENT.toString().replace("%gained%", String.valueOf(gained)),
				  Lang.HOLOGRAM_MESSAGE_EXP_MAX.toString()
				    .replace("%current%", String.valueOf(current))
				  	.replace("%needed%", String.valueOf(needed)));
		  holo.show(p, loc);
		  Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new BukkitRunnable() {
			  @Override
			  public void run() {
				  holo.destroy();
			  }
		  }, instance.getHologramTime());
	  }

   /**
   * Gets the lang.yml config.
   * @return The lang.yml config.
   */
   public YamlConfiguration getLang() {
	   return LANG;
   }
   
   /**
   * Get the lang.yml file.
   * @return The lang.yml file.
   */
   public File getLangFile() {
	   return LANG_FILE;
   }

   /**
    * Load the lang.yml file.
    * @return The lang.yml config.
    */
   @SuppressWarnings("static-access")
   public void loadLang() {
	   File lang = new File(getDataFolder(), "lang.yml");
	   if (!lang.exists()) {
		   try {
			   getDataFolder().mkdir();
			   lang.createNewFile();
			   InputStream defConfigStream = this.getResource("lang.yml");
			   if (defConfigStream != null) {
				   YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				   defConfig.save(lang);
				   Lang.setFile(defConfig);
				   return;
			   }
		   } catch(IOException e) {
			   e.printStackTrace();
			   Logger.severe("[HeroesSkillTree] Couldn't create language file.");
			   Logger.severe("[HeroesSkillTree] This is a fatal error. Now disabling");
			   this.setEnabled(false); // Without it loaded, we can't send them messages
		   }
	   }
	   YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
	   for(Lang item:Lang.values()) {
		   if (conf.getString(item.getPath()) == null) {
			   conf.set(item.getPath(), item.getDefault());
		   }
	   }
	   Lang.setFile(conf);
	   this.LANG = conf;
	   this.LANG_FILE = lang;
	   try {
		   conf.save(getLangFile());
	   } catch(IOException e) {
		   Logger.warning("HeroesSkillTree: Failed to save lang.yml.");
		   Logger.warning("HeroesSkillTree: Report this stack trace to Wiedzmin137.");
		   e.printStackTrace();
	   }
   	}
}
