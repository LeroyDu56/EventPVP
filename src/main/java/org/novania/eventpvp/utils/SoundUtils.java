// ===== SoundUtils.java =====
package org.novania.eventpvp.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SoundUtils {
    
    public static void playSound(Player player, Sound sound) {
        playSound(player, sound, 1.0f, 1.0f);
    }
    
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        if (player != null && player.isOnline()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
    
    public static void playSoundToAll(Sound sound) {
        playSoundToAll(sound, 1.0f, 1.0f);
    }
    
    public static void playSoundToAll(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playSound(player, sound, volume, pitch);
        }
    }
    
    public static void playSoundToPlayers(Collection<Player> players, Sound sound) {
        playSoundToPlayers(players, sound, 1.0f, 1.0f);
    }
    
    public static void playSoundToPlayers(Collection<Player> players, Sound sound, float volume, float pitch) {
        for (Player player : players) {
            playSound(player, sound, volume, pitch);
        }
    }
    
    public static void playSoundAtLocation(Location location, Sound sound) {
        playSoundAtLocation(location, sound, 1.0f, 1.0f);
    }
    
    public static void playSoundAtLocation(Location location, Sound sound, float volume, float pitch) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }
    
    // Sons prédéfinis pour EventPVP
    public static class EventSounds {
        public static void playKillSound(Player player) {
            playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        }
        
        public static void playDeathSound(Player player) {
            playSound(player, Sound.ENTITY_PLAYER_DEATH, 0.8f, 0.8f);
        }
        
        public static void playVictorySound(Player player) {
            playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        
        public static void playKitSound(Player player) {
            playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        }
        
        public static void playClickSound(Player player) {
            playSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
        
        public static void playErrorSound(Player player) {
            playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
        
        public static void playSuccessSound(Player player) {
            playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        }
        
        public static void playKillstreakSound(Player player, int killstreak) {
            if (killstreak >= 10) {
                playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            } else if (killstreak >= 7) {
                playSound(player, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.2f);
            } else if (killstreak >= 5) {
                playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.5f);
            } else if (killstreak >= 3) {
                playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);
            }
        }
        
        public static void playTeamVictory(Sound sound) {
            playSoundToAll(sound, 1.0f, 1.0f);
            
            // Son de feu d'artifice pour tous
            Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("EventPVP"), 
                () -> playSoundToAll(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f), 
                10L
            );
        }
    }
    
    // Utilitaires pour les sons configurables
    public static Sound getSoundSafely(String soundName) {
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Sound.UI_BUTTON_CLICK; // Son par défaut
        }
    }
    
    public static void playConfigurableSound(Player player, String soundName, float volume, float pitch) {
        Sound sound = getSoundSafely(soundName);
        playSound(player, sound, volume, pitch);
    }
}