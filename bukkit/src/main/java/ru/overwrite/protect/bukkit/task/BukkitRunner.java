package ru.overwrite.protect.bukkit.task;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BukkitRunner implements Runner {

	private final Plugin plugin;
	private final BukkitScheduler scheduler;

	public BukkitRunner(Plugin plugin) {
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
        scheduler.cancelTasks(plugin);
    }
}
