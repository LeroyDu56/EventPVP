// ===== TeamManager.java - CORRECTIONS GLOW ET PVP =====
package org.novania.eventpvp.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.enums.Team;
import org.novania.eventpvp.integrations.TheGlowIntegration;

public class TeamManager {
    
    private final EventPVP plugin;
    private final ConfigManager configManager;
    private final TheGlowIntegration glowIntegration;
    
    // Stockage des équipes des joueurs
    private final Map<UUID, Team> playerTeams = new HashMap<>();
    // Stockage des permissions temporaires
    private final Map<UUID, PermissionAttachment> permissionAttachments = new HashMap<>();
    
    public TeamManager(EventPVP plugin, TheGlowIntegration glowIntegration) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.glowIntegration = glowIntegration;
    }
    
    // ===== GESTION DES ÉQUIPES =====
    
    public void assignPlayerToTeam(Player player, Team team) {
        UUID uuid = player.getUniqueId();
        Team oldTeam = playerTeams.get(uuid);
        
        // Retirer l'ancienne équipe si elle existe
        if (oldTeam != null) {
            removePlayerFromTeam(player);
        }
        
        // Assigner la nouvelle équipe
        playerTeams.put(uuid, team);
        
        // Donner les permissions de l'équipe
        giveTeamPermissions(player, team);
        
        // Appliquer le gamemode selon l'équipe
        if (team == Team.SPECTATOR) {
            if (isInEventWorld(player)) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        } else {
            if (isInEventWorld(player)) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
        
        // Appliquer le glow si le joueur est dans le monde event
        if (isInEventWorld(player)) {
            applyTeamGlow(player, team);
        }
        
        // Message de confirmation
        String teamName = configManager.getTeamDisplayName(team.name().toLowerCase());
        String teamColor = configManager.getTeamColorCode(team.name().toLowerCase());
        player.sendMessage(configManager.getPrefix() + 
            configManager.getMessage("team_assigned", "team", teamColor + teamName));
        
        configManager.debugLog("Joueur " + player.getName() + " assigné à l'équipe " + team);
    }
    
    public void removePlayerFromTeam(Player player) {
        UUID uuid = player.getUniqueId();
        Team team = playerTeams.remove(uuid);
        
        if (team != null) {
            // Retirer les permissions
            removeTeamPermissions(player);
            
            // Retirer le glow
            removeGlow(player);
            
            // Remettre en survival si c'était spectator
            if (team == Team.SPECTATOR && isInEventWorld(player)) {
                player.setGameMode(GameMode.SURVIVAL);
            }
            
            configManager.debugLog("Joueur " + player.getName() + " retiré de l'équipe " + team);
        }
    }
    
    public Team getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }
    
    public boolean hasTeam(Player player) {
        return playerTeams.containsKey(player.getUniqueId());
    }
    
    public boolean isInTeam(Player player, Team team) {
        return team.equals(getPlayerTeam(player));
    }
    
    public boolean canAccessWarp(Player player, String warpName) {
        Team team = getPlayerTeam(player);
        if (team == null) return false;
        
        String teamWarp = configManager.getTeamWarp(team.name().toLowerCase());
        return teamWarp.equalsIgnoreCase(warpName);
    }
    
    // ===== GESTION DES PERMISSIONS =====
    
    private void giveTeamPermissions(Player player, Team team) {
        // Retirer les anciennes permissions d'abord
        removeTeamPermissions(player);
        
        // Créer un nouveau attachment de permissions
        PermissionAttachment attachment = player.addAttachment(plugin);
        permissionAttachments.put(player.getUniqueId(), attachment);
        
        // Donner la permission de l'équipe
        String teamPermission = configManager.getTeamPermission(team.name().toLowerCase());
        attachment.setPermission(teamPermission, true);
        
        // Permissions générales de participation
        attachment.setPermission("eventpvp.participate", true);
        attachment.setPermission("eventpvp.kits.use", true);
        attachment.setPermission("eventpvp.stats.view", true);
        
        // Permissions spéciales pour les spectateurs
        if (team == Team.SPECTATOR) {
            attachment.setPermission("eventpvp.spectate", true);
        }
        
        // CORRECTION: Donner permission multiverse aux admins sans équipe
        if (player.hasPermission("eventpvp.admin.bypass")) {
            attachment.setPermission("multiverse.teleport.self.event", true);
        }
        
        configManager.debugLog("Permissions données à " + player.getName() + " pour l'équipe " + team);
    }
    
    private void removeTeamPermissions(Player player) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = permissionAttachments.remove(uuid);
        
        if (attachment != null) {
            player.removeAttachment(attachment);
            configManager.debugLog("Permissions retirées de " + player.getName());
        }
    }
    
    // ===== GESTION DU GLOW =====
    
    public void applyTeamGlow(Player player, Team team) {
        if (!configManager.isAutoGlow() || !glowIntegration.isEnabled()) {
            configManager.debugLog("Glow désactivé ou TheGlow non disponible");
            return;
        }
        
        // Vérifier l'immunité au glow
        if (player.hasPermission("eventpvp.glow.immune")) {
            configManager.debugLog("Joueur " + player.getName() + " immunisé contre le glow");
            return;
        }
        
        String glowColor = configManager.getTeamGlowColor(team.name().toLowerCase());
        
        // Essayer d'appliquer le glow plusieurs fois si nécessaire
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (glowIntegration.setPlayerGlow(player, glowColor)) {
                // Message selon l'équipe
                String messageKey = "glow_applied_" + team.name().toLowerCase();
                String message = configManager.getMessage(messageKey);
                if (message.contains("Message introuvable")) {
                    message = configManager.getMessage("glow_applied_rouge")
                        .replace("rouge", configManager.getTeamDisplayName(team.name().toLowerCase()))
                        .replace("🔴", getTeamEmoji(team));
                }
                player.sendMessage(configManager.getPrefix() + message);
                
                configManager.debugLog("Glow " + glowColor + " appliqué à " + player.getName());
            } else {
                configManager.debugLog("ÉCHEC: Impossible d'appliquer le glow à " + player.getName());
            }
        }, 10L); // Délai de 0.5 seconde
    }
    
    public void removeGlow(Player player) {
        if (glowIntegration.isEnabled()) {
            if (glowIntegration.removePlayerGlow(player)) {
                player.sendMessage(configManager.getPrefix() + configManager.getMessage("glow_removed"));
                configManager.debugLog("Glow retiré de " + player.getName());
            }
        }
    }
    
    private String getTeamEmoji(Team team) {
        switch (team) {
            case ROUGE: return "🔴";
            case BLEU: return "🔵";
            case SPECTATOR: return "⚪";
            default: return "❌";
        }
    }
    
    // ===== GESTION DU MONDE EVENT =====
    
    public boolean isInEventWorld(Player player) {
        return player.getWorld().getName().equals(configManager.getWorldName());
    }
    
    public void handlePlayerEnterEventWorld(Player player) {
        Team team = getPlayerTeam(player);
        
        // CORRECTION: Permettre aux admins sans équipe d'entrer
        if (team == null && !player.hasPermission("eventpvp.admin.bypass")) {
            // Joueur sans équipe - pas d'accès
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("no_access"));
            // Téléporter hors du monde event (spawn du serveur)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }, 5L);
            return;
        }
        
        if (team != null) {
            // Appliquer le gamemode selon l'équipe
            if (team == Team.SPECTATOR) {
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.SURVIVAL);
            }
            
            // Appliquer le glow selon l'équipe
            applyTeamGlow(player, team);
        }
        
        configManager.debugLog("Joueur " + player.getName() + " est entré dans le monde event avec l'équipe " + team);
    }
    
    public void handlePlayerLeaveEventWorld(Player player) {
        // Retirer le glow automatiquement
        removeGlow(player);
        
        // Remettre en survival si c'était spectator
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        
        configManager.debugLog("Joueur " + player.getName() + " a quitté le monde event");
    }
    
    // ===== VALIDATION DES WARPS =====
    
    public boolean validateWarpAccess(Player player, String warpName) {
        // CORRECTION: Admins peuvent accéder aux warps même sans équipe
        if (player.hasPermission("eventpvp.admin.bypass")) {
            return true;
        }
        
        // Vérifier si c'est un warp d'event
        if (!isEventWarp(warpName)) {
            return true; // Pas un warp d'event, laisser passer
        }
        
        Team team = getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("no_team"));
            return false;
        }
        
        String teamWarp = configManager.getTeamWarp(team.name().toLowerCase());
        if (!teamWarp.equalsIgnoreCase(warpName)) {
            String correctWarp = teamWarp;
            String teamName = configManager.getTeamDisplayName(team.name().toLowerCase());
            String teamColor = configManager.getTeamColorCode(team.name().toLowerCase());
            
            player.sendMessage(configManager.getPrefix() + 
                configManager.getMessage("wrong_warp", 
                    "team", teamColor + teamName,
                    "correct_warp", correctWarp));
            return false;
        }
        
        return true;
    }
    
    private boolean isEventWarp(String warpName) {
        return warpName.toLowerCase().startsWith("event");
    }
    
    // ===== COMBAT ET PVP =====
    
    public boolean canAttack(Player attacker, Player victim) {
        // Vérifier si on est dans le monde event
        if (!isInEventWorld(attacker) || !isInEventWorld(victim)) {
            return true; // Pas dans le monde event, règles normales
        }
        
        Team attackerTeam = getPlayerTeam(attacker);
        Team victimTeam = getPlayerTeam(victim);
        
        // Pas d'équipe = pas de combat (sauf admins)
        if (attackerTeam == null || victimTeam == null) {
            return false;
        }
        
        // Les spectateurs ne peuvent pas attaquer ni être attaqués
        if (attackerTeam == Team.SPECTATOR || victimTeam == Team.SPECTATOR) {
            return false;
        }
        
        // CORRECTION: Vérifier le bypass ET empêcher team kill
        if (attackerTeam == victimTeam) {
            if (!attacker.hasPermission("eventpvp.teamkill.bypass")) {
                attacker.sendMessage(configManager.getPrefix() + configManager.getMessage("team_kill_denied"));
                return false;
            }
        }
        
        return true;
    }
    
    // ===== STATISTIQUES D'ÉQUIPE =====
    
    public int getTeamPlayerCount(Team team) {
        return (int) playerTeams.values().stream()
                .filter(t -> t == team)
                .count();
    }
    
    public Map<Team, Integer> getTeamCounts() {
        Map<Team, Integer> counts = new HashMap<>();
        for (Team team : Team.values()) {
            counts.put(team, getTeamPlayerCount(team));
        }
        return counts;
    }
    
    public int getTotalPlayers() {
        return playerTeams.size();
    }
    
    public int getPlayersWithoutTeam() {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        return onlinePlayers - playerTeams.size();
    }
    
    // ===== NETTOYAGE =====
    
    public void cleanupOfflinePlayers() {
        playerTeams.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            return player == null || !player.isOnline();
        });
        
        permissionAttachments.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                // Nettoyer l'attachment si le joueur n'est plus là
                entry.getValue().remove();
                return true;
            }
            return false;
        });
    }
    
    // ===== MÉTHODES D'ACCÈS =====
    
    public Map<UUID, Team> getAllPlayerTeams() {
        return new HashMap<>(playerTeams);
    }
    
    public void clearAllTeams() {
        // Retirer tous les joueurs de leurs équipes
        for (UUID uuid : new HashMap<>(playerTeams).keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                removePlayerFromTeam(player);
            }
        }
        playerTeams.clear();
        
        configManager.debugLog("Toutes les équipes ont été effacées");
    }
}