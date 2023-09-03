package com.limachi.arss.blockEntities;

import com.limachi.lim_lib.SoundUtils;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class AnalogJukeboxBlockEntity extends BaseOpaqueContainerBlockEntity {

    @RegisterBlockEntity(name = "analog_jukebox", blocks = "analog_jukebox")
    public static RegistryObject<BlockEntityType<BlockEntity>> TYPE;

    protected int playing = 0;

    private int ticksSinceLastEvent;
    private long recordStartedTick;
    private long tickCount;

    public AnalogJukeboxBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state, 15); }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        playing = tag.getInt("Playing");
        if (playing > 0)
            play(playing + 1);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Playing", playing);
    }

    public int playing() { return playing; }

    public void play(int rsPower) {
        if (level instanceof ServerLevel) {
            ItemStack record = rsPower > 0 ? getItem(rsPower - 1) : ItemStack.EMPTY;
            playing = record.isEmpty() ? 0 : rsPower;
            if (playing != 0) {
                recordStartedTick = tickCount;
                SoundUtils.startRecord(level, worldPosition, record);
            }
            else
                SoundUtils.stopRecord(level, worldPosition);
            setChanged();
            level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
        }
    }

    public boolean insertRecord(ItemStack recordStack) {
        if (recordStack.getItem() instanceof RecordItem && level instanceof ServerLevel) {
            for (int i = 0; i < getContainerSize(); ++i)
                if (getItem(i).isEmpty()) {
                    int power = level.getBestNeighborSignal(worldPosition);
                    if (power == i + 1) {
                        playing = power;
                        recordStartedTick = tickCount;
                        SoundUtils.startRecord(level, worldPosition, recordStack);
                    }
                    setItem(i, recordStack);
                    level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
                    return true;
                }
        }
        return false;
    }

    public void dropAllRecords() {
        if (level instanceof ServerLevel) {
            if (playing > 0) {
                playing = 0;
                SoundUtils.stopRecord(level, worldPosition);
            }
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
            clearContent();
            level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
        }
    }

    public int getAnalogOutputSignal() {
        if (playing == 0) return 0;
        Item record = getItem(playing - 1).getItem();
        if (record instanceof RecordItem)
            return ((RecordItem)record).getAnalogOutput();
        return 0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof RecordItem && super.canPlaceItem(slot, stack);
    }

    @Override
    public void setChanged() {
        if (level instanceof ServerLevel) {
            if (playing != 0 && getItem(playing - 1).isEmpty()) {
                playing = 0;
                SoundUtils.stopRecord(level, worldPosition);
            } else if (playing == 0) {
                int power = level.getBestNeighborSignal(worldPosition);
                if (power > 0) {
                    ItemStack record = getItem(power - 1);
                    if (!record.isEmpty()) {
                        playing = power;
                        recordStartedTick = tickCount;
                        SoundUtils.startRecord(level, worldPosition, record);
                    }
                }
            }
        }
        super.setChanged();
    }

    private boolean shouldRecordStopPlaying(RecordItem record) {
        return this.tickCount >= this.recordStartedTick + (long)record.getLengthInTicks() + 20L;
    }

    public void tick() {
        ++this.ticksSinceLastEvent;
        if (playing != 0) {
            ItemStack stack = getItem(playing - 1);
            if (stack.getItem() instanceof RecordItem record) {
                if (shouldRecordStopPlaying(record))
                    play(0);
                else if (ticksSinceLastEvent >= 20) {
                    ticksSinceLastEvent = 0;
                    if (level != null)
                        level.gameEvent(GameEvent.JUKEBOX_PLAY, worldPosition, GameEvent.Context.of(getBlockState()));
                    Vec3 vec3 = Vec3.atBottomCenterOf(worldPosition).add(0.0D, 1.2F, 0.0D);
                    float f = (float)level.getRandom().nextInt(4) / 24.0F;
                    ((ServerLevel)level).sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, f, 0.0D, 0.0D, 1.0D);
                }
            }
        }
        ++this.tickCount;
    }
}
