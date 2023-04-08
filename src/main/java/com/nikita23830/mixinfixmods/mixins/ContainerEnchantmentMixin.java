package com.nikita23830.mixinfixmods.mixins;

import com.nikita23830.mixinfixmods.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerEnchantment.class)
public abstract class ContainerEnchantmentMixin extends Container {

    @Shadow
    private World worldPointer;
    @Shadow
    public IInventory tableInventory;

    @Inject(method = "onContainerClosed", at = @At("HEAD"), cancellable = true)
    public void onContainerClosedM(EntityPlayer player, CallbackInfo ci) {
        super.onContainerClosed(player);

        if (!this.worldPointer.isRemote) {
            ItemStack itemstack = this.tableInventory.getStackInSlotOnClosing(0);

            boolean add = false;
            if (itemstack != null) {
                ItemStack s = InventoryUtils.addToInventory(player, itemstack);
                if (s == null || s.stackSize != itemstack.stackSize)
                    add = true;
                if (s != null)
                    player.dropPlayerItemWithRandomChoice(s, false);
            }
            if (add)
                InventoryUtils.syncInventory(player);
        }
        ci.cancel();
    }
}
