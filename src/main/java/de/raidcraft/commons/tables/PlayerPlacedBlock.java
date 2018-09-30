package de.raidcraft.commons.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.RaidCraftPlugin;
import de.raidcraft.util.ChunkLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rc_player_placed_blocks")
@Getter
@Setter
public class PlayerPlacedBlock {

    public static boolean isPlayerPlaced(Block block) {

        return RaidCraft.getDatabase(RaidCraftPlugin.class).find(PlayerPlacedBlock.class).where()
                .eq("world", block.getWorld().getUID())
                .eq("x", block.getX())
                .eq("y", block.getY())
                .eq("z", block.getZ()).findOne() != null;
    }

    public static List<PlayerPlacedBlock> getPlayerPlacedBlocks(ChunkLocation chunkLocation) {

        return RaidCraft.getDatabase(RaidCraftPlugin.class).find(PlayerPlacedBlock.class).where()
                .eq("world", chunkLocation.getWorld())
                .eq("chunk_x", chunkLocation.getX())
                .eq("chunk_z", chunkLocation.getZ())
                .findList();
    }

    @Id
    private int id;
    private UUID world;
    private int chunkX;
    private int chunkZ;
    private int x;
    private int y;
    private int z;
    private Timestamp timestamp;

    public PlayerPlacedBlock() {

    }

    public PlayerPlacedBlock(Block block) {

        this(block.getWorld().getUID(), block.getChunk().getX(), block.getChunk().getZ(), block.getX(), block.getY(), block.getZ());
    }

    public PlayerPlacedBlock(UUID world, int chunkX, int chunkZ, int x, int y, int z) {

        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }
}
