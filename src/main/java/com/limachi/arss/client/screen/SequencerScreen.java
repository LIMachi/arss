package com.limachi.arss.client.screen;

import com.google.common.collect.ImmutableList;
import com.limachi.arss.Arss;
import com.limachi.arss.blockEntities.SequencerBlockEntity;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.lim_lib.ClientEvents;
import com.limachi.lim_lib.data.History;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.render.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

//exact working: use client side tile entity as data holder, when relevant changes are made, sync messages should be sent to server

/**
 * controls:
 *OK left click: set head at cursor, reset selection
 *OK drag left click + ctrl: copy current head value to new head position
 *OK drag left click: select area
 *OK (drag) right click: set power under cursor
 *OK (drag) middle click: set length, force head to be lower
 *OK middle click + ctrl: toggle limit
 *OK up/down key: increase/decrease power at head/if selection, increase/decrease all selected powers
 *OK left/right key: move head left/right/if selection, move selection left/right, and move head at start/end of selection
 *OK left/right key + ctrl: copy power when moving head, copy borders of selection instead of clearing to 0
 *OK left/right key + shift: select while moving head
 *OK scroll: move view by 10 ticks
 *OK home: move head/view to 0 (+ctrl: copy power while traveling, +shift: select while traveling)
 *OK end: move head/view to length (+ctrl: copy power while traveling, +shift: select while traveling)
 *OK page up/down: move view by a full screen (50 ticks)
 *OK space: toggle play/pause (override the redstone controls, plays from start to finish, in loop unless the diode is in "play once" mode)
 *OK ctrl + c: copy selection to internal clipboard (if no selection, copy single note) (internal clip board is stored in the screen), clear selection
 *OK ctrl + x: cut selection (copy + clear to 0), clear selection
 *OK ctrl + v: paste from internal clipboard (moving the head in the process), create selection, put head after selection
 * \n
 * block entity working:
 * playing is controlled both by the sides and screen:
 * powers on sides only trigger/stop the playing when they change! (so even when powered on the sides, a sequence can be stopped by the screen)
 * inversely, a loop started by the screen can be stopped by changing the power input
 * finally, input binding can remapped via screen (default: back -> pass-through/recording, left -> start of sequence, right -> end of sequence)
 * \n
 * rendering change: show limit number above cut instead of on the sides, show limits on the sides with a < or > to show that there is a limit somewhere, show limit 1 and 15 on tick 0 and tick length
 */

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class SequencerScreen extends Screen {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Arss.MOD_ID, "textures/screen/sequencer_screen.png");
    public static final int IMAGE_WIDTH = 196;
    public static final int SCREEN_WIDTH = 193;
    public static final int IMAGE_HEIGHT = 166;
    public static final int WIDGET_X_OFFSET = 193;
    public static final int SELECTED_NODE_Y_OFFSET = 0;
    public static final int NODE_Y_OFFSET = 3;
    public static final int LINE_Y_OFFSET = 6;
    public static final int HOVERED_NODE_Y_OFFSET = 9;
    public static final int SEQUENCER_HEAD_Y_OFFSET = 12;
    public static final int SEQUENCER_WIDGET_X_OFFSET = 20;
    public static final int SEQUENCER_WIDGET_Y_OFFSET = 30;
    public static final int SEQUENCER_WIDGET_SIZE_X = 153;
    public static final int SEQUENCER_WIDGET_SIZE_Y = 48;
    public static final int MAXIMUM_VISIBLE_SECTIONS = 51;
    public static final int SECTION_OFFSET_ALIGNMENT = 10;
    public static final int SECTION_MARGIN = 10;
    public static final int SECTION_JUMP_ON_MARGIN = 20;
    public static final int SCROLL_JUMP = 10;
    public static final int PAGE_JUMP = 50;
    private int section_offset = 0;
    protected int helpChapter = 0;
    protected int[] helpScroll = new int[7];

    protected int top = 0;
    protected int left = 0;
    protected int selection = -1; //head is always on the limits of the selection (start inclusive, end exclusive)
    protected ArrayList<Integer> clipBoard = null; //copy/cut/paste clipboard
    private final SequencerBlockEntity be;
    private final Player player = Minecraft.getInstance().player;
    private final History<Pair<Integer, Integer>> history = new History<>(new Pair<>(600, 0));

    protected ArrayList<Integer> sections() {
        if (stillValid())
            return be.getTicks();
        return new ArrayList<>(); //should not be called that often, since on next tick with invalid state, this screen should close itself
    }

    protected boolean isHeadAutomatic() {
        return stillValid() && (be.isPlaying() || be.isRecording());
    }

    protected int head() {
        if (stillValid())
            return be.getHead();
        return 0;
    }

    protected int power() {
        if (stillValid())
            return be.readPower();
        return 0;
    }

    protected int length() {
        if (stillValid())
            return be.getLength();
        return 0;
    }

    @RegisterMsg
    public record SetLengthMsg(BlockPos pos, int len) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            if (player.level().getBlockEntity(pos) instanceof SequencerBlockEntity be)
                be.setLength(len);
        }
    }

    protected void setLength(int len) {
        if (stillValid()) {
            NetworkManager.toServer(new SetLengthMsg(be.getBlockPos(), len));
            be.setLength(len);
        }
    }

    @RegisterMsg
    public record SetSectionMsg(BlockPos pos, int section, int power) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            if (player.level().getBlockEntity(pos) instanceof SequencerBlockEntity be)
                be.setTick(section, power);
        }
    }

    protected void setSection(int section, int power) {
        if (stillValid()) {
            int prevPower = be.getTicks().get(section);
            if (power != prevPower) {
                history.write(new Pair<>(section, be.getTicks().get(section)));
                history.write(new Pair<>(section, power));
                NetworkManager.toServer(new SetSectionMsg(be.getBlockPos(), section, power));
                be.setTick(section, power);
            }
        }
    }

    protected void historyLoad(boolean redo) {
        if (stillValid()) {
            for (int i = 0; i < 6; ++i) {
                Pair<Integer, Integer> page = history.read(redo ? -1 : 1);
                int section = page.getFirst();
                int power = page.getSecond();
                if (section >= 0 && section < be.getTicks().size() && power >= 0 && power <= 15 && be.getTicks().get(section) != power) {
                    NetworkManager.toServer(new SetSectionMsg(be.getBlockPos(), section, power));
                    be.setTick(section, power);
                }
            }
        }
    }

    @RegisterMsg
    public record SetHeadMsg(BlockPos pos, int at) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            if (player.level().getBlockEntity(pos) instanceof SequencerBlockEntity be)
                be.setHead(at);
        }
    }

    protected void setHead(int at) {
        if (stillValid() /*&& !be.isPlaying() && !be.isRecording()*/) {
            NetworkManager.toServer(new SetHeadMsg(be.getBlockPos(), at));
            be.setHead(at);
        }
    }

    private final boolean boosted;

    public static void client_open(SequencerBlockEntity be) {
        if (EffectiveSide.get().isClient())
            Minecraft.getInstance().setScreen(new SequencerScreen(be));
    }

    public SequencerScreen(SequencerBlockEntity be) {
        super(Component.empty());
        this.be = be;
        boosted = be != null && be.getBlockState().getValue(ArssBlockStateProperties.BOOSTED);
    }

    protected void ensureHeadIsVisible() {
        int head = head();
        if (head <= section_offset + SECTION_MARGIN)
            section_offset = head - SECTION_JUMP_ON_MARGIN;
        if (head > section_offset + MAXIMUM_VISIBLE_SECTIONS - SECTION_MARGIN)
            section_offset = head + SECTION_MARGIN - MAXIMUM_VISIBLE_SECTIONS;
        ensureOffsetValid();
    }

    protected void ensureOffsetValid() {
        if (section_offset + MAXIMUM_VISIBLE_SECTIONS >= SequencerBlockEntity.MAXIMUM_TICK_COUNT)
            section_offset = SequencerBlockEntity.MAXIMUM_TICK_COUNT - MAXIMUM_VISIBLE_SECTIONS;
        if (section_offset % SECTION_OFFSET_ALIGNMENT != 0)
            section_offset += SECTION_OFFSET_ALIGNMENT - section_offset % SECTION_OFFSET_ALIGNMENT;
        if (section_offset < 0)
            section_offset = 0;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        int rk = key;
        int head = head();
        int length = length();
        ArrayList<Integer> sections = sections();
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        switch (rk) {
            case GLFW.GLFW_KEY_Z -> {
                if (ctrl) {
                    historyLoad(false);
                }
            }
            case GLFW.GLFW_KEY_Y -> {
                if (ctrl) {
                    historyLoad(true);
                }
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (selection != -1) {
                    selection = -1;
                    return true;
                }
            }
            case GLFW.GLFW_KEY_C -> {
                if (ctrl && selection != -1) {
                    clipBoard = new ArrayList<>();
                    int l = Integer.max(head, selection);
                    for (int i = Integer.min(head, selection); i <= l; ++i)
                        clipBoard.add(sections.get(i));
                    selection = -1;
                    return true;
                }
            }
            case GLFW.GLFW_KEY_X -> {
                if (ctrl && selection != -1) {
                    clipBoard = new ArrayList<>();
                    int l = Integer.max(head, selection);
                    for (int i = Integer.min(head, selection); i <= l; ++i) {
                        clipBoard.add(sections.get(i));
                        sections.set(i, 0);
                    }
                    selection = -1;
                    return true;
                }
            }
            case GLFW.GLFW_KEY_V -> {
                if (ctrl && clipBoard != null) {
                    int i = 0;
                    for (; i < clipBoard.size() && i + head < sections.size(); ++i)
                        setSection(i + head, clipBoard.get(i));
                    setHead(head + i < length ? head + i : length - 1);
                    selection = -1;
                    return true;
                }
            }
            case GLFW.GLFW_KEY_SPACE -> {
                if (stillValid())
                    be.toggleTestPlay();
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (head > 0 && head < sections.size()) {
                    if (selection == -1 || shift)
                        setHead(head - 1);
                    else {
                        int l = Integer.max(head, selection);
                        for (int i = Integer.max(0, Integer.min(head, selection) - 1); i < l; ++i)
                            setSection(i, sections.get(i + 1));
                        selection = Integer.max(0, selection - 1);
                        if (!ctrl)
                            setSection(l, 0);
                        setHead(head - 1);
                    }
                    if (shift && selection == -1)
                        selection = head;
                    ensureHeadIsVisible();
                    if (ctrl)
                        setSection(head - 1, sections.get(head));
                    return true;
                }
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (head >= 0 && head < sections.size() - 1) {
                    if (selection == -1 || shift)
                        setHead(head + 1);
                    else {
                        int l = Integer.min(head, selection);
                        for (int i = Integer.max(head, selection) + 1; i > l; --i)
                            setSection(i, sections.get(i - 1));
                        selection = Integer.min(length - 1, selection + 1);
                        if (!ctrl)
                            setSection(l, 0);
                        setHead(head + 1);
                    }
                    if (shift && selection == -1)
                        selection = head;
                    ensureHeadIsVisible();
                    if (ctrl)
                        setSection(head + 1, sections.get(head));
                    return true;
                }
            }
            case GLFW.GLFW_KEY_UP -> {
                if (head >= 0 && head < sections.size()) {
                    if (selection == -1) {
                        if (ctrl) {
                            setSection(head, 15);
                        } else {
                            setSection(head, sections.get(head) + (shift ? 5 : 1));
                        }
                    }
                    else {
                        int l = Integer.max(selection, head);
                        for (int i = Integer.min(selection, head); i <= l; ++i)
                            if (ctrl) {
                                setSection(i, 15);
                            } else {
                                setSection(i, sections.get(i) + (shift ? 5 : 1));
                            }
                    }
                }
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (head >= 0 && head < sections.size()) {
                    if (selection == -1) {
                        if (ctrl) {
                            setSection(head, 0);
                        } else {
                            setSection(head, sections.get(head) - (shift ? 5 : 1));
                        }
                    }
                    else {
                        int l = Integer.max(selection, head);
                        for (int i = Integer.min(selection, head); i <= l; ++i)
                            if (ctrl) {
                                setSection(i, 0);
                            } else {
                                setSection(i, sections.get(i) - (shift ? 5 : 1));
                            }
                    }
                }
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0)
                    for (int i = head - 1; i >= 0; --i)
                        setSection(i, sections.get(head));
                if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0)
                    selection = head;
                else
                    selection = -1;
                section_offset = 0;
                setHead(0);
                ensureHeadIsVisible();
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                if ((modifiers & (GLFW.GLFW_MOD_CONTROL)) != 0)
                    for (int i = head + 1; i < length; ++i)
                        setSection(i, sections.get(head));
                if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0)
                    selection = head;
                else
                    selection = -1;
                setHead(length - 1);
                ensureHeadIsVisible();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_UP -> {
                section_offset += PAGE_JUMP;
                ensureOffsetValid();
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                section_offset -= PAGE_JUMP;
                ensureOffsetValid();
                return true;
            }
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char letter, int modifiers) {
        return super.charTyped(letter, modifiers);
    }

    @RegisterMsg
    public record ChangeMappingMsg(BlockPos pos, String key, String value) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            if (player.level().getBlockEntity(pos) instanceof SequencerBlockEntity be && (key.equals("record") || key.equals("start") || key.equals("finish"))) {
                HashMap<String, String> map = be.getMappings();
                map.put(key, value);
                be.setChanged();
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        left = (width - SCREEN_WIDTH) / 2;
        top = (height - IMAGE_HEIGHT) / 2;
        clearWidgets();
        if (stillValid()) {
            addRenderableWidget(CycleButton.builder(t -> Component.translatable("screen.arss.sequencer.input_side." + t)).withValues(ImmutableList.of("back", "right", "left", "disabled")).withInitialValue(be.getMappings().get("record")).create(left + 10, top + IMAGE_HEIGHT - 45, SCREEN_WIDTH - 20, 16, Component.translatable("screen.arss.sequencer.input_mapping.record_pass_through"), (b, v) -> {
                if (stillValid()) {
                    be.getMappings().put("record", (String)v);
                    NetworkManager.toServer(new ChangeMappingMsg(be.getBlockPos(), "record", (String)v));
                }
                b.setFocused(false);
                setFocused(null);
            }));
            addRenderableWidget(CycleButton.builder(t -> Component.translatable("screen.arss.sequencer.input_side." + t)).withValues(ImmutableList.of("back", "right", "left", "disabled")).withInitialValue(be.getMappings().get("start")).create(left + 10, top + IMAGE_HEIGHT - 25, (SCREEN_WIDTH - 24) / 2, 16, Component.translatable("screen.arss.sequencer.input_mapping.start"), (b, v) -> {
                if (stillValid()) {
                    be.getMappings().put("start", (String)v);
                    NetworkManager.toServer(new ChangeMappingMsg(be.getBlockPos(), "start", (String)v));
                }
                b.setFocused(false);
                setFocused(null);
            }));
            addRenderableWidget(CycleButton.builder(t -> Component.translatable("screen.arss.sequencer.input_side." + t)).withValues(ImmutableList.of("back", "right", "left", "disabled")).withInitialValue(be.getMappings().get("finish")).create(left + SCREEN_WIDTH - (SCREEN_WIDTH - 24) / 2 - 12, top + IMAGE_HEIGHT - 25, (SCREEN_WIDTH - 20) / 2, 16, Component.translatable("screen.arss.sequencer.input_mapping.finish"), (b, v) -> {
                if (stillValid()) {
                    be.getMappings().put("finish", (String)v);
                    NetworkManager.toServer(new ChangeMappingMsg(be.getBlockPos(), "finish", (String)v));
                }
                b.setFocused(false);
                setFocused(null);
            }));
        }
    }

    private int clickTick = 0;

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int sx = (int)Math.round(x - SEQUENCER_WIDGET_X_OFFSET - left);
        int sy = (int)Math.round(y - SEQUENCER_WIDGET_Y_OFFSET - top);
        if (sx >= 0 && sx < SEQUENCER_WIDGET_SIZE_X && sy >= 0 && sy < SEQUENCER_WIDGET_SIZE_Y) {
            clickTick = ClientEvents.tick;
            return true;
        }
        int mouseX = (int)Math.round(x - left);
        int mouseY = (int)Math.round(y - top);
        if (mouseX >= 172 && mouseX < 189 && mouseY >= 4 && mouseY < 20) {
            Minecraft.getInstance().pushGuiLayer(new SequencerHelpScreen(this));
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        int sx = (int)Math.round(x - SEQUENCER_WIDGET_X_OFFSET - left);
        int sy = (int)Math.round(y - SEQUENCER_WIDGET_Y_OFFSET - top);
        if (sx >= 0 && sx < SEQUENCER_WIDGET_SIZE_X && sy >= 0 && sy < SEQUENCER_WIDGET_SIZE_Y) {
            int column = sx / 3 + section_offset;
            int row = 15 - (sy / 3);
            switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_1 -> {
                    if (ClientEvents.tick - clickTick < 3) {
                        int power = power();
                        if (Screen.hasShiftDown())
                            selection = head();
                        else
                            selection = -1;
                        setHead(column);
                        if (Screen.hasControlDown())
                            setSection(head(), power);
                    }
                }
                case GLFW.GLFW_MOUSE_BUTTON_2 -> {
                    setSection(column, row);
                }
                case GLFW.GLFW_MOUSE_BUTTON_3 -> {
                    if (Screen.hasControlDown()) {
                        if (stillValid())
                            be.toggleLimit(column, 1);
                    }
                    else
                        setLength(column);
                }
            }
            return true;
        }
        return super.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        int sx = (int)Math.round(x - SEQUENCER_WIDGET_X_OFFSET - left);
        int sy = (int)Math.round(y - SEQUENCER_WIDGET_Y_OFFSET - top);
        if (sx >= 0 && sx < SEQUENCER_WIDGET_SIZE_X && sy >= 0 && sy < SEQUENCER_WIDGET_SIZE_Y) {
            section_offset += amount > 0 ? -SCROLL_JUMP : SCROLL_JUMP;
            ensureOffsetValid();
            return true;
        }
        return super.mouseScrolled(x, y, amount);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double px, double py) {
        int sx = (int)Math.round(x - SEQUENCER_WIDGET_X_OFFSET - left);
        int sy = (int)Math.round(y - SEQUENCER_WIDGET_Y_OFFSET - top);
        if (sx >= 0 && sx < SEQUENCER_WIDGET_SIZE_X && sy >= 0 && sy < SEQUENCER_WIDGET_SIZE_Y) {
            int column = sx / 3 + section_offset;
            switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_1 -> {
                    if (Screen.hasControlDown()) {
                        selection = -1;
                        int power = power();
                        setHead(column);
                        setSection(head(), power);
                    } else {
                        int head = head();
                        if (selection == -1 && Screen.hasShiftDown() && column < length())
                            selection = column;
                        setHead(column);
                    }
                }
                case GLFW.GLFW_MOUSE_BUTTON_2 -> {
                    setSection(column, 15 - (sy / 3));
                }
                case GLFW.GLFW_MOUSE_BUTTON_3 -> {
                    if (!Screen.hasControlDown())
                        setLength(column);
                }
            }
            return true;
        }
        return false;
    }

    protected void renderSections(@Nonnull GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, Component.translatable("screen.arss.sequencer.title", Component.translatable("display.arss.sequencer.mode." + be.getBlockState().getValue(ArssBlockStateProperties.SEQUENCER_MODE))), left + 8, top + 8, 4210752, false);
        gui.drawString(font, Component.translatable("screen.arss.sequencer.help_button"), left + 179, top + 8, 0x99FF, false);
        if (isHeadAutomatic())
            ensureHeadIsVisible();
        int head = head();
        int length = length();
        ArrayList<Integer> limits = stillValid() ? be.getLimits() : new ArrayList<>();
        RenderSystem.setShaderColor(0, 1, 0, 1);
        for (int i = 0; i < limits.size(); ++i) {
            int l = limits.get(i);
            if (l > 0 && l >= section_offset && l < section_offset + MAXIMUM_VISIBLE_SECTIONS) {
                int j = (l - section_offset) * 3 + SEQUENCER_WIDGET_X_OFFSET + left;
                gui.blit(BACKGROUND, j - 1, top + SEQUENCER_WIDGET_Y_OFFSET, SCREEN_WIDTH, SEQUENCER_HEAD_Y_OFFSET, 3, SEQUENCER_WIDGET_SIZE_Y, IMAGE_WIDTH, IMAGE_HEIGHT);
                gui.drawCenteredString(font, "" + (i + 2), j + 1, top + 18, -1);
            }
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        ArrayList<Integer> sections = sections();
        for (int i = 0; i < MAXIMUM_VISIBLE_SECTIONS; ++i) {
            if (length == i + section_offset) {
                RenderSystem.setShaderColor(1f/3f, 1f/3f, 1f/3f, 1);
                int j = left + SEQUENCER_WIDGET_X_OFFSET + i * 3;
                gui.blit(BACKGROUND, j - 1, top + SEQUENCER_WIDGET_Y_OFFSET, SCREEN_WIDTH, SEQUENCER_HEAD_Y_OFFSET, 3, SEQUENCER_WIDGET_SIZE_Y, IMAGE_WIDTH, IMAGE_HEIGHT);
                RenderSystem.setShaderColor(0, 1, 0, 1);
                gui.drawCenteredString(font, "15", j, top + 18, -1);
            } else if (head == i + section_offset) {
                RenderSystem.setShaderColor(0, 1, 1, 1);
                gui.blit(BACKGROUND, left + SEQUENCER_WIDGET_X_OFFSET + i * 3, top + SEQUENCER_WIDGET_Y_OFFSET, SCREEN_WIDTH, SEQUENCER_HEAD_Y_OFFSET, 3, SEQUENCER_WIDGET_SIZE_Y, IMAGE_WIDTH, IMAGE_HEIGHT);
            }
            if (i + section_offset == 0 && length > 0) {
                RenderSystem.setShaderColor(0, 1, 0, 1);
                gui.drawCenteredString(font, "1", left + 21, top + 18, -1);
            }
            int j = i + section_offset;
            if (j < sections.size()) {
                int lp = sections.get(j);
                int offset = j == 0 || j == sections.size() - 1 || sections.get(j - 1) != lp || sections.get(j + 1) != lp ? NODE_Y_OFFSET : LINE_Y_OFFSET;
                Vector4f color = RenderUtils.expandColor(RedStoneWireBlock.getColorForPower(lp), false);
                RenderSystem.setShaderColor(color.x, color.y, color.z, 1);
                gui.blit(BACKGROUND, left + SEQUENCER_WIDGET_X_OFFSET + i * 3, top + SEQUENCER_WIDGET_Y_OFFSET + 48 - lp * 3 - 3, SCREEN_WIDTH, offset, 3, 3, IMAGE_WIDTH, IMAGE_HEIGHT);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                if (head == i + section_offset)
                    gui.blit(BACKGROUND, left + SEQUENCER_WIDGET_X_OFFSET + i * 3, top + SEQUENCER_WIDGET_Y_OFFSET + 48 - lp * 3 - 3, SCREEN_WIDTH, SELECTED_NODE_Y_OFFSET, 3, 3, IMAGE_WIDTH, IMAGE_HEIGHT);
            } else
                RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        if (selection != -1) {
            int start, finish;
            if (selection < head) {
                start = Integer.max(0, selection - section_offset);
                finish = Integer.max(0, head - section_offset);
            } else {
                start = Integer.max(0, head - section_offset);
                finish = Integer.max(0, selection - section_offset);
            }
            if (start < MAXIMUM_VISIBLE_SECTIONS && finish > 0)
                gui.fill(start * 3 + left + SEQUENCER_WIDGET_X_OFFSET, top + SEQUENCER_WIDGET_Y_OFFSET, (finish + 1) * 3 + left + SEQUENCER_WIDGET_X_OFFSET, top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y, 0x550000FF);
        }
        for (int i = 0; i <= 5; ++i)
            gui.drawCenteredString(font, "" + (i + section_offset / 10) * (boosted ? 5 : 10), (left + SEQUENCER_WIDGET_X_OFFSET + 2 + i * 30), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y + 3), 0xFFFF);
        for (int i = 0; i < 4; ++i) {
            gui.drawString(font, "" + i * 5, (left + (i < 2 ? 11 : 5)), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y - 5 - i * 15), RedStoneWireBlock.getColorForPower(i * 5), false);
            gui.drawString(font, "" + i * 5, (left + SCREEN_WIDTH - 16), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y - 5 - i * 15), RedStoneWireBlock.getColorForPower(i * 5), false);
        }
        gui.drawString(font, Component.translatable("screen.arss.sequencer.head_info"), (left + 12), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y + 14), 0xFFFF00);
        gui.drawString(font, Component.translatable("screen.arss.sequencer.head_tick", head, head / (boosted ? 20. : 10.)), (left + 12), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y + 23), 0xFFFF00);
        gui.drawString(font, Component.translatable("screen.arss.sequencer.head_power", be != null ? be.readPower() : 0), (left + 12), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y + 32), 0xFFFF00);
        gui.drawString(font, Component.translatable("screen.arss.sequencer.cursor_info"), (left + 25 + SEQUENCER_WIDGET_SIZE_X / 2), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y + 14), 0xFFFF00);
        mouseX -= left;
        mouseY -= top;
        if (mouseX >= SEQUENCER_WIDGET_X_OFFSET && mouseX < SEQUENCER_WIDGET_X_OFFSET + SEQUENCER_WIDGET_SIZE_X && mouseY >= SEQUENCER_WIDGET_Y_OFFSET && mouseY < SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y) {
            int column = (mouseX - SEQUENCER_WIDGET_X_OFFSET) / 3 + section_offset;
            int row = 15 - ((mouseY - SEQUENCER_WIDGET_Y_OFFSET) / 3);
            gui.drawString(font, Component.translatable("screen.arss.sequencer.cursor_tick", column, column / (boosted ? 20. : 10.)), (left + 25 + SEQUENCER_WIDGET_SIZE_X / 2), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y + 23), 0xFFFF00);
            gui.drawString(font, Component.translatable("screen.arss.sequencer.cursor_power", row), (left + 25 + SEQUENCER_WIDGET_SIZE_X / 2), (top + SEQUENCER_WIDGET_Y_OFFSET + SEQUENCER_WIDGET_SIZE_Y + 32), 0xFFFF00);
        }
        if (mouseX >= 172 && mouseX < 189 && mouseY >= 4 && mouseY < 20) {
            gui.renderTooltip(font, Collections.singletonList(Component.translatable("screen.arss.sequencer.show_help")), Optional.empty(), ItemStack.EMPTY, mouseX + left, mouseY + top);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getInstance().screen == this)
            renderBackground(gui);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        gui.blit(BACKGROUND, left, top, 0, 0, SCREEN_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
        super.render(gui, mouseX, mouseY, partialTick);
        renderSections(gui, mouseX, mouseY);
    }

//    public void resyncServer() {
//        if (be != null)
//            NetworkManager.toServer(new SequencerBlockEntity.SyncManually(be.getBlockPos(), be.saveSyncData(new CompoundTag())));
//    }

    @Override
    public void onClose() {
        super.onClose();
//        resyncServer();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return selection == -1;
    }

    public boolean stillValid() {
        return player != null && be != null && player.level().getBlockEntity(be.getBlockPos()) == be && player.blockPosition().distSqr(be.getBlockPos()) <= 36;
    }

    @Override
    public void tick() {
        if (!stillValid())
            onClose();
//        else
//            be.editorTick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
