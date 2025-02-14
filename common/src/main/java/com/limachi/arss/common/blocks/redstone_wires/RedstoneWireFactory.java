package com.limachi.arss.common.blocks.redstone_wires;

import com.limachi.arss.client.ClientDef;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.client.annotations.FabricLayer;
import com.mojang.datafixers.util.Pair;

import dev.architectury.registry.registries.RegistrySupplier;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public abstract class RedstoneWireFactory {

    private static final HashMap<String, Pair<RegistrySupplier<Item>, RegistrySupplier<Block>>> REDSTONE_WIRES = new HashMap<>();

    public static Iterator<Map.Entry<String, Pair<RegistrySupplier<Item>, RegistrySupplier<Block>>>> iter() {
        return REDSTONE_WIRES.entrySet().iterator();
    }

    public static Block getBlock(String name) { return REDSTONE_WIRES.get(name).getSecond().get(); }
    public static RegistrySupplier<Block> getBlockRegister(String name) { return REDSTONE_WIRES.get(name).getSecond(); }
    public static Item getItem(String name) { return REDSTONE_WIRES.get(name).getFirst().get(); }
    public static RegistrySupplier<Item> getItemRegister(String name) { return REDSTONE_WIRES.get(name).getFirst(); }

    public static final Vec3[] COLORS = Util.make(new Vec3[16], vec -> {
        for(int i = 0; i <= 15; ++i) {
            double f = (double)i / 15.;
            double r = f * 0.6 + (f > 0. ? 0.4 : 0.3);
            double g = Mth.clamp(f * f * 0.7 - 0.5, 0., 1.);
            double b = Mth.clamp(f * f * 0.6 - 0.7, 0., 1.);
            vec[i] = new Vec3(r, g, b);
        }

    });

    public static int getColor(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        Vec3 vec3 = COLORS[state.getValue(BlockStateProperties.POWER)];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    protected static class PR {
        int power;
        int range;

        PR() {
            power = 0;
            range = 0;
        }

        PR(int power, int range) {
            this.power = power;
            this.range = range;
        }

        PR max(PR against) {
            if (this.power > against.power) return this;
            if (this.power == against.power && this.range > against.range) return this;
            return against;
        }
    }

    public static void create(String fName, BlockBehaviour.Properties bProps, Item.Properties iProps, IntegerProperty fRange, int fMaxRange, int fRangeFalloff) {
        class Product extends /*BaseRedstoneWire*/NewRedstoneWire {
            protected Product() {
                super(bProps, fRange, fMaxRange, fRangeFalloff);
            }

            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
                super.appendHoverText(stack, ctx, components, flags);
                ClientDef.commonHoverText(fName, components);
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(fRange);
            }
        }
        RegistrySupplier<Block> R_BLOCK = ModBase.registries.block(fName, Product::new);
        RegistrySupplier<Item> R_ITEM = ModBase.registries.item(fName, p->new BlockItem(R_BLOCK.get(), p));
        REDSTONE_WIRES.put(fName, new Pair<>(R_ITEM, R_BLOCK));
        EnvExecutor.runInEnv(Env.CLIENT, ()->()->{
            ModBase.ClientModBase.registries.registerBlockTint((s, g, p, i) -> 0xFF000000 | RedStoneWireBlock.getColorForPower(s.getValue(BlockStateProperties.POWER)), R_BLOCK.getId());
        });
    }

    @FabricLayer("cutout")
    public static Collection<Block> registerCutoutRender() {
        return REDSTONE_WIRES.values().stream().map(e->e.getSecond().get()).collect(Collectors.toSet());
    }
}