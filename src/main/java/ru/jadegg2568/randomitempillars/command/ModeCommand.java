package ru.jadegg2568.randomitempillars.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.Arena;
import ru.jadegg2568.randomitempillars.arena.ArenaManager;
import ru.jadegg2568.randomitempillars.arena.ArenaMode;
import ru.jadegg2568.randomitempillars.arena.ArenaPlayer;
import ru.jadegg2568.randomitempillars.util.ColorReference;

public class ModeCommand implements CommandExecutor {

    private final ArenaManager arenaManager;

    public ModeCommand(Main main) {
        this.arenaManager = main.getArenaManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("You must be a player.");
            return true;
        }
        if (args.length == 0) {
            help(p);
            return true;
        }

        ArenaPlayer player = arenaManager.getPlayer(p.getUniqueId());
        if (player == null) {
            p.sendMessage(Component.text("Вы не находитесь на арене.").color(ColorReference.commandColor));
            return true;
        }

        Arena arena = arenaManager.getArena(player);
        if (arena == null) {
            p.sendMessage(Component.text("Вы не находитесь на арене.").color(ColorReference.commandColor));
            return true;
        }

        if (!arena.inWaiting()) {
            player.sendMessage(Component.text("Сейчас не время для голосования!").color(ColorReference.commandColor));
            return true;
        }

        String modeStr = args[0];
        ArenaMode mode;
        try {
            mode = ArenaMode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Неверный режим!").color(ColorReference.commandColor));
            player.playSound(Sound.ENTITY_VILLAGER_NO, 1, 1);
            return true;
        }
        player.setVote(mode);
        player.sendMessage(Component.text("Вы успешно проголосовали за режим " + mode.name() + ".").color(ColorReference.commandColor));
        player.playSound(Sound.ENTITY_VILLAGER_YES, 1, 1);
        return true;
    }

    public void help(Player p) {
        p.sendMessage(Component.text("/mode <режим>").color(ColorReference.commandColor));
    }
}
