package com.limachi.arss.blockEntities;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.arss.client.screen.ProgrammableGateScreen;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

//truth table (16*16, emits from 0 to 15 or keep last signal)

@StaticInit(Stage.BLOCK_ENTITY)
public class ProgrammableGateBlockEntity extends GenericDiodeBlockEntity {

    public static final RegistryObject<BlockEntityType<ProgrammableGateBlockEntity>> TYPE = Registries.blockEntity(Arss.MOD_ID, "programmable_gate", ProgrammableGateBlockEntity::new, DiodeBlockFactory.getBlockRegister("programmable_gate"));

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
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, ()->()->{
            ProgrammableGateScreen.client_open(this);
            return null;
        });
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByteArray("layout", this.layout);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
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
            saveAdditional(t);
            stack.getOrCreateTag().put("BlockEntityTag", t);
        }
        return Collections.singletonList(stack);
    }
}
