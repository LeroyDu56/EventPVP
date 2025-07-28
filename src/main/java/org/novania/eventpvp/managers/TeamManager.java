// ===== TeamManager.java - CORRECTION RÉASSIGNATION ÉQUIPE =====
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
    
    // ===== GESTION DES ÉQUIPES - CORRECTION RÉASSIGNATION =====
    
    public void assignPlayerToTeam(Player player, Team team) {
        UUID uuid = player.getUniqueId();
        Team oldTeam = playerTeams.get(uuid);
        
        configManager.debugLog("=== ASSIGNATION ÉQUIPE ===");
        configManager.debugLog("Joueur: " + player.getName());
        configManager.debugLog("Ancienne équipe: " + (oldTeam != null ? oldTeam.toString() : "Aucune"));
        configManager.debugLog("Nouvelle équipe: " + team.toString());
        configManager.debugLog("Event actif: " + plugin.getEventManager().isEventActive());
        
        // CORRECTION: Retirer proprement l'ancienne équipe
        if (oldTeam != null) {
            configManager.debugLog("Retrait de l'ancienne équipe " + oldTeam);
            removePlayerFromTeamComplete(player, oldTeam);
        }
        
        // Assigner la nouvelle équipe
        playerTeams.put(uuid, team);
        
        // CORRECTION: Gérer l'event actif - Mettre à jour/Ajouter dans la base
        if (plugin.getEventManager().isEventActive()) {
            configManager.debugLog("Event actif - Mise à jour de la session");
            updatePlayerInActiveSession(player, team, oldTeam);
        }
        
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
        
        // Message de confirmation
        String teamName = configManager.getTeamDisplayName(team.name().toLowerCase());
        String teamColor = configManager.getTeamColorCode(team.name().toLowerCase());
        
        if (oldTeam != null) {
            String oldTeamName = configManager.getTeamDisplayName(oldTeam.name().toLowerCase());
            player.sendMessage(configManager.getPrefix() + 
                "§eTransféré de l'équipe §c" + oldTeamName + " §evers " + teamColor + teamName + " §e!");
        } else {
            player.sendMessage(configManager.getPrefix() + 
                configManager.getMessage("team_assigned", "team", teamColor + teamName));
        }
        
        // GLOW: Application immédiate si dans le monde event
        if (isInEventWorld(player)) {
            // Dans le monde event : téléporter puis glow
            teleportToTeamWarpWithGlow(player, team);
        } else {
            // Pas dans le monde event : juste informer
            String warpName = configManager.getTeamWarp(team.name().toLowerCase());
            player.sendMessage(configManager.getPrefix() + 
                "§eUtilisez §6/warp " + warpName + " §epour aller à votre base !");
        }
        
        configManager.debugLog("✅ Joueur " + player.getName() + " assigné à l'équipe " + team);
    }
    
    // CORRECTION: Nouvelle méthode pour retirer complètement un joueur d'une équipe
    private void removePlayerFromTeamComplete(Player player, Team oldTeam) {
        UUID uuid = player.getUniqueId();
        
        configManager.debugLog("=== RETRAIT ÉQUIPE COMPLET ===");
        configManager.debugLog("Joueur: " + player.getName());
        configManager.debugLog("Équipe à retirer: " + oldTeam);
        
        // 1. Retirer des permissions
        removeTeamPermissions(player);
        
        // 2. Retirer le glow
        removeGlow(player);
        
        // 3. CORRECTION: Si event actif, nettoyer les données de session
        if (plugin.getEventManager().isEventActive()) {
            configManager.debugLog("Nettoyage des données de session pour l'ancienne équipe");
            // Marquer comme ayant quitté l'ancienne équipe dans la DB
            plugin.getDatabaseManager().markPlayerLeftSession(uuid.toString());
        }
        
        // 4. Remettre en survival si c'était spectator
        if (oldTeam == Team.SPECTATOR && isInEventWorld(player)) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        
        configManager.debugLog("✅ Joueur " + player.getName() + " retiré complètement de l'équipe " + oldTeam);
    }
    
    // CORRECTION: Nouvelle méthode pour gérer les sessions actives lors des changements d'équipe
    private void updatePlayerInActiveSession(Player player, Team newTeam, Team oldTeam) {
        String playerUuid = player.getUniqueId().toString();
        String playerName = player.getName();
        String newTeamName = newTeam.name().toLowerCase();
        
        configManager.debugLog("=== MISE À JOUR SESSION ACTIVE ===");
        configManager.debugLog("Joueur: " + playerName);
        configManager.debugLog("Ancienne équipe: " + (oldTeam != null ? oldTeam.toString() : "Aucune"));
        configManager.debugLog("Nouvelle équipe: " + newTeam.toString());
        
        if (oldTeam != null) {
            // Le joueur change d'équipe pendant une session active
            configManager.debugLog("Changement d'équipe pendant session active");
            
            // CORRECTION: Réinitialiser les stats pour éviter les doublons
            plugin.getDatabaseManager().resetPlayerSessionStats(playerUuid);
            
            // Ajouter avec la nouvelle équipe
            plugin.getDatabaseManager().addPlayerToSession(playerUuid, playerName, newTeamName);
            
            configManager.debugLog("Stats reset et joueur re-ajouté avec nouvelle équipe");
        } else {
            // Nouveau joueur dans la session
            configManager.debugLog("Nouveau joueur ajouté à la session");
            plugin.getDatabaseManager().addPlayerToSession(playerUuid, playerName, newTeamName);
        }
    }
    
    public void removePlayerFromTeam(Player player) {
        UUID uuid = player.getUniqueId();
        Team team = playerTeams.remove(uuid);
        
        if (team != null) {
            configManager.debugLog("=== RETRAIT ÉQUIPE STANDARD ===");
            configManager.debugLog("Joueur: " + player.getName());
            configManager.debugLog("Équipe: " + team);
            
            removePlayerFromTeamComplete(player, team);
            
            player.sendMessage(configManager.getPrefix() + "§cVous avez été retiré de votre équipe.");
        }
    }
    
    // ===== MÉTHODES GLOW - INCHANGÉES =====
    
    // CORRECTION: Méthode pour téléporter et appliquer le glow
    private void teleportToTeamWarpWithGlow(Player player, Team team) {
        String warpName = configManager.getTeamWarp(team.name().toLowerCase());
        
        configManager.debugLog("=== ASSIGNATION - TÉLÉPORTATION + GLOW ===");
        configManager.debugLog("Joueur: " + player.getName());
        configManager.debugLog("Équipe: " + team);
        configManager.debugLog("Warp: " + warpName);
        
        // 1. Téléporter au warp
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String command = "warp " + warpName + " " + player.getName();
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            configManager.debugLog("Téléportation warp: " + success);
            
            if (success) {
                // 2. Attendre que la téléportation soit terminée puis appliquer le glow
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    configManager.debugLog("Application du glow après téléportation pour " + player.getName());
                    applyGlowWithRetry(player, team, 0);
                }, 40L); // 2 secondes après la téléportation
            } else {
                // Si la téléportation échoue, essayer quand même le glow
                player.sendMessage(configManager.getPrefix() + "§cErreur de téléportation au warp!");
                applyGlowWithRetry(player, team, 0);
            }
        }, 10L); // 0.5 seconde de délai initial
    }
    
    /**
     * NOUVELLE MÉTHODE: Application du glow avec retry automatique
     */
    public void applyGlowWithRetry(Player player, Team team, int attempt) {
        if (!configManager.isAutoGlow() || !glowIntegration.isEnabled()) {
            configManager.debugLog("Glow désactivé ou TheGlow non disponible");
            return;
        }
        
        // Vérifier l'immunité au glow
        if (player.hasPermission("eventpvp.glow.immune")) {
            configManager.debugLog("Joueur " + player.getName() + " immunisé contre le glow");
            return;
        }
        
        // Maximum 5 tentatives
        if (attempt >= 5) {
            plugin.getLogger().warning("ÉCHEC DÉFINITIF: Impossible d'appliquer le glow à " + player.getName() + " après 5 tentatives");
            player.sendMessage(configManager.getPrefix() + "§c⚠️ Impossible d'appliquer l'effet glow. Contactez un admin.");
            return;
        }
        
        String glowColor = configManager.getTeamGlowColor(team.name().toLowerCase());
        
        configManager.debugLog("=== APPLICATION GLOW AVEC RETRY ===");
        configManager.debugLog("Joueur: " + player.getName());
        configManager.debugLog("Équipe: " + team);
        configManager.debugLog("Couleur: " + glowColor);
        configManager.debugLog("Tentative: " + (attempt + 1) + "/5");
        configManager.debugLog("Dans monde event: " + isInEventWorld(player));
        
        // COMMANDE DIRECTE TheGlow - MÉTHODE GARANTIE
        String glowCommand = "theglow set " + player.getName() + " " + glowColor;
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            configManager.debugLog("Exécution commande: /" + glowCommand);
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), glowCommand);
            
            configManager.debugLog("Résultat commande: " + success);
            
            if (success) {
                // Succès - Message de confirmation
                sendGlowConfirmationMessage(player, team);
                configManager.debugLog("✅ SUCCÈS - Glow " + glowColor + " appliqué à " + player.getName());
            } else {
                // Échec - Programmer un retry
                configManager.debugLog("❌ ÉCHEC - Retry dans 2 secondes...");
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    applyGlowWithRetry(player, team, attempt + 1);
                }, 40L); // 2 secondes d'attente
            }
        });
    }
    
    /**
     * NOUVELLE MÉTHODE: Message de confirmation selon l'équipe
     */
    private void sendGlowConfirmationMessage(Player player, Team team) {
        String messageKey = "glow_applied_" + team.name().toLowerCase();
        String message = configManager.getMessage(messageKey);
        
        // Fallback si message pas trouvé
        if (message.contains("Message introuvable")) {
            String teamName = configManager.getTeamDisplayName(team.name().toLowerCase());
            String teamColor = configManager.getTeamColorCode(team.name().toLowerCase());
            String emoji = getTeamEmoji(team);
            message = teamColor + emoji + " Glow " + teamName.toLowerCase() + " appliqué - Prêt au combat !";
        }
        
        player.sendMessage(configManager.getPrefix() + message);
    }
    
    /**
     * ANCIENNE MÉTHODE MODIFIÉE: Plus simple, utilise la nouvelle méthode
     */
    public void applyTeamGlow(Player player, Team team) {
        configManager.debugLog("=== APPEL applyTeamGlow ===");
        configManager.debugLog("Joueur: " + player.getName());
        configManager.debugLog("Équipe: " + team);
        
        // Utiliser la nouvelle méthode avec retry
        applyGlowWithRetry(player, team, 0);
    }
    
    public void removeGlow(Player player) {
        if (glowIntegration.isEnabled()) {
            configManager.debugLog("=== RETRAIT GLOW ===");
            configManager.debugLog("Joueur: " + player.getName());
            
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
    
    // ===== AUTRES MÉTHODES INCHANGÉES =====
    
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
    
    // ===== GESTION DES PERMISSIONS - INCHANGÉE =====
    
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
        
        // Donner permission multiverse aux admins sans équipe
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
    
    // ===== GESTION DU MONDE EVENT - INCHANGÉE =====
    
    public boolean isInEventWorld(Player player) {
        boolean inEventWorld = player.getWorld().getName().equals(configManager.getWorldName());
        configManager.debugLog("Joueur " + player.getName() + " dans monde event: " + inEventWorld + 
            " (monde: " + player.getWorld().getName() + ", attendu: " + configManager.getWorldName() + ")");
        return inEventWorld;
    }
    
    /**
     * CORRECTION ENTRÉE MONDE EVENT: Glow immédiat
     */
    public void handlePlayerEnterEventWorld(Player player) {
        Team team = getPlayerTeam(player);
        
        configManager.debugLog("=== ENTRÉE MONDE EVENT ===");
        configManager.debugLog("Joueur: " + player.getName());
        configManager.debugLog("Équipe: " + (team != null ? team.toString() : "Aucune"));
        
        // Permettre aux admins sans équipe d'entrer
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
            
            // CORRECTION: Application immédiate du glow
            configManager.debugLog("Application immédiate du glow pour " + player.getName());
            applyGlowWithRetry(player, team, 0);
        }
        
        configManager.debugLog("Joueur " + player.getName() + " est entré dans le monde event avec l'équipe " + team);
    }
    
    public void handlePlayerLeaveEventWorld(Player player) {
        configManager.debugLog("=== SORTIE MONDE EVENT ===");
        configManager.debugLog("Joueur: " + player.getName());
        
        // Retirer le glow automatiquement
        removeGlow(player);
        
        // Remettre en survival si c'était spectator
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        
        configManager.debugLog("Joueur " + player.getName() + " a quitté le monde event");
    }
    
    // ===== TÉLÉPORTATION AUX WARPS - INCHANGÉE =====
    
    public void teleportToTeamWarp(Player player, Team team) {
        String warpName = configManager.getTeamWarp(team.name().toLowerCase());
        
        configManager.debugLog("=== TÉLÉPORTATION WARP STANDARD ===");
        configManager.debugLog("Joueur: " + player.getName());
        configManager.debugLog("Équipe: " + team);
        configManager.debugLog("Warp: " + warpName);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String command = "warp " + warpName + " " + player.getName();
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            configManager.debugLog("Commande warp exécutée: /" + command + " - Succès: " + success);
            
            if (success) {
                // Appliquer le glow après téléportation
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    applyGlowWithRetry(player, team, 0);
                }, 20L); // 1 seconde après la téléportation
            } else {
                plugin.getLogger().warning("Échec de la téléportation au warp " + warpName + " pour " + player.getName());
                player.sendMessage(configManager.getPrefix() + "§cErreur de téléportation au warp d'équipe!");
            }
        }, 10L);
    }
    
    /**
     * NOUVELLE MÉTHODE: Gestion complète du respawn avec glow
     */
    public void handlePlayerRespawn(Player player) {
        Team team = getPlayerTeam(player);
        
        configManager.debugLog("=== RESPAWN GLOW ===");
        configManager.debugLog("Joueur: " + player.getName());
        configManager.debugLog("Équipe: " + (team != null ? team.toString() : "Aucune"));
        
        if (team != null && isInEventWorld(player)) {
            // Attendre que le respawn soit complètement terminé
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                configManager.debugLog("Application du glow après respawn pour " + player.getName());
                applyGlowWithRetry(player, team, 0);
            }, 60L); // 3 secondes après le respawn
        }
    }
    
    // ===== VALIDATION DES WARPS - INCHANGÉE =====
    
    public boolean validateWarpAccess(Player player, String warpName) {
        // Admins peuvent accéder aux warps même sans équipe
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
    
    // ===== COMBAT ET PVP - INCHANGÉ =====
    
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
        
        // Vérifier le bypass ET empêcher team kill
        if (attackerTeam == victimTeam) {
            if (!attacker.hasPermission("eventpvp.teamkill.bypass")) {
                attacker.sendMessage(configManager.getPrefix() + configManager.getMessage("team_kill_denied"));
                return false;
            }
        }
        
        return true;
    }
    
    // ===== STATISTIQUES D'ÉQUIPE - INCHANGÉES =====
    
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
    
    // ===== NETTOYAGE - INCHANGÉ =====
    
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