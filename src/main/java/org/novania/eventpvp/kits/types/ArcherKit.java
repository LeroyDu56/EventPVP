// ===== ArcherKit.java - CORRECTION POTIONS =====
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

public class ArcherKit extends Kit {
    
    public ArcherKit() {
        super(
            "archer",
            KitType.COMBAT,
            Material.BOW,
            "§a🏹 Kit ARCHER",
            List.of(
                "§7Spécialisé dans le",
                "§7combat à distance",
                "§e• Arc puissant",
                "§e• Flèches infinies",
                "§e• Armure légère",
                "§e• Vitesse accrue"
            ),
            -1, // Illimité
            true // Reset à la mort
        );
    }
    
    @Override
    public void giveToPlayer(Player player) {
        clearPlayerInventory(player);
        
        // Arc principal
        ItemStack bow = new ItemBuilder(Material.BOW)
                .setDisplayName("§a🏹 Arc de Précision")
                .addEnchantment(Enchantment.POWER, 3)
                .addEnchantment(Enchantment.PUNCH, 1)
                .addEnchantment(Enchantment.INFINITY, 1)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .build();
        
        // Arme de secours
        ItemStack sword = new ItemBuilder(Material.STONE_SWORD)
                .setDisplayName("§7⚔️ Épée de Secours")
                .addEnchantment(Enchantment.SHARPNESS, 1)
                .build();
        
        // Armure légère (cuir)
        ItemStack helmet = new ItemBuilder(Material.LEATHER_HELMET)
                .addEnchantment(Enchantment.PROTECTION, 2)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack chestplate = new ItemBuilder(Material.CHAINMAIL_CHESTPLATE)
                .addEnchantment(Enchantment.PROTECTION, 2)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack leggings = new ItemBuilder(Material.LEATHER_LEGGINGS)
                .addEnchantment(Enchantment.PROTECTION, 2)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack boots = new ItemBuilder(Material.LEATHER_BOOTS)
                .addEnchantment(Enchantment.PROTECTION, 2)
                .addEnchantment(Enchantment.FEATHER_FALLING, 3)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        // Consommables - CORRECTION: Vraies potions
        ItemStack arrows = new ItemStack(Material.ARROW, 1); // Une seule flèche (Infinity)
        ItemStack food = new ItemStack(Material.COOKED_CHICKEN, 64);
        ItemStack speedPotions = PotionUtils.EventPotions.speedPotion(2); // 2 potions de vitesse
        
        // Équiper le joueur
        setArmor(player, helmet, chestplate, leggings, boots);
        giveItems(player, bow, sword, arrows, food, speedPotions);
    }
    
    @Override
    public boolean canUse(Player player) {
        return true; // Toujours disponible
    }
}