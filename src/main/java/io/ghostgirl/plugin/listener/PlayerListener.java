package io.ghostgirl.plugin.listener;

import io.ghostgirl.plugin.manager.GhostGirlManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Keeps the ghost girl system in sync with players joining and leaving.
 */
public class PlayerListener implements Listener {

    private final GhostGirlManager ghostGirlManager;

    public PlayerListener(GhostGirlManager ghostGirlManager) {
        this.ghostGirlManager = ghostGirlManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Intentionally no work here: the spawn task iterates over
        // Bukkit.getOnlinePlayers() every cycle, so a newly joined player
        // is automatically eligible for the next scheduled spawn with no
        // extra bookkeeping required.
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up any ghost girl that was tracking this player so it
        // doesn't linger or leak a scheduled removal task pointing at
        // a player who is no longer online.
        ghostGirlManager.despawnForPlayer(event.getPlayer().getUniqueId());
    }
}
