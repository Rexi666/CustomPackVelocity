package org.rexi.customPackVelocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.rexi.customPackVelocity.utils.ConfigManager;

import java.util.List;
import java.util.Optional;

public class CustomPackCommand implements SimpleCommand {

    private final ConfigManager configManager;
    private final ProxyServer server;
    private final CustomPackVelocity plugin;

    public CustomPackCommand(ConfigManager configManager, ProxyServer server, CustomPackVelocity plugin) {
        this.configManager = configManager;
        this.server = server;
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("custompackvelocity.admin")) {
            source.sendMessage(configManager.deserialize(configManager.getMessagePrefix("no_permission")));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(configManager.deserialize(configManager.getMessagePrefix("command_usage")));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                configManager.loadConfig();
                source.sendMessage(configManager.deserialize(configManager.getMessagePrefix("config_reloaded")));

                if (configManager.getBoolean("resend_on_reload")) {
                    for (Player player : server.getAllPlayers()) {
                        if (player.getUsername().contains(".") && configManager.getBoolean("geyser_players")) {
                            continue; // No enviar pack a jugadores de Geyser
                        }
                        plugin.sendGlobalPackToServer(player, "reload");
                    }
                }
            }

            case "version" -> {
                String version = plugin.getVersion();
                source.sendMessage(Component.text("CustomPackVelocity Version: " + version).color(NamedTextColor.GREEN));
            }

            case "send" -> {
                if (args.length < 2) {
                    source.sendMessage(Component.text("/cpv send <player>").color(NamedTextColor.RED));
                    return;
                }

                Optional<Player> optionalPlayer = server.getPlayer(args[1]);
                if (optionalPlayer.isEmpty()) {
                    String message = configManager.getMessage("send_player_not_found").replace("{player}", args[1]);
                    source.sendMessage(configManager.deserialize(message));
                    return;
                }

                Player target = optionalPlayer.get();
                plugin.sendGlobalPackToServer(target, "forcesend");
            }

            default -> source.sendMessage(configManager.deserialize(configManager.getMessagePrefix("command_usage")));

        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return List.of("reload", "version", "send");
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return List.of("reload", "version", "send").stream()
                    .filter(s -> s.startsWith(input))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            String partialName = args[1].toLowerCase();
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .toList();
        }

        return List.of();
    }
}