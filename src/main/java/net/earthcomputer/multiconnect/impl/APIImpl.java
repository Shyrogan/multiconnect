package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.*;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.earthcomputer.multiconnect.protocols.generic.ICustomPayloadC2SPacket;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.v1_12_2.CustomPayloadC2SPacket_1_12_2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.Arrays;
import java.util.List;

public class APIImpl extends MultiConnectAPI {
    @Override
    public int getProtocolVersion() {
        return ConnectionInfo.protocolVersion;
    }

    @Override
    public IProtocol byProtocolVersion(int version) {
        ConnectionMode protocol = ConnectionMode.byValue(version);
        return protocol == ConnectionMode.AUTO ? null : protocol;
    }

    @Override
    public List<IProtocol> getSupportedProtocols() {
        return Arrays.asList(ConnectionMode.protocolValues());
    }

    @Override
    public void addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        CustomPayloadHandler.addClientboundIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void removeIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        CustomPayloadHandler.removeClientboundIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void addStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        CustomPayloadHandler.addClientboundStringCustomPayloadListener(listener);
    }

    @Override
    public void removeStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        CustomPayloadHandler.removeClientboundStringCustomPayloadListener(listener);
    }

    @Override
    public void forceSendCustomPayload(Identifier channel, PacketByteBuf data) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            throw new IllegalStateException("Trying to send custom payload when not in-game");
        }
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, data);
        //noinspection ConstantConditions
        ((ICustomPayloadC2SPacket) packet).multiconnect_unblock();
        networkHandler.sendPacket(packet);
    }

    @Override
    public void forceSendStringCustomPayload(String channel, PacketByteBuf data) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            throw new IllegalStateException("Trying to send custom payload when not in-game");
        }
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2) {
            throw new IllegalStateException("Trying to send string custom payload to " + ConnectionMode.byValue(ConnectionInfo.protocolVersion).getName() + " server");
        }
        CustomPayloadC2SPacket_1_12_2 packet = new CustomPayloadC2SPacket_1_12_2(channel, data);
        packet.unblock();
        networkHandler.sendPacket(packet);
    }

    @Override
    public void addServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        CustomPayloadHandler.addServerboundIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void removeServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        CustomPayloadHandler.removeServerboundIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void addServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        CustomPayloadHandler.addServerboundStringCustomPayloadListener(listener);
    }

    @Override
    public void removeServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        CustomPayloadHandler.removeServerboundStringCustomPayloadListener(listener);
    }

    @Override
    public <T> boolean doesServerKnow(Registry<T> registry, T value) {
        return registry.getKey(value).map(key -> doesServerKnow(registry, key)).orElse(false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> boolean doesServerKnow(Registry<T> registry, RegistryKey<T> key) {
        if (!DefaultRegistries.DEFAULT_REGISTRIES.containsKey(registry)) {
            return super.doesServerKnow(registry, key);
        }
        return ((ISimpleRegistry<T>) registry).getRealEntries().contains(key);
    }
}
