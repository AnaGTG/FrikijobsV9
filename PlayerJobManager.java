package com.frikifest.frikijobs.manager;

import com.frikifest.frikijobs.FrikiJobs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJobManager {
    private final FrikiJobs plugin;
    private File file;
    private FileConfiguration config;
    private final Map<UUID, String> playerJobs = new HashMap<>();
    private final Map<UUID, String> playerTiers = new HashMap<>();
    private final Map<UUID, Double> playerEarnings = new HashMap<>();

    public PlayerJobManager(FrikiJobs plugin) {
        this.plugin = plugin;
        createFile();
    }

    private void createFile() {
        file = new File(plugin.getDataFolder(), "player_jobs.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("No se pudo crear player_jobs.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                if (config.isConfigurationSection(key)) {
                    playerJobs.put(uuid, config.getString(key + ".job"));
                    playerTiers.put(uuid, config.getString(key + ".tier", "TI"));
                    playerEarnings.put(uuid, config.getDouble(key + ".earnings", 0.0));
                } else {
                    playerJobs.put(uuid, config.getString(key));
                    playerTiers.put(uuid, "TI");
                    playerEarnings.put(uuid, 0.0);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public String getPlayerJob(UUID uuid) {
        return playerJobs.get(uuid);
    }

    public String getPlayerTier(UUID uuid) {
        return playerTiers.getOrDefault(uuid, "TI");
    }

    public double getPlayerEarnings(UUID uuid) {
        return playerEarnings.getOrDefault(uuid, 0.0);
    }

    public double addPlayerEarnings(UUID uuid, double amount) {
        double current = getPlayerEarnings(uuid);
        double updated = current + amount;
        playerEarnings.put(uuid, updated);

        if (playerJobs.containsKey(uuid)) {
            config.set(uuid + ".earnings", updated);
            save();
        }
        return updated;
    }

    public void setPlayerJob(UUID uuid, String jobId) {
        if (jobId == null) {
            playerJobs.remove(uuid);
            playerTiers.remove(uuid);
            playerEarnings.remove(uuid);
            config.set(uuid.toString(), null);
        } else {
            playerJobs.put(uuid, jobId);
            if (!playerTiers.containsKey(uuid)) {
                playerTiers.put(uuid, "TI");
            }
            if (!playerEarnings.containsKey(uuid)) {
                playerEarnings.put(uuid, 0.0);
            }
            config.set(uuid + ".job", jobId);
            config.set(uuid + ".tier", playerTiers.get(uuid));
            config.set(uuid + ".earnings", playerEarnings.get(uuid));
        }
        save();
    }

    public void setPlayerTier(UUID uuid, String tierId) {
        String formattedTier = tierId.toUpperCase();
        playerTiers.put(uuid, formattedTier);
        if (playerJobs.containsKey(uuid)) {
            config.set(uuid + ".tier", formattedTier);
            save();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("No se pudo guardar player_jobs.yml");
        }
    }
}
