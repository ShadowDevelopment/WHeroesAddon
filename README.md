WHeroesAddon
===============
**<div align="center">A Heroes addon expanding RPG possibilities!</div><br>**
<div align="center"><a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Licence Creative Commons" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a>
<br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">WHeroesAddon</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/wiedzmin137/WHeroesAddon" property="cc:attributionName" rel="cc:attributionURL">Wiedzmin137 and Whatshywl</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">CC A-NC-ND 4.0 International Licences</a>.<br />Based on the work available at <a xmlns:dct="http://purl.org/dc/terms/" href="http://dev.bukkit.org/bukkit-plugins/heroes-skill-tree/" rel="dct:source">http://dev.bukkit.org/bukkit-plugins/heroes-skill-tree/</a></div>

ToDo
-------

**Add:**
- [ ] **RequirementAPI** for upgrading & unlocking skills;<br>
	`rAPI` - create your own module providing new power for making
	unavailable, REAL RPG based skilltrees. 
- [ ] **SkillTreeAPI** (modules) for creating skills to another's plugins;<br>
	 `stAPI` - create your own modules providing compatibility skilltrees
	 with other plugins! Any plugin giving skills, spells, abilities or
	 traits can be used. Feel free and extend this core to make your plugin
	 based skilltree with unlimited power. Use `rAPI` to upgrade that system.
- [ ] **AddonsAPI** (modules) for new external things (needs ideas);<br>
	 `aAPI` - Doesn't use Heroes for Holograms, ItemGUI and other goodness?
	 No problem. `aAPI` gives you power to manipulate those WAddon systems
	 to compatible this with your environment. Use `stAPI` for skilltrees.<br><br>
- [ ] [*InProgress*] '/skills' *command GUI* (for LordKaizo);<br>
	 Using ChestCommands. A `/skills` commands with skill list. It
	 will be contain optional support to using by Left click.
	 Right click will be bind this skill (in future).
- [ ] *New layout of visual skilltrees* (need ideas!);<br>
         I need some ideas for that, this may be quite hard due to lack
	 of ideas. If I not find good system, I'll create something looks
	 similar to SkillAPI.<br>
- [ ] *Usage ChestCommands for SkillTrees & GUIs*;<br>
	 Just one reason - CC looks A LOT OF faster than SMS.
	 Of course support for ScrollingMenuSign will be exist.
- [ ] *HoloAPI support*;<br>
	 A HolographicDisplays enemy.
- [ ] *New addons introducing Heroes to new technologies*;<br>
	 Holograms, SkillTrees, ItemGUIs - they're just beginning.
	 I need new ideas what I can do.<br><br>
- [ ] **Through-tier SkillTrees**;<br>
	 It will be hard but yes, an Through-tier SkillTrees. I planing
	 this by adding that boolean variable into skill options. If player
	 is going to change class for high tier, then this skill's levels
	 will be saved. May be good output for classes with higher skill levels
	 need than amount of SkillPoints.
- [ ] *Support BHereosStamina* (or BMedievalFight);<br>
	 This plugin will be a Stamina handler. I don't know exactly what I will
	 add to WHA but skilltrees will use that Stamina.<br><br>
- [ ] [*HARD*] *Clean up to speed up plugin*;<br>
	 As clean up may be easy, speed up no. I'll try to make it as speed
	 as it really possible. Advanced skilltrees will not be so expansive.
- [ ] *New UUID support*;<br>
	 I don't know how it will be looks like.<br>
- [ ] *Move pom.xml, delete .settings and .project*;<br>
- [ ] Take care about *encapsulation SkillTree HashMaps*;<br>

___
**Fix:**
- [ ] *Not jet done RequirementAPI*;<br>
	 There's a problem with usage it in `SkillUpCommand.java`. A FileConfigurations,
	 YamlConfigurations and ConfigurationSections killing me. I'm fixing this for about
	 3 days. If you know output, please tell me.
- [ ] **Problems created by update to 1.7.5** (Heroes Effects);<br>
	 HeroesAPI was changed a bit, try to fix `EventListener.java`.
