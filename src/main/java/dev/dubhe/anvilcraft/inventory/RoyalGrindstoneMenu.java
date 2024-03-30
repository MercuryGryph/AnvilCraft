package dev.dubhe.anvilcraft.inventory;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.init.ModMenuTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class RoyalGrindstoneMenu extends AbstractContainerMenu {
    private final Container repairToolSlots;
    private final Container resultToolSlots;
    private final Container repairMaterialSlots;
    private final Container resultMaterialSlots;
    private final ContainerLevelAccess access;

    public Integer usedGold = 0;
    public Integer removeRepairCostNumber = 0;
    public Integer removeCurseNumber = 0;

    public RoyalGrindstoneMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public RoyalGrindstoneMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.ROYAL_GRINDSTONE, containerId);
        this.repairToolSlots = new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                RoyalGrindstoneMenu.this.slotsChanged(this);
            }
        };
        this.resultToolSlots = new ResultContainer();
        this.repairMaterialSlots = new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                RoyalGrindstoneMenu.this.slotsChanged(this);
            }
        };
        this.resultMaterialSlots = new ResultContainer();
        this.access = access;
        this.addSlot(new Slot(this.repairToolSlots, 0, 25, 34) {
            public boolean mayPlace(ItemStack stack) {
                return stack.isDamageableItem() || stack.is(Items.ENCHANTED_BOOK) || stack.isEnchanted();
            }
        });
        this.addSlot(new Slot(this.repairMaterialSlots, 0, 89, 22) {
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.GOLD_INGOT);
            }
        });
        this.addSlot(new Slot(this.resultToolSlots, 2, 145, 34) {
            public boolean mayPlace(ItemStack stack) {return false;}
            public void onTake(Player player, ItemStack stack) {
                player.playSound(SoundEvents.GRINDSTONE_USE);
                repairToolSlots.setItem(0, ItemStack.EMPTY);
                repairMaterialSlots.setItem(0, new ItemStack(Items.GOLD_INGOT, repairMaterialSlots.getItem(0).getCount()-usedGold));
                resultMaterialSlots.setItem(2, new ItemStack(ModItems.CURSED_GOLD_INGOT, usedGold + resultMaterialSlots.getItem(2).getCount()));
            }
        });
        this.addSlot(new Slot(this.resultMaterialSlots, 2, 89, 48) {
            public boolean mayPlace(ItemStack stack) {return false;}
        });
        int i;
        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private ItemStack createResult() {
        ItemStack repairTool = repairToolSlots.getItem(0);
        ItemStack repairMaterial = repairMaterialSlots.getItem(0);
        if (repairTool.isEmpty()||repairMaterial.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = repairTool.copy();
        Map<Enchantment, Integer> curseMap = EnchantmentHelper.getEnchantments(result).entrySet().stream().filter((entry) -> entry.getKey().isCurse()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        int curseNumber = curseMap.size();
        int repairCost = repairTool.getBaseRepairCost();
        int goldNumber = repairMaterial.getCount();
        int goldUsed = 0;
        while ((goldNumber>0&&repairCost>0)) {
            result.setRepairCost(repairCost - 1);
            repairCost -= 1;
            goldNumber -= 1;
            goldUsed += 1;
        }
        int removeCurseNumber = 0;
        Iterator<Enchantment> iterator = curseMap.keySet().iterator();
        while ((goldNumber>=3 && curseNumber>0)){
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(result);
            map.remove(iterator.next());
            ItemStack itemStack = result.copy();
            itemStack.removeTagKey("Enchantments");
            itemStack.removeTagKey("StoredEnchantments");
            EnchantmentHelper.setEnchantments(map, itemStack);
            result = itemStack.copy();
            curseNumber -= 1;
            goldUsed += 3;
            removeCurseNumber += 1;
        }
        if (result.is(Items.ENCHANTED_BOOK) && EnchantmentHelper.getEnchantments(result).isEmpty()) {
            result = new ItemStack(Items.BOOK, result.getCount());
        }
        this.usedGold = goldUsed;
        this.removeRepairCostNumber = repairTool.getBaseRepairCost() - repairCost;
        this.removeCurseNumber = removeCurseNumber;
        return result;
    }


    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack clickedItem = slot.getItem();
            itemStack = clickedItem.copy();
            if (index >= 0 && index <= 3) {
                if (!this.moveItemStackTo(itemStack, 4, 39, false)) {
                    return ItemStack.EMPTY;
                }
                else {
                    if (index == 2) {
                        slot.onTake(player, clickedItem);
                    }
                    this.getSlot(index).setByPlayer(ItemStack.EMPTY);
                }
            } else {
                if (itemStack.isDamageableItem() || itemStack.is(Items.ENCHANTED_BOOK) || itemStack.isEnchanted()) {
                    if (!this.getSlot(0).hasItem()) {
                        this.getSlot(0).setByPlayer(itemStack);
                        this.getSlot(index).setByPlayer(ItemStack.EMPTY);
                    }
                    else {
                        return ItemStack.EMPTY;
                    }
                } else if (itemStack.is(Items.GOLD_INGOT)) {
                    if (!this.getSlot(1).hasItem()) {
                        this.getSlot(1).setByPlayer(itemStack);
                        this.getSlot(index).setByPlayer(ItemStack.EMPTY);
                    }
                    else {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }


    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ROYAL_GRINDSTONE);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        resultToolSlots.setItem(2, createResult());
    }
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> {
            this.clearContainer(player, this.repairToolSlots);
            this.clearContainer(player, this.repairMaterialSlots);
            this.clearContainer(player, this.resultMaterialSlots);
        });
    }
    protected void clearContainer(Player player, Container container) {
        int i;
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            for(i = 0; i < container.getContainerSize(); ++i) {
                player.drop(container.removeItemNoUpdate(i), false);
            }

        } else {
            for(i = 0; i < container.getContainerSize(); ++i) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
                }
            }

        }
    }
}
