package dev.dubhe.anvilcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$7")
abstract class DispenseItemEmptyBucketBehaviorMixin extends DefaultDispenseItemBehavior {
    @Inject(
        method = "execute(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)"
            + "Lnet/minecraft/world/item/ItemStack;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/dispenser/DefaultDispenseItemBehavior;"
                + "execute(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)"
                + "Lnet/minecraft/world/item/ItemStack;",
            ordinal = 1),
        cancellable = true
    )
    public void takeMilkFromCow(@NotNull BlockSource source, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        BlockPos blockPos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
        ServerLevel level = source.level();
        ServerLevel levelAccessor = source.level();
        List<Cow> cows =
            level.getEntities(EntityTypeTest.forClass(Cow.class), new AABB(blockPos), Entity::isAlive).stream()
                .toList();
        List<Goat> goats =
            level.getEntities(EntityTypeTest.forClass(Goat.class), new AABB(blockPos), Entity::isAlive).stream()
                .toList();
        if (cows.isEmpty() && goats.isEmpty()) return;
        levelAccessor.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
        Item item = Items.MILK_BUCKET;
        cir.setReturnValue(
            this.consumeWithRemainder(
                source,
                stack,
                new ItemStack(item)
            )
        );
        cir.cancel();
    }
}
