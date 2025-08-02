package com.limachi.arss.client;

import com.limachi.lim_lib.client.annotations.RegisterKeyBinding;
import dev.architectury.platform.Platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class ClientDef {
    @RegisterKeyBinding
    public static KeyMapping SCROLL_KEY = new KeyMapping("key.hold_to_scroll", GLFW.GLFW_KEY_LEFT_ALT, "key.categories.arss");

    public static int lineWidth = 60;

    public static Style legacyStyleFormat(char escaped) { return legacyStyleFormat(escaped, Style.EMPTY); }
    public static Style legacyStyleFormat(char escaped, Style currentStyle) {
        return switch (escaped) {
            case '0' -> currentStyle.withColor(0x000000); //black
            case '1' -> currentStyle.withColor(0x0000AA); //dark_blue
            case '2' -> currentStyle.withColor(0x00AA00); //dark_green
            case '3' -> currentStyle.withColor(0x00AAAA); //dark_aqua
            case '4' -> currentStyle.withColor(0xAA0000); //dark_red
            case '5' -> currentStyle.withColor(0xAA00AA); //dark_purple
            case '6' -> currentStyle.withColor(0xFFAA00); //gold
            case '7' -> currentStyle.withColor(0xAAAAAA); //gray

            case '8' -> currentStyle.withColor(0x555555); //dark_gray
            case '9' -> currentStyle.withColor(0x5555FF); //blue
            case 'a' -> currentStyle.withColor(0x55FF55); //green
            case 'b' -> currentStyle.withColor(0x55FFFF); //aqua
            case 'c' -> currentStyle.withColor(0xFF5555); //red
            case 'd' -> currentStyle.withColor(0xFF55FF); //light_purple
            case 'e' -> currentStyle.withColor(0xFFFF55); //yellow
            case 'f' -> currentStyle.withColor(0xFFFFFF); //white

            //bedrock edition
//                    case 'g' -> currentStyle.withColor(0xDDD605); //minecoin_gold
//                    case 'h' -> currentStyle.withColor(0x5555FF); //material_quartz
//                    case 'i' -> currentStyle.withColor(0x5555FF); //material_iron
//                    case 'j' -> currentStyle.withColor(0x5555FF); //material_netherite
//                    case 'm' -> currentStyle.withColor(0x5555FF); //material_redstone
//                    case 'n' -> currentStyle.withColor(0x5555FF); //material_copper
//                    case 'p' -> currentStyle.withColor(0x5555FF); //material_gold
//                    case 'q' -> currentStyle.withColor(0x5555FF); //material_emerald
//                    case 's' -> currentStyle.withColor(0x5555FF); //material_diamond
//                    case 't' -> currentStyle.withColor(0x5555FF); //material_lapis
//                    case 'u' -> currentStyle.withColor(0x5555FF); //material_amethyst

            //formatting
            case 'k' -> currentStyle.withObfuscated(true);
            case 'l' -> currentStyle.withBold(true);
            case 'm' -> currentStyle.withStrikethrough(true);
            case 'n' -> currentStyle.withUnderlined(true);
            case 'o' -> currentStyle.withItalic(true);

            case 'r' -> Style.EMPTY;

            default -> null;
        };
    }

    public static Component fromRawLegacy(String raw) { return fromRawLegacy(raw, Style.EMPTY); }
    public static Component fromRawLegacy(String raw, Style style) {
        MutableComponent root = null;
        MutableComponent last = null;
        StringBuilder tmp = new StringBuilder();
        boolean format = false;
        for (char c : raw.toCharArray()) {
            if (c == '§') {
                if (format) {
                    tmp.append('§');
                    format = false;
                } else
                    format = true;
            } else if (format) {
                var ts = legacyStyleFormat(c, style);
                if (ts == null)
                    tmp.append('§').append(c);
                else if (ts != style) {
                    if (!tmp.isEmpty()) {
                        if (root == null) {
                            root = Component.literal(tmp.toString()).withStyle(style);
                            last = root;
                        } else {
                            MutableComponent tc = Component.literal(tmp.toString()).withStyle(style);
                            last.append(tc);
                            last = tc;
                        }
                        tmp = new StringBuilder();
                    }
                    style = ts;
                }
                format = false;
            } else
                tmp.append(c);
        }
        if (!tmp.isEmpty()) {
            if (root == null)
                root = Component.literal(tmp.toString()).withStyle(style);
            else
                last.append(Component.literal(tmp.toString()).withStyle(style));
        }
        return root == null ? Component.empty().withStyle(style) : root;
    }

    public static Style style(Component component) {
        var sib = component.getSiblings();
        if (sib.isEmpty())
            return component.getStyle();
        return style(sib.getLast());
    }

    public static List<Component> split(Component line) {
        String raw = line.getString();
        Style[] style = {line.getStyle()};
        if (!Platform.isFabric()) //forge/neoforge both wrap lines automatically on tooltips
            lineWidth = 500;
        if (raw.contains("\n") || raw.length() > lineWidth) {
            return Arrays.stream(raw.split("\n")).flatMap(l->{
                if (l.length() > lineWidth) {
                    var s = new LinkedList<String>();
                    while (l.length() > lineWidth) {
                        int space = l.substring(0, lineWidth).lastIndexOf(' ');
                        if (space == -1)
                            break;
                        s.addLast(l.substring(0, space));
                        l = l.substring(space + 1);
                    }
                    s.addLast(l);
                    return s.stream();
                } else
                    return Stream.of(l);
            }).map(s->{
                var out = fromRawLegacy(s, style[0]);
                style[0] = style(out);
                return out;
            }).collect(Collectors.toList());
        } else
            return List.of(line);
    }

    protected static String LANGUAGE = null;
    protected static Component[] SUB_SEQUENCES_BOUND = null;
    protected static Component[] SUB_SEQUENCES_UNBOUND = null;
    protected static final Component SHIFT_FOR_HELP = Component.translatable("tooltip.help.press_shift_for_help");
    protected static final Component CTRL_FOR_HELP = Component.translatable("tooltip.help.press_ctrl_for_help");
    protected static final HashMap<String, List<Component>> CACHE = new HashMap<>();

    public static void commonHoverText(String name, List<Component> components) {
        if (LANGUAGE == null)
            LANGUAGE = Minecraft.getInstance().getLanguageManager().getSelected();
        if (!Minecraft.getInstance().getLanguageManager().getSelected().equals(LANGUAGE)) {
            CACHE.clear();
            SUB_SEQUENCES_BOUND = null;
            SUB_SEQUENCES_UNBOUND = null;
        }
        if (SUB_SEQUENCES_BOUND == null) {
            SUB_SEQUENCES_BOUND = new Component[9];
            SUB_SEQUENCES_UNBOUND = new Component[9];
            KeyMapping sneak = Minecraft.getInstance().options.keyShift;
            KeyMapping use = Minecraft.getInstance().options.keyUse;
            SUB_SEQUENCES_UNBOUND[0] = SUB_SEQUENCES_BOUND[0] = Component.keybind(SCROLL_KEY.getName());
            SUB_SEQUENCES_UNBOUND[1] = SUB_SEQUENCES_BOUND[1] = Component.keybind(sneak.getName());
            SUB_SEQUENCES_UNBOUND[2] = SUB_SEQUENCES_BOUND[2] = Component.keybind(use.getName());
            for (int i = 1; i <= 6; ++i) {
                SUB_SEQUENCES_BOUND[i + 2] = Component.translatable("tooltip.help.general." + i, Component.keybind(SCROLL_KEY.getName()), Component.keybind(sneak.getName()), Component.keybind(use.getName()));
                if (i == 3)
                    SUB_SEQUENCES_UNBOUND[5] = Component.translatable("tooltip.help.general.3_alternate", Component.keybind(SCROLL_KEY.getName()), Component.keybind(sneak.getName()), Component.keybind(use.getName()));
                else
                    SUB_SEQUENCES_UNBOUND[i + 2] = SUB_SEQUENCES_BOUND[i + 2];
            }
        }
        var cachedShift = CACHE.get("tooltip.help.shift." + name);
        if (cachedShift == null) {
            if (!Component.translatable("tooltip.help.shift." + name).getString().isBlank()) {
                cachedShift = split(Component.translatable("tooltip.help.shift." + name, (Object[]) (SCROLL_KEY.isUnbound() ? SUB_SEQUENCES_UNBOUND : SUB_SEQUENCES_BOUND)));
                CACHE.put("tooltip.help.shift." + name, cachedShift);
            } else
                cachedShift = new ArrayList<>();
        }
        if (Screen.hasShiftDown())
            components.addAll(cachedShift);
        else if (!cachedShift.isEmpty() && !(cachedShift.size() == 1 && cachedShift.get(0).getString().isBlank()))
            components.add(SHIFT_FOR_HELP);
        var cachedCtrl = CACHE.get("tooltip.help.ctrl." + name);
        if (cachedCtrl == null) {
            if (!Component.translatable("tooltip.help.ctrl." + name).getString().isBlank()) {
                cachedCtrl = split(Component.translatable("tooltip.help.ctrl." + name, (Object[]) (SCROLL_KEY.isUnbound() ? SUB_SEQUENCES_UNBOUND : SUB_SEQUENCES_BOUND)));
                CACHE.put("tooltip.help.ctrl." + name, cachedCtrl);
            } else
                cachedCtrl = new ArrayList<>();
        }
        if (Screen.hasControlDown()) {
            if (Screen.hasShiftDown() && !cachedShift.isEmpty() && !(cachedShift.size() == 1 && cachedShift.get(0).getString().isBlank()))
                components.add(Component.empty());
            components.addAll(cachedCtrl);
        }
        else if (!cachedCtrl.isEmpty() && !(cachedCtrl.size() == 1 && cachedCtrl.get(0).getString().isBlank()))
            components.add(CTRL_FOR_HELP);
    }
}
