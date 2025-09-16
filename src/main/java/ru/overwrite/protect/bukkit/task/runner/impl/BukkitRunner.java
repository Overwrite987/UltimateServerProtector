package ru.overwrite.protect.bukkit.task.runner.impl;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.task.runner.Runner;

@SuppressWarnings("deprecation")
public final class BukkitRunner implements Runner {

    private final ServerProtectorManager plugin;
    private final BukkitScheduler scheduler;

    public BukkitRunner(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void runPlayer(@NotNull Runnable task, @NotNull Player player) {
        run(task);
    }

    @Override
    public void run(@NotNull Runnable task) {
        scheduler.runTask(plugin, task);
    }

    @Override
    public void runAsync(@NotNull Runnable task) {
        scheduler.runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runDelayed(@NotNull Runnable task, long delayTicks) {
        scheduler.runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runDelayedAsync(@NotNull Runnable task, long delayTicks) {
        scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    @Override
    public void runPeriodical(@NotNull Runnable task, long delayTicks, long periodTicks) {
        scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public void runPeriodicalAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public void cancelTasks() {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        scheduler.cancelTasks(plugin);
    }
}
