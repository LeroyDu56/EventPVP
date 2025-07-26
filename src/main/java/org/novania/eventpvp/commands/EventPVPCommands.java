// ===== EventPVPCommands.java - VERSION COMPLÈTE CORRIGÉE =====
package org.novania.eventpvp.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
            case "debug" -> handleDebug(sender, args);  // NOUVEAU
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
        sender.sendMessage("§eMonde event: §f" + plugin.getConfigManager().getWorldName());
        sender.sendMessage("§eDebug mode: §f" + (plugin.getConfigManager().isDebugMode() ? "§aActivé" : "§cDésactivé"));
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
                case "glow" -> {
                    plugin.getGlowIntegration().forceRemoveAllGlows();
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                        "§aTous les glows ont été retirés!");
                }
                default -> sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                    "§cUsage: /eventpvp reset <points|kits|teams|glow>");
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cUsage: /eventpvp reset <points|kits|teams|glow>");
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
                "§cUsage: /eventpvp glow <refresh|remove|test> [joueur]");
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
                if (args.length >= 3) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target != null) {
                        plugin.getGlowIntegration().removePlayerGlow(target);
                        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                            "§aGlow de " + target.getName() + " retiré!");
                    } else {
                        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                            "§cJoueur introuvable!");
                    }
                } else {
                    plugin.getGlowIntegration().forceRemoveAllGlows();
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                        "§aTous les glows retirés!");
                }
            }
            case "test" -> {
                if (args.length >= 3) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target != null) {
                        boolean result = plugin.getGlowIntegration().testGlowFunctionality(target);
                        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                            "§7Test glow pour " + target.getName() + ": " + 
                            (result ? "§aSuccès" : "§cÉchec"));
                    } else {
                        sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                            "§cJoueur introuvable!");
                    }
                }
            }
            default -> sender.sendMessage(plugin.getConfigManager().getPrefix() + 
                "§cUsage: /eventpvp glow <refresh|remove|test> [joueur]");
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
        sender.sendMessage("§eVersion TheGlow: §f" + plugin.getGlowIntegration().getTheGlowVersion());
        
        if (plugin.getEventManager().isEventActive()) {
            sender.sendMessage("§ePoints Rouge: §c" + plugin.getEventManager().getRougePoints());
            sender.sendMessage("§ePoints Bleu: §9" + plugin.getEventManager().getBleuPoints());
            sender.sendMessage("§eObjectif: §f" + plugin.getEventManager().getVictoryTarget());
        }
        
        sender.sendMessage("§6§l═══════════════════════════");
    }
    
    // ===== NOUVELLES COMMANDES DEBUG =====
    
    private void handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eventpvp.admin.debug")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§cPermission manquante!");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§6=== Commandes Debug EventPVP ===");
            sender.sendMessage("§e/eventpvp debug glow <joueur> <couleur> §7- Test glow");
            sender.sendMessage("§e/eventpvp debug warp <joueur> <warp> §7- Test warp");
            sender.sendMessage("§e/eventpvp debug world <joueur> §7- Info monde joueur");
            sender.sendMessage("§e/eventpvp debug teams §7- Liste équipes");
            sender.sendMessage("§e/eventpvp debug respawn <joueur> §7- Test respawn");
            sender.sendMessage("§e/eventpvp debug forcerespawn <joueur> §7- Forcer mort");
            sender.sendMessage("§e/eventpvp debug location <joueur> §7- Info position");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "glow" -> handleDebugGlow(sender, args);
            case "warp" -> handleDebugWarp(sender, args);
            case "world" -> handleDebugWorld(sender, args);
            case "teams" -> handleDebugTeams(sender);
            case "respawn" -> handleDebugRespawn(sender, args);
            case "forcerespawn" -> handleDebugForceRespawn(sender, args);
            case "location" -> handleDebugLocation(sender, args);
            default -> sender.sendMessage("§cCommande debug inconnue: " + args[1]);
        }
    }
    
    private void handleDebugGlow(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /eventpvp debug glow <joueur> <couleur>");
            sender.sendMessage("§7Couleurs: red, blue, green, yellow, orange, purple, pink, gray, white, black");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[2]);
            return;
        }
        
        String color = args[3].toLowerCase();
        sender.sendMessage("§eTentative d'application du glow " + color + " à " + target.getName() + "...");
        
        // Test direct avec TheGlow
        String command = "theglow set " + target.getName() + " " + color;
        boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        
        if (success) {
            sender.sendMessage("§aCommande TheGlow exécutée: /" + command);
        } else {
            sender.sendMessage("§cÉchec de la commande TheGlow: /" + command);
        }
        
        // Test via notre intégration
        boolean integrationResult = plugin.getGlowIntegration().setPlayerGlow(target, color);
        sender.sendMessage("§7Résultat intégration: " + (integrationResult ? "§aSuccès" : "§cÉchec"));
    }
    
    private void handleDebugWarp(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /eventpvp debug warp <joueur> <warp>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[2]);
            return;
        }
        
        String warpName = args[3];
        sender.sendMessage("§eTentative de téléportation de " + target.getName() + " au warp " + warpName + "...");
        
        String command = "warp " + warpName + " " + target.getName();
        boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        
        if (success) {
            sender.sendMessage("§aCommande warp exécutée: /" + command);
        } else {
            sender.sendMessage("§cÉchec de la commande warp: /" + command);
        }
    }
    
    private void handleDebugWorld(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eventpvp debug world <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[2]);
            return;
        }
        
        sender.sendMessage("§6=== Debug Monde - " + target.getName() + " ===");
        sender.sendMessage("§7Monde actuel: §e" + target.getWorld().getName());
        sender.sendMessage("§7Monde event configuré: §e" + plugin.getConfigManager().getWorldName());
        sender.sendMessage("§7Dans monde event: " + (plugin.getTeamManager().isInEventWorld(target) ? "§aOui" : "§cNon"));
        sender.sendMessage("§7Position: §e" + target.getLocation().getBlockX() + ", " + 
                           target.getLocation().getBlockY() + ", " + target.getLocation().getBlockZ());
        sender.sendMessage("§7GameMode: §e" + target.getGameMode());
        
        var team = plugin.getTeamManager().getPlayerTeam(target);
        sender.sendMessage("§7Équipe: " + (team != null ? "§e" + team : "§cAucune"));
    }
    
    private void handleDebugTeams(CommandSender sender) {
        var teamCounts = plugin.getTeamManager().getTeamCounts();
        
        sender.sendMessage("§6=== Debug Équipes ===");
        sender.sendMessage("§cRouge: §f" + teamCounts.getOrDefault(Team.ROUGE, 0));
        sender.sendMessage("§9Bleu: §f" + teamCounts.getOrDefault(Team.BLEU, 0));
        sender.sendMessage("§7Spectateurs: §f" + teamCounts.getOrDefault(Team.SPECTATOR, 0));
        sender.sendMessage("§7Total: §f" + plugin.getTeamManager().getTotalPlayers());
        sender.sendMessage("§7Sans équipe: §f" + plugin.getTeamManager().getPlayersWithoutTeam());
        
        sender.sendMessage("§6TheGlow:");
        sender.sendMessage("§7Activé: " + (plugin.getGlowIntegration().isEnabled() ? "§aOui" : "§cNon"));
        sender.sendMessage("§7Version: §e" + plugin.getGlowIntegration().getTheGlowVersion());
    }
    
    private void handleDebugRespawn(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eventpvp debug respawn <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[2]);
            return;
        }
        
        var team = plugin.getTeamManager().getPlayerTeam(target);
        if (team == null) {
            sender.sendMessage("§cLe joueur n'a pas d'équipe assignée!");
            return;
        }
        
        // Calculer la position de respawn
        World eventWorld = Bukkit.getWorld(plugin.getConfigManager().getWorldName());
        if (eventWorld == null) {
            sender.sendMessage("§cMonde event introuvable!");
            return;
        }
        
        Location respawnLoc = getDebugRespawnLocation(team, eventWorld);
        
        sender.sendMessage("§6=== Debug Respawn - " + target.getName() + " ===");
        sender.sendMessage("§7Équipe: §e" + team);
        sender.sendMessage("§7Monde actuel: §e" + target.getWorld().getName());
        sender.sendMessage("§7Position actuelle: §e" + target.getLocation().getBlockX() + ", " + 
                           target.getLocation().getBlockY() + ", " + target.getLocation().getBlockZ());
        sender.sendMessage("§7Position respawn calculée: §e" + respawnLoc.getBlockX() + ", " + 
                           respawnLoc.getBlockY() + ", " + respawnLoc.getBlockZ());
        sender.sendMessage("§7Bloc sous respawn: §e" + respawnLoc.clone().subtract(0, 1, 0).getBlock().getType());
        
        // Test de téléportation
        sender.sendMessage("§eTentative de téléportation...");
        target.teleport(respawnLoc);
        sender.sendMessage("§aTéléportation effectuée!");
    }
    
    private void handleDebugForceRespawn(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eventpvp debug forcerespawn <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[2]);
            return;
        }
        
        sender.sendMessage("§eSimulation de la mort et respawn pour " + target.getName() + "...");
        
        // Simuler la mort
        target.setHealth(0);
        
        sender.sendMessage("§aMort simulée! Le joueur va respawn automatiquement.");
    }
    
    private void handleDebugLocation(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eventpvp debug location <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable: " + args[2]);
            return;
        }
        
        Location loc = target.getLocation();
        World world = loc.getWorld();
        
        sender.sendMessage("§6=== Debug Location - " + target.getName() + " ===");
        sender.sendMessage("§7Monde: §e" + world.getName());
        sender.sendMessage("§7Coordonnées: §e" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        sender.sendMessage("§7Coordonnées exactes: §e" + String.format("%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ()));
        sender.sendMessage("§7Yaw/Pitch: §e" + String.format("%.1f, %.1f", loc.getYaw(), loc.getPitch()));
        sender.sendMessage("§7Bloc sous les pieds: §e" + loc.clone().subtract(0, 1, 0).getBlock().getType());
        sender.sendMessage("§7Bloc à la position: §e" + loc.getBlock().getType());
        sender.sendMessage("§7Chunk chargé: " + (world.isChunkLoaded(loc.getChunk()) ? "§aOui" : "§cNon"));
        
        // Vérifier si c'est une position sûre
        boolean safe = !loc.getBlock().getType().isSolid() && 
                       !loc.clone().add(0, 1, 0).getBlock().getType().isSolid() &&
                       !loc.clone().subtract(0, 1, 0).getBlock().getType().isAir();
        
        sender.sendMessage("§7Position sûre: " + (safe ? "§aOui" : "§cNon"));
    }
    
    private Location getDebugRespawnLocation(Team team, World eventWorld) {
        switch (team) {
            case ROUGE:
                return new Location(eventWorld, 100.5, 65, 100.5, 0, 0);
            case BLEU:
                return new Location(eventWorld, -99.5, 65, -99.5, 180, 0);
            case SPECTATOR:
                return new Location(eventWorld, 0.5, 70, 0.5, 0, 0);
            default:
                return eventWorld.getSpawnLocation();
        }
    }
    
    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== EventPVP - Admin ===");
        sender.sendMessage("§e/eventpvp reload §7- Recharger la configuration");
        sender.sendMessage("§e/eventpvp info §7- Informations du plugin");
        sender.sendMessage("§e/eventpvp start §7- Démarrer l'event");
        sender.sendMessage("§e/eventpvp stop §7- Arrêter l'event");
        sender.sendMessage("§e/eventpvp reset <points|kits|teams|glow> §7- Reset");
        sender.sendMessage("§e/eventpvp assign <joueur> <équipe> §7- Assigner équipe");
        sender.sendMessage("§e/eventpvp victory <points> §7- Changer objectif");
        sender.sendMessage("§e/eventpvp balance §7- Voir l'équilibrage");
        sender.sendMessage("§e/eventpvp glow <refresh|remove|test> §7- Gérer glow");
        sender.sendMessage("§e/eventpvp stats §7- Statistiques système");
        sender.sendMessage("§e/eventpvp debug <...> §7- Commandes de debug");
        sender.sendMessage("§6§l═══════════════════════");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "info", "start", "stop", "reset", 
                "assign", "victory", "balance", "glow", "stats", "debug"));
        } 
        else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "reset" -> completions.addAll(Arrays.asList("points", "kits", "teams", "glow"));
                case "assign" -> completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
                case "glow" -> completions.addAll(Arrays.asList("refresh", "remove", "test"));
                case "victory" -> completions.addAll(Arrays.asList("25", "50", "75", "100"));
                case "debug" -> completions.addAll(Arrays.asList("glow", "warp", "world", "teams", 
                        "respawn", "forcerespawn", "location"));
            }
        }
        else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "assign" -> completions.addAll(Arrays.asList("rouge", "bleu", "spectator"));
                case "glow" -> {
                    if ("refresh".equals(args[1]) || "remove".equals(args[1]) || "test".equals(args[1])) {
                        completions.addAll(Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .collect(Collectors.toList()));
                    }
                }
                case "debug" -> {
                    switch (args[1].toLowerCase()) {
                        case "glow", "warp", "world", "respawn", "forcerespawn", "location" -> 
                            completions.addAll(Bukkit.getOnlinePlayers().stream()
                                    .map(Player::getName)
                                    .collect(Collectors.toList()));
                    }
                }
            }
        }
        else if (args.length == 4) {
            switch (args[0].toLowerCase()) {
                case "debug" -> {
                    switch (args[1].toLowerCase()) {
                        case "glow" -> completions.addAll(Arrays.asList("red", "blue", "green", 
                                "yellow", "orange", "purple", "pink", "gray", "white", "black"));
                        case "warp" -> completions.addAll(Arrays.asList("eventred", "eventblue", 
                                "eventspectator", "spawn"));
                    }
                }
            }
        }
        
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}