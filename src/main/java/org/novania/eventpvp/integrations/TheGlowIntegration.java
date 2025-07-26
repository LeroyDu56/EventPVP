package org.novania.eventpvp.integrations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.novania.eventpvp.EventPVP;

import java.lang.reflect.Method;

public class TheGlowIntegration {
    
    private final EventPVP plugin;
    private Plugin theGlowPlugin;
    private boolean enabled = false;
    
    // Méthodes réflectives pour TheGlow
    private Method setGlowMethod;
    private Method removeGlowMethod;
    private Method hasGlowMethod;
    
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
        
        try {
            // Recherche des méthodes de TheGlow via réflection
            // Note: Ces noms de méthodes peuvent changer selon la version de TheGlow
            Class<?> glowAPIClass = Class.forName("fr.skytasul.glow.api.GlowAPI");
            
            // Méthode pour appliquer un glow
            setGlowMethod = glowAPIClass.getMethod("setGlow", Player.class, String.class);
            
            // Méthode pour retirer un glow
            removeGlowMethod = glowAPIClass.getMethod("removeGlow", Player.class);
            
            // Méthode pour vérifier si un joueur a un glow
            hasGlowMethod = glowAPIClass.getMethod("hasGlow", Player.class);
            
            enabled = true;
            plugin.getLogger().info("Intégration TheGlow initialisée avec succès");
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Classe GlowAPI de TheGlow non trouvée: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            plugin.getLogger().warning("Méthode TheGlow non trouvée: " + e.getMessage());
            plugin.getLogger().warning("Votre version de TheGlow pourrait être incompatible");
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de l'initialisation de TheGlow: " + e.getMessage());
        }
    }
    
    public boolean setPlayerGlow(Player player, String color) {
        if (!enabled || setGlowMethod == null) {
            return false;
        }
        
        try {
            setGlowMethod.invoke(null, player, color);
            plugin.getConfigManager().debugLog("Glow " + color + " appliqué à " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de l'application du glow à " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean removePlayerGlow(Player player) {
        if (!enabled || removeGlowMethod == null) {
            return false;
        }
        
        try {
            removeGlowMethod.invoke(null, player);
            plugin.getConfigManager().debugLog("Glow retiré de " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors du retrait du glow de " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean hasGlow(Player player) {
        if (!enabled || hasGlowMethod == null) {
            return false;
        }
        
        try {
            return (Boolean) hasGlowMethod.invoke(null, player);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors de la vérification du glow de " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    public void forceRemoveAllGlows() {
        if (!enabled) {
            return;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            removePlayerGlow(player);
        }
        
        plugin.getLogger().info("Tous les glows ont été retirés");
    }
    
    public void refreshPlayerGlow(Player player) {
        if (!enabled) {
            return;
        }
        
        // Retirer le glow existant puis le remettre
        if (hasGlow(player)) {
            removePlayerGlow(player);
            
            // Redéterminer la couleur selon l'équipe
            org.novania.eventpvp.enums.Team team = plugin.getTeamManager().getPlayerTeam(player);
            if (team != null && plugin.getTeamManager().isInEventWorld(player)) {
                String glowColor = plugin.getConfigManager().getTeamGlowColor(team.name().toLowerCase());
                setPlayerGlow(player, glowColor);
            }
        }
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
            // Test d'application
            setPlayerGlow(testPlayer, "red");
            
            // Vérifier que le glow est appliqué
            boolean hasGlowAfterSet = hasGlow(testPlayer);
            
            // Test de retrait
            removePlayerGlow(testPlayer);
            
            // Vérifier que le glow est retiré
            boolean hasGlowAfterRemove = hasGlow(testPlayer);
            
            return hasGlowAfterSet && !hasGlowAfterRemove;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Test de fonctionnalité TheGlow échoué: " + e.getMessage());
            return false;
        }
    }
}