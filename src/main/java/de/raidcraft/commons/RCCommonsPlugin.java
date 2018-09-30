package de.raidcraft.commons;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.NestedCommand;
import de.raidcraft.RaidCraft;
import de.raidcraft.RaidCraftPlugin;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.action.flow.FlowType;
import de.raidcraft.api.action.requirement.tables.TPersistantRequirement;
import de.raidcraft.api.action.requirement.tables.TPersistantRequirementMapping;
import de.raidcraft.api.config.Comment;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.MultiComment;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.config.builder.ConfigGenerator;
import de.raidcraft.api.inventory.TPersistentInventory;
import de.raidcraft.api.inventory.TPersistentInventorySlot;
import de.raidcraft.api.inventory.sync.TPlayerInventory;
import de.raidcraft.api.player.PlayerResolver;
import de.raidcraft.api.player.PlayerStatisticProvider;
import de.raidcraft.api.storage.TObjectStorage;
import de.raidcraft.commons.tables.*;
import de.raidcraft.commons.tracking.BlockTracking;
import de.raidcraft.commons.util.BukkitPlayerResolver;
import de.raidcraft.util.BlockTracker;
import de.raidcraft.util.BlockUtil;
import de.raidcraft.util.TimeUtil;
import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class RCCommonsPlugin extends BasePlugin implements Listener {

    private LocalConfiguration config;
    private BlockTracker blockTracker;
    private PlayerResolver playerResolver;
    private final Map<UUID, Integer> playerLogs = new HashMap<>();

    @Override
    public void enable() {

        this.config = configure(new LocalConfiguration(this));
        this.blockTracker = new BlockTracking(this);
        this.playerResolver = new BukkitPlayerResolver();
        registerEvents(this);

        // sync all ActionAPI stuff into Database
        Bukkit.getScheduler().runTaskLater(this, this::trackActionApi, TimeUtil.secondsToTicks(getConfig().actoionapiSyncDelay));
    }

    @Override
    public void disable() {

        for (UUID uuid : new ArrayList<>(playerLogs.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                completePlayerLog(player);
            }
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {

        List<Class<?>> classes = new ArrayList<>();
        classes.add(PlayerPlacedBlock.class);
        classes.add(TObjectStorage.class);
        classes.add(TPersistentInventory.class);
        classes.add(TPersistentInventorySlot.class);
        classes.add(TPersistantRequirement.class);
        classes.add(TPersistantRequirementMapping.class);
        classes.add(TCommand.class);
        classes.add(TActionApi.class);
        classes.add(TRcPlayer.class);
        classes.add(TListener.class);
        classes.add(TPlugin.class);
        classes.add(TPlayerLog.class);
        classes.add(TPlayerLogStatistic.class);
        classes.add(TPlayerInventory.class);
        return classes;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {

        createPlayerLog(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {

        completePlayerLog(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event) {

        completePlayerLog(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {

        completePlayerLog(event.getPlayer());
        createPlayerLog(event.getPlayer());
    }

    private void createPlayerLog(Player player) {

        TPlayerLog log = new TPlayerLog();
        Timestamp joinTime = Timestamp.from(Instant.now());
        log.setJoinTime(joinTime);
        log.setPlayer(player.getUniqueId());
        log.setName(player.getName());
        log.setWorld(player.getLocation().getWorld().getName());
        getRcDatabase().save(log);
        for (Statistic statistic : Statistic.values()) {
            if (!statistic.isSubstatistic()) {
                TPlayerLogStatistic stat = new TPlayerLogStatistic();
                stat.setLog(log);
                stat.setStatistic(statistic.name());
                stat.setLogonValue(player.getStatistic(statistic));
                getRcDatabase().save(stat);
            }
        }
        for (Map.Entry<String, PlayerStatisticProvider> playerStat : RaidCraft.getStatisticProviders().entrySet()) {
            TPlayerLogStatistic stat = new TPlayerLogStatistic();
            stat.setLog(log);
            stat.setStatistic(playerStat.getKey());
            stat.setLogonValue(playerStat.getValue().getStatisticValue(player));
            getRcDatabase().save(stat);
        }
        TPlayerLog addedLog = getRcDatabase().find(TPlayerLog.class).where().eq("player", player.getUniqueId()).eq("join_time", joinTime).findOne();
        if (addedLog == null) {
            getLogger().warning("Could not find added log for " + player.getName());
        } else {
            playerLogs.put(player.getUniqueId(), addedLog.getId());
        }
    }

    private void completePlayerLog(Player player) {

        if (player == null || player.getUniqueId() == null || !playerLogs.containsKey(player.getUniqueId())) return;
        int id = playerLogs.remove(player.getUniqueId());
        TPlayerLog log = getRcDatabase().find(TPlayerLog.class, id);
        if (log == null) {
            getLogger().warning("Could not find player log with id " + id);
            return;
        }
        log.setQuitTime(Timestamp.from(Instant.now()));
        getRcDatabase().update(log);
        Set<String> stats = Arrays.asList(Statistic.values()).stream().map(s -> s.name()).collect(Collectors.toSet());
        List<TPlayerLogStatistic> statistics = log.getStatistics();
        for (TPlayerLogStatistic statistic : statistics) {
            if (stats.contains(statistic.getStatistic())) {
                statistic.setLogoffValue(player.getStatistic(Statistic.valueOf(statistic.getStatistic())));
            } else {
                PlayerStatisticProvider provider = RaidCraft.getStatisticProvider(statistic.getStatistic());
                if (provider != null) {
                    statistic.setLogoffValue(provider.getStatisticValue(player));
                }
            }
            getRcDatabase().update(statistic);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void preJoinUUID(AsyncPlayerPreLoginEvent event) {

        UUID uuid = event.getUniqueId();
        String name = event.getName();

        TRcPlayer player = getRcDatabase().find(TRcPlayer.class)
                .where().eq("uuid", uuid.toString()).findOne();
        // known player
        if (player != null) {
            // if displayName changed
            if (!player.getLastName().equalsIgnoreCase(name)) {
                getLogger().warning("---- NAME CHANGE FOUND (" + uuid + ") !!! ----");
                getLogger().warning("---- old displayName (" + player.getLastName() + ") !!! ----");
                getLogger().warning("---- new displayName (" + name + ") !!! ----");
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        "You changed your playername. Contact raid-craft.de to reactivate.");
            }
            player.setLastJoined(new Date());
            getRcDatabase().save(player);
            return;
        }
        // new player
        player = getRcDatabase().find(TRcPlayer.class)
                .where().ieq("last_name", name).findOne();
        // check if displayName already in use
        if (player != null) {
            getLogger().warning("---- NEW UUID FOR NAME (" + name + ") FOUND !!! ----");
            getLogger().warning("---- new uuid (" + uuid + ") FOUND !!! ----");
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Your playername is protected. Visit raid-craft.de for more informations");
            return;
        }
        // add new player
        player = new TRcPlayer();
        player.setLastName(name);
        player.setUuid(uuid);
        Date currentTime = new Date();
        player.setFirstJoined(currentTime);
        player.setLastJoined(currentTime);
        getRcDatabase().save(player);
    }

    /**
     * Do not call this method
     * use registerCommands(Class<?> class, String host)
     *
     * @param clazz
     */
    public void trackCommand(Class<?> clazz, String host, String baseClass) {

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            Command anno_cmd = method.getAnnotation(Command.class);
            if (anno_cmd == null) {
                continue;
            }
            NestedCommand anno_nested = method.getAnnotation(NestedCommand.class);
            if (anno_nested != null) {
                for (Class<?> childClass : anno_nested.value()) {
                    trackCommand(childClass, host, TCommand.printArray(anno_cmd.aliases()));
                }
                continue;
            }
            getRcDatabase().save(TCommand.parseCommand(method, host, baseClass));
        }
    }

    public void trackActionApi() {
        getRcDatabase().find(TActionApi.class).delete();
        trackActionApi(FlowType.ACTION, ActionAPI.getActions());
        trackActionApi(FlowType.REQUIREMENT, ActionAPI.getRequirements());
        trackActionApi(FlowType.TRIGGER, ActionAPI.getTrigger());
    }

    public <T extends ConfigGenerator> void trackActionApi(FlowType type, Map<String, T> map) {

        EbeanServer db = RaidCraftPlugin.getPlugin(RaidCraftPlugin.class).getRcDatabase();
        String server = Bukkit.getServerName();
        for (String key : map.keySet()) {
            T entry = map.get(key);
            if (entry == null) {
                continue;
            }
            TActionApi actionApi = db.find(TActionApi.class)
                    .where()
                    .eq("name", key)
                    .eq("action_type", type.name().toLowerCase())
                    .findOne();
            if (actionApi == null) {
                actionApi = new TActionApi();
                actionApi.setName(key);
                actionApi.setAction_type(type.name().toLowerCase());
                actionApi.setServer(server);
                Optional<ConfigGenerator.Information> information = Optional.empty();
                switch (type) {
                    case ACTION:
                        information = ActionAPI.getActionInformation(key);
                        break;
                    case REQUIREMENT:
                        information = ActionAPI.getRequirementInformation(key);
                        break;
                    case TRIGGER:
                        information = ActionAPI.getTriggerInformation(key);
                        break;
                }
                if (information.isPresent()) {
                    actionApi.setDescription(information.get().desc());
                    actionApi.setConf(String.join(";", information.get().conf()));
                }
            }
            actionApi.setActive(true);
            actionApi.setLastActive(new Date());
            db.save(actionApi);
        }
    }

    public static class LocalConfiguration extends ConfigurationBase<RCCommonsPlugin> {

        public LocalConfiguration(RCCommonsPlugin plugin) {

            super(plugin, "config.yml");
        }

        @Setting("check-player-block-placement")
        public boolean checkPlayerBlockPlacement = false;
        @Setting("player-placed-block-worlds")
        public List<String> player_placed_block_worlds = new ArrayList<>();
        @Setting("actionapi-to-db-delay")
        public int actoionapiSyncDelay = 10;
        @Setting("hide-attributes")
        public boolean hideAttributes = true;

        @Setting("pastebin.apikey")
        @Comment("Get your pastebin api key from: https://pastebin.com/api")
        public String pastebinApiKey = "";
    }
}
