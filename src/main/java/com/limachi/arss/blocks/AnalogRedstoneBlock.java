package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.Registries;
import com.limachi.arss.Static;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.NonnullDefault;

import static com.limachi.arss.Registries.BLOCK_REGISTER;
import static com.limachi.arss.Registries.ITEM_REGISTER;

@SuppressWarnings({"deprecation", "unused"})
@Static
@NonnullDefault
public class AnalogRedstoneBlock extends PoweredBlock {

    public static final Properties PROPS = BlockBehaviour.Properties.of(Material.METAL, MaterialColor.FIRE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL).isRedstoneConductor((state, get, pos)->false);
    public static final RegistryObject<Block> R_BLOCK = BLOCK_REGISTER.register("analog_redstone_block", AnalogRedstoneBlock::new);
    public static final RegistryObject<Item> R_ITEM = ITEM_REGISTER.register("analog_redstone_block", ()->new BlockItem(R_BLOCK.get(), new Item.Properties().tab(Arss.ITEM_GROUP)));

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    static {
        Registries.setColor(R_BLOCK, AnalogRedstoneBlock::getColor);
    }

    public static int getColor(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        return RedStoneWireBlock.getColorForPower(state.getValue(POWER));
    }

    public AnalogRedstoneBlock() { super(PROPS); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWER);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
        int p = state.getValue(POWER);
        p = p + (player.isShiftKeyDown() ? -1 : 1);
        if (p > 15) p = 0;
        if (p < 0) p = 15;
        level.setBlock(pos, state.setValue(POWER, p), 3);
        player.displayClientMessage(new TextComponent(Integer.toString(p)), true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
