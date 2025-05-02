package com.tom.stockbridge.ae;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import com.tom.stockbridge.ae.util.AEMenuTypeBuilder;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;

public class AEStockBridgeMenu extends AEBaseMenu {
	public static final MenuType<AEStockBridgeMenu> TYPE = AEMenuTypeBuilder.create(AEStockBridgeMenu::new, AEStockBridgeBlockEntity.class)
			.build();
	private static final String ACTION_SET_ADDRESS = "setAddress";

	@GuiSync(0)
	private String address;

	public AEStockBridgeMenu(MenuType<?> menuType, int id, Inventory playerInventory, AEStockBridgeBlockEntity host) {
		super(menuType, id, playerInventory, host);

		this.createPlayerInventorySlots(playerInventory);
		this.registerClientAction(ACTION_SET_ADDRESS, String.class, this::setAddress);
	}

	public void setAddress(final String value) {
		this.address = value;
		if (this.isClientSide()) {
			this.sendClientAction(ACTION_SET_ADDRESS, (Object) value);
		} else {
			getBlockEntity().behaviour.recipeAddress = value;
			getBlockEntity().setChanged();
		}
	}

	@Override
	public AEStockBridgeBlockEntity getBlockEntity() {
		return (AEStockBridgeBlockEntity) super.getBlockEntity();
	}

	@Override
	public void broadcastChanges() {
		address = getBlockEntity().behaviour.recipeAddress;
		super.broadcastChanges();
	}

	public String getAddress() {
		return address;
	}
}
