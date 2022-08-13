package org.crypticplank.smp.Util;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.crypticplank.smp.Smp;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Update {
    private URL url;

    private final JavaPlugin plugin;

    private final String pluginUrl;

    public Update(JavaPlugin plugin, String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            plugin.getLogger().log(Level.WARNING, "Error: Bad URL while checking {0} !", plugin.getName());
        }
        this.plugin = plugin;
        this.pluginUrl = url;
    }

    public void update(CommandSender sender) {
        if(sender == null)
            sender = this.plugin.getServer().getConsoleSender();
        try {
            URL download = new URL(this.pluginUrl);
            BufferedInputStream in = null;
            FileOutputStream fout = null;
            try {
                sender.sendMessage(Component.text(String.format("Trying to download %s...", this.pluginUrl)));
                in = new BufferedInputStream(download.openStream());
                String pluginJar = getFileNameFromPath(Smp.class.getProtectionDomain().getCodeSource().getLocation().getFile());
                sender.sendMessage(Component.text(String.format("Found our JAR %s", pluginJar)));
                fout = new FileOutputStream("plugins/" + pluginJar);
                byte[] data = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1)
                    fout.write(data, 0, count);
            } finally {
                if (in != null)
                    in.close();
                if (fout != null)
                    fout.close();
            }
            sender.sendMessage(Component.text(String.format("Successfully downloaded file %s!", this.pluginUrl)));
        } catch (IOException iOException) {
            sender.sendMessage(Component.text(iOException.getMessage(), NamedTextColor.RED));
        }
    }
    private String getFileNameFromPath(String path) {
        return Paths.get(path).getFileName().toString();
    }
}