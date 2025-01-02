package ru.jadegg2568.randomitempillars.arena;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.World;
import ru.jadegg2568.randomitempillars.Main;

import java.util.*;

public class ArenaManager {

    private final Main main;
    private final Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(Main main) {
        this.main = main;
    }

    public void addArena(Arena arena) {
        arena.load();
        arenas.put(arena.getName(), arena);
    }

    public void addArenas(Set<Arena> arenas) {
        for (Arena arena : arenas) {
            addArena(arena);
        }
    }

    public void removeArena(String name) {
        arenas.get(name).unload();
        arenas.remove(name);
    }

    public void removeArenas() {
        for (String name : arenas.keySet()) {
            removeArena(name);
        }
    }

    public boolean addToArena(ArenaPlayer player, Arena arena) {
        Arena currentArena = getArena(player);
        if (currentArena != null) {
            removeFromArena(player, arena);
        }

        if (arena.addPlayer(player)) {
            player.setPlayersVisible(arena.getPlayers());
            return true;
        } else {
            return false;
        }
    }

    public void removeFromArena(ArenaPlayer player, Arena arena) {
        arena.removePlayer(player);
        player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.setPlayersVisible(Sets.newHashSet());
    }

    public Arena getArena(String name) {
        for (Arena arena : arenas.values()) {
            if (arena.getName().equals(name))
                return arena;
        }
        return null;
    }

    public Arena getArena(ArenaPlayer player) {
        for (Arena arena : arenas.values()) {
            if (arena.getPlayer(player.getUuid()) != null)
                return arena;
        }
        return null;
    }

    public Arena getArenaByWorld(World world) {
        for (Arena arena : arenas.values()) {
            if (arena.getWorld().getWorld().getUID() == world.getUID())
                return arena;
        }
        return null;
    }

    public Set<ArenaPlayer> getAllPlayers() {
        Set<ArenaPlayer> players = new HashSet<>();
        for (Arena arena : arenas.values()) {
            players.addAll(arena.getPlayers());
        }
        return players;
    }

    public ArenaPlayer getPlayer(UUID uuid) {
        for (Arena arena : arenas.values()) {
            ArenaPlayer player = arena.getPlayer(uuid);
            if (player != null)
                return player;
        }
        return null;
    }

    public Arena findArenaForPlayer(ArenaPlayer player) {
        return arenas.values().stream()
                .filter(Arena::inWaiting)
                .sorted(Comparator.comparingInt(arena -> arena.getPlayersOnline().size()))
                .min(Collections.reverseOrder())
                .orElse(null);
    }

    public Map<String, Arena> getArenas() {
        return arenas;
    }
}
