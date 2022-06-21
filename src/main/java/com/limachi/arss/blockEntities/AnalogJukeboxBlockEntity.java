package com.limachi.arss.blockEntities;

import com.limachi.arss.blocks.AnalogJukebox;
import com.limachi.arss.utils.StaticInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.system.NonnullDefault;

import static com.limachi.arss.Registries.BLOCK_ENTITY_REGISTER;

@StaticInitializer.Static
@NonnullDefault
public class AnalogJukeboxBlockEntity extends BaseOpaqueContainerBlockEntity {

    public static final RegistryObject<BlockEntityType<?>> TYPE = BLOCK_ENTITY_REGISTER.register("analog_jukebox", ()->BlockEntityType.Builder.of(AnalogJukeboxBlockEntity::new, AnalogJukebox.R_BLOCK.get()).build(null));

    protected int playing = 0;

    public AnalogJukeboxBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state, 15); }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        playing = tag.getInt("Playing");
        if (playing > 0)
            play(playing + 1);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Playing", playing);
    }

    public int playing() { return playing; }

    public void play(int rsPower) {
        if (level != null) {
            ItemStack record = rsPower > 0 ? getItem(rsPower - 1) : ItemStack.EMPTY;
            playing = record.isEmpty() ? 0 : rsPower;
            level.levelEvent(1010, worldPosition, record.isEmpty() ? 0 : Item.getId(record.getItem()));
            setChanged();
            level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
        }
    }

    public boolean insertRecord(ItemStack recordStack) {
        if (recordStack.getItem() instanceof RecordItem && level != null) {
            for (int i = 0; i < getContainerSize(); ++i)
                if (getItem(i).isEmpty()) {
                    int power = level.getBestNeighborSignal(worldPosition);
                    if (power == i + 1) {
                        playing = power;
                        level.levelEvent(1010, worldPosition, Item.getId(recordStack.getItem()));
                    }
                    setItem(i, recordStack);
                    level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
                    return true;
                }
        }
        return false;
    }

    public void dropRecord() {
        if (level != null) {
            for (int i = getContainerSize() - 1; i >= 0; --i) {
                ItemStack record = getItem(i);
                if (!record.isEmpty()) {
                    if (playing == i) {
                        playing = 0;
                        level.levelEvent(1010, worldPosition, 0);
                    }
                    double d0 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
                    double d1 = (double)(level.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
                    double d2 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
                    ItemEntity itementity = new ItemEntity(level, (double)worldPosition.getX() + d0, (double)worldPosition.getY() + d1, (double)worldPosition.getZ() + d2, record.copy());
                    itementity.setDefaultPickUpDelay();
                    level.addFreshEntity(itementity);
                    removeItem(i, 1);
                    level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
                    return;
                }
            }
        }
    }

    public void dropAllRecords() {
        if (level != null) {
            if (playing > 0) {
                playing = 0;
                level.levelEvent(1010, worldPosition, 0);
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
}
