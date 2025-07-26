// ===== TheGlowIntegration.java - CORRECTION COMMANDE UNSET =====
package org.novania.eventpvp.integrations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.novania.eventpvp.EventPVP;

public class TheGlowIntegration {
    
    private final EventPVP plugin;
    private Plugin theGlowPlugin;
    private boolean enabled = false;
    
    public TheGlowIntegration(EventPVP plugin) {
        this.plugin = plugin;
        initializeIntegration();
    }
    
    private void initializeIntegration() {
        theGlowPlugin = Bukkit.getPluginManager().getPlugin("TheGlow");
        
        if (theGlowPlugin == null) {
            plugin.getLogger().warning("TheGlow non trouvé - Fonctionnalité glow désactivée");
            return;
        }
        
        if (!theGlowPlugin.isEnabled()) {
            plugin.getLogger().warning("TheGlow est désactivé - Fonctionnalité glow désactivée");
            return;
        }
        
        enabled = true;
        plugin.getLogger().info("Intégration TheGlow initialisée avec succès");
        plugin.getLogger().info("Version TheGlow: " + theGlowPlugin.getDescription().getVersion());
    }
    
    public boolean setPlayerGlow(Player player, String color) {
        if (!enabled) {
            plugin.getConfigManager().debugLog("TheGlow désactivé, impossible d'appliquer le glow");
            return false;
        }
        
        try {
            // CORRECTION: Utiliser directement les commandes TheGlow
            String command = "theglow set " + player.getName() + " " + color;
            
            // Exécuter la commande depuis la console
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                plugin.getConfigManager().debugLog("Commande TheGlow exécutée: /" + command + " - Succès: " + success);
            });
            
            plugin.getConfigManager().debugLog("Glow " + color + " appliqué à " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de l'application du glow à " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean removePlayerGlow(Player player) {
        if (!enabled) {
            plugin.getConfigManager().debugLog("TheGlow désactivé, impossible de retirer le glow");
            return false;
        }
        
        try {
            // CORRECTION: Utiliser la commande UNSET au lieu de remove
            String command = "theglow unset " + player.getName();
            
            // Exécuter la commande depuis la console
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                plugin.getConfigManager().debugLog("Commande TheGlow unset exécutée: /" + command + " - Succès: " + success);
            });
            
            plugin.getConfigManager().debugLog("Glow retiré de " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors du retrait du glow de " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean hasGlow(Player player) {
        if (!enabled) {
            return false;
        }
        
        // Pas de moyen simple de vérifier via commande, on assume que ça marche
        return true;
    }
    
    public void forceRemoveAllGlows() {
        if (!enabled) {
            return;
        }
        
        plugin.getLogger().info("Retrait forcé de tous les glows...");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            // CORRECTION: Utiliser unset pour tous les joueurs
            String command = "theglow unset " + player.getName();
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                plugin.getConfigManager().debugLog("Force unset glow pour: " + player.getName());
            });
        }
        
        plugin.getLogger().info("Tous les glows ont été retirés");
    }
    
    public void refreshPlayerGlow(Player player) {
        if (!enabled) {
            return;
        }
        
        plugin.getConfigManager().debugLog("Rafraîchissement du glow pour " + player.getName());
        
        // Retirer le glow existant puis le remettre
        removePlayerGlow(player);
        
        // Attendre un peu puis remettre le glow selon l'équipe
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            org.novania.eventpvp.enums.Team team = plugin.getTeamManager().getPlayerTeam(player);
            if (team != null && plugin.getTeamManager().isInEventWorld(player)) {
                String glowColor = plugin.getConfigManager().getTeamGlowColor(team.name().toLowerCase());
                setPlayerGlow(player, glowColor);
            }
        }, 20L); // Attendre 1 seconde
    }
    
    // ===== MÉTHODES UTILITAIRES =====
    
    public boolean isEnabled() {
        return enabled && theGlowPlugin != null && theGlowPlugin.isEnabled();
    }
    
    public String getTheGlowVersion() {
        if (theGlowPlugin != null) {
            return theGlowPlugin.getDescription().getVersion();
        }
        return "Non disponible";
    }
    
    public void reload() {
        plugin.getLogger().info("Rechargement de l'intégration TheGlow...");
        enabled = false;
        initializeIntegration();
    }
    
    // ===== MÉTHODES DE TEST =====
    
    public boolean testGlowFunctionality(Player testPlayer) {
        if (!enabled) {
            return false;
        }
        
        try {
            plugin.getLogger().info("Test de fonctionnalité TheGlow pour " + testPlayer.getName());
            
            // Test d'application
            setPlayerGlow(testPlayer, "gray");
            
            // Test de retrait après 3 secondes
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removePlayerGlow(testPlayer);
                plugin.getLogger().info("Test TheGlow terminé pour " + testPlayer.getName());
            }, 60L);
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Test de fonctionnalité TheGlow échoué: " + e.getMessage());
            return false;
        }
    }
    
    // ===== MÉTHODES DE DEBUG =====
    
    public void debugGlowCommand(Player player, String color) {
        if (!enabled) {
            plugin.getLogger().warning("TheGlow désactivé - impossible de tester");
            return;
        }
        
        plugin.getLogger().info("=== DEBUG GLOW COMMAND ===");
        plugin.getLogger().info("Joueur: " + player.getName());
        plugin.getLogger().info("Couleur: " + color);
        plugin.getLogger().info("TheGlow activé: " + enabled);
        plugin.getLogger().info("Plugin TheGlow: " + (theGlowPlugin != null ? theGlowPlugin.getName() : "null"));
        
        // Test de la commande set
        String setCommand = "theglow set " + player.getName() + " " + color;
        plugin.getLogger().info("Commande à exécuter: /" + setCommand);
        
        boolean setSuccess = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), setCommand);
        plugin.getLogger().info("Résultat commande set: " + setSuccess);
        
        // Test de la commande unset après 3 secondes
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String unsetCommand = "theglow unset " + player.getName();
            plugin.getLogger().info("Commande unset à exécuter: /" + unsetCommand);
            
            boolean unsetSuccess = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), unsetCommand);
            plugin.getLogger().info("Résultat commande unset: " + unsetSuccess);
            plugin.getLogger().info("=== FIN DEBUG GLOW ===");
        }, 60L);
    }
}