// ===== TankKit.java - CORRECTION POTIONS =====
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

public class TankKit extends Kit {
    
    public TankKit() {
        super(
            "tank",
            KitType.COMBAT,
            Material.DIAMOND_CHESTPLATE,
            "§b🛡️ Kit TANK",
            List.of(
                "§7Résistant et défensif",
                "§7pour tenir les positions",
                "§e• Armure renforcée",
                "§e• Bouclier résistant",
                "§e• Régénération",
                "§e• Résistance au feu"
            ),
            -1, // Illimité
            true // Reset à la mort
        );
    }
    
    @Override
    public void giveToPlayer(Player player) {
        clearPlayerInventory(player);
        
        // Armes défensives
        ItemStack sword = new ItemBuilder(Material.IRON_SWORD)
                .setDisplayName("§b⚔️ Épée de Gardien")
                .addEnchantment(Enchantment.KNOCKBACK, 1)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .build();
        
        ItemStack shield = new ItemBuilder(Material.SHIELD)
                .setDisplayName("§b🛡️ Bouclier Renforcé")
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .build();
        
        // Armure lourde
        ItemStack helmet = new ItemBuilder(Material.DIAMOND_HELMET)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .addEnchantment(Enchantment.RESPIRATION, 2)
                .build();
        
        ItemStack chestplate = new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .build();
        
        ItemStack leggings = new ItemBuilder(Material.DIAMOND_LEGGINGS)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .build();
        
        ItemStack boots = new ItemBuilder(Material.DIAMOND_BOOTS)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.FEATHER_FALLING, 4)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .build();
        
        // Consommables de survie - CORRECTION: Vraies potions
        ItemStack food = new ItemStack(Material.GOLDEN_APPLE, 8);
        ItemStack food2 = new ItemStack(Material.COOKED_BEEF, 64);
        ItemStack regenPotions = PotionUtils.EventPotions.regenerationPotion(3); // 3 potions de régénération
        ItemStack fireResPotions = PotionUtils.EventPotions.fireResistancePotion(2); // 2 potions de résistance au feu
        ItemStack blocks = new ItemStack(Material.COBBLESTONE, 32); // Pour se protéger
        
        // Équiper le joueur
        setArmor(player, helmet, chestplate, leggings, boots);
        giveItems(player, sword, shield, food, food2, regenPotions, fireResPotions, blocks);
        
        // Placer le bouclier dans la main gauche
        player.getInventory().setItemInOffHand(shield);
    }
    
    @Override
    public boolean canUse(Player player) {
        return true; // Toujours disponible
    }
}