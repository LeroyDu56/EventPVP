// ===== EventPVPCommands.java =====
package org.novania.eventpvp.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.enums.Team;

public class EventPVPCommands implements CommandExecutor, TabCompleter {
    
    private final EventPVP plugin;
    
    public EventPVPCommands(EventPVP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // Vérifier les permissions admin
        if (!sender.hasPermission("eventpvp.admin.panel")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("no_permission"));
            return true;
        }
        
        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "info" -> showPluginInfo(sender);
            case "start" -> handleStart(sender);
            case "stop" -> handleStop(sender);
            case "reset" -> handleReset(sender, args);
            case "assign" -> handleAssign(sender, args);
            case "victory" -> handleVictory(sender, args);
            case "balance" -> handleBalance(sender);
            case "glow" -> handleGlow(sender, args);
            case "stats" -> handleStats(sender);
            default -> showAdminHelp(sender);
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().loadConfig();
        plugin.getKitManager().reloadKits();
        
        if (plugin.getScoreboardManager().isEnabled()) {
            plugin.getScoreboardManager().reload();
        }
        
        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
            plugin.getConfigManager().getMessage("reload_success"));
    }
    
    private void showPluginInfo(CommandSender sender) {
        sender.sendMessage("§6§l=== EventPVP - Informations ===");
        sender.sendMessage("§eVersion: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§eAuteur: §f" + plugin.getDescription().getAuthors().get(0));
        sender.sendMessage("§eDescription: §f" + plugin.getDescription().getDescription());
        sender.sendMessage("§eIntégrations: §fTheGlow" + (plugin.hasEconomy() ? ", Vault" : ""));
        sender.sendMessage("§eEvent actif: §f" + (plugin.getEventManager().isEventActive() ? "§aOui" : "§cNon"));
        sender.sendMessage("§eJoueurs avec équipe: §f" + plugin.getTeamManager().getTotalPlayers());
        sender.sendMessage("§6§l════════════════════════════");
    }
    
    private void handleStart(CommandSender sender) {
        if (plugin.getEventManager().startEvent()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("event_started"));
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cImpossible de démarrer l'event (déjà actif?)");
        }
    }
    
    private void handleStop(CommandSender sender) {
        plugin.getEventManager().stopEvent();
        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
            plugin.getConfigManager().getMessage("event_stopped"));
    }
    
    private void handleReset(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "points" -> {
                    plugin.getEventManager().resetEvent();
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                        plugin.getConfigManager().getMessage("points_reset"));
                }
                case "kits" -> {
                    plugin.getKitManager().resetAllBuildKits();
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                        plugin.getConfigManager().getMessage("kits_reset"));
                }
                case "teams" -> {
                    plugin.getTeamManager().clearAllTeams();
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                        "§aToutes les équipes ont été vidées!");
                }
                default -> sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                    "§cUsage: /eventpvp reset <points|kits|teams>");
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cUsage: /eventpvp reset <points|kits|teams>");
        }
    }
    
    private void handleAssign(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cUsage: /eventpvp assign <joueur> <rouge|bleu|spectator>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cJoueur §e" + args[1] + " §cintrouvable!");
            return;
        }
        
        Team team = Team.fromString(args[2]);
        if (team == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cÉquipe invalide! Utilisez: rouge, bleu, spectator");
            return;
        }
        
        plugin.getTeamManager().assignPlayerToTeam(target, team);
        
        String teamName = plugin.getConfigManager().getTeamDisplayName(team.name().toLowerCase());
        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
            "§a✓ " + target.getName() + " assigné à l'équipe " + teamName);
        
        // Ajouter à la session si event actif
        if (plugin.getEventManager().isEventActive()) {
            plugin.getEventManager().addPlayerToEvent(target, team);
        }
    }
    
    private void handleVictory(CommandSender sender, String[] args) {
        if (args.length < 2) {
            int current = plugin.getConfigManager().getVictoryTarget();
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§6Condition de victoire actuelle: §e" + current + " points");
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§7Usage: /eventpvp victory <points>");
            return;
        }
        
        try {
            int newTarget = Integer.parseInt(args[1]);
            if (newTarget <= 0) {
                sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                    "§cLe nombre de points doit être positif!");
                return;
            }
            
            // Mettre à jour la condition de victoire dans la configuration
            plugin.getConfig().set("victory.target", newTarget);
            plugin.saveConfig();
            plugin.getConfigManager().loadConfig();
            
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§aCondition de victoire changée: §e" + newTarget + " points");
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§7Note: Redémarrez l'event pour appliquer les changements");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cNombre invalide: §e" + args[1]);
        }
    }
    
    private void handleBalance(CommandSender sender) {
        var teamCounts = plugin.getTeamManager().getTeamCounts();
        int rouge = teamCounts.getOrDefault(Team.ROUGE, 0);
        int bleu = teamCounts.getOrDefault(Team.BLEU, 0);
        int spectator = teamCounts.getOrDefault(Team.SPECTATOR, 0);
        
        sender.sendMessage("§6§l=== Équilibrage des Équipes ===");
        sender.sendMessage("§c🔴 Rouge: §f" + rouge + " joueur(s)");
        sender.sendMessage("§9🔵 Bleu: §f" + bleu + " joueur(s)");
        sender.sendMessage("§7⚪ Spectateurs: §f" + spectator + " joueur(s)");
        
        int difference = Math.abs(rouge - bleu);
        if (difference <= 1) {
            sender.sendMessage("§a✓ Les équipes sont équilibrées!");
        } else {
            sender.sendMessage("§e⚠ Différence de " + difference + " joueur(s)");
            sender.sendMessage("§7Auto-équilibrage non implémenté pour l'instant");
        }
    }
    
    private void handleGlow(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cUsage: /eventpvp glow <refresh|remove> [joueur]");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "refresh" -> {
                if (args.length >= 3) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target != null) {
                        plugin.getGlowIntegration().refreshPlayerGlow(target);
                        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                            "§aGlow de " + target.getName() + " rafraîchi!");
                    } else {
                        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                            "§cJoueur introuvable!");
                    }
                } else {
                    // Rafraîchir tous
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (plugin.getTeamManager().isInEventWorld(player)) {
                            plugin.getGlowIntegration().refreshPlayerGlow(player);
                        }
                    }
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                        "§aTous les glows rafraîchis!");
                }
            }
            case "remove" -> {
                plugin.getGlowIntegration().forceRemoveAllGlows();
                sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                    "§aTous les glows retirés!");
            }
            default -> sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cUsage: /eventpvp glow <refresh|remove> [joueur]");
        }
    }
    
    private void handleStats(CommandSender sender) {
        var stats = plugin.getScoreboardManager().getScoreboardStats();
        
        sender.sendMessage("§6§l=== Statistiques EventPVP ===");
        sender.sendMessage("§eEvent actif: §f" + (plugin.getEventManager().isEventActive() ? "§aOui" : "§cNon"));
        
        // Cast sécurisé pour les valeurs du Map
        boolean scoreboardEnabled = stats.get("enabled") instanceof Boolean ? 
            (Boolean) stats.get("enabled") : false;
        sender.sendMessage("§eScoreboard: §f" + (scoreboardEnabled ? "§aActivé" : "§cDésactivé"));
        
        Object activeCount = stats.get("active_scoreboards");
        sender.sendMessage("§eScoreboards actifs: §f" + (activeCount != null ? activeCount : "0"));
        
        sender.sendMessage("§eIntégration TheGlow: §f" + (plugin.getGlowIntegration().isEnabled() ? "§aOK" : "§cErreur"));
        
        if (plugin.getEventManager().isEventActive()) {
            sender.sendMessage("§ePoints Rouge: §c" + plugin.getEventManager().getRougePoints());
            sender.sendMessage("§ePoints Bleu: §9" + plugin.getEventManager().getBleuPoints());
            sender.sendMessage("§eObjectif: §f" + plugin.getEventManager().getVictoryTarget());
        }
        
        sender.sendMessage("§6§l═══════════════════════════");
    }
    
    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== EventPVP - Admin ===");
        sender.sendMessage("§e/eventpvp reload §7- Recharger la configuration");
        sender.sendMessage("§e/eventpvp info §7- Informations du plugin");
        sender.sendMessage("§e/eventpvp start §7- Démarrer l'event");
        sender.sendMessage("§e/eventpvp stop §7- Arrêter l'event");
        sender.sendMessage("§e/eventpvp reset <points|kits|teams> §7- Reset");
        sender.sendMessage("§e/eventpvp assign <joueur> <équipe> §7- Assigner équipe");
        sender.sendMessage("§e/eventpvp victory <points> §7- Changer objectif");
        sender.sendMessage("§e/eventpvp balance §7- Voir l'équilibrage");
        sender.sendMessage("§e/eventpvp glow <refresh|remove> §7- Gérer glow");
        sender.sendMessage("§e/eventpvp stats §7- Statistiques système");
        sender.sendMessage("§6§l═══════════════════════");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "info", "start", "stop", "reset", 
                "assign", "victory", "balance", "glow", "stats"));
        } 
        else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "reset" -> completions.addAll(Arrays.asList("points", "kits", "teams"));
                case "assign" -> completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
                case "glow" -> completions.addAll(Arrays.asList("refresh", "remove"));
                case "victory" -> completions.addAll(Arrays.asList("25", "50", "75", "100"));
            }
        }
        else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "assign" -> completions.addAll(Arrays.asList("rouge", "bleu", "spectator"));
                case "glow" -> {
                    if ("refresh".equals(args[1])) {
                        completions.addAll(Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .collect(Collectors.toList()));
                    }
                }
            }
        }
        
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}