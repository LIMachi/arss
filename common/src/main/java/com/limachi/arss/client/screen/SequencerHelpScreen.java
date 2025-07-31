package com.limachi.arss.client.screen;

import com.limachi.arss.Arss;

import com.limachi.lim_lib.client.screens.SimpleBackgroundScreen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

@Environment(EnvType.CLIENT)
public class SequencerHelpScreen extends SimpleBackgroundScreen {
    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(Arss.INSTANCE.registries.mod_id, "textures/screen/sequencer_help_screen.png");
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
    public SequencerHelpScreen(SequencerScreen parent) {
        super(parent);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }
    final Button[] buttons = new Button[7];

    public static int parseIntOrDefault(String toParse, int onFail) {
        try {
            return Integer.parseInt(toParse);
        } catch (NumberFormatException ignore) {
            return onFail;
        }
    }

    @Override
    public SequencerScreen parent() {
        return super.parent();
    }

    @Override
    protected void init() {
        super.init();
        if (parent().stillValid()) {
            int bq = Mth.clamp(parseIntOrDefault(Component.translatable("screen.arss.sequencer_help.chapter_count_between_2_and_7").getString(), 2), 2, 7);
            int h = (TEXT_HEIGHT - (bq - 1) * BUTTON_SPACING) / bq;
            for (int bi = 0; bi < bq; ++bi) {
                int finalBi = bi;
                buttons[bi] = Button.builder(Component.translatable("screen.arss.sequencer_help.chapter_button." + bi), b->{
                    for (int i = 0; i < bq; ++i)
                        buttons[i].setFocused(i == finalBi);
                    parent().helpChapter = finalBi;
                }).bounds(leftPos + BUTTON_X, topPos + BUTTON_Y + (h + BUTTON_SPACING) * bi, BUTTON_WIDTH, h).build();
                buttons[bi].setFocused(bi == parent().helpChapter);
                if (bi == parent().helpChapter)
                    setFocused(buttons[bi]);
                addRenderableWidget(buttons[bi]);
            }
        }
    }

    public int lines() { return (TEXT_HEIGHT - TEXT_BORDER * 2) / font.lineHeight; }

    @Override
    public void renderFg(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.renderFg(gui, mouseX, mouseY, partialTick);
        List<FormattedCharSequence> lines = font.split(Component.translatable("screen.arss.sequencer_help.chapter." + parent().helpChapter), TEXT_WIDTH - TEXT_BORDER * 2);
        int scroll = parent().helpScroll[parent().helpChapter];
        int l = lines();
        for (int i = 0; i + scroll < lines.size() && i < l; ++i)
            gui.drawString(font, lines.get(i + scroll), leftPos + TEXT_X + TEXT_BORDER, topPos + TEXT_Y + TEXT_BORDER + font.lineHeight * i, -1, false);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double xd, double dy) {
        if (dy != 0) {
            int lines = font.split(Component.translatable("screen.arss.sequencer_help.chapter." + parent().helpChapter), TEXT_WIDTH - TEXT_BORDER * 2).size();
            int l = lines();
            if (lines > l) {
                parent().helpScroll[parent().helpChapter] = Mth.clamp(parent().helpScroll[parent().helpChapter] + (dy > 0 ? -1 : 1), 0, lines - l);
            } else
                parent().helpScroll[parent().helpChapter] = 0;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (x < leftPos || x > leftPos + imageWidth || y < topPos || y > topPos + imageHeight) {
            onClose();
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public void tick() {
        super.tick();
        if (!parent().stillValid())
            onClose();
    }

    @Override
    protected ResourceLocation backgroundLocation() {
        return BACKGROUND;
    }
}