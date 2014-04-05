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
import me.Whatshiywl.heroesskilltree.commands.CommandManager;
import me.Whatshiywl.heroesskilltree.commands.SkillAdminCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillDownCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillInfoCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillListCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillLockedCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillUpCommand;
import me.Wiedzmin137.wheroesaddon.Hologram;
import me.Wiedzmin137.wheroesaddon.ItemGUI;
import me.Wiedzmin137.wheroesaddon.Lang;
import me.Wiedzmin137.wheroesaddon.ManaPotion;
import me.Wiedzmin137.wheroesaddon.WEventListener;
import me.desht.scrollingmenusign.ScrollingMenuSign;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
   private ManaPotion manaPotion;
   private ItemGUI IGUI;
   private final EventListener HEventListener = new EventListener(this);
   private final WEventListener WEventListener = new WEventListener();
   private final ManaPotion WManaPotion = new ManaPotion();
   private HashMap<String, FileConfiguration> hConfigs = new LinkedHashMap<String, FileConfiguration>();
   
   private HashMap<String, HashMap<String, HashMap<String, Integer>>> playerSkills 
  	= new LinkedHashMap<String, HashMap<String, HashMap<String, Integer>>>();
   private HashMap<String, HashMap<String, Integer>> playerClasses = new LinkedHashMap<String, HashMap<String, Integer>>();
   
   private int pointsPerLevel = 1;
   private int hologram_time = 2500;
   private boolean holograms = false;
   private boolean useJoinChoose = false;
   private boolean useManaPotion = true;
   
   public static Logger Logger;
   public static YamlConfiguration LANG;
   public static File LANG_FILE;
   public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");

   public List<Skill> SkillStrongParents = new ArrayList<Skill>();
   public List<Skill> SkillWeakParents = new ArrayList<Skill>();
   
   //FIXME error on /hero reset (can't delete player.yml in WHA)
  
   @Override
   public void onEnable() {
      setInstance(this);
      
      PluginManager pm = getServer().getPluginManager();
	  Logger Logger = getServer().getLogger();
      
      getConfig().options().copyDefaults(true).copyHeader(true);
      saveConfig();
      loadConfig();
      loadLang();
      
      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
        Hero hero = heroes.getCharacterManager().getHero(player);
        recalcPlayerPoints(hero, hero.getHeroClass());
      }
      
      setupSMS(pm);
      setupManaPotion();
      registerEvents(pm);
      if (IGUI != null) { IGUI.setAutosave(true); }
      
      getCommand("skilltree").setExecutor(new CommandManager(this));
      
      Logger.info(Lang.CONSOLE_ENABLED.toString());
   }
   
   public void onDisable() {
	      saveAll();
	      LANG = null;
	      LANG_FILE = null;
	      HandlerList.unregisterAll(HEventListener);
	      HandlerList.unregisterAll(WEventListener);
	      
	      getServer().getLogger().info(Lang.CONSOLE_DISABLED.toString());
	      instance = null;
   }
   
   private void setupSMS(PluginManager pm) {
	   Plugin p = pm.getPlugin("ScrollingMenuSign");
       if (p instanceof ScrollingMenuSign && p.isEnabled()) {
    	   IGUI = new ItemGUI((ScrollingMenuSign) p);
    	   getServer().getLogger().info(Lang.CONSOLE_SMS_ENABLED.toString());
       } else {
    	   // plugin not available
       }
   }
   
   public void setupManaPotion() {
	   manaPotion = new ManaPotion();
	   if(!getConfig().getBoolean("static-regain")) {
		   manaPotion.setRegainRand(true);
		   manaPotion.setRegains(getConfig().getInt("regain-min"), getConfig().getInt("regain-max"));
	   } else {
		   manaPotion.setRegainRand(false);
		   manaPotion.setRegain(getConfig().getInt("regain"));
	   }
	   byte id = (byte)getConfig().getInt("potion-id");
	   manaPotion.setPotion(Material.POTION);
	   manaPotion.setPotionData(id);
   }
   
   public void registerEvents(PluginManager pm) {
	   Plugin p = pm.getPlugin("AuthMe");
	   pm.registerEvents(HEventListener, this);
	   
	   if ((getConfig().getBoolean("useManaPotion", true)) ) {
		   pm.registerEvents(WManaPotion, this);
	   }
	   if ((getConfig().getBoolean("useJoinChoose", true)) && (p.isEnabled())) {
		   pm.registerEvents(WEventListener, this);
	   }
   }
   
//   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
//	  //FIXME fix strange ClassCastException (it's occur even if it shouldn't) casted in console (Spigot)
//	  //FIXME some command doesn't work due to unreal permissions lack
//	  //TODO clean up all commands
//	  
//      Hero hero = heroes.getCharacterManager().getHero((Player)sender);
//      String skillPoints = String.valueOf(getPlayerPoints(hero));
//      
//      if(commandLabel.equalsIgnoreCase("skillup")) {
//         SkillUpCommand.skillUp(this, sender, args);
//         return true;
//      } else if(commandLabel.equalsIgnoreCase("skillgui")) {
//    	 //TODO clean up commands
//         if (sender instanceof Player) {
//        	 if (sender.hasPermission("skilltree.skillgui")) {
//                 HeroClass heroclass = hero.getHeroClass();
//            	 ItemGUI.createSkillTree(sender, heroclass, heroes, this);
//        	 } else {
//            	 sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
//                 return true;
//        	 }
//             return true;
//         } else {
//        	 sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
//        	 return true;
//         }
//      } else if(commandLabel.equalsIgnoreCase("wybor")) {
//          ItemGUI.createClassChoose((Player)sender);
//          return true;
//      } else if(commandLabel.equalsIgnoreCase("skilldown")) {
//         SkillDownCommand.skillDown(this, sender, args);
//         return true;
//      } else if(commandLabel.equalsIgnoreCase("skillinfo")) {
//         SkillInfoCommand.skillInfo(this, sender, args);
//         return true;
//      } else if(commandLabel.equalsIgnoreCase("skillpoints")) {
//         if(!(sender instanceof Player)) {
//        	 sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
//            return true;
//         } else {
//            if(sender.hasPermission("skilltree.points")) {
//               sender.sendMessage(Lang.TITLE.toString() + Lang.INFO_SKILLPOINTS.toString().replace("%points%", skillPoints));
//            } else {
//               sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PERMISSION_DENIED);
//            }
//            return true;
//         }
//      } else if(commandLabel.equalsIgnoreCase("skilladmin")) {
//         SkillAdminCommand.skillAdmin(this, sender, args);
//         return true;
//      } else if(!commandLabel.equalsIgnoreCase("slist") && !commandLabel.equalsIgnoreCase("sl")) {
//         if(!commandLabel.equalsIgnoreCase("unlocks") && !commandLabel.equalsIgnoreCase("un")) {
//        	sender.sendMessage(Lang.HELP_1.toString());
//        	sender.sendMessage(Lang.HELP_2.toString());
//        	sender.sendMessage(Lang.HELP_3.toString());
//        	sender.sendMessage(Lang.HELP_4.toString());
//        	sender.sendMessage(Lang.HELP_5.toString());
//        	sender.sendMessage(Lang.HELP_6.toString());
//        	sender.sendMessage(Lang.HELP_7.toString());
//        	if(sender instanceof Player) {
//            	sender.sendMessage(Lang.INFO_SKILLPOINTS.toString().replace("%points%", skillPoints));
//        	}
//            return true;
//         } else {
//            SkillLockedCommand.skillList(this, sender, args);
//            return true;
//         }
//      } else {
//         SkillListCommand.skillList(this, sender, args);
//         return true;
//      }
//   }

   public void resetPlayer(Player player) {
	  //FIXME error on /Hero reset
      String name = player.getName();
      playerSkills.put(name, playerClasses);
      playerClasses.put(name, new HashMap<String, Integer>());
      resetPlayerConfig(name);
   } 
   
   private void resetPlayerConfig(String name) {
	 //FIXME error on /Hero reset
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
	   int points = hero.getLevel(hClass) * getPointsPerLevel();
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
             ((HashMap<String, Integer>)playerSkills.get(name).get(s))
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

   private void saveAll() {
     for (String s : playerClasses.keySet()) {
       savePlayerConfig(s);
     }
   }

   @SuppressWarnings("rawtypes")
   public void savePlayerConfig(String name) {
	  FileConfiguration playerConfig = new YamlConfiguration();
	  File playerDataFolder = new File(getDataFolder(), "data");
      if(!playerDataFolder.exists()) {
         playerDataFolder.mkdir();
      }
      File playerFile = new File(getDataFolder() + "/data", name + ".yml");
      String message;
      if(!playerFile.exists()) {
         try {
            playerFile.createNewFile();
         } catch (IOException ioe) {
            message = "[HeroesSkillTree] failed to save " + name + ".yml";
            Logger.severe(message);
            return;
         }
      }
      try {
         playerConfig.load(playerFile);
         
         Iterator<String> message1 = this.playerClasses.get(name).keySet().iterator();

         //TODO clean up those Iterator and HashMaps
         //TODO rename var, message and e
         while(message1.hasNext()) {
            String e = message1.next();
            playerConfig.set(e + ".points", this.playerClasses.get(name).get(e));
            if(this.playerSkills.containsKey(name) && this.playerSkills.get(name).containsKey(e)) {
               Iterator var8 = ((HashMap)this.playerSkills.get(name).get(e)).keySet().iterator();

               while(var8.hasNext()) {
                  String skillName = (String)var8.next();
                  playerConfig.set(e + ".skills." + skillName, ((HashMap)this.playerSkills.get(name).get(e)).get(skillName));
               }
            }
         }
         playerConfig.save(playerFile);
      } catch (Exception e) {
         message = "[HeroesSkillTree] failed to save " + name + ".yml";
         Logger.severe(message);
      }
   }

   private void loadConfig() {
      File configFile = new File(getDataFolder(), "config.yml");
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
         pointsPerLevel = config.getInt("points-per-level", 1);
         hologram_time = config.getInt("hologram_time");
         holograms = config.getBoolean("holograms");
         useJoinChoose = config.getBoolean("useJoinChoose");
         useManaPotion = config.getBoolean("useJoinChoose");
      } catch (Exception e) {
    	  Logger.severe("[HeroesSkillTree] failed to load config.yml");
      }
   }
   
   public static void expMessage(Player p, Location loc, double gained, double needed, double current) {
	   if(gained == 0) { return; }
	   final Hologram holo = new Hologram(
		 Lang.HOLOGRAM_MESSAGE_EXP_GAINED.toString().replace("%gained%", String.valueOf(gained)),
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

   public void loadLang() {
	   File lang = new File(getDataFolder(), "lang.yml");
	   if (!lang.exists()) {
		   try {
			   getDataFolder().mkdir();
			   lang.createNewFile();
			   InputStream defConfigStream = getResource("lang.yml");
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
			   setEnabled(false);
		   }
	   }
	   YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
	   for(Lang item:Lang.values()) {
		   if (conf.getString(item.getPath()) == null) {
			   conf.set(item.getPath(), item.getDefault());
		   }
	   }
	   Lang.setFile(conf);
	   HeroesSkillTree.LANG = conf;
	   HeroesSkillTree.LANG_FILE = lang;
	   try {
		   conf.save(getLangFile());
	   } catch(IOException e) {
		   Logger.warning("HeroesSkillTree: Failed to save lang.yml.");
		   Logger.warning("HeroesSkillTree: Report this stack trace to Wiedzmin137.");
		   e.printStackTrace();
	   }
   	}
   
   private void setInstance(HeroesSkillTree HST) { instance = HST; }
   
   public YamlConfiguration getLang() { return LANG; }
   public File getLangFile() { return LANG_FILE; }
   
   public int getPointsPerLevel() { return pointsPerLevel; } 
   public int getHologramTime() { return hologram_time; }
   public boolean areHologramsEnabled() { return holograms; }
   public boolean isUsingJoinChoose() { return useJoinChoose; }
   public boolean isUsingManaPotion() { return useManaPotion; }
   public static HeroesSkillTree getInstance() { return instance; }
}
