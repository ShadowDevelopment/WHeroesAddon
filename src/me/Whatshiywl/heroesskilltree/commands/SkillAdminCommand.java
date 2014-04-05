package me.Whatshiywl.heroesskilltree.commands;

import com.herocraftonline.heroes.characters.Hero;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.Lang;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillAdminCommand {
   //TODO check language support - need testers!
   public static void skillAdmin(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(args.length < 2) {
         sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_ADMIN_NOT_ENOUGH_ARGUMENTS);
      } else {
         Hero hero;
         if(args[1].equalsIgnoreCase("clear")) {
            if(!sender.hasPermission("skilladmin.clear")) {
            	sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PERMISSION_DENIED);
            } else {
               if(args.length == 3) {
                  if(Bukkit.getPlayer(args[2]) == null) {
                     sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PLAYER_OFFLINE.toString().replace("%player%", args[2]));
                     return;
                  }
                  hero = HeroesSkillTree.heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[2]));
                  hst.setPlayerPoints(hero, 0);
               } else {
                  if(!(sender instanceof Player)) {
                	 sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_IN_CONSOLE_DENIED);
                     return;
                  }
                  hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
                  hst.setPlayerPoints(hero, 0);
               }
               sender.sendMessage(Lang.TITLE.toString() + Lang.ADMIN_RESET_SUCCESS.toString().replace("%player%", args[2]));
            }
         } else if(args[1].equalsIgnoreCase("reset")) {
            if(!sender.hasPermission("skilladmin.reset")) {
            	sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PERMISSION_DENIED);
            } else {
               if(args.length == 3) {
                  if(Bukkit.getPlayer(args[2]) != null) {
                     hst.resetPlayer(Bukkit.getPlayer(args[2]));
                     sender.sendMessage(Lang.TITLE.toString() +  Lang.ADMIN_RESET_SUCCESS.toString().replace("%player%", args[2]));
                  } else {
                     sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PLAYER_OFFLINE.toString().replace("%player", args[2]));
                  }
               } else {
                  if(!(sender instanceof Player)) {
                     sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_IN_CONSOLE_DENIED.toString().replace("%player%", args[2]));
                     return;
                  }
                  hst.resetPlayer((Player)sender);
                  sender.sendMessage(Lang.TITLE.toString() + Lang.SELF_RESET_SUCCESS);
               }
            }
         } else if(args.length < 3) {
            sender.sendMessage(Lang.WRONG_CMD_USAGE.toString());
         } else if(args[1].equalsIgnoreCase("set")) {
            if(!sender.hasPermission("skilladmin.set")) {
               sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PERMISSION_DENIED);
            } else {
               if(args.length > 3) {
                  if(Bukkit.getPlayer(args[3]) != null) {
                     hero = HeroesSkillTree.heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[2]));
                     hst.setPlayerPoints(hero, Integer.parseInt(args[2]));
                     sender.sendMessage(Lang.TITLE.toString() + Lang.ADMIN_SKILLPOINTS_ADD_SUCCESS.toString().replace("%player%", args[3]).replace("%points%", args[2]));
                  } else {
                     sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PLAYER_OFFLINE.toString().replace("%player%", args[3]));
                  }
               } else {
                  if(!(sender instanceof Player)) {
                     sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_IN_CONSOLE_DENIED);
                     return;
                  }
                  hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
                  hst.setPlayerPoints(hero, Integer.parseInt(args[2]));
                  sender.sendMessage(Lang.TITLE.toString() + Lang.ADMIN_SKILLPOINTS_ADD_SUCCESS.toString().replace("%player%", "your").replace("%points%", args[2]));
               }
            }
         } else if(args[1].equalsIgnoreCase("give")) {
            if(!sender.hasPermission("skilladmin.give")) {
               sender.sendMessage(Lang.ERROR_PERMISSION_DENIED.toString());
            } else {
               if(args.length > 3) {
                  if(Bukkit.getPlayer(args[3]) != null) {
                     hero = HeroesSkillTree.heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[3]));
                     hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) + Integer.parseInt(args[2]));
                     sender.sendMessage(Lang.TITLE.toString()+ Lang.ADMIN_SKILLPOINTS_ADD_SUCCESS.toString().replace("%player%", args[3]) + Integer.parseInt(args[2]) + ".");
                  } else {
                     sender.sendMessage(Lang.ERROR_PLAYER_OFFLINE.toString().replace("%player%", args[2]));
                  }
               } else {
                  if(!(sender instanceof Player)) {
                     sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
                     return;
                  }
                  hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
                  hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) + Integer.parseInt(args[2]));
                  sender.sendMessage(Lang.TITLE.toString() + Lang.ADMIN_SKILLPOINTS_REMOVE_SUCCESS.toString().replace("%player%", "your").replace("%skillpoints%", args[2]));
               }

            }
         } else {
            if(args[1].equalsIgnoreCase("remove")) {
               if(sender.hasPermission("skilladmin.remove")) {
                  sender.sendMessage(Lang.ERROR_PERMISSION_DENIED.toString());
                  return;
               }
               if(args.length > 3) {
                  if(Bukkit.getPlayer(args[3]) != null) {
                     hero = HeroesSkillTree.heroes.getCharacterManager().getHero(Bukkit.getPlayer(args[3]));
                     hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) - Integer.parseInt(args[2]));
                     sender.sendMessage(Lang.TITLE.toString() + Lang.ADMIN_SKILLPOINTS_REMOVE_SUCCESS.toString().replace("%player%", args[3]).replace("%skillpoints%", args[2]));
                  } else {
                     sender.sendMessage(Lang.ERROR_PLAYER_OFFLINE.toString().replace("%player%", args[2]));
                  }
               } else {
                  if(!(sender instanceof Player)) {
                     sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
                     return;
                  }
                  hero = HeroesSkillTree.heroes.getCharacterManager().getHero((Player)sender);
                  hst.setPlayerPoints(hero, hst.getPlayerPoints(hero) - Integer.parseInt(args[2]));
                  sender.sendMessage(Lang.TITLE.toString() + Lang.ADMIN_SKILLPOINTS_REMOVE_SUCCESS.toString().replace("%player%", "yourself").replace("%skillpoints%", args[2]));
               }
            }
         }
      }
   }
}
