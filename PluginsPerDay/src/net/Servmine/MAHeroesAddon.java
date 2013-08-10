package net.Servmine;

import java.util.logging.Logger;

import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.framework.ArenaMaster;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public final class MAHeroesAddon extends JavaPlugin {
	public void setupMobArenaListener(MAaddon maListener)
	{
	    Plugin maPlugin = (MobArena) Bukkit.getServer().getPluginManager().getPlugin("MobArena");
	    
	    if (maPlugin == null)
	        return;

	}
	
    private static Logger log = Logger.getLogger("Minecraft");
    private static PluginDescriptionFile info;
    public static ArenaMaster am;
    private static boolean foundMA = false;
    
    public void onEnable()
    {
        info = getDescription();

        printToConsole("v" + info.getVersion() + " Wlaczono pomyslnie! Autorzy: " + info.getAuthors(), false);
    }
	


    public static void printToConsole(String msg, boolean warn)
    {
        if(warn)
            log.warning("[" + info.getName() + "] " + msg);
        else
            log.info("[" + info.getName() + "] " + msg);
    }
    public static void printToPlayer(Player p, String msg, boolean warn)
    {
        String color = "";
        if(warn)
            color += ChatColor.RED + "";
        else
            color += ChatColor.AQUA + "";
        color += "[MAHeroesAddon]";
        p.sendMessage(color + ChatColor.WHITE + msg);
    }
		
    public static boolean foundMA()
    {
        return foundMA;
    }
 
}