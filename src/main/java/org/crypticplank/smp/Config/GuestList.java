package org.crypticplank.smp.Config;

import org.bukkit.plugin.Plugin;
import org.crypticplank.smp.Smp;

public class GuestList extends Config {
    private Plugin plugin;
    public GuestList(Plugin plugin) {
        super(Smp.getPlugin(Smp.class), "guest_mapping");
        this.plugin = plugin;
    }
}
