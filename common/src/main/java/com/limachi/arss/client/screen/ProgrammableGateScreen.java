package com.limachi.arss.client.screen;

import com.limachi.arss.common.block_entities.ProgrammableGateBlockEntity;
import com.limachi.arss.utils.client.screens.SimpleScreen;
import com.limachi.arss.utils.network.IC2SMsg;
import com.limachi.arss.utils.annotations.RegisterMsg;

import dev.architectury.networking.NetworkManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;

import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ProgrammableGateScreen extends SimpleScreen {
    public final int GUI_WIDTH = 210;
    public final int GUI_HEIGHT = 223;
    public final int GRID_LEFT = 20;
    public final int GRID_TOP = 30;

    private final ProgrammableGateBlockEntity be;
    private final Player player = Minecraft.getInstance().player;
    protected final byte[] layout;

    public static void client_open(ProgrammableGateBlockEntity be) {
        if (Minecraft.getInstance().isSameThread())
            Minecraft.getInstance().setScreen(new ProgrammableGateScreen(be));
    }

    public ProgrammableGateScreen(ProgrammableGateBlockEntity be) {
        super(Component.empty());
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        this.be = be;
        this.layout = be.layout.clone();
    }

    protected int color(byte v) {
        if (v >= 0 && v < 16)
            return RedStoneWireBlock.getColorForPower(v) | 0xFF000000;
        return RedStoneWireBlock.getColorForPower(8) | 0xFF000000;
    }

    protected boolean doClick(double x, double y, int offset) {
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
                layout[col + i * 16] = (byte)Mth.clamp(layout[col + i * 16] + offset, 0, 16);
            return true;
        } else if (horizontal && y_in) {
            for (int i = 0; i < 16; ++i)
                layout[i + row * 16] = (byte)Mth.clamp(layout[i + row * 16] + offset, 0, 16);
            return true;
        } else if (x_in && y_in) {
            layout[col + row * 16] = (byte)Mth.clamp(layout[col + row * 16] + offset, 0, 16);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double sx, double sy) {
        if (doClick(x, y, sy > 0 ? 1 : -1))
            return true;
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
            if (doClick(x, y, offset))
                return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public void renderFg(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(font, Component.translatable("screen.arss.programmable_gate.title"), leftPos + 8, topPos + 8, 4210752, false);
        for (int y = 0; y < 16; ++y) {
            guiGraphics.drawString(minecraft.font, "0123456789ABCDEF?".substring(y, y + 1), leftPos + GRID_LEFT + y * 11, topPos + GRID_TOP - 10, color((byte)y), false);
            guiGraphics.drawString(minecraft.font, "0123456789ABCDEF?".substring(y, y + 1), leftPos + GRID_LEFT + y * 11, topPos + GRID_TOP + 16 * 11, color((byte)y), false);
            guiGraphics.drawString(minecraft.font, "0123456789ABCDEF?".substring(y, y + 1), leftPos + GRID_LEFT - 10, topPos + GRID_TOP + y * 11, color((byte)y), false);
            guiGraphics.drawString(minecraft.font, "0123456789ABCDEF?".substring(y, y + 1), leftPos + GRID_LEFT + 16 * 11, topPos + GRID_TOP + y * 11, color((byte)y), false);
            for (int x = 0; x < 16; ++x) {
                byte v = layout[x + y * 16];
                guiGraphics.fill(leftPos + GRID_LEFT - 2 + x * 11, topPos + GRID_TOP - 1 + y * 11, leftPos + GRID_LEFT - 2 + x * 11 + 10, topPos + GRID_TOP - 1 + y * 11 + 10, color(v));
                guiGraphics.drawString(minecraft.font, "0123456789ABCDEF?".substring(v, v + 1), leftPos + GRID_LEFT + x * 11, topPos + GRID_TOP + y * 11, -1, false);
            }
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @RegisterMsg
    public record NewLayoutMsg(BlockPos pos, byte[] layout) implements IC2SMsg<NewLayoutMsg> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            Level level = ctx.getPlayer().level();
            if (level.getBlockEntity(pos) instanceof ProgrammableGateBlockEntity be && layout.length == 256) {
                System.arraycopy(layout, 0, be.layout, 0, 256);
                be.setChanged();
                level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 2);
            }
        }
    }

    @Override
    public void closing() {
        new NewLayoutMsg(be.getBlockPos(), layout).sendToServer();
    }
}
