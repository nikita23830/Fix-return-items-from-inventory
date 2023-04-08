package com.nikita23830.mixinfixmods;

import com.nikita23830.scaddons.StreamCraftAddons;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class InventoryUtils {

    public static ItemStack addToInventory(EntityPlayer player, ItemStack in) {
        InventoryPlayer ip = player.inventory;
        for (int i = 0; i < player.inventory.mainInventory.length; ++i) {
            ItemStack s = ip.getStackInSlot(i);
            if (in == null)
                return null;
            if (s == null) {
                ip.setInventorySlotContents(i, in.copy());
                return null;
            } else if (s.getItem() == in.getItem() && s.getItemDamage() == in.getItemDamage() && ItemStack.areItemStackTagsEqual(in, s)) {
                if (s.stackSize == s.getMaxStackSize())
                    continue;
                if ((s.stackSize + in.stackSize) <= s.getMaxStackSize()) {
                    s.stackSize += in.stackSize;
                    ip.setInventorySlotContents(i, s);
                    return null;
                } else {
                    int can = s.getMaxStackSize() - s.stackSize;
                    s.stackSize = s.getMaxStackSize();
                    ip.setInventorySlotContents(i, s);
                    in.stackSize -= can;
                }
            }
        }
        return in;
    }

    public static void syncInventory(EntityPlayer player) {
//
        StreamCraftAddons.syncInventory(player.getUniqueID());
    }
}
