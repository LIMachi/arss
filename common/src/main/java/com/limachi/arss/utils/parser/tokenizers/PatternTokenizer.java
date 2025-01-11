package com.limachi.arss.utils.parser.tokenizers;

import com.limachi.arss.utils.parser.Tokenizer;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTokenizer implements Tokenizer.TryTokenize {

    final Pattern pattern;
    final Tokenizer.Kind kind;
    final Function<String, Object> transformer;

    public static final Function<String, Object> defaultTransformer = s->s;

    public PatternTokenizer(Tokenizer.Kind kind, String pattern) { this(kind, Pattern.compile(pattern), defaultTransformer); }
    public PatternTokenizer(Tokenizer.Kind kind, String pattern, Function<String, Object> transformer) { this(kind, Pattern.compile(pattern), transformer); }
    public PatternTokenizer(Tokenizer.Kind kind, Pattern pattern) { this(kind, pattern, defaultTransformer); }
    public PatternTokenizer(Tokenizer.Kind kind, Pattern pattern, Function<String, Object> transformer) {
        this.kind = kind;
        this.pattern = pattern;
        this.transformer = transformer;
    }

    @Override
    public Tokenizer.Token tryTokenize(Tokenizer state) {
        Matcher m = pattern.matcher(state.getString(0, -1));
        if (m.find() && m.start() == 0 && m.end() > 0) {
            String out = state.getString(0, m.end());
            return new Tokenizer.Token(kind, out, transformer.apply(out));
        }
        return null;
    }
}
