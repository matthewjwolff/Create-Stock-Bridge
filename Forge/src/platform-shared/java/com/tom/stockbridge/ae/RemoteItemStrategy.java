package com.tom.stockbridge.ae;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.AEBaseMenu;
import appeng.menu.locator.MenuLocator;
import appeng.menu.me.crafting.CraftAmountMenu;

public class RemoteItemStrategy implements ContainerItemStrategy<AERemoteItemKey, Context> {

	@Override
	public @Nullable GenericStack getContainedStack(ItemStack p0) {
		return null;
	}

	@Override
	public @Nullable Context findCarriedContext(Player p0, AbstractContainerMenu p1) {
		return new ContextImpl(p0, p1);
	}

	@Override
	public long extract(Context p0, AERemoteItemKey p1, long p2, Actionable p3) {
		return 0;
	}

	@Override
	public long insert(Context ctx, AERemoteItemKey key, long p2, Actionable p3) {
		// Pop crafting menu
		if (ctx.menu() instanceof AEBaseMenu menu && ctx.player() instanceof ServerPlayer player) {
			final MenuLocator locator = menu.getLocator();
			if (locator != null && key != null) {
				AEItemKey clickedKey = key.getAsItem();
				CraftAmountMenu.open(player, locator, clickedKey, clickedKey.getAmountPerUnit());
			}
		}
		return 0;
	}

	@Override
	public void playFillSound(Player p0, AERemoteItemKey p1) {
	}

	@Override
	public void playEmptySound(Player p0, AERemoteItemKey p1) {
	}

	@Override
	public @Nullable GenericStack getExtractableContent(Context p0) {
		return null;
	}

	record ContextImpl(Player player, AbstractContainerMenu menu) implements Context {}
}

interface Context {
	Player player();
	AbstractContainerMenu menu();
}