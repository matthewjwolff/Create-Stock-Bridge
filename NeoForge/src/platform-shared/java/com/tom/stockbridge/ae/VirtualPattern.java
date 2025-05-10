package com.tom.stockbridge.ae;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.tom.stockbridge.Registration;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

public class VirtualPattern implements IPatternDetails {
	private static final LoadingCache<AEItemKey, VirtualPattern> PATTERN_CACHE = CacheBuilder.newBuilder().build(CacheLoader.from(VirtualPattern::new));
	private final IInput[] input;
	private final List<GenericStack> output;
	private final AEItemKey item;
	private final AEItemKey result;

	private VirtualPattern(AEItemKey result) {
		this.result = result;
		output = List.of(new GenericStack(result, 1));
		input = new IInput[] {new Input(result)};
		var stack = Registration.REQUEST_ITEM.asStack();
		stack.set(AERegistration.VIRTUAL_PATTERN_OUT, result);
		item = AEItemKey.of(stack);
	}

	public static VirtualPattern of(AEItemKey result) {
		try {
			return PATTERN_CACHE.get(result);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public AEItemKey getDefinition() {
		return item;
	}

	@Override
	public IInput[] getInputs() {
		return input;
	}

	@Override
	public List<GenericStack> getOutputs() {
		return output;
	}

	public AEItemKey getResult() {
		return result;
	}

	private static class Input implements IInput {
		private GenericStack[] template;

		public Input(AEItemKey item) {
			this.template = new GenericStack[]{new GenericStack(AERemoteItemKey.of(item.getReadOnlyStack()), 1L)};
		}

		@Override
		public GenericStack[] getPossibleInputs() {
			return template;
		}

		@Override
		public long getMultiplier() {
			return 1;
		}

		@Override
		public boolean isValid(AEKey input, Level p1) {
			return input.matches(this.template[0]);
		}

		@Override
		public @Nullable AEKey getRemainingKey(AEKey p0) {
			return null;
		}
	}
}
