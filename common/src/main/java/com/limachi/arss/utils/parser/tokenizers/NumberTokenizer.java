package com.limachi.arss.utils.parser.tokenizers;

import com.limachi.arss.utils.parser.Tokenizer;
import com.mojang.datafixers.util.Pair;

public class NumberTokenizer implements Tokenizer.TryTokenize {
    enum Base {
        BINARY(2),
        OCTAL(8),
        DECIMAL(10),
        HEXADECIMAL(16);

        final long exponent;

        Base(long exponent) { this.exponent = exponent; }

        long getNum(char num) {
            for (int i = 0; i < exponent; ++i) {
                if (i > 9) {
                    if (num == 'A' + i || num == 'a' + i)
                        return i;
                } else if (num == '0' + i)
                    return i;
            }
            return -1;
        }

        long getExponent() { return exponent; }
    }

    int extracted;

    private long unsignedLongExtractor(Tokenizer s, int skip, Base base) {
        long result = 0;
        extracted = 0;
        while (s.ok(skip + extracted)) {
            char c = s.c(skip + extracted);
            if (extracted > 0 && c == '_') {
                ++extracted;
                continue;
            }
            long n = base.getNum(c);
            if (n != -1)
                result = result * base.exponent + n;
            else
                break;
            ++extracted;
        }
        if (extracted > 0 && s.c(skip + extracted) == '_')
            --extracted;
        return result;
    }

    private boolean signExtractor(Tokenizer s, int skip) {
        boolean negative = false;
        extracted = 0;
        while (s.ok(skip + extracted)) {
            char c = s.c(skip + extracted);
            if (c == '-')
                negative = !negative;
            else if (c != '+')
                break;
            ++extracted;
        }
        return negative;
    }

    private Base baseExtractor(Tokenizer s, int skip) {
        extracted = 0;
        char c = s.c(skip);
        if (c == '0') {
            Base base = null;
            char n = s.c(skip + 1);
            if (n == 'b' || n == 'B')
                base = Base.BINARY;
            else if (n == 'o' || n == 'O')
                base = Base.OCTAL;
            else if (n == 'd' || n == 'D')
                base = Base.DECIMAL;
            else if (n == 'x' || n == 'X')
                base = Base.HEXADECIMAL;
            if (base != null) {
                extracted = 2;
                return base;
            } else
                return Base.OCTAL;
        }
        return Base.DECIMAL;
    }

    enum State {
        NAN,
        SIGN,
        INF,
        BASE,
        LONG,
        THOUSANDS_OR_DOT_OR_E,
        FRACT,
        EXP,
        QUALIFIER
    }

    public static long K_DECIMAL = 1000L;
    public static long K_BINARY = 1024L;

    @Override
    public Tokenizer.Token tryTokenize(Tokenizer s) {
        int l = 0;
        Long i = null;
        Double f = null;
        boolean negative = false;
        Base base = null;
        int consumedBase = 0;
        State state = State.NAN;
        while (s.ok(l)) {
            char c = s.c(l);
            switch (state) {
                case NAN -> {
                    if (c == 'N' || c == 'n') {
                        char c1 = s.c(1);
                        char c2 = s.c(2);
                        if ((c1 == 'A' || c1 == 'a') && (c2 == 'N' || c2 == 'n'))
                            return new Tokenizer.Token(Tokenizer.Kind.NUMBER, s.getString(0, l + 2), new Pair<>(Double.NaN, 0l));
                        return null; //got n or na but not nan
                    } else
                        state = State.SIGN;
                }
                case SIGN -> {
                    negative = signExtractor(s, l);
                    l += extracted;
                    state = State.INF;
                }
                case INF -> {
                    if (c == 'I' || c == 'i') {
                        char c1 = s.c(l + 1);
                        char c2 = s.c(l + 2);
                        if ((c1 == 'n' || c1 == 'N') && (c2 == 'f' || c2 == 'F'))
                            return new Tokenizer.Token(Tokenizer.Kind.NUMBER, s.getString(0, l + 2), new Pair<>(negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY, 0l));
                        return null; //got i or in but not inf
                    } else
                        state = State.BASE;
                }
                case BASE -> {
                    base = baseExtractor(s, l);
                    if (base == null)
                        return null; //invalid character after or before base
                    l += extracted;
                    consumedBase = extracted;
                    state = State.LONG;
                }
                case LONG -> {
                    i = unsignedLongExtractor(s, l, base);
                    if (extracted == 0 && consumedBase == 2)
                        return null; //base should be followed with a valid number, even if the . is present
                    if (extracted >= 1 && i == 0 && base == Base.OCTAL && consumedBase != 2)
                        base = Base.DECIMAL;
                    l += extracted;
                    state = State.THOUSANDS_OR_DOT_OR_E;
                }
                //FIXME: missing case: no base nor long before . or e
                case THOUSANDS_OR_DOT_OR_E -> {
                    if ("kKmMgGtTpP".indexOf(c) != -1) {
                        ++l;
                        if (c == 'k' || c == 'K')
                            i *= base == Base.DECIMAL ? K_DECIMAL : K_BINARY;
                        else if (c == 'm' || c == 'M')
                            i *= base == Base.DECIMAL ? K_DECIMAL * K_DECIMAL : K_BINARY * K_BINARY;
                        else if (c == 'g' || c == 'G')
                            i *= base == Base.DECIMAL ? K_DECIMAL * K_DECIMAL * K_DECIMAL : K_BINARY * K_BINARY * K_BINARY;
                        else if (c == 't' || c == 'T')
                            i *= base == Base.DECIMAL ? K_DECIMAL * K_DECIMAL * K_DECIMAL * K_DECIMAL : K_BINARY * K_BINARY * K_BINARY * K_BINARY;
                        else if (c == 'p' || c == 'P')
                            i *= base == Base.DECIMAL ? K_DECIMAL * K_DECIMAL * K_DECIMAL * K_DECIMAL * K_DECIMAL : K_BINARY * K_BINARY * K_BINARY * K_BINARY * K_BINARY;
                        state = State.QUALIFIER;
                    } else if (c == '.') {
                        ++l;
                        state = State.FRACT;
                    } else if (c == 'e') {
                        ++l;
                        state = State.EXP;
                    } else
                        state = State.QUALIFIER;
                }
                case FRACT -> {
                    f = (double)i;
                    long fract = unsignedLongExtractor(s, l, base);
                    l += extracted;
                    if (extracted > 0) {
                        double df = (double)fract;
                        for (int t = 0; t < extracted; ++t)
                            df /= (double)base.exponent;
                        f += df;
                    }
                    if (s.c(l) == 'e') {
                        ++l;
                        state = State.EXP;
                    } else
                        state = State.QUALIFIER;
                }
                case EXP -> {
                    boolean neg = signExtractor(s, l);
                    l += extracted;

                    Base baseExp = baseExtractor(s, l);
                    if (baseExp == null)
                        return null; //invalid character after or before base
                    l += extracted;
                    consumedBase = extracted;

                    long exp = unsignedLongExtractor(s, l, base);
                    if (extracted == 0 && consumedBase != 1)
                        return null; //base should be followed with a valid number
                    l += extracted;

                    if (f == null)
                        f = (double)i;
                    while (exp > 0) {
                        --exp;
                        if (neg) {
                            f /= (double)base.exponent;
                            i /= base.exponent;
                        }
                        else {
                            f *= (double)base.exponent;
                            i *= base.exponent;
                        }
                    }
                    state = State.QUALIFIER;
                }
                case QUALIFIER -> {
                    ++l;
                    if (f == null)
                        f = (double)i;
                    if (c == 'f' || c == 'F') {
                        f = (double)(float)(double)f;
                    } else if (c == 'd' || c == 'D') {
                        ;
                    } else if (c == 'b' || c == 'B')
                        i = (long)(byte)(long)i;
                    else if (c == 's' || c == 'S')
                        i = (long)(short)(long)i;
                    else if (c == 'i' || c == 'I')
                        i = (long)(int)(long)i;
                    else if (c == 'l' || c == 'L')
                        ;
                    else
                        --l;
                    return new Tokenizer.Token(Tokenizer.Kind.NUMBER, s.getString(0, l), new Pair<>(f, i));
                }
            }
        }
        return null;
    }
}
