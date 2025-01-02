package ru.jadegg2568.randomitempillars.arena;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.jadegg2568.randomitempillars.Main;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ArenaPlayer {

    private final UUID uuid;
    private final String name;
    private final Player p;
    private boolean isSpectator;
    private ArenaMode vote;

    public ArenaPlayer(UUID uuid, Player p) {
        this.uuid = uuid;
        this.name = p.getName();
        this.p = p;
        this.isSpectator = false;
        this.vote = null;
    }

    public void sendMessage(Component component) {
        p.sendMessage(component);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        p.playSound(p.getLocation(), sound, volume, pitch);
    }

    public void teleport(Location loc) {
        if (loc != null) {
            p.teleportAsync(loc);
        }
    }

    public void setSpectator(boolean set) {
        this.isSpectator = set;
        p.setGameMode(set ? GameMode.SPECTATOR : GameMode.SURVIVAL);
    }

    public void setPlayersVisible(Set<ArenaPlayer> players) {
        Set<UUID> playerUuids = players.stream().map(ArenaPlayer::getUuid).collect(Collectors.toSet());

        for (Player p2 : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(uuid)) continue;
            if (playerUuids.contains(p.getUniqueId())) {
                p.showPlayer(Main.getInstance(), p2);
                p2.showPlayer(Main.getInstance(), p);
            } else {
                p.hidePlayer(Main.getInstance(), p2);
                p2.hidePlayer(Main.getInstance(), p);
            }
        }
    }

    public void setExp(float exp) {
        p.setExp(exp);
    }

    public void reset() {
        p.getInventory().clear();
        p.setFireTicks(0);
        p.setFoodLevel(20);
        for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());
        p.setHealthScale(20.0f);
        p.setHealth(20.0f);
        p.setExp(0.0f);
        p.closeInventory();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Player getPlayer() {
        return p;
    }

    public boolean isSpectator() {
        return isSpectator;
    }

    public ArenaMode getVote() {
        return vote;
    }

    public void setVote(ArenaMode vote) {
        this.vote = vote;
    }
}
