package com.limachi.arss.integration;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.arss.blocks.AnalogNoteBlock;
import com.limachi.arss.blocks.AnalogRedstoneBlock;
import com.limachi.arss.blocks.AnalogRedstoneLampBlock;
import com.limachi.arss.blocks.IScrollBlockPowerOutput;
import com.limachi.arss.blocks.diodes.BaseAnalogDiodeBlock;
import com.mojang.datafixers.util.Pair;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.function.Function;

public class TOPplugin implements Function<ITheOneProbe, Void>, IProbeInfoProvider {
    public static final ResourceLocation ID = new ResourceLocation(Arss.MOD_ID, "top_block");

    @Override
    public Void apply(ITheOneProbe access) {
        access.registerProvider(this);
        return null;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData iProbeHitData) {
        if (state.getBlock() instanceof IScrollBlockPowerOutput) {
            if (!(state.getBlock() instanceof AnalogRedstoneBlock))
                info.text(Component.translatable("top.info.stored_power", state.getValue(BlockStateProperties.POWER).toString()));
            info.text(Component.translatable("top.info." + (state.getValue(ArssBlockStateProperties.CAN_SCROLL) ? "unlocked" : "locked")));
        }
        if (state.getBlock() instanceof BaseAnalogDiodeBlock block) {
            Pair<String, EnumProperty<?>> p = block.instanceType();
            String name = p.getFirst();
            EnumProperty<?> modeProp = p.getSecond();
            IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(2));
            IProbeInfo h = v.horizontal(info.defaultLayoutStyle().spacing(2).alignment(ElementAlignment.ALIGN_TOPLEFT));
            h.item(new ItemStack(Items.REDSTONE), new ItemStyle().height(14).width(14)).text(Component.translatable("top.info.power", state.getValue(BlockStateProperties.POWER).toString()));
            if (modeProp != null)
                v.text(Component.translatable("top.info.mode").append(Component.translatable("display.arss." + name + ".mode." + state.getValue(modeProp))));
        }
        if (state.getBlock() instanceof AnalogNoteBlock) {
            info.text(Component.translatable("top.info.pitch").append(Component.translatable("top.info.pitch." + (state.getValue(ArssBlockStateProperties.HIGH) ? "high" : "low"))));
        }
        if (state.getBlock() instanceof AnalogRedstoneLampBlock) {
            IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(2));
            IProbeInfo h = v.horizontal(info.defaultLayoutStyle().spacing(2).alignment(ElementAlignment.ALIGN_TOPLEFT));
            h.item(new ItemStack(Items.GLOWSTONE_DUST), new ItemStyle().height(14).width(14)).text(Component.translatable("top.info.light_level", state.getValue(BlockStateProperties.POWER).toString()));
        }
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
