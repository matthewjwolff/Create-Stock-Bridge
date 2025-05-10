package com.tom.stockbridge.ae;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.google.common.base.Preconditions;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;

// Copy of AEItemKey
public class AERemoteItemKey extends AEKey {
	/**
	 * We currently cannot directly use {@link ItemStack#SINGLE_ITEM_CODEC} since it is wrapped up in a lazy codec,
	 * which prevents the dispatch codec from recognizing it as a MapCodec, making it unable to inline the fields.
	 */
	public static final MapCodec<AERemoteItemKey> MAP_CODEC = RecordCodecBuilder.mapCodec(
			builder -> builder.group(
					BuiltInRegistries.ITEM.holderByNameCodec().validate(
							item -> item.is(Items.AIR.builtInRegistryHolder())
							? DataResult.error(() -> "Item must not be minecraft:air")
									: DataResult.success(item))
					.fieldOf("id").forGetter(key -> key.stack.getItemHolder()),
					DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
					.forGetter(key -> key.stack.getComponentsPatch()))
			.apply(builder, (item, componentPatch) -> new AERemoteItemKey(new ItemStack(item, 1, componentPatch))));
	public static final Codec<AERemoteItemKey> CODEC = MAP_CODEC.codec();

	private final ItemStack stack;
	private final int hashCode;
	private final int maxStackSize;
	private final int damage;

	private AERemoteItemKey(ItemStack stack) {
		Preconditions.checkArgument(!stack.isEmpty(), "stack is empty");
		this.stack = stack;
		this.hashCode = ItemStack.hashItemAndComponents(stack);
		this.maxStackSize = stack.getMaxStackSize();
		this.damage = stack.getDamageValue();
	}

	@Nullable
	public static AERemoteItemKey of(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}

		return new AERemoteItemKey(stack.copy());
	}

	public static boolean matches(AEKey what, ItemStack itemStack) {
		return what instanceof AERemoteItemKey itemKey && itemKey.matches(itemStack);
	}

	public static boolean is(AEKey what) {
		return what instanceof AERemoteItemKey;
	}

	public static AEKeyFilter filter() {
		return AERemoteItemKey::is;
	}

	@Override
	public AEKeyType getType() {
		return RemoteItemKeyType.INSTANCE;
	}

	@Override
	public AERemoteItemKey dropSecondary() {
		return of(stack.getItem().getDefaultInstance());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AERemoteItemKey aeItemKey = (AERemoteItemKey) o;
		// The hash code comparison is a fast-fail cheap check
		return this.hashCode == aeItemKey.hashCode && ItemStack.isSameItemSameComponents(stack, aeItemKey.stack);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public static AERemoteItemKey of(ItemLike item) {
		return of(item.asItem().getDefaultInstance());
	}

	public boolean is(ItemLike item) {
		return stack.is(item.asItem());
	}

	public boolean matches(ItemStack stack) {
		return !stack.isEmpty() && ItemStack.isSameItemSameComponents(this.stack, stack);
	}

	public boolean matches(Ingredient ingredient) {
		return ingredient.test(getReadOnlyStack());
	}

	/**
	 * @return The ItemStack represented by this key. <strong>NEVER MUTATE THIS</strong>
	 */
	public ItemStack getReadOnlyStack() {
		return stack;
	}

	public ItemStack toStack() {
		return toStack(1);
	}

	public ItemStack toStack(int count) {
		if (count <= 0) {
			return ItemStack.EMPTY;
		}

		return stack.copyWithCount(count);
	}

	public Item getItem() {
		return stack.getItem();
	}

	@Nullable
	public static AERemoteItemKey fromTag(HolderLookup.Provider registries, CompoundTag tag) {
		var ops = registries.createSerializationContext(NbtOps.INSTANCE);
		try {
			return CODEC.decode(ops, tag).getOrThrow().getFirst();
		} catch (Exception e) {
			AELog.debug("Tried to load an invalid item key from NBT: %s", tag, e);
			return null;
		}
	}

	@Override
	public CompoundTag toTag(HolderLookup.Provider registries) {
		var ops = registries.createSerializationContext(NbtOps.INSTANCE);
		return (CompoundTag) CODEC.encodeStart(ops, this)
				.getOrThrow();
	}

	@Override
	public Object getPrimaryKey() {
		return stack.getItem();
	}

	/**
	 * @see ItemStack#getMaxDamage()
	 */
	@Override
	public int getFuzzySearchValue() {
		return this.damage;
	}

	/**
	 * @see ItemStack#getDamageValue()
	 */
	@Override
	public int getFuzzySearchMaxValue() {
		return getReadOnlyStack().getMaxDamage();
	}

	@Override
	public ResourceLocation getId() {
		return BuiltInRegistries.ITEM.getKey(stack.getItem());
	}

	@Override
	public ItemStack wrapForDisplayOrFilter() {
		return toStack();
	}

	@Override
	public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
		while (amount > 0) {
			if (drops.size() > 1000) {
				AELog.warn("Tried dropping an excessive amount of items, ignoring %s %ss", amount, stack.getItem());
				break;
			}

			var taken = Math.min(amount, getMaxStackSize());
			amount -= taken;
			drops.add(toStack((int) taken));
		}
	}

	@Override
	protected Component computeDisplayName() {
		return getReadOnlyStack().getHoverName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isTagged(TagKey<?> tag) {
		// This will just return false for incorrectly cast tags
		return stack.is((TagKey<Item>) tag);
	}

	@Override
	public <T> @Nullable T get(DataComponentType<T> type) {
		return stack.get(type);
	}

	@Override
	public boolean hasComponents() {
		return stack.getComponents().isEmpty();
	}

	/**
	 * @return True if the item represented by this key is damaged.
	 */
	public boolean isDamaged() {
		return damage > 0;
	}

	public int getMaxStackSize() {
		return maxStackSize;
	}

	@Override
	public void writeToPacket(RegistryFriendlyByteBuf data) {
		ItemStack.STREAM_CODEC.encode(data, stack);
	}

	public static AERemoteItemKey fromPacket(RegistryFriendlyByteBuf data) {
		var stack = ItemStack.STREAM_CODEC.decode(data);
		return new AERemoteItemKey(stack);
	}

	@Override
	public String toString() {
		var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		String idString = id != BuiltInRegistries.ITEM.getDefaultKey() ? id.toString()
				: stack.getItem().getClass().getName() + "(unregistered)";
		return stack.isComponentsPatchEmpty() ? idString : idString + " (with patches)";
	}

	public AEItemKey getAsItem() {
		return AEItemKey.of(stack);
	}
}
