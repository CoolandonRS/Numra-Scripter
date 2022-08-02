package net.numra.scripter;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimedCommand {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Scripter plugin;
    private String command;
    private long time;
    private ScheduledFuture<?> future;
    
    
    public void start() {
        future = executor.scheduleAtFixedRate(this::run, time, time, TimeUnit.SECONDS);
        
    }
    
    public void cancel() {
        future.cancel(false);
    }
    
    private void run() {
        Bukkit.getScheduler().runTask(plugin, l -> {
            Server server = plugin.getServer();
            server.dispatchCommand(server.getConsoleSender(), command);
            plugin.getLogger().info("Ran scheduled command: " + command);
        });
    }
    
    public TimedCommand(String commandStr, int time, Scripter plugin) {
        this.plugin = plugin;
        this.command = commandStr;
        this.time = time;
    }
}