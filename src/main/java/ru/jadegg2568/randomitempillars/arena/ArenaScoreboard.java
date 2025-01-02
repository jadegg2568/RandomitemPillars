package ru.jadegg2568.randomitempillars.arena;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashSet;
import java.util.List;

public class ArenaScoreboard {

    private final Scoreboard scoreboard;
    private final Objective objective;

    public ArenaScoreboard(Component displayName) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("objective", Criteria.DUMMY, displayName);
        displayObjective(true);
    }

    public Scoreboard get() {
        return scoreboard;
    }

    public void set(ArenaPlayer player) {
        player.getPlayer().setScoreboard(scoreboard);
    }

    public void displayObjective(boolean display) {
        this.objective.setDisplaySlot(display ? DisplaySlot.SIDEBAR : null);
    }

    public void setLines(List<Component> lines) {
        if (lines == null)
            return;

        for (int i = 0; i < 16; i++) {
            String name = String.valueOf(i);
            if (lines.size() > i) {
                Score score = objective.getScore(name);
                score.setScore(lines.size() - i);
                score.customName(lines.get(i));
            } else {
                scoreboard.resetScores(name);
            }
        }
    }

    public void setPrefixTeam(String name, String displayName, List<String> entries) {
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
        }
        team.setPrefix(displayName);

        for (String entry : new HashSet<>(team.getEntries())) {
            if (!entries.contains(entry)) {
                team.removeEntry(entry);
            }
        }
        for (String entry : entries) {
            if (!team.getEntries().contains(entry)) {
                team.addEntry(entry);
            }
        }
    }
}
