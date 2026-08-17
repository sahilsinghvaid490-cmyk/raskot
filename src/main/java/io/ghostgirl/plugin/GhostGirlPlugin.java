package io.ghostgirl.plugin;

import io.ghostgirl.plugin.command.GhostGirlCommand;
import io.ghostgirl.plugin.listener.PlayerListener;
import io.ghostgirl.plugin.manager.ConfigManager;
import io.ghostgirl.plugin.manager.GhostGirlManager;
import io.ghostgirl.plugin.task.GhostGirlSpawnTask;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

/**
 * Entry point of the GhostGirl plugin.
 *
 * <p>Responsible for plugin lifecycle only - all gameplay logic lives in the
 * manager/task/listener/command classes so this class stays small and readable.</p>
 */
public final class GhostGirlPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private GhostGirlManager ghostGirlManager;
    private BukkitTask spawnTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.ghostGirlManager = new GhostGirlManager(this, configManager);

        registerListeners();
        registerCommands();

        startSpawnTask();

        getLogger().info("GhostGirl has awakened. She is watching...");
    }

    @Override
    public void onDisable() {
        stopSpawnTask();

        if (ghostGirlManager != null) {
            ghostGirlManager.removeAll();
        }

        getLogger().info("GhostGirl has gone back into the shadows.");
    }

    /**
     * Reloads configuration values, clears any currently active ghost girls
     * (since their behaviour may have depended on old settings) and restarts
     * the spawn task with the new interval.
     */
    public void reload() {
        stopSpawnTask();
        configManager.load();

        if (ghostGirlManager != null) {
            ghostGirlManager.removeAll();
        }

        startSpawnTask();
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(ghostGirlManager), this);
    }

    private void registerCommands() {
        GhostGirlCommand command = new GhostGirlCommand(this, configManager, ghostGirlManager);
        PluginCommand pluginCommand = getCommand("ghostgirl");

        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        } else {
            getLogger().log(Level.WARNING, "Failed to register /ghostgirl command. Check plugin.yml.");
        }
    }

    /**
     * Starts (or restarts) the repeating task that periodically spawns ghost girls
     * near eligible online players. Safe to call multiple times.
     */
    public void startSpawnTask() {
        if (configManager == null || !configManager.isSpawnEnabled()) {
            getLogger().info("Automatic ghost girl spawning is disabled in config.yml.");
            return;
        }

        long periodTicks = Math.max(20L, configManager.getIntervalMinutes() * 60L * 20L);

        this.spawnTask = getServer().getScheduler().runTaskTimer(
                this,
                new GhostGirlSpawnTask(this, ghostGirlManager, configManager),
                periodTicks,
                periodTicks
        );
    }

    public void stopSpawnTask() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GhostGirlManager getGhostGirlManager() {
        return ghostGirlManager;
    }
}
