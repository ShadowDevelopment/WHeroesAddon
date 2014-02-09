package me.Wiedzmin137.wheroesaddon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.desht.scrollingmenusign.SMSException;
import me.desht.scrollingmenusign.SMSHandler;
import me.desht.scrollingmenusign.SMSMenu;
import me.desht.scrollingmenusign.ScrollingMenuSign;
import me.desht.scrollingmenusign.dhutils.LogUtils;
import me.desht.scrollingmenusign.dhutils.MiscUtil;
import me.desht.scrollingmenusign.enums.SMSMenuAction;
import me.desht.scrollingmenusign.views.SMSInventoryView;
import me.desht.scrollingmenusign.views.SMSMapView;
import me.desht.scrollingmenusign.views.SMSView;
import me.desht.scrollingmenusign.views.ViewManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;

public class ItemGUI implements Listener {

    //private HashMap<String, HashMap<String, HashMap<String, Integer>>> playerSkills = new LinkedHashMap<String, HashMap<String, HashMap<String, Integer>>>();
    public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
    public static Logger Logger;
    
    // menu names
    private static final String SKILLTREE = "SkillTree";

    public static SMSHandler smsHandler;
    public static Map<HeroClass, SMSMenu> menus = new HashMap<HeroClass, SMSMenu>();
    public static Map<HeroClass, SMSMapView> views = new HashMap<HeroClass, SMSMapView>();


    public ItemGUI(ScrollingMenuSign sms) {
            smsHandler = sms.getHandler();
            Bukkit.getPluginManager().registerEvents(this, HeroesSkillTree.getInstance());
            createMenus();
    }
    
	//TODO Down was for test
    //public void onEnable() {
	//	SMSMenu SkillTree = smsHandler.createMenu("SkillTree", "&1My Title", "wiedzmin137"); 
	//	if (SkillTree != null) {  
    //		//for (Skill skill : heroes.getSkillManager().getSkills()) {
    //		//	addItem(SKILLTREE, skill.toString(), "/skill " + skill); 
    //		//}
	//		
    //
	//		addItem(SKILLTREE, "Day", "/time day"); 
	//   	addItem(SKILLTREE, "Night", "/time night"); 
	//   	addItem(SKILLTREE, "Compass", "/compass"); 
   	//	}
    //}

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
              menu.addItem(skill.getName(), "/" + skill.getIdentifiers()[0], "");
            }
          }
        }
        menu.setAutosave(true);
        menu.setAutosort(true);
        //TODO There is SMSMapView but I'll change it for SMSItemView
        SMSMapView view = null;
        try {
          view = (SMSMapView)SMSView.getView(name + " view");
        } catch (SMSException e) {
          short id = Bukkit.getServer().createMap((World)Bukkit.getWorlds().get(0)).getId();
          view = new SMSMapView(name + " view", menu);
          view.register();
          view.setMapId(id);
          view.update(menu, me.desht.scrollingmenusign.enums.SMSMenuAction.REPAINT);
        }
        views.put(hc, view);
        view.setAutosave(true);
		
        //TODO Here it's my old code 
        //view.toggleGUI((Player)sender);
    }
		
		// to show/hide the inventory view via code:
		
		//Logger.info("Testing two!");

    		//SMSMenu menu = smsHandler.getMenu("SkillTree");
			//ViewManager mgr = ScrollingMenuSign.getInstance().getViewManager();
			
    		//for (Skill skill : heroes.getSkillManager().getSkills()) {
    		//	addItem(SKILLTREE, skill.toString(), "/skill " + skill); //$NON-NLS-1$
    		//}

			// create an Inventory view
			//SMSInventoryView view = mgr.addInventoryViewToMenu(menu, sender);
			// other attributes are AUTO_POPDOWN (boolean) and SPACING (integer)
			//view.setAttribute(SMSInventoryView.WIDTH, "5");
			
	        /* while(view.hasNext()) {
	         *     String e = (String)view.next();
	         *     Skill id = plugin.getSkillManager().getSkill(e);
	         *     if(id instanceof ActiveSkill) {
	         *        if(id.getIdentifiers().length == 0) {
	         *           Heroes.log(Level.SEVERE, "Skill " + e + " has no valid identifiers and can not be used on the menu!  Please contact the author to fix the skill.");
	         *        } else {
	         *           menu.addItem(id.getName(), "/" + id.getIdentifiers()[0], "");
	         *        }
	         *     }
	         *  }
	         */
    		
    private void createMenus() {
            createMenu(SKILLTREE, "Test"); //$NON-NLS-1$

            setAutosave(false);
    }

    private void createMenu(String name, String title) {
            SMSMenu menu;
            if (!smsHandler.checkMenu(name)) {
                    menu = smsHandler.createMenu(name, title, "wiedzmin137"); //$NON-NLS-1$
                    menu.setAutosort(true);
            } else {
                    try {
                            // clear all menu items - start with a clean slate
                            menu = smsHandler.getMenu(name);
                            menu.setTitle(MiscUtil.parseColourSpec(title));
                            menu.removeAllItems();
                    } catch (SMSException e) {
                            // shouldn't get here - we already checked that the menu exists
                            Logger.warning(e.toString()); //$NON-NLS-1$
                    }
            }
    }
    
    private void addItem(String menuName, String label, String command) {
    	if (smsHandler.checkMenu(menuName)) {
    		try {
    			SMSMenu menu = smsHandler.getMenu(menuName);
    			menu.addItem(label, command, ""); //$NON-NLS-1$
    			menu.notifyObservers(SMSMenuAction.REPAINT);
    		} catch (SMSException e) {
    			// shouldn't get here
    			LogUtils.warning(null, e); //$NON-NLS-1$
    		}
    	}
    }
}
