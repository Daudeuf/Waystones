package net.blay09.mods.waystones.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WaystoneButton extends Button {

    private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation TRANSFER_LIMITATION_TEXTURE = new ResourceLocation(Waystones.MOD_ID, "textures/gui/transfer_limitation.png");

    private final int xpLevelCost;
    private final IWaystone waystone;
    private final boolean canCarry;
    private final boolean canPay;

    public WaystoneButton(int x, int y, IWaystone waystone, boolean canCarry, boolean canPay, int xpLevelCost, OnPress pressable) {
        super(x, y, 200, 20, getWaystoneNameComponent(waystone), pressable, Button.DEFAULT_NARRATION);
        Player player = Minecraft.getInstance().player;
        this.xpLevelCost = xpLevelCost;
        this.canCarry = canCarry;
        this.canPay = canPay;
        this.waystone = waystone;
        if (player == null || !PlayerWaystoneManager.mayTeleportToWaystone(player, waystone)) {
            active = false;
        } else if ((player.experienceLevel < xpLevelCost || (!canCarry && !canPay)) && !player.getAbilities().instabuild) {
            active = false;
        }
    }

    private static Component getWaystoneNameComponent(IWaystone waystone) {
        String effectiveName = waystone.getName();
        if (effectiveName.isEmpty()) {
            effectiveName = I18n.get("gui.waystones.waystone_selection.unnamed_waystone");
        }
        final var textComponent = Component.literal(effectiveName);
        if (waystone.isGlobal()) {
            textComponent.withStyle(ChatFormatting.YELLOW);
        }
        return textComponent;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        final List<Component> tooltip = new ArrayList<>();

        Minecraft mc = Minecraft.getInstance();

        // render distance
        if (waystone.getDimension() == mc.player.level().dimension() && isActive()) {
            int distance = (int) mc.player.position().distanceTo(waystone.getPos().getCenter());
            String distanceStr;
            if (distance < 10000 && (mc.font.width(getMessage()) < 120 || distance < 1000)) {
                distanceStr = distance + "m";
            } else {
                // sorry for ugly code, chatgpt was down and this was the only thing my dumbed down brain could come up with
                distanceStr = String.format("%.1f", distance / 1000f).replace(",0", "").replace(".0", "") + "km";
            }
            int xOffset = getWidth() - mc.font.width(distanceStr);
            guiGraphics.drawString(mc.font, distanceStr, getX() + xOffset - 4, getY() + 6, 0xFFFFFF);
        }

        int gap = 0;

        // render xp cost
        if (xpLevelCost > 0) {
            boolean canAfford = Objects.requireNonNull(mc.player).experienceLevel >= xpLevelCost || mc.player.getAbilities().instabuild;

            guiGraphics.blit(ENCHANTMENT_TABLE_GUI_TEXTURE, getX() + 2, getY() + 2, (Math.min(xpLevelCost, 3) - 1) * 16, 223 + (!canAfford ? 16 : 0), 16, 16);

            if (xpLevelCost > 3) {
                guiGraphics.drawString(mc.font, "+", getX() + 17, getY() + 6, 0xC8FF8F);
            }

            if (isHovered && mouseX <= getX() + 20) {
                final var levelRequirementText = Component.translatable("gui.waystones.waystone_selection.level_requirement", xpLevelCost);
                levelRequirementText.withStyle(canAfford ? ChatFormatting.GREEN : ChatFormatting.RED);
                tooltip.add(levelRequirementText);
            }

            gap += 20;
        }

        // render item limit
        if (!canCarry) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(WaystonesConfig.getActive().itemLimit.costItem));
            ItemStack stack = item.getDefaultInstance().copyWithCount(WaystonesConfig.getActive().itemLimit.costCount);

            if (canPay) {
                guiGraphics.blit(TRANSFER_LIMITATION_TEXTURE, getX() + 2 + gap, getY() + 2, 16, 0, 16, 16, 32, 16);
                guiGraphics.drawString(mc.font, Integer.toString(stack.getCount()), getX() + gap + 10, getY() + 2, 0x00FF00 );
            } else {
                guiGraphics.blit(TRANSFER_LIMITATION_TEXTURE, getX() + 2 + gap, getY() + 2, 0, 0, 16, 16, 32, 16);
            }

            if (isHovered && mouseX <= getX() + 20 + gap && mouseX > getX() + gap) {
                tooltip.clear();

                final var itemLimitRequirementText = Component.translatable("gui.waystones.waystone_selection.item_limit_requirement", stack.getCount());
                itemLimitRequirementText.append(stack.getHoverName().copy());
                itemLimitRequirementText.withStyle(canPay ? ChatFormatting.GREEN : ChatFormatting.RED);
                tooltip.add(itemLimitRequirementText);
            }
        }

        if (!tooltip.isEmpty())
            guiGraphics.renderTooltip(mc.font, tooltip, Optional.empty(), mouseX, mouseY + mc.font.lineHeight);
    }
}
