package com.tom.stockbridge.ae.util;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import com.google.common.base.Preconditions;

import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;

public class AEMenuTypeBuilder<M extends AEBaseMenu, I> {
	private final Class<I> hostInterface;

	private final MenuFactory<M, I> factory;

	private Function<I, Component> menuTitleStrategy = this::getDefaultMenuTitle;

	@Nullable
	private InitialDataSerializer<I> initialDataSerializer;

	@Nullable
	private InitialDataDeserializer<M, I> initialDataDeserializer;

	private MenuType<M> menuType;

	private AEMenuTypeBuilder(Class<I> hostInterface, TypedMenuFactory<M, I> typedFactory) {
		this.hostInterface = hostInterface;
		this.factory = (containerId, playerInv, accessObj) -> typedFactory.create(menuType, containerId, playerInv,
				accessObj);
	}

	private AEMenuTypeBuilder(Class<I> hostInterface, MenuFactory<M, I> factory) {
		this.hostInterface = hostInterface;
		this.factory = factory;
	}

	public static <C extends AEBaseMenu, I> AEMenuTypeBuilder<C, I> create(MenuFactory<C, I> factory,
			Class<I> hostInterface) {
		return new AEMenuTypeBuilder<>(hostInterface, factory);
	}

	public static <C extends AEBaseMenu, I> AEMenuTypeBuilder<C, I> create(TypedMenuFactory<C, I> factory,
			Class<I> hostInterface) {
		return new AEMenuTypeBuilder<>(hostInterface, factory);
	}

	/**
	 * Specifies a custom strategy for obtaining a custom menu name.
	 * <p>
	 * The strategy should return {@link Component#empty()} if there's no custom name.
	 */
	public AEMenuTypeBuilder<M, I> withMenuTitle(Function<I, Component> menuTitleStrategy) {
		this.menuTitleStrategy = menuTitleStrategy;
		return this;
	}

	/**
	 * Sets a serializer and deserializer for additional data that should be transmitted from server->client when the
	 * menu is being first opened.
	 */
	public AEMenuTypeBuilder<M, I> withInitialData(InitialDataSerializer<I> initialDataSerializer,
			InitialDataDeserializer<M, I> initialDataDeserializer) {
		this.initialDataSerializer = initialDataSerializer;
		this.initialDataDeserializer = initialDataDeserializer;
		return this;
	}

	/**
	 * Opens a menu that is based around a single block entity. The block entity's position is encoded in the packet
	 * buffer.
	 */
	private M fromNetwork(int containerId, Inventory inv, RegistryFriendlyByteBuf packetBuf) {
		var locator = MenuLocators.readFromPacket(packetBuf);
		I host = locator.locate(inv.player, hostInterface);
		if (host == null) {
			var connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				connection.send(new ServerboundContainerClosePacket(containerId));
			}
			throw new IllegalStateException("Couldn't find menu host at " + locator + " for " + BuiltInRegistries.MENU.getId(menuType)
			+ " on client. Closing menu.");
		}
		M menu = factory.create(containerId, inv, host);
		menu.setReturnedFromSubScreen(packetBuf.readBoolean());
		if (initialDataDeserializer != null) {
			initialDataDeserializer.deserializeInitialData(host, menu, packetBuf);
		}
		return menu;
	}

	private boolean open(Player player, MenuHostLocator locator, boolean fromSubMenu) {
		if (!(player instanceof ServerPlayer)) {
			// Cannot open menus on the client or for non-players
			// FIXME logging?
			return false;
		}

		var accessInterface = locator.locate(player, hostInterface);

		if (accessInterface == null) {
			return false;
		}

		Component title = menuTitleStrategy.apply(accessInterface);

		class AppEngMenuProvider implements MenuProvider {
			@Override
			public Component getDisplayName() {
				return title;
			}

			@Nullable
			@Override
			public AbstractContainerMenu createMenu(int wnd, Inventory p, Player pl) {
				M m = factory.create(wnd, p, accessInterface);
				// Set the original locator on the opened server-side menu for it to more
				// easily remember how to re-open after being closed.
				m.setLocator(locator);
				return m;
			}

			@Override
			public boolean shouldTriggerClientSideContainerClosingOnOpen() {
				// Do not send close packets when switching between AE menus
				return !(player.containerMenu instanceof AEBaseMenu);
			}
		}

		player.openMenu(new AppEngMenuProvider(), buffer -> {
			MenuLocators.writeToPacket(buffer, locator);
			buffer.writeBoolean(fromSubMenu);
			if (initialDataSerializer != null) {
				initialDataSerializer.serializeInitialData(accessInterface, buffer);
			}
		});

		return true;
	}

	/**
	 * Creates a menu type that uses this helper as a factory and network deserializer.
	 */
	public MenuType<M> build() {
		Preconditions.checkState(menuType == null, "build was already called");

		menuType = IMenuTypeExtension.create(this::fromNetwork);
		MenuOpener.addOpener(menuType, this::open);
		return menuType;
	}

	@FunctionalInterface
	public interface MenuFactory<C, I> {
		C create(int containerId, Inventory playerInv, I menuHost);
	}

	@FunctionalInterface
	public interface TypedMenuFactory<C extends AbstractContainerMenu, I> {
		C create(MenuType<C> type, int containerId, Inventory playerInv, I accessObj);
	}

	/**
	 * Strategy used to serialize initial data for opening the menu on the client-side into the packet that is sent to
	 * the client.
	 */
	@FunctionalInterface
	public interface InitialDataSerializer<I> {
		void serializeInitialData(I host, RegistryFriendlyByteBuf buffer);
	}

	/**
	 * Strategy used to deserialize initial data for opening the menu on the client-side from the packet received by the
	 * server.
	 */
	@FunctionalInterface
	public interface InitialDataDeserializer<C, I> {
		void deserializeInitialData(I host, C menu, RegistryFriendlyByteBuf buffer);
	}

	private Component getDefaultMenuTitle(I accessInterface) {
		if (accessInterface instanceof Nameable nameable) {
			if (nameable.hasCustomName()) {
				return nameable.getCustomName();
			}
		}

		return Component.empty();
	}
}
