package net.findsnow.simplyrightclick.common;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BedDyeFeature {
	public static void register() {
		InteractionEvent.RIGHT_CLICK_BLOCK.register(BedDyeFeature::onRightClick);
	}

	private static InteractionResult onRightClick(Player player, InteractionHand interactionHand, BlockPos blockPos, Direction direction) {
		Level level = player.level();
		BlockState blockState = level.getBlockState(blockPos);
		ItemStack heldItem = player.getItemInHand(interactionHand);

		if (blockState.getBlock() instanceof BedBlock && heldItem.getItem() instanceof DyeItem) {
			DyeColor dyeColor = ((DyeItem) heldItem.getItem()).getDyeColor();

			Block currentBed = blockState.getBlock();
			Block newBedBlock = getBedFromColor(dyeColor);

			if (currentBed == newBedBlock) {
				// write a red message here on the screen letting the player know they already have the same color bed as the dye
				return InteractionResult.PASS;
			}

			BedPart bedPart = blockState.getValue(BedBlock.PART);
			Direction facing = blockState.getValue(BedBlock.FACING);
			BlockPos headPos = bedPart == BedPart.HEAD ? blockPos : blockPos.relative(facing);
			BlockPos bottomPos = bedPart == BedPart.FOOT ? blockPos : blockPos.relative(facing.getOpposite());

			Map<UUID, BlockPos> spawnPoint = new HashMap<>();
			if (level instanceof ServerLevel) {
				ServerLevel serverLevel = (ServerLevel) level;
				for (ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
					BlockPos playerSpawn = serverPlayer.getRespawnPosition();
					if (playerSpawn != null && playerSpawn.equals(headPos)) {
						spawnPoint.put(serverPlayer.getUUID(), playerSpawn);
					}
				}
			}

			updateBed(level, headPos, bottomPos, newBedBlock, facing);

			if (level instanceof ServerLevel) {
				ServerLevel serverLevel = (ServerLevel) level;
				for (Map.Entry<UUID, BlockPos> bedEntry : spawnPoint.entrySet()) {
					ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(bedEntry.getKey());
					if (serverPlayer != null) {
						serverPlayer.setRespawnPosition(serverLevel.dimension(), headPos, 0, true, false);
					}
				}
			}

			level.playSound(null, blockPos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

			if (!player.isCreative()) {
				heldItem.shrink(1);
			}
			return InteractionResult.SUCCESS;

		}
		return InteractionResult.PASS;
	}

	private static void updateBed(Level level, BlockPos headPos, BlockPos footPos, Block newBedBlock, Direction facing) {
		BlockState oldHeadState = level.getBlockState(headPos);
		BlockState oldFootState = level.getBlockState(footPos);

		if (oldHeadState.getBlock() instanceof BedBlock && oldFootState.getBlock() instanceof BedBlock) {
			boolean isOccupied = oldHeadState.getValue(BedBlock.OCCUPIED);

			level.removeBlock(headPos, false);
			level.removeBlock(footPos, false);

			BlockState newHeadState = newBedBlock.defaultBlockState()
					.setValue(BedBlock.PART, BedPart.HEAD)
					.setValue(BedBlock.FACING, facing)
					.setValue(BedBlock.OCCUPIED, isOccupied);

			BlockState currentFootState = level.getBlockState(footPos);
			if (!(currentFootState.getBlock() instanceof BedBlock)) {
				BlockState newFootState = newBedBlock.defaultBlockState()
						.setValue(BedBlock.PART, BedPart.FOOT)
						.setValue(BedBlock.FACING, facing)
						.setValue(BedBlock.OCCUPIED, isOccupied);

				level.setBlock(headPos, newHeadState, 3);
				level.setBlock(footPos, newFootState, 3);
			}
		}
	}

	private static Block getBedFromColor(DyeColor dyeColor) {
		return switch(dyeColor) {
			case WHITE -> Blocks.WHITE_BED;
            case ORANGE -> Blocks.ORANGE_BED;
            case MAGENTA -> Blocks.MAGENTA_BED;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_BED;
            case YELLOW -> Blocks.YELLOW_BED;
            case LIME -> Blocks.LIME_BED;
            case PINK -> Blocks.PINK_BED;
            case GRAY -> Blocks.GRAY_BED;
			case LIGHT_GRAY -> Blocks.LIGHT_GRAY_BED;
			case CYAN -> Blocks.CYAN_BED;
            case PURPLE -> Blocks.PURPLE_BED;
            case BLUE -> Blocks.BLUE_BED;
            case BROWN -> Blocks.BROWN_BED;
            case GREEN -> Blocks.GREEN_BED;
			case RED -> Blocks.RED_BED;
			case BLACK -> Blocks.BLACK_BED;
		};
	}
}
