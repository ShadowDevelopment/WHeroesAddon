package me.Wiedzmin137.wheroesaddon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	protected static YamlConfiguration langConfig;
    protected static File playerConfigFolder;
	protected static File langFile;
	protected static File skillExampleFile;
	
	private int pointsPerLevel = 1;
	private int hologram_time = 2500;
	private boolean holograms = false;
	private boolean useJoinChoose = false;
	private boolean useManaPotion = true;
	private boolean useSkillTree = true;
	
	private static File configFile;
    private WAddonCore plugin;
    private static FileConfiguration config;
    
    public Config(WAddonCore plugin) {
    	this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        playerConfigFolder = new File(dataFolder + File.separator + "data");
        langFile = new File(dataFolder, "lang.yml");
        skillExampleFile = new File(dataFolder, "skillExample.yml");
        configFile = new File(dataFolder, "config.yml");
        
        config = plugin.getConfig();
	}
	
    public void load() throws Exception {
    	checkFile(langFile);
    	checkFile(skillExampleFile);
    	checkFile(configFile);
        if (!playerConfigFolder.exists()) {
        	playerConfigFolder.mkdirs();
        }
    }
    
    private void checkFile(File out) throws Exception  {
    	if (!out.exists()) {
    		InputStream fis = Config.class.getResourceAsStream("/configs/" + out.getName());
    		FileOutputStream fos = new FileOutputStream(out);
    		try {
    			byte[] buf = new byte[1024];
    			int i = 0;
    			while ((i = fis.read(buf)) != -1) {
    				fos.write(buf, 0, i);
    			}
    		} catch (Exception  e) {
 	          throw e;
    		} finally {
    			if (fis != null) {
    				fis.close();
    			}
    			if (fos != null) {
    				fos.close();
    			}
    		}
    	}
    }
    
	protected void loadLang() {
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(langFile);
		for (Lang item : Lang.values()) {
			if (conf.getString(item.getPath()) == null) {
				conf.set(item.getPath(), item.getDefault());
			}
		}
		Lang.setFile(conf);
		langConfig = conf;
		try {
			conf.save(plugin.getLangFile());
		} catch(IOException e) {
			WAddonCore.Log.warning("[WHeroesAddon] Failed to save lang.yml.");
			WAddonCore.Log.warning("[WHeroesAddon] Report this stack trace to Wiedzmin137.");
			e.printStackTrace();
		}
	}
	
	protected void loadConfig() {
		try {
			config.load(configFile);
			pointsPerLevel = config.getInt("SkillTree.PointsPerLevel", 1);
			hologram_time = config.getInt("Hologram.Time");
			holograms = config.getBoolean("Hologram.Enabled");
			useJoinChoose = config.getBoolean("UseJoinChoose");
			useManaPotion = config.getBoolean("ManaPotion.Enabled");
			useSkillTree = config.getBoolean("SkillTree.Enabled");
		} catch (Exception e) {
			WAddonCore.Log.severe("[WHeroesAddon] failed to load config.yml");
		}
	}
	
	protected void saveAll() {
		for (String s : plugin.getSkillTree().playerClasses.keySet()) {
			savePlayerConfig(s);
		}
	}
	   
	
	public void savePlayerConfig(String name) {
		FileConfiguration playerConfig = new YamlConfiguration();
		File playerDataFolder = new File(plugin.getDataFolder(), "data");
		if (!playerDataFolder.exists()) {
			playerDataFolder.mkdir();
		}
		File playerFile = new File(plugin.getDataFolder() + "/data", name + ".yml");
		if (!playerFile.exists()) {
			try {
				playerFile.createNewFile();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return;
			}
		}
		try {
			playerConfig.load(playerFile);
			plugin.getSkillTree().saveSkillTree(playerConfig, name);
			playerConfig.save(playerFile);
		} catch (Exception e) {
			e.printStackTrace();;
		}
	}
	
	public static FileConfiguration getFConfig() { return config; }
	
	public int getPointsPerLevel() { return pointsPerLevel; } 
	public int getHologramTime() { return hologram_time; }
	public boolean areHologramsEnabled() { return holograms; }
	public boolean isUsingJoinChoose() { return useJoinChoose; }
	public boolean isUsingManaPotion() { return useManaPotion; }
	public boolean isUsingSkillTree() { return useSkillTree; }
}
