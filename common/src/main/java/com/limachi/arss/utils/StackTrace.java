package com.limachi.arss.utils;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class StackTrace {
    Throwable inner;
    String newLine = "\n";

    public StackTrace(Throwable from) {
        inner = from;
    }

    protected void enclosedStackTrace(StringBuilder str, StackTraceElement[] enclosingTrace, String caption, String prefix, Set<Throwable> dejaVu) {
        if (dejaVu.contains(this.inner))
            str.append(prefix).append(caption).append("[CIRCULAR REFERENCE: ").append(this).append(']').append(newLine);
        else {
            dejaVu.add(this.inner);
            StackTraceElement[] trace = inner.getStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >=0 && trace[m].equals(enclosingTrace[n])) {
                m--; n--;
            }
            int framesInCommon = trace.length - 1 - m;

            str.append(prefix).append(caption).append(this).append(newLine);
            for (int i = 0; i <= m; i++)
                str.append(prefix).append("\tat ").append(trace[i]).append(newLine);
            if (framesInCommon != 0)
                str.append(prefix).append("\t... ").append(framesInCommon).append(" more").append(newLine);

            for (Throwable se : inner.getSuppressed())
                new StackTrace(se).enclosedStackTrace(str, trace, "Suppressed: ", prefix + "\t", dejaVu);

            Throwable ourCause = inner.getCause();
            if (ourCause != null)
                new StackTrace(ourCause).enclosedStackTrace(str, trace, "Caused by: ", prefix, dejaVu);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>());
        dejaVu.add(inner);

        str.append(inner).append(newLine);
        StackTraceElement[] trace = inner.getStackTrace();
        for (StackTraceElement traceElement : trace)
            str.append("\tat ").append(traceElement).append(newLine);

        for (Throwable se : inner.getSuppressed())
            new StackTrace(se).enclosedStackTrace(str, trace, "Suppressed: ", "\t", dejaVu);

        Throwable ourCause = inner.getCause();
        if (ourCause != null)
            new StackTrace(ourCause).enclosedStackTrace(str, trace, "Caused by: ", "", dejaVu);
        return str.toString();
    }
}
