package me.Wiedzmin137.wheroesaddon;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.desht.scrollingmenusign.SMSException;
import me.desht.scrollingmenusign.SMSHandler;
import me.desht.scrollingmenusign.SMSMenu;
import me.desht.scrollingmenusign.SMSMenuItem;
import me.desht.scrollingmenusign.ScrollingMenuSign;
import me.desht.scrollingmenusign.dhutils.MiscUtil;
import me.desht.scrollingmenusign.views.SMSInventoryView;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

public class ItemGUI implements Listener {

    //private HashMap<String, HashMap<String, HashMap<String, Integer>>> playerSkills = new LinkedHashMap<String, HashMap<String, HashMap<String, Integer>>>();
    public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
    public static Logger Logger;
    
    // menu names
    private static final String SKILLTREE = "SkillTree";

    public static SMSHandler smsHandler;
    public static Map<HeroClass, SMSMenu> menus = new HashMap<HeroClass, SMSMenu>();
    public static Map<HeroClass, SMSInventoryView> views = new HashMap<HeroClass, SMSInventoryView>();


    public ItemGUI(ScrollingMenuSign sms) {
    	smsHandler = sms.getHandler();
    	Bukkit.getPluginManager().registerEvents(this, HeroesSkillTree.getInstance());
    	createMenus();
    }

    public void setAutosave(boolean autosave) {
    	for (SMSMenu menu : smsHandler.listMenus()) {
    		menu.setAutosave(autosave);
    	}
    }

    public void createSkillTree(CommandSender sender, HeroClass hc, Heroes plugin) {
        String name = hc.getName();
        SMSMenu menu = null;
        
        if (smsHandler == null) {
          return;
        }
        
        try {
          menu = smsHandler.getMenu(name + " menu");
        } catch (SMSException e) {
          menu = smsHandler.createMenu(name + " menu", name + " Skills", name);
        }
        if (menu == null) {
          menu = smsHandler.createMenu(name + " menu", name + " Skills", name);
        }
        menu.removeAllItems();
        
        menu.setAutosave(false);
        menu.setAutosort(false);
        
        menus.put(hc, menu);
        for (String sn : hc.getSkillNames()) {
          Skill skill = plugin.getSkillManager().getSkill(sn);
          if ((skill instanceof ActiveSkill)) {
            if (skill.getIdentifiers().length == 0) {
              Heroes.log(Level.SEVERE, "Skill " + sn + " has no valid identifiers and can not be used on the menu!  Please contact the author to fix the skill.");
            }
            else {
              //String indicator;
              //if (is != null) {
              //ItemStack indicator = new ItemStack(Material.NETHER_STAR);
              
              //String finalIndicator = getItemStack(SkillConfigManager.getSetting(hc, skill, indicator));
              
              //}	
            	
              //indicator1 = SkillConfigManager.getSetting(hc, skill, "indicator", false);
              
              
              
              
              //if ((SkillConfigManager.getSetting(hc, skill, finalIndicator) = ) {
              //	  String indicator = SkillConfigManager.getUseSetting(name, skill, "indicator", 0.0D, false);
              //} else {
            	  
              //String finalIndicator1 = "nether_star";
              //ItemStack finalIndicator2 = new ItemStack(Material.NETHER_STAR);
              //String finalIndicator3 = getItemStack(finalIndicator2);
              //}
              
              String finalIndicator = (String)SkillConfigManager.getSetting(hc, skill, "hst-indicator");
              //if (finalIndicator != null) {
              //	  finalIndicator = "nether_star";
              //}
    
              SMSMenuItem skillsClass = new SMSMenuItem(menu, ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + skill.getName(), "/" + skill.getIdentifiers()[0], "", finalIndicator, new String[] { ChatColor.YELLOW + "Click for use!" });
              menu.addItem(skillsClass);
            }
          }
        }
        menu.setAutosave(true);
        menu.setAutosort(true);

        SMSInventoryView view = null;
        try {
          sender.sendMessage("Yea");
          view = new SMSInventoryView(name + " view", menu);
          view.update(menu, me.desht.scrollingmenusign.enums.SMSMenuAction.REPAINT);
          //view = (SMSInventoryView)SMSView.getView(name + " view");
        } catch (SMSException e) {
          sender.sendMessage("Nope ;(");
          view = new SMSInventoryView(name + " view", menu);
          view.update(menu, me.desht.scrollingmenusign.enums.SMSMenuAction.REPAINT);
        }
        views.put(hc, view);
        view.setAutosave(true);
		
        view.toggleGUI((Player)sender);
    }
    		
    private void createMenus() {
    	createMenu(SKILLTREE, "Test"); //$NON-NLS-1$
    	setAutosave(false);
    }

    private void createMenu(String name, String title) {
    	SMSMenu menu;
    	if (!smsHandler.checkMenu(name)) {
    		menu = smsHandler.createMenu(name, title, "wiedzmin137");
    		menu.setAutosort(true);
    	} else {
    		try {
    			menu = smsHandler.getMenu(name);
    			menu.setTitle(MiscUtil.parseColourSpec(title));
    			menu.removeAllItems();
    		} catch (SMSException e) {
    			Logger.warning(e.toString());
    		}
    	}
    }
}
