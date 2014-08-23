package com.comze_instancelabs.mgseabattle;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.config.ClassesConfig;

public class IClassesConfig extends ClassesConfig {

	public IClassesConfig(JavaPlugin plugin) {
		super(plugin, true);
		this.getConfig().options().header("Used for saving classes. Default class:");

		// default
		this.getConfig().addDefault("config.kits.default.name", "default");
		this.getConfig().addDefault("config.kits.default.items", "332*64;332*64;332*64;332*64;332*64");
		this.getConfig().addDefault("config.kits.default.lore", "Default kit.");
		this.getConfig().addDefault("config.kits.default.requires_money", false);
		this.getConfig().addDefault("config.kits.default.requires_permission", false);
		this.getConfig().addDefault("config.kits.default.money_amount", 100);
		this.getConfig().addDefault("config.kits.default.permission_node", "minigames.kits.default");

		// pro
		this.getConfig().addDefault("config.kits.pro.name", "Pro");
		this.getConfig().addDefault("config.kits.pro.items", "332*64;332*64;332*64;332*64;332*64;332*64;332*64;322*64");
		this.getConfig().addDefault("config.kits.pro.lore", "Pro kit.");
		this.getConfig().addDefault("config.kits.pro.requires_money", false);
		this.getConfig().addDefault("config.kits.pro.requires_permission", false);
		this.getConfig().addDefault("config.kits.pro.money_amount", 100);
		this.getConfig().addDefault("config.kits.pro.permission_node", "minigames.kits.pro");

		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

}
