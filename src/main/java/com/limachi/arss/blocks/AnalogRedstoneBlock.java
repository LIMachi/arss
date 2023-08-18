package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.arss.items.BlockItemWithCustomRenderer;
import com.limachi.lim_lib.registries.annotations.HasRedstoneTint;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings({"unused", "deprecation"})
@ParametersAreNonnullByDefault
public class AnalogRedstoneBlock extends PoweredBlock implements IScrollBlockPowerOutput {

    @HasRedstoneTint
    @RegisterBlock(name = "analog_redstone_block")
    public static RegistryObject<Block> R_BLOCK;

    public static class AnalogRedstoneBlockItem extends BlockItemWithCustomRenderer {

        @RegisterItem(name = "analog_redstone_block")
        public static RegistryObject<BlockItem> R_ITEM;

        public AnalogRedstoneBlockItem() { super(R_BLOCK.get(), new Item.Properties(), Blocks.REDSTONE_BLOCK); }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, level, components, flags);
        Arss.commonHoverText("analog_redstone_block", components);
    }

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public AnalogRedstoneBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK));
        registerDefaultState(stateDefinition.any().setValue(POWER, 15).setValue(ArssBlockStateProperties.CAN_SCROLL, true));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
        builder.add(ArssBlockStateProperties.CAN_SCROLL);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWER);
    }

    @Override
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return use(state, level, pos, player, hand, ()->super.use(state, level, pos, player, hand, hit));
    }
}
