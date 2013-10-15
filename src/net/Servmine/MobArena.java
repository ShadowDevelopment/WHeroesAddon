package net.Servmine;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
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
import com.herocraftonline.heroes.api.events.HeroKillCharacterEvent;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.effects.common.CombustEffect;
import com.herocraftonline.heroes.characters.effects.common.SummonEffect;
import com.herocraftonline.heroes.util.Properties;
import com.herocraftonline.heroes.util.Util;
     
public final class MobArena implements Listener {
	public MobArena(WHeroesAddon plugin) {
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
 
    public MobArena(Heroes plugin)
    {
      this.plugin = plugin;
    }
      
    private Player getAttacker(EntityDamageEvent event) {
        if (event == null) {
          return null;
        }
        if ((event instanceof EntityDamageByEntityEvent)) {
          Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
          if ((damager instanceof Player))
            return (Player)damager;
          if ((damager instanceof Projectile)) {
            Projectile projectile = (Projectile)damager;
            if ((projectile.getShooter() instanceof Player))
              return (Player)projectile.getShooter();
            if ((projectile.getShooter() instanceof Skeleton)) {
              CharacterTemplate character = this.plugin.getCharacterManager().getCharacter(projectile.getShooter());
              if (character.hasEffect("Summon")) {
                SummonEffect sEffect = (SummonEffect)character.getEffect("Summon");
                return sEffect.getSummoner().getPlayer();
              }
            }
          } else if ((damager instanceof LivingEntity)) {
            if ((damager instanceof Tameable)) {
              Tameable tamed = (Tameable)damager;
              if ((tamed.isTamed()) && ((tamed.getOwner() instanceof Player))) {
                return (Player)tamed.getOwner();
              }
            }
            CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity)damager);
            if (character.hasEffect("Summon")) {
              SummonEffect sEffect = (SummonEffect)character.getEffect("Summon");
              return sEffect.getSummoner().getPlayer();
            }
          }
        } else if ((event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) && ((event.getEntity() instanceof LivingEntity))) {
          CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity)event.getEntity());
          if (character.hasEffect("Combust")) {
            return ((CombustEffect)character.getEffect("Combust")).getApplier();
          }
        }
        return null;
      }

    private void awardKillExp(Hero attacker, LivingEntity defender) {
      Properties prop = Heroes.properties;

      double addedExp = 0.0D;
      HeroClass.ExperienceType experienceType = null;

      if ((attacker.getSummons().contains(defender)) || (attacker.getPlayer().equals(defender))) {
        return;
      }
        
      if (maHandler != null && maHandler.isPlayerInArena(player)) {
        return;
      }

      if ((defender instanceof Player))
      {
        Util.deaths.put(((Player)defender).getName(), defender.getLocation());
        addedExp = prop.playerKillingExp;
        int aLevel = attacker.getTieredLevel(false);
        int dLevel = this.plugin.getCharacterManager().getHero((Player)defender).getTieredLevel(false);
        addedExp *= findExpAdjustment(aLevel, dLevel);
        experienceType = HeroClass.ExperienceType.PVP;
      } else if (((defender instanceof LivingEntity)) && (!(defender instanceof Player))) {
        Monster monster = this.plugin.getCharacterManager().getMonster(defender);
        addedExp = monster.getExperience();

        if ((addedExp == -1.0D) && (!prop.creatureKillingExp.containsKey(defender.getType())))
          return;
        if (addedExp == -1.0D) {
          addedExp = ((Double)prop.creatureKillingExp.get(defender.getType())).doubleValue();
        }
        experienceType = HeroClass.ExperienceType.KILLING;
      }

      if ((experienceType != null) && (addedExp > 0.0D))
        if (attacker.hasParty())
          attacker.getParty().gainExp(addedExp, experienceType, defender.getLocation());
        else if (attacker.canGain(experienceType))
          attacker.gainExp(addedExp, experienceType, defender.getLocation());
    }
    
    
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event)
    {
      LivingEntity defender = event.getEntity();

      Player attacker = getAttacker(defender.getLastDamageCause());
      CharacterManager characterManager = this.plugin.getCharacterManager();
      CharacterTemplate character = characterManager.getCharacter(defender);
      event.setDroppedExp(0);

      if (attacker != null) {
        HeroKillCharacterEvent hkc = new HeroKillCharacterEvent(character, characterManager.getHero(attacker));
        Bukkit.getPluginManager().callEvent(hkc);
      }
      if ((attacker != null) && (!attacker.equals(defender)) && ((defender instanceof LivingEntity))) {
        Hero hero = characterManager.getHero(attacker);
        awardKillExp(hero, defender);
      }
      Hero heroDefender;
      if ((defender instanceof Player)) {
        Player player = (Player)defender;
        heroDefender = (Hero)character;
        Util.deaths.put(player.getName(), event.getEntity().getLocation());
        heroDefender.cancelDelayedSkill();
      }
      else
      {
        this.plugin.getCharacterManager().removeMonster(character.getEntity());
      }

      character.clearEffects();
    }
    
    
    
    private double findExpAdjustment(int aLevel, int dLevel) {
        int diff = aLevel - dLevel;
        if (Math.abs(diff) <= Heroes.properties.pvpExpRange)
          return 1.0D;
        if (diff >= Heroes.properties.pvpMaxExpRange)
          return 0.0D;
        if (diff <= -Heroes.properties.pvpMaxExpRange)
          return 2.0D;
        if (diff > 0)
          return 1.0D - (diff - Heroes.properties.pvpExpRange) / Heroes.properties.pvpMaxExpRange;
        if (diff < 0) {
          return 1.0D + (Math.abs(diff) - Heroes.properties.pvpExpRange) / Heroes.properties.pvpMaxExpRange;
        }
        return 1.0D;
      }
    
    public static enum CombatReason
    {
      DAMAGED_BY_MOB, 
      DAMAGED_BY_PLAYER, 
      ATTACKED_MOB, 
      ATTACKED_PLAYER, 
      BENEFIT_DEFENDER, 
      BENEFIT_ATTACKER, 
      CUSTOM;
    }
}
