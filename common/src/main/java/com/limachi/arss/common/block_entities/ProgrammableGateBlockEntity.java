package com.limachi.arss.common.block_entities;

import com.limachi.arss.Arss;

import com.limachi.arss.client.screen.ProgrammableGateScreen;

import com.limachi.arss.common.blocks.diodes.DiodeBlockFactory;

import com.limachi.lim_lib.common.annotations.StaticInit;
import com.limachi.lim_lib.common.modCreation.Stage;

import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class ProgrammableGateBlockEntity extends BaseAnalogDiodeBlockEntity {
    public static RegistrySupplier<BlockEntityType<ProgrammableGateBlockEntity>> TYPE;

    @StaticInit(Stage.BLOCK_ENTITY)
    public static void registerType() {
        TYPE = Arss.INSTANCE.registries.blockEntity("programmable_gate", ProgrammableGateBlockEntity::new, DiodeBlockFactory.getBlockRegister("programmable_gate"));
    }

    public final byte[] layout = new byte[256];
    public int mode = 0;

    public ProgrammableGateBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
    }

    public void update(int back, int sides) {
        int target = layout[back + sides * 16];
        if (target != 16 && target != this.getOutput())
            this.setOutput(target);
    }

    public void startEditing(Player player) {
        EnvExecutor.runInEnv(Env.CLIENT, ()->()->{
            ProgrammableGateScreen.client_open(this);
        });
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) { return saveWithoutMetadata(provider); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putByteArray("layout", this.layout);
        tag.putInt("mode", this.mode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("layout", Tag.TAG_BYTE_ARRAY)) {
            byte[] t = tag.getByteArray("layout");
            if (t.length == 256)
                System.arraycopy(t, 0, this.layout, 0, 256);
        }
        mode = tag.getInt("mode");
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
        if (!def && level != null) {
            CompoundTag t = saveWithId(level.registryAccess());
            saveAdditional(t, level.registryAccess());
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(t));
        }
        return Collections.singletonList(stack);
    }
}
