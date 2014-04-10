package me.Wiedzmin137.wheroesaddon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import me.Whatshiywl.heroesskilltree.EventListener;
import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Whatshiywl.heroesskilltree.commands.CommandManager;
import me.desht.scrollingmenusign.ScrollingMenuSign;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;

public class WAddonCore extends JavaPlugin {
	
	private static WAddonCore instance;
	private ManaPotion manaPotion;
	private ItemGUI IGUI;
	private final EventListener HEventListener = new EventListener(this, HeroesSkillTree.getInstance());
	private final WEventListener WEventListener = new WEventListener();
	private final ManaPotion WManaPotion = new ManaPotion();
	   
	private int pointsPerLevel = 1;
	private int hologram_time = 2500;
	private boolean holograms = false;
	private boolean useJoinChoose = false;
	private boolean useManaPotion = true;
	private boolean useSkillTree = true;
	   
	public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
	   
    public final static Logger Log = Logger.getLogger("Minecraft");
	public static YamlConfiguration LANG;
	public static File LANG_FILE;

	@Override
	public void onEnable() {
		setInstance(this);
	      
		PluginManager pm = getServer().getPluginManager();
		Logger Logger = getServer().getLogger();
	      
		getConfig().options().copyDefaults(true).copyHeader(true);
		saveConfig();
		loadConfig();
		loadLang();
	      
		setupSMS(pm);
		setupManaPotion();
		registerEvents(pm);
	      
		if (IGUI != null) { IGUI.setAutosave(true); }
	      
		Logger.info(Lang.CONSOLE_ENABLED.toString());
	}
	   
	public void onDisable() {
		if (isUsingSkillTree()) {
			saveAll();
		}
		      
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
		} else {}
	}
	
	public void setupManaPotion() {
		manaPotion = new ManaPotion();
		if(!getConfig().getBoolean("ManaPotion.StaticRegain")) {
			manaPotion.setRegainRand(true);
			manaPotion.setRegains(getConfig().getInt("ManaPotion.RegainMin"), getConfig().getInt("ManaPotion.RegainMax"));
		} else {
			manaPotion.setRegainRand(false);
			manaPotion.setRegain(getConfig().getInt("ManaPotion.Regain"));
		}
		short id = (short)getConfig().getInt("ManaPotion.Potion-ID");
		manaPotion.setPotion(Material.POTION);
		manaPotion.setPotionData(id);
	}
	   
	public void registerEvents(PluginManager pm) {
		Plugin p = pm.getPlugin("AuthMe");
		
		if (isUsingSkillTree()) {
			pm.registerEvents(HEventListener, this);
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				Hero hero = heroes.getCharacterManager().getHero(player);
				HeroesSkillTree hst = new HeroesSkillTree();
				hst.recalcPlayerPoints(hero, hero.getHeroClass());
			}
			getCommand("skilltree").setExecutor(new CommandManager(HeroesSkillTree.getInstance()));
		}
		
		if ((getConfig().getBoolean("useManaPotion", true)) ) {
			pm.registerEvents(WManaPotion, this);
		}
		if ((getConfig().getBoolean("useJoinChoose", true)) && (p.isEnabled())) {	   
			pm.registerEvents(WEventListener, this);
			getCommand("choose").setExecutor(new CommandManager(HeroesSkillTree.getInstance()));
		}
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
				Log.severe("[HeroesSkillTree] Couldn't create language file.");
				Log.severe("[HeroesSkillTree] This is a fatal error. Now disabling");
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
		WAddonCore.LANG = conf;
		WAddonCore.LANG_FILE = lang;
		try {
			   conf.save(getLangFile());
		   } catch(IOException e) {
			   Log.warning("HeroesSkillTree: Failed to save lang.yml.");
			   Log.warning("HeroesSkillTree: Report this stack trace to Wiedzmin137.");
			   e.printStackTrace();
		   }
	   }
	   
	   private void saveAll() {
		     for (String s : HeroesSkillTree.getInstance().playerClasses.keySet()) {
		       savePlayerConfig(s);
		     }
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
				   Log.severe(Lang.SERVRE_FAILED_CREATE.toString().replace("%name%", name));
				   return;
			   }
		   }
		   try {
			   playerConfig.load(playerConfigFile);
			   HeroesSkillTree hst = HeroesSkillTree.getInstance();
			   if (!hst.playerClasses.containsKey(name)) {
				   hst.playerClasses.put(name, new HashMap<String, Integer>());
			   }
			   for (String s : playerConfig.getKeys(false)) {
				   hst.playerClasses.get(name).put(s, Integer.valueOf(playerConfig.getInt(s + ".points", 0)));
				   if (!hst.playerSkills.containsKey(s)) {
					   hst.playerSkills.put(name, new HashMap<String, HashMap<String, Integer>>());
				   }
		    	    if (!hst.playerSkills.get(name).containsKey(s)) {
		    	    	hst.playerSkills.get(name).put(s, new HashMap<String, Integer>());
		    	    }
		    	    if (playerConfig.getConfigurationSection(s + ".skills") != null) {
		    	    	for (String st : playerConfig.getConfigurationSection(s + ".skills").getKeys(false)) {
		      	      ((HashMap<String, Integer>)hst.playerSkills.get(name).get(s))
		      	      	.put(st, Integer.valueOf(playerConfig.getInt(s + ".skills." + st, 0)));
		    	    	}
		    	    }
			   }
		   }
		   catch (Exception e) {
			   Log.severe("[HeroesSkillTree] failed to load " + name + ".yml");
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
				   Log.severe(message);
				   return;
			   }
		   }
		   try {
			   playerConfig.load(playerFile);
			   HeroesSkillTree hst = HeroesSkillTree.getInstance();
	         
			   Iterator<String> message1 = hst.playerClasses.get(name).keySet().iterator();

			   //TODO clean up those Iterator and HashMaps
			   //TODO rename var, message and e
			   while(message1.hasNext()) {
				   String e = message1.next();
				   playerConfig.set(e + ".points", hst.playerClasses.get(name).get(e));
				   if(hst.playerSkills.containsKey(name) && hst.playerSkills.get(name).containsKey(e)) {
					   Iterator var8 = ((HashMap)hst.playerSkills.get(name).get(e)).keySet().iterator();

					   while(var8.hasNext()) {
						   String skillName = (String)var8.next();
						   playerConfig.set(e + ".skills." + skillName, ((HashMap)hst.playerSkills.get(name).get(e)).get(skillName));
					   }
				   }
			   }
			   playerConfig.save(playerFile);
		   } catch (Exception e) {
			   message = "[HeroesSkillTree] failed to save " + name + ".yml";
			   Log.severe(message);
		   }
	   }
	   
	   private void loadConfig() {
		   File configFile = new File(getDataFolder(), "config.yml");
		   if(!configFile.exists()) {
			   try {
				   configFile.createNewFile();
			   } catch (IOException ioe) {
				   Log.severe("[HeroesSkillTree] failed to create new config.yml");
				   return;
			   }
		   }
		   FileConfiguration config = new YamlConfiguration();
		   try {
			   config.load(configFile);
			   pointsPerLevel = config.getInt("SkillTree.PointsPerLevel", 1);
			   hologram_time = config.getInt("Hologram.Time");
			   holograms = config.getBoolean("Hologram.Enabled");
			   useJoinChoose = config.getBoolean("UseJoinChoose");
			   useManaPotion = config.getBoolean("ManaPotion.Enabled");
			   useSkillTree = config.getBoolean("SkillTree.Enabled");
		   } catch (Exception e) {
			   Log.severe("[HeroesSkillTree] failed to load config.yml");
		   }
	   }
	      
	   public YamlConfiguration getLang() { return LANG; }
	   public File getLangFile() { return LANG_FILE; }
	   
	   public int getPointsPerLevel() { return pointsPerLevel; } 
	   public int getHologramTime() { return hologram_time; }
	   public boolean areHologramsEnabled() { return holograms; }
	   public boolean isUsingJoinChoose() { return useJoinChoose; }
	   public boolean isUsingManaPotion() { return useManaPotion; }
	   public boolean isUsingSkillTree() { return useSkillTree; }
	   public static WAddonCore getInstance() { return instance; }
	   
	   private void setInstance(WAddonCore WAdddonCore) { instance = WAdddonCore; }
}
