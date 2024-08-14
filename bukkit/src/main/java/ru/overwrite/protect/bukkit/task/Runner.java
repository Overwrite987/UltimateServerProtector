package ru.overwrite.protect.bukkit.task;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Runner {

	void runPlayer(@NotNull Runnable task, @NotNull Player player);

	void run(@NotNull Runnable task);

	void runAsync(@NotNull Runnable task);

	void runDelayed(@NotNull Runnable task, long delayTicks);

	void runDelayedAsync(@NotNull Runnable task, long delayTicks);

	void runPeriodical(@NotNull Runnable task, long delayTicks, long periodTicks);

	void runPeriodicalAsync(@NotNull Runnable task, long delayTicks, long periodTicks);

	void cancelTasks();
}