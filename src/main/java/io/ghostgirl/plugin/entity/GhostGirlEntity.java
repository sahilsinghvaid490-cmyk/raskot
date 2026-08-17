package io.ghostgirl.plugin.entity;

import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Runtime-only wrapper around a spawned ghost girl armor stand.
 * Holds no persistent state - everything here is discarded once the
 * entity despawns or the plugin disables.
 */
public class GhostGirlEntity {

    private final UUID id;
    private final ArmorStand armorStand;
    private final UUID targetPlayerId;
    private final long spawnTimeMillis;
    private BukkitTask removalTask;

    public GhostGirlEntity(ArmorStand armorStand, UUID targetPlayerId) {
        this.id = UUID.randomUUID();
        this.armorStand = armorStand;
        this.targetPlayerId = targetPlayerId;
        this.spawnTimeMillis = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public UUID getTargetPlayerId() {
        return targetPlayerId;
    }

    public long getSpawnTimeMillis() {
        return spawnTimeMillis;
    }

    public void setRemovalTask(BukkitTask removalTask) {
        this.removalTask = removalTask;
    }

    public BukkitTask getRemovalTask() {
        return removalTask;
    }

    /**
     * @return true if the underlying entity still exists in the world and can be removed safely.
     */
    public boolean isValid() {
        return armorStand != null && !armorStand.isDead() && armorStand.isValid();
    }
}
