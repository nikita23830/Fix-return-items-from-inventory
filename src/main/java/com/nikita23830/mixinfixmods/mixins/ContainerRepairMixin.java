package com.nikita23830.mixinfixmods.mixins;

import com.nikita23830.mixinfixmods.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerRepair.class)
public abstract class ContainerRepairMixin extends Container {
    @Shadow
    private World theWorld;
    @Shadow
    private IInventory inputSlots;

    @Inject(method = "onContainerClosed", at = @At("HEAD"), cancellable = true)
    public void onContainerClosedM(EntityPlayer p_75134_1_, CallbackInfo ci) {
        super.onContainerClosed(p_75134_1_);

        if (!this.theWorld.isRemote) {
            boolean add = false;
            for (int i = 0; i < this.inputSlots.getSizeInventory(); ++i) {
                ItemStack itemstack = this.inputSlots.getStackInSlotOnClosing(i);

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
}
