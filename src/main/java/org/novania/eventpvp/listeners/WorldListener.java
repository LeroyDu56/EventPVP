// ===== WorldListener.java - CORRECTION COMMANDE UNSET =====
package org.novania.eventpvp.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.managers.TeamManager;

public class WorldListener implements Listener {
    
    private final EventPVP plugin;
    private final TeamManager teamManager;
    
    public WorldListener(EventPVP plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String eventWorldName = plugin.getConfigManager().getWorldName();
        String currentWorld = player.getWorld().getName();
        String previousWorld = event.getFrom().getName();
        
        plugin.getConfigManager().debugLog("Joueur " + player.getName() + " changé de monde: " + previousWorld + " -> " + currentWorld);
        
        // Le joueur ENTRE dans le monde event
        if (currentWorld.equals(eventWorldName)) {
            plugin.getConfigManager().debugLog("Joueur " + player.getName() + " ENTRE dans le monde event");
            teamManager.handlePlayerEnterEventWorld(player);
        }
        // Le joueur QUITTE le monde event
        else if (previousWorld.equals(eventWorldName)) {
            plugin.getConfigManager().debugLog("Joueur " + player.getName() + " QUITTE le monde event");
            handlePlayerLeaveEventWorld(player);
        }
    }
    
    // CORRECTION: Méthode dédiée pour quitter le monde event
    private void handlePlayerLeaveEventWorld(Player player) {
        plugin.getConfigManager().debugLog("=== PLAYER LEAVE EVENT WORLD ===");
        plugin.getConfigManager().debugLog("Joueur: " + player.getName());
        
        // PRIORITÉ 1: Retirer le glow immédiatement avec la bonne commande
        removePlayerGlowUnset(player);
        
        // Gérer la sortie du monde
        teamManager.handlePlayerLeaveEventWorld(player);
        
        // Si event actif, retirer le joueur
        if (plugin.getEventManager().isEventActive()) {
            plugin.getEventManager().removePlayerFromEvent(player);
        }
        
        plugin.getConfigManager().debugLog("Joueur " + player.getName() + " complètement retiré du monde event");
    }
    
    // CORRECTION: Méthode renforcée pour retirer le glow avec UNSET
    private void removePlayerGlowUnset(Player player) {
        try {
            plugin.getConfigManager().debugLog("=== REMOVE GLOW UNSET ===");
            plugin.getConfigManager().debugLog("Joueur: " + player.getName());
            
            // Méthode 1: Via notre intégration (utilise déjà unset maintenant)
            plugin.getGlowIntegration().removePlayerGlow(player);
            
            // Méthode 2: Commande directe UNSET en backup
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String unsetCommand = "theglow unset " + player.getName();
                boolean success = org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), unsetCommand);
                plugin.getConfigManager().debugLog("Commande glow unset forcée: /" + unsetCommand + " - Succès: " + success);
            }, 5L);
            
            // Méthode 3: Retry après délai plus long
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String unsetCommand2 = "theglow unset " + player.getName();
                boolean success2 = org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), unsetCommand2);
                plugin.getConfigManager().debugLog("Commande glow unset retry: /" + unsetCommand2 + " - Succès: " + success2);
            }, 20L);
            
            // Méthode 4: Dernière tentative avec délai encore plus long
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String unsetCommand3 = "theglow unset " + player.getName();
                boolean success3 = org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), unsetCommand3);
                plugin.getConfigManager().debugLog("Commande glow unset finale: /" + unsetCommand3 + " - Succès: " + success3);
            }, 60L);
            
            plugin.getConfigManager().debugLog("Glow unset programmé pour " + player.getName() + " (sortie monde event)");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors du retrait du glow de " + player.getName() + ": " + e.getMessage());
        }
    }
    
    // CORRECTION: Écouter aussi les téléportations
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        String eventWorldName = plugin.getConfigManager().getWorldName();
        String fromWorld = event.getFrom().getWorld().getName();
        String toWorld = event.getTo().getWorld().getName();
        
        // Si téléportation HORS du monde event
        if (fromWorld.equals(eventWorldName) && !toWorld.equals(eventWorldName)) {
            plugin.getConfigManager().debugLog("Téléportation hors du monde event détectée pour " + player.getName());
            
            // Programmer le retrait du glow après la téléportation
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removePlayerGlowUnset(player);
            }, 10L);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // Intercepter les commandes de warp pour les warps d'event
        if (command.startsWith("/warp ")) {
            String[] args = command.split(" ");
            if (args.length >= 2) {
                String warpName = args[1];
                
                // Vérifier l'accès au warp d'event
                if (!teamManager.validateWarpAccess(player, warpName)) {
                    event.setCancelled(true);
                }
            }
        }
        
        // CORRECTION: Détecter les commandes qui peuvent faire sortir du monde
        if (isWorldExitCommand(command)) {
            String eventWorldName = plugin.getConfigManager().getWorldName();
            if (player.getWorld().getName().equals(eventWorldName)) {
                plugin.getConfigManager().debugLog("Commande de sortie détectée: " + command);
                
                // Programmer le retrait du glow
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!player.getWorld().getName().equals(eventWorldName)) {
                        removePlayerGlowUnset(player);
                    }
                }, 20L);
            }
        }
    }
    
    private boolean isWorldExitCommand(String command) {
        return command.startsWith("/spawn") ||
               command.startsWith("/hub") ||
               command.startsWith("/lobby") ||
               command.startsWith("/warp ") ||
               command.startsWith("/tp ") ||
               command.startsWith("/tpa ") ||
               command.startsWith("/home") ||
               command.startsWith("/back") ||
               command.startsWith("/mv tp") ||
               command.startsWith("/multiverse tp");
    }
}