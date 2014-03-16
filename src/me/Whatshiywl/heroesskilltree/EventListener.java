package me.Whatshiywl.heroesskilltree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.ClassChangeEvent;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.api.events.SkillUseEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
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
  public void onEntityKill(EntityDeathEvent e) {
	  if (e.getEntity().getKiller() instanceof Player) {		
		  if ((e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) 
				  && (e.getEntity().getKiller() instanceof Player) 
				  && (plugin.areHologramsEnabled())) {
			  Player p = e.getEntity().getKiller();
			  Hero killingHero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)e.getEntity().getKiller());
			  HeroClass heroClass = killingHero.getHeroClass();
				
			  //double total = killingHero.getExperience(heroClass);
			  //double needed = killingHero.getMaxExperiance(level);
			  //int level = killingHero.getLevel();
			  double addedExperiation = getKillExp(killingHero, e.getEntity()) * heroClass.getExpModifier();
			  double current = killingHero.currentXPToNextLevel(heroClass);
			  double exp = killingHero.getExperience(heroClass);
			  int level = Properties.getLevel(exp);
			  double maxExperiation = Properties.getTotalExp(level + 1) - Properties.getTotalExp(level);
			  
			  p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
			  if (plugin.areHologramsEnabled()) {
				  HeroesSkillTree.expMessage(p, e.getEntity().getLocation().subtract(0.0D, 0.5D, 0.0D), 
						  addedExperiation, maxExperiation, current + addedExperiation);
			  }
		  }
	  }
  }
  
  @EventHandler
  public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
    Player player = event.getPlayer();
    final Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero(player);
    plugin.loadPlayerConfig(player.getName());
    plugin.recalcPlayerPoints(hero, hero.getHeroClass());
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
      public void run() {
        for (Effect effect : hero.getEffects()) {
          Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(effect.getName());
          if (skill != null) {
            if (EventListener.plugin.isLocked(hero, skill))
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
            if (EventListener.plugin.isLocked(hero, skill))
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
          for (com.herocraftonline.heroes.characters.classes.HeroClass hClass : HeroesSkillTree.heroes.getClassManager().getClasses()) {
            if (hero.getExperience(hClass) != 0.0D) {
              reset = false;
              break;
            }
          }
        }
        if (reset) {
          EventListener.plugin.resetPlayer(hero.getPlayer());
        } else {
          EventListener.plugin.recalcPlayerPoints(hero, evt.getTo());
        }
        for (Effect effect : hero.getEffects()) {
          Skill skill = HeroesSkillTree.heroes.getSkillManager().getSkill(effect.getName());
          if (skill != null) {
            if (EventListener.plugin.isLocked(hero, skill))
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
    //TODO test "hst-damage" feature
    //TODO repair them all (@gabizou have said they looks OK but they not good like he said)
    int damage = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-damage", 0.0D, false) 
    	* plugin.getSkillLevel(hero, skill);
    int firstDamage = (int)SkillConfigManager.getUseSetting(hero, skill, "damage", 0.0D, false);
    damage = (damage > 0) ? damage : 0;
    event.getHero().setSkillSetting(skill, "damage", firstDamage + damage);
    
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
  
  private double getKillExp(Hero attacker, LivingEntity defender) {
      Properties prop = Heroes.properties;
      HeroClass.ExperienceType experienceType = null;
      if (!attacker.isOwnedSummon(defender) && !attacker.getPlayer().equals(defender)) {
         double addedExp = 0.0D;
         if(defender instanceof Player) {
            addedExp = prop.playerKillingExp;
            int aLevel = attacker.getTieredLevel(false);
            int dLevel = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)defender).getTieredLevel(false);
            addedExp *= findExpAdjustment(aLevel, dLevel);
            experienceType = HeroClass.ExperienceType.PVP;
         } else if(defender instanceof LivingEntity && !(defender instanceof Player)) {
            Monster monster = HeroesSkillTree.heroes.getCharacterManager().getMonster(defender);
            addedExp = (double) monster.getExperience();
            if(addedExp == -1.0D && !prop.creatureKillingExp.containsKey(defender.getType())) {
            }

            if(addedExp == -1.0D) {
               addedExp = prop.creatureKillingExp.get(defender.getType()).doubleValue();
            }

            if(prop.noSpawnCamp && monster.getSpawnReason() == SpawnReason.SPAWNER) {
               addedExp *= prop.spawnCampExpMult;
            }
         }
         if(experienceType != null && addedExp > 0.0D) { return addedExp; }
         return addedExp;
      }
      return 1.0D;
   }
  
//  private double findExpAdjustment(int aLevel, int dLevel) {
//	  //It works but it's MADNESS
//      int diff = aLevel - dLevel;
//      return (Math.abs(diff) <= Heroes.properties.pvpExpRange)
//    		  ? 1.0D : ((diff >= Heroes.properties.pvpMaxExpRange)
//    		  	? 0.0D : ((diff <= -Heroes.properties.pvpMaxExpRange)
//    		  	  ? 2.0D : ((diff > 0)
//    		  	    ? 1.0D - (double)((diff - Heroes.properties.pvpExpRange) / Heroes.properties.pvpMaxExpRange) : ((diff < 0) 
//    		  	      ? 1.0D + (double)((Math.abs(diff) - Heroes.properties.pvpExpRange) / Heroes.properties.pvpMaxExpRange)
//    		  	    	: 1.0D))));
//  }
  
  private double findExpAdjustment(int aLevel, int dLevel){
    int diff = aLevel - dLevel;
    if (Math.abs(diff) <= Heroes.properties.pvpExpRange) {
      return 1.0D;
    }
    if (diff >= Heroes.properties.pvpMaxExpRange) {
      return 0.0D;
    }
    if (diff <= -Heroes.properties.pvpMaxExpRange) {
      return 2.0D;
    }
    if (diff > 0) {
      return 1.0D - (diff - Heroes.properties.pvpExpRange) / Heroes.properties.pvpMaxExpRange;
    }
    if (diff < 0) {
      return 1.0D + (Math.abs(diff) - Heroes.properties.pvpExpRange) / Heroes.properties.pvpMaxExpRange;
    }
    return 1.0D;
  }
}
