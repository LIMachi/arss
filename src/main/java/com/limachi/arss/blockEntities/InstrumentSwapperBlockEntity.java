package com.limachi.arss.blockEntities;

import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class InstrumentSwapperBlockEntity extends BaseOpaqueContainerBlockEntity {

    @RegisterBlockEntity
    public static RegistryObject<BlockEntityType<BlockEntity>> TYPE;

    public InstrumentSwapperBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state, 16); }

    public void dropAllInstruments() {
        if (level instanceof ServerLevel) {
            for (int i = 0; i < getContainerSize(); ++i) {
                ItemStack stack = getItem(i);
                if (!stack.isEmpty()) {
                    double d0 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
                    double d1 = (double)(level.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
                    double d2 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
                    ItemEntity itementity = new ItemEntity(level, (double)worldPosition.getX() + d0, (double)worldPosition.getY() + d1, (double)worldPosition.getZ() + d2, stack);
                    itementity.setDefaultPickUpDelay();
                    level.addFreshEntity(itementity);
                }
            }
            clearContent();
            level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
        }
    }

    @Nullable
    public ResourceLocation customSkullSound() {
        ItemStack stack = getItem(getBlockState().getValue(BlockStateProperties.POWER));
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof EntityBlock ebi)
            if (ebi.newBlockEntity(worldPosition, bi.getBlock().defaultBlockState()) instanceof SkullBlockEntity skull) {
                CompoundTag tag = BlockItem.getBlockEntityData(stack);
                if (tag != null)
                    skull.load(tag);
                return skull.getNoteBlockSound();
            }
        return null;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof BlockItem && super.canPlaceItem(slot, stack);
    }

    public void updateInstrument(BlockState state) {
        if (level instanceof ServerLevel) {
            ItemStack stack = getItem(state.getValue(BlockStateProperties.POWER));
            if (stack.getItem() instanceof BlockItem bi)
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, bi.getBlock().defaultBlockState().instrument()));
            else
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, NoteBlockInstrument.HARP));
        }
    }

    @Override
    public void setChanged() {
        updateInstrument(getBlockState());
        super.setChanged();
    }
}
