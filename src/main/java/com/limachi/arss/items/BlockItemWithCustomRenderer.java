package com.limachi.arss.items;

import com.limachi.arss.client.CustomItemStackRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockItemWithCustomRenderer extends BlockItem implements ICustomItemRenderers {

    public final BlockState blockRender;
    public final Supplier<ItemStack> itemRender;

    public BlockItemWithCustomRenderer(Block block, Properties properties, Block blockRender) {
        super(block, properties);
        this.blockRender = blockRender.defaultBlockState();
        itemRender = null;
    }

    public BlockItemWithCustomRenderer(Block block, Properties properties, Item itemRender) {
        super(block, properties);
        blockRender = itemRender instanceof BlockItem bi ? bi.getBlock().defaultBlockState() : null;
        this.itemRender = ()->new ItemStack(itemRender);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CustomItemStackRenderer.getInstance();
            }
        });
    }

    @Override
    public ItemStack itemRenderer() {
        if (itemRender == null) return null;
        return itemRender.get();
    }

    @Override
    public BlockState blockRenderer() { return blockRender; }

    @Override
    public BlockState self() { return getBlock().defaultBlockState(); }
}
