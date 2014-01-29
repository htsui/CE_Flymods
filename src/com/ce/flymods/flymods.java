package com.ce.flymods;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class flymods extends JavaPlugin {
	private PluginManager pm;
	public void onEnable(){
		
		getLogger().info("flymods enabling");
		pm.registerEvents(new eventListener<Object>(this), this);
		getLogger().info("flymods enabled");
	}
	
	public void onDisable(){
		getLogger().info("flymods disabled");
	}
	
}