package net.Servmine;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.garbagemule.MobArena.events.ArenaPlayerDeathEvent;
import com.garbagemule.MobArena.events.ArenaPlayerLeaveEvent;
import com.garbagemule.MobArena.framework.Arena;
import com.herocraftonline.heroes.api.events.SkillDamageEvent;

public class MAaddon implements Listener
{
    private HashMap<Arena,HashSet<String>> godMAMap = new HashMap<Arena,HashSet<String>>();
	
	
    @EventHandler
    public void onMAPlayerDeath(ArenaPlayerDeathEvent event)
    {
        Arena arena = event.getArena();
        Player p = event.getPlayer();
        if(godMAMap.get(arena) == null) {return;}
        if(godMAMap.get(arena).contains(p.getName()));
    }
    
    @EventHandler
    public void onMAPlayerLeave(ArenaPlayerLeaveEvent event)
    {
        Arena arena = event.getArena();
        Player p = event.getPlayer();
        if(godMAMap.get(arena) == null) {return;}
        if(godMAMap.get(arena).contains(p.getName()));
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHeroesSkillDmg(SkillDamageEvent event)
    {
        if(!(event.getEntity() instanceof Player)) {return;}
        event.setCancelled(true);
    }
}
