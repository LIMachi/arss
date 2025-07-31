package com.limachi.arss.common.block_entities;

import com.limachi.lim_lib.common.annotations.RegisterBlockEntity;
import com.limachi.lim_lib.common.annotations.RegisterMsg;
import com.limachi.lim_lib.common.network.IC2SMsg;
import com.limachi.lim_lib.common.network.IS2CMsg;
import com.limachi.lim_lib.common.utils.Game;

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
    private static final HashMap<String, HashSet<ResonantGateBlockEntity>> networks = new HashMap<>(); //server only
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

    protected static void syncPowers(HashSet<ResonantGateBlockEntity> listeners, int direct) {
        if (direct < 15)
            for (ResonantGateBlockEntity b : listeners) {
                int t = b.getReceivedPower();
                if (t > direct) {
                    if (t >= 15) {
                        direct = 15;
                        break;
                    }
                    direct = t;
                }
            }
        for (ResonantGateBlockEntity b : listeners)
            b.setOutput(direct);
    }

    protected static void syncPowers(HashSet<ResonantGateBlockEntity> listeners) { syncPowers(listeners, 0); }

    protected static void runOnNetwork(String frequency, Consumer<HashSet<ResonantGateBlockEntity>> run) {
        if (Game.isLogicalServer() && networks.containsKey(frequency)) {
            var network = networks.get(frequency);
            run.accept(network);
            if (network.isEmpty())
                networks.remove(frequency);
        }
    }

    protected static void newNetwork(String frequency) {
        if (networks.containsKey(frequency))
            networks.get(frequency).clear();
        else
            networks.put(frequency, new HashSet<>());
    }

    public static void setDirect(String frequency, int power) {
        runOnNetwork(frequency, n->syncPowers(n, power));
    }

    public static List<String> getAllNetworkNames() {
        return new ArrayList<>(networks.keySet());
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
            runOnNetwork(frequency, ResonantGateBlockEntity::syncPowers);
            setChanged();
        }
    }

    public void setOutput(int power) {
        if (getLevel() instanceof ServerLevel && power != getBlockState().getValue(BlockStateProperties.POWER) && power >= 0 && power <= 15)
            level.setBlock(worldPosition, getBlockState().setValue(BlockStateProperties.POWER, power).setValue(BlockStateProperties.POWERED, power != 0), 3);
    }

    public void updateOutputs() {
        runOnNetwork(frequency, ResonantGateBlockEntity::syncPowers);
    }

    public String getFrequency() { return frequency; }
    public int getReceivedPower() { return receivedPower; }

    public void disconnect() {
        runOnNetwork(frequency, n->{
            n.remove(ResonantGateBlockEntity.this);
            syncPowers(n);
        });
    }

    public void connect() {
        runOnNetwork(frequency, n->{
            n.add(ResonantGateBlockEntity.this);
            syncPowers(n);
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
            if (level != null)
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        } else
            frequency = value;
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
