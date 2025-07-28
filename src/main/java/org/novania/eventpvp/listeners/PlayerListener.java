// ===== PlayerListener.java - CORRECTION TEAMKILL COMPLET =====
package org.novania.eventpvp.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.projectiles.ProjectileSource;
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
        
        // CORRECTION: Retirer le glow de façon renforcée
        forceRemoveGlow(player);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Vérifier que la victime est un joueur
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        
        // Déterminer l'attaquant selon le type de dégât
        Player attacker = null;
        
        // CAS 1: Attaque directe (épée, main nue, etc.)
        if (event.getDamager() instanceof Player directAttacker) {
            attacker = directAttacker;
            plugin.getConfigManager().debugLog("Attaque directe détectée: " + attacker.getName() + " -> " + victim.getName());
        }
        // CAS 2: Projectile (arc, trident, etc.)
        else if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player projectileAttacker) {
                attacker = projectileAttacker;
                plugin.getConfigManager().debugLog("Attaque projectile détectée: " + attacker.getName() + " -> " + victim.getName() + 
                    " (projectile: " + projectile.getType() + ")");
            }
        }
        
        // Si pas d'attaquant joueur identifié, laisser passer
        if (attacker == null) {
            return;
        }
        
        // Vérifier si on est dans le monde event
        if (!teamManager.isInEventWorld(attacker) || !teamManager.isInEventWorld(victim)) {
            plugin.getConfigManager().debugLog("Attaque hors monde event, ignorée");
            return;
        }
        
        // Vérifier les règles de combat avec le système unifié
        if (!teamManager.canAttack(attacker, victim)) {
            event.setCancelled(true);
            
            // Log détaillé pour debug
            Team attackerTeam = teamManager.getPlayerTeam(attacker);
            Team victimTeam = teamManager.getPlayerTeam(victim);
            
            plugin.getConfigManager().debugLog("=== ATTAQUE BLOQUÉE ===");
            plugin.getConfigManager().debugLog("Attaquant: " + attacker.getName() + " (Équipe: " + attackerTeam + ")");
            plugin.getConfigManager().debugLog("Victime: " + victim.getName() + " (Équipe: " + victimTeam + ")");
            plugin.getConfigManager().debugLog("Type: " + (event.getDamager() instanceof Projectile ? "Projectile" : "Direct"));
            plugin.getConfigManager().debugLog("Dégâts: " + event.getFinalDamage());
            
            // Message informatif à l'attaquant (éviter le spam)
            if (attackerTeam == victimTeam) {
                // C'est un teamkill tenté
                long currentTime = System.currentTimeMillis();
                String lastMessageKey = "lastTeamkillMessage:" + attacker.getUniqueId();
                Long lastMessage = (Long) plugin.getConfig().get(lastMessageKey, 0L);
                
                // Envoyer message max 1 fois par 3 secondes pour éviter spam
                if (currentTime - lastMessage > 3000) {
                    attacker.sendMessage(plugin.getConfigManager().getPrefix() + 
                        plugin.getConfigManager().getMessage("team_kill_denied"));
                    plugin.getConfig().set(lastMessageKey, currentTime);
                }
            }
            
            return;
        }
        
        // Attaque autorisée, continuer le traitement normal
        plugin.getConfigManager().debugLog("Attaque autorisée: " + attacker.getName() + " -> " + victim.getName());
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
        
        // CORRECTION: Gestion améliorée du killer
        if (killer != null && teamManager.isInEventWorld(killer)) {
            // Vérifier que c'est un kill valide (pas de teamkill)
            Team killerTeam = teamManager.getPlayerTeam(killer);
            Team victimTeam = teamManager.getPlayerTeam(victim);
            
            if (killerTeam != null && victimTeam != null && killerTeam != victimTeam) {
                // Kill valide entre équipes ennemies
                eventManager.handlePlayerKill(killer, victim);
                plugin.getConfigManager().debugLog("Kill valide enregistré: " + killer.getName() + " -> " + victim.getName());
            } else if (killerTeam == victimTeam) {
                // Teamkill - ne pas compter
                plugin.getConfigManager().debugLog("Teamkill détecté et ignoré: " + killer.getName() + " -> " + victim.getName());
            }
        } else {
            // Mort sans killer ou killer hors monde event
            plugin.getConfigManager().debugLog("Mort sans killer valide: " + victim.getName());
        }
        
        plugin.getConfigManager().debugLog("Joueur " + victim.getName() + " mort dans le monde event");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Team playerTeam = teamManager.getPlayerTeam(player);
        
        plugin.getConfigManager().debugLog("=== RESPAWN EVENT ===");
        plugin.getConfigManager().debugLog("Joueur: " + player.getName());
        // CORRECTION: Vérification null
        String teamInfo = (playerTeam != null ? playerTeam.toString() : "Aucune");
        plugin.getConfigManager().debugLog("Équipe: " + teamInfo);
        
        // CORRECTION: Ne traiter que si le joueur a une équipe
        if (playerTeam != null) {
            // CORRECTION: NE PAS définir de respawn location ici, laisser Minecraft gérer
            // On va téléporter après coup avec les warps
            
            plugin.getConfigManager().debugLog("Programmation de la téléportation post-respawn...");
            
            // CORRECTION: Programmer la téléportation au warp APRÈS le respawn
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                handlePostRespawnTeleport(player, playerTeam);
            }, 5L); // 0.25 seconde après le respawn
        }
    }
    
    private void handlePostRespawnTeleport(Player player, Team team) {
        try {
            plugin.getConfigManager().debugLog("=== POST RESPAWN TELEPORT ===");
            plugin.getConfigManager().debugLog("Joueur: " + player.getName());
            plugin.getConfigManager().debugLog("Équipe: " + team.toString());
            plugin.getConfigManager().debugLog("Monde actuel: " + player.getWorld().getName());
            
            // CORRECTION: Éviter concaténation inefficace
            Location loc = player.getLocation();
            StringBuilder posBuilder = new StringBuilder("Position actuelle: ");
            posBuilder.append(loc.getBlockX()).append(", ")
                     .append(loc.getBlockY()).append(", ")
                     .append(loc.getBlockZ());
            plugin.getConfigManager().debugLog(posBuilder.toString());
            
            // CORRECTION: Téléporter au warp de l'équipe
            String warpName = plugin.getConfigManager().getTeamWarp(team.name().toLowerCase());
            plugin.getConfigManager().debugLog("Warp cible: " + warpName);
            
            // Exécuter la commande de warp
            String warpCommand = "warp " + warpName + " " + player.getName();
            plugin.getConfigManager().debugLog("Commande warp: /" + warpCommand);
            
            boolean warpSuccess = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), warpCommand);
            plugin.getConfigManager().debugLog("Succès warp: " + warpSuccess);
            
            if (warpSuccess) {
                // Programmer les actions post-téléportation
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    handlePostWarpActions(player, team);
                }, 20L); // 1 seconde après le warp
            } else {
                String warningMsg = "Échec de la téléportation au warp " + warpName + " pour " + player.getName();
                plugin.getLogger().warning(warningMsg);
                // Fallback: utiliser les coordonnées de secours
                teleportToFallbackLocation(player, team);
            }
            
        } catch (Exception e) {
            // CORRECTION: Éviter la concaténation inefficace
            String errorMsg = "Erreur lors de la téléportation post-respawn de " + player.getName() + ": " + e.getMessage();
            plugin.getLogger().warning(errorMsg);
            // CORRECTION: Ne pas utiliser printStackTrace en production
            if (plugin.getConfigManager().isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
    
    private void handlePostWarpActions(Player player, Team team) {
        try {
            plugin.getConfigManager().debugLog("=== POST WARP ACTIONS ===");
            plugin.getConfigManager().debugLog("Joueur: " + player.getName());
            
            // CORRECTION: Éviter concaténation inefficace
            Location loc = player.getLocation();
            StringBuilder posBuilder = new StringBuilder("Position après warp: ");
            posBuilder.append(loc.getBlockX()).append(", ")
                     .append(loc.getBlockY()).append(", ")
                     .append(loc.getBlockZ());
            plugin.getConfigManager().debugLog(posBuilder.toString());
            
            // Appliquer le gamemode selon l'équipe
            if (team == Team.SPECTATOR) {
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.SURVIVAL);
            }
            
            // CORRECTION: Appeler la méthode du TeamManager pour gérer le respawn et le glow
            teamManager.handlePlayerRespawn(player);
            
            // Messages de respawn
            String teamName = plugin.getConfigManager().getTeamDisplayName(team.name().toLowerCase());
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("respawn_location", "team", teamName));
            
            if (team != Team.SPECTATOR) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + 
                    plugin.getConfigManager().getMessage("take_kit"));
            }
            
            plugin.getConfigManager().debugLog("Actions post-warp terminées pour " + player.getName());
            
        } catch (Exception e) {
            // CORRECTION: Éviter la concaténation inefficace
            String errorMsg = "Erreur lors des actions post-warp pour " + player.getName() + ": " + e.getMessage();
            plugin.getLogger().warning(errorMsg);
        }
    }
    
    private void teleportToFallbackLocation(Player player, Team team) {
        plugin.getConfigManager().debugLog("=== FALLBACK TELEPORT ===");
        plugin.getLogger().warning("Utilisation des coordonnées de fallback pour " + player.getName());
        
        World eventWorld = Bukkit.getWorld(plugin.getConfigManager().getWorldName());
        if (eventWorld == null) {
            plugin.getLogger().severe("Monde event introuvable pour le fallback!");
            return;
        }
        
        Location fallbackLocation = getFallbackLocation(team, eventWorld);
        if (fallbackLocation != null) {
            player.teleport(fallbackLocation);
            
            // CORRECTION: Éviter concaténation inefficace
            StringBuilder coordBuilder = new StringBuilder("Téléportation fallback réussie: ");
            coordBuilder.append(fallbackLocation.getBlockX()).append(", ")
                       .append(fallbackLocation.getBlockY()).append(", ")
                       .append(fallbackLocation.getBlockZ());
            plugin.getConfigManager().debugLog(coordBuilder.toString());
            
            // Actions post-téléportation
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                handlePostWarpActions(player, team);
            }, 10L);
        }
    }
    
    private Location getFallbackLocation(Team team, World eventWorld) {
        // CORRECTION: Utiliser les coordonnées de la config ou des valeurs par défaut
        double x, y, z;
        float yaw, pitch; // CORRECTION: Utiliser float au lieu de double
        
        // CORRECTION: Utiliser switch avec règles modernes
        switch (team) {
            case ROUGE -> {
                x = plugin.getConfig().getDouble("respawn.coordinates.rouge.x", 100.5);
                y = plugin.getConfig().getDouble("respawn.coordinates.rouge.y", 65.0);
                z = plugin.getConfig().getDouble("respawn.coordinates.rouge.z", 100.5);
                yaw = (float) plugin.getConfig().getDouble("respawn.coordinates.rouge.yaw", 0.0);
                pitch = (float) plugin.getConfig().getDouble("respawn.coordinates.rouge.pitch", 0.0);
            }
            case BLEU -> {
                x = plugin.getConfig().getDouble("respawn.coordinates.bleu.x", -99.5);
                y = plugin.getConfig().getDouble("respawn.coordinates.bleu.y", 65.0);
                z = plugin.getConfig().getDouble("respawn.coordinates.bleu.z", -99.5);
                yaw = (float) plugin.getConfig().getDouble("respawn.coordinates.bleu.yaw", 180.0);
                pitch = (float) plugin.getConfig().getDouble("respawn.coordinates.bleu.pitch", 0.0);
            }
            case SPECTATOR -> {
                x = plugin.getConfig().getDouble("respawn.coordinates.spectator.x", 0.5);
                y = plugin.getConfig().getDouble("respawn.coordinates.spectator.y", 70.0);
                z = plugin.getConfig().getDouble("respawn.coordinates.spectator.z", 0.5);
                yaw = (float) plugin.getConfig().getDouble("respawn.coordinates.spectator.yaw", 0.0);
                pitch = (float) plugin.getConfig().getDouble("respawn.coordinates.spectator.pitch", 0.0);
            }
            default -> {
                return eventWorld.getSpawnLocation();
            }
        }
        
        // CORRECTION: Utiliser le constructeur correct pour Location
        return new Location(eventWorld, x, y, z, yaw, pitch);
    }
    
    // CORRECTION: Méthode pour retirer le glow de façon renforcée
    private void forceRemoveGlow(Player player) {
        try {
            // Tentative immédiate
            plugin.getGlowIntegration().removePlayerGlow(player);
            
            // Tentative avec délai
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String removeCommand = "theglow unset " + player.getName();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), removeCommand);
            }, 5L);
            
            // Tentative finale
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String removeCommand = "theglow unset " + player.getName();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), removeCommand);
            }, 20L);
            
        } catch (Exception e) {
            // CORRECTION: Éviter la concaténation inefficace
            String errorMsg = "Erreur lors du retrait forcé du glow: " + e.getMessage();
            plugin.getLogger().warning(errorMsg);
        }
    }
}