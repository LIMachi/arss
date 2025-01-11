package com.limachi.arss.utils.parser.tokenizers;

import com.limachi.arss.utils.parser.Tokenizer;

public class QuotedStringTokenizer implements Tokenizer.TryTokenize {
    protected boolean escape;
    char globe;
    String result;
    int l;

    @Override
    public Tokenizer.Token tryTokenize(Tokenizer s) {
        globe = 0;
        result = "";
        escape = false;
        l = 0;
        if (s.c(0) == '"' || s.c(0) == '\'') {
            globe = s.c(0);
            l = 1;
            while (s.ok(l)) {
                char c = s.c(l++);
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
                        return new Tokenizer.Token(Tokenizer.Kind.STRING, s.getString(0, l), result);
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
        return null;
    }
}
