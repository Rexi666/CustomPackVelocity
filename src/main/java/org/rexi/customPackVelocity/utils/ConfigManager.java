package org.rexi.customPackVelocity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConfigManager {

    private final Path configPath;
    private final YamlConfigurationLoader loader;

    public ConfigManager() {
        // Define la carpeta del plugin dentro de "plugins/"
        Path pluginFolder = Paths.get("plugins", "CustomPackVelocity");

        // Asegura que la carpeta del plugin existe
        if (!Files.exists(pluginFolder)) {
            try {
                Files.createDirectories(pluginFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Ruta correcta dentro de "plugins/VelocityUtils/"
        this.configPath = pluginFolder.resolve("config.yml");

        // 💡 Configura el YAML para evitar inline objects
        this.loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .nodeStyle(NodeStyle.BLOCK) // 🔥 Evita la serialización en una sola línea
                .build();
    }

    public void loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                saveConfig();
            } else {
                ConfigurationNode node = loader.load();
                // Cargar los mensajes si no existen

                if (node.node("global_pack", "url").empty()) {
                    node.node("global_pack", "url").set("");
                }
                if (node.node("global_pack", "hash").empty()) {
                    node.node("global_pack", "hash").set("");
                }

                if (node.node("geyser_players").empty()) {
                    node.node("geyser_players").set(false);
                }
                if (node.node("resend_on_reload").empty()) {
                    node.node("resend_on_reload").set(true);
                }

                if (node.node("special_servers").empty()) {
                    node.node("special_servers", "survival", "bycommand").set(false);
                    node.node("special_servers", "survival", "url").set("");
                    node.node("special_servers", "survival", "hash").set("");
                }

                if (node.node("messages", "prefix").empty()) {
                    node.node("messages", "prefix").set("&7[&aCustomPackVelocity&7]");
                }
                if (node.node("messages", "config_reloaded").empty()) {
                    node.node("messages", "config_reloaded").set("&eThe config has been reloaded.");
                }
                if (node.node("messages", "no_permission").empty()) {
                    node.node("messages", "no_permission").set("&cYou do not have permission to do that.");
                }
                if (node.node("messages", "send_player_not_found").empty()) {
                    node.node("messages", "send_player_not_found").set("&cPlayer &b{player} &cnot found.");
                }
                if (node.node("messages", "url_empty").empty()) {
                    node.node("messages", "url_empty").set("&cThe URL for the global pack is empty.");
                }
                if (node.node("messages", "pack_sent").empty()) {
                    node.node("messages", "pack_sent").set("&eThe global pack was sent to {player} on reason {reason}");
                }
                if (node.node("messages", "server_own_pack").empty()) {
                    node.node("messages", "server_own_pack").set("&eSending &b{player} &eto &b{server} &ewith their own pack.");
                }

                // Guardar en caso de que se hayan agregado valores predeterminados
                loader.save(node);
            }
        } catch (SerializationException e) {
            System.err.println("Error al serializar/deserializar la configuración.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error al leer/escribir el archivo de configuración.");
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            ConfigurationNode node = loader.createNode();

            // 💡 Crear la estructura correctamente sin inline mapping
            node.node("global_pack", "url").set("");
            node.node("global_pack", "hash").set("");

            node.node("geyser_players").set(false);
            node.node("resend_on_reload").set(true);

            node.node("special_servers", "survival", "bycommand").set(false);
            node.node("special_servers", "survival", "url").set("");
            node.node("special_servers", "survival", "hash").set("");

            node.node("messages", "prefix").set("&7[&aCustomPackVelocity&7]");
            node.node("messages", "config_reloaded").set("&eThe config has been reloaded.");
            node.node("messages", "no_permission").set("&cYou do not have permission to do that.");
            node.node("messages", "send_player_not_found").set("&cPlayer &b{player} &cnot found.");
            node.node("messages", "url_empty").set("&cThe URL for the global pack is empty.");
            node.node("messages", "pack_sent").set("&eThe global pack was sent to {player} on reason {reason}");
            node.node("messages", "server_own_pack").set("&eSending &b{player} &eto &b{server} &ewith their own pack.");

            loader.save(node);
        } catch (SerializationException e) {
            System.err.println("Error al serializar la configuración.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo de configuración.");
            e.printStackTrace();
        }
    }

    public String getMessage(String key) {
        try {
            ConfigurationNode node = loader.load();
            return node.node("messages", key).getString("&cMessage not found: " + key);
        } catch (IOException e) {
            e.printStackTrace();
            return "&cError loading message: " + key;
        }
    }

    public String getMessagePrefix(String key) {
        try {
            ConfigurationNode node = loader.load();
            return getMessage("prefix")+ " " +node.node("messages", key).getString("&cMessage not found: " + key);
        } catch (IOException e) {
            e.printStackTrace();
            return "&c[[CustomPackVelocity] ] Error loading message: " + key;
        }
    }

    public String getString(String key) {
        try {
            ConfigurationNode node = loader.load();
            String[] parts = key.split("\\.");
            for (String part : parts) {
                node = node.node(part);
            }
            String result = node.getString();
            return (result != null && !result.isBlank()) ? result : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean getBoolean(String key) {
        try {
            ConfigurationNode node = loader.load();
            for (String part : key.split("\\.")) {
                node = node.node(part);
            }
            return node.getBoolean(false);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getStringList(String key) {
        try {
            ConfigurationNode node = loader.load();
            for (String part : key.split("\\.")) {
                node = node.node(part);
            }
            return node.getList(String.class, List.of());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Component deserialize(String input) {
        // Si contiene <...> asumimos que es MiniMessage
        if (input.contains("<") && input.contains(">")) {
            try {
                return MiniMessage.miniMessage().deserialize(input);
            } catch (Exception e) {
                // En caso de error, usa como texto plano
                return Component.text(input);
            }
        }

        // Si no, asumimos que es con códigos &
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }
}
