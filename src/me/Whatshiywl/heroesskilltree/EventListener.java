package me.Whatshiywl.heroesskilltree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.ClassChangeEvent;
import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.api.events.SkillUseEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
	//TODO speed up this class
	//TODO translate messages
	
	private static HeroesSkillTree plugin; 
	public EventListener(HeroesSkillTree instance) {
		plugin = instance;
	}
	  
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().getDescription().getName().equals("Heroes")) {
			HeroesSkillTree.heroes = (Heroes)event.getPlugin();
		}
	}
	  
	@EventHandler
	public void onPluginDisable(org.bukkit.event.server.PluginDisableEvent event) {
		if (event.getPlugin().getDescription().getName().equals("Heroes")) {
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
	  
	@EventHandler(priority=EventPriority.LOW)
	public void onEntityKill(ExperienceChangeEvent e) {
      Hero hero = e.getHero();
      Player player = hero.getPlayer();
      HeroClass heroClass = e.getHeroClass();
      
	  if ((e.getSource() == HeroClass.ExperienceType.KILLING) && (plugin.areHologramsEnabled())) {
		  double change = e.getExpChange();
		  double current = hero.currentXPToNextLevel(heroClass);
		  
		  double exp = hero.getExperience(heroClass);
		  int level = Properties.getLevel(exp);
		  double maxExperiation = Properties.getTotalExp(level + 1) - Properties.getTotalExp(level);
		  
		  HeroesSkillTree.expMessage(player, e.getLocation().subtract(0.0D, 0.5D, 0.0D), 
				  change, maxExperiation, Math.round(current) + change);
	  }
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    final Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero(player);
    plugin.loadPlayerConfig(player.getName());
    plugin.recalcPlayerPoints(hero, hero.getHeroClass());
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
      public void run() {
        for (Effect effect : hero.getEffects()) {
          Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(effect.getName());
          if (skill != null) {
            if (plugin.isLocked(hero, skill))
              hero.removeEffect(effect);
          }
        }
      }
    }, 1L);
  }
  
  @EventHandler(priority=EventPriority.MONITOR)
  public void onLevelChangeEvent(HeroChangeLevelEvent event) {
    final Hero hero = event.getHero();
    plugin.setPlayerPoints(hero, event.getHeroClass(), 
    plugin.getPlayerPoints(hero) + (event.getTo() - event.getFrom()) * plugin.getPointsPerLevel());
    plugin.savePlayerConfig(hero.getPlayer().getName());
    if (hero.getHeroClass() != event.getHeroClass()) return;
    hero.getPlayer().sendMessage(org.bukkit.ChatColor.GOLD + "[HST] " + org.bukkit.ChatColor.AQUA + "SkillPoints: " + plugin.getPlayerPoints(hero));
    
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
      public void run() {
        for (Effect effect : hero.getEffects()) {
          Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(effect.getName());
          if (skill != null) {
            if (plugin.isLocked(hero, skill))
              hero.removeEffect(effect);
          }
        }
      }
    }, 1L);
  }

  @EventHandler(priority=EventPriority.MONITOR)
  public void onClassChangeEvent(ClassChangeEvent event) {
    final Hero hero = event.getHero();
    final ClassChangeEvent evt = event;

    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
      public void run() {
        boolean reset = false;
        if (evt.getTo().isDefault()) {
          reset = true;
          for (HeroClass hClass : HeroesSkillTree.heroes.getClassManager().getClasses()) {
            if (hero.getExperience(hClass) != 0.0D) {
              reset = false;
              break;
            }
          }
        }
        if (reset) {
          plugin.resetPlayer(hero.getPlayer());
        } else {
          plugin.recalcPlayerPoints(hero, evt.getTo());
        }
        for (Effect effect : hero.getEffects()) {
          Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(effect.getName());
          if (skill != null) {
            if (plugin.isLocked(hero, skill))
              hero.removeEffect(effect);
          }
        }
      }
    }, 1L);
  }
  
  @EventHandler(priority=EventPriority.MONITOR)
  public void onPlayerUseSkill(SkillUseEvent event) {
    Hero hero = event.getHero();
    Skill skill = event.getSkill();
    if ((plugin.isLocked(event.getHero(), event.getSkill())) && (!event.getPlayer().hasPermission("skilltree.override.locked"))) {
      event.getPlayer().sendMessage(org.bukkit.ChatColor.RED + "This skill is still locked! /skillup (skill) to unlock it.");
      event.getHero().hasEffect(event.getSkill().getName());
      event.setCancelled(true);
      return;
    }
    
    int health = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-health", 0.0D, false) 
    	* plugin.getSkillLevel(hero, skill);
    health = (health > 0) ? health : 0;
    event.setHealthCost(event.getHealthCost() + health);

    int mana = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-mana", 0.0D, false)
    	* plugin.getSkillLevel(hero, skill);
    mana = (mana > 0) ? mana : 0;
    event.setManaCost(event.getManaCost() - mana);

    int reagent = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-reagent", 0.0D, false) 
    	* plugin.getSkillLevel(hero, skill);
    reagent = (reagent > 0) ? reagent : 0;
    ItemStack is = event.getReagentCost();
    if (is != null) { is.setAmount(event.getReagentCost().getAmount() - reagent); }
    event.setReagentCost(is);

    int stamina = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-stamina", 0.0D, false)
    	* plugin.getSkillLevel(hero, skill);
    stamina = (stamina > 0) ? stamina : 0;
    event.setStaminaCost(event.getStaminaCost() - stamina);
  }
  
  //TODO test "hst-damage" feature
//  @EventHandler
//  public void onSkillDamage(SkillDamageEvent event) {
//	  Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)event.getDamager());  
//	  Skill skill = event.getSkill();
//	  
//	  double damage = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-damage", 0.0D, false) 
//			  * plugin.getSkillLevel(hero, skill);
//	    int firstDamage = (int)SkillConfigManager.getUseSetting(hero, skill, "damage", 0.0D, false);
//	    damage = (damage > 0) ? damage : 0;
//	    
//	  event.setDamage(firstDamage + damage);
//  }
  
    //TODO do that!
//  @EventHandler
//  public void onSkillComplete(SkillCompleteEvent event) {
//	  Hero hero = event.getHero();
//	  Skill skill = event.getSkill();
//  }
}
