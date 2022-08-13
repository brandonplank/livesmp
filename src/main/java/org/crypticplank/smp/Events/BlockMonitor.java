package org.crypticplank.smp.Events;

import com.comphenix.protocol.wrappers.BlockPosition;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.crypticplank.smp.Config.ConfigFields;
import org.crypticplank.smp.Util.ConfigHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class BlockMonitor implements Listener {

    final Plugin plugin;
    HashMap<UUID, String> michaelJackson = new HashMap<>();

    public BlockMonitor(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        if(placedBlock.getType() == Material.SCULK_SHRIEKER && (Boolean)ConfigHelper.get(ConfigFields.UTIL_PLAYER_PLACED_SHRIEKER)) {
            SculkShrieker shrieker = (SculkShrieker)placedBlock.getBlockData();
            shrieker.setCanSummon(true);
            placedBlock.setBlockData(shrieker);
        }
    }

    boolean isWitherRoseCloseToFeet(Location feet) {
        /*
        0 0 0
        0 1 0
        0 0 0
         */
        for(int x = feet.getBlockX() - 1; x < feet.getBlockX() + 3; x++) {
            for(int z = feet.getBlockZ() - 1; z < feet.getBlockZ() + 3; z++) {
                Location check = new Location(feet.getWorld(), x, feet.getBlockY(), z);
                if(check.getBlock().getType() == Material.WITHER_ROSE)
                    return true;
            }
        }
        return false;
    }

    // The micheal jackson experience
    @EventHandler
    void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            Location feet = player.getLocation();
            if(event.getCause() == EntityDamageEvent.DamageCause.WITHER && isWitherRoseCloseToFeet(feet)  && (Boolean)ConfigHelper.get(ConfigFields.UTIL_MICHAEL_JACKSON_EXPERIENCE_ENABLED)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    int xMax = feet.getBlockX() + 20;
                    int xMin = feet.getBlockX() - 20;
                    int zMax = feet.getBlockZ() + 20;
                    int zMin = feet.getBlockZ() - 20;
                    int x = new Random().nextInt(xMax - xMin) + xMin;
                    int z = new Random().nextInt(zMax - zMin) + zMin;
                    player.teleport(new Location(feet.getWorld(), x, 32, z));
                    ArrayList<String> messages = (ArrayList<String>)ConfigHelper.get(ConfigFields.UTIL_MICHAEL_JACKSON_EXPERIENCE_MESSAGES);
                    Random random = new Random();
                    String message = messages.get(random.nextInt(messages.size()));
                    plugin.getServer().broadcast(Component.text(String.format(message, player.getName())));
                    michaelJackson.put(player.getUniqueId(), player.getName());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        UUID playerUUID = player.getUniqueId(); // Stores inside the task
                        michaelJackson.remove(playerUUID);
                    }, 20L * 45L); // ~ 45 seconds
                });
            }
        }
    }

    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if(michaelJackson.containsKey(player.getUniqueId())) {
            ArrayList<String> messages = (ArrayList<String>)ConfigHelper.get(ConfigFields.UTIL_MICHAEL_JACKSON_EXPERIENCE_DEATH_MESSAGES);
            Random random = new Random();
            String message = messages.get(random.nextInt(messages.size()));
            event.deathMessage(Component.text(String.format(message, player.getName())));
            michaelJackson.remove(player.getUniqueId());
        }
    }

    // Cryptofyres request for being able to collect lava using dispenser fron LAVA_CAULDRON
    @EventHandler
    void onDispenser(BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        if(item.getType() != Material.BUCKET) {
            return;
        }
        org.bukkit.block.data.type.Dispenser dispenserData = (org.bukkit.block.data.type.Dispenser) event.getBlock().getState().getBlockData();
        Block block = event.getBlock().getRelative(dispenserData.getFacing());
        if(block.getType() == Material.LAVA_CAULDRON) {
            block.setType(Material.CAULDRON);
            Dispenser dispenser = (Dispenser)event.getBlock().getState();
            ItemStack []items = dispenser.getInventory().getStorageContents();

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                int itemcount = 0;
                for (int i = 0; i < 9; i++) {
                    if (items[i] != null) {
                        itemcount+=items[i].getAmount();
                    }
                }
                if (itemcount == 1) {
                    dispenser.getInventory().clear();
                }
                int slot = dispenser.getInventory().first(Material.BUCKET);
                ItemStack temp = dispenser.getInventory().getItem(slot);
                assert temp != null;
                temp.setAmount(temp.getAmount() - 1);
                dispenser.getInventory().setItem(slot, temp);
                dispenser.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));
            }, 1);

            dispenser.update(true);
            event.setCancelled(true);
        }
    }
}
