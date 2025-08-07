package org.rexi.customPackVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.rexi.customPackVelocity.listeners.TexturePackListener;
import org.rexi.customPackVelocity.utils.ConfigManager;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Plugin(id = "custompackvelocity", name = "CustomPackVelocity", version = BuildConstants.VERSION)
public class CustomPackVelocity {

    private final ProxyServer server;
    private final ConfigManager configManager;
    private final PluginContainer plugin;

    private final ChannelIdentifier PACK_CHANNEL = MinecraftChannelIdentifier.create("custompack", "main");

    @Inject
    public CustomPackVelocity(ProxyServer server, PluginContainer plugin) {
        this.server = server;
        this.plugin = plugin;
        this.configManager = new ConfigManager();
    }

    @Inject public Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configManager.loadConfig();

        server.getChannelRegistrar().register(PACK_CHANNEL);

        server.getEventManager().register(this, new TexturePackListener(this, configManager, server));

        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("custompackvelocity").build(),
                new CustomPackCommand(configManager, server, this));
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("cpv").build(),
                new CustomPackCommand(configManager, server, this));
    }

    public void sendGlobalPackToServer(Player player, String reason) {
        String url = configManager.getString("global_pack.url");
        String hash = configManager.getString("global_pack.hash");

        if (url.isEmpty()) {
            server.getConsoleCommandSource().sendMessage(configManager.deserialize(configManager.getMessagePrefix("url_empty")));
            return;
        }

        // Crear el mensaje para enviarlo al servidor Paper
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(out);

        try {
            data.writeUTF("SET_PACK"); // acción
            data.writeUTF(url);        // URL del pack
            data.writeUTF(hash);       // SHA1
            data.writeUTF(reason);     // "login" o nombre del servidor actual
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Enviar mensaje al servidor al que está conectado el jugador
        player.getCurrentServer().ifPresent(serverConn ->
                serverConn.sendPluginMessage(PACK_CHANNEL, out.toByteArray())
        );

        String message = configManager.getMessagePrefix("pack_sent")
                .replace("{player}", player.getUsername())
                .replace("{reason}", reason);
        server.getConsoleCommandSource().sendMessage(configManager.deserialize(message));
    }

    public boolean isSpecialServer(String serverName) {
        // Luego puedes cargar esto desde la config si lo necesitas
        return serverName.equalsIgnoreCase("eventos") || serverName.equalsIgnoreCase("pvp");
    }

    public String getVersion() {
        return BuildConstants.VERSION;
    }
}
