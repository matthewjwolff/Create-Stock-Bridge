package com.tom.stockbridge;

import net.minecraft.nbt.CompoundTag;

public interface IDataReceiver {
	void receive(CompoundTag tag);

}
