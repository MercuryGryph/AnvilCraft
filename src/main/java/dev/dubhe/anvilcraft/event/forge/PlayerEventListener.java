package dev.dubhe.anvilcraft.event.forge;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.event.entity.PlayerEvent;
import dev.dubhe.anvilcraft.event.TooltipEventListener;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;


@EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class PlayerEventListener {
    /**
     * @param event 玩家交互实体事件
     */
    @SubscribeEvent
    public static void useEntity(@NotNull PlayerInteractEvent.EntityInteract event) {
        PlayerEvent.UseEntity playerEvent = new PlayerEvent.UseEntity(
            event.getEntity(), event.getTarget(), event.getHand(), event.getLevel()
        );
        AnvilCraft.EVENT_BUS.post(playerEvent);
        InteractionResult result = playerEvent.getResult();
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void itemTooltip(@NotNull ItemTooltipEvent event) {
        TooltipEventListener.addTooltip(event.getItemStack(), event.getToolTip());
    }
}