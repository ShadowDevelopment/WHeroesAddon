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
import me.Wiedzmin137.wheroesaddon.addons.ItemGUI;
import me.Wiedzmin137.wheroesaddon.addons.ManaPotion;
import me.Wiedzmin137.wheroesaddon.addons.WEventListener;
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
	private Module moduleManager;
	private final WEventListener WEventListener = new WEventListener();
	private final ManaPotion WManaPotion = new ManaPotion();
	private final HeroesSkillTree instanceHST = new HeroesSkillTree();
	private final EventListener HEventListener = new EventListener(this);
	   
	private int pointsPerLevel = 1;
	private int hologram_time = 2500;
	private boolean holograms = false;
	private boolean useJoinChoose = false;
	private boolean useManaPotion = true;
	private boolean useSkillTree = true;
	
	private boolean useHolographicDisplays;
    private boolean HeroesEnabled = false;
	public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
	   
    public final static Logger Log = Logger.getLogger("Minecraft");
	public static YamlConfiguration LANG;
	public static File LANG_FILE;

	@Override
	public void onEnable() {
		instance = this;
	      
		PluginManager pm = getServer().getPluginManager();
		Logger Logger = getServer().getLogger();
		
        setupHeroes();

        if (!isHeroesEnabled()) {
            Log.warning("[WHeroesAddon] Requires Heroes to run, please download it");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
	      
		getConfig().options().copyDefaults(true).copyHeader(true);
		saveConfig();
		loadConfig();
		loadLang();
	      
		registerEvents(pm);
		setupSMS(pm);
		setupManaPotion();
		
	    useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
	      
		if (IGUI != null) { IGUI.setAutosave(true); }
	      
		Logger.info("[WHeroesAddon] vA0.2 has been enabled!");
	}
	   
	public void onDisable() {
		if (isHeroesEnabled()) {
			if (isUsingSkillTree()) {
				saveAll();
			}	      
			LANG = null;
			LANG_FILE = null;
			HandlerList.unregisterAll(HEventListener);
			HandlerList.unregisterAll(WEventListener);
		}
		instance = null;
	}
	
	private void registerEvents(PluginManager pm) {
		Plugin p = pm.getPlugin("AuthMe");
		
		if (isUsingSkillTree()) {
			pm.registerEvents(HEventListener, this);
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				Hero hero = heroes.getCharacterManager().getHero(player);
				HeroesSkillTree hst = new HeroesSkillTree();
				hst.recalcPlayerPoints(hero, hero.getHeroClass());
			}
			getCommand("skilltree").setExecutor(new CommandManager(instanceHST));
			moduleManager = new Module(this);
			moduleManager.loadModules();
		}
		
		if ((getConfig().getBoolean("useManaPotion", true)) ) {
			pm.registerEvents(WManaPotion, this);
		}
		if ((getConfig().getBoolean("useJoinChoose", true)) && (p.isEnabled())) {	   
			pm.registerEvents(WEventListener, this);
			getCommand("choose").setExecutor(new CommandManager(instanceHST));
		}
	}
	   
	private void setupSMS(PluginManager pm) {
		Plugin p = pm.getPlugin("ScrollingMenuSign");
		if (p instanceof ScrollingMenuSign && p.isEnabled()) {
			IGUI = new ItemGUI((ScrollingMenuSign) p);
			Log.info("[WHeroesAddon] ScrollingMenuSign integration is enabled; menus created");
		} else {/*Plugin is not available*/}
	}
	
    private void setupHeroes() {
        if (getServer().getPluginManager().isPluginEnabled("Heroes")) {
            HeroesEnabled = true;
        }
    }
	
	private void setupManaPotion() {
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
	   
	private void loadLang() {
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
		for (String s : instanceHST.getPlayerClasses().keySet()) {
			savePlayerConfig(s);
		}
	}
	   
	public void resetPlayer(Player player) {
		//FIXME error on /Hero reset
		String name = player.getName();
		instanceHST.getPlayerSkills().put(name, instanceHST.getPlayerClasses());
		instanceHST.getPlayerClasses().put(name, new HashMap<String, Integer>());
		instanceHST.resetPlayerSkillTree(name);
	} 
	   
	
	public void savePlayerConfig(String name) {
		FileConfiguration playerConfig = new YamlConfiguration();
		File playerDataFolder = new File(getDataFolder(), "data");
		if(!playerDataFolder.exists()) {
			playerDataFolder.mkdir();
		}
		File playerFile = new File(getDataFolder() + "/data", name + ".yml");
		if(!playerFile.exists()) {
			try {
				playerFile.createNewFile();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return;
			}
		}
		try {
			playerConfig.load(playerFile);
			
			if(!instanceHST.getPlayerClasses().containsKey(name)) {
				instanceHST.getPlayerClasses().put(name, new HashMap<String, Integer>());
			}

			Iterator<String> pName = instanceHST.getPlayerClasses().get(name).keySet().iterator();
			while(pName.hasNext()) {
				String pClass = pName.next();
				playerConfig.set(pClass + ".points", instanceHST.getPlayerClasses().get(name).get(pClass));
				if(instanceHST.getPlayerSkills().containsKey(name) && instanceHST.getPlayerSkills().get(name).containsKey(pClass)) {
					Iterator<?> pSkills = ((HashMap<?, ?>)instanceHST.getPlayerSkills().get(name).get(pClass)).keySet().iterator();

					while(pSkills.hasNext()) {
						String skillName = (String)pSkills.next();
						playerConfig.set(pClass + ".skills." + skillName, ((HashMap<?, ?>)instanceHST.getPlayerSkills().get(name).get(pClass)).get(skillName));
						Log.info(pClass + ".skills." + skillName);
					}
				}
			}
			playerConfig.save(playerFile);
		} catch (Exception e) {
			e.printStackTrace();;
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
	public boolean isHeroesEnabled() { return HeroesEnabled; }
	      
	public YamlConfiguration getLang() { return LANG; }
	public File getLangFile() { return LANG_FILE; }
	   
	public int getPointsPerLevel() { return pointsPerLevel; } 
	public int getHologramTime() { return hologram_time; }
	public boolean areHologramsEnabled() { return holograms; }
	public boolean isUsingJoinChoose() { return useJoinChoose; }
	public boolean isUsingManaPotion() { return useManaPotion; }
	public boolean isUsingSkillTree() { return useSkillTree; }
	public boolean isUsingHolographicDisplays() { return useHolographicDisplays; }
	public static WAddonCore getInstance() { return instance; }

	public Module getModuleManager() { return moduleManager; }
}
