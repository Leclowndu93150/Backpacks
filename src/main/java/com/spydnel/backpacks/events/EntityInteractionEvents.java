package com.spydnel.backpacks.events;

import com.spydnel.backpacks.Backpacks;
import com.spydnel.backpacks.items.BackpackItemContainer;
import com.spydnel.backpacks.registry.BPItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Backpacks.MODID)
public class EntityInteractionEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        LivingEntity target = event.getTarget() instanceof LivingEntity ? (LivingEntity) event.getTarget() : null;
        ItemStack item = target != null ? target.getItemBySlot(EquipmentSlot.CHEST) : null;

        if (target != null && item != null && item.getItem() == BPItems.BACKPACK.get() && isBehind(player, target)) {
            BackpackItemContainer container = new BackpackItemContainer(target, player);
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> ChestMenu.threeRows(containerId, playerInventory, container),
                    Component.translatable("container.backpack")
            ));
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    public static boolean isBehind(Player player, LivingEntity target) {
        float t = 1.0F;
        Vec3 vector = player.getPosition(t).subtract(target.getPosition(t)).normalize();
        vector = new Vec3(vector.x, 0, vector.z);
        return target.getViewVector(t).dot(vector) < 0;
    }
}
