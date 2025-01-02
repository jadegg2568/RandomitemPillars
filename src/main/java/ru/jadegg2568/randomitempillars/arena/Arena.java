package ru.jadegg2568.randomitempillars.arena;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.task.ArenaCountdownTask;
import ru.jadegg2568.randomitempillars.arena.task.ArenaEndTask;
import ru.jadegg2568.randomitempillars.arena.task.ArenaPlayingTask;
import ru.jadegg2568.randomitempillars.configuration.LocationType;
import ru.jadegg2568.randomitempillars.util.ColorReference;
import ru.jadegg2568.randomitempillars.util.TimeUtil;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Arena {

    private final String name;
    private final int minimum;
    private final int maximum;
    private final Material pillarMaterial;
    private final EnumMap<LocationType, Location> locations;
    private final ArenaWorld world;
    private final ArenaScoreboard scoreboard;
    private final Set<ArenaPlayer> players;
    private BukkitTask scoreboardTask;
    private ArenaCountdownTask countdownTask;
    private ArenaPlayingTask playingTask;
    private ArenaEndTask endTask;
    private ArenaMode mode;
    private ArenaPlayer winner;
    private ArenaState state;

    public Arena(String name, int minimum, int maximum, Material pillarMaterial, World w, EnumMap<LocationType, Location> locations) {
        this.name = name;
        this.minimum = minimum;
        this.maximum = maximum;
        this.pillarMaterial = pillarMaterial;
        this.locations = locations;
        this.world = new ArenaWorld(this, w);
        this.scoreboard = new ArenaScoreboard(Component.text("RANDOM ITEM PILLARS").color(ColorReference.scoreboardColorName));
        this.players = new HashSet<>();
        this.state = ArenaState.DISABLED;
    }

    public void load() {
        world.setupRules();
        world.clearEntities();

        changeState(ArenaState.WAITING);
        this.scoreboardTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::updateScoreboard, 0L, 10L);
    }

    public void unload() {
        players.forEach(ArenaPlayer::reset);
        players.forEach(player -> player.getPlayer().kick(Component.text("Арена выключается!").color(ColorReference.chatColorError)));

        if (scoreboardTask != null && !scoreboardTask.isCancelled())
            scoreboardTask.cancel();
        if (countdownTask != null && !countdownTask.isCancelled())
            countdownTask.cancel();
        if (playingTask != null && !playingTask.isCancelled())
            playingTask.cancel();
        if (endTask != null && !endTask.isCancelled())
            endTask.cancel();
        if (inGame())
            world.restoreWorld();

        changeState(ArenaState.DISABLED);
    }

    public void changeState(ArenaState state) {
        this.state = state;

        players.forEach(ArenaPlayer::reset);
        if (state == ArenaState.WAITING) {
            world.buildPillars(pillarMaterial);
            world.buildCages(Material.GLASS);
            teleportToSpawns();
            checkArena();
        } else if (state == ArenaState.PLAYING) {
            togglePlayingMechanics(true);
            playingTask = new ArenaPlayingTask(this);
            playingTask.start();
        } else if (state == ArenaState.END) {
            togglePlayingMechanics(false);
            endTask = new ArenaEndTask(this);
            endTask.start();
        }
    }

    public void updateScoreboard() {
        if (inWaiting()) {
            scoreboard.setLines(Lists.newArrayList(
                    Component.empty(),
                    Component.text("Игроков: ").color(ColorReference.scoreboardColor)
                            .append(Component.text(String.valueOf(getOnline())).color(ColorReference.scoreboardColor2)),
                    Component.empty(),
                    countdownTask == null ? Component.text("Ожидаем игроков...").color(ColorReference.scoreboardColor)
                            : Component.text("Время: ").color(ColorReference.scoreboardColor)
                            .append(Component.text(TimeUtil.getMMSS(countdownTask.getTime().get())).color(ColorReference.scoreboardColor2)),
                    Component.empty(),
                    Component.text("jadegg2568.github.io").color(ColorReference.scoreboardColor2)
            ));
        } else if (inGame() || inEnd()) {
            scoreboard.setLines(Lists.newArrayList(
                    Component.empty(),
                    Component.text("Живых: ").color(ColorReference.scoreboardColor)
                            .append(Component.text(String.valueOf(getOnline())).color(ColorReference.scoreboardColor2)),
                    Component.empty(),
                    Component.text("Предмет через:").color(ColorReference.scoreboardColor),
                    Component.text((playingTask == null ? "00:00"
                            : TimeUtil.getMMSS(playingTask.getGiveTime().get()) + " сек.")).color(ColorReference.scoreboardColor2),
                    Component.empty(),
                    Component.text("jadegg2568.github.io").color(ColorReference.scoreboardColor2)
            ));
        }
    }

    public boolean addPlayer(ArenaPlayer player) {
        if (state == ArenaState.DISABLED) return false;
        if (inWaiting() && getOnline() < maximum) {
            players.add(player);
            scoreboard.set(player);
            teleportToSpawns();
            player.reset();
            sendAll(Component.text(player.getName()).color(ColorReference.chatColor2)
                    .append(Component.text( " зашёл.").color(ColorReference.chatColor)));
            player.sendMessage(Component.text( "Проголосовать за режим - ").color(ColorReference.chatColor)
                    .append(Component.text( "/mode <режим>, возможные режимы (число означает задержку выдачи):").color(ColorReference.chatColor2)));
            for (ArenaMode arenaMode :ArenaMode.values()) {
                player.sendMessage(Component.text( "- " + arenaMode.name()).color(ColorReference.chatColor2));
            }

            checkArena();
            return true;
        } else {
            return false;
        }
    }

    public void removePlayer(ArenaPlayer player) {
        if (state == ArenaState.DISABLED) return;

        players.remove(player);
        player.setVote(null);
        player.reset();
        if (player.isSpectator())
            player.setSpectator(false);

        if (inWaiting()) {
            sendAll(Component.text(player.getName()).color(ColorReference.chatColor2)
                    .append(Component.text( " вышел.").color(ColorReference.chatColor)));
        } else if (inGame()) {
            sendAll(Component.text(player.getName()).color(ColorReference.gameChatColor2)
                    .append(Component.text( " покинул игру.").color(ColorReference.gameChatColor2)));
        }
        checkArena();
    }

    public void checkArena() {
        if (inWaiting()) {
            if (getOnline() == minimum) {
                countdownTask = new ArenaCountdownTask(this);
                countdownTask.start();
                sendAll(Component.text("Запуск обратного отсчёта...").color(ColorReference.chatColorSuccess));
                playSoundAll(Sound.ENTITY_VILLAGER_YES, 1, 1);
            } else if (getOnline() == (minimum - 1) && countdownTask != null && !countdownTask.isCancelled()) {
                countdownTask.cancel();
                sendAll(Component.text("Остановка обратного отсчёта...").color(ColorReference.chatColorError));
                playSoundAll(Sound.ENTITY_VILLAGER_NO, 1, 1);
            }
        } else if (inGame()) {
            if (getPlayersOnline().size() <= 1) {
                winner = getPlayersOnline().stream().findFirst().orElse(null);
                changeState(ArenaState.END);
            }
        }
    }

    public void resetAfterGame() {
        world.restoreWorld();
        players.forEach(player -> player.setVote(null));
        players.forEach(ArenaPlayer::reset);
        mode = null;
        winner = null;
    }

    public void teleportToSpawns() {
        LocationType[] locationTypes = {LocationType.SPAWN1, LocationType.SPAWN2, LocationType.SPAWN3, LocationType.SPAWN4,
                LocationType.SPAWN5, LocationType.SPAWN6, LocationType.SPAWN7, LocationType.SPAWN8};
        List<ArenaPlayer> players = new ArrayList<>(getPlayersOnline());
        for (int i = 0; i < Math.min(locationTypes.length, players.size()); i++) {
            ArenaPlayer player = players.get(i);
            player.teleport(locations.get(locationTypes[i]));
        }
    }

    public void togglePlayingMechanics(boolean toggle) {
        if (toggle) {
            EnumMap<ArenaMode, Integer> votes = new EnumMap<>(ArenaMode.class);
            for (ArenaPlayer player : getPlayersOnline()) {
                if (player.getVote() == null) continue;
                votes.put(player.getVote(), votes.getOrDefault(player.getVote(), 0) + 1);
                player.setVote(null);
            }
            mode = votes.keySet().stream()
                    .sorted(Comparator.comparingInt(votes::get))
                    .max(Comparator.naturalOrder())
                    .orElse(ArenaMode.ONE_ITEM_5);
            sendAll(Component.text("Победивший в результате голосов режим: ").color(ColorReference.gameChatColor)
                            .append(Component.text(mode.name()).color(ColorReference.gameChatColor2)
                            .append(Component.text(". Удачной игры!").color(ColorReference.gameChatColor))));
            sendTitleAll(Component.text("Режим: ").color(ColorReference.chatColor)
                            .append(Component.text( mode.name()).color(ColorReference.chatColor2)),
                    Component.text("Удачной игры!").color(ColorReference.chatColorSuccess), 3);
            playSoundAll(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

            players.forEach(player -> player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 3, 0)));
            world.buildCages(Material.AIR);
        } else {
            getPlayersSpectators().forEach(player -> player.setSpectator(false));
            sendTitleAll(Component.text("Победитель: ").color(ColorReference.chatColor)
                            .append(Component.text(winner == null ? "НИЧЬЯ" : winner.getName()).color(ColorReference.chatColor2)),
                    Component.text("Спасибо за игру!").color(ColorReference.chatColorSuccess), 3);
        }
    }

    public void playerDead(ArenaPlayer player) {
        player.setSpectator(true);
        player.teleport(locations.get(LocationType.SPECTATOR_SPAWN));

        sendAll(Component.text(player.getName()).color(ColorReference.gameChatColor2)
                .append(Component.text(" помер.").color(ColorReference.gameChatColor)));

        checkArena();
    }

    public void sendAll(Component message) {
        for (ArenaPlayer player : players) {
            player.sendMessage(message);
        }
    }

    public void playSoundAll(Sound sound, float volume, float pitch) {
        for (ArenaPlayer player : players) {
            player.playSound(sound, volume, pitch);
        }
    }

    public void sendTitleAll(Component title, Component subtitle, int durationSeconds) {
        for (ArenaPlayer player : players) {
            player.getPlayer().showTitle(Title.title(title, subtitle, Title.Times.times(Duration.ZERO, Duration.ofSeconds(durationSeconds), Duration.ZERO)));
        }
    }

    public void sendActionBarAll(Component message) {
        for (ArenaPlayer player : players) {
            player.getPlayer().sendActionBar(message);
        }
    }

    public ArenaPlayer getPlayer(UUID uuid) {
        for (ArenaPlayer pl : players) {
            if (pl.getUuid().equals(uuid))
                return pl;
        }
        return null;
    }

    public Set<ArenaPlayer> getPlayersOnline() {
        return players.stream().filter(player -> !player.isSpectator()).collect(Collectors.toSet());
    }

    public Set<ArenaPlayer> getPlayersSpectators() {
        return players.stream().filter(ArenaPlayer::isSpectator).collect(Collectors.toSet());
    }

    public int getOnline() {
        return getPlayersOnline().size();
    }

    public boolean inWaiting() {
        return state == ArenaState.WAITING;
    }

    public boolean inGame() {
        return state == ArenaState.PLAYING;
    }

    public boolean inEnd() {
        return state == ArenaState.END;
    }

    public boolean isDisabled() {
        return state == ArenaState.DISABLED;
    }

    public String getName() {
        return name;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public EnumMap<LocationType, Location> getLocations() {
        return locations;
    }

    public Set<ArenaPlayer> getPlayers() {
        return players;
    }

    public ArenaWorld getWorld() {
        return world;
    }

    public ArenaState getState() {
        return state;
    }

    public ArenaMode getMode() {
        return mode;
    }

    public ArenaPlayer getWinner() {
        return winner;
    }

    public Material getPillarMaterial() {
        return pillarMaterial;
    }
}
