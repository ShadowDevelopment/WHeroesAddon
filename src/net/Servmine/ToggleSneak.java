package net.Servmine;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ToggleSneak implements Listener
{
 
	private WRPG plugin; //Making it so we can access the scoreboard board and objective o
	
	public boolean PlayerToggleSneakEvent (Player player, final boolean isSneaking) {
		if (PlayerToggleSneakEvent(player, isSneaking)) {
			return true;
		}
		return false;
	}
    
	public ToggleSneak(WRPG plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void pjoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer(); //Defining the player
		boolean isSneaking = true;
		
		if(PlayerToggleSneakEvent(p, isSneaking) == true) { //Getting online players
//TODO			p.setScoreboard(plugin.timerBoard); //Making it so the player can see the scoreboard
			p.sendMessage("You must be a player!");
		}
//		if(PlayerToggleSneakEvent(p, isSneaking) == false) { //If countdown == 0.
//			plugin.getServer().getScheduler().cancelTasks(plugin); //Stopping it from running. You can also add another scoreboard to take over the timer one!
//		}
	}
}
