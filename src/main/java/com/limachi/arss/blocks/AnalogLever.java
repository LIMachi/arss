package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.NonnullDefault;

import static com.limachi.arss.Registries.BLOCK_REGISTER;
import static com.limachi.arss.Registries.ITEM_REGISTER;

@SuppressWarnings({"deprecation", "unused"})
@Mod.EventBusSubscriber
@NonnullDefault
public class AnalogLever extends LeverBlock {

    public static final Properties PROPS = BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD);
    public static final RegistryObject<Block> R_BLOCK = BLOCK_REGISTER.register("analog_lever", AnalogLever::new);
    public static final RegistryObject<Item> R_ITEM = ITEM_REGISTER.register("analog_lever", ()->new BlockItem(R_BLOCK.get(), new Item.Properties().tab(Arss.ITEM_GROUP)));

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public AnalogLever() {
        super(PROPS);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(FACE, AttachFace.WALL).setValue(POWER, 15));
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWERED) ? state.getValue(POWER) : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWERED) && getConnectedDirection(state) == dir ? state.getValue(POWER) : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
    }

    @Override
    public void attack(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        if (player.isShiftKeyDown()) {
            int i = state.getValue(POWER) + 1;
            if (i == 16) i = 1;
            player.displayClientMessage(new TextComponent(Integer.toString(i)), true);
            if (level instanceof ServerLevel)
                level.setBlock(pos, state.setValue(POWER, i), 3);
        }
    }

    @SubscribeEvent
    public static void analogLeversCannotBeMinedWhileSneaking(PlayerEvent.BreakSpeed event) {
        Block b = event.getState().getBlock();
        if (b instanceof AnalogLever && event.getPlayer().isShiftKeyDown())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void analogLeversCannotBeBrokenWhileSneaking(BlockEvent.BreakEvent event) {
        Block b = event.getState().getBlock();
        if (b instanceof AnalogLever && event.getPlayer().isShiftKeyDown())
            event.setCanceled(true);
    }
}
