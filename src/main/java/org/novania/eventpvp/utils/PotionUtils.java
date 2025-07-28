// ===== PotionUtils.java - CORRECTION POTIONS MINECRAFT 1.21 =====
package org.novania.eventpvp.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionUtils {
    
    /**
     * Crée une potion avec un effet spécifique
     */
    public static ItemStack createPotion(PotionEffectType effectType, int duration, int amplifier, int amount) {
        ItemStack potion = new ItemStack(Material.POTION, amount);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        
        if (meta != null) {
            // Ajouter l'effet personnalisé
            meta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
            
            // Définir la couleur de la potion selon l'effet
            meta.setColor(getEffectColor(effectType));
            
            // Nom de la potion
            meta.setDisplayName("§r" + getEffectDisplayName(effectType));
            
            potion.setItemMeta(meta);
        }
        
        return potion;
    }
    
    /**
     * Crée une potion de base avec PotionType (pour les potions standards)
     * CORRECTION: Utilise la nouvelle API 1.21
     */
    public static ItemStack createBasePotion(PotionType potionType, int amount) {
        ItemStack potion = new ItemStack(Material.POTION, amount);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        
        if (meta != null) {
            // CORRECTION: Nouvelle méthode pour 1.21
            meta.setBasePotionType(potionType);
            potion.setItemMeta(meta);
        }
        
        return potion;
    }
    
    /**
     * Potions prédéfinies pour EventPVP
     */
    public static class EventPotions {
        
        // Potions de soin
        public static ItemStack healingPotion(int amount) {
            return createPotion(PotionEffectType.INSTANT_HEALTH, 1, 1, amount);
        }
        
        public static ItemStack strongHealingPotion(int amount) {
            return createPotion(PotionEffectType.INSTANT_HEALTH, 1, 2, amount);
        }
        
        // Potions de vitesse
        public static ItemStack speedPotion(int amount) {
            return createPotion(PotionEffectType.SPEED, 3600, 1, amount); // 3 minutes
        }
        
        public static ItemStack strongSpeedPotion(int amount) {
            return createPotion(PotionEffectType.SPEED, 1800, 2, amount); // 1.5 minutes, niveau 2
        }
        
        // Potions de force - CORRECTION: STRENGTH au lieu de INCREASE_DAMAGE
        public static ItemStack strengthPotion(int amount) {
            return createPotion(PotionEffectType.STRENGTH, 3600, 0, amount); // 3 minutes
        }
        
        // Potions de régénération
        public static ItemStack regenerationPotion(int amount) {
            return createPotion(PotionEffectType.REGENERATION, 900, 1, amount); // 45 secondes
        }
        
        public static ItemStack strongRegenerationPotion(int amount) {
            return createPotion(PotionEffectType.REGENERATION, 450, 2, amount); // 22.5 secondes, niveau 2
        }
        
        // Potions d'invisibilité
        public static ItemStack invisibilityPotion(int amount) {
            return createPotion(PotionEffectType.INVISIBILITY, 3600, 0, amount); // 3 minutes
        }
        
        // Potions de résistance au feu
        public static ItemStack fireResistancePotion(int amount) {
            return createPotion(PotionEffectType.FIRE_RESISTANCE, 3600, 0, amount); // 3 minutes
        }
        
        // Potions de vision nocturne
        public static ItemStack nightVisionPotion(int amount) {
            return createPotion(PotionEffectType.NIGHT_VISION, 3600, 0, amount); // 3 minutes
        }
        
        // Potions de saut - CORRECTION: JUMP_BOOST au lieu de JUMP
        public static ItemStack jumpBoostPotion(int amount) {
            return createPotion(PotionEffectType.JUMP_BOOST, 3600, 1, amount); // 3 minutes
        }
        
        // Potions de respiration aquatique
        public static ItemStack waterBreathingPotion(int amount) {
            return createPotion(PotionEffectType.WATER_BREATHING, 3600, 0, amount); // 3 minutes
        }
    }
    
    /**
     * Obtient la couleur de la potion selon l'effet
     */
    private static org.bukkit.Color getEffectColor(PotionEffectType effectType) {
        if (effectType.equals(PotionEffectType.INSTANT_HEALTH)) {
            return org.bukkit.Color.RED;
        } else if (effectType.equals(PotionEffectType.SPEED)) {
            return org.bukkit.Color.AQUA;
        } else if (effectType.equals(PotionEffectType.STRENGTH)) { // CORRECTION: STRENGTH
            return org.bukkit.Color.ORANGE;
        } else if (effectType.equals(PotionEffectType.REGENERATION)) {
            return org.bukkit.Color.fromRGB(255, 192, 203); // CORRECTION: RGB pour rose
        } else if (effectType.equals(PotionEffectType.INVISIBILITY)) {
            return org.bukkit.Color.GRAY;
        } else if (effectType.equals(PotionEffectType.FIRE_RESISTANCE)) {
            return org.bukkit.Color.YELLOW;
        } else if (effectType.equals(PotionEffectType.NIGHT_VISION)) {
            return org.bukkit.Color.BLUE;
        } else if (effectType.equals(PotionEffectType.JUMP_BOOST)) { // CORRECTION: JUMP_BOOST
            return org.bukkit.Color.GREEN;
        } else if (effectType.equals(PotionEffectType.WATER_BREATHING)) {
            return org.bukkit.Color.NAVY;
        }
        
        return org.bukkit.Color.PURPLE; // Couleur par défaut
    }
    
    /**
     * Obtient le nom d'affichage de l'effet
     */
    private static String getEffectDisplayName(PotionEffectType effectType) {
        if (effectType.equals(PotionEffectType.INSTANT_HEALTH)) {
            return "Potion de Soin";
        } else if (effectType.equals(PotionEffectType.SPEED)) {
            return "Potion de Vitesse";
        } else if (effectType.equals(PotionEffectType.STRENGTH)) { // CORRECTION: STRENGTH
            return "Potion de Force";
        } else if (effectType.equals(PotionEffectType.REGENERATION)) {
            return "Potion de Régénération";
        } else if (effectType.equals(PotionEffectType.INVISIBILITY)) {
            return "Potion d'Invisibilité";
        } else if (effectType.equals(PotionEffectType.FIRE_RESISTANCE)) {
            return "Potion de Résistance au Feu";
        } else if (effectType.equals(PotionEffectType.NIGHT_VISION)) {
            return "Potion de Vision Nocturne";
        } else if (effectType.equals(PotionEffectType.JUMP_BOOST)) { // CORRECTION: JUMP_BOOST
            return "Potion de Saut";
        } else if (effectType.equals(PotionEffectType.WATER_BREATHING)) {
            return "Potion de Respiration Aquatique";
        }
        
        return "Potion Mystérieuse";
    }
    
    /**
     * Convertit des secondes en ticks Minecraft (20 ticks = 1 seconde)
     */
    public static int secondsToTicks(int seconds) {
        return seconds * 20;
    }
    
    /**
     * Convertit des minutes en ticks Minecraft
     */
    public static int minutesToTicks(int minutes) {
        return minutes * 60 * 20;
    }
}