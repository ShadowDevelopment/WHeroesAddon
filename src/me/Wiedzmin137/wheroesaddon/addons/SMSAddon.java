package me.Wiedzmin137.wheroesaddon.addons;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.Lang;
import me.Wiedzmin137.wheroesaddon.WAddonCore;
import me.desht.scrollingmenusign.SMSException;
import me.desht.scrollingmenusign.SMSHandler;
import me.desht.scrollingmenusign.SMSMenu;
import me.desht.scrollingmenusign.SMSMenuItem;
import me.desht.scrollingmenusign.ScrollingMenuSign;
import me.desht.scrollingmenusign.enums.SMSMenuAction;
import me.desht.scrollingmenusign.views.SMSInventoryView;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

public class SMSAddon implements Listener {
	private static SMSHandler smsHandler;
    
	public SMSAddon(ScrollingMenuSign sms) {
		smsHandler = sms.getHandler();
		Bukkit.getPluginManager().registerEvents(this, WAddonCore.getInstance());
	}

	public void setAutosave(boolean autosave) {
		for (SMSMenu menu : smsHandler.listMenus()) {
			menu.setAutosave(autosave);
		}
	}
	
	//TODO SkillGUI
	//Create 1 long string with all letters and use for and if
	
	public static void createSkillTree(CommandSender sender, HeroesSkillTree hst) {
    	//TODO cleanup. Some things and change some names
        SMSMenu menu = null;
        
        Hero commandSendingHero = WAddonCore.heroes.getCharacterManager().getHero((Player) sender);
        HeroClass hc = commandSendingHero.getHeroClass();
        String name = hc.getName();

        if (smsHandler == null) {
          return;
        }
        
        try {
          menu = smsHandler.getMenu(name + " SkillTree");
        } catch (SMSException e) {
          menu = smsHandler.createMenu(name + " SkillTree", Lang.TITLE_ITEM_GUI.toString().replace("%class%", name), (Player)sender);
        }
        menu.removeAllItems();
        
        menu.setAutosave(false);
        menu.setAutosort(false);
        
        for (String skillNames : hc.getSkillNames()) {
        	Skill skill = WAddonCore.heroes.getSkillManager().getSkill(skillNames);
        	if (skill instanceof ActiveSkill) {
        		if (skill.getIdentifiers().length == 0) {
        			WAddonCore.Log.severe(Lang.GUI_INVAILD_SKILLS.toString().replace("%skill%", skillNames));
        		} else {
        			//TODO add level of skills - by quantity of items
        			//TODO get statistics from .getSettings() and take them to the lore
        			//TODO add full language support
        			//TODO add e.g. .replace("{Level}", getSkillLevel(skill))
        			int skillLevel = WAddonCore.getInstance().getSkillTree().getSkillLevel(commandSendingHero, skill);
        			int skillMaxLevel = hst.getSkillMaxLevel(commandSendingHero, skill);
        			String indicator = (String)SkillConfigManager.getSetting(hc, skill, "hst-indicator");
              
        			SMSMenuItem skillClass = new SMSMenuItem.Builder(menu, 
        			  Lang.GUI_TITLE_SKILL.toString().replace("%skill%", skill.getName()))
        				.withCommand("/st down " + skill.getName() + " 1")
        				.withAltCommand("/st up " + skill.getName() + " 1")
        				.withIcon(indicator)
        				.withLore(Lang.GUI_LORE.toString(),
        					Lang.GUI_LORE_LEVEL.toString() //SkillLevel: String
                			.replace("%level%", String.valueOf(skillLevel)) //skillLevel: int
                			.replace("%maxLevel%", String.valueOf(skillMaxLevel))) //maxLevel: int
        				.build();
        			menu.addItem(skillClass);
        		}
        	}
        }
        menu.setAutosave(true);
        menu.setAutosort(true);
    }
	
	public static void showSkillTree(CommandSender sender, String hClass) {
		SMSMenu menu = null;
		try {
			menu = smsHandler.getMenu(hClass + " SkillTree");
		} catch (SMSException e) {
			menu = smsHandler.createMenu(hClass + " SkillTree", Lang.TITLE_ITEM_GUI.toString().replace("%class%", hClass), (Player)sender);
		}
		
		SMSInventoryView view = null;
		try {
			view = (SMSInventoryView)smsHandler.getViewManager().getView(hClass);
		} catch (SMSException e) {
			view = new SMSInventoryView(hClass, menu);
			view.update(menu, SMSMenuAction.REPAINT);
			smsHandler.getViewManager().registerView(view);
		}
		view.setAutosave(true);
		
		view.toggleGUI((Player)sender);
	}
    
    public static void showClassChoose(Player player) {
        SMSMenu menuChoose = null;
        
        if (smsHandler == null) { return; }
        try { menuChoose = smsHandler.getMenu("ClassChoose"); }
        catch (SMSException e) { 
        	menuChoose = smsHandler.createMenu("ClassChoose", Lang.GUI_TITLE_CHOOSE.toString(), WAddonCore.getInstance()); 
        }

        SMSInventoryView view = null;
        try { 
        	view = (SMSInventoryView)smsHandler.getViewManager().getView("ClassChoose"); 
        } catch (SMSException e) {
          view = new SMSInventoryView("ClassChoose", menuChoose);
          view.update(menuChoose, SMSMenuAction.REPAINT);
          smsHandler.getViewManager().registerView(view);
        }

        view.toggleGUI(player);
    }
}
