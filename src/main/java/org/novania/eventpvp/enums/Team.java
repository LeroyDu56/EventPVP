package org.novania.eventpvp.enums;

public enum Team {
    ROUGE,
    BLEU,
    SPECTATOR;
    
    public static Team fromString(String teamName) {
        if (teamName == null) return null;
        
        switch (teamName.toLowerCase()) {
            case "rouge":
            case "red":
                return ROUGE;
            case "bleu":
            case "blue":
                return BLEU;
            case "spectator":
            case "spectateur":
            case "spec":
                return SPECTATOR;
            default:
                return null;
        }
    }
    
    public String getDisplayName() {
        switch (this) {
            case ROUGE: return "Rouge";
            case BLEU: return "Bleu";
            case SPECTATOR: return "Spectateur";
            default: return name();
        }
    }
    
    public String getColorCode() {
        switch (this) {
            case ROUGE: return "§c";
            case BLEU: return "§9";
            case SPECTATOR: return "§7";
            default: return "§f";
        }
    }
    
    public String getEmoji() {
        switch (this) {
            case ROUGE: return "🔴";
            case BLEU: return "🔵";
            case SPECTATOR: return "⚪";
            default: return "❌";
        }
    }
    
    public boolean isCombatTeam() {
        return this == ROUGE || this == BLEU;
    }
}