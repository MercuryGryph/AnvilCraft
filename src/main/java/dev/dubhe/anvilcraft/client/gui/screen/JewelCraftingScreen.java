package dev.dubhe.anvilcraft.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.inventory.JewelCraftingMenu;
import dev.dubhe.anvilcraft.inventory.component.jewel.JewelInputSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class JewelCraftingScreen extends AbstractContainerScreen<JewelCraftingMenu> {

    private final ResourceLocation CONTAINER_LOCATION = AnvilCraft.of("textures/gui/container/jewelcrafting/background.png");


    public JewelCraftingScreen(JewelCraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHintItemSlot(guiGraphics);
        renderHintItemDecorations(guiGraphics);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderHintItemSlot(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(leftPos, topPos, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);

        for (int i = JewelCraftingMenu.CRAFT_SLOT_START; i <= JewelCraftingMenu.CRAFT_SLOT_END; i++) {
            Slot slot = menu.getSlot(i);
            if (!slot.hasItem() && slot instanceof JewelInputSlot inputSlot) {
                ItemStack @Nullable [] ingredientItems = inputSlot.getIngredientItems();
                if (ingredientItems != null) {
                    int index = Math.round(minecraft.getTimer().getGameTimeDeltaTicks() / 20) % ingredientItems.length;
                    ItemStack stack = ingredientItems[index];
                    guiGraphics.renderItem(stack, slot.x, slot.y);
                }
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private void renderHintItemDecorations(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(leftPos, topPos, 0);

        for (int i = JewelCraftingMenu.CRAFT_SLOT_START; i <= JewelCraftingMenu.CRAFT_SLOT_END; i++) {
            Slot slot = menu.getSlot(i);
            if (!slot.hasItem() && slot instanceof JewelInputSlot inputSlot) {
                int count = inputSlot.getHintCount();
                ItemStack @Nullable [] ingredientItems = inputSlot.getIngredientItems();
                if (ingredientItems != null) {
                    int index = Math.round(minecraft.getTimer().getGameTimeDeltaTicks() / 20) % ingredientItems.length;
                    ItemStack stack = ingredientItems[index];
                    stack.setCount(count);
                    guiGraphics.renderItemDecorations(font, stack, slot.x, slot.y);
                }
            }
        }
        poseStack.popPose();
    }
}