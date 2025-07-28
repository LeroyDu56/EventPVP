// ===== AssassinKit.java - CORRECTION POTIONS =====
package org.novania.eventpvp.kits.types;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.novania.eventpvp.enums.KitType;
import org.novania.eventpvp.kits.Kit;
import org.novania.eventpvp.utils.ItemBuilder;
import org.novania.eventpvp.utils.PotionUtils;

public class AssassinKit extends Kit {
    
    public AssassinKit() {
        super(
            "assassin",
            KitType.COMBAT,
            Material.DIAMOND_SWORD,
            "§5🗡️ Kit ASSASSIN",
            List.of(
                "§7Rapide et mortel",
                "§7pour les attaques éclair",
                "§e• Épée tranchante",
                "§e• Vitesse maximale",
                "§e• Invisibilité temporaire",
                "§e• Vision nocturne"
            ),
            -1, // Illimité
            true // Reset à la mort
        );
    }
    
    @Override
    public void giveToPlayer(Player player) {
        clearPlayerInventory(player);
        
        // Armes d'assassin
        ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD)
                .setDisplayName("§5🗡️ Lame de l'Ombre")
                .addEnchantment(Enchantment.SHARPNESS, 4)
                .addEnchantment(Enchantment.FIRE_ASPECT, 1)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .build();
        
        ItemStack bow = new ItemBuilder(Material.BOW)
                .setDisplayName("§5🏹 Arc Silencieux")
                .addEnchantment(Enchantment.POWER, 2)
                .addEnchantment(Enchantment.PUNCH, 2)
                .build();
        
        // Armure légère mais efficace
        ItemStack helmet = new ItemBuilder(Material.LEATHER_HELMET)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack chestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack leggings = new ItemBuilder(Material.LEATHER_LEGGINGS)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack boots = new ItemBuilder(Material.LEATHER_BOOTS)
                .addEnchantment(Enchantment.FEATHER_FALLING, 4)
                .addEnchantment(Enchantment.DEPTH_STRIDER, 3)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        // Outils d'assassin - CORRECTION: Vraies potions
        ItemStack arrows = new ItemStack(Material.ARROW, 32);
        ItemStack food = new ItemStack(Material.COOKED_SALMON, 64);
        ItemStack speedPotions = PotionUtils.EventPotions.strongSpeedPotion(2); // 2 potions de vitesse II
        ItemStack invisibilityPotions = PotionUtils.EventPotions.invisibilityPotion(1); // 1 potion d'invisibilité
        ItemStack nightVisionPotions = PotionUtils.EventPotions.nightVisionPotion(1); // 1 potion de vision nocturne
        ItemStack enderPearls = new ItemStack(Material.ENDER_PEARL, 3); // Pour l'évasion
        
        // Équiper le joueur
        setArmor(player, helmet, chestplate, leggings, boots);
        giveItems(player, sword, bow, arrows, food, speedPotions, invisibilityPotions, nightVisionPotions, enderPearls);
    }
    
    @Override
    public boolean canUse(Player player) {
        return true; // Toujours disponible
    }
}