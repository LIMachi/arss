package com.limachi.arss.common.block_entities;

import com.limachi.arss.common.blocks.KeyboardLecternBlock;
import com.limachi.arss.utils.annotations.RegisterBlockEntity;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class KeyboardLecternBlockEntity extends BlockEntity {
    //WARN: TRANSITIVE HASHMAP!
    protected static final HashMap<UUID, HashSet<BlockPos>> CONTROLLED_LECTERNS = new HashMap<>();
    protected ItemStack keyboard = ItemStack.EMPTY;
    protected int userCount = 0;

    @RegisterBlockEntity
    public static RegistrySupplier<BlockEntityType<KeyboardLecternBlockEntity>> TYPE;

    public void setKeyboard(ItemStack keyboard) { this.keyboard = keyboard; }

    public ItemStack getKeyboard() { return keyboard; }

    public static List<KeyboardLecternBlockEntity> getControlled(Player player) {
        ArrayList<KeyboardLecternBlockEntity> out = new ArrayList<>();
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.level() instanceof ServerLevel level) {
            UUID id = serverPlayer.getUUID();
            synchronized (CONTROLLED_LECTERNS) {
                var set = CONTROLLED_LECTERNS.get(id);
                if (set == null)
                    return out;
                HashSet<BlockPos> keep = new HashSet<>();
                boolean resync = false;
                for (var lectern : set)
                    if (serverPlayer.blockPosition().distSqr(lectern) <= 36 && level.getBlockEntity(lectern) instanceof KeyboardLecternBlockEntity be) {
                        out.add(be);
                        keep.add(lectern);
                    } else
                        resync = true;
                if (resync) {
                    if (keep.isEmpty())
                        CONTROLLED_LECTERNS.remove(id);
                    else
                        CONTROLLED_LECTERNS.put(id, keep);
                }
            }
        }
        return out;
    }

//    @RegisterMsg
//    public record SyncLinkedLecterns(long[] lecterns) implements IRecordMsg {
//        @Override
//        public void clientWork(Player player) {
//            if (lecterns.length > 0)
//                player.getPersistentData().putLongArray("KeyboardLecternBlock", lecterns);
//            else
//                player.getPersistentData().remove("KeyboardLecternBlock");
//        }
//
//        @Override
//        public void serverWork(Player player) {
//            ArrayList<Long> current = new ArrayList<>(Arrays.stream(player.getPersistentData().getLongArray("KeyboardLecternBlock")).boxed().toList());
//            ArrayList<Long> valid = new ArrayList<>();
//            for (long l : lecterns) {
//                if (player.level().getBlockEntity(BlockPos.of(l)) instanceof KeyboardLecternBlockEntity be) {
//                    valid.add(l);
//                    if (!current.contains(l))
//                        be.addUser(player);
//                }
//                current.remove(l);
//            }
//            for (long l : current)
//                if (player.level().getBlockEntity(BlockPos.of(l)) instanceof KeyboardLecternBlockEntity be)
//                    be.removeUser(player);
//            if (valid.isEmpty())
//                player.getPersistentData().remove("KeyboardLecternBlock");
//            else
//                player.getPersistentData().putLongArray("KeyboardLecternBlock", valid);
//        }
//    }

    protected void addUser(Player player) {
        player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard.true"), true);
        if (userCount == 0 && level != null && getBlockState().is(KeyboardLecternBlock.R_BLOCK.get()) && !getBlockState().getValue(KeyboardLecternBlock.POWERED))
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(KeyboardLecternBlock.POWERED, true));
        ++userCount;
        setChanged();
    }

    protected void removeUser(Player player) {
        player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard.false"), true);
        if (userCount > 0) {
            --userCount;
            if (userCount == 0 && level != null && getBlockState().is(KeyboardLecternBlock.R_BLOCK.get()) && getBlockState().getValue(KeyboardLecternBlock.POWERED))
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(KeyboardLecternBlock.POWERED, false));
            setChanged();
        }
    }

    public void setController(Player player) {
        if (player != null) {
//            CompoundTag data = player.getPersistentData();
//            if (data.contains("KeyboardLecternBlock", Tag.TAG_LONG_ARRAY)) {
//                ArrayList<Long> keep = new ArrayList<>(Arrays.stream(data.getLongArray("KeyboardLecternBlock")).boxed().toList());
//                if (keep.contains(worldPosition.asLong())) {
//                    removeUser(player);
//                    keep.remove(worldPosition.asLong());
//                }
//                else {
//                    addUser(player);
//                    keep.add(worldPosition.asLong());
//                }
//                if (keep.isEmpty())
//                    data.remove("KeyboardLecternBlock");
//                else
//                    data.putLongArray("KeyboardLecternBlock", keep);
//            }
//            else {
//                addUser(player);
//                data.putLongArray("KeyboardLecternBlock", Collections.singletonList(worldPosition.asLong()));
//            }
//            if (player instanceof ServerPlayer serverPlayer)
//                NetworkManager.toClient(serverPlayer, new SyncLinkedLecterns(data.getLongArray("KeyboardLecternBlock")));
        }
    }

    public KeyboardLecternBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

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
        tag.put("keyboard", keyboard.save(provider));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ItemStack.parse(provider, tag.getCompound("keyboard")).ifPresent(s->keyboard = s);
    }
}
