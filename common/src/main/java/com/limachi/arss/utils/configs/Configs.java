package com.limachi.arss.utils.configs;

//new minimal config layout:
//allow comments from multiple languages: // /**/ ? ; # @
//key value style: references on the right then the assignation symbol: = then value in string format
//string as values can be globed by either single or double quote
//list style values can be either globed by square or curly braces
//map/object style values can be globed by curly or normal braces
//map do not require the keys to be strings
//type could be encoded when needed (tbd)

/*
example file:

#comment explaining the use of the following value
value = true

//comment explaining the use of another value
other = 10

map = {"test": 1, "other": 2}
map2 = {2: true, 3: false}
object = {field1: true, field2: "string"}
multiline_object = {
    field1: {"was_map": true},
    field2: [1, 2, 3]
}
concatenated_multiline_string = "first three words"
" added four more words"
"\nnew line"
*/

import com.limachi.arss.utils.Stage;
import com.limachi.arss.utils.StringUtils;
import com.limachi.arss.utils.annotations.StaticInit;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Configs {
    public interface ConfigSerializer {
        ArrayList<String> serialize(boolean recompute, int depth, Function<Integer, ConfigStyle> style);
        ConfigValue deserialize(ArrayList<String> lines);
    }

    public static class Separator implements ConfigSerializer {
        int size;
        final ArrayList<String> content = new ArrayList<>();

        public Separator() {
            size = 1;
            content.add("");
        }

        public Separator(int size) {
            this.size = Math.max(size, 0);
            for (int i = 0; i < this.size; ++i)
                 content.add("");
        }

        @Override
        public ArrayList<String> serialize(boolean recompute, int depth, Function<Integer, ConfigStyle> style) {
            return content;
        }

        @Override
        public ConfigValue deserialize(ArrayList<String> lines) {
            return ConfigValue.EMPTY;
        }
    }

    public static class Comment implements ConfigSerializer {
        final ArrayList<String> commentLines = new ArrayList<>();
        final ArrayList<String> content = new ArrayList<>();

        public Comment() {}

        public Comment(String comment) {
            this.commentLines.add(comment);
        }

        public Comment(Iterable<String> commentLines) {
            commentLines.forEach(this.commentLines::add);
        }

        @Override
        public ArrayList<String> serialize(boolean recompute, int depth, Function<Integer, ConfigStyle> style) {
            if (recompute) {
                ConfigStyle s = style.apply(depth);
                content.clear();
                for (String l : commentLines)
                    content.add(s.commentStarter + l);
            }
            return content;
        }

        @Override
        public ConfigValue deserialize(ArrayList<String> lines) {
            return ConfigValue.EMPTY;
        }
    }

    ArrayList<ConfigSerializer> serializers = new ArrayList<>(); //can be a combination of Separator, Comment or ConfigValue

    public String serialize() {
        StringBuilder out = new StringBuilder();
        for (var s : serializers)
            for (String l : s.serialize(true, 0, ConfigStyle.DEFAULT_PROVIDER)) {
                out.append(l);
                out.append(ConfigStyle.TOP.newLine);
            }
        return out.toString();
    }

    public static <T, C extends Codec<T>> ConfigValue codecSerializer(C codec, T value) {
        return codec.encode(value, ConfigOps.INSTANCE, ConfigValue.EMPTY).getOrThrow();
    }

    public static <T, C extends Codec<T>> T codecDeserialize(C codec, ConfigValue value) {
        return codec.decode(ConfigOps.INSTANCE, value).getOrThrow().getFirst();
    }

    @StaticInit(Stage.BLOCK)
    public static void test() {
        Configs c = new Configs();
        c.serializers.add(new Comment("test"));
        c.serializers.add(new Separator(2));
        c.serializers.add(new ConfigValue(ConfigValueType.LIST));
        c.serializers.add(codecSerializer(ItemStack.CODEC, new ItemStack(Items.COMPARATOR)));
        ItemStack test = codecDeserialize(ItemStack.CODEC, (ConfigValue)c.serializers.getLast());
        System.out.println(c.serialize());
        System.out.println(test);
        test2();
    }

    public static class Tokenizer {
        private static final HashMap<String, Supplier<?>> objectBuilders = new HashMap<>();
        private static final HashMap<Class<?>, String> objectTokens = new HashMap<>();

        public static <T> void registerObject(Class<T> clazz, String name, Supplier<T> builder) {
            if (clazz == null)
                return;
            if (builder == null) {
                Constructor<T> c;
                try {
                    c = clazz.getConstructor();
                } catch (NoSuchMethodException e) {
                    return;
                }
                builder = ()-> {
                    try {
                        return c.newInstance();
                    } catch (Exception e) {
                        return null;
                    }
                };
            }
            if (name == null)
                name = StringUtils.getSimplifiedClassName(clazz.getName());
            objectBuilders.put(name, builder);
            objectTokens.put(clazz, name);
        }

        public static <T> void registerObject(Class<T> clazz, Supplier<T> builder) { registerObject(clazz, null, builder); }
        public static <T> void registerObject(Class<T> clazz, String name) { registerObject(clazz, name, null); }
        public static <T> void registerObject(Class<T> clazz) { registerObject(clazz, null, null); }

        //guess map:
        //unknown:                  0b111111111111 //can be anything
        //quoted string:            0b000000000001 //can be single or double quote string
        //comment:                  0b000000000010 //can be single or multiline
        //pair separator:           0b000000000100
        //new line:                 0b000000001000 //can be coerced in white
        //white:                    0b000000010000 //can be skipped
        //long:                     0b000001100000 //can also be double
        //                          0b000000100000
        //double:                   0b000001000000
        //iteration separator:      0b000010000000
        //block:                    0b000100000000 //can be list/map
        //free form string/keyword: 0b111000000000 //can be object if it matches a name and followed by block, or boolean
        //                          0b001000000000
        //boolean:                  0b010000000000
        //object:                   0b100000000000
        protected int guess = 0b111111111111;
        protected char[] input;
        protected int consumed = 0;

        protected String getString(int skip, int chars) {
            StringBuilder b = new StringBuilder();
            if (chars < 0)
                chars = input.length;
            int l = Integer.min(input.length - consumed - skip, chars);
            for (int i = 0; i < l; ++i)
                b.append(input[consumed + i + skip]);
            return b.toString();
        }

        public Tokenizer() {}

        protected boolean multiline_comment = false;

        interface TryTokenize {
            Optional<RegToken> tryTokenize();
        }

        protected class QuotedStringTokenizer implements TryTokenize {
            protected boolean escape;
            char globe;
            String result;
            int consumptionLength;

            @Override
            public Optional<RegToken> tryTokenize() {
                globe = 0;
                result = "";
                escape = false;
                consumptionLength = 0;
                if (input[consumed] == '"' || input[consumed] == '\'') {
                    globe = input[consumed];
                    consumptionLength = 1;
                    while (consumptionLength + consumed < input.length) {
                        char c = input[consumed + consumptionLength];
                        ++consumptionLength;
                        if (c == '\\') {
                            if (escape) {
                                result += '\\';
                                escape = false;
                            } else
                                escape = true;
                        } else if (c == globe) {
                            if (escape) {
                                result += globe;
                                escape = false;
                            } else
                                return Optional.of(new RegToken(RegToken.RegKind.String, getString(0, consumptionLength), result));
                        } else {
                            if (escape) {
                                if (c == 'n')
                                    result += '\n';
                                else if (c == 'r')
                                    result += '\r';
                                else if (c == 't')
                                    result += '\t';
                                else
                                    result = result + '\\' + c;
                                escape = false;
                            } else
                                result += c;
                        }
                    }
                }
                return Optional.empty();
            }
        }

        protected class CommentTokenizer implements TryTokenize {
            boolean multiline;
            int skip;
            int consumptionLength;

            @Override
            public Optional<RegToken> tryTokenize() {
                multiline = false;
                skip = 0;
                consumptionLength = 0;
                char c = input[consumed];
                if (c == '/' && consumed + 1 < input.length) {
                    if (input[consumed + 1] == '/')
                        consumptionLength = 2;
                    else if (input[consumed + 1] == '*') {
                        consumptionLength = 2;
                        multiline = true;
                    } else
                        return Optional.empty();
                } else if (c == '#' || c == ';' || c == '@' || c == '?') {
                    consumptionLength = 1;
                } else
                    return Optional.empty();
                skip = consumptionLength;
                while (consumptionLength + consumed < input.length) {
                    c = input[consumed + consumptionLength];
                    ++consumptionLength;
                    if (multiline) {
                        if (c == '/' && consumptionLength > 2 && input[consumed + consumptionLength - 1] == '*')
                            return Optional.of(new RegToken(RegToken.RegKind.Comment, getString(0, consumptionLength), getString(2, consumptionLength - 4)));
                    } else {
                        if (c == '\n' || c == '\r')
                            return Optional.of(new RegToken(RegToken.RegKind.Comment, getString(0, consumptionLength), getString(1, consumptionLength - 2)));
                    }
                }
                if (multiline)
                    return Optional.empty(); //missing closing pair '*','/'
                else
                    return Optional.of(new RegToken(RegToken.RegKind.Comment, getString(0, consumptionLength), getString(1, consumptionLength - 1)));
            }
        }

        protected class WhiteTokenizer implements TryTokenize {
            int consumptionLength;

            @Override
            public Optional<RegToken> tryTokenize() {
                consumptionLength = 0;
                while (consumptionLength + consumed < input.length && Character.getType(input[consumed + consumptionLength]) == Character.SPACE_SEPARATOR)
                    ++consumptionLength;
                if (consumptionLength > 0) {
                    String out = getString(0, consumptionLength);
                    return Optional.of(new RegToken(RegToken.RegKind.White, out, out));
                }
                return Optional.empty();
            }
        }

        protected class LineSepTokenizer implements TryTokenize {
            int consumptionLength;

            @Override
            public Optional<RegToken> tryTokenize() {
                consumptionLength = 0;
                while (consumptionLength + consumed < input.length) {
                    int t = Character.getType(input[consumed + consumptionLength]);
                    if (t != Character.LINE_SEPARATOR && t != Character.PARAGRAPH_SEPARATOR)
                        break;
                    ++consumptionLength;
                }
                if (consumptionLength > 0) {
                    String out = getString(0, consumptionLength);
                    return Optional.of(new RegToken(RegToken.RegKind.NewLine, out, out));
                }
                return Optional.empty();
            }
        }

        protected class SimpleMatchTokenizer implements TryTokenize {
            final HashSet<String> set;
            final RegToken.RegKind kind;

            SimpleMatchTokenizer(RegToken.RegKind kind, String ... valid) {
                this(kind, Arrays.stream(valid));
            }

            SimpleMatchTokenizer(RegToken.RegKind kind, Stream<String> valid) {
                this.kind = kind;
                set = new HashSet<>();
                valid.forEach(c -> {
                    if (c != null && !c.isEmpty())
                        set.add(c);
                });
            }

            SimpleMatchTokenizer(RegToken.RegKind kind, Iterable<String> valid) {
                this.kind = kind;
                set = new HashSet<>();
                for (String c : valid)
                    if (c != null && !c.isEmpty())
                        set.add(c);
            }

            @Override
            public Optional<RegToken> tryTokenize() {
                for (String t : set)
                    if (t.equals(getString(0, t.length())))
                        return Optional.of(new RegToken(kind, t, t));
                return Optional.empty();
            }
        }

        protected class KeyWordTokenizer implements TryTokenize {
            public static final Pattern keywordPattern = Pattern.compile("\\b[a-zA-Z_]\\w*\\b");

            @Override
            public Optional<RegToken> tryTokenize() {
                Matcher m = keywordPattern.matcher(getString(0, -1));
                if (m.find() && m.start() == 0) {
                    String out = getString(0, m.end());
                    return Optional.of(new RegToken(RegToken.RegKind.Unknown, out, out));
                }
                return Optional.empty();
            }
        }

        protected class NumberTokenizer implements TryTokenize {
            public static final Pattern numberPattern = Pattern.compile("[+-]*\\d*.?\\d*(?:[eE][+-]?\\d+)?[bsilfd]?|[Nn][Aa][Na]|[+-]?[Ii][Nn][Ff]");

            @Override
            public Optional<RegToken> tryTokenize() {
                Matcher m = numberPattern.matcher(getString(0, -1));
                if (m.find() && m.start() == 0 && m.end() > 0) {
                    String out = getString(0, m.end());
                    return Optional.of(new RegToken(RegToken.RegKind.Number, out, out));
                }
                return Optional.empty();
            }
        }

        public List<RegToken> tokenize(String input) {
            var out = new ArrayList<RegToken>();
            if (input.isEmpty()) return out;
            this.input = input.toCharArray();
            TryTokenize[] tokenizers = new TryTokenize[]{
                    new QuotedStringTokenizer(),
                    new CommentTokenizer(),
                    new WhiteTokenizer(),
                    new LineSepTokenizer(),
                    new SimpleMatchTokenizer(RegToken.RegKind.Pair, "=", ":", "->", "=>", "<-", "<="),
                    new SimpleMatchTokenizer(RegToken.RegKind.Separator, ","),
                    new KeyWordTokenizer(),
            };
            final RegToken[] test = {null};
            while (consumed != this.input.length) {
                for (TryTokenize t : tokenizers) {
                    t.tryTokenize().ifPresent(tok->{
                        if (test[0] == null)
                            test[0] = tok;
                        else if (test[0].match.length() < tok.match.length())
                            test[0] = tok;
                    });
                }
                if (test[0] == null)
                    break;
                out.add(test[0]);
            }
            return out;
        }
    }

    public static class RegToken {

        public static final Pattern EXTRACTOR = Pattern.compile(RegKind.masterExtractor);

        public enum RegKind {
            Unknown("un1", "un1", 0b111111111111),
            String("gstr", "str", 0b000000000001),
            Comment("gcmt", "cmt", 0b000000000010),
            Pair("pair", "pair", 0b000000000100),
            NewLine("nl", "nl", 0b000000001000),
            White("white", "white", 0b000000010000),
            True("true", "true", 0b010000000000),
            False("false", "false", 0b010000000000),
            Number("num", "num", 0b000001100000),
            Separator("sep", "sep", 0b000010000000),
            List("glist", "list", 0b000100000000),
            Map("gmap", "map", 0b000100000000),
            Trailing("un2", "un2", 0b111111111111),
            Object(null, null, 0b100000000000);

            private final String groupName;
            private final String subGroup;
            private final int mask;

            private static final String strExtractor = "(?<gstr>(?<!\\\\)(?<d>[\"'])(?<str>.*?)(?<!\\\\)\\k<d>)";
            private static final String commentExtractor = "(?<gcmt>[#](?<cmt>.*(?=[\n\r]|$)))";
            private static final String pairExtractor = "(?<pair>[=])";
            private static final String whiteSpaceExtractor = "(?<white>\\s+)";
            private static final String trueExtractor = "(?<=\\W|^)(?<true>[Tt](?:[Rr][Uu][Ee])?|[Yy](?:[Ee][Ss])?|[Oo][Kk])(?=\\W|\\s|$)";
            private static final String falseExtractor = "(?<=\\W|^)(?<false>[Ff](?:[Aa][Ll][Ss][Ee])?|[Nn][Oo]?)(?=\\W|\\s|$)";
            private static final String numberExtractor = "(?<num>[+-]?\\d+.?\\d*(?:[eE][+-]?\\d+)?[bsilfd]?|[Nn][Aa][Na]|[+-]?[Ii][Nn][Ff])";
            private static final String separatorExtractor = "(?<sep>[,])";
            private static final String listExtractor = "(?<glist>\\[(?<list>.+)\\])";
            private static final String mapExtractor = "(?<gmap>\\{(?<map>.+)\\})";

            public static final String masterExtractor = "(?<un1>.*?)(?:" + strExtractor + "|" + commentExtractor + "|" + pairExtractor + "|" + whiteSpaceExtractor + "|" + trueExtractor + "|" + falseExtractor + "|" + numberExtractor + "|" + separatorExtractor + "|" + listExtractor + "|" + mapExtractor + ")|(?<un2>.*?$)";

            RegKind(String groupName, String subGroup, int mask) {
                this.groupName = groupName;
                this.subGroup = subGroup;
                this.mask = mask;
            }

            public String superGroup() { return groupName; }
            public String innerGroup() { return subGroup; }
            public int mask() { return mask; }
        }

        public RegKind kind = RegKind.Unknown;
        public String match = "";
        public String innerMatch = "";
        public ArrayList<RegToken> inner = null;

        public RegToken(Matcher matcher, boolean onlyMain) {
            for (RegKind k : RegKind.values()) {
                if (k.groupName == null)
                    continue;
                if (onlyMain && (k == RegKind.Unknown || k == RegKind.Trailing))
                    continue;
                String m = matcher.group(k.groupName);
                if (m != null && m.length() > match.length()) {
                    kind = k;
                    match = m;
                    innerMatch = matcher.group(k.subGroup);
                }
            }
            if (kind == RegKind.Map || kind == RegKind.List)
                inner = tokenize(innerMatch, false);
        }

        protected RegToken(RegKind kind, String match, String innerMatch) {
            this.kind = kind;
            this.match = match;
            this.innerMatch = innerMatch;
        }

        public boolean empty() { return kind == RegKind.Unknown && match.isEmpty(); }

        @Override
        public String toString() { return "T(" + kind + "):" + match + (inner != null ? "->" + inner : ""); }

        public String rebuild() {
            return "";
//            return switch (kind) {
//                case Unknown -> match;
//                case String -> "\"" + match + "\"";
//                case Comment -> "#" + match;
//                case Pair -> "=";
//                case White -> " ";
//                case True -> "TRUE";
//                case False -> "FALSE";
//                case Number -> match;
//                case Separator -> ", ";
//                case List -> match;
//                case Map -> match;
//                case Trailing -> match;
//            };
        }
    }

    public static ArrayList<RegToken> tokenize(String input, boolean discardWhites) {
        ArrayList<RegToken> out = new ArrayList<>();
        Matcher m = RegToken.EXTRACTOR.matcher(input);
        while (m.find()) {
            RegToken t = new RegToken(m, false);
            if (!t.empty()) {
                if (t.kind == RegToken.RegKind.Trailing) {
                    RegToken n = new RegToken(m, true);
                    if (!n.match.isEmpty() && !(discardWhites && n.kind == RegToken.RegKind.White))
                        out.add(n);
                }
                if (!discardWhites || t.kind != RegToken.RegKind.White)
                    out.add(t);
            }
            if (t.kind == RegToken.RegKind.Unknown) {
                RegToken n = new RegToken(m, true);
                if (!n.match.isEmpty() && !(discardWhites && n.kind == RegToken.RegKind.White))
                    out.add(n);
            }
        }
        return out;
    }

    public static String rebuild(ArrayList<RegToken> tokens) {
        StringBuilder b = new StringBuilder();
        for (RegToken t : tokens)
            b.append(t.rebuild());
        return b.toString();
    }

    public static void test2() {
        String input = "\"'escaped\\\"'\"\n" +
                "'\"\\''''\n" +
                "\"#\"\n" +
                "#comment   \n" +
                "id = \"minecraft:comparator\"\n" +
                "count=1.e1d\n" +
                "valid=true  ,  other=false\n" +
                "[list, of, things]\n" +
                "[\"contains# strings\"]\n" +
                "\"[contains list]\"\n" +
                "[#]";
        var tokens = tokenize(input, true);
        System.out.println(tokens);
        System.out.println(rebuild(tokens));
    }
}
