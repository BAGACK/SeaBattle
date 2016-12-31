package com.comze_instancelabs.mgseabattle;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.util.Util;

public class IArena extends Arena {

	PluginInstance pli;
	Main plugin;

	HashMap<String, Integer> plives = new HashMap<String, Integer>(); // player -> lives
	HashMap<String, Integer> boathp = new HashMap<String, Integer>(); // player -> boat hp
	HashMap<String, Integer> pspawn = new HashMap<String, Integer>(); // player -> spawn id

	ArrayList<String> ptwokills = new ArrayList<String>();

	public IArena(Main plugin, String name) {
		super(plugin, name, ArenaType.REGENERATION);
		this.plugin = plugin;
		MinigamesAPI.getAPI();
		pli = MinigamesAPI.pinstances.get(plugin);
	}

	@Override
	public void leavePlayer(String playername, boolean fullLeave) {
		for (String p_ : this.getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			if (p.isInsideVehicle()) {
				if (p.getVehicle() instanceof Vehicle) {
					Vehicle v = (Vehicle) p.getVehicle();
					v.eject();
					v.remove();
				}
			}
		}
		super.leavePlayer(playername, fullLeave);
	}

	@Override
	public void stop() {
		ArrayList<String> temp = new ArrayList<String>(this.getAllPlayers());
		for (String p_ : temp) {
			if (plives.containsKey(p_)) {
				if (plives.get(p_) >= plugin.lives) {
					// player hasn't lost any lives
					pli.getArenaAchievements().setAchievementDone(p_, "win_game_with_full_lives", false);
				}
			}
		}
		super.stop();
		plives.clear();
		boathp.clear();
		pspawn.clear();
		ptwokills.clear();
		
		// really destroy all boats
		Util.clearEntites(getBoundaries(), p -> p instanceof Boat);
	}

	@Override
	public void started()
	{
		// really destroy all boats being present from a previous match
		Util.clearEntites(getBoundaries(), p -> p instanceof Boat);
		
		super.started();
		
		// spawn boats
		for (String p_ : this.getAllPlayers()) {
			final Player p = Bukkit.getPlayer(p_);
			final Block block = p.getLocation().add(new Vector(0, -1, 0)).getBlock();
			if (block.getType() != Material.WATER)
			{
				this.getSmartReset().addChanged(block);
				block.setType(Material.WATER);
			}
			final Boat b = p.getWorld().spawn(p.getLocation(), Boat.class);
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					b.setPassenger(p);
				}
			}, 1L);
		}
	}

	@Override
	public void start(boolean tp) {
		super.start(false);

		// init
		for (String p_ : this.getAllPlayers()) {
			plives.put(p_, plugin.lives);
			boathp.put(p_, plugin.boat_health);
		}

		// tp
		teleportAllPlayers(this.getAllPlayers(), this.getSpawns());

		// update scoreboard
		final IArena a = this;
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				plugin.xscore.updateScoreboard(a);
			}
		}, 15L);

	}

	public void teleportAllPlayers(ArrayList<String> players, ArrayList<Location> locs) {
		int currentid = 0;
		int locslength = locs.size();
		for (String p_ : players) {
			Player p = Bukkit.getPlayer(p_);
			Util.teleportPlayerFixed(p, locs.get(currentid));
			pspawn.put(p.getName(), currentid);
			currentid++;
			if (currentid > locslength - 1) {
				currentid = 0;
			}
		}
	}

}
