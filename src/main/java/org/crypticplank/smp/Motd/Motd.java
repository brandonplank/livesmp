package org.crypticplank.smp.Motd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.crypticplank.smp.Config.ConfigFields;
import org.crypticplank.smp.Smp;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.crypticplank.smp.Util.ConfigHelper;

public class Motd implements Listener {
    private Smp plugin;

    /**
     * Custom MOTDs from a file
     * @param plugin
     */
    public Motd(Smp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(ServerListPingEvent event) throws FileNotFoundException {
        if (!(Boolean)ConfigHelper.get(ConfigFields.MOTD_ENABLED))
            return;
        File filemotd = new File(this.plugin.getDataFolder(), "motd.txt");
        if (!filemotd.exists())
            try {
                filemotd.createNewFile();
                FileWriter filewrite = new FileWriter(filemotd);
                filewrite.write(String.format("SMP Legends plugin by %scrypticplank%s", ChatColor.AQUA, ChatColor.RESET));
                filewrite.close();
            } catch (IOException e1) {
                this.plugin.getLogger().severe("MOTD's data file could not be created!");
                e1.printStackTrace();
            }
        List<String> lines = Arrays.asList((new Scanner(filemotd))
                .useDelimiter("\\Z").next().split("[\\r\\n]+"));
        List<String> systemmotdlist = lines;
        Random random = new Random();
        String motd = systemmotdlist.get(random.nextInt(systemmotdlist.size()));
        motd = motd.replaceAll("&", "");
        motd = motd.replaceAll("%VERSION%", Bukkit.getServer().getBukkitVersion());
        event.setMotd(motd);
    }
}
