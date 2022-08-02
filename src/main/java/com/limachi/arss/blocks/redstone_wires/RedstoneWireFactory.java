package com.limachi.arss.blocks.redstone_wires;

import com.limachi.arss.Registries;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public abstract class RedstoneWireFactory {

    private static final HashMap<String, Pair<RegistryObject<Item>, RegistryObject<Block>>> REDSTONE_WIRES = new HashMap<>();

    public static Block getBlock(String name) { return REDSTONE_WIRES.get(name).getSecond().get(); }
    public static RegistryObject<Block> getBlockRegister(String name) { return REDSTONE_WIRES.get(name).getSecond(); }
    public static Item getItem(String name) { return REDSTONE_WIRES.get(name).getFirst().get(); }
    public static RegistryObject<Item> getItemRegister(String name) { return REDSTONE_WIRES.get(name).getFirst(); }

    private static final Vec3[] COLORS = Util.make(new Vec3[16], vec -> {
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
        class Product extends BaseRedstoneWire {

            protected Product() { super(bProps, fRange, fMaxRange, fRangeFalloff); }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(fRange);
            }
        }
        Pair<RegistryObject<Item>, RegistryObject<Block>> p = Registries.registerBlockAndItem(fName, Product::new);
        Registries.hasRedstoneTint(p.getSecond());
        Registries.isCutout(p.getSecond());
        REDSTONE_WIRES.put(fName, p);
    }
}
