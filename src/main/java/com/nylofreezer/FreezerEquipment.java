package com.nylofreezer;

import java.util.Set;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.ItemID;

final class FreezerEquipment
{
    private static final Set<Integer> MAGE_HELMS = Set.of(
        ItemID.GAME_PEST_MAGE_HELM, ItemID.GAME_PEST_MAGE_HELM_TROUVER,
        ItemID.LEAGUE_3_VOID_MAGE_HELM, ItemID.LEAGUE_3_VOID_MAGE_HELM_TROUVER);
    private static final Set<Integer> TOPS = Set.of(
        ItemID.PEST_VOID_KNIGHT_TOP, ItemID.PEST_VOID_KNIGHT_TOP_TROUVER,
        ItemID.ELITE_VOID_KNIGHT_TOP, ItemID.ELITE_VOID_KNIGHT_TOP_TROUVER,
        ItemID.LEAGUE_3_VOID_KNIGHT_TOP, ItemID.LEAGUE_3_VOID_KNIGHT_TOP_TROUVER,
        ItemID.LEAGUE_3_VOID_KNIGHT_TOP_ELITE, ItemID.LEAGUE_3_VOID_KNIGHT_TOP_ELITE_TROUVER);
    private static final Set<Integer> ROBES = Set.of(
        ItemID.PEST_VOID_KNIGHT_ROBES, ItemID.PEST_VOID_KNIGHT_ROBES_TROUVER,
        ItemID.ELITE_VOID_KNIGHT_ROBES, ItemID.ELITE_VOID_KNIGHT_ROBES_TROUVER,
        ItemID.LEAGUE_3_VOID_KNIGHT_ROBES, ItemID.LEAGUE_3_VOID_KNIGHT_ROBES_TROUVER,
        ItemID.LEAGUE_3_VOID_KNIGHT_ROBES_ELITE, ItemID.LEAGUE_3_VOID_KNIGHT_ROBES_ELITE_TROUVER);
    private static final Set<Integer> GLOVES = Set.of(
        ItemID.PEST_VOID_KNIGHT_GLOVES, ItemID.PEST_VOID_KNIGHT_GLOVES_TROUVER,
        ItemID.LEAGUE_3_VOID_KNIGHT_GLOVES, ItemID.LEAGUE_3_VOID_KNIGHT_GLOVES_TROUVER);
    private static final Set<Integer> ICE_SCEPTRES = Set.of(
        ItemID.ANCIENT_SCEPTRE_ICE, ItemID.ANCIENT_SCEPTRE_ICE_TROUVER);

    private FreezerEquipment()
    {
    }

    static boolean hasVoidMage(ItemContainer equipment)
    {
        return MAGE_HELMS.contains(itemId(equipment, EquipmentInventorySlot.HEAD))
            && TOPS.contains(itemId(equipment, EquipmentInventorySlot.BODY))
            && ROBES.contains(itemId(equipment, EquipmentInventorySlot.LEGS))
            && GLOVES.contains(itemId(equipment, EquipmentInventorySlot.GLOVES));
    }

    static boolean hasIceSceptre(ItemContainer equipment)
    {
        return ICE_SCEPTRES.contains(itemId(equipment, EquipmentInventorySlot.WEAPON));
    }

    private static int itemId(ItemContainer equipment, EquipmentInventorySlot slot)
    {
        Item item = equipment.getItem(slot.getSlotIdx());
        return item == null ? -1 : item.getId();
    }
}
