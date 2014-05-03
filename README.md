WHeroesAddon
===============

<div align="center">Brings addons to the Heroes plugin.


<a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Licence Creative Commons" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a>

<br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">WHeroesAddon</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/wiedzmin137/WHeroesAddon" property="cc:attributionName" rel="cc:attributionURL">Wiedzmin137 and Whatshywl</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">CC A-NC-ND 4.0 International Licences</a>.<br />Based on the work available at <a xmlns:dct="http://purl.org/dc/terms/" href="http://dev.bukkit.org/bukkit-plugins/heroes-skill-tree/" rel="dct:source">http://dev.bukkit.org/bukkit-plugins/heroes-skill-tree/</a></div>.


ToDo
===============

Add:
+ [InProgress] RequirementAPI for upgrading & unlocking skills;
	rAPI - create your own module providing new power for making
	unavailable, REAL RPG based skilltrees. 
* SkillTreeAPI (modules) for creating skills to another's plugins;
	stAPI - create your own modules providing compatibility skilltrees
	with other plugins! Any plugin giving skills, spells, abilities or
	traits can be used. Feel free and extend this core to make your plugin
	based skilltree with unlimited power. Use rAPI to upgrade that system.
+ AddonsAPI (modules) for new external things (needs ideas);
	aAPI - Doesn't use Heroes for Holograms, ItemGUI and other goodness?
	No problem. aAPI gives you power to manipulate those WAddon systems
	to compatible this with your environment. Use stAPI for skilltrees.
	
+ New layout of visual skilltrees (need ideas!)
	I need some ideas for that, this may be quite hard due to lack
	of ideas. If I not find good system, I'll create something looks
	similar to SkillAPI.
	
+ [InProgress] /skills command GUI (for LordKaizo)
	Using ChestCommands. A /skills commands with skill list. It
	will be contain optional support to using by Left click.
	Right click will be bind this skill (if possible).
+ Usage ChestCommands instead of ScrollingMenuSigns
	Just one reason - CC looks A LOT OF faster than SMS.
+ New addons introducing Heroes to new technologies
	Holograms, SkillTrees, ItemGUIs - they're just beginning.
	I need new ideas what I can do.
   
+ Through-tier SkillTrees
	It will be hard but yes, an Through-tier SkillTrees. I planing
	this by adding that boolean variable into skill options. If player
	is going to change class for high tier, then this skill's levels
	will be saved. May be good output for classes with higher skill levels
	need than amount of SkillPoints.
+ Support BHereosStamina (or BMedievalFight)
	This plugin will be a Stamina handler. I don't know exactly what I will
	add to WHA but skilltrees will use that Stamina.
	
+ Add comments and documentation to all classes
	A lot of works and will be provide nice results for me.
+ (HARD) Clean up to speed up plugin
	As clean up may be easy, speed up no. I'll try to make it as speed
	as it really possible. Advanced skilltrees will not be so expansive.
+ (Low priority) Take care about encapsulation SkillTree HashMaps.
	
+ New UUID support;
	I don't know how it will be looks like.
	
+ HoloAPI support
	A HolographicDisplays enemy.
	
+ Move pom.xml somewhere, delete .settings and .project

Fix:
* Not jet done RequirementAPI
	There's a problem with usage it in SkillUpCommand.java. A FileConfigurations,
	YamlConfigurations and ConfigurationSections kill me. I'm fixing this for about
	3 days. If you know output, please tell me.
* Problems created by update to 1.7.5 (Heroes Effects)
	HeroesAPI was changed a bit, try to fix EventListener.
