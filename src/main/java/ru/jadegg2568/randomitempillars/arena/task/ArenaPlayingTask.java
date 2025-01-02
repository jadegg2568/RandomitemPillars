package ru.jadegg2568.randomitempillars.arena.task;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.Arena;
import ru.jadegg2568.randomitempillars.arena.ArenaMode;
import ru.jadegg2568.randomitempillars.arena.ArenaState;
import ru.jadegg2568.randomitempillars.util.ColorReference;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ArenaPlayingTask extends BukkitRunnable {

    private final Arena arena;
    private final AtomicInteger time = new AtomicInteger(0);
    private final AtomicInteger giveTime = new AtomicInteger(0);

    public ArenaPlayingTask(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        time.set(60 * 3);
        giveTime.set(15);
        runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (!arena.inGame()) {
            this.cancel();
            return;
        }

        arena.sendActionBarAll(Component.text("Вы получите предмет через ").color(ColorReference.gameChatColor)
                .append(Component.text(giveTime.get()).color(ColorReference.chatColor2))
                .append(Component.text(" сек.").color(ColorReference.gameChatColor)));

        int currentTime = time.decrementAndGet();
        if (currentTime == 0) {
            arena.changeState(ArenaState.END);
            this.cancel();
        } else if (currentTime == 30) {
            arena.sendTitleAll(Component.empty(), Component.text("Конец игры через 30 сек!").color(ColorReference.chatColorError), 60);
            arena.playSoundAll(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
        }
        if (giveTime.decrementAndGet() == 0) {
            giveTime.set(giveRandomItems());
        }
    }

    public int giveRandomItems() {
        Material[] items = Material.values();
        Random r = new Random();

        ArenaMode mode = arena.getMode();
        switch (mode) {
            case ONE_ITEM_3:
            case ONE_ITEM_5:
            case ONE_ITEM_10:
                arena.getPlayers().forEach(player -> {
                    player.getPlayer().getInventory().addItem(new ItemStack(items[r.nextInt(items.length)]));
                    player.playSound(Sound.ENTITY_ITEM_PICKUP, 1, 1);
                });
                break;

            case HOTBARS_SHUFFLE_8:
            case HOTBARS_SHUFFLE_15:
                arena.getPlayers().forEach(player -> {
                    player.getPlayer().getInventory().clear();
                    for (int i = 0; i < 9; i++) {
                        player.getPlayer().getInventory().setItem(i, new ItemStack(items[r.nextInt(items.length)]));
                    }
                    player.playSound(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1, 1);
                });
                break;

            case INVENTORY_SHUFFLE_10:
            case INVENTORY_SHUFFLE_15:
            case INVENTORY_SHUFFLE_20:
                arena.getPlayers().forEach(player -> {
                    player.getPlayer().getInventory().clear();
                    for (int i = 0; i < 36; i++) {
                        player.getPlayer().getInventory().setItem(i, new ItemStack(items[r.nextInt(items.length)]));
                    }
                    player.playSound(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1, 1);
                });
                break;
        }
        String[] parts = mode.name().split("_");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    public AtomicInteger getTime() {
        return time;
    }

    public AtomicInteger getGiveTime() {
        return giveTime;
    }
}
