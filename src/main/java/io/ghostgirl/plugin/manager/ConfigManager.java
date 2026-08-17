package io.ghostgirl.plugin.manager;

import io.ghostgirl.plugin.GhostGirlPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Loads config.yml into typed, validated fields so the rest of the plugin
 * never touches raw configuration keys directly.
 */
public class ConfigManager {

    private final GhostGirlPlugin plugin;

    private boolean spawnEnabled;
    private long intervalMinutes;
    private int lifetimeSeconds;
    private int radius;
    private int maxSpawnAttempts;

    private boolean soundEnabled;
    private String soundName;
    private boolean particleEnabled;
    private String particleName;
    private int particleCount;
    private boolean darknessEnabled;
    private int darknessRadius;
    private int darknessDurationSeconds;

    private String reloadSuccessMessage;
    private String statusHeaderMessage;
    private boolean announceSpawn;

    public ConfigManager(GhostGirlPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * (Re)loads all values from config.yml on disk. Applies safe defaults and
     * clamps invalid values so a broken config file can never crash the plugin.
     */
    public void load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.spawnEnabled = config.getBoolean("spawn.enabled", true);
        this.intervalMinutes = Math.max(1, config.getInt("spawn.interval-minutes", 10));
        this.lifetimeSeconds = Math.max(1, config.getInt("spawn.lifetime-seconds", 10));
        this.radius = Math.max(1, config.getInt("spawn.radius", 5));
        this.maxSpawnAttempts = Math.max(1, config.getInt("spawn.max-spawn-attempts", 10));

        this.soundEnabled = config.getBoolean("effects.sound-enabled", true);
        this.soundName = config.getString("effects.sound-name", "AMBIENT_CAVE");
        this.particleEnabled = config.getBoolean("effects.particle-enabled", true);
        this.particleName = config.getString("effects.particle-name", "SOUL");
        this.particleCount = Math.max(1, config.getInt("effects.particle-count", 15));
        this.darknessEnabled = config.getBoolean("effects.darkness-effect", true);
        this.darknessRadius = Math.max(1, config.getInt("effects.darkness-radius", 10));
        this.darknessDurationSeconds = Math.max(1, config.getInt("effects.darkness-duration-seconds", 4));

        this.reloadSuccessMessage = config.getString("messages.reload-success", "Configuration reloaded.");
        this.statusHeaderMessage = config.getString("messages.status-header", "GhostGirl - Plugin Status");
        this.announceSpawn = config.getBoolean("features.announce-spawn", false);
    }

    public boolean isSpawnEnabled() {
        return spawnEnabled;
    }

    public long getIntervalMinutes() {
        return intervalMinutes;
    }

    public int getLifetimeSeconds() {
        return lifetimeSeconds;
    }

    public int getRadius() {
        return radius;
    }

    public int getMaxSpawnAttempts() {
        return maxSpawnAttempts;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public String getSoundName() {
        return soundName;
    }

    public boolean isParticleEnabled() {
        return particleEnabled;
    }

    public String getParticleName() {
        return particleName;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public boolean isDarknessEnabled() {
        return darknessEnabled;
    }

    public int getDarknessRadius() {
        return darknessRadius;
    }

    public int getDarknessDurationSeconds() {
        return darknessDurationSeconds;
    }

    public String getReloadSuccessMessage() {
        return reloadSuccessMessage;
    }

    public String getStatusHeaderMessage() {
        return statusHeaderMessage;
    }

    public boolean isAnnounceSpawn() {
        return announceSpawn;
    }
}
