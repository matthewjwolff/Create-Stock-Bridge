package com.tom.stockbridge.data;

import java.util.function.BiConsumer;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.data.DataGenerator;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.tterrag.registrate.providers.ProviderType;

import com.tom.stockbridge.StockBridge;
import com.tom.stockbridge.client.ClientRegistration;

@Mod.EventBusSubscriber(modid = StockBridge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		generator.addProvider(event.includeServer(), new SBRecipes(generator.getPackOutput()));
		if(event.includeClient()) {
			ClientRegistration.register();
			StockBridge.registrate().addDataGenerator(ProviderType.LANG, provider -> {
				BiConsumer<String, String> langConsumer = provider::add;
				PonderIndex.getLangAccess().provideLang(StockBridge.MODID, langConsumer);
			});
		}
	}
}
