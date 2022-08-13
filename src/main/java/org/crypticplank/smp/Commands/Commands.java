package org.crypticplank.smp.Commands;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.crypticplank.smp.Config.ConfigFields;
import org.crypticplank.smp.Config.GuestList;
import org.crypticplank.smp.Events.PlayerMonitor;
import org.crypticplank.smp.Smp;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.crypticplank.smp.Util.ChatUtil;
import org.crypticplank.smp.Util.ComponentUtil;
import org.crypticplank.smp.Util.ConfigHelper;
import org.crypticplank.smp.Util.Update;

public class Commands implements CommandExecutor, Listener {
    private Smp plugin;
    private PlayerMonitor monitor;

    public Commands(Smp plugin, PlayerMonitor monitor) {
        this.plugin = plugin;
        this.monitor = monitor;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can send that command!");
            return true;
        }
        Player player = (Player)sender;
        String playername = player.getName();
        if (command.getName().equalsIgnoreCase("reload")) {
            if (player.isOp() || player.hasPermission("smp-reload") || playername.equals("crypticplank")) {
                this.plugin.saveDefaultConfig();
                this.plugin.reloadConfig();
                this.plugin.getServer().reload();
                sender.sendMessage("" + ChatColor.GREEN + "[Smp] reloaded");
            }
        } else if (command.getName().equalsIgnoreCase("restart")) {
            if(player.isOp() || player.hasPermission("smp-reload") || playername.equals("crypticplank") || (Boolean)ConfigHelper.get(ConfigFields.PERMISSIONS_SMP_RELOAD_DEFAULT)) {
                sender.sendMessage("" + ChatColor.GREEN + "[Smp] Restarting");
                this.plugin.getServer().dispatchCommand((CommandSender)this.plugin.getServer().getConsoleSender(), "restart");
            } else {
                sender.sendMessage((String)ConfigHelper.get(ConfigFields.PERMISSIONS_MESSAGE));
            }
        } else if (command.getName().equalsIgnoreCase("twitchchecklink")) {
            if (args.length == 0) return false;
            String twitch = args[0];
            String mc = null;
            if(args.length > 1) {
                mc = args[1];
            }

            Player cmdPlayer;
            if(mc != null && sender.isOp() || (Boolean)ConfigHelper.get(ConfigFields.PREFS_NON_OP_LINK_OTHER_PLAYERS)) {
                cmdPlayer = Bukkit.getPlayer(mc);
            } else {
                cmdPlayer = player;
            }

            if (cmdPlayer == null) {
                ChatUtil.error(sender, "No player by name " + mc);
                return true;
            }

            if(twitch.contains("/") || twitch.contains("twitch.tv")) {
                cmdPlayer.sendMessage(Component.text("Please just use your username"));
                return true;
            }

            monitor.linkTwitch(player.getUniqueId(), twitch);
            cmdPlayer.kick(Component.text("Please reconnect, account linked."));
        } else if (command.getName().equalsIgnoreCase("guest")) {
            if (args.length == 0) return false;
            String guestName = args[0];
            Tuple<Boolean, String> guest = monitor.addGuest(player.getUniqueId(), guestName);
            if(guest.getA()) {
                player.sendMessage(Component.text("Added " + guestName + " as a guest for 10 hours"));
            } else {
                player.sendMessage(Component.text(guest.getB(), NamedTextColor.RED));
            }
        } else if (command.getName().equalsIgnoreCase("update")) {
            if(!sender.isOp() && !playername.equals("crypticplank")) {
                sender.sendMessage(ComponentUtil.serializedComponent("<rainbow>You cannot use this command :)</rainbow>"));
                return true;
            }
            if (args.length == 0) {
                Update update = new Update(this.plugin, "https://brandonplank.org/smp/smp-1.0.0-SNAPSHOT-1.19.jar");
                update.update(sender);
                return true;
            } else {
                String arg = args[0];
                if(arg.equals("reboot")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "restart");
                        }
                    }.runTask(plugin);
                } else {
                    try {
                        new URL(arg);
                    } catch (MalformedURLException e) {
                        sender.sendMessage(Component.text("Bad URL", NamedTextColor.RED));
                    }
                    Update update = new Update(this.plugin, arg);
                    update.update(sender);
                }
            }
        }
        return true;
    }
}
