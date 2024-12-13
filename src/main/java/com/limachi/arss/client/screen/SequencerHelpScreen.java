package com.limachi.arss.client.screen;

import com.limachi.arss.Arss;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SequencerHelpScreen extends Screen {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(Arss.MOD_ID, "textures/screen/sequencer_help_screen.png");
    public static final int WIDTH  = 350;
    public static final int HEIGHT = 200;
    public static final int BUTTON_X = 5;
    public static final int BUTTON_Y = 6;
    public static final int BUTTON_WIDTH = 140;
    public static final int BUTTON_SPACING = 5;
    public static final int TEXT_X = 148;
    public static final int TEXT_Y = 6;
    public static final int TEXT_WIDTH = 197;
    public static final int TEXT_HEIGHT = 188;
    public static final int TEXT_BORDER = 2;

    protected final SequencerScreen parent;
    public SequencerHelpScreen(SequencerScreen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    protected int top = 0;
    protected int left = 0;
    final Button[] buttons = new Button[7];

    public static int parseIntOrDefault(String toParse, int onFail) {
        try {
            return Integer.parseInt(toParse);
        } catch (NumberFormatException ignore) {
            return onFail;
        }
    }

    @Override
    protected void init() {
        super.init();
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        clearWidgets();
        if (parent.stillValid()) {
            int bq = Mth.clamp(parseIntOrDefault(Component.translatable("screen.arss.sequencer_help.chapter_count_between_2_and_7").getString(), 2), 2, 7);
            int h = (TEXT_HEIGHT - (bq - 1) * BUTTON_SPACING) / bq;
            for (int bi = 0; bi < bq; ++bi) {
                int finalBi = bi;
                buttons[bi] = Button.builder(Component.translatable("screen.arss.sequencer_help.chapter_button." + bi), b->{
                    for (int i = 0; i < bq; ++i)
                        buttons[i].setFocused(i == finalBi);
                    parent.helpChapter = finalBi;
                }).bounds(left + BUTTON_X, top + BUTTON_Y + (h + BUTTON_SPACING) * bi, BUTTON_WIDTH, h).build();
                buttons[bi].setFocused(bi == parent.helpChapter);
                if (bi == parent.helpChapter)
                    setFocused(buttons[bi]);
                addRenderableWidget(buttons[bi]);
            }
        }
    }

    public int lines() {
        return (TEXT_HEIGHT - TEXT_BORDER * 2) / font.lineHeight;
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        gui.blit(BACKGROUND, left, top, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        List<FormattedCharSequence> lines = font.split(Component.translatable("screen.arss.sequencer_help.chapter." + parent.helpChapter), TEXT_WIDTH - TEXT_BORDER * 2);
        int scroll = parent.helpScroll[parent.helpChapter];
        int l = lines();
        for (int i = 0; i + scroll < lines.size() && i < l; ++i)
            gui.drawString(font, lines.get(i + scroll), left + TEXT_X + TEXT_BORDER, top + TEXT_Y + TEXT_BORDER + font.lineHeight * i, -1, false);
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        if (delta != 0) {
            int lines = font.split(Component.translatable("screen.arss.sequencer_help.chapter." + parent.helpChapter), TEXT_WIDTH - TEXT_BORDER * 2).size();
            int l = lines();
            if (lines > l) {
                parent.helpScroll[parent.helpChapter] = Mth.clamp(parent.helpScroll[parent.helpChapter] + (delta > 0 ? -1 : 1), 0, lines - l);
            } else
                parent.helpScroll[parent.helpChapter] = 0;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (x < left || x > left + WIDTH || y < top || y > top + HEIGHT) {
            onClose();
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public void tick() {
        if (!parent.stillValid())
            onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
