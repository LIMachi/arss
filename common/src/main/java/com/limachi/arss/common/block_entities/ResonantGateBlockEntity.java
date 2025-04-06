package com.limachi.arss.common.block_entities;

import com.limachi.arss.utils.Game;
import com.limachi.arss.utils.annotations.RegisterBlockEntity;

import com.limachi.arss.utils.annotations.RegisterMsg;
import com.limachi.arss.utils.network.IC2SMsg;
import com.limachi.arss.utils.network.IS2CMsg;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * difficulty: synchronicity of object that are updated in chunk order
 * one solution: use the vanilla event system (see note block) to update all gates
 * (collect all changes in 1 tick and standardise the power at event time)
 * since this is blockstate behavior and not block entity, will need to extend the diode manually
 */

public class ResonantGateBlockEntity extends BlockEntity {
    private static int lastId = 0;
    private static final HashMap<String, Network> networks = new HashMap<>(); //server only
    public static final HashSet<String> clientNames = new HashSet<>(); //client only

    @RegisterMsg
    public record RequestNames() implements IC2SMsg<RequestNames> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            if (ctx.getPlayer() instanceof ServerPlayer player)
                new SendNames(getAllNetworkNames().toArray(new String[0])).sendToClient(player);
        }
    }

    @RegisterMsg
    public record SendNames(String[] names) implements IS2CMsg<SendNames> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            clientNames.clear();
            clientNames.addAll(List.of(names));
        }
    }

    protected static void runOnNetwork(String frequency, Consumer<Network> run) {
        if (Game.isLogicalServer() && networks.containsKey(frequency)) {
            Network network = networks.get(frequency);
            run.accept(network);
            if (network.connections.isEmpty())
                networks.remove(frequency);
        }
    }

    protected static void newNetwork(String frequency) {
       networks.put(frequency, new Network());
    }

    public static void setDirect(String frequency, int power) {
        runOnNetwork(frequency, n->n.setDirect(power));
    }

    public static List<String> getAllNetworkNames() {
        return new ArrayList<>(networks.keySet());
    }

    public static class Network {
        HashSet<ResonantGateBlockEntity> connections = new HashSet<>();
        int direct = 0;

        public void syncPowers() {
            int best = direct;
            if (best < 15)
                for (ResonantGateBlockEntity b : connections) {
                    int t = b.getReceivedPower();
                    if (t > best) {
                        if (t >= 15) {
                            best = 15;
                            break;
                        }
                        best = t;
                    }
                }
            for (ResonantGateBlockEntity b : connections)
                b.setOutput(best);
        }

        public void setDirect(int direct) {
            int prev = this.direct;
            this.direct = direct;
            if (prev != direct)
                syncPowers();
        }
    }

    private String frequency = "";
    private int receivedPower = 0;

    @RegisterBlockEntity(value = "resonant_gate")
    public static RegistrySupplier<BlockEntityType<BlockEntity>> TYPE;

    public ResonantGateBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
    }

    public void updatePowerInput(int power) {
        if (Game.isLogicalServer() && power != receivedPower && power >= 0 && power <= 15) {
            receivedPower = power;
            runOnNetwork(frequency, Network::syncPowers);
            setChanged();
        }
    }

    public void setOutput(int power) {
        if (getLevel() instanceof ServerLevel && power != getBlockState().getValue(BlockStateProperties.POWER) && power >= 0 && power <= 15)
            level.setBlock(worldPosition, getBlockState().setValue(BlockStateProperties.POWER, power), 3);
    }

    public void updateOutputs() {
        runOnNetwork(frequency, Network::syncPowers);
    }

    public String getFrequency() { return frequency; }
    public int getReceivedPower() { return receivedPower; }

    public void disconnect() {
        runOnNetwork(frequency, n->{
            n.connections.remove(this);
            n.syncPowers();
        });
    }

    public void connect() {
        runOnNetwork(frequency, n->{
            n.connections.add(this);
            n.syncPowers();
        });
    }

    public void changeFrequency(String value) {
        if (Game.isLogicalServer() && !value.equals(frequency)) {
            disconnect();
            frequency = value;
            if (!frequency.isBlank()) {
                if (!networks.containsKey(frequency))
                    newNetwork(frequency);
                connect();
            }
            setChanged();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        disconnect();
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.putString("frequency", frequency);
        compoundTag.putInt("received", receivedPower);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        changeFrequency(compoundTag.getString("frequency"));
        updatePowerInput(compoundTag.getInt("received"));
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }
}
