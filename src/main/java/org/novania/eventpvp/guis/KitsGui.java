// ===== KitsGui.java - CORRECTION DUPLICATION BUILD =====
package org.novania.eventpvp.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.enums.Team;
import org.novania.eventpvp.kits.Kit;
import org.novania.eventpvp.utils.ItemBuilder;
import org.novania.eventpvp.utils.SoundUtils;

public class KitsGui {
    
    private final EventPVP plugin;
    
    public KitsGui(EventPVP plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        // Vérifications préliminaires
        if (!plugin.getTeamManager().isInEventWorld(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("wrong_world"));
            SoundUtils.EventSounds.playErrorSound(player);
            return;
        }
        
        Team playerTeam = plugin.getTeamManager().getPlayerTeam(player);
        if (playerTeam == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("no_team"));
            SoundUtils.EventSounds.playErrorSound(player);
            return;
        }
        
        if (playerTeam == Team.SPECTATOR) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cLes spectateurs ne peuvent pas utiliser de kits!");
            SoundUtils.EventSounds.playErrorSound(player);
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, 
            plugin.getConfigManager().getMessage("gui_kits_title"));
        
        // Remplir la GUI
        populateKitsGui(gui, player, playerTeam);
        
        player.openInventory(gui);
        SoundUtils.EventSounds.playClickSound(player);
    }
    
    private void populateKitsGui(Inventory gui, Player player, Team team) {
        // Titre et informations
        ItemStack teamInfo = new ItemBuilder(Material.WHITE_BANNER)
                .setDisplayName(team.getColorCode() + team.getEmoji() + " Équipe: " + team.getDisplayName())
                .setLore(
                    "§7─────────────────",
                    "§7Vous êtes dans l'équipe " + team.getDisplayName(),
                    "§7Sélectionnez un kit ci-dessous",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(4, teamInfo);
        
        // CORRECTION: Placer les kits à des positions fixes pour éviter la duplication
        
        // Kit BUILD (position fixe slot 19)
        Kit buildKit = plugin.getKitManager().getKit("build");
        if (buildKit != null) {
            ItemStack buildItem = buildKit.createDisplayItem(player);
            gui.setItem(19, buildItem);
        }
        
        // Kits COMBAT (positions fixes à partir du slot 28)
        Kit pvpKit = plugin.getKitManager().getKit("pvp");
        if (pvpKit != null) {
            gui.setItem(28, pvpKit.createDisplayItem(player));
        }
        
        Kit archerKit = plugin.getKitManager().getKit("archer");
        if (archerKit != null) {
            gui.setItem(29, archerKit.createDisplayItem(player));
        }
        
        Kit tankKit = plugin.getKitManager().getKit("tank");
        if (tankKit != null) {
            gui.setItem(30, tankKit.createDisplayItem(player));
        }
        
        Kit assassinKit = plugin.getKitManager().getKit("assassin");
        if (assassinKit != null) {
            gui.setItem(31, assassinKit.createDisplayItem(player));
        }
        
        // Informations sur les types de kits
        ItemStack buildInfo = new ItemBuilder(Material.STONE_BRICKS)
                .setDisplayName("§6🏗️ Kits BUILD")
                .setLore(
                    "§7─────────────────",
                    "§7• Usage unique par session",
                    "§7• Conservé à la mort",
                    "§7• Matériaux de construction",
                    "§7• Outils efficaces",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(10, buildInfo);
        
        ItemStack combatInfo = new ItemBuilder(Material.IRON_SWORD)
                .setDisplayName("§c⚔️ Kits COMBAT")
                .setLore(
                    "§7─────────────────",
                    "§7• Usage illimité",
                    "§7• Reset à la mort",
                    "§7• Différents styles de combat",
                    "§7• Équipement spécialisé",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(37, combatInfo);
        
        // Stats personnelles si disponibles
        var playerStats = plugin.getEventManager().getPlayerStats(player);
        if (playerStats != null) {
            ItemStack statsItem = new ItemBuilder(Material.BOOK)
                    .setDisplayName("§e📊 Vos Stats")
                    .setLore(
                        "§7─────────────────",
                        "§6Kills: §e" + playerStats.getKills(),
                        "§6Deaths: §c" + playerStats.getDeaths(),
                        "§6K/D: §a" + playerStats.getFormattedKDRatio(),
                        "§6Killstreak: §e" + plugin.getEventManager().getPlayerKillstreak(player),
                        "§7─────────────────"
                    )
                    .build();
            gui.setItem(16, statsItem);
        }
        
        // Bouton de fermeture
        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
                .setDisplayName("§cFermer")
                .setLore("§7Cliquez pour fermer ce menu")
                .build();
        gui.setItem(49, closeButton);
        
        // Décoration
        ItemStack glassPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName("§0")
                .build();
        
        // Bordure
        int[] borderSlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : borderSlots) {
            gui.setItem(slot, glassPane);
        }
    }
}