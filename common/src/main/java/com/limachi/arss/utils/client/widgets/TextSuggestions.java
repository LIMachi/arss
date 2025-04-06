package com.limachi.arss.utils.client.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class TextSuggestions extends AbstractWidget implements Renderable {

    protected List<Closeness> suggestions;
    protected int visibleSuggestions;
    protected final TextEditor parent;

    protected int offset = 0;
    protected int cursor = 0;

    public TextSuggestions(TextEditor parent, int visibleSuggestions, Collection<String> suggestions) {
        super(
                parent.getX() + (parent.isBordered() ? 4 : 0),
                parent.getY() + parent.getHeight() + 1,
                parent.getInnerWidth(),
                parent.font.lineHeight * visibleSuggestions + 2,
                Component.empty());
        this.visibleSuggestions = visibleSuggestions;
        this.parent = parent;
        parent.setSuggestions(this);
        updateSuggestions(suggestions);
        parent.setResponder(this::responder);
        parent.setFormatter(this::formatter);
    }

    //red for character that would be destroyed
    //grey for missing characters
    //yellow for valid characters
    //white for current text if not hover
    protected FormattedCharSequence formatter(String text, int i) { //FIXME: missing color formatter
        if (!isHovered && parent.isActive())
            return FormattedCharSequence.forward(text, Style.EMPTY.withColor(ChatFormatting.WHITE));
        return FormattedCharSequence.forward(text, Style.EMPTY.withColor(ChatFormatting.GRAY));
    }

    protected void responder(String input) {
        if (!suggestions.isEmpty()) {
            String prev = suggestions.get(cursor).getText();
            suggestions.forEach(s -> s.updateScore(input));
            suggestions.sort(null);
            for (int i = 0; i < suggestions.size(); ++i)
                if (suggestions.get(i).getText().equals(prev)) {
                    cursor = i;
                    break;
                }
            //FIXME: missing auto scroll
        }
    }

    public void updateSuggestions(Collection<String> suggestions) {
        this.suggestions = suggestions.stream().map(s->new Closeness(s).updateScore(parent.getValue())).collect(Collectors.toList());
        this.suggestions.sort(null);
        offset = 0;
        cursor = 0;
    }

    protected static class Closeness implements Comparable<Closeness> {

        protected final String text;
        protected int position;
        protected int length;

        public Closeness(String sugggestion) {
            text = sugggestion;
            position = text.length();
            length = 0;
        }

        public String getText() { return text; }

        public Closeness updateScore(String input) {
            String ltext = text.toLowerCase();
            String cmp = input.toLowerCase();
            if (!ltext.equals(cmp)) {
                length = 0;
                int il = cmp.length();
                int tl = ltext.length();
                position = tl;
                for (int i = 0; i < tl; ++i) {
                    int bl = 0;
                    for (int j = 0; j < il && i + j < tl; ++j)
                        if (ltext.charAt(i + j) != cmp.charAt(j)) {
                            if (bl > length) {
                                length = bl;
                                position = i - bl;
                            }
                            bl = 0;
                        }
                        else
                            bl++;
                    if (bl > length) {
                        length = bl;
                        position = i - length;
                    }
                }
            } else {
                length = text.length();
                position = 0;
            }
            return this;
        }

        public int position() { return position; }
        public int length() { return length; }
        public int score() { return -100 * length + position; }

        @Override
        public int compareTo(TextSuggestions.Closeness o) {
            int d = score() - o.score();
            if (d == 0)
                return text.compareTo(o.text);
            return d;
        }
    }

    public int getVisibleSuggestions() {
        if (suggestions == null)
            return 0;
        return Math.clamp(visibleSuggestions, 0, suggestions.size());
    }

    public int getMaxVisibleSuggestions() {
        if (suggestions == null)
            return 0;
        return suggestions.size();
    }

    @Override
    public int getX() {
        return parent.getX() + (parent.isBordered() ? 1 : 0);
    }

    @Override
    public int getY() {
        return parent.getY() + parent.getHeight();
    }

    @Override
    public int getHeight() {
        return getVisibleSuggestions() * parent.font.lineHeight + 4;
    }

    @Override
    public int getWidth() {
        return parent.getWidth() - (parent.isBordered() ? 2 : 0);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return active && visible && !suggestions.isEmpty() && d >= getX() && e >= getY() && d < (getX() + getWidth()) && e < (getY() + getHeight());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!suggestions.isEmpty()) {
            isHovered = isMouseOver(mouseX, mouseY);
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Integer.MIN_VALUE);
            int lines = getVisibleSuggestions();
            if (offset > 0)
                for (int m = 0; m < getWidth(); ++m)
                    if (m % 2 == 0)
                        guiGraphics.fill(getX() + m, getY(), getX() + m + 1, getY() + 1, -1);
            if (offset + lines < getMaxVisibleSuggestions())
                for (int m = 0; m < getWidth(); ++m)
                    if (m % 2 == 0)
                        guiGraphics.fill(getX() + m, getY() + getHeight() - 1, getX() + m + 1, getY() + getHeight(), -1);
            for (int i = 0; i < lines; ++i) {
                String suggestion = suggestions.get(i + offset).getText();
                if (isHovered && mouseY > getY() + 1 + parent.font.lineHeight * i && mouseY < getY() + 1 + parent.font.lineHeight * (i + 1))
                    cursor = i + offset;
                guiGraphics.drawString(parent.font, suggestion, getX() + 2, getY() + 2 + parent.font.lineHeight * i, i + offset == cursor ? -256 : -5592406);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double sx, double sy) {
        if (sy > 0.)
            moveCursor(true);
        else if (sy < 0.)
            moveCursor(false);
        else
            return false;
        return true;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (isHovered && !suggestions.isEmpty()) {
            parent.setValue(suggestions.get(cursor).getText());
            parent.setFocused(false);
            return true;
        }
        return false;
    }

    public void moveCursor(boolean down) {
        int lines = getVisibleSuggestions();
        cursor = Math.clamp(cursor + (down ? 1 : -1), 0, suggestions.size() - 1);
        if (cursor < offset)
            offset = cursor;
        if (cursor >= offset + lines)
            offset = offset + lines - 1;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (!suggestions.isEmpty()) {
            if (i == GLFW.GLFW_KEY_UP || i == GLFW.GLFW_KEY_DOWN) {
                moveCursor(i == GLFW.GLFW_KEY_DOWN);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
