// ===== BuildKit.java =====
package org.novania.eventpvp.kits.types;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.novania.eventpvp.EventPVP;
import org.novania.eventpvp.enums.KitType;
import org.novania.eventpvp.kits.Kit;
import org.novania.eventpvp.utils.ItemBuilder;

import java.util.List;

public class BuildKit extends Kit {
    
    public BuildKit() {
        super(
            "build",
            KitType.BUILD,
            Material.STONE_BRICKS,
            "§6🏗️ Kit BUILD",
            List.of(
                "§7Kit de construction pour",
                "§7fortifier votre base",
                "§e• Matériaux variés",
                "§e• Outils efficaces",
                "§e• Torches d'éclairage"
            ),
            1, // Une seule fois par session
            false // Pas de reset à la mort
        );
    }
    
    @Override
    public void giveToPlayer(Player player) {
        // Ne pas vider l'inventaire pour le kit build
        
        // Outils de construction
        ItemStack pickaxe = new ItemBuilder(Material.IRON_PICKAXE)
                .setDisplayName("§6⛏️ Pioche de Construction")
                .addEnchantment(Enchantment.EFFICIENCY, 3)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .setUnbreakable(true)
                .build();
        
        ItemStack axe = new ItemBuilder(Material.IRON_AXE)
                .setDisplayName("§6🪓 Hache de Construction")
                .addEnchantment(Enchantment.EFFICIENCY, 3)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .setUnbreakable(true)
                .build();
        
        ItemStack shovel = new ItemBuilder(Material.IRON_SHOVEL)
                .setDisplayName("§6🥄 Pelle de Construction")
                .addEnchantment(Enchantment.EFFICIENCY, 3)
                .addEnchantment(Enchantment.UNBREAKING, 3)
                .setUnbreakable(true)
                .build();
        
        // Matériaux de construction
        ItemStack stoneBricks = new ItemStack(Material.STONE_BRICKS, 64);
        ItemStack cobblestone = new ItemStack(Material.COBBLESTONE, 64);
        ItemStack wood = new ItemStack(Material.OAK_PLANKS, 64);
        ItemStack glass = new ItemStack(Material.GLASS, 32);
        
        // Éclairage et décoration
        ItemStack torches = new ItemStack(Material.TORCH, 32);
        ItemStack ladders = new ItemStack(Material.LADDER, 16);
        ItemStack doors = new ItemStack(Material.OAK_DOOR, 4);
        
        // Donner les items
        giveItems(player, pickaxe, axe, shovel, stoneBricks, cobblestone, wood, glass, torches, ladders, doors);
        
        // Marquer comme utilisé
        EventPVP.getInstance().getDatabaseManager().setBuildKitUsed(player.getUniqueId().toString(), true);
    }
    
    @Override
    public boolean canUse(Player player) {
        // Vérifier si le kit build a déjà été utilisé cette session
        return !EventPVP.getInstance().getDatabaseManager().hasBuildKitUsed(player.getUniqueId().toString());
    }
}