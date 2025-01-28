package com.limachi.arss.utils;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SingleRunnableReloadListener implements PreparableReloadListener {
    private final Runnable run;

    public SingleRunnableReloadListener(Runnable run) {
        this.run = run != null ? run : ()->{};
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager i0, ProfilerFiller i1, ProfilerFiller i2, Executor executor, Executor i3) {
        return CompletableFuture.runAsync(run, executor).thenCompose(barrier::wait);
    }
}
