package ru.jadegg2568.randomitempillars.configuration;

import com.google.common.collect.Sets;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.Arena;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class ConfigManager {

    private final Main main;
    private final FileConfiguration config;

    public ConfigManager(Main main) {
        this.main = main;
        this.config = main.getConfig();
        config.options().copyDefaults(true);
        main.saveDefaultConfig();
    }

    public Set<Arena> readArenas() {
        ConfigurationSection arenasSection = config.getConfigurationSection("arenas");
        if (arenasSection == null) {
            return Sets.newHashSet();
        }

        Set<Arena> arenas = new HashSet<>();
        for (String name : arenasSection.getKeys(false)) {
            try {
                int minimum = arenasSection.getInt(name + ".minimum");
                int maximum = arenasSection.getInt(name + ".maximum");
                Material pillarMaterial = Material.valueOf(arenasSection.getString(name + ".pillarMaterial", "BEDROCK"));
                String worldName = arenasSection.getString(name + ".world");

                EnumMap<LocationType, Location> locations = new EnumMap<>(LocationType.class);

                ConfigurationSection locationsSection = config.getConfigurationSection("arenas." + name + ".locations");
                if (locationsSection != null) {
                    for (String locName : locationsSection.getKeys(false)) {
                        LocationType type = LocationType.valueOf(locName);
                        Location loc = readLocation("arenas." + name + ".locations." + locName);
                        locations.put(type, loc);
                    }
                }
                World w = getWorld(worldName);

                arenas.add(new Arena(name, minimum, maximum, pillarMaterial, w, locations));
            } catch (IllegalArgumentException e) {
                main.getSLF4JLogger().warn("Failed loading arena {}: {}. Check configuration.", name, e.getMessage());
            }
        }
        return arenas;
    }

    public void saveArena(Arena arena) {
        String name = arena.getName();
        config.set("arenas." + name + ".minimum", arena.getMinimum());
        config.set("arenas." + name + ".maximum", arena.getMaximum());
        config.set("arenas." + name + ".pillarMaterial", arena.getPillarMaterial().name());
        config.set("arenas." + name + ".world", arena.getWorld().getWorld().getName());
        try {
            config.save(new File(main.getDataFolder() + File.separator + "config.yml"));
        } catch (IOException e) {
            main.getSLF4JLogger().error("Failed saving config to save arena properties: {}: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            return;
        }

        for (LocationType locType : arena.getLocations().keySet()) {
            Location loc = arena.getLocations().get(locType);
            saveLocation("arenas." + name + ".locations." + locType.name(), loc);
        }
    }

    public Location readLocation(String path) {
        String worldName = config.getString(path + ".world");
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float pitch = (float) config.getDouble(path + ".pitch");
        float yaw = (float) config.getDouble(path + ".yaw");
        World w = getWorld(worldName);

        return new Location(w, x, y, z, pitch, yaw);
    }

    public void saveLocation(String path, Location loc) {
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".pitch", (double) loc.getPitch());
        config.set(path + ".yaw", (double) loc.getYaw());
        try {
            config.save(new File(main.getDataFolder() + File.separator + "config.yml"));
        } catch (IOException e) {
            main.getSLF4JLogger().error("Failed saving config to save location: {}: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            return;
        }
    }

    public World getWorld(String name) {
        if (name == null) {
            throw new IllegalArgumentException("World name cannot be null");
        }

        World w = Bukkit.getWorld(name);
        if (w == null) {
            w = Bukkit.createWorld(new WorldCreator(name));
        }
        return w;
    }
}
