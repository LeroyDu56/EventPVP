package org.novania.eventpvp.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.database.models.EventSession;
import org.novania.eventpvp.database.models.SessionStats;
import org.novania.eventpvp.enums.Team;
import org.novania.eventpvp.utils.MessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardManager {
    
    private final EventPVP plugin;
    private final ConfigManager configManager;
    private final EventManager eventManager;
    
    private BukkitTask updateTask;
    private final Map<Player, Scoreboard> playerScoreboards = new HashMap<>();
    
    public ScoreboardManager(EventPVP plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.eventManager = eventManager;
    }
    
    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        int interval = configManager.getScoreboardUpdateInterval();
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllScoreboards, 0L, interval);
        
        plugin.getLogger().info("Tâche de mise à jour du scoreboard démarrée (intervalle: " + interval + " ticks)");
    }
    
    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        // Nettoyer tous les scoreboards
        for (Player player : playerScoreboards.keySet()) {
            removeScoreboard(player);
        }
        playerScoreboards.clear();
        
        plugin.getLogger().info("Tâche de mise à jour du scoreboard arrêtée");
    }
    
    private void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getTeamManager().isInEventWorld(player)) {
                updatePlayerScoreboard(player);
            } else {
                removeScoreboard(player);
            }
        }
    }
    
    public void updatePlayerScoreboard(Player player) {
        if (!configManager.isScoreboardEnabled()) {
            return;
        }
        
        Scoreboard scoreboard = createScoreboard(player);
        if (scoreboard != null) {
            player.setScoreboard(scoreboard);
            playerScoreboards.put(player, scoreboard);
        }
    }
    
    private Scoreboard createScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return null;
        
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("eventpvp", "dummy", 
            MessageUtils.colorize(configManager.getScoreboardTitle()));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Construction du contenu du scoreboard
        List<String> lines = buildScoreboardLines(player);
        
        // Ajouter les lignes au scoreboard (en ordre inverse car le score détermine l'ordre)
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }
            
            Score score = objective.getScore(line);
            score.setScore(lines.size() - i);
        }
        
        return scoreboard;
    }
    
    private List<String> buildScoreboardLines(Player player) {
        List<String> lines = new java.util.ArrayList<>();
        
        // Ligne vide du haut
        lines.add("§7");
        
        // Informations de l'event
        if (eventManager.isEventActive()) {
            EventSession session = eventManager.getCurrentSession();
            
            // Objectif
            lines.add("§6🎯 Objectif: §e" + eventManager.getVictoryTarget());
            
            // Points des équipes
            int rougePoints = eventManager.getRougePoints();
            int bleuPoints = eventManager.getBleuPoints();
            
            lines.add("§c🔴 Rouge: §f" + rougePoints);
            lines.add("§9🔵 Bleu: §f" + bleuPoints);
            
            // Progression
            int leadingPoints = Math.max(rougePoints, bleuPoints);
            double progress = (double) leadingPoints / eventManager.getVictoryTarget() * 100;
            String progressBar = MessageUtils.getProgressBar(leadingPoints, eventManager.getVictoryTarget(), 
                10, '█', "§a", "§7");
            lines.add(progressBar + " §f" + (int)progress + "%");
            
            lines.add("§8"); // Ligne vide
            
            // Top 3 de la session
            lines.add("§6🏆 Top Session:");
            List<SessionStats> topKillers = eventManager.getTopKillers(3);
            
            for (int i = 0; i < Math.min(3, topKillers.size()); i++) {
                SessionStats stats = topKillers.get(i);
                String teamEmoji = stats.getTeamEmoji();
                lines.add("§e" + (i + 1) + "." + teamEmoji + stats.getPlayerName() + 
                         " §7(" + stats.getKills() + ")");
            }
            
            // Si moins de 3 joueurs
            for (int i = topKillers.size(); i < 3; i++) {
                lines.add("§e" + (i + 1) + ".§7 ---");
            }
            
            lines.add("§9"); // Ligne vide
            
            // Stats personnelles du joueur
            Team playerTeam = plugin.getTeamManager().getPlayerTeam(player);
            if (playerTeam != null && playerTeam != Team.SPECTATOR) {
                lines.add("§6Votre session:");
                
                SessionStats playerStats = eventManager.getPlayerStats(player);
                if (playerStats != null) {
                    lines.add(playerTeam.getEmoji() + " " + playerTeam.getColorCode() + 
                             playerTeam.getDisplayName() + " §7(" + playerStats.getKills() + " kills)");
                    lines.add("§c💀 " + playerStats.getDeaths() + " deaths");
                    lines.add("§a⚡ K/D: " + playerStats.getFormattedKDRatio());
                    
                    // Killstreak actuelle
                    int killstreak = eventManager.getPlayerKillstreak(player);
                    if (killstreak > 0) {
                        lines.add("§e🔥 Série: " + killstreak);
                    }
                } else {
                    lines.add(playerTeam.getEmoji() + " " + playerTeam.getColorCode() + 
                             playerTeam.getDisplayName());
                    lines.add("§7Aucune stats encore");
                }
            } else if (playerTeam == Team.SPECTATOR) {
                lines.add("§7⚪ Mode Spectateur");
                lines.add("§7Profitez du show !");
            } else {
                lines.add("§c❌ Sans équipe");
                lines.add("§7Contactez un admin");
            }
        } else {
            // Event inactif
            lines.add("§c📴 Event Inactif");
            lines.add("§7");
            lines.add("§7En attente du");
            lines.add("§7prochain event...");
            lines.add("§7");
            
            Team playerTeam = plugin.getTeamManager().getPlayerTeam(player);
            if (playerTeam != null) {
                lines.add("§6Votre équipe:");
                lines.add(playerTeam.getEmoji() + " " + playerTeam.getColorCode() + 
                         playerTeam.getDisplayName());
            } else {
                lines.add("§c❌ Sans équipe");
                lines.add("§7Contactez un admin");
            }
        }
        
        // Ligne vide du bas
        lines.add("§0");
        
        return lines;
    }
    
    public void removeScoreboard(Player player) {
        if (playerScoreboards.containsKey(player)) {
            org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager != null) {
                player.setScoreboard(manager.getNewScoreboard());
            }
            playerScoreboards.remove(player);
        }
    }
    
    public void forceUpdatePlayer(Player player) {
        if (plugin.getTeamManager().isInEventWorld(player)) {
            updatePlayerScoreboard(player);
        }
    }
    
    public void forceUpdateAll() {
        updateAllScoreboards();
    }
    
    // Méthode pour créer un scoreboard temporaire pour les GUIs/stats
    public Scoreboard createStatsScoreboard(Player player, SessionStats stats) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return null;
        
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("stats", "dummy", 
            MessageUtils.colorize("§6📊 Vos Statistiques"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        List<String> lines = List.of(
            "§7",
            "§6Kills: §e" + stats.getKills(),
            "§6Deaths: §c" + stats.getDeaths(), 
            "§6K/D Ratio: §a" + stats.getFormattedKDRatio(),
            "§6Dégâts: §e" + MessageUtils.formatNumber(stats.getDamageDealt()),
            "§6Killstreak Max: §e" + stats.getLongestKillstreak(),
            "§6Assists: §e" + stats.getAssists(),
            "§8",
            "§6Équipe: " + stats.getTeamColorCode() + stats.getTeam().toUpperCase(),
            "§7"
        );
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }
            
            Score score = objective.getScore(line);
            score.setScore(lines.size() - i);
        }
        
        return scoreboard;
    }
    
    // Méthode pour créer un scoreboard de leaderboard
    public Scoreboard createLeaderboardScoreboard(List<SessionStats> topPlayers) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return null;
        
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("leaderboard", "dummy", 
            MessageUtils.colorize("§6🏆 Classement Session"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        List<String> lines = new java.util.ArrayList<>();
        lines.add("§7");
        
        for (int i = 0; i < Math.min(10, topPlayers.size()); i++) {
            SessionStats stats = topPlayers.get(i);
            String medal = getMedalEmoji(i + 1);
            lines.add(medal + stats.getTeamEmoji() + stats.getPlayerName() + 
                     " §7(" + stats.getKills() + ")");
        }
        
        if (topPlayers.isEmpty()) {
            lines.add("§7Aucun joueur");
            lines.add("§7dans cette session");
        }
        
        lines.add("§0");
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }
            
            Score score = objective.getScore(line);
            score.setScore(lines.size() - i);
        }
        
        return scoreboard;
    }
    
    private String getMedalEmoji(int position) {
        return switch (position) {
            case 1 -> "§e🥇 ";
            case 2 -> "§7🥈 ";
            case 3 -> "§6🥉 ";
            default -> "§f" + position + ". ";
        };
    }
    
    // Gestion des équipes pour le scoreboard (noms colorés)
    public void setupTeamColors() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getTeamManager().isInEventWorld(player)) {
                setupPlayerTeamColor(player);
            }
        }
    }
    
    private void setupPlayerTeamColor(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) return;
        
        Team playerTeam = plugin.getTeamManager().getPlayerTeam(player);
        if (playerTeam == null) return;
        
        // Créer ou récupérer l'équipe du scoreboard
        String teamName = playerTeam.name().toLowerCase();
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(teamName);
        
        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(teamName);
        }
        
        // Configurer les couleurs
        switch (playerTeam) {
            case ROUGE:
                scoreboardTeam.setColor(ChatColor.RED);
                scoreboardTeam.setPrefix("§c🔴 ");
                break;
            case BLEU:
                scoreboardTeam.setColor(ChatColor.BLUE);
                scoreboardTeam.setPrefix("§9🔵 ");
                break;
            case SPECTATOR:
                scoreboardTeam.setColor(ChatColor.GRAY);
                scoreboardTeam.setPrefix("§7⚪ ");
                break;
        }
        
        // Ajouter le joueur à l'équipe
        if (!scoreboardTeam.hasEntry(player.getName())) {
            scoreboardTeam.addEntry(player.getName());
        }
    }
    
    // Nettoyer l'équipe d'un joueur
    public void removePlayerFromTeams(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) return;
        
        for (org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
    }
    
    // Méthodes de gestion
    public boolean isEnabled() {
        return configManager.isScoreboardEnabled();
    }
    
    public void reload() {
        stopUpdateTask();
        if (isEnabled()) {
            startUpdateTask();
        }
    }
    
    public int getUpdateInterval() {
        return configManager.getScoreboardUpdateInterval();
    }
    
    public void setUpdateInterval(int ticks) {
        if (updateTask != null) {
            stopUpdateTask();
            startUpdateTask();
        }
    }
    
    // Statistiques du scoreboard
    public Map<String, Object> getScoreboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("enabled", isEnabled());
        stats.put("update_interval", getUpdateInterval());
        stats.put("active_scoreboards", playerScoreboards.size());
        stats.put("task_running", updateTask != null);
        return stats;
    }
}