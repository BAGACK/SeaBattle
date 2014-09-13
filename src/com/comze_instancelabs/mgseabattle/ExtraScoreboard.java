package com.comze_instancelabs.mgseabattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ExtraScoreboard {

	Scoreboard board;
	Objective objective;

	Main plugin;

	public ExtraScoreboard(Main m) {
		this.plugin = m;
	}

	public void updateScoreboard(final IArena a) {
		for (String p_ : a.getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			if (board == null) {
				board = Bukkit.getScoreboardManager().getNewScoreboard();
			}
			if (objective == null) {
				objective = board.registerNewObjective("test", "dummy");
			}

			objective.setDisplaySlot(DisplaySlot.BELOW_NAME);

			objective.setDisplayName(ChatColor.AQUA + "Lives");

			board.resetScores(Bukkit.getOfflinePlayer(p_));
			if (!a.plives.containsKey(p_)) {
				a.plives.put(p_, plugin.lives);
			}
			objective.getScore(Bukkit.getOfflinePlayer(p_)).setScore(a.plives.get(p_));

			p.setScoreboard(board);
		}
	}

}
