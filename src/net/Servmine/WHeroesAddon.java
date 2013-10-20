package net.Servmine;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import com.garbagemule.MobArena.framework.ArenaMaster;
import com.garbagemule.MobArena.MobArena;
     
public final class WHeroesAddon extends JavaPlugin {
    private static boolean foundMA = false;
    private static Logger log = Logger.getLogger("Minecraft");
    private static PluginDescriptionFile info;
    public static ArenaMaster am;	
	
    public void onEnable() {
        setupListeners();
        info = getDescription();
    }
    
    private void setupListeners()
    {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new MobArenaListener(this), this);
        
        MobArena maPlugin = (MobArena)pm.getPlugin("MobArena");
    	
        if(maPlugin != null && maPlugin.isEnabled()) {
            am = maPlugin.getArenaMaster();
            pm.registerEvents(new MobArenaListener(this), this);
            foundMA = true;
            printToConsole("Znaleziono Mob Arene!", false);
        }
    }
    
    public static void printToConsole(String msg, boolean warn)
    {
        if(warn)
            log.warning("[" + info.getName() + "] " + msg);
        else
            log.info("[" + info.getName() + "] " + msg);
    }
    
    public static boolean foundMA()
    {
        return foundMA;
    }
}