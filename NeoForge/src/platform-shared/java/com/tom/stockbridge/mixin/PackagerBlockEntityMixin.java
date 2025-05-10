package com.tom.stockbridge.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;

import com.tom.stockbridge.ae.AEStockBridgeBlock;
import com.tom.stockbridge.block.entity.AbstractStockBridgeBlockEntity;
import com.tom.stockbridge.block.entity.AbstractStockBridgeBlockEntity.BridgeInventory;

@Mixin(value = PackagerBlockEntity.class, remap = false)
public abstract class PackagerBlockEntityMixin extends SmartBlockEntity {
	public @Shadow InvManipulationBehaviour targetInventory;

	public PackagerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(at = @At("HEAD"), method = "getLinkPos", cancellable = true)
	private void stockbridge_onGetLinkPos(CallbackInfoReturnable<BlockPos> cbi) {
		for (Direction d : Iterate.directions) {
			BlockState adjacentState = level.getBlockState(worldPosition.relative(d));
			if (adjacentState.getBlock() instanceof AEStockBridgeBlock) {
				cbi.setReturnValue(worldPosition.relative(d));
				return;
			}
		}
	}

	/*@Inject(at = @At("HEAD"), method = "flashLink")
	private void stockbridge_onFlashLink(CallbackInfo cbi) {
		for (Direction d : Iterate.directions) {
			BlockState adjacentState = level.getBlockState(worldPosition.relative(d));
			if (adjacentState.getBlock() instanceof AEStockBridgeBlock) {
				WiFiEffectPacket.send(level, worldPosition.relative(d));
				return;
			}
		}
	}*/

	@Inject(at = @At("HEAD"), method = "attemptToSend")
	private void stockbridge_onAttemptToSend(List<PackagingRequest> queuedRequests, CallbackInfo cbi) {
		IItemHandler targetInv = targetInventory.getInventory();
		if (!(targetInv instanceof BridgeInventory bi))return;

		AbstractStockBridgeBlockEntity bridge = bi.getBlockEntity();

		boolean requestQueue = queuedRequests != null;

		if (requestQueue && !queuedRequests.isEmpty()) {
			bridge.pull(queuedRequests.get(0));
		}
	}
}
