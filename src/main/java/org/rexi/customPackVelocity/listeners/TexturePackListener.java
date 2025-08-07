package org.rexi.customPackVelocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.rexi.customPackVelocity.CustomPackVelocity;
import org.rexi.customPackVelocity.utils.ConfigManager;

import java.util.concurrent.TimeUnit;

public class TexturePackListener {

    private final CustomPackVelocity plugin;
    private final ConfigManager configManager;
    private final ProxyServer server;

    public TexturePackListener(CustomPackVelocity plugin, ConfigManager configManager, ProxyServer server) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.server = server;
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        if (player.getUsername().contains(".") && configManager.getString("geyser_players").equals("true")) {
            return;
        }

        server.getScheduler().buildTask(plugin, () -> {
            plugin.sendGlobalPackToServer(player, "login");
        }).delay(1, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();

        String previousServer = event.getPreviousServer()
                .map(srv -> srv.getServerInfo().getName())
                .orElse("N/A");

        if (previousServer.equalsIgnoreCase("N/A")) return;

        if (player.getUsername().contains(".") && configManager.getString("geyser_players").equals("true")) {
            return;
        }

        RegisteredServer newServer = event.getServer();

        String serverName = newServer.getServerInfo().getName();
        // Aquí puedes decidir si el servidor tiene un pack propio:
        if (plugin.isSpecialServer(serverName) && plugin.shouldApplyOnJoin(serverName)) {
            // No mandar el global, dejar que el servidor lo maneje
            if (plugin.shouldApplyOnJoin(serverName)) {
                server.getConsoleCommandSource().sendMessage(configManager.deserialize(configManager.getMessagePrefix("server_own_pack").replace("{player}", player.getUsername()).replace("{server}", serverName)));
            }
        } else if (plugin.isSpecialServer(previousServer)) {
            // Si venía de un servidor especial, reestablece el pack global
            server.getScheduler().buildTask(plugin, () -> {
                plugin.sendGlobalPackToServer(player, newServer.getServerInfo().getName());
            }).delay(1, TimeUnit.SECONDS).schedule();
        }
    }
}
