package com.limachi.arss.utils.parser.tokenizers;

import com.limachi.arss.utils.parser.Tokenizer;

public class CommentTokenizer implements Tokenizer.TryTokenize {
    boolean multiline;
    int skip;
    int l;

    @Override
    public Tokenizer.Token tryTokenize(Tokenizer s) {
        multiline = false;
        skip = 0;
        l = 0;
        char c = s.c();
        if (c == '/' && s.ok(1)) {
            if (s.c(1) == '/')
                l = 2;
            else if (s.c(1) == '*') {
                l = 2;
                multiline = true;
            } else
                return null;
        } else if (c == '#' || c == ';' || c == '@' || c == '?') {
            l = 1;
        } else
            return null;
        skip = l;
        while (s.ok(l)) {
            c = s.c(l);
            ++l;
            if (multiline) {
                if (c == '/' && l > 2 && s.c(l - 1) == '*')
                    return new Tokenizer.Token(Tokenizer.Kind.COMMENT, s.getString(0, l), s.getString(2, l - 4));
            } else if (c == '\n' || c == '\r')
                return new Tokenizer.Token(Tokenizer.Kind.COMMENT, s.getString(0, l), s.getString(1, l - 2));
        }
        if (multiline)
            return null; //missing closing pair '*','/'
        else
            return new Tokenizer.Token(Tokenizer.Kind.COMMENT, s.getString(0, l), s.getString(1, l - 1));
    }
}
