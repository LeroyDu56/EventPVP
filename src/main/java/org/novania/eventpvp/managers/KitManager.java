package org.novania.eventpvp.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.enums.KitType;
import org.novania.eventpvp.kits.Kit;
import org.novania.eventpvp.kits.types.ArcherKit;
import org.novania.eventpvp.kits.types.AssassinKit;
import org.novania.eventpvp.kits.types.PvPKit;
import org.novania.eventpvp.kits.types.TankKit;
import org.novania.eventpvp.utils.SoundUtils;

public class KitManager {
    
    private final EventPVP plugin;
    private final ConfigManager configManager;
    private final Map<String, Kit> kits;
    
    public KitManager(EventPVP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.kits = new HashMap<>();
        
        registerKits();
    }
    
    private void registerKits() {
        // Enregistrer tous les kits disponibles
        registerKit(new PvPKit());
        registerKit(new ArcherKit());
        registerKit(new TankKit());
        registerKit(new AssassinKit());
        
        plugin.getLogger().info("Kits enregistrés: " + kits.size());
    }
    
    private void registerKit(Kit kit) {
        kits.put(kit.getName().toLowerCase(), kit);
        configManager.debugLog("Kit enregistré: " + kit.getName());
    }
    
    public boolean giveKit(Player player, String kitName) {
        // Vérifications préliminaires
        if (!plugin.getTeamManager().isInEventWorld(player)) {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("wrong_world"));
            SoundUtils.EventSounds.playErrorSound(player);
            return false;
        }
        
        if (!plugin.getTeamManager().hasTeam(player)) {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("no_team"));
            SoundUtils.EventSounds.playErrorSound(player);
            return false;
        }
        
        // Vérifier les permissions
        if (!player.hasPermission("eventpvp.kits.use")) {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("no_permission"));
            SoundUtils.EventSounds.playErrorSound(player);
            return false;
        }
        
        // Récupérer le kit
        Kit kit = kits.get(kitName.toLowerCase());
        if (kit == null) {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("kit_not_available"));
            SoundUtils.EventSounds.playErrorSound(player);
            return false;
        }
        
        // Vérifier si le joueur peut utiliser ce kit
        if (!kit.canUse(player)) {
            if (kit.getType() == KitType.BUILD) {
                player.sendMessage(configManager.getPrefix() + configManager.getMessage("kit_build_used"));
            } else {
                player.sendMessage(configManager.getPrefix() + configManager.getMessage("kit_not_available"));
            }
            SoundUtils.EventSounds.playErrorSound(player);
            return false;
        }
        
        // Donner le kit au joueur
        try {
            kit.giveToPlayer(player);
            
            // Message de succès
            player.sendMessage(configManager.getPrefix() + 
                configManager.getMessage("kit_taken", "kit", kit.getDisplayName()));
            
            // Sons et effets
            SoundUtils.EventSounds.playKitSound(player);
            
            configManager.debugLog("Kit " + kitName + " donné à " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur lors du don du kit " + kitName + " à " + player.getName() + ": " + e.getMessage());
            player.sendMessage(configManager.getPrefix() + "§cErreur lors de l'équipement du kit!");
            SoundUtils.EventSounds.playErrorSound(player);
            return false;
        }
    }
    
    public Kit getKit(String kitName) {
        return kits.get(kitName.toLowerCase());
    }
    
    public Map<String, Kit> getAllKits() {
        return new HashMap<>(kits);
    }
    
    public Map<String, Kit> getKitsByType(KitType type) {
        Map<String, Kit> filteredKits = new HashMap<>();
        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            if (entry.getValue().getType() == type) {
                filteredKits.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredKits;
    }
    
    public Map<String, Kit> getBuildKits() {
        return getKitsByType(KitType.BUILD);
    }
    
    public Map<String, Kit> getCombatKits() {
        return getKitsByType(KitType.COMBAT);
    }
    
    // Méthode pour reset les kits build de tous les joueurs
    public void resetAllBuildKits() {
        plugin.getDatabaseManager().resetBuildKits();
        plugin.getLogger().info("Tous les kits build ont été reset");
    }
    
    // Méthode pour vérifier si un joueur a utilisé son kit build
    public boolean hasBuildKitUsed(Player player) {
        return plugin.getDatabaseManager().hasBuildKitUsed(player.getUniqueId().toString());
    }
    
    // Méthode pour forcer la réinitialisation du kit build d'un joueur
    public void resetPlayerBuildKit(Player player) {
        plugin.getDatabaseManager().setBuildKitUsed(player.getUniqueId().toString(), false);
        player.sendMessage(configManager.getPrefix() + "§aVotre kit build a été réinitialisé!");
    }
    
    // Méthode appelée à la mort d'un joueur pour reset les kits appropriés
    public void handlePlayerDeath(Player player) {
        // Les kits de combat sont automatiquement "reset" car l'inventaire est vidé à la mort
        // Les kits build restent utilisés car ils ne se reset pas à la mort
        configManager.debugLog("Gestion de la mort pour les kits de " + player.getName());
    }
    
    // Méthode pour obtenir le kit recommandé selon l'équipe
    public String getRecommendedKit(org.novania.eventpvp.enums.Team team) {
        // Kit par défaut selon l'équipe (peut être configuré plus tard)
        switch (team) {
            case ROUGE:
            case BLEU:
                return "pvp"; // Kit PvP par défaut pour les équipes de combat
            case SPECTATOR:
                return null; // Les spectateurs n'ont pas de kit
            default:
                return "pvp";
        }
    }
    
    // Statistiques sur l'utilisation des kits
    public Map<String, Integer> getKitUsageStats() {
        // TODO: Implémenter des statistiques sur l'utilisation des kits
        // Pour l'instant, retourner une map vide
        return new HashMap<>();
    }
    
    // Méthode pour recharger les kits (si configuration changée)
    public void reloadKits() {
        kits.clear();
        registerKits();
        plugin.getLogger().info("Kits rechargés: " + kits.size());
    }
    
    // Validation d'un nom de kit
    public boolean isValidKit(String kitName) {
        return kits.containsKey(kitName.toLowerCase());
    }
    
    // Obtenir les noms de tous les kits
    public String[] getKitNames() {
        return kits.keySet().toArray(new String[0]);
    }
    
    // Obtenir les kits disponibles pour un joueur
    public Map<String, Kit> getAvailableKits(Player player) {
        Map<String, Kit> availableKits = new HashMap<>();
        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            Kit kit = entry.getValue();
            if (kit.canUse(player)) {
                availableKits.put(entry.getKey(), kit);
            }
        }
        return availableKits;
    }
    
    // Méthode pour donner un kit automatiquement selon des critères
    public boolean giveAutoKit(Player player) {
        org.novania.eventpvp.enums.Team team = plugin.getTeamManager().getPlayerTeam(player);
        if (team == null) return false;
        
        String recommendedKit = getRecommendedKit(team);
        if (recommendedKit != null) {
            return giveKit(player, recommendedKit);
        }
        
        return false;
    }
}