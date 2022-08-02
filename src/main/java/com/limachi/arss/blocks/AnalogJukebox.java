package com.limachi.arss.blocks;

import com.limachi.arss.Registries;
import com.limachi.arss.blockEntities.AnalogJukeboxBlockEntity;
import com.limachi.arss.utils.StaticInitializer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.system.NonnullDefault;

@SuppressWarnings({"unused", "deprecation"})
@StaticInitializer.Static
@NonnullDefault
public class AnalogJukebox extends BaseEntityBlock {

    public static final Properties PROPS = Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 6.0F);
    public static final RegistryObject<Block> R_BLOCK = Registries.registerBlockAndItem("analog_jukebox",AnalogJukebox::new).getSecond();
    static {
        Registries.isTranslucent(R_BLOCK);
    }

    public AnalogJukebox() {
        super(PROPS);
    }

    /**
     * since record item have their own logic for rigth click on jukebox, might have to code insertion here
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack recordStack = player.getItemInHand(hand);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AnalogJukeboxBlockEntity) {
            if (recordStack.getItem() instanceof RecordItem) {
                if (((AnalogJukeboxBlockEntity)be).insertRecord(recordStack)) {
                    ItemStack out = recordStack.copy();
                    out.shrink(1);
                    player.setItemInHand(hand, out);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else {
                ((AnalogJukeboxBlockEntity)be).dropRecord();
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55045_, boolean p_55046_) {
        int power = level.getBestNeighborSignal(pos);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AnalogJukeboxBlockEntity && power != ((AnalogJukeboxBlockEntity)be).playing())
            ((AnalogJukeboxBlockEntity)be).play(power);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state1, boolean bool) {
        if (!state.is(state1.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AnalogJukeboxBlockEntity)
                ((AnalogJukeboxBlockEntity)be).dropAllRecords();
            super.onRemove(state, level, pos, state1, bool);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState p_54296_) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnalogJukeboxBlockEntity(pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AnalogJukeboxBlockEntity)
            return ((AnalogJukeboxBlockEntity)be).getAnalogOutputSignal();
        return 0;
    }
}
