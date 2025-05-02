package com.tom.stockbridge.ae;

import java.util.List;

import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.simibubi.create.infrastructure.ponder.scenes.highLogistics.FrogAndConveyorScenes;
import com.simibubi.create.infrastructure.ponder.scenes.highLogistics.PonderHilo;
import com.tterrag.registrate.util.entry.BlockEntry;

import com.tom.stockbridge.StockBridge;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.init.client.InitScreens;

public class AEClientRegistration {
	private static final Logger LOG = LoggerFactory.getLogger(AEClientRegistration.class);

	public static void register() {
		AEKeyRendering.register(RemoteItemKeyType.INSTANCE, AERemoteItemKey.class, new AEKeyRenderHandler<AERemoteItemKey>() {
			private final Component REMOTE_INFO = Component.translatable("tooltip.stockbridge.remote_item");

			@Override
			public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, AERemoteItemKey stack) {
				var poseStack = guiGraphics.pose();
				poseStack.pushPose();

				var displayStack = stack.getReadOnlyStack();
				guiGraphics.renderItem(displayStack, x, y);
				guiGraphics.renderItemDecorations(minecraft.font, displayStack, x, y, "");

				poseStack.translate(x + 10.5f, y, 200);
				poseStack.scale(0.33f, 0.33f, 1);

				guiGraphics.blit(ResourceLocation.tryBuild(StockBridge.MODID, "textures/gui/remote_item.png"), 0, 0, 0, 0, 0, 16, 16, 16, 16);

				poseStack.popPose();
			}

			@Override
			public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AERemoteItemKey what, float scale,
					int combinedLight, Level level) {
				poseStack.pushPose();
				// Push it out of the block face a bit to avoid z-fighting
				poseStack.translate(0, 0, 0.01f);
				// The Z-scaling by 0.001 causes the model to be visually "flattened"
				// This cannot replace a proper projection, but it's cheap and gives the desired effect.
				// We don't scale the normal matrix to avoid lighting issues.
				poseStack.mulPoseMatrix(new Matrix4f().scale(scale, scale, 0.001f));
				// Rotate the normal matrix a little for nicer lighting.
				poseStack.last().normal().rotateX(Mth.DEG_TO_RAD * -45f);

				Minecraft.getInstance().getItemRenderer().renderStatic(what.getReadOnlyStack(), ItemDisplayContext.GUI,
						combinedLight, OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);

				poseStack.popPose();
			}

			@Override
			public Component getDisplayName(AERemoteItemKey stack) {
				return stack.getDisplayName();
			}

			@Override
			public List<Component> getTooltip(AERemoteItemKey stack) {
				try {
					List<Component> l = stack.getReadOnlyStack().getTooltipLines(Minecraft.getInstance().player,
							Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED
									: TooltipFlag.Default.NORMAL);
					l.add(REMOTE_INFO);
					return l;
				} catch (Exception e) {
					LOG.error("Getting the tooltip of item {} crashed!", stack.getId(), e);
					return List.of(
							stack.getDisplayName(),
							Component.literal(stack.getId().toString()),
							Component.literal("GETTING TOOLTIP CRASHED").withStyle(ChatFormatting.RED));
				}
			}

		});

		InitScreens.register(AEStockBridgeMenu.TYPE, AEStockBridgeScreen::new, "/screens/csb_bridge.json");
	}

	public static void registerPonder(PonderSceneRegistrationHelper<BlockEntry<?>> HELPER) {
		HELPER.forComponents(AERegistration.BRIDGE_BLOCK)
		.addStoryBoard("ae_stockbridge", AEClientRegistration::stockbridge, AllCreatePonderTags.HIGH_LOGISTICS);
	}

	public static void stockbridge(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		BlockPos ticker = util.grid().at(5, 1, 1);
		BlockPos bridge = util.grid()
				.at(2, 1, 2);

		BlockPos conv1 = util.grid()
				.at(5, 4, 1);
		BlockPos conv2 = util.grid()
				.at(5, 4, 6);

		scene.title("ae_stockbridge", "Connecting the Stock System to AE");
		scene.configureBasePlate(0, 0, 7);
		scene.scaleSceneView(.75f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		ElementLink<WorldSectionElement> terminal = scene.world().showIndependentSection(util.select().position(2, 1, 2), Direction.DOWN);
		scene.world().moveSection(terminal, util.vector().of(0, 0, 0), 0);
		scene.idle(10);

		scene.overlay().showText(70)
		.pointAt(util.vector().of(2, 2, 2))
		.placeNearTarget()
		.attachKeyFrame()
		.text("AE Stock Bridge links the Create Stock system with AE");
		scene.idle(60);

		ElementLink<WorldSectionElement> ae = scene.world().showIndependentSection(util.select().fromTo(1, 1, 2, 1, 2, 4), Direction.DOWN);
		scene.world().moveSection(ae, util.vector().of(0, 0, 0), 0);
		scene.idle(10);

		ElementLink<WorldSectionElement> stock = scene.world().showIndependentSection(util.select().fromTo(3, 1, 1, 6, 4, 6), Direction.DOWN);
		scene.world().moveSection(stock, util.vector().of(0, 0, 0), 0);
		scene.idle(10);

		scene.overlay().showText(70)
		.pointAt(util.vector().of(2, 2, 2))
		.placeNearTarget()
		.attachKeyFrame()
		.text("Set the AE Stock Bridge address to match the Frogport address");
		scene.idle(60);

		scene.overlay()
		.showText(40)
		.colored(PonderPalette.BLUE)
		.text("AE")
		.pointAt(util.vector()
				.topOf(bridge))
		.placeNearTarget();
		scene.overlay()
		.showText(40)
		.colored(PonderPalette.BLUE)
		.text("AE")
		.pointAt(util.vector()
				.topOf(util.grid().at(3, 2, 2)))
		.placeNearTarget();
		scene.idle(20);

		scene.overlay().showText(70)
		.pointAt(util.vector().of(1, 2, 3))
		.placeNearTarget()
		.attachKeyFrame()
		.text("AE can request items by 'crafting'");
		scene.idle(10);
		PonderHilo.linkEffect(scene, bridge);
		scene.idle(20);

		ItemStack box = PackageStyles.getDefaultBox().copy();
		PackageItem.addAddress(box, "AE");
		scene.world().modifyBlockEntity(conv2, ChainConveyorBlockEntity.class, be -> {
			be.addLoopingPackage(new ChainConveyorPackage(0, box));
			FrogAndConveyorScenes.boxTransfer(conv1, conv2, be);
		});

		scene.idle(60);

		PonderHilo.packagerUnpack(scene, util.grid().at(3, 1, 2), box);
		scene.idle(20);

		scene.overlay().showText(70)
		.pointAt(util.vector().of(5, 2, 1))
		.independent(0)
		.attachKeyFrame()
		.text("Create can request stored items from AE");

		ItemStack box2 = PackageStyles.getDefaultBox().copy();
		PackageItem.addAddress(box2, "Vault");

		scene.idle(10);
		PonderHilo.linkEffect(scene, ticker);
		PonderHilo.linkEffect(scene, bridge);
		scene.idle(20);
		PonderHilo.packagerCreate(scene, util.grid().at(3, 1, 2), box2);
		scene.idle(30);
		scene.world()
		.modifyBlockEntity(util.grid().at(3, 1, 2), PackagerBlockEntity.class, be -> {
			be.heldBox = ItemStack.EMPTY;
		});
		scene.world()
		.modifyBlockEntity(util.grid().at(3, 2, 2), FrogportBlockEntity.class, be -> be.startAnimation(box2, true));
		scene.idle(100);

		scene.world().modifyBlockEntity(conv1, ChainConveyorBlockEntity.class, be -> {
			FrogAndConveyorScenes.boxTransfer(conv2, conv1, be);
		});
		scene.idle(110);
		scene.world().modifyBlockEntity(conv2, ChainConveyorBlockEntity.class, be -> {
			if (!be.getLoopingPackages().isEmpty())
				be.getLoopingPackages().remove(0);
		});
	}
}
