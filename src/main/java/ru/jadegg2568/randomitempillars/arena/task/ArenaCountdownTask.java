package ru.jadegg2568.randomitempillars.arena.task;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.Arena;
import ru.jadegg2568.randomitempillars.arena.ArenaState;
import ru.jadegg2568.randomitempillars.util.ColorReference;

import java.util.concurrent.atomic.AtomicInteger;

public class ArenaCountdownTask extends BukkitRunnable {

    private final Arena arena;
    private final AtomicInteger time = new AtomicInteger(0);

    public ArenaCountdownTask(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        time.set(60);
        runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (!arena.inWaiting()) {
            this.cancel();
            return;
        }

        int currentTime = time.decrementAndGet();
        switch (currentTime) {
            case 59:
            case 30:
            case 15:
            case 10:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
                arena.sendAll(Component.text("Мы стартуем через ").color(ColorReference.chatColor)
                        .append(Component.text(String.valueOf(currentTime)).color(ColorReference.chatColor2)
                                .append(Component.text(" секунд.").color(ColorReference.chatColor))));
                arena.playSoundAll(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                break;
            case 0:
                arena.sendAll(Component.text("ПУСК!").color(ColorReference.chatColorSuccess));
                arena.changeState(ArenaState.PLAYING);
                this.cancel();
                break;

        }
    }

    public AtomicInteger getTime() {
        return time;
    }
}
