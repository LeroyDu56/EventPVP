// ===== SessionKill.java =====
package org.novania.eventpvp.database.models;

public class SessionKill {
    
    private final int id;
    private final int sessionId;
    private final String killerUuid;
    private final String killerName;
    private final String victimUuid;
    private final String victimName;
    private final String weapon;
    private final long timestamp;
    private final double damageFinal;
    private final int killstreakCount;
    
    public SessionKill(int id, int sessionId, String killerUuid, String killerName,
                      String victimUuid, String victimName, String weapon, long timestamp,
                      double damageFinal, int killstreakCount) {
        this.id = id;
        this.sessionId = sessionId;
        this.killerUuid = killerUuid;
        this.killerName = killerName;
        this.victimUuid = victimUuid;
        this.victimName = victimName;
        this.weapon = weapon;
        this.timestamp = timestamp;
        this.damageFinal = damageFinal;
        this.killstreakCount = killstreakCount;
    }
    
    // Getters
    public int getId() { return id; }
    public int getSessionId() { return sessionId; }
    public String getKillerUuid() { return killerUuid; }
    public String getKillerName() { return killerName; }
    public String getVictimUuid() { return victimUuid; }
    public String getVictimName() { return victimName; }
    public String getWeapon() { return weapon; }
    public long getTimestamp() { return timestamp; }
    public double getDamageFinal() { return damageFinal; }
    public int getKillstreakCount() { return killstreakCount; }
    
    // Méthodes utilitaires
    public String getFormattedWeapon() {
        if (weapon == null || weapon.isEmpty()) {
            return "Inconnu";
        }
        
        // Formater le nom de l'arme
        String formatted = weapon.replace("_", " ").toLowerCase();
        
        // Capitaliser chaque mot
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : formatted.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    public boolean isKillstreak() {
        return killstreakCount > 1;
    }
    
    public String getKillstreakSuffix() {
        if (killstreakCount <= 1) return "";
        
        if (killstreakCount >= 10) return " §c§l(MONSTER KILL!)";
        if (killstreakCount >= 7) return " §6§l(RAMPAGE!)";
        if (killstreakCount >= 5) return " §e§l(KILLING SPREE!)";
        if (killstreakCount >= 3) return " §a§l(MULTI KILL!)";
        
        return " §f(x" + killstreakCount + ")";
    }
    
    public long getTimeAgo() {
        return System.currentTimeMillis() - timestamp;
    }
    
    public String getFormattedTimeAgo() {
        long timeAgo = getTimeAgo();
        
        if (timeAgo < 60000) { // moins d'1 minute
            return (timeAgo / 1000) + "s";
        } else if (timeAgo < 3600000) { // moins d'1 heure
            return (timeAgo / 60000) + "min";
        } else {
            return (timeAgo / 3600000) + "h";
        }
    }
    
    // Méthode pour obtenir une description complète du kill
    public String getKillDescription() {
        StringBuilder description = new StringBuilder();
        description.append(killerName).append(" a tué ").append(victimName);
        
        if (weapon != null && !weapon.isEmpty() && !weapon.equals("AIR")) {
            description.append(" avec ").append(getFormattedWeapon());
        }
        
        if (isKillstreak()) {
            description.append(getKillstreakSuffix());
        }
        
        return description.toString();
    }
    
    // Méthode pour obtenir l'icône d'arme selon le type
    public String getWeaponIcon() {
        if (weapon == null || weapon.isEmpty()) {
            return "⚔️";
        }
        
        String weaponLower = weapon.toLowerCase();
        
        if (weaponLower.contains("sword")) {
            return "⚔️";
        } else if (weaponLower.contains("bow")) {
            return "🏹";
        } else if (weaponLower.contains("axe")) {
            return "🪓";
        } else if (weaponLower.contains("trident")) {
            return "🔱";
        } else if (weaponLower.contains("crossbow")) {
            return "🏹";
        } else if (weaponLower.contains("tnt")) {
            return "💥";
        } else if (weaponLower.contains("lava") || weaponLower.contains("fire")) {
            return "🔥";
        } else if (weaponLower.contains("fall")) {
            return "⬇️";
        } else {
            return "⚔️";
        }
    }
    
    // Méthode pour déterminer si c'est un kill à distance
    public boolean isRangedKill() {
        if (weapon == null) return false;
        
        String weaponLower = weapon.toLowerCase();
        return weaponLower.contains("bow") || 
               weaponLower.contains("crossbow") || 
               weaponLower.contains("trident") ||
               weaponLower.contains("snowball") ||
               weaponLower.contains("egg") ||
               weaponLower.contains("ender_pearl");
    }
    
    // Méthode pour obtenir la couleur selon le type de kill
    public String getKillColor() {
        if (isKillstreak()) {
            if (killstreakCount >= 10) return "§c";
            if (killstreakCount >= 7) return "§6";
            if (killstreakCount >= 5) return "§e";
            if (killstreakCount >= 3) return "§a";
        }
        
        if (isRangedKill()) {
            return "§b";
        }
        
        return "§f";
    }
}