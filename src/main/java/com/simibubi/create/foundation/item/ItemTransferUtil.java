package com.simibubi.create.foundation.item;

import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/** Temporary boundary between Create's legacy inventory model and NeoForge's transfer API. */
public final class ItemTransferUtil {
	private ItemTransferUtil() {}

	public static @Nullable ResourceHandler<ItemResource> of(@Nullable IItemHandler handler) {
		return handler == null ? null : new LegacyItemHandlerResourceHandler(handler);
	}

	public static @Nullable IItemHandler legacy(@Nullable ResourceHandler<ItemResource> handler) {
		return handler == null ? null : IItemHandler.of(handler);
	}
}
