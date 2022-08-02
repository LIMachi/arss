package com.limachi.arss.blocks;

import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.arss.Registries;
import com.limachi.arss.blocks.scrollSystem.IScrollBlockPowerOutput;
import com.limachi.arss.utils.StaticInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.NonnullDefault;

@SuppressWarnings({"unused", "deprecation"})
@StaticInitializer.Static
@NonnullDefault
public class AnalogRedstoneBlock extends PoweredBlock implements IScrollBlockPowerOutput {

    public static final Properties PROPS = BlockBehaviour.Properties.of(Material.METAL, MaterialColor.FIRE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL).isRedstoneConductor((state, get, pos)->false);
    static {
        RegistryObject<Block> rb = Registries.registerBlockAndItem("analog_redstone_block", AnalogRedstoneBlock::new).getSecond();
        Registries.isTranslucent(rb);
        Registries.hasRedstoneTint(rb);
    }
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    @OnlyIn(Dist.CLIENT)
    public static int getColor(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        return RedStoneWireBlock.getColorForPower(state.getValue(POWER));
    }

    public AnalogRedstoneBlock() {
        super(PROPS);
        registerDefaultState(stateDefinition.any().setValue(POWER, 15).setValue(ArssBlockStateProperties.CAN_SCROLL, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
        builder.add(ArssBlockStateProperties.CAN_SCROLL);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWER);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Item held = player.getItemInHand(hand).getItem();
        if (held == Items.REDSTONE_TORCH || held == AnalogTorch.R_ITEM.get()) {
            boolean can_scroll = !state.getValue(ArssBlockStateProperties.CAN_SCROLL);
            level.setBlock(pos, state.setValue(ArssBlockStateProperties.CAN_SCROLL, can_scroll), 3);
            player.displayClientMessage(new TranslatableComponent("display.arss.scrollable_block.can_scroll." + can_scroll), true);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
