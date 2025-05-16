package com.tom.stockbridge.ae;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import appeng.api.AECapabilities;

public class AEPlatformRegistration {
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AERegistration.BRIDGE_TILE.get(), (be, side) -> be.getInv());
		event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, AERegistration.BRIDGE_TILE.get(), (be, _v) -> be);
	}
}
