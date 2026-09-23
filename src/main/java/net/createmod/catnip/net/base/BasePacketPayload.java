package net.createmod.catnip.net.base;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Compatibility contract for Create payloads while they are registered through
 * Catnip's split clientbound/serverbound payload registries.
 */
public interface BasePacketPayload extends CustomPacketPayload {
	PacketTypeProvider getTypeProvider();

	@Override
	default Type<? extends CustomPacketPayload> type() {
		return getTypeProvider().getType();
	}

	interface PacketTypeProvider {
		<T extends CustomPacketPayload> Type<T> getType();
	}
}
