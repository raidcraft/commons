package de.raidcraft.commons.util;

import de.raidcraft.RaidCraft;
import de.raidcraft.RaidCraftPlugin;
import de.raidcraft.api.player.PlayerResolver;
import de.raidcraft.commons.RCCommonsPlugin;
import de.raidcraft.commons.tables.TRcPlayer;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitPlayerResolver implements PlayerResolver {

    public BukkitPlayerResolver() {
        RaidCraft.registerComponent(PlayerResolver.class, this);
    }

    @Override
    public <T> UUID getUUIDfrom(T type) {

        if (type instanceof Entity) {
            return ((Entity) type).getUniqueId();
        } else {
            return UUID.randomUUID();
        }
    }

    @Override
    public UUID convertPlayer(@NonNull String name) {

        TRcPlayer tPlayer = RaidCraft.getComponent(RCCommonsPlugin.class).getRcDatabase()
                .find(TRcPlayer.class).where().eq("last_name", name).findOne();
        if (tPlayer != null) {
            return tPlayer.getUuid();
        }
        // if we are here there is no player for this displayName -> must be a npc
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    @Override
    public String getUUIDStringFromName(String name) {

        UUID uuid = convertPlayer(name);
        return uuid != null ? uuid.toString() : null;
    }

    @Override
    public String getNameFromUUID(UUID uuid) {

        if(uuid == null) {
            return null;
        }

        TRcPlayer tPlayer = RaidCraft.getComponent(RCCommonsPlugin.class).getRcDatabase()
                .find(TRcPlayer.class).where().eq("uuid", uuid.toString()).findOne();
        if (tPlayer != null) {
            return tPlayer.getLastName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if(offlinePlayer == null) {
            return null;
        }
        return offlinePlayer.getName();
    }

    @Override
    public String getNameFromUUID(String uuid) {

        return getNameFromUUID(UUID.fromString(uuid));
    }

    @Override
    public String castUUID(CommandSender sender) {

        return ((Player) sender).getUniqueId().toString();
    }

    @Override
    public int getPlayerId(Player player) {

        return getPlayerId(getUUIDfrom(player));
    }

    @Override
    public int getPlayerId(UUID uuid) {

        if (uuid == null) return 0;
        TRcPlayer rcPlayer = RaidCraft.getDatabase(RCCommonsPlugin.class).find(TRcPlayer.class).where().eq("uuid", uuid).findOne();
        if (rcPlayer != null) {
            return rcPlayer.getId();
        }
        return 0;
    }

    @Override
    public UUID getUuidFromPlayerId(int id) {

        TRcPlayer rcPlayer = RaidCraft.getDatabase(RCCommonsPlugin.class).find(TRcPlayer.class, id);
        if (rcPlayer != null) {
            return rcPlayer.getUuid();
        }
        return null;
    }
}
