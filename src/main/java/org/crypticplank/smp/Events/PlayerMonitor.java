package org.crypticplank.smp.Events;

import it.unimi.dsi.fastutil.Hash;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.crypticplank.smp.Config.ConfigFields;
import org.crypticplank.smp.Config.GuestList;
import org.crypticplank.smp.Config.TwitchNames;
import org.crypticplank.smp.Discord.Discord;
import org.crypticplank.smp.Util.*;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class PlayerMonitor implements Listener {
    private Plugin plugin;
    private final TwitchNames twitchNames;
    private final GuestList guestList;
    private Discord discord;
    private Twitch twitch;
    public PlayerMonitor(Plugin plugin, TwitchNames twitchNames, GuestList guestList, Twitch twitch, Discord discord) {
        this.plugin = plugin;
        this.twitchNames = twitchNames;
        this.guestList = guestList;
        this.discord = discord;
        this.twitch = twitch;
    }

    static ArrayList<UUID> noTwitch = new ArrayList<>();

    /**
     * Links a Twitch account name to your player UUID.
     * @param user
     * @param twitch
     */
    public void linkTwitch(UUID user, String twitch) {
        noTwitch.remove(user);
        twitchNames.config.set(user.toString(), twitch);
        twitchNames.save();
    }

    public Tuple<Boolean, String> addGuest(UUID user, String guestName) {
        UUID guestUUID = MinecraftUUID.GetUUID(guestName);
        if(guestUUID == null) {
            return new Tuple<>(false, "Could not find players UUID");
        }

        if(twitchNames.config.contains(guestUUID.toString())) {
            return new Tuple<>(false, "You cannot add a member to the guest list");
        }

        HashMap<String, Long> map = (HashMap<String, Long>)guestList.config.get(user.toString());
        if(map == null) {
            map = new HashMap<>();
        }
        if(map.size() >= 3) {
            return new Tuple<>(false, "Your guest limit has been reached");
        }
        map.put(guestUUID.toString(), Instant.now().getEpochSecond() + ((60 * 60) * 10));
        guestList.config.setComments(user.toString(), List.of(Objects.requireNonNull(Bukkit.getPlayer(user)).getName()));
        guestList.config.set(user.toString(), map);
        guestList.save();
        return new Tuple<>(true, null);
    }

    public void removeGuest(UUID user, UUID guest) {
        HashMap<String, Long> map = (HashMap<String, Long>)guestList.config.get(user.toString());
        if(map == null) {
            map = new HashMap<>();
        }
        map.remove(guest.toString());
        guestList.config.set(user.toString(), map);
        guestList.save();
    }

    /**
     * Check if the player is linked to twitch.
     * @param player
     * @return
     */
    public String checkPlayer(Player player) {
        if (noTwitch.contains(player.getUniqueId())) return null;
        String twitchname = twitchNames.config.getString(player.getUniqueId().toString());
        if (twitchname == null && !(player.isOp() && !(Boolean)ConfigHelper.get(ConfigFields.PREFS_KICK_OP))) {
            ChatUtil.info(player, (String)ConfigHelper.get(ConfigFields.MESSAGES_NO_TWITCH));
            noTwitch.add(player.getUniqueId());
        }
        return twitchname;
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        new Thread(() -> {
            String twitchname = twitchNames.config.getString(event.getPlayer().getUniqueId().toString());
            if(twitchname == null)
                return;
            try {
                Twitch.TwitchStream stream = twitch.GetStream(twitchname, true);
                if(stream.IsOnline) {
                    discord.addEmbed(new Discord.EmbedObject()
                            .setThumbnail(stream.ProfilePictureUrl)
                            .addField("Live", String.format("%s is now streaming on Live SMP", twitchname), true)
                            .setDescription("https://twitch.tv/"+twitchname)
                            .setFooter("https://twitch.tv/"+twitchname, stream.ProfilePictureUrl)
                    );
                    discord.execute();
                }
            } catch (Exception ignored) {}
        }).start();
        checkPlayer(event.getPlayer());
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerMove(PlayerMoveEvent event) {
        if (noTwitch.contains(event.getPlayer().getUniqueId())) {
            for (HashMap.Entry<String, Object> set : guestList.config.getValues(true).entrySet()) {
                //System.out.println(set.getKey() + " = " + set.getValue());
                Map<String, Long> guests = (HashMap<String, Long>)set.getValue();
                for (HashMap.Entry<String, Long> guest : guests.entrySet()) {
                    if(event.getPlayer().getUniqueId().toString().equals(guest.getKey())) {
                        if(Instant.now().getEpochSecond() <= guest.getValue()) {
                            return;
                        } else {
                            removeGuest(UUID.fromString(set.getKey()), event.getPlayer().getUniqueId());
                        }
                    }
                }
            }
            event.setCancelled(true);
            ChatUtil.actionError(event.getPlayer(), (String)ConfigHelper.get(ConfigFields.MESSAGES_NO_TWITCH));
        }
    }

    // Cryptofyres custom death message
    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getPlayer().getUniqueId().equals(UUID.fromString("c0de0d51-ac4b-48bb-8b4f-69864457164a"))) {
            event.deathMessage(ComponentUtil.serializedComponent(String.format("<rainbow>%s died lit</rainbow>", event.getPlayer().getName())));
        }
    }
}
