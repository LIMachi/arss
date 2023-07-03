package com.limachi.arss;

import com.limachi.arss.blocks.*;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.arss.blocks.redstone_wires.RedstoneWireFactory;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.ModBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(Arss.MOD_ID)
public class Arss extends ModBase
{
    public static final KeyMapController.GlobalKeyBinding SCROLL_KEY = KeyMapController.registerKeyBind("key.hold_to_scroll", 340, "key.categories.arss");

    public static final String MOD_ID = "arss";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> DIM_BAG_TAB = CREATIVE_MODE_TABS.register(MOD_ID, () -> CreativeModeTab.builder()
            .title(Component.translatable("creative_tab." + MOD_ID))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS, CreativeModeTabs.INGREDIENTS)
            .icon(Items.COMPARATOR::getDefaultInstance)
            .displayItems((parameters, output) -> {
                RedstoneWireFactory.iter().forEachRemaining(c->output.accept(c.getValue().getFirst().get()));
                DiodeBlockFactory.iter().forEachRemaining(c->output.accept(c.getValue().getFirst().get()));
                output.accept(AnalogButtonBlock.R_ITEM.get());
                output.accept(AnalogJukeboxBlock.R_ITEM.get());
                output.accept(AnalogLeverBlock.R_ITEM.get());
                output.accept(AnalogNoteBlock.R_ITEM.get());
                output.accept(AnalogRedstoneBlock.R_ITEM.get());
                output.accept(AnalogRedstoneLampBlock.R_ITEM.get());
                output.accept(AnalogRedstoneTorchBlock.R_ITEM.get());
            }).build());

    public Arss() {
        super(MOD_ID, "Analog_Redstone_Suite", null);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        CREATIVE_MODE_TABS.register(bus);
    }

    public static Arss getInstance() { return (Arss)INSTANCES.get(MOD_ID); }
}
