package com.limachi.arss.blocks.diodes;

import com.limachi.arss.Arss;
import com.limachi.arss.Registries;
import com.limachi.arss.blocks.AnalogRedstoneBlock;
import com.limachi.arss.blocks.redstone_wires.RedstoneWireFactory;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

import static com.limachi.arss.Registries.BLOCK_REGISTER;
import static com.limachi.arss.Registries.ITEM_REGISTER;

@SuppressWarnings({"deprecation", "unused"})
public class DiodeBlockFactory {
    @FunctionalInterface
    interface SignalGenerator {
        BlockState calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    interface BlockEntityBuilder {
        BlockEntity build(BlockPos pos, BlockState state);
    }

    public static final BlockBehaviour.Properties PROPS = BlockBehaviour.Properties.of(Material.DECORATION).instabreak().sound(SoundType.STONE);
    public static final Item.Properties I_PROPS = new Item.Properties().tab(Arss.ITEM_GROUP);

    private static final HashMap<String, Pair<RegistryObject<Item>, RegistryObject<Block>>> DIODE_BLOCKS = new HashMap<>();

    public static Block getBlock(String name) { return DIODE_BLOCKS.get(name).getSecond().get(); }

    public static RegistryObject<Block> getBlockRegister(String name) { return DIODE_BLOCKS.get(name).getSecond(); }

    public static Item getItem(String name) { return DIODE_BLOCKS.get(name).getFirst().get(); }

    public static RegistryObject<Item> getItemRegister(String name) { return DIODE_BLOCKS.get(name).getFirst(); }

    public static void create(String fName, EnumProperty<?> fMode, SignalGenerator fGen, boolean fTickOnceAfterUpdate, boolean fIsTicking, Property<?>... extraProps) { create(fName, fMode, fGen, PROPS, I_PROPS, 1, fTickOnceAfterUpdate, fIsTicking, false, null, extraProps); }

    public static void create(String fName, SignalGenerator fGen, Property<?> ... extraProps) { create(fName, null, fGen, PROPS, I_PROPS, 1, false, false, false, null, extraProps); }

    public static void create(String fName, EnumProperty<?> fMode, SignalGenerator fGen, Property<?> ... extraProps) { create(fName, fMode, fGen, PROPS, I_PROPS, 1, false, false, false, null, extraProps); }

    public static void create(String fName, EnumProperty<?> fMode, SignalGenerator fGen, BlockEntityBuilder beb, Property<?> ... extraProps) { create(fName, fMode, fGen, PROPS, I_PROPS, 1, false, true, false, beb, extraProps); }

    public static void create(String fName, EnumProperty<?> fMode, SignalGenerator fGen, boolean hasPowerTint, Property<?> ... extraProps) { create(fName, fMode, fGen, PROPS, I_PROPS, 1, false, false, hasPowerTint, null, extraProps); }

    public static void create(String fName, EnumProperty<?> fMode, SignalGenerator fGen, BlockBehaviour.Properties props, Item.Properties iProps, int fDelay, boolean fTickOnceAfterUpdate, boolean fIsTicking, boolean hasPowerTint, BlockEntityBuilder beb, Property<?> ... extraProps) {
        Supplier<Block> gBlock;
        if (beb == null) {
            class Product extends BaseAnalogDiodeBlock {

                protected Product() {
                    super(props);
                    delay = fDelay;
                    name = fName;
                    modeProp = fMode;
                    tickOnceAfterUpdate = fTickOnceAfterUpdate;
                    isTicking = fIsTicking;
                    BlockState builder = stateDefinition.any();
                    builder = builder.setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(POWER, 0);
                    //for now let the mode default to a random value, hopefully the first element of the enum
                    registerDefaultState(builder);
                }

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(FACING, POWERED, POWER);
                    if (fMode != null)
                        builder.add(fMode);
                    if (extraProps.length > 0)
                        builder.add(extraProps);
                }

                @Override
                protected BlockState calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state) {
                    return fGen.calculateOutputSignal(test, level, pos, state);
                }
            }

            gBlock = Product::new;
        }
        else {
            class Product extends BaseAnalogDiodeBlock implements EntityBlock {

                protected Product() {
                    super(props);
                    delay = fDelay;
                    name = fName;
                    modeProp = fMode;
                    tickOnceAfterUpdate = fTickOnceAfterUpdate;
                    isTicking = fIsTicking;
                    BlockState builder = stateDefinition.any();
                    builder = builder.setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(POWER, 0);
                    registerDefaultState(builder);
                }

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(FACING, POWERED, POWER);
                    if (fMode != null)
                        builder.add(fMode);
                    if (extraProps.length > 0)
                        builder.add(extraProps);
                }

                @Override
                protected BlockState calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state) {
                    return fGen.calculateOutputSignal(test, level, pos, state);
                }

                public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int e1, int e2) {
                    super.triggerEvent(state, level, pos, e1, e2);
                    BlockEntity blockentity = level.getBlockEntity(pos);
                    return blockentity != null && blockentity.triggerEvent(e1, e2);
                }

                @Nullable
                @Override
                public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
                    return beb.build(pos, state);
                }
            }

            gBlock = Product::new;
        }
        Pair<RegistryObject<Item>, RegistryObject<Block>> p = Registries.registerBlockAndItem(fName, gBlock);
        if (hasPowerTint)
            Registries.setColor(p.getSecond(), RedstoneWireFactory::getColor);
        Registries.setRenderLayer(p.getSecond(), RenderType.cutout());
        DIODE_BLOCKS.put(fName, p);
    }
}
