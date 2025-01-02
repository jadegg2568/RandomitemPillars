package ru.jadegg2568.randomitempillars.arena;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ru.jadegg2568.randomitempillars.configuration.LocationType;

import java.util.ArrayList;
import java.util.List;

public class ArenaWorld {

    private final Arena arena;
    private final World w;
    private final List<BlockState> changes = new ArrayList<>();

    public ArenaWorld(Arena arena, World w) {
        this.arena = arena;
        this.w = w;
    }

    private boolean equalsLocation(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) &&
                loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }

    public void saveChange(BlockState change) {
        for (BlockState change1 : changes) {
            if (equalsLocation(change.getLocation(), change1.getLocation()))
                return;
        }
        changes.add(change);
    }

    public void restoreWorld() {
        changes.forEach((change) -> {
            change.getBlock().setType(change.getType());
            change.getBlock().setBlockData(change.getBlockData());
        });
        changes.clear();
    }

    public void setupRules() {
        w.setAutoSave(false);
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

    public void clearEntities() {
        for (Entity entity : w.getEntities()) {
            if (!(entity instanceof Player)) {
                entity.remove();
            }
        }
    }

    public void buildCage(Material material, Location loc) {
        if (loc == null) return;
        loc.clone().add(0, -1, 0).getBlock().setType(material);
        loc.clone().add(0, 2, 0).getBlock().setType(material);
        loc.clone().add(-1, 0, 0).getBlock().setType(material);
        loc.clone().add(-1, 1, 0).getBlock().setType(material);
        loc.clone().add(1, 0, 0).getBlock().setType(material);
        loc.clone().add(1, 1, 0).getBlock().setType(material);
        loc.clone().add(0, 0, -1).getBlock().setType(material);
        loc.clone().add(0, 1, -1).getBlock().setType(material);
        loc.clone().add(0, 0, 1).getBlock().setType(material);
        loc.clone().add(0, 1, 1).getBlock().setType(material);
    }

    public void buildCages(Material material) {
        buildCage(material, arena.getLocations().get(LocationType.SPAWN1));
        buildCage(material, arena.getLocations().get(LocationType.SPAWN2));
        buildCage(material, arena.getLocations().get(LocationType.SPAWN3));
        buildCage(material, arena.getLocations().get(LocationType.SPAWN4));
        buildCage(material, arena.getLocations().get(LocationType.SPAWN5));
        buildCage(material, arena.getLocations().get(LocationType.SPAWN6));
        buildCage(material, arena.getLocations().get(LocationType.SPAWN7));
        buildCage(material, arena.getLocations().get(LocationType.SPAWN8));
    }

    public void buildPillar(Material material, int size, Location location) {
        if (location == null) return;
        for (int i = 1; i <= size; i++) {
            Location loc = location.clone().add(0, -i - 4, 0);
            loc.getBlock().setType(material);
        }
    }

    public void buildPillars(Material material) {
        int size = 32;
        buildPillar(material, size, arena.getLocations().get(LocationType.SPAWN1));
        buildPillar(material, size, arena.getLocations().get(LocationType.SPAWN2));
        buildPillar(material, size, arena.getLocations().get(LocationType.SPAWN3));
        buildPillar(material, size, arena.getLocations().get(LocationType.SPAWN4));
        buildPillar(material, size, arena.getLocations().get(LocationType.SPAWN5));
        buildPillar(material, size, arena.getLocations().get(LocationType.SPAWN6));
        buildPillar(material, size, arena.getLocations().get(LocationType.SPAWN7));
        buildPillar(material, size, arena.getLocations().get(LocationType.SPAWN8));
    }

    public World getWorld() {
        return w;
    }
}
