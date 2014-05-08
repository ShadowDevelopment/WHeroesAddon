package me.Wiedzmin137.wheroesaddon;

import java.io.File;
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
    private Config configManager;
	private HeroesSkillTree skillTree;
	
	private WEventListener WEventListener = new WEventListener();
	private ManaPotion WManaPotion = new ManaPotion();
	private EventListener HEventListener = new EventListener(this/*, getSkillTree()*/);
	
	private boolean useHolographicDisplays = false;
	private boolean useAuthMe = false;
    private boolean HeroesEnabled = false;
    
	public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
	   
    public final static Logger Log = Logger.getLogger("Minecraft");

	@Override
	public void onEnable() {
		instance = this;
		PluginManager pm = getServer().getPluginManager();
		
        HeroesEnabled = getServer().getPluginManager().isPluginEnabled("Heroes");
        if (!isHeroesEnabled()) {
            Log.warning("[WHeroesAddon] Requires Heroes to run for now, please download it");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        configManager = new Config(this);
        try {
            configManager.load();
        } catch (Exception e) {
            e.printStackTrace();
            Log.severe("[WAddonCore] Critical config error encountered while loading. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        skillTree = HEventListener.getST();
        
		configManager.loadConfig();
		configManager.loadLang();
		
	    useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
	    useAuthMe = Bukkit.getPluginManager().isPluginEnabled("AuthMe");
	      
		registerEvents(pm);
		prepareSkillTree(pm);
		
		setupSMS(pm);
		setupManaPotion();
	      
		if (IGUI != null) { IGUI.setAutosave(true); }
		
		//IGUI.getSkillPosition();
		//IGUI.moveIntoClassFile("Wojownik");
	      
		Log.info("[WHeroesAddon] vA0.1.4 has been enabled!");
	}

	@Override
	public void onDisable() {
		if (isHeroesEnabled()) {
			if (getConf().isUsingSkillTree()) {
				getConf().saveAll();
			}	      
			Config.langConfig = null;
			Config.langFile = null;
			HandlerList.unregisterAll(HEventListener);
			HandlerList.unregisterAll(WEventListener);
		}
		instance = null;
	}
	
	private void registerEvents(PluginManager pm) {
		if (getConfig().getBoolean("useManaPotion", true)) {
			pm.registerEvents(WManaPotion, this);
		}
		if (getConfig().getBoolean("useJoinChoose", true) && pm.isPluginEnabled("AuthMe")) {
			pm.registerEvents(WEventListener, this);
			getCommand("choose").setExecutor(new CommandManager(skillTree));
		}
	}
	
	private void prepareSkillTree(PluginManager pm) {
		if (getConf().isUsingSkillTree()) {
			pm.registerEvents(HEventListener, this);
			//FIXME this NOT work proper
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				Hero hero = heroes.getCharacterManager().getHero(player);
				skillTree.loadPlayerConfig(player.getName());
				skillTree.recalcPlayerPoints(hero, hero.getHeroClass());
			}
			getCommand("skilltree").setExecutor(new CommandManager(skillTree));
			//moduleManager = new Module(this);
			//moduleManager.loadModules();

		}
	}
	   
	private void setupSMS(PluginManager pm) {
		Plugin p = pm.getPlugin("ScrollingMenuSign");
		if (p instanceof ScrollingMenuSign && p.isEnabled()) {
			IGUI = new ItemGUI((ScrollingMenuSign) p);
			Log.info("[WHeroesAddon] ScrollingMenuSign integration is enabled; menus created");
		} else {/*Plugin is not available*/}
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
	
	public boolean isHeroesEnabled() { return HeroesEnabled; }
	public boolean isUsingHolographicDisplays() { return useHolographicDisplays; }
	public boolean isUsingAuthMe() { return useAuthMe; }
	
	public static WAddonCore getInstance() { return instance; }
	
    public Config getConf() { return configManager; }
	public Module getModuleManager() { return moduleManager; }
	public HeroesSkillTree getSkillTree() { return skillTree; }
	
	public YamlConfiguration getLang() { return Config.langConfig; }
	public File getLangFile() { return Config.langFile; }
}
