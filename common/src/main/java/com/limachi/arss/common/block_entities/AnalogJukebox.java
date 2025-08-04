package com.limachi.arss.common.block_entities;

import com.limachi.lim_lib.common.annotations.RegisterBlockEntity;
import com.limachi.lim_lib.common.containers.MinimalListInventory;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

public class AnalogJukebox extends MinimalListInventory.MinimalListInventoryBlockEntity {

    @RegisterBlockEntity
    public static RegistrySupplier<BlockEntityType<BlockEntity>> TYPE;

    protected int playing;
    private final JukeboxSongPlayer jukeboxSongPlayer;

    public AnalogJukebox(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state, 15);
        jukeboxSongPlayer = new JukeboxSongPlayer(this::onSongChanged, this.getBlockPos());
        playing = 0;
    }

    public void onSongChanged() {
        if (level != null)
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        playing = Math.clamp(tag.getInt("playing"), 0, 15);
        if (tag.contains("ticks", 4) && playing > 0)
            JukeboxSong.fromStack(provider, getItem(playing)).ifPresent(h->jukeboxSongPlayer.setSongWithoutPlaying(h, tag.getLong("ticks")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("playing", playing);
        if (jukeboxSongPlayer.getSong() != null)
            tag.putLong("ticks", jukeboxSongPlayer.getTicksSinceSongStarted());
    }

    public int playing() { return playing; }

    public void play(int rsPower) {
        if (level instanceof ServerLevel) {
            Optional<Holder<JukeboxSong>> optional = JukeboxSong.fromStack(level.registryAccess(), rsPower > 0 ? getItem(rsPower - 1) : ItemStack.EMPTY);
            playing = optional.isEmpty() ? 0 : rsPower;
            if (playing != 0)
                jukeboxSongPlayer.play(level, optional.get());
            else
                jukeboxSongPlayer.stop(level, getBlockState());
        }
    }

    public boolean insertRecord(ItemStack recordStack) {
        if (level instanceof ServerLevel) {
            return JukeboxSong.fromStack(level.registryAccess(), recordStack).map(h->{
                for (int i = 0; i < getContainerSize(); ++i)
                    if (getItem(i).isEmpty()) {
                        setItem(i, recordStack);
                        level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
                        return true;
                    }
                return false;
            }).orElse(false);
        }
        return false;
    }

    public void dropAllRecords() {
        if (level instanceof ServerLevel) {
            for (int i = 0; i < getContainerSize(); ++i) {
                ItemStack record = getItem(i);
                if (!record.isEmpty()) {
                    double d0 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
                    double d1 = (double)(level.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
                    double d2 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
                    ItemEntity itementity = new ItemEntity(level, (double)worldPosition.getX() + d0, (double)worldPosition.getY() + d1, (double)worldPosition.getZ() + d2, record);
                    itementity.setDefaultPickUpDelay();
                    level.addFreshEntity(itementity);
                }
            }
        }
        getItems().replaceAll(s->ItemStack.EMPTY);
        playing = 0;
        jukeboxSongPlayer.stop(level, getBlockState());
        level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
    }

    public int getAnalogOutputSignal() {
        if (jukeboxSongPlayer.getSong() instanceof JukeboxSong s)
            return s.comparatorOutput();
        return 0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.has(DataComponents.JUKEBOX_PLAYABLE);
    }

    @Override
    public void setChanged() {
        if (level instanceof ServerLevel) {
            if (playing != 0 && getItem(playing - 1).isEmpty()) {
                playing = 0;
                jukeboxSongPlayer.stop(level, getBlockState());
            } else if (playing == 0) {
                int power = level.getBestNeighborSignal(worldPosition);
                if (power > 0) {
                    JukeboxSong.fromStack(level.registryAccess(), getItem(power - 1)).ifPresent(h->{
                        playing = power;
                        jukeboxSongPlayer.play(level, h);
                    });
                }
            }
        }
        super.setChanged();
    }

    public void tick(Level level, BlockState state) {
        jukeboxSongPlayer.tick(level, state);
    }
}
