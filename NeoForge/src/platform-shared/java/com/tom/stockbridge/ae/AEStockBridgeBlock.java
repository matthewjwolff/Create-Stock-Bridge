package com.tom.stockbridge.ae;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.simibubi.create.foundation.block.IBE;

public class AEStockBridgeBlock extends Block implements IBE<AEStockBridgeBlockEntity> {

	public AEStockBridgeBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public Class<AEStockBridgeBlockEntity> getBlockEntityClass() {
		return AEStockBridgeBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends AEStockBridgeBlockEntity> getBlockEntityType() {
		return AERegistration.BRIDGE_TILE.get();
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState p_60503_, Level pLevel, BlockPos pPos, Player pPlayer,
			BlockHitResult p_60508_) {
		if (!pLevel.isClientSide) {
			if (pLevel.getBlockEntity(pPos) instanceof AEStockBridgeBlockEntity e)
				e.openConfigMenu(pPlayer);
		}
		return InteractionResult.SUCCESS;
	}
}
