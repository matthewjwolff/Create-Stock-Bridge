package com.tom.stockbridge;

import org.slf4j.Logger;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;

import com.tom.stockbridge.client.ClientRegistration;
import com.tom.stockbridge.network.NetworkHandler;

@Mod(StockBridge.MODID)
public class StockBridge {
	public static final String MODID = "createstockbridge";
	public static final Logger LOGGER = LogUtils.getLogger();

	private static CreateRegistrate registrate;

	public StockBridge() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		// Register the setup method for modloading
		bus.addListener(this::setup);
		// Register the doClientStuff method for modloading
		bus.addListener(this::doClientStuff);

		registrate = CreateRegistrate.create(MODID).registerEventListeners(bus);

		Registration.register();
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Create Contraption Terminals starting");
		Registration.postRegister();
		NetworkHandler.init();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		event.enqueueWork(ClientRegistration::register);
	}

	public static CreateRegistrate registrate() {
		return registrate;
	}
}
