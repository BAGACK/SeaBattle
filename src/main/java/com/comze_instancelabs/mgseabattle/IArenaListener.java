package com.comze_instancelabs.mgseabattle;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaListener;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.PluginInstance;

public class IArenaListener extends ArenaListener {

	private PluginInstance pli;

	public IArenaListener(JavaPlugin plugin, PluginInstance pinstance, String minigame) {
		super(plugin, pinstance, minigame);
		this.pli = pinstance;
	}

	@Override
	@EventHandler
	public void onMove(PlayerMoveEvent event) {

        final Player p = event.getPlayer();
        if (this.pli.containsGlobalPlayer(p.getName()))
        {
            final Arena a = this.pli.global_players.get(p.getName());
            if (!this.pli.containsGlobalLost(p.getName()) && !this.pli.global_arcade_spectator.containsKey(p.getName()))
            {
                if (a.getArenaState() == ArenaState.INGAME)
                {
                	final Block target = event.getTo().getBlock();
                	if (target.getType() == Material.STATIONARY_WATER && event.getTo().getY() < ((double)target.getY() + 0.25d))
                	{
                		a.spectate(p.getName());
                		return;
                	}
                }
            }
        }
		super.onMove(event);
	}

}
