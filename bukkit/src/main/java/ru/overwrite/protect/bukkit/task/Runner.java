package ru.overwrite.protect.bukkit.task;

import org.bukkit.entity.Player;

public interface Runner {
	void runPlayer(Runnable task, Player player);

	void run(Runnable task);

	void runAsync(Runnable task);

	void runDelayed(Runnable task, long delayTicks);

	void runDelayedAsync(Runnable task, long delayTicks);

	void runPeriodical(Runnable task, long delayTicks, long periodTicks);

	void runPeriodicalAsync(Runnable task, long delayTicks, long periodTicks);

	void cancelTasks();
}
