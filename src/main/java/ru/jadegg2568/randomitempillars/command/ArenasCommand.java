package ru.jadegg2568.randomitempillars.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.Arena;
import ru.jadegg2568.randomitempillars.arena.ArenaManager;
import ru.jadegg2568.randomitempillars.arena.ArenaPlayer;
import ru.jadegg2568.randomitempillars.arena.ArenaState;
import ru.jadegg2568.randomitempillars.configuration.ConfigManager;
import ru.jadegg2568.randomitempillars.configuration.LocationType;
import ru.jadegg2568.randomitempillars.util.ColorReference;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArenasCommand implements CommandExecutor {

    private static final String ADMIN_PERMISSION = "randomitempillars.commands.arenas.admin";
    private final Main main;
    private final ConfigManager configManager;
    private final ArenaManager arenaManager;
    private final Map<Player, EnumMap<LocationType, Location>> placedSpawns = new HashMap<>();

    public ArenasCommand(Main main) {
        this.main = main;
        this.configManager = main.getConfigManager();
        this.arenaManager = main.getArenaManager();
    }

                         @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("You aren't a player!");
            return true;
        }
        if (args.length == 0) {
            help(p);
            return true;
        }
        switch (args[0]) {
         case "create":
             create(p, args);
             break;
         case "remove":
             remove(p, args);
             break;
         case "setSpawn":
             setSpawn(p, args);
             break;
        case "info":
            info(p, args);
            break;
        case "start":
            start(p, args);
            break;
        case "end":
            end(p, args);
            break;
        case "check":
            check(p, args);
            break;
        case "loadWorld":
            loadWorld(p, args);
            break;
        case "teleportWorld":
            teleportWorld(p, args);
            break;
         case "join":
             join(p, args);
             break;
         case "leave":
             leave(p);
             break;
         default:
             help(p);
             break;
        }
        return true;
    }

    public void help(Player p) {
        TextColor discordColor = TextColor.fromHexString("#2B00FF");
        TextColor telegramColor = TextColor.fromHexString("#4BF2FF");

        p.sendMessage(Component.text("------------ИНСТРУКЦИЯ-ПО-КОМАНДАМ------------").color(ColorReference.commandColor));
        if (p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("/arenas create <название> <минимум> <максимум>").color(ColorReference.commandColor));
            p.sendMessage(Component.text("/arenas remove <название>").color(ColorReference.commandColor));
            p.sendMessage(Component.text("/arenas setSpawn <арена> <название>").color(ColorReference.commandColor));
            p.sendMessage(Component.text("/arenas info <арена>").color(ColorReference.commandColor));
            p.sendMessage(Component.text("/arenas start <арена>").color(ColorReference.commandColor));
            p.sendMessage(Component.text("/arenas end <арена>").color(ColorReference.commandColor));
            p.sendMessage(Component.text("/arenas check <арена>").color(ColorReference.commandColor));
            p.sendMessage(Component.text("/arenas loadWorld <мир>").color(ColorReference.commandColor));
            p.sendMessage(Component.text("/arenas teleportWorld <мир>").color(ColorReference.commandColor));
        }
        p.sendMessage(Component.text("/arenas join <арена>").color(ColorReference.commandColor));
        p.sendMessage(Component.text("/arenas leave").color(ColorReference.commandColor));
        p.sendMessage(Component.text("----------------------------------------------").color(ColorReference.commandColor));
        if (p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Создано игроком JadeGG_").color(ColorReference.commandColor));
            p.sendMessage(Component.text("Контакты:").color(ColorReference.commandColor));
            p.sendMessage(Component.text("Discord: itsjadegg").color(discordColor));
            p.sendMessage(Component.text("Telegram: jadetgt").color(telegramColor));
            p.sendMessage(Component.text("----------------------------------------------").color(ColorReference.commandColor));
        }
    }

    public void create(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 5) {
            help(p);
            return;
        }
        String name = args[1];
        Optional<Integer> minimumOptional = parseInt(args[2]);
        Optional<Integer> maximumOptional = parseInt(args[3]);
        Material pillarMaterial;

        try {
            pillarMaterial = Material.valueOf(args[4]);
        } catch (IllegalArgumentException e) {
            p.sendMessage(Component.text("Ошибка: Некорректный материал столбов.").color(ColorReference.commandColor));
            return;
        }

        if (minimumOptional.isEmpty()) {
            p.sendMessage(Component.text("Ошибка: Некорректный минимум.").color(ColorReference.commandColor));
            return;
        }
        if (maximumOptional.isEmpty()) {
            p.sendMessage(Component.text("Ошибка: Некорректный максимум.").color(ColorReference.commandColor));
            return;
        }
        int minimum = minimumOptional.get();
        int maximum = maximumOptional.get();

        if (arenaManager.getArena(name) != null) {
            p.sendMessage(Component.text("Ошибка: Арена с таким именем уже существует.").color(ColorReference.commandColor));
            return;
        }

        EnumMap<LocationType, Location> locations = placedSpawns.remove(p);

        arenaManager.addArena(new Arena(name, minimum, maximum, pillarMaterial, p.getWorld(), locations == null ? new EnumMap<>(LocationType.class) : locations));
    }

    public void remove(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 2) {
            help(p);
            return;
        }
        String name = args[1];

        Arena arena = arenaManager.getArena(args[1]);
        if (arena == null) {
            p.sendMessage(Component.text("Ошибка: Арена не найдена!").color(ColorReference.commandColor));
            return;
        }

        arenaManager.removeArena(name);
    }

    public void setSpawn(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 2) {
            help(p);
            return;
        }

        LocationType type;
        try {
            type = LocationType.valueOf(args[1]);
        } catch (IllegalArgumentException e) {
            p.sendMessage(Component.text("Ошибка: Неверный тип спавна. Возможные спавны: SPAWN1-8, CENTER, SPECTATOR_SPAWN, POS1, POS2").color(ColorReference.commandColor));
            return;
        }

        Location loc = p.getLocation();

        if (!placedSpawns.containsKey(p)) {
            placedSpawns.put(p, new EnumMap<>(LocationType.class));
        }
        placedSpawns.get(p).put(type, loc);

        p.sendMessage(Component.text("Успешно установлена точка " + type.name() + " на:" +
                " World: " + loc.getWorld().getName() +
                " X: " + loc.getX() +
                " Y: " + loc.getY() +
                " Z: " + loc.getZ() +
                " Pitch: " + loc.getPitch() +
                " Yaw: " + loc.getYaw()).color(ColorReference.commandColor));
    }

    public void info(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 2) {
            help(p);
            return;
        }

        Arena arena = arenaManager.getArena(args[1]);
        if (arena == null) {
            p.sendMessage(Component.text("Ошибка: Арена не найдена!").color(ColorReference.commandColor));
            return;
        }

        p.sendMessage(Component.text("-------------------").color(ColorReference.commandColor));
        p.sendMessage(Component.text("Название: " + arena.getName()).color(ColorReference.commandColor));
        p.sendMessage(Component.text("Минимум: " + arena.getMinimum()).color(ColorReference.commandColor));
        p.sendMessage(Component.text("Максимум: " + arena.getMaximum()).color(ColorReference.commandColor));
        p.sendMessage(Component.text("Установленные точки: " + arena.getLocations().keySet().stream().map(Enum::name).collect(Collectors.joining(", "))).color(ColorReference.commandColor));
        p.sendMessage(Component.text("Игроков: " + arena.getPlayersOnline().size()).color(ColorReference.commandColor));
        p.sendMessage(Component.text("Спектаторов: " + arena.getPlayersSpectators().size()).color(ColorReference.commandColor));
        p.sendMessage(Component.text("Состояние: " + arena.getState().name()).color(ColorReference.commandColor));
        p.sendMessage(Component.text("-------------------").color(ColorReference.commandColor));
    }

    public void start(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 2) {
            help(p);
            return;
        }

        Arena arena = arenaManager.getArena(args[1]);
        if (arena == null) {
            p.sendMessage(Component.text("Ошибка: Арена не найдена!").color(ColorReference.commandColor));
            return;
        }

        if (arena.inWaiting()) {
            arena.changeState(ArenaState.PLAYING);
        } else {
            p.sendMessage(Component.text("Ошибка: Арена должна быть в ожидании.").color(ColorReference.commandColor));
        }
    }

    public void end(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 2) {
            help(p);
            return;
        }

        Arena arena = arenaManager.getArena(args[1]);
        if (arena == null) {
            p.sendMessage(Component.text("Ошибка: Арена не найдена!").color(ColorReference.commandColor));
            return;
        }

        if (arena.inGame()) {
            arena.changeState(ArenaState.END);
        } else {
            p.sendMessage(Component.text("Ошибка: Арена должна быть в ожидании.").color(ColorReference.commandColor));
        }
    }

    public void check(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 2) {
            help(p);
            return;
        }

        Arena arena = arenaManager.getArena(args[1]);
        if (arena == null) {
            p.sendMessage(Component.text("Ошибка: Арена не найдена!").color(ColorReference.commandColor));
            return;
        }
        arena.checkArena();
    }

    public void loadWorld(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 2) {
            help(p);
            return;
        }

        String name = args[1];
        World w = Bukkit.createWorld(new WorldCreator(name));

    }

    public void teleportWorld(Player p, String[] args) {
        if (!p.hasPermission(ADMIN_PERMISSION)) {
            p.sendMessage(Component.text("Ошибка: У вас нет прав!").color(ColorReference.commandColor));
            return;
        }
        if (args.length < 2) {
            help(p);
            return;
        }

        String name = args[1];
        World w = Bukkit.getWorld(name);

        if (w == null) {
            p.sendMessage(Component.text("Ошибка: Мир не найден.").color(ColorReference.commandColor));
            return;
        }

        Location loc = p.getLocation().clone();
        loc.setWorld(w);
        p.teleportAsync(loc);
    }

    public void join(Player p, String[] args) {
        ArenaPlayer player = arenaManager.getPlayer(p.getUniqueId());
        if (player == null) {
            player = new ArenaPlayer(p.getUniqueId(), p);
        }
        if (args.length < 2) {
            help(p);
            return;
        }

        Arena arena = arenaManager.getArena(args[1]);
        if (arena == null) {
            p.sendMessage(Component.text("Ошибка: Арена не найдена!").color(ColorReference.commandColor));
            return;
        }

        if (arena.getPlayer(p.getUniqueId()) != null) {
            p.sendMessage(Component.text("Ошибка: Вы уже на арене!").color(ColorReference.commandColor));
            return;
        }
        arenaManager.addToArena(player, arena);
    }

    public void leave(Player p) {
        ArenaPlayer player = arenaManager.getPlayer(p.getUniqueId());
        if (player == null) {
            p.sendMessage(Component.text("Ошибка: Арена не найдена!").color(ColorReference.commandColor));
            return;
        }

        Arena arena = arenaManager.getArena(player);
        if (arena == null) {
            p.sendMessage(Component.text("Ошибка: Арена не найдена!").color(ColorReference.commandColor));
            return;
        }

        arenaManager.removeFromArena(player, arena);
        if (!p.isOp() && arenaManager.getArenas().size() == 1) {
            p.kick(Component.text("Вы вышли.").color(ColorReference.chatColorError));
        }
    }

    public Optional<Integer> parseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
