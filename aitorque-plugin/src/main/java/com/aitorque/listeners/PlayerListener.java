package com.aitorque.listeners;

import com.aitorque.AITorquePlugin;
import com.aitorque.entity.AITorqueEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens for player-related events for AI Torque interactions
 */
public class PlayerListener implements Listener {

    private final AITorquePlugin plugin;
    private boolean hasSpawnedOnPlayerJoin = false;

    public PlayerListener(AITorquePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Spawn AI Torque under first player if configured
        if (!hasSpawnedOnPlayerJoin && plugin.getConfig().getBoolean("spawn.spawn-on-first-player-join", true)) {
            hasSpawnedOnPlayerJoin = true;

            Player player = event.getPlayer();

            // Wait 30 seconds to allow terrain to load and mobs to spawn naturally
            // This ensures creepers, zombies, etc. exist before AI Torque hunts them
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.spawnTorqueUnderPlayer(player);
            }, 600L); // 30 seconds (20 ticks/second)
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Check if player is too close to neutral AI Torque
        // Trigger aggro based on configuration
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Handle statue interactions (wood block placement)
        // Trigger AI Torque spawn when wood is placed in statue
    }
}
