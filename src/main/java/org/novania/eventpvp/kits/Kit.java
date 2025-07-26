// ===== Kit.java =====
package org.novania.eventpvp.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.novania.eventpvp.enums.KitType;
import org.novania.eventpvp.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class Kit {
    
    protected final String name;
    protected final KitType type;
    protected final Material displayMaterial;
    protected final String displayName;
    protected final List<String> description;
    protected final int usageLimit;
    protected final boolean resetOnDeath;
    
    public Kit(String name, KitType type, Material displayMaterial, String displayName, 
               List<String> description, int usageLimit, boolean resetOnDeath) {
        this.name = name;
        this.type = type;
        this.displayMaterial = displayMaterial;
        this.displayName = displayName;
        this.description = new ArrayList<>(description);
        this.usageLimit = usageLimit;
        this.resetOnDeath = resetOnDeath;
    }
    
    // Méthode abstraite à implémenter par chaque kit
    public abstract void giveToPlayer(Player player);
    
    // Méthode pour vérifier si le joueur peut utiliser ce kit
    public abstract boolean canUse(Player player);
    
    // Créer l'item de prévisualisation pour la GUI
    public ItemStack createDisplayItem(Player player) {
        ItemBuilder builder = new ItemBuilder(displayMaterial)
                .setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7─────────────────");
        lore.addAll(description);
        lore.add("§7─────────────────");
        
        // Informations d'usage
        if (usageLimit == 1) {
            lore.add("§6Usage: §eUne fois par session");
        } else if (usageLimit == -1) {
            lore.add("§6Usage: §aIllimité");
        } else {
            lore.add("§6Usage: §e" + usageLimit + " fois");
        }
        
        if (resetOnDeath) {
            lore.add("§6Reset: §cÀ la mort");
        } else {
            lore.add("§6Reset: §aPermanent");
        }
        
        // Statut de disponibilité
        if (canUse(player)) {
            lore.add("§a§l✓ DISPONIBLE");
            lore.add("§e§lClic gauche §7pour équiper");
        } else {
            lore.add("§c§l✗ NON DISPONIBLE");
            if (type == KitType.BUILD) {
                lore.add("§c§lDéjà utilisé cette session");
            }
        }
        
        builder.setLore(lore);
        
        // Effet visuel selon la disponibilité
        if (!canUse(player)) {
            builder.setGlowing(false);
        } else {
            builder.setGlowing(true);
        }
        
        return builder.build();
    }
    
    // Getters
    public String getName() { return name; }
    public KitType getType() { return type; }
    public Material getDisplayMaterial() { return displayMaterial; }
    public String getDisplayName() { return displayName; }
    public List<String> getDescription() { return new ArrayList<>(description); }
    public int getUsageLimit() { return usageLimit; }
    public boolean isResetOnDeath() { return resetOnDeath; }
    
    // Méthodes utilitaires pour les kits
    protected void clearPlayerInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
    }
    
    protected void giveItems(Player player, ItemStack... items) {
        for (ItemStack item : items) {
            if (item != null) {
                player.getInventory().addItem(item);
            }
        }
    }
    
    protected void setArmor(Player player, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        if (helmet != null) player.getInventory().setHelmet(helmet);
        if (chestplate != null) player.getInventory().setChestplate(chestplate);
        if (leggings != null) player.getInventory().setLeggings(leggings);
        if (boots != null) player.getInventory().setBoots(boots);
    }
}