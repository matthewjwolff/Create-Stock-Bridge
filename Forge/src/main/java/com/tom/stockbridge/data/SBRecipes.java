package com.tom.stockbridge.data;

import java.util.function.Consumer;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import com.simibubi.create.AllBlocks;

import com.tom.stockbridge.ae.AERegistration;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class SBRecipes extends RecipeProvider {

	public SBRecipes(PackOutput p_248933_) {
		super(p_248933_);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AERegistration.BRIDGE_BLOCK.get())
		.pattern("bsb")
		.pattern("eIe")
		.pattern("iti")
		.define('s', AllBlocks.STOCK_LINK)
		.define('I', AEBlocks.INTERFACE)
		.define('t', AllBlocks.STOCK_TICKER)
		.define('b', TagKey.create(Registries.ITEM, ResourceLocation.parse("forge:plates/brass")))
		.define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("forge:plates/iron")))
		.define('e', AEItems.ENGINEERING_PROCESSOR)
		.group("create")
		.unlockedBy("stuff", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(AllBlocks.STOCK_LINK, AEBlocks.INTERFACE).build()))
		.save(consumer);
	}
}