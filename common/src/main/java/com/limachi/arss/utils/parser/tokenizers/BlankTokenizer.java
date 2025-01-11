package com.limachi.arss.utils.parser.tokenizers;

import com.limachi.arss.utils.parser.Tokenizer;

public class BlankTokenizer implements Tokenizer.TryTokenize {

    int l;

    @Override
    public Tokenizer.Token tryTokenize(Tokenizer s) {
        l = 0;
        while (s.ok(l) && Character.getType(s.c(l)) == Character.SPACE_SEPARATOR || s.c(l) == '\t')
            ++l;
        if (l > 0) {
            String out = s.getString(0, l);
            return new Tokenizer.Token(Tokenizer.Kind.BLANK, out, out);
        }
        return null;
    }
}
