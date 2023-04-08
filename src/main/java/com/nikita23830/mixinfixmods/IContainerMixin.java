package com.nikita23830.mixinfixmods;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface IContainerMixin {

    ItemStack mergeItemStackMS(ItemStack stack, int slotA, int slotB, boolean doit);
}
