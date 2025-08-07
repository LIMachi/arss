package com.limachi.arss.common.blocks.diodes;

import com.limachi.arss.Arss;
import com.limachi.arss.client.ClientDef;
import com.limachi.arss.common.ArssBlockStateProperties;

import com.limachi.arss.common.block_entities.BaseAnalogDiodeBlockEntity;

import com.limachi.lim_lib.client.annotations.FabricLayer;
import com.limachi.lim_lib.client.modCreation.ClientRegistries;
import com.limachi.lim_lib.common.blocks.IAcceptCrouchInteractWithItem;
import com.mojang.datafixers.util.Pair;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.RegistrySupplier;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.limachi.arss.common.ArssBlockStateProperties.*;

@SuppressWarnings("unused")
public class DiodeBlockFactory {

    @FunctionalInterface
    public interface SignalGenerator {
        int calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface BlockEntityBuilder {
        BlockEntity build(BlockPos pos, BlockState state);
    }

    public static final BlockBehaviour.Properties PROPS = BlockBehaviour.Properties.ofFullCopy(Blocks.COMPARATOR);

    private static final HashMap<String, Pair<RegistrySupplier<Item>, RegistrySupplier<Block>>> DIODE_BLOCKS = new HashMap<>();

    public static Iterator<Map.Entry<String, Pair<RegistrySupplier<Item>, RegistrySupplier<Block>>>> iter() {
        return DIODE_BLOCKS.entrySet().iterator();
    }

    public static Block getBlock(String name) { return DIODE_BLOCKS.get(name).getSecond().get(); }

    public static RegistrySupplier<Block> getBlockRegister(String name) { return DIODE_BLOCKS.get(name).getSecond(); }

    public static Item getItem(String name) { return DIODE_BLOCKS.get(name).getFirst().get(); }

    public static RegistrySupplier<Item> getItemRegister(String name) { return DIODE_BLOCKS.get(name).getFirst(); }

    @FunctionalInterface
    public interface UseMethod {
        ItemInteractionResult use(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit);
    }

    @FunctionalInterface
    public interface BlockEventHandler {
        boolean triggerEvent(BlockState state, Level level, BlockPos pos, int var0, int var1);
    }

    public static final class Builder {
        private String name = "Must be a valid registry key";
        private EnumProperty<?> mode = null;
        private SignalGenerator generator = (b, l, p, s)->{
            Arss.INSTANCE.logger.error("Invalid gate generator for: " + name);
            return 0;
        };
        private BlockBehaviour.Properties blockProperties = PROPS;
        private Function<Item.Properties, Item.Properties> itemProperties = p->p;
        private int delay = 1;
        private BaseAnalogDiodeBlock.TickingMode tickingMode = BaseAnalogDiodeBlock.TickingMode.ON_CHANGE;
        private boolean hasPowerTint = false;
        private BlockEntityBuilder blockEntityBuilder = BaseAnalogDiodeBlockEntity::new;
        private final List<Property<?>> extraProperties = new ArrayList<>();
        private BiFunction<Block, Item.Properties, BlockItem> itemProvider = BlockItem::new;

        private boolean canToggleBothSides = false;
        private boolean canToggleInput = false;
        private UseMethod use = null;
        private IAcceptCrouchInteractWithItem override = null;
        private BlockEventHandler blockEventHandler = null;

        private Builder() {}

        public void finish() {
            create(name, mode, generator, blockProperties, itemProperties, delay, tickingMode, hasPowerTint, blockEntityBuilder, canToggleBothSides, canToggleInput, extraProperties, use, itemProvider, override, blockEventHandler);
        }

        public Builder mode(EnumProperty<?> mode) { this.mode = mode; return this; }
        public Builder blockProperties(BlockBehaviour.Properties blockProperties) { this.blockProperties = blockProperties; return this; }
        public Builder itemProperties(Function<Item.Properties, Item.Properties> itemProperties) { this.itemProperties = itemProperties; return this; }
        public Builder delay(int delay) { this.delay = delay; return this; }
        public Builder tickingMode(BaseAnalogDiodeBlock.TickingMode mode) { tickingMode = mode; return this; }
        public Builder hasPowerTint(boolean state) { hasPowerTint = state; return this; }
        public Builder blockEntityBuilder(BlockEntityBuilder builder) { this.blockEntityBuilder = builder; return this; }
        public Builder addProperties(Property<?> ... properties) { extraProperties.addAll(List.of(properties)); return this; }
        public Builder addProperties(Collection<Property<?>> properties) { extraProperties.addAll(properties); return this; }
        public Builder canToggleBothSides(boolean state) { canToggleBothSides = state; return this; }
        public Builder canToggleInput(boolean state) { canToggleInput = state; return this; }
        public Builder catchUse(UseMethod use) { this.use = use; return this; }
        public Builder itemBuilder(BiFunction<Block, Item.Properties, BlockItem> builder) { itemProvider = builder; return this; }
        public Builder blockEventHandler(BlockEventHandler handler) { blockEventHandler = handler; return this; }

        public Builder overrideItemCrouch(IAcceptCrouchInteractWithItem override) { this.override = override; return this; }
    }

    public static Builder builder(String name, SignalGenerator generator) {
        Builder out = new Builder();
        out.name = name;
        out.generator = generator;
        return out;
    }

    private static void create(String fName, EnumProperty<?> fMode, SignalGenerator fGen, BlockBehaviour.Properties props, Function<Item.Properties, Item.Properties> iProps, int fDelay, BaseAnalogDiodeBlock.TickingMode fTickingMode, boolean hasPowerTint, BlockEntityBuilder beb, boolean canToggleBothSides, boolean canToggleInput, List<Property<?>> extraProps, UseMethod use, BiFunction<Block, Item.Properties, BlockItem> itemBuilder, IAcceptCrouchInteractWithItem override, BlockEventHandler blockEventHandler) {
        Supplier<Block> gBlock;

        class Product extends BaseAnalogDiodeBlock {

            final MapCodec<Product> CODEC = simpleCodec(Product::new);

            @Override
            public @NotNull MapCodec<Product> codec() { return CODEC; }

            protected Product() {
                super(props);
                delay = fDelay;
                name = fName;
                modeProp = fMode;
                tickingMode = fTickingMode;
                BlockState builder = stateDefinition.any();
                builder = builder.setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(POWER, 0).setValue(ArssBlockStateProperties.BOOSTED, false);
                registerDefaultState(builder);
            }

            protected Product(BlockBehaviour.Properties discarded) { this(); }

            @Override
            protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
                if (use != null) {
                    ItemInteractionResult res = use.use(stack, state, level, pos, player, hand, hit);
                    if (res.consumesAction() || res == ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION)
                        return res;
                }
                return super.useItemOn(stack, state, level, pos, player, hand, hit);
            }

            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
                super.appendHoverText(stack, ctx, components, flags);
                ClientDef.commonHoverText(fName, components);
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(FACING, POWERED, POWER, SIDES, BOOSTED);
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
            protected SideToggling cycleSideStates(SideToggling current, boolean shifting) {
                return current.cycle(!shifting, canToggleBothSides, canToggleInput);
            }

            @Override
            public boolean overrideCrouchInteraction(ItemStack stack, Player player, BlockState state, BlockPos pos) {
                return override == null ? Arss.isWrench(stack) : override.overrideCrouchInteraction(stack, player, state, pos);
            }

            @Override
            public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int var0, int var1) {
                if (blockEventHandler != null)
                    return blockEventHandler.triggerEvent(state, level, pos, var0, var1);
                return super.triggerEvent(state, level, pos, var0, var1);
            }
        }

        if (beb == null)
            gBlock = Product::new;
        else {
            class Product2 extends Product implements EntityBlock {
                protected Product2() { super(); }
                protected Product2(Properties discarded) { this(); }

                public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int e1, int e2) {
                    super.triggerEvent(state, level, pos, e1, e2);
                    BlockEntity blockentity = level.getBlockEntity(pos);
                    return blockentity != null && blockentity.triggerEvent(e1, e2);
                }

                @Override
                public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return beb.build(pos, state); }
            }

            gBlock = Product2::new;
        }
        RegistrySupplier<Block> R_BLOCK = Arss.INSTANCE.registries.block(fName, gBlock);
        RegistrySupplier<Item> R_ITEM = Arss.INSTANCE.registries.item(fName, p->itemBuilder.apply(R_BLOCK.get(), iProps.apply(p)));
        DIODE_BLOCKS.put(fName, new Pair<>(R_ITEM, R_BLOCK));
        if (hasPowerTint)
            EnvExecutor.runInEnv(Env.CLIENT, ()->()->{
                Arss.INSTANCE.clientRegistries().registerBlockTint((s, g, p, i) -> 0xFF000000 | RedStoneWireBlock.getColorForPower(s.getValue(BlockStateProperties.POWER)), R_BLOCK.getId());
            });
    }

    @FabricLayer("cutout")
    public static Collection<Block> registerCutoutRender() {
        return DIODE_BLOCKS.values().stream().map(e->e.getSecond().get()).collect(Collectors.toSet());
    }
}
