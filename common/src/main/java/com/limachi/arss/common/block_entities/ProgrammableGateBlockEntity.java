package com.limachi.arss.common.block_entities;

import com.limachi.arss.Arss;

import com.limachi.arss.client.screen.ProgrammableGateScreen;
import com.limachi.arss.common.blocks.diodes.DiodeBlockFactory;
import com.limachi.arss.utils.Stage;
import com.limachi.arss.utils.annotations.StaticInit;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

//truth table (16*16, emits from 0 to 15 or keep last signal)

public class ProgrammableGateBlockEntity extends BaseAnalogDiodeBlockEntity {
    public static RegistrySupplier<BlockEntityType<ProgrammableGateBlockEntity>> TYPE;

    @StaticInit(Stage.BLOCK_ENTITY)
    public static void registerType() {
        TYPE = Arss.registries.blockEntity("programmable_gate", ProgrammableGateBlockEntity::new, DiodeBlockFactory.getBlockRegister("programmable_gate"));
    }

    public final byte[] layout = new byte[256];

    private final HashSet<Player> editing = new HashSet<>();

    public ProgrammableGateBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
    }

    public void update(int back, int sides) {
        int target = layout[back + sides * 16];
        if (target != 16 && target != this.getOutput())
            this.setOutput(target);
    }

    public void startEditing(Player player) {
        editing.add(player);
        EnvExecutor.runInEnv(Env.CLIENT, ()->()->{
            ProgrammableGateScreen.client_open(this);
        });
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putByteArray("layout", this.layout);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("layout", Tag.TAG_BYTE_ARRAY)) {
            byte[] t = tag.getByteArray("layout");
            if (t.length == 256)
                System.arraycopy(t, 0, this.layout, 0, 256);
        }
    }

    @Override
    public List<ItemStack> getDrops(ServerLevel level, BlockPos pos, BlockState state, Player player) {
        boolean def = true;
        for (byte b : this.layout) {
            if (b != 0) {
                def = false;
                break;
            }
        }
        if (def && player.isCreative())
            return Collections.emptyList();
        ItemStack stack = state.getBlock().getCloneItemStack(level, pos, state);
        if (!def) {
            CompoundTag t = new CompoundTag();
//            saveAdditional(t);
//            stack.getOrCreateTag().put("BlockEntityTag", t);
        }
        return Collections.singletonList(stack);
    }
}
