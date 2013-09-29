package net.Servmine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class WRPG extends JavaPlugin {
	
	

  public Objective o; //Creates a objective called o
  public Scoreboard timerBoard = null; //Creates a scoreboard called timerBoard(You will see what thats used for later)
  public Objective timerObj = null; // Same as above but it creates a objective called timerObj
   
  @Override
  public void onEnable() {
   
  Scoreboard board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
   
  o = board.registerNewObjective("timer", "dummy"); //Registering the objective needed for the timer
  o.setDisplayName(ChatColor.GREEN + "Czary   " + ChatColor.GRAY + " | " + ChatColor.GOLD + "   Mana"); // Setting the title for the scoreboard. This would look like: TCGN | Walls
  o.setDisplaySlot(DisplaySlot.SIDEBAR); //Telling the scoreboard where to display when we tell it to display
   
  this.timerBoard = board; //Setting timerBoard equal to board.
  this.timerObj = o; //Setting timerObj equal to o. This makes it so we can access it by typing plugin.timerObj
  }


}