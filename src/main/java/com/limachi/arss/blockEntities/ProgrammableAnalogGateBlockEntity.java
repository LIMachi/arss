package com.limachi.arss.blockEntities;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.lim_lib.blockEntities.IOnUseBlockListener;
import com.limachi.lim_lib.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class ProgrammableAnalogGateBlockEntity extends BlockEntity implements IOnUseBlockListener {

    public static final RegistryObject<BlockEntityType<ProgrammableAnalogGateBlockEntity>> TYPE = Registries.blockEntity(Arss.MOD_ID, "programmable_analog_gate", ProgrammableAnalogGateBlockEntity::new, DiodeBlockFactory.getBlockRegister("programmable_analog_gate"));

    public ProgrammableAnalogGateBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        //menu open
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
    }

    public int getPower() {
        return 0;
    }
}
