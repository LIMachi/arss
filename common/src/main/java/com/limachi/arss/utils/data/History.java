package com.limachi.arss.utils.data;

import net.minecraft.util.Mth;

import java.util.ArrayList;

/**
 * simple stack like structure used to simulate a modification history
 * @param <T> any of your type you'd like to store
 */
public class History<T> {
    protected int page = 0;
    protected final ArrayList<T> stack = new ArrayList<>();
    protected T quickAccessState;

    public History(T initialState) {
        stack.add(initialState);
        quickAccessState = initialState;
    }

    /**
     * write a new history page, discarding any "more recent" pages
     * @param state to store
     */
    public void write(T state) {
        while (stack.size() > page + 1)
            stack.remove(stack.size() - 1);
        stack.add(state);
        quickAccessState = state;
        ++page;
    }

    /**
     * read a history page and move the page cursor to it
     * @param back can also be 0 or negative, 0 return the current page (as quickly as possible) and a negative will jump forward in the history (clamped)
     * @return the state that was stored
     */
    public T read(int back) {
        if (back == 0) return quickAccessState;
        page = Mth.clamp(page - back, 0, stack.size() - 1);
        quickAccessState = stack.get(page);
        return quickAccessState;
    }
}