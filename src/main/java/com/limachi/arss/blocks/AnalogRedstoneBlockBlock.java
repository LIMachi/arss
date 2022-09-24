package com.limachi.arss.blocks;

import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import com.limachi.lim_lib.registries.client.ClientRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"unused", "deprecation"})
@ParametersAreNonnullByDefault
public class AnalogRedstoneBlockBlock extends PoweredBlock implements IScrollBlockPowerOutput {

    public static void hasRedstoneTint(RegistryObject<Block> block) {
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, ()->()->{ ClientRegistries.setColor(block, AnalogRedstoneBlockBlock::getColor); return null; });
    }

    public static final Properties PROPS = BlockBehaviour.Properties.of(Material.METAL, MaterialColor.FIRE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL).isRedstoneConductor((state, get, pos)->false);

    @RegisterBlock
    public static RegistryObject<Block> R_BLOCK;

    @StaticInit(Stage.BLOCK)
    public static void setTint() {
        AnalogRedstoneBlockBlock.hasRedstoneTint(R_BLOCK);
    }

    @RegisterBlockItem(jeiInfoKey = "jei.info.analog_redstone_block")
    public static RegistryObject<Item> R_ITEM;

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    @OnlyIn(Dist.CLIENT)
    public static int getColor(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        return RedStoneWireBlock.getColorForPower(state.getValue(POWER));
    }

    public AnalogRedstoneBlockBlock() {
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
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Item held = player.getItemInHand(hand).getItem();
        if (held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.R_ITEM.get()) {
            boolean can_scroll = !state.getValue(ArssBlockStateProperties.CAN_SCROLL);
            level.setBlock(pos, state.setValue(ArssBlockStateProperties.CAN_SCROLL, can_scroll), 3);
            player.displayClientMessage(Component.translatable("display.arss.scrollable_block.can_scroll." + can_scroll), true);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
