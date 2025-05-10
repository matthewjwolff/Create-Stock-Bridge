package com.tom.stockbridge.ae;

import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import com.mojang.serialization.MapCodec;

import com.tom.stockbridge.StockBridge;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

public class RemoteItemKeyType extends AEKeyType {
	private static final ResourceLocation ID = ResourceLocation.tryBuild(StockBridge.MODID, "ri");
	public static final RemoteItemKeyType INSTANCE = new RemoteItemKeyType();

	private RemoteItemKeyType() {
		super(ID, AERemoteItemKey.class, Component.translatable("gui.stockbridge.remote_items"));
	}

	@Override
	public AERemoteItemKey readFromPacket(final RegistryFriendlyByteBuf input) {
		Objects.requireNonNull(input);
		return AERemoteItemKey.fromPacket(input);
	}

	@Override
	public AERemoteItemKey loadKeyFromTag(HolderLookup.Provider registries, final CompoundTag tag) {
		return AERemoteItemKey.fromTag(registries, tag);
	}

	@Override
	public boolean supportsFuzzyRangeSearch() {
		return true;
	}

	@Override
	public Stream<TagKey<?>> getTagNames() {
		return BuiltInRegistries.ITEM.getTagNames().map(t -> t);
	}

	@Override
	public MapCodec<? extends AEKey> codec() {
		return AERemoteItemKey.MAP_CODEC;
	}
}
