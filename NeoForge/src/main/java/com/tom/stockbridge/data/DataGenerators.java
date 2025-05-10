package com.tom.stockbridge.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;

import com.tom.stockbridge.StockBridge;
import com.tom.stockbridge.client.ClientRegistration;

@EventBusSubscriber(modid = StockBridge.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		generator.addProvider(event.includeServer(), new SBRecipes(packOutput, lookupProvider));
		if(event.includeClient()) {
			ClientRegistration.register();
			StockBridge.registrate().addDataGenerator(ProviderType.LANG, provider -> {
				BiConsumer<String, String> langConsumer = provider::add;
				PonderIndex.getLangAccess().provideLang(StockBridge.MODID, langConsumer);
			});
		}

		event.getGenerator().addProvider(true, StockBridge.registrate().setDataProvider(new RegistrateDataProvider(StockBridge.registrate(), StockBridge.MODID, event)));
	}
}
