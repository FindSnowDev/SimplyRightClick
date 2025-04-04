package net.findsnow.simplyrightclick.core.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {

	@Shadow public abstract DyeColor getColor();

	@Unique
	private void playErrorSound(Entity entity) {
		entity.playSound(SoundEvents.NOTE_BLOCK_XYLOPHONE.value(), 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	@Unique
	private String getColorAdjective(DyeColor dyeColor)  {
		return switch (dyeColor) {
			case WHITE -> "pristine";
			case ORANGE -> "vibrant";
			case MAGENTA -> "dazzling";
			case LIGHT_BLUE -> "serene";
			case YELLOW -> "sunny";
			case LIME -> "refreshing";
			case PINK -> "rosy";
			case GRAY -> "sophisticated";
			case LIGHT_GRAY -> "subtle";
			case CYAN -> "tranquil";
			case PURPLE -> "regal";
			case BLUE -> "deep";
			case BROWN -> "earthy";
			case GREEN -> "lush";
			case RED -> "fiery";
			case BLACK -> "sleek";
		};
	}

	@Unique
	private String getColorName(DyeColor dyeColor)  {
		return switch (dyeColor) {
			case WHITE -> "White";
			case ORANGE -> "Orange";
			case MAGENTA -> "Magenta";
			case LIGHT_BLUE -> "Light Blue";
			case YELLOW -> "Yellow";
			case LIME -> "Lime";
			case PINK -> "Pink";
			case GRAY -> "Gray";
			case LIGHT_GRAY -> "Light Gray";
			case CYAN -> "Cyan";
			case PURPLE -> "Purple";
			case BLUE -> "Blue";
			case BROWN -> "Brown";
			case GREEN -> "Green";
			case RED -> "Red";
			case BLACK -> "Black";
		};
	}


	@Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
	private void onUse(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (heldItem.isEmpty()) {
			heldItem = player.getItemInHand(InteractionHand.OFF_HAND);
		}

		if (heldItem.getItem() instanceof DyeItem) {
			DyeItem dyeItem = (DyeItem) heldItem.getItem();
			DyeColor dyeColor = dyeItem.getDyeColor();
			DyeColor bedColor = this.getColor();

			if (dyeColor == bedColor) {
				player.displayClientMessage(Component.literal("[!] This bed is already a " + getColorAdjective(dyeColor) + " shade of " + getColorName(dyeColor) + "!").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), true);
				this.playErrorSound(player);
				cir.setReturnValue(InteractionResult.SUCCESS);
			}
		}
	}
}