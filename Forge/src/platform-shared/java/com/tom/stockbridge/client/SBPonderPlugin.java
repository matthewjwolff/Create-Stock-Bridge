package com.tom.stockbridge.client;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.util.entry.BlockEntry;

import com.tom.stockbridge.StockBridge;
import com.tom.stockbridge.ae.AEClientRegistration;

public class SBPonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return StockBridge.MODID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		PonderSceneRegistrationHelper<BlockEntry<?>> HELPER = helper.withKeyFunction(BlockEntry::getId);

		AEClientRegistration.registerPonder(HELPER);
	}
}
