package ru.jadegg2568.randomitempillars.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.Arena;
import ru.jadegg2568.randomitempillars.arena.ArenaManager;
import ru.jadegg2568.randomitempillars.arena.ArenaPlayer;
import ru.jadegg2568.randomitempillars.util.ColorReference;

public class PlayerListener implements Listener {

    private final Main main;
    private final ArenaManager arenaManager;

    public PlayerListener(Main main) {
        this.main = main;
        this.arenaManager = main.getArenaManager();
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        e.joinMessage(null);

        if (arenaManager.getArenas().size() == 1) {
            ArenaPlayer player = new ArenaPlayer(p.getUniqueId(), p);
            Arena arena = arenaManager.findArenaForPlayer(player);

            if (arena != null) {
                if (arena.isDisabled()) return;
                if (!arenaManager.addToArena(player, arena)) {
                    p.kick(Component.text("Каннот джоин банги арена").color(ColorReference.chatColorError));
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.quitMessage(null);

        ArenaPlayer player = arenaManager.getPlayer(p.getUniqueId());
        if (player != null) {
            Arena arena = arenaManager.getArena(player);
            if (arena.isDisabled()) return;
            arenaManager.removeFromArena(player, arena);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();

        ArenaPlayer player = arenaManager.getPlayer(p.getUniqueId());
        if (player != null) {
            Arena arena = arenaManager.getArena(player);
            if (arena.isDisabled()) return;
            if (arena.inGame()) {
                if (e.getDamage() >= p.getHealth()) {
                    arena.playerDead(player);
                    e.setDamage(0);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }
}
