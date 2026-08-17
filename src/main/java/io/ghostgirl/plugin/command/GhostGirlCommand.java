package io.ghostgirl.plugin.command;

import io.ghostgirl.plugin.GhostGirlPlugin;
import io.ghostgirl.plugin.manager.ConfigManager;
import io.ghostgirl.plugin.manager.GhostGirlManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles /ghostgirl and /ghostgirl reload. Works identically from
 * a player or from the console.
 */
public class GhostGirlCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload");
    private static final String PERMISSION = "ghostgirl.admin";

    private final GhostGirlPlugin plugin;
    private final ConfigManager configManager;
    private final GhostGirlManager ghostGirlManager;

    public GhostGirlCommand(GhostGirlPlugin plugin, ConfigManager configManager, GhostGirlManager ghostGirlManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.ghostGirlManager = ghostGirlManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendStatus(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            handleReload(sender);
            return true;
        }

        sender.sendMessage(Component.text("Usage: /ghostgirl [reload]", NamedTextColor.YELLOW));
        return true;
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage(Component.text(stripLegacyColor(configManager.getStatusHeaderMessage()), NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("Spawning enabled: " + configManager.isSpawnEnabled(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Interval: " + configManager.getIntervalMinutes() + " minute(s)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Lifetime: " + configManager.getLifetimeSeconds() + " second(s)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Radius: " + configManager.getRadius() + " block(s)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Max spawn attempts: " + configManager.getMaxSpawnAttempts(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Active ghost girls right now: " + ghostGirlManager.getActiveCount(), NamedTextColor.GRAY));
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.reload();
            sender.sendMessage(Component.text(configManager.getReloadSuccessMessage(), NamedTextColor.GREEN));
        } catch (Exception exception) {
            sender.sendMessage(Component.text("Failed to reload configuration. Check the console for details.", NamedTextColor.RED));
            plugin.getLogger().severe("Error while reloading GhostGirl config: " + exception.getMessage());
        }
    }

    private String stripLegacyColor(String text) {
        // messages.status-header may contain legacy '&' color codes from config.yml;
        // strip them here since we render everything through Adventure Components instead.
        return text == null ? "" : text.replaceAll("(?i)&[0-9A-FK-OR]", "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            for (String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(partial)) {
                    suggestions.add(subcommand);
                }
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}
