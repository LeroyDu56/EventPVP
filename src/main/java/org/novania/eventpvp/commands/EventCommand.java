// ===== EventCommand.java =====
package org.novania.eventpvp.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.guis.AdminGui;
import org.novania.eventpvp.guis.KitsGui;
import org.novania.eventpvp.guis.LeaderboardGui;
import org.novania.eventpvp.guis.StatsGui;
import org.novania.eventpvp.utils.SoundUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventCommand implements CommandExecutor, TabCompleter {
    
    private final EventPVP plugin;
    
    public EventCommand(EventPVP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // Vérifier que c'est un joueur
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§cCette commande ne peut être utilisée que par un joueur!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Vérifier les permissions de base
        if (!player.hasPermission("eventpvp.participate")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("no_permission"));
            SoundUtils.EventSounds.playErrorSound(player);
            return true;
        }
        
        // Si aucun argument, afficher l'aide
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        // Traiter les sous-commandes
        switch (args[0].toLowerCase()) {
            case "kits":
            case "kit":
                handleKitsCommand(player, args);
                break;
                
            case "stats":
            case "stat":
                handleStatsCommand(player, args);
                break;
                
            case "team":
            case "équipe":
                handleTeamCommand(player);
                break;
                
            case "leaderboard":
            case "classement":
            case "top":
                handleLeaderboardCommand(player);
                break;
                
            case "admin":
                handleAdminCommand(player);
                break;
                
            case "help":
            case "aide":
                showHelp(player);
                break;
                
            default:
                player.sendMessage(plugin.getConfigManager().getPrefix() + 
                    "§cSous-commande inconnue: §e" + args[0]);
                showHelp(player);
        }
        
        return true;
    }
    
    private void handleKitsCommand(Player player, String[] args) {
        // Vérifier les permissions
        if (!player.hasPermission("eventpvp.kits.use")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("no_permission"));
            SoundUtils.EventSounds.playErrorSound(player);
            return;
        }
        
        // Si un kit spécifique est demandé
        if (args.length >= 2) {
            String kitName = args[1];
            if (plugin.getKitManager().giveKit(player, kitName)) {
                // Kit donné avec succès
                return;
            }
        }
        
        // Ouvrir la GUI des kits
        new KitsGui(plugin).open(player);
    }
    
    private void handleStatsCommand(Player player, String[] args) {
        // Vérifier les permissions
        if (!player.hasPermission("eventpvp.stats.view")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("no_permission"));
            SoundUtils.EventSounds.playErrorSound(player);
            return;
        }
        
        // Déterminer le joueur cible
        Player targetPlayer = player;
        if (args.length >= 2) {
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + 
                    "§cJoueur §e" + args[1] + " §cintrouvable!");
                SoundUtils.EventSounds.playErrorSound(player);
                return;
            }
        }
        
        // Ouvrir la GUI des stats
        new StatsGui(plugin).open(player, targetPlayer);
    }
    
    private void handleTeamCommand(Player player) {
        var team = plugin.getTeamManager().getPlayerTeam(player);
        
        if (team == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("no_team"));
        } else {
            String teamName = plugin.getConfigManager().getTeamDisplayName(team.name().toLowerCase());
            String teamColor = plugin.getConfigManager().getTeamColorCode(team.name().toLowerCase());
            
            player.sendMessage("§7─────────────────");
            player.sendMessage("§6🎯 Votre Équipe:");
            player.sendMessage(teamColor + team.getEmoji() + " " + teamName);
            
            if (team.isCombatTeam()) {
                player.sendMessage("§7Type: §eCombat");
                player.sendMessage("§7Warp: §e/warp " + plugin.getConfigManager().getTeamWarp(team.name().toLowerCase()));
            } else {
                player.sendMessage("§7Type: §7Spectateur");
            }
            
            // Compter les membres de l'équipe
            int teamCount = plugin.getTeamManager().getTeamPlayerCount(team);
            player.sendMessage("§7Membres: §e" + teamCount + " joueur(s)");
            player.sendMessage("§7─────────────────");
        }
        
        SoundUtils.EventSounds.playClickSound(player);
    }
    
    private void handleLeaderboardCommand(Player player) {
        new LeaderboardGui(plugin).open(player);
    }
    
    private void handleAdminCommand(Player player) {
        // Vérifier les permissions admin
        if (!player.hasPermission("eventpvp.admin.panel")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("no_permission"));
            SoundUtils.EventSounds.playErrorSound(player);
            return;
        }
        
        // Ouvrir la GUI admin
        new AdminGui(plugin).open(player);
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6§l=== EventPVP - Commandes ===");
        player.sendMessage("§e/event kits §7- Ouvrir le menu des kits");
        player.sendMessage("§e/event kit <nom> §7- Équiper un kit directement");
        player.sendMessage("§e/event stats [joueur] §7- Voir les statistiques");
        player.sendMessage("§e/event team §7- Voir votre équipe");
        player.sendMessage("§e/event leaderboard §7- Voir le classement");
        
        if (player.hasPermission("eventpvp.admin.panel")) {
            player.sendMessage("§e/event admin §7- Panel d'administration");
        }
        
        player.sendMessage("§7");
        player.sendMessage("§7Kits disponibles:");
        String[] kitNames = plugin.getKitManager().getKitNames();
        player.sendMessage("§e" + String.join("§7, §e", kitNames));
        player.sendMessage("§6§l═══════════════════════════");
        
        SoundUtils.EventSounds.playClickSound(player);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Première argument : sous-commandes
            List<String> subcommands = Arrays.asList("kits", "kit", "stats", "team", "leaderboard", "help");
            
            // Ajouter admin si permission
            if (sender.hasPermission("eventpvp.admin.panel")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("admin");
            }
            
            completions.addAll(subcommands);
        } 
        else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "kit":
                    // Noms des kits
                    completions.addAll(Arrays.asList(plugin.getKitManager().getKitNames()));
                    break;
                    
                case "stats":
                    // Noms des joueurs en ligne
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()));
                    break;
            }
        }
        
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}