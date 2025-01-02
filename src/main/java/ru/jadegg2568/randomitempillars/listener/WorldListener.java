package ru.jadegg2568.randomitempillars.listener;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import ru.jadegg2568.randomitempillars.Main;
import ru.jadegg2568.randomitempillars.arena.Arena;
import ru.jadegg2568.randomitempillars.arena.ArenaManager;
import ru.jadegg2568.randomitempillars.arena.ArenaPlayer;

import java.util.List;

public class WorldListener implements Listener {

    private final Main main;
    private final ArenaManager arenaManager;

    public WorldListener(Main main) {
        this.main = main;
        this.arenaManager =  main.getArenaManager();
    }

    public boolean cannotBlockChanged(BlockState newState, Player p) {
        Arena arena = null;
        if (p != null) {
            ArenaPlayer player = arenaManager.getPlayer(p.getUniqueId());
            if (player != null) {
                arena = arenaManager.getArena(player);
            }
        } else {
            arena = arenaManager.getArenaByWorld(newState.getWorld());
        }

        if (arena != null) {
            if (arena.inGame() || arena.isDisabled()) {
                return false;
            } else {
                arena.getWorld().saveChange(newState);
                return true;
            }
        } else {
            return false;
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (cannotBlockChanged(e.getBlockReplacedState(), p))
            e.setCancelled(true);
    }

    @EventHandler
    public void onMultiPlace(BlockMultiPlaceEvent e) {
        Player p = e.getPlayer();
        for (BlockState state : e.getReplacedBlockStates())
            if (cannotBlockChanged(state, p))
                e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (cannotBlockChanged(e.getBlock().getState(), p))
            e.setCancelled(true);
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e) {
        List<Block> blockList = e.blockList();
        for (Block b : blockList) {
            if (cannotBlockChanged(b.getState(), null)) {
                blockList.clear();
                break;
            }
        }
    }

    @EventHandler
    public void onBurn(BlockBurnEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(BlockDamageEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onFade(BlockFadeEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onForm(BlockFormEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onFromTo(BlockFromToEvent e) {
        if (cannotBlockChanged(e.getToBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onGrow(BlockGrowEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPhysics(BlockPhysicsEvent e) {
        if (cannotBlockChanged(e.getSourceBlock().getState(), null) && cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks())
            if (cannotBlockChanged(b.getState(), null))
                e.setCancelled(true);
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks())
            if (cannotBlockChanged(b.getState(), null))
                e.setCancelled(true);
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setNewCurrent(e.getOldCurrent());
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onFluidPlace(PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        if (cannotBlockChanged(e.getBlock().getState(), p))
            e.setCancelled(true);
    }

    @EventHandler
    public void onFluidTake(PlayerBucketFillEvent e) {
        Player p = e.getPlayer();
        if (cannotBlockChanged(e.getBlock().getState(), p))
            e.setCancelled(true);
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent e) {
        if (cannotBlockChanged(e.getBlock().getState(), null))
            e.setCancelled(true);
    }
}
