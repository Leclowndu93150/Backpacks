package com.spydnel.backpacks.networking;

import com.spydnel.backpacks.BackpackWearer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BackpackOpenPacket {
    private final boolean isOpen;
    private final int entityId;

    public BackpackOpenPacket(boolean isOpen, int entityId) {
        this.isOpen = isOpen;
        this.entityId = entityId;
    }

    public static void encode(BackpackOpenPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.isOpen);
        buf.writeInt(packet.entityId);
    }

    public static BackpackOpenPacket decode(FriendlyByteBuf buf) {
        return new BackpackOpenPacket(buf.readBoolean(), buf.readInt());
    }

    public static void handle(BackpackOpenPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(BackpackOpenPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(packet.entityId);
            if (entity instanceof BackpackWearer backpackWearer) {
                if (packet.isOpen) {
                    backpackWearer.onBackpackOpen();
                } else {
                    backpackWearer.onBackpackClose();
                }
            }
        }
    }
}
