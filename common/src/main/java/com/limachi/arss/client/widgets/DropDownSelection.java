package com.limachi.arss.client.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class DropDownSelection extends AbstractWidget {
    private final Button mainButton;
    private final DropdownList listWidget;
    private final Screen screen;
    private boolean expanded = false;

    public DropDownSelection(Screen screen, int x, int y, int width, List<String> options, Consumer<String> onSelection) {
        super(x, y, width, 20, Component.literal(options.get(0)));
        this.screen = screen;
        mainButton = Button.builder(Component.literal(options.get(0)), btn->toggle()).bounds(x, y, width, 14).build();
        listWidget = new DropdownList(x, y + 20, width, Math.min(5, options.size()) * 20);
        listWidget.setOptions(options, selected -> {
            mainButton.setMessage(Component.literal(selected));
            if (onSelection != null)
                onSelection.accept(selected);
            collapse();
        });
    }

    public void toggle() {
        expanded = !expanded;
        if (expanded)
            screen.setFocused(listWidget);
    }

    public void collapse() { expanded = false; }

    public boolean isExpanded() { return expanded; }

    public String getSelectedValue() { return mainButton.getMessage().getString(); }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        mainButton.render(context, mouseX, mouseY, delta);
        if (expanded) {
            context.pose().pushPose();
            context.pose().translate(0., 0., 100.);
            listWidget.renderWidget(context, mouseX, mouseY, delta);
            context.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!expanded)
            return mainButton.mouseClicked(mouseX, mouseY, button);
        if (mainButton.isMouseOver(mouseX, mouseY)) {
            mainButton.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        if (listWidget.isMouseOver(mouseX, mouseY))
            return listWidget.mouseClicked(mouseX, mouseY, button);
        collapse();
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private class DropdownList extends AbstractSelectionList<DropdownOptionEntry> {
        public DropdownList(int x, int y, int width, int height) {
            super(Minecraft.getInstance(), y, width, height, 10);
            setX(x);
        }

        public void setOptions(List<String> options, Consumer<String> onSelect) {
            clearEntries();
            for (String option : options)
                addEntry(new DropdownOptionEntry(option, onSelect));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(NarratedElementType.TITLE, Component.literal("Dropdown: " + getSelectedValue()));
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
            guiGraphics.fill(getX(), getY(), getRight(), getBottom(), 0xFF000000);
        }
    }

    private class DropdownOptionEntry extends ObjectSelectionList.Entry<DropdownOptionEntry> {
        private final String label;
        private final Consumer<String> onClick;

        public DropdownOptionEntry(String label, Consumer<String> onClick) {
            this.label = label;
            this.onClick = onClick;
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            int color = hovered ? 0xFFFFA0 : 0xFFFFFF;
            context.drawString(Minecraft.getInstance().font, label, x + 5, y + (height - 8) / 2, color, false);
        }

        @Override
        public Component getNarration() { return Component.literal(label); }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            onClick.accept(label);
            return true;
        }
    }
}
