// ===== StatsGui.java =====
package org.novania.eventpvp.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.database.models.SessionStats;
import org.novania.eventpvp.utils.ItemBuilder;
import org.novania.eventpvp.utils.MessageUtils;
import org.novania.eventpvp.utils.SoundUtils;

import java.util.ArrayList;
import java.util.List;

public class StatsGui {
    
    private final EventPVP plugin;
    
    public StatsGui(EventPVP plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player viewer, Player target) {
        if (!plugin.getEventManager().isEventActive()) {
            viewer.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cAucun event actif pour afficher les statistiques!");
            SoundUtils.EventSounds.playErrorSound(viewer);
            return;
        }
        
        SessionStats stats = plugin.getEventManager().getPlayerStats(target);
        if (stats == null) {
            viewer.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cAucune statistique trouvée pour " + target.getName());
            SoundUtils.EventSounds.playErrorSound(viewer);
            return;
        }
        
        String title = target.equals(viewer) ? 
            plugin.getConfigManager().getMessage("gui_stats_title") :
            "§6📊 Stats de " + target.getName();
            
        Inventory gui = Bukkit.createInventory(null, 54, title);
        
        populateStatsGui(gui, stats, viewer, target);
        
        viewer.openInventory(gui);
        SoundUtils.EventSounds.playClickSound(viewer);
    }
    
    private void populateStatsGui(Inventory gui, SessionStats stats, Player viewer, Player target) {
        // Tête du joueur
        ItemStack playerHead = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullOwner(target.getName())
                .setDisplayName(stats.getTeamColorCode() + target.getName())
                .setLore(
                    "§7─────────────────",
                    "§6Équipe: " + stats.getTeamColorCode() + stats.getTeam().toUpperCase(),
                    "§6Session actuelle",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(4, playerHead);
        
        // Stats de combat
        ItemStack combatStats = new ItemBuilder(Material.IRON_SWORD)
                .setDisplayName("§c⚔️ Combat")
                .setLore(
                    "§7─────────────────",
                    "§6Kills: §e" + stats.getKills(),
                    "§6Deaths: §c" + stats.getDeaths(),
                    "§6K/D Ratio: §a" + stats.getFormattedKDRatio(),
                    "§6Assists: §e" + stats.getAssists(),
                    "§7─────────────────"
                )
                .build();
        gui.setItem(20, combatStats);
        
        // Stats de killstreak
        ItemStack killstreakStats = new ItemBuilder(Material.BLAZE_ROD)
                .setDisplayName("§e🔥 Killstreaks")
                .setLore(
                    "§7─────────────────",
                    "§6Série actuelle: §e" + stats.getCurrentKillstreak(),
                    "§6Meilleure série: §e" + stats.getLongestKillstreak(),
                    "§7─────────────────"
                )
                .build();
        gui.setItem(22, killstreakStats);
        
        // Stats de dégâts
        ItemStack damageStats = new ItemBuilder(Material.TNT)
                .setDisplayName("§6💥 Dégâts")
                .setLore(
                    "§7─────────────────",
                    "§6Dégâts infligés: §e" + MessageUtils.formatNumber(stats.getDamageDealt()),
                    "§6Dégâts reçus: §c" + MessageUtils.formatNumber(stats.getDamageTaken()),
                    "§6Dégâts/Kill: §e" + String.format("%.1f", stats.getDamagePerKill()),
                    "§7─────────────────"
                )
                .build();
        gui.setItem(24, damageStats);
        
        // Kit build
        ItemStack kitStatus = new ItemBuilder(stats.isBuildKitUsed() ? Material.RED_WOOL : Material.GREEN_WOOL)
                .setDisplayName("§6🛠️ Kit Build")
                .setLore(
                    "§7─────────────────",
                    stats.isBuildKitUsed() ? "§c✗ Déjà utilisé" : "§a✓ Disponible",
                    "§7Usage: Une fois par session",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(40, kitStatus);
        
        // Temps de jeu
        long playTime = stats.isActive() ? 
            (System.currentTimeMillis() - stats.getJoinTime()) : 
            (stats.getLeaveTime() - stats.getJoinTime());
            
        ItemStack timeStats = new ItemBuilder(Material.CLOCK)
                .setDisplayName("§6⏰ Temps")
                .setLore(
                    "§7─────────────────",
                    "§6Temps de jeu: §e" + MessageUtils.formatTime(playTime),
                    "§6Statut: " + (stats.isActive() ? "§aEn ligne" : "§cHors ligne"),
                    "§7─────────────────"
                )
                .build();
        gui.setItem(42, timeStats);
        
        // Comparaison avec les autres si pas le viewer
        if (!target.equals(viewer)) {
            SessionStats viewerStats = plugin.getEventManager().getPlayerStats(viewer);
            if (viewerStats != null) {
                List<String> comparison = new ArrayList<>();
                comparison.add("§7─────────────────");
                comparison.add("§6Comparaison avec vous:");
                
                int killDiff = stats.getKills() - viewerStats.getKills();
                if (killDiff > 0) {
                    comparison.add("§c+" + killDiff + " kills de plus");
                } else if (killDiff < 0) {
                    comparison.add("§a" + Math.abs(killDiff) + " kills de moins");
                } else {
                    comparison.add("§7Même nombre de kills");
                }
                
                double kdDiff = stats.getKDRatio() - viewerStats.getKDRatio();
                if (kdDiff > 0) {
                    comparison.add("§c+" + String.format("%.2f", kdDiff) + " K/D de plus");
                } else if (kdDiff < 0) {
                    comparison.add("§a" + String.format("%.2f", Math.abs(kdDiff)) + " K/D de moins");
                } else {
                    comparison.add("§7Même K/D");
                }
                
                comparison.add("§7─────────────────");
                
                ItemStack comparisonItem = new ItemBuilder(Material.COMPARATOR)
                        .setDisplayName("§e⚖️ Comparaison")
                        .setLore(comparison)
                        .build();
                gui.setItem(38, comparisonItem);
            }
        }
        
        // Bouton retour
        ItemStack backButton = new ItemBuilder(Material.BARRIER)
                .setDisplayName("§cRetour")
                .setLore("§7Cliquez pour fermer")
                .build();
        gui.setItem(49, backButton);
        
        // Décoration
        fillEmptySlots(gui);
    }
    
    private void fillEmptySlots(Inventory gui) {
        ItemStack glassPane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§0")
                .build();
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glassPane);
            }
        }
    }
}