package org.novania.eventpvp.managers;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private SimpleDateFormat dateFormat;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        String format = config.getString("date_format", "dd/MM/yyyy HH:mm");
        this.dateFormat = new SimpleDateFormat(format);
    }
    
    public String getMessage(String key) {
        return config.getString("messages." + key, "Message introuvable: " + key)
                .replace("&", "§");
    }
    
    public String getMessage(String key, String placeholder, String value) {
        return getMessage(key).replace("{" + placeholder + "}", value);
    }
    
    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }
    
    public String getPrefix() {
        return getMessage("prefix");
    }
    
    public String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }
    
    // ===== PARAMÈTRES GÉNÉRAUX =====
    
    public String getWorldName() {
        return config.getString("general.world_name", "event");
    }
    
    public boolean isAutoGlow() {
        return config.getBoolean("general.auto_glow", true);
    }
    
    public boolean isSoundsEnabled() {
        return config.getBoolean("general.sounds_enabled", true);
    }
    
    public boolean isEffectsEnabled() {
        return config.getBoolean("general.effects_enabled", true);
    }
    
    public boolean isDebugMode() {
        return config.getBoolean("general.debug_mode", false);
    }
    
    // ===== CONDITIONS DE VICTOIRE =====
    
    public String getVictoryType() {
        return config.getString("victory.type", "team_points");
    }
    
    public int getVictoryTarget() {
        return config.getInt("victory.target", 50);
    }
    
    public boolean isAutoReset() {
        return config.getBoolean("victory.auto_reset", true);
    }
    
    public boolean isAnnounceProgress() {
        return config.getBoolean("victory.announce_progress", true);
    }
    
    public List<Integer> getProgressMilestones() {
        return config.getIntegerList("victory.milestones");
    }
    
    // ===== ÉQUIPES =====
    
    public String getTeamDisplayName(String team) {
        return config.getString("teams." + team + ".display_name", team);
    }
    
    public String getTeamColorCode(String team) {
        return config.getString("teams." + team + ".color_code", "&f").replace("&", "§");
    }
    
    public String getTeamGlowColor(String team) {
        return config.getString("teams." + team + ".glow_color", "white");
    }
    
    public String getTeamWarp(String team) {
        return config.getString("teams." + team + ".warp", "event" + team);
    }
    
    public String getTeamPermission(String team) {
        return config.getString("teams." + team + ".permission", "eventpvp.team." + team);
    }
    
    // ===== KITS =====
    
    public int getBuildKitUsageLimit() {
        return config.getInt("kits.build.usage_limit", 1);
    }
    
    public boolean isBuildKitResetOnDeath() {
        return config.getBoolean("kits.build.reset_on_death", false);
    }
    
    public int getCombatKitUsageLimit() {
        return config.getInt("kits.combat.usage_limit", -1);
    }
    
    public boolean isCombatKitResetOnDeath() {
        return config.getBoolean("kits.combat.reset_on_death", true);
    }
    
    // ===== RESPAWN =====
    
    public int getRespawnDelay() {
        return config.getInt("respawn.delay", 3);
    }
    
    public boolean isClearInventory() {
        return config.getBoolean("respawn.clear_inventory", true);
    }
    
    public boolean isKeepItems() {
        return config.getBoolean("respawn.keep_items", false);
    }
    
    public String getRespawnLocation() {
        return config.getString("respawn.location", "team_warp");
    }
    
    // ===== GUI =====
    
    public int getGuiSize() {
        return config.getInt("gui.size", 54);
    }
    
    public int getItemsPerPage() {
        return config.getInt("gui.items_per_page", 45);
    }
    
    public Sound getClickSound() {
        try {
            return Sound.valueOf(config.getString("gui.sounds.click", "UI_BUTTON_CLICK"));
        } catch (IllegalArgumentException e) {
            return Sound.UI_BUTTON_CLICK;
        }
    }
    
    public Sound getSuccessSound() {
        try {
            return Sound.valueOf(config.getString("gui.sounds.success", "ENTITY_PLAYER_LEVELUP"));
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_PLAYER_LEVELUP;
        }
    }
    
    public Sound getErrorSound() {
        try {
            return Sound.valueOf(config.getString("gui.sounds.error", "ENTITY_VILLAGER_NO"));
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_VILLAGER_NO;
        }
    }
    
    public Sound getKillSound() {
        try {
            return Sound.valueOf(config.getString("gui.sounds.kill", "ENTITY_EXPERIENCE_ORB_PICKUP"));
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }
    
    public Sound getDeathSound() {
        try {
            return Sound.valueOf(config.getString("gui.sounds.death", "ENTITY_PLAYER_DEATH"));
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_PLAYER_DEATH;
        }
    }
    
    public Sound getVictorySound() {
        try {
            return Sound.valueOf(config.getString("gui.sounds.victory", "UI_TOAST_CHALLENGE_COMPLETE"));
        } catch (IllegalArgumentException e) {
            return Sound.UI_TOAST_CHALLENGE_COMPLETE;
        }
    }
    
    public Sound getKitTakenSound() {
        try {
            return Sound.valueOf(config.getString("gui.sounds.kit_taken", "BLOCK_NOTE_BLOCK_PLING"));
        } catch (IllegalArgumentException e) {
            return Sound.BLOCK_NOTE_BLOCK_PLING;
        }
    }
    
    // ===== EFFETS =====
    
    public boolean isKillFirework() {
        return config.getBoolean("effects.kill_firework", true);
    }
    
    public boolean isVictoryFirework() {
        return config.getBoolean("effects.victory_firework", true);
    }
    
    public boolean isGlowParticles() {
        return config.getBoolean("effects.glow_particles", false);
    }
    
    // ===== SCOREBOARD =====
    
    public boolean isScoreboardEnabled() {
        return config.getBoolean("scoreboard.enabled", true);
    }
    
    public String getScoreboardTitle() {
        return config.getString("scoreboard.title", "&6⚔️ EVENT PVP ⚔️").replace("&", "§");
    }
    
    public int getScoreboardUpdateInterval() {
        return config.getInt("scoreboard.update_interval", 20);
    }
    
    // ===== BASE DE DONNÉES =====
    
    public String getDatabaseFile() {
        return config.getString("database.file", "eventpvp.db");
    }
    
    // ===== MÉTHODES UTILITAIRES =====
    
    public void debugLog(String message) {
        if (isDebugMode()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
}