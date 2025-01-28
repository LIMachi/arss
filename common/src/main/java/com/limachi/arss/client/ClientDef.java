package com.limachi.arss.client;

import com.limachi.arss.utils.client.annotations.RegisterKeyBinding;

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
            SUB_SEQUENCES_BOUND = new Component[5];
            SUB_SEQUENCES_UNBOUND = new Component[5];
            KeyMapping sneak = Minecraft.getInstance().options.keyShift;
            KeyMapping use = Minecraft.getInstance().options.keyUse;
            for (int i = 1; i <= 5; ++i) {
                SUB_SEQUENCES_BOUND[i - 1] = Component.translatable("tooltip.help.general." + i, Component.keybind(SCROLL_KEY.getName()), Component.keybind(sneak.getName()), Component.keybind(use.getName()));
                if (i == 3)
                    SUB_SEQUENCES_UNBOUND[2] = Component.translatable("tooltip.help.general.3_alternate", Component.keybind(SCROLL_KEY.getName()), Component.keybind(sneak.getName()), Component.keybind(use.getName()));
                else
                    SUB_SEQUENCES_UNBOUND[i - 1] = SUB_SEQUENCES_BOUND[i - 1];
            }
        }
        if (Screen.hasShiftDown()) {
            var cached = CACHE.get("tooltip.help.shift." + name);
            if (cached == null) {
                cached = split(Component.translatable("tooltip.help.shift." + name, (Object[])(SCROLL_KEY.isUnbound() ? SUB_SEQUENCES_UNBOUND : SUB_SEQUENCES_BOUND)));
                CACHE.put("tooltip.help.shift." + name, cached);
            }
            components.addAll(cached);
        } else
            components.add(SHIFT_FOR_HELP);
        if (Screen.hasControlDown()) {
            var cached = CACHE.get("tooltip.help.ctrl." + name);
            if (cached == null) {
                cached = split(Component.translatable("tooltip.help.ctrl." + name, (Object[])(SCROLL_KEY.isUnbound() ? SUB_SEQUENCES_UNBOUND : SUB_SEQUENCES_BOUND)));
                CACHE.put("tooltip.help.ctrl." + name, cached);
            }
            if (Screen.hasShiftDown())
                components.add(Component.empty());
            components.addAll(cached);
        } else
            components.add(CTRL_FOR_HELP);
    }
}
