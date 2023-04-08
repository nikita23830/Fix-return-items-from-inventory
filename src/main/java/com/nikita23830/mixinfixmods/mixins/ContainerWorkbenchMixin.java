package com.nikita23830.mixinfixmods.mixins;

import com.nikita23830.mixinfixmods.IContainerMixin;
import com.nikita23830.mixinfixmods.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerWorkbench.class)
public abstract class ContainerWorkbenchMixin extends Container {

    @Shadow
    private World worldObj;
    @Shadow
    public InventoryCrafting craftMatrix;

    @Inject(method = "onContainerClosed", at = @At("HEAD"), cancellable = true)
    public void onContainerClosedM(EntityPlayer p_75134_1_, CallbackInfo ci) {
        super.onContainerClosed(p_75134_1_);

        if (!this.worldObj.isRemote) {
            boolean add = false;

            for (int i = 0; i < 9; ++i) {
                ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);

                if (itemstack != null) {
                    ItemStack s = InventoryUtils.addToInventory(p_75134_1_, itemstack);
                    if (s == null || s.stackSize != itemstack.stackSize)
                        add = true;
                    if (s != null)
                        p_75134_1_.dropPlayerItemWithRandomChoice(s, false);
                }
            }
            if (add)
                InventoryUtils.syncInventory(p_75134_1_);
        }
    }

    private String getDataItem(ItemStack stack) {
        return stack == null ? "NULL" : Item.itemRegistry.getNameForObject(stack.getItem()) + ":" + stack.getItemDamage() + " " + stack.stackSize + " " + (stack.hasTagCompound() ? stack.getTagCompound().toString() : "null");
    }

    private ItemStack mergeItemStackMS(ItemStack stack, int slotA, int slotB, boolean doit) {
        boolean flag1 = false;
        int k = slotA;

        if (doit) {
            k = slotB - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (stack.isStackable()) {
            while (stack.stackSize > 0 && (!doit && k < slotB || doit && k >= slotA)) {
                slot = (Slot)this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (itemstack1 != null && itemstack1.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
                    int l = itemstack1.stackSize + stack.stackSize;

                    if (l <= stack.getMaxStackSize()) {
                        stack.stackSize = 0;
                        itemstack1.stackSize = l;
                        slot.onSlotChanged();
                        flag1 = true;
                    } else if (itemstack1.stackSize < stack.getMaxStackSize()) {
                        stack.stackSize -= stack.getMaxStackSize() - itemstack1.stackSize;
                        itemstack1.stackSize = stack.getMaxStackSize();
                        slot.onSlotChanged();
                        flag1 = true;
                    }
                }

                if (doit) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        if (stack.stackSize > 0) {
            if (doit) {
                k = slotB - 1;
            }  else {
                k = slotA;
            }

            while (!doit && k < slotB || doit && k >= slotA) {
                slot = (Slot)this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (itemstack1 == null) {
                    slot.putStack(stack.copy());
                    slot.onSlotChanged();
                    stack.stackSize = 0;
                    flag1 = true;
                    break;
                }

                if (doit) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        if (stack.stackSize <= 0)
            stack = null;
        return stack;
    }

    @Inject(method = "transferStackInSlot", at = @At("HEAD"), cancellable = true)
    public void transferStackInSlotM(EntityPlayer p_82846_1_, int p_82846_2_, CallbackInfoReturnable<ItemStack> ci) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(p_82846_2_);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (p_82846_2_ == 0) {
                ItemStack s = mergeItemStackMS(itemstack1, 10, 46, true);
                if (s != null) {
                    slot.putStack(s);
                    slot.onSlotChanged();
                    ci.setReturnValue(null);
                    ci.cancel();
                    return;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (p_82846_2_ >= 10 && p_82846_2_ < 37)
            {
                if (!this.mergeItemStack(itemstack1, 37, 46, false))
                {
                    ci.setReturnValue(null);
                    ci.cancel();
                    return;
                }
            }
            else if (p_82846_2_ >= 37 && p_82846_2_ < 46)
            {
                if (!this.mergeItemStack(itemstack1, 10, 37, false))
                {
                    ci.setReturnValue(null);
                    ci.cancel();
                    return;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 10, 46, false))
            {
                ci.setReturnValue(null);
                ci.cancel();
                return;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                ci.setReturnValue(null);
                ci.cancel();
                return;
            }

            slot.onPickupFromSlot(p_82846_1_, itemstack1);
        }

        ci.setReturnValue(itemstack);
        ci.cancel();
        return;
    }
}
