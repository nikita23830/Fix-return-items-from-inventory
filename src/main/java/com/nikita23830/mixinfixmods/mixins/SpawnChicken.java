package com.nikita23830.mixinfixmods.mixins;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityEgg.class)
public abstract class SpawnChicken extends EntityThrowable {

    public SpawnChicken(World p_i1776_1_) {
        super(p_i1776_1_);
    }

    @Inject(method = "onImpact", at = @At("HEAD"), cancellable = true)
    private void onImpactChicken(MovingObjectPosition mop, CallbackInfo ci) {
        EntityLivingBase owner = this.getThrower();
        if (owner instanceof EntityPlayer) {
            BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(worldObj, WorldSettings.GameType.SURVIVAL, (EntityPlayerMP) owner, MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
            if (event.isCanceled()) {
                this.setDead();
                ci.cancel();
            }
        } else {
            this.setDead();
            ci.cancel();
        }
    }
}
