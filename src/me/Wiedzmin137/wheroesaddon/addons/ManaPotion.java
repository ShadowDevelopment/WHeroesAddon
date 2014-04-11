package me.Wiedzmin137.wheroesaddon.addons;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ManaPotion implements Listener {
   //FIXME it is probably not work
   //TODO add more functions for potions
   //TODO clean up

   private Material potion;
   private short potionData;
   private boolean regainRand = false;
   private int regain = 0;
   private int regain_min = 0;
   private int regain_max = 0;
   
   @EventHandler(priority = EventPriority.NORMAL)
   public void onPlayerInteract(PlayerInteractEvent event) {
      Action action = event.getAction();
      Player player = event.getPlayer();
      ItemStack material = player.getItemInHand();
      Material materialItemStack = player.getItemInHand().getType();
      if (material.getDurability() == getPotionData()
    		  && materialItemStack == getPotion() 
    		  && action == Action.RIGHT_CLICK_AIR) {
    	  addMana(event.getPlayer());
      }
   }
   
   public void setRegains(int min, int max) {
	      regain_min = min;
	      regain_max = max;
	   }
   
   public void setRegainRand(boolean RegainRand) { regainRand = RegainRand; }
   public void setRegain(int newRegain) { regain = newRegain; }
   public void setPotion(Material mat) { potion = mat; }
   public void setPotionData(short data) { potionData = data; }
   
   public short getPotionData() { return potionData; }
   public Material getPotion() { return potion; }

   public int getRegain() {
      int randNum1;
      if(regainRand) {
         Random rand = new Random();
         randNum1 = rand.nextInt(regain_max - regain_min + 1) + regain_min;
         if(randNum1 < 1) {
            randNum1 = 1;
         }
      } else {
         randNum1 = regain;
      }
      return randNum1;
   }

   private void addMana(Player player) {
      Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero(player);
      int mana = getRegain();
      HeroClass hClass = hero.getHeroClass();
      if (hero.getMana() + mana > hero.getMaxMana()) {
         hero.setMana(hero.getMaxMana());
      } else {
         hero.setMana(hero.getMana() + mana + hero.getLevel(hClass));
      }
      player.sendMessage(ChatColor.AQUA + "You regained " + ChatColor.DARK_AQUA + mana + ChatColor.AQUA + " mana");
      if(player.getItemInHand() != null) {
         if(player.getItemInHand().getAmount() == 1) {
            player.setItemInHand((ItemStack)null);
         } else {
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
         }
      }
   }
}
