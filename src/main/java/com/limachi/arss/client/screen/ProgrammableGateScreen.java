package com.limachi.arss.client.screen;

import com.limachi.arss.blockEntities.ProgrammableGateBlockEntity;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.render.GuiUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ProgrammableGateScreen extends Screen {
    public final int GUI_WIDTH = 210;
    public final int GUI_HEIGHT = 223;
    public final int GRID_LEFT = 20;
    public final int GRID_TOP = 30;

    private final ProgrammableGateBlockEntity be;
    private final Player player = Minecraft.getInstance().player;
    protected final byte[] layout;
    int left = 0;
    int top = 0;

    public static void client_open(ProgrammableGateBlockEntity be) {
        if (EffectiveSide.get().isClient())
            Minecraft.getInstance().setScreen(new ProgrammableGateScreen(be));
    }

    public ProgrammableGateScreen(ProgrammableGateBlockEntity be) {
        super(Component.empty());
        this.be = be;
        this.layout = be.layout.clone();
    }

    protected int color(byte v) {
        if (v >= 0 && v < 16)
            return RedStoneWireBlock.getColorForPower(v) | 0xFF000000;
        return RedStoneWireBlock.getColorForPower(8) | 0xFF000000;
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getInstance().screen == this)
            renderBackground(gui);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        gui.blitNineSliced(GuiUtils.BACKGROUND_TEXTURE, left, top, GUI_WIDTH, GUI_HEIGHT, 8, 256, 256, 0, 0);
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawString(font, Component.translatable("screen.arss.programmable_gate.title"), left + 8, top + 8, 4210752, false);
        for (int y = 0; y < 16; ++y) {
            gui.drawString(minecraft.font, "0123456789ABCDEF?".substring(y, y + 1), left + GRID_LEFT + y * 11, top + GRID_TOP - 10, color((byte)y), false);
            gui.drawString(minecraft.font, "0123456789ABCDEF?".substring(y, y + 1), left + GRID_LEFT + y * 11, top + GRID_TOP + 16 * 11, color((byte)y), false);
            gui.drawString(minecraft.font, "0123456789ABCDEF?".substring(y, y + 1), left + GRID_LEFT - 10, top + GRID_TOP + y * 11, color((byte)y), false);
            gui.drawString(minecraft.font, "0123456789ABCDEF?".substring(y, y + 1), left + GRID_LEFT + 16 * 11, top + GRID_TOP + y * 11, color((byte)y), false);
            for (int x = 0; x < 16; ++x) {
                byte v = layout[x + y * 16];
                gui.fill(left + GRID_LEFT - 2 + x * 11, top + GRID_TOP - 1 + y * 11, left + GRID_LEFT - 2 + x * 11 + 10, top + GRID_TOP - 1 + y * 11 + 10, color(v));
                gui.drawString(minecraft.font, "0123456789ABCDEF?".substring(v, v + 1), left + GRID_LEFT + x * 11, top + GRID_TOP + y * 11, -1, false);
            }
        }
    }

    protected boolean doClick(double x, double y, int offset) {
        int sx = (int)Math.round(x - (GRID_LEFT - 2) - left);
        int sy = (int)Math.round(y - (GRID_TOP - 1) - top);
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
    public boolean mouseScrolled(double x, double y, double amount) {
        if (doClick(x, y, amount > 0 ? 1 : -1))
            return true;
        return super.mouseScrolled(x, y, amount);
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
    protected void init() {
        super.init();
        left = (width - GUI_WIDTH) / 2;
        top = (height - GUI_HEIGHT) / 2;
        clearWidgets();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @RegisterMsg
    public record NewLayoutMsg(BlockPos pos, byte[] layout) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            if (player.level().getBlockEntity(pos) instanceof ProgrammableGateBlockEntity be && layout.length == 256) {
                System.arraycopy(layout, 0, be.layout, 0, 256);
                be.setChanged();
                player.level().sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 2);
            }
        }
    }

    @Override
    public void onClose() {
        NetworkManager.toServer(new NewLayoutMsg(be.getBlockPos(), layout));
        super.onClose();
    }
}
