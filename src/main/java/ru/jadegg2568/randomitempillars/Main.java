package ru.jadegg2568.randomitempillars;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.jadegg2568.randomitempillars.arena.Arena;
import ru.jadegg2568.randomitempillars.arena.ArenaManager;
import ru.jadegg2568.randomitempillars.command.ArenasCommand;
import ru.jadegg2568.randomitempillars.command.ModeCommand;
import ru.jadegg2568.randomitempillars.configuration.ConfigManager;
import ru.jadegg2568.randomitempillars.listener.PlayerListener;
import ru.jadegg2568.randomitempillars.listener.WorldListener;

import java.util.Objects;

public final class Main extends JavaPlugin {

    private static Main instance;
    private ConfigManager configManager;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        arenaManager = new ArenaManager(this);
        arenaManager.addArenas(configManager.readArenas());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
        Objects.requireNonNull(getCommand("arenas")).setExecutor(new ArenasCommand(this));
        Objects.requireNonNull(getCommand("mode")).setExecutor(new ModeCommand(this));
    }

    @Override
    public void onDisable() {
        arenaManager.removeArenas();
        instance = null;
    }

    public static Main getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}
