package org.novania.eventpvp.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import org.novania.eventpvp.database.models.EventSession;
import org.novania.eventpvp.database.models.SessionStats;
import org.novania.eventpvp.database.models.SessionKill;

public class DatabaseManager {
    
    private final JavaPlugin plugin;
    private Connection connection;
    private int currentSessionId = -1;
    
    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean initDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            String dbFile = plugin.getConfig().getString("database.file", "eventpvp.db");
            File dbPath = new File(dataFolder, dbFile);
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath.getAbsolutePath());
            
            createTables();
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            return false;
        }
    }
    
    private void createTables() throws SQLException {
        // Table des sessions d'events
        String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS event_sessions (
                session_id INTEGER PRIMARY KEY AUTOINCREMENT,
                start_time INTEGER NOT NULL,
                end_time INTEGER DEFAULT NULL,
                winner_team TEXT DEFAULT NULL,
                rouge_points INTEGER DEFAULT 0,
                bleu_points INTEGER DEFAULT 0,
                total_kills INTEGER DEFAULT 0,
                total_damage INTEGER DEFAULT 0,
                victory_condition INTEGER DEFAULT 50,
                status TEXT DEFAULT 'active'
            )
        """;
        
        // Table des statistiques joueurs par session
        String createStatsTable = """
            CREATE TABLE IF NOT EXISTS session_stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                player_uuid TEXT NOT NULL,
                player_name TEXT NOT NULL,
                team TEXT NOT NULL,
                kills INTEGER DEFAULT 0,
                deaths INTEGER DEFAULT 0,
                damage_dealt INTEGER DEFAULT 0,
                damage_taken INTEGER DEFAULT 0,
                longest_killstreak INTEGER DEFAULT 0,
                current_killstreak INTEGER DEFAULT 0,
                assists INTEGER DEFAULT 0,
                time_played INTEGER DEFAULT 0,
                build_kit_used BOOLEAN DEFAULT FALSE,
                join_time INTEGER DEFAULT 0,
                leave_time INTEGER DEFAULT NULL,
                FOREIGN KEY (session_id) REFERENCES event_sessions(session_id)
            )
        """;
        
        // Table des kills détaillés
        String createKillsTable = """
            CREATE TABLE IF NOT EXISTS session_kills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                killer_uuid TEXT NOT NULL,
                killer_name TEXT NOT NULL,
                victim_uuid TEXT NOT NULL,
                victim_name TEXT NOT NULL,
                weapon TEXT DEFAULT NULL,
                timestamp INTEGER NOT NULL,
                damage_final REAL DEFAULT 0,
                killstreak_count INTEGER DEFAULT 0,
                FOREIGN KEY (session_id) REFERENCES event_sessions(session_id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createSessionsTable);
            stmt.executeUpdate(createStatsTable);
            stmt.executeUpdate(createKillsTable);
            
            // Index pour améliorer les performances
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_session_stats_player ON session_stats(session_id, player_uuid)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_session_kills_session ON session_kills(session_id)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_sessions_status ON event_sessions(status)");
        }
    }
    
    // ===== GESTION DES SESSIONS =====
    
    public int startNewSession(int victoryCondition) {
        String sql = """
            INSERT INTO event_sessions (start_time, victory_condition, status) 
            VALUES (?, ?, 'active')
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setInt(2, victoryCondition);
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    currentSessionId = rs.getInt(1);
                    plugin.getLogger().info("Nouvelle session d'event démarrée: " + currentSessionId);
                    return currentSessionId;
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors du démarrage de la session: " + e.getMessage());
        }
        
        return -1;
    }
    
    public void endCurrentSession(String winnerTeam) {
        if (currentSessionId == -1) return;
        
        String sql = """
            UPDATE event_sessions 
            SET end_time = ?, winner_team = ?, status = 'completed'
            WHERE session_id = ?
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, winnerTeam);
            stmt.setInt(3, currentSessionId);
            
            stmt.executeUpdate();
            
            plugin.getLogger().info("Session " + currentSessionId + " terminée. Gagnant: " + winnerTeam);
            currentSessionId = -1;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la fin de session: " + e.getMessage());
        }
    }
    
    public EventSession getCurrentSession() {
        if (currentSessionId == -1) return null;
        
        String sql = "SELECT * FROM event_sessions WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createSessionFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération de la session: " + e.getMessage());
        }
        
        return null;
    }
    
    public void updateTeamPoints(String team, int points) {
        if (currentSessionId == -1) return;
        
        String column = team.toLowerCase() + "_points";
        String sql = "UPDATE event_sessions SET " + column + " = ? WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, points);
            stmt.setInt(2, currentSessionId);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la mise à jour des points: " + e.getMessage());
        }
    }
    
    public void resetCurrentSession() {
        if (currentSessionId == -1) return;
        
        String sql = """
            UPDATE event_sessions 
            SET rouge_points = 0, bleu_points = 0, total_kills = 0, total_damage = 0
            WHERE session_id = ?
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.executeUpdate();
            
            // Reset aussi les stats des joueurs
            resetSessionStats();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors du reset de session: " + e.getMessage());
        }
    }
    
    // ===== GESTION DES STATISTIQUES JOUEURS =====
    
    public void addPlayerToSession(String playerUuid, String playerName, String team) {
        if (currentSessionId == -1) return;
        
        String sql = """
            INSERT OR REPLACE INTO session_stats 
            (session_id, player_uuid, player_name, team, join_time) 
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.setString(2, playerUuid);
            stmt.setString(3, playerName);
            stmt.setString(4, team);
            stmt.setLong(5, System.currentTimeMillis());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'ajout du joueur à la session: " + e.getMessage());
        }
    }
    
    public void updatePlayerStats(String playerUuid, String statType, int value) {
        if (currentSessionId == -1) return;
        
        String sql = "UPDATE session_stats SET " + statType + " = ? WHERE session_id = ? AND player_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, value);
            stmt.setInt(2, currentSessionId);
            stmt.setString(3, playerUuid);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la mise à jour des stats: " + e.getMessage());
        }
    }
    
    public void incrementPlayerStat(String playerUuid, String statType) {
        if (currentSessionId == -1) return;
        
        String sql = "UPDATE session_stats SET " + statType + " = " + statType + " + 1 WHERE session_id = ? AND player_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.setString(2, playerUuid);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'incrémentation: " + e.getMessage());
        }
    }
    
    public SessionStats getPlayerStats(String playerUuid) {
        if (currentSessionId == -1) return null;
        
        String sql = "SELECT * FROM session_stats WHERE session_id = ? AND player_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.setString(2, playerUuid);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createStatsFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération des stats: " + e.getMessage());
        }
        
        return null;
    }
    
    public void setBuildKitUsed(String playerUuid, boolean used) {
        if (currentSessionId == -1) return;
        
        String sql = "UPDATE session_stats SET build_kit_used = ? WHERE session_id = ? AND player_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, used);
            stmt.setInt(2, currentSessionId);
            stmt.setString(3, playerUuid);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la mise à jour du kit build: " + e.getMessage());
        }
    }
    
    public boolean hasBuildKitUsed(String playerUuid) {
        if (currentSessionId == -1) return false;
        
        String sql = "SELECT build_kit_used FROM session_stats WHERE session_id = ? AND player_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.setString(2, playerUuid);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("build_kit_used");
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la vérification du kit build: " + e.getMessage());
        }
        
        return false;
    }
    
    public void resetSessionStats() {
        if (currentSessionId == -1) return;
        
        String sql = """
            UPDATE session_stats SET 
            kills = 0, deaths = 0, damage_dealt = 0, damage_taken = 0,
            longest_killstreak = 0, current_killstreak = 0, assists = 0
            WHERE session_id = ?
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.executeUpdate();
            
            // Effacer aussi les kills détaillés
            String deleteKills = "DELETE FROM session_kills WHERE session_id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteKills)) {
                deleteStmt.setInt(1, currentSessionId);
                deleteStmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors du reset des stats: " + e.getMessage());
        }
    }
    
    public void resetBuildKits() {
        if (currentSessionId == -1) return;
        
        String sql = "UPDATE session_stats SET build_kit_used = FALSE WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors du reset des kits build: " + e.getMessage());
        }
    }
    
    // ===== GESTION DES KILLS =====
    
    public void recordKill(String killerUuid, String killerName, String victimUuid, String victimName, 
                          String weapon, double finalDamage, int killstreakCount) {
        if (currentSessionId == -1) return;
        
        String sql = """
            INSERT INTO session_kills 
            (session_id, killer_uuid, killer_name, victim_uuid, victim_name, weapon, timestamp, damage_final, killstreak_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.setString(2, killerUuid);
            stmt.setString(3, killerName);
            stmt.setString(4, victimUuid);
            stmt.setString(5, victimName);
            stmt.setString(6, weapon);
            stmt.setLong(7, System.currentTimeMillis());
            stmt.setDouble(8, finalDamage);
            stmt.setInt(9, killstreakCount);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'enregistrement du kill: " + e.getMessage());
        }
    }
    
    public List<SessionKill> getSessionKills() {
        if (currentSessionId == -1) return new ArrayList<>();
        
        List<SessionKill> kills = new ArrayList<>();
        String sql = "SELECT * FROM session_kills WHERE session_id = ? ORDER BY timestamp DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    kills.add(createKillFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération des kills: " + e.getMessage());
        }
        
        return kills;
    }
    
    // ===== LEADERBOARDS ET CLASSEMENTS =====
    
    public List<SessionStats> getTopKillers(int limit) {
        if (currentSessionId == -1) return new ArrayList<>();
        
        List<SessionStats> topKillers = new ArrayList<>();
        String sql = """
            SELECT * FROM session_stats 
            WHERE session_id = ? 
            ORDER BY kills DESC, longest_killstreak DESC 
            LIMIT ?
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topKillers.add(createStatsFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération du top killers: " + e.getMessage());
        }
        
        return topKillers;
    }
    
    public List<SessionStats> getTopKillstreaks(int limit) {
        if (currentSessionId == -1) return new ArrayList<>();
        
        List<SessionStats> topStreaks = new ArrayList<>();
        String sql = """
            SELECT * FROM session_stats 
            WHERE session_id = ? 
            ORDER BY longest_killstreak DESC, kills DESC 
            LIMIT ?
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topStreaks.add(createStatsFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération du top killstreaks: " + e.getMessage());
        }
        
        return topStreaks;
    }
    
    public Map<String, Integer> getTeamStats() {
        if (currentSessionId == -1) return new HashMap<>();
        
        Map<String, Integer> teamStats = new HashMap<>();
        String sql = """
            SELECT team, SUM(kills) as total_kills, SUM(deaths) as total_deaths, 
                   SUM(damage_dealt) as total_damage, COUNT(*) as player_count
            FROM session_stats 
            WHERE session_id = ? 
            GROUP BY team
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentSessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String team = rs.getString("team");
                    teamStats.put(team + "_kills", rs.getInt("total_kills"));
                    teamStats.put(team + "_deaths", rs.getInt("total_deaths"));
                    teamStats.put(team + "_damage", rs.getInt("total_damage"));
                    teamStats.put(team + "_players", rs.getInt("player_count"));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération des stats d'équipe: " + e.getMessage());
        }
        
        return teamStats;
    }
    
    // ===== MÉTHODES UTILITAIRES =====
    
    private EventSession createSessionFromResultSet(ResultSet rs) throws SQLException {
        return new EventSession(
            rs.getInt("session_id"),
            rs.getLong("start_time"),
            rs.getLong("end_time"),
            rs.getString("winner_team"),
            rs.getInt("rouge_points"),
            rs.getInt("bleu_points"),
            rs.getInt("total_kills"),
            rs.getInt("total_damage"),
            rs.getInt("victory_condition"),
            rs.getString("status")
        );
    }
    
    private SessionStats createStatsFromResultSet(ResultSet rs) throws SQLException {
        return new SessionStats(
            rs.getInt("id"),
            rs.getInt("session_id"),
            rs.getString("player_uuid"),
            rs.getString("player_name"),
            rs.getString("team"),
            rs.getInt("kills"),
            rs.getInt("deaths"),
            rs.getInt("damage_dealt"),
            rs.getInt("damage_taken"),
            rs.getInt("longest_killstreak"),
            rs.getInt("current_killstreak"),
            rs.getInt("assists"),
            rs.getInt("time_played"),
            rs.getBoolean("build_kit_used"),
            rs.getLong("join_time"),
            rs.getLong("leave_time")
        );
    }
    
    private SessionKill createKillFromResultSet(ResultSet rs) throws SQLException {
        return new SessionKill(
            rs.getInt("id"),
            rs.getInt("session_id"),
            rs.getString("killer_uuid"),
            rs.getString("killer_name"),
            rs.getString("victim_uuid"),
            rs.getString("victim_name"),
            rs.getString("weapon"),
            rs.getLong("timestamp"),
            rs.getDouble("damage_final"),
            rs.getInt("killstreak_count")
        );
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public int getCurrentSessionId() {
        return currentSessionId;
    }
    
    public boolean hasActiveSession() {
        return currentSessionId != -1;
    }
}