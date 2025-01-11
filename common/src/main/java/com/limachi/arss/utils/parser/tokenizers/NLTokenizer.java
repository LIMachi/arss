package com.limachi.arss.utils.parser.tokenizers;

import com.limachi.arss.utils.parser.Tokenizer;

public class NLTokenizer implements Tokenizer.TryTokenize {

    int l;

    @Override
    public Tokenizer.Token tryTokenize(Tokenizer s) {
        l = 0;
        while (s.ok(l)) {
            int t = Character.getType(s.c(l));
            if (t != Character.LINE_SEPARATOR && t != Character.PARAGRAPH_SEPARATOR && t != Character.CONTROL)
                break;
            if (t == Character.CONTROL) {
                char c = s.c(l);
                if (c != '\n' && c != '\r' && c != '\u0085' && c != '\u000B' && c != '\f')
                    break;
            }
            ++l;
        }
        if (l > 0) {
            String out = s.getString(0, l);
            return new Tokenizer.Token(Tokenizer.Kind.NL, out, out);
        }
        return null;
    }
}
