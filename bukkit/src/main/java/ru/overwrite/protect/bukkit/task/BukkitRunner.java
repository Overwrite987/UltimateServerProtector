package ru.overwrite.protect.bukkit.task;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

@SuppressWarnings("deprecation")
public class BukkitRunner implements Runner {
	private final Plugin plugin;
	private final BukkitScheduler scheduler;

	public BukkitRunner(Plugin plugin) {
		this.plugin = plugin;
		this.scheduler = plugin.getServer().getScheduler();
	}

	@Override
	public void runPlayer(Runnable task, Player player) {
		run(task);
	}

	@Override
	public void run(Runnable task) {
		scheduler.runTask(plugin, task);
	}

	@Override
	public void runAsync(Runnable task) {
		scheduler.runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runDelayed(Runnable task, long delayTicks) {
        scheduler.runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runDelayedAsync(Runnable task, long delayTicks) {
        scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    @Override
    public void runPeriodical(Runnable task, long delayTicks, long periodTicks) {
        scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public void runPeriodicalAsync(Runnable task, long delayTicks, long periodTicks) {
        scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public void cancelTasks() {
        scheduler.cancelTasks(plugin);
    }
}
