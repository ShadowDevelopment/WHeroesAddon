package net.Servmine;

import org.bukkit.plugin.java.JavaPlugin;
     
public final class WHeroesAddon extends JavaPlugin {
    public void onEnable() {
        new MobArena(this);
    }
}