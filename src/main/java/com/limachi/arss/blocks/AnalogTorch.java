package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.Registries;
import com.limachi.arss.Static;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.system.NonnullDefault;

import java.util.Random;

import static com.limachi.arss.Registries.BLOCK_REGISTER;
import static com.limachi.arss.Registries.ITEM_REGISTER;

@SuppressWarnings({"deprecation", "unused"})
@Static
@NonnullDefault
public class AnalogTorch extends RedstoneTorchBlock {

    public static final BlockBehaviour.Properties PROPS = BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 7 : 0).sound(SoundType.WOOD);
    public static final RegistryObject<Block> R_BLOCK = BLOCK_REGISTER.register("analog_redstone_torch", AnalogTorch::new);
    public static final RegistryObject<Block> R_WALL_VARIANT = BLOCK_REGISTER.register("analog_redstone_wall_torch", Analog_Wall_Torch::new);
    public static final RegistryObject<Item> R_ITEM = ITEM_REGISTER.register("analog_redstone_torch", ()->new StandingAndWallBlockItem(R_BLOCK.get(), R_WALL_VARIANT.get(), new Item.Properties().tab(Arss.ITEM_GROUP)));

    static {
        Registries.setRenderLayer(R_BLOCK, RenderType.cutout());
        Registries.setRenderLayer(R_WALL_VARIANT, RenderType.cutout());
    }

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public AnalogTorch() {
        super(PROPS);
        registerDefaultState(stateDefinition.any().setValue(LIT, true).setValue(POWER, 15));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rng) {
        boolean flag = hasNeighborSignal(level, pos, state);

        if (state.getValue(LIT)) {
            if (flag)
                level.setBlock(pos, state.setValue(LIT, false), 3);
        } else if (!flag) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(LIT) && Direction.UP != dir ? state.getValue(POWER) : 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
        int i = state.getValue(POWER) + (player.isShiftKeyDown() ? -1 : 1);
        if (i == 16) i = 1;
        if (i == 0) i = 15;
        player.displayClientMessage(new TextComponent(Integer.toString(i)), true);
        if (level instanceof ServerLevel) {
            level.setBlock(pos, state.setValue(POWER, i), 3);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    public static class Analog_Wall_Torch extends RedstoneWallTorchBlock {

        public static final IntegerProperty POWER = BlockStateProperties.POWER;

        public Analog_Wall_Torch() {
            super(PROPS.dropsLike(AnalogTorch.R_BLOCK.get()));
            registerDefaultState(stateDefinition.any().setValue(LIT, true).setValue(POWER, 15));
        }

        @Override
        public String getDescriptionId() { return "block.arss.analog_redstone_torch"; }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(ctx);
            return blockstate == null ? null : defaultBlockState().setValue(FACING, blockstate.getValue(FACING)).setValue(POWER, 15);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(POWER);
        }

        @Override
        public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rng) {
            boolean flag = hasNeighborSignal(level, pos, state);

            if (state.getValue(LIT)) {
                if (flag)
                    level.setBlock(pos, state.setValue(LIT, false), 3);
            } else if (!flag) {
                level.setBlock(pos, state.setValue(LIT, true), 3);
            }
        }

        @Override
        public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
            return state.getValue(LIT) && state.getValue(FACING) != dir ? state.getValue(POWER) : 0;
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
            int i = state.getValue(POWER) + (player.isShiftKeyDown() ? -1 : 1);
            if (i == 16) i = 1;
            if (i == 0) i = 15;
            player.displayClientMessage(new TextComponent(Integer.toString(i)), true);
            if (level instanceof ServerLevel) {
                level.setBlock(pos, state.setValue(POWER, i), 3);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.SUCCESS;
        }
    }
}
