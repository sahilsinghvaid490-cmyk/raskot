package io.ghostgirl.plugin.task;

import io.ghostgirl.plugin.GhostGirlPlugin;
import io.ghostgirl.plugin.manager.ConfigManager;
import io.ghostgirl.plugin.manager.GhostGirlManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Runs on a fixed interval (configurable, default every 10 minutes).
 * Iterates over currently online players only - no world scanning - and
 * asks the manager to try spawning a ghost girl near each eligible one.
 */
public class GhostGirlSpawnTask implements Runnable {

    private final GhostGirlPlugin plugin;
    private final GhostGirlManager ghostGirlManager;
    private final ConfigManager configManager;

    public GhostGirlSpawnTask(GhostGirlPlugin plugin, GhostGirlManager ghostGirlManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.ghostGirlManager = ghostGirlManager;
        this.configManager = configManager;
    }

    @Override
    public void run() {
        if (!configManager.isSpawnEnabled()) {
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isEligible(player)) {
                ghostGirlManager.spawnNear(player);
            }
        }
    }

    private boolean isEligible(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }
        return !ghostGirlManager.hasActiveGhost(player);
    }
}
