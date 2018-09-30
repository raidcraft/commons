package de.raidcraft.commons.tracking;

import de.raidcraft.RaidCraft;
import de.raidcraft.RaidCraftPlugin;
import de.raidcraft.api.Component;
import de.raidcraft.commons.RCCommonsPlugin;
import de.raidcraft.commons.tables.PlayerPlacedBlock;
import de.raidcraft.util.BlockTracker;
import lombok.Data;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

@Data
public class BlockTracking implements BlockTracker, Listener {

    private final RCCommonsPlugin plugin;

    public BlockTracking(RCCommonsPlugin plugin) {
        this.plugin = plugin;
        plugin.registerEvents(this);
        RaidCraft.registerComponent(BlockTracker.class, this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPlacedBlock(BlockPlaceEvent event) {

        if (getPlugin().getConfig().checkPlayerBlockPlacement) {
            addPlayerPlacedBlock(event.getBlock());
        }
    }

    public void addPlayerPlacedBlock(Block block) {

        if (!getPlugin().getConfig().checkPlayerBlockPlacement || isPlayerPlacedBlock(block)) {
            return;
        }
        RaidCraft.getDatabase(RaidCraftPlugin.class).save(new PlayerPlacedBlock(block));
    }

    public boolean isPlayerPlacedBlock(Block block) {

        if (!plugin.getConfig().checkPlayerBlockPlacement) {
            return false;
        }
        return PlayerPlacedBlock.isPlayerPlaced(block);
    }
}
