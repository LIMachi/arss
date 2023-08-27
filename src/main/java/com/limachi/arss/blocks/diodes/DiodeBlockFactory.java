package com.limachi.arss.blocks.diodes;

import com.limachi.arss.Arss;
import com.limachi.arss.blockEntities.GenericDiodeBlockEntity;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.lim_lib.Log;
import com.limachi.lim_lib.RedstoneUtils;
import com.limachi.lim_lib.registries.Registries;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.RegistryObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings({"deprecation", "unused"})
public class DiodeBlockFactory {
    @FunctionalInterface
    public interface SignalGenerator {
        int calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface BlockEntityBuilder {
        BlockEntity build(BlockPos pos, BlockState state);
    }

    public static final BlockBehaviour.Properties PROPS = BlockBehaviour.Properties.copy(Blocks.COMPARATOR);
    public static final Item.Properties I_PROPS = new Item.Properties();

    private static final HashMap<String, Pair<RegistryObject<Item>, RegistryObject<Block>>> DIODE_BLOCKS = new HashMap<>();

    public static Iterator<Map.Entry<String, Pair<RegistryObject<Item>, RegistryObject<Block>>>> iter() {
        return DIODE_BLOCKS.entrySet().iterator();
    }

    public static Block getBlock(String name) { return DIODE_BLOCKS.get(name).getSecond().get(); }

    public static RegistryObject<Block> getBlockRegister(String name) { return DIODE_BLOCKS.get(name).getSecond(); }

    public static Item getItem(String name) { return DIODE_BLOCKS.get(name).getFirst().get(); }

    public static RegistryObject<Item> getItemRegister(String name) { return DIODE_BLOCKS.get(name).getFirst(); }

    public static final class Builder {
        private String name = "Must be a valid registry key";
        @Nullable
        private EnumProperty<?> mode = null;
        private SignalGenerator generator = (b, l, p, s)->{
            Log.error("Invalid gate generator for: " + name);
            return 0;
        };
        private BlockBehaviour.Properties blockProperties = PROPS;
        private Item.Properties itemProperties = I_PROPS;
        private int delay = 1;
        private boolean tickOnceAfterUpdate = false;
        private boolean ticking = false;
        private boolean hasPowerTint = false;
        @Nullable
        private BlockEntityBuilder blockEntityBuilder = GenericDiodeBlockEntity::new;
        private final List<Property<?>> extraProperties = new ArrayList<>();

        private boolean canToggleBothSides = false;
        private boolean canToggleInput = false;

        private Builder() {}

        public void finish() {
            create(name, mode, generator, blockProperties, itemProperties, delay, tickOnceAfterUpdate, ticking, hasPowerTint, blockEntityBuilder, canToggleBothSides, canToggleInput, extraProperties);
        }

        public Builder mode(@Nullable EnumProperty<?> mode) { this.mode = mode; return this; }
        public Builder blockProperties(BlockBehaviour.Properties blockProperties) { this.blockProperties = blockProperties; return this; }
        public Builder itemProperties(Item.Properties itemProperties) { this.itemProperties = itemProperties; return this; }
        public Builder delay(int delay) { this.delay = delay; return this; }
        public Builder tickOnceAfterUpdate(boolean state) { tickOnceAfterUpdate = state; return this; }
        public Builder ticking(boolean state) { ticking = state; return this; }
        public Builder hasPowerTint(boolean state) { hasPowerTint = state; return this; }
        public Builder blockEntityBuilder(@Nullable BlockEntityBuilder builder) { this.blockEntityBuilder = builder; return this; }
        public Builder addProperties(Property<?> ... properties) { extraProperties.addAll(List.of(properties)); return this; }
        public Builder addProperties(Collection<Property<?>> properties) { extraProperties.addAll(properties); return this; }
        public Builder canToggleBothSides(boolean state) { canToggleBothSides = state; return this; }
        public Builder canToggleInput(boolean state) { canToggleInput = state; return this; }
    }

    public static Builder builder(@Nonnull String name, @Nonnull SignalGenerator generator) {
        Builder out = new Builder();
        out.name = name;
        out.generator = generator;
        return out;
    }

    private static void create(String fName, EnumProperty<?> fMode, SignalGenerator fGen, BlockBehaviour.Properties props, Item.Properties iProps, int fDelay, boolean fTickOnceAfterUpdate, boolean fIsTicking, boolean hasPowerTint, BlockEntityBuilder beb, boolean canToggleBothSides, boolean canToggleInput, List<Property<?>> extraProps) {
        Supplier<Block> gBlock;

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
                registerDefaultState(builder);
            }

            @Override
            public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flags) {
                super.appendHoverText(stack, level, components, flags);
                Arss.commonHoverText(fName, components);
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(FACING, POWERED, POWER, ArssBlockStateProperties.SIDES);
                if (fMode != null)
                    builder.add(fMode);
                for (Property<?> prop : extraProps)
                    builder.add(prop);
            }

            @Override
            protected int calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state) {
                return fGen.calculateOutputSignal(test, level, pos, state);
            }

            @Override
            protected ArssBlockStateProperties.SideToggling cycleSideStates(ArssBlockStateProperties.SideToggling current, boolean shifting) {
                return current.cycle(!shifting, canToggleBothSides, canToggleInput);
            }
        }

        if (beb == null)
            gBlock = Product::new;
        else {
            class Product2 extends Product implements EntityBlock {

                protected Product2() {}

                public boolean triggerEvent(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, int e1, int e2) {
                    super.triggerEvent(state, level, pos, e1, e2);
                    BlockEntity blockentity = level.getBlockEntity(pos);
                    return blockentity != null && blockentity.triggerEvent(e1, e2);
                }

                @Nullable
                @Override
                public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
                    return beb.build(pos, state);
                }
            }

            gBlock = Product2::new;
        }
        RegistryObject<Block> R_BLOCK = Registries.block(Arss.MOD_ID, fName, gBlock);
        if (hasPowerTint)
            RedstoneUtils.hasRedstoneTint(R_BLOCK);
        RegistryObject<Item> R_ITEM = Registries.item(Arss.MOD_ID, fName, ()->new BlockItem(R_BLOCK.get(), I_PROPS), null, new ArrayList<>(Collections.singleton("automatic")));
        DIODE_BLOCKS.put(fName, new Pair<>(R_ITEM, R_BLOCK));
    }
}
