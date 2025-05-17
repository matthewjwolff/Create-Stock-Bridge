package com.tom.stockbridge.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import com.simibubi.create.AllBlocks;

import com.tom.stockbridge.ae.AERegistration;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class SBRecipes extends RecipeProvider {

	public SBRecipes(PackOutput output, CompletableFuture<Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AERegistration.BRIDGE_BLOCK.get())
		.pattern("bsb")
		.pattern("eIe")
		.pattern("iti")
		.define('s', AllBlocks.STOCK_LINK)
		.define('I', AEBlocks.INTERFACE)
		.define('t', AllBlocks.STOCK_TICKER)
		.define('b', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:plates/brass")))
		.define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:plates/iron")))
		.define('e', AEItems.ENGINEERING_PROCESSOR)
		.group("create")
		.unlockedBy("stuff", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(AllBlocks.STOCK_LINK, AEBlocks.INTERFACE).build()))
		.save(consumer);
	}
}