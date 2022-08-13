package org.crypticplank.smp.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtil {
    private static void msg(CommandSender player, NamedTextColor color, String msg) {
        player.sendMessage(Component.text(msg, color));
    }

    public static void royal(CommandSender player, String msg) {
        msg(player, NamedTextColor.GOLD, msg);
    }

    public static void success(CommandSender player, String msg) {
        msg(player, NamedTextColor.GREEN, msg);
    }

    public static void error(CommandSender player, String msg) {
        msg(player, NamedTextColor.RED, msg);
    }

    public static void info(CommandSender player, String msg) {
        msg(player, NamedTextColor.AQUA, msg);
    }

    public static void title(Player player, String title, String subtitle) {
        player.showTitle(Title.title(Component.text(title), Component.text(subtitle)));
    }


    public static void actionRoyal(Player player, String msg) {
        actionMsg(player, NamedTextColor.GOLD, msg);
    }

    public static void actionSuccess(Player player, String msg) {
        actionMsg(player, NamedTextColor.GREEN, msg);
    }

    public static void actionError(Player player, String msg) {
        actionMsg(player, NamedTextColor.RED, msg);
    }

    public static void actionInfo(Player player, String msg) {
        actionMsg(player, NamedTextColor.AQUA, msg);
    }

    public static void actionMsg(Player player, NamedTextColor color, String msg) {
        player.sendActionBar(Component.text(msg, color));
    }
}
