package dev.cw.dungeon;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Таблицы лута. У каждого типа сундука своя таблица. Формат: e(предмет, мин, макс, вес). */
public final class Loot {
    public enum Table {
        O_CRYPT, O_CRYPT_RARE, O_TOWER, O_ARMORY, O_CAMP,
        N_ARMORY, N_TREASURE, N_ALCHEMY,
        E_TREASURE, E_ENCHANT, E_RELICS
    }

    private record E(Material m, int min, int max, int w, Enchantment ench, int lvl, boolean book) {}

    private static E e(Material m, int min, int max, int w) { return new E(m, min, max, w, null, 0, false); }
    private static E g(Material m, Enchantment en, int lvl, int w) { return new E(m, 1, 1, w, en, lvl, false); }
    private static E b(Enchantment en, int lvl, int w) { return new E(Material.ENCHANTED_BOOK, 1, 1, w, en, lvl, true); }

    private static final Map<Table, List<E>> T = new EnumMap<>(Table.class);

    static {
        T.put(Table.O_CRYPT, List.of(
                e(Material.BONE, 4, 12, 10), e(Material.ROTTEN_FLESH, 3, 10, 9), e(Material.ARROW, 4, 16, 8),
                e(Material.COAL, 3, 10, 8), e(Material.IRON_INGOT, 1, 5, 7), e(Material.GOLD_INGOT, 1, 4, 6),
                e(Material.BREAD, 2, 5, 6), e(Material.STRING, 2, 8, 6), e(Material.GUNPOWDER, 2, 6, 6),
                e(Material.SPIDER_EYE, 1, 3, 4), e(Material.BONE_MEAL, 3, 9, 5), e(Material.EXPERIENCE_BOTTLE, 2, 5, 4),
                e(Material.GOLDEN_APPLE, 1, 1, 2), e(Material.NAME_TAG, 1, 1, 2), b(Enchantment.UNBREAKING, 2, 2)));

        T.put(Table.O_CRYPT_RARE, List.of(
                e(Material.DIAMOND, 1, 3, 7), e(Material.EMERALD, 2, 6, 7), e(Material.LAPIS_LAZULI, 5, 15, 7),
                e(Material.GOLD_BLOCK, 1, 1, 3), e(Material.DIAMOND_HORSE_ARMOR, 1, 1, 2), e(Material.MUSIC_DISC_OTHERSIDE, 1, 1, 1),
                e(Material.TOTEM_OF_UNDYING, 1, 1, 1), e(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 1),
                b(Enchantment.MENDING, 1, 2), b(Enchantment.PROTECTION, 3, 2), g(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 3, 2),
                g(Material.DIAMOND_PICKAXE, Enchantment.EFFICIENCY, 3, 2)));

        T.put(Table.O_TOWER, List.of(
                e(Material.ARROW, 8, 24, 10), e(Material.TORCH, 8, 24, 8), e(Material.COOKED_BEEF, 3, 8, 8),
                e(Material.IRON_INGOT, 2, 6, 7), e(Material.LADDER, 4, 12, 5), e(Material.COBWEB, 2, 6, 4),
                e(Material.SPYGLASS, 1, 1, 3), e(Material.COMPASS, 1, 1, 3), e(Material.CLOCK, 1, 1, 3),
                e(Material.ENDER_PEARL, 1, 2, 2), e(Material.GOLDEN_CARROT, 2, 6, 4),
                g(Material.BOW, Enchantment.POWER, 2, 3), g(Material.CROSSBOW, Enchantment.QUICK_CHARGE, 2, 2)));

        T.put(Table.O_ARMORY, List.of(
                g(Material.IRON_HELMET, Enchantment.PROTECTION, 2, 4), g(Material.IRON_CHESTPLATE, Enchantment.PROTECTION, 2, 4),
                g(Material.IRON_LEGGINGS, Enchantment.PROTECTION, 2, 4), g(Material.IRON_BOOTS, Enchantment.FEATHER_FALLING, 3, 4),
                g(Material.IRON_SWORD, Enchantment.SHARPNESS, 2, 4), g(Material.IRON_AXE, Enchantment.EFFICIENCY, 3, 3),
                e(Material.SHIELD, 1, 1, 4), e(Material.ARROW, 8, 20, 6), e(Material.IRON_INGOT, 2, 6, 5),
                g(Material.DIAMOND_BOOTS, Enchantment.UNBREAKING, 2, 1), g(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 2, 1)));

        T.put(Table.O_CAMP, List.of(
                e(Material.BREAD, 2, 6, 9), e(Material.APPLE, 2, 5, 8), e(Material.BAKED_POTATO, 2, 6, 8),
                e(Material.COOKED_SALMON, 2, 5, 7), e(Material.CARROT, 3, 8, 7), e(Material.WHEAT_SEEDS, 4, 12, 6),
                e(Material.TORCH, 6, 16, 6), e(Material.LEATHER, 2, 6, 5), e(Material.FLINT, 2, 6, 5),
                e(Material.BUCKET, 1, 1, 3), e(Material.LEAD, 1, 2, 3), e(Material.NAME_TAG, 1, 1, 2),
                g(Material.FISHING_ROD, Enchantment.LURE, 2, 2), e(Material.EMERALD, 1, 3, 3)));

        T.put(Table.N_ARMORY, List.of(
                g(Material.GOLDEN_SWORD, Enchantment.FIRE_ASPECT, 2, 4), g(Material.GOLDEN_AXE, Enchantment.SHARPNESS, 3, 3),
                g(Material.DIAMOND_BOOTS, Enchantment.FIRE_PROTECTION, 3, 2), g(Material.DIAMOND_HELMET, Enchantment.FIRE_PROTECTION, 3, 2),
                g(Material.CROSSBOW, Enchantment.PIERCING, 3, 3), g(Material.IRON_CHESTPLATE, Enchantment.PROTECTION, 3, 3),
                e(Material.SHIELD, 1, 1, 4), e(Material.ARROW, 8, 24, 6), e(Material.GOLD_INGOT, 3, 8, 6),
                b(Enchantment.FIRE_PROTECTION, 3, 3), e(Material.NETHERITE_SCRAP, 1, 1, 1)));

        T.put(Table.N_TREASURE, List.of(
                e(Material.GOLD_INGOT, 3, 9, 8), e(Material.GOLD_BLOCK, 1, 2, 4), e(Material.QUARTZ, 4, 12, 7),
                e(Material.ANCIENT_DEBRIS, 1, 2, 3), e(Material.NETHERITE_SCRAP, 1, 2, 2), e(Material.DIAMOND, 1, 3, 3),
                e(Material.EMERALD, 2, 5, 4), e(Material.CRYING_OBSIDIAN, 2, 6, 5), e(Material.OBSIDIAN, 2, 6, 5),
                e(Material.GOLDEN_APPLE, 1, 1, 3), e(Material.MUSIC_DISC_PIGSTEP, 1, 1, 1), e(Material.WITHER_SKELETON_SKULL, 1, 1, 1),
                e(Material.NETHERITE_INGOT, 1, 1, 1), b(Enchantment.MENDING, 1, 1)));

        T.put(Table.N_ALCHEMY, List.of(
                e(Material.NETHER_WART, 4, 12, 9), e(Material.BLAZE_ROD, 1, 4, 6), e(Material.BLAZE_POWDER, 2, 6, 6),
                e(Material.MAGMA_CREAM, 1, 4, 6), e(Material.GHAST_TEAR, 1, 2, 3), e(Material.GLOWSTONE_DUST, 4, 12, 6),
                e(Material.FIRE_CHARGE, 2, 5, 5), e(Material.SPIDER_EYE, 2, 5, 4), e(Material.FERMENTED_SPIDER_EYE, 1, 3, 3),
                e(Material.GLISTERING_MELON_SLICE, 2, 6, 4), e(Material.GOLDEN_CARROT, 2, 5, 4), e(Material.RABBIT_FOOT, 1, 2, 3),
                e(Material.SUGAR, 3, 8, 3), e(Material.BREWING_STAND, 1, 1, 1)));

        T.put(Table.E_TREASURE, List.of(
                e(Material.ENDER_PEARL, 1, 4, 9), e(Material.CHORUS_FRUIT, 3, 9, 8), e(Material.POPPED_CHORUS_FRUIT, 2, 6, 6),
                e(Material.DIAMOND, 1, 4, 7), e(Material.EMERALD, 2, 6, 6), e(Material.GOLD_INGOT, 2, 7, 6),
                e(Material.IRON_INGOT, 3, 8, 6), e(Material.ENDER_EYE, 1, 3, 5), e(Material.SHULKER_SHELL, 1, 2, 3),
                e(Material.DIAMOND_BLOCK, 1, 1, 1), e(Material.ENDER_CHEST, 1, 1, 1), e(Material.NETHERITE_INGOT, 1, 1, 1)));

        T.put(Table.E_ENCHANT, List.of(
                e(Material.LAPIS_LAZULI, 8, 24, 8), e(Material.EXPERIENCE_BOTTLE, 4, 10, 8),
                b(Enchantment.MENDING, 1, 3), b(Enchantment.UNBREAKING, 3, 5), b(Enchantment.PROTECTION, 4, 3),
                b(Enchantment.SHARPNESS, 5, 3), b(Enchantment.EFFICIENCY, 5, 3), b(Enchantment.FORTUNE, 3, 3),
                b(Enchantment.LOOTING, 3, 3), b(Enchantment.SILK_TOUCH, 1, 3), b(Enchantment.FEATHER_FALLING, 4, 3),
                b(Enchantment.POWER, 5, 3), g(Material.DIAMOND_PICKAXE, Enchantment.FORTUNE, 3, 1)));

        T.put(Table.E_RELICS, List.of(
                e(Material.DIAMOND, 3, 8, 7), e(Material.GOLDEN_APPLE, 1, 3, 5), e(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 2),
                e(Material.TOTEM_OF_UNDYING, 1, 1, 3), e(Material.ELYTRA, 1, 1, 3), e(Material.SHULKER_BOX, 1, 1, 2),
                e(Material.END_CRYSTAL, 1, 2, 2), e(Material.DRAGON_HEAD, 1, 1, 1), e(Material.NETHERITE_INGOT, 1, 2, 2),
                e(Material.MUSIC_DISC_OTHERSIDE, 1, 1, 1),
                g(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 4, 2), g(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 5, 2)));
    }

    private Loot() {}

    public static void fill(Inventory inv, Table table, Random r) {
        List<E> list = T.get(table);
        int total = 0;
        for (E x : list) total += x.w();
        int rolls = 4 + r.nextInt(5);
        for (int i = 0; i < rolls; i++) {
            int pick = r.nextInt(total);
            E chosen = list.get(0);
            for (E x : list) {
                pick -= x.w();
                if (pick < 0) { chosen = x; break; }
            }
            ItemStack is = new ItemStack(chosen.m(), chosen.min() + r.nextInt(chosen.max() - chosen.min() + 1));
            if (chosen.book()) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) is.getItemMeta();
                meta.addStoredEnchant(chosen.ench(), chosen.lvl(), true);
                is.setItemMeta(meta);
            } else if (chosen.ench() != null) {
                is.addUnsafeEnchantment(chosen.ench(), chosen.lvl());
            }
            int slot = r.nextInt(inv.getSize());
            if (inv.getItem(slot) == null) inv.setItem(slot, is);
        }
    }
}
