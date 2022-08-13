package org.crypticplank.smp.Config;

import org.bukkit.plugin.Plugin;
import org.crypticplank.smp.Smp;

public class TwitchNames extends Config {
    private Plugin plugin;
    public TwitchNames(Plugin plugin) {
        super(Smp.getPlugin(Smp.class), "twitch_mapping");
        this.plugin = plugin;
    }
}
