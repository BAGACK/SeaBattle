package com.comze_instancelabs.mgseabattle;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.commands.CommandHandler;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.config.DefaultConfig;
import com.comze_instancelabs.minigamesapi.config.MessagesConfig;
import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Main extends JavaPlugin implements Listener {

	// players get boats at spawn [done]
	// fix movement lock at spawn
	// multiple lives (respawn) -> possibly scoreboard support? [done]
	// players die when their boat gets destroyed [done]

	static Main m;
	MinigamesAPI api;
	PluginInstance pli;

	int boat_health = 5;
	int lives = 2;

	ExtraScoreboard xscore;
	ICommandHandler cmdhandler = new ICommandHandler();

	String you_lost_a_life = "&cYou lost a life. Lives left: <count>";

	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "seabattle", IArena.class, new ArenasConfig(this), new IMessagesConfig(this), new IClassesConfig(this), new StatsConfig(this, false), new DefaultConfig(this, false), false);
		PluginInstance pinstance = api.pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
		pinstance.arenaSetup = new ArenaSetup();
		try {
			pinstance.getClass().getMethod("setAchievementGuiEnabled", boolean.class);
			pinstance.setAchievementGuiEnabled(true);
		} catch (NoSuchMethodException e) {
			System.out.println("Update your MinigamesLib to the latest version to use the Achievement Gui.");
		}

		this.getConfig().addDefault("config.default_boat_health", boat_health);
		this.getConfig().addDefault("config.default_player_lives", lives);
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();

		boat_health = this.getConfig().getInt("config.default_boat_health");
		lives = this.getConfig().getInt("config.default_player_lives");

		pli = pinstance;

		xscore = new ExtraScoreboard(this);
	}

	public static ArrayList<Arena> loadArenas(JavaPlugin plugin, ArenasConfig cf) {
		ArrayList<Arena> ret = new ArrayList<Arena>();
		FileConfiguration config = cf.getConfig();
		if (!config.isSet("arenas")) {
			return ret;
		}
		for (String arena : config.getConfigurationSection("arenas.").getKeys(false)) {
			if (Validator.isArenaValid(plugin, arena, cf.getConfig())) {
				ret.add(initArena(arena));
			}
		}
		return ret;
	}

	public static IArena initArena(String arena) {
		IArena a = new IArena(m, arena);
		ArenaSetup s = MinigamesAPI.getAPI().pinstances.get(m).arenaSetup;
		a.init(Util.getSignLocationFromArena(m, arena), Util.getAllSpawns(m, arena), Util.getMainLobby(m), Util.getComponentForArena(m, arena, "lobby"), s.getPlayerCount(m, arena, true), s.getPlayerCount(m, arena, false), s.getArenaVIP(m, arena));
		return a;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return cmdhandler.handleArgs(this, "mgseabattle", "/" + cmd.getName(), sender, args);
	}

	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if (event.getVehicle().getPassenger() instanceof Player) {
			Player p = (Player) event.getVehicle().getPassenger();
			if (pli.global_players.containsKey(p.getName())) {
				event.getVehicle().setVelocity(new Vector(0D, 0D, 0D));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.getVehicle().getPassenger() instanceof Player && event.getAttacker() instanceof Player) {
			final Player p = (Player) event.getVehicle().getPassenger();
			final Player attacker = (Player) event.getAttacker();
			if (pli.global_players.containsKey(p.getName()) && pli.global_players.containsKey(attacker.getName())) {
				// event.getVehicle().setVelocity(new Vector(0D, 0D, 0D));
				event.setCancelled(true);

				if (p.getName().equalsIgnoreCase(attacker.getName())) {
					// disallow players to shoot their own boat
					return;
				}

				IArena a = (IArena) pli.global_players.get(p.getName());

				int currenthealth = a.boathp.get(p.getName());
				currenthealth--;
				a.boathp.put(p.getName(), currenthealth);
				if (currenthealth < 1) {
					int currentlife = a.plives.get(p.getName());
					currentlife--;
					if (currentlife > 0) {
						a.plives.put(p.getName(), currentlife);
						// player still has a life, respawn
						event.getVehicle().eject();
						event.getVehicle().remove();
						Util.teleportPlayerFixed(p, a.getSpawns().get(a.pspawn.get(p.getName())));
						final Boat b = p.getWorld().spawn(p.getLocation(), Boat.class);
						Bukkit.getScheduler().runTaskLater(this, new Runnable() {
							public void run() {
								b.setPassenger(p);
							}
						}, 5L);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', you_lost_a_life.replaceAll("<count>", Integer.toString(currentlife))));
						currenthealth = boat_health;
						a.boathp.put(p.getName(), currenthealth);
					} else {
						// player lost, remove boat and spectate
						event.getVehicle().eject();
						event.getVehicle().remove();
						a.spectate(p.getName());
					}
					xscore.updateScoreboard(a);
				}
			}
		}
	}

	@EventHandler
	public void onVehicleExit(VehicleExitEvent event) {
		if (event.getVehicle().getPassenger() instanceof Player) {
			Player p = (Player) event.getVehicle().getPassenger();
			if (pli.global_players.containsKey(p.getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			IArena a = (IArena) pli.global_players.get(p.getName());
			if (a.getArenaState() != ArenaState.INGAME) {
				Location l = p.getLocation();
				try {
					if (a.pspawn.containsKey(p.getName())) {
						Location spawn = a.getSpawns().get(a.pspawn.get(p.getName()));
						if (Math.abs(l.getBlockX() - spawn.getBlockX()) > 2 || Math.abs(l.getBlockZ() - spawn.getBlockZ()) > 2) {
							// player moved away from spawn while game not started
							if (p.isInsideVehicle()) {
								Vehicle v = (Vehicle) p.getVehicle();
								v.eject();
								v.remove();
								final Boat b = p.getWorld().spawn(spawn, Boat.class);
								Bukkit.getScheduler().runTaskLater(this, new Runnable() {
									public void run() {
										b.setPassenger(p);
									}
								}, 5L);
							}
						}
					}
				} catch (Exception e) {
					System.out.println("Couldn't find spawn.");
				}
			}
		}
	}
}
