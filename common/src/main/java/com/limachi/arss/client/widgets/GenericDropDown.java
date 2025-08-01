package com.limachi.arss.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GenericDropDown extends AbstractContainerWidget {
    protected int slotHeight;
    protected final ArrayList<OptionWrapper> list = new ArrayList<>();
    protected OptionWrapper selected;
    protected Consumer<OptionWrapper> onSelect;
    protected boolean expanded = false;
    protected int maxVisible;
    protected int scroll = 0;
    protected boolean dragging = false;
    protected int dragY = 0;
    protected int scrollS = 0;
    protected int scrollBarWidth = 10;

    public static class OptionWrapper extends AbstractWidget {
        protected final AbstractWidget inner;
        protected final Consumer<OptionWrapper> onClick;

        public OptionWrapper(AbstractWidget inner, Consumer<OptionWrapper> onClick) {
            super(inner.getX(), inner.getY(), inner.getWidth(), inner.getHeight(), Component.empty());
            this.inner = inner;
            this.onClick = onClick;
        }

        @Override public int getX() { return inner.getX(); }
        @Override public int getY() { return inner.getY(); }
        @Override public int getWidth() { return inner.getWidth(); }
        @Override public int getHeight() { return inner.getHeight(); }
        @Override public void setX(int i) { super.setX(i); inner.setX(i); }
        @Override public void setY(int i) { super.setY(i); inner.setY(i); }
        @Override public void setWidth(int i) { super.setWidth(i); inner.setWidth(i); }
        @Override public void setHeight(int i) { super.setHeight(i); inner.setHeight(i); }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            inner.updateNarration(narrationElementOutput);
        }

        public AbstractWidget getInner() { return inner; }

        public boolean isMouseOver(double x, double y) {
            return active && visible && x >= getX() && y >= getY() && x < getX() + getWidth() && y < getY() + getHeight();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY)) {
                if (onClick != null)
                    onClick.accept(this);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            inner.render(guiGraphics, i, j, f);
        }
    }

    public static class Builder {
        int x;
        int y;
        int width;
        int slotHeight;
        List<AbstractWidget> list = null;
        Consumer<OptionWrapper> onSelect = null;
        int selected = 0;
        int maxVisible = 5;

        public Builder() {
            x = 0;
            y = 0;
            width = 150;
            slotHeight = 16;
        }
        public Builder(int x, int y, int width, int slotHeight) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.slotHeight = slotHeight;
        }
        public Builder(GenericDropDown prev) {
            if (prev != null) {
                x = prev.getX();
                y = prev.getY();
                width = prev.getWidth();
                slotHeight = prev.getHeight();
                list = new ArrayList<>(prev.list.size());
                for (OptionWrapper option : prev.list)
                    list.add(option.getInner());
                onSelect = prev.onSelect;
                selected = prev.getSelectedIndex();
            } else {
                x = 0;
                y = 0;
                width = 150;
                slotHeight = 16;
            }
        }
        public Builder onSelect(Consumer<OptionWrapper> onSelect) { this.onSelect = onSelect; return this; }
        public Builder options(List<?> options) {
            if (list == null)
                list = new ArrayList<>(options.size());
            for (var option : options)
                if (option instanceof AbstractWidget w)
                    list.add(w);
                else if (option instanceof Component c)
                    list.add(new Button.Builder(c, w->{}).build());
                else if (option instanceof String s)
                    list.add(new Button.Builder(Component.literal(s), w->{}).build());
            return this;
        }
        public Builder options(AbstractWidget ... options) {
            if (list == null)
                list = new ArrayList<>(options.length);
            list.addAll(Arrays.asList(options));
            return this;
        }
        public Builder options(String ... options) {
            if (list == null)
                list = new ArrayList<>(options.length);
            for (var option : options)
                list.add(new Button.Builder(Component.literal(option), w->{}).build());
            return this;
        }
        public Builder options(Component ... options) {
            if (list == null)
                list = new ArrayList<>(options.length);
            for (var option : options)
                list.add(new Button.Builder(option, w->{}).build());
            return this;
        }
        public Builder selected(AbstractWidget option) {
            for (int i = 0; i < list.size(); ++i)
                if (list.get(i) == option)
                    selected = i;
            return this;
        }
        public Builder selected(String option) {
            for (int i = 0; i < list.size(); ++i)
                if (list.get(i).getMessage().getString().equals(option))
                    selected = i;
            return this;
        }
        public Builder selected(Component option) {
            for (int i = 0; i < list.size(); ++i)
                if (list.get(i).getMessage().equals(option))
                    selected = i;
            return this;
        }
        public Builder selected(int option) {
            if (option >= 0 && option < list.size())
                selected = option;
            return this;
        }
        public Builder maxVisible(int options) {
            maxVisible = options;
            return this;
        }
        public GenericDropDown build() {
            return new GenericDropDown(x, y, width, slotHeight, list, onSelect, selected, maxVisible);
        }
    }

    protected GenericDropDown(int x, int y, int width, int slotHeight, List<AbstractWidget> list, Consumer<OptionWrapper> onSelect, int selected, int maxVisible) {
        super(x, y, width, slotHeight, Component.empty());
        this.slotHeight = slotHeight;
        for (var w : list)
            this.list.add(new OptionWrapper(w, b->{
                if (expanded) {
                    this.selected = b;
                    setFocused(b);
                    expanded = false;
                    scroll = 0;
                    updateVisibility();
                    if (onSelect != null)
                        onSelect.accept(b);
                } else if (this.selected == b) {
                    setFocused(null);
                    setFocused(true);
                    expanded = true;
                    updateVisibility();
                }
            }));
        this.selected = this.list.get(selected);
        setFocused(this.selected);
        updateVisibility();
    }

    public AbstractWidget getSelected() { return selected; }

    public int getSelectedIndex() {
        for (int i = 0; i < list.size(); ++i)
            if (list.get(i) == selected)
                return i;
        return 0;
    }

    public void select(int option) {
        boolean changed = false;
        if (option >= 0 && option < list.size()) {
            var prev = selected;
            selected = list.get(option);
            changed = prev != selected;
        }
        if (expanded) {
            expanded = false;
            changed = true;
        }
        if (changed)
            updateVisibility();
    }

    public void updateVisibility() {
        if (expanded)
            setHeight(slotHeight
                    * Math.min(list.size(), maxVisible)
            );
        else
            setHeight(slotHeight);
        for (int i = 0; i < list.size(); ++i) {
            var w = list.get(i);
            w.setWidth(width
                    - (expanded && list.size() > maxVisible ? scrollBarWidth : 0)
            );
            w.setHeight(slotHeight);
            w.setX(getX());
            w.setY(getY() + i * slotHeight
                    - scroll
            );
            w.visible = expanded || selected == w;
        }
        if (!expanded && selected != null)
            selected.setY(getY());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        if (expanded) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0., 0., 100.); //make sure the widget are visible, even if they are at the same position as other widgets
//            RenderSystem.enableScissor(getX(), guiGraphics.guiHeight() - getY() - getHeight(), getWidth(), slotHeight * maxVisible);
            for (var w : list)
                w.render(guiGraphics, i, j, f);

//            RenderSystem.disableScissor();
            if (list.size() > maxVisible) {
                int x = getX() + getWidth() - scrollBarWidth;
                int sh = slotHeight * maxVisible;
                guiGraphics.fill(x, getY(), x + scrollBarWidth, getY() + sh, 0xFF555555);
                int h = Math.max((sh * (maxVisible / list.size())), 10);
                int y = sh + (int)((float)(scroll) / (list.size() * slotHeight - sh) * (sh - h));
                guiGraphics.fill(x, y, x + scrollBarWidth, y + h, 0xFFAAAAAA);
            }

            guiGraphics.pose().popPose();
        }
        else if (selected != null)
            selected.render(guiGraphics, i, j, f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dx, double dy) {
        if (expanded && list.size() > maxVisible) {
            int maxScroll = Math.max(0, list.size() * slotHeight - maxVisible * slotHeight);
            scroll = (int)Math.clamp(scroll - dy * slotHeight, 0, maxScroll);
            updateVisibility();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, dx, dy);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expanded && button == 0 && list.size() > maxVisible && mouseX >= getX() + getWidth() - scrollBarWidth && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight()) {
            dragging = true;
            dragY = (int)mouseY;
            scrollS = scroll;
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;
        if (expanded) {
            expanded = false;
            scroll = 0;
            updateVisibility();
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging && button == 0) {
            int sh = slotHeight * maxVisible;
            int max = Math.max(0, list.size() * slotHeight - sh);
            int d = (int)(mouseY - dragY);
            int r = max / (sh - Math.max(sh * (maxVisible / list.size()), 10));
            scroll = Math.clamp(scrollS + d * r, 0, max);
            updateVisibility();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<? extends GuiEventListener> children() { return list; }
}
