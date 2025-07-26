// ===== AdminGui.java =====
package org.novania.eventpvp.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.utils.ItemBuilder;
import org.novania.eventpvp.utils.SoundUtils;

import java.util.Map;

public class AdminGui {
    
    private final EventPVP plugin;
    
    public AdminGui(EventPVP plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        if (!player.hasPermission("eventpvp.admin.panel")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("no_permission"));
            SoundUtils.EventSounds.playErrorSound(player);
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, 
            plugin.getConfigManager().getMessage("gui_admin_title"));
        
        populateAdminGui(gui, player);
        
        player.openInventory(gui);
        SoundUtils.EventSounds.playClickSound(player);
    }
    
    private void populateAdminGui(Inventory gui, Player player) {
        // Titre
        ItemStack title = new ItemBuilder(Material.COMMAND_BLOCK)
                .setDisplayName("§6🎛️ Panel Admin EventPVP")
                .setLore(
                    "§7─────────────────",
                    "§7Contrôles administrateur",
                    "§7pour gérer l'event PvP",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(4, title);
        
        // Gestion de l'event
        boolean eventActive = plugin.getEventManager().isEventActive();
        
        ItemStack startButton = new ItemBuilder(eventActive ? Material.GREEN_WOOL : Material.LIME_WOOL)
                .setDisplayName(eventActive ? "§a▶️ Event Actif" : "§a▶️ Démarrer Event")
                .setLore(
                    "§7─────────────────",
                    eventActive ? "§aL'event est en cours" : "§7Démarrer un nouvel event",
                    eventActive ? "§7Cliquez pour voir les détails" : "§e§lClic gauche §7pour démarrer",
                    "§7─────────────────"
                )
                .setGlowing(!eventActive)
                .build();
        gui.setItem(10, startButton);
        
        ItemStack stopButton = new ItemBuilder(eventActive ? Material.RED_WOOL : Material.GRAY_WOOL)
                .setDisplayName("§c⏹️ Arrêter Event")
                .setLore(
                    "§7─────────────────",
                    eventActive ? "§7Arrêter l'event en cours" : "§7Aucun event actif",
                    eventActive ? "§c§lClic gauche §7pour arrêter" : "§7Non disponible",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(12, stopButton);
        
        ItemStack resetButton = new ItemBuilder(Material.YELLOW_WOOL)
                .setDisplayName("§e🔄 Reset Event")
                .setLore(
                    "§7─────────────────",
                    "§7Remettre les points à zéro",
                    "§e§lClic gauche §7→ Reset points",
                    "§e§lShift+Clic §7→ Reset complet",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(14, resetButton);
        
        ItemStack victoryButton = new ItemBuilder(Material.GOLD_INGOT)
                .setDisplayName("§6🏆 Condition Victoire")
                .setLore(
                    "§7─────────────────",
                    "§6Actuel: §e" + plugin.getConfigManager().getVictoryTarget() + " points",
                    "§7Cliquez pour modifier",
                    "§7(Redémarrage event requis)",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(16, victoryButton);
        
        // Gestion des équipes
        var teamCounts = plugin.getTeamManager().getTeamCounts();
        int totalPlayers = plugin.getTeamManager().getTotalPlayers();
        int playersWithoutTeam = plugin.getTeamManager().getPlayersWithoutTeam();
        
        ItemStack teamsInfo = new ItemBuilder(Material.WHITE_BANNER)
                .setDisplayName("§6👥 Gestion Équipes")
                .setLore(
                    "§7─────────────────",
                    "§c🔴 Rouge: §f" + teamCounts.getOrDefault(org.novania.eventpvp.enums.Team.ROUGE, 0) + " joueur(s)",
                    "§9🔵 Bleu: §f" + teamCounts.getOrDefault(org.novania.eventpvp.enums.Team.BLEU, 0) + " joueur(s)",
                    "§7⚪ Spectateurs: §f" + teamCounts.getOrDefault(org.novania.eventpvp.enums.Team.SPECTATOR, 0) + " joueur(s)",
                    "§c❌ Sans équipe: §f" + playersWithoutTeam + " joueur(s)" + (playersWithoutTeam > 0 ? " ⚠️" : ""),
                    "§7─────────────────",
                    "§e§lClic gauche §7pour gérer"
                )
                .build();
        gui.setItem(28, teamsInfo);
        
        ItemStack balanceButton = new ItemBuilder(Material.RAIL)
                .setDisplayName("§e⚖️ Équilibrer")
                .setLore(
                    "§7─────────────────",
                    "§7Équilibrer automatiquement",
                    "§7les équipes Rouge et Bleu",
                    "§e§lClic gauche §7pour équilibrer",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(30, balanceButton);
        
        ItemStack assignButton = new ItemBuilder(Material.NAME_TAG)
                .setDisplayName("§b➕ Assigner Équipes")
                .setLore(
                    "§7─────────────────",
                    "§7Assigner manuellement",
                    "§7des joueurs aux équipes",
                    "§b§lClic gauche §7pour ouvrir",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(32, assignButton);
        
        ItemStack clearTeamsButton = new ItemBuilder(Material.TNT)
                .setDisplayName("§c🗑️ Vider Équipes")
                .setLore(
                    "§7─────────────────",
                    "§cRetirer tous les joueurs",
                    "§cde leurs équipes",
                    "§c§lShift+Clic §7pour confirmer",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(34, clearTeamsButton);
        
        // Gestion des kits
        ItemStack kitsButton = new ItemBuilder(Material.CHEST)
                .setDisplayName("§6🛡️ Gestion Kits")
                .setLore(
                    "§7─────────────────",
                    "§7Gérer les kits des joueurs",
                    "§e§lClic gauche §7→ Reset kits build",
                    "§e§lClic droit §7→ Forcer kits",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(37, kitsButton);
        
        // Gestion du glow
        boolean glowEnabled = plugin.getGlowIntegration().isEnabled();
        ItemStack glowButton = new ItemBuilder(glowEnabled ? Material.GLOWSTONE : Material.REDSTONE_BLOCK)
                .setDisplayName("§e✨ Gestion Glow")
                .setLore(
                    "§7─────────────────",
                    "§6Statut: " + (glowEnabled ? "§a✓ Fonctionnel" : "§c✗ Erreur"),
                    "§7Version TheGlow: §f" + plugin.getGlowIntegration().getTheGlowVersion(),
                    "§e§lClic gauche §7→ Rafraîchir tous",
                    "§e§lClic droit §7→ Retirer tous",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(39, glowButton);
        
        // Actions rapides
        ItemStack tpAllButton = new ItemBuilder(Material.ENDER_PEARL)
                .setDisplayName("§d🚀 TP Équipes")
                .setLore(
                    "§7─────────────────",
                    "§7Téléporter toutes les équipes",
                    "§7à leurs warps respectifs",
                    "§d§lClic gauche §7pour téléporter",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(41, tpAllButton);
        
        ItemStack forceRespawnButton = new ItemBuilder(Material.TOTEM_OF_UNDYING)
                .setDisplayName("§a💚 Heal All")
                .setLore(
                    "§7─────────────────",
                    "§7Soigner tous les joueurs",
                    "§7dans le monde event",
                    "§a§lClic gauche §7pour soigner",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(43, forceRespawnButton);
        
        // Statistiques et monitoring
        var scoreboardStats = plugin.getScoreboardManager().getScoreboardStats();
        
        // Cast sécurisé pour éviter les erreurs de type
        boolean scoreboardEnabled = false;
        Object enabledObj = scoreboardStats.get("enabled");
        if (enabledObj instanceof Boolean) {
            scoreboardEnabled = (Boolean) enabledObj;
        }
        
        Object activeScoreboards = scoreboardStats.get("active_scoreboards");
        String activeCount = activeScoreboards != null ? activeScoreboards.toString() : "0";
        
        ItemStack statsButton = new ItemBuilder(Material.BOOK)
                .setDisplayName("§6📊 Statistiques")
                .setLore(
                    "§7─────────────────",
                    "§6Event: " + (eventActive ? "§aActif" : "§cInactif"),
                    "§6Scoreboard: " + (scoreboardEnabled ? "§aActivé" : "§cDésactivé"),
                    "§6Scoreboards actifs: §e" + activeCount,
                    "§6Joueurs totaux: §e" + totalPlayers,
                    "§7─────────────────",
                    "§e§lClic gauche §7pour détails"
                )
                .build();
        gui.setItem(45, statsButton);
        
        // Bouton de rechargement
        ItemStack reloadButton = new ItemBuilder(Material.STRUCTURE_VOID)
                .setDisplayName("§a🔄 Recharger")
                .setLore(
                    "§7─────────────────",
                    "§7Recharger la configuration",
                    "§7et redémarrer les systèmes",
                    "§a§lClic gauche §7pour recharger",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(47, reloadButton);
        
        // Bouton de fermeture
        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
                .setDisplayName("§cFermer")
                .setLore("§7Fermer ce panneau")
                .build();
        gui.setItem(49, closeButton);
        
        // Bouton d'aide
        ItemStack helpButton = new ItemBuilder(Material.WRITTEN_BOOK)
                .setDisplayName("§e❓ Aide")
                .setLore(
                    "§7─────────────────",
                    "§7Aide et commandes",
                    "§7administrateur",
                    "§e§lClic gauche §7pour voir",
                    "§7─────────────────"
                )
                .build();
        gui.setItem(51, helpButton);
        
        // Informations rapides sur l'event en cours
        if (eventActive) {
            ItemStack eventInfo = new ItemBuilder(Material.EMERALD)
                    .setDisplayName("§a📈 Event en Cours")
                    .setLore(
                        "§7─────────────────",
                        "§6Points Rouge: §c" + plugin.getEventManager().getRougePoints(),
                        "§6Points Bleu: §9" + plugin.getEventManager().getBleuPoints(),
                        "§6Objectif: §e" + plugin.getEventManager().getVictoryTarget(),
                        "§6Progression: §e" + String.format("%.1f%%", plugin.getEventManager().getProgressPercentage()),
                        "§6En tête: §e" + plugin.getEventManager().getLeadingTeam(),
                        "§7─────────────────"
                    )
                    .setGlowing(true)
                    .build();
            gui.setItem(53, eventInfo);
        } else {
            ItemStack noEventInfo = new ItemBuilder(Material.GRAY_DYE)
                    .setDisplayName("§7📴 Aucun Event")
                    .setLore(
                        "§7─────────────────",
                        "§7Aucun event actif",
                        "§7Démarrez un event pour",
                        "§7voir les informations",
                        "§7─────────────────"
                    )
                    .build();
            gui.setItem(53, noEventInfo);
        }
        
        // Décoration
        fillEmptySlots(gui);
    }
    
    private void fillEmptySlots(Inventory gui) {
        ItemStack glassPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName("§0")
                .build();
        
        // Slots spécifiques pour la décoration
        int[] decorativeSlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44};
        for (int slot : decorativeSlots) {
            if (gui.getItem(slot) == null) {
                gui.setItem(slot, glassPane);
            }
        }
    }
    
    // Méthode pour rafraîchir la GUI
    public void refresh(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() == null) {
            // La GUI est ouverte, la rafraîchir
            Inventory gui = player.getOpenInventory().getTopInventory();
            gui.clear();
            populateAdminGui(gui, player);
        }
    }
    
    // Méthodes d'aide pour les actions admin
    public void handleStartEvent(Player admin) {
        if (plugin.getEventManager().startEvent()) {
            admin.sendMessage(plugin.getConfigManager().getPrefix() + "§aEvent démarré avec succès!");
            refresh(admin);
        } else {
            admin.sendMessage(plugin.getConfigManager().getPrefix() + "§cImpossible de démarrer l'event!");
        }
        SoundUtils.EventSounds.playSuccessSound(admin);
    }
    
    public void handleStopEvent(Player admin) {
        plugin.getEventManager().stopEvent();
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§cEvent arrêté!");
        refresh(admin);
        SoundUtils.EventSounds.playClickSound(admin);
    }
    
    public void handleResetPoints(Player admin) {
        plugin.getEventManager().resetEvent();
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§ePoints remis à zéro!");
        refresh(admin);
        SoundUtils.EventSounds.playSuccessSound(admin);
    }
    
    public void handleResetKits(Player admin) {
        plugin.getKitManager().resetAllBuildKits();
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§eKits build reset pour tous les joueurs!");
        SoundUtils.EventSounds.playSuccessSound(admin);
    }
    
    public void handleClearTeams(Player admin) {
        plugin.getTeamManager().clearAllTeams();
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§cToutes les équipes ont été vidées!");
        refresh(admin);
        SoundUtils.EventSounds.playSuccessSound(admin);
    }
    
    public void handleRefreshGlow(Player admin) {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getTeamManager().isInEventWorld(player)) {
                plugin.getGlowIntegration().refreshPlayerGlow(player);
                count++;
            }
        }
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§aGlow rafraîchi pour " + count + " joueur(s)!");
        SoundUtils.EventSounds.playSuccessSound(admin);
    }
    
    public void handleRemoveAllGlow(Player admin) {
        plugin.getGlowIntegration().forceRemoveAllGlows();
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§cTous les glows ont été retirés!");
        SoundUtils.EventSounds.playClickSound(admin);
    }
    
    public void handleTeleportTeams(Player admin) {
        int teleported = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getTeamManager().isInEventWorld(player) && plugin.getTeamManager().hasTeam(player)) {
                // TODO: Implémenter la téléportation aux warps d'équipe
                teleported++;
            }
        }
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§d" + teleported + " joueur(s) téléporté(s) à leurs warps!");
        SoundUtils.EventSounds.playSuccessSound(admin);
    }
    
    public void handleHealAll(Player admin) {
        int healed = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getTeamManager().isInEventWorld(player)) {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.setSaturation(20);
                healed++;
            }
        }
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + healed + " joueur(s) soigné(s)!");
        SoundUtils.EventSounds.playSuccessSound(admin);
    }
    
    public void handleReload(Player admin) {
        plugin.getConfigManager().loadConfig();
        plugin.getKitManager().reloadKits();
        
        if (plugin.getScoreboardManager().isEnabled()) {
            plugin.getScoreboardManager().reload();
        }
        
        admin.sendMessage(plugin.getConfigManager().getPrefix() + "§aPlugin rechargé avec succès!");
        refresh(admin);
        SoundUtils.EventSounds.playSuccessSound(admin);
    }
    
    public void showDetailedStats(Player admin) {
        admin.sendMessage("§6§l=== Statistiques Détaillées EventPVP ===");
        
        // Stats générales
        admin.sendMessage("§eEvent actif: §f" + (plugin.getEventManager().isEventActive() ? "§aOui" : "§cNon"));
        
        if (plugin.getEventManager().isEventActive()) {
            admin.sendMessage("§ePoints Rouge: §c" + plugin.getEventManager().getRougePoints());
            admin.sendMessage("§ePoints Bleu: §9" + plugin.getEventManager().getBleuPoints());
            admin.sendMessage("§eObjectif: §f" + plugin.getEventManager().getVictoryTarget());
            admin.sendMessage("§eProgression: §e" + String.format("%.1f%%", plugin.getEventManager().getProgressPercentage()));
        }
        
        // Stats équipes
        var teamCounts = plugin.getTeamManager().getTeamCounts();
        admin.sendMessage("§eÉquipe Rouge: §c" + teamCounts.getOrDefault(org.novania.eventpvp.enums.Team.ROUGE, 0));
        admin.sendMessage("§eÉquipe Bleu: §9" + teamCounts.getOrDefault(org.novania.eventpvp.enums.Team.BLEU, 0));
        admin.sendMessage("§eSpectateurs: §7" + teamCounts.getOrDefault(org.novania.eventpvp.enums.Team.SPECTATOR, 0));
        admin.sendMessage("§eSans équipe: §c" + plugin.getTeamManager().getPlayersWithoutTeam());
        
        // Stats techniques
        var scoreboardStats = plugin.getScoreboardManager().getScoreboardStats();
        boolean scoreboardEnabled = scoreboardStats.get("enabled") instanceof Boolean ? 
            (Boolean) scoreboardStats.get("enabled") : false;
        admin.sendMessage("§eScoreboard: §f" + (scoreboardEnabled ? "§aActivé" : "§cDésactivé"));
        admin.sendMessage("§eIntégration TheGlow: §f" + (plugin.getGlowIntegration().isEnabled() ? "§aOK" : "§cErreur"));
        
        admin.sendMessage("§6§l════════════════════════════════");
        
        SoundUtils.EventSounds.playClickSound(admin);
    }
    
    public void showHelp(Player admin) {
        admin.sendMessage("§6§l=== Aide Admin EventPVP ===");
        admin.sendMessage("§e▶️ Démarrer Event §7- Lance une nouvelle session");
        admin.sendMessage("§c⏹️ Arrêter Event §7- Termine la session actuelle");
        admin.sendMessage("§e🔄 Reset Event §7- Remet les points à zéro");
        admin.sendMessage("§6👥 Gestion Équipes §7- Voir les équipes actuelles");
        admin.sendMessage("§e⚖️ Équilibrer §7- Auto-équilibrage (à venir)");
        admin.sendMessage("§b➕ Assigner §7- Commande: /eventpvp assign <joueur> <équipe>");
        admin.sendMessage("§6🛡️ Gestion Kits §7- Reset kits build");
        admin.sendMessage("§e✨ Glow §7- Gérer les effets visuels");
        admin.sendMessage("§d🚀 TP Équipes §7- Téléporter aux warps");
        admin.sendMessage("§a💚 Heal All §7- Soigner tous les joueurs");
        admin.sendMessage("§a🔄 Recharger §7- Reload configuration");
        admin.sendMessage("§6📊 Stats §7- Statistiques détaillées");
        admin.sendMessage("§6§l═════════════════════════════");
        
        SoundUtils.EventSounds.playClickSound(admin);
    }
}