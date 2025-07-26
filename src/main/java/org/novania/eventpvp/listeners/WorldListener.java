// ===== WorldListener.java =====
package org.novania.eventpvp.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.managers.TeamManager;

public class WorldListener implements Listener {
    
    private final EventPVP plugin;
    private final TeamManager teamManager;
    
    public WorldListener(EventPVP plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldName = plugin.getConfigManager().getWorldName();
        
        // Le joueur entre dans le monde event
        if (player.getWorld().getName().equals(worldName)) {
            teamManager.handlePlayerEnterEventWorld(player);
        }
        // Le joueur quitte le monde event
        else if (event.getFrom().getName().equals(worldName)) {
            teamManager.handlePlayerLeaveEventWorld(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // Intercepter les commandes de warp pour les warps d'event
        if (command.startsWith("/warp ")) {
            String[] args = command.split(" ");
            if (args.length >= 2) {
                String warpName = args[1];
                
                // Vérifier l'accès au warp d'event
                if (!teamManager.validateWarpAccess(player, warpName)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}