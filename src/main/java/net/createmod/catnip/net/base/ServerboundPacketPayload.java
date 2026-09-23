package net.createmod.catnip.net.base;

import net.minecraft.server.level.ServerPlayer;

public interface ServerboundPacketPayload extends BasePacketPayload {
	/** Called on the server thread after Catnip dispatches this payload. */
	void handle(ServerPlayer player);
}
