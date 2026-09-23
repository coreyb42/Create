package net.createmod.catnip.net.base;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.createmod.catnip.api.network.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Bridges Create's established packet catalog onto Catnip's 26.2 split payload
 * registries.  Keeping the catalog allows payload implementations to migrate
 * independently without changing wire identifiers.
 */
public class CatnipPacketRegistry {
	private final Set<PacketType<?>> packets = new LinkedHashSet<>();
	public final Set<PacketType<?>> packetsView = Collections.unmodifiableSet(packets);
	private boolean registered;

	public CatnipPacketRegistry(String modId, String networkVersion) {}

	public void registerPacket(PacketType<?> packetType) {
		if (registered)
			throw new IllegalStateException("Cannot register packets after registration");
		packets.add(packetType);
	}

	public void registerAllPackets() {
		if (registered)
			throw new IllegalStateException("Packets have already been registered");
		for (PacketType<?> packet : packets)
			register(packet);
		registered = true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void register(PacketType packet) {
		if (ClientboundPacketPayload.class.isAssignableFrom(packet.clazz())) {
			NetworkHelper.INSTANCE.clientboundCodecs().register(packet.type(), packet.codec());
			ClientNetworkHelper.INSTANCE.registerPayloadHandler(packet.type(),
				(payload, player) -> ((ClientboundPacketPayload) payload).handle(player));
		} else if (ServerboundPacketPayload.class.isAssignableFrom(packet.clazz())) {
			NetworkHelper.INSTANCE.serverboundCodecs().register(packet.type(), packet.codec());
			NetworkHelper.INSTANCE.registerPayloadHandler(packet.type(),
				(payload, player) -> ((ServerboundPacketPayload) payload).handle(player));
		} else {
			throw new IllegalArgumentException("Payload does not declare a direction: " + packet.clazz().getName());
		}
	}

	public record PacketType<T extends BasePacketPayload>(CustomPacketPayload.Type<T> type, Class<T> clazz,
		StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
	}
}
