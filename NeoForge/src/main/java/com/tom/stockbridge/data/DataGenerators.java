package com.tom.stockbridge.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import com.tterrag.registrate.providers.ProviderType;

import com.tom.stockbridge.StockBridge;
import com.tom.stockbridge.client.ClientRegistration;

@EventBusSubscriber(modid = StockBridge.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void gatherDataLang(GatherDataEvent event) {
		if (event.getMods().contains(StockBridge.MODID)) {
			StockBridge.registrate().addDataGenerator(ProviderType.LANG, provider -> {
				BiConsumer<String, String> langConsumer = provider::add;
				providePonderLang(langConsumer);
			});
		}
	}

	private static void providePonderLang(BiConsumer<String, String> consumer) {
		// Register this since FMLClientSetupEvent does not run during datagen
		ClientRegistration.register();

		PonderIndex.getLangAccess().provideLang(StockBridge.MODID, consumer);
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		if (!event.getMods().contains(StockBridge.MODID)) {
			return;
		}
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		generator.addProvider(event.includeServer(), new SBRecipes(packOutput, lookupProvider));
	}
}
