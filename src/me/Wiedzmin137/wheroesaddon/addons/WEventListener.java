package me.Wiedzmin137.wheroesaddon.addons;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.herocraftonline.heroes.characters.Hero;

public class WEventListener implements Listener {
	//TODO add more events
	
	@EventHandler
	public void onPlayerRegister(fr.xephi.authme.events.LoginEvent e) {
		Player player = e.getPlayer();
		Hero hero = HeroesSkillTree.heroes.getCharacterManager().getHero(player);
		if (hero.getHeroClass().isDefault()) {
			ItemGUI.createClassChoose(e.getPlayer());
		}
	}
}
