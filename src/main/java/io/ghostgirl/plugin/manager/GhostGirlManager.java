package io.ghostgirl.plugin.manager;

import io.ghostgirl.plugin.GhostGirlPlugin;
import io.ghostgirl.plugin.entity.GhostGirlEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Owns the full lifecycle of every ghost girl currently in the world:
 * finding a safe spawn point, spawning the entity, playing horror effects,
 * and removing it again once its lifetime expires.
 */
public class GhostGirlManager {

    private final GhostGirlPlugin plugin;
    private final ConfigManager configManager;
    private final Random random = new Random();

    private final Map<UUID, GhostGirlEntity> activeGhosts = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToGhost = new ConcurrentHashMap<>();

    public GhostGirlManager(GhostGirlPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public boolean hasActiveGhost(Player player) {
        UUID ghostId = playerToGhost.get(player.getUniqueId());
        if (ghostId == null) {
            return false;
        }
        GhostGirlEntity entity = activeGhosts.get(ghostId);
        return entity != null && entity.isValid();
    }

    /**
     * Attempts to spawn a ghost girl near the given player. Fails silently
     * (no exception, no error spam) if no safe location can be found.
     */
    public void spawnNear(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (hasActiveGhost(player)) {
            return;
        }

        Location safeLocation = findSafeLocation(player);
        if (safeLocation == null) {
            plugin.getLogger().fine("No safe location found for " + player.getName() + ", skipping ghost girl spawn.");
            return;
        }

        try {
            ArmorStand armorStand = safeLocation.getWorld().spawn(safeLocation, ArmorStand.class, this::configureArmorStand);

            GhostGirlEntity ghostGirlEntity = new GhostGirlEntity(armorStand, player.getUniqueId());
            activeGhosts.put(ghostGirlEntity.getId(), ghostGirlEntity);
            playerToGhost.put(player.getUniqueId(), ghostGirlEntity.getId());

            applySpawnEffects(safeLocation);

            if (configManager.isAnnounceSpawn()) {
                player.sendMessage("§5§oYou feel a presence nearby...");
            }

            long lifetimeTicks = configManager.getLifetimeSeconds() * 20L;
            BukkitTask removalTask = plugin.getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> despawn(ghostGirlEntity.getId()),
                    lifetimeTicks
            );
            ghostGirlEntity.setRemovalTask(removalTask);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to spawn ghost girl near " + player.getName(), exception);
        }
    }

    private void configureArmorStand(ArmorStand stand) {
        // Invisible base + visible equipment is a stable, vanilla-only way to create
        // a floating humanoid silhouette without needing a real player skin or NPC library.
        stand.setVisible(false);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(true);
        stand.setSmall(false);
        stand.setMarker(false);
        stand.setCollidable(false);
        stand.setSilent(true);
        stand.setCustomNameVisible(false);
        stand.setPersistent(false);

        EntityEquipment equipment = stand.getEquipment();
        if (equipment == null) {
            return;
        }

        equipment.setHelmet(new ItemStack(Material.PLAYER_HEAD));
        equipment.setChestplate(createGhostlyLeather(Material.LEATHER_CHESTPLATE));
        equipment.setLeggings(createGhostlyLeather(Material.LEATHER_LEGGINGS));
        equipment.setBoots(createGhostlyLeather(Material.LEATHER_BOOTS));

        equipment.setHelmetDropChance(0f);
        equipment.setChestplateDropChance(0f);
        equipment.setLeggingsDropChance(0f);
        equipment.setBootsDropChance(0f);
    }

    private ItemStack createGhostlyLeather(Material material) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() instanceof LeatherArmorMeta meta) {
            meta.setColor(Color.fromRGB(235, 235, 240));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void applySpawnEffects(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        if (configManager.isSoundEnabled()) {
            Sound sound = resolveSound(configManager.getSoundName());
            world.playSound(location, sound, 1.0f, 0.6f);
        }

        if (configManager.isParticleEnabled()) {
            Particle particle = resolveParticle(configManager.getParticleName());
            world.spawnParticle(particle, location.clone().add(0, 1, 0), configManager.getParticleCount(), 0.3, 0.5, 0.3, 0.01);
        }

        if (configManager.isDarknessEnabled()) {
            applyDarknessNearby(world, location);
        }
    }

    private void applyDarknessNearby(World world, Location location) {
        int radius = configManager.getDarknessRadius();
        long radiusSquared = (long) radius * radius;
        int durationTicks = configManager.getDarknessDurationSeconds() * 20;

        for (Player onlinePlayer : world.getPlayers()) {
            if (onlinePlayer.getLocation().distanceSquared(location) <= radiusSquared) {
                onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, durationTicks, 0, true, false));
            }
        }
    }

    private Sound resolveSound(String name) {
        try {
            return Sound.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Invalid effects.sound-name in config.yml: '" + name + "'. Using default.");
            return Sound.AMBIENT_CAVE;
        }
    }

    private Particle resolveParticle(String name) {
        try {
            return Particle.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Invalid effects.particle-name in config.yml: '" + name + "'. Using default.");
            return Particle.SOUL;
        }
    }

    /**
     * Searches for a safe location within the configured radius of the player:
     * loaded chunk, solid ground, two blocks of headroom, no liquid/hazardous block,
     * and not directly on top of the player. Returns null if nothing suitable is found.
     */
    private Location findSafeLocation(Player player) {
        World world = player.getWorld();
        Location origin = player.getLocation();
        int radius = configManager.getRadius();
        int maxAttempts = configManager.getMaxSpawnAttempts();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 2 + (random.nextDouble() * Math.max(1, radius - 2));

            int blockX = origin.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int blockZ = origin.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);

            if (!world.isChunkLoaded(blockX >> 4, blockZ >> 4)) {
                continue;
            }

            int highestY = world.getHighestBlockYAt(blockX, blockZ);
            Location candidate = new Location(world, blockX + 0.5, highestY, blockZ + 0.5);

            if (candidate.distanceSquared(origin) < 4) {
                // Too close to (or on top of) the player - try again.
                continue;
            }

            if (isSafeLocation(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);

        if (!ground.getType().isSolid()) {
            return false;
        }

        if (!feet.isPassable() || !head.isPassable()) {
            return false;
        }

        Material groundType = ground.getType();
        return groundType != Material.LAVA
                && groundType != Material.WATER
                && groundType != Material.MAGMA_BLOCK
                && groundType != Material.CACTUS;
    }

    /**
     * Removes a single ghost girl by its internal id: cancels its pending removal
     * task and removes the underlying entity from the world, if still present.
     */
    public void despawn(UUID ghostId) {
        GhostGirlEntity entity = activeGhosts.remove(ghostId);
        if (entity == null) {
            return;
        }

        playerToGhost.remove(entity.getTargetPlayerId(), ghostId);

        if (entity.getRemovalTask() != null) {
            entity.getRemovalTask().cancel();
        }

        ArmorStand armorStand = entity.getArmorStand();
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
        }
    }

    public void despawnForPlayer(UUID playerId) {
        UUID ghostId = playerToGhost.get(playerId);
        if (ghostId != null) {
            despawn(ghostId);
        }
    }

    /**
     * Removes every currently active ghost girl and clears all tracked state.
     * Called on plugin disable and on config reload.
     */
    public void removeAll() {
        for (UUID ghostId : activeGhosts.keySet().toArray(new UUID[0])) {
            despawn(ghostId);
        }
        activeGhosts.clear();
        playerToGhost.clear();
    }

    public int getActiveCount() {
        return activeGhosts.size();
    }
}
