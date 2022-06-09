package com.limachi.arss.blocks.diodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import static com.limachi.arss.blocks.diodes.BaseAnalogDiodeBlock.POWER;
import static net.minecraft.world.level.block.DiodeBlock.POWERED;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber
public class ComparatorReplacer {

    public static final RegistryObject<Block> BETTER_COMPARATOR = DiodeBlockFactory.getBlockRegister("better_comparator");

    private static void replaceComparator(LevelAccessor level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.COMPARATOR))
            level.setBlock(pos, BETTER_COMPARATOR.get().defaultBlockState().setValue(FACING, state.getValue(FACING)).setValue(POWERED, state.getValue(POWERED)).setValue(BlockStateProperties.MODE_COMPARATOR, state.getValue(BlockStateProperties.MODE_COMPARATOR)).setValue(POWER, state.getBlock().getSignal(state, level, pos, state.getValue(FACING))), 3);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void replacePlacedComparator(BlockEvent.EntityPlaceEvent event) {
        replaceComparator(event.getWorld(), event.getPos(), event.getPlacedBlock());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void replacePlacedComparators(BlockEvent.EntityMultiPlaceEvent event) {
        LevelAccessor level = event.getWorld();
        for (BlockSnapshot block : event.getReplacedBlockSnapshots())
            replaceComparator(level, block.getPos(), block.getCurrentBlock());
    }
}
