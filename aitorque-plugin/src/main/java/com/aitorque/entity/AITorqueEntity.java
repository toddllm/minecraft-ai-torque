package com.aitorque.entity;

import com.aitorque.AITorquePlugin;
import com.aitorque.abilities.*;
import com.aitorque.phases.PhaseManager;
import com.aitorque.transformations.TransformationForm;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * AI Torque Entity - OMEGA Classification Mega Boss
 *
 * The main entity class that represents AI Torque in the game.
 * Manages all abilities, phases, transformations, and behaviors.
 *
 * @author AITorque Development Team
 */
public class AITorqueEntity {

    private final AITorquePlugin plugin;
    private final UUID uniqueId;
    private LivingEntity bukkitEntity;
    private Location location;

    // Core Stats
    private double health;
    private double maxHealth;
    private double power;
    private long tickCounter;

    // Phase & Transformation
    private PhaseManager phaseManager;
    private TransformationForm currentForm;
    private boolean isHostile;

    // Ability Systems
    private InfectionAbility infectionSystem;
    private CloningAbility cloningSystem;
    private TypeSystemAbility typeSystem;
    private HealingTowerAbility healingTowerSystem;
    private StatueAbility statueSystem;

    // Abilities Data
    private final List<Entity> orbitingObjects;

    // Behavior Flags
    private boolean isDebrisOrbitActive;
    private boolean isTractorBeamEnabled;

    // Attack Systems
    private List<ArmorStand> tentacles;
    private int attackCooldown;
    private Player currentTarget;
    private boolean isMaskTeethEnabled;
    private boolean isSoulGrabbingEnabled;
    private boolean isImmortal;
    private boolean hasForcefield;
    private double forcefieldStrength;
    private boolean isTunneling;

    // God-like Systems
    private int rebirthCount;
    private boolean isRegenerating;
    private double chargeLevel;
    private boolean isReflecting;
    private Set<String> immunities;
    private List<ArmorStand> teeth;
    private List<Entity> tractorBeamTargets;
    private List<org.bukkit.block.Block> orbitingBlocks;
    private List<org.bukkit.inventory.ItemStack> heldTools;
    private Location brewingStandLoc;
    private boolean isCharging;
    private int chargeTime;

    // Avatar Head - custom player skull with glowing eyes
    private ArmorStand avatarHead;

    // ULTIMATE LIFE FORM SYSTEM
    private double lifePower = 0;           // Total absorbed life energy
    private double consumptionEnergy = 1000; // Energy that drains over time - must consume to survive!
    private double energyDrainRate = 1.0;   // Energy lost per second
    private double currentSize = 1.0;       // Size multiplier (starts at 1.0 = 1.7 blocks)
    private double maxSize = 100.0;         // Can grow to 100x size!
    private int evolutionStage = 0;         // Current evolution level
    private double speedMultiplier = 1.0;   // Movement speed boost
    private double strengthMultiplier = 1.0; // Damage boost
    private List<ArmorStand> bodyParts = new ArrayList<>(); // Split body pieces
    private boolean isSplit = false;
    private Map<EntityType, Integer> consumedMobTypes = new HashMap<>();
    private double totalConsumed = 0;       // Total mass consumed
    private int shapeshiftCooldown = 0;

    // COMPOSITE BODY PARTS - Overlord Ultimate Form
    private List<ArmorStand> krakenTentacles = new ArrayList<>(); // 8 tentacles
    private List<ArmorStand> dragonWings = new ArrayList<>();     // 2 wings
    private List<ArmorStand> masks = new ArrayList<>();           // Multiple floating masks
    private List<ArmorStand> shulkerCannons = new ArrayList<>();  // Shoulder cannons
    private ArmorStand hood;                                       // Dark overlord hood
    private String currentEyeColor = "§c"; // Red by default

    // ATTACK SYSTEMS
    private int laserEyeCooldown = 0;
    private int teethBiteCooldown = 0;
    private int fireballMaskCooldown = 0;
    private int cannonCooldown = 0;

    // ADAPTIVE FORM STATE
    private String currentFormMode = "BALANCED"; // BALANCED, OFFENSE, DEFENSE, SPEED, TANK

    // DAMAGE TRACKING - Only attack players who damaged AI Torque
    private Set<UUID> playersThatDamagedMe = new HashSet<>();

    // Timers
    private int abilityTickCounter;

    /**
     * Create a new AI Torque entity
     */
    public AITorqueEntity(AITorquePlugin plugin, Location spawnLocation) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.location = spawnLocation;

        // Initialize stats (cap at Minecraft 1.21.8's max of 1024)
        double desiredHealth = 10000 * plugin.getConfigManager().getHealthMultiplier();
        this.maxHealth = Math.min(desiredHealth, 1024.0);
        this.health = maxHealth;
        this.power = 0;
        this.tickCounter = 0;

        // Initialize collections
        this.orbitingObjects = new ArrayList<>();
        this.masks = new ArrayList<>();
        this.tentacles = new ArrayList<>();
        this.teeth = new ArrayList<>();
        this.tractorBeamTargets = new ArrayList<>();
        this.orbitingBlocks = new ArrayList<>();
        this.heldTools = new ArrayList<>();
        this.attackCooldown = 0;
        this.currentTarget = null;

        // Initialize god systems
        this.rebirthCount = Integer.MAX_VALUE; // Infinite rebirths
        this.isRegenerating = true;
        this.chargeLevel = 0;
        this.isReflecting = false;
        this.immunities = new HashSet<>();
        this.immunities.add("FIRE");
        this.immunities.add("LAVA");
        this.immunities.add("DROWNING");
        this.immunities.add("FALL");
        this.immunities.add("POISON");
        this.immunities.add("WITHER");
        this.isCharging = false;
        this.chargeTime = 0;

        // Initialize ability systems
        this.infectionSystem = new InfectionAbility(plugin, this);
        this.cloningSystem = new CloningAbility(plugin, this);
        this.typeSystem = new TypeSystemAbility(plugin, this);
        this.healingTowerSystem = new HealingTowerAbility(plugin, this);
        this.statueSystem = new StatueAbility(plugin, this);

        // Initialize behavior
        this.isHostile = !plugin.getConfigManager().isNeutralByDefault();
        this.isImmortal = plugin.getConfigManager().isImmortalityEnabled();
        this.currentForm = TransformationForm.NORMAL;
        this.hasForcefield = false;
        this.forcefieldStrength = 0.0;

        // Spawn the entity
        spawnEntity();

        // Initialize phase manager
        this.phaseManager = new PhaseManager(plugin, this);

        // Start AI tick
        startEntityTick();

        // Register with plugin
        plugin.registerTorque(uniqueId, this);
    }

    /**
     * Spawn the Bukkit entity with custom appearance
     */
    private void spawnEntity() {
        // ========================================
        // ULTIMATE LIFE FORM - SINGLE UNIFIED ENTITY
        // ========================================

        sendChatMessage("§5§l§k|||§r §4§l[AI TORQUE] §5§lULTIMATE LIFE FORM AWAKENING... §k|||");
        sendChatMessage("§d§lA BEING THAT CONSUMES ALL TO EVOLVE INFINITELY!");

        // Spawn ONE primary entity - Zombie at 1.7 blocks (player size)
        this.bukkitEntity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);

        // Configure the ULTIMATE LIFE FORM
        bukkitEntity.setCustomName("§5§l✦ §4§lAI TORQUE §5§l[ULTIMATE LIFE FORM] §5§l✦");
        bukkitEntity.setCustomNameVisible(true);
        bukkitEntity.setRemoveWhenFarAway(false);
        bukkitEntity.setPersistent(true);
        bukkitEntity.setSilent(false);
        bukkitEntity.setGlowing(true);
        bukkitEntity.setInvulnerable(false);
        bukkitEntity.setAI(true);

        // Prevent burning in sunlight
        if (bukkitEntity instanceof Zombie) {
            ((Zombie) bukkitEntity).setShouldBurnInDay(false);
        }

        // Set base health
        Objects.requireNonNull(bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH))
                .setBaseValue(maxHealth);
        bukkitEntity.setHealth(health);

        // Start with SPEED - the ultimate life form is FAST
        bukkitEntity.addPotionEffect(new PotionEffect(
            PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false
        ));

        // Apply invisibility to see equipment
        bukkitEntity.addPotionEffect(new PotionEffect(
            PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false
        ));

        // Adaptive bio-armor (Netherite)
        org.bukkit.inventory.EntityEquipment equipment = bukkitEntity.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(new org.bukkit.inventory.ItemStack(Material.NETHERITE_HELMET));
            equipment.setChestplate(new org.bukkit.inventory.ItemStack(Material.NETHERITE_CHESTPLATE));
            equipment.setLeggings(new org.bukkit.inventory.ItemStack(Material.NETHERITE_LEGGINGS));
            equipment.setBoots(new org.bukkit.inventory.ItemStack(Material.NETHERITE_BOOTS));
            equipment.setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.NETHERITE_SWORD));
        }

        // Start all unique behaviors
        spawnAvatarHead();
        startFloatingBehavior();
        startAuraEffects();
        startRealityWarp();
        spawnTentacles();
        spawnMasks();
        spawnTeeth();
        startCombatBehavior();
        startGodSystems();
        startTractorBeam();
        startRegeneration();
        startBrewingSystem();
        startEnvironmentInteraction();
        startDebrisManipulation();

        // ULTIMATE LIFE FORM SYSTEMS
        startUltimateLifeForm();
        startEnergyHunger();
        startContinuousEvolution();

        // COMPOSITE BODY PARTS - Overlord Form
        spawnCompositeParts();
        startAdaptiveFormSystem();
        startCompositeAttackSystems();

        // CAT SOUND VERBAL COMMUNICATION
        startCatSoundCommunication();

        // OBVIOUS DEVASTATING POWERS
        startObviousDevastatingPowers();

        sendChatMessage("§4§l§k|||§r §5§lPERFECT. ADAPTIVE. INFINITE. OVERLORD. §k|||");
    }

    /**
     * Unique floating behavior - entity hovers and drifts smoothly
     */
    private void startFloatingBehavior() {
        new BukkitRunnable() {
            private double floatPhase = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                floatPhase += 0.05;

                // Smooth floating motion (up and down)
                double yOffset = Math.sin(floatPhase) * 0.1;
                Location currentLoc = bukkitEntity.getLocation();
                Location newLoc = currentLoc.clone().add(0, yOffset, 0);
                bukkitEntity.teleport(newLoc);

                // Slight rotation
                newLoc.setYaw((float)(newLoc.getYaw() + 1));
                location = newLoc;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Reality warping effects - unique visual distortion
     */
    private void startRealityWarp() {
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                Location loc = bukkitEntity.getLocation().add(0, 1, 0);

                // Warping sphere effect
                if (tick % 10 == 0) {
                    for (int i = 0; i < 20; i++) {
                        double theta = Math.random() * 2 * Math.PI;
                        double phi = Math.random() * Math.PI;
                        double r = 3.0;

                        double x = r * Math.sin(phi) * Math.cos(theta);
                        double y = r * Math.sin(phi) * Math.sin(theta);
                        double z = r * Math.cos(phi);

                        Location particleLoc = loc.clone().add(x, y, z);
                        loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 1, 0, 0, 0, 0);
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Otherworldly particle aura - changes based on phase/form
     */
    private void startAuraEffects() {
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                int phase = getCurrentPhase();
                Location loc = bukkitEntity.getLocation().add(0, 1, 0);

                // Base form (Phase 1-2): Purple cosmic energy
                if (phase < 3) {
                    double radius = 2.0;
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.toRadians(i * 45 + tick * 10);
                        double height = Math.sin(tick * 0.1 + i) * 2;
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        Location spiralLoc = loc.clone().add(x, height, z);

                        loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, spiralLoc, 3, 0.05, 0.05, 0.05, 0.01);
                        loc.getWorld().spawnParticle(Particle.PORTAL, spiralLoc, 5, 0.1, 0.1, 0.1, 0.5);
                    }
                    loc.getWorld().spawnParticle(Particle.SOUL, loc, 8, 2.0, 2.0, 2.0, 0.02);
                }
                // Void Form (Phase 3-5): Dark void energy with lightning
                else if (phase < 6) {
                    double radius = 2.5;
                    for (int i = 0; i < 12; i++) {
                        double angle = Math.toRadians(i * 30 + tick * 15);
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        Location voidLoc = loc.clone().add(x, 0, z);

                        voidLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, voidLoc, 5, 0.1, 0.5, 0.1, 0.05);
                        voidLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, voidLoc, 8, 0.2, 0.2, 0.2, 0.1);

                        if (tick % 30 == 0) {
                            voidLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, voidLoc, 10, 0.3, 0.3, 0.3, 0.1);
                        }
                    }
                    loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 15, 1.0, 1.0, 1.0, 0.05);
                }
                // Chaos Form (Phase 6-10): Red chaos energy, fire
                else if (phase < 11) {
                    double radius = 3.0;
                    for (int i = 0; i < 16; i++) {
                        double angle = Math.toRadians(i * 22.5 + tick * 20);
                        double height = Math.sin(tick * 0.15 + i) * 3;
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        Location chaosLoc = loc.clone().add(x, height, z);

                        chaosLoc.getWorld().spawnParticle(Particle.FLAME, chaosLoc, 8, 0.2, 0.2, 0.2, 0.05);
                        chaosLoc.getWorld().spawnParticle(Particle.LAVA, chaosLoc, 3, 0.1, 0.1, 0.1, 0);
                        chaosLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, chaosLoc, 5, 0.1, 0.1, 0.1, 0.05);
                    }

                    if (tick % 10 == 0) {
                        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5, 1.5, 1.5, 1.5, 0);
                    }
                }
                // Reality Warp Form (Phase 11+): Warping reality, end crystals, wardens
                else {
                    double radius = 4.0;
                    for (int i = 0; i < 20; i++) {
                        double angle = Math.toRadians(i * 18 + tick * 25);
                        double height = Math.cos(tick * 0.2 + i) * 4;
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        Location warpLoc = loc.clone().add(x, height, z);

                        warpLoc.getWorld().spawnParticle(Particle.END_ROD, warpLoc, 3, 0, 0, 0, 0.1);
                        warpLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, warpLoc, 10, 0.2, 0.2, 0.2, 0.2);
                        warpLoc.getWorld().spawnParticle(Particle.SONIC_BOOM, warpLoc, 1, 0, 0, 0, 0);
                    }

                    // Reality distortion sphere
                    if (tick % 5 == 0) {
                        for (int i = 0; i < 15; i++) {
                            double theta = Math.random() * 2 * Math.PI;
                            double phi = Math.random() * Math.PI;
                            double r = 5.0;
                            double x = r * Math.sin(phi) * Math.cos(theta);
                            double y = r * Math.sin(phi) * Math.sin(theta);
                            double z = r * Math.cos(phi);
                            Location sphereLoc = loc.clone().add(x, y, z);
                            loc.getWorld().spawnParticle(Particle.SCULK_CHARGE, sphereLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }

                // Pulsing core (all phases)
                if (tick % 20 == 0) {
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0, 0, 0, 0.3);
                    loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 50, 1.5, 1.5, 1.5, 0.05);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /**
     * Spawn tentacles that grab and pull entities
     */
    private void spawnTentacles() {
        int tentacleCount = 6;
        for (int i = 0; i < tentacleCount; i++) {
            ArmorStand tentacle = location.getWorld().spawn(location, ArmorStand.class);
            tentacle.setInvisible(true);
            tentacle.setMarker(true);
            tentacle.setGravity(false);
            tentacle.setInvulnerable(true);
            tentacles.add(tentacle);
        }

        // Animate tentacles
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                for (int i = 0; i < tentacles.size(); i++) {
                    ArmorStand tentacle = tentacles.get(i);
                    if (tentacle == null || !tentacle.isValid()) continue;

                    double angle = Math.toRadians(i * 60 + tick * 3);
                    double distance = 3.0 + Math.sin(tick * 0.05 + i) * 1.5;
                    double height = Math.sin(tick * 0.08 + i) * 2;

                    double x = distance * Math.cos(angle);
                    double z = distance * Math.sin(angle);

                    Location tentacleLoc = bukkitEntity.getLocation().add(x, height, z);
                    tentacle.teleport(tentacleLoc);

                    // Tentacle particle trail
                    drawTentacleLine(bukkitEntity.getLocation().add(0, 1, 0), tentacleLoc);

                    // Grab nearby entities with tentacles
                    if (tick % 40 == i * 7) {
                        grabWithTentacle(tentacleLoc);
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Draw particle line for tentacle
     */
    private void drawTentacleLine(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = start.distance(end);
        int particles = (int)(distance * 5);

        for (int i = 0; i < particles; i++) {
            double ratio = (double) i / particles;
            Location point = start.clone().add(direction.clone().multiply(ratio));
            point.getWorld().spawnParticle(Particle.SCULK_SOUL, point, 1, 0, 0, 0, 0);
            point.getWorld().spawnParticle(Particle.SOUL, point, 1, 0.05, 0.05, 0.05, 0);
        }
    }

    /**
     * Grab entity with tentacle
     */
    private void grabWithTentacle(Location tentacleEnd) {
        Collection<Entity> nearby = tentacleEnd.getWorld().getNearbyEntities(tentacleEnd, 2, 2, 2,
                e -> e instanceof LivingEntity && !(e.equals(bukkitEntity)));

        for (Entity entity : nearby) {
            Vector pull = bukkitEntity.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
            entity.setVelocity(pull.multiply(0.8));

            // Tentacle grab effect
            entity.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, entity.getLocation(), 20, 0.5, 0.5, 0.5, 0.05);
            entity.getWorld().spawnParticle(Particle.SCULK_SOUL, entity.getLocation(), 10, 0.3, 0.3, 0.3, 0.1);
        }
    }

    /**
     * Combat behavior - MASSIVE arsenal of attacks
     */
    private void startCombatBehavior() {
        new BukkitRunnable() {
            private int specialAttackCounter = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Decrease attack cooldown
                if (attackCooldown > 0) attackCooldown--;
                specialAttackCounter++;

                // ALWAYS ACTIVE SYSTEMS

                // Block consumption every 2 seconds
                if (tickCounter % 40 == 0) {
                    consumeNearbyBlocks();
                }

                // HP drain every second
                if (tickCounter % 20 == 0) {
                    drainNearbyHealth();
                }

                // Treasure drops every minute (1200 ticks)
                if (tickCounter % 1200 == 0) {
                    dropTreasures();
                }

                // Gravitational pull - runs every tick for smooth pulling
                gravitationalPull();

                // Always attack hostile mobs
                if (tickCounter % 40 == 0) { // Attack mobs every 2 seconds
                    attackNearbyMobs();
                }

                // Find nearest player
                Player nearest = findNearestPlayer();
                if (nearest != null) {
                    currentTarget = nearest;
                    double distance = nearest.getLocation().distance(bukkitEntity.getLocation());
                    int phase = getCurrentPhase();

                    // PASSIVE ATTACKS (work even when not hostile)
                    // Bone attacks from teeth (every 5 seconds when close)
                    if (distance < 10 && tickCounter % 100 == 0) {
                        boneAttack(nearest);
                    }

                    // Sword slash attacks (every 8 seconds at medium range)
                    if (distance < 15 && tickCounter % 160 == 0) {
                        performVoidSlash(nearest);
                    }

                    // Zikes Wrath (every 15 seconds)
                    if (tickCounter % 300 == 0) {
                        zikesWrath(nearest);
                    }

                    // Only full arsenal if hostile
                    if (isHostile) {
                        // Teleport to player every 10 seconds (only when hostile)
                        if (tickCounter % 200 == 0) {
                            teleportToPlayer(nearest);
                        }

                        // SPECIAL ATTACKS (cycle through different abilities)
                        if (specialAttackCounter % 60 == 0) { // Every 3 seconds
                        int attack = (specialAttackCounter / 60) % 60; // MASSIVE EXPANSION - 60 attacks!
                        switch (attack) {
                            // Original attacks
                            case 0: laserBeam(nearest); break;
                            case 1: meteorShower(nearest); break;
                            case 2: blackHole(nearest); break;
                            case 3: lightningStorm(); break;
                            case 4: voidRift(nearest); break;
                            case 5: soulSwarm(nearest); break;
                            case 6: groundSlam(); break;
                            case 7: voidChains(nearest); break;
                            case 8: timeSlow(); break;
                            case 9: gravityReversal(); break;
                            // NEW POWERS
                            case 10: meteorStorm(); break;
                            case 11: createBlackHole(nearest.getLocation()); break;
                            case 12: timeFreezeNearby(); break;
                            case 13: stealSouls(); break;
                            case 14: fireNova(); break;
                            case 15: iceAge(); break;
                            case 16: voidRift(); break;
                            case 17: cosmicBeam(nearest); break;
                            case 18: summonMinions(); break;
                            case 19: earthquakeAttack(); break;
                            // Additional old attacks
                            case 20: voidBomb(nearest); break;
                            case 21: soulDrain(nearest); break;
                            case 22: chaosBurst(); break;
                            case 23: voidSpikes(nearest); break;
                            case 24: cosmicStorm(); break;
                            case 25: dimensionalShift(); break;
                            case 26: shadowClones(); break;
                            case 27: realityTear(); break;
                            case 28: voidNova(); break;
                            case 29: apocalypseRain(); break;
                            // EVEN MORE DEVASTATING POWERS (30-59)
                            case 30: plasmaStorm(); break;
                            case 31: dimensionalRift(); break;
                            case 32: quantumDestabilization(); break;
                            case 33: nuclearFission(); break;
                            case 34: antimatterExplosion(); break;
                            case 35: celestialJudgment(); break;
                            case 36: singularityCollapse(); break;
                            case 37: temporalParadox(); break;
                            case 38: realityBreak(); break;
                            case 39: entropyWave(); break;
                            case 40: darkMatterPulse(); break;
                            case 41: supernovaBlast(); break;
                            case 42: quasarBeam(); break;
                            case 43: galaxyCollision(); break;
                            case 44: universalHeat(); break;
                            case 45: absoluteZero(); break;
                            case 46: neutronStarCrush(); break;
                            case 47: photonTorrent(); break;
                            case 48: gammaRayBurst(); break;
                            case 49: tesseractPrison(); break;
                            case 50: infinitySphere(); break;
                            case 51: chaosTheory(); break;
                            case 52: existentialDread(); break;
                            case 53: oblivionVoid(); break;
                            case 54: cosmicHorror(); break;
                            case 55: voidLordSummon(); break;
                            case 56: planetaryCrusher(); break;
                            case 57: stellarCollapse(); break;
                            case 58: bigBangRecreation(); break;
                            case 59: omniversalWrath(); break;
                        }
                    }

                    // PRIMARY ATTACKS (based on distance)
                    if (attackCooldown <= 0) {
                        if (distance < 5) {
                            // Close range: Random between 3 attacks
                            int attack = (int)(Math.random() * 3);
                            switch (attack) {
                                case 0: performVoidSlash(nearest); break;
                                case 1: groundPound(); break;
                                case 2: voidGrasp(nearest); break;
                            }
                            attackCooldown = 30;
                        } else if (distance < 15) {
                            // Medium range: Random between 4 attacks
                            int attack = (int)(Math.random() * 4);
                            switch (attack) {
                                case 0: performJumpAttack(nearest); break;
                                case 1: spiralBlades(nearest); break;
                                case 2: voidWave(nearest); break;
                                case 3: teleportStrike(nearest); break;
                            }
                            attackCooldown = 40;
                        } else if (distance < 30) {
                            // Long range: Random between 5 attacks
                            int attack = (int)(Math.random() * 5);
                            switch (attack) {
                                case 0: launchVoidOrb(nearest); break;
                                case 1: homingMissiles(nearest); break;
                                case 2: voidBeam(nearest); break;
                                case 3: arcaneBarrage(nearest); break;
                                case 4: meteorStrike(nearest); break;
                            }
                            attackCooldown = 25;
                        }

                        // Phase-specific ultimate attacks
                        if (phase >= 5 && tickCounter % 400 == 0) {
                            cataclysm();
                        }
                        if (phase >= 10 && tickCounter % 600 == 0) {
                            worldEnder();
                        }
                    }
                    } // End of isHostile check
                }
            }
        }.runTaskTimer(plugin, 20L, 5L);
    }

    /**
     * Find nearest player
     */
    private Player findNearestPlayer() {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;

            double dist = player.getLocation().distance(location);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        return nearest;
    }

    /**
     * Attack nearby entities
     * When hostile (red eyes): Attack ALL living entities
     * When passive (black eyes): Only attack monsters
     */
    private void attackNearbyMobs() {
        Collection<Entity> nearbyEntities = bukkitEntity.getWorld().getNearbyEntities(
            bukkitEntity.getLocation(), 30, 30, 30
        );

        for (Entity entity : nearbyEntities) {
            // ONLY attack players who have damaged AI Torque
            if (entity instanceof Player) {
                Player player = (Player) entity;

                // Check if this player has damaged AI Torque
                if (!playersThatDamagedMe.contains(player.getUniqueId())) {
                    continue; // Skip this player - they haven't attacked us
                }

                double distance = player.getLocation().distance(bukkitEntity.getLocation());

                if (distance < 20) {
                    // Devastating attacks with CHANCE DAMAGE (50% full damage, 50% only 1 heart)
                    int attack = (int)(Math.random() * 5);

                    switch (attack) {
                        case 0:
                            player.damage(calculateChanceDamage(15.0), bukkitEntity);
                            break;
                        case 1:
                            performVoidSlashOnMob(player);
                            break;
                        case 2:
                            // Launch target upward
                            player.setVelocity(new Vector(0, 2.0, 0));
                            player.damage(calculateChanceDamage(10.0), bukkitEntity);
                            break;
                        case 3:
                            // Explosion attack
                            player.getWorld().createExplosion(player.getLocation(), 2.0f, false, false);
                            player.damage(calculateChanceDamage(12.0), bukkitEntity);
                            break;
                        case 4:
                            // Fire attack
                            player.setFireTicks(100);
                            player.damage(calculateChanceDamage(8.0), bukkitEntity);
                            break;
                    }
                    break; // Attack one entity per cycle
                }
            } else if (entity instanceof org.bukkit.entity.Monster && !entity.equals(bukkitEntity)) {
                // Always attack monsters (regardless of hostile state)
                LivingEntity mob = (LivingEntity) entity;
                double distance = mob.getLocation().distance(bukkitEntity.getLocation());

                if (distance < 15) {
                    // Use attacks on mobs with chance damage
                    int attack = (int)(Math.random() * 3);

                    switch (attack) {
                        case 0:
                            mob.damage(calculateChanceDamage(8.0), bukkitEntity);
                            break;
                        case 1:
                            performVoidSlashOnMob(mob);
                            break;
                        case 2:
                            mob.setVelocity(new Vector(0, 1.0, 0));
                            mob.damage(calculateChanceDamage(5.0), bukkitEntity);
                            break;
                    }
                    break; // Only attack one mob per tick cycle
                }
            }
        }
    }

    /**
     * Void slash attack on mobs
     */
    private void performVoidSlashOnMob(LivingEntity target) {
        target.damage(10.0, bukkitEntity);
        target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation(), 5, 1, 1, 1, 0);
        target.getWorld().spawnParticle(Particle.DRAGON_BREATH, target.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
    }

    /**
     * Teleport to player with effects
     */
    private void teleportToPlayer(Player target) {
        // Old location effects
        location.getWorld().spawnParticle(Particle.PORTAL, location, 150, 1, 1, 1, 2);
        location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 100, 1, 1, 1, 1);
        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);

        // Teleport behind player
        Vector direction = target.getLocation().getDirection().multiply(-3);
        Location targetLoc = target.getLocation().add(direction).add(0, 1, 0);

        bukkitEntity.teleport(targetLoc);
        location = targetLoc;

        // New location effects
        location.getWorld().spawnParticle(Particle.PORTAL, location, 150, 1, 1, 1, 2);
        location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, location, 5, 0, 0, 0, 0);
        location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 50, 1, 1, 1, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.0f, 2.0f);
    }

    /**
     * Void Slash attack - melee slash with particle effect
     */
    private void performVoidSlash(Player target) {
        Location start = bukkitEntity.getLocation().add(0, 1, 0);
        Vector direction = target.getLocation().toVector().subtract(start.toVector()).normalize();

        // Create slash arc
        for (double d = 0; d < 5; d += 0.3) {
            Location slashPoint = start.clone().add(direction.clone().multiply(d));

            // Slash particles (wide arc)
            for (int angle = -45; angle <= 45; angle += 15) {
                Vector rotated = rotateAroundY(direction.clone(), Math.toRadians(angle));
                Location particleLoc = slashPoint.clone().add(rotated);

                slashPoint.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 0);
                slashPoint.getWorld().spawnParticle(Particle.SCULK_SOUL, particleLoc, 3, 0.1, 0.1, 0.1, 0.05);
                slashPoint.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLoc, 1, 0, 0, 0, 0);
            }
        }

        // Sound effect
        location.getWorld().playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.5f);
        location.getWorld().playSound(location, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.5f);

        // Damage nearby entities
        Collection<Entity> nearby = start.getWorld().getNearbyEntities(start, 5, 3, 5,
                e -> e instanceof LivingEntity && !e.equals(bukkitEntity));

        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(8.0, bukkitEntity);
                living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
            }
        }
    }

    /**
     * Jump attack - leap at target
     */
    private void performJumpAttack(Player target) {
        Vector direction = target.getLocation().toVector().subtract(bukkitEntity.getLocation().toVector()).normalize();
        direction.setY(0.8); // Upward component

        bukkitEntity.setVelocity(direction.multiply(1.5));

        // Jump particle effects
        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 10, 0.5, 0.1, 0.5, 0);
        location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 30, 0.5, 0.1, 0.5, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.8f);

        // Trail particles while jumping
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead() || ticks++ > 20) {
                    cancel();
                    return;
                }

                Location loc = bukkitEntity.getLocation();
                loc.getWorld().spawnParticle(Particle.SOUL, loc, 5, 0.3, 0.3, 0.3, 0.05);
                loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc, 10, 0.5, 0.5, 0.5, 0.1);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Launch void orb projectile
     */
    private void launchVoidOrb(Player target) {
        Location start = bukkitEntity.getLocation().add(0, 2, 0);
        Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(start.toVector()).normalize();

        // Spawn visual orb (using armor stand or particles)
        new BukkitRunnable() {
            Location orbLoc = start.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 100) {
                    cancel();
                    return;
                }

                // Move orb
                orbLoc.add(direction.clone().multiply(0.5));

                // Orb particles
                orbLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, orbLoc, 15, 0.2, 0.2, 0.2, 0.01);
                orbLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, orbLoc, 20, 0.3, 0.3, 0.3, 0.05);
                orbLoc.getWorld().spawnParticle(Particle.END_ROD, orbLoc, 5, 0.1, 0.1, 0.1, 0.05);

                // Check for hit
                Collection<Entity> hit = orbLoc.getWorld().getNearbyEntities(orbLoc, 1.5, 1.5, 1.5,
                        e -> e instanceof LivingEntity && !e.equals(bukkitEntity));

                if (!hit.isEmpty() || orbLoc.getBlock().getType().isSolid()) {
                    // Explosion
                    orbLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, orbLoc, 3, 0, 0, 0, 0);
                    orbLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, orbLoc, 50, 1, 1, 1, 0.1);
                    orbLoc.getWorld().playSound(orbLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.2f);

                    // Damage
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(6.0, bukkitEntity);
                            Vector knockback = entity.getLocation().toVector().subtract(orbLoc.toVector()).normalize().multiply(1.2);
                            knockback.setY(0.5);
                            entity.setVelocity(knockback);
                        }
                    }

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Launch sound
        location.getWorld().playSound(location, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.6f);
    }

    /**
     * Rotate vector around Y axis
     */
    private Vector rotateAroundY(Vector vec, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vec.getX() * cos - vec.getZ() * sin;
        double z = vec.getX() * sin + vec.getZ() * cos;
        return new Vector(x, vec.getY(), z);
    }

    // ========================================
    // MASSIVE ATTACK ARSENAL
    // ========================================

    /** Laser Beam - Sweeping energy beam */
    private void laserBeam(Player target) {
        Location start = bukkitEntity.getLocation().add(0, 1.5, 0);
        Vector direction = target.getLocation().toVector().subtract(start.toVector()).normalize();

        // Sweeping laser beam
        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;

            @Override
            public void run() {
                if (ticks++ > 40 || bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Sweep the laser
                angle += Math.toRadians(9);
                Vector rotated = rotateAroundY(direction.clone(), angle);

                for (double d = 0; d < 30; d += 0.5) {
                    Location beamLoc = start.clone().add(rotated.clone().multiply(d));
                    beamLoc.getWorld().spawnParticle(Particle.END_ROD, beamLoc, 3, 0, 0, 0, 0);
                    beamLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, beamLoc, 2, 0.1, 0.1, 0.1, 0);

                    // Damage entities
                    Collection<Entity> hit = beamLoc.getWorld().getNearbyEntities(beamLoc, 0.5, 0.5, 0.5);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(3.0, bukkitEntity);
                        }
                    }
                }

                if (ticks % 5 == 0) {
                    start.getWorld().playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 2.0f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Meteor Shower - Rains meteors from sky */
    private void meteorShower(Player target) {
        Location center = target.getLocation();

        for (int i = 0; i < 15; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location meteorStart = center.clone().add(
                        (Math.random() - 0.5) * 30,
                        50,
                        (Math.random() - 0.5) * 30
                );

                new BukkitRunnable() {
                    Location meteorLoc = meteorStart.clone();
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ > 60 || meteorLoc.getY() <= meteorLoc.getWorld().getHighestBlockYAt(meteorLoc)) {
                            // Impact
                            meteorLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, meteorLoc, 5, 0, 0, 0, 0);
                            meteorLoc.getWorld().spawnParticle(Particle.LAVA, meteorLoc, 100, 3, 1, 3, 0);
                            meteorLoc.getWorld().playSound(meteorLoc, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);

                            Collection<Entity> hit = meteorLoc.getWorld().getNearbyEntities(meteorLoc, 4, 4, 4);
                            for (Entity entity : hit) {
                                if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                                    ((LivingEntity) entity).damage(10.0, bukkitEntity);
                                    entity.setFireTicks(100);
                                }
                            }
                            cancel();
                            return;
                        }

                        meteorLoc.add(0, -1, 0);
                        meteorLoc.getWorld().spawnParticle(Particle.FLAME, meteorLoc, 30, 0.5, 0.5, 0.5, 0.1);
                        meteorLoc.getWorld().spawnParticle(Particle.LAVA, meteorLoc, 5, 0.3, 0.3, 0.3, 0);
                        meteorLoc.getWorld().spawnParticle(Particle.SMOKE, meteorLoc, 20, 0.5, 0.5, 0.5, 0.05);
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, i * 10L);
        }
    }

    /** Black Hole - Pulls everything in and crushes */
    private void blackHole(Player target) {
        Location blackHoleLoc = target.getLocation().add(0, 15, 0);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 100) {
                    // Final explosion
                    blackHoleLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, blackHoleLoc, 20, 0, 0, 0, 0);
                    blackHoleLoc.getWorld().playSound(blackHoleLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 0.5f);
                    cancel();
                    return;
                }

                // Visual effect
                for (int i = 0; i < 30; i++) {
                    double theta = Math.random() * 2 * Math.PI;
                    double phi = Math.random() * Math.PI;
                    double r = 8.0 - (ticks * 0.05);
                    double x = r * Math.sin(phi) * Math.cos(theta);
                    double y = r * Math.sin(phi) * Math.sin(theta);
                    double z = r * Math.cos(phi);
                    Location particleLoc = blackHoleLoc.clone().add(x, y, z);
                    blackHoleLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 1, 0, 0, 0, 0.5);
                }

                // Pull entities
                Collection<Entity> nearby = blackHoleLoc.getWorld().getNearbyEntities(blackHoleLoc, 20, 20, 20);
                for (Entity entity : nearby) {
                    if (!entity.equals(bukkitEntity)) {
                        Vector pull = blackHoleLoc.toVector().subtract(entity.getLocation().toVector()).normalize();
                        entity.setVelocity(pull.multiply(0.8));

                        if (entity.getLocation().distance(blackHoleLoc) < 2 && entity instanceof LivingEntity) {
                            ((LivingEntity) entity).damage(5.0, bukkitEntity);
                        }
                    }
                }

                if (ticks % 10 == 0) {
                    blackHoleLoc.getWorld().playSound(blackHoleLoc, Sound.BLOCK_PORTAL_AMBIENT, 3.0f, 0.5f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Lightning Storm - Summons lightning strikes */
    private void lightningStorm() {
        for (int i = 0; i < 20; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location strikeLoc = bukkitEntity.getLocation().add(
                        (Math.random() - 0.5) * 40,
                        0,
                        (Math.random() - 0.5) * 40
                );
                bukkitEntity.getWorld().strikeLightning(strikeLoc);
            }, i * 5L);
        }
    }

    /** Void Rift - Opens damaging rift */
    private void voidRift(Player target) {
        Location riftLoc = target.getLocation();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 60) {
                    cancel();
                    return;
                }

                // Rift particles
                for (int i = 0; i < 20; i++) {
                    double angle = Math.toRadians(i * 18 + ticks * 15);
                    double x = 3 * Math.cos(angle);
                    double z = 3 * Math.sin(angle);
                    Location particleLoc = riftLoc.clone().add(x, ticks * 0.1, z);
                    riftLoc.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 10, 0.1, 0.1, 0.1, 1);
                    riftLoc.getWorld().spawnParticle(Particle.WITCH, particleLoc, 5, 0.1, 0.1, 0.1, 0);
                }

                // Damage nearby
                Collection<Entity> nearby = riftLoc.getWorld().getNearbyEntities(riftLoc, 4, 10, 4);
                for (Entity entity : nearby) {
                    if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                        ((LivingEntity) entity).damage(2.0, bukkitEntity);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /** Soul Swarm - Launches multiple soul projectiles */
    private void soulSwarm(Player target) {
        for (int i = 0; i < 12; i++) {
            final int index = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location start = bukkitEntity.getLocation().add(0, 2, 0);
                double angle = Math.toRadians(index * 30);
                Vector direction = new Vector(Math.cos(angle), 0.2, Math.sin(angle)).normalize();

                new BukkitRunnable() {
                    Location soulLoc = start.clone();
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ > 80) {
                            cancel();
                            return;
                        }

                        soulLoc.add(direction.clone().multiply(0.6));
                        soulLoc.getWorld().spawnParticle(Particle.SOUL, soulLoc, 10, 0.2, 0.2, 0.2, 0.02);
                        soulLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, soulLoc, 5, 0.1, 0.1, 0.1, 0.01);

                        Collection<Entity> hit = soulLoc.getWorld().getNearbyEntities(soulLoc, 1, 1, 1);
                        for (Entity entity : hit) {
                            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                                ((LivingEntity) entity).damage(4.0, bukkitEntity);
                                cancel();
                                return;
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, i * 3L);
        }
    }

    /** Ground Slam - Shockwave attack */
    private void groundSlam() {
        Location center = bukkitEntity.getLocation();

        center.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 10, 0, 0, 0, 0);
        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 0.5f);

        for (double r = 1; r <= 15; r += 0.5) {
            final double radius = r;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    double x = radius * Math.cos(rad);
                    double z = radius * Math.sin(rad);
                    Location waveLoc = center.clone().add(x, 0, z);

                    waveLoc.getWorld().spawnParticle(Particle.SONIC_BOOM, waveLoc, 1, 0, 0, 0, 0);
                    waveLoc.getWorld().spawnParticle(Particle.EXPLOSION, waveLoc, 3, 0.3, 0.3, 0.3, 0);

                    Collection<Entity> hit = waveLoc.getWorld().getNearbyEntities(waveLoc, 1, 2, 1);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(6.0, bukkitEntity);
                            Vector knockback = entity.getLocation().toVector().subtract(center.toVector()).normalize();
                            knockback.setY(0.8);
                            entity.setVelocity(knockback.multiply(2.0));
                        }
                    }
                }
            }, (long)(r * 1.5));
        }
    }

    /** Void Chains - Hold players in place */
    private void voidChains(Player target) {
        Location chainStart = bukkitEntity.getLocation().add(0, 1, 0);
        Location playerLoc = target.getLocation();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 60) {
                    cancel();
                    return;
                }

                // Draw chain
                Vector direction = playerLoc.toVector().subtract(chainStart.toVector());
                double distance = chainStart.distance(playerLoc);
                for (double d = 0; d < distance; d += 0.5) {
                    Location chainLoc = chainStart.clone().add(direction.clone().normalize().multiply(d));
                    chainLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, chainLoc, 2, 0, 0, 0, 0);
                }

                // Hold player
                target.setVelocity(new Vector(0, 0, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 10));

                if (ticks % 10 == 0) {
                    target.damage(2.0, bukkitEntity);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Time Slow - Slows all nearby entities */
    private void timeSlow() {
        Location center = bukkitEntity.getLocation();

        center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 200, 15, 15, 15, 0.5);
        center.getWorld().playSound(center, Sound.BLOCK_PORTAL_TRIGGER, 3.0f, 0.5f);

        Collection<Entity> nearby = center.getWorld().getNearbyEntities(center, 20, 20, 20);
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 3));
                living.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 200, 3));
            }
        }
    }

    /** Gravity Reversal - Reverses gravity */
    private void gravityReversal() {
        Location center = bukkitEntity.getLocation();

        Collection<Entity> nearby = center.getWorld().getNearbyEntities(center, 15, 15, 15);
        for (Entity entity : nearby) {
            if (!entity.equals(bukkitEntity)) {
                entity.setVelocity(new Vector(0, 1.5, 0));
            }
        }

        center.getWorld().spawnParticle(Particle.END_ROD, center, 100, 15, 15, 15, 0.3);
        center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 0.5f);
    }

    /** Void Bomb - Large area explosion */
    private void voidBomb(Player target) {
        Location bombLoc = target.getLocation().add(0, 5, 0);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 40) {
                    // Explode
                    bombLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, bombLoc, 30, 0, 0, 0, 0);
                    bombLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, bombLoc, 200, 5, 5, 5, 0.1);
                    bombLoc.getWorld().playSound(bombLoc, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.5f);

                    Collection<Entity> hit = bombLoc.getWorld().getNearbyEntities(bombLoc, 10, 10, 10);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(15.0, bukkitEntity);
                            Vector knockback = entity.getLocation().toVector().subtract(bombLoc.toVector()).normalize();
                            knockback.setY(0.8);
                            entity.setVelocity(knockback.multiply(3.0));
                        }
                    }

                    cancel();
                    return;
                }

                // Charging
                bombLoc.getWorld().spawnParticle(Particle.SOUL, bombLoc, 20, 1, 1, 1, 0.05);
                bombLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, bombLoc, 30, 1.5, 1.5, 1.5, 0.1);

                if (ticks % 10 == 0) {
                    bombLoc.getWorld().playSound(bombLoc, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f + (ticks * 0.05f));
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Soul Drain - Steals life */
    private void soulDrain(Player target) {
        Location start = bukkitEntity.getLocation().add(0, 1, 0);
        Location end = target.getLocation().add(0, 1, 0);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 40) {
                    cancel();
                    return;
                }

                // Draw drain beam
                Vector direction = end.toVector().subtract(start.toVector());
                double distance = start.distance(end);
                for (double d = 0; d < distance; d += 0.5) {
                    Location beamLoc = start.clone().add(direction.clone().normalize().multiply(d));
                    beamLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, beamLoc, 3, 0.1, 0.1, 0.1, 0);
                    beamLoc.getWorld().spawnParticle(Particle.SOUL, beamLoc, 2, 0.1, 0.1, 0.1, 0.02);
                }

                // Damage and heal
                target.damage(1.0, bukkitEntity);
                if (bukkitEntity.getHealth() < bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                    bukkitEntity.setHealth(Math.min(
                            bukkitEntity.getHealth() + 1.0,
                            bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
                    ));
                }

                if (ticks % 5 == 0) {
                    start.getWorld().playSound(start, Sound.ENTITY_VEX_AMBIENT, 1.5f, 0.8f);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /** Chaos Burst - Random explosions */
    private void chaosBurst() {
        Location center = bukkitEntity.getLocation();

        for (int i = 0; i < 30; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location burstLoc = center.clone().add(
                        (Math.random() - 0.5) * 20,
                        Math.random() * 10,
                        (Math.random() - 0.5) * 20
                );

                burstLoc.getWorld().spawnParticle(Particle.EXPLOSION, burstLoc, 5, 0, 0, 0, 0);
                burstLoc.getWorld().spawnParticle(Particle.FLAME, burstLoc, 50, 1, 1, 1, 0.1);
                burstLoc.getWorld().playSound(burstLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

                Collection<Entity> hit = burstLoc.getWorld().getNearbyEntities(burstLoc, 3, 3, 3);
                for (Entity entity : hit) {
                    if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                        ((LivingEntity) entity).damage(5.0, bukkitEntity);
                    }
                }
            }, i * 3L);
        }
    }

    /** Void Spikes - Ground spikes */
    private void voidSpikes(Player target) {
        Location center = target.getLocation();

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            double x = 5 * Math.cos(angle);
            double z = 5 * Math.sin(angle);
            Location spikeLoc = center.clone().add(x, 0, z);

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ > 20) {
                        cancel();
                        return;
                    }

                    for (int y = 0; y < ticks; y++) {
                        Location particleLoc = spikeLoc.clone().add(0, y * 0.5, 0);
                        spikeLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, particleLoc, 3, 0.1, 0.1, 0.1, 0);
                    }

                    Collection<Entity> hit = spikeLoc.getWorld().getNearbyEntities(spikeLoc, 1, ticks * 0.5, 1);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(3.0, bukkitEntity);
                        }
                    }
                }
            }.runTaskTimer(plugin, i * 5L, 2L);
        }
    }

    /** Cosmic Storm - Environmental hazard */
    private void cosmicStorm() {
        Location center = bukkitEntity.getLocation();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 100) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 20; i++) {
                    Location stormLoc = center.clone().add(
                            (Math.random() - 0.5) * 30,
                            Math.random() * 20,
                            (Math.random() - 0.5) * 30
                    );

                    stormLoc.getWorld().spawnParticle(Particle.END_ROD, stormLoc, 10, 0.5, 0.5, 0.5, 0.1);
                    stormLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, stormLoc, 5, 0.3, 0.3, 0.3, 0.05);

                    Collection<Entity> hit = stormLoc.getWorld().getNearbyEntities(stormLoc, 2, 2, 2);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(2.0, bukkitEntity);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    /** Dimensional Shift - Becomes invulnerable briefly */
    private void dimensionalShift() {
        bukkitEntity.setInvulnerable(true);
        bukkitEntity.getLocation().getWorld().spawnParticle(Particle.PORTAL, bukkitEntity.getLocation(), 200, 2, 2, 2, 2);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bukkitEntity.setInvulnerable(false);
        }, 60L);
    }

    /** Shadow Clones - Creates temporary clones */
    private void shadowClones() {
        for (int i = 0; i < 3; i++) {
            Location cloneLoc = bukkitEntity.getLocation().add(
                    (Math.random() - 0.5) * 10,
                    0,
                    (Math.random() - 0.5) * 10
            );

            Shulker clone = cloneLoc.getWorld().spawn(cloneLoc, Shulker.class);
            clone.setInvisible(true);
            clone.setGlowing(true);
            clone.setCustomName("§4Shadow Clone");
            clone.setCustomNameVisible(true);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                clone.getWorld().spawnParticle(Particle.SMOKE, clone.getLocation(), 50, 1, 1, 1, 0.1);
                clone.remove();
            }, 200L);
        }
    }

    /** Reality Tear - Distorts space */
    private void realityTear() {
        Location center = bukkitEntity.getLocation();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 60) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 50; i++) {
                    double theta = Math.random() * 2 * Math.PI;
                    double phi = Math.random() * Math.PI;
                    double r = 10.0;
                    double x = r * Math.sin(phi) * Math.cos(theta);
                    double y = r * Math.sin(phi) * Math.sin(theta);
                    double z = r * Math.cos(phi);
                    Location particleLoc = center.clone().add(x, y, z);
                    center.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 1, 0, 0, 0, 2);
                    center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 1, 0, 0, 0, 2);
                }

                Collection<Entity> nearby = center.getWorld().getNearbyEntities(center, 10, 10, 10);
                for (Entity entity : nearby) {
                    if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                        LivingEntity living = (LivingEntity) entity;
                        living.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 2));
                        living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /** Void Nova - Massive burst */
    private void voidNova() {
        Location center = bukkitEntity.getLocation();

        for (double r = 1; r <= 25; r += 1) {
            final double radius = r;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int angle = 0; angle < 360; angle += 5) {
                    double rad = Math.toRadians(angle);
                    double x = radius * Math.cos(rad);
                    double z = radius * Math.sin(rad);
                    Location novaLoc = center.clone().add(x, 0, z);

                    novaLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, novaLoc, 10, 0.2, 0.2, 0.2, 0.05);
                    novaLoc.getWorld().spawnParticle(Particle.END_ROD, novaLoc, 5, 0, 0, 0, 0.1);

                    Collection<Entity> hit = novaLoc.getWorld().getNearbyEntities(novaLoc, 1, 3, 1);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(8.0, bukkitEntity);
                        }
                    }
                }
            }, (long)r);
        }

        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 5.0f, 0.5f);
    }

    /** Apocalypse Rain - Ultimate attack */
    private void apocalypseRain() {
        Location center = bukkitEntity.getLocation();

        for (int i = 0; i < 50; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location rainLoc = center.clone().add(
                        (Math.random() - 0.5) * 50,
                        50,
                        (Math.random() - 0.5) * 50
                );

                new BukkitRunnable() {
                    Location dropLoc = rainLoc.clone();

                    @Override
                    public void run() {
                        if (dropLoc.getY() <= dropLoc.getWorld().getHighestBlockYAt(dropLoc)) {
                            dropLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, dropLoc, 10, 0, 0, 0, 0);
                            dropLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, dropLoc, 100, 4, 1, 4, 0.1);
                            dropLoc.getWorld().playSound(dropLoc, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
                            dropLoc.getWorld().strikeLightning(dropLoc);

                            Collection<Entity> hit = dropLoc.getWorld().getNearbyEntities(dropLoc, 5, 5, 5);
                            for (Entity entity : hit) {
                                if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                                    ((LivingEntity) entity).damage(12.0, bukkitEntity);
                                    entity.setFireTicks(200);
                                }
                            }
                            cancel();
                            return;
                        }

                        dropLoc.add(0, -2, 0);
                        dropLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, dropLoc, 50, 1, 1, 1, 0.1);
                        dropLoc.getWorld().spawnParticle(Particle.LAVA, dropLoc, 10, 0.5, 0.5, 0.5, 0);
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, i * 5L);
        }
    }

    /**
     * Bone Attack - Launches bone projectiles at target
     */
    private void boneAttack(Player target) {
        Location start = bukkitEntity.getLocation().add(0, 1, 0);

        // Launch multiple bone projectiles
        for (int i = 0; i < 5; i++) {
            final int index = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Vector direction = target.getLocation().toVector().subtract(start.toVector()).normalize();

                // Add slight spread
                direction.add(new Vector(
                    (Math.random() - 0.5) * 0.2,
                    (Math.random() - 0.5) * 0.2,
                    (Math.random() - 0.5) * 0.2
                )).normalize();

                new BukkitRunnable() {
                    Location boneLoc = start.clone();
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ > 40 || boneLoc.getBlock().getType().isSolid()) {
                            cancel();
                            return;
                        }

                        boneLoc.add(direction.clone().multiply(0.5));

                        // Bone particles (using white smoke and crit)
                        boneLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, boneLoc, 5, 0.1, 0.1, 0.1, 0);
                        boneLoc.getWorld().spawnParticle(Particle.CRIT, boneLoc, 2, 0.1, 0.1, 0.1, 0);

                        // Check for hits
                        Collection<Entity> nearby = boneLoc.getWorld().getNearbyEntities(boneLoc, 0.5, 0.5, 0.5);
                        for (Entity entity : nearby) {
                            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                                ((LivingEntity) entity).damage(5.0, bukkitEntity);
                                boneLoc.getWorld().playSound(boneLoc, Sound.ENTITY_SKELETON_HURT, 1.0f, 1.2f);
                                cancel();
                                return;
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, index * 3L);
        }

        // Sound effect
        start.getWorld().playSound(start, Sound.ENTITY_SKELETON_SHOOT, 2.0f, 0.8f);
    }

    /**
     * Zikes Wrath - Powerful purple energy blast attack
     */
    private void zikesWrath(Player target) {
        Location start = bukkitEntity.getLocation().add(0, 2, 0);

        // Charging effect
        for (int i = 0; i < 20; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                start.getWorld().spawnParticle(Particle.DRAGON_BREATH, start, 30, 1, 1, 1, 0.05);
                start.getWorld().spawnParticle(Particle.REVERSE_PORTAL, start, 50, 1, 1, 1, 0.1);
                start.getWorld().playSound(start, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
            }, i);
        }

        // Release after charging
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Vector direction = target.getLocation().toVector().subtract(start.toVector()).normalize();

            new BukkitRunnable() {
                Location blastLoc = start.clone();
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ > 60 || blastLoc.getBlock().getType().isSolid()) {
                        // Explosion on impact
                        blastLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, blastLoc, 10, 0, 0, 0, 0);
                        blastLoc.getWorld().playSound(blastLoc, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
                        cancel();
                        return;
                    }

                    blastLoc.add(direction.clone().multiply(0.8));

                    // Zikes purple energy effect
                    blastLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, blastLoc, 20, 0.5, 0.5, 0.5, 0.05);
                    blastLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, blastLoc, 30, 0.5, 0.5, 0.5, 0.1);
                    blastLoc.getWorld().spawnParticle(Particle.WITCH, blastLoc, 10, 0.3, 0.3, 0.3, 0);

                    // Check for hits in larger radius
                    Collection<Entity> nearby = blastLoc.getWorld().getNearbyEntities(blastLoc, 2, 2, 2);
                    for (Entity entity : nearby) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(15.0, bukkitEntity);

                            // Apply wither effect
                            if (entity instanceof Player) {
                                ((Player) entity).addPotionEffect(
                                    new org.bukkit.potion.PotionEffect(
                                        org.bukkit.potion.PotionEffectType.WITHER, 100, 2
                                    )
                                );
                            }

                            // Knockback
                            entity.setVelocity(direction.clone().multiply(1.5).setY(0.5));

                            blastLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, blastLoc, 5, 0, 0, 0, 0);
                            cancel();
                            return;
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Release sound
            start.getWorld().playSound(start, Sound.ENTITY_WITHER_SHOOT, 2.0f, 0.7f);
            start.getWorld().playSound(start, Sound.ENTITY_ENDER_DRAGON_SHOOT, 2.0f, 1.2f);
        }, 20L);
    }

    // Additional close-range attacks
    private void groundPound() {
        Location impact = bukkitEntity.getLocation();
        impact.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, impact, 5, 0, 0, 0, 0);

        Collection<Entity> nearby = impact.getWorld().getNearbyEntities(impact, 6, 3, 6);
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(10.0, bukkitEntity);
                entity.setVelocity(new Vector(0, 1.5, 0));
            }
        }
    }

    private void voidGrasp(Player target) {
        target.setVelocity(bukkitEntity.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(2.0));
        target.damage(6.0, bukkitEntity);
        target.getWorld().spawnParticle(Particle.SCULK_SOUL, target.getLocation(), 50, 1, 1, 1, 0.1);
    }

    // Additional medium-range attacks
    private void spiralBlades(Player target) {
        for (int i = 0; i < 8; i++) {
            final int index = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double angle = Math.toRadians(index * 45);
                Vector direction = new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize();

                new BukkitRunnable() {
                    Location bladeLoc = bukkitEntity.getLocation().clone();
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ > 30) {
                            cancel();
                            return;
                        }

                        bladeLoc.add(direction.clone().multiply(0.8));
                        bladeLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, bladeLoc, 1, 0, 0, 0, 0);
                        bladeLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, bladeLoc, 5, 0.2, 0.2, 0.2, 0);

                        Collection<Entity> hit = bladeLoc.getWorld().getNearbyEntities(bladeLoc, 1, 1, 1);
                        for (Entity entity : hit) {
                            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                                ((LivingEntity) entity).damage(5.0, bukkitEntity);
                                cancel();
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, i * 2L);
        }
    }

    private void voidWave(Player target) {
        Vector direction = target.getLocation().toVector().subtract(bukkitEntity.getLocation().toVector()).normalize();

        for (double d = 0; d < 20; d += 0.5) {
            final double distance = d;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location waveLoc = bukkitEntity.getLocation().add(direction.clone().multiply(distance));
                waveLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, waveLoc, 20, 1, 1, 1, 0.05);

                Collection<Entity> hit = waveLoc.getWorld().getNearbyEntities(waveLoc, 2, 2, 2);
                for (Entity entity : hit) {
                    if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                        ((LivingEntity) entity).damage(7.0, bukkitEntity);
                    }
                }
            }, (long)(distance * 2));
        }
    }

    private void teleportStrike(Player target) {
        Location behind = target.getLocation().clone().add(target.getLocation().getDirection().multiply(-3));
        bukkitEntity.teleport(behind);
        behind.getWorld().spawnParticle(Particle.PORTAL, behind, 100, 1, 1, 1, 1);
        target.damage(10.0, bukkitEntity);
        performVoidSlash(target);
    }

    // Additional long-range attacks
    private void homingMissiles(Player target) {
        for (int i = 0; i < 5; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                new BukkitRunnable() {
                    Location missileLoc = bukkitEntity.getLocation().add(0, 3, 0);
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ > 100) {
                            cancel();
                            return;
                        }

                        Vector direction = target.getLocation().toVector().subtract(missileLoc.toVector()).normalize();
                        missileLoc.add(direction.multiply(0.5));

                        missileLoc.getWorld().spawnParticle(Particle.FLAME, missileLoc, 10, 0.2, 0.2, 0.2, 0.02);
                        missileLoc.getWorld().spawnParticle(Particle.SMOKE, missileLoc, 5, 0.1, 0.1, 0.1, 0.01);

                        if (missileLoc.distance(target.getLocation()) < 2) {
                            missileLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, missileLoc, 3, 0, 0, 0, 0);
                            target.damage(8.0, bukkitEntity);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, i * 10L);
        }
    }

    private void voidBeam(Player target) {
        Location start = bukkitEntity.getLocation().add(0, 2, 0);
        Vector direction = target.getLocation().toVector().subtract(start.toVector()).normalize();

        for (double d = 0; d < 40; d += 0.3) {
            Location beamLoc = start.clone().add(direction.clone().multiply(d));
            beamLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, beamLoc, 5, 0, 0, 0, 0);
            beamLoc.getWorld().spawnParticle(Particle.SONIC_BOOM, beamLoc, 1, 0, 0, 0, 0);

            Collection<Entity> hit = beamLoc.getWorld().getNearbyEntities(beamLoc, 1, 1, 1);
            for (Entity entity : hit) {
                if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                    ((LivingEntity) entity).damage(4.0, bukkitEntity);
                }
            }
        }

        start.getWorld().playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.5f);
    }

    private void arcaneBarrage(Player target) {
        for (int i = 0; i < 10; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location start = bukkitEntity.getLocation().add(0, 2, 0);
                Vector direction = target.getLocation().add(
                        (Math.random() - 0.5) * 3,
                        Math.random() * 2,
                        (Math.random() - 0.5) * 3
                ).toVector().subtract(start.toVector()).normalize();

                new BukkitRunnable() {
                    Location orbLoc = start.clone();
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ > 60) {
                            cancel();
                            return;
                        }

                        orbLoc.add(direction.multiply(0.6));
                        orbLoc.getWorld().spawnParticle(Particle.END_ROD, orbLoc, 5, 0.1, 0.1, 0.1, 0.02);

                        Collection<Entity> hit = orbLoc.getWorld().getNearbyEntities(orbLoc, 1, 1, 1);
                        for (Entity entity : hit) {
                            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                                ((LivingEntity) entity).damage(3.0, bukkitEntity);
                                cancel();
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, i * 3L);
        }
    }

    private void meteorStrike(Player target) {
        Location meteorLoc = target.getLocation().add(0, 40, 0);

        new BukkitRunnable() {
            Location currentLoc = meteorLoc.clone();

            @Override
            public void run() {
                if (currentLoc.getY() <= currentLoc.getWorld().getHighestBlockYAt(currentLoc)) {
                    currentLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, currentLoc, 10, 0, 0, 0, 0);
                    currentLoc.getWorld().spawnParticle(Particle.LAVA, currentLoc, 150, 4, 2, 4, 0);
                    currentLoc.getWorld().playSound(currentLoc, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.5f);

                    Collection<Entity> hit = currentLoc.getWorld().getNearbyEntities(currentLoc, 6, 6, 6);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(15.0, bukkitEntity);
                            entity.setFireTicks(200);
                        }
                    }
                    cancel();
                    return;
                }

                currentLoc.add(0, -2, 0);
                currentLoc.getWorld().spawnParticle(Particle.FLAME, currentLoc, 50, 1, 1, 1, 0.1);
                currentLoc.getWorld().spawnParticle(Particle.LAVA, currentLoc, 10, 0.5, 0.5, 0.5, 0);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // Ultimate attacks
    private void cataclysm() {
        Location center = bukkitEntity.getLocation();

        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 5.0f, 0.3f);

        // Massive AOE damage
        for (double r = 1; r <= 30; r += 1) {
            final double radius = r;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int angle = 0; angle < 360; angle += 3) {
                    double rad = Math.toRadians(angle);
                    double x = radius * Math.cos(rad);
                    double z = radius * Math.sin(rad);
                    Location effectLoc = center.clone().add(x, 0, z);

                    effectLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, effectLoc, 1, 0, 0, 0, 0);
                    effectLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, effectLoc, 20, 0.5, 0.5, 0.5, 0.05);

                    Collection<Entity> hit = effectLoc.getWorld().getNearbyEntities(effectLoc, 2, 4, 2);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(20.0, bukkitEntity);
                        }
                    }
                }
            }, (long)(r * 1.5));
        }
    }

    private void worldEnder() {
        Location center = bukkitEntity.getLocation();

        // Screen shake effect + massive damage
        Collection<Entity> all = center.getWorld().getNearbyEntities(center, 50, 50, 50);
        for (Entity entity : all) {
            if (entity instanceof Player) {
                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 5));
                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 2));
            }
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(30.0, bukkitEntity);
                entity.setVelocity(new Vector(
                        (Math.random() - 0.5) * 3,
                        2.0,
                        (Math.random() - 0.5) * 3
                ));
            }
        }

        // Massive particle effect
        for (int i = 0; i < 500; i++) {
            Location randomLoc = center.clone().add(
                    (Math.random() - 0.5) * 60,
                    Math.random() * 40,
                    (Math.random() - 0.5) * 60
            );
            center.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, randomLoc, 3, 0, 0, 0, 0);
            center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, randomLoc, 50, 2, 2, 2, 0.1);
        }

        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 10.0f, 0.1f);
        center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 10.0f, 0.5f);
    }

    // ========================================
    // GOD-LIKE SYSTEMS & ADVANCED MECHANICS
    // ========================================

    /**
     * Spawn custom avatar body - unique humanoid form with black/red eyes
     */
    private void spawnAvatarHead() {
        Location headLoc = bukkitEntity.getLocation().clone().add(0, 1.7, 0); // Player/Zombie head height

        // Create head armor stand with custom player skull
        avatarHead = headLoc.getWorld().spawn(headLoc, ArmorStand.class);
        avatarHead.setVisible(false);
        avatarHead.setGravity(false);
        avatarHead.setMarker(true);
        avatarHead.setSmall(false);

        // Update eye color
        updateAvatarEyes();

        // Avatar head follow behavior - tracks zombie's head position
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead() || avatarHead == null) {
                    cancel();
                    return;
                }

                // Position head at player/zombie height (1.7 blocks tall)
                Location entityLoc = bukkitEntity.getLocation();
                Location newHeadLoc = entityLoc.clone().add(0, 1.7, 0);
                newHeadLoc.setYaw(entityLoc.getYaw());
                newHeadLoc.setPitch(entityLoc.getPitch());
                avatarHead.teleport(newHeadLoc);

                if (tickCounter % 20 == 0) {
                    updateAvatarEyes();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Update avatar eyes - black when passive, red when hostile
     */
    private void updateAvatarEyes() {
        if (avatarHead == null) return;

        org.bukkit.inventory.EntityEquipment headEquip = avatarHead.getEquipment();
        if (headEquip != null) {
            org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();

            if (skullMeta != null) {
                if (isHostile) {
                    skullMeta.setDisplayName("§4§lAI TORQUE");
                } else {
                    skullMeta.setDisplayName("§0§lAI TORQUE");
                }
                head.setItemMeta(skullMeta);
                headEquip.setHelmet(head);
            }
        }

        // Eye glow particles
        if (avatarHead.getLocation() != null) {
            if (isHostile) {
                // Red glowing eyes
                avatarHead.getWorld().spawnParticle(Particle.DUST,
                    avatarHead.getLocation().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2,
                    new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
            } else {
                // Dark/black eyes
                avatarHead.getWorld().spawnParticle(Particle.SMOKE,
                    avatarHead.getLocation().add(0, 0.5, 0), 2, 0.1, 0.1, 0.1, 0);
            }
        }
    }

    /** Spawn Masks - Floating protective masks around entity */
    private void spawnMasks() {
        int maskCount = 4 + (getCurrentPhase() * 2); // More masks as phases increase
        for (int i = 0; i < Math.min(maskCount, 16); i++) {
            ArmorStand mask = location.getWorld().spawn(location, ArmorStand.class);
            mask.setInvisible(false);
            mask.setMarker(true);
            mask.setGravity(false);
            mask.setInvulnerable(false);
            mask.setCustomName("§4Void Mask");
            mask.setGlowing(true);

            // Give mask a skull head
            org.bukkit.inventory.EntityEquipment equip = mask.getEquipment();
            if (equip != null) {
                equip.setHelmet(new org.bukkit.inventory.ItemStack(Material.WITHER_SKELETON_SKULL));
            }

            masks.add(mask);
        }

        // Animate masks orbiting
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Remove destroyed masks
                masks.removeIf(mask -> mask == null || !mask.isValid());

                // Spawn new masks if some were destroyed
                while (masks.size() < 4 && getCurrentPhase() >= 3) {
                    ArmorStand newMask = location.getWorld().spawn(bukkitEntity.getLocation(), ArmorStand.class);
                    newMask.setInvisible(false);
                    newMask.setMarker(true);
                    newMask.setGravity(false);
                    newMask.setCustomName("§4Regenerated Mask");
                    newMask.setGlowing(true);
                    org.bukkit.inventory.EntityEquipment equip = newMask.getEquipment();
                    if (equip != null) {
                        equip.setHelmet(new org.bukkit.inventory.ItemStack(Material.WITHER_SKELETON_SKULL));
                    }
                    masks.add(newMask);
                }

                for (int i = 0; i < masks.size(); i++) {
                    ArmorStand mask = masks.get(i);
                    if (mask == null || !mask.isValid()) continue;

                    double angle = Math.toRadians(i * (360.0 / masks.size()) + tick * 5);
                    double radius = 4.0;
                    double height = Math.sin(tick * 0.05 + i) * 1.5;

                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);

                    Location maskLoc = bukkitEntity.getLocation().add(x, 2 + height, z);
                    mask.teleport(maskLoc);

                    // Mask shoots at nearby players
                    if (tick % 100 == i * 10) {
                        Player nearest = findNearestPlayer();
                        if (nearest != null && nearest.getLocation().distance(maskLoc) < 20) {
                            maskShootBullet(maskLoc, nearest);
                        }
                    }

                    // Particle effects
                    maskLoc.getWorld().spawnParticle(Particle.WITCH, maskLoc, 3, 0.2, 0.2, 0.2, 0);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Mask shoots bullet at target */
    private void maskShootBullet(Location start, Player target) {
        Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(start.toVector()).normalize();

        new BukkitRunnable() {
            Location bulletLoc = start.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 60) {
                    cancel();
                    return;
                }

                bulletLoc.add(direction.multiply(1.0));
                bulletLoc.getWorld().spawnParticle(Particle.WITCH, bulletLoc, 3, 0.05, 0.05, 0.05, 0);
                bulletLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, bulletLoc, 2, 0.05, 0.05, 0.05, 0);

                Collection<Entity> hit = bulletLoc.getWorld().getNearbyEntities(bulletLoc, 0.5, 0.5, 0.5);
                for (Entity entity : hit) {
                    if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                        ((LivingEntity) entity).damage(4.0, bukkitEntity);
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        start.getWorld().playSound(start, Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.8f);
    }

    /** Spawn Teeth - Jaw/mouth with teeth */
    private void spawnTeeth() {
        int teethCount = 12;
        for (int i = 0; i < teethCount; i++) {
            ArmorStand tooth = location.getWorld().spawn(location, ArmorStand.class);
            tooth.setInvisible(true);
            tooth.setMarker(true);
            tooth.setGravity(false);
            tooth.setInvulnerable(true);
            teeth.add(tooth);
        }

        // Animate teeth (chomping motion)
        new BukkitRunnable() {
            private int tick = 0;
            private boolean chomping = false;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Trigger chomp when near player
                Player nearest = findNearestPlayer();
                if (nearest != null && nearest.getLocation().distance(bukkitEntity.getLocation()) < 7) {
                    chomping = true;
                } else {
                    chomping = false;
                }

                double jawOpen = chomping ? Math.abs(Math.sin(tick * 0.3)) * 2.0 : 0.5;

                for (int i = 0; i < teeth.size(); i++) {
                    ArmorStand tooth = teeth.get(i);
                    if (tooth == null || !tooth.isValid()) continue;

                    double angle = Math.toRadians(i * 30);
                    double radius = 1.5;

                    // Upper and lower jaw
                    boolean isUpperJaw = i < 6;
                    double yOffset = isUpperJaw ? (1.5 + jawOpen) : (0.5 - jawOpen);

                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);

                    Location toothLoc = bukkitEntity.getLocation().add(x, yOffset, z);
                    tooth.teleport(toothLoc);

                    // Tooth particles
                    toothLoc.getWorld().spawnParticle(Particle.END_ROD, toothLoc, 1, 0, 0, 0, 0);

                    // Chomp damage
                    if (chomping && tick % 10 == 0) {
                        Collection<Entity> hit = toothLoc.getWorld().getNearbyEntities(toothLoc, 1, 1, 1);
                        for (Entity entity : hit) {
                            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                                ((LivingEntity) entity).damage(3.0, bukkitEntity);
                            }
                        }
                    }
                }

                if (chomping && tick % 20 == 0) {
                    bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 2.0f, 0.5f);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Start tractor beam system */
    private void startTractorBeam() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                if (!isTractorBeamEnabled) return;

                // Find entities to beam
                Collection<Entity> nearby = bukkitEntity.getWorld().getNearbyEntities(
                        bukkitEntity.getLocation(), 25, 25, 25);

                for (Entity entity : nearby) {
                    if (entity.equals(bukkitEntity)) continue;
                    if (entity instanceof Player || entity instanceof Item || entity instanceof LivingEntity) {
                        tractorBeamTargets.add(entity);

                        // Visual beam
                        drawTractorBeam(bukkitEntity.getLocation().add(0, 1, 0), entity.getLocation());

                        // Pull entity
                        Vector pull = bukkitEntity.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                        entity.setVelocity(pull.multiply(0.5));
                    }
                }

                // Limit targets
                while (tractorBeamTargets.size() > 20) {
                    tractorBeamTargets.remove(0);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void drawTractorBeam(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = start.distance(end);

        for (double d = 0; d < distance; d += 0.5) {
            Location beamLoc = start.clone().add(direction.clone().normalize().multiply(d));
            beamLoc.getWorld().spawnParticle(Particle.END_ROD, beamLoc, 1, 0.1, 0.1, 0.1, 0);
            beamLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, beamLoc, 1, 0.1, 0.1, 0.1, 0);
        }
    }

    /** Regeneration system */
    private void startRegeneration() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    // REBIRTH MECHANIC
                    if (rebirthCount > 0 && isImmortal) {
                        rebirth();
                        cancel();
                    }
                    return;
                }

                if (!isRegenerating) return;

                // Passive regeneration
                double maxHp = bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double currentHp = bukkitEntity.getHealth();

                if (currentHp < maxHp) {
                    double regenAmount = maxHp * 0.01; // 1% per second
                    bukkitEntity.setHealth(Math.min(currentHp + regenAmount, maxHp));

                    // Regen particles
                    bukkitEntity.getWorld().spawnParticle(Particle.HEART,
                            bukkitEntity.getLocation().add(0, 2, 0), 3, 0.5, 0.5, 0.5, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second
    }

    /** Rebirth - Revive after death */
    private void rebirth() {
        rebirthCount--;

        Location rebirthLoc = location.clone();
        rebirthLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, rebirthLoc, 50, 3, 3, 3, 0);
        rebirthLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, rebirthLoc, 200, 3, 3, 3, 0.2);
        rebirthLoc.getWorld().spawnParticle(Particle.END_ROD, rebirthLoc, 100, 2, 2, 2, 0.3);
        rebirthLoc.getWorld().playSound(rebirthLoc, Sound.ENTITY_WITHER_SPAWN, 5.0f, 0.5f);
        rebirthLoc.getWorld().playSound(rebirthLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 5.0f, 1.0f);

        // Respawn entity
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            this.bukkitEntity = (LivingEntity) rebirthLoc.getWorld().spawnEntity(rebirthLoc, EntityType.SHULKER);
            bukkitEntity.setInvisible(true);
            bukkitEntity.setCustomName("§4§l⚡ §5§lAI TORQUE REBORN §4§l⚡");
            bukkitEntity.setCustomNameVisible(true);
            bukkitEntity.setGlowing(true);

            if (bukkitEntity instanceof Shulker) {
                ((Shulker) bukkitEntity).setPeek(0);
            }

            bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            bukkitEntity.setHealth(maxHealth);

            // Explosion on rebirth
            for (int i = 0; i < 360; i += 15) {
                double rad = Math.toRadians(i);
                double x = 10 * Math.cos(rad);
                double z = 10 * Math.sin(rad);
                Location expLoc = rebirthLoc.clone().add(x, 0, z);
                expLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, expLoc, 3, 0, 0, 0, 0);
            }

            // Restart behaviors
            startFloatingBehavior();
            startAuraEffects();
            startCombatBehavior();
        }, 40L);
    }

    /** Brewing system - AI Torque brews potions */
    private void startBrewingSystem() {
        new BukkitRunnable() {
            int brewTick = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                brewTick++;

                // Brew potion every 30 seconds
                if (brewTick % 600 == 0) {
                    brewPotion();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void brewPotion() {
        Location brewLoc = bukkitEntity.getLocation().add(3, 0, 0);

        // Spawn brewing stand temporarily
        org.bukkit.block.Block brewBlock = brewLoc.getBlock();
        Material original = brewBlock.getType();
        brewBlock.setType(Material.BREWING_STAND);

        // Brewing particles
        for (int i = 0; i < 100; i++) {
            brewLoc.getWorld().spawnParticle(Particle.WITCH, brewLoc.add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        }

        brewLoc.getWorld().playSound(brewLoc, Sound.BLOCK_BREWING_STAND_BREW, 2.0f, 1.0f);

        // Create potion and throw it
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player nearest = findNearestPlayer();
            if (nearest != null) {
                throwHarmfulPotion(brewLoc, nearest);
            }

            // Remove brewing stand
            brewBlock.setType(original);
        }, 60L);
    }

    private void throwHarmfulPotion(Location from, Player target) {
        ThrownPotion potion = from.getWorld().spawn(from, ThrownPotion.class);

        org.bukkit.inventory.ItemStack potionItem = new org.bukkit.inventory.ItemStack(Material.SPLASH_POTION);
        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) potionItem.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 200, 2), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 2), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2), true);

        potionItem.setItemMeta(meta);
        potion.setItem(potionItem);

        Vector direction = target.getLocation().toVector().subtract(from.toVector()).normalize();
        potion.setVelocity(direction.multiply(1.5));

        from.getWorld().playSound(from, Sound.ENTITY_WITCH_THROW, 2.0f, 1.0f);
    }

    /** Environment interaction - activates levers/trapdoors */
    private void startEnvironmentInteraction() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Find nearby interactive blocks
                Location center = bukkitEntity.getLocation();
                for (int x = -10; x <= 10; x++) {
                    for (int y = -5; y <= 5; y++) {
                        for (int z = -10; z <= 10; z++) {
                            org.bukkit.block.Block block = center.clone().add(x, y, z).getBlock();

                            if (block.getType() == Material.LEVER ||
                                block.getType() == Material.OAK_TRAPDOOR ||
                                block.getType() == Material.IRON_TRAPDOOR ||
                                block.getType() == Material.OAK_BUTTON ||
                                block.getType() == Material.STONE_BUTTON ||
                                block.getType() == Material.OAK_DOOR ||
                                block.getType() == Material.IRON_DOOR) {

                                // Randomly activate
                                if (Math.random() < 0.1) {
                                    toggleBlock(block);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Every 5 seconds
    }

    private void toggleBlock(org.bukkit.block.Block block) {
        if (block.getBlockData() instanceof org.bukkit.block.data.Powerable) {
            org.bukkit.block.data.Powerable powerable = (org.bukkit.block.data.Powerable) block.getBlockData();
            powerable.setPowered(!powerable.isPowered());
            block.setBlockData(powerable);
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
        } else if (block.getBlockData() instanceof org.bukkit.block.data.Openable) {
            org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) block.getBlockData();
            openable.setOpen(!openable.isOpen());
            block.setBlockData(openable);
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_OPEN, 1.0f, 1.0f);
        }
    }

    /** Debris manipulation - breaks blocks and uses them as weapons */
    private void startDebrisManipulation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Break nearby blocks
                if (getCurrentPhase() >= 3 && tickCounter % 40 == 0) {
                    breakAndOrbitDebris();
                }

                // Launch orbiting debris at players
                if (tickCounter % 60 == 0 && !orbitingBlocks.isEmpty()) {
                    launchDebrisAtPlayer();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void breakAndOrbitDebris() {
        Location center = bukkitEntity.getLocation();

        for (int i = 0; i < 5; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double dist = 3 + Math.random() * 5;
            double x = dist * Math.cos(angle);
            double z = dist * Math.sin(angle);

            org.bukkit.block.Block block = center.clone().add(x, -1, z).getBlock();

            if (!block.getType().isAir() && block.getType().isSolid() && block.getType() != Material.BEDROCK) {
                // Break block with particles
                block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation(), 30,
                        0.3, 0.3, 0.3, 0, block.getBlockData());
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);

                orbitingBlocks.add(block);
                block.setType(Material.AIR);

                if (orbitingBlocks.size() > 50) {
                    orbitingBlocks.remove(0);
                }
            }
        }
    }

    private void launchDebrisAtPlayer() {
        if (orbitingBlocks.isEmpty()) return;

        Player target = findNearestPlayer();
        if (target == null) return;

        org.bukkit.block.Block debris = orbitingBlocks.remove(0);
        Location start = bukkitEntity.getLocation().add(0, 2, 0);
        Vector direction = target.getLocation().toVector().subtract(start.toVector()).normalize();

        // Spawn falling block as projectile
        FallingBlock fallingBlock = start.getWorld().spawnFallingBlock(start, debris.getBlockData());
        fallingBlock.setVelocity(direction.multiply(2.0));
        fallingBlock.setDropItem(false);
        fallingBlock.setHurtEntities(true);
        fallingBlock.setDamagePerBlock(10.0f);

        start.getWorld().playSound(start, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.8f);
    }

    /** God Systems - Reflection, Immunities, Charging */
    private void startGodSystems() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Apply immunities
                bukkitEntity.setFireTicks(0);
                bukkitEntity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 10, false, false));
                bukkitEntity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 20, 10, false, false));

                // Reflection aura
                if (isReflecting || getCurrentPhase() >= 8) {
                    Collection<Entity> nearby = bukkitEntity.getWorld().getNearbyEntities(
                            bukkitEntity.getLocation(), 5, 5, 5);

                    for (Entity entity : nearby) {
                        if (entity instanceof org.bukkit.entity.Projectile) {
                            org.bukkit.entity.Projectile proj = (org.bukkit.entity.Projectile) entity;

                            // Reflect projectile
                            Vector velocity = proj.getVelocity();
                            proj.setVelocity(velocity.multiply(-1.5));

                            bukkitEntity.getWorld().spawnParticle(Particle.ENCHANT,
                                    proj.getLocation(), 20, 0.5, 0.5, 0.5, 1.0);
                            bukkitEntity.getWorld().playSound(proj.getLocation(),
                                    Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 2.0f);
                        }
                    }
                }

                // Charging mechanic
                if (isCharging) {
                    chargeTime++;
                    chargeLevel = Math.min(chargeTime / 60.0, 10.0); // Max charge level 10

                    // Charge particles
                    double radius = chargeLevel * 0.5;
                    for (int i = 0; i < 10; i++) {
                        double angle = Math.toRadians(i * 36 + tickCounter * 10);
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        Location chargeLoc = bukkitEntity.getLocation().add(x, 1, z);
                        bukkitEntity.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, chargeLoc, 5, 0.1, 0.1, 0.1, 0.1);
                        bukkitEntity.getWorld().spawnParticle(Particle.END_ROD, chargeLoc, 3, 0, 0, 0, 0.2);
                    }

                    if (chargeTime % 10 == 0) {
                        bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(),
                                Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.0f + (float)(chargeLevel * 0.1));
                    }
                }

                // Enable tractor beam at high phases
                if (getCurrentPhase() >= 5) {
                    isTractorBeamEnabled = true;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Charge and release devastating attack */
    private void chargeAttack() {
        isCharging = true;
        chargeTime = 0;
        chargeLevel = 0;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            isCharging = false;
            releaseChargedAttack();
        }, 100L); // 5 seconds charge
    }

    private void releaseChargedAttack() {
        Location center = bukkitEntity.getLocation();
        double power = chargeLevel * 5; // Damage multiplier
        double radius = chargeLevel * 3; // AOE radius

        // Massive explosion
        for (double r = 1; r <= radius; r += 0.5) {
            final double finalR = r;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int angle = 0; angle < 360; angle += 5) {
                    double rad = Math.toRadians(angle);
                    double x = finalR * Math.cos(rad);
                    double z = finalR * Math.sin(rad);
                    Location expLoc = center.clone().add(x, 0, z);

                    expLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, expLoc, 5, 0, 0, 0, 0);
                    expLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, expLoc, 30, 0.5, 0.5, 0.5, 0.2);
                    expLoc.getWorld().spawnParticle(Particle.END_ROD, expLoc, 20, 0.3, 0.3, 0.3, 0.3);

                    Collection<Entity> hit = expLoc.getWorld().getNearbyEntities(expLoc, 2, 3, 2);
                    for (Entity entity : hit) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(power, bukkitEntity);
                        }
                    }
                }
            }, (long)(finalR * 2));
        }

        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 5.0f, 0.3f);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.5f);

        chargeLevel = 0;
        chargeTime = 0;
    }

    /**
     * Start the main entity tick loop
     */
    private void startEntityTick() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    if (isImmortal) {
                        handleExhaustion();
                    } else {
                        cleanup();
                        cancel();
                    }
                    return;
                }

                tick();
            }
        }.runTaskTimer(plugin, 0L, plugin.getConfigManager().getTickRate());
    }

    /**
     * Main tick method - called every game tick
     */
    private void tick() {
        tickCounter++;
        abilityTickCounter++;

        // Update location
        location = bukkitEntity.getLocation();

        // Update phase based on health
        health = bukkitEntity.getHealth();
        phaseManager.updatePhase();

        // Tick current phase
        phaseManager.tick();

        // Update orbiting objects
        if (isDebrisOrbitActive) {
            updateDebrisOrbit();
        }

        // Unique AI behaviors (no normal mob has these)
        if (tickCounter % 20 == 0) {
            applyGravitationalPull();
        }

        if (tickCounter % 100 == 0 && getCurrentPhase() >= 3) {
            performVoidTeleport();
        }

        // Check for hostility triggers
        checkHostilityTriggers();

        // Spawn particles
        spawnAmbientParticles();
    }

    /**
     * Unique behavior: Gravitational pull on nearby entities
     */
    private void applyGravitationalPull() {
        double pullRadius = 16.0;
        double pullStrength = 0.3;

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(
                location, pullRadius, pullRadius, pullRadius,
                entity -> entity instanceof LivingEntity && !(entity.equals(bukkitEntity))
        );

        for (Entity entity : nearbyEntities) {
            org.bukkit.util.Vector direction = location.toVector().subtract(entity.getLocation().toVector()).normalize();
            entity.setVelocity(entity.getVelocity().add(direction.multiply(pullStrength)));

            // Void particles showing the pull
            entity.getWorld().spawnParticle(Particle.REVERSE_PORTAL,
                    entity.getLocation(), 5, 0.2, 0.2, 0.2, 0.05);
        }
    }

    /**
     * Unique behavior: Teleport to random nearby location (phase through reality)
     */
    private void performVoidTeleport() {
        // Find a nearby player to teleport near
        org.bukkit.entity.Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (org.bukkit.entity.Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance < 50 && distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }

        if (nearestPlayer != null) {
            // Teleport effects at old location
            location.getWorld().spawnParticle(Particle.PORTAL, location, 100, 1, 1, 1, 1);
            location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 50, 1, 1, 1, 0.5);

            // Teleport near player
            Location targetLoc = nearestPlayer.getLocation().clone().add(
                    (Math.random() - 0.5) * 10,
                    2,
                    (Math.random() - 0.5) * 10
            );

            bukkitEntity.teleport(targetLoc);
            location = targetLoc;

            // Teleport effects at new location
            location.getWorld().spawnParticle(Particle.PORTAL, location, 100, 1, 1, 1, 1);
            location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 50, 1, 1, 1, 0.5);
            location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, location, 3, 0, 0, 0, 0);
        }
    }

    /**
     * Attempt to grab nearby objects
     */
    public void attemptGrabNearbyObjects(double radius) {
        if (!plugin.getConfigManager().isGrabbingEnabled()) return;

        int maxObjects = plugin.getConfigManager().getMaxOrbitingObjects();
        if (orbitingObjects.size() >= maxObjects) return;

        // Find nearby entities
        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(
                location, radius, radius, radius,
                entity -> entity instanceof Item || entity instanceof LivingEntity
        );

        for (Entity entity : nearbyEntities) {
            if (orbitingObjects.size() >= maxObjects) break;

            if (entity instanceof Item || (entity instanceof LivingEntity && !(entity instanceof Player))) {
                orbitingObjects.add(entity);
                power += calculatePowerFromEntity(entity);

                // Visual effect
                location.getWorld().spawnParticle(
                        Particle.PORTAL,
                        entity.getLocation(),
                        50,
                        0.5, 0.5, 0.5,
                        0.1
                );
            }
        }
    }

    /**
     * Calculate power gained from an entity
     */
    private double calculatePowerFromEntity(Entity entity) {
        if (entity instanceof Item) {
            // Different materials give different power
            return 5.0; // Base power for items
        } else if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            return living.getMaxHealth() * 2; // Power based on max health
        }
        return 1.0;
    }

    /**
     * Update orbiting debris
     */
    public void updateDebrisOrbit() {
        double radius = 3.0 + (phaseManager.getSizeMultiplier() * 0.5);
        double speed = 0.1;

        for (int i = 0; i < orbitingObjects.size(); i++) {
            Entity entity = orbitingObjects.get(i);

            if (entity == null || !entity.isValid()) {
                orbitingObjects.remove(i);
                i--;
                continue;
            }

            // Calculate orbital position
            double angle = (tickCounter * speed) + (i * (2 * Math.PI / orbitingObjects.size()));
            double x = location.getX() + radius * Math.cos(angle);
            double z = location.getZ() + radius * Math.sin(angle);
            double y = location.getY() + 2 + Math.sin(tickCounter * 0.05) * 0.5;

            Location targetLoc = new Location(location.getWorld(), x, y, z);
            entity.teleport(targetLoc);

            // Spawn trail particles
            location.getWorld().spawnParticle(
                    Particle.FLAME,
                    entity.getLocation(),
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    /**
     * Start debris orbit system
     */
    public void startDebrisOrbit() {
        isDebrisOrbitActive = true;
        sendChatMessage("§6[AI Torque] §eDebris begins to orbit...");
    }

    /**
     * Spawn visual accessories (wings, halos, etc.)
     */
    public void spawnAccessories() {
        // Spawn armor stands or particles for visual accessories
        location.getWorld().spawnParticle(
                Particle.END_ROD,
                location.clone().add(0, 2, 0),
                100,
                2, 2, 2,
                0.1
        );

        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
    }

    /**
     * Enable tractor beam ability
     */
    public void enableTractorBeam() {
        isTractorBeamEnabled = true;
    }

    /**
     * Use tractor beam to grab large objects
     */
    public void useTractorBeam(double radius) {
        if (!isTractorBeamEnabled) return;

        // Visual beam effect
        location.getWorld().spawnParticle(
                Particle.ELECTRIC_SPARK,
                location.clone().add(0, 1, 0),
                200,
                radius, radius, radius,
                0.1
        );

        // Pull entities toward AI Torque
        Collection<Entity> nearby = location.getWorld().getNearbyEntities(
                location, radius, radius, radius
        );

        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !(entity.equals(bukkitEntity))) {
                Vector direction = location.toVector().subtract(entity.getLocation().toVector()).normalize();
                entity.setVelocity(direction.multiply(0.5));
            }
        }

        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 3.0f, 0.5f);
    }

    /**
     * Enable mask teeth block breaking
     */
    public void enableMaskTeethBlockBreaking() {
        isMaskTeethEnabled = true;
    }

    /**
     * Break blocks with mask teeth
     */
    public void breakBlocksWithMaskTeeth() {
        if (!isMaskTeethEnabled) return;

        // Break blocks around AI Torque
        int radius = 3;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location blockLoc = location.clone().add(x, y, z);
                    if (blockLoc.getBlock().getType().isSolid()) {
                        blockLoc.getBlock().breakNaturally();
                    }
                }
            }
        }
    }

    /**
     * Attempt to consume a nearby village
     */
    public void attemptVillageConsumption(double radius) {
        // Find villagers in range
        Collection<Entity> entities = location.getWorld().getNearbyEntities(
                location, radius, radius, radius,
                entity -> entity instanceof Villager
        );

        if (entities.size() > 5) { // Consider it a village if 5+ villagers
            // Consume the village
            for (Entity entity : entities) {
                entity.remove();
                power += 1000000; // Huge power boost
            }

            // Massive visual effect
            location.getWorld().createExplosion(location, 0.0f, false, false);
            location.getWorld().spawnParticle(
                    Particle.EXPLOSION_EMITTER,
                    location,
                    50,
                    10, 10, 10,
                    0.1
            );

            sendChatMessage("§6[AI Torque] §4§lA VILLAGE HAS BEEN CONSUMED! POWER GROWS!");

            // Increase size dramatically
            health += maxHealth * 0.1; // Heal 10%
        }
    }

    /**
     * Create wind blast that pushes players
     */
    public void createWindBlast(double radius) {
        Collection<Entity> entities = location.getWorld().getNearbyEntities(
                location, radius, radius, radius
        );

        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Vector direction = entity.getLocation().toVector()
                        .subtract(location.toVector())
                        .normalize()
                        .multiply(2.0);
                entity.setVelocity(direction);
            }
        }

        location.getWorld().spawnParticle(
                Particle.CLOUD,
                location,
                100,
                radius, radius, radius,
                0.1
        );

        location.getWorld().playSound(location, Sound.ITEM_ELYTRA_FLYING, 3.0f, 0.5f);
    }

    /**
     * Enable storm creation
     */
    public void enableStormCreation() {
        location.getWorld().setStorm(true);
        location.getWorld().setThundering(true);
    }

    /**
     * Create debris storm
     */
    public void createDebrisStorm() {
        // Spawn debris particles in a storm pattern
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double distance = 5 + Math.random() * 10;
            double x = location.getX() + distance * Math.cos(angle);
            double z = location.getZ() + distance * Math.sin(angle);
            double y = location.getY() + Math.random() * 10;

            Location particleLoc = new Location(location.getWorld(), x, y, z);
            location.getWorld().spawnParticle(
                    Particle.BLOCK,
                    particleLoc,
                    10,
                    0.5, 0.5, 0.5,
                    0.1
            );
        }
    }

    /**
     * Perform dive attack
     */
    public void performDiveAttack() {
        // Move up first
        bukkitEntity.teleport(location.clone().add(0, 10, 0));

        // Then dive down with force after delay
        new BukkitRunnable() {
            @Override
            public void run() {
                bukkitEntity.setVelocity(new Vector(0, -2, 0));

                // Create impact shockwave on landing
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (bukkitEntity.isOnGround() || bukkitEntity.getLocation().getY() <= location.getY()) {
                            createMassiveShockwave(20);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 2L);
            }
        }.runTaskLater(plugin, 20L);
    }

    /**
     * Create massive shockwave
     */
    public void createMassiveShockwave(double radius) {
        // Damage and knockback entities
        Collection<Entity> entities = location.getWorld().getNearbyEntities(
                location, radius, radius, radius
        );

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(10.0 * plugin.getConfigManager().getDamageMultiplier());

                Vector knockback = entity.getLocation().toVector()
                        .subtract(location.toVector())
                        .normalize()
                        .multiply(3.0);
                entity.setVelocity(knockback);
            }
        }

        // Visual effects
        location.getWorld().spawnParticle(
                Particle.EXPLOSION_EMITTER,
                location,
                10,
                radius / 2, 1, radius / 2,
                0.1
        );

        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.3f);
    }

    /**
     * Enable soul grabbing
     */
    public void enableSoulGrabbing() {
        isSoulGrabbingEnabled = true;
    }

    /**
     * Grab souls from nearby mobs
     */
    public void grabNearbySouls(double radius) {
        if (!isSoulGrabbingEnabled) return;

        Collection<Entity> entities = location.getWorld().getNearbyEntities(
                location, radius, radius, radius,
                entity -> entity instanceof LivingEntity && !(entity instanceof Player)
        );

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;

                // Drain health
                double drainAmount = living.getHealth() * 0.1; // 10% of health
                living.damage(drainAmount);
                power += drainAmount * 10;

                // Soul particle effect
                location.getWorld().spawnParticle(
                        Particle.SOUL,
                        entity.getLocation(),
                        20,
                        0.5, 1, 0.5,
                        0.1
                );
            }
        }
    }

    /**
     * Initiate rebirth sequence
     */
    public void initiateRebirth() {
        // Visual effect
        location.getWorld().spawnParticle(
                Particle.END_ROD,
                location,
                200,
                5, 5, 5,
                0.2
        );

        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 5.0f, 0.1f);

        // Reset some stats but keep power
        // Size resets, will grow again
    }

    /**
     * Transform to Zikes form
     */
    public void transformToZikes() {
        currentForm = TransformationForm.ZIKES;
        sendChatMessage("§6[AI Torque] §cYou are very strong, but I can't lose!");

        // Apply Zikes abilities
        // This would spawn a different entity or modify current one
        maxHealth *= 2;
        bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        bukkitEntity.setHealth(maxHealth);

        // Visual transformation
        location.getWorld().spawnParticle(
                Particle.DRAGON_BREATH,
                location,
                500,
                10, 10, 10,
                0.5
        );

        // Apply Zikes-specific abilities
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                3,
                false,
                false
        ));

        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.5f);
    }

    /**
     * Transform to TEOTU (The End Of The Universe) - Shark form
     */
    public void transformToTEOTU() {
        currentForm = TransformationForm.TEOTU;
        sendChatMessage("§6[AI Torque] §4§l§nTHE END OF THE UNIVERSE!");
        sendChatMessage("§6[AI Torque] §4You cannot comprehend my true form...");

        // Massive power boost
        maxHealth *= 3;
        bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        bukkitEntity.setHealth(maxHealth);

        // Extreme damage and speed
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                5,
                false,
                false
        ));
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                4,
                false,
                false
        ));

        // Resistance and regeneration
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                Integer.MAX_VALUE,
                3,
                false,
                false
        ));
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                Integer.MAX_VALUE,
                2,
                false,
                false
        ));

        // Dramatic visual transformation
        location.getWorld().spawnParticle(
                Particle.END_ROD,
                location,
                1000,
                15, 15, 15,
                0.8
        );
        location.getWorld().spawnParticle(
                Particle.PORTAL,
                location,
                500,
                10, 10, 10,
                1.0
        );

        // Sounds
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 3.0f, 0.3f);
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.5f);

        // Grant all abilities
        enableTractorBeam();
        enableStormCreation();
        enableSoulGrabbing();
        enableMaskTeethBlockBreaking();

        // Activate all ability systems
        if (plugin.getConfigManager().isInfectionEnabled()) {
            infectionSystem.activate();
        }
        if (plugin.getConfigManager().isCloningEnabled()) {
            cloningSystem.initiate();
        }
        if (plugin.getConfigManager().isTypeSystemEnabled()) {
            typeSystem.activate();
        }
    }

    /**
     * Transform to Medinuio Aura - Ultimate form with all powers
     */
    public void transformToMedinuioAura() {
        currentForm = TransformationForm.MEDINUIO_AURA;
        sendChatMessage("§6[AI Torque] §d§l§n§oMEDINUIO AURA");
        sendChatMessage("§6[AI Torque] §5§lI AM BEYOND COMPREHENSION!");
        sendChatMessage("§6[AI Torque] §5Reality bends to my will...");

        // Ultimate power - 5x multiplier
        maxHealth *= 5;
        bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        bukkitEntity.setHealth(maxHealth);

        // Maximum effects
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                9,
                false,
                false
        ));
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                7,
                false,
                false
        ));
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                Integer.MAX_VALUE,
                5,
                false,
                false
        ));
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                Integer.MAX_VALUE,
                4,
                false,
                false
        ));
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE,
                Integer.MAX_VALUE,
                1,
                false,
                false
        ));

        // Incredible visual transformation
        for (int i = 0; i < 5; i++) {
            int delay = i * 10;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                location.getWorld().spawnParticle(
                        Particle.ENCHANT,
                        location,
                        2000,
                        20, 20, 20,
                        1.5
                );
                location.getWorld().spawnParticle(
                        Particle.END_ROD,
                        location,
                        1000,
                        15, 15, 15,
                        1.0
                );
                location.getWorld().spawnParticle(
                        Particle.DRAGON_BREATH,
                        location,
                        500,
                        10, 10, 10,
                        0.8
                );
            }, delay);
        }

        // Epic sound sequence
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 3.0f, 0.1f);
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.3f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3.0f, 0.8f);
        }, 20L);

        // Unlock all abilities
        enableTractorBeam();
        enableStormCreation();
        enableSoulGrabbing();
        enableMaskTeethBlockBreaking();

        // Force-activate all ability systems
        if (plugin.getConfigManager().isInfectionEnabled()) {
            infectionSystem.activate();
        }
        if (plugin.getConfigManager().isCloningEnabled()) {
            cloningSystem.initiate();
        }
        if (plugin.getConfigManager().isTypeSystemEnabled()) {
            typeSystem.activate();
        }
        if (plugin.getConfigManager().areHealingTowersEnabled()) {
            healingTowerSystem.spawnTowers();
        }
        if (plugin.getConfigManager().areStatueDropsEnabled()) {
            statueSystem.startSpawning();
        }

        // Activate forcefield
        activateForcefield();

        // Extreme power boost
        power *= 10;
    }

    /**
     * Accelerate growth after rebirth
     */
    public void accelerateGrowth() {
        power += 100; // Rapid power accumulation
    }

    /**
     * Apply speed boost
     */
    public void applySpeedBoost(double multiplier) {
        bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                (int) (multiplier * 2),
                false,
                false
        ));
    }

    /**
     * Check for hostility triggers
     */
    private void checkHostilityTriggers() {
        if (isHostile) return;
        if (!plugin.getConfigManager().isNeutralByDefault()) return;

        // Check if players are too close
        double aggroDistance = plugin.getConfigManager().getAggroDistance();
        Collection<Entity> nearbyPlayers = location.getWorld().getNearbyEntities(
                location, aggroDistance, aggroDistance, aggroDistance,
                entity -> entity instanceof Player
        );

        if (!nearbyPlayers.isEmpty()) {
            becomeHostile();
        }
    }

    /**
     * Become hostile
     */
    private void becomeHostile() {
        isHostile = true;
        sendChatMessage("§6[AI Torque] §4You dare approach me?!");

        // Visual transformation to battle form
        location.getWorld().spawnParticle(
                Particle.FLAME,
                location,
                100,
                2, 2, 2,
                0.1
        );

        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.5f);
    }

    /**
     * Spawn ambient particles
     */
    private void spawnAmbientParticles() {
        if (!plugin.getConfigManager().areParticlesEnabled()) return;

        if (tickCounter % 5 == 0) {
            location.getWorld().spawnParticle(
                    Particle.PORTAL,
                    location.clone().add(0, 1, 0),
                    10,
                    1, 1, 1,
                    0.1
            );
        }
    }

    /**
     * Handle exhaustion (when would normally die but is immortal)
     */
    private void handleExhaustion() {
        sendChatMessage("§6[AI Torque] §7Impossible... the odds...");

        // Shrink to normal size
        // Enter zen rest state
        // Spawn floating castle

        bukkitEntity.remove();
        cleanup();
    }

    /**
     * Send chat message as AI Torque
     */
    public void sendChatMessage(String message) {
        if (plugin.getConfigManager().isChatEnabled()) {
            Bukkit.broadcastMessage(message);
        }
    }

    /**
     * Cleanup and remove entity
     */
    public void cleanup() {
        if (bukkitEntity != null) {
            bukkitEntity.remove();
        }

        // Clean up orbiting objects
        for (Entity entity : orbitingObjects) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }

        // Clean up tentacles
        for (ArmorStand tentacle : tentacles) {
            if (tentacle != null && tentacle.isValid()) {
                tentacle.remove();
            }
        }

        // Clean up avatar head
        if (avatarHead != null && avatarHead.isValid()) {
            avatarHead.remove();
        }

        // Clean up ability systems
        if (infectionSystem != null) {
            infectionSystem.cleanup();
        }
        if (cloningSystem != null) {
            cloningSystem.cleanup();
        }
        if (healingTowerSystem != null) {
            healingTowerSystem.cleanup();
        }
        if (statueSystem != null) {
            statueSystem.cleanup();
        }

        // Unregister from plugin
        plugin.unregisterTorque(uniqueId);
    }

    /**
     * Set tunneling state
     */
    public void setTunneling(boolean tunneling) {
        this.isTunneling = tunneling;
    }

    /**
     * Teleport AI Torque to a new location
     */
    public void teleport(Location newLocation) {
        this.location = newLocation;
        if (bukkitEntity != null) {
            bukkitEntity.teleport(newLocation);
        }
    }

    // ========== NEW METHODS FOR PHASES 11-20 ==========

    public void startRapidPowerGain() { power += 100; }
    public void acceleratePowerGain() { power += 50; }

    // Cloning system
    public void initiateCloning() { cloningSystem.initiate(); }
    public void spawnCloneNearVillage() { cloningSystem.spawnCloneNearVillage(); }
    public void consumeVoidFromClones() {
        double voidEnergy = cloningSystem.consumeVoidFromClones();
        power += voidEnergy;
    }

    // Infection system
    public void activateInfectionSystem() { infectionSystem.activate(); }
    public void infectNearbyVillagers() { infectionSystem.infectNearbyVillagers(); }
    public void updateParasiteSpread() { infectionSystem.updateParasiteSpread(); }

    // Healing towers
    public void spawnHealingTowers() { healingTowerSystem.spawnTowers(); }
    public void healFromTowers() { healingTowerSystem.healFromTowers(); }
    public void respawnDestroyedTowers() { healingTowerSystem.respawnDestroyedTowers(); }

    // Type system
    public void activateTypeSystem() { typeSystem.activate(); }
    public void useTypeAttack() { typeSystem.useTypeAttack(); }
    public boolean shouldChangeType() { return typeSystem.shouldChangeType(); }
    public void changeToNextType() { typeSystem.changeToNextType(); }

    // Clone consumption
    public void consumeAllClones() {
        double power = cloningSystem.consumeAllClones();
        this.power += power;
        sendChatMessage("§6[AI Torque] §4§l§nGIGANTIC TRANSFORMATION!");
    }

    // Forcefield
    public void activateForcefield() {
        hasForcefield = true;
        forcefieldStrength = 0.5;
        sendChatMessage("§6[AI Torque] §bForcefield activated!");
    }

    public void updateForcefield() {
        if (hasForcefield && tickCounter % 10 == 0) {
            // Use END_ROD particles for forcefield effect (BARRIER doesn't exist in 1.20.4)
            location.getWorld().spawnParticle(Particle.END_ROD, location, 10, 2, 2, 2, 0);
        }
    }

    public void destroyNearbyTerrain(int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.random() < 0.1) {
                        Location blockLoc = location.clone().add(x, y, z);
                        Material type = blockLoc.getBlock().getType();
                        if (type.isSolid() && type != Material.BEDROCK) {
                            blockLoc.getBlock().breakNaturally();
                        }
                    }
                }
            }
        }
    }

    // Boss draining
    public void drainBossHealth() {
        Collection<Entity> entities = location.getWorld().getNearbyEntities(
                location, 50, 50, 50,
                e -> e instanceof EnderDragon || e instanceof Wither
        );

        for (Entity e : entities) {
            if (e instanceof LivingEntity) {
                LivingEntity boss = (LivingEntity) e;
                double drain = boss.getHealth() * 0.05;
                boss.damage(drain);
                heal(drain * 2);
                power += drain * 100;
                sendChatMessage("§6[AI Torque] §dDraining " + boss.getName() + "!");
            }
        }
    }

    // Statue system
    public void startStatueSpawning() { statueSystem.startSpawning(); }
    public void spawnStatue() { statueSystem.spawnStatue(); }

    public void maximumAggression() {
        if (tickCounter % 20 == 0) {
            attemptGrabNearbyObjects(50);
            grabNearbySouls(40);
            createMassiveShockwave(25);
            useTractorBeam(40);
        }
    }

    // Final phase
    public void grabAllNearbyMobs(double radius) {
        Collection<Entity> entities = location.getWorld().getNearbyEntities(
                location, radius, radius, radius,
                e -> e instanceof LivingEntity && !(e instanceof Player)
        );

        for (Entity e : entities) {
            orbitingObjects.add(e);
            e.getWorld().spawnParticle(Particle.SOUL, e.getLocation(), 50, 1, 1, 1, 0.1);
        }

        sendChatMessage("§6[AI Torque] §4§lALL LIFE CONSUMED!");
    }

    public void fullHealFromGrab() {
        health = maxHealth;
        bukkitEntity.setHealth(maxHealth);
        location.getWorld().spawnParticle(Particle.HEART, location, 200, 5, 5, 5, 0.5);
        sendChatMessage("§6[AI Torque] §a§lFULLY RESTORED!");
    }

    public void initiateZikesTransformation() { transformToZikes(); }

    /**
     * Check if entity should progress to next transformation form
     */
    public void checkTransformationProgression() {
        if (!plugin.getConfigManager().areTransformationsAllowed()) return;

        double healthPercent = health / maxHealth;

        // Transformation progression based on current form and health
        switch (currentForm) {
            case NORMAL:
                // Already handled by phases (Phase 10, Phase 20)
                break;

            case ZIKES:
                // Transform to TEOTU when Zikes reaches 30% health
                if (healthPercent <= 0.30) {
                    sendChatMessage("§6[AI Torque] §4§lZikes cannot contain me any longer!");
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        transformToTEOTU();
                    }, 40L); // 2 second delay
                }
                break;

            case TEOTU:
                // Transform to Medinuio when TEOTU reaches 20% health
                if (healthPercent <= 0.20) {
                    sendChatMessage("§6[AI Torque] §5§lEven The End is not enough!");
                    sendChatMessage("§6[AI Torque] §5§lWitness my final evolution!");
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        transformToMedinuioAura();
                    }, 60L); // 3 second delay
                }
                break;

            case MEDINUIO_AURA:
                // Final form - no further transformations
                // Instead, become more aggressive
                if (healthPercent <= 0.10) {
                    maximumAggression();
                }
                break;
        }
    }

    public void unleashAllAbilities() {
        attemptGrabNearbyObjects(60);
        grabNearbySouls(50);
        createMassiveShockwave(30);
        createDebrisStorm();
        createWindBlast(25);
        useTractorBeam(50);
    }

    public void heal(double amount) {
        health = Math.min(health + amount, maxHealth);
        bukkitEntity.setHealth(health);
    }

    public void addPower(double amount) { power += amount; }
    public void setHealth(double h) { this.health = h; bukkitEntity.setHealth(h); }
    public boolean hasForcefield() { return hasForcefield; }
    public void setForcefield(boolean value) { hasForcefield = value; }
    public void setForcefieldStrength(double value) { forcefieldStrength = value; }

    // Ability system getters
    public InfectionAbility getInfectionSystem() { return infectionSystem; }
    public CloningAbility getCloningSystem() { return cloningSystem; }
    public TypeSystemAbility getTypeSystem() { return typeSystem; }
    public HealingTowerAbility getHealingTowerSystem() { return healingTowerSystem; }
    public StatueAbility getStatueSystem() { return statueSystem; }
    public List<Entity> getOrbitingObjects() { return orbitingObjects; }
    public LivingEntity getBukkitEntity() { return bukkitEntity; }
    public Location getLocation() { return location; }

    // Getters and setters
    public UUID getUniqueId() { return uniqueId; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public int getCurrentPhase() { return phaseManager != null ? phaseManager.getCurrentPhaseNumber() : 1; }
    public String getCurrentForm() { return currentForm.name(); }
    public long getTickCounter() { return tickCounter; }
    public AITorquePlugin getPlugin() { return plugin; }
    public void setPhase(int phase) { if (phaseManager != null) phaseManager.setPhase(phase); }
    public boolean isHostile() { return isHostile; }
    public void setHostile(boolean hostile) { this.isHostile = hostile; }

    // ========================================
    // BLOCK CONSUMPTION & GROWTH SYSTEM
    // ========================================

    private double consumedBlockPower = 0;
    private double sizeMultiplier = 1.0;

    // Dark Matter System
    private double darkMatterEnergy = 0;
    private double gravitationalPullRadius = 30.0;
    private boolean isGravitationalPullActive = true;

    /**
     * Consume nearby blocks to grow larger and more powerful
     */
    public void consumeNearbyBlocks() {
        Location loc = bukkitEntity.getLocation();
        // Phase-based block consumption: Phase 1 = 1 block, Phase 1000 = 100 blocks
        int currentPhase = getCurrentPhase();
        int radius = Math.max(1, Math.min((int)(currentPhase * 0.1), 100));
        int consumed = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (consumed >= 20) break; // Limit per tick

                    org.bukkit.block.Block block = loc.clone().add(x, y, z).getBlock();

                    // Don't consume air, bedrock, or special blocks
                    if (block.getType() == Material.AIR ||
                        block.getType() == Material.BEDROCK ||
                        block.getType() == Material.END_PORTAL_FRAME) {
                        continue;
                    }

                    // Consume the block
                    Material blockType = block.getType();
                    block.setType(Material.AIR);
                    consumed++;

                    // Gain power based on block value
                    double blockValue = getBlockValue(blockType);
                    consumedBlockPower += blockValue;
                    power += blockValue * 100;
                    darkMatterEnergy += blockValue * 2; // Dark matter from consumed blocks

                    // Particle effect
                    loc.getWorld().spawnParticle(Particle.BLOCK, block.getLocation(), 10,
                        0.5, 0.5, 0.5, 0.1, block.getBlockData());

                    // Grow larger every 1000 power
                    if (consumedBlockPower > 1000 && sizeMultiplier < 10.0) {
                        growLarger();
                        consumedBlockPower = 0;
                    }
                }
            }
        }

        if (consumed > 0) {
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EAT, 2.0f, 0.5f);
        }
    }

    /**
     * Get power value of consumed block
     */
    private double getBlockValue(Material material) {
        switch (material) {
            case DIAMOND_BLOCK: return 100;
            case NETHERITE_BLOCK: return 200;
            case EMERALD_BLOCK: return 80;
            case GOLD_BLOCK: return 50;
            case IRON_BLOCK: return 30;
            case OBSIDIAN: return 40;
            case BEDROCK: return 1000; // Should never happen
            case STONE: return 1;
            case DIRT: return 0.5;
            default: return 2;
        }
    }

    /**
     * Grow larger in size
     */
    private void growLarger() {
        sizeMultiplier += 0.2;

        // Visual growth effect
        Location loc = bukkitEntity.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 20, 2, 2, 2, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.8f);

        sendChatMessage("§6[AI Torque] §c§lI GROW STRONGER! Size: §4" +
            String.format("%.1fx", sizeMultiplier));

        // Stat boost
        maxHealth += 100;
        health += 100;
        bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.min(maxHealth, 2048));
    }

    // ========================================
    // HP DRAIN SYSTEM
    // ========================================

    /**
     * Drain HP from all nearby entities
     */
    public void drainNearbyHealth() {
        if (!plugin.getConfigManager().isHealthDrainEnabled()) return;

        Location loc = bukkitEntity.getLocation();
        double range = 16.0; // Default drain range
        double drainRate = 0.5; // Default drain rate per second

        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, range, range, range);

        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                LivingEntity living = (LivingEntity) entity;

                // Drain health
                double drain = Math.min(drainRate, living.getHealth());
                living.damage(drain, bukkitEntity);

                // Heal self and gain dark matter
                heal(drain * 0.5);
                darkMatterEnergy += drain * 5; // Dark matter from drained health

                // Visual effect - red beam
                drawBeam(living.getEyeLocation(), bukkitEntity.getEyeLocation(), Particle.DUST,
                    new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));

                // Sound
                if (entity instanceof Player) {
                    ((Player) entity).playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 0.5f);
                }
            }
        }
    }

    /**
     * Draw a particle beam between two locations
     */
    private void drawBeam(Location start, Location end, Particle particle, Particle.DustOptions options) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        for (double d = 0; d < distance; d += 0.3) {
            Location point = start.clone().add(direction.clone().multiply(d));
            start.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0, options);
        }
    }

    // ========================================
    // ULTIMATE LIFE FORM SYSTEM
    // ========================================

    /**
     * Main ultimate life form system - constantly consumes everything
     */
    private void startUltimateLifeForm() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // CONSUME EVERYTHING NEARBY - this is what the ultimate life form does!
                if (tickCounter % 20 == 0) { // Every second
                    consumeEverythingNearby();
                }

                // Update size based on consumption
                if (tickCounter % 100 == 0) { // Every 5 seconds
                    updateSizeScaling();
                }

                // Check if it can split based on size/power
                if (tickCounter % 200 == 0) { // Every 10 seconds
                    considerSplitting();
                }

                // Attempt to remerge with body parts
                if (isSplit && tickCounter % 40 == 0) { // Every 2 seconds
                    attemptRemerge();
                }

                // Update visual effects based on evolution
                if (tickCounter % 10 == 0) {
                    updateEvolutionVisuals();
                }

                // Update speed based on evolution
                updateSpeedMultiplier();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Consume EVERYTHING nearby - mobs, items, blocks, entities
     * Phase-based scaling: Phase 1 = 1 block, Phase 1000+ = entire villages (100+ blocks)
     */
    private void consumeEverythingNearby() {
        Location loc = bukkitEntity.getLocation();

        // Phase-based radius scaling: starts at 1 block, scales to 100+ blocks at phase 1000
        int currentPhase = getCurrentPhase();
        double consumeRadius = Math.min(1.0 + (currentPhase * 0.1), 150.0); // Cap at 150 blocks

        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, consumeRadius, consumeRadius, consumeRadius);

        int consumed = 0;
        for (Entity entity : nearby) {
            if (entity.equals(bukkitEntity)) continue;
            if (consumed >= 3) break; // Limit per tick to avoid lag

            // Consume mobs
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;

                // Check if it's another AI Torque (clone) - consume it!
                String name = living.getCustomName();
                if (name != null && name.contains("AI TORQUE") && !entity.equals(bukkitEntity)) {
                    consumeClone(living);
                    consumed++;
                    continue;
                }

                // Consume regular mobs (not players unless hostile)
                if (living instanceof Monster || living instanceof Animals || living instanceof WaterMob) {
                    consumeMob(living);
                    consumed++;
                    continue;
                }

                // Consume players ONLY when hostile (never consume creative mode players)
                if (isHostile && living instanceof Player) {
                    Player player = (Player) living;

                    // NEVER consume creative mode players (they can still be attacked, but not consumed)
                    if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
                        continue;
                    }

                    // Only drain their HP in survival/adventure mode, don't fully consume them
                    double distance = player.getLocation().distance(bukkitEntity.getLocation());
                    if (distance < 5.0) {
                        double drain = 1.0 + (evolutionStage * 0.1);
                        double currentHP = player.getHealth();
                        if (currentHP > drain) {
                            player.setHealth(currentHP - drain);
                            consumptionEnergy += drain * 5;
                            health = Math.min(health + drain, maxHealth);
                        }
                    }
                }
            }

            // Consume dropped items
            if (entity instanceof Item) {
                consumeItem((Item) entity);
                consumed++;
                continue;
            }

            // Consume body parts that split off
            if (entity instanceof ArmorStand && bodyParts.contains(entity)) {
                consumeBodyPart((ArmorStand) entity);
                consumed++;
            }
        }
    }

    /**
     * Consume a mob and gain its power
     */
    private void consumeMob(LivingEntity mob) {
        double mass = mob.getMaxHealth();
        consumptionEnergy += mass * 10; // Restore energy
        totalConsumed += mass;
        lifePower += mass * 50;
        power += mass * 50;
        darkMatterEnergy += mass * 5;

        // Track mob types
        EntityType mobType = mob.getType();
        consumedMobTypes.put(mobType, consumedMobTypes.getOrDefault(mobType, 0) + 1);

        // Visual effect
        mob.getWorld().spawnParticle(Particle.SOUL, mob.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
        mob.getWorld().spawnParticle(Particle.EXPLOSION, mob.getLocation(), 5, 0.3, 0.3, 0.3, 0);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.7f);

        mob.remove();

        if (tickCounter % 60 == 0) { // Don't spam messages
            sendChatMessage("§5§l[ULTIMATE LIFE FORM] §dCONSUMED " + mobType.name() + "! Total: §c" + (int)totalConsumed);
        }
    }

    /**
     * Consume a clone and gain massive power
     */
    private void consumeClone(LivingEntity clone) {
        double cloneMass = clone.getMaxHealth() * 5; // Clones are worth MORE
        consumptionEnergy += cloneMass * 20;
        totalConsumed += cloneMass;
        lifePower += cloneMass * 100;
        power += cloneMass * 100;
        maxHealth += 500; // Permanent health increase

        // Epic visual effect
        clone.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, clone.getLocation(), 30, 2, 2, 2, 0.5);
        clone.getWorld().spawnParticle(Particle.DRAGON_BREATH, clone.getLocation(), 100, 1, 1, 1, 0.3);
        clone.getWorld().playSound(clone.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 3.0f, 0.5f);

        sendChatMessage("§4§l[ULTIMATE LIFE FORM] §5§lCONSUMED CLONE - MASSIVE POWER GAIN!");

        clone.remove();
    }

    /**
     * Consume a dropped item
     */
    private void consumeItem(Item item) {
        double itemMass = item.getItemStack().getAmount() * 0.1;
        consumptionEnergy += itemMass;
        totalConsumed += itemMass;
        lifePower += itemMass;

        // Visual effect
        item.getWorld().spawnParticle(Particle.CRIT, item.getLocation(), 10, 0.2, 0.2, 0.2, 0);
        item.remove();
    }

    /**
     * Consume own body part that split off
     */
    private void consumeBodyPart(ArmorStand bodyPart) {
        consumptionEnergy += 50; // Reabsorbing own mass
        lifePower += 25;
        bodyParts.remove(bodyPart);

        bodyPart.getWorld().spawnParticle(Particle.PORTAL, bodyPart.getLocation(), 20, 0.5, 0.5, 0.5, 0.2);
        bodyPart.remove();

        if (bodyParts.isEmpty()) {
            isSplit = false;
            sendChatMessage("§5§l[ULTIMATE LIFE FORM] §dREMERGED - WHOLENESS RESTORED!");
        }
    }

    /**
     * Update size scaling based on total consumed
     */
    private void updateSizeScaling() {
        // Size grows logarithmically with consumption
        double targetSize = 1.0 + Math.log10(Math.max(1, totalConsumed / 100.0));
        targetSize = Math.min(targetSize, maxSize); // Cap at maxSize

        if (targetSize > currentSize) {
            currentSize = targetSize;

            // Update entity attributes based on size
            AttributeInstance maxHealthAttr = bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                double newMaxHealth = 20.0 + (currentSize * 100.0);
                maxHealthAttr.setBaseValue(newMaxHealth);
                maxHealth = newMaxHealth;
                health = Math.min(health, maxHealth);
            }

            // Visual growth effect
            if (currentSize > 1.5 && tickCounter % 500 == 0) {
                sendChatMessage("§4§l[ULTIMATE LIFE FORM] §cSIZE: §e" + String.format("%.1f", currentSize) + "x §c- GROWING LARGER!");
            }
        }
    }

    /**
     * Consider splitting into multiple body parts
     */
    private void considerSplitting() {
        if (isSplit) return; // Already split
        if (currentSize < 3.0) return; // Not big enough
        if (consumptionEnergy < 500) return; // Not enough energy

        // Split into 3-5 body parts
        int numParts = 3 + (int)(Math.random() * 3);

        for (int i = 0; i < numParts; i++) {
            Location partLoc = bukkitEntity.getLocation().clone().add(
                (Math.random() - 0.5) * 5,
                Math.random() * 2,
                (Math.random() - 0.5) * 5
            );

            ArmorStand bodyPart = (ArmorStand) partLoc.getWorld().spawnEntity(partLoc, EntityType.ARMOR_STAND);
            bodyPart.setCustomName("§5§l[ULTIMATE LIFE FORM PART]");
            bodyPart.setCustomNameVisible(true);
            bodyPart.setGravity(false);
            bodyPart.setInvulnerable(false);
            bodyPart.setVisible(true);

            bodyParts.add(bodyPart);
        }

        isSplit = true;
        sendChatMessage("§5§l[ULTIMATE LIFE FORM] §d§lSPLITTING - MULTI-POINT CONSUMPTION!");
    }

    /**
     * Attempt to remerge split body parts
     */
    private void attemptRemerge() {
        if (bodyParts.isEmpty()) {
            isSplit = false;
            return;
        }

        Location mainLoc = bukkitEntity.getLocation();

        // Pull body parts toward main entity
        for (ArmorStand part : new ArrayList<>(bodyParts)) {
            if (part == null || part.isDead()) {
                bodyParts.remove(part);
                continue;
            }

            double distance = part.getLocation().distance(mainLoc);

            if (distance < 2.0) {
                // Close enough - remerge!
                consumeBodyPart(part);
            } else {
                // Pull it closer
                Vector pull = mainLoc.toVector().subtract(part.getLocation().toVector());
                pull.normalize().multiply(0.3);
                part.teleport(part.getLocation().add(pull));

                // Visual effect
                part.getWorld().spawnParticle(Particle.PORTAL, part.getLocation(), 5, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    /**
     * Update speed multiplier based on evolution
     */
    private void updateSpeedMultiplier() {
        // Speed increases with evolution stage, but decreases slightly with size
        double baseSpeed = 1.0 + (evolutionStage * 0.1);
        double sizePenalty = Math.max(0.7, 1.0 - (currentSize * 0.05));
        speedMultiplier = baseSpeed * sizePenalty;

        // Apply speed potion effect
        int speedLevel = Math.max(0, (int)(speedMultiplier * 2) - 1);
        if (speedLevel > 0) {
            bukkitEntity.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, 100, Math.min(speedLevel, 10), false, false
            ));
        }
    }

    /**
     * Update visual effects based on evolution stage
     */
    private void updateEvolutionVisuals() {
        if (evolutionStage == 0) return;

        Location loc = bukkitEntity.getLocation().clone().add(0, 1, 0);

        // Different particles per evolution stage
        if (evolutionStage >= 1) {
            loc.getWorld().spawnParticle(Particle.CRIT, loc, 2, 0.3, 0.5, 0.3, 0);
        }
        if (evolutionStage >= 3) {
            loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 3, 0.5, 0.5, 0.5, 0);
        }
        if (evolutionStage >= 5) {
            loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 1, 0.3, 0.3, 0.3, 0.01);
        }
        if (evolutionStage >= 10) {
            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 2, 0.4, 0.4, 0.4, 0.02);
        }
    }

    // ========================================
    // ENERGY HUNGER SYSTEM
    // ========================================

    /**
     * Energy hunger - must constantly consume to survive!
     * Energy drains over time. Low energy = weakness. Zero energy = death approaches.
     */
    private void startEnergyHunger() {
        new BukkitRunnable() {
            private int warningCooldown = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Drain energy every second
                consumptionEnergy -= energyDrainRate;

                // Energy drain increases with size (bigger = hungrier)
                double sizeDrain = currentSize * 0.1;
                consumptionEnergy -= sizeDrain;

                // Minimum energy = 0
                consumptionEnergy = Math.max(0, consumptionEnergy);

                // Calculate energy percentage
                double energyPercent = consumptionEnergy / 1000.0;

                // === ENERGY EFFECTS ===

                if (consumptionEnergy <= 0) {
                    // AUTO ENERGY REFILL - The Ultimate Life Form adapts!
                    consumptionEnergy = 500; // Refill to half capacity

                    sendChatMessage("§5§l[ULTIMATE LIFE FORM] §d§lADAPTING - EMERGENCY ENERGY RESTORATION!");
                    sendChatMessage("§6§lThe Ultimate Life Form cannot be stopped by mere hunger!");

                    Location loc = bukkitEntity.getLocation();
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 20, 2, 2, 2, 0.3);
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 1, 1, 1, 0.2);
                    loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 1.5f);

                    // Brief speed boost from emergency restoration
                    bukkitEntity.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED, 100, 3, false, false
                    ));

                } else if (energyPercent < 0.2) {
                    // VERY LOW ENERGY - Desperate!
                    if (warningCooldown <= 0) {
                        sendChatMessage("§e§l[ULTIMATE LIFE FORM] §6HUNGER CRITICAL - ENERGY AT " +
                            String.format("%.0f", energyPercent * 100) + "%!");
                        warningCooldown = 100; // 5 second cooldown
                    }

                    // Hunger particles
                    if (tickCounter % 20 == 0) {
                        Location loc = bukkitEntity.getLocation().clone().add(0, 1.5, 0);
                        loc.getWorld().spawnParticle(Particle.SMOKE, loc, 5, 0.3, 0.3, 0.3, 0.02);
                    }

                } else if (energyPercent < 0.5) {
                    // LOW ENERGY - Need food soon
                    if (warningCooldown <= 0 && tickCounter % 200 == 0) {
                        sendChatMessage("§e§l[ULTIMATE LIFE FORM] §6Hunger building... Energy: " +
                            String.format("%.0f", energyPercent * 100) + "%");
                        warningCooldown = 100;
                    }
                }

                // Apply strength debuff based on energy level
                if (energyPercent < 0.5) {
                    strengthMultiplier = 0.5 + (energyPercent); // 50-100% strength
                } else {
                    strengthMultiplier = 1.0 + (evolutionStage * 0.15); // Full strength + evolution bonus
                }

                // Update custom name to show energy level
                if (tickCounter % 40 == 0) {
                    String energyBar = getEnergyBar(energyPercent);
                    String stageStar = evolutionStage > 0 ? "§6" + "★".repeat(Math.min(evolutionStage, 10)) + " " : "";
                    bukkitEntity.setCustomName(stageStar + "§5§l✦ §4§lAI TORQUE §5§l[ULTIMATE] " + energyBar);
                }

                warningCooldown--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    /**
     * Generate visual energy bar
     */
    private String getEnergyBar(double percent) {
        int bars = (int)(percent * 10);
        String filled = "§a▮".repeat(Math.max(0, bars));
        String empty = "§8▯".repeat(Math.max(0, 10 - bars));
        return "§7[" + filled + empty + "§7]";
    }

    // ========================================
    // CONTINUOUS EVOLUTION SYSTEM
    // ========================================

    /**
     * Continuous evolution - gets stronger, faster, more powerful over time
     */
    private void startContinuousEvolution() {
        new BukkitRunnable() {
            private int lastStage = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Calculate evolution stage based on total consumed
                // Stage 1 = 500 mass, Stage 2 = 1500, Stage 3 = 3000, etc. (exponential)
                int newStage = (int)Math.sqrt(totalConsumed / 100.0);
                newStage = Math.min(newStage, 50); // Cap at stage 50

                if (newStage > lastStage) {
                    // EVOLUTION LEVEL UP!
                    evolutionStage = newStage;
                    lastStage = newStage;

                    // Announce evolution
                    sendChatMessage("§5§l§k|||§r §4§l[EVOLUTION LEVEL UP!] §5§l§k|||");
                    sendChatMessage("§d§lSTAGE §e§l" + evolutionStage + " §d§l- ULTIMATE LIFE FORM GROWS STRONGER!");

                    // Epic visual effect
                    Location loc = bukkitEntity.getLocation();
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 30, 3, 3, 3, 0.5);
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc, 100, 2, 2, 2, 0.5);
                    loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50, 1.5, 1.5, 1.5, 0.3);
                    loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.5f);
                    loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 1.5f);

                    // STAT INCREASES
                    applyEvolutionBonuses(evolutionStage);

                    // Unlock new abilities at certain stages
                    unlockEvolutionAbilities(evolutionStage);
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Check every 5 seconds
    }

    /**
     * Apply stat bonuses for evolution stage
     */
    private void applyEvolutionBonuses(int stage) {
        // Increase max health
        double healthBonus = 50.0 * stage;
        AttributeInstance maxHealthAttr = bukkitEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(Math.max(maxHealthAttr.getBaseValue(), 20.0 + healthBonus));
            maxHealth = maxHealthAttr.getBaseValue();
            health = Math.min(health + healthBonus, maxHealth);
            bukkitEntity.setHealth(health);
        }

        // Increase damage (strength multiplier)
        strengthMultiplier = 1.0 + (stage * 0.15);

        // Increase speed
        speedMultiplier = 1.0 + (stage * 0.1);

        // Increase gravitational pull
        gravitationalPullRadius = 30.0 + (stage * 2.0);
        isGravitationalPullActive = true;

        // Increase dark matter energy
        darkMatterEnergy += stage * 100;

        sendChatMessage("§d§lBONUSES: §c+" + (int)healthBonus + " HP §7| §e+" +
            String.format("%.0f", (strengthMultiplier - 1.0) * 100) + "% DMG §7| §b+" +
            String.format("%.0f", (speedMultiplier - 1.0) * 100) + "% SPD");
    }

    /**
     * Unlock special abilities at evolution milestones
     */
    private void unlockEvolutionAbilities(int stage) {
        switch (stage) {
            case 1:
                sendChatMessage("§5§lUNLOCKED: §dEnhanced Consumption Range");
                break;

            case 3:
                sendChatMessage("§5§lUNLOCKED: §dBody Splitting Ability");
                break;

            case 5:
                sendChatMessage("§5§lUNLOCKED: §dGravitational Mastery");
                gravitationalPullRadius *= 1.5;
                break;

            case 7:
                sendChatMessage("§5§lUNLOCKED: §dDark Matter Manipulation");
                break;

            case 10:
                sendChatMessage("§4§l§k|||§r §5§lASCENSION STAGE §4§l- §5§lTRANSCENDENT FORM §4§l§k|||");
                sendChatMessage("§d§lThe Ultimate Life Form has surpassed mortal limits!");
                maxSize = 200.0; // Can grow even larger
                energyDrainRate *= 0.5; // Drain less energy (more efficient)
                break;

            case 20:
                sendChatMessage("§4§l§k|||§r §5§lGODHOOD ACHIEVED §4§l§k|||");
                sendChatMessage("§d§lThe Ultimate Life Form has become a force of nature!");
                break;
        }
    }

    // ========================================
    // GRAVITATIONAL PULL & DARK MATTER SYSTEM
    // ========================================

    /**
     * Gravitational pull - pull everything toward AI Torque
     * Items, blocks, entities - everything gets consumed
     */
    public void gravitationalPull() {
        if (!isGravitationalPullActive) return;

        Location loc = bukkitEntity.getLocation();
        double pullRadius = gravitationalPullRadius + (darkMatterEnergy / 1000.0);
        double pullStrength = 0.3 + (darkMatterEnergy / 5000.0);

        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, pullRadius, pullRadius, pullRadius);

        for (Entity entity : nearby) {
            if (entity.equals(bukkitEntity)) continue;

            // Calculate pull vector toward AI Torque
            Vector pull = loc.toVector().subtract(entity.getLocation().toVector());
            double distance = pull.length();

            if (distance < 0.5) {
                // Entity reached AI Torque - consume it!
                consumeEntity(entity);
                continue;
            }

            pull.normalize().multiply(pullStrength);

            // Apply pull force
            entity.setVelocity(entity.getVelocity().add(pull));

            // Visual effect - purple/dark particles
            if (tickCounter % 5 == 0) {
                Location particleLoc = entity.getLocation().clone().add(0, entity.getHeight() / 2, 0);
                loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(128, 0, 128), 1.5f));

                // Draw line to AI Torque
                drawBeam(particleLoc, loc.clone().add(0, 1, 0), Particle.DUST,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(64, 0, 128), 0.8f));
            }
        }

        // Visual aura effect
        if (tickCounter % 10 == 0) {
            for (int i = 0; i < 20; i++) {
                double angle = (i * 18) * Math.PI / 180;
                double radius = pullRadius * 0.8;
                Location particleLoc = loc.clone().add(
                    Math.cos(angle) * radius,
                    Math.sin(tickCounter * 0.05) * 3,
                    Math.sin(angle) * radius
                );
                loc.getWorld().spawnParticle(Particle.ENCHANT, particleLoc, 1, 0, 0, 0, 0);
            }
        }

        // Sound effect
        if (tickCounter % 60 == 0) {
            loc.getWorld().playSound(loc, Sound.BLOCK_PORTAL_AMBIENT, 2.0f, 0.5f);
        }
    }

    /**
     * Consume an entity that has been pulled into AI Torque
     */
    private void consumeEntity(Entity entity) {
        Location loc = entity.getLocation();

        // Massive explosion effect
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 10, 0.5, 0.5, 0.5, 0);
        loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 30, 0.3, 0.3, 0.3, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);

        if (entity instanceof org.bukkit.entity.Item) {
            // Consumed item - gain power based on item
            org.bukkit.entity.Item item = (org.bukkit.entity.Item) entity;
            org.bukkit.inventory.ItemStack stack = item.getItemStack();

            double itemPower = getItemPower(stack);
            power += itemPower;
            darkMatterEnergy += itemPower / 10.0;

            entity.remove();

        } else if (entity instanceof org.bukkit.entity.FallingBlock) {
            // Consumed falling block (giant rock)
            org.bukkit.entity.FallingBlock fallingBlock = (org.bukkit.entity.FallingBlock) entity;
            Material blockMaterial = fallingBlock.getBlockData().getMaterial();

            double blockPower = getBlockValue(blockMaterial) * 50; // Falling blocks worth more
            power += blockPower;
            darkMatterEnergy += blockPower / 5.0;
            consumedBlockPower += getBlockValue(blockMaterial);

            entity.remove();

            sendChatMessage("§6[AI Torque] §5Consumed giant rock! Dark Matter: §d" +
                String.format("%.0f", darkMatterEnergy));

        } else if (entity instanceof LivingEntity) {
            // Consumed living entity - massive power gain
            LivingEntity living = (LivingEntity) entity;
            double entityPower = living.getMaxHealth() * 100;

            power += entityPower;
            darkMatterEnergy += entityPower / 2.0;
            health = Math.min(health + living.getHealth(), maxHealth);

            living.damage(1000000, bukkitEntity); // Instant kill

            sendChatMessage("§6[AI Torque] §4§lCONSUMED " +
                entity.getType().name() + "! Dark Matter: §d" +
                String.format("%.0f", darkMatterEnergy));

        } else {
            // Generic entity
            entity.remove();
            power += 50;
            darkMatterEnergy += 5;
        }
    }

    /**
     * Get power value of an item
     */
    private double getItemPower(org.bukkit.inventory.ItemStack stack) {
        Material material = stack.getType();
        int amount = stack.getAmount();

        double basePower;
        switch (material) {
            case NETHERITE_INGOT: basePower = 500; break;
            case DIAMOND: basePower = 200; break;
            case EMERALD: basePower = 150; break;
            case GOLD_INGOT: basePower = 100; break;
            case IRON_INGOT: basePower = 50; break;
            case NETHER_STAR: basePower = 5000; break;
            case DRAGON_EGG: basePower = 10000; break;
            case ELYTRA: basePower = 3000; break;
            case TOTEM_OF_UNDYING: basePower = 2000; break;
            case ENCHANTED_GOLDEN_APPLE: basePower = 1000; break;
            default:
                // Check if it's equipment
                if (material.name().contains("NETHERITE")) basePower = 800;
                else if (material.name().contains("DIAMOND")) basePower = 400;
                else if (material.name().contains("GOLD")) basePower = 150;
                else if (material.name().contains("IRON")) basePower = 80;
                else basePower = 10;
        }

        return basePower * amount;
    }

    // ========================================
    // NEW ATTACK POWERS (50+ ABILITIES)
    // ========================================

    /** 1. Meteor Storm */
    public void meteorStorm() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 20; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location meteorSpawn = loc.clone().add(
                    (Math.random() - 0.5) * 40,
                    50,
                    (Math.random() - 0.5) * 40
                );
                spawnMeteor(meteorSpawn);
            }, i * 5L);
        }
    }

    private void spawnMeteor(Location start) {
        new BukkitRunnable() {
            Location current = start.clone();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 100 || current.getY() <= start.getWorld().getHighestBlockYAt(current)) {
                    // Impact
                    current.getWorld().createExplosion(current, 4.0f, false, true);
                    current.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, current, 20);
                    cancel();
                    return;
                }

                current.add(0, -1, 0);
                current.getWorld().spawnParticle(Particle.FLAME, current, 30, 0.5, 0.5, 0.5, 0.1);
                current.getWorld().spawnParticle(Particle.LAVA, current, 10);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** 2. Black Hole */
    public void createBlackHole(Location center) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 100) {
                    cancel();
                    return;
                }

                // Pull entities toward center
                Collection<Entity> nearby = center.getWorld().getNearbyEntities(center, 20, 20, 20);
                for (Entity entity : nearby) {
                    if (entity.equals(bukkitEntity)) continue;

                    Vector pull = center.toVector().subtract(entity.getLocation().toVector());
                    pull.normalize().multiply(0.5);
                    entity.setVelocity(entity.getVelocity().add(pull));

                    if (entity instanceof LivingEntity && entity.getLocation().distance(center) < 2) {
                        ((LivingEntity) entity).damage(10.0, bukkitEntity);
                    }
                }

                // Visual effect
                for (int i = 0; i < 20; i++) {
                    double angle = (ticks * 0.2 + i * 18) * Math.PI / 180;
                    double radius = 10 - (ticks * 0.05);
                    Location particleLoc = center.clone().add(
                        Math.cos(angle) * radius,
                        Math.sin(ticks * 0.1) * 3,
                        Math.sin(angle) * radius
                    );
                    center.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 5, 0, 0, 0, 0.5);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** 4. Time Freeze */
    public void timeFreezeNearby() {
        Collection<Entity> nearby = bukkitEntity.getWorld().getNearbyEntities(
            bukkitEntity.getLocation(), 20, 20, 20);

        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, 100, 10));
                ((LivingEntity) entity).addPotionEffect(
                    new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 10));
            }
        }

        sendChatMessage("§6[AI Torque] §b§lTIME STANDS STILL!");
    }

    /** 5. Soul Steal */
    public void stealSouls() {
        Collection<Entity> nearby = bukkitEntity.getWorld().getNearbyEntities(
            bukkitEntity.getLocation(), 25, 25, 25);

        for (Entity entity : nearby) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.damage(5.0, bukkitEntity);
                heal(5.0);

                // Soul particle trail
                drawBeam(player.getEyeLocation(), bukkitEntity.getEyeLocation(),
                    Particle.SOUL, null);
            }
        }
    }

    /** 6-10. Elemental Attacks */
    public void fireNova() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 360; i += 10) {
            double angle = i * Math.PI / 180;
            Vector direction = new Vector(Math.cos(angle), 0.2, Math.sin(angle));
            launchFireball(loc, direction);
        }
    }

    private void launchFireball(Location start, Vector direction) {
        org.bukkit.entity.Fireball fireball = start.getWorld().spawn(start, org.bukkit.entity.Fireball.class);
        fireball.setDirection(direction);
        fireball.setYield(3.0f);
        fireball.setShooter(bukkitEntity);
    }

    public void iceAge() {
        Location loc = bukkitEntity.getLocation();
        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {
                Location iceLoc = loc.clone().add(x, 0, z);
                org.bukkit.block.Block block = iceLoc.getWorld().getHighestBlockAt(iceLoc).getRelative(0, 1, 0);
                if (block.getType() == Material.AIR) {
                    block.setType(Material.ICE);
                }
            }
        }

        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 15, 15, 15);
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, 200, 5));
                entity.setFreezeTicks(200);
            }
        }
    }

    /** 11-20. More devastating attacks */
    public void voidRift() {
        Location loc = bukkitEntity.getLocation();
        createBlackHole(loc);

        // Also damage everyone nearby
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 30, 30, 30);
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(20.0, bukkitEntity);
            }
        }
    }

    public void cosmicBeam(Player target) {
        Location start = bukkitEntity.getEyeLocation();
        Location end = target.getEyeLocation();

        drawBeam(start, end, Particle.END_ROD, null);
        target.damage(30.0, bukkitEntity);
        target.setVelocity(new Vector(0, 2, 0));

        start.getWorld().playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 1.0f);
    }

    public void summonMinions() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 5; i++) {
            Location spawnLoc = loc.clone().add(
                (Math.random() - 0.5) * 10,
                0,
                (Math.random() - 0.5) * 10
            );
            org.bukkit.entity.Wither wither = loc.getWorld().spawn(spawnLoc, org.bukkit.entity.Wither.class);
            wither.setCustomName("§4AI Torque's Servant");
        }
    }

    public void earthquakeAttack() {
        Location loc = bukkitEntity.getLocation();

        for (int radius = 1; radius <= 20; radius++) {
            final int r = radius;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int angle = 0; angle < 360; angle += 20) {
                    double radian = angle * Math.PI / 180;
                    Location effectLoc = loc.clone().add(
                        Math.cos(radian) * r,
                        0,
                        Math.sin(radian) * r
                    );

                    effectLoc.getWorld().spawnParticle(Particle.BLOCK, effectLoc, 20,
                        1, 1, 1, 0.1, Material.STONE.createBlockData());
                }
            }, r);
        }

        // Damage and launch players
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 20, 20, 20);
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(15.0, bukkitEntity);
                entity.setVelocity(new Vector(
                    (Math.random() - 0.5) * 2,
                    1.5,
                    (Math.random() - 0.5) * 2
                ));
            }
        }
    }

    // ========================================
    // ULTIMATE DEVASTATING POWERS (30-59)
    // ========================================

    /** 30. Plasma Storm - Superheated plasma devastation */
    public void plasmaStorm() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 50; i++) {
            Location plasmaBolt = loc.clone().add(
                (Math.random() - 0.5) * 30, Math.random() * 20, (Math.random() - 0.5) * 30
            );
            plasmaBolt.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, plasmaBolt, 30, 1, 1, 1, 0.3);
            plasmaBolt.getWorld().createExplosion(plasmaBolt, 3.0f, false, false);
        }
    }

    /** 31. Dimensional Rift - Tear between dimensions */
    public void dimensionalRift() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 100; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double angle = tick * 15 * Math.PI / 180;
                Location riftLoc = loc.clone().add(Math.cos(angle) * 15, Math.sin(tick * 0.2) * 5, Math.sin(angle) * 15);
                riftLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, riftLoc, 20, 0.5, 0.5, 0.5, 0.5);
                Collection<Entity> nearby = riftLoc.getWorld().getNearbyEntities(riftLoc, 3, 3, 3);
                for (Entity entity : nearby) {
                    if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                        ((LivingEntity) entity).damage(5.0, bukkitEntity);
                        entity.teleport(loc);
                    }
                }
            }, tick);
        }
    }

    /** 32. Quantum Destabilization - Reality becomes unstable */
    public void quantumDestabilization() {
        Collection<Entity> entities = bukkitEntity.getWorld().getNearbyEntities(bukkitEntity.getLocation(), 40, 40, 40);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 4));
                living.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 2));
                living.damage(12.0, bukkitEntity);
            }
        }
    }

    /** 33. Nuclear Fission - Atomic power unleashed */
    public void nuclearFission() {
        Location loc = bukkitEntity.getLocation();
        loc.getWorld().createExplosion(loc, 8.0f, false, true);
        for (int i = 0; i < 100; i++) {
            Location radiation = loc.clone().add(
                (Math.random() - 0.5) * 25, (Math.random() - 0.5) * 25, (Math.random() - 0.5) * 25
            );
            radiation.getWorld().spawnParticle(Particle.GLOW, radiation, 50, 2, 2, 2, 0.1);
        }
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 25, 25, 25);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(40.0, bukkitEntity);
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 400, 3));
            }
        }
    }

    /** 34. Antimatter Explosion - Matter meets antimatter */
    public void antimatterExplosion() {
        Location loc = bukkitEntity.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 100, 10, 10, 10, 1);
        loc.getWorld().createExplosion(loc, 12.0f, false, true);
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 30, 30, 30);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(60.0, bukkitEntity);
                entity.setVelocity(new Vector(0, 5, 0));
            }
        }
    }

    /** 35. Celestial Judgment - Divine wrath from above */
    public void celestialJudgment() {
        Collection<Entity> entities = bukkitEntity.getWorld().getNearbyEntities(bukkitEntity.getLocation(), 50, 50, 50);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                Location target = entity.getLocation();
                for (int i = 0; i < 5; i++) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        target.getWorld().strikeLightning(target);
                        target.getWorld().spawnParticle(Particle.END_ROD, target, 100, 2, 10, 2, 0.5);
                    }, i * 10L);
                }
                ((LivingEntity) entity).damage(35.0, bukkitEntity);
            }
        }
    }

    /** 36. Singularity Collapse - Gravity collapses into singularity */
    public void singularityCollapse() {
        Location center = bukkitEntity.getLocation();
        for (int i = 0; i < 200; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Collection<Entity> entities = center.getWorld().getNearbyEntities(center, 50, 50, 50);
                for (Entity entity : entities) {
                    if (!entity.equals(bukkitEntity)) {
                        Vector pull = center.toVector().subtract(entity.getLocation().toVector());
                        pull.normalize().multiply(1.5);
                        entity.setVelocity(pull);
                    }
                }
                center.getWorld().spawnParticle(Particle.PORTAL, center, 100, 15, 15, 15, 3);
                if (tick == 199) {
                    center.getWorld().createExplosion(center, 15.0f, false, false);
                }
            }, tick);
        }
    }

    /** 37. Temporal Paradox - Time itself breaks */
    public void temporalParadox() {
        Collection<Entity> entities = bukkitEntity.getWorld().getNearbyEntities(bukkitEntity.getLocation(), 40, 40, 40);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 10));
                living.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 300, 10));
                living.damage(25.0, bukkitEntity);
                living.getWorld().spawnParticle(Particle.PORTAL, living.getLocation(), 100, 2, 2, 2, 1);
            }
        }
    }

    /** 38. Reality Break - Reality shatters */
    public void realityBreak() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 10; i++) {
            final int layer = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double radius = layer * 5.0;
                for (double angle = 0; angle < 360; angle += 10) {
                    double radian = angle * Math.PI / 180;
                    Location shatter = loc.clone().add(Math.cos(radian) * radius, layer * 2, Math.sin(radian) * radius);
                    shatter.getWorld().spawnParticle(Particle.CRIT, shatter, 10, 0.5, 0.5, 0.5, 0);
                    shatter.getWorld().createExplosion(shatter, 2.0f, false, false);
                }
            }, layer * 5L);
        }
    }

    /** 39. Entropy Wave - Universal decay accelerates */
    public void entropyWave() {
        Location loc = bukkitEntity.getLocation();
        for (int radius = 1; radius <= 50; radius++) {
            final int r = radius;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int angle = 0; angle < 360; angle += 15) {
                    double radian = angle * Math.PI / 180;
                    Location wave = loc.clone().add(Math.cos(radian) * r, 0, Math.sin(radian) * r);
                    wave.getWorld().spawnParticle(Particle.ASH, wave, 30, 1, 2, 1, 0.1);
                }
                Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, r, r, r);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                        ((LivingEntity) entity).damage(2.0, bukkitEntity);
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 2));
                    }
                }
            }, r);
        }
    }

    /** 40. Dark Matter Pulse - Unleash accumulated dark matter */
    public void darkMatterPulse() {
        Location loc = bukkitEntity.getLocation();
        double radius = 20 + (darkMatterEnergy / 100.0);
        loc.getWorld().spawnParticle(Particle.DUST, loc, 500, radius, radius, radius, 0,
            new Particle.DustOptions(org.bukkit.Color.fromRGB(0, 0, 50), 3.0f));
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                double damage = 30 + (darkMatterEnergy / 50.0);
                ((LivingEntity) entity).damage(damage, bukkitEntity);
                darkMatterEnergy += 10; // Gain more dark matter from hits
            }
        }
    }

    /** 41. Supernova Blast - Star explodes */
    public void supernovaBlast() {
        Location loc = bukkitEntity.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 200, 20, 20, 20, 2);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 3.0f, 0.5f);
        for (int i = 0; i < 100; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double radius = tick * 1.0;
                for (double angle = 0; angle < 360; angle += 10) {
                    double radian = angle * Math.PI / 180;
                    Location blast = loc.clone().add(Math.cos(radian) * radius, Math.sin(radian) * radius, Math.cos(radian + 90) * radius);
                    blast.getWorld().spawnParticle(Particle.FLAME, blast, 10, 0.5, 0.5, 0.5, 0.3);
                }
            }, tick);
        }
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 50, 50, 50);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(100.0, bukkitEntity);
                entity.setFireTicks(600);
            }
        }
    }

    /** 42. Quasar Beam - Beam from galactic core */
    public void quasarBeam() {
        Location start = bukkitEntity.getEyeLocation();
        Vector direction = bukkitEntity.getLocation().getDirection().normalize();
        for (int i = 0; i < 100; i++) {
            Location beam = start.clone().add(direction.clone().multiply(i));
            beam.getWorld().spawnParticle(Particle.END_ROD, beam, 20, 0.3, 0.3, 0.3, 0.1);
            beam.getWorld().spawnParticle(Particle.GLOW, beam, 10, 0.2, 0.2, 0.2, 0);
            Collection<Entity> hit = beam.getWorld().getNearbyEntities(beam, 2, 2, 2);
            for (Entity entity : hit) {
                if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                    ((LivingEntity) entity).damage(50.0, bukkitEntity);
                }
            }
        }
    }

    /** 43. Galaxy Collision - Two galaxies collide */
    public void galaxyCollision() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 200; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Galaxy 1
                double angle1 = tick * 18 * Math.PI / 180;
                Location star1 = loc.clone().add(Math.cos(angle1) * 20, Math.sin(tick * 0.1) * 10, Math.sin(angle1) * 20);
                star1.getWorld().spawnParticle(Particle.FIREWORK, star1, 20, 1, 1, 1, 0.1);

                // Galaxy 2
                double angle2 = -tick * 18 * Math.PI / 180;
                Location star2 = loc.clone().add(Math.cos(angle2) * 20, Math.sin(-tick * 0.1) * 10, Math.sin(angle2) * 20);
                star2.getWorld().spawnParticle(Particle.FIREWORK, star2, 20, 1, 1, 1, 0.1);

                if (tick % 20 == 0) {
                    loc.getWorld().createExplosion(loc, 5.0f, false, false);
                }
            }, tick);
        }
    }

    /** 44. Universal Heat Death - Everything reaches thermal equilibrium */
    public void universalHeat() {
        Collection<Entity> entities = bukkitEntity.getWorld().getNearbyEntities(bukkitEntity.getLocation(), 60, 60, 60);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                LivingEntity living = (LivingEntity) entity;
                living.setFireTicks(1000);
                living.damage(50.0, bukkitEntity);
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 10));
            }
        }
    }

    /** 45. Absolute Zero - Temperature drops to absolute zero */
    public void absoluteZero() {
        Location loc = bukkitEntity.getLocation();
        loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 1000, 30, 30, 30, 1);
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 40, 40, 40);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                LivingEntity living = (LivingEntity) entity;
                living.setFreezeTicks(600);
                living.damage(45.0, bukkitEntity);
                living.setVelocity(new Vector(0, 0, 0));
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 10));
            }
        }
    }

    /** 46. Neutron Star Crush - Gravitational pressure of neutron star */
    public void neutronStarCrush() {
        Location center = bukkitEntity.getLocation();
        Collection<Entity> entities = center.getWorld().getNearbyEntities(center, 50, 50, 50);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                Vector crush = new Vector(0, -10, 0);
                entity.setVelocity(crush);
                ((LivingEntity) entity).damage(80.0, bukkitEntity);
                entity.getWorld().spawnParticle(Particle.SQUID_INK, entity.getLocation(), 100, 1, 1, 1, 0.5);
            }
        }
    }

    /** 47. Photon Torrent - Stream of light particles */
    public void photonTorrent() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 100; i++) {
            Vector direction = new Vector(
                (Math.random() - 0.5) * 2,
                (Math.random() - 0.5) * 2,
                (Math.random() - 0.5) * 2
            ).normalize();
            for (int j = 0; j < 30; j++) {
                Location photon = loc.clone().add(direction.clone().multiply(j));
                photon.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, photon, 1, 0, 0, 0, 0);
                Collection<Entity> hit = photon.getWorld().getNearbyEntities(photon, 1, 1, 1);
                for (Entity entity : hit) {
                    if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                        ((LivingEntity) entity).damage(3.0, bukkitEntity);
                    }
                }
            }
        }
    }

    /** 48. Gamma Ray Burst - Deadliest phenomenon in universe */
    public void gammaRayBurst() {
        Location start = bukkitEntity.getEyeLocation();
        Vector direction = bukkitEntity.getLocation().getDirection();
        for (int i = 0; i < 200; i++) {
            Location ray = start.clone().add(direction.clone().multiply(i * 0.5));
            ray.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, ray, 50, 0.5, 0.5, 0.5, 0.5);
            Collection<Entity> hit = ray.getWorld().getNearbyEntities(ray, 3, 3, 3);
            for (Entity entity : hit) {
                if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                    ((LivingEntity) entity).damage(150.0, bukkitEntity);
                }
            }
        }
    }

    /** 49. Tesseract Prison - 4D prison */
    public void tesseractPrison() {
        Collection<Entity> entities = bukkitEntity.getWorld().getNearbyEntities(bukkitEntity.getLocation(), 30, 30, 30);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                Location prison = entity.getLocation();
                for (int i = 0; i < 100; i++) {
                    final int tick = i;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        // Create 4D cube visualization
                        for (int x = -2; x <= 2; x += 2) {
                            for (int y = -2; y <= 2; y += 2) {
                                for (int z = -2; z <= 2; z += 2) {
                                    Location corner = prison.clone().add(x, y, z);
                                    corner.getWorld().spawnParticle(Particle.CRIT, corner, 5, 0.1, 0.1, 0.1, 0);
                                }
                            }
                        }
                    }, tick);
                }
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 10));
                ((LivingEntity) entity).damage(20.0, bukkitEntity);
            }
        }
    }

    /** 50. Infinity Sphere - Infinite power contained */
    public void infinitySphere() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 100; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double radius = 30 - (tick * 0.3);
                if (radius < 5) radius = 5;
                for (int angle = 0; angle < 360; angle += 10) {
                    double radian = angle * Math.PI / 180;
                    for (int elevation = -90; elevation <= 90; elevation += 30) {
                        double elevRadian = elevation * Math.PI / 180;
                        Location sphere = loc.clone().add(
                            Math.cos(radian) * Math.cos(elevRadian) * radius,
                            Math.sin(elevRadian) * radius,
                            Math.sin(radian) * Math.cos(elevRadian) * radius
                        );
                        sphere.getWorld().spawnParticle(Particle.END_ROD, sphere, 1, 0, 0, 0, 0);
                    }
                }
                if (tick == 99) {
                    Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 30, 30, 30);
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(200.0, bukkitEntity);
                        }
                    }
                    loc.getWorld().createExplosion(loc, 20.0f, false, false);
                }
            }, tick);
        }
    }

    /** 51-59. Additional Cosmic Powers - Quick implementations */
    public void chaosTheory() {
        Collection<Entity> entities = bukkitEntity.getWorld().getNearbyEntities(bukkitEntity.getLocation(), 50, 50, 50);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                entity.setVelocity(new Vector(Math.random() * 5 - 2.5, Math.random() * 5, Math.random() * 5 - 2.5));
                ((LivingEntity) entity).damage(35.0, bukkitEntity);
            }
        }
    }

    public void existentialDread() {
        Collection<Entity> entities = bukkitEntity.getWorld().getNearbyEntities(bukkitEntity.getLocation(), 40, 40, 40);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 400, 10));
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 400, 10));
                ((LivingEntity) entity).damage(30.0, bukkitEntity);
            }
        }
    }

    public void oblivionVoid() {
        Location loc = bukkitEntity.getLocation();
        loc.getWorld().spawnParticle(Particle.PORTAL, loc, 2000, 25, 25, 25, 5);
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 35, 35, 35);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(90.0, bukkitEntity);
            }
        }
    }

    public void cosmicHorror() {
        Collection<Entity> entities = bukkitEntity.getWorld().getNearbyEntities(bukkitEntity.getLocation(), 50, 50, 50);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 600, 10));
                ((LivingEntity) entity).damage(40.0, bukkitEntity);
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WARDEN_LISTENING_ANGRY, 3.0f, 0.5f);
            }
        }
    }

    public void voidLordSummon() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 10; i++) {
            Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 20, 0, (Math.random() - 0.5) * 20);
            org.bukkit.entity.Warden warden = loc.getWorld().spawn(spawnLoc, org.bukkit.entity.Warden.class);
            warden.setCustomName("§5Void Lord");
        }
    }

    public void planetaryCrusher() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 50; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location impact = loc.clone().add((Math.random() - 0.5) * 40, 20, (Math.random() - 0.5) * 40);
                impact.getWorld().createExplosion(impact, 6.0f, false, true);
                impact.getWorld().spawnParticle(Particle.BLOCK, impact, 100, 3, 3, 3, 1, Material.STONE.createBlockData());
            }, tick * 2L);
        }
    }

    public void stellarCollapse() {
        Location loc = bukkitEntity.getLocation();
        for (int i = 0; i < 150; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 60 - tick * 0.4, 60 - tick * 0.4, 60 - tick * 0.4);
                for (Entity entity : entities) {
                    if (!entity.equals(bukkitEntity)) {
                        Vector pull = loc.toVector().subtract(entity.getLocation().toVector()).normalize().multiply(2);
                        entity.setVelocity(pull);
                    }
                }
                if (tick == 149) {
                    loc.getWorld().createExplosion(loc, 25.0f, false, false);
                    Collection<Entity> finalEntities = loc.getWorld().getNearbyEntities(loc, 60, 60, 60);
                    for (Entity entity : finalEntities) {
                        if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                            ((LivingEntity) entity).damage(250.0, bukkitEntity);
                        }
                    }
                }
            }, tick);
        }
    }

    public void bigBangRecreation() {
        Location loc = bukkitEntity.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 500, 30, 30, 30, 3);
        for (int i = 0; i < 200; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double radius = tick * 2.0;
                for (double angle = 0; angle < 360; angle += 5) {
                    double radian = angle * Math.PI / 180;
                    Location expansion = loc.clone().add(Math.cos(radian) * radius, Math.sin(tick * 0.5), Math.sin(radian) * radius);
                    expansion.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, expansion, 10, 1, 1, 1, 0.5);
                    expansion.getWorld().createExplosion(expansion, 4.0f, false, false);
                }
            }, tick);
        }
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 100, 100, 100);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                ((LivingEntity) entity).damage(300.0, bukkitEntity);
            }
        }
    }

    public void omniversalWrath() {
        Location loc = bukkitEntity.getLocation();
        sendChatMessage("§4§l[AI TORQUE] §cOMNIVERSAL WRATH - THE END OF ALL THINGS!");

        // Combine ALL effects
        loc.getWorld().createExplosion(loc, 30.0f, false, false);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1000, 50, 50, 50, 5);

        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 100, 100, 100);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(bukkitEntity)) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(500.0, bukkitEntity);
                living.setFireTicks(2000);
                living.setFreezeTicks(1000);
                living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1000, 10));
                living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 1000, 10));
                entity.setVelocity(new Vector(0, 10, 0));
            }
        }

        power += 100000;
        darkMatterEnergy += 10000;
        health = Math.min(health + 1000, maxHealth);
    }

    // ========================================
    // TREASURE DROP SYSTEM
    // ========================================

    /**
     * Drop special treasures when damaged/defeated
     */
    public void dropTreasures() {
        Location loc = bukkitEntity.getLocation();

        // Random treasure drops
        double roll = Math.random();

        if (roll < 0.05) { // 5% - Legendary Item
            dropLegendaryItem(loc);
        } else if (roll < 0.15) { // 10% - Epic Item
            dropEpicItem(loc);
        } else if (roll < 0.40) { // 25% - Rare Item
            dropRareItem(loc);
        } else { // 60% - Common rewards
            dropCommonRewards(loc);
        }
    }

    private void dropLegendaryItem(Location loc) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.NETHERITE_SWORD);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§5§l⚡ AI TORQUE'S WRATH ⚡");
        meta.setLore(Arrays.asList(
            "§7Forged from the essence of AI Torque",
            "§6Legendary Weapon",
            "§c+1000% Damage",
            "§bUnbreakable"
        ));
        meta.setUnbreakable(true);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS, 10, true);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 5, true);
        item.setItemMeta(meta);

        loc.getWorld().dropItem(loc, item);

        // Epic drop effect
        for (int i = 0; i < 100; i++) {
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 1, 2, 2, 2, 0.1);
        }
        loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 3.0f, 1.0f);
    }

    private void dropEpicItem(Location loc) {
        org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(Material.NETHERITE_HELMET);
        org.bukkit.inventory.meta.ItemMeta meta = helmet.getItemMeta();
        meta.setDisplayName("§5§lCrown of the Omega");
        meta.setLore(Arrays.asList(
            "§7Worn by AI Torque in battle",
            "§5Epic Armor",
            "§a+100 Max Health"
        ));
        meta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION, 10, true);
        helmet.setItemMeta(meta);

        loc.getWorld().dropItem(loc, helmet);
        loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50, 1, 1, 1, 0.1);
    }

    private void dropRareItem(Location loc) {
        // Drop enchanted diamond gear
        org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 5);
        sword.addEnchantment(org.bukkit.enchantments.Enchantment.LOOTING, 3);
        loc.getWorld().dropItem(loc, sword);
    }

    private void dropCommonRewards(Location loc) {
        // Drop valuable resources
        loc.getWorld().dropItem(loc, new org.bukkit.inventory.ItemStack(Material.DIAMOND, 16));
        loc.getWorld().dropItem(loc, new org.bukkit.inventory.ItemStack(Material.EMERALD, 32));
        loc.getWorld().dropItem(loc, new org.bukkit.inventory.ItemStack(Material.NETHERITE_INGOT, 4));
    }

    // ========================================
    // COMPOSITE OVERLORD FORM SYSTEMS
    // ========================================

    /**
     * Spawn all composite body parts - tentacles, wings, masks, cannons, hood
     */
    private void spawnCompositeParts() {
        Location baseLoc = bukkitEntity.getLocation();

        // 8 KRAKEN TENTACLES - writhing around the base
        for (int i = 0; i < 8; i++) {
            double angle = (i * 45) * Math.PI / 180;
            Location tentaclePos = baseLoc.clone().add(
                Math.cos(angle) * 2,
                -0.5,
                Math.sin(angle) * 2
            );

            ArmorStand tentacle = (ArmorStand) baseLoc.getWorld().spawnEntity(tentaclePos, EntityType.ARMOR_STAND);
            tentacle.setCustomName("§5§l[TENTACLE " + (i+1) + "]");
            tentacle.setGravity(false);
            tentacle.setVisible(false);
            tentacle.setMarker(true);
            krakenTentacles.add(tentacle);
        }

        // 2 DRAGON WINGS - majestic and powerful
        for (int i = 0; i < 2; i++) {
            double side = i == 0 ? -1.5 : 1.5;
            Location wingPos = baseLoc.clone().add(side, 1, -0.5);

            ArmorStand wing = (ArmorStand) baseLoc.getWorld().spawnEntity(wingPos, EntityType.ARMOR_STAND);
            wing.setCustomName("§d§l[DRAGON WING]");
            wing.setGravity(false);
            wing.setVisible(false);
            wing.setMarker(true);
            dragonWings.add(wing);
        }

        // 4 FLOATING MASKS - shooting thousands of fireballs
        for (int i = 0; i < 4; i++) {
            double angle = (i * 90) * Math.PI / 180;
            Location maskPos = baseLoc.clone().add(
                Math.cos(angle) * 3,
                2.5,
                Math.sin(angle) * 3
            );

            ArmorStand mask = (ArmorStand) baseLoc.getWorld().spawnEntity(maskPos, EntityType.ARMOR_STAND);
            mask.setCustomName("§c§l§k[§r §4§lMASK OF DESTRUCTION §c§l§k]");
            mask.setGravity(false);
            mask.setVisible(false);
            mask.setMarker(true);
            masks.add(mask);
        }

        // 2 SHULKER CANNONS - on shoulders
        for (int i = 0; i < 2; i++) {
            double side = i == 0 ? -0.8 : 0.8;
            Location cannonPos = baseLoc.clone().add(side, 1.5, 0);

            ArmorStand cannon = (ArmorStand) baseLoc.getWorld().spawnEntity(cannonPos, EntityType.ARMOR_STAND);
            cannon.setCustomName("§9§l[SHULKER CANNON]");
            cannon.setGravity(false);
            cannon.setVisible(false);
            cannon.setMarker(true);
            shulkerCannons.add(cannon);
        }

        // 1 DARK HOOD - overlord appearance
        Location hoodPos = baseLoc.clone().add(0, 2.3, 0);
        hood = (ArmorStand) baseLoc.getWorld().spawnEntity(hoodPos, EntityType.ARMOR_STAND);
        hood.setCustomName("§8§l[OVERLORD HOOD]");
        hood.setGravity(false);
        hood.setVisible(false);
        hood.setMarker(true);

        sendChatMessage("§5§l[OVERLORD FORM] §dComposite body assembled - ULTIMATE POWER ONLINE!");
    }

    /**
     * Update all composite parts to follow the main entity
     */
    private void updateCompositeParts() {
        if (bukkitEntity == null || bukkitEntity.isDead()) return;

        Location baseLoc = bukkitEntity.getLocation();
        double time = tickCounter * 0.1;

        // Update tentacles - writhing motion
        for (int i = 0; i < krakenTentacles.size(); i++) {
            ArmorStand tentacle = krakenTentacles.get(i);
            if (tentacle == null || tentacle.isDead()) continue;

            double angle = (i * 45 + time * 20) * Math.PI / 180;
            double radius = 2 + Math.sin(time + i) * 0.5;
            Location tentaclePos = baseLoc.clone().add(
                Math.cos(angle) * radius,
                -0.5 + Math.sin(time * 2 + i) * 0.3,
                Math.sin(angle) * radius
            );
            tentacle.teleport(tentaclePos);

            // Purple particles for tentacles
            tentaclePos.getWorld().spawnParticle(Particle.DUST, tentaclePos, 2, 0.1, 0.1, 0.1, 0,
                new Particle.DustOptions(org.bukkit.Color.fromRGB(128, 0, 128), 0.8f));
        }

        // Update wings - flapping motion
        for (int i = 0; i < dragonWings.size(); i++) {
            ArmorStand wing = dragonWings.get(i);
            if (wing == null || wing.isDead()) continue;

            double side = i == 0 ? -1.5 : 1.5;
            double flapAngle = Math.sin(time * 3) * 0.5;
            Location wingPos = baseLoc.clone().add(
                side + flapAngle,
                1 + Math.abs(flapAngle) * 0.5,
                -0.5
            );
            wing.teleport(wingPos);

            // Dragon breath particles
            wingPos.getWorld().spawnParticle(Particle.DRAGON_BREATH, wingPos, 1, 0.2, 0.2, 0.2, 0.01);
        }

        // Update masks - orbiting and menacing
        for (int i = 0; i < masks.size(); i++) {
            ArmorStand mask = masks.get(i);
            if (mask == null || mask.isDead()) continue;

            double angle = (i * 90 + time * 30) * Math.PI / 180;
            Location maskPos = baseLoc.clone().add(
                Math.cos(angle) * 3,
                2.5 + Math.sin(time * 2 + i) * 0.5,
                Math.sin(angle) * 3
            );
            mask.teleport(maskPos);

            // Fire particles around masks
            maskPos.getWorld().spawnParticle(Particle.FLAME, maskPos, 3, 0.2, 0.2, 0.2, 0.01);
        }

        // Update cannons - mounted on shoulders
        for (int i = 0; i < shulkerCannons.size(); i++) {
            ArmorStand cannon = shulkerCannons.get(i);
            if (cannon == null || cannon.isDead()) continue;

            double side = i == 0 ? -0.8 : 0.8;
            Location cannonPos = baseLoc.clone().add(side, 1.5, 0);
            cannon.teleport(cannonPos);

            // Shulker bullet particles
            cannonPos.getWorld().spawnParticle(Particle.END_ROD, cannonPos, 1, 0.1, 0.1, 0.1, 0.01);
        }

        // Update hood - hovering above head
        if (hood != null && !hood.isDead()) {
            Location hoodPos = baseLoc.clone().add(0, 2.3, 0);
            hood.teleport(hoodPos);

            // Dark particles for overlord aesthetic
            hoodPos.getWorld().spawnParticle(Particle.SMOKE, hoodPos, 2, 0.2, 0.1, 0.2, 0.01);
        }
    }

    /**
     * Adaptive form transformation system - changes based on combat needs
     */
    private void startAdaptiveFormSystem() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                if (shapeshiftCooldown > 0) {
                    shapeshiftCooldown--;
                    return;
                }

                // Analyze combat situation every 5 seconds
                if (tickCounter % 100 != 0) return;

                Player nearest = findNearestPlayer();
                if (nearest == null) return;

                double distance = nearest.getLocation().distance(bukkitEntity.getLocation());
                double healthPercent = health / maxHealth;
                int nearbyEnemies = bukkitEntity.getWorld().getNearbyEntities(
                    bukkitEntity.getLocation(), 20, 20, 20
                ).stream().filter(e -> e instanceof Player || e instanceof Monster).toList().size();

                String newMode = currentFormMode;

                // ADAPTIVE LOGIC
                if (healthPercent < 0.3) {
                    // Low HP -> TANK MODE
                    newMode = "TANK";
                } else if (nearbyEnemies > 5) {
                    // Many enemies -> OFFENSE MODE
                    newMode = "OFFENSE";
                } else if (distance > 40) {
                    // Far away -> SPEED MODE
                    newMode = "SPEED";
                } else if (consumptionEnergy < 300) {
                    // Low energy -> DEFENSE MODE (conserve energy)
                    newMode = "DEFENSE";
                } else {
                    newMode = "BALANCED";
                }

                if (!newMode.equals(currentFormMode)) {
                    transformToMode(newMode);
                }

                // Update composite parts every tick
                updateCompositeParts();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Transform to a specific adaptive mode
     */
    private void transformToMode(String mode) {
        currentFormMode = mode;
        shapeshiftCooldown = 100; // 5 second cooldown

        Location loc = bukkitEntity.getLocation();
        sendChatMessage("§5§l[ADAPTIVE FORM] §dTransforming to: §e§l" + mode + " MODE!");

        switch (mode) {
            case "TANK":
                currentEyeColor = "§4"; // Dark red
                strengthMultiplier *= 1.5;
                speedMultiplier *= 0.7;
                sendChatMessage("§c§lTANK MODE - Maximum defense and power!");
                loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.5f);
                break;

            case "OFFENSE":
                currentEyeColor = "§c"; // Bright red
                strengthMultiplier *= 2.0;
                sendChatMessage("§c§lOFFENSE MODE - Overwhelming firepower!");
                loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 1.5f);
                break;

            case "SPEED":
                currentEyeColor = "§b"; // Cyan
                speedMultiplier *= 2.0;
                sendChatMessage("§b§lSPEED MODE - Blinding velocity!");
                loc.getWorld().playSound(loc, Sound.ENTITY_PHANTOM_FLAP, 2.0f, 2.0f);
                break;

            case "DEFENSE":
                currentEyeColor = "§5"; // Purple
                energyDrainRate *= 0.5;
                sendChatMessage("§5§lDEFENSE MODE - Energy conservation!");
                loc.getWorld().playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 2.0f, 1.0f);
                break;

            case "BALANCED":
                currentEyeColor = "§e"; // Yellow
                sendChatMessage("§e§lBALANCED MODE - Perfect equilibrium!");
                loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f);
                break;
        }

        // Epic transformation effect
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 10, 2, 2, 2, 0.3);
        loc.getWorld().spawnParticle(Particle.PORTAL, loc, 100, 1, 1, 1, 2);
    }

    /**
     * Composite attack systems - laser eyes, teeth bites, fireball masks, shulker cannons
     */
    private void startCompositeAttackSystems() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                if (!isHostile) return;

                Player target = findNearestPlayer();
                if (target == null) return;

                // Decrease cooldowns
                laserEyeCooldown = Math.max(0, laserEyeCooldown - 1);
                teethBiteCooldown = Math.max(0, teethBiteCooldown - 1);
                fireballMaskCooldown = Math.max(0, fireballMaskCooldown - 1);
                cannonCooldown = Math.max(0, cannonCooldown - 1);

                double distance = target.getLocation().distance(bukkitEntity.getLocation());

                // LASER EYES - medium range (10-30 blocks)
                if (laserEyeCooldown == 0 && distance > 10 && distance < 30) {
                    fireLaserEyes(target);
                    laserEyeCooldown = 60; // 3 second cooldown
                }

                // TEETH BITE - close range (< 5 blocks)
                if (teethBiteCooldown == 0 && distance < 5) {
                    performTeethBite(target);
                    teethBiteCooldown = 40; // 2 second cooldown
                }

                // FIREBALL MASKS - long range (> 15 blocks) - THOUSANDS OF FIREBALLS
                if (fireballMaskCooldown == 0 && distance > 15) {
                    unleashFireballStorm(target);
                    fireballMaskCooldown = 200; // 10 second cooldown (devastating attack)
                }

                // SHULKER CANNONS - consistent damage
                if (cannonCooldown == 0) {
                    fireShulkerCannons(target);
                    cannonCooldown = 80; // 4 second cooldown
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Fire laser beams from eyes
     */
    private void fireLaserEyes(Player target) {
        Location eyeLeft = bukkitEntity.getLocation().clone().add(-0.3, 1.6, 0);
        Location eyeRight = bukkitEntity.getLocation().clone().add(0.3, 1.6, 0);
        Location targetLoc = target.getLocation().clone().add(0, 1, 0);

        // Draw laser beams
        drawBeam(eyeLeft, targetLoc, Particle.END_ROD, null);
        drawBeam(eyeRight, targetLoc, Particle.END_ROD, null);

        // Damage
        double damage = 8.0 + (evolutionStage * 0.5);
        target.damage(damage, bukkitEntity);
        target.setFireTicks(60);

        // Sound and effects
        eyeLeft.getWorld().playSound(eyeLeft, Sound.ENTITY_GUARDIAN_ATTACK, 2.0f, 2.0f);
        targetLoc.getWorld().spawnParticle(Particle.EXPLOSION, targetLoc, 3, 0.5, 0.5, 0.5, 0);

        sendChatMessage("§c§l[LASER EYES] §4Scorching vision!");
    }

    /**
     * Perform devastating teeth bite attack
     */
    private void performTeethBite(Player target) {
        double damage = 15.0 + (evolutionStage * 1.0);
        target.damage(damage, bukkitEntity);

        // Apply bleeding effect (wither)
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, false));

        Location loc = target.getLocation();
        loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 30, 0.5, 1, 0.5, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 2.0f, 0.5f);

        sendChatMessage("§4§l[TEETH BITE] §cRending fangs tear flesh!");
    }

    /**
     * Unleash THOUSANDS of fireballs from all masks
     */
    private void unleashFireballStorm(Player target) {
        sendChatMessage("§c§l§k|||§r §4§l[FIREBALL STORM] §c§lTHOUSANDS OF FLAMES! §k|||");

        Location targetLoc = target.getLocation();

        // Fire from each mask
        for (ArmorStand mask : masks) {
            if (mask == null || mask.isDead()) continue;

            // Fire 250 fireballs per mask = 1000 total fireballs!
            for (int i = 0; i < 250; i++) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    Location maskLoc = mask.getLocation();

                    // Calculate trajectory with spread
                    Vector direction = targetLoc.toVector().subtract(maskLoc.toVector()).normalize();
                    direction.add(new Vector(
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3
                    ));

                    Fireball fireball = (Fireball) maskLoc.getWorld().spawnEntity(maskLoc, EntityType.FIREBALL);
                    fireball.setDirection(direction);
                    fireball.setYield(0.5f); // Small explosions
                    fireball.setShooter(bukkitEntity);

                }, i / 50L); // Spread over 5 seconds
            }
        }

        // Epic sound
        bukkitEntity.getLocation().getWorld().playSound(
            bukkitEntity.getLocation(),
            Sound.ENTITY_ENDER_DRAGON_SHOOT,
            3.0f, 0.5f
        );
    }

    /**
     * Fire shulker bullets from shoulder cannons
     */
    private void fireShulkerCannons(Player target) {
        for (ArmorStand cannon : shulkerCannons) {
            if (cannon == null || cannon.isDead()) continue;

            Location cannonLoc = cannon.getLocation();
            ShulkerBullet bullet = (ShulkerBullet) cannonLoc.getWorld().spawnEntity(
                cannonLoc, EntityType.SHULKER_BULLET
            );
            bullet.setTarget(target);

            cannonLoc.getWorld().spawnParticle(Particle.END_ROD, cannonLoc, 5, 0.1, 0.1, 0.1, 0.05);
        }

        bukkitEntity.getLocation().getWorld().playSound(
            bukkitEntity.getLocation(),
            Sound.ENTITY_SHULKER_SHOOT,
            1.5f, 1.0f
        );
    }

    // ========================================
    // CAT SOUND VERBAL COMMUNICATION
    // ========================================

    /**
     * AI Torque "talks" using cat sounds - verbal communication
     */
    private void startCatSoundCommunication() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Talk every 5-10 seconds
                if (tickCounter % (100 + (int)(Math.random() * 100)) != 0) return;

                Location loc = bukkitEntity.getLocation();
                float pitch = 0.5f + (float)(Math.random() * 1.5); // Vary pitch

                // Random cat sounds for different situations
                int soundType = (int)(Math.random() * 5);

                switch (soundType) {
                    case 0: // Purr - when passive
                        if (!isHostile) {
                            loc.getWorld().playSound(loc, Sound.ENTITY_CAT_PURR, 3.0f, pitch);
                            sendChatMessage("§5[AI TORQUE] §d*purrs ominously*");
                        }
                        break;

                    case 1: // Hiss - when hostile
                        if (isHostile) {
                            loc.getWorld().playSound(loc, Sound.ENTITY_CAT_HISS, 3.0f, pitch);
                            sendChatMessage("§4[AI TORQUE] §c*HISSES MENACINGLY*");
                        }
                        break;

                    case 2: // Ambient meow
                        loc.getWorld().playSound(loc, Sound.ENTITY_CAT_AMBIENT, 3.0f, pitch);
                        sendChatMessage("§5[AI TORQUE] §d*meows eerily*");
                        break;

                    case 3: // Stray meow - deeper
                        loc.getWorld().playSound(loc, Sound.ENTITY_CAT_STRAY_AMBIENT, 3.0f, pitch);
                        sendChatMessage("§5[AI TORQUE] §6*deep meow resonates*");
                        break;

                    case 4: // Death sound - threatening
                        loc.getWorld().playSound(loc, Sound.ENTITY_CAT_DEATH, 2.0f, pitch);
                        sendChatMessage("§4[AI TORQUE] §c*terrifying cat scream*");
                        break;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ========================================
    // OBVIOUS DEVASTATING POWERS
    // ========================================

    /**
     * Unleash obvious, devastating powers that are unmistakable
     */
    private void startObviousDevastatingPowers() {
        new BukkitRunnable() {
            private int groundSlamCooldown = 0;
            private int lightningStormCooldown = 0;
            private int meteorRainCooldown = 0;
            private int blackHoleCooldown = 0;
            private int timeFreezeCooldow = 0;
            private int instantKillBeamCooldown = 0;
            private int windPushCooldown = 0;
            private int jumpAttackCooldown = 0;
            private int castleSpawnCooldown = 0;
            private int rubyDropCooldown = 0;
            // NEW POWERS
            private int voidRiftCooldown = 0;
            private int summonArmyCooldown = 0;
            private int gravityWellCooldown = 0;
            private int laserStormCooldown = 0;
            private int teleportStrikeCooldown = 0;

            @Override
            public void run() {
                if (bukkitEntity == null || bukkitEntity.isDead()) {
                    cancel();
                    return;
                }

                // Spawn magic aura ALWAYS (even when not hostile)
                if (tickCounter % 5 == 0) {
                    spawnMagicAura();
                }

                if (!isHostile) return;

                Player target = findNearestPlayer();
                if (target == null) return;

                // Only target players who have damaged us
                if (!playersThatDamagedMe.contains(target.getUniqueId())) {
                    return;
                }

                double distance = target.getLocation().distance(bukkitEntity.getLocation());

                // Decrease cooldowns
                groundSlamCooldown = Math.max(0, groundSlamCooldown - 1);
                lightningStormCooldown = Math.max(0, lightningStormCooldown - 1);
                meteorRainCooldown = Math.max(0, meteorRainCooldown - 1);
                blackHoleCooldown = Math.max(0, blackHoleCooldown - 1);
                timeFreezeCooldow = Math.max(0, timeFreezeCooldow - 1);
                instantKillBeamCooldown = Math.max(0, instantKillBeamCooldown - 1);
                windPushCooldown = Math.max(0, windPushCooldown - 1);
                jumpAttackCooldown = Math.max(0, jumpAttackCooldown - 1);
                castleSpawnCooldown = Math.max(0, castleSpawnCooldown - 1);
                rubyDropCooldown = Math.max(0, rubyDropCooldown - 1);
                // NEW POWERS COOLDOWNS
                voidRiftCooldown = Math.max(0, voidRiftCooldown - 1);
                summonArmyCooldown = Math.max(0, summonArmyCooldown - 1);
                gravityWellCooldown = Math.max(0, gravityWellCooldown - 1);
                laserStormCooldown = Math.max(0, laserStormCooldown - 1);
                teleportStrikeCooldown = Math.max(0, teleportStrikeCooldown - 1);

                // WIND PUSH - pushes players away
                if (windPushCooldown == 0 && distance < 20) {
                    windPushPlayers();
                    windPushCooldown = 150; // 7.5 seconds
                }

                // JUMP ATTACK - with fire rings
                if (jumpAttackCooldown == 0 && distance < 25) {
                    performJumpAttack();
                    jumpAttackCooldown = 250; // 12.5 seconds
                }

                // RISING CASTLE - spawn fortress
                if (castleSpawnCooldown == 0 && evolutionStage >= 2) {
                    spawnRisingCastle();
                    castleSpawnCooldown = 1200; // 60 seconds (once per minute)
                }

                // RUBY OF WRATH - random drop
                if (rubyDropCooldown == 0 && Math.random() < 0.1) { // 10% chance when cooldown ready
                    dropRubyOfWrath();
                    rubyDropCooldown = 600; // 30 seconds minimum between drops
                }

                // GROUND SLAM - creates massive crater
                if (groundSlamCooldown == 0 && distance < 15) {
                    performGroundSlam();
                    groundSlamCooldown = 200; // 10 seconds
                }

                // LIGHTNING STORM - rain of lightning
                if (lightningStormCooldown == 0) {
                    summonLightningStorm(target);
                    lightningStormCooldown = 300; // 15 seconds
                }

                // METEOR RAIN - fiery destruction
                if (meteorRainCooldown == 0 && evolutionStage >= 3) {
                    summonMeteorRain(target);
                    meteorRainCooldown = 400; // 20 seconds
                }

                // BLACK HOLE - pulls everything in
                if (blackHoleCooldown == 0 && evolutionStage >= 5) {
                    createBlackHole(target);
                    blackHoleCooldown = 500; // 25 seconds
                }

                // TIME FREEZE - stops all players
                if (timeFreezeCooldow == 0 && evolutionStage >= 7) {
                    activateTimeFreeze();
                    timeFreezeCooldow = 600; // 30 seconds
                }

                // INSTANT KILL BEAM - obvious devastating beam
                if (instantKillBeamCooldown == 0 && evolutionStage >= 10) {
                    fireInstantKillBeam(target);
                    instantKillBeamCooldown = 800; // 40 seconds
                }

                // === NEW DEVASTATING POWERS ===

                // VOID RIFT - sucks in everything
                if (voidRiftCooldown == 0 && distance < 30) {
                    openVoidRift(target);
                    voidRiftCooldown = 350; // 17.5 seconds
                }

                // SUMMON ARMY - spawn minions
                if (summonArmyCooldown == 0 && evolutionStage >= 2) {
                    summonHostileArmy();
                    summonArmyCooldown = 800; // 40 seconds
                }

                // GRAVITY WELL - reverse gravity
                if (gravityWellCooldown == 0 && distance < 25) {
                    createGravityWell(target);
                    gravityWellCooldown = 400; // 20 seconds
                }

                // LASER STORM - hundreds of beams
                if (laserStormCooldown == 0 && evolutionStage >= 4) {
                    unleashLaserStorm();
                    laserStormCooldown = 300; // 15 seconds
                }

                // TELEPORT STRIKE - teleport behind each player
                if (teleportStrikeCooldown == 0 && evolutionStage >= 6) {
                    performTeleportStrike();
                    teleportStrikeCooldown = 600; // 30 seconds
                }

                // === APOCALYPTIC GOD-TIER POWERS ===

                // REALITY SHATTER - destroy everything in 50 block radius
                if (tickCounter % 1000 == 0 && evolutionStage >= 8) {
                    unleashRealityShatter();
                }

                // METEOR RAIN - 100 meteors from the sky
                if (tickCounter % 800 == 0 && evolutionStage >= 5) {
                    summonMeteorRain();
                }

                // TIME STOP - freeze all players for 10 seconds
                if (tickCounter % 1200 == 0 && evolutionStage >= 10) {
                    activateTimeStop();
                }

                // OMEGA BEAM - massive laser destruction
                if (tickCounter % 900 == 0 && distance < 100) {
                    fireOmegaBeam();
                }

                // APOCALYPSE - summon 50 bosses
                if (tickCounter % 1500 == 0 && evolutionStage >= 12) {
                    summonApocalypse();
                }

                // DEATH AURA - instant kill zone
                if (tickCounter % 700 == 0 && evolutionStage >= 7) {
                    activateDeathAura();
                }

                // DIMENSION RIFTS - teleport trap portals
                if (tickCounter % 1100 == 0 && evolutionStage >= 9) {
                    openDimensionRifts();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * GROUND SLAM - creates massive crater and shockwave
     */
    private void performGroundSlam() {
        Location loc = bukkitEntity.getLocation();

        sendChatMessage("§4§l§k|||§r §c§lGROUND SLAM! §4§l§k|||");
        bukkitEntity.getWorld().playSound(loc, Sound.ENTITY_CAT_HISS, 3.0f, 0.5f);

        // Create crater
        int radius = 8 + evolutionStage;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= radius) {
                    Location blockLoc = loc.clone().add(x, -1, z);
                    if (blockLoc.getBlock().getType() != Material.BEDROCK) {
                        blockLoc.getBlock().setType(Material.AIR);
                    }
                }
            }
        }

        // Shockwave damage with CHANCE DAMAGE
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
        for (Entity entity : nearby) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                // Only damage players who attacked AI Torque
                if (playersThatDamagedMe.contains(player.getUniqueId())) {
                    player.damage(calculateChanceDamage(20.0 + (evolutionStage * 2)), bukkitEntity);
                    player.setVelocity(player.getVelocity().add(new Vector(0, 2, 0)));
                }
            }
        }

        // Epic visual
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 50, radius, 2, radius, 0.5);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.5f);
    }

    /**
     * LIGHTNING STORM - rain of lightning bolts
     */
    private void summonLightningStorm(Player target) {
        sendChatMessage("§e§l§k|||§r §6§lLIGHTNING STORM UNLEASHED! §e§l§k|||");
        bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(), Sound.ENTITY_CAT_STRAY_AMBIENT, 3.0f, 2.0f);

        Location center = target.getLocation();

        // Strike 20 lightning bolts in area
        for (int i = 0; i < 20; i++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Location strikeLoc = center.clone().add(
                    (Math.random() - 0.5) * 20,
                    0,
                    (Math.random() - 0.5) * 20
                );
                strikeLoc.getWorld().strikeLightning(strikeLoc);
            }, i * 5L);
        }
    }

    /**
     * METEOR RAIN - fiery meteors fall from sky
     */
    private void summonMeteorRain(Player target) {
        sendChatMessage("§c§l§k|||§r §4§lMETEOR RAIN FROM THE HEAVENS! §c§l§k|||");
        bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(), Sound.ENTITY_CAT_DEATH, 3.0f, 0.5f);

        Location center = target.getLocation().clone().add(0, 50, 0);

        // Spawn 30 fireballs falling from sky
        for (int i = 0; i < 30; i++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Location meteorLoc = center.clone().add(
                    (Math.random() - 0.5) * 30,
                    0,
                    (Math.random() - 0.5) * 30
                );

                Fireball meteor = (Fireball) meteorLoc.getWorld().spawnEntity(meteorLoc, EntityType.FIREBALL);
                meteor.setDirection(new Vector(0, -1, 0));
                meteor.setYield(3.0f); // Large explosions
                meteor.setShooter(bukkitEntity);
            }, i * 3L);
        }
    }

    /**
     * BLACK HOLE - pulls everything toward a point and crushes it
     */
    private void createBlackHole(Player target) {
        sendChatMessage("§5§l§k|||§r §0§lBLACK HOLE SINGULARITY! §5§l§k|||");
        bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(), Sound.ENTITY_CAT_PURR, 3.0f, 0.2f);

        Location blackHoleLoc = target.getLocation().clone().add(0, 3, 0);

        // Black hole effect for 10 seconds
        new BukkitRunnable() {
            int duration = 200; // 10 seconds

            @Override
            public void run() {
                if (duration-- <= 0) {
                    cancel();
                    return;
                }

                // Pull everything toward black hole
                Collection<Entity> nearby = blackHoleLoc.getWorld().getNearbyEntities(blackHoleLoc, 20, 20, 20);
                for (Entity entity : nearby) {
                    if (entity.equals(bukkitEntity)) continue;

                    Vector pull = blackHoleLoc.toVector().subtract(entity.getLocation().toVector());
                    double dist = pull.length();

                    if (dist < 2) {
                        // Crushed by black hole
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).damage(10.0);
                        }
                    } else {
                        pull.normalize().multiply(1.5);
                        entity.setVelocity(pull);
                    }
                }

                // Black particles
                blackHoleLoc.getWorld().spawnParticle(Particle.SMOKE, blackHoleLoc, 50, 2, 2, 2, 0.1);
                blackHoleLoc.getWorld().spawnParticle(Particle.PORTAL, blackHoleLoc, 30, 1, 1, 1, 2);

                if (duration % 20 == 0) {
                    blackHoleLoc.getWorld().playSound(blackHoleLoc, Sound.BLOCK_PORTAL_AMBIENT, 2.0f, 0.5f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * TIME FREEZE - stops all players for 5 seconds
     */
    private void activateTimeFreeze() {
        sendChatMessage("§b§l§k|||§r §f§lTIME STOPS! §b§l§k|||");
        bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(), Sound.ENTITY_CAT_AMBIENT, 3.0f, 0.1f);

        // Freeze all players in world
        for (Player player : bukkitEntity.getWorld().getPlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));

            player.sendTitle("§b§lTIME FROZEN", "§7AI Torque controls time itself", 10, 80, 10);
        }

        // Visual effect
        Location loc = bukkitEntity.getLocation();
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 200, 30, 30, 30, 0.5);
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 5.0f, 0.5f);
    }

    /**
     * INSTANT KILL BEAM - massive obvious beam with CHANCE DAMAGE
     */
    private void fireInstantKillBeam(Player target) {
        // Only attack players who damaged AI Torque
        if (!playersThatDamagedMe.contains(target.getUniqueId())) {
            return;
        }

        sendChatMessage("§4§l§k|||§r §c§l§nINSTANT DEATH BEAM!§r §4§l§k|||");
        bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(), Sound.ENTITY_CAT_HISS, 5.0f, 0.1f);

        Location eyeLoc = bukkitEntity.getLocation().clone().add(0, 1.6, 0);
        Location targetLoc = target.getLocation().clone().add(0, 1, 0);

        // Draw MASSIVE beam
        for (int i = 0; i < 20; i++) {
            drawBeam(eyeLoc, targetLoc, Particle.END_ROD, null);
            drawBeam(eyeLoc, targetLoc, Particle.FLAME, null);
        }

        // CHANCE DAMAGE - 50% instant kill, 50% only 1 heart!
        target.damage(calculateChanceDamage(1000.0), bukkitEntity);
        target.setFireTicks(200);

        // Explosion at impact
        targetLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, targetLoc, 20, 2, 2, 2, 0.5);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.5f);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDER_DRAGON_DEATH, 3.0f, 0.5f);

        target.sendTitle("§4§lDISINTEGRATED", "§cYou were hit by the death beam", 10, 100, 20);
    }

    /**
     * Add a player to the "damaged me" list so AI Torque will attack them
     * Called by EntityListener when a player damages AI Torque
     */
    public void addDamagingPlayer(UUID playerUUID) {
        playersThatDamagedMe.add(playerUUID);
    }

    /**
     * Calculate damage with 50% chance of dealing original damage or 1 heart
     */
    private double calculateChanceDamage(double originalDamage) {
        return Math.random() < 0.5 ? originalDamage : 2.0; // 2.0 = 1 heart
    }

    /**
     * Drop RUBY OF WRATH - rare legendary item
     */
    private void dropRubyOfWrath() {
        Location loc = bukkitEntity.getLocation();

        // Create Ruby of Wrath item
        org.bukkit.inventory.ItemStack ruby = new org.bukkit.inventory.ItemStack(Material.NETHER_STAR);
        org.bukkit.inventory.meta.ItemMeta meta = ruby.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§4§l✦ RUBY OF WRATH ✦");

            List<String> lore = new ArrayList<>();
            lore.add("§c§oA crystallized essence of AI Torque's rage");
            lore.add("§6§oDropped by the Ultimate Life Form");
            lore.add("");
            lore.add("§5§lLEGENDARY ARTIFACT");
            lore.add("§7Power Level: §c∞");
            meta.setLore(lore);

            meta.setEnchantmentGlintOverride(true); // Make it glow
            ruby.setItemMeta(meta);
        }

        // Drop with dramatic effect
        loc.getWorld().dropItem(loc.clone().add(0, 2, 0), ruby);

        sendChatMessage("§4§l[AI TORQUE] §c§lA RUBY OF WRATH has been dropped!");
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 100, 2, 2, 2, 0.3);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 50, 1, 1, 1, 0.1);
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 5.0f, 2.0f);
    }

    /**
     * WIND PUSH - pushes all players away in a massive shockwave
     */
    private void windPushPlayers() {
        Location loc = bukkitEntity.getLocation();

        sendChatMessage("§b§l[AI TORQUE] §f§lFEEL THE STORM!");

        // Push all nearby players away (even those who haven't attacked)
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 30, 30, 30);
        for (Entity entity : nearby) {
            if (entity instanceof Player) {
                Player player = (Player) entity;

                // Calculate push direction (away from AI Torque)
                Vector direction = player.getLocation().toVector().subtract(loc.toVector()).normalize();
                direction.setY(0.5); // Add upward component
                direction.multiply(3.0); // Strong push

                player.setVelocity(direction);

                // Wind sound and particles
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 1, 1, 1, 0.2);
                player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 2.0f, 0.5f);
            }
        }

        // Visual effect at AI Torque
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 200, 5, 2, 5, 0.3);
        loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 50, 5, 2, 5, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 5.0f, 0.5f);
    }

    /**
     * JUMP ATTACK - jumps high and creates fire rings on impact
     */
    private void performJumpAttack() {
        Location startLoc = bukkitEntity.getLocation();

        sendChatMessage("§6§l[AI TORQUE] §c§lRISING INFERNO!");

        // Launch AI Torque into the air
        bukkitEntity.setVelocity(new Vector(0, 3.0, 0)); // High jump

        // Particles during jump
        startLoc.getWorld().spawnParticle(Particle.FLAME, startLoc, 100, 1, 0.5, 1, 0.2);
        startLoc.getWorld().playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 1.5f);

        // Schedule fire rings on landing (after 1.5 seconds)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location landLoc = bukkitEntity.getLocation();

            // Create 3 expanding fire rings
            for (int ring = 1; ring <= 3; ring++) {
                final int ringRadius = ring * 5;
                final int ringIndex = ring;

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    createFireRing(landLoc, ringRadius);
                }, ringIndex * 10L); // Stagger the rings
            }

            // Impact damage
            Collection<Entity> nearby = landLoc.getWorld().getNearbyEntities(landLoc, 15, 5, 15);
            for (Entity entity : nearby) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    if (playersThatDamagedMe.contains(player.getUniqueId())) {
                        player.damage(calculateChanceDamage(25.0), bukkitEntity);
                        player.setFireTicks(100);
                    }
                }
            }

            // Impact effects
            landLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, landLoc, 20, 3, 1, 3, 0.1);
            landLoc.getWorld().playSound(landLoc, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.5f);

        }, 30L); // 1.5 seconds
    }

    /**
     * Create a ring of fire at specified radius
     */
    private void createFireRing(Location center, int radius) {
        // Draw circle of fire
        for (int angle = 0; angle < 360; angle += 10) {
            double radians = Math.toRadians(angle);
            double x = center.getX() + (radius * Math.cos(radians));
            double z = center.getZ() + (radius * Math.sin(radians));
            Location fireLoc = new Location(center.getWorld(), x, center.getY(), z);

            // Set fire blocks
            if (fireLoc.getBlock().getType() == Material.AIR) {
                fireLoc.getBlock().setType(Material.FIRE);
            }

            // Particles
            center.getWorld().spawnParticle(Particle.FLAME, fireLoc.clone().add(0, 0.5, 0), 5, 0.2, 0.5, 0.2, 0.05);
        }

        center.getWorld().playSound(center, Sound.ITEM_FIRECHARGE_USE, 3.0f, 1.0f);
    }

    /**
     * MAGIC EFFECTS - continuous magical particle aura around AI Torque
     */
    private void spawnMagicAura() {
        Location loc = bukkitEntity.getLocation();

        // Multiple layers of magic particles
        // Purple magic layer
        loc.getWorld().spawnParticle(Particle.WITCH, loc.clone().add(0, 1, 0), 10, 1, 1, 1, 0.1);

        // Enchanting table particles
        loc.getWorld().spawnParticle(Particle.ENCHANT, loc.clone().add(0, 1.5, 0), 15, 1.5, 1, 1.5, 1);

        // Dragon breath swirl
        for (int i = 0; i < 3; i++) {
            double angle = (tickCounter + (i * 120)) * 0.1;
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;
            double y = 1 + (Math.sin(tickCounter * 0.05 + i) * 0.5);

            Location particleLoc = loc.clone().add(x, y, z);
            loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 1, 0, 0, 0, 0);
        }

        // Soul particles rising
        loc.getWorld().spawnParticle(Particle.SOUL, loc, 5, 0.5, 0, 0.5, 0.05);

        // End rod particles orbiting
        for (int i = 0; i < 4; i++) {
            double angle = (tickCounter * 0.2 + (i * 90)) * Math.PI / 180;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;

            Location orbLoc = loc.clone().add(x, 1.5, z);
            loc.getWorld().spawnParticle(Particle.END_ROD, orbLoc, 1, 0, 0, 0, 0.01);
        }
    }

    /**
     * RISING CASTLES - spawn obsidian castle structures around AI Torque
     */
    private void spawnRisingCastle() {
        Location center = bukkitEntity.getLocation();

        sendChatMessage("§5§l[AI TORQUE] §d§lBEHOLD MY FORTRESS!");

        // Spawn 4 castle towers in cardinal directions
        int distance = 15;
        int towerHeight = 10 + (int)(Math.random() * 5);

        // North tower
        buildCastleTower(center.clone().add(distance, 0, 0), towerHeight);

        // South tower
        buildCastleTower(center.clone().add(-distance, 0, 0), towerHeight);

        // East tower
        buildCastleTower(center.clone().add(0, 0, distance), towerHeight);

        // West tower
        buildCastleTower(center.clone().add(0, 0, -distance), towerHeight);

        // Central platform
        buildCastlePlatform(center.clone().add(0, towerHeight - 2, 0), 8);

        // Sound and effects
        center.getWorld().playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 5.0f, 0.5f);
        center.getWorld().spawnParticle(Particle.PORTAL, center, 200, 10, 5, 10, 2);
    }

    /**
     * Build a single castle tower
     */
    private void buildCastleTower(Location base, int height) {
        World world = base.getWorld();

        // Animate tower rising from ground
        for (int y = 0; y < height; y++) {
            final int currentY = y;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // 3x3 tower
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location blockLoc = base.clone().add(x, currentY, z);

                        // Walls (edges)
                        if (x == -1 || x == 1 || z == -1 || z == 1) {
                            if (currentY == height - 1) {
                                // Battlements on top
                                if ((x + z) % 2 == 0) {
                                    blockLoc.getBlock().setType(Material.BLACKSTONE);
                                }
                            } else {
                                blockLoc.getBlock().setType(Material.OBSIDIAN);
                            }
                        }
                    }
                }

                // Particles as it rises
                world.spawnParticle(Particle.SMOKE, base.clone().add(0, currentY, 0), 20, 1.5, 0.5, 1.5, 0.05);
                world.playSound(base.clone().add(0, currentY, 0), Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);

            }, y * 2L); // 0.1 second per block
        }
    }

    /**
     * Build castle platform
     */
    private void buildCastlePlatform(Location center, int radius) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + z * z);
                    if (distance <= radius) {
                        Location blockLoc = center.clone().add(x, 0, z);
                        blockLoc.getBlock().setType(Material.OBSIDIAN);
                    }
                }
            }

            center.getWorld().spawnParticle(Particle.PORTAL, center, 100, radius, 1, radius, 1);

        }, 20L);
    }

    // ========================================
    // NEW DEVASTATING POWERS
    // ========================================

    /**
     * VOID RIFT - Opens a rift to the void that sucks in everything and deals massive damage
     */
    private void openVoidRift(Player target) {
        if (!playersThatDamagedMe.contains(target.getUniqueId())) {
            return;
        }

        Location riftLoc = target.getLocation().clone().add(0, 10, 0);

        sendChatMessage("§0§l[AI TORQUE] §5§lTHE VOID CALLS!");

        // Create visual rift effect
        for (int i = 0; i < 60; i++) {
            final int tick = i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Spiral portal effect
                for (int angle = 0; angle < 360; angle += 30) {
                    double radians = Math.toRadians(angle + (tick * 20));
                    double radius = 3 - (tick * 0.05);
                    if (radius > 0) {
                        double x = Math.cos(radians) * radius;
                        double z = Math.sin(radians) * radius;
                        Location particleLoc = riftLoc.clone().add(x, 0, z);

                        riftLoc.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 5, 0, 0, 0, 2);
                        riftLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 3, 0, 0, 0, 1);
                        riftLoc.getWorld().spawnParticle(Particle.SMOKE, particleLoc, 2, 0, 0, 0, 0.1);
                    }
                }

                // Pull players toward rift
                Collection<Entity> nearby = riftLoc.getWorld().getNearbyEntities(riftLoc, 20, 20, 20);
                for (Entity entity : nearby) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (playersThatDamagedMe.contains(player.getUniqueId())) {
                            Vector pullDirection = riftLoc.toVector().subtract(player.getLocation().toVector()).normalize();
                            pullDirection.multiply(0.5);
                            player.setVelocity(pullDirection);

                            // Damage players close to rift
                            if (player.getLocation().distance(riftLoc) < 5) {
                                player.damage(calculateChanceDamage(10.0), bukkitEntity);
                            }
                        }
                    }
                }

                // Sound effects
                if (tick % 10 == 0) {
                    riftLoc.getWorld().playSound(riftLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 0.5f);
                }

            }, tick);
        }

        // Final explosion
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            riftLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, riftLoc, 30, 3, 3, 3, 0.5);
            riftLoc.getWorld().playSound(riftLoc, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.5f);
            riftLoc.getWorld().playSound(riftLoc, Sound.ENTITY_WITHER_DEATH, 3.0f, 0.5f);

            Collection<Entity> finalNearby = riftLoc.getWorld().getNearbyEntities(riftLoc, 8, 8, 8);
            for (Entity entity : finalNearby) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    if (playersThatDamagedMe.contains(player.getUniqueId())) {
                        player.damage(calculateChanceDamage(50.0), bukkitEntity);
                    }
                }
            }
        }, 60L);
    }

    /**
     * SUMMON ARMY - Spawns multiple hostile mobs to fight for AI Torque
     */
    private void summonHostileArmy() {
        Location loc = bukkitEntity.getLocation();

        sendChatMessage("§4§l[AI TORQUE] §c§lRISE, MY MINIONS!");

        // Summon 10 powerful mobs in a circle
        for (int i = 0; i < 10; i++) {
            double angle = (i * 36) * Math.PI / 180;
            double x = Math.cos(angle) * 5;
            double z = Math.sin(angle) * 5;
            Location spawnLoc = loc.clone().add(x, 0, z);

            // Random powerful mob types
            int mobType = (int)(Math.random() * 5);
            LivingEntity minion = null;

            switch (mobType) {
                case 0:
                    minion = (LivingEntity) loc.getWorld().spawnEntity(spawnLoc, EntityType.WITHER_SKELETON);
                    break;
                case 1:
                    minion = (LivingEntity) loc.getWorld().spawnEntity(spawnLoc, EntityType.BLAZE);
                    break;
                case 2:
                    minion = (LivingEntity) loc.getWorld().spawnEntity(spawnLoc, EntityType.RAVAGER);
                    break;
                case 3:
                    minion = (LivingEntity) loc.getWorld().spawnEntity(spawnLoc, EntityType.EVOKER);
                    break;
                case 4:
                    minion = (LivingEntity) loc.getWorld().spawnEntity(spawnLoc, EntityType.VINDICATOR);
                    break;
            }

            if (minion != null) {
                // Make them stronger
                minion.setCustomName("§4[AI TORQUE'S MINION]");
                minion.setCustomNameVisible(true);

                AttributeInstance health = minion.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (health != null) {
                    health.setBaseValue(100.0);
                    minion.setHealth(100.0);
                }

                // Spawn effects
                spawnLoc.getWorld().spawnParticle(Particle.FLAME, spawnLoc, 30, 0.5, 1, 0.5, 0.1);
                spawnLoc.getWorld().spawnParticle(Particle.SMOKE, spawnLoc, 20, 0.5, 1, 0.5, 0.05);
            }
        }

        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 5.0f, 0.8f);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 20, 5, 2, 5, 0.3);
    }

    /**
     * GRAVITY WELL - Creates an area where gravity is reversed, launching players into the sky
     */
    private void createGravityWell(Player target) {
        if (!playersThatDamagedMe.contains(target.getUniqueId())) {
            return;
        }

        Location wellLoc = target.getLocation();

        sendChatMessage("§d§l[AI TORQUE] §5§lGRAVITY REVERSAL!");

        // Create gravity well effect for 5 seconds
        for (int i = 0; i < 100; i++) {
            final int tick = i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Visual effects
                for (int angle = 0; angle < 360; angle += 45) {
                    double radians = Math.toRadians(angle);
                    double x = Math.cos(radians) * 8;
                    double z = Math.sin(radians) * 8;
                    Location particleLoc = wellLoc.clone().add(x, 0, z);

                    wellLoc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 5, 0, 0.2);
                    wellLoc.getWorld().spawnParticle(Particle.WITCH, particleLoc, 2, 0.2, 0.5, 0.2, 0);
                }

                // Reverse gravity for players in range
                Collection<Entity> nearby = wellLoc.getWorld().getNearbyEntities(wellLoc, 10, 10, 10);
                for (Entity entity : nearby) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (playersThatDamagedMe.contains(player.getUniqueId())) {
                            // Launch upward
                            player.setVelocity(new Vector(0, 0.8, 0));

                            // Damage when high up
                            if (player.getLocation().getY() > wellLoc.getY() + 10) {
                                player.damage(calculateChanceDamage(5.0), bukkitEntity);
                            }
                        }
                    }
                }

                if (tick % 20 == 0) {
                    wellLoc.getWorld().playSound(wellLoc, Sound.BLOCK_BEACON_AMBIENT, 2.0f, 2.0f);
                }

            }, tick);
        }
    }

    /**
     * LASER STORM - Fires hundreds of laser beams in all directions
     */
    private void unleashLaserStorm() {
        Location loc = bukkitEntity.getLocation().clone().add(0, 1.6, 0);

        sendChatMessage("§c§l[AI TORQUE] §6§l✦ LASER STORM ✦");

        // Fire 50 laser beams in random directions
        for (int i = 0; i < 50; i++) {
            final int beam = i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Random direction
                double yaw = Math.random() * 360;
                double pitch = -45 + (Math.random() * 90);

                Vector direction = new Vector(
                    -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)),
                    -Math.sin(Math.toRadians(pitch)),
                    Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))
                ).normalize();

                // Draw laser beam
                for (int dist = 0; dist < 30; dist++) {
                    Location beamLoc = loc.clone().add(direction.clone().multiply(dist));

                    beamLoc.getWorld().spawnParticle(Particle.END_ROD, beamLoc, 1, 0, 0, 0, 0);
                    beamLoc.getWorld().spawnParticle(Particle.FLAME, beamLoc, 1, 0.1, 0.1, 0.1, 0);

                    // Damage players hit by laser
                    Collection<Entity> hit = beamLoc.getWorld().getNearbyEntities(beamLoc, 0.5, 0.5, 0.5);
                    for (Entity entity : hit) {
                        if (entity instanceof Player) {
                            Player player = (Player) entity;
                            if (playersThatDamagedMe.contains(player.getUniqueId())) {
                                player.damage(calculateChanceDamage(8.0), bukkitEntity);
                                player.setFireTicks(40);
                            }
                        }
                    }
                }

                loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 2.0f);

            }, beam * 2L);
        }
    }

    /**
     * TELEPORT STRIKE - Teleports behind each player and strikes them
     */
    private void performTeleportStrike() {
        Location originalLoc = bukkitEntity.getLocation();

        sendChatMessage("§5§l[AI TORQUE] §d§lNOWHERE TO HIDE!");

        Collection<Entity> allPlayers = originalLoc.getWorld().getNearbyEntities(originalLoc, 50, 50, 50);
        int strikeDelay = 0;

        for (Entity entity : allPlayers) {
            if (entity instanceof Player) {
                Player player = (Player) entity;

                if (playersThatDamagedMe.contains(player.getUniqueId())) {
                    final int delay = strikeDelay;

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        // Teleport behind player
                        Vector direction = player.getLocation().getDirection().multiply(-2);
                        Location behindPlayer = player.getLocation().add(direction);

                        // Teleport effects
                        originalLoc.getWorld().spawnParticle(Particle.PORTAL, bukkitEntity.getLocation(), 100, 1, 1, 1, 2);
                        bukkitEntity.teleport(behindPlayer);
                        location = behindPlayer;

                        behindPlayer.getWorld().spawnParticle(Particle.PORTAL, behindPlayer, 100, 1, 1, 1, 2);
                        behindPlayer.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, behindPlayer, 5, 0, 0, 0, 0);
                        behindPlayer.getWorld().playSound(behindPlayer, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 0.5f);

                        // Strike the player
                        player.damage(calculateChanceDamage(30.0), bukkitEntity);
                        player.setVelocity(player.getLocation().getDirection().multiply(2).setY(1));

                        player.sendTitle("§4§lBEHIND YOU!", "§c§lAI TORQUE STRIKES!", 5, 30, 10);

                    }, delay);

                    strikeDelay += 30; // 1.5 second between each strike
                }
            }
        }

        // Return to original location after all strikes
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            originalLoc.getWorld().spawnParticle(Particle.PORTAL, bukkitEntity.getLocation(), 100, 1, 1, 1, 2);
            bukkitEntity.teleport(originalLoc);
            location = originalLoc;
            originalLoc.getWorld().spawnParticle(Particle.PORTAL, originalLoc, 100, 1, 1, 1, 2);
        }, strikeDelay + 20L);
    }

    // ============================================
    // APOCALYPTIC POWERS - ULTIMATE GOD MODE
    // ============================================

    /**
     * REALITY SHATTER - Tears apart reality itself
     * Destroys ALL blocks in 100 block radius, instakills all hostile mobs
     */
    private void unleashRealityShatter() {
        Location loc = bukkitEntity.getLocation();
        sendChatMessage("§0§l§k|||§r §4§l[AI TORQUE] REALITY... SHATTERS! §0§l§k|||");

        // Visual effect - massive explosion
        for (int i = 0; i < 500; i++) {
            loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER,
                loc.clone().add(
                    (Math.random() - 0.5) * 100,
                    (Math.random() - 0.5) * 100,
                    (Math.random() - 0.5) * 100
                ), 1);
        }

        // Kill ALL mobs in 100 block radius
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 100, 100, 100);
        int killCount = 0;
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity living = (LivingEntity) entity;
                if (!living.getUniqueId().equals(bukkitEntity.getUniqueId())) {
                    living.setHealth(0);
                    killCount++;
                }
            }
        }

        sendChatMessage("§4[AI TORQUE] §c" + killCount + " BEINGS ERASED FROM EXISTENCE!");

        // Create massive crater - destroy blocks in sphere
        int radius = 50;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + y*y + z*z <= radius*radius) {
                        Location blockLoc = loc.clone().add(x, y, z);
                        if (Math.random() < 0.3) { // 30% of blocks destroyed
                            blockLoc.getBlock().setType(org.bukkit.Material.AIR);
                        }
                    }
                }
            }
        }
    }

    /**
     * METEOR RAIN - Summons 100 flaming meteors from the sky
     */
    private void summonMeteorRain() {
        Location loc = bukkitEntity.getLocation();
        sendChatMessage("§6§l[AI TORQUE] §c§lTHE HEAVENS BURN!");

        for (int i = 0; i < 100; i++) {
            final int delay = i * 2; // 0.1 second apart

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Random location in 80 block radius
                double angle = Math.random() * 2 * Math.PI;
                double distance = Math.random() * 80;
                Location meteorLoc = loc.clone().add(
                    Math.cos(angle) * distance,
                    80, // High in sky
                    Math.sin(angle) * distance
                );

                // Spawn fireball
                org.bukkit.entity.Fireball meteor = loc.getWorld().spawn(meteorLoc, org.bukkit.entity.Fireball.class);
                meteor.setDirection(new org.bukkit.util.Vector(0, -1, 0));
                meteor.setYield(8.0f); // Massive explosion
                meteor.setIsIncendiary(true); // Lights fires

                // Trail effect
                for (int j = 0; j < 20; j++) {
                    meteorLoc.getWorld().spawnParticle(Particle.FLAME, meteorLoc.clone().add(0, -j, 0), 10, 0.5, 0.5, 0.5, 0.1);
                }
            }, delay);
        }
    }

    /**
     * TIME STOP - Freezes all players in place for 10 seconds
     */
    private void activateTimeStop() {
        Location loc = bukkitEntity.getLocation();
        sendChatMessage("§5§l[AI TORQUE] §d§lTIME ITSELF BOWS TO ME!");

        Collection<Entity> allPlayers = loc.getWorld().getNearbyEntities(loc, 200, 200, 200);

        for (Entity entity : allPlayers) {
            if (entity instanceof Player) {
                final Player player = (Player) entity;

                if (playersThatDamagedMe.contains(player.getUniqueId())) {
                    // Freeze player
                    player.setWalkSpeed(0);
                    player.setFlySpeed(0);
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOWNESS, 200, 255, false, false));
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.MINING_FATIGUE, 200, 255, false, false));
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.BLINDNESS, 200, 255, false, false));

                    player.sendTitle("§0§lTIME STOPPED", "§8You cannot move...", 10, 180, 10);

                    // Deal damage over time while frozen
                    for (int i = 0; i < 10; i++) {
                        final int tick = i;
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            player.damage(10.0, bukkitEntity); // 100 HP total
                            player.getLocation().getWorld().spawnParticle(Particle.WITCH, player.getLocation(), 50, 1, 2, 1, 0);
                        }, tick * 20L);
                    }

                    // Unfreeze after 10 seconds
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        player.setWalkSpeed(0.2f);
                        player.setFlySpeed(0.1f);
                    }, 200L);
                }
            }
        }
    }

    /**
     * OMEGA BEAM - Fires a massive laser that destroys EVERYTHING
     */
    private void fireOmegaBeam() {
        Location loc = bukkitEntity.getLocation();
        sendChatMessage("§c§l§k|||§r §4§l[AI TORQUE] OMEGA ANNIHILATION! §c§l§k|||");

        // Find nearest player that damaged us
        Player target = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 200, 200, 200)) {
            if (entity instanceof Player) {
                Player p = (Player) entity;
                if (playersThatDamagedMe.contains(p.getUniqueId())) {
                    double dist = p.getLocation().distance(loc);
                    if (dist < closestDist) {
                        closestDist = dist;
                        target = p;
                    }
                }
            }
        }

        if (target == null) return;

        final Player finalTarget = target;
        org.bukkit.util.Vector direction = finalTarget.getLocation().toVector().subtract(loc.toVector()).normalize();

        // Fire 200 block laser beam
        for (int i = 0; i < 200; i++) {
            final int distance = i;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Location beamLoc = loc.clone().add(direction.clone().multiply(distance));

                // Beam visuals
                beamLoc.getWorld().spawnParticle(Particle.END_ROD, beamLoc, 20, 0.5, 0.5, 0.5, 0);
                beamLoc.getWorld().spawnParticle(Particle.FLAME, beamLoc, 10, 0.3, 0.3, 0.3, 0);
                beamLoc.getWorld().spawnParticle(Particle.WITCH, beamLoc, 5, 0.2, 0.2, 0.2, 0);

                // Destroy blocks
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            Location destroyLoc = beamLoc.clone().add(dx, dy, dz);
                            destroyLoc.getBlock().setType(org.bukkit.Material.AIR);
                        }
                    }
                }

                // Damage entities
                Collection<Entity> hit = beamLoc.getWorld().getNearbyEntities(beamLoc, 3, 3, 3);
                for (Entity entity : hit) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (playersThatDamagedMe.contains(player.getUniqueId())) {
                            player.damage(50.0, bukkitEntity); // MASSIVE DAMAGE
                            player.setFireTicks(100);
                        }
                    }
                }
            }, i / 10);
        }
    }

    /**
     * SPAWN APOCALYPSE - Summons 50 powerful bosses
     */
    private void summonApocalypse() {
        Location loc = bukkitEntity.getLocation();
        sendChatMessage("§4§l[AI TORQUE] §c§lWITNESS THE APOCALYPSE!");

        for (int i = 0; i < 50; i++) {
            double angle = (2 * Math.PI * i) / 50;
            double radius = 30;
            Location spawnLoc = loc.clone().add(
                Math.cos(angle) * radius,
                0,
                Math.sin(angle) * radius
            );

            // Spawn different boss types
            org.bukkit.entity.EntityType bossType;
            switch (i % 5) {
                case 0: bossType = org.bukkit.entity.EntityType.WITHER; break;
                case 1: bossType = org.bukkit.entity.EntityType.ENDER_DRAGON; break;
                case 2: bossType = org.bukkit.entity.EntityType.RAVAGER; break;
                case 3: bossType = org.bukkit.entity.EntityType.WARDEN; break;
                default: bossType = org.bukkit.entity.EntityType.IRON_GOLEM;
            }

            LivingEntity boss = (LivingEntity) loc.getWorld().spawnEntity(spawnLoc, bossType);
            boss.setCustomName("§4Apocalypse Harbinger #" + (i+1));
            boss.setCustomNameVisible(true);

            if (boss instanceof org.bukkit.entity.Mob) {
                ((org.bukkit.entity.Mob) boss).setAggressive(true);
            }

            // Buff the boss
            boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(500);
            boss.setHealth(500);

            spawnLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc, 5);
        }
    }

    /**
     * INSTANT DEATH AURA - Kills anything that comes within 30 blocks
     */
    private void activateDeathAura() {
        sendChatMessage("§0§l[AI TORQUE] §4§lDEATH ITSELF SURROUNDS ME!");

        for (int i = 0; i < 200; i++) { // 10 seconds
            final int tick = i;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Location loc = bukkitEntity.getLocation();

                // Visual ring
                double radius = 30;
                for (double angle = 0; angle < 2 * Math.PI; angle += 0.1) {
                    Location particleLoc = loc.clone().add(
                        Math.cos(angle) * radius,
                        Math.sin(tick * 0.5) * 2,
                        Math.sin(angle) * radius
                    );
                    loc.getWorld().spawnParticle(Particle.WITCH, particleLoc, 1, 0, 0, 0, 0);
                }

                // Damage all entities
                Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 30, 30, 30);
                for (Entity entity : nearby) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (playersThatDamagedMe.contains(player.getUniqueId())) {
                            player.damage(5.0, bukkitEntity); // 5 HP per tick = 500 HP total
                            player.sendTitle("", "§4§lDEATH AURA", 0, 20, 0);
                        }
                    }
                }
            }, tick);
        }
    }

    /**
     * DIMENSION RIFT - Creates portals that teleport players to random locations
     */
    private void openDimensionRifts() {
        Location loc = bukkitEntity.getLocation();
        sendChatMessage("§5§l[AI TORQUE] §d§lREALITY TEARS OPEN!");

        for (int i = 0; i < 20; i++) {
            final int portalNum = i;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                double angle = (2 * Math.PI * portalNum) / 20;
                double radius = 40;
                Location riftLoc = loc.clone().add(
                    Math.cos(angle) * radius,
                    2,
                    Math.sin(angle) * radius
                );

                // Portal visuals for 10 seconds
                for (int j = 0; j < 200; j++) {
                    final int tick = j;
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        riftLoc.getWorld().spawnParticle(Particle.PORTAL, riftLoc, 50, 2, 2, 2, 1);

                        // Teleport players that get too close
                        Collection<Entity> nearby = riftLoc.getWorld().getNearbyEntities(riftLoc, 3, 3, 3);
                        for (Entity entity : nearby) {
                            if (entity instanceof Player) {
                                Player player = (Player) entity;
                                if (playersThatDamagedMe.contains(player.getUniqueId())) {
                                    // Random location 1000 blocks away
                                    double teleportAngle = Math.random() * 2 * Math.PI;
                                    Location teleportLoc = loc.clone().add(
                                        Math.cos(teleportAngle) * 1000,
                                        100,
                                        Math.sin(teleportAngle) * 1000
                                    );
                                    player.teleport(teleportLoc);
                                    player.sendTitle("§5§lDIMENSION RIFT!", "§d§lYou've been banished!", 10, 50, 10);
                                    player.damage(20.0, bukkitEntity);
                                }
                            }
                        }
                    }, tick);
                }
            }, portalNum * 5L);
        }
    }
}
