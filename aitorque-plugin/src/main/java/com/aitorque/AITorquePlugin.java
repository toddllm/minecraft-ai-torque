package com.aitorque;

import com.aitorque.entity.AITorqueEntity;
import com.aitorque.listeners.EntityListener;
import com.aitorque.listeners.PlayerListener;
import com.aitorque.phases.PhaseManager;
import com.aitorque.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * AI Torque Plugin - OMEGA Classification Mega Boss
 *
 * Main plugin class that manages the AI Torque entity, phases, and all systems.
 *
 * @author AITorque Development Team
 * @version 1.0.0-ALPHA
 */
public class AITorquePlugin extends JavaPlugin {

    private static AITorquePlugin instance;
    private ConfigManager configManager;
    private Map<UUID, AITorqueEntity> activeTorques;

    @Override
    public void onEnable() {
        instance = this;
        activeTorques = new HashMap<>();

        // Initialize configuration
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // Register events
        registerEvents();

        // Register custom entity
        registerCustomEntity();

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║   AI TORQUE - OMEGA BOSS ACTIVATED    ║");
        getLogger().info("║   Version: " + getDescription().getVersion() + "                   ║");
        getLogger().info("╚════════════════════════════════════════╝");

        // Spawn on start if configured
        if (getConfig().getBoolean("spawn.spawn-on-server-start", false)) {
            Bukkit.getScheduler().runTaskLater(this, this::spawnInitialTorque, 100L);
        }
    }

    @Override
    public void onDisable() {
        // Clean up all active AI Torques
        for (AITorqueEntity torque : activeTorques.values()) {
            torque.cleanup();
        }
        activeTorques.clear();

        getLogger().info("AI Torque has been deactivated.");
    }

    /**
     * Register event listeners
     */
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    /**
     * Register custom entity type
     */
    private void registerCustomEntity() {
        // This will be implemented with NMS (version-specific)
        // For now, we'll use a simpler approach with existing entity types
        getLogger().info("Custom entity registration initialized.");
    }

    /**
     * Spawn initial AI Torque
     */
    private void spawnInitialTorque() {
        // Find a suitable spawn location (first player spawn or world spawn)
        org.bukkit.World world = Bukkit.getWorlds().get(0);
        org.bukkit.Location spawnLoc = world.getSpawnLocation().clone();

        // Spawn deep underground at configured depth
        int spawnDepth = getConfig().getInt("spawn.spawn-depth", -60);
        spawnLoc.setY(spawnDepth);

        // Check max instances
        int maxInstances = getConfig().getInt("spawn.max-instances", 1);
        if (activeTorques.size() >= maxInstances) {
            getLogger().info("Maximum AI Torque instances already exist");
            return;
        }

        getLogger().info("AI Torque awakening...");

        // Spawn AI Torque underground
        AITorqueEntity torque = new AITorqueEntity(this, spawnLoc);
        activeTorques.put(torque.getUniqueId(), torque);

        // Start tunneling up if configured
        if (getConfig().getBoolean("spawn.tunnel-up", true)) {
            startTunnelingUp(torque);
        }
    }

    /**
     * Spawn AI Torque under a specific player
     */
    public void spawnTorqueUnderPlayer(org.bukkit.entity.Player player) {
        org.bukkit.Location spawnLoc = player.getLocation().clone();

        // Spawn deep underground at configured depth
        int spawnDepth = getConfig().getInt("spawn.spawn-depth", -60);
        spawnLoc.setY(spawnDepth);

        // Check max instances
        int maxInstances = getConfig().getInt("spawn.max-instances", 1);
        if (activeTorques.size() >= maxInstances) {
            getLogger().info("Maximum AI Torque instances already exist");
            return;
        }

        getLogger().info("AI Torque spawning beneath player");

        // Spawn AI Torque underground
        AITorqueEntity torque = new AITorqueEntity(this, spawnLoc);
        activeTorques.put(torque.getUniqueId(), torque);

        // Start tunneling up if configured
        if (getConfig().getBoolean("spawn.tunnel-up", true)) {
            startTunnelingUp(torque);
        }
    }

    /**
     * Make AI Torque tunnel up from underground to the surface
     */
    private void startTunnelingUp(AITorqueEntity torque) {
        int tunnelSpeed = getConfig().getInt("spawn.tunnel-speed", 20);
        int ticksBetweenBlocks = Math.max(1, 20 / tunnelSpeed); // Convert blocks/second to ticks (minimum 1 tick)
        boolean showSkull = getConfig().getBoolean("spawn.show-skull-marker", true);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                org.bukkit.Location loc = torque.getLocation();

                // Check if reached surface (Y > 60)
                if (loc.getY() >= 60) {
                    torque.setTunneling(false);
                    return;
                }

                // Show skull marker particles at AI Torque's location
                if (showSkull) {
                    // Spawn large skull particles above ground to mark location
                    org.bukkit.Location surfaceLoc = loc.clone();
                    surfaceLoc.setY(loc.getWorld().getHighestBlockYAt(loc) + 2);

                    loc.getWorld().spawnParticle(
                        org.bukkit.Particle.SOUL,
                        surfaceLoc, 100, 1.0, 1.0, 1.0, 0.05
                    );
                    loc.getWorld().spawnParticle(
                        org.bukkit.Particle.LARGE_SMOKE,
                        surfaceLoc, 50, 1.5, 0.5, 1.5, 0.1
                    );
                }

                // Move up one block
                org.bukkit.Location newLoc = loc.clone().add(0, 1, 0);

                // Break blocks above (create tunnel)
                org.bukkit.block.Block blockAbove = newLoc.getBlock();
                if (!blockAbove.getType().isAir()) {
                    blockAbove.breakNaturally();

                    // Intense particle effects
                    loc.getWorld().spawnParticle(
                        org.bukkit.Particle.EXPLOSION_EMITTER,
                        newLoc, 3, 0.5, 0.5, 0.5, 0.1
                    );
                    loc.getWorld().spawnParticle(
                        org.bukkit.Particle.LAVA,
                        newLoc, 30, 0.5, 0.5, 0.5, 0.1
                    );
                    loc.getWorld().spawnParticle(
                        org.bukkit.Particle.FLAME,
                        newLoc, 50, 1.0, 1.0, 1.0, 0.2
                    );

                    // Sound effects
                    loc.getWorld().playSound(newLoc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
                    loc.getWorld().playSound(newLoc, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
                }

                // Move AI Torque up
                torque.teleport(newLoc);
            }
        }, 20L, ticksBetweenBlocks);
    }

    /**
     * Handle plugin commands
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("aitorque")) {
            return handleAITorqueCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("spawntorque")) {
            return handleSpawnCommand(sender);
        }
        return false;
    }

    /**
     * Handle /aitorque command
     */
    private boolean handleAITorqueCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aitorque.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6=== AI Torque Commands ===");
            sender.sendMessage("§e/aitorque spawn §7- Spawn AI Torque");
            sender.sendMessage("§e/aitorque remove §7- Remove all AI Torques");
            sender.sendMessage("§e/aitorque phase <id> <phase> §7- Set AI Torque phase");
            sender.sendMessage("§e/aitorque debug §7- Toggle debug mode");
            sender.sendMessage("§e/aitorque reload §7- Reload configuration");
            sender.sendMessage("§e/aitorque info §7- Show AI Torque info");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn":
                return handleSpawnCommand(sender);

            case "remove":
                return handleRemoveCommand(sender);

            case "phase":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /aitorque phase <id> <phase>");
                    return true;
                }
                return handlePhaseCommand(sender, args);

            case "debug":
                return handleDebugCommand(sender);

            case "reload":
                reloadConfig();
                configManager = new ConfigManager(this);
                sender.sendMessage("§aConfiguration reloaded!");
                return true;

            case "info":
                return handleInfoCommand(sender);

            default:
                sender.sendMessage("§cUnknown subcommand. Use /aitorque for help.");
                return true;
        }
    }

    /**
     * Handle spawn command
     */
    private boolean handleSpawnCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check max instances
        int maxInstances = getConfig().getInt("spawn.max-instances", 1);
        if (activeTorques.size() >= maxInstances) {
            sender.sendMessage("§cMaximum AI Torque instances already exist (" + maxInstances + ")");
            return true;
        }

        // Spawn AI Torque at player location
        AITorqueEntity torque = new AITorqueEntity(this, player.getLocation());
        activeTorques.put(torque.getUniqueId(), torque);

        sender.sendMessage("§4AI Torque spawned");

        return true;
    }

    /**
     * Handle remove command
     */
    private boolean handleRemoveCommand(CommandSender sender) {
        int count = activeTorques.size();

        for (AITorqueEntity torque : activeTorques.values()) {
            torque.cleanup();
        }
        activeTorques.clear();

        sender.sendMessage("§aRemoved " + count + " AI Torque instance(s).");
        return true;
    }

    /**
     * Handle phase command
     */
    private boolean handlePhaseCommand(CommandSender sender, String[] args) {
        try {
            UUID uuid = UUID.fromString(args[1]);
            int phase = Integer.parseInt(args[2]);

            AITorqueEntity torque = activeTorques.get(uuid);
            if (torque == null) {
                sender.sendMessage("§cAI Torque with ID " + uuid + " not found.");
                return true;
            }

            torque.setPhase(phase);
            sender.sendMessage("§aSet AI Torque phase to " + phase);

        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid UUID or phase number.");
        }

        return true;
    }

    /**
     * Handle debug command
     */
    private boolean handleDebugCommand(CommandSender sender) {
        boolean currentDebug = getConfig().getBoolean("debug.enabled", false);
        getConfig().set("debug.enabled", !currentDebug);
        saveConfig();

        sender.sendMessage("§aDebug mode: " + (!currentDebug ? "§aENABLED" : "§cDISABLED"));
        return true;
    }

    /**
     * Handle info command
     */
    private boolean handleInfoCommand(CommandSender sender) {
        sender.sendMessage("§6=== AI Torque Information ===");
        sender.sendMessage("§eActive Instances: §f" + activeTorques.size());
        sender.sendMessage("§eMax Instances: §f" + getConfig().getInt("spawn.max-instances"));

        for (Map.Entry<UUID, AITorqueEntity> entry : activeTorques.entrySet()) {
            AITorqueEntity torque = entry.getValue();
            sender.sendMessage("§e  ID: §f" + entry.getKey());
            sender.sendMessage("§e  Phase: §f" + torque.getCurrentPhase());
            sender.sendMessage("§e  Health: §f" + torque.getHealth() + "/" + torque.getMaxHealth());
            sender.sendMessage("§e  Form: §f" + torque.getCurrentForm());
        }

        return true;
    }

    /**
     * Register an active AI Torque
     */
    public void registerTorque(UUID uuid, AITorqueEntity torque) {
        activeTorques.put(uuid, torque);
    }

    /**
     * Unregister an AI Torque
     */
    public void unregisterTorque(UUID uuid) {
        activeTorques.remove(uuid);
    }

    /**
     * Remove an active AI Torque (alias for unregisterTorque)
     */
    public void removeActiveTorque(UUID uuid) {
        unregisterTorque(uuid);
    }

    /**
     * Get a specific active AI Torque by UUID
     */
    public AITorqueEntity getActiveTorque(UUID uuid) {
        return activeTorques.get(uuid);
    }

    /**
     * Get all active AI Torques
     */
    public Map<UUID, AITorqueEntity> getActiveTorques() {
        return activeTorques;
    }

    /**
     * Get config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get plugin instance
     */
    public static AITorquePlugin getInstance() {
        return instance;
    }
}
