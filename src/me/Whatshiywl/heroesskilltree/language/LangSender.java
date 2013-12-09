package me.Whatshiywl.heroesskilltree.language;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LangSender
{
    private static final Logger log = Logger.getLogger("Minecraft");
    
    private static final String prefix = "[MobArena] ";
    
    private LangSender() {}

    public static boolean tell(CommandSender p, String msg) {
        // If the input sender is null or the string is empty, return.
        if (p == null || msg.equals(" ")) {
            return false;
        }

        // Otherwise, send the message with the [HeroesSkillTree] tag.
        p.sendMessage(ChatColor.DARK_BLUE + "[" + ChatColor.BLUE + "HeroesSkillTree" + ChatColor.DARK_BLUE + "] " + ChatColor.RESET + msg);
        return true;
    }

    public static boolean tell(CommandSender p, LangList msg, String s) {
        return tell(p, msg.format(s));
    }

    public static boolean tell(CommandSender p, LangList msg) {
        return tell(p, msg.toString());
    }

    public static void announce(String msg) {
        List<Player> players = new ArrayList<Player>();
        for (Player p : players) {
            tell(p, msg);
        }
    }

    public static void announce(LangList msg, String s) {
        announce(msg.format(s));
    }

    public static void announce(LangList msg) {
        announce(msg.toString());
    }
    
    public static void info(String msg) {
        log.info(prefix + msg);
    }
    
    public static void warning(String msg) {
        log.warning(prefix + msg);
    }
    
    public static void severe(String msg) {
        log.severe(prefix + msg);
    }
}
