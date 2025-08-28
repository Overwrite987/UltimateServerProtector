package ru.overwrite.protect.bukkit.task.runner.impl;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.task.runner.Runner;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class PaperRunner implements Runner {

    private final ServerProtectorManager plugin;
    private final AsyncScheduler asyncScheduler;
    private final GlobalRegionScheduler globalScheduler;

    public PaperRunner(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
        this.globalScheduler = plugin.getServer().getGlobalRegionScheduler();
    }

    @Override
    public void runPlayer(@NotNull Runnable task, @NotNull Player player) {
        player.getScheduler().run(plugin, toConsumer(task), null);
    }

    @Override
    public void run(@NotNull Runnable task) {
        globalScheduler.run(plugin, toConsumer(task));
    }

    @Override
    public void runAsync(@NotNull Runnable task) {
        asyncScheduler.runNow(plugin, toConsumer(task));
    }

    @Override
    public void runDelayed(@NotNull Runnable task, long delayTicks) {
        globalScheduler.runDelayed(plugin, toConsumer(task), delayTicks);
    }

    @Override
    public void runDelayedAsync(@NotNull Runnable task, long delayTicks) {
        asyncScheduler.runDelayed(plugin, toConsumer(task), toMilli(delayTicks), TimeUnit.MILLISECONDS);
    }

    @Override
    public void runPeriodical(@NotNull Runnable task, long delayTicks, long periodTicks) {
        globalScheduler.runAtFixedRate(plugin, toConsumer(task), delayTicks, periodTicks);
    }

    @Override
    public void runPeriodicalAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        asyncScheduler.runAtFixedRate(plugin, toConsumer(task), toMilli(delayTicks), toMilli(periodTicks), TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelTasks() {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        globalScheduler.cancelTasks(plugin);
        asyncScheduler.cancelTasks(plugin);
    }

    private static Consumer<ScheduledTask> toConsumer(Runnable task) {
        return st -> task.run();
    }

    private static long toMilli(long ticks) {
        return ticks * 50L;
    }
}
