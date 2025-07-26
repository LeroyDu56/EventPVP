// ===== LeaderboardGui.java - Corrections =====
package org.novania.eventpvp.guis;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.database.models.SessionStats;
import org.novania.eventpvp.utils.ItemBuilder;
import org.novania.eventpvp.utils.SoundUtils;

public class LeaderboardGui {
    
    private final EventPVP plugin;
    
    public LeaderboardGui(EventPVP plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        if (!plugin.getEventManager().isEventActive()) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cAucun event actif pour afficher le classement!");
            SoundUtils.EventSounds.playErrorSound(player);
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, 
            plugin.getConfigManager().getMessage("gui_leaderboard_title"));
        
        populateLeaderboardGui(gui, player);
        
        player.openInventory(gui);
        SoundUtils.EventSounds.playClickSound(player);
    }
    
    private void populateLeaderboardGui(Inventory gui, Player viewer) {
        // Titre
        ItemStack title = new ItemBuilder(Material.GOLD_BLOCK)
                .setDisplayName("§6🏆 Classement de la Session")
                .setLore(
                    "§7─────────────────",
                    "§7Classement par nombre de kills",
                    "§7Session en cours",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(4, title);
        
        // Top killers
        List<SessionStats> topKillers = plugin.getEventManager().getTopKillers(10);
        
        for (int i = 0; i < Math.min(10, topKillers.size()); i++) {
            SessionStats stats = topKillers.get(i);
            ItemStack playerItem = createLeaderboardItem(stats, i + 1, viewer);
            
            // Placement spécial pour le podium
            switch (i) {
                case 0 -> gui.setItem(13, playerItem); // 1ère place au centre
                case 1 -> gui.setItem(12, playerItem); // 2ème place à gauche
                case 2 -> gui.setItem(14, playerItem); // 3ème place à droite
                default -> gui.setItem(28 + (i - 3), playerItem); // Autres places en bas
            }
        }
        
        // Podium visuel
        if (topKillers.size() >= 2) {
            ItemStack silver = new ItemBuilder(Material.IRON_BLOCK)
                    .setDisplayName("§7🥈 2ème Place")
                    .build();
            gui.setItem(21, silver);
        }
        
        if (topKillers.size() >= 1) {
            ItemStack gold = new ItemBuilder(Material.GOLD_BLOCK)
                    .setDisplayName("§e🥇 1ère Place")
                    .build();
            gui.setItem(22, gold);
        }
        
        if (topKillers.size() >= 3) {
            ItemStack bronze = new ItemBuilder(Material.COPPER_BLOCK)
                    .setDisplayName("§6🥉 3ème Place")
                    .build();
            gui.setItem(23, bronze);
        }
        
        // Stats globales de l'event
        ItemStack eventStats = new ItemBuilder(Material.BOOK)
                .setDisplayName("§6📊 Stats de l'Event")
                .setLore(
                    "§7─────────────────",
                    "§6Points Rouge: §c" + plugin.getEventManager().getRougePoints(),
                    "§6Points Bleu: §9" + plugin.getEventManager().getBleuPoints(),
                    "§6Objectif: §e" + plugin.getEventManager().getVictoryTarget(),
                    "§6Progression: §e" + String.format("%.1f%%", plugin.getEventManager().getProgressPercentage()),
                    "§7─────────────────"
                )
                .build();
        gui.setItem(45, eventStats);
        
        // Position du viewer
        SessionStats viewerStats = plugin.getEventManager().getPlayerStats(viewer);
        if (viewerStats != null) {
            int position = findPlayerPosition(topKillers, viewer.getName());
            ItemStack viewerPositionItem = new ItemBuilder(Material.COMPASS)
                    .setDisplayName("§e🎯 Votre Position")
                    .setLore(
                        "§7─────────────────",
                        "§6Position: §e#" + position,
                        "§6Kills: §e" + viewerStats.getKills(),
                        "§6K/D: §a" + viewerStats.getFormattedKDRatio(),
                        "§7─────────────────"
                    )
                    .build();
            gui.setItem(53, viewerPositionItem);
        }
        
        // Bouton rafraîchir
        ItemStack refreshButton = new ItemBuilder(Material.LIME_DYE)
                .setDisplayName("§a🔄 Rafraîchir")
                .setLore("§7Cliquez pour mettre à jour")
                .build();
        gui.setItem(46, refreshButton);
        
        // Bouton fermer
        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
                .setDisplayName("§cFermer")
                .build();
        gui.setItem(49, closeButton);
        
        // Décoration
        fillEmptySlots(gui);
    }
    
    private ItemStack createLeaderboardItem(SessionStats stats, int position, Player viewer) {
        Material material = position <= 3 ? Material.PLAYER_HEAD : Material.PAPER;
        
        String positionPrefix = switch (position) {
            case 1 -> "§e🥇 ";
            case 2 -> "§7🥈 ";
            case 3 -> "§6🥉 ";
            default -> "§f" + position + ". ";
        };
        
        ItemBuilder builder = new ItemBuilder(material)
                .setDisplayName(positionPrefix + stats.getTeamEmoji() + stats.getPlayerName())
                .setLore(
                    "§7─────────────────",
                    "§6Équipe: " + stats.getTeamColorCode() + stats.getTeam().toUpperCase(),
                    "§6Kills: §e" + stats.getKills(),
                    "§6Deaths: §c" + stats.getDeaths(),
                    "§6K/D: §a" + stats.getFormattedKDRatio(),
                    "§6Killstreak Max: §e" + stats.getLongestKillstreak(),
                    "§7─────────────────"
                );
        
        // Tête du joueur si c'est un player head
        if (material == Material.PLAYER_HEAD) {
            builder.setSkullOwner(stats.getPlayerName());
        }
        
        // Effet spécial si c'est le viewer
        if (stats.getPlayerName().equals(viewer.getName())) {
            builder.addLore("§a§l➤ C'est vous!");
            builder.setGlowing(true);
        }
        
        return builder.build();
    }
    
    private int findPlayerPosition(List<SessionStats> topPlayers, String playerName) {
        for (int i = 0; i < topPlayers.size(); i++) {
            if (topPlayers.get(i).getPlayerName().equals(playerName)) {
                return i + 1;
            }
        }
        return topPlayers.size() + 1; // Si pas trouvé, position approximative
    }
    
    private void fillEmptySlots(Inventory gui) {
        ItemStack glassPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName("§0")
                .build();
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glassPane);
            }
        }
    }
}