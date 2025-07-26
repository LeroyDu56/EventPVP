// ===== PvPKit.java =====
package org.novania.eventpvp.kits.types;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.novania.eventpvp.enums.KitType;
import org.novania.eventpvp.kits.Kit;
import org.novania.eventpvp.utils.ItemBuilder;

import java.util.List;

public class PvPKit extends Kit {
    
    public PvPKit() {
        super(
            "pvp",
            KitType.COMBAT,
            Material.IRON_SWORD,
            "§c⚔️ Kit PVP",
            List.of(
                "§7Kit équilibré pour",
                "§7le combat rapproché",
                "§e• Épée et arc",
                "§e• Armure en fer",
                "§e• Potions de soin"
            ),
            -1, // Illimité
            true // Reset à la mort
        );
    }
    
    @Override
    public void giveToPlayer(Player player) {
        clearPlayerInventory(player);
        
        // Armes principales
        ItemStack sword = new ItemBuilder(Material.IRON_SWORD)
                .setDisplayName("§c⚔️ Épée de Combat")
                .addEnchantment(Enchantment.SHARPNESS, 2)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack bow = new ItemBuilder(Material.BOW)
                .setDisplayName("§6🏹 Arc de Combat")
                .addEnchantment(Enchantment.POWER, 1)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        // Armure
        ItemStack helmet = new ItemBuilder(Material.IRON_HELMET)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack chestplate = new ItemBuilder(Material.IRON_CHESTPLATE)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack leggings = new ItemBuilder(Material.IRON_LEGGINGS)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        ItemStack boots = new ItemBuilder(Material.IRON_BOOTS)
                .addEnchantment(Enchantment.PROTECTION, 1)
                .addEnchantment(Enchantment.UNBREAKING, 2)
                .build();
        
        // Consommables
        ItemStack arrows = new ItemStack(Material.ARROW, 64);
        ItemStack food = new ItemStack(Material.COOKED_BEEF, 16);
        ItemStack healingPotions = new ItemStack(Material.POTION, 3); // TODO: Configurer potion de soin
        
        // Équiper le joueur
        setArmor(player, helmet, chestplate, leggings, boots);
        giveItems(player, sword, bow, arrows, food, healingPotions);
    }
    
    @Override
    public boolean canUse(Player player) {
        return true; // Toujours disponible
    }
}