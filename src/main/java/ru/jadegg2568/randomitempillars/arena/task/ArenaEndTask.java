package ru.jadegg2568.randomitempillars.arena.task;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.Arena;
import ru.jadegg2568.randomitempillars.arena.ArenaPlayer;
import ru.jadegg2568.randomitempillars.arena.ArenaState;
import ru.jadegg2568.randomitempillars.util.FireworkFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ArenaEndTask extends BukkitRunnable {

    private final Arena arena;
    private final AtomicInteger time = new AtomicInteger(0);

    public ArenaEndTask(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        time.set(10);
        runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (!arena.inEnd()) {
            this.cancel();
            return;
        }

        if (time.decrementAndGet() == 0) {
            arena.resetAfterGame();
            arena.changeState(ArenaState.WAITING);
            this.cancel();
        } else {
            ArenaPlayer winner = arena.getWinner();
            if (winner != null)
                FireworkFactory.spawnFirework(winner.getPlayer().getLocation());
        }
    }

    public AtomicInteger getTime() {
        return time;
    }
}
