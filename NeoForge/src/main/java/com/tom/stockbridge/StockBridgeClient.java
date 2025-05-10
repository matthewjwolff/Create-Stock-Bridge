package com.tom.stockbridge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

import com.tom.stockbridge.ae.AEClientPlatformRegistration;

public class StockBridgeClient {

	public static void preInit(ModContainer mc, IEventBus bus) {
		bus.addListener(AEClientPlatformRegistration::registerScreens);
	}

}
