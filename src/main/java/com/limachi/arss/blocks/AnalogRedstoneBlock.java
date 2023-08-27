package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.items.BlockItemWithCustomRenderer;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.registries.annotations.HasRedstoneTint;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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

import static com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties.CAN_SCROLL;
import static com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties.HIDE_DOT;

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
        registerDefaultState(stateDefinition.any().setValue(POWER, 15).setValue(CAN_SCROLL, true).setValue(HIDE_DOT, false));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER, CAN_SCROLL, HIDE_DOT);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWER);
    }

    @Override
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Item held = player.getItemInHand(hand).getItem();
        if ((held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.AnalogRedstoneTorchItem.R_ITEM.get()) && !KeyMapController.SNEAK.getState(player)) {
            level.setBlock(pos, state.setValue(HIDE_DOT, !state.getValue(HIDE_DOT)), 3);
            return InteractionResult.SUCCESS;
        }
        return use(state, level, pos, player, hand, ()->super.use(state, level, pos, player, hand, hit));
    }
}
