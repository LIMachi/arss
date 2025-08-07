package com.limachi.arss.client.screen;

import com.limachi.arss.client.widgets.PowerSelector;
import com.limachi.arss.common.block_entities.ProgrammableGateBlockEntity;

import com.limachi.lim_lib.client.screens.SimpleScreen;
import com.limachi.lim_lib.client.widgets.GenericDropDown;

import com.limachi.lim_lib.common.annotations.RegisterMsg;
import com.limachi.lim_lib.common.network.IC2SMsg;

import dev.architectury.networking.NetworkManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ProgrammableGateScreen extends SimpleScreen {
    public final int GUI_WIDTH = 210;
    public final int GUI_HEIGHT = 223;
    public final int GRID_LEFT = 20;
    public final int GRID_TOP = 30;

    private final ProgrammableGateBlockEntity be;
    private final Player player = Minecraft.getInstance().player;
    protected final PowerSelector[] layout = new PowerSelector[16*16];
    protected GenericDropDown mode;

    public static void client_open(ProgrammableGateBlockEntity be) {
        if (Minecraft.getInstance().isSameThread())
            Minecraft.getInstance().setScreen(new ProgrammableGateScreen(be));
    }

    public ProgrammableGateScreen(ProgrammableGateBlockEntity be) {
        super(Component.empty());
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        this.be = be;
    }

    protected final ArrayList<String> options = new ArrayList<>();

    //list: custom, memory set, memory reset, adder compare, add, comparator compare, subtract, and, nand, or, nor, xor, xnor, eq, neq, demux 1, demux 2, demux 3, demux 4, <<, >>
    static HashMap<Component, byte[]> presets = new HashMap<>();
    static byte[] preset(BiFunction<Integer, Integer, Integer> solve) {
        byte[] out = new byte[256];
        for (int y = 0; y < 16; ++y)
            for (int x = 0; x < 16; ++x)
                out[x + y * 16] = (byte)Math.clamp(solve.apply(x, y), 0, 16);
        return out;
    }
    static {
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.mem_set"), preset((b, s)->s > 0 ? b : 16));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.mem_reset"), preset((b, s)->s > 0 ? 0 : b > 0 ? b : 16));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.-"), preset((b, s)->b - s));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.<="), preset((b, s)->b <= s ? b : 0));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.+"), preset((b, s)->Math.min(15, b + s)));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.>="), preset((b, s)->b >= s ? b : 0));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.&"), preset((b, s)->b & s));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.|"), preset((b, s)->b | s));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.^"), preset((b, s)->(b ^ s) & 15));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.!&"), preset((b, s)->~(b & s) & 15));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.!|"), preset((b, s)->~(b | s) & 15));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.!^"), preset((b, s)->~(b ^ s) & 15));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.=="),  preset((b, s)-> Objects.equals(b, s) ? b : 0));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.!="),  preset((b, s)-> !Objects.equals(b, s) ? b : 0));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.demux"), preset((b, s)->(b & (1 << s)) & 15));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.<<"), preset((b, s)->(b << s) & 15));
        presets.put(Component.translatable("screen.arss.programmable_gate.preset.>>"), preset((b, s)->(b >> s) & 15));
    }

    @Override
    protected void init() {
        super.init();
        boolean first = layout[0] == null;
        var builder = new GenericDropDown.Builder(leftPos + GUI_WIDTH - 85, topPos + 4, 80, 14).options(Component.translatable("screen.arss.programmable_gate.preset.custom")).options(presets.keySet().toArray(new Component[0])).onSelect(s->{
            var p = presets.get(s.getInner().getMessage());
            if (p != null)
                for (int i = 0; i < 256; ++i)
                    layout[i].value = p[i];
        });
        if (first)
            builder.selected(be.mode);
        mode = builder.build();
        addRenderableWidget(mode);
        for (int y = 0; y < 16; ++y) {
            for (int x = 0; x < 16; ++x) {
                int i = y * 16 + x;
                layout[i] = new PowerSelector(leftPos + GRID_LEFT - 2 + x * 11, topPos + GRID_TOP - 1 + y * 11, Component.empty(), layout[i]);
                if (first) {
                    layout[i].setWidth(10);
                    layout[i].setHeight(10);
                    layout[i].value = be.layout[i];
                    layout[i].unknown = true;
                    layout[i].onChange = b->mode.select(0);
                }
                addRenderableWidget(layout[i]);
            }
        }
    }

    protected boolean sideBars(double x, double y, int offset) {
        if (getFocused() != null && !(getFocused() instanceof PowerSelector))
            return false;

        int sx = (int)Math.round(x - (GRID_LEFT - 2) - leftPos);
        int sy = (int)Math.round(y - (GRID_TOP - 1) - topPos);
        boolean x_in = sx >= 0 && sx < 16 * 11;
        boolean y_in = sy >= 0 && sy < 16 * 11;
        boolean horizontal = (sx < 0 && sx >= -11) || (sx >= 16 * 11 && sx < 17 * 11);
        boolean vertical = (sy < 0 && sy >= -11) || (sy >= 16 * 11 && sy < 17 * 11);
        int col = sx / 11;
        int row = sy / 11;

        if (vertical && x_in) {
            for (int i = 0; i < 16; ++i)
                layout[col + i * 16].applyOffset(offset);// = (byte)Mth.clamp(layout[col + i * 16].value + offset, 0, 16);
            return true;
        } else if (horizontal && y_in) {
            for (int i = 0; i < 16; ++i)
                layout[i + row * 16].applyOffset(offset);// = (byte)Mth.clamp(layout[i + row * 16].value + offset, 0, 16);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double sx, double sy) {
        if (sideBars(x, y, sy > 0 ? 1 : -1)) {
            mode.select(0);
            return true;
        }
        return super.mouseScrolled(x, y, sx, sy);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int offset = 0;
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            offset = 1;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            offset = -1;
        }
        if (offset != 0) {
            if (Screen.hasShiftDown()) {
                offset *= 5;
            } else if (Screen.hasControlDown()) {
                offset *= 15;
            }
            if (sideBars(x, y, offset)) {
                mode.select(0);
                return true;
            }
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public void renderFg(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(font, Component.translatable("screen.arss.programmable_gate.title"), leftPos + 8, topPos + 8, 4210752, false);
        for (int y = 0; y < 16; ++y) {
            guiGraphics.drawString(font, "0123456789ABCDEF?".substring(y, y + 1), leftPos + GRID_LEFT + y * 11, topPos + GRID_TOP - 10, RedStoneWireBlock.getColorForPower(y) | 0xFF000000, false);
            guiGraphics.drawString(font, "0123456789ABCDEF?".substring(y, y + 1), leftPos + GRID_LEFT + y * 11, topPos + GRID_TOP + 16 * 11, RedStoneWireBlock.getColorForPower(y) | 0xFF000000, false);
            guiGraphics.drawString(font, "0123456789ABCDEF?".substring(y, y + 1), leftPos + GRID_LEFT - 10, topPos + GRID_TOP + y * 11, RedStoneWireBlock.getColorForPower(y) | 0xFF000000, false);
            guiGraphics.drawString(font, "0123456789ABCDEF?".substring(y, y + 1), leftPos + GRID_LEFT + 16 * 11, topPos + GRID_TOP + y * 11, RedStoneWireBlock.getColorForPower(y) | 0xFF000000, false);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @RegisterMsg
    public record NewLayoutMsg(BlockPos pos, byte[] layout, int mode) implements IC2SMsg<NewLayoutMsg> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            Level level = ctx.getPlayer().level();
            if (level.getBlockEntity(pos) instanceof ProgrammableGateBlockEntity be && layout.length == 256) {
                System.arraycopy(layout, 0, be.layout, 0, 256);
                be.mode = mode;
                be.setChanged();
                level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 2);
            }
        }
    }

    @Override
    public void closing() {
        byte[] blayout = new byte[256];
        for (int i = 0; i < 256; ++i)
            blayout[i] = (byte)layout[i].value;
        new NewLayoutMsg(be.getBlockPos(), blayout, mode.getSelectedIndex()).sendToServer();
    }
}
