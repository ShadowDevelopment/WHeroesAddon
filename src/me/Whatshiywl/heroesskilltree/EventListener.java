package me.Whatshiywl.heroesskilltree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.ClassChangeEvent;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.api.events.SkillDamageEvent;
import com.herocraftonline.heroes.api.events.SkillUseEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
  //TODO speed up this class
  //TODO translate messages

  private static HeroesSkillTree plugin;
  
  @EventHandler
  public void onPluginEnable(PluginEnableEvent event) {
    if (event.getPlugin().getDescription().getName().equals("Heroes")) {
      HeroesSkillTree.heroes = (Heroes)event.getPlugin();
    }
  }
  
  @EventHandler
  public void onPluginDisable(PluginDisableEvent event) {
    if (event.getPlugin().getDescription().getName().equals("Heroes")) {
      Bukkit.getPluginManager().disablePlugin(plugin);
    }
  }
  
  @EventHandler(priority=EventPriority.LOW)
  public void onEntityKill(EntityDeathEvent e) {
	  //TODO it will be hard but I'll try to DELETE all calcuations and only GET proper value from Hereos
	  //FIXME sometimes too long current experience 
	  if (e.getEntity().getKiller() instanceof Player) {		
		  if ((e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) 
				  && (e.getEntity().getKiller() instanceof Player) 
				  && (plugin.areHologramsEnabled())) {
			  Player p = e.getEntity().getKiller();
			  Hero killingHero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)e.getEntity().getKiller());
			  HeroClass heroClass = killingHero.getHeroClass();

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
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    final Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero(player);
    try {
        plugin.loadPlayerConfig(player.getName());
    } catch (NullPointerException e) {
        plugin.savePlayerConfig(player.toString());
    }
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
    if (hero.getHeroClass() != event.getHeroClass()) {return;}
    hero.getPlayer().sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "SkillPoints: " + plugin.getPlayerPoints(hero));
    
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
    final ClassChangeEvent e = event;

    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
      public void run() {
        boolean reset = false;
        if (e.getTo().isDefault()) {
          reset = true;
          for (HeroClass hClass : HeroesSkillTree.heroes.getClassManager().getClasses()) {
            if (hero.getExperience(hClass) != 0.0D) {
              reset = false;
              break;
            }
          }
        }
        if (reset) {
          EventListener.plugin.resetPlayer(hero.getPlayer());
        } else {
          EventListener.plugin.recalcPlayerPoints(hero, e.getTo());
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
  
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onPlayerUseSkill(SkillUseEvent event) {
    Hero hero = event.getHero();
    Skill skill = event.getSkill();
    if ((plugin.isLocked(event.getHero(), event.getSkill())) && (!event.getPlayer().hasPermission("skilltree.override.locked"))) {
      event.getPlayer().sendMessage(org.bukkit.ChatColor.RED + "This skill is still locked! /skillup (skill) to unlock it.");
      event.getHero().hasEffect(event.getSkill().getName());
      event.setCancelled(true);
      return;
    }
    
    int health = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-health", 0, false) 
    	* plugin.getSkillLevel(hero, skill);
    health = (health > 0) ? health : 0;
    event.setHealthCost(event.getHealthCost() + health);

    int mana = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-mana", 0, false)
    	* plugin.getSkillLevel(hero, skill);
    mana = (mana > 0) ? mana : 0;
    event.setManaCost(event.getManaCost() - mana);

    int reagent = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-reagent", 0, false) 
    	* plugin.getSkillLevel(hero, skill);
    reagent = (reagent > 0) ? reagent : 0;
    ItemStack is = event.getReagentCost();
    if (is != null) { is.setAmount(event.getReagentCost().getAmount() - reagent); }
    event.setReagentCost(is);

    int stamina = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-stamina", 0, false)
    	* plugin.getSkillLevel(hero, skill);
    stamina = (stamina > 0) ? stamina : 0;
    event.setStaminaCost(event.getStaminaCost() - stamina);
  }
  
  //TODO test "hst-damage" feature
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onSkillDamage(SkillDamageEvent event) {
	  Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)event.getDamager());  
	  Skill skill = event.getSkill();
	  
	  double damage = (int)SkillConfigManager.getUseSetting(hero, skill, "hst-damage", 0, false) 
			  * plugin.getSkillLevel(hero, skill);
	    int firstDamage = (int)SkillConfigManager.getUseSetting(hero, skill, "damage", 0, false);
	    damage = (damage > 0) ? damage : 0;
	    
	  event.setDamage(firstDamage + damage);
  }
  
    //TODO do that!
//  @EventHandler
//  public void onSkillComplete(SkillCompleteEvent event) {
//	  Hero hero = event.getHero();
//	  Skill skill = event.getSkill();
//  }
  
  //TODO delete that, instead of that add more get features 
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
  
  //TODO delete that, instead of that add more get features 
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
