package com.nikita23830.mixinfixmods.mixins;

import cpw.mods.fml.common.eventhandler.Event;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;

import java.util.HashMap;
import java.util.UUID;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerWritableBook implements INetHandlerPlayServer {
    @Shadow
    public EntityPlayerMP playerEntity;
    @Shadow
    private final MinecraftServer serverController;
    @Shadow
    public final NetworkManager netManager;
    @Shadow
    private boolean hasMoved = true;

    public NetHandlerPlayServerWritableBook(MinecraftServer p_i1530_1_, NetworkManager p_i1530_2_, EntityPlayerMP p_i1530_3_) {
        this.serverController = p_i1530_1_;
        this.netManager = p_i1530_2_;
    }

    @Inject(method = "processVanilla250Packet", at = @At("HEAD"), cancellable = true)
    public void processVanilla250PacketBook(C17PacketCustomPayload payload, CallbackInfo info) {
        if (payload != null && payload.func_149559_c() != null && payload.func_149559_c().equals("MC|BEdit")) {
            try {
                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(payload.func_149558_e()));
                ItemStack stack = packetbuffer.readItemStackFromBuffer();
                if (stack != null && stack.getItem() instanceof ItemWritableBook && this.playerEntity.inventory.getCurrentItem() != null && this.playerEntity.inventory.getCurrentItem().getItem() instanceof ItemWritableBook) {
                    NBTTagCompound nbt = null;
                    if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("pages")) {
                        NBTTagList list = stack.stackTagCompound.getTagList("pages", 8);
                        if (list.tagCount() > 5) {
                            for (int j = 5; j < list.tagCount(); ++j)
                                list.removeTag(j);
                            this.playerEntity.addChatMessage(new ChatComponentText("§e> §cВнимание! Достигнут лимит страниц: 5"));
                        }
                        nbt = stack.getTagCompound();
                        nbt.setTag("pages", list);
                    }
                    ItemStack clone = stack.copy();
                    if (nbt != null)
                        clone.setTagCompound(nbt);
                    this.playerEntity.inventory.setInventorySlotContents(this.playerEntity.inventory.currentItem, clone);
                    info.cancel();
                }
            } catch (Exception e) {
            }
        }
    }

    @Inject(method = "processPlayerDigging", at = @At("HEAD"), cancellable = true)
    public void processPlayerDiggingMixin(C07PacketPlayerDigging p_147345_1_, CallbackInfo ci) {
        if (p_147345_1_.func_149506_g() != 2)
            return;
        MovingObjectPosition mop = rayTrace(this.playerEntity, 1.0f, 10, false);
        if (mop == null) {
            ci.cancel();
            return;
        }
        int x = p_147345_1_.func_149505_c();
        int y = p_147345_1_.func_149503_d();
        int z = p_147345_1_.func_149502_e();
        AxisAlignedBB axis = AxisAlignedBB.getBoundingBox((x - 1.0D), (y - 1.0D), (z - 1.0D), (x + 1.0D), (y + 1.0D), (z + 1.0D));
        if (!isCurrent(mop, axis)) {
            ci.cancel();
            return;
        }
    }

    private MovingObjectPosition rayTrace(EntityLivingBase entity_base, float fasc, double dist, boolean interact) {
        Vec3 vec3 = Vec3.createVectorHelper(entity_base.posX, entity_base.posY + entity_base.getEyeHeight(), entity_base.posZ);
        Vec3 vec31 = entity_base.getLook(fasc);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * dist, vec31.yCoord * dist, vec31.zCoord * dist);
        return entity_base.worldObj.rayTraceBlocks(vec3, vec32, interact);
    }

    private boolean isCurrent(MovingObjectPosition mop, AxisAlignedBB axis) {
        return mop.blockX >= axis.minX && mop.blockX <= axis.maxX && mop.blockY >= axis.minY && mop.blockY <= axis.maxY && mop.blockZ >= axis.minZ && mop.blockZ <= axis.maxZ;
    }
}
