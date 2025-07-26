// ===== EventSession.java =====
package org.novania.eventpvp.database.models;

public class EventSession {
    
    private final int sessionId;
    private final long startTime;
    private final long endTime;
    private final String winnerTeam;
    private final int rougePoints;
    private final int bleuPoints;
    private final int totalKills;
    private final int totalDamage;
    private final int victoryCondition;
    private final String status;
    
    public EventSession(int sessionId, long startTime, long endTime, String winnerTeam,
                       int rougePoints, int bleuPoints, int totalKills, int totalDamage,
                       int victoryCondition, String status) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.winnerTeam = winnerTeam;
        this.rougePoints = rougePoints;
        this.bleuPoints = bleuPoints;
        this.totalKills = totalKills;
        this.totalDamage = totalDamage;
        this.victoryCondition = victoryCondition;
        this.status = status;
    }
    
    // Getters
    public int getSessionId() { return sessionId; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public String getWinnerTeam() { return winnerTeam; }
    public int getRougePoints() { return rougePoints; }
    public int getBleuPoints() { return bleuPoints; }
    public int getTotalKills() { return totalKills; }
    public int getTotalDamage() { return totalDamage; }
    public int getVictoryCondition() { return victoryCondition; }
    public String getStatus() { return status; }
    
    public boolean isActive() {
        return "active".equals(status);
    }
    
    public long getDuration() {
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    public int getLeadingTeamPoints() {
        return Math.max(rougePoints, bleuPoints);
    }
    
    public String getLeadingTeam() {
        if (rougePoints > bleuPoints) return "rouge";
        if (bleuPoints > rougePoints) return "bleu";
        return "égalité";
    }
    
    public double getProgressPercentage() {
        return (double) getLeadingTeamPoints() / victoryCondition * 100;
    }
}