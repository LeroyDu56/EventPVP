// ===== GuiListener.java - CORRECTION COMPLÈTE =====
package org.novania.eventpvp.listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.guis.AdminGui;

public class GuiListener implements Listener {
    
    private final EventPVP plugin;
    
    public GuiListener(EventPVP plugin) {
        this.plugin = plugin;
    }
    
    private boolean isOurGui(String title) {
        return title.contains("EventPVP") ||
               title.contains("Sélection de Kits") ||
               title.contains("Statistiques Event") ||
               title.contains("Classement Session") ||
               title.contains("Panel Admin") ||
               title.contains("🛡️ Sélection de Kits") ||
               title.contains("📊 Statistiques Event") ||
               title.contains("🏆 Classement Session") ||
               title.contains("🎛️ Panel Admin EventPVP");
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Vérifier si c'est une de nos GUI
        if (!isOurGui(title)) {
            return;
        }
        
        // Empêcher la modification de l'inventaire dans TOUTES nos GUI
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // Gérer selon le type de GUI
        if (title.contains("Sélection de Kits") || title.contains("🛡️")) {
            handleKitsGui(player, clickedItem);
        } else if (title.contains("Statistiques Event") || title.contains("📊")) {
            handleStatsGui(player, clickedItem);
        } else if (title.contains("Classement Session") || title.contains("🏆")) {
            handleLeaderboardGui(player, clickedItem);
        } else if (title.contains("Panel Admin") || title.contains("🎛️")) {
            handleAdminGui(player, clickedItem, event.getClick());
        }
    }
    
    private void handleKitsGui(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        String displayName = meta.getDisplayName();
        
        // Bouton retour
        if (item.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }
        
        // Kits - Vérifier si disponible dans le lore
        List<String> lore = meta.getLore();
        if (lore != null && lore.stream().anyMatch(line -> line.contains("NON DISPONIBLE"))) {
            return; // Kit non disponible
        }
        
        // Donner les kits selon le displayName
        if (displayName.contains("BUILD")) {
            plugin.getKitManager().giveKit(player, "build");
            player.closeInventory();
        } else if (displayName.contains("PVP") || displayName.contains("⚔️")) {
            plugin.getKitManager().giveKit(player, "pvp");
            player.closeInventory();
        } else if (displayName.contains("ARCHER") || displayName.contains("🏹")) {
            plugin.getKitManager().giveKit(player, "archer");
            player.closeInventory();
        } else if (displayName.contains("TANK") || displayName.contains("🛡️")) {
            plugin.getKitManager().giveKit(player, "tank");
            player.closeInventory();
        } else if (displayName.contains("ASSASSIN") || displayName.contains("🗡️")) {
            plugin.getKitManager().giveKit(player, "assassin");
            player.closeInventory();
        }
    }
    
    private void handleStatsGui(Player player, ItemStack item) {
        if (item.getType() == Material.BARRIER) {
            player.closeInventory();
        }
    }
    
    private void handleLeaderboardGui(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        if (item.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (item.getType() == Material.LIME_DYE && meta.getDisplayName().contains("Rafraîchir")) {
            // Rafraîchir le leaderboard
            plugin.getScoreboardManager().forceUpdateAll();
            new org.novania.eventpvp.guis.LeaderboardGui(plugin).open(player);
        }
    }
    
    private void handleAdminGui(Player player, ItemStack item, ClickType clickType) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        String displayName = meta.getDisplayName();
        AdminGui adminGui = new AdminGui(plugin);
        
        // Actions selon l'item cliqué
        if (item.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (displayName.contains("Démarrer Event") || displayName.contains("▶️")) {
            adminGui.handleStartEvent(player);
        } else if (displayName.contains("Arrêter Event") || displayName.contains("⏹️")) {
            adminGui.handleStopEvent(player);
        } else if (displayName.contains("Reset Event") || displayName.contains("🔄")) {
            if (clickType == ClickType.SHIFT_LEFT) {
                // Reset complet
                adminGui.handleResetPoints(player);
                adminGui.handleResetKits(player);
            } else {
                // Reset points seulement
                adminGui.handleResetPoints(player);
            }
        } else if (displayName.contains("Condition Victoire") || displayName.contains("🏆")) {
            player.sendMessage("§e💡 Utilisez §6/eventpvp victory <points> §epour changer l'objectif");
            player.closeInventory();
        } else if (displayName.contains("Gestion Équipes") || displayName.contains("👥")) {
            adminGui.showDetailedStats(player);
            player.closeInventory();
        } else if (displayName.contains("Équilibrer") || displayName.contains("⚖️")) {
            player.sendMessage("§e💡 Auto-équilibrage en développement. Utilisez §6/eventpvp assign §epour assigner manuellement.");
            player.closeInventory();
        } else if (displayName.contains("Assigner Équipes") || displayName.contains("➕")) {
            player.sendMessage("§e💡 Utilisez §6/eventpvp assign <joueur> <rouge|bleu|spectator>");
            player.closeInventory();
        } else if (displayName.contains("Vider Équipes") || displayName.contains("🗑️")) {
            if (clickType == ClickType.SHIFT_LEFT) {
                adminGui.handleClearTeams(player);
            } else {
                player.sendMessage("§c⚠️ Shift+Clic pour confirmer la suppression de toutes les équipes!");
            }
        } else if (displayName.contains("Gestion Kits") || displayName.contains("🛡️")) {
            if (clickType == ClickType.RIGHT) {
                player.sendMessage("§e💡 Fonctionnalité 'Forcer kits' en développement");
            } else {
                adminGui.handleResetKits(player);
            }
        } else if (displayName.contains("Gestion Glow") || displayName.contains("✨")) {
            if (clickType == ClickType.RIGHT) {
                adminGui.handleRemoveAllGlow(player);
            } else {
                adminGui.handleRefreshGlow(player);
            }
        } else if (displayName.contains("TP Équipes") || displayName.contains("🚀")) {
            adminGui.handleTeleportTeams(player);
        } else if (displayName.contains("Heal All") || displayName.contains("💚")) {
            adminGui.handleHealAll(player);
        } else if (displayName.contains("Statistiques") || displayName.contains("📊")) {
            adminGui.showDetailedStats(player);
            player.closeInventory();
        } else if (displayName.contains("Recharger") || displayName.contains("🔄")) {
            adminGui.handleReload(player);
        } else if (displayName.contains("Aide") || displayName.contains("❓")) {
            adminGui.showHelp(player);
            player.closeInventory();
        }
    }
}