package com.limachi.arss.utils.parser;

import com.limachi.arss.utils.Stage;
import com.limachi.arss.utils.StringUtils;
import com.limachi.arss.utils.annotations.StaticInit;
import com.limachi.arss.utils.parser.tokenizers.*;
import com.mojang.datafixers.util.Pair;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Tokenizer {
    public enum Kind {
        KEYWORD, //may be boolean, object, raw string or something else
        STRING, //quoted string delimited by either " or '
        COMMENT, //single or multiline comment delimited by /**/ or any of //, #, ;, ?, @ and finished by a newline
        PAIR, //either =, :, ->, =>, <-, <= (denote a link between 2 tokens, ignoring comment, nl and blank)
        SEP, //, separator for a stream of token
        NL, //new line in the broadest sense of the term, may contain more than 1 line
        BLANK, //white space not including new lines
        NUMBER, //pair of long and double for best representation of a parsed number, long might be null if this is only representable as double
        BLOCK_START, //either {, [ or (
        BLOCK_END, //either }, ] or )
        BOOLEAN, //boolean keywords are transformed to boolean values by the sanitizer
    }

    public static class Token {
        public Kind kind;
        public String match;
        public Object value;

        public Token(Kind kind, String match, Object value) {
            this.kind = kind;
            this.match = match;
            this.value = value;
        }

        @Override
        public String toString() { return "T(" + kind + "){" + match + "}:" + value; }
    }

    public interface TryTokenize {
        Token tryTokenize(Tokenizer state);
    }

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

    protected char[] input;
    protected int consumed = 0;

    public String getString(int skip, int chars) {
        StringBuilder b = new StringBuilder();
        if (chars < 0)
            chars = input.length;
        int l = Integer.min(input.length - consumed - skip, chars);
        for (int i = 0; i < l; ++i)
            b.append(input[consumed + i + skip]);
        return b.toString();
    }

    public char c() { return c(0); }
    public char c(int offset) { return ok(offset) ? input[consumed + offset] : '\0'; }
    public boolean ok(int offset) { return consumed + offset < input.length && consumed + offset >= 0; }
    public void consume(int amount) { consumed = Math.clamp(consumed + amount, 0, input.length); }
    public boolean finished() { return consumed < 0 || consumed >= input.length || input[consumed] == '\0' || input[consumed] == '\u0003' || input[consumed] == '\u0004'; }

    public Tokenizer() {}

    public static final Pattern separatorPattern = Pattern.compile(",");
    public static final Pattern pairPattern = Pattern.compile("=|:|->|=>|<-|<=");
    public static final Pattern keywordPattern = Pattern.compile("\\b[a-zA-Z_]\\w*\\b");
    public static final Pattern numberPattern = Pattern.compile("[+-]*0?[xbo]?\\d*\\.?\\d*(?:[eE][+-]?\\d+)?[BbSsIiLlFfDd]?|[Nn][Aa][Na]|[+-]?[Ii][Nn][Ff]");
    public static final Pattern blockStartPattern = Pattern.compile("[{\\[(]");
    public static final Pattern blockEndPattern = Pattern.compile("[}\\])]");

    protected static Pair<Character, Integer> convertBlock(String s) {
        return new Pair<>(s.toCharArray()[0], null);
    }

//    protected static Pair<Double, Long> convertNumber(String s) {
//        Long l = null;
//        Double d = null;
//        //FIXME: does not handle all the cases (hex/bin/octal long, hex/bin/octal exponent, multiple sign characters, finisher qualifier, etc...)
//        try {
//            d = Double.parseDouble(s);
//        } catch (Exception ignore) {}
//        try {
//            l = Long.parseLong(s);
//        } catch (Exception ignore) {}
//        return new Pair<>(d, l);
//    }

    protected List<Token> tokens = new ArrayList<>();

    public static final Pattern booleanTruePattern = Pattern.compile("^(?:[Tt](?:[Rr][Uu][Ee])?|[Yy](?:[Ee][Ss])?|[Oo][Kk])$");
    public static final Pattern booleanFalsePattern = Pattern.compile("^(?:[Ff](?:[Aa][Ll][Ss][Ee])?|[Nn][Oo]?)$");

    protected void sanitize() {
        int i = -1;
        for (Token t : tokens) {
            ++i;
            if (t.kind == Kind.BLOCK_START) {
                char open = ((Pair<Character, Integer>)t.value).getFirst();
                char close = open == '{' ? '}' : open == '[' ? ']' : ')';
                int subCount = 0;
                for (int j = i + 1; j < tokens.size(); ++j) {
                    Token t2 = tokens.get(j);
                    if (t2.kind == Kind.BLOCK_START && ((Pair<Character, Integer>)t2.value).getFirst() == open)
                        ++subCount;
                    else if (t2.kind == Kind.BLOCK_END && ((Pair<Character, Integer>)t2.value).getFirst() == close) {
                        if (subCount > 0)
                            --subCount;
                        else {
                            t.value = new Pair<>(open, j);
                            t2.value = new Pair<>(close, i);
                            break;
                        }
                    }
                }
                if (subCount > 0) {
                    //TODO: handle dangling start without end
                }
            } else if (t.kind == Kind.BLOCK_END && ((Pair<Character, Integer>)t.value).getSecond() == null) {
                //TODO: handle dangling end without start
            } else if (t.kind == Kind.KEYWORD) {
                if (booleanTruePattern.matcher(t.match).matches()) {
                    t.kind = Kind.BOOLEAN;
                    t.value = true;
                } else if (booleanFalsePattern.matcher(t.match).matches()) {
                    t.kind = Kind.BOOLEAN;
                    t.value = false;
                }
            }
        }
    }

    public Tokenizer tokenize(String input) {
        tokens.clear();
        if (input == null || input.isEmpty()) return this;
        this.input = input.toCharArray();
        TryTokenize[] tokenizers = new TryTokenize[]{
                new QuotedStringTokenizer(),
                new CommentTokenizer(),
                new BlankTokenizer(),
                new NLTokenizer(),
                new PatternTokenizer(Kind.PAIR, pairPattern),
                new PatternTokenizer(Kind.SEP, separatorPattern),
                new PatternTokenizer(Kind.KEYWORD, keywordPattern),
//                new PatternTokenizer(Kind.NUMBER, numberPattern, Tokenizer::convertNumber), //might need a custom tokenizer instead
                new NumberTokenizer(),
                new PatternTokenizer(Kind.BLOCK_START, blockStartPattern, Tokenizer::convertBlock),
                new PatternTokenizer(Kind.BLOCK_END, blockEndPattern, Tokenizer::convertBlock),
        };
        while (!finished()) {
            Token test = null;
            for (TryTokenize t : tokenizers) {
                Token o = t.tryTokenize(this);
                if (o != null && (test == null || test.match.length() < o.match.length()))
                    test = o;
            }
            if (test == null)
                break;
            tokens.add(test);
            consume(test.match.length());
        }
        sanitize();
        return this;
    }

    public List<Token> tokens() { return tokens; }

    public String trailing() { return ""; } //TODO

    @StaticInit(Stage.BLOCK)
    public static void test() {
        String input = "\"'escaped\\\"'\"\n" +
                "'\"\\''''\n" +
                "\"#\"\n" +
                "#comment   \n" +
                "id = \"minecraft:comparator\"\n" +
                "count=1.e1d\n" + //failed: got =1 and .e1d as numbers, should be = pair and 1.e1d as number
                "valid=true  ,  other=false\n" + //failed, =f was parsed as a number, resulting in 'alse' keyword
                "[list, of, things]\n" + //failed, [l was parsed as a number, and so was ]
                "[\"contains# strings\"]\n" + //both [ and ] where parsed as number, string was parsed correctly
                "\"[contains list]\"\n" +
                "[#]"; //parsed as number then comment
        var t = new Tokenizer().tokenize(input).tokens();
        System.out.println("tokens: " + t);
    }
}
