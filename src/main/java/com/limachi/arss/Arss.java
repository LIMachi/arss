package com.limachi.arss;

import com.limachi.arss.blocks.AnalogRedstoneTorchBlock;
import com.limachi.arss.blocks.IScrollBlockPowerOutput;
import com.limachi.arss.blocks.diodes.BaseAnalogDiodeBlock;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.ModBase;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Arss.MOD_ID)
@Mod(Arss.MOD_ID)
public class Arss extends ModBase {

    public static final KeyMapController.GlobalKeyBinding SCROLL_KEY = KeyMapController.registerKeyBind("key.hold_to_scroll", 340, "key.categories.arss");
    public static final String MOD_ID = "arss";

    public static void commonHoverText(String name, List<Component> components) {
        Component[] subSentences = new Component[5];
        if (Screen.hasShiftDown() || Screen.hasControlDown()) {
            for (int i = 1; i <= 5; ++i) {
                if (i == 3 && SCROLL_KEY.getKeybinding().isUnbound()) {
                    subSentences[i - 1] = Component.translatable("tooltip.help.general.3_alternate", Component.keybind(SCROLL_KEY.getKeybinding().getName()), Component.keybind(KeyMapController.SNEAK.getKeybinding().getName()), Component.keybind(KeyMapController.USE.getKeybinding().getName()));
                    continue;
                }
                subSentences[i - 1] = Component.translatable("tooltip.help.general." + i, Component.keybind(SCROLL_KEY.getKeybinding().getName()), Component.keybind(KeyMapController.SNEAK.getKeybinding().getName()), Component.keybind(KeyMapController.USE.getKeybinding().getName()));
            }
        }
        if (Screen.hasShiftDown()) {
            if (!Screen.hasControlDown())
                components.add(Component.translatable("tooltip.help.press_ctrl_for_help"));
            components.add(Component.translatable("tooltip.help.shift." + name, (Object[])subSentences));
        }
        else
            components.add(Component.translatable("tooltip.help.press_shift_for_help"));
        if (Screen.hasControlDown()) {
            if (Screen.hasShiftDown())
                components.add(Component.empty());
            components.add(Component.translatable("tooltip.help.ctrl." + name, (Object[])subSentences));
        } else if (!Screen.hasShiftDown())
            components.add(Component.translatable("tooltip.help.press_ctrl_for_help"));
    }

    @SubscribeEvent
    public static void acceptSneakUseOfBlockWithItem(PlayerInteractEvent.RightClickBlock event) {
        Item item = event.getEntity().getItemInHand(event.getHand()).getItem();
        if (item == Items.REDSTONE_TORCH || item == AnalogRedstoneTorchBlock.AnalogRedstoneTorchItem.R_ITEM.get()) {
            Block block = event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock();
            if (block instanceof IScrollBlockPowerOutput || block instanceof BaseAnalogDiodeBlock) {
                event.setUseBlock(Event.Result.ALLOW);
                event.setUseItem(Event.Result.DENY);
            }
        }
    }

    public Arss() { super(MOD_ID, "Analog_Redstone_Suite", false, createTab(MOD_ID, MOD_ID, ()->()->Items.COMPARATOR)); }
}
