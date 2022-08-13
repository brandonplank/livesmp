package org.crypticplank.smp.Config;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class Config {
    private Plugin plugin;
    private final String name;
    private File file;
    public YamlConfiguration config;

    public Config(Plugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        file = new File(plugin.getDataFolder(), name + ".yml");
        if(!file.exists()) {
            try {
                file.getParentFile().mkdirs();
            } catch (Exception e) {
                plugin.getLogger().warning("Could not save config file to disk");
                e.printStackTrace();
            }
            plugin.saveResource(name + ".yml", false);
        }
        config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().warning(ChatColor.RED + "Couldn't open config file " + name);
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning(ChatColor.RED + "Couldn't save config file " + name);
            e.printStackTrace();
        }
    }
}
