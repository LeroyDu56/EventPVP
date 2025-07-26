// ===== ItemBuilder.java - Corrections =====
package org.novania.eventpvp.utils;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    
    private final ItemStack item;
    private final ItemMeta meta;
    
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }
    
    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }
    
    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }
    
    public ItemBuilder setDisplayName(String name) {
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
        }
        return this;
    }
    
    public ItemBuilder setLore(String... lore) {
        if (meta != null) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line.replace("&", "§"));
            }
            meta.setLore(loreList);
        }
        return this;
    }
    
    public ItemBuilder setLore(List<String> lore) {
        if (meta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(line.replace("&", "§"));
            }
            meta.setLore(coloredLore);
        }
        return this;
    }
    
    public ItemBuilder addLore(String... lines) {
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            
            for (String line : lines) {
                lore.add(line.replace("&", "§"));
            }
            meta.setLore(lore);
        }
        return this;
    }
    
    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }
    
    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }
    
    public ItemBuilder addUnsafeEnchantment(Enchantment enchantment, int level) {
        item.addUnsafeEnchantment(enchantment, level);
        return this;
    }
    
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }
    
    public ItemBuilder addItemFlags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }
    
    public ItemBuilder hideAllFlags() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }
    
    public ItemBuilder setGlowing(boolean glowing) {
        if (glowing) {
            // Utiliser un enchantement qui existe vraiment
            addUnsafeEnchantment(Enchantment.FORTUNE, 1);
            hideAllFlags();
        }
        return this;
    }
    
    public ItemBuilder setSkullOwner(String owner) {
        if (meta instanceof SkullMeta skullMeta) {
            // Utiliser la méthode moderne
            OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(owner);
            skullMeta.setOwningPlayer(offlinePlayer);
        }
        return this;
    }
    
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
    
    // Méthodes statiques utiles
    public static ItemStack createBarrier(String name) {
        return new ItemBuilder(Material.BARRIER)
                .setDisplayName("&c" + name)
                .build();
    }
    
    public static ItemStack createGlass(Material glassType, String name, String... lore) {
        return new ItemBuilder(glassType)
                .setDisplayName(name)
                .setLore(lore)
                .build();
    }
    
    public static ItemStack createNavigationArrow(String name, String action) {
        return new ItemBuilder(Material.ARROW)
                .setDisplayName("&e" + name)
                .addLore("&7Action: " + action)
                .build();
    }
}