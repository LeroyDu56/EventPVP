// ===== PlayerListener.java - CORRECTIONS MAJEURES =====
package org.novania.eventpvp.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.enums.Team;
import org.novania.eventpvp.managers.EventManager;
import org.novania.eventpvp.managers.TeamManager;

public class PlayerListener implements Listener {
    
    private final EventPVP plugin;
    private final EventManager eventManager;
    private final TeamManager teamManager;
    
    public PlayerListener(EventPVP plugin, EventManager eventManager, TeamManager teamManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.teamManager = teamManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Si le joueur rejoint et est dans le monde event, appliquer les règles
        if (teamManager.isInEventWorld(player)) {
            teamManager.handlePlayerEnterEventWorld(player);
        }
        
        // Nettoyer les données des joueurs hors ligne
        teamManager.cleanupOfflinePlayers();
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Retirer le joueur de l'event s'il était dedans
        if (eventManager.isEventActive() && teamManager.isInEventWorld(player)) {
            eventManager.removePlayerFromEvent(player);
        }
        
        // Retirer le glow au cas où
        teamManager.removeGlow(player);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Vérifier que c'est PvP dans le monde event
        if (!(event.getEntity() instanceof Player victim) || 
            !(event.getDamager() instanceof Player attacker)) {
            return;
        }
        
        // Vérifier si on est dans le monde event
        if (!teamManager.isInEventWorld(attacker) || !teamManager.isInEventWorld(victim)) {
            return;
        }
        
        // Vérifier les règles de combat
        if (!teamManager.canAttack(attacker, victim)) {
            event.setCancelled(true);
            plugin.getLogger().info("[PvP Blocked] " + attacker.getName() + " tried to attack " + victim.getName());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        // Vérifier si c'est dans le monde event
        if (!teamManager.isInEventWorld(victim)) {
            return;
        }
        
        // CORRECTION: Empêcher le drop d'items dans le monde event
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // Gérer la mort dans l'event
        eventManager.handlePlayerDeath(victim);
        
        // Vérifier qu'il y a bien un tueur joueur
        if (killer != null && teamManager.isInEventWorld(killer)) {
            // Gérer le kill dans le système d'event
            eventManager.handlePlayerKill(killer, victim);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Team playerTeam = teamManager.getPlayerTeam(player);
        
        // Si le joueur est dans une équipe et l'event est actif
        if (eventManager.isEventActive() && playerTeam != null) {
            // Programmer la téléportation après le respawn
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                handleEventRespawn(player, playerTeam);
            }, 1L);
        }
    }
    
    private void handleEventRespawn(Player player, Team team) {
        try {
            Location respawnLocation = getTeamRespawnLocation(team);
            if (respawnLocation != null) {
                player.teleport(respawnLocation);
                
                // Appliquer le gamemode spectator pour les spectateurs
                if (team == Team.SPECTATOR) {
                    player.setGameMode(GameMode.SPECTATOR);
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                }
                
                // Messages de respawn
                String teamName = plugin.getConfigManager().getTeamDisplayName(team.name().toLowerCase());
                player.sendMessage(plugin.getConfigManager().getPrefix() + 
                    plugin.getConfigManager().getMessage("respawn_location", "team", teamName));
                
                if (team != Team.SPECTATOR) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + 
                        plugin.getConfigManager().getMessage("take_kit"));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors du respawn de " + player.getName() + ": " + e.getMessage());
        }
    }
    
    private Location getTeamRespawnLocation(Team team) {
        String worldName = plugin.getConfigManager().getWorldName();
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        
        if (world == null) {
            return null;
        }
        
        // Coordonnées par défaut (vous devez les configurer selon votre serveur)
        switch (team) {
            case ROUGE:
                return new Location(world, 100, 65, 100); // Remplacez par vos coordonnées
            case BLEU:
                return new Location(world, -100, 65, -100); // Remplacez par vos coordonnées
            case SPECTATOR:
                return new Location(world, 0, 70, 0); // Remplacez par vos coordonnées
            default:
                return world.getSpawnLocation();
        }
    }
}