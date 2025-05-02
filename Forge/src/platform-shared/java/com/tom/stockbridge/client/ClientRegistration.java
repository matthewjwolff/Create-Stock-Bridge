package com.tom.stockbridge.client;

import net.createmod.ponder.foundation.PonderIndex;

import com.tom.stockbridge.ae.AEClientRegistration;

public class ClientRegistration {

	public static void register() {
		AEClientRegistration.register();
		PonderIndex.addPlugin(new SBPonderPlugin());
	}
}
