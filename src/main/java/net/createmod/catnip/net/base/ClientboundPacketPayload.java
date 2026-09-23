package net.createmod.catnip.net.base;

import net.minecraft.client.player.LocalPlayer;

public interface ClientboundPacketPayload extends BasePacketPayload {
	/** Called on the client thread after Catnip dispatches this payload. */
	void handle(LocalPlayer player);
}
