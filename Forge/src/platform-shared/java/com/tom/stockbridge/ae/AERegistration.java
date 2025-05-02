package com.tom.stockbridge.ae;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.content.redstone.displayLink.LinkBulbRenderer;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

import com.tom.stockbridge.Registration;
import com.tom.stockbridge.StockBridge;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.core.AppEng;

public class AERegistration {
	private static final CreateRegistrate REGISTRATE = StockBridge.registrate();

	public static final BlockEntry<AEStockBridgeBlock> BRIDGE_BLOCK = REGISTRATE.block("ae_bridge", AEStockBridgeBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.properties(p -> p.noOcclusion())
			.tag(BlockTags.NEEDS_IRON_TOOL)
			.transform(TagGen.pickaxeOnly())
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models()
					.getExistingFile(prov.modLoc("ae_bridge"))))
			.item(LogisticallyLinkedBlockItem::new)
			.transform(b -> b.model((c, p) -> {
				p.withExistingParent("ae_bridge",
						p.modLoc("block/ae_bridge"));
			}).build())
			.lang("AE Stock Bridge")
			.register();

	public static final BlockEntityEntry<AEStockBridgeBlockEntity> BRIDGE_TILE = REGISTRATE
			.blockEntity("ae_bridge", AEStockBridgeBlockEntity::new)
			.renderer(() -> LinkBulbRenderer::new)
			.validBlocks(BRIDGE_BLOCK)
			.register();

	private static final ResourceKey<Registry<AEKeyType>> AE_TYPE_REGISTRY = ResourceKey.createRegistryKey(AppEng.makeId("keytypes"));
	public static final RegistryEntry<RemoteItemKeyType> REMOTE_KEY_TYPE = REGISTRATE.generic("ri", AE_TYPE_REGISTRY, () -> RemoteItemKeyType.INSTANCE).register();

	static {
		REGISTRATE.generic("ae_bridge", Registries.MENU, () -> AEStockBridgeMenu.TYPE).register();
	}

	public static void register() {
	}

	public static void postRegister() {
		ContainerItemStrategies.register(RemoteItemKeyType.INSTANCE, AERemoteItemKey.class, new RemoteItemStrategy());
		PatternDetailsHelper.registerDecoder(new IPatternDetailsDecoder() {

			@Override
			public boolean isEncodedPattern(ItemStack is) {
				return is.getItem() == Registration.REQUEST_ITEM.get();
			}

			@Override
			public @Nullable IPatternDetails decodePattern(ItemStack is, Level level, boolean tryRecovery) {
				if (is.hasTag()) {
					return VirtualPattern.fromTag(is.getTag());
				}
				return null;
			}

			@Override
			public @Nullable IPatternDetails decodePattern(AEItemKey is, Level level) {
				return decodePattern(is.toStack(), level, false);
			}
		});
	}
}
