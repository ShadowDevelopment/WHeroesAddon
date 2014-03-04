package me.Wiedzmin137.wheroesaddon;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.desht.scrollingmenusign.SMSException;
import me.desht.scrollingmenusign.SMSHandler;
import me.desht.scrollingmenusign.SMSMenu;
import me.desht.scrollingmenusign.SMSMenuItem;
import me.desht.scrollingmenusign.ScrollingMenuSign;
import me.desht.scrollingmenusign.views.SMSInventoryView;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

public class ItemGUI implements Listener {

	public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
    public static Logger Logger;

    public static SMSHandler smsHandler;
    public static Map<HeroClass, SMSMenu> menus = new HashMap<HeroClass, SMSMenu>();
    public static Map<HeroClass, SMSInventoryView> views = new HashMap<HeroClass, SMSInventoryView>();
    
    public ItemGUI(ScrollingMenuSign sms) {
    	smsHandler = sms.getHandler();
    	Bukkit.getPluginManager().registerEvents(this, HeroesSkillTree.getInstance());
    }

    public void setAutosave(boolean autosave) {
    	for (SMSMenu menu : smsHandler.listMenus()) {
    		menu.setAutosave(autosave);
    	}
    }

    public void createSkillTree(CommandSender sender, HeroClass hc, Heroes plugin, HeroesSkillTree hst) {
    	//TODO cleanup. Some things and change some names
        String name = hc.getName();
        SMSMenu menu = null;
        Hero commandSendingHero = heroes.getCharacterManager().getHero((Player) sender);

        
        if (smsHandler == null) {
          return;
        }
        
        try {
          menu = smsHandler.getMenu(name + " SkillTree");
        } catch (SMSException e) {
          menu = smsHandler.createMenu(name + " SkillTree", Lang.TITLE_ITEM_GUI.toString().replace("%class%", name), name);
        }
        menu.removeAllItems();
        
        menu.setAutosave(false);
        menu.setAutosort(false);
        
        menus.put(hc, menu);
        for (String sn : hc.getSkillNames()) {
          Skill skill = plugin.getSkillManager().getSkill(sn);
          if (skill instanceof ActiveSkill) {
            if (skill.getIdentifiers().length == 0) {
              Logger.severe(Lang.GUI_INVAILD_SKILLS.toString().replace("%skill%", sn));
            } else { 
                //TODO add level of skills - by quantity of items
                //TODO get statistics from .getSettings() and take them to the lore
            	//TODO add full language support
              int skillLevel = commandSendingHero.getSkillLevel(skill);
              int skillMaxLevel = hst.getSkillMaxLevel(commandSendingHero, skill);
              int manaA = (int)SkillConfigManager.getUseSetting(commandSendingHero, skill, "mana", 0.0D, false);
              int manaB = SkillConfigManager.getSetting(hc, skill, "hst-mana", 1);
              int manaC = hst.getSkillLevel(commandSendingHero, skill) - 1;
              int manaCost = manaA;
              String indicator = (String)SkillConfigManager.getSetting(hc, skill, "hst-indicator");
              
              SMSMenuItem skillsClass = new SMSMenuItem(menu, /*menu*/
            		  Lang.GUI_TITLE_SKILL.toString().replace("%skill%", skill.getName()), /*label*/
            		  "/" + skill.getIdentifiers()[0], "", /*command, message*/
            		  indicator, /*iconMaterialName*/
            		  new String[] { /*lore*/
            	  		Lang.GUI_LORE.toString(), //Click for use!: String
            	  		Lang.GUI_LORE_LEVEL.toString() //SkillLevel: String
            	  			.replace("%level%", String.valueOf(skillLevel)) //skillLevel: int
            	  			.replace("%maxLevel%", String.valueOf(skillMaxLevel)), //maxLevel: int
            	  	    Lang.GUI_LORE_MANA.toString().replace("%manaCost%", String.valueOf(manaCost))
            	  		//SkillDamage (if it's war spell): int
            	  		//SkillManaCost: int
            	  		//SkillReagentCost: int
            	  		//SkillReagentName: String
            	  		/*TODO automatic generating hst-* parameters here*/
            	  		/*TODO automatic generating ALL parameters here*/}); 
              menu.addItem(skillsClass);
            }
          }
        }
        menu.setAutosave(true);
        menu.setAutosort(true);

        SMSInventoryView view = null;
        try {
          //TODO fix a bug with creating new view any time ten use command /skillgui
        	
          //ViewManager mgr = ScrollingMenuSign.getInstance().getViewManager();
          //view = (SMSInventoryView)mgr.getView(name);
          //view = new SMSInventoryView(name + " view", menu);
          //view.update(menu, me.desht.scrollingmenusign.enums.SMSMenuAction.REPAINT);
          view = (SMSInventoryView)smsHandler.getViewManager().getView(name);
        } catch (SMSException e) {
          view = new SMSInventoryView(name, menu);
          view.update(menu, me.desht.scrollingmenusign.enums.SMSMenuAction.REPAINT);
        }
        views.put(hc, view);
        view.setAutosave(true);
		
        view.toggleGUI((Player)sender);
    }
}
