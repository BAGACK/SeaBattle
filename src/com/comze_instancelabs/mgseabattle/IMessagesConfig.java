package com.comze_instancelabs.mgseabattle;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.config.MessagesConfig;

public class IMessagesConfig extends MessagesConfig {

	public IMessagesConfig(Main arg0) {
		super(arg0);

		this.getConfig().addDefault("messages.you_lost_a_life", arg0.you_lost_a_life);

		this.getConfig().options().copyDefaults(true);
		this.saveConfig();

		arg0.you_lost_a_life = this.getConfig().getString("messages.you_lost_a_life");
	}

}
