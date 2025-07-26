// ===== SessionStats.java =====
package org.novania.eventpvp.database.models;

public class SessionStats {
    
    private final int id;
    private final int sessionId;
    private final String playerUuid;
    private final String playerName;
    private final String team;
    private final int kills;
    private final int deaths;
    private final int damageDealt;
    private final int damageTaken;
    private final int longestKillstreak;
    private final int currentKillstreak;
    private final int assists;
    private final int timePlayed;
    private final boolean buildKitUsed;
    private final long joinTime;
    private final long leaveTime;
    
    public SessionStats(int id, int sessionId, String playerUuid, String playerName, String team,
                       int kills, int deaths, int damageDealt, int damageTaken, int longestKillstreak,
                       int currentKillstreak, int assists, int timePlayed, boolean buildKitUsed,
                       long joinTime, long leaveTime) {
        this.id = id;
        this.sessionId = sessionId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.team = team;
        this.kills = kills;
        this.deaths = deaths;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.longestKillstreak = longestKillstreak;
        this.currentKillstreak = currentKillstreak;
        this.assists = assists;
        this.timePlayed = timePlayed;
        this.buildKitUsed = buildKitUsed;
        this.joinTime = joinTime;
        this.leaveTime = leaveTime;
    }
    
    // Getters
    public int getId() { return id; }
    public int getSessionId() { return sessionId; }
    public String getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public String getTeam() { return team; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getDamageDealt() { return damageDealt; }
    public int getDamageTaken() { return damageTaken; }
    public int getLongestKillstreak() { return longestKillstreak; }
    public int getCurrentKillstreak() { return currentKillstreak; }
    public int getAssists() { return assists; }
    public int getTimePlayed() { return timePlayed; }
    public boolean isBuildKitUsed() { return buildKitUsed; }
    public long getJoinTime() { return joinTime; }
    public long getLeaveTime() { return leaveTime; }
    
    // Méthodes utilitaires
    public double getKDRatio() {
        if (deaths == 0) {
            return kills > 0 ? kills : 0.0;
        }
        return (double) kills / deaths;
    }
    
    public String getFormattedKDRatio() {
        double kd = getKDRatio();
        return String.format("%.2f", kd);
    }
    
    public int getTotalCombatActions() {
        return kills + deaths + assists;
    }
    
    public double getDamagePerKill() {
        if (kills == 0) return 0.0;
        return (double) damageDealt / kills;
    }
    
    public boolean isActive() {
        return leaveTime == 0;
    }
    
    public String getTeamColorCode() {
        switch (team.toLowerCase()) {
            case "rouge": return "§c";
            case "bleu": return "§9";
            case "spectator": return "§7";
            default: return "§f";
        }
    }
    
    public String getTeamEmoji() {
        switch (team.toLowerCase()) {
            case "rouge": return "🔴";
            case "bleu": return "🔵";
            case "spectator": return "⚪";
            default: return "❌";
        }
    }
}