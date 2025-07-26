// ===== EventManager.java - CORRECTION FIREWORKS =====
package org.novania.eventpvp.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.novania.eventpvp.database.DatabaseManager;
import org.novania.eventpvp.database.models.EventSession;
import org.novania.eventpvp.database.models.SessionStats;
import org.novania.eventpvp.enums.Team;

public class EventManager {
    
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final TeamManager teamManager;
    private final ConfigManager configManager;
    
    // Cache des points d'équipe
    private int rougePoints = 0;
    private int bleuPoints = 0;
    private boolean eventActive = false;
    
    // Cache des killstreaks actuelles
    private final Map<UUID, Integer> playerKillstreaks = new HashMap<>();
    
    public EventManager(JavaPlugin plugin, DatabaseManager databaseManager, TeamManager teamManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.teamManager = teamManager;
        this.configManager = ((org.novania.eventpvp.EventPVP) plugin).getConfigManager();
    }
    
    // ===== GESTION DE L'EVENT =====
    
    public boolean startEvent() {
        if (eventActive) {
            return false;
        }
        
        int victoryTarget = configManager.getVictoryTarget();
        int sessionId = databaseManager.startNewSession(victoryTarget);
        
        if (sessionId != -1) {
            eventActive = true;
            rougePoints = 0;
            bleuPoints = 0;
            playerKillstreaks.clear();
            
            // Ajouter tous les joueurs avec équipe à la session
            addAllPlayersToSession();
            
            // Annonce globale
            Bukkit.broadcastMessage(configManager.getPrefix() + 
                configManager.getMessage("event_started"));
            
            configManager.debugLog("Event démarré - Session: " + sessionId);
            return true;
        }
        
        return false;
    }
    
    public void stopEvent() {
        if (!eventActive) {
            return;
        }
        
        databaseManager.endCurrentSession(null);
        eventActive = false;
        rougePoints = 0;
        bleuPoints = 0;
        playerKillstreaks.clear();
        
        Bukkit.broadcastMessage(configManager.getPrefix() + 
            configManager.getMessage("event_stopped"));
        
        configManager.debugLog("Event arrêté");
    }
    
    public void resetEvent() {
        if (!eventActive) {
            return;
        }
        
        databaseManager.resetCurrentSession();
        rougePoints = 0;
        bleuPoints = 0;
        playerKillstreaks.clear();
        
        Bukkit.broadcastMessage(configManager.getPrefix() + 
            configManager.getMessage("points_reset"));
        
        configManager.debugLog("Event reset");
    }
    
    private void addAllPlayersToSession() {
        for (Map.Entry<UUID, Team> entry : teamManager.getAllPlayerTeams().entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                databaseManager.addPlayerToSession(
                    player.getUniqueId().toString(),
                    player.getName(),
                    entry.getValue().name().toLowerCase()
                );
            }
        }
    }
    
    // ===== GESTION DES KILLS =====
    
    public void handlePlayerKill(Player killer, Player victim) {
        if (!eventActive || !teamManager.isInEventWorld(killer) || !teamManager.isInEventWorld(victim)) {
            return;
        }
        
        Team killerTeam = teamManager.getPlayerTeam(killer);
        Team victimTeam = teamManager.getPlayerTeam(victim);
        
        if (killerTeam == null || victimTeam == null) {
            return;
        }
        
        // Vérifier que c'est un kill valide (équipes différentes)
        if (killerTeam == victimTeam && !killer.hasPermission("eventpvp.teamkill.bypass")) {
            return;
        }
        
        // Les spectateurs ne participent pas
        if (killerTeam == Team.SPECTATOR || victimTeam == Team.SPECTATOR) {
            return;
        }
        
        UUID killerUuid = killer.getUniqueId();
        UUID victimUuid = victim.getUniqueId();
        
        // Incrémenter la killstreak du tueur
        int killstreak = playerKillstreaks.getOrDefault(killerUuid, 0) + 1;
        playerKillstreaks.put(killerUuid, killstreak);
        
        // Reset la killstreak de la victime
        playerKillstreaks.put(victimUuid, 0);
        
        // Enregistrer le kill dans la base
        String weapon = killer.getInventory().getItemInMainHand().getType().name();
        databaseManager.recordKill(
            killerUuid.toString(),
            killer.getName(),
            victimUuid.toString(),
            victim.getName(),
            weapon,
            0.0, // TODO: Récupérer les dégâts réels
            killstreak
        );
        
        // Mettre à jour les stats
        updatePlayerKillStats(killer, killstreak);
        updatePlayerDeathStats(victim);
        
        // Mettre à jour les points d'équipe
        updateTeamPoints(killerTeam);
        
        // Messages et effets
        notifyKill(killer, victim, killstreak);
        
        // CORRECTION: Ajouter feux d'artifice pour killstreaks
        if (killstreak >= 3) {
            spawnKillstreakFirework(killer, killstreak);
        }
        
        // Vérifier la condition de victoire
        checkVictoryCondition();
    }
    
    private void spawnKillstreakFirework(Player player, int killstreak) {
        if (!configManager.isKillFirework()) {
            return;
        }
        
        Location loc = player.getLocation();
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        
        // Couleur selon la killstreak
        Color color;
        if (killstreak >= 10) {
            color = Color.RED; // Monster Kill
        } else if (killstreak >= 7) {
            color = Color.ORANGE; // Rampage
        } else if (killstreak >= 5) {
            color = Color.YELLOW; // Killing Spree
        } else {
            color = Color.GREEN; // Multi Kill
        }
        
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(color)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withFlicker()
                .withTrail()
                .build();
        
        meta.addEffect(effect);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
        
        // Explosion immédiate
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            firework.detonate();
        }, 1L);
    }
    
    private void updatePlayerKillStats(Player killer, int killstreak) {
        String killerUuid = killer.getUniqueId().toString();
        
        // Incrémenter les kills
        databaseManager.incrementPlayerStat(killerUuid, "kills");
        
        // Mettre à jour la killstreak actuelle
        databaseManager.updatePlayerStats(killerUuid, "current_killstreak", killstreak);
        
        // Vérifier si c'est un nouveau record de killstreak
        SessionStats stats = databaseManager.getPlayerStats(killerUuid);
        if (stats != null && killstreak > stats.getLongestKillstreak()) {
            databaseManager.updatePlayerStats(killerUuid, "longest_killstreak", killstreak);
        }
    }
    
    private void updatePlayerDeathStats(Player victim) {
        String victimUuid = victim.getUniqueId().toString();
        
        // Incrémenter les morts
        databaseManager.incrementPlayerStat(victimUuid, "deaths");
        
        // Reset la killstreak
        databaseManager.updatePlayerStats(victimUuid, "current_killstreak", 0);
    }
    
    private void updateTeamPoints(Team team) {
        if (team == Team.ROUGE) {
            rougePoints++;
            databaseManager.updateTeamPoints("rouge", rougePoints);
        } else if (team == Team.BLEU) {
            bleuPoints++;
            databaseManager.updateTeamPoints("bleu", bleuPoints);
        }
    }
    
    private void notifyKill(Player killer, Player victim, int killstreak) {
        // Message au tueur
        SessionStats killerStats = databaseManager.getPlayerStats(killer.getUniqueId().toString());
        int totalKills = killerStats != null ? killerStats.getKills() : 0;
        
        killer.sendMessage(configManager.getPrefix() + 
            configManager.getMessage("kill_reward", "kills", String.valueOf(totalKills)));
        
        // Son et effets
        if (configManager.isSoundsEnabled()) {
            killer.playSound(killer.getLocation(), configManager.getKillSound(), 1.0f, 1.0f);
        }
        
        // Annonce pour les killstreaks importantes
        if (killstreak >= 3) {
            String suffix = getKillstreakMessage(killstreak);
            Bukkit.broadcastMessage(configManager.getPrefix() + 
                killer.getName() + " §f-> §c" + victim.getName() + suffix);
        }
    }
    
    private String getKillstreakMessage(int killstreak) {
        if (killstreak >= 10) return " §c§l(MONSTER KILL!)";
        if (killstreak >= 7) return " §6§l(RAMPAGE!)";
        if (killstreak >= 5) return " §e§l(KILLING SPREE!)";
        if (killstreak >= 3) return " §a§l(MULTI KILL!)";
        return "";
    }
    
    // ===== GESTION DES POINTS ET VICTOIRE =====
    
    private void checkVictoryCondition() {
        int victoryTarget = configManager.getVictoryTarget();
        
        // Annonces de progression
        if (configManager.isAnnounceProgress()) {
            checkProgressAnnouncements(victoryTarget);
        }
        
        // Vérifier la victoire
        String winner = null;
        if (rougePoints >= victoryTarget) {
            winner = "rouge";
        } else if (bleuPoints >= victoryTarget) {
            winner = "bleu";
        }
        
        if (winner != null) {
            handleVictory(winner);
        }
    }
    
    private void checkProgressAnnouncements(int victoryTarget) {
        List<Integer> milestones = configManager.getProgressMilestones();
        
        for (int milestone : milestones) {
            int targetPoints = (victoryTarget * milestone) / 100;
            
            // Vérifier si une équipe vient d'atteindre ce jalon
            if ((rougePoints == targetPoints || bleuPoints == targetPoints) && targetPoints > 0) {
                announceProgress(targetPoints, victoryTarget);
                break;
            }
        }
        
        // Annonce spéciale pour la dernière ligne droite (90%+)
        int lastStretchTarget = (victoryTarget * 90) / 100;
        if (Math.max(rougePoints, bleuPoints) >= lastStretchTarget && 
            Math.max(rougePoints, bleuPoints) < victoryTarget) {
            
            Bukkit.broadcastMessage(configManager.getPrefix() + 
                configManager.getMessage("last_straight", 
                    "rouge_points", String.valueOf(rougePoints),
                    "bleu_points", String.valueOf(bleuPoints),
                    "target", String.valueOf(victoryTarget)));
        }
    }
    
    private void announceProgress(int points, int target) {
        int progress = (points * 100) / target;
        String leadingTeam = rougePoints > bleuPoints ? "Rouge" : "Bleu";
        String color = rougePoints > bleuPoints ? "c" : "9";
        
        Bukkit.broadcastMessage(configManager.getPrefix() + 
            configManager.getMessage("team_progress",
                "color", color,
                "team", leadingTeam,
                "points", String.valueOf(points),
                "target", String.valueOf(target),
                "progress", String.valueOf(progress)));
    }
    
    private void handleVictory(String winnerTeam) {
        // Arrêter l'event
        databaseManager.endCurrentSession(winnerTeam);
        eventActive = false;
        
        // Annonces de victoire
        String teamName = configManager.getTeamDisplayName(winnerTeam);
        String teamColor = configManager.getTeamColorCode(winnerTeam);
        
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(configManager.getPrefix() + 
            configManager.getMessage("victory_announcement", "team", teamColor + teamName.toUpperCase()));
        Bukkit.broadcastMessage(configManager.getPrefix() + 
            configManager.getMessage("victory_objective", "target", String.valueOf(configManager.getVictoryTarget())));
        
        // Trouver et annoncer le MVP
        List<SessionStats> topKillers = databaseManager.getTopKillers(1);
        if (!topKillers.isEmpty()) {
            SessionStats mvp = topKillers.get(0);
            Bukkit.broadcastMessage(configManager.getPrefix() + 
                configManager.getMessage("victory_mvp", 
                    "mvp", mvp.getPlayerName(),
                    "kills", String.valueOf(mvp.getKills())));
        }
        Bukkit.broadcastMessage("");
        
        // Sons et effets pour tous
        if (configManager.isSoundsEnabled()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), configManager.getVictorySound(), 1.0f, 1.0f);
            }
        }
        
        // Auto-reset si configuré
        if (configManager.isAutoReset()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!eventActive) {
                    startEvent();
                }
            }, 200L); // 10 secondes
        }
        
        configManager.debugLog("Victoire de l'équipe " + winnerTeam);
    }
    
    // ===== GESTION DES JOUEURS =====
    
    public void addPlayerToEvent(Player player, Team team) {
        if (!eventActive) {
            return;
        }
        
        databaseManager.addPlayerToSession(
            player.getUniqueId().toString(),
            player.getName(),
            team.name().toLowerCase()
        );
        
        configManager.debugLog("Joueur " + player.getName() + " ajouté à l'event avec l'équipe " + team);
    }
    
    public void removePlayerFromEvent(Player player) {
        if (!eventActive) {
            return;
        }
        
        // Reset la killstreak du joueur
        playerKillstreaks.remove(player.getUniqueId());
        
        configManager.debugLog("Joueur " + player.getName() + " retiré de l'event");
    }
    
    // ===== GESTION DES RESPAWNS =====
    
    public void handlePlayerDeath(Player player) {
        if (!eventActive || !teamManager.isInEventWorld(player)) {
            return;
        }
        
        Team team = teamManager.getPlayerTeam(player);
        if (team == null || team == Team.SPECTATOR) {
            return;
        }
        
        // Reset la killstreak
        playerKillstreaks.put(player.getUniqueId(), 0);
        
        // Message de mort
        int respawnDelay = configManager.getRespawnDelay();
        player.sendMessage(configManager.getPrefix() + 
            configManager.getMessage("death_message", "delay", String.valueOf(respawnDelay)));
    }
    
    // ===== STATISTIQUES ET LEADERBOARDS =====
    
    public SessionStats getPlayerStats(Player player) {
        if (!eventActive) {
            return null;
        }
        
        return databaseManager.getPlayerStats(player.getUniqueId().toString());
    }
    
    public List<SessionStats> getTopKillers(int limit) {
        if (!eventActive) {
            return List.of();
        }
        
        return databaseManager.getTopKillers(limit);
    }
    
    public List<SessionStats> getTopKillstreaks(int limit) {
        if (!eventActive) {
            return List.of();
        }
        
        return databaseManager.getTopKillstreaks(limit);
    }
    
    public Map<String, Integer> getTeamStats() {
        if (!eventActive) {
            return Map.of();
        }
        
        return databaseManager.getTeamStats();
    }
    
    // ===== MÉTHODES D'ACCÈS =====
    
    public boolean isEventActive() {
        return eventActive;
    }
    
    public int getRougePoints() {
        return rougePoints;
    }
    
    public int getBleuPoints() {
        return bleuPoints;
    }
    
    public int getVictoryTarget() {
        return configManager.getVictoryTarget();
    }
    
    public EventSession getCurrentSession() {
        return databaseManager.getCurrentSession();
    }
    
    public int getPlayerKillstreak(Player player) {
        return playerKillstreaks.getOrDefault(player.getUniqueId(), 0);
    }
    
    public String getLeadingTeam() {
        if (rougePoints > bleuPoints) return "Rouge";
        if (bleuPoints > rougePoints) return "Bleu";
        return "Égalité";
    }
    
    public int getLeadingPoints() {
        return Math.max(rougePoints, bleuPoints);
    }
    
    public double getProgressPercentage() {
        return (double) getLeadingPoints() / getVictoryTarget() * 100;
    }
    
    public String getEventStatus() {
        if (!eventActive) return "§cInactif";
        
        long duration = System.currentTimeMillis();
        EventSession session = getCurrentSession();
        if (session != null) {
            duration = session.getDuration();
        }
        
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        
        return String.format("§aActif §7(%dm %ds)", minutes, seconds);
    }
}