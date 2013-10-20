package net.Servmine;

import net.Servmine.WHeroesAddon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import com.garbagemule.MobArena.MobArenaHandler;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.common.CombustEffect;
import com.herocraftonline.heroes.util.Properties;
import com.herocraftonline.heroes.util.Util;
     
public final class MobArenaListener implements Listener {
	public MobArenaListener(WHeroesAddon plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public static MobArenaHandler maHandler;
    public Player player;
	private Heroes plugin;
    
    public void setupMobArenaHandler()
    {
        Plugin maPlugin = Bukkit.getServer().getPluginManager().getPlugin("MobArena");
        
        if (maPlugin == null)
            return;

        maHandler = new MobArenaHandler();
    }
 
    public MobArenaListener(Heroes plugin)
    {
      this.plugin = plugin;
    }

	private Player getAttacker(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player) {
                return (Player) damager;
            } else if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof Player) {
                    return (Player) projectile.getShooter();
                }
            } else if (damager instanceof LivingEntity) {
                if (damager instanceof Tameable) {
                    Tameable tamed = (Tameable) damager;
                    if (tamed.isTamed() && tamed.getOwner() instanceof Player)
                        return (Player) tamed.getOwner();
                }
            }
        }
        return null;
    }

    private void awardKillExp(Hero attacker, LivingEntity defender) {
        Properties prop = Heroes.properties;

        double addedExp = 0;
        ExperienceType experienceType = null;

        // If this entity is on the summon map, don't award XP!
        if (attacker.getSummons().contains(defender) || attacker.getPlayer().equals(defender))
            return;

        if (defender instanceof Player) {
            // Don't award XP for Players killing themselves
            Util.deaths.put(((Player) defender).getName(), defender.getLocation());
            addedExp = prop.playerKillingExp;
            experienceType = ExperienceType.PVP;
        } else if (defender instanceof LivingEntity && !(defender instanceof Player)) {

            // Get the dying entity's CreatureType
            if (defender.getType() != null) {
                // If EXP hasn't been assigned for this Entity then we stop here.
                if (!prop.creatureKillingExp.containsKey(defender.getType()))
                    return;

                addedExp = prop.creatureKillingExp.get(defender.getType());
                experienceType = ExperienceType.KILLING;

                // Check if the kill was near a spawner
                if (prop.noSpawnCamp && Util.isNearSpawner(defender, 10))
                    addedExp *= 0.5;
            }
        }

        if (experienceType != null && addedExp > 0) {
            if (attacker.hasParty())
                attacker.getParty().gainExp(addedExp, experienceType, defender.getLocation());
            else if (attacker.canGain(experienceType))
                attacker.gainExp(addedExp, experienceType, defender.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity defender = event.getEntity();
        //If this is a disabled world ignore it

        Player attacker = getAttacker(defender.getLastDamageCause());
        CharacterManager heroManager = plugin.getCharacterManager();

        event.setDroppedExp(0);

        if (defender instanceof Player && maHandler != null && maHandler.isPlayerInArena(player))  {
            Player player = (Player) defender;
            Hero heroDefender = heroManager.getHero(player);
            Util.deaths.put(player.getName(), event.getEntity().getLocation());
            heroDefender.cancelDelayedSkill();
            // check to see if this death was caused by FireTick
            if (attacker == null && heroDefender.hasEffect("Combust")) {
                attacker = ((CombustEffect) heroDefender.getEffect("Combust")).getApplier();
            }
            
            double multiplier = Heroes.properties.expLoss;
            if (attacker != null) {
                multiplier = Heroes.properties.pvpExpLossMultiplier;
            }
            
            heroDefender.loseExpFromDeath(multiplier, attacker != null);

            // Remove any nonpersistent effects
            for (Effect effect : heroDefender.getEffects()) {
                if (!effect.isPersistent()) {
                    heroDefender.removeEffect(effect);
                }
            }
            
            if (attacker != null && !attacker.equals(defender)) {
                Hero hero = heroManager.getHero(attacker);
                awardKillExp(hero, defender);
            }
        }
    }
}
